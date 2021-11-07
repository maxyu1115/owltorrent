package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import edu.rice.owltorrent.network.messagereader.SingleThreadBlockingMessageReader;
import edu.rice.owltorrent.network.selectorthread.ConnectionSelectorThread;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class SocketChannelConnectorFactory implements PeerConnectorFactory {
    private final Selector connectionSelector;
    private Selector readSelector;
    private Selector writeSelector;
    private final ConnectionSelectorThread connectionThread;
    private final TwentyByteId ourPeerId;

    /**
     * Initiate all selector threads, gets the selector, which
     * all socketChannel will be registered to.
     * @throws IOException
     */
    SocketChannelConnectorFactory(Boolean socketChannel, TwentyByteId id) throws IOException {
        ourPeerId = id;
        connectionThread = new ConnectionSelectorThread();
        connectionSelector = connectionThread.getSelector();
        new Thread(connectionThread).start();
        // TODO(llyu): add selector threads for read and write.
    }

    @Override
    public PeerConnector makeInitialConnection(Peer peer, TorrentManager manager, StorageAdapter storageAdapter) throws IOException {
        SocketChannelConnector connector = new SocketChannelConnector(ourPeerId, peer, manager, new SingleThreadBlockingMessageReader(), connectionSelector, storageAdapter);
        connector.initiateConnection();
        return connector;
    }

    @Override
    public PeerConnector makeRespondingConnection(Peer peer, TorrentManager manager, SocketChannel peerSocket) throws IOException {
        SocketChannelConnector connector = new SocketChannelConnector(ourPeerId, peer, manager, new SingleThreadBlockingMessageReader(), connectionSelector);
        connector.respondToConnection();
        return connector;
    }
}
