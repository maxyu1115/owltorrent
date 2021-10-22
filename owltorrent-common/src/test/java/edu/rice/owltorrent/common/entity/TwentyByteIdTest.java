package edu.rice.owltorrent.common.entity;

import org.junit.Assert;
import org.junit.Test;

/** @author Max Yu */
public class TwentyByteIdTest {

  @Test
  public void testEquals() {
    Assert.assertEquals(
        TwentyByteId.fromString("12345678901234567890"),
        TwentyByteId.fromString("12345678901234567890"));
  }

  @Test
  public void testHashcodeEquals() {
    Assert.assertEquals(
        TwentyByteId.fromString("12345678901234567890").hashCode(),
        TwentyByteId.fromString("12345678901234567890").hashCode());
  }

  @Test
  public void testUrl() throws Exception {
    byte[] testArray = new byte[20];
    for (int i = 0; i < 20; i++) {
      testArray[i] = (byte) 0xff;
    }
    TwentyByteId hash = new TwentyByteId(testArray);

    Assert.assertEquals(
        hash.hexEncodeURL(), "%ff%ff%ff%ff%ff%ff%ff%ff%ff%ff%ff%ff%ff%ff%ff%ff%ff%ff%ff%ff");
  }
}
