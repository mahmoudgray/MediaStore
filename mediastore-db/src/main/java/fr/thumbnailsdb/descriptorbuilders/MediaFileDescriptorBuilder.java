package fr.thumbnailsdb.descriptorbuilders;

import fr.thumbnailsdb.dbservices.DBManagerIF;
import fr.thumbnailsdb.utils.MetaDataFinder;
import fr.thumbnailsdb.utils.Utils;
import fr.thumbnailsdb.hash.ImageHash;
import fr.thumbnailsdb.utils.MD5Generator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by mohannad on 02/12/15.
 */
public class MediaFileDescriptorBuilder {
    protected DBManagerIF dbManagerIF;
    protected MD5Generator md5Generator;

    public MediaFileDescriptorBuilder() {
        this.md5Generator = new MD5Generator();
    }
    public MediaFileDescriptorBuilder(DBManagerIF dbManagerIF) {
        this.dbManagerIF = dbManagerIF;
        this.md5Generator = new MD5Generator();
    }
    public void setDbManager(DBManagerIF dbManagerIF) {
        this.dbManagerIF = dbManagerIF;
    }
    public MediaFileDescriptorIF getMediaFileDescriptorFromDB(int index) {
        ResultSet res = dbManagerIF.getFromDatabase(index);
        try {
            res.next();
            return getCurrentMediaFileDescriptor(res);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public MediaFileDescriptorIF getMediaFileDescriptorFromDB(String path) {
        ResultSet res = dbManagerIF.getFromDatabase(path);
        try {
            res.next();
            return getCurrentMediaFileDescriptor(res);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public MediaFileDescriptorIF getCurrentMediaFileDescriptor(ResultSet res) {
        MediaFileDescriptorIF id = null;
        try {
            String path = res.getString("path");
            String md5 = res.getString("md5");
            long mtime = res.getLong("mtime");
            long size = res.getLong("size");
            String hash = res.getString("hash");

            id = new MediaFileDescriptor(path, size, mtime, md5, hash,this.dbManagerIF);
            id.setId(res.getInt("id"));
        } catch (SQLException e) {
        }
        return id;
    }
    public MediaFileDescriptorIF buildMediaDescriptor(File f) {
        MediaFileDescriptorIF id = new MediaFileDescriptor(this.dbManagerIF);
        int[] data;
        String md5;
        try {
            id.setPath(f.getCanonicalPath());
            id.setMtime(f.lastModified());
            id.setSize(f.length());
            // generate thumbnails only for images, not video
            if (Utils.isValideImageName(f.getName())) {
                MetaDataFinder mdf = new MetaDataFinder(f);
                double[] latLon = mdf.getLatLong();
                if (latLon != null) {
                    id.setLat(latLon[0]);
                    id.setLon(latLon[1]);
                }
                //bufferize images in memory, read directly from file for others
                ByteArrayInputStream fbi = Utils.readFileToMemory(f);
                id.setHash(new ImageHash().generateSignature(fbi));
                fbi.reset();
                md5 = this.md5Generator.generateMD5(fbi);
                id.setMd5Digest(md5);
            }   else {
                md5 = this.md5Generator.generateMD5(f);
                id.setMd5Digest(md5);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error processing  file " + f.getName());
            e.printStackTrace();
        }
        return id;
    }
}
