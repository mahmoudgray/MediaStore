package fr.thumbnailsdb.lsh;


import java.util.Random;

/**
 * A k-bit locality sensitive hash function
 *
 */
public class KbitLSH {


    int[] indexes;

    public KbitLSH(int nbBits, int keyLength){
        indexes = new int[nbBits];
        Random r = new Random();
        for (int i = 0; i < nbBits; i++) {
             indexes[i]=r.nextInt(keyLength);
        }
    }

    public KbitLSH(int nbBits, int keyLength, long seed){
        indexes = new int[nbBits];
        Random r = new Random(seed);
        for (int i = 0; i < nbBits; i++) {
            indexes[i]=r.nextInt(keyLength);
        }
    }


    public String hash(String s) {
        String result="";
        for (int i = 0; i < indexes.length; i++) {
           result+=s.charAt(indexes[i]);
        }
        return result;
    }

    public static void main(String[] args) {
        KbitLSH klsh = new KbitLSH(5,10);
        String test = "1010111010";
        String res =  klsh.hash(test);
        System.out.println(res);
    }

}
