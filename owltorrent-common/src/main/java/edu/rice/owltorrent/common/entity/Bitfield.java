package edu.rice.owltorrent.common.entity;

import java.util.BitSet;

public class Bitfield {
  public BitSet bitSet;
  /** For peers who don't send a bitfield message. */
  public static final EmptyBitfield EMPTY_BITFIELD_SINGLETON = new EmptyBitfield();

  public Bitfield(BitSet bitSet) {
    this.bitSet = bitSet;
  }

  public boolean getBit(int index) {
    /*
     The order in the BitSet is 0 at the least significant digit and 7 at the most significant digit for the first byte.
     Therefore, a conversion is necessary to make the most significant bit be at index 0.
     For index i, the sum of i and its actual index in the BitSet is ((i/8) * 2 + 1) * 8 - 1.
    */
    int actualIndex = 8 * ((index / 8) * 2 + 1) - 1 - index;
    return this.bitSet.get(actualIndex);
  }

  /**
   * Note: The return value is NOT the total piece length. Returns the number of bits of space for
   * the bitset.
   */
  public int size() {
    return this.bitSet.size();
  }

  public byte[] toByteArray() {
    return this.bitSet.toByteArray();
  }

  public int cardinality() {
    return this.bitSet.cardinality();
  }

  private static class EmptyBitfield extends Bitfield {

    public EmptyBitfield() {
      super(new BitSet());
    }

    @Override
    public boolean getBit(int index) {
      return true;
    }
  }
}
