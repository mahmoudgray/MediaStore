package fr.thumbnailsdb.descriptorbuilders;

import fr.thumbnailsdb.MediaFileDescriptor;
import fr.thumbnailsdb.dbservices.DBManager;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by mohannad on 02/12/15.
 */
public class MediaFileDescriptorBuilder {

    protected DBManager dbManager;

    public MediaFileDescriptorBuilder() {}
    public MediaFileDescriptorBuilder(DBManager dbManager) {
        this.dbManager = dbManager;
    }
    public void setDbManager(DBManager dbManager) {
        this.dbManager = dbManager;
    }
    public MediaFileDescriptor getMediaFileDescriptorFromDB(int index) {
        ResultSet res = dbManager.getFromDatabase(index);
        try {
            res.next();
            return getCurrentMediaFileDescriptor(res);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public MediaFileDescriptor getMediaFileDescriptorFromDB(String path) {
        ResultSet res = dbManager.getFromDatabase(path);
        try {
            res.next();
            return getCurrentMediaFileDescriptor(res);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
}
