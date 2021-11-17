package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.Peer;

/**
 * Functional Interface for message handler.
 *
 * @author yunlyu
 */
public interface MessageHandler {
  void handleMessage(PeerMessage message, PeerConnector connector) throws InterruptedException;

  void removePeer(Peer peer);
}
