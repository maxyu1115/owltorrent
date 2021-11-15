package edu.rice.owltorrent.network;

import com.google.common.math.IntMath;
import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.Bitfield;
import edu.rice.owltorrent.common.entity.FileBlockInfo;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.entity.TorrentContext;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import edu.rice.owltorrent.common.util.Exceptions;
import edu.rice.owltorrent.network.messages.BitfieldMessage;
import edu.rice.owltorrent.network.messages.PayloadlessMessage;
import edu.rice.owltorrent.network.messages.PieceActionMessage;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Manager that takes care of everything related to a Torrent. The TorrentManager will manage the
 * peer connections and ask for missing file pieces.
 *
 * @author Lorraine Lyu, Max Yu
 */
@Log4j2(topic = "network")
public class TorrentManager implements Runnable, AutoCloseable {
  @RequiredArgsConstructor
  private static class PeerConnectionContext {
    final PeerConnector peerConnector;
    final AtomicBoolean waitingForRequest = new AtomicBoolean(false);
  }

  private static final int DEFAULT_BLOCK_NUM = 2;

  private static final int BLOCK_NOT_STARTED = 0;
  private static final int BLOCK_IN_PROGRESS = 1;
  private static final int BLOCK_DONE = 2;

  private final Set<Integer> completedPieces = Collections.newSetFromMap(new ConcurrentHashMap<>());
  private final Map<Integer, PieceStatus> uncompletedPieces = new ConcurrentHashMap<>();
  private final Queue<Integer> notStartedPieces = new ConcurrentLinkedQueue<>();

  private final int totalPieces;

  private final TorrentContext torrentContext;
  @Getter private final TwentyByteId ourPeerId;
  private final Map<Peer, PeerConnectionContext> peers = new ConcurrentHashMap<>();
  private final Set<Peer> seeders = Collections.newSetFromMap(new ConcurrentHashMap<>());

  private final StorageAdapter networkStorageAdapter;
  @Getter private final Torrent torrent;

  private TorrentManager(TorrentContext torrentContext, StorageAdapter adapter) {
    this.torrentContext = torrentContext;
    this.ourPeerId = torrentContext.getOurPeerId();
    this.torrent = torrentContext.getTorrent();
    this.networkStorageAdapter = adapter;
    this.totalPieces = torrent.getPieceHashes().size();
  }

  public static TorrentManager makeSeeder(TorrentContext torrentContext, StorageAdapter adapter) {
    TorrentManager manager = new TorrentManager(torrentContext, adapter);
    for (int idx = 0; idx < manager.totalPieces; idx++) {
      manager.completedPieces.add(idx);
    }
    manager.announce(torrentContext.getTorrent().getTotalLength(), 0, 0, Event.STARTED);
    log.info("Started seeding torrent {}", torrentContext.getTorrent());
    return manager;
  }

  public static TorrentManager makeDownloader(
      TorrentContext torrentContext, StorageAdapter adapter) {
    TorrentManager manager = new TorrentManager(torrentContext, adapter);
    for (int idx = 0; idx < manager.totalPieces; idx++) {
      manager.notStartedPieces.add(idx);
    }
    manager.initPeers(
        manager.announce(0, torrentContext.getTorrent().getTotalLength(), 0, Event.STARTED));
    //    manager.initPeers(
    //        List.of(new Peer(new InetSocketAddress("168.5.37.50", 6881), manager.torrent)));
    log.info("Started downloading torrent {}", torrentContext.getTorrent());
    return manager;
  }

  /**
   * @param downloaded bytes already downloaded
   * @param left bytes still left to download
   * @param uploaded bytes already uploaded
   * @param event what kind of announce are we doing
   * @return list of Peers we retrieved from Tracker
   */
  private List<Peer> announce(long downloaded, long left, long uploaded, Event event) {
    PeerLocator locator = new MultipleTrackerConnector();
    return locator.locatePeers(torrentContext, downloaded, left, uploaded, event);
  }

