package fr.thumbnailsdb;

import fr.thumbnailsdb.lsh.KbitLSH;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.BitSet;

/**
 * Created by mohannad on 05/12/15.
 */
public class KbitLSHTest {

    @Test
    public void testHash(){
        BitSet b1 = new BitSet();
        b1.set(0,10,true);

        KbitLSH klsh = new KbitLSH(5,10);

        BitSet res =  klsh.hash(b1);
        Assert.assertEquals(res.cardinality(),5);
    }
}
