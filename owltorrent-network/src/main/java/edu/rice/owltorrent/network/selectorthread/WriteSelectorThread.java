package edu.rice.owltorrent.network.selectorthread;

import edu.rice.owltorrent.network.SocketChannelConnector;
import lombok.Getter;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

public class WriteSelectorThread implements SelectorThread {
    @Getter
    private final Selector selector;
    private final ThreadPoolExecutor threadPoolExecutor;

    public WriteSelectorThread(ThreadPoolExecutor threadPool) throws IOException {
        selector = Selector.open();
        threadPoolExecutor = threadPool;
    }


    private class WriteMessage implements Runnable {
        private final SocketChannelConnector connector;

        WriteMessage(SocketChannelConnector connector) {
            this.connector = connector;
        }

        @Override
        public void run() {
            try {
                connector.processOutgoingMsg();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Selector getSelector() {
        return null;
    }

    @Override
    public void run() {
        while (true) {
            try {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();

                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {

                    SelectionKey key = keyIterator.next();

                    if (key.isWritable()) {
                        SocketChannelConnector connector = (SocketChannelConnector) key.attachment();
                        Runnable writeMessage = new WriteMessage(connector);
                        threadPoolExecutor.submit(writeMessage);
                    }

                    keyIterator.remove();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
