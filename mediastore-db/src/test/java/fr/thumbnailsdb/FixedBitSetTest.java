package fr.thumbnailsdb;

import fr.thumbnailsdb.utils.FixedBitSet;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by mohannad on 04/12/15.
 */
public class FixedBitSetTest {

    @Test
    public void testHashToBitSitConversion(){
        String [] hash = {"11111111000000","00000000000000","1111111111111"};
        for(int i=0;i<hash.length ; i++) {
            FixedBitSet fixedBitSet = new FixedBitSet(hash[i]);
            Assert.assertTrue(hash[i].equals(fixedBitSet.toString()));
        }
    }

    @Test
    public void testHashFormate(){
        try{
            FixedBitSet fixedBitSet = new FixedBitSet("10000f55555");
            Assert.assertTrue(false);
        }catch(RuntimeException e){}
    }
}
