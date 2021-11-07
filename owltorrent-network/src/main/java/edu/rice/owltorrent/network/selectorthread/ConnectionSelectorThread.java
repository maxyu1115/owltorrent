package edu.rice.owltorrent.network.selectorthread;

import edu.rice.owltorrent.network.SocketChannelConnector;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import lombok.Getter;

/** @author yunlyu */
public class ConnectionSelectorThread implements SelectorThread {
  @Getter private final Selector selector;

  public ConnectionSelectorThread() throws IOException {
    selector = Selector.open();
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

          if (key.isConnectable()) {
            // a connection was established with a remote server.
            SocketChannelConnector connector = (SocketChannelConnector) key.attachment();

            connector.finishConnection();
          }

          keyIterator.remove();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  // TODO: design proper closing behavior regarding peer connectors
}
