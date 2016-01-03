package fr.thumbnailsdb.utils;

import fr.thumbnailsdb.dbservices.DBManagerIF;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptorIF;
import fr.thumbnailsdb.mediaIndexers.MediaIndexer;
import fr.thumbnailsdb.dbservices.DBManager;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptorBuilder;
import fr.thumbnailsdb.hash.ImageHash;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageComparator {

    private static boolean debug = false;
    private static final int THRESHOLD = 50 ;

    public static double compareUsingRMSE(BufferedImage img1, BufferedImage img2) {
        int h = img1.getHeight();
        int w = img1.getWidth();
        if (w != img2.getWidth() || h != img2.getHeight()) {
            // images should have the same size
            return -1;
        }

        int[] data1 = new int[w * h];
        img1.getRGB(0, 0, w, h, data1, 0, w);

        int[] data2 = new int[w * h];
        img2.getRGB(0, 0, w, h, data2, 0, w);

        float totalR = 0;
        float totalG = 0;
        float totalB = 0;

        int red2 = 0;
        int green2 = 0;
        int blue2 = 0;

        for (int i = 0; i < w * h; i++) {
            int red1 = (data1[i] >>> 16) & 0xFF;
            int green1 = (data1[i] >>> 8) & 0xFF;
            int blue1 = (data1[i] >>> 0) & 0xFF;

            if (i == 0) {
                System.out.println("ImageComparator.compareUsingRMSE() first pixel " + red1 + "," + green1 + "," + blue1);
            }
            red2 = (data2[i] >>> 16) & 0xFF;
            green2 = (data2[i] >>> 8) & 0xFF;
            blue2 = (data2[i] >>> 0) & 0xFF;

            totalR += (red1 / 128 - red2 / 128) * (red1 / 128 - red2 / 128);
            totalG += (green1 / 128 - green2 / 128) * (green1 / 128 - green2 / 128);
            totalB += (blue1 / 128 - blue2 / 128) * (blue1 / 128 - blue2 / 128);
        }

        if (debug) {
            System.out.println("RMSE Red " + totalR / (h * w));
            System.out.println("RMSE Green " + totalG / (h * w));
            System.out.println("RMSE Blue " + totalB / (h * w));
            System.out.println("RMSE total " + (totalR + totalG + totalB) / (3 * w * h));
            System.out.println("RMSE % " + (totalR + totalG + totalB) / (3 * w * h) * 100);
        }
        return (totalR + totalG + totalB) / (3 * w * h) * 100;
    }
    public static double compareARGBUsingRMSE(int[] img1, int[] img2) {
        float totalR = 0;
        float totalG = 0;
        float totalB = 0;

        int red2 = 0;
        int green2 = 0;
        int blue2 = 0;

        //format is argb
        for (int i = 0; i < img1.length; i++) {

            int red1 = (img1[i] >>> 16) & 0xFF;
            int green1 = (img1[i] >>> 8) & 0xFF;
            int blue1 = (img1[i] >>> 0) & 0xFF;

            red2 = (img2[i] >>> 16) & 0xFF;
            green2 = (img2[i] >>> 8) & 0xFF;
            blue2 = (img2[i] >>> 0) & 0xFF;

            totalR += (red1 / 128 - red2 / 128) * (red1 / 128 - red2 / 128);
            totalG += (green1 / 128 - green2 / 128) * (green1 / 128 - green2 / 128);
            totalB += (blue1 / 128 - blue2 / 128) * (blue1 / 128 - blue2 / 128);

        }


        if (debug) {
            System.out.println("RMSE Red " + totalR / img1.length);
            System.out.println("RMSE Green " + totalG / img1.length);
            System.out.println("RMSE Blue " + totalB / img1.length);
            System.out.println("RMSE total " + (totalR + totalG + totalB) / (3 * img1.length));
            System.out.println("RMSE % " + (totalR + totalG + totalB) / (3 * img1.length) * 100);
        }
        return (totalR + totalG + totalB) / (3 * img1.length) * 100;
    }
    public static double compareRGBUsingRMSE(int[] img1, int[] img2) {
        if (img1== null || img2==null) {
            return Double.MAX_VALUE;
        }
        double total=0;
        for (int i = 0; i < img1.length; i++) {
            int grey1 = (img1[i] >>> 16) & 0xFF;
            int grey2 = (img2[i] >>> 16) & 0xFF;

            //total+=Math.abs(grey1-grey2);
            if (Math.abs(grey1-grey2)>THRESHOLD) {
                total+=1;
            }

        }
        return total;
    }
    public static double compareUsingHammingDistance(String sg1, String sg2) {
        if (sg1.length() != sg2.length()) {
            return -1;
        }

        int distance = 0;
        for (int i = 0; i < sg1.length(); i++) {
            if (sg1.charAt(i) != sg2.charAt(i)) {
                distance++;
            }
        }
        return distance;
    }
    protected static void testFullScaleImages() throws IOException {
        System.out.println("ImageComparator.main() ---- similar images");
        String path = "/user/fhuet/desktop/home/workspaces/rechercheefficaceimagessimilaires/images/original.jpg";
        BufferedImage img = ImageIO.read(new File(path));
        String path2 = "/user/fhuet/desktop/home/workspaces/rechercheefficaceimagessimilaires/images/original-modifie.jpg";
        BufferedImage img2 = ImageIO.read(new File(path2));
        ImageComparator.compareUsingRMSE(img, img2);

        System.out.println("ImageComparator.main() ---- different images");
        path = "/user/fhuet/desktop/home/workspaces/rechercheefficaceimagessimilaires/images/original.jpg";
        img = ImageIO.read(new File(path));
        path2 = "/user/fhuet/desktop/home/workspaces/rechercheefficaceimagessimilaires/images/original-different.jpg";
        img2 = ImageIO.read(new File(path2));
        ImageComparator.compareUsingRMSE(img, img2);
    }
    protected static void testThumbnailImages() throws IOException {
        int x = 10;
        int y = 10;

        System.out.println("ImageComparator.testThumbnailImages()  Thumbnail size is " + x + "x" + y);
        MediaFileDescriptorBuilder mediaFileDescriptorBuilder = new MediaFileDescriptorBuilder();
        DBManagerIF dbManagerIF = new DBManager(null , mediaFileDescriptorBuilder);
        MediaIndexer tb = new MediaIndexer(null,mediaFileDescriptorBuilder);
        String path = "/user/fhuet/desktop/home/workspaces/rechercheefficaceimagessimilaires/images/original.jpg";
        BufferedImage img = ImageIO.read(new File(path));

        System.out.println("-------");
        BufferedImage imgTb = ImageHash.downScaleImageToGray(img, x, y);
        String path2 = "/user/fhuet/desktop/home/workspaces/rechercheefficaceimagessimilaires/images/Jaguar_1600x1200.jpg";
        BufferedImage img2 = ImageIO.read(new File(path2));
        BufferedImage img2Tb = ImageHash.downScaleImageToGray(img2, x, y);
        System.out.println("ImageComparator.testThumbnailImages() Comparison of DIFFERENT original images RMSE : " + ImageComparator.compareUsingRMSE(img, img2));
        System.out.println("ImageComparator.main() Comparison of DIFFERENT thumbnails RMSE " + ImageComparator.compareUsingRMSE(imgTb, img2Tb));

        System.out.println("-------------------");
        String path3 = "/user/fhuet/desktop/home/workspaces/rechercheefficaceimagessimilaires/images/original-modifie.jpg";
        BufferedImage img3 = ImageIO.read(new File(path3));
        BufferedImage img3Tb = ImageHash.downScaleImageToGray(img3, x, y);
        System.out.println("ImageComparator.testThumbnailImages() Comparison of MODIFIED original images RMSE : " + ImageComparator.compareUsingRMSE(img, img3));
        System.out.println("ImageComparator.main() Comparison of MODIFIED thumbnails RMSE " + ImageComparator.compareUsingRMSE(imgTb, img3Tb));
        System.out.println("--------------");
        String path4 = "/user/fhuet/desktop/home/workspaces/rechercheefficaceimagessimilaires/images/original-different.jpg";
        BufferedImage img4 = ImageIO.read(new File(path4));
        BufferedImage img4Tb = ImageHash.downScaleImageToGray(img4, x, y);
        System.out.println("ImageComparator.testThumbnailImages() Comparison of VERY MODIFIED original images RMSE : " + ImageComparator.compareUsingRMSE(img, img4));
        System.out.println("ImageComparator.main() Comparison of VERY MODIFIED thumbnails RMSE " + ImageComparator.compareUsingRMSE(imgTb, img4Tb));


        System.out.println("--------------");


        MediaFileDescriptorIF id1 = mediaFileDescriptorBuilder.buildMediaDescriptor(new File(path));
        MediaFileDescriptorIF id4 = mediaFileDescriptorBuilder.buildMediaDescriptor(new File(path4));
//        System.out.println("ImageComparator.main() Comparison of VERY MODIFIED thumbnails using ARGB RMSE " + ImageComparator.compareARGBUsingRMSE(id1.getData(), id4.getData()));

    }
    public static void main(String[] args) throws IOException {
        //testFullScaleImages();
        testThumbnailImages();

    }

}
