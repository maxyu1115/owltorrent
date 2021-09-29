package edu.rice.owltorrent.network.messages;

import edu.rice.owltorrent.network.PeerMessage;
import java.nio.ByteBuffer;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Unchoke Message for Peer Message
 *
 * @author: Shijie Fan
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class UnchokeMessage extends PeerMessage {
    public static int unchokeMessageSize = 5; // 4 bytes for length + 1 byte for type

    public UnchokeMessage() {
        super(MessageType.UNCHOKE);
    } // No payload

    @Override
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(unchokeMessageSize);
        buffer.putInt(unchokeMessageSize);
        buffer.put(MessageType.UNCHOKE.getByteValue());
        buffer.rewind();
        return buffer.array();
    }
}