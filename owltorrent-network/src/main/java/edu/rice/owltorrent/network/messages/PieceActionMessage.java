package edu.rice.owltorrent.network.messages;

import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.network.PeerMessage;
import java.nio.ByteBuffer;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Message sent to request/cancel piece from peer
 *
 * @author Lorraine Lyu
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class PieceActionMessage extends PeerMessage {

  private static final int SIZE = 13;

  /** Default block size is 2^14 bytes, or 16kB. */
  public static final int DEFAULT_REQUEST_SIZE = 16384;

  private final int index;
  private final int begin;
  private final int length;

  public PieceActionMessage(MessageType type, int index, int begin, int length) {
    super(type);
    this.index = index;
    this.begin = begin;
    this.length = length;
  }

  @Override
  public byte[] toBytes() {
    ByteBuffer buffer = ByteBuffer.allocate(SIZE + LENGTH_FIELD_SIZE);
    buffer.putInt(PieceActionMessage.SIZE);
    buffer.put(this.messageType.getByteValue());
    buffer.putInt(index);
    buffer.putInt(begin);
    buffer.putInt(length);

    // Sets position to 0
    buffer.rewind();
    return buffer.array();
  }

  @Override
  public boolean verify(Torrent torrent) {
    // TODO add torrent file piece size
    return this.index >= 0
        && this.index < torrent.getPieceLength()
        && (this.messageType.equals(MessageType.REQUEST)
            || this.messageType.equals(MessageType.CANCEL));
    //   && this.begin + this.length <= torrent.getPieceSize(this.piece);
  }

  public static PieceActionMessage parse(MessageType type, ByteBuffer buffer) {
    int index = buffer.getInt();
    int begin = buffer.getInt();
    int length = buffer.getInt();
    return new PieceActionMessage(type, index, begin, length);
  }
}
