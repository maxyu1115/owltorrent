package edu.rice.owltorrent.network.task;

import edu.rice.owltorrent.network.SocketChannelConnector;

public class ParseMsgTask implements Runnable {
  private final SocketChannelConnector connector;

  public ParseMsgTask(SocketChannelConnector connector) {
    this.connector = connector;
  }

  @Override
  public void run() {
    connector.readIncomingMsg();
  }
}
