package edu.rice.owltorrent.network.socketconnector;

import edu.rice.owltorrent.common.adapters.TaskExecutor.Task;
import edu.rice.owltorrent.network.PeerMessage;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/** @author Max Yu */
@RequiredArgsConstructor
@Log4j2(topic = "network")
public class SocketWriteMsgTask implements Task {
  private final SocketConnector socketConnector;

  @Override
  public void run() {
    PeerMessage msg = socketConnector.getOutQueue().poll();
    try {
      if (msg != null) {
        socketConnector.writeMessage(msg);
      } else {
        log.error("Outgoing message queue empty despite SocketWriteMsgTask");
      }
    } catch (IOException ioException) {
      socketConnector.getOutQueue().add(msg);
    }
  }
}
