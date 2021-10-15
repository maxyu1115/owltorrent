package edu.rice.owltorrent.network.messages;

import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.network.PeerMessage;
import java.nio.ByteBuffer;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Payloadless Message for Peer Message: 1. Choke 2. Unchoke 3. interested 4. not interested
 *
 * @author: Shijie Fan
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PayloadlessMessage extends PeerMessage {
  public static final int PAYLOADLESS_MESSAGE_SIZE = 1; // 1 byte for type

  public PayloadlessMessage(MessageType type) {
    super(type);
  } // No payload

  @Override
  public byte[] toBytes() {
    ByteBuffer buffer = ByteBuffer.allocate(LENGTH_FIELD_SIZE + PAYLOADLESS_MESSAGE_SIZE);
    buffer.putInt(PAYLOADLESS_MESSAGE_SIZE);
    buffer.put(this.messageType.getByteValue());
    buffer.rewind();
    return buffer.array();
  }

  @Override
  public boolean verify(Torrent torrent) {
    return this.messageType.equals(MessageType.CHOKE)
        || this.messageType.equals(MessageType.UNCHOKE)
        || this.messageType.equals(MessageType.INTERESTED)
        || this.messageType.equals(MessageType.NOT_INTERESTED);
  }
}
