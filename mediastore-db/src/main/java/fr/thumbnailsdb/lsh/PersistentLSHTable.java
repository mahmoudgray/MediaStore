package fr.thumbnailsdb.lsh;

import fr.thumbnailsdb.candidates.Candidate;
import fr.thumbnailsdb.lsh.KbitLSH;
import fr.thumbnailsdb.utils.ComparableBitSet;
import org.mapdb.DB;
import org.mapdb.Fun;

import java.util.*;

/**
 * Created by fhuet on 23/04/2014.
 */
public class PersistentLSHTable {

    private KbitLSH hashFunction;
    NavigableSet<Fun.Tuple2<ComparableBitSet, Candidate>> multiMap;

    public PersistentLSHTable(int k, int maxExcluded, int index, DB db) {
         hashFunction = new KbitLSH(k, maxExcluded, index);
        if (db.exists("lsh"+index)) {
            multiMap = db.getTreeSet("lsh"+index);
        }      else {
           multiMap = db.createTreeSet("lsh" + index).counterEnable().make();
        }
    }
    public void add(BitSet hash, int index) {
        BitSet hv = hashFunction.hash(hash);
        multiMap.add(Fun.t2(new ComparableBitSet(hv),new Candidate(index, hash)));
    }
    public List<Candidate> get(BitSet key) {
        List<Candidate> list = new ArrayList<>();
        for(Candidate l: Fun.filter(multiMap, new ComparableBitSet(hashFunction.hash(key)))){
            list.add(l);
        }
        return list;
    }
    public void clear() {
        multiMap.clear();
    }
    public int size() {
        return multiMap.size();
    }
}


