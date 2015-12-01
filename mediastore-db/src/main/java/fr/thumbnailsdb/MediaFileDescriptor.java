package fr.thumbnailsdb;

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


//    protected int[] data;
    protected double lat;
    protected double lon;

    //id in the database
    protected int id;
    //the DB used to access this media
    protected Connection connection;

    @XmlElement
    protected double distance;

    public static MediaFileDescriptor readFromDisk(String path) {
        MediaFileDescriptor id = null;
        File f = new File(path);
        long size = f.length();
        long modifiedTime = f.lastModified();

        int[] data;

//		try {
        //	FileInputStream fi = new FileInputStream(f);
        data = new int[(int) size];
        // int read = fi.read(data);
        // System.out.println("ImageDescriptor.main() read " + read +
        // " bytes from file");
        id = new MediaFileDescriptor(path, size, modifiedTime, null,  null);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
        return id;
    }

    //

    public MediaFileDescriptor() {

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

    /**
     * int[] data will be converted to argb byte[]
     *
     * @param path
     * @param size
     * @param mtime
     * @param md5
     * @param hash
     */
    public MediaFileDescriptor(String path, long size, long mtime,  String md5, String hash) {
        super();
        this.path = path;

        this.size = size;
        this.mtime = mtime;

        // byte[] rgba = convertToARGB(data);
        // this.data = rgba;
       // this.data = data;
        this.md5Digest = md5;
        this.hash = hash;
    }

    protected byte[] convertToARGB(int[] data) {
        byte[] rgba = new byte[data.length * 4];
        // int j = 0;
        for (int i = 0; i < data.length - 4; i++) {
            if (i == 0) {
                System.out.println("ImageDescriptor.convertToARGB()");
                System.out.println("   " + ((data[i] >>> 16) & 0xFF) + " -> " + (byte) ((data[i] >>> 16) & 0xFF));
                System.out.println("   " + ((data[i] >>> 8) & 0xFF) + " -> " + (byte) ((data[i] >>> 8) & 0xFF));

                System.out.println("   " + ((data[i] >>> 0) & 0xFF) + " -> " + (byte) ((data[i] >>> 0) & 0xFF));

            }
            rgba[4 * i] = (byte) ((data[i] >>> 24) & 0xFF);
            rgba[4 * i + 1] = (byte) ((data[i] >>> 16) & 0xFF);
            rgba[4 * 1 + 2] = (byte) ((data[i] >>> 8) & 0xFF);
            rgba[4 * +3] = (byte) ((data[i] >>> 0) & 0xFF);
            // j++;
        }
        return rgba;
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
        //if (path==null) {
         //   return     ThumbStore.getPath(this.id);
       // }
        return path;
    }

    public long getSize() {
        return size;
    }

    public long getMtime() {
        return mtime;
    }

//    public int[] getData() {
//        return data;
//    }

//    public byte[] getDataAsByte() {
//        // convert the int[] array to byte[] array
//        if (data == null) {
//            System.out.println("MediaFileDescriptor.getDataAsByte found null data");
//            return null;
//        }
//        ByteArrayOutputStream ba = new ByteArrayOutputStream();
//        ObjectOutputStream oi;
//        try {
//            oi = new ObjectOutputStream(ba);
//            oi.writeObject(data);
//            oi.close();
//
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return ba.toByteArray();
//    }


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
//        BufferedImage dest =  new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
//        dest.setRGB(0,0,10,10,data,0,10);

//        return dest;
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
    //    System.out.println("MediaFileDescriptor.equals : " + this  +  "   <>   " + obj);
        if (!(obj instanceof MediaFileDescriptor)) return false;
        MediaFileDescriptor target = (MediaFileDescriptor) obj;
        if ((this.path==null) || (target.getPath()==null))  {
          //  System.out.println("fr.thumbnailsdb.MediaFileDescriptor.equals " + this.id + " =?= " + target.getId());

            //we don't have the path, just the index in the DB
             return (this.id == target.getId());
        }  else {
             return this.path==target.getPath();
        }
        //return (this.)
        //return super.equals(obj);    //To change body of overridden methods use File | Settings | File Templates.
    }



    public int compareTo(MediaFileDescriptor o) {
        return this.md5Digest.compareTo(o.md5Digest);
        //return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
