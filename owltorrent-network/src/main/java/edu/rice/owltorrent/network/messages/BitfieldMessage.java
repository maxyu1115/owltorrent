package edu.rice.owltorrent.network.messages;

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
  public BitSet bitfield;

  public BitfieldMessage(BitSet bitfield) {
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

  public boolean getBit(int index) {
    /*
     The order in the BitSet is 0 at the least significant digit and 7 at the most significant digit for the first byte.
     Therefore, a conversion is necessary to make the most significant bit be at index 0.
     For index i, the sum of i and its actual index in the BitSet is ((i/8) * 2 + 1) * 8 - 1.
    */
    int actualIndex = 8 * ((index / 8) * 2 + 1) - 1 - index;
    return this.bitfield.get(actualIndex);
  }

  public static BitfieldMessage parse(ByteBuffer buffer) {
    BitSet bitfield = BitSet.valueOf(buffer);
    return new BitfieldMessage(bitfield);
  }
}
