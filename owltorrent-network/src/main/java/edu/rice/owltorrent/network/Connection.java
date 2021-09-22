package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.PeerMessage;

public interface Connection {
  void connectTo(Peer peer);

  boolean writeMessage(PeerMessage message);

  void registerListener(MessageHandler handler);
}
