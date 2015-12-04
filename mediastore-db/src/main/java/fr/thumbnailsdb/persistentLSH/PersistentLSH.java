package fr.thumbnailsdb.persistentLSH;

import fr.thumbnailsdb.candidates.Candidate;
import fr.thumbnailsdb.utils.Configuration;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by fhuet on 23/04/2014.
 */
public class PersistentLSH {


    private static String file = "lsh";
    //
    //   private int nbTables = 10;
    private PersistentLSHTable[] tables;
    private PersistentLSHTable t;
    private int lastCandidatesCount;
    DB db;

    private ExecutorService executorService ;


    public PersistentLSH(int nbTables, int k, int maxExcluded) {
        System.out.println("fr.thumbnailsdb.persistentLSH.PersistentLSH.PersistentLSH");
        executorService = Executors.newFixedThreadPool(nbTables);
        db = DBMaker.newFileDB(new File(file))
                .closeOnJvmShutdown().make();
        //DBMaker.newMemoryDB().make();
        tables = new PersistentLSHTable[nbTables];
        StopWatch stopWatch = null;
        if (Configuration.timing()) {
            stopWatch = new LoggingStopWatch("PersistentLSH");
        }
        for (int i = 0; i < nbTables; i++) {
            //System.out.println("fr.thumbnailsdb.persistentLSH.PersistentLSH.PersistentLSH loading table " + i);
            tables[i] = new PersistentLSHTable(k, maxExcluded, i, db);
            if (Configuration.timing()) {
                stopWatch.lap("PersistentLSH." + i);
            }
        }
        if (Configuration.timing()) {
            stopWatch.stop("PersistentLSH");
        }
    }


    public void add(String key, int value) {
        for (PersistentLSHTable t : tables) {
            t.add(key, value);
        }
    }


    public List<Candidate> lookupCandidates(String key) {
        HashSet<Candidate> hs = new HashSet<>();
        LoggingStopWatch watch = null;
        if (Configuration.timing()) {
            watch = new LoggingStopWatch("lookupCandidates");
        }
        for (PersistentLSHTable t : tables) {
            List<Candidate> r = t.get(key);
            hs.addAll(r);
            if (Configuration.timing()) {
                watch.lap("testLoad.lookupCandidates");
            }
        }
        lastCandidatesCount = hs.size();
        return new ArrayList<Candidate>(hs);
    }


    public List<Candidate> lookupCandidatesMT(String key) {
        //build the list of tasks
        List<Callable<List<Candidate>>> callableList = new ArrayList<>();
        HashSet<Candidate> hs = new HashSet<Candidate>();
        LoggingStopWatch watch = null;
        if (Configuration.timing()) {
            watch = new LoggingStopWatch("lookupCandidatesMT");
        }
        for (PersistentLSHTable t : tables) {
            //fill the list of tasks
            callableList.add(new LookupTask(t, key));
        }
        try {
            List<Future<List<Candidate>>> futureList = executorService.invokeAll(callableList);
            for (Future<List<Candidate>> f : futureList) {
                hs.addAll(f.get());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        lastCandidatesCount = hs.size();
        if (Configuration.timing()) {
            watch.stop();
        }

        return new ArrayList<>(hs);
    }

    class LookupTask implements Callable<List<Candidate>> {

        private PersistentLSHTable table;
        private String key;

        public LookupTask(PersistentLSHTable t, String k) {
            this.table = t;
            this.key = k;
        }

        public List<Candidate> call() throws Exception {
            //System.out.println("fr.thumbnailsdb.persistentLSH.PersistentLSH.LookupTask.call " + table.get(key));
            return table.get(key);
        }
    }


    public int lastCandidatesCount() {
        return lastCandidatesCount;
    }

    public int size() {
        return tables[0].size();
    }

    public void clear() {
        for (PersistentLSHTable t : tables) {
            t.clear();
        }
    }

    public void commit() {
        db.commit();
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
        int nbBits = 20;
        System.out.println("Generating random strings");
        String[] input = new String[max];
        for (int i = 0; i < max; i++) {
            input[i] = randomString(nbBits);
        }
        long usedMemory0 = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        System.out.println("Adding to LSH " + max + " items");
        long t0 = System.currentTimeMillis();
        PersistentLSH lsh = new PersistentLSH(10, 30, nbBits);
        for (int i = 0; i < max; i++) {
//            lsh.add(randomString(100), i);
            lsh.add(input[i], i);
        }
        lsh.commit();
        long t1 = System.currentTimeMillis();
        long usedMemory1 = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        System.out.println("Test took " + (t1 - t0) + " ms");
        System.out.println("Test took " + ((usedMemory1 - usedMemory0) / 1024 / 1024) + " MB");
        String s = randomString(nbBits);
        System.out.println("LSH looking for similar to " + s);
        List<Candidate> result = lsh.lookupCandidates(s);
        System.out.println("Found " + result.size() + " candidates");
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

        System.out.println("PersistentLSH building reference set");
        PersistentLSH lsh = new PersistentLSH(10, 30, 100);
        for (int i = 0; i < input.length; i++) {
            String[] tokens = input[i].split(",");
            lsh.add(tokens[1], Integer.parseInt(tokens[0]));
        }

        System.out.println("PersistentLSH looking for similar to index 15");
        List<Candidate> result = lsh.lookupCandidates(input[14].split(",")[1]);
        for (Candidate i : result) {
            System.out.println("  " + i);
        }
    }

    public static void testLoad(String f) {
        System.out.println("fr.thumbnailsdb.persistentLSH.PersistentLSH.testLoad with file " + f);
        PersistentLSH.file = f;
        PersistentLSH lsh = new PersistentLSH(10, 30, 100);
        //now perform some random lookup
        StopWatch stopWatch = null;
        if (Configuration.timing()) {
            stopWatch = new LoggingStopWatch("testLoad.lookupCandidates");
        }
        for (int i = 0; i < 10; i++) {
//            if (Configuration.timing()) {
//                stopWatch.lap("testLoad.lookupCandidates." +i);
//            }
            lsh.lookupCandidatesMT(randomString(100));
        }
        if (Configuration.timing()) {
            stopWatch.stop("testLoad.lookupCandidates");
//            System.out.println("fr.thumbnailsdb.persistentLSH.PersistentLSH.testLoad total " + stopWatch.getElapsedTime());
        }
    }

    public static void main(String[] args) {
          testLocal();
       // testLoad(args[0]);
    }

}
