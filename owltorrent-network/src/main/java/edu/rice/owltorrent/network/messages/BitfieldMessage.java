package main.java.edu.rice.owltorrent.network.messages;

import edu.rice.owltorrent.network.PeerMessage;
import java.nio.ByteBuffer;
import java.util.BitSet;

public class BitfieldMessage extends PeerMessage {

  public static final int BASE_SIZE = 5;
  public BitSet bitfield;

  public BitfieldMessage(MessageType type, BitSet bitfield) {
    super(type);
    this.bitfield = bitfield;
  }

  @Override
  public byte[] toBytes() {
    ByteBuffer buffer = ByteBuffer.allocate(BASE_SIZE + this.bitfield.size() / 8);
    buffer.putInt(BASE_SIZE + this.bitfield.size() / 8);
    buffer.put(this.messageType.getByteValue());
    buffer.put(this.bitfield.toByteArray());
    buffer.rewind();
    return buffer.array();
  }

  public static BitfieldMessage parse(ByteBuffer buffer) {
    BitSet bitfield = BitSet.valueOf(buffer);
    return new BitfieldMessage(MessageType.BITFIELD, bitfield);
  }
}
