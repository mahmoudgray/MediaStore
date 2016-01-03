package fr.thumbnailsdb;

import com.google.common.collect.ArrayListMultimap;
import fr.thumbnailsdb.dbservices.DBManagerIF;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptor;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptorIF;
import fr.thumbnailsdb.utils.ProgressBar;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/*
    Singleton cache for all the app
 */
public class PreloadedDescriptors {

    private ArrayListMultimap<String, MediaFileDescriptorIF> list;
    private Comparator comp;
    private static PreloadedDescriptors preloadedDescriptors;
    private static boolean useFullPath = false;


    private PreloadedDescriptors(int size, Comparator comp) {
        this.comp = comp;
        this.list = ArrayListMultimap.<String, MediaFileDescriptorIF>create(size, 10);
    }
    public static void setUseFullPath(boolean fullPath){
        useFullPath = fullPath;
    }
    public static  PreloadedDescriptors getPreloadedDescriptors(DBManagerIF dbManagerIF) {
        if (preloadedDescriptors == null) {
            long ti = System.currentTimeMillis();
            Status.getStatus().setStringStatus("Building descriptors list");
            int dbSize = dbManagerIF.size();
            ProgressBar pb = new ProgressBar(0, dbSize, dbSize / 100);
            preloadedDescriptors = new PreloadedDescriptors(dbSize, new Comparator<MediaFileDescriptorIF>() {
                public int compare(MediaFileDescriptorIF o1, MediaFileDescriptorIF o2) {
                    return o1.getMD5().compareTo(o2.getMD5());
                }
            });
            int increment = dbSize / 100;
            int processed = 0;
            int processedSinceLastTick = 0;
            ResultSet res = dbManagerIF.getAllInDataBase();
            try {
                while (res.next()) {
                    processed++;
                    processedSinceLastTick++;
                    if (processedSinceLastTick >= increment) {
                        pb.tick(processed);
                        Status.getStatus().setStringStatus("Building descriptors list  " + pb.getPercent() + "%");
                        processedSinceLastTick = 0;
                    }
                    String path = null;
                    path = res.getString("path");
                    int id = res.getInt("id");
                    String md5 = res.getString("md5");
                    long size = res.getLong("size");
                    String hash = res.getString("hash");
                    if (path != null && md5 != null) {
                        MediaFileDescriptorIF imd = new MediaFileDescriptor(dbManagerIF);
                        if (useFullPath) {
                            imd.setPath(path);
                        }
                        imd.setId(id);
                        imd.setHash(hash);
                        imd.setSize(size);
                        imd.setMd5Digest(md5);
                        imd.setConnection(dbManagerIF.getConnection());
                        preloadedDescriptors.add(imd);
                    } else {
                        //TODO : we should clean the data here
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            System.err.println("PreloadedDescriptors.getPreloadedDescriptors sorting  " + preloadedDescriptors.size() + " data");
            long t0 = System.currentTimeMillis();
            preloadedDescriptors.sort();
            long t1 = System.currentTimeMillis();
            System.err.println("PreloadedDescriptors.getPreloadedDescriptors sorting data .... done after " + (t1 - t0));
            Status.getStatus().setStringStatus(Status.IDLE);
            System.err.println("PreloadedDescriptors.getPreloadedDescriptors all done  " + (t1 - ti));
        }
        return preloadedDescriptors;
    }
    public static boolean preloadedDescriptorsExists() {
        return (preloadedDescriptors != null);
    }
    public static void flushPreloadedDescriptors() {
        if (preloadedDescriptors != null) {
            preloadedDescriptors.clear();
            preloadedDescriptors = null;
        }
    }
    public void add(MediaFileDescriptorIF t) {
        this.list.put(t.getMD5(),t);
    }
    public void remove(MediaFileDescriptorIF t){
        list.remove(t.getMD5() ,t);
    }
    public void removeAll(MediaFileDescriptorIF mediaFileDescriptorIF){
        this.list.removeAll(mediaFileDescriptorIF.getMD5());
    }
    public int size() {
        return list.size();
    }
    public void clear() {
        this.list.clear();
    }
    public Iterator iterator() {
        return list.values().iterator();
    }
    public Iterator keyIterator() {
        return list.keySet().iterator();
    }
    public List<MediaFileDescriptorIF> get(String key) {
        return list.get(key);
    }
    public void sort() {
    }
    public String toString() {
        StringBuffer str = new StringBuffer();
        for (String key : list.keySet()) {
            str.append("[ ");
            str.append(key);
            str.append(" : ");
            str.append(java.util.Arrays.toString(list.get(key).toArray()));
            str.append(" ]");
        }
        return str.toString();
    }

}
