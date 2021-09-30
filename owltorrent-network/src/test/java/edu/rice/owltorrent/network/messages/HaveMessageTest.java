package edu.rice.owltorrent.network.messages;

import java.io.IOException;
import org.junit.Test;

/** @author yuchengu */
public class HaveMessageTest {

  @Test
  public void bytesAndBack() throws IOException {
    GenericMessageTestHelper.binaryAndBackWorks(new HaveMessage(10));
  }
}
