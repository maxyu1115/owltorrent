package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.Peer;
import java.io.IOException;
import java.net.Socket;

/** @author Max Yu */
public interface PeerConnectorFactory {
  PeerConnector makeInitialConnection(
      Peer peer, TorrentManager manager, StorageAdapter storageAdapter) throws IOException;

  PeerConnector makeRespondingConnection(Peer peer, TorrentManager manager, Socket peerSocket);
}
