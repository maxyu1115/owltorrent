package edu.rice.owltorrent.common.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ProgressBarTest {
  @Test
  public void testNormalBar() {
    ProgressBar bar = new ProgressBar(10, "test");
    assertEquals(bar.getProgressBar(70), "test [=======>   ] 70.00% \r");
  }

  @Test
  public void testNormalBar1() {
    ProgressBar bar = new ProgressBar(10, "test");
    assertEquals(bar.getProgressBar((float) 69.8), "test [=======>   ] 69.80% \r");
  }

  @Test
  public void testZeroBar() {
    ProgressBar bar = new ProgressBar(10, "test");
    assertEquals(bar.getProgressBar(0), "test [>          ] 0.00% \r");
  }

  @Test
  public void testFullBar() {
    ProgressBar bar = new ProgressBar(10, "test");
    assertEquals(bar.getProgressBar(100), "test [==========>] Done! 100%\r");
  }
}
