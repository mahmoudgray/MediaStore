package fr.thumbnailsdb.descriptorbuilders;

import fr.thumbnailsdb.dbservices.DBManagerIF;
import fr.thumbnailsdb.hash.ImageHash;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Connection;


@XmlRootElement
public class MediaFileDescriptor implements MediaFileDescriptorIF {
    @XmlElement
    protected String path;
    protected long size;
    protected long mtime;
    protected String md5Digest;
    protected String hash;
    protected double lat;
    protected double lon;
    DBManagerIF dbManagerIF;


    //id in the database
    protected int id;
    //the DB used to access this media
    protected Connection connection;

    @XmlElement
    private double distance;


    public MediaFileDescriptor(DBManagerIF dbManagerIF) {
        this.dbManagerIF = dbManagerIF;
    }
    /**
     * int[] data will be converted to argb byte[]
     *
     * @param path
     * @param size
     * @param mtime
     * @param md5
     * @param hash
     * @param dbManagerIF
     */
    public MediaFileDescriptor(String path, long size, long mtime,  String md5, String hash,DBManagerIF dbManagerIF) {
        super();
        this.path = path;
        this.size = size;
        this.mtime = mtime;
        this.md5Digest = md5;
        this.hash = hash;
        this.dbManagerIF = dbManagerIF;
    }
    @Override
    public void setId(int id) {
        this.id = id;
    }
    @Override
    public int getId() {
        return id;
    }
    @Override
    public void setLat(double lat) {

        this.lat = lat;
    }
    @Override
    public void setLon(double lon) {
        this.lon = lon;
    }
    @Override
    public double getLat() {

        return lat;
    }
    @Override
    public double getLon() {
        return lon;
    }
    @Override
    public double getDistance() {
        return distance;
    }
    @Override
    public void setDistance(double distance) {
        this.distance = distance;
    }
    @Override
    public void setPath(String path) {
        this.path = path;
    }
    @Override
    public String getHash() {
        return hash;
    }
    @Override
    public void setHash(String hash) {
        this.hash = hash;
    }
    @Override
    public void setSize(long size) {
        this.size = size;

    }
    @Override
    public void setMtime(long mtime) {
        this.mtime = mtime;
    }
    @Override
    public void setMd5Digest(String md5Digest) {
        this.md5Digest = md5Digest;
    }
    @Override
    public String getPath() {
        if (path==null) {
             return this.dbManagerIF.getPath(this.id);
         }
        return path;
    }
    @Override
    public long getSize() {
        return size;
    }
    @Override
    public long getMtime() {
        return mtime;
    }
    /**
     * Convert the signature to a BufferedImage and return the
     * corresponding byte[]
     * @return
     */
    @Override
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
    @Override
    public BufferedImage getSignatureAsImage() {
        return ImageHash.signatureToImage(this.hash);
    }
    @Override
    public String getMD5() {
        return md5Digest;
    }
    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
    @Override
    public Connection getConnection() {

        return connection;
    }
    @Override
    public String toString() {
        return "[path=" + path + "\n size=" + size + ",\n mtime=" + mtime + ",\n md5="  + md5Digest +",\n hash="  + hash + "]";
    }
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MediaFileDescriptorIF)) return false;
        MediaFileDescriptorIF target = (MediaFileDescriptorIF) obj;
        if ((this.path==null) || (target.getPath()==null))  {
             return (this.id == target.getId());
        }  else {
             return this.path==target.getPath();
        }
    }

    @Override
    public int compareTo(MediaFileDescriptorIF o) {
        return this.md5Digest.compareTo(o.getMD5());
    }


}
