package edu.rice.owltorrent.network;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

/**
 * Message Reader interface for reading from Peer's inputChannels.
 *
 * @author Max Yu
 */
public interface MessageReader {
  PeerMessage readMessage(ReadableByteChannel inputChannel) throws IOException;
}
