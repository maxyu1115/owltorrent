package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.adapters.NetworkToStorageAdapter;
import edu.rice.owltorrent.common.entity.Peer;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Socket implementation of PeerConnector class.
 *
 * @author Lorraine Lyu, Max Yu
 */
public class SocketConnector extends PeerConnector {
  private final Socket peerSocket;
  private DataOutputStream out;
  private DataInputStream in;

  public SocketConnector(Peer peer, NetworkToStorageAdapter storageAdapter) throws IOException {
    super(peer, storageAdapter);
    peerSocket = new Socket(peer.getAddress().getAddress(), peer.getAddress().getPort());
  }

  @Override
  public void connect() throws IOException {
    peerSocket.connect(peer.getAddress());
    this.in = new DataInputStream(peerSocket.getInputStream());
    this.out = new DataOutputStream(peerSocket.getOutputStream());

    this.out.write(constructHelloWorldMessage(this.peer));

    // read and confirm handshake from peer
    byte[] incomingHandshakeBuffer = new byte[68];
    in.read(incomingHandshakeBuffer);
    if (!confirmHandShake(incomingHandshakeBuffer, this.peer)) {
      throw new IOException(
          String.format("Invalid handshake from peer id=%s", this.peer.getPeerID().getId()));
    }
  }

  @Override
  public void writeMessage(PeerMessage message) throws IOException {
    out.write(message.toBytes());
  }

  private static byte[] constructHelloWorldMessage(Peer peer) {
    ByteBuffer message = ByteBuffer.allocate(68);
    message.put((byte) 19);
    byte[] pstr = new String("BitTorrent protocol").getBytes();
    message.put(pstr);
    message.put(new byte[8]);
    message.put(peer.getTorrent().getInfoHash());
    message.put(peer.getPeerID().getBytes());
    return message.array();
  }

  private static boolean confirmHandShake(byte[] buffer, Peer peer) {
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
