package edu.rice.owltorrent.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/** @author Lorraine Lyu */
@Log4j2(topic = "network")
@RequiredArgsConstructor
public class HandShakeListener implements Runnable {
  private final TorrentRepository torrentRepository;
  private final int port;

  @Override
  public void run() {
    try (ServerSocketChannel serverSocket = ServerSocketChannel.open()) {
      serverSocket.bind(new InetSocketAddress(port));
      serverSocket.configureBlocking(true);
      while (true) {
        try {
          SocketChannel clientSocketChannel = serverSocket.accept();
          log.info("Incoming network connection");
          ClientHandler handler = new ClientHandler(torrentRepository, clientSocketChannel);
          new Thread(handler).start();
        } catch (IOException ioException) {
          ioException.printStackTrace();
          log.error(
              "Exception caught when trying to listen on port "
                  + port
                  + " or listening for a connection");
          log.error(ioException.getMessage());
        }
      }
    } catch (IOException e) {
      log.error("Cannot initiate local server.");
      e.printStackTrace();
    }
  }
}
