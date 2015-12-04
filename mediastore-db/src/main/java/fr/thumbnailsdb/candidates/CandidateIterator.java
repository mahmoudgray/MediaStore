package fr.thumbnailsdb.candidates;

import java.util.Iterator;

/**
 * Created by fhuet on 26/04/2014.
 */
public class CandidateIterator implements Iterator {

    private Iterator<DCandidate> it;
    private DCandidate current;

    public CandidateIterator(Iterator it) {
        this.it = it;
    }
    @Override
    public boolean hasNext() {
        return this.it.hasNext();
    }

    @Override
    public Candidate next() {
        current = this.it.next();
        if (current!=null) {
            return current.getCandidate();
        }
        return null;
    }

    public double distance() {
        if (current!=null) {
            return current.getDistance();
        }
        return -1;

    }

    @Override
    public void remove() {
        this.it.remove();

    }
}