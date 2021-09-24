package edu.rice.owltorrent.network;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

public interface MessageReader {
  PeerMessage readMessage(ReadableByteChannel inputChannel) throws IOException;
}
