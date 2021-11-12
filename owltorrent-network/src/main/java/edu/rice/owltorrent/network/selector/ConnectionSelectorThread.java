package edu.rice.owltorrent.network.selector;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class ConnectionSelectorThread extends ASelectorThread {
    public ConnectionSelectorThread(Selector selector) {
        super(selector);
    }

    @Override
    void handleKey(SelectionKey key) throws IOException {
        if (key.isConnectable()) {
            // a connection was established with a remote server.
            SocketChannelConnector connector = (SocketChannelConnector) key.attachment();

            connector.finishConnection();
            key.cancel();
        }
    }
}
