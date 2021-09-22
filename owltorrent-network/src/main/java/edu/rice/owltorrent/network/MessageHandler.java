package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.PeerMessage;

public interface MessageHandler {
  void handleMessage(PeerMessage message);
}
