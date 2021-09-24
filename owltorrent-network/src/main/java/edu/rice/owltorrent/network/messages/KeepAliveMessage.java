package edu.rice.owltorrent.network.messages;

import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.network.PeerMessage;
import java.nio.ByteBuffer;

/**
 * Message sent to keep connection alive.
 *
 * @author Max Yu
 */
public final class KeepAliveMessage extends PeerMessage {

  public KeepAliveMessage() {
    super(MessageType.KEEP_ALIVE);
  }

  @Override
  public byte[] toBytes() {
    ByteBuffer buffer = ByteBuffer.allocate(4);
    buffer.putInt(0);
    return buffer.array();
  }

  @Override
  public boolean verify(Torrent torrent) {
    return true;
  }
}
