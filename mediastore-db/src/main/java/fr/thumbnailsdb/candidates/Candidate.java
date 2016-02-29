package fr.thumbnailsdb.candidates;

import fr.thumbnailsdb.utils.FixedBitSet;

import java.io.Serializable;
import java.util.BitSet;


/**
 * Class used for storing candidate images for similarity search with LSH
 * avoid performind DB lookups to get hash values
 */
public class Candidate implements Serializable, Comparable {

    protected int index;
    protected BitSet hash;

    public Candidate() {

    }
    public Candidate(int i, BitSet s) {
        this.index=i;
        this.hash=s;// new FixedBitSet(s);
    }
    public int getIndex() {
        return this.index;
    }
    public BitSet getHash() {
        return this.hash;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return ((Candidate) o).index == this.index;
    }
    @Override
    public int hashCode() {
        return index;
    }
    @Override
    public int compareTo(Object o) {
        return this.index - ((Candidate)o).index;
    }
}
