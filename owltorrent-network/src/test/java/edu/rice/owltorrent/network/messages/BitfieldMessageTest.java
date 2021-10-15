package edu.rice.owltorrent.network.messages;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import edu.rice.owltorrent.common.entity.Bitfield;
import java.io.IOException;
import java.util.BitSet;
import org.junit.Test;

/** @author shijie */
public class BitfieldMessageTest {
  @Test
  public void bytesAndBack() throws IOException {
    GenericMessageTestHelper.binaryAndBackWorks(
        new BitfieldMessage(
            new Bitfield(BitSet.valueOf(new byte[] {(byte) 0b10000000, (byte) 0b11111101}))));
  }

  @Test
  public void testBitSetIndex() {
    BitfieldMessage testMessage =
        new BitfieldMessage(
            new Bitfield(
                BitSet.valueOf(
                    new byte[] {(byte) 0b10001000, (byte) 0b10000000, (byte) 0b01000000})));
    assertTrue(testMessage.getBitfield().getBit(0));
    assertTrue(testMessage.getBitfield().getBit(4));
    assertTrue(testMessage.getBitfield().getBit(8));
    assertTrue(testMessage.getBitfield().getBit(17));
    assertFalse(testMessage.getBitfield().getBit(1));
    assertFalse(testMessage.getBitfield().getBit(7));
    assertFalse(testMessage.getBitfield().getBit(16));
  }
}
