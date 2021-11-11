package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Factory for creating peer connectors
 *
 * @author Max Yu
 */
public interface PeerConnectorFactory {
  PeerConnector makeInitialConnection(TwentyByteId ourPeerId, Peer peer, MessageHandler handler)
      throws IOException;

  PeerConnector makeRespondingConnection(
      TwentyByteId ourPeerId, Peer peer, MessageHandler handler, SocketChannel socketChannel)
      throws IOException;
}
