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

  private SocketConnector(
      Peer peer,
      NetworkToStorageAdapter storageAdapter,
      MessageReader messageReader,
      Socket peerSocket) {
    super(peer, storageAdapter, messageReader);
    this.peerSocket = peerSocket;
  }

  public static SocketConnector initiateConnection(
      Peer peer, NetworkToStorageAdapter storageAdapter, MessageReader messageReader)
      throws IOException {
    SocketConnector connector =
        new SocketConnector(
            peer,
            storageAdapter,
            messageReader,
            new Socket(peer.getAddress().getAddress(), peer.getAddress().getPort()));
    connector.initiateConnection();
    return connector;
  }

  public static SocketConnector respondToConnection(
      Peer peer,
      Socket peerSocket,
      NetworkToStorageAdapter storageAdapter,
      MessageReader messageReader)
      throws IOException {
    SocketConnector connector =
        new SocketConnector(peer, storageAdapter, messageReader, peerSocket);
    connector.respondToConnection();
    return connector;
  }

  @Override
  protected void initiateConnection() throws IOException {
    this.in = new DataInputStream(peerSocket.getInputStream());
    this.out = new DataOutputStream(peerSocket.getOutputStream());

    this.out.write(constructHandShakeMessage(this.peer));

    // read and confirm handshake from peer
    byte[] incomingHandshakeBuffer = new byte[68];
    int readByteLength = in.read(incomingHandshakeBuffer);
    if (readByteLength != 68 || !confirmHandShake(incomingHandshakeBuffer, this.peer)) {
      throw new IOException(
          String.format("Invalid handshake from peer id=%s", this.peer.getPeerID().getId()));
    }
    new Thread(
            () -> {
              while (true) {}
            })
        .start();
  }

  @Override
  protected void respondToConnection() throws IOException {
    this.in = new DataInputStream(peerSocket.getInputStream());
    this.out = new DataOutputStream(peerSocket.getOutputStream());

    this.out.write(constructHandShakeMessage(this.peer));
  }

  @Override
  public void writeMessage(PeerMessage message) throws IOException {
    out.write(message.toBytes());
  }

  private static byte[] constructHandShakeMessage(Peer peer) {
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

  @Override
  public void close() throws Exception {
    peerSocket.close();
  }
}
