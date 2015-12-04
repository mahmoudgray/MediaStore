package fr.thumbnailsdb;

import fr.thumbnailsdb.candidates.*;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
        c1 = new Candidate(1,"1111111");
        c2 = new Candidate(2,"0010101");
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
