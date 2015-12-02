package fr.thumbnailsdb.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by mohannad on 02/12/15.
 */
public class ImageProcessor {


    protected  boolean debug;
    private boolean software;

    /**
     * Load the image and resize it if necessary
     * @param bi
     * @return
     * @throws IOException
     */
    public BufferedImage downScaleImageToGray(BufferedImage bi, int nw, int nh) throws IOException {
        if (debug) {
            System.out.println("ImageProcessor.downScaleImageToGray()  original image is " + bi.getWidth() + "x" + bi.getHeight());
            System.out.println("ImageProcessor.downScaleImageToGray() to " + nw + "x" + nh);
        }
        BufferedImage scaledBI = null;
        scaledBI = new BufferedImage(nw, nh, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = scaledBI.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(bi, 0, 0, nw, nh, null);
        g.dispose();
        return scaledBI;
    }
    protected int[] generateThumbnail(File f) {
        // byte[] data;
        BufferedImage source;
        int[] data1 = null;
        try {
            source = ImageIO.read(f);
            BufferedImage dest = null;
            if (software) {
                dest = this.downScaleImageToGray(source, 10, 10);
            }
            data1 = new int[dest.getWidth() * dest.getHeight()];
            dest.getRGB(0, 0, dest.getWidth(), dest.getHeight(), data1, 0, dest.getWidth());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return data1;
    }

}
