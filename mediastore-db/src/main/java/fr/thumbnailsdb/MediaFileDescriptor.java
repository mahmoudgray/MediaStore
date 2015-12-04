package fr.thumbnailsdb;

import fr.thumbnailsdb.dbservices.DBManager;
import fr.thumbnailsdb.hash.ImageHash;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Connection;


@XmlRootElement
public class MediaFileDescriptor implements Serializable, Comparable<MediaFileDescriptor> {
    @XmlElement
    protected String path;
    protected long size;
    protected long mtime;
    protected String md5Digest;
    protected String hash;
    protected double lat;
    protected double lon;
    DBManager dbManager;


    //id in the database
    protected int id;
    //the DB used to access this media
    protected Connection connection;

    @XmlElement
    protected double distance;


    public MediaFileDescriptor(DBManager dbManager) {
        this.dbManager=dbManager;
    }
    /**
     * int[] data will be converted to argb byte[]
     *
     * @param path
     * @param size
     * @param mtime
     * @param md5
     * @param hash
     * @param dbManager
     */
    public MediaFileDescriptor(String path, long size, long mtime,  String md5, String hash,DBManager dbManager) {
        super();
        this.path = path;
        this.size = size;
        this.mtime = mtime;
        this.md5Digest = md5;
        this.hash = hash;
        this.dbManager = dbManager;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
    public void setLat(double lat) {

        this.lat = lat;
    }
    public void setLon(double lon) {
        this.lon = lon;
    }
    public double getLat() {

        return lat;
    }
    public double getLon() {
        return lon;
    }
    public double getDistance() {
        return distance;
    }
    public void setDistance(double distance) {
        this.distance = distance;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getHash() {
        return hash;
    }
    public void setHash(String hash) {
        this.hash = hash;
    }
    public void setSize(long size) {
        this.size = size;

    }
    public void setMtime(long mtime) {
        this.mtime = mtime;
    }
    public void setMd5Digest(String md5Digest) {
        this.md5Digest = md5Digest;
    }
    public String getPath() {
        if (path==null) {
             return this.dbManager.getPath(this.id);
         }
        return path;
    }
    public long getSize() {
        return size;
    }
    public long getMtime() {
        return mtime;
    }
    /**
     * Convert the signature to a BufferedImage and return the
     * corresponding byte[]
     * @return
     */
    public byte[] getSignatureAsByte() {
          BufferedImage bf = ImageHash.signatureToImage(this.hash);
        int[] data = new int[bf.getWidth()*bf.getHeight()];
        bf.getRGB(0,0,bf.getWidth(),bf.getHeight(),data,0,bf.getWidth());
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        ObjectOutputStream oi;
        try {
            oi = new ObjectOutputStream(ba);
            oi.writeObject(data);
            oi.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ba.toByteArray();
    }
    public BufferedImage getSignatureAsImage() {
        return ImageHash.signatureToImage(this.hash);
    }
    public String getMD5() {
        return md5Digest;
    }
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
    public Connection getConnection() {

        return connection;
    }
    @Override
    public String toString() {
        return "[path=" + path + "\n size=" + size + ",\n mtime=" + mtime + ",\n md5="  + md5Digest +",\n hash="  + hash + "]";
    }
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MediaFileDescriptor)) return false;
        MediaFileDescriptor target = (MediaFileDescriptor) obj;
        if ((this.path==null) || (target.getPath()==null))  {
             return (this.id == target.getId());
        }  else {
             return this.path==target.getPath();
        }
    }
    public int compareTo(MediaFileDescriptor o) {
        return this.md5Digest.compareTo(o.md5Digest);
        //return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
