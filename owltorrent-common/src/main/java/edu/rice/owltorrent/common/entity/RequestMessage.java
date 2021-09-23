package edu.rice.owltorrent.common.entity;

import java.nio.ByteBuffer;

public class RequestMessage extends PeerMessage {

    private static final int SIZE = 13;

    /**
     * Default block size is 2^14 bytes, or 16kB.
     */
    public static final int DEFAULT_REQUEST_SIZE = 16384;

    private final int index;
    private final int begin;
    private final int length;

    private RequestMessage(ByteBuffer buffer, int index, int begin, int length) {
        super(Type.REQUEST, buffer);
        this.index = index;
        this.begin = begin;
        this.length = length;
    }

    public int getBegin() {
        return begin;
    }

    public int getIndex() {
        return index;
    }

    public int getLength() {
        return length;
    }

    public RequestMessage construct(int index, int begin, int length) {
        ByteBuffer buffer = ByteBuffer.allocate(
                SIZE + LENGTH_FIELD_SIZE);
        buffer.putInt(RequestMessage.SIZE);
        buffer.put(PeerMessage.Type.REQUEST.getByteValue());
        buffer.putInt(index);
        buffer.putInt(begin);
        buffer.putInt(length);
        return new RequestMessage(buffer, index, begin, length);
    }
}
