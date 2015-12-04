package fr.thumbnailsdb;

import fr.thumbnailsdb.candidates.Candidate;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by mohannad on 04/12/15.
 */
public class CandidateTest {

    @Test
    public void candidateEqualityTest(){
        Candidate c1 = new Candidate(1,"1111111");
        Candidate c2 = new Candidate(1,"1111111");
        Candidate c3 = new Candidate(2,"00000");
        Assert.assertEquals(c1,c2);
        Assert.assertNotEquals(c2,c3);

    }
}
