package fr.thumbnailsdb.lsh;

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

    private PersistentLSHTable[] tables;
    private int lastCandidatesCount;
    DB db;

    private ExecutorService executorService ;


    public PersistentLSH(int nbTables, int k, int maxExcluded) {
        System.out.println("PersistentLSH Constructor");
        executorService = Executors.newFixedThreadPool(nbTables);
        db = DBMaker.newFileDB(new File(file)).closeOnJvmShutdown().make();
        tables = new PersistentLSHTable[nbTables];
        StopWatch stopWatch = null;
        if (Configuration.timing()) {
            stopWatch = new LoggingStopWatch("PersistentLSH");
        }
        for (int i = 0; i < nbTables; i++) {
            tables[i] = new PersistentLSHTable(k, maxExcluded, i, db);
            if (Configuration.timing()) {
                stopWatch.lap("PersistentLSH." + i);
            }
        }
        if (Configuration.timing()) {
            stopWatch.stop("PersistentLSH");
        }
    }
    public void add(BitSet key, int value) {
        for (PersistentLSHTable t : tables) {
            t.add(key, value);
        }
    }
    public List<Candidate> lookupCandidates(BitSet key) {
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
        return new ArrayList<>(hs);
    }
    public List<Candidate> lookupCandidatesMT(BitSet key) {
        //build the list of tasks
        List<Callable<List<Candidate>>> callableList = new ArrayList<>();
        HashSet<Candidate> candidateHashSet = new HashSet<Candidate>();
        LoggingStopWatch watch = null;
        if (Configuration.timing()) {
            watch = new LoggingStopWatch("lookupCandidatesMT");
        }
        for (PersistentLSHTable persistentLSHTable : tables) {
            //fill the list of tasks
            callableList.add(new LookupTask(persistentLSHTable, key));
        }
        try {
            List<Future<List<Candidate>>> futureList = executorService.invokeAll(callableList);
            for (Future<List<Candidate>> f : futureList) {
                candidateHashSet.addAll(f.get());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        lastCandidatesCount = candidateHashSet.size();
        if (Configuration.timing()) {
            watch.stop();
        }

        return new ArrayList<>(candidateHashSet);
    }
    class LookupTask implements Callable<List<Candidate>> {

        private PersistentLSHTable table;
        private BitSet key;

        public LookupTask(PersistentLSHTable t, BitSet k) {
            this.table = t;
            this.key = k;
        }

        public List<Candidate> call() throws Exception {
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


}
