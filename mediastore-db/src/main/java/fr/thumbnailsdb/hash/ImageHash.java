package fr.thumbnailsdb.hash;

import fr.thumbnailsdb.MediaFileDescriptor;
import fr.thumbnailsdb.PreloadedDescriptors;
import fr.thumbnailsdb.dbservices.DBManager;
import fr.thumbnailsdb.utils.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;


public class ImageHash {

    private static int WIDTH = 10;
    private static int HEIGHT = 10;

    public static BufferedImage downScaleImageToGray(BufferedImage bi, int nw, int nh) throws IOException {
        if (Logger.getLogger().isEnabled()) {
            Logger.getLogger().log("ThumbnailGenerator.downScaleImageToGray()  original image is " + bi.getWidth() + "x"
                    + bi.getHeight());
        }
        BufferedImage scaledBI = null;
        // if (nw < width || nh < height) {
        if (Logger.getLogger().isEnabled()) {
            Logger.getLogger().log("ThumbnailGenerator.downScaleImageToGray() to " + nw + "x" + nh);
        }
        if (Logger.getLogger().isEnabled()) {
            Logger.getLogger().log("resizing to " + nw + "x" + nh);
        }
        scaledBI = new BufferedImage(nw, nh, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = scaledBI.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(bi, 0, 0, nw, nh, null);
        g.dispose();
        // }
        return scaledBI;
    }


    private static int meanValue(BufferedImage bf) {
        int[] data1 = new int[bf.getWidth() * bf.getHeight()];
        bf.getRGB(0, 0, bf.getWidth(), bf.getHeight(), data1, 0, bf.getWidth());
        //now average values
        long total = 0;
        for (int i = 0; i < data1.length; i++) {
            total += data1[i];
        }
        return (int) (total / data1.length);
    }

    public static  String generateSignature(BufferedImage source) throws IOException {
        BufferedImage bf = downScaleImageToGray(source, WIDTH, HEIGHT);
        String signature = "";
        int[] data1 = new int[bf.getWidth() * bf.getHeight()];
        bf.getRGB(0, 0, bf.getWidth(), bf.getHeight(), data1, 0, bf.getWidth());
        int mean = meanValue(bf);
        for (int i = 0; i < data1.length; i++) {
            if (data1[i] > mean) {
                signature += "1";
            } else {
                signature += "0";
            }
        }
        return signature;
    }

    public static BufferedImage signatureToImage(String signature) {
        final BufferedImage bf = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
        int[] data = new int[WIDTH * HEIGHT];
        for (int i = 0; i < data.length; i++) {
            if (signature.charAt(i) == '0') {
                data[i] = Color.white.getRGB();
            } else {
                data[i] = Color.black.getRGB();
            }
        }
        bf.setRGB(0, 0, WIDTH, HEIGHT, data, 0, WIDTH);
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                final JFrame f = new JFrame();
//                f.setTitle("Test");
//                f.getContentPane().add((new ScrollingImagePanel(bf, bf.getWidth(), bf.getHeight())));
//                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                f.pack();
//                f.setVisible(true);
//            }
//        });
        return bf;
    }


    public static String generateSignature(String path) throws IOException {
        BufferedImage bf = ImageIO.read(new File(path));
        return generateSignature(bf);
    }


    public static String generateSignature(InputStream in) throws IOException {
        BufferedImage bf = ImageIO.read(in);
        return generateSignature(bf);
    }

    public static int hammingDistance(String sg1, String sg2) {

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





    public static void testDB() {
        ImageHash imh = new ImageHash();
        DBManager tb = new DBManager();
        System.out.println(" Size of DB :  " + tb.size());
        PreloadedDescriptors pl = tb.getPreloadedDescriptors();
        Iterator it = pl.iterator();
        while (it.hasNext()) {
            MediaFileDescriptor mf = (MediaFileDescriptor) it.next();
            String path = tb.getPath(mf.getId());
            mf.setPath(path);
            try {
                System.out.println("-- Image " + mf.getPath());
                System.out.println("    original : " + imh.generateSignature(mf.getPath()));
                System.out.println("    data     : " + imh.generateSignature(mf.getSignatureAsImage()));

            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            //  System.out.println(mf);
        }

    }
    public static void testHash(String[] args){
        ImageHash imh = new ImageHash();
        if (args.length < 1) {
            System.err.println("Usage : java  " + ImageHash.class + " <paths>");
            System.exit(-1);
        }

        String signature;
        for (String s : args) {
            try {
                System.out.print("Signature for " + s + "   ");
                signature = imh.generateSignature(s);
                System.out.println(signature);
                imh.signatureToImage(signature);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (args.length > 2) {
            try {
                String sig1 = imh.generateSignature(args[0]);
                String sig2 = imh.generateSignature(args[1]);
                String sig3 = imh.generateSignature(args[2]);
                System.out.println("Hamming distance " + args[0] + "<->" + args[1] + "  : " + imh.hammingDistance(sig1, sig2));
                System.out.println("Hamming distance " + args[0] + "<->" + args[2] + "  : " + imh.hammingDistance(sig1, sig3));
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public static void main(String[] args) {
//                       testHash(args);
        testDB();
    }


}
