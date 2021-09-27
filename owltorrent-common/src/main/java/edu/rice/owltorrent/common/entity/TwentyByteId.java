package edu.rice.owltorrent.common.entity;

import java.nio.charset.StandardCharsets;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Entity class for Twenty Bytes. This is common in the BitTorrent specification, and making this
 * entity class also makes it easier to use such byte fields as dictionary keys.
 *
 * @author Max Yu
 */
@EqualsAndHashCode
public class TwentyByteId {
  @Getter private final byte[] bytes;

  public TwentyByteId(byte[] bytes) {
    if (bytes.length != 20) {
      throw new IllegalStateException("Incorrect number of bytes");
    }
    this.bytes = bytes;
  }

  @Override
  public String toString() {
    return new String(bytes);
  }

  public static TwentyByteId fromString(String str) {
    return new TwentyByteId(str.getBytes(StandardCharsets.US_ASCII));
  }
}
