package edu.rice.owltorrent.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "general")
public class ClientHandler implements Runnable, AutoCloseable {
  private final Socket socket;
  private PrintWriter out;
  private BufferedReader in;

  ClientHandler(Socket socket) {
    this.socket = socket;
    log.error("connected to peer");
  }

  public void run() {
    try {
      out = new PrintWriter(socket.getOutputStream(), true);

      // get the inputstream of client
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      String line;
      while ((line = in.readLine()) != null) {

        // writing the received message from
        // client
        log.error(String.format(" Sent from the client: %s\n", line));
        // out.println(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
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
