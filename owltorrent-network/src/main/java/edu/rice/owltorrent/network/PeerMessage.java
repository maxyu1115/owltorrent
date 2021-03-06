package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import edu.rice.owltorrent.network.messages.*;
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
@Log4j2(topic = "network")
@EqualsAndHashCode
public abstract class PeerMessage {
  /** The size, in bytes, of the length field in a message (one 32-bit integer). */
  public static final int LENGTH_FIELD_SIZE = 4;

  public static final String BIT_TORRENT_PROTOCOL = "BitTorrent protocol";

  public static final int HANDSHAKE_BYTE_SIZE = 68;
  public static final int HANDSHAKE_LENGTH_BYTE = 19;

  public static final int HANDSHAKE_EMPTY_BYTE_LEN = 8;
  public static final int HANDSHAKE_PROTOCOL_INDEX = 1;
  public static final int HANDSHAKE_INFO_HASH_INDEX = 28;
  public static final int HANDSHAKE_PEER_ID_INDEX = 48;

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

  protected final MessageType messageType;

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
        return new PayloadlessMessage(type);
      case HAVE:
        return HaveMessage.parse(buffer);
      case BITFIELD:
        return BitfieldMessage.parse(buffer);
      case PIECE:
        return PieceMessage.parse(buffer);
      case REQUEST:
      case CANCEL:
        return PieceActionMessage.parse(type, buffer);
      default:
        throw new IllegalStateException("Illegal Message Type.");
    }
  }

  public static byte[] constructHandShakeMessage(Torrent torrent, TwentyByteId ourPeerID) {
    ByteBuffer message = ByteBuffer.allocate(HANDSHAKE_BYTE_SIZE);
    message.put((byte) HANDSHAKE_LENGTH_BYTE);
    byte[] pstr = BIT_TORRENT_PROTOCOL.getBytes(StandardCharsets.US_ASCII);
    message.put(pstr);
    message.put(new byte[HANDSHAKE_EMPTY_BYTE_LEN]);
    message.put(torrent.getInfoHash().getBytes());
    message.put(ourPeerID.getBytes());
    return message.array();
  }

  static boolean confirmHandShake(byte[] buffer, Peer peer) {
    if (buffer[0] != HANDSHAKE_LENGTH_BYTE) return false;

    byte[] title = BIT_TORRENT_PROTOCOL.getBytes(StandardCharsets.US_ASCII);
    for (int i = HANDSHAKE_PROTOCOL_INDEX;
        i < HANDSHAKE_PROTOCOL_INDEX + HANDSHAKE_LENGTH_BYTE;
        i++) {
      if (title[i - 1] != buffer[i]) return false;
    }
    byte[] infoHash = peer.getTorrent().getInfoHash().getBytes();
    for (int i = HANDSHAKE_INFO_HASH_INDEX; i < HANDSHAKE_PEER_ID_INDEX; i++) {
      if (infoHash[i - HANDSHAKE_INFO_HASH_INDEX] != buffer[i]) return false;
    }

    if (peer.getPeerID() != null) {
      byte[] peerId = peer.getPeerID().getBytes();
      for (int i = HANDSHAKE_PEER_ID_INDEX; i < HANDSHAKE_BYTE_SIZE; i++) {
        if (peerId[i - HANDSHAKE_PEER_ID_INDEX] != buffer[i]) return false;
      }
    } else {
      byte[] peerId = new byte[20];
      System.arraycopy(buffer, HANDSHAKE_PEER_ID_INDEX, peerId, 0, 20);
      peer.setPeerID(new TwentyByteId(peerId));
    }
    return true;
  }
}
