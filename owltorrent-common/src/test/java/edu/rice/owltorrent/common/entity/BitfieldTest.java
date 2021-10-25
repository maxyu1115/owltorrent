package edu.rice.owltorrent.common.entity;

import java.util.BitSet;
import org.junit.Assert;
import org.junit.Test;

public class BitfieldTest {
  private static BitSet bitSet1 = BitSet.valueOf(new byte[] {(byte) 0b00000100, (byte) 0b01000100});
  private static BitSet bitSet2 =
      BitSet.valueOf(new byte[] {(byte) 0b00000000, (byte) 0b00000100, (byte) 0b01100100});
  private static Bitfield bitfield1 = new Bitfield(bitSet1);
  private static Bitfield bitfield2 = new Bitfield(bitSet2);

  @Test
  public void testSize() {
    Assert.assertEquals(bitSet1.size(), bitfield1.size());
    Assert.assertEquals(bitSet2.size(), bitfield2.size());
  }

  @Test
  public void testGetBit() {
    for (int i = 0; i < bitSet1.size(); i++) {
      int setIndex = 8 * ((i / 8) * 2 + 1) - 1 - i;
      Assert.assertEquals(bitfield1.getBit(i), bitSet1.get(setIndex));
    }
  }

  @Test
  public void testToByteArray() {
    Assert.assertArrayEquals(bitSet1.toByteArray(), bitfield1.toByteArray());
    Assert.assertArrayEquals(bitSet2.toByteArray(), bitfield2.toByteArray());
  }
}
