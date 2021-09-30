package edu.rice.owltorrent.network.messages;

import edu.rice.owltorrent.network.PeerMessage;
import java.nio.ByteBuffer;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Payloadless Message for Peer Message: 1. Choke 2. Unchoke 3. interested 4. not interested
 *
 * @author: Shijie Fan
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class PayloadlessMessage extends PeerMessage {
  public static final int PAYLOADLESS_MESSAGE_SIZE = 5; // 4 bytes for length + 1 byte for type

  public PayloadlessMessage(MessageType type) {
    super(type);
  } // No payload

  @Override
  public byte[] toBytes() {
    ByteBuffer buffer = ByteBuffer.allocate(PAYLOADLESS_MESSAGE_SIZE);
    buffer.putInt(PAYLOADLESS_MESSAGE_SIZE);
    buffer.put(this.messageType.getByteValue());
    buffer.rewind();
    return buffer.array();
  }
}
