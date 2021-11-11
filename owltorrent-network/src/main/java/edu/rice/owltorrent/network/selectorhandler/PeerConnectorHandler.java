package edu.rice.owltorrent.network.selectorhandler;

import edu.rice.owltorrent.network.SelectorHandler;
import edu.rice.owltorrent.network.SocketChannelConnector;
import java.io.IOException;

/**
 * The wrapper class for PeerConnector. Handles all messages except handshake from peers.
 *
 * @author yunlyu
 */
public class PeerConnectorHandler implements SelectorHandler {
  private final SocketChannelConnector socketChannelConnector;

  public PeerConnectorHandler(SocketChannelConnector connector) {
    this.socketChannelConnector = connector;
  }

  @Override
  public void read() {
    socketChannelConnector.readIncomingMsg();
  }

  @Override
  public void write() throws IOException {
    socketChannelConnector.processOutgoingMsg();
  }
}
