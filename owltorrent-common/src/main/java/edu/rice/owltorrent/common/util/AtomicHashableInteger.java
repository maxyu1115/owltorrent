package edu.rice.owltorrent.common.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Atomic Integer where hash and equals only rely on value.
 *
 * @author Max Yu
 */
public final class AtomicHashableInteger extends AtomicInteger {

  public AtomicHashableInteger(int x) {
    super(x);
  }

  @Override
  public int hashCode() {
    return this.get();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final AtomicHashableInteger that = (AtomicHashableInteger) o;
    return this.get() == that.get();
  }
}
