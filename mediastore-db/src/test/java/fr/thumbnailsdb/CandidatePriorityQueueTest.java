package fr.thumbnailsdb;

import fr.thumbnailsdb.candidates.*;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.BitSet;

/**
 * Created by mohannad on 04/12/15.
 */
public class CandidatePriorityQueueTest {
    CandidatePriorityQueue candidatePriorityQueue;
    Candidate c1;
    Candidate c2;

    @BeforeClass
    public void init()  {
        candidatePriorityQueue = new CandidatePriorityQueue(3);
        BitSet b1 = new BitSet();
        BitSet b2 = new BitSet();
        b1.set(0,6);
        b2.set(2);
        b2.set(4);
        b2.set(6);
        c1 = new Candidate(1,b1);
        c2 = new Candidate(2,b2);
    }
    @AfterClass
    public void clear()  {
        candidatePriorityQueue =null;
    }

    @Test
    public void testAdd(){
        candidatePriorityQueue.add(c1,3);
        candidatePriorityQueue.add(c2,2);
        Assert.assertTrue(candidatePriorityQueue.size()==2);
    }

    @Test(dependsOnMethods={"testAdd"})
    public void testPeek(){
        // because it is sorted in reverse order.
        Assert.assertEquals(candidatePriorityQueue.peek(),c1);
    }




}
