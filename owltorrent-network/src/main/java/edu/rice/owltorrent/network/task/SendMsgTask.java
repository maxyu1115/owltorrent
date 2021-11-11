package edu.rice.owltorrent.network.task;

import edu.rice.owltorrent.network.SocketChannelConnector;
import java.io.IOException;

public class SendMsgTask implements Runnable {
  private final SocketChannelConnector connector;

  public SendMsgTask(SocketChannelConnector connector) {
    this.connector = connector;
  }

  @Override
  public void run() {
    try {
      connector.processOutgoingMsg();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
