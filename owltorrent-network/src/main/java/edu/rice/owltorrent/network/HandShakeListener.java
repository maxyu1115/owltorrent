package edu.rice.owltorrent.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "general")
public class HandShakeListener {
  static void listenOnPort(int port) {
    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket(port);
    } catch (IOException e) {
      log.error("Cannot initiate local server.");
      e.printStackTrace();
      return;
    }
    while (true) {
      try {
        Socket clientSocket = serverSocket.accept();
        ClientHandler handler = new ClientHandler(clientSocket);
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
  }
}
