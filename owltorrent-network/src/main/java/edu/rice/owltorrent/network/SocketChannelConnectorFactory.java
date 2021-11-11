package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import edu.rice.owltorrent.network.messagereader.SingleThreadBlockingMessageReader;
import edu.rice.owltorrent.network.selectorthread.ConnectionSelectorThread;
import edu.rice.owltorrent.network.selectorthread.ReadSelectorThread;
import edu.rice.owltorrent.network.selectorthread.SelectorThread;
import edu.rice.owltorrent.network.selectorthread.WriteSelectorThread;
import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;

/** @author yunlyu */
public class SocketChannelConnectorFactory implements PeerConnectorFactory {
  private final Selector connectionSelector;
  private final Selector readSelector;
  private final Selector writeSelector;

  private final SelectorThread connectionThread;
  private final SelectorThread readThread;
  private final SelectorThread writeThread;

  private final TwentyByteId ourPeerId;
  private final ExecutorService threadPoolExecutor;

  /**
   * Initiate all selector threads, gets the selector, which all socketChannel will be registered
   * to.
   *
   * @throws IOException
   */
  public SocketChannelConnectorFactory(TwentyByteId id, ExecutorService executor)
      throws IOException {
    ourPeerId = id;
    threadPoolExecutor = executor;
    connectionThread = new ConnectionSelectorThread(threadPoolExecutor);
    connectionSelector = connectionThread.getSelector();
    new Thread(connectionThread).start();
    // TODO(llyu): add selector threads for read and write.
    readThread = new ReadSelectorThread(threadPoolExecutor);
    readSelector = readThread.getSelector();
    writeThread = new WriteSelectorThread(threadPoolExecutor);
    writeSelector = writeThread.getSelector();

    new Thread(readThread).start();
    new Thread(writeThread).start();
  }

  @Override
  public Runnable makeInitiateConnectionTask(
      Peer peer, TorrentManager manager, StorageAdapter storageAdapter) {
    return null;
  }

  @Override
  public PeerConnector makeInitialConnection(
      Peer peer, TorrentManager manager, StorageAdapter storageAdapter) throws IOException {
    return new SocketChannelConnector(
        ourPeerId,
        peer,
        manager,
        new SingleThreadBlockingMessageReader(),
        storageAdapter,
        connectionSelector,
        readSelector,
        writeSelector);
  }

  @Override
  public PeerConnector makeRespondingConnection(
      Peer peer, TorrentManager manager, SocketChannel peerSocket) throws IOException {
    return new SocketChannelConnector(
        ourPeerId,
        peer,
        manager,
        new SingleThreadBlockingMessageReader(),
        peerSocket,
        readSelector,
        writeSelector);
  }
}
