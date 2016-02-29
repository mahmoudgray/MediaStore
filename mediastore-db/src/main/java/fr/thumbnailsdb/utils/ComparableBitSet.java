package fr.thumbnailsdb.utils;

import java.io.Serializable;
import java.util.BitSet;

/**
 * Created by mohannad on 24/02/16.
 */

public class ComparableBitSet implements Comparable<ComparableBitSet>,Serializable {

    private BitSet bitSet;

    public ComparableBitSet(BitSet bitSet) {
        this.bitSet = bitSet;
    }

    public BitSet getBitSet(){
        return this.bitSet;
    }

    @Override
    public int compareTo(ComparableBitSet rhs) {
        if (this.bitSet.equals(rhs.getBitSet())) return 0;
        BitSet xor = (BitSet) this.bitSet.clone();
        xor.xor(rhs.getBitSet());
        int firstDifferent = xor.length() - 1;
        return rhs.getBitSet().get(firstDifferent) ? 1 : -1;

    }
}
