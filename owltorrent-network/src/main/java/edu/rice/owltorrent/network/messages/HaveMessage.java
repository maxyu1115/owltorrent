package edu.rice.owltorrent.network.messages;

import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.network.PeerMessage;
import java.nio.ByteBuffer;
import lombok.ToString;

/**
 * Peer Messages for Have
 *
 * @author yuchengu
 */
@ToString
public class HaveMessage extends PeerMessage {

  private static final int SIZE = 5; // 1 byte for type + 4 bytes for index

  private final int index;

  public HaveMessage(int index) {
    super(MessageType.HAVE);
    this.index = index;
  }

  @Override
  public byte[] toBytes() {
    ByteBuffer buffer = ByteBuffer.allocate(SIZE + LENGTH_FIELD_SIZE);
    buffer.putInt(HaveMessage.SIZE);
    buffer.put(MessageType.HAVE.getByteValue());
    buffer.putInt(index);
    buffer.rewind();
    return buffer.array();
  }

  @Override
  public boolean verify(Torrent torrent) {
    return this.index >= 0 && this.index < torrent.getPieceLength();
  }

  public static HaveMessage parse(ByteBuffer buffer) {
    int index = buffer.getInt();
    return new HaveMessage(index);
  }

  public int getIndex() {
    return this.index;
  }
}
