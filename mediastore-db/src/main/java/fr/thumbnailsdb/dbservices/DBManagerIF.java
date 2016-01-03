package fr.thumbnailsdb.dbservices;

import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptorIF;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mohannad on 03/01/16.
 */
public interface DBManagerIF {
    void addDB(String path);

    Connection connectToDB(String path) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException;

    Connection getConnection();

    void checkAndCreateTables() throws SQLException;

    void compact();

    void addIndexPath(String path);

    void deleteIndexedPath(String path);

    ArrayList<String> getIndexedPaths();

    void updateIndexedPath(String current, String newP);

    void saveToDB(MediaFileDescriptorIF mediaFileDescriptorIF);

    void updateToDB(MediaFileDescriptorIF mediaFileDescriptorIF);

    int size();

    boolean isInDataBaseBasedOnName(String path);

    // some difficult dependence problem in deleteFromdatabase
    void deleteFromDatabase(String path);

    int getIndex(String path);

    ResultSet getFromDatabase(String path);

    ResultSet getFromDatabase(int index);

    long getMTime(String path);

    ArrayList<String> getAllWithGPS();

    ResultSet getAllInDataBase();

    ArrayList<MediaFileDescriptorIF> getFromDB(String filter, boolean gps);

    // I think it is a dead method
    String getPath(int[] data);

    String getPath(int index);

    // search in db for indexed files with the same md5 of the param
    ArrayList<MediaFileDescriptorIF> getDuplicatesMD5(MediaFileDescriptorIF mfd);

    void fix();

    void shrink();

    void shrink(List<String> paths);

    void dump(boolean p);
}
