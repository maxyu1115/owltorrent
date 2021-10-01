package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.adapters.NetworkToStorageAdapter;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.network.messages.PieceActionMessage;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/** @author Lorraine Lyu, Max Yu */
@Log4j2(topic = "general")
public class TorrentManager implements Runnable, AutoCloseable {
  private final Set<Integer> completedPieces;
  // TODO: Track individual piece status. Needs a new class.
  private final Set<Integer> inProgressPieces;
  private final Set<Integer> notStartedPieces;

  private final int totalPieces;

  private final Map<Peer, PeerConnector> peers;
  private final NetworkToStorageAdapter networkStorageAdapter;
  @Getter private final Torrent torrent;

  public TorrentManager(Torrent file, List<Peer> peerList, NetworkToStorageAdapter adapter) {
    this.torrent = file;
    this.networkStorageAdapter = adapter;
    this.completedPieces = Collections.newSetFromMap(new ConcurrentHashMap<>());
    this.inProgressPieces = Collections.newSetFromMap(new ConcurrentHashMap<>());
    this.notStartedPieces = Collections.newSetFromMap(new ConcurrentHashMap<>());
    this.totalPieces = torrent.getPieces().size();

    for (int idx = 0; idx < totalPieces; idx++) {
      this.notStartedPieces.add(idx);
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

  private void requestPieceFromPeer(Peer peer, int pieceNum) {
    PeerConnector peerConnector = peers.get(peer);
    if (peerConnector == null) {
      return;
    }

    // TODO: fix int casting.
    try {
      peerConnector.writeMessage(
          PieceActionMessage.makeRequestMessage(pieceNum, 0, (int) torrent.getPieceLength()));
    } catch (IOException e) {
      log.error(e);
    }
  }

  @Override
  public void run() {
    while (!notStartedPieces.isEmpty()) {
      Random random = new Random();
      PeerConnector[] connectors = new PeerConnector[peers.size()];
      peers.values().toArray(connectors);
      for (int idx : notStartedPieces) {
        var peer = connectors[random.nextInt(connectors.length)];
        requestPieceFromPeer(peer.peer, idx);
      }
    }
  }
  // TODO: Not compatable with current setup
  // private sendPieceToPeer(PeerConnector peer, int pieceNum);
  // private sendHaveMessageToPeer(PeerConnector peer);
}
