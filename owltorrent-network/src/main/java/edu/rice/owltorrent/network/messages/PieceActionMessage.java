package edu.rice.owltorrent.network.messages;

import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.network.PeerMessage;
import java.nio.ByteBuffer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Message sent to request/cancel piece from peer
 *
 * @author Lorraine Lyu
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PieceActionMessage extends PeerMessage {

  private static final int SIZE = 13;

  /** Default block size is 2^14 bytes, or 16kB. */
  public static final int DEFAULT_REQUEST_SIZE = 16384;

  private final int index;
  private final int begin;
  private final int length;

  private PieceActionMessage(MessageType type, int index, int begin, int length) {
    super(type);
    this.index = index;
    this.begin = begin;
    this.length = length;
  }

  public static PieceActionMessage makeRequestMessage(int index, int begin, int length) {
    return new PieceActionMessage(MessageType.REQUEST, index, begin, length);
  }

  public static PieceActionMessage makeCancelMessage(int index, int begin, int length) {
    return new PieceActionMessage(MessageType.CANCEL, index, begin, length);
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
    return this.index >= 0 && this.index < torrent.getPieceLength();
    //   && this.begin + this.length <= torrent.getPieceSize(this.piece);
  }

  public static PieceActionMessage parse(MessageType type, ByteBuffer buffer) {
    if (!(type.equals(MessageType.REQUEST) || type.equals(MessageType.CANCEL))) {
      throw new IllegalArgumentException("Piece Action cannot be made with message type: " + type);
    }
    int index = buffer.getInt();
    int begin = buffer.getInt();
    int length = buffer.getInt();
    return new PieceActionMessage(type, index, begin, length);
  }
}
