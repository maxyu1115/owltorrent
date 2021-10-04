package edu.rice.owltorrent.network.messages;

import edu.rice.owltorrent.common.entity.FileBlock;
import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.network.PeerMessage;
import java.nio.ByteBuffer;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Piece Message used to send/receive piece messages.
 *
 * @author Max Yu
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class PieceMessage extends PeerMessage {

  /** 2 * 4 from the index and begin and one index */
  private static final int BASE_SIZE = 9;

  int index;
  int begin;
  byte[] piece;

  // TODO: Make constructor private and add a create factory that also takes in the torrent and
  // performs verification.
  public PieceMessage(int index, int begin, byte[] piece) {
    super(MessageType.PIECE);
    this.index = index;
    this.begin = begin;
    this.piece = piece;
  }

  public FileBlock getFileBlock() {
    return new FileBlock(index, begin, piece);
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

  @Override
  public boolean verify(Torrent torrent) {
    // TODO: write verification logic. Currently blocked by hash function refactor
    return true;
  }

  public static PieceMessage parse(ByteBuffer buffer) {
    int index = buffer.getInt();
    int begin = buffer.getInt();
    byte[] piece = new byte[buffer.remaining()];
    buffer.get(piece);
    return new PieceMessage(index, begin, piece);
  }
}
