package fr.thumbnailsdb;

import fr.thumbnailsdb.lsh.KbitLSH;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by mohannad on 05/12/15.
 */
public class KbitLSHTest {

    @Test
    public void testHash(){
        KbitLSH klsh = new KbitLSH(5,10);
        String test = "1010111010";
        String res =  klsh.hash(test);
        Assert.assertEquals(res.length(),5);
    }
}
