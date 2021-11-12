package edu.rice.owltorrent.network.peerconnector;

import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import edu.rice.owltorrent.network.MessageHandler;
import edu.rice.owltorrent.network.PeerConnector;
import edu.rice.owltorrent.network.PeerConnectorFactory;
import edu.rice.owltorrent.network.TorrentManager;
import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;

/** @author yunlyu */
public class SocketChannelConnectorFactory implements PeerConnectorFactory {
    private final Selector connectionSelector;
    private final Selector readWriteSelector;

    private final ExecutorService threadPoolExecutor;

    /**
     * Initiate all selector threads, gets the selector, which all socketChannel will be registered
     * to.
     *
     * @throws IOException
     */
    public SocketChannelConnectorFactory(ExecutorService executor, Selector cSelector, Selector rwSelector)
            throws IOException {
        threadPoolExecutor = executor;
        connectionSelector = cSelector;
        readWriteSelector = rwSelector;
    }

    public Runnable makeInitiateConnectionTask(
            Peer peer, TorrentManager manager, StorageAdapter storageAdapter) {
        return null;
    }

    @Override
    public PeerConnector makeInitialConnection(TwentyByteId ourPeerId,
                                               Peer peer, MessageHandler handler) throws IOException {
        return new SocketChannelConnector(
                ourPeerId,
                peer,
                handler,
                connectionSelector,
                readWriteSelector);
    }

    @Override
    public PeerConnector makeRespondingConnection(TwentyByteId ourPeerId,
            Peer peer, MessageHandler handler, SocketChannel peerSocket) throws IOException {
        return new SocketChannelConnector(
                ourPeerId,
                peer,
                handler,
                peerSocket,
                readWriteSelector);
    }
}