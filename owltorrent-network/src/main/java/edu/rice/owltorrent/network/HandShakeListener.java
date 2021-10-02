package edu.rice.owltorrent.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/** @author Lorraine Lyu */
@Log4j2(topic = "general")
@RequiredArgsConstructor
public class HandShakeListener implements Runnable {
  private final TorrentRepository torrentRepository;
  private final int port;

  @Override
  public void run() {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      while (true) {
        try {
          Socket clientSocket = serverSocket.accept();
          ClientHandler handler = new ClientHandler(torrentRepository, clientSocket);
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
