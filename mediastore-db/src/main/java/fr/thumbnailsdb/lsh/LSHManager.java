package fr.thumbnailsdb.lsh;

import fr.thumbnailsdb.candidates.Candidate;
import fr.thumbnailsdb.Status;
import fr.thumbnailsdb.dbservices.DBManagerIF;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptorIF;
import org.perf4j.LoggingStopWatch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by mohannad on 02/12/15.
 */
public class LSHManager {
    protected PersistentLSH lsh;
    protected DBManagerIF dbManagerIF;



    public LSHManager(DBManagerIF dbManagerIF) {
        this.dbManagerIF = dbManagerIF;
    }

    public void buildLSH(boolean force) {
        lsh = new PersistentLSH(5, 15, 100);
        if ((force)  || lsh.size() == 0) {
            Status.getStatus().setStringStatus("Teaching LSH");
            System.out.println("LSHManager.buildLSH forced build or empty lsh size : " + lsh.size());
            lsh.clear();
            int total = this.dbManagerIF.size();
            System.out.println("LSHManager.buildLSH db.size =" + total);
            int processed = 0;
            ResultSet res = this.dbManagerIF.getAllInDataBase();

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
    public void clear(){
        lsh.clear();
    }
    public List<Candidate> findCandidatesUsingLSH(MediaFileDescriptorIF mediaFileDescriptorIF) {
        if (lsh == null) {
            buildLSH(false);
        }
        List<Candidate> result = lsh.lookupCandidatesMT(mediaFileDescriptorIF.getHash());
        System.out.println("Found " + result.size() + " candidates out of " + lsh.size());
        LoggingStopWatch watch = null;
        return result;
    }
    public int size(){
        return lsh.size();
    }
}
