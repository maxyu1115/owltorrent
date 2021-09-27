package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.network.messages.KeepAliveMessage;
import edu.rice.owltorrent.network.messages.PieceMessage;
import edu.rice.owltorrent.network.messages.RequestMessage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

/**
 * Abstract message class for messages sent between peers.
 *
 * @author Lorraine Lyu
 */
@Data
@Log4j2(topic = "general")
@EqualsAndHashCode
public abstract class PeerMessage {
  /** The size, in bytes, of the length field in a message (one 32-bit integer). */
  public static final int LENGTH_FIELD_SIZE = 4;

  public static final int HANDSHAKE_BYTE_SIZE = 68;

  public enum MessageType {
    KEEP_ALIVE(-1),
    CHOKE(0),
    UNCHOKE(1),
    INTERESTED(2),
    NOT_INTERESTED(3),
    HAVE(4),
    BITFIELD(5),
    REQUEST(6),
    PIECE(7),
    CANCEL(8);

    private final byte value;

    MessageType(int value) {
      this.value = (byte) value;
    }

    public boolean equals(byte c) {
      return this.value == c;
    }

    public byte getByteValue() {
      return this.value;
    }

    private static final Map<Byte, MessageType> BYTE_TO_TYPE;

    static {
      Map<Byte, MessageType> map = new HashMap<>();
      for (MessageType type : MessageType.values()) {
        if (type == KEEP_ALIVE) {
          continue;
        }
        map.put(type.getByteValue(), type);
      }
      BYTE_TO_TYPE = Collections.unmodifiableMap(map);
    }

    public static MessageType getType(byte typeByte) {
      return BYTE_TO_TYPE.get(typeByte);
    }
  }

  private final MessageType messageType;

  public PeerMessage(MessageType type) {
    this.messageType = type;
  }

  public abstract byte[] toBytes();

  /**
   * Verifies if the message is a valid message. This mainly involves checking info against the
   * torrent file.
   *
   * @param torrent the torrent file
   * @return true if the message is valid
   */
  public boolean verify(Torrent torrent) {
    return true;
  }

  /**
   * Parses the bytes in the bytebuffer into a peer message. This method does NOT validate if the
   * message is valid. This method accepts the bytebuffer with the first 5 bytes already parsed and
   * truncated (4 bytes for the length and one byte for the identifier).
   *
   * @param buffer byte buffer containing message
   * @return peer message
   * @throws IOException Throws an IO exception if the message fails to parse.
   */
  public static PeerMessage parse(ByteBuffer buffer) throws IOException {
    int length = buffer.getInt();
    if (length != buffer.remaining()) {
      throw new IOException("Message size did not match announced size!");
    } else if (length == 0) {
      return new KeepAliveMessage();
    }

    MessageType type = MessageType.getType(buffer.get());
    if (type == null) {
      throw new IOException("Unknown message ID!");
    }

    switch (type) {
      case CHOKE:
      case UNCHOKE:
      case INTERESTED:
      case NOT_INTERESTED:
      case HAVE:
      case BITFIELD:
        throw new IllegalStateException("Not implemented yet. ");
      case REQUEST:
        return RequestMessage.parse(buffer);
      case PIECE:
        return PieceMessage.parse(buffer);
      case CANCEL:
        throw new IllegalStateException("Also not implemented yet. ");
      default:
        throw new IllegalStateException("Illegal Message Type.");
    }
  }

  static byte[] constructHandShakeMessage(Peer peer) {
    ByteBuffer message = ByteBuffer.allocate(68);
    message.put((byte) 19);
    byte[] pstr = new String("BitTorrent protocol").getBytes(StandardCharsets.US_ASCII);
    message.put(pstr);
    message.put(new byte[8]);
    message.put(peer.getTorrent().getInfoHash().getBytes());
    message.put(peer.getPeerID().getBytes());
    return message.array();
  }

  static boolean confirmHandShake(byte[] buffer, Peer peer) {
    if (buffer[0] != 19) return false;

    byte[] title = "BitTorrent protocol".getBytes(StandardCharsets.US_ASCII);
    for (int i = 1; i < 20; i++) if (title[i - 1] != buffer[i]) return false;
    byte[] infoHash = peer.getTorrent().getInfoHash().getBytes();
    for (int i = 28; i < 48; i++) {
      if (infoHash[i - 28] != buffer[i]) return false;
    }

    byte[] peerId = peer.getPeerID().getBytes();
    for (int i = 48; i < 68; i++) {
      if (peerId[i - 48] != buffer[i]) return false;
    }

    return true;
  }
}
