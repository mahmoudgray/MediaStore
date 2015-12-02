package fr.thumbnailsdb;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDescriptor;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import fr.thumbnailsdb.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: fhuet
 * Date: 13/11/12
 * Time: 11:25
 * To change this template use File | Settings | File Templates.
 */
public class MetaDataFinder {

    Metadata metadata;
    File file;

    public MetaDataFinder(File f) {
        file = f;
        metadata = null;
        try {
            metadata = ImageMetadataReader.readMetadata(f);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void printTags() {
        //File jpegFile = new File("myImage.jpg");

        for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                Logger.getLogger().log(tag.toString());
            }
        }


    }

    public boolean hasGPSData() {
        return getLatLong() != null;
    }


    public String getDate() {
        if (metadata == null) {
            return null;
        }
        ExifSubIFDDirectory directory = metadata.getDirectory(ExifSubIFDDirectory.class);
        ExifSubIFDDescriptor descriptor = new ExifSubIFDDescriptor((ExifSubIFDDirectory) directory);
        Date date = null;
        if (directory != null) {
            date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            return date.toString();
        } else return "";
    }

    public String getGPS() {
        if (metadata == null) {
            return null;
        }
        String result = "";
        GpsDirectory directory = metadata.getDirectory(GpsDirectory.class);
        if (directory != null && directory.getTags().size() > 2) {
//         //   Logger.getLogger().log("---- " + file + " ----");
            for (Tag tag : directory.getTags()) {

                result += tag + "\n";
            }
        }

        Logger.getLogger().log("MetaDataFinder.getGPS file : " + file + " has coordinates " + result);

        return result;

    }

    public double[] getLatLong() {
        if (metadata == null) {
            return null;
        }
        Logger.getLogger().log("MetaDataFinder.getLatLon processing file : " + file);
        GpsDirectory directory = metadata.getDirectory(GpsDirectory.class);
        if (directory != null && directory.getTags().size() > 2) {
//         //   Logger.getLogger().log("---- " + file + " ----");
//            for (Tag tag : directory.getTags()) {
//                Logger.getLogger().log(tag);
//            }
//            double lat = getAsDecimalDegree(directory.getDescription(2));
//            double lon = getAsDecimalDegree(directory.getDescription(4));
////            Logger.getLogger().log(lat+", " +lon );
            GeoLocation gl = directory.getGeoLocation();
            double lat = 0;
            double lon = 0;
            if (gl != null) {
                lat = gl.getLatitude();
                lon = gl.getLongitude();
            }

            Logger.getLogger().log("MetaDataFinder.getLatLon file : " + file + " has coordinates {" + lat + "," + lon + "}");

            return new double[]{lat, lon};
        } else {

            return null;
        }

    }


    public double getAsDecimalDegree(String co) {

        String[] temp = null;

        double decimaldms = 0;
        temp = co.split("[°]|[\"]|[\']");
        for (int i = 0; i < temp.length; i++) {
//                Logger.getLogger().log("degree : "+temp[0]);
//                Logger.getLogger().log("minutes : "+temp[1]);
//                Logger.getLogger().log("second : "+temp[2]);

            String deg = temp[0];
            double ndeg = Double.parseDouble(deg);
            String min = temp[1];
            double nmin = Double.parseDouble(min);
            String sec = temp[2];
            double nsec = Double.parseDouble(sec);
            if (ndeg < 0) {
                decimaldms = -(-ndeg + (nmin / 60) + (nsec / 3600));
            } else {
                decimaldms = (ndeg + (nmin / 60) + (nsec / 3600));
            }
        }
        return decimaldms;
    }


    public static void main(String[] args) {
        File f = new File("/user/fhuet/desktop/home/workspaces/rechercheefficaceimagessimilaires/surfjavacl/RESTfulSimilar/gps/30-05-2011_0573.jpg");
        MetaDataFinder mdf = new MetaDataFinder(f);
        mdf.printTags();
        Logger.getLogger().log(mdf.getDate());
        Logger.getLogger().log(mdf.getGPS());
        double ll[] = mdf.getLatLong();
        Logger.getLogger().log("Latitude " + ll[0]);
        Logger.getLogger().log("Longitude " + ll[1]);
//        }
    }
}
