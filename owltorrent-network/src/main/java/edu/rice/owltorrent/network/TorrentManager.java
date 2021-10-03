package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.adapters.NetworkToStorageAdapter;
import edu.rice.owltorrent.common.entity.FileBlockInfo;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.network.messages.PayloadlessMessage;
import edu.rice.owltorrent.network.messages.PieceActionMessage;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
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
  private final Queue<Integer> notStartedPieces;

  private final int totalPieces;

  private final Map<Peer, PeerConnector> peers;
  private final NetworkToStorageAdapter networkStorageAdapter;
  @Getter private final Torrent torrent;

  public TorrentManager(Torrent file, NetworkToStorageAdapter adapter) {
    this.torrent = file;
    this.networkStorageAdapter = adapter;
    this.completedPieces = Collections.newSetFromMap(new ConcurrentHashMap<>());
    this.uncompletedPieces = new ConcurrentHashMap<>();
    this.notStartedPieces = new ConcurrentLinkedQueue<>();
    this.totalPieces = torrent.getPieces().size();

    for (int idx = 0; idx < totalPieces; idx++) {
      this.notStartedPieces.add(idx);
    }

    this.peers = new ConcurrentHashMap<>();

    // initPeers();
  }

  private void initPeers(List<Peer> peerList) {
    // TODO: potentially make async?
    for (Peer peer : peerList) {
      try {
        PeerConnector connector =
            SocketConnector.makeInitialConnection(peer, this, networkStorageAdapter);
        addPeer(connector, peer);
        // TODO: revise
        connector.writeMessage(new PayloadlessMessage(PeerMessage.MessageType.INTERESTED));
      } catch (IOException e) {
        log.error(String.format("Error connecting peer id=%s", peer.getPeerID().toString()));
      }
    }
  }

  public void startDownloadingAsynchronously() {
    // TODO: make thread pool scheduler
    new Thread(this).start();
  }

  public float getProgressPercent() {
    // TODO: report completed blocks instead?
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

  private void requestBlockFromPeer(Peer peer, PieceStatus pieceStatus, int blockIndex) {
    PeerConnector peerConnector = peers.get(peer);
    if (peerConnector == null) {
      return;
    }

    // TODO: fix int casting.
    try {
      int actualBlockSize = blockIndex * pieceStatus.blockLength;
      if (pieceStatus.pieceIndex == totalPieces - 1
          && blockIndex == pieceStatus.status.size() - 1) {
        actualBlockSize = ((int) torrent.getLastPieceLength()) % pieceStatus.blockLength;
      }
      peerConnector.writeMessage(
          PieceActionMessage.makeRequestMessage(
              pieceStatus.pieceIndex, blockIndex * pieceStatus.blockLength, actualBlockSize));
    } catch (IOException e) {
      log.error(e);
    }
  }

  @Override
  public void run() {
    while (!(uncompletedPieces.isEmpty() && notStartedPieces.isEmpty())) {
      // Request a missing piece from each Peer
      List<Peer> connections = new ArrayList<>(peers.keySet());
      Collections.shuffle(connections);

      for (PieceStatus progress : uncompletedPieces.values()) {
        for (int i = 0; i < progress.status.size(); i++) {
          if (connections.isEmpty()) break;

          if (progress.status.get(i).get() != BLOCK_NOT_STARTED) {
            continue;
          }

          requestBlockFromPeer(connections.remove(0), progress, i);
        }
        if (connections.isEmpty()) break;
      }
      while (!connections.isEmpty() && !notStartedPieces.isEmpty()) {
        int notStartedIndex = notStartedPieces.remove();
        PieceStatus newPieceStatus = makeNewPieceStatus(notStartedIndex);
        uncompletedPieces.put(notStartedIndex, newPieceStatus);
        requestBlockFromPeer(connections.remove(0), newPieceStatus, notStartedIndex);
      }

      // Thread.sleep(1000);
    }
  }

  private PieceStatus makeNewPieceStatus(int pieceIndex) {
    // TODO: fix int casting, and also add handling when piece length isn't a multiple of
    //  default block num
    if (((int) torrent.getPieceLength()) % DEFAULT_BLOCK_NUM != 0) {
      log.error("Piece length not divisible by block num");
      throw new IllegalStateException("Piece length not divisible by block num");
    }
    return new PieceStatus(
        pieceIndex, DEFAULT_BLOCK_NUM, (int) torrent.getPieceLength() / DEFAULT_BLOCK_NUM);
  }

  private PieceStatus getOrInitPieceStatus(int pieceIndex) {
    return uncompletedPieces.computeIfAbsent(
        pieceIndex,
        i -> {
          // Note remove is expensive. Prefer
          if (!notStartedPieces.remove(i)) {
            log.error("uncompleted piece missing from Map");
            throw new IllegalStateException("uncompleted piece missing from Map");
          }
          return makeNewPieceStatus(i);
        });
  }

  /**
   * validates and reports the progress of a block
   *
   * @param blockInfo block info
   * @return false if another thread already started to download that block or the block size is not
   *     expected
   */
  public boolean validateAndReportBlockInProgress(FileBlockInfo blockInfo) {
    if (completedPieces.contains(blockInfo.getPieceIndex())) {
      log.debug("block already finished downloading");
      return false;
    }

    PieceStatus status = getOrInitPieceStatus(blockInfo.getPieceIndex());

    if (status.blockLength != blockInfo.getLength()) {
      return false;
    }

    return status
        .status
        .get(blockInfo.getOffsetWithinPiece() / status.blockLength)
        .compareAndSet(BLOCK_NOT_STARTED, BLOCK_IN_PROGRESS);
  }

  public void reportBlockCompletion(FileBlockInfo blockInfo) {
    PieceStatus status = uncompletedPieces.get(blockInfo.getPieceIndex());
    if (status == null) {
      log.error("Block missing from uncompletedPieces: " + blockInfo);
      throw new IllegalStateException("Block missing from uncompletedPieces: " + blockInfo);
    }

    if (status
        .status
        .get(blockInfo.getOffsetWithinPiece() / status.blockLength)
        .compareAndSet(BLOCK_IN_PROGRESS, BLOCK_DONE)) {
      for (AtomicInteger blockStatus : status.status) {
        if (blockStatus.get() != BLOCK_DONE) {
          // return if any block is not done
          return;
        }
      }
      // TODO: add piece hash verification here
      // if all blocks are done, move the piece to the completed set.
      uncompletedPieces.remove(blockInfo.getPieceIndex());
      completedPieces.add(blockInfo.getPieceIndex());
    }
  }

  /** Piece Status class used to keep track of how much each piece is downloaded */
  static final class PieceStatus {
    final int pieceIndex;
    // Should be fine to use Atomic Integer for now. Since we don't expect a lot of concurrency on
    // the same piece
    final List<AtomicInteger> status;
    final int blockLength;

    PieceStatus(int pieceIndex, int blockNum, int blockLength) {
      this.pieceIndex = pieceIndex;
      List<AtomicInteger> status = new ArrayList<>();
      for (int i = 0; i < blockNum; i++) {
        status.add(new AtomicInteger(BLOCK_NOT_STARTED));
      }
      this.status = Collections.unmodifiableList(status);
      this.blockLength = blockLength;
    }
  }
}
