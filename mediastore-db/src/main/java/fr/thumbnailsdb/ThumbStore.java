package fr.thumbnailsdb;

import fr.thumbnailsdb.persistentLSH.PersistentLSH;
import fr.thumbnailsdb.utils.Logger;
import fr.thumbnailsdb.utils.ProgressBar;
import org.perf4j.LoggingStopWatch;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ThumbStore {

    protected static String DEFAULT_DB = "localDB";
    protected static int CURRENT_VERSION = 5;
    // its better to revert dependency
    //This is used as a cache of preloaded descriptors
    protected PreloadedDescriptors preloadedDescriptors;
    // protected LSH lsh;  // dependency must be reverted
    protected PersistentLSH lsh;
    protected Connection connection;

    public ThumbStore() {
        this(DEFAULT_DB);

    }
    public ThumbStore(String path) {
        if (path == null) {
            path = DEFAULT_DB;
        }
        System.err.println("ThumbStore.ThumbStore() using " + path + " as DB");
        this.addDB(path);
    }
    public void addDB(String path) {
        try {
            this.connection = connectToDB(path);
            checkAndCreateTables();
            ArrayList<String> paths = getIndexedPaths();
            if (paths.size() == 0) {
                System.err.println("ThumbStore.addDB found empty db");

            } else {
                for (String s : paths) {
                    System.err.println("ThumbStore.addDB path : " + s + " with db : " + this.connection);

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
    private Connection getConnection() {
        return this.connection;
    }
    public Connection connectToDB(String path) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver").newInstance();
        Connection connection = DriverManager.getConnection("jdbc:h2:" + path + "", "sa", "");

        return connection;
    }
    public void checkAndCreateTables() throws SQLException {

        DatabaseMetaData dbm = this.connection.getMetaData();
        ResultSet tables = dbm.getTables(null, null, "IMAGES", null);
        if (tables.next()) {
            Logger.getLogger().log("ThumbStore.checkAndCreateTables() table IMAGES exists!");
            checkOrAddColumns(dbm);
            // Table exists
        } else {
            Logger.getLogger().log("ThumbStore.checkAndCreateTables() table IMAGES does not exist, should create it");
            String table = "CREATE TABLE IMAGES(id  bigint identity(1,1),path varchar(256), path_id int, size long, mtime long, md5 varchar(256), hash varchar(100),  lat double, lon double);";
            //"CREATE TABLE IMAGES(id  bigint identity(1,1),path varchar(256), size long, mtime long, md5 varchar(256), hash varchar(100),  lat double, lon double)";
            Statement st = this.connection.createStatement();
            st.execute(table);
            Logger.getLogger().log("ThumbStore.checkAndCreateTables() table created!");
            st = this.connection.createStatement();

            Logger.getLogger().log("ThumbStore.checkAndCreateTables() creating Index on path and path_id");
            String action = "CREATE UNIQUE INDEX path_index ON IMAGES(path)";
            st.execute(action);
            action = "CREATE INDEX PATH_ID_INDEX ON IMAGES(path_id)";
            st.execute(action);

            st = this.connection.createStatement();
            action = "CREATE  INDEX md5_index ON IMAGES(md5)";
            Logger.getLogger().log("ThumbStore.checkAndCreateTables() creating Index on md5");
            st.execute(action);
        }
        //now we look for the path table
        tables = dbm.getTables(null, null, "PATHS", null);
        if (tables.next()) {
            Logger.getLogger().log("ThumbStore.checkAndCreateTables() table PATHS exists!");
            // Table exists
        } else {
            Logger.getLogger().log("ThumbStore.checkAndCreateTables() table PATHS does not exist, should create it");
            // Table does not exist
            String table = "CREATE TABLE PATHS(path varchar(256),  path_id int AUTO_INCREMENT, PRIMARY KEY ( path ));";
            Statement st = this.connection.createStatement();
            st.execute(table);
            Logger.getLogger().log("ThumbStore.checkAndCreateTables() table created!");
        }
        //and  the version table
        tables = dbm.getTables(null, null, "VERSION", null);
        if (tables.next()) {
            Logger.getLogger().log("ThumbStore.checkAndCreateTables() table VERSION exists!");
            // Table exists
        } else {
            Logger.getLogger().log("ThumbStore.checkAndCreateTables() table VERSION does not exist, should create it");
            // Table does not exist
            String table = "CREATE TABLE VERSION(version int)";
            Statement st = this.connection.createStatement();
            st.execute(table);
            table = "INSERT into VERSION VALUES(" + CURRENT_VERSION + ")";
            st.execute(table);
            Logger.getLogger().log("ThumbStore.checkAndCreateTables() table created with version 0");
        }


        // now we check for version number and decides if an upgrade is required
        String version = "SELECT * FROM VERSION";
        Statement st = this.connection.createStatement();
        ResultSet res = st.executeQuery(version);
        int v = -1;
        while (res.next()) {
            v = res.getInt("version");
        }

        Logger.getLogger().log("Database version is " + v);
        upgradeDatabase(v);
    }
    private void checkOrAddColumns(DatabaseMetaData dbm) throws SQLException {
        //ALTER TABLE IMAGES DROP PRIMARY KEY ;
        //ALTER TABLE IMAGES ADD  id  BIGINT IDENTITY;
        //CREATE TABLE IMAGES_tmp(path varchar(256), size long, mtime long, md5 varchar(256), data blob,  lat double, lon double,  id  bigint identity(1,1))
        //INSERT INTO  IMAGES_TMP  (path,size,mtime,md5,data, lat, lon)  SELECT * from IMAGES
        //DROP table IMAGES
        //ALTER table images_tmp rename to IMAGES

        ResultSet rs = dbm.getColumns(null, null, "IMAGES", "LAT");
        if (!rs.next()) {
            //Column in table exist
            Logger.getLogger().log("Lat not found, updating table");
            Statement st = dbm.getConnection().createStatement();
            st.executeUpdate("ALTER TABLE IMAGES ADD lat double");

        }
        rs = dbm.getColumns(null, null, "IMAGES", "LON");
        if (!rs.next()) {
            Logger.getLogger().log("Lon not found, updating table");
            Statement st = dbm.getConnection().createStatement();
            st.executeUpdate("ALTER TABLE IMAGES ADD lon double");
        }
    }
    private void upgradeDatabase(int dbVersion) throws SQLException {
        Logger.getLogger().log("ThumbStore.upgradeDatabase started");

        if (dbVersion < CURRENT_VERSION) {
            if (dbVersion == 0) {
                upgradeToV1();
                upgradeToV2();
            }
            if (dbVersion == 1) {
                upgradeToV2();
            }

            if (dbVersion == 2) {
                upgradeToV3();
            }
            if (dbVersion == 3) {
                upgradeToV4();
            }

            if (dbVersion == 4) {
                upgradeToV5();
            }

            Statement st = connection.createStatement();
            String action = "Shutdown compact";
            st.execute(action);
            Logger.getLogger().log("ThumbStore.upgradeDatabase upgrade done please restart");
            System.exit(0);
        }
    }
    private void upgradeToV1() throws SQLException {
        //ok we need to upgrade the DB to the next version
        //CREATE TABLE IMAGES_tmp(path varchar(256), size long, mtime long, md5 varchar(256), data blob,  lat double, lon double,  id  bigint identity(1,1))
        //INSERT INTO  IMAGES_TMP  (path,size,mtime,md5,data, lat, lon)  SELECT * from IMAGES
        //DROP table IMAGES
        //ALTER table images_tmp rename to IMAGES
        Statement st = connection.createStatement();
        String action = "ALTER TABLE IMAGES DROP PRIMARY KEY ";
        st.execute(action);
        action = "CREATE TABLE IMAGES_tmp(id  bigint identity(1,1), path varchar(256), size long, mtime long, md5 varchar(256), data blob,  lat double, lon double)";
        st.execute(action);
        action = "INSERT INTO  IMAGES_TMP  (path,id, size,mtime,md5,data, lat, lon)  SELECT * from IMAGES";
        Logger.getLogger().log("ThumbStore.upgradeToV1 moving data to new table");
        st.execute(action);
        Logger.getLogger().log("ThumbStore.upgradeToV1 droping old table");

        action = "DROP table IMAGES";
        st.execute(action);
        action = "ALTER table images_tmp rename to IMAGES";
        st.execute(action);

        action = "UPDATE VERSION SET version=1 WHERE version=0";
        st.execute(action);
    }
    private void upgradeToV2() throws SQLException {
        //ok we need to upgrade the DB to the next version
        //This one includes an index for the path
        //CREATE INDEX index_name
        // ON table_name (column_name)
        Statement st = connection.createStatement();
        String action = "CREATE UNIQUE INDEX path_index ON IMAGES(path)";
        Logger.getLogger().log("ThumbStore.upgradeToV2 creating Index");
        st.execute(action);
        action = "UPDATE VERSION SET version=2 WHERE version=1";
        st.execute(action);
    }
    private void upgradeToV3() throws SQLException {
        //ok we need to upgrade the DB to the next version
        //This one includes an index for the path
        //CREATE INDEX md5_index
        // ON table_name (column_name)
        Statement st = connection.createStatement();
        String action = "CREATE  INDEX md5_index ON IMAGES(md5)";
        Logger.getLogger().log("ThumbStore.upgradeToV3 creating Index for MD5");
        st.execute(action);
        action = "UPDATE VERSION SET version=3 WHERE version=2";
        st.execute(action);
    }
    private void upgradeToV4() throws SQLException {
        Statement st = connection.createStatement();
        String action = "ALTER TABLE IMAGES ADD hash varchar(100)";
        Logger.getLogger().log("ThumbStore.upgradeToV4 creating column for hash");
        Logger.getLogger().log("ThumbStore.upgradeToV4 dropping column data");
        st.execute(action);
        action = " ALTER TABLE IMAGES DROP COLUMN data";
        st.execute(action);
        action = "UPDATE VERSION SET version=4 WHERE version=3";
        st.execute(action);
    }
    private void upgradeToV5() throws SQLException {
        //There are 3 tables : VERSION, PATHS and IMAGES
        Statement st = connection.createStatement();
        Logger.getLogger().log("ThumbStore.upgradeToV5 creating column for path id in IMAGES");
        //create a temporary table
        String action = "CREATE TABLE IMAGES_TMP(id  bigint identity(1,1),path varchar(256), path_id int, size long, mtime long, md5 varchar(256), hash varchar(100),  lat double, lon double);";
        st.execute(action);
        //add the path_id to the PATHS table
        action = "ALTER TABLE PATHS ADD path_id  int AUTO_INCREMENT";
        st.execute(action);
        //now get the current list of paths
        // and turn it into a HashMap for fast lookup
        ArrayList<String> currentPaths = new ArrayList<String>();
        ResultSet res = st.executeQuery("SELECT path FROM PATHS ORDER BY path_id");
        final String dir = System.getProperty("user.dir");
        System.out.println("current dir = " + dir);
        while (res.next()) {
            String s = res.getString("path");
            System.out.println("ThumbStore.upgradeToV5 found path " + s);
            currentPaths.add(s);

        }
        System.out.println("ThumbStore.upgradeToV5 got " + currentPaths.size() + " indexed paths");
        int index = 0;

        for (String p : currentPaths) {
            String u_p = p.replaceAll("\\\\", "\\\\\\\\");
            //get all paths matching the root path
            System.out.println("ThumbStore.upgradeToV5 performning query  " + "SELECT * FROM IMAGES WHERE images.path LIKE \'" + u_p + "%\'");
            ResultSet allResults = st.executeQuery("SELECT * FROM IMAGES WHERE images.path LIKE \'" + u_p + "%\'");
            index++;
            while (allResults.next()) {
                PreparedStatement psmnt;
                psmnt = connection.prepareStatement("insert into IMAGES_TMP(path, path_id, size, mtime, md5, hash, lat, lon) "
                        + "values(?,?,?,?,?,?,?,?)");
                psmnt.setString(1, allResults.getString("path").replace(p, ""));
                psmnt.setInt(2, index);
                psmnt.setLong(3, allResults.getLong("size"));
                psmnt.setLong(4, allResults.getLong("mtime"));
                psmnt.setString(5, allResults.getString("md5"));
                psmnt.setString(6, allResults.getString("hash"));
                psmnt.setDouble(7, allResults.getDouble("lat"));
                psmnt.setDouble(8, allResults.getDouble("lon"));
                psmnt.execute();
            }
        }

        action = "DROP table IMAGES";
        st.execute(action);
        action = "ALTER table images_tmp rename to IMAGES";
        st.execute(action);
        action = "CREATE INDEX PATH_ID_INDEX ON IMAGES(path_id)";
        st.execute(action);
        action = "CREATE INDEX PATH_INDEX ON IMAGES(path)";
        st.execute(action);
        action = "UPDATE VERSION SET version=5 WHERE version=4";
        st.execute(action);
    }
    public void compact() {
            System.out.println("ThumbStore.compact " + connection);
            try {
                Statement st = this.connection.createStatement();
                st.executeUpdate("SHUTDOWN COMPACT");
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
    public void addIndexPath(String path) {
        PreparedStatement statement;
        try {
            statement = this.connection.prepareStatement("SELECT * FROM PATHS WHERE path=?");
            statement.setString(1,path);
            statement.execute();
            ResultSet re = statement.getResultSet();
            if(re.next()){
                return;
            }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        System.err.println("ThumbStore.addIndexPath no information for path " + path + "  found");
        try {
            statement = this.connection.prepareStatement("insert into PATHS(path)" + "values(?)");
            statement.setString(1, path);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public ArrayList<String> getIndexedPaths() {
        Statement sta;
        ResultSet res = null;
        ArrayList<String> paths = new ArrayList<String>();
            try {
                sta = this.connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                res = sta.executeQuery("SELECT path,path_id AS path FROM PATHS ORDER BY path_id");
                while (res.next()) {
                    String s = res.getString("path");
                    paths.add(s);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        return paths;
    }
    public void updateIndexedPath(String current, String newP) {
        PreparedStatement psmnt;
            try {
                Statement st;
                psmnt = this.connection.prepareStatement("UPDATE PATHS SET path=? WHERE path=? ");
                psmnt.setString(1, newP);
                psmnt.setString(2, current);
                psmnt.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }
    /**
     * return s as root path and relative path
     *
     * @param s
     * @return
     */
    private String[] decomposePath(String s) {
        ArrayList<String> l = this.getIndexedPaths();
        for (String root : l) {
            if (s.contains(root)) {
                return new String[]{root, s.replace(root, "")};
            }
        }
        return null;
    }
    /**
     * Save the descriptor to the db
     * DO NOT check that the key is not used
     *
     * @param mediaFileDescriptor
     */
    public void saveToDB(MediaFileDescriptor mediaFileDescriptor) {
        PreparedStatement psmnt;
        try {
            psmnt = this.connection.prepareStatement("insert into IMAGES(path, path_id, size, mtime, md5, hash, lat, lon) "
                    + "values(?,?,?,?,?,?,?,?)");
            //we need to change the path to remove the root directory
            String[] decomposedPath = this.decomposePath(mediaFileDescriptor.getPath());
            psmnt.setString(1, decomposedPath[1]);
            psmnt.setInt(2, this.getIndexedPaths().indexOf(decomposedPath[0]) + 1);
            psmnt.setLong(3, mediaFileDescriptor.getSize());
            psmnt.setLong(4, mediaFileDescriptor.getMtime());
            psmnt.setString(5, mediaFileDescriptor.getMD5());
            psmnt.setString(6, mediaFileDescriptor.getHash());
            psmnt.setDouble(7, mediaFileDescriptor.getLat());
            psmnt.setDouble(8, mediaFileDescriptor.getLon());
            psmnt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (preloadedDescriptorsExists()) {
            Logger.getLogger().log("MediaIndexer.generateAndSave Adding to preloaded descriptors " + mediaFileDescriptor);
            mediaFileDescriptor.setConnection(connection);
            mediaFileDescriptor.setId(getIndex(mediaFileDescriptor.getPath()));
            this.getPreloadedDescriptors().add(mediaFileDescriptor);
        }
    }
    public void updateToDB(MediaFileDescriptor mediaFileDescriptor) {
        PreparedStatement psmnt = null;
        try {
            Statement st;
            psmnt =this.connection.prepareStatement("UPDATE IMAGES SET path=?, path_id=?, size=?, mtime=?, hash=?, md5=? , lat=?, lon=? WHERE path=? AND (FROM PATHS SELECT path_id WHERE path=?)");
            //we need to change the path to remove the root directory
            String[] decomposedPath = this.decomposePath(mediaFileDescriptor.getPath());
            psmnt.setString(1, decomposedPath[1]);
            psmnt.setInt(2, this.getIndexedPaths().indexOf(decomposedPath[0]) + 1);
            psmnt.setLong(3, mediaFileDescriptor.getSize());
            psmnt.setLong(4, mediaFileDescriptor.getMtime());
            psmnt.setString(5, mediaFileDescriptor.getHash());
            psmnt.setString(6, mediaFileDescriptor.getMD5());
            psmnt.setDouble(7, mediaFileDescriptor.getLat());
            psmnt.setDouble(8, mediaFileDescriptor.getLon());
            psmnt.setString(9, decomposedPath[1]);
            psmnt.setString(10, decomposedPath[0]);
            psmnt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (preloadedDescriptorsExists()) {
            mediaFileDescriptor.setConnection(this.connection);
            mediaFileDescriptor.setId(getIndex(mediaFileDescriptor.getPath()));
            getPreloadedDescriptors().remove(mediaFileDescriptor);
            getPreloadedDescriptors().add(mediaFileDescriptor);
        }

    }
    public int size() {
        int count = 0;
            String select = "SELECT COUNT(*) FROM IMAGES";
            Statement st;
            try {
                st = this.connection.createStatement();
                ResultSet res = st.executeQuery(select);
                if (res.next()) {
                    count += res.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        return count;
    }
    public boolean isInDataBaseBasedOnName(String path) {
        boolean result = false;
        ResultSet res = getFromDatabase(path);
        if (res != null) {
            try {
                result = res.next();
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return result;
    }
    // some difficult dependence problem i deleteFromdatabase
    public void deleteFromDatabase(String path) {
        Logger.getLogger().log("ThumbStore.deleteFromDatabase " + path);
        MediaFileDescriptor mf = this.getMediaFileDescriptor(path);
        ResultSet res = this.getFromDatabase(path);
        try {
            while (res.next()) {
                res.deleteRow();
                Logger.getLogger().log("    ... done");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //delete it from the cache
        if ((mf != null) && (this.preloadedDescriptorsExists())) {
            this.getPreloadedDescriptors().remove(mf);
        }
    }
    public int getIndex(String path) {
        ResultSet res = null;
        try {
            PreparedStatement psmnt = this.connection.prepareStatement("FROM IMAGES, PATHS " +
                    "SELECT paths.path||images.path as path,id,size,mtime,md5,hash,lat,lon WHERE (paths.path||images.path)=? AND images.path_ID=paths.path_ID", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            psmnt.setString(1, path);
            //		st = connection.createStatement();
            psmnt.execute();
            res = psmnt.getResultSet();
            res.next();
            return res.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    public ResultSet getFromDatabase(String path) {
        ResultSet res = null;
        try {
            Logger.getLogger().log("ThumbStore.getFromDatabase   ---- " + path);
            PreparedStatement psmnt = null;
            String[] decomposedPath = this.decomposePath(path);
            if (decomposedPath == null) {
                psmnt = this.connection.prepareStatement("FROM IMAGES, PATHS " +
                        "SELECT paths.path||images.path as fpath,id,size,mtime,md5,hash,lat,lon WHERE (paths.path||images.path)=? AND images.path_ID=paths.path_ID", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                psmnt.setString(1, path);
            } else {
                psmnt = this.connection.prepareStatement("SELECT * FROM IMAGES WHERE path=?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

                psmnt.setString(1, decomposedPath[1]);
            }
            psmnt.execute();
            res = psmnt.getResultSet();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
    public ResultSet getFromDatabase(int index) {
        ResultSet res = null;
        try {
            PreparedStatement psmnt = this.connection.prepareStatement(
                    //"SELECT * FROM IMAGES WHERE id=?"
                    "FROM IMAGES, PATHS\n" +
                            "SELECT paths.path||images.path as path,id,size,mtime,md5,hash,lat,lon   WHERE images.id=? AND images.path_ID=paths.path_ID"
                    , ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            psmnt.setInt(1, index);
            psmnt.execute();
            res = psmnt.getResultSet();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
    public long getMTime(String path) {

        ResultSet res = null;

        try {
            PreparedStatement psmnt = this.connection.prepareStatement("SELECT * FROM IMAGES WHERE path=?");
            psmnt.setString(1, path);
            //		st = connection.createStatement();
            psmnt.execute();
            res = psmnt.getResultSet();
            res.next();
            return res.getLong("mtime");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public ArrayList<String> getAllWithGPS() {
        Statement sta;
        ResultSet res = null;
        ArrayList<String> al = new ArrayList<String>();
            try {
                sta = this.connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                res = sta.executeQuery("SELECT * FROM IMAGES WHERE lat <> 0 OR lon <>0");
                while (res.next()) {
                    // System.err.println("getAllWithGPS adding  " + res.getString("path"));
                    al.add(res.getString("path").replaceAll("\\\\", "\\\\\\\\"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        return al;
    }
    public ResultSet getAllInDataBase() {
        Statement sta;
        long t0 = System.currentTimeMillis();
        try {
            sta = this.connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            return sta.executeQuery("FROM IMAGES, PATHS " +
                    "SELECT paths.path||images.path as path,id size,mtime,md5,hash,lat,lon WHERE" +
                    " paths.path_id=images.path_id ");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            long t1 = System.currentTimeMillis();
            System.err.println("ThumbStore.getAllInDataBase took " + (t1 - t0) + " ms");
        }
        return null;
    }
    public ArrayList<MediaFileDescriptor> getFromDB(String filter, boolean gps) {
        ArrayList<MediaFileDescriptor> list = new ArrayList<MediaFileDescriptor>();
        String query = null;
        if (!gps) {
            query = "FROM IMAGES, PATHS " +
                    "SELECT paths.path||images.path as path,size,mtime,md5,hash,lat,lon WHERE" +
                    " paths.path_id=images.path_id AND (LCASE(paths.path||images.path)) LIKE LCASE(\'%" + filter + "%\')";
        } else {
            query = "FROM IMAGES, PATHS " +
                    "SELECT paths.path||images.path as path,size,mtime,md5,hash,lat,lon WHERE " +
                    " paths.path_id=images.path_id AND (LCASE(paths.path||images.path)) LIKE LCASE(\'%" + filter + "%\') " +
                    "AND  (lat <> 0 OR lon <>0))";
        }
            Statement sta;
            try {
                sta = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet res = sta.executeQuery(query);
                // mrs.add(connection,r);
                while (res.next()) {
                    list.add(getCurrentMediaFileDescriptor(res));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        return list;
    }
    // I think it is a dead method
    public String getPath(int[] data) {
        Statement sta;
        ResultSet res = null;
        String p = null;
            try {
                sta = this.connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                PreparedStatement psmnt;
                psmnt = this.connection.prepareStatement("SELECT paths.path||images.path AS path FROM IMAGES, PATHS WHERE data=(?)");
                psmnt.setBytes(1, Utils.toByteArray(data));
                psmnt.execute();
                res = psmnt.getResultSet();

                while (res.next()) {
                    p = res.getString("path");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        return p;
    }
    public  String getPath(int index) {
        ResultSet res = null;
        String p = null;
        if (this.connection == null) {
            Logger.getLogger().err("ThumbStore : Connection is NULL ");
            return null;
        }
        try {
            PreparedStatement psmnt;
            psmnt = this.connection.prepareStatement("SELECT paths.path||images.path AS path FROM IMAGES, PATHS WHERE id=(?)");
            psmnt.setInt(1, index);
            psmnt.execute();
            res = psmnt.getResultSet();
            while (res.next()) {
                p = res.getString("path");
            }
        } catch (SQLException e) {
            System.err.println("Cannot find image with index " + index);
            e.printStackTrace();
        }
        return p;
    }
    // search in db for indexed files with the same md5 of the param
    public ArrayList<MediaFileDescriptor> getDuplicatesMD5(MediaFileDescriptor mfd) {
        Statement sta;
        ResultSet res = null;
        ArrayList<MediaFileDescriptor> results = new ArrayList<MediaFileDescriptor>();
            try {
                sta = this.connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                res = sta
                        .executeQuery("SELECT paths.path||images.path as path, md5, size FROM IMAGES, PATHS WHERE md5=\'" + mfd.getMD5() + "\'");
                while (res.next()) {
                    results.add(getCurrentMediaFileDescriptor(res));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        return results;
    }
    /**
     * remove incorrect records from the DB
     */
    public void fix() {
        ResultSet all = this.getAllInDataBase();
        MediaFileDescriptor id = null;
        System.err.println("ThumbStore.fix() BD has " + this.size() + " entries");
            try {
                while (all.next()) {
                    id = getCurrentMediaFileDescriptor(all);
                    if (Utils.isValideImageName(id.getPath())) {
                        if (id.getHash() == null || id.getMD5() == null) {
                            System.err.println("ThumbStore.fix() " + id.getPath() + " has null data ord md5");
                            all.deleteRow();
                        }
                    }
                    if (Utils.isValideVideoName(id.getPath())) {
                        if (id.getMD5() == null) {
                            System.err.println("ThumbStore.fix() " + id.getPath() + " has null md5");
                            all.deleteRow();
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }
    /**
     * remove outdated records from the DB An outdated record is one which has
     * no corresponding file on the FS
     */
    public void shrink() {
        this.shrink(this.getIndexedPaths());
    }
    public void shrink(List<String> paths) {
        if (Logger.getLogger().isEnabled()) {
            Logger.getLogger().log("ThumbStore.shrink() BD has " + this.size() + " entries");
        }
        for (String path : paths) {
            Logger.getLogger().log("ThumbStore.shrink() processing path " + path);
            ResultSet all = this.getAllInDataBase();
            MediaFileDescriptor id = null;
            try {
                int i = 0;
                while (all.next()) {
                    id = getCurrentMediaFileDescriptor(all);
                    Logger.getLogger().log("ThumbStore.shrink() processing  " + id);
                    File tmp = new File(id.getPath());
                    if (!tmp.exists()) {
                        i++;
                        //all.deleteRow();
                        this.deleteFromDatabase(id.getPath());
                    }
                }
                System.err.println("ThumbStore.shrink() has deleted  " + i + " records");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
    public MediaFileDescriptor getCurrentMediaFileDescriptor(ResultSet res) {
        MediaFileDescriptor id = null;
        try {
            String path = res.getString("path");
            String md5 = res.getString("md5");
            long mtime = res.getLong("mtime");
            long size = res.getLong("size");
            String hash = res.getString("hash");

            id = new MediaFileDescriptor(path, size, mtime, md5, hash);
            id.setId(res.getInt("id"));
        } catch (SQLException e) {
        }
        return id;
    }
    public MediaFileDescriptor getMediaFileDescriptor(String path) {
        ResultSet res = getFromDatabase(path);
        try {
            res.next();
            return this.getCurrentMediaFileDescriptor(res);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public MediaFileDescriptor getMediaFileDescriptor(int index) {
        ResultSet res = getFromDatabase(index);
        try {
            res.next();
            return this.getCurrentMediaFileDescriptor(res);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    // todo move this method from here to some where else
    public void displayImage(BufferedImage bf) {
        Graphics2D gg = bf.createGraphics();
        gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        gg.drawImage(bf, 0, 0, null);
        // drawFeatures(gg, surf, id);
        JFrame frame = null;
        // display results
        if (frame == null) {
            frame = new JFrame();
            final JLabel label = new JLabel(new ImageIcon(bf));
            frame.add(label);
        }
        frame.pack();
        frame.setVisible(true);
    }
    // todo move this method from here to tests
    public void test() {
        System.err.println("ThumbStore.test() reading descriptor from disk ");
        MediaIndexer tg = new MediaIndexer(this);
        String s = "/user/fhuet/desktop/home/workspaces/rechercheefficaceimagessimilaires/images/test.jpg";
        MediaFileDescriptor id = tg.buildMediaDescriptor(new File(s));
        System.err.println("ThumbStore.test() writting to database");
        saveToDB(id);
        System.err.println("ThumbStore.test() dumping entries");
        String select = "SELECT * FROM IMAGES, PATHS";
        Statement st;
            try {
                st = this.connection.createStatement();
                ResultSet res = st.executeQuery(select);
                while (res.next()) {
                    String i = res.getString("path");
                    //  byte[] d = res.getBytes("data");
                    System.err.println(i + " has mtime " + res.getLong("mtime"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        System.err.println("Testing update ");
        id.setMtime(0);
        updateToDB(id);
        System.err.println("ThumbStore.test() dumping entries");
    }
    public void dump(boolean p) {
        String select = "SELECT paths.path||images.path AS path,id,hash FROM IMAGES, PATHS";
        Statement st;
            try {
                st = this.connection.createStatement();
                ResultSet res = st.executeQuery(select);
                while (res.next()) {
                    String path = res.getString("path");
                    int i = res.getInt("id");
                    String s = res.getString("hash");
                    if (s != null) {
                        if (p) {
                            System.out.println(path + "," + s);
                        } else {
                            System.out.println(i + "," + s);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

    }

    /*
    public ArrayList<String> getPath() {
        return this.pathsOfDBOnDisk;
    }
    */

    public PreloadedDescriptors getPreloadedDescriptors() {
        if (preloadedDescriptors == null) {
            long ti = System.currentTimeMillis();
            Status.getStatus().setStringStatus("Building descriptors list");
            int dbSize = size();
            ProgressBar pb = new ProgressBar(0, dbSize, dbSize / 100);
            preloadedDescriptors = new PreloadedDescriptors(dbSize, new Comparator<MediaFileDescriptor>() {
                public int compare(MediaFileDescriptor o1, MediaFileDescriptor o2) {
                    return o1.getMD5().compareTo(o2.getMD5());
                }
            });
            int increment = dbSize / 100;
            int processed = 0;
            int processedSinceLastTick = 0;
            ResultSet res = getAllInDataBase();
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
                            MediaFileDescriptor imd = new MediaFileDescriptor();
                            if (SimilarImageFinder.USE_FULL_PATH) {
                                imd.setPath(path);
                            }
                            imd.setId(id);
                            imd.setHash(hash);
                            imd.setSize(size);
                            imd.setMd5Digest(md5);
                            imd.setConnection(this.connection);
                            preloadedDescriptors.add(imd);
                        } else {
                            //TODO : we should clean the data here
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            System.err.println("ThumbStore.getPreloadedDescriptors sorting  " + preloadedDescriptors.size() + " data");
            long t0 = System.currentTimeMillis();
            preloadedDescriptors.sort();
            long t1 = System.currentTimeMillis();
            System.err.println("ThumbStore.getPreloadedDescriptors sorting data .... done after " + (t1 - t0));
            Status.getStatus().setStringStatus(Status.IDLE);
            System.err.println("ThumbStore.getPreloadedDescriptors all done  " + (t1 - ti));
        }
        return preloadedDescriptors;
    }
    /**
     * return LSH status
     * [0] = LSH size
     * [1] = number of candidates for last query
     *
     * @return
     */
    // TODO: move this method from here to lsh builder
    public int[] getLSHStatus() {
        if (lsh == null) {
            buildLSH(false);
        }
        return new int[]{lsh.size(), lsh.lastCandidatesCount()};
    }
    // TODO: move this method from here to candidate selector
    public List<Candidate> findCandidatesUsingLSH(MediaFileDescriptor id) {
        if (lsh == null) {
            buildLSH(false);
        }
        List<Candidate> result = lsh.lookupCandidatesMT(id.getHash());
        System.out.println("Found " + result.size() + " candidates out of " + lsh.size());
        LoggingStopWatch watch = null;
        return result;
    }
    // TODO: move this method from here to lsh builder
    public void buildLSH(boolean force) {
        lsh = new PersistentLSH(5, 15, 100);
        if ((force)  || lsh.size() == 0) {
            Status.getStatus().setStringStatus("Teaching LSH");
            System.out.println("ThumbStore.buildLSH forced build or empty lsh size : " + lsh.size());
            lsh.clear();
            int total = this.size();
            int processed = 0;
            ResultSet res = this.getAllInDataBase();
                try {
                    while (res.next()) {
                        processed++;
                        if (processed>10000) {
                            System.out.println("ThumbStore.buildLSH processed 10 000");
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
    public boolean preloadedDescriptorsExists() {
        return (this.preloadedDescriptors != null);
    }
    public void flushPreloadedDescriptors() {
        if (this.preloadedDescriptors != null) {
            this.preloadedDescriptors.clear();
            this.preloadedDescriptors = null;
        }
    }
}
