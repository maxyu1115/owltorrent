package edu.rice.owltorrent.common.entity;

import java.util.BitSet;
import lombok.Getter;
import lombok.ToString;

/**
 * Represents a bitfield.
 *
 * @author Lorraine Lyu
 */
@ToString
public class Bitfield {
  private final BitSet bitSet;
  @Getter private final int numBits;
  /** For peers who don't send a bitfield message. */
  public static final Bitfield EMPTY_BITFIELD_SINGLETON = new EmptyBitfield();

  public Bitfield(int numBits) {
    this.numBits = numBits;
    this.bitSet = new BitSet(numBits);
  }

  public synchronized boolean getBit(int index) {
    return this.bitSet.get(index);
  }

  /**
   * Sets the bit at given index location to true.
   *
   * @param index index of bit
   */
  public synchronized void setBit(int index) {
    this.bitSet.set(index);
  }

  /**
   * Note: The return value is NOT the total piece length. Returns the number of bytes of space for
   * the bitset.
   */
  public int size() {
    int len = numBits / 8;
    if (numBits % 8 > 0) len++;
    return len;
  }

  public byte[] toByteArray() {
    byte[] bitfield = new byte[size()];
    for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i + 1)) {
      bitfield[i / 8] |= 1 << (7 - (i % 8));
    }
    return bitfield;
  }

  /** @return The number of bits set to true in the bitfield. */
  public int cardinality() {
    return this.bitSet.cardinality();
  }

  private static class EmptyBitfield extends Bitfield {

    public EmptyBitfield() {
      super(1);
    }

    @Override
    public boolean getBit(int index) {
      return false;
    }

    @Override
    public void setBit(int index) {
      throw new UnsupportedOperationException("Cannot set bit on an empty bitfield.");
    }
  }
}
