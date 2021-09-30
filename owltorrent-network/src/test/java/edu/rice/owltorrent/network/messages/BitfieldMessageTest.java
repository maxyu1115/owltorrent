package edu.rice.owltorrent.network.messages;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.BitSet;
import org.junit.Test;

/** @author shijie */
public class BitfieldMessageTest {
  @Test
  public void bytesAndBack() throws IOException {
    GenericMessageTestHelper.binaryAndBackWorks(
        new BitfieldMessage(BitSet.valueOf(new byte[] {(byte) 0b10000000, (byte) 0b11111101})));
  }

  @Test
  public void testBitSetIndex() {
    BitfieldMessage testMessage =
        new BitfieldMessage(
            BitSet.valueOf(new byte[] {(byte) 0b10001000, (byte) 0b10000000, (byte) 0b01000000}));
    assertTrue(testMessage.getBit(0));
    assertTrue(testMessage.getBit(4));
    assertTrue(testMessage.getBit(8));
    assertTrue(testMessage.getBit(17));
    assertFalse(testMessage.getBit(1));
    assertFalse(testMessage.getBit(7));
    assertFalse(testMessage.getBit(16));
  }
}