  private void initPeers(List<Peer> peerList) {
    for (Peer peer : peerList) {
      if (peers.containsKey(peer)) {
        continue;
      }
      // TODO: refactor this when adding the thread pool...
      new Thread(
              () -> {
                try {
                  PeerConnector connector =
                      SocketConnector.makeInitialConnection(peer, this, networkStorageAdapter);
                  connector.initiateConnection();
                  addPeer(connector, peer);
                  // TODO: revise
                  connector.writeMessage(
                      new PayloadlessMessage(PeerMessage.MessageType.INTERESTED));
                } catch (IOException e) {
                  e.printStackTrace(System.err);
                  log.error("Error connecting peer {}", peer.getAddress());
                }
              })
          .start();
    }
  }

  public void startDownloadingAsynchronously() {
    // TODO: make thread pool scheduler
    new Thread(this).start();
  }

  public float getProgressPercent() {
    return (completedPieces.size() * 1.0f) / totalPieces;
  }

  /**
   * Adds an already connected peer to the torrent manager for it to be managed.
   *
   * @param connector a peer connector that already established a connection
   * @param peer the peer we're adding
   */
  public void addPeer(PeerConnector connector, Peer peer) {
    try {
      // If peer already added, close the new connection
      if (peers.putIfAbsent(peer, new PeerConnectionContext(connector)) != null) {
        connector.close();
      } else {
        // finish setting up the connection
        connector.setStorageAdapter(networkStorageAdapter);
        connector.writeMessage(new BitfieldMessage(buildBitfield()));
        log.info("Added peer {}", peer.getPeerID());
      }
    } catch (Exception exception) {
      log.error(exception);
    }
  }

  public void removePeer(Peer peer) {
    peers.remove(peer);
    seeders.remove(peer);
  }

  /**
   * Declares that the specified peer is a seeder
   *
   * @param peer already connected peer
   */
  public void declareSeeder(Peer peer) {
    if (peers.containsKey(peer)) {
      seeders.add(peer);
    }
  }

  @Override
  public void close() throws Exception {
    // Announce that we're dropping off
    announce(0, 0, 0, Event.STOPPED);
    for (var pair : peers.entrySet()) {
      pair.getValue().peerConnector.close();
    }
    // TODO: Unregister from TorrentRepository
  }

  private void requestBlockFromPeer(Peer peer, PieceStatus pieceStatus, int blockIndex) {
    PeerConnectionContext peerContext = peers.get(peer);
    PeerConnector peerConnector = peerContext.peerConnector;

    if (peerConnector == null) {
      return;
    }

    // TODO: fix int casting.
    try {
      int actualBlockSize = pieceStatus.blockLength;
      if (isLastBlock(pieceStatus, blockIndex)) {
        actualBlockSize = ((int) torrent.getLastPieceLength()) % pieceStatus.blockLength;
      }
      // Only write request when not waiting
      if (peerContext.waitingForRequest.compareAndSet(false, true)) {
        peerConnector.writeMessage(
            PieceActionMessage.makeRequestMessage(
                pieceStatus.pieceIndex, blockIndex * pieceStatus.blockLength, actualBlockSize));
      }
    } catch (IOException e) {
      log.error(e);
    }
  }

