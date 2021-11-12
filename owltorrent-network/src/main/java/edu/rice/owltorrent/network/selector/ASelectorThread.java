package edu.rice.owltorrent.network.selector;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Lorraine Lyu, Max Yu
 */
public abstract class ASelectorThread implements Runnable {
    protected final Selector selector;
    public ASelectorThread(Selector selector) {
        this.selector = selector;
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
                    handleKey(key);
                    keyIterator.remove();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    abstract void handleKey(SelectionKey key) throws IOException;
}
