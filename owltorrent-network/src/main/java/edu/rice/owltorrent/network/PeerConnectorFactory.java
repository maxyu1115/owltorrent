package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.Peer;
import java.io.IOException;
import java.nio.channels.SocketChannel;

/** @author Max Yu */
public interface PeerConnectorFactory {
  Runnable makeInitiateConnectionTask(
      Peer peer, TorrentManager manager, StorageAdapter storageAdapter);

  PeerConnector makeInitialConnection(
      Peer peer, TorrentManager manager, StorageAdapter storageAdapter) throws IOException;

  PeerConnector makeRespondingConnection(
      Peer peer, TorrentManager manager, SocketChannel peerSocket) throws IOException;
}
