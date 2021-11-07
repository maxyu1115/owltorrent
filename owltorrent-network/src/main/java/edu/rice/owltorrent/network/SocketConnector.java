package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.TwentyByteId;
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
@Log4j2(topic = "network")
public class SocketConnector extends PeerConnector {
  private final Socket peerSocket;

  private boolean initiated = false;

  private Thread listenerThread;

  private DataOutputStream out;
  private ReadableByteChannel in;

  private final Runnable listenForInput =
      new Runnable() {
        @Override
        public void run() {
          while (true) {
            try {
              PeerMessage message = handleMessage(in);
              handleMessage(message);
            } catch (InterruptedException e) {
              log.info(e);

              manager.removePeer(peer);
              try {
                close();
              } catch (Exception exception) {
                log.error("Could not close: ", exception);
              }
              return;
            }
          }
        }
      };

  private SocketConnector(
      TwentyByteId ourPeerId,
      Peer peer,
      TorrentManager manager,
      StorageAdapter storageAdapter,
      MessageReader messageReader,
      Socket peerSocket) {
    super(ourPeerId, peer, manager, messageReader);
    this.setStorageAdapter(storageAdapter);
    this.peerSocket = peerSocket;
  }

  /** Called when storageAdapter is unknown to the caller. */
  private SocketConnector(
      TwentyByteId ourPeerId,
      Peer peer,
      TorrentManager manager,
      MessageReader messageReader,
      Socket peerSocket) {
    super(ourPeerId, peer, manager, messageReader);
    this.peerSocket = peerSocket;
  }

  public static SocketConnector makeInitialConnection(
      Peer peer, TorrentManager manager, StorageAdapter storageAdapter) throws IOException {
    return new SocketConnector(
        manager.getOurPeerId(),
        peer,
        manager,
        storageAdapter,
        new SingleThreadBlockingMessageReader(),
        new Socket(peer.getAddress().getAddress(), peer.getAddress().getPort()));
  }

  public static SocketConnector makeRespondingConnection(
      Peer peer, TorrentManager manager, Socket peerSocket) {
    return new SocketConnector(
        manager.getOurPeerId(), peer, manager, new SingleThreadBlockingMessageReader(), peerSocket);
  }

  @Override
  protected void initiateConnection() throws IOException {
    if (initiated) {
      return;
    }
    this.in = Channels.newChannel(new DataInputStream(peerSocket.getInputStream()));
    this.out = new DataOutputStream(peerSocket.getOutputStream());

    this.out.write(PeerMessage.constructHandShakeMessage(this.peer.getTorrent(), ourPeerId));

    // read and confirm handshake from peer
    //    byte[] incomingHandshakeBuffer = new byte[PeerMessage.HANDSHAKE_BYTE_SIZE];
    //    int readByteLength = in.read(incomingHandshakeBuffer);
    //    log.info("Read bytes: " + readByteLength);
    //    if (readByteLength != PeerMessage.HANDSHAKE_BYTE_SIZE
    //        || !PeerMessage.confirmHandShake(incomingHandshakeBuffer, this.peer)) {
    //      throw new IOException(
    //          String.format("Invalid handshake from peer id=%s", this.peer.getPeerID()));
    //    }
    if (!messageReader.handShake(in, peer)) {
      throw new IOException(
          String.format("Invalid handshake from peer id=%s", this.peer.getPeerID()));
    }
    // listen for input with busy waiting
    listenerThread = new Thread(listenForInput);
    listenerThread.start();
    initiated = true;
  }

  @Override
  protected void respondToConnection() throws IOException {
    if (initiated) {
      return;
    }
    this.in = Channels.newChannel(new DataInputStream(peerSocket.getInputStream()));
    this.out = new DataOutputStream(peerSocket.getOutputStream());

    this.out.write(PeerMessage.constructHandShakeMessage(this.peer.getTorrent(), ourPeerId));
    // listen for input with busy waiting
    listenerThread = new Thread(listenForInput);
    listenerThread.start();
    initiated = true;
  }

  @Override
  public void writeMessage(PeerMessage message) throws IOException {
    log.info("Writing: {}", message);
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
