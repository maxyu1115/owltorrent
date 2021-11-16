package edu.rice.owltorrent.network.socketconnector;

import edu.rice.owltorrent.common.adapters.TaskExecutor.Task;
import edu.rice.owltorrent.network.PeerMessage;
import java.io.IOException;
import lombok.RequiredArgsConstructor;

/** @author Max Yu */
@RequiredArgsConstructor
public class SocketWriteMsgTask implements Task {
  private final SocketConnector socketConnector;

  @Override
  public void run() {
    PeerMessage msg = socketConnector.getOutQueue().poll();
    try {
      if (msg != null) {
        socketConnector.writeMessage(msg);
      } else {

      }
    } catch (IOException ioException) {
      socketConnector.getOutQueue().add(msg);
    }
  }
}
