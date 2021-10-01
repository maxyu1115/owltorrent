package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.network.messages.PieceMessage;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * PeerConnector class handling the connections regarding a remote peer.
 *
 * @author Lorraine Lyu, Max Yu
 */
@RequiredArgsConstructor
@Log4j2(topic = "general")
public abstract class PeerConnector implements AutoCloseable {
  protected final Peer peer;
  protected final StorageAdapter storageAdapter;

  protected final MessageReader messageReader;

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

  public abstract void writeMessage(PeerMessage message) throws IOException;

  protected final void handleMessage(ReadableByteChannel inputStream) {
    PeerMessage message = null;
    try {
      message = messageReader.readMessage(inputStream);
    } catch (IOException ioException) {
      log.error(ioException);
    }
    if (message == null) {
      // TODO: shutdown
      return;
    }

    handleMessage(message);
  }

  protected final void handleMessage(PeerMessage message) {
    switch (message.getMessageType()) {
      case CHOKE:
        peer.setChoked(true);
        break;
      case UNCHOKE:
        peer.setChoked(false);
        break;
      case INTERESTED:
        peer.setInterested(true);
        break;
      case NOT_INTERESTED:
        peer.setInterested(false);
        break;
      case HAVE:
      case BITFIELD:
      case REQUEST:
        break;
      case PIECE:
        storageAdapter.write(((PieceMessage) message).getFilePiece());
        break;
      case CANCEL:
      default:
        throw new IllegalStateException("Unhandled message type");
    }
  }
}
