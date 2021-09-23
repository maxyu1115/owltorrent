package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.adapters.NetworkToStorageAdapter;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.network.messages.PieceMessage;
import java.io.IOException;
import lombok.RequiredArgsConstructor;

/**
 * PeerConnector class handling the connections regarding a remote peer.
 *
 * @author Lorraine Lyu, Max Yu
 */
@RequiredArgsConstructor
public abstract class PeerConnector {
  protected final Peer peer;
  protected final NetworkToStorageAdapter storageAdapter;

  /**
   * Connects to the remote peer. Normally this would involve handshaking.
   *
   * @throws IOException when connection fails
   */
  public abstract void connect() throws IOException;

  public abstract void writeMessage(PeerMessage message) throws IOException;

  protected void handleMessage(PeerMessage message) {
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
