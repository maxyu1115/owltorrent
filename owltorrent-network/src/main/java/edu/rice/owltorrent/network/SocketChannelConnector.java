package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

import edu.rice.owltorrent.common.util.Exceptions;
import edu.rice.owltorrent.network.messages.BitfieldMessage;
import edu.rice.owltorrent.network.messages.PayloadlessMessage;
import edu.rice.owltorrent.network.messages.PieceActionMessage;
import edu.rice.owltorrent.network.messages.PieceMessage;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.text.html.Option;

@Log4j2(topic = "network")
public class SocketChannelConnector extends PeerConnector {
  //    private ServerSocketChannel socketChannel;
  @Getter private SocketChannel socketChannel;
  private StorageAdapter storageAdapter;
  private boolean initiated = false;
  private final Queue<PeerMessage> outgoingMsg = new ConcurrentLinkedQueue<>();
  private final Selector connectSelector;
  private final Selector writeSelector;
  private final Selector readSelector;

  public SocketChannelConnector(
      TwentyByteId ourPeerId,
      Peer peer,
      TorrentManager manager,
      MessageReader messageReader,
      StorageAdapter storageAdapter,
      Selector connect,
      Selector read,
      Selector write) {
    super(ourPeerId, peer, manager, messageReader);
    this.storageAdapter = storageAdapter;
    this.connectSelector = connect;
    this.readSelector = read;
    this.writeSelector = write;
  }

  public SocketChannelConnector(
      TwentyByteId ourPeerId,
      Peer peer,
      TorrentManager manager,
      MessageReader messageReader,
      SocketChannel socketChannel,
      Selector read,
      Selector write) {
    super(ourPeerId, peer, manager, messageReader);
    this.socketChannel = socketChannel;
    this.connectSelector = null;
    this.readSelector = read;
    this.writeSelector = write;
  }

  @Override
  protected void initiateConnection() throws IOException {
    socketChannel = SocketChannel.open();
    socketChannel.configureBlocking(false);
    socketChannel.bind(peer.getAddress());
    // TODO: see if this works (using the connector as attachment)
    socketChannel.register(connectSelector, SelectionKey.OP_CONNECT, this);
  }

  public void finishConnection() throws IOException {
    boolean result = socketChannel.connect(peer.getAddress());
    if (result) {
      if (!this.messageReader.handShake(this.socketChannel, this.peer)) {
        throw new IOException(
            String.format("Invalid handshake from peer id=%s", this.peer.getPeerID()));
      }
    }
    socketChannel.register(readSelector, SelectionKey.OP_READ, this);
    socketChannel.register(writeSelector, SelectionKey.OP_WRITE, this);
    initiated = true;
  }

  private Optional<PeerMessage> handleMessageWithoutReply(PeerMessage message) throws InterruptedException {
    switch (message.getMessageType()) {
      case REQUEST:
        if (!peer.isPeerInterested() || peer.isAmChoked()) break;
        if (!message.verify(manager.getTorrent())) {
          log.error("Invalid Request Messgae");
          throw new InterruptedException("Invalid Request Messgae, Connection closed");
        }
        // Verify if the piece exists
        int index = ((PieceActionMessage) message).getIndex();
        int begin = ((PieceActionMessage) message).getBegin();
        int length = ((PieceActionMessage) message).getLength();
        FileBlock piece;
        try {
          piece = storageAdapter.read(new FileBlockInfo(index, begin, length));
          // Send the piece
          return Optional.of(
                  new PieceMessage(
                          piece.getPieceIndex(), piece.getOffsetWithinPiece(), piece.getData()));
        } catch (Exceptions.IllegalByteOffsets | IOException blockReadException) {
          log.error(blockReadException);
          throw new InterruptedException("Invalid block read, Connection closed");
        }
      default:
        handleMessage(message);
    }
    return Optional.empty();
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
    // this will also deregister from the selectors
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
      message = parseMessage(socketChannel);
      Optional<PeerMessage> reply = handleMessageWithoutReply(message);
      if (reply.isPresent()) {
        addOutGoingMessage(reply.get());
      }
      log.info("Received: {}", message);
    } catch (InterruptedException ioException) {
      log.error(ioException);
    }
  }
}
