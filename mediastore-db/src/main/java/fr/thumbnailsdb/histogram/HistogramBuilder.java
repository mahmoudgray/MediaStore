package fr.thumbnailsdb.histogram;

import fr.thumbnailsdb.ThumbnailGenerator;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.ColorQuantizerDescriptor;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: fhuet
 * Date: 06/09/13
 * Time: 13:51
 * To change this template use File | Settings | File Templates.
 */
public class HistogramBuilder {

    HashMap<Integer, Integer> colorTable = new HashMap<Integer, Integer>();

    static {
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                for (int k = 0; k < 256; k++) {
                    //  colorTable.put()
                }
            }
        }
    }


//    public static void test() {
//        File file = new File("/user/fhuet/desktop/home/workspaces/rechercheefficaceimagessimilaires/MediaStore/testImages/folder1/icons3.jpg");
//
//        PlanarImage image = JAI.create("fileload", file.getAbsolutePath());
//        ParameterBlock pb = new ParameterBlock();
//
//        int[] bins = {256};
//        double[] low = {0.0D};
//        double[] high = {256.0D};
//
//        pb.addSource(image);
//        pb.add(null);
//        pb.add(1);
//        pb.add(1);
//        pb.add(bins);
//        pb.add(low);
//        pb.add(high);
//
//        RenderedOp op = JAI.create("histogram", pb, null);
//        //  Histogram hist = (Histogram)op.getProperty("histogram");
////        Histogram histogram = (Histogram) op.getProperty("histogram");
//    }

// /*   public static BufferedImage reduceColors(BufferedImage src) {
//        final RenderedOp cqImage = ColorQuantizerDescriptor.create(
//                src, ColorQuantizerDescriptor.OCTTREE,
//                new Integer(8), null, null, null, null, null);
//        return cqImage.getAsBufferedImage();
//    }
//*/
    /**
     * build a color histogram for a 256 colors image
     *
     * @param bf
     * @return
     */
    public static int[][] buildHistogram(BufferedImage bf) {
//        int[] b = bf.getColorModel().getComponentSize();
//        System.out.println(b[0] + ","+ b[1] + ","+b[2] + ",");

        //create the 3 histograms for RGB
        int[][] his = new int[3][];
        his[0] = new int[256];
        his[1] = new int[256];
        his[2] = new int[256];

        int[] data1 = new int[bf.getWidth() * bf.getHeight()];
        bf.getRGB(0, 0, bf.getWidth(), bf.getHeight(), data1, 0, bf.getWidth());
        for (int i = 0; i < data1.length; i++) {
            int red1 = (data1[i] >>> 16) & 0xFF;
            int green1 = (data1[i] >>> 8) & 0xFF;
            int blue1 = (data1[i] >>> 0) & 0xFF;

            his[0][red1] ++;
            his[1][green1]++;
            his[2][blue1]++;

           // int value =  (short)(red1*0.299+green1*0.587+blue1*0.144);
          //  System.out.println(value + "  " + red1 + "," + green1 + "," + blue1 + "  -> " + ((red1 << 16) | (green1 << 8) | (blue1 << 0)));
        }
//        System.out.println(" " + ((0 << 16) | (0 << 8) | (0 << 0)));
        //normalize values
        Set s = new HashSet();
        for (int i = 0; i < data1.length; i++) {
            s.add(data1[i]);
        }
        System.out.println(" Number of values " + s.size());
        //Collections.sort(s);

        return his;
    }


    public static void main(String[] args) throws Exception {


        BufferedImage original = ImageIO.read(new File("/user/fhuet/desktop/home/workspaces/rechercheefficaceimagessimilaires/MediaStore/testImages/folder1/icons3.jpg"));
        int[] b = original.getColorModel().getComponentSize();
        System.out.println(b[0] + ","+ b[1] + ","+b[2] + ",");
//      final BufferedImage converted = reduceColors(original);
        final BufferedImage converted = original;
        //    final BufferedImage converted = new ThumbnailGenerator(null).downScaleImageToGray(original, original.getWidth(),original.getHeight());
        int[][] his = buildHistogram(converted);
        System.out.println();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final JFrame f = new JFrame();
                f.setTitle("Test");
                f.getContentPane().add((new ScrollingImagePanel(converted,converted.getWidth(), converted.getHeight())));
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.pack();
                f.setVisible(true);
            }
        });
    }
}
