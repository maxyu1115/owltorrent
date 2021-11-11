package edu.rice.owltorrent.network.task;

import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.network.*;
import edu.rice.owltorrent.network.messages.PayloadlessMessage;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Task for initiating socket connection
 *
 * @author Max Yu
 */
@RequiredArgsConstructor
@Log4j2(topic = "network")
public class InitiateSocketConnectionTask implements Runnable {
  private final PeerConnectorFactory peerConnectorFactory;
  private final Peer peer;
  private final TorrentManager manager;
  private final StorageAdapter storageAdapter;

  @Override
  public void run() {
    try {
      PeerConnector connector =
          peerConnectorFactory.makeInitialConnection(peer, manager, storageAdapter);
      connector.initiateConnection();
      manager.addPeer(connector, peer);
      connector.writeMessage(new PayloadlessMessage(PeerMessage.MessageType.INTERESTED));
    } catch (IOException e) {
      e.printStackTrace(System.err);
      log.error("Error connecting peer {}", peer.getAddress());
    }
  }
}
