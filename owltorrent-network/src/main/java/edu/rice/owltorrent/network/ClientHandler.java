package edu.rice.owltorrent.network;

import java.io.*;
import java.net.Socket;
import java.util.Optional;

import edu.rice.owltorrent.common.adapters.NetworkToStorageAdapter;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.Torrent;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "general")
public class ClientHandler implements Runnable, AutoCloseable {
  private final Socket socket;
  private DataOutputStream out;
  private DataInputStream in;

  ClientHandler(Socket socket) {
    this.socket = socket;
    log.info("connected to peer");
  }

  public void run() {
    try {
      out = new DataOutputStream(socket.getOutputStream());

      // Gets the inputstream of client
      in = new DataInputStream(socket.getInputStream());

      byte[] handShakeBuffer = new byte[68];
      in.read(handShakeBuffer);
      Optional<Peer> peer = verifyHandShake(handShakeBuffer);
      if (peer.isEmpty()) {
        return;
      }
      // TODO: fill in NetworkToStorage adapter and messageReader.
      SocketConnector.respondToConnection(peer.get(), this.socket, null, null);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static Optional<Peer> verifyHandShake(byte[] handShake) {
    // TODO: check if the info_hash valid to confirm that we have the requested piece
    // returns corresponding torrent object and construct peer if verification works.
    return Optional.empty();
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
