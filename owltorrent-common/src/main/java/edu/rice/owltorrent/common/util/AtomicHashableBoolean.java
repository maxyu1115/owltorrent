package edu.rice.owltorrent.common.util;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Atomic Boolean where hash and equals only rely on value.
 *
 * @author Max Yu
 */
public final class AtomicHashableBoolean extends AtomicBoolean {

  public AtomicHashableBoolean(boolean b) {
    super(b);
  }

  @Override
  public int hashCode() {
    return Boolean.valueOf(this.get()).hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final AtomicHashableBoolean that = (AtomicHashableBoolean) o;
    return this.get() == that.get();
  }
}
