package fr.thumbnailsdb.persistentLSH;

import fr.thumbnailsdb.candidates.Candidate;
import fr.thumbnailsdb.lsh.KbitLSH;
import org.mapdb.DB;
import org.mapdb.Fun;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

/**
 * Created by fhuet on 23/04/2014.
 */
public class PersistentLSHTable {

    private KbitLSH hashFunction;
    NavigableSet<Fun.Tuple2<String, Candidate>> multiMap;

    public PersistentLSHTable(int k, int maxExcluded, int index, DB db) {
         hashFunction = new KbitLSH(k, maxExcluded, index);
        if (db.exists("lsh"+index)) {
            multiMap = db.getTreeSet("lsh"+index);
        }      else {
           multiMap = db.createTreeSet("lsh" + index).counterEnable().make();
        }
    }

    public void add(String hash, int index) {
        String hv = hashFunction.hash(hash);
        multiMap.add(Fun.t2(hv,new Candidate(index, hash)));
    }

    public List<Candidate> get(String key) {
        List<Candidate> list = new ArrayList<>();
        for(Candidate l: Fun.filter(multiMap, hashFunction.hash(key))){
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
