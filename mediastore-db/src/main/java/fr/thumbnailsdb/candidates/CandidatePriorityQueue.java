package fr.thumbnailsdb.candidates;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created by fhuet on 26/04/2014.
 */
public class CandidatePriorityQueue {

    private  int max;
    //priority queue with custom comparator to reverse sorting order
    private PriorityQueue<DCandidate> queue ;


    public CandidatePriorityQueue(int max) {
        this.max = max;
        queue = new PriorityQueue<>(max,new Comparator<DCandidate>() {
            //	@Override
            public int compare(DCandidate o1, DCandidate o2) {
                double e1 = o1.getDistance();
                double e2 = o2.getDistance();
                //Sorted in reverse order
                return Double.compare(e2, e1);
            }
        });
    }
    public Candidate peek() {
        return queue.peek().getCandidate();
    }
    public int size() {
        return queue.size();
    }

    public boolean add(Candidate o, double distance) {
        if (queue.size() >= max) {
            DCandidate df = queue.peek();
            if (df.getDistance() > distance) {
                queue.remove();
                return queue.add(new DCandidate(new Candidate(o.getIndex(),o.getHash()), distance));
            } else {
                return false;
            }
        }
            return queue.add(new DCandidate(new Candidate(o.getIndex(),o.getHash()), distance));
    }


    public CandidateIterator iterator() {
        return new CandidateIterator(queue.iterator());
    }
    




    
}
