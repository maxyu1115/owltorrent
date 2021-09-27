package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.adapters.NetworkToStorageAdapter;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.network.messagereader.SingleThreadBlockingMessageReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import lombok.extern.log4j.Log4j2;

/**
 * Socket implementation of PeerConnector class.
 *
 * @author Lorraine Lyu, Max Yu
 */
@Log4j2(topic = "general")
public class SocketConnector extends PeerConnector {
  private final Socket peerSocket;
  private DataOutputStream out;
  private DataInputStream in;

  private final Runnable listenForInput =
      new Runnable() {
        @Override
        public void run() {
          ReadableByteChannel channel = Channels.newChannel(in);
          while (true) {
            try {
              PeerMessage message = messageReader.readMessage(channel);
              handleMessage(message);
            } catch (IOException e) {
              // TODO: add more error handling.
              log.error(e);
            }
          }
        }
      };

  private SocketConnector(
      Peer peer,
      NetworkToStorageAdapter storageAdapter,
      MessageReader messageReader,
      Socket peerSocket) {
    super(peer, storageAdapter, messageReader);
    this.peerSocket = peerSocket;
  }

  public static SocketConnector initiateConnection(
      Peer peer, NetworkToStorageAdapter storageAdapter) throws IOException {
    SocketConnector connector =
        new SocketConnector(
            peer,
            storageAdapter,
            new SingleThreadBlockingMessageReader(),
            new Socket(peer.getAddress().getAddress(), peer.getAddress().getPort()));
    connector.initiateConnection();
    return connector;
  }

  public static SocketConnector respondToConnection(
      Peer peer, Socket peerSocket, NetworkToStorageAdapter storageAdapter) throws IOException {
    SocketConnector connector =
        new SocketConnector(
            peer, storageAdapter, new SingleThreadBlockingMessageReader(), peerSocket);
    connector.respondToConnection();
    return connector;
  }

  @Override
  protected void initiateConnection() throws IOException {
    this.in = new DataInputStream(peerSocket.getInputStream());
    this.out = new DataOutputStream(peerSocket.getOutputStream());

    this.out.write(PeerMessage.constructHandShakeMessage(this.peer));

    // read and confirm handshake from peer
    byte[] incomingHandshakeBuffer = new byte[PeerMessage.HANDSHAKE_BYTE_SIZE];
    int readByteLength = in.read(incomingHandshakeBuffer);
    if (readByteLength != PeerMessage.HANDSHAKE_BYTE_SIZE
        || !PeerMessage.confirmHandShake(incomingHandshakeBuffer, this.peer)) {
      throw new IOException(
          String.format("Invalid handshake from peer id=%s", this.peer.getPeerID()));
    }
    // listen for input with busy waiting
    new Thread(listenForInput).start();
  }

  @Override
  protected void respondToConnection() throws IOException {
    this.in = new DataInputStream(peerSocket.getInputStream());
    this.out = new DataOutputStream(peerSocket.getOutputStream());

    this.out.write(PeerMessage.constructHandShakeMessage(this.peer));
    // listen for input with busy waiting
    new Thread(listenForInput).start();
  }

  @Override
  public void writeMessage(PeerMessage message) throws IOException {
    out.write(message.toBytes());
  }

  @Override
  public void close() throws Exception {
    peerSocket.close();
  }
}
