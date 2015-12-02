package fr.thumbnailsdb.lshbuilders;

import fr.thumbnailsdb.Candidate;
import fr.thumbnailsdb.MediaFileDescriptor;
import fr.thumbnailsdb.Status;
import fr.thumbnailsdb.dbservices.DBManager;
import fr.thumbnailsdb.persistentLSH.PersistentLSH;
import org.perf4j.LoggingStopWatch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by mohannad on 02/12/15.
 */
public class LSHManager {
    protected PersistentLSH lsh;
    protected DBManager dbManager;



    public LSHManager(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    public void buildLSH(boolean force) {
        lsh = new PersistentLSH(5, 15, 100);
        if ((force)  || lsh.size() == 0) {
            Status.getStatus().setStringStatus("Teaching LSH");
            System.out.println("LSHManager.buildLSH forced build or empty lsh size : " + lsh.size());
            lsh.clear();
            int total = this.dbManager.size();
            System.out.println("LSHManager.buildLSH db.size =" + total);
            int processed = 0;
            ResultSet res = this.dbManager.getAllInDataBase();

            try {
                while (res.next()) {
                    processed++;
                    if (processed>10000) {
                        System.out.println("LSHManager.buildLSH processed 10 000");
                        processed=0;
                    }
                    int index = res.getInt("ID");
                    String s = res.getString("hash");
                    if (s != null) {
                        lsh.add(s, index);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            lsh.commit();
            Status.getStatus().setStringStatus(Status.IDLE);
        }
    }
    public int[] getLSHStatus() {
        if (lsh == null) {
            buildLSH(false);
        }
        return new int[]{lsh.size(), lsh.lastCandidatesCount()};
    }
    public List<Candidate> findCandidatesUsingLSH(MediaFileDescriptor id) {
        if (lsh == null) {
            buildLSH(false);
        }
        List<Candidate> result = lsh.lookupCandidatesMT(id.getHash());
        System.out.println("Found " + result.size() + " candidates out of " + lsh.size());
        LoggingStopWatch watch = null;
        return result;
    }
    public int size(){
        return lsh.size();
    }
}
