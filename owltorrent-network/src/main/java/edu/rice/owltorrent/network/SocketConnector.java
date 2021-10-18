package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.network.messagereader.SingleThreadBlockingMessageReader;
import lombok.extern.log4j.Log4j2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Socket implementation of PeerConnector class.
 *
 * @author Lorraine Lyu, Max Yu
 */
@Log4j2(topic = "network")
public class SocketConnector extends PeerConnector {
  private final Socket peerSocket;

  private boolean initiated = false;

  private Thread listenerThread;

  private DataOutputStream out;
  private DataInputStream in;

  private final Runnable listenForInput =
      new Runnable() {
        @Override
        public void run() {
          ReadableByteChannel channel = Channels.newChannel(in);
          while (true) {
            handleMessage(channel);
          }
        }
      };

  private SocketConnector(
      Peer peer,
      TorrentManager manager,
      StorageAdapter storageAdapter,
      MessageReader messageReader,
      Socket peerSocket) {
    super(peer, manager, storageAdapter, messageReader);
    this.peerSocket = peerSocket;
  }

  public static SocketConnector makeInitialConnection(
      Peer peer, TorrentManager manager, StorageAdapter storageAdapter) throws IOException {
    return new SocketConnector(
        peer,
        manager,
        storageAdapter,
        new SingleThreadBlockingMessageReader(),
        new Socket(peer.getAddress().getAddress(), peer.getAddress().getPort()));
  }

  public static SocketConnector makeRespondingConnection(
      Peer peer, TorrentManager manager, Socket peerSocket, StorageAdapter storageAdapter) {
    return new SocketConnector(
        peer, manager, storageAdapter, new SingleThreadBlockingMessageReader(), peerSocket);
  }

  @Override
  protected void initiateConnection() throws IOException {
    if (initiated) {
      return;
    }
    this.in = new DataInputStream(peerSocket.getInputStream());
    this.out = new DataOutputStream(peerSocket.getOutputStream());

    this.out.write(PeerMessage.constructHandShakeMessage(this.peer));

    // read and confirm handshake from peer
    byte[] incomingHandshakeBuffer = new byte[PeerMessage.HANDSHAKE_BYTE_SIZE];
    int readByteLength = in.read(incomingHandshakeBuffer);
    log.info("Read bytes: " + readByteLength);
    if (readByteLength != PeerMessage.HANDSHAKE_BYTE_SIZE
        || !PeerMessage.confirmHandShake(incomingHandshakeBuffer, this.peer)) {
      throw new IOException(
          String.format("Invalid handshake from peer id=%s", this.peer.getPeerID()));
    }
    // listen for input with busy waiting
    listenerThread = new Thread(listenForInput);
    listenerThread.start();
  }

  @Override
  protected void respondToConnection() throws IOException {
    if (initiated) {
      return;
    }
    this.in = new DataInputStream(peerSocket.getInputStream());
    this.out = new DataOutputStream(peerSocket.getOutputStream());

    this.out.write(PeerMessage.constructHandShakeMessage(this.peer));
    // listen for input with busy waiting
    listenerThread = new Thread(listenForInput);
    listenerThread.start();
  }

  @Override
  public void writeMessage(PeerMessage message) throws IOException {
    log.info(message);
    out.write(message.toBytes());
  }

  @Override
  public void close() throws Exception {
    if (listenerThread != null) {
      listenerThread.interrupt();
    }
    peerSocket.close();
  }
}
