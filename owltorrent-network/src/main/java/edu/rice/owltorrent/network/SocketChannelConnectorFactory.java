package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import edu.rice.owltorrent.network.messagereader.SingleThreadBlockingMessageReader;
import edu.rice.owltorrent.network.selectorthread.ConnectionSelectorThread;
import edu.rice.owltorrent.network.selectorthread.ReadSelectorThread;
import edu.rice.owltorrent.network.selectorthread.SelectorThread;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadPoolExecutor;

public class SocketChannelConnectorFactory implements PeerConnectorFactory {
    private final Selector connectionSelector;
    private Selector readSelector;
    private Selector writeSelector;
    private final SelectorThread connectionThread;
    private final SelectorThread readThread;
    private final TwentyByteId ourPeerId;
    private final ThreadPoolExecutor threadPoolExecutor;

    /**
     * Initiate all selector threads, gets the selector, which
     * all socketChannel will be registered to.
     * @throws IOException
     */
    SocketChannelConnectorFactory(Boolean socketChannel, TwentyByteId id, ThreadPoolExecutor executor) throws IOException {
        ourPeerId = id;
        connectionThread = new ConnectionSelectorThread();
        connectionSelector = connectionThread.getSelector();
        threadPoolExecutor = executor;
        new Thread(connectionThread).start();
        // TODO(llyu): add selector threads for read and write.
        readThread = new ReadSelectorThread(threadPoolExecutor);
        readSelector = readThread.getSelector();
    }

    @Override
    public PeerConnector makeInitialConnection(Peer peer, TorrentManager manager, StorageAdapter storageAdapter) throws IOException {
        SocketChannelConnector connector = new SocketChannelConnector(ourPeerId, peer, manager, new SingleThreadBlockingMessageReader(), storageAdapter);
        connector.initiateConnection();
        connector.getSocketChannel().register(connectionSelector, SelectionKey.OP_CONNECT, connector);
        return connector;
    }

    @Override
    public PeerConnector makeRespondingConnection(Peer peer, TorrentManager manager, SocketChannel peerSocket) throws IOException {
        SocketChannelConnector connector = new SocketChannelConnector(ourPeerId, peer, manager, new SingleThreadBlockingMessageReader());
        connector.respondToConnection();
        return connector;
    }
}
