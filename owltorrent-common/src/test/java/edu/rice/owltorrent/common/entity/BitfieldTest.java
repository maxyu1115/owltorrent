package edu.rice.owltorrent.common.entity;

import org.junit.Assert;
import org.junit.Test;

public class BitfieldTest {

  @Test
  public void testSize() {
    Bitfield bitfield = new Bitfield(30);
    Assert.assertEquals(4, bitfield.size());
    Assert.assertEquals(30, bitfield.getNumBits());
  }

  @Test
  public void testGetBit() {
    Bitfield bitfield = new Bitfield(30);

    for (int i = 0; i < 30; i++) {
      Assert.assertFalse(bitfield.getBit(i));
      bitfield.setBit(i);
      Assert.assertTrue(bitfield.getBit(i));
    }
    Assert.assertFalse(bitfield.getBit(32));
  }

  @Test
  public void testToByteArray() {
    Bitfield bitfield = new Bitfield(30);

    bitfield.setBit(3);
    bitfield.setBit(0);
    bitfield.setBit(29);

    byte[] expected = new byte[] {(byte) 0b10010000, 0, 0, (byte) 0b00000100};
    Assert.assertArrayEquals(expected, bitfield.toByteArray());
  }
}
