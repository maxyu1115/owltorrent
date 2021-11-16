package edu.rice.owltorrent.network.socketconnector;

import edu.rice.owltorrent.common.adapters.TaskExecutor;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import edu.rice.owltorrent.network.MessageHandler;
import edu.rice.owltorrent.network.PeerConnector;
import edu.rice.owltorrent.network.PeerConnectorFactory;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import lombok.RequiredArgsConstructor;

/**
 * Socket Connector Factory. Socket Connector Factories only have a singleton instance since there
 * are no variations
 *
 * @author Max Yu
 */
@RequiredArgsConstructor
public class SocketConnectorFactory implements PeerConnectorFactory {
  private final TaskExecutor taskExecutor;

  @Override
  public PeerConnector makeInitialConnection(
      TwentyByteId ourPeerId, Peer peer, MessageHandler handler) throws IOException {
    return SocketConnector.makeInitialConnection(ourPeerId, peer, handler, taskExecutor);
  }

  @Override
  public PeerConnector makeRespondingConnection(
      TwentyByteId ourPeerId, Peer peer, MessageHandler handler, SocketChannel socketChannel) {
    return SocketConnector.makeRespondingConnection(
        ourPeerId, peer, handler, taskExecutor, socketChannel.socket());
  }
}
