package edu.rice.owltorrent.network.messages;

import edu.rice.owltorrent.common.entity.Bitfield;
import edu.rice.owltorrent.network.PeerMessage;
import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * Bitfield Message for Peer Messaging
 *
 * @author shijie
 */
public class BitfieldMessage extends PeerMessage {

  public static final int BASE_SIZE = 1;
  public Bitfield bitfield;

  public BitfieldMessage(Bitfield bitfield) {
    super(MessageType.BITFIELD);
    this.bitfield = bitfield;
  }

  @Override
  public byte[] toBytes() {
    ByteBuffer buffer =
        ByteBuffer.allocate(LENGTH_FIELD_SIZE + BASE_SIZE + this.bitfield.size() / 8);
    buffer.putInt(BASE_SIZE + this.bitfield.size() / 8);
    buffer.put(this.messageType.getByteValue());
    buffer.put(this.bitfield.toByteArray());
    buffer.rewind();
    return buffer.array();
  }

  public static BitfieldMessage parse(ByteBuffer buffer) {
    BitSet bitSet = BitSet.valueOf(buffer);
    Bitfield bitfield = new Bitfield(bitSet);
    return new BitfieldMessage(bitfield);
  }
}
