package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "network")
public class SocketChannelConnector extends PeerConnector {
  //    private ServerSocketChannel socketChannel;
  private SocketChannel socketChannel;
  private final Selector selector;
  private boolean initiated = false;
  private final Queue<PeerMessage> incomingMsg = new ConcurrentLinkedQueue<>();
  private final Queue<PeerMessage> outgoingMsg = new ConcurrentLinkedQueue<>();

  public SocketChannelConnector(
      TwentyByteId ourPeerId,
      Peer peer,
      TorrentManager manager,
      MessageReader messageReader,
      Selector selector) {
    super(ourPeerId, peer, manager, messageReader);
    this.selector = selector;
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
    socketChannel.register(selector, SelectionKey.OP_ACCEPT, this);
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

  void processOutgoingMsg() throws IOException {
    PeerMessage msg = outgoingMsg.poll();
    writeMessage(msg);
  }

  /** Reads message from input stream and puts message onto queue. */
  void readIncomingMsg() {
    PeerMessage message = null;
    try {
      message = messageReader.readMessage(socketChannel);
      incomingMsg.add(message);
      log.info("Received: {}", message);
    } catch (IOException ioException) {
      log.error(ioException);
    }
  }
}
