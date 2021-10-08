package edu.rice.owltorrent.common.entity;

import java.util.BitSet;

public class Bitfield {
    public BitSet bitSet;

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
}
