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
}
