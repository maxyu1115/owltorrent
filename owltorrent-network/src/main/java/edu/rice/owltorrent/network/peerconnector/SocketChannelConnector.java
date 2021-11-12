package edu.rice.owltorrent.network.peerconnector;

import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.*;
import edu.rice.owltorrent.common.util.Exceptions;
import edu.rice.owltorrent.network.MessageHandler;
import edu.rice.owltorrent.network.MessageReader;
import edu.rice.owltorrent.network.PeerConnector;
import edu.rice.owltorrent.network.PeerMessage;
import edu.rice.owltorrent.network.messages.PieceActionMessage;
import edu.rice.owltorrent.network.messages.PieceMessage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.message.Message;

@Log4j2(topic = "network")
public class SocketChannelConnector extends PeerConnector {
    //    private ServerSocketChannel socketChannel;
    private SocketChannel socketChannel;
    private boolean initiated = false;
    private final Queue<PeerMessage> outgoingMsg = new ConcurrentLinkedQueue<>();
    private final Selector connectSelector;
    private final Selector readWriteSelector;

    public SocketChannelConnector(
            TwentyByteId ourPeerId,
            Peer peer,
            MessageHandler handler,
            Selector connect,
            Selector rw) {
        super(ourPeerId, peer, handler);
        this.connectSelector = connect;
        this.readWriteSelector = rw;
    }

    public SocketChannelConnector(
            TwentyByteId ourPeerId,
            Peer peer,
            MessageHandler handler,
            SocketChannel socketChannel,
            Selector rw) {
        super(ourPeerId, peer, handler);
        this.socketChannel = socketChannel;
        this.connectSelector = null;
        this.readWriteSelector = rw;
    }

    @Override
    public void initiateConnection() throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.bind(peer.getAddress());
        // TODO: see if this works (using the connector as attachment)
        socketChannel.register(connectSelector, SelectionKey.OP_CONNECT, this);
    }

    public void finishInitiatingConnection() throws IOException {
        boolean result = socketChannel.connect(peer.getAddress());
        socketChannel.configureBlocking(true);
        if (result) {
            socketChannel.write(ByteBuffer.wrap(PeerMessage.constructHandShakeMessage(this.peer.getTorrent(), ourPeerId)));
            if (!MessageReader.handShake(this.socketChannel, this.peer)) {
                throw new IOException(
                        String.format("Invalid handshake from peer id=%s", this.peer.getPeerID()));
            }
        }
        socketChannel.register(readWriteSelector, SelectionKey.OP_READ, this);
        socketChannel.register(readWriteSelector, SelectionKey.OP_WRITE, this);
        initiated = true;
        socketChannel.configureBlocking(false);
    }

    @Override
    public void respondToConnection() throws IOException {
        if (!initiated) {
            return;
        }
    }

    @Override
    public void sendMessage(PeerMessage message) throws IOException {
        outgoingMsg.add(message);
    }

    public void writeMessage(PeerMessage message) throws IOException {
        this.socketChannel.write(ByteBuffer.wrap(message.toBytes()));
    }

    @Override
    public void close() throws Exception {
        // this will also deregister from the selectors
        this.socketChannel.close();
    }

    public boolean hasOutgoingMsg() {
        return !outgoingMsg.isEmpty();
    }

    public void processOutgoingMsg() throws IOException {
        PeerMessage msg = outgoingMsg.poll();
        if (msg != null) {
            writeMessage(msg);
        }
    }

    /** Reads message from input stream and puts message onto queue. */
    public void readIncomingMsg() {
        PeerMessage message = null;
        try {
            messageHandler.handleMessage(message, this);
            log.info("Received: {}", message);
        } catch (InterruptedException ioException) {
            log.error(ioException);
        }
    }
}