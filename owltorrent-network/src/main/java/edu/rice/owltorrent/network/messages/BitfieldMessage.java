package edu.rice.owltorrent.network.messages;

import edu.rice.owltorrent.common.entity.Bitfield;
import edu.rice.owltorrent.network.PeerMessage;
import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.ToString;

/**
 * Bitfield Message for Peer Messaging
 *
 * @author shijie
 */
@ToString
public class BitfieldMessage extends PeerMessage {

  public static final int BASE_SIZE = 1;
  @Getter private Bitfield bitfield;

  public BitfieldMessage(Bitfield bitfield) {
    super(MessageType.BITFIELD);
    this.bitfield = bitfield;
  }

  @Override
  public byte[] toBytes() {
    ByteBuffer buffer = ByteBuffer.allocate(LENGTH_FIELD_SIZE + BASE_SIZE + bitfield.size());
    buffer.putInt(BASE_SIZE + bitfield.size());
    buffer.put(this.messageType.getByteValue());
    buffer.put(bitfield.toByteArray());
    buffer.rewind();
    return buffer.array();
  }

  public static BitfieldMessage parse(ByteBuffer buffer) {
    // FIXME: look to eliminate the numBits inconsistencies between Bitfields we create ourselves
    //  and others send us. Bitfields we receive will always be of numBits of multiples of 8
    //  (padded), while our bitfields would be the exact bytes.
    Bitfield bitfield = new Bitfield(buffer.remaining() * 8);
    for (int i = 0; i < buffer.remaining() * 8; i++) {
      if ((buffer.get(PeerMessage.LENGTH_FIELD_SIZE + BASE_SIZE + (i / 8)) & (1 << (7 - (i % 8))))
          > 0) {
        bitfield.setBit(i);
      }
    }
    return new BitfieldMessage(bitfield);
  }
}
