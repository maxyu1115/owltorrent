package edu.rice.owltorrent.network.messages;

import edu.rice.owltorrent.common.entity.Bitfield;
import java.io.IOException;
import org.junit.Test;

/** @author shijie */
public class BitfieldMessageTest {
  @Test
  public void bytesAndBack() throws IOException {
    Bitfield bitfield = new Bitfield(16);
    bitfield.setBit(0);
    bitfield.setBit(8);
    bitfield.setBit(9);
    bitfield.setBit(10);
    bitfield.setBit(11);
    bitfield.setBit(12);
    bitfield.setBit(13);
    bitfield.setBit(15);
    GenericMessageTestHelper.binaryAndBackWorks(new BitfieldMessage(bitfield));
  }

  @Test
  public void bytesAndBack2() throws IOException {
    Bitfield bitfield = new Bitfield(706);
    for (int i = 0; i < 706; i++) {
      bitfield.setBit(i);
    }
    GenericMessageTestHelper.binaryAndBackWorks(new BitfieldMessage(bitfield));
  }
}
