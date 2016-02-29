package fr.thumbnailsdb;

import fr.thumbnailsdb.candidates.Candidate;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.BitSet;

/**
 * Created by mohannad on 04/12/15.
 */
public class CandidateTest {

    @Test
    public void candidateEqualityTest(){
        BitSet b1 = new BitSet();
        BitSet b2 = new BitSet();
        b1.set(0,6);

        Candidate c1 = new Candidate(1,b1);
        Candidate c2 = new Candidate(1,b1);
        Candidate c3 = new Candidate(2,b2);
        Assert.assertEquals(c1,c2);
        Assert.assertNotEquals(c2,c3);

    }
}
