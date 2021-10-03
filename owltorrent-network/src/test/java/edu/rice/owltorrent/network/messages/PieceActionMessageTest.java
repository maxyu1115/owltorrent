package edu.rice.owltorrent.network.messages;

import static org.junit.Assert.assertTrue;

import edu.rice.owltorrent.common.entity.Torrent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Test;

/** @author yuchen gu, shijie */
public class PieceActionMessageTest {

  @Test
  public void bytesAndBackRequest() throws IOException {
    GenericMessageTestHelper.binaryAndBackWorks(PieceActionMessage.makeRequestMessage(47, 12, 99));
  }

  @Test
  public void bytesAndBackCancel() throws IOException {
    GenericMessageTestHelper.binaryAndBackWorks(PieceActionMessage.makeCancelMessage(47, 12, 99));
  }

  @Test
  public void testVerifyCorrectType() {
    assertTrue(
        PieceActionMessage.makeRequestMessage(0, 12, 99)
            .verify(new Torrent("", "", 10, new ArrayList<>(), new HashMap<>(), null)));
  }
}
