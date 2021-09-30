package edu.rice.owltorrent.network.messages;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.network.PeerMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Test;

/** @author yuchen gu, shijie */
public class PieceActionMessageTest {

  @Test
  public void bytesAndBackRequest() throws IOException {
    GenericMessageTestHelper.binaryAndBackWorks(
        new PieceActionMessage(PeerMessage.MessageType.REQUEST, 47, 12, 99));
  }

  @Test
  public void bytesAndBackCancel() throws IOException {
    GenericMessageTestHelper.binaryAndBackWorks(
        new PieceActionMessage(PeerMessage.MessageType.CANCEL, 47, 12, 99));
  }

  @Test
  public void testVerifyCorrectType() {
    assertTrue(
        new PieceActionMessage(PeerMessage.MessageType.REQUEST, 0, 12, 99)
            .verify(new Torrent("", "", 10, new ArrayList<>(), new HashMap<>())));
  }

  @Test
  public void testVerifyInCorrectType() {
    assertFalse(
        new PieceActionMessage(PeerMessage.MessageType.CHOKE, 0, 12, 99)
            .verify(new Torrent("", "", 10, new ArrayList<>(), new HashMap<>())));
  }
}
