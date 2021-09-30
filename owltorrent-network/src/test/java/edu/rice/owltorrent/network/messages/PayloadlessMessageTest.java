package edu.rice.owltorrent.network.messages;

import edu.rice.owltorrent.network.PeerMessage;
import java.io.IOException;
import org.junit.Test;

/** @author shijie */
public class PayloadlessMessageTest {
  @Test
  public void bytesAndBackChoke() throws IOException {
    GenericMessageTestHelper.binaryAndBackWorks(
        new PayloadlessMessage(PeerMessage.MessageType.CHOKE));
  }

  @Test
  public void bytesAndBackUnchoke() throws IOException {
    GenericMessageTestHelper.binaryAndBackWorks(
        new PayloadlessMessage(PeerMessage.MessageType.UNCHOKE));
  }

  @Test
  public void bytesAndBackInterested() throws IOException {
    GenericMessageTestHelper.binaryAndBackWorks(
        new PayloadlessMessage(PeerMessage.MessageType.INTERESTED));
  }

  @Test
  public void bytesAndBackNotInterested() throws IOException {
    GenericMessageTestHelper.binaryAndBackWorks(
        new PayloadlessMessage(PeerMessage.MessageType.NOT_INTERESTED));
  }
}
