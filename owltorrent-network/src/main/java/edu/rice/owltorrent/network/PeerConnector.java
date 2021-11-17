package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * PeerConnector class handling the connections regarding a remote peer.
 *
 * @author Lorraine Lyu, Max Yu
 */
@Log4j2(topic = "network")
public abstract class PeerConnector implements AutoCloseable {
  protected TwentyByteId ourPeerId;
  @Getter protected Peer peer;
  protected MessageHandler messageHandler;

  public PeerConnector(TwentyByteId ourPeerId, Peer peer, MessageHandler messageHandler) {
    this.ourPeerId = ourPeerId;
    this.peer = peer;
    this.messageHandler = messageHandler;
  }

  /**
   * Connects to the remote peer. Normally this would involve handshaking them.
   *
   * @throws IOException when connection fails
   */
  protected abstract void initiateConnection() throws IOException;

  /**
   * Responds to the remote peer's handshake.
   *
   * @throws IOException when connection fails
   */
  protected abstract void respondToConnection() throws IOException;

  public abstract void sendMessage(PeerMessage message) throws IOException;

  protected final void handleMessage(ReadableByteChannel inputStream) throws InterruptedException {
    PeerMessage message = null;
    try {
      message = MessageReader.readMessage(inputStream);
      log.info("Received: {}", message);
    } catch (IOException ioException) {
      log.error(ioException);
    }
    if (message == null) {
      throw new InterruptedException("Connection closed");
    }

    messageHandler.handleMessage(message, this);
  }
}
