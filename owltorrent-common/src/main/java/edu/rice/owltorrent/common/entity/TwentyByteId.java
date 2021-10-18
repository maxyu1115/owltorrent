package edu.rice.owltorrent.common.entity;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import lombok.Getter;

/**
 * Entity class for Twenty Bytes. This is common in the BitTorrent specification, and making this
 * entity class also makes it easier to use such byte fields as dictionary keys.
 *
 * @author Max Yu
 */
public class TwentyByteId {
  @Getter private final byte[] bytes;

  public TwentyByteId(byte[] bytes) {
    if (bytes.length != 20) {
      throw new IllegalStateException("Incorrect number of bytes: " + bytes.length);
    }
    this.bytes = Arrays.copyOf(bytes, 20);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final TwentyByteId that = (TwentyByteId) o;
    return Arrays.equals(bytes, that.bytes);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(bytes);
  }

  @Override
  public String toString() {
    return new BigInteger(1, bytes).toString(16);
  }

  public static TwentyByteId fromString(String str) {
    return new TwentyByteId(str.getBytes(StandardCharsets.UTF_8));
  }
}
