package edu.rice.owltorrent.network.selectorhandler;

import edu.rice.owltorrent.network.SelectorHandler;
import edu.rice.owltorrent.network.task.InitiatingReadHSTask;
import edu.rice.owltorrent.network.task.InitiatingWriteHSTask;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;

/**
 * Selector handler to deal with handshakes we initiate
 *
 * @author Max Yu
 */
@RequiredArgsConstructor
public class InitiatingHandShakeHandler implements SelectorHandler {
  private enum State {
    INIT,
    WAITING_HANDSHAKE_BACK,
    DONE
  }

  private final ExecutorService executorService;
  private State currentState;

  @Override
  public void read() {
    if (currentState == State.WAITING_HANDSHAKE_BACK) {
      executorService.submit(new InitiatingReadHSTask());
      currentState = State.DONE;
    }
  }

  @Override
  public void write() {
    if (currentState == State.INIT) {
      executorService.submit(new InitiatingWriteHSTask());
    }
  }
}
