package edu.rice.owltorrent.common.entity;

import lombok.Data;

import java.nio.ByteBuffer;

@Data
public abstract class PeerMessage {
    /**
     * The size, in bytes, of the length field in a message (one 32-bit
     * integer).
     */
    public static final int LENGTH_FIELD_SIZE = 4;

    public enum Type {
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

        Type(int value) {
            this.value = (byte) value;
        }

        public boolean equals(byte c) {
            return this.value == c;
        }

        public byte getByteValue() {
            return this.value;
        }
    }


    private final Type type;
    private final ByteBuffer data;

    PeerMessage(Type type, ByteBuffer data) {
        this.type = type;
        this.data = data;
        // Sets position to 0
        this.data.rewind();
    }

    public Type getType() {
        return type;
    }
}
