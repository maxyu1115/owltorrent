package edu.rice.owltorrent.network;

import com.google.common.annotations.VisibleForTesting;
import edu.rice.owltorrent.common.adapters.TaskExecutor.Task;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;

/** @author Lorraine Lyu */
@Log4j2(topic = "network")
public class ClientHandler implements Task, AutoCloseable {
  private final TorrentRepository torrentRepository;
  private final PeerConnectorFactory peerConnectorFactory;
  private final SocketChannel socketChannel;

  ClientHandler(
      TorrentRepository torrentRepository,
      PeerConnectorFactory peerConnectorFactory,
      SocketChannel socketChannel) {
    this.torrentRepository = torrentRepository;
    this.peerConnectorFactory = peerConnectorFactory;
    this.socketChannel = socketChannel;
    log.debug("connected to peer");
  }

  public void run() {
    try {

      ByteBuffer handShakeBuffer = ByteBuffer.allocate(PeerMessage.HANDSHAKE_BYTE_SIZE);
      if (socketChannel.read(handShakeBuffer) != PeerMessage.HANDSHAKE_BYTE_SIZE) {
        log.warn("Received invalid handshake.");
        return;
      }
      handShakeBuffer.rewind();
      Optional<TorrentManager> torrentManager = verifyHandShake(handShakeBuffer);
      if (torrentManager.isEmpty()) {
        return;
      }

      Peer peer = getPeer(handShakeBuffer, torrentManager.get());
      PeerConnector connector =
          peerConnectorFactory.makeRespondingConnection(
              torrentManager.get().getOurPeerId(),
              peer,
              torrentManager.get().getMessageHandler(),
              this.socketChannel);
      connector.respondToConnection();
      torrentManager.get().addPeer(connector, connector.peer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @VisibleForTesting
  Optional<TorrentManager> verifyHandShake(ByteBuffer handShake) {
    log.info("Received Handshake: {}", Arrays.toString(handShake.array()));
    if (handShake.get() != 19) return Optional.empty();

    byte[] title = "BitTorrent protocol".getBytes(StandardCharsets.US_ASCII);
    byte[] protocol = new byte[PeerMessage.HANDSHAKE_LENGTH_BYTE];
    handShake.get(protocol);
    if (!Arrays.equals(title, protocol)) {
      return Optional.empty();
    }

    byte[] extension = new byte[8];
    handShake.get(extension);

    byte[] infoHash = new byte[20];
    handShake.get(infoHash);
    Optional<TorrentManager> torrentManager =
        torrentRepository.retrieveTorrent(new TwentyByteId(infoHash));
    if (torrentManager.isEmpty()) {
      log.info("Received a request for file that's not being seeded.");
      return Optional.empty();
    }

    return torrentManager;
  }

  private Peer getPeer(ByteBuffer handShake, TorrentManager torrentManager) throws IOException {
    byte[] peerId = new byte[20];
    handShake.get(peerId);
    Peer peer =
        new Peer(
            new TwentyByteId(peerId),
            new InetSocketAddress(
                socketChannel.socket().getInetAddress(), socketChannel.socket().getPort()),
            torrentManager.getTorrent());

    return peer;
  }

  @Override
  public void close() throws Exception {
    try {
      socketChannel.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
