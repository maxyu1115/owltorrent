package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.adapters.NetworkToStorageAdapter;
import edu.rice.owltorrent.common.entity.Bitfield;
import edu.rice.owltorrent.common.entity.FileBlockInfo;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.util.AtomicHashableInteger;
import edu.rice.owltorrent.network.messages.PieceActionMessage;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * Manager that takes care of everything related to a Torrent. The TorrentManager will manage the
 * peer connections and ask for missing file pieces.
 *
 * @author Lorraine Lyu, Max Yu
 */
@Log4j2(topic = "general")
public class TorrentManager implements Runnable, AutoCloseable {

  private static final int DEFAULT_BLOCK_NUM = 2;

  private static final int BLOCK_NOT_STARTED = 0;
  private static final int BLOCK_IN_PROGRESS = 1;
  private static final int BLOCK_DONE = 2;

  private final Set<Integer> completedPieces;
  private final Map<Integer, PieceStatus> uncompletedPieces;

  private final int totalPieces;

  private final Map<Peer, PeerConnector> peers;
  private final NetworkToStorageAdapter networkStorageAdapter;
  @Getter private final Torrent torrent;

  public TorrentManager(Torrent file, List<Peer> peerList, NetworkToStorageAdapter adapter) {
    this.torrent = file;
    this.networkStorageAdapter = adapter;
    this.completedPieces = Collections.newSetFromMap(new ConcurrentHashMap<>());
    this.uncompletedPieces = new ConcurrentHashMap<>();
    this.totalPieces = torrent.getPieces().size();

    for (int idx = 0; idx < totalPieces; idx++) {
      // TODO: fix int casting, and also add handling when piece length isn't a multiple of default
      //  block num
      this.uncompletedPieces.put(
          idx,
          new PieceStatus(
              idx, DEFAULT_BLOCK_NUM, (int) torrent.getPieceLength() / DEFAULT_BLOCK_NUM));
    }

    this.peers = new ConcurrentHashMap<>();

    for (Peer peer : peerList) {
      try {
        addPeer(SocketConnector.makeInitialConnection(peer, networkStorageAdapter), peer);
      } catch (IOException e) {
        log.error(String.format("Error connecting peer id=%s", peer.getPeerID().toString()));
      }
    }
  }

  public void startDownloadingAsynchronously() {
    // Start thread that calls checkStatus once every x seconds
    // TODO: make thread pool scheduler
    new Thread(this).start();
  }

  public float getProgressPercent() {
    return completedPieces.size() * 1.0f / totalPieces;
  }

  // For handshake listener
  // Later if get updated peerlist from tracker.
  public void addPeer(PeerConnector connector, Peer peer) {
    this.peers.put(peer, connector);
  }

  @Override
  public void close() throws Exception {
    for (var pair : peers.entrySet()) {
      pair.getValue().close();
    }
  }

  // Can come up with better names, basically figures out what messages need to be sent to what
  // peers
  private void checkStatus() {}

  private Bitfield getBitField() {
    
  }

  private void requestPieceFromPeer(Peer peer, int pieceNum, int blockNum, int blockSize) {
    PeerConnector peerConnector = peers.get(peer);
    if (peerConnector == null) {
      return;
    }

    // TODO: fix int casting.
    try {
      peerConnector.writeMessage(
          PieceActionMessage.makeRequestMessage(pieceNum, blockNum * blockSize, blockSize));
    } catch (IOException e) {
      log.error(e);
    }
  }

  @Override
  public void run() {
    while (!uncompletedPieces.isEmpty()) {
      // Request a missing piece from each Peer
      List<Peer> connections = new ArrayList<>(peers.keySet());
      Collections.shuffle(connections);

      for (PieceStatus progress : uncompletedPieces.values()) {
        for (int i = 0; i < progress.status.size(); i++) {
          if (connections.isEmpty()) break;

          if (progress.status.get(i).get() != BLOCK_NOT_STARTED) {
            continue;
          }

          requestPieceFromPeer(connections.remove(0), progress.pieceIndex, i, progress.blockLength);
        }
        if (connections.isEmpty()) break;
      }
      // Thread.sleep(1000);
    }
  }
  // TODO: Not compatable with current setup
  // private sendPieceToPeer(PeerConnector peer, int pieceNum);
  // private sendHaveMessageToPeer(PeerConnector peer);

  public boolean reportPieceInProgress(FileBlockInfo blockInfo) {
    PieceStatus status = uncompletedPieces.get(blockInfo.getIndex());
    assert status != null;

    return status
        .status
        .get(blockInfo.getBegin() / status.blockLength)
        .compareAndSet(BLOCK_NOT_STARTED, BLOCK_IN_PROGRESS);
  }

  public void reportPieceCompletion(FileBlockInfo blockInfo) {
    PieceStatus status = uncompletedPieces.get(blockInfo.getIndex());
    assert status != null;

    if (status
        .status
        .get(blockInfo.getBegin() / status.blockLength)
        .compareAndSet(BLOCK_IN_PROGRESS, BLOCK_DONE)) {
      for (AtomicInteger blockStatus : status.status) {
        if (blockStatus.get() != BLOCK_DONE) {
          // return if any block is not done
          return;
        }
      }
      // if all blocks are done, move the piece to the completed set.
      uncompletedPieces.remove(blockInfo.getIndex());
      completedPieces.add(blockInfo.getIndex());
    }
  }

  /** Piece Status class used to keep track of how much each piece is downloaded */
  @EqualsAndHashCode
  static final class PieceStatus {
    final int pieceIndex;
    // Should be fine to use Atomic Boolean for now. Since we don't expect a lot of concurrency on
    // the same
    final List<AtomicHashableInteger> status;
    final int blockLength;

    PieceStatus(int pieceIndex, int blockNum, int blockLength) {
      this.pieceIndex = pieceIndex;
      List<AtomicHashableInteger> status = new ArrayList<>();
      for (int i = 0; i < blockNum; i++) {
        status.add(new AtomicHashableInteger(0));
      }
      this.status = Collections.unmodifiableList(status);
      this.blockLength = blockLength;
    }
  }
}
