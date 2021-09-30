package edu.rice.owltorrent.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "general")
@Deprecated
public class Peer extends Socket {

  private final InetSocketAddress address;

  public Peer(String ipAddress, int port) throws IOException {
    super(ipAddress, port);
    this.address = new InetSocketAddress(ipAddress, port);
    log.info("HEYYYY???");
  }

  public void connect() throws IOException {
    this.connect(address);
  }
}
