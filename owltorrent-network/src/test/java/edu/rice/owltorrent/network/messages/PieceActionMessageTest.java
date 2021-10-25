package edu.rice.owltorrent.network.messages;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import edu.rice.owltorrent.common.entity.Torrent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
  public void testVerifyCorrect() {
    List<byte[]> testList = new ArrayList<>();
    testList.add(new byte[] {});
    assertTrue(
        PieceActionMessage.makeRequestMessage(0, 12, 99)
            .verify(new Torrent("", "", 1024, testList, new HashMap<>(), null)));
  }

  @Test
  public void testVerifyIncorrect() {
    List<byte[]> testList = new ArrayList<>();
    testList.add(new byte[] {});
    assertFalse(
        PieceActionMessage.makeRequestMessage(0, 12, 20)
            .verify(new Torrent("", "", 30, testList, new HashMap<>(), null)));
  }
}
