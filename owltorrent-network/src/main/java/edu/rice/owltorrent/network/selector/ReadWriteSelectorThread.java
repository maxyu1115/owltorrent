package edu.rice.owltorrent.network.selector;

import edu.rice.owltorrent.network.SelectorHandler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class ReadWriteSelectorThread extends ASelectorThread {
    public ReadWriteSelectorThread(Selector selector) {
        super(selector);
    }

    @Override
    void handleKey(SelectionKey key) throws IOException {
        if (key.isReadable()) {
            SelectorHandler handler = (SelectorHandler) key.attachment();
            if (handler.read()) {
            }

        }
        if (key.isWritable()) {
            SelectorHandler handler = (SelectorHandler) key.attachment();
            handler.write();
        }
    }
}
