package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.FileBlock;
import edu.rice.owltorrent.common.entity.FileBlockInfo;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import edu.rice.owltorrent.common.util.Exceptions;
import edu.rice.owltorrent.network.messages.PieceActionMessage;
import edu.rice.owltorrent.network.messages.PieceMessage;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * PeerConnector class handling the connections regarding a remote peer.
 *
 * @author Lorraine Lyu, Max Yu
 */
@Log4j2(topic = "network")
public abstract class PeerConnector implements AutoCloseable {
  protected TwentyByteId ourPeerId;
  protected Peer peer;
  protected TorrentManager manager;

  @Setter(AccessLevel.PACKAGE)
  protected StorageAdapter storageAdapter;

  protected MessageReader messageReader;

  public PeerConnector(
      TwentyByteId ourPeerId, Peer peer, TorrentManager manager, MessageReader messageReader) {
    this.ourPeerId = ourPeerId;
    this.peer = peer;
    this.manager = manager;
    this.messageReader = messageReader;
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
        break;
      case REQUEST:
        if (!((PieceActionMessage) message).verify(manager.getTorrent())) {
          log.error("Invalid Request Messgae");
          // TODO: close connection?
          break;
        }
        // Verify if the piece exists
        int index = ((PieceActionMessage) message).getIndex();
        int begin = ((PieceActionMessage) message).getBegin();
        int length = ((PieceActionMessage) message).getLength();
        FileBlock piece;
        try {
          piece = storageAdapter.read(new FileBlockInfo(index, begin, length));
          // Send the piece
          writeMessage(
              new PieceMessage(
                  piece.getPieceIndex(), piece.getOffsetWithinPiece(), piece.getData()));
        } catch (Exceptions.IllegalByteOffsets | IOException blockReadException) {
          log.error(blockReadException);
          // TODO: close connection?
        }
        break;
      case PIECE:
        FileBlock fileBlock = ((PieceMessage) message).getFileBlock();
        if (manager.validateAndReportBlockInProgress(peer, fileBlock)) {
          try {
            log.info(
                "Trying to write file block "
                    + fileBlock.getPieceIndex()
                    + " "
                    + fileBlock.getOffsetWithinPiece());
            storageAdapter.write(fileBlock);
            manager.reportBlockCompletion(fileBlock);
          } catch (Exceptions.IllegalByteOffsets | IOException blockWriteException) {
            log.error(blockWriteException);
            manager.reportBlockFailed(fileBlock);
          }
        }
        break;
      case CANCEL:
      default:
        throw new IllegalStateException("Unhandled message type");
    }
  }
}
