package edu.rice.owltorrent.common.util;

import org.junit.Assert;
import org.junit.Test;

public class AtomicHashableTypeTest {

  @Test
  public void testBooleanEquals() {
    Assert.assertEquals(new AtomicHashableBoolean(true), new AtomicHashableBoolean(true));
    Assert.assertEquals(new AtomicHashableBoolean(false), new AtomicHashableBoolean(false));
  }

  @Test
  public void testBooleanHashEquals() {
    Assert.assertEquals(
        new AtomicHashableBoolean(true).hashCode(), new AtomicHashableBoolean(true).hashCode());
    Assert.assertEquals(
        new AtomicHashableBoolean(false).hashCode(), new AtomicHashableBoolean(false).hashCode());
  }

  @Test
  public void testIntegerEquals() {
    Assert.assertEquals(new AtomicHashableInteger(17), new AtomicHashableInteger(17));
    Assert.assertEquals(new AtomicHashableInteger(2048), new AtomicHashableInteger(2048));
  }

  @Test
  public void testIntegerHashEquals() {
    Assert.assertEquals(
        new AtomicHashableInteger(17).hashCode(), new AtomicHashableInteger(17).hashCode());
    Assert.assertEquals(
        new AtomicHashableInteger(2048).hashCode(), new AtomicHashableInteger(2048).hashCode());
  }
}
