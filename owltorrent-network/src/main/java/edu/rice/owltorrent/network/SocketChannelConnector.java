package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "network")
public class SocketChannelConnector extends PeerConnector {
  //    private ServerSocketChannel socketChannel;
  @Getter private SocketChannel socketChannel;
  private StorageAdapter storageAdapter;
  private boolean initiated = false;
  private final Queue<PeerMessage> incomingMsg = new ConcurrentLinkedQueue<>();
  private final Queue<PeerMessage> outgoingMsg = new ConcurrentLinkedQueue<>();

  public SocketChannelConnector(
      TwentyByteId ourPeerId,
      Peer peer,
      TorrentManager manager,
      MessageReader messageReader,
      StorageAdapter storageAdapter) {
    super(ourPeerId, peer, manager, messageReader);
    this.storageAdapter = storageAdapter;
  }

  public SocketChannelConnector(
          TwentyByteId ourPeerId,
          Peer peer,
          TorrentManager manager,
          MessageReader messageReader) {
    super(ourPeerId, peer, manager, messageReader);
  }

  @Override
  protected void initiateConnection() throws IOException {
    //        if (selector == null) {
    //            selector = Selector.open();
    //        }
    //        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
    socketChannel = SocketChannel.open();
    socketChannel.configureBlocking(false);
    socketChannel.bind(peer.getAddress());
    // TODO: see if this works (using the connector as attachment)
  }

  public void finishConnection() throws IOException {
    Boolean result = socketChannel.connect(peer.getAddress());
    if (result) {
      if (!this.messageReader.handShake(this.socketChannel, this.peer)) {
        throw new IOException(
            String.format("Invalid handshake from peer id=%s", this.peer.getPeerID()));
      }
    }
    initiated = true;
  }

  @Override
  protected void respondToConnection() throws IOException {
    if (!initiated) {
      return;
    }
  }

  @Override
  public void writeMessage(PeerMessage message) throws IOException {
    this.socketChannel.write(ByteBuffer.wrap(message.toBytes()));
  }

  @Override
  public void close() throws Exception {
    this.socketChannel.close();
  }

  void addOutGoingMessage(PeerMessage msg) {
    outgoingMsg.add(msg);
  }

  public void processOutgoingMsg() throws IOException {
    PeerMessage msg = outgoingMsg.poll();
    writeMessage(msg);
  }

  /** Reads message from input stream and puts message onto queue. */
  public void readIncomingMsg() {
    PeerMessage message = null;
    try {
      message = handleMessage(socketChannel);
      incomingMsg.add(message);
      log.info("Received: {}", message);
    } catch (InterruptedException ioException) {
      log.error(ioException);
    }
  }
}
