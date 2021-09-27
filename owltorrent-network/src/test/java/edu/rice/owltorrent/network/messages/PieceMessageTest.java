package edu.rice.owltorrent.network.messages;

import java.io.IOException;
import org.junit.Test;

/** @author Josh */
public class PieceMessageTest {

  @Test
  public void bytesAndBack() throws IOException {
    GenericMessageTestHelper.binaryAndBackWorks(new PieceMessage(47, 12, new byte[] {1, 6, 42, 0}));
  }
}
