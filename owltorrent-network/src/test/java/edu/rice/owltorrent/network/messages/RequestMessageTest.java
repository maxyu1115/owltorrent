package edu.rice.owltorrent.network.messages;

import java.io.IOException;
import org.junit.Test;

/** @author Josh */
public class RequestMessageTest {

  @Test
  public void bytesAndBack() throws IOException {
    GenericMessageTestHelper.binaryAndBackWorks(new RequestMessage(47, 12, 99));
  }
}
