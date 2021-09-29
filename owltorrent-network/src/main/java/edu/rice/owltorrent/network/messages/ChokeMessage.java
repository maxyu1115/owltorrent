package edu.rice.owltorrent.network.messages;

import edu.rice.owltorrent.network.PeerMessage;
import java.nio.ByteBuffer;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Choke Message for Peer Message
 *
 * @author: Shijie Fan
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class ChokeMessage extends PeerMessage {
    public static int chokeMessageSize = 5; // 4 bytes for length + 1 byte for type

    public ChokeMessage() {
        super(MessageType.CHOKE);
    } // No payload

    @Override
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(chokeMessageSize);
        buffer.putInt(chokeMessageSize);
        buffer.put(MessageType.CHOKE.getByteValue());
        buffer.rewind();
        return buffer.array();
    }
}