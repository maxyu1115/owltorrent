package edu.rice.owltorrent.network.selectorthread;

import edu.rice.owltorrent.network.SocketChannelConnector;
import edu.rice.owltorrent.network.task.ParseMsgTask;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import lombok.Getter;

public class ReadSelectorThread implements SelectorThread {
  @Getter private final Selector selector;
  private final ExecutorService threadPoolExecutor;

  public ReadSelectorThread(ExecutorService threadPool) throws IOException {
    selector = Selector.open();
    threadPoolExecutor = threadPool;
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

          if (key.isReadable()) {
            SocketChannelConnector connector = (SocketChannelConnector) key.attachment();
            Runnable readMessageThread = new ParseMsgTask(connector);
            threadPoolExecutor.submit(readMessageThread);
          }

          keyIterator.remove();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
