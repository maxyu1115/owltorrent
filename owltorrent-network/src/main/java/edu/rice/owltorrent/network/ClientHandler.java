package edu.rice.owltorrent.network;

import com.google.common.annotations.VisibleForTesting;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;

/** @author Lorraine Lyu */
@Log4j2(topic = "general")
public class ClientHandler implements Runnable, AutoCloseable {
  private final TorrentRepository torrentRepository;
  private final Socket socket;
  private DataOutputStream out;
  private DataInputStream in;

  private TorrentManager torrentManager;

  ClientHandler(TorrentRepository torrentRepository, Socket socket) {
    this.torrentRepository = torrentRepository;
    this.socket = socket;
    log.info("connected to peer");
  }

  public void run() {
    try {
      out = new DataOutputStream(socket.getOutputStream());

      // Gets the inputstream of client
      in = new DataInputStream(socket.getInputStream());

      byte[] handShakeBuffer = new byte[68];
      // TODO: handle read != 68 bytes
      in.read(handShakeBuffer);
      Optional<Peer> peer = verifyHandShake(handShakeBuffer);
      if (peer.isEmpty()) {
        return;
      }
      // TODO: fill in NetworkToStorage adapter and messageReader.
      SocketConnector connector =
          SocketConnector.makeRespondingConnection(peer.get(), null, this.socket, null);
      connector.respondToConnection();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @VisibleForTesting
  Optional<Peer> verifyHandShake(byte[] handShake) {
    if (handShake[0] != 19) return Optional.empty();

    byte[] title = "BitTorrent protocol".getBytes(StandardCharsets.US_ASCII);
    for (int i = 1; i < 20; i++) if (title[i - 1] != handShake[i]) return Optional.empty();

    byte[] infoHash = new byte[20];
    System.arraycopy(handShake, 28, infoHash, 0, 20);
    Optional<TorrentManager> torrentManager =
        torrentRepository.retrieveTorrent(new TwentyByteId(infoHash));
    if (torrentManager.isEmpty()) {
      return Optional.empty();
    }

    this.torrentManager = torrentManager.get();

    byte[] peerId = new byte[20];
    System.arraycopy(handShake, 48, peerId, 0, 20);
    Peer peer = new Peer(new TwentyByteId(peerId), null, this.torrentManager.getTorrent());

    return Optional.of(peer);
  }

  @Override
  public void close() throws Exception {
    try {
      if (out != null) {
        out.close();
      }
      if (in != null) {
        in.close();
        socket.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
