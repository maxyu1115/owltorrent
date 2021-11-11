package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.Peer;
import java.io.IOException;
import java.nio.channels.SocketChannel;

/** @author Max Yu */
public class SocketConnectorFactory implements PeerConnectorFactory {
  public static final SocketConnectorFactory SINGLETON = new SocketConnectorFactory();

  @Override
  public Runnable makeInitiateConnectionTask(
      Peer peer, TorrentManager manager, StorageAdapter storageAdapter) {
    return null;
  }

  @Override
  public PeerConnector makeInitialConnection(
      Peer peer, TorrentManager manager, StorageAdapter storageAdapter) throws IOException {
    return SocketConnector.makeInitialConnection(peer, manager, storageAdapter);
  }

  @Override
  public PeerConnector makeRespondingConnection(
      Peer peer, TorrentManager manager, SocketChannel peerSocket) {
    return SocketConnector.makeRespondingConnection(peer, manager, peerSocket.socket());
  }
}
