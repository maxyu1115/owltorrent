package edu.rice.owltorrent.network;

import lombok.Data;

/** @author Lorraine Lyu */
@Data
public abstract class PeerMessage {
  /** The size, in bytes, of the length field in a message (one 32-bit integer). */
  public static final int LENGTH_FIELD_SIZE = 4;

  public enum MessageType {
    CHOKE(0),
    UNCHOKE(1),
    INTERESTED(2),
    NOT_INTERESTED(3),
    HAVE(4),
    BITFIELD(5),
    REQUEST(6),
    PIECE(7),
    CANCEL(8);

    private byte value;

    MessageType(int value) {
      this.value = (byte) value;
    }

    public boolean equals(byte c) {
      return this.value == c;
    }

    public byte getByteValue() {
      return this.value;
    }
  }

  private final MessageType messageType;

  public PeerMessage(MessageType type) {
    this.messageType = type;
  }

  public abstract byte[] toBytes();
}
