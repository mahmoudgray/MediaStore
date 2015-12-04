package fr.thumbnailsdb.candidates;

import fr.thumbnailsdb.utils.FixedBitSet;

import java.io.Serializable;


/**
 * Class used for storing candidate images for similarity search with LSH
 * avoid performind DB lookups to get hash values
 */
public class Candidate implements Serializable, Comparable {

    protected int index;
    protected FixedBitSet hash;

    public Candidate() {

    }
    public Candidate(int i, String s) {
        this.index=i;
        this.hash= new FixedBitSet(s);
    }
    public int getIndex() {
        return this.index;
    }
    public String getHash() {
        return this.hash.toString();
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