  @Override
  public void run() {
    while (!(uncompletedPieces.isEmpty() && notStartedPieces.isEmpty())) {
      // TODO: here we're only downloading from seeders
      //  Request a missing piece from each Peer
      List<Peer> connections =
          seeders.stream()
              .filter(
                  peer ->
                      !peer.isPeerChoked()
                          && peer.isAmInterested()
                          && !peers.get(peer).waitingForRequest.get())
              .collect(Collectors.toList());
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
        requestBlockFromPeer(connections.remove(0), newPieceStatus, 0);
      }

      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        log.error(e);
      }
    }
  }

  private PieceStatus makeNewPieceStatus(int pieceIndex) {
    // TODO: fix int casting, and also add handling when piece length isn't a multiple of
    //  default block num
    if (((int) torrent.getPieceLength()) % DEFAULT_BLOCK_NUM != 0) {
      log.error("Piece length not divisible by block num");
      throw new IllegalStateException("Piece length not divisible by block num");
    }
    int blockLength = (int) torrent.getPieceLength() / DEFAULT_BLOCK_NUM;
    int blockNum =
        pieceIndex < totalPieces - 1
            ? DEFAULT_BLOCK_NUM
            : IntMath.divide((int) torrent.getLastPieceLength(), blockLength, RoundingMode.CEILING);
    return new PieceStatus(pieceIndex, blockNum, blockLength);
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
  public boolean validateAndReportBlockInProgress(Peer peer, FileBlockInfo blockInfo) {
    peers.get(peer).waitingForRequest.set(false);
    if (completedPieces.contains(blockInfo.getPieceIndex())) {
      log.debug("block already finished downloading");
      return false;
    }

    PieceStatus status = getOrInitPieceStatus(blockInfo.getPieceIndex());
    int blockIndex = blockInfo.getOffsetWithinPiece() / status.blockLength;

    if (isLastBlock(status, blockIndex)) {
      if (blockInfo.getLength() != ((int) torrent.getLastPieceLength()) % status.blockLength) {
        log.warn(
            "Block length invalid for final piece: expected [{}], found [{}]",
            ((int) torrent.getLastPieceLength()) % status.blockLength,
            blockInfo.getLength());
        return false;
      }
    } else {
      if (status.blockLength != blockInfo.getLength()) {
        log.warn(
            "Block length invalid: expected [{}], found [{}]",
            status.blockLength,
            blockInfo.getLength());
        return false;
      }
    }

    return status.status.get(blockIndex).compareAndSet(BLOCK_NOT_STARTED, BLOCK_IN_PROGRESS);
  }

  public void reportBlockCompletion(FileBlockInfo blockInfo) {
    int pieceIndex = blockInfo.getPieceIndex();
    PieceStatus status = uncompletedPieces.get(pieceIndex);
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
          log.info(
              "Block "
                  + blockInfo.getOffsetWithinPiece() / status.blockLength
                  + " not done for piece number "
                  + pieceIndex);
          // return if any block is not done
          return;
        }
      }

      uncompletedPieces.remove(pieceIndex);
      boolean valid = true;
      try {
        valid = networkStorageAdapter.verify(pieceIndex, torrent.getPieceHashes().get(pieceIndex));
      } catch (Exceptions.IllegalByteOffsets | IOException e) {
        log.error(e);
        e.printStackTrace();
      }
      if (!valid) {
        for (AtomicInteger blockStatus : status.status) {
          blockStatus.set(BLOCK_NOT_STARTED);
        }
        uncompletedPieces.put(pieceIndex, status);
      } else {
        completedPieces.add(pieceIndex);
      }
    }
  }

  public void reportBlockFailed(FileBlockInfo blockInfo) {
    PieceStatus status = uncompletedPieces.get(blockInfo.getPieceIndex());
    if (status == null) {
      log.error("Block missing from uncompletedPieces: " + blockInfo);
      throw new IllegalStateException("Block missing from uncompletedPieces: " + blockInfo);
    }

    // We must set to uncompleted here because even if the block was previously completed, a failed
    // write could
    // have messed it up
    status.status.get(blockInfo.getOffsetWithinPiece() / status.blockLength).set(BLOCK_NOT_STARTED);
  }

  private boolean isLastBlock(PieceStatus pieceStatus, int blockIndex) {
    return pieceStatus.pieceIndex == totalPieces - 1 && blockIndex == pieceStatus.status.size() - 1;
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

  /* s must be an even-length string. */
  public static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] =
          (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  private Bitfield buildBitfield() {
    Bitfield bitfield = new Bitfield(totalPieces);
    for (Integer i : completedPieces) {
      bitfield.setBit(i);
    }
    return bitfield;
  }
}
