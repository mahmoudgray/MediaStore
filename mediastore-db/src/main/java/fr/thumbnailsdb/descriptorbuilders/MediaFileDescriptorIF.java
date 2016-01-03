package fr.thumbnailsdb.descriptorbuilders;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.sql.Connection;

/**
 * Created by mohannad on 03/01/16.
 */
public interface MediaFileDescriptorIF extends Serializable, Comparable<MediaFileDescriptorIF> {
    void setId(int id);

    int getId();

    void setLat(double lat);

    void setLon(double lon);

    double getLat();

    double getLon();

    double getDistance();

    void setDistance(double distance);

    void setPath(String path);

    String getHash();

    void setHash(String hash);

    void setSize(long size);

    void setMtime(long mtime);

    void setMd5Digest(String md5Digest);

    String getPath();

    long getSize();

    long getMtime();

    byte[] getSignatureAsByte();

    BufferedImage getSignatureAsImage();

    String getMD5();

    void setConnection(Connection connection);

    Connection getConnection();
}
