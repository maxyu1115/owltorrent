package edu.rice.owltorrent.network;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;

/**
 * Message Reader for reading from Peer's inputChannels.
 *
 * @author Max Yu
 */
@Log4j2(topic = "network")
public class MessageReader {
  // 2 MB
  private static final int MAX_MESSAGE_SIZE = 2 * 1024 * 1024;

  public PeerMessage readMessage(ReadableByteChannel inputChannel) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(PeerMessage.LENGTH_FIELD_SIZE);
    buffer.limit(PeerMessage.LENGTH_FIELD_SIZE);
    int readBytes = inputChannel.read(buffer);
    if (readBytes < 0) {
      log.debug("connection is closed by other peer");
      return null;
    }
    log.debug(Arrays.toString(buffer.array()));
    int pstrLength = buffer.getInt(0);
    log.trace("read of message length finished, Message length is {}", pstrLength);

    if (pstrLength > MAX_MESSAGE_SIZE) {
      log.warn(
              "Proposed limit of {} is larger than max message size {}",
              PeerMessage.LENGTH_FIELD_SIZE + pstrLength,
              MAX_MESSAGE_SIZE);
      log.warn("current bytes in buffer is {}", Arrays.toString(buffer.array()));
      return null;
    }

    // resize buffer to read actual message
    if (PeerMessage.LENGTH_FIELD_SIZE + pstrLength > buffer.capacity()) {
      ByteBuffer old = buffer;
      old.rewind();
      buffer = ByteBuffer.allocate(PeerMessage.LENGTH_FIELD_SIZE + pstrLength);
      buffer.put(old);
    }

    int totalReadBytes = 0;
    while (totalReadBytes < pstrLength) {
      readBytes = inputChannel.read(buffer);
      if (readBytes < 0) {
        log.debug("connection is closed by other peer");
        return null;
      }
      totalReadBytes += readBytes;
      log.debug("Total read bytes " + totalReadBytes);
    }
    log.info("Actual length read: " + totalReadBytes + ", goal length " + pstrLength);
    buffer.rewind();
    return PeerMessage.parse(buffer);
  }
}
