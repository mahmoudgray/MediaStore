package fr.thumbnailsdb.utils;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by mohannad on 02/12/15.
 */
public class LimitedQueue<E> extends LinkedBlockingQueue<E> {
    public LimitedQueue(int maxSize) {
        super(maxSize);
    }

    @Override
    public boolean add(E e) {
        return super.add(e);
    }

    @Override
    public boolean offer(E e) {
        //	System.out.println("MediaIndexer.LimitedQueue.offer() " + this.size());
        // turn offer() and add() into a blocking calls (unless interrupted)
        try {
            put(e);
            return true;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        // System.out.println("MediaIndexer.LimitedQueue.offer()   ... done");
        return false;
    }

    @Override
    public E take() throws InterruptedException {
        return super.take();
    }
}
