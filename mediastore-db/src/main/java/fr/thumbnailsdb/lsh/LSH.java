package fr.thumbnailsdb.lsh;


import fr.thumbnailsdb.ThumbStore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class LSH {

    LSHTable[] tables;
    private int lastCandidatesCount;

    public LSH(int nbTables, int k, int maxExcluded) {
        tables = new LSHTable[nbTables];
        for (int i = 0; i < nbTables; i++) {
            tables[i] = new LSHTable(k, maxExcluded);
        }
    }

    public void add(String key, int value) {
        for (LSHTable t : tables) {
            t.add(key, value);
        }
    }

    public List<Integer> lookupCandidates(String key) {
        HashSet<Integer> hs = new HashSet<Integer>();
        for (LSHTable t : tables) {
            List<Integer> r = t.get(key);
            hs.addAll(r);
        }
        lastCandidatesCount=hs.size();
        return new ArrayList<Integer>(hs);
    }

    public int size() {
        return tables[0].size();
    }

    public int lastCandidatesCount() {
          return lastCandidatesCount;
    }


    private static void testLocal() {
        String[] input = new String[]{"1,0000000000000000000000000000000000100000110010111111000100111101111111111011111111111111111111111111",
                "2,1111111111110111101111110010011111011111111001111111101011111110000001111000001011111111111111111111",
                "3,0000100000000101000000000010001000000000100011001110011110111101101001110111100100111111011110111111",
                "4,1111111101111111110001111011100110011110011000011000100111100001000001000100001000001000100001010010",
                "5,1101111111111011111111111111111111110111111100111111110001111111000111000011111100000000110000000000",
                "6,1111111111111111111111111111111111111111111111111111111111111111111111000000000000000000000000000000",
                "7,0000100000000101000000000010001000000000100011001110011110111101101001110111100100111111011110111111",
                "8,1111110000011111100001111111000011111100101111100001111111000111111100011111110001011110000111111100",
                "9,0011111001101111100101110101101111001100110101010011010100101111111011011101000001111011001011111100",
                "10,1111111111111111111111111101111110100011111000000111111100001100000011110001111101111111000111111111",
                "11,1111111111111111111111111101111110100011111000000111111100001100000011110001111101111111000111111111",
                "12,0000000000111010000011111100101111100011111000000111100000011110000101111000000111111000001111111111",
                "13,0000000000111010000011111100101111100011111000000111100000011110000101111000000111111000001111111111",
                "14,0010011000000011000000010000000111111111011111111111111111111001011111110011011100000010100000000110",
                "15,1110000111110000001111011100111000101011100111100110011110011000111001001011101111111111111111111111",
                "16,1111111111011111111101111011111000011111100001101110000111111111011111111111111100000000010000000000",
                "17,1110000111110000001111011100111000101011100111100110011110011000111001001011101111111111111111111111",
                "18,1110000011110000000111011110011101111101110101100111010011011101000101111110001111100001111110001111",
                "19,0000000000111010000011111100101111100011111000000111100000011110000101111000000111111000001111111111",
                "20,1111111111111111111111111101111110100011111000000111111100001100000011110001111101111111000111111111",
                "21,0010011000000011000000010000000111111111011111111111111111111001011111110011011100000010100000000110",
                "22,0000000100001111101000111111001010110101101011011111011110111101111111111011111111100000111111000111",
                "23,1111111111111000111110110011111010111111100010000110100001011000000001101100000111000000011111111111",
                "25,0001100000010111001010011111111000111111000000011101111111111111111111011111111000000000000000000000",
                "26,0000000100001111101000111111001010110101101011011111011110111101111111111011111111100000111111000111",
                "27,1110000111110000001111011100111000101011100111100110011110011000111001001011101111111111111111111111",
                "28,0000000100001111101000111111001010110101101011011111011110111101111111111011111111100000111111000111",
                "29,1111111111111111111111111001111111000011101000000010010000001010000000011110010000000110000100011000",
                "30,1111001111110000001111011110111001000011110101101110101110111111111011010111100110000000000000011101",};

        System.out.println("LSH building reference set");
        LSH lsh = new LSH(10, 30, 100);
        for (int i = 0; i < input.length; i++) {
            String[] tokens = input[i].split(",");
            lsh.add(tokens[1], Integer.parseInt(tokens[0]));
        }

        System.out.println("LSH looking for similar to index 15");
        List<Integer> result = lsh.lookupCandidates(input[14].split(",")[1]);
        for (Integer i : result) {
            System.out.println("  " + i);
        }
    }


    public static void testThumbstore() {
        System.out.println("LSH building reference set");

        ThumbStore thumbstore = new ThumbStore();
        int size = thumbstore.size();
        //   VPTree vpTree = new VPTree();
        LSH lsh = new LSH(10, 30, 100);
        //   ArrayList<MediaFileDescriptor> al = new ArrayList<MediaFileDescriptor>(size);

        ResultSet res = thumbstore.getAllInDataBase();
            try {
                while (res.next()) {
                    // String path = res.getString("path");
                    int index = res.getInt("ID");

                    //  byte[] d = res.getBytes("data");
                    String s = res.getString("hash");
                    if (s != null) {

                        lsh.add(s, index);

                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        System.out.println(" ... done");
    }


    private static String randomString(int max) {
        StringBuilder s = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < max; i++) {
            s.append(r.nextInt(2));
        }
        return s.toString();
    }

    public static void testRandom(int max) {
        int nbBits=100;
        System.out.println("Generating random strings");
        String[] input = new String[max];
        for (int i = 0; i < max; i++) {
            input[i] = randomString(nbBits);
        }
        long usedMemory0 = (Runtime.getRuntime().totalMemory() -Runtime.getRuntime().freeMemory());
        System.out.println("Adding to LSH " + max + " items");
        long t0 = System.currentTimeMillis();
        LSH lsh = new LSH(5, 10, nbBits);
        for (int i = 0; i < max; i++) {
//            lsh.add(randomString(100), i);
            lsh.add(input[i], i);
        }
        long t1 = System.currentTimeMillis();
        long usedMemory1 = (Runtime.getRuntime().totalMemory() -Runtime.getRuntime().freeMemory());        System.out.println("Test took " + (t1 - t0) + " ms");
        System.out.println("Test took " + ((usedMemory1 -usedMemory0)/1024/1024) + " MB");
        String s = randomString(nbBits);
        System.out.println("LSH looking for similar to " + s);
        List<Integer> result = lsh.lookupCandidates(s);
        System.out.println("Found " + result.size() + " candidates");
//        for (Integer i : result) {
//            System.out.println("  " + i);
//        }

    }

    public static void main(String[] args) {

          testLocal();
//        testThumbstore();
        //testRandom(900000);


    }


}
