package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.entity.Peer;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import java.nio.ByteBuffer;

/** @author Lorraine Lyu */
@Data
@Log4j2(topic="general")
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

  public abstract PeerMessage parse(Torrent torrent);

  public abstract PeerMessage construct();

  public abstract boolean verify(Torrent torrent);

  static final byte[] constructHelloWorldMessage(Peer peer) {
    ByteBuffer message = ByteBuffer.allocate(68);
    message.put((byte) 19);
    byte[] pstr = new String("BitTorrent protocol").getBytes();
    message.put(pstr);
    message.put(new byte[8]);
    message.put(peer.getTorrent().getInfoHash());
    message.put(peer.getPeerID().getBytes());
    return message.array();
  }

  static final boolean confirmHandShake(byte[] buffer, Peer peer) {
    if (buffer[0] != 19) return false;

    byte[] title = "BitTorrent protocol".getBytes();
    for (int i = 1; i < 20; i++) if (title[i - 1] != buffer[i]) return false;
    byte[] infoHash = peer.getTorrent().getInfoHash();
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
