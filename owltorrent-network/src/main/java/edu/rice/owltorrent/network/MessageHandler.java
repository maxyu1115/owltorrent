package edu.rice.owltorrent.network;

public interface MessageHandler {
  void handleMessage(PeerMessage message);
}
