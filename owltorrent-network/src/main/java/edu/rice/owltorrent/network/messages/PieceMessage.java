package edu.rice.owltorrent.network.messages;

import edu.rice.owltorrent.common.entity.FilePiece;
import edu.rice.owltorrent.network.PeerMessage;
import java.nio.ByteBuffer;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Piece Message used to send/receive piece messages.
 *
 * @author Max Yu
 */
@EqualsAndHashCode(callSuper = true)
@Value
public class PieceMessage extends PeerMessage {

  private static final int BASE_SIZE = 9;

  int index;
  int begin;
  byte[] piece;

  private PieceMessage(int index, int begin, byte[] piece) {
    super(MessageType.PIECE);
    this.index = index;
    this.begin = begin;
    this.piece = piece;
  }

  public FilePiece getFilePiece() {
    // FIXME: why is this hash in filePiece? no data?
    return new FilePiece(index, begin, piece);
  }

  @Override
  public byte[] toBytes() {
    ByteBuffer buffer = ByteBuffer.allocate(4 + PieceMessage.BASE_SIZE + piece.length);
    buffer.putInt(PieceMessage.BASE_SIZE + piece.length);
    buffer.put(MessageType.PIECE.getByteValue());
    buffer.putInt(index);
    buffer.putInt(begin);
    buffer.put(piece);

    // Sets position to 0
    buffer.rewind();
    return buffer.array();
  }
}
