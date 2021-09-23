package edu.rice.owltorrent.network;

import java.io.DataInputStream;

public interface MessageReader {
  PeerMessage readMessage(DataInputStream inputStream);
}
