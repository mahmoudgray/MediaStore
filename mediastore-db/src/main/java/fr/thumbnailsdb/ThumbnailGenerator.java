package fr.thumbnailsdb;

import fr.thumbnailsdb.dbservices.DBManager;
import fr.thumbnailsdb.hash.ImageHash;
import fr.thumbnailsdb.utils.LimitedQueue;
import fr.thumbnailsdb.utils.Logger;
import org.apache.commons.codec.digest.DigestUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ThumbnailGenerator {

    public static final int WIDTH = 10;
    public static final int HEIGHT = 10;
    protected boolean debug;
    protected boolean software = true;
    protected DBManager dbManager;
    protected Logger log = Logger.getLogger();
    protected ThreadPoolExecutor executorService =
            new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS,new LimitedQueue<Runnable>(50));

    public ThumbnailGenerator(DBManager t) {
        this.dbManager = t;
    }
    /**
     * Load the image and resize it if necessary
     *
     * @return
     * @throws IOException
     */
    public BufferedImage downScaleImageToGray(BufferedImage bi, int nw, int nh) throws IOException {

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

    /**
     * restace to nh x nw while maintaining aspect ratio
     *
     * @param bi
     * @param nw
     * @param nh
     * @return
     * @throws IOException
     */
    public BufferedImage downScaleImage(BufferedImage bi, int nw, int nh) throws IOException {
        if (Logger.getLogger().isEnabled()) {
            Logger.getLogger().log("ThumbnailGenerator.downScaleImage()  original image is " + bi.getWidth() + "x"
                + bi.getHeight());
          }
        BufferedImage scaledBI = null;
        if (Logger.getLogger().isEnabled()) {
            Logger.getLogger().log("ThumbnailGenerator.downScaleImage() requested to " + nw + "x" + nh);
          }


        float h_original = bi.getHeight();
        float w_original = bi.getWidth();

        float h_ratio = h_original / nh;
        float w_ratio = w_original / nw;
        //ensure we have at least one
        if (h_ratio < 1) {
            h_ratio = 1;
        }
        if (w_ratio < 1) {
            w_ratio = 1;
        }

        float ratio = Math.max(h_ratio, w_ratio);

        int fWidth = Math.round(w_original / ratio);
        int fHeight = Math.round(h_original / ratio);

        if (Logger.getLogger().isEnabled()) {
            Logger.getLogger().log("resizing to " + fWidth + "x" + fHeight + " with scale " + ratio);
        }
        scaledBI = new BufferedImage(fWidth, fHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaledBI.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(bi, 0, 0, fWidth, fHeight, null);
        g.dispose();
        // }
        return scaledBI;
    }


    public String generateMD5(File f) throws IOException {
        InputStream fis = new FileInputStream(f);
        byte[] buffer = DigestUtils.md5(fis);
        String s = DigestUtils.md5Hex(buffer);

        fis.close();
        return s;
    }

    protected int[] generateThumbnail(File f) {
        // byte[] data;
        BufferedImage source;
        int[] data1 = null;
        try {
            source = ImageIO.read(f);

            BufferedImage dest = null;
            if (software) {
                dest = this.downScaleImageToGray(source, WIDTH, HEIGHT);
            }
            // ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // ImageIO.write(dest, "jpg", baos);
            // baos.flush();
            // data = baos.toByteArray();
            // baos.close();

            data1 = new int[dest.getWidth() * dest.getHeight()];
            dest.getRGB(0, 0, dest.getWidth(), dest.getHeight(), data1, 0, dest.getWidth());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return data1;
    }

    public MediaFileDescriptor buildMediaDescriptor(File f) {
        MediaFileDescriptor id = new MediaFileDescriptor(this.dbManager);
        int[] data;
        String md5;
        try {
            id.setPath(f.getCanonicalPath());
            id.setMtime(f.lastModified());
            id.setSize(f.length());
            // generate thumbnails only for images, not video
            if (Utils.isValideImageName(f.getName())) {
                String sig = ImageHash.generateSignature(f.getCanonicalPath());
                id.setHash(sig);
            }
            md5 = generateMD5(f);
            id.setMd5Digest(md5);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error processing  file " + f.getName());
            e.printStackTrace();
        }
        return id;
    }

    public void generateAndSave(File f) {
        //	if (Utils.isValideImageName(f.getName()) || Utils.isValideVideoName(f.getName())) {
        // System.out.println("ThumbnailGenerator.generateAndSave() processing "
        // + f);
        // System.out.println("checking if in DB");
        try {
            if (dbManager.isInDataBaseBasedOnName(f.getCanonicalPath())) {
                // System.out.println("ThumbnailGenerator.generateImageDescriptor() Already in DB, ignoring");
                if (log.isEnabled()) {
                    log.log(f.getCanonicalPath() + " already in DB");
                }
            } else {
                MediaFileDescriptor id = this.buildMediaDescriptor(f);
                if (id != null) {
                    dbManager.saveToDB(id);
                }
                if (log.isEnabled()) {
                    log.log(f.getCanonicalPath() + " ..... size  " + (f.length() / 1024) + " KiB OK " + executorService.getActiveCount() + " threads running");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //}
    }

    public void process(String path) {
        try {
            this.process(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void process(File fd) throws IOException {
        if (isValideFile(fd)) {
            this.generateAndSave(fd);
            // }
        } else {
            if (fd.isDirectory()) {
                String entries[] = fd.list();
                if (entries != null) {
                    for (int i = 0; i < entries.length; i++) {
                        File f = new File(fd.getCanonicalPath() + "/" + entries[i]);
                        if (isValideFile(fd)) {
                            this.generateAndSave(f);
                        } else {
                            this.process(f);
                        }
                    }
                }
            }
        }
    }

    public boolean isValideFile(File fd) {
        return fd.isFile(); //&& (Utils.isValideImageName(fd.getName()) || Utils.isValideVideoName(fd.getName()));
    }

    public void processMT(String path) {
        // System.out.println("ThumbnailGenerator.processMTRoot()");
        try {
            this.processMT(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void processMT(File fd) throws IOException {
        if (isValideFile(fd)) {
            asyncProcessing(fd);
        } else {
            if (fd.isDirectory()) {
                String entries[] = fd.list();
                if (entries != null) {
                    for (int i = 0; i < entries.length; i++) {
                        File f = new File(fd.getCanonicalPath() + "/" + entries[i]);
                        if (isValideFile(fd)) {
                            asyncProcessing(f);
                        } else {
                            this.processMT(f);
                        }
                    }
                }
            }
        }
    }




    public void asyncProcessing(File f) {
        executorService.submit(new RunnableProcess(f));
    }





    protected class RunnableProcess implements Runnable {
        protected File fd;

        public RunnableProcess(File fd) {
            this.fd = fd;
        }

        //	@Override
        public void run() {
            generateAndSave(fd);
        }
    }


    public static void main(String[] args) {
        ThumbnailGenerator tg = new ThumbnailGenerator(null);
        MediaFileDescriptor mf = tg.buildMediaDescriptor(new File(args[0]));

        BufferedImage image = null; //new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage target = null;
        try {
            image = ImageIO.read(new FileInputStream(args[0]));
            //   InputStream in = new ByteArrayInputStream(mf.getDataAsByte());
            // image = ImageIO.read(in);
            // image.setRGB(0,0,WIDTH,HEIGHT,mf.getData(),0,WIDTH);
            target = tg.downScaleImage(image, 200, 200);
            System.out.println("ThumbnailGenerator.main image is " + target);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(target, "JPEG", out);
            System.out.println("ThumbnailGenerator.main size of thumbnails is " + out.toByteArray().length  +  " bytes");

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


//
// Use a label to display the image
        JFrame frame = new JFrame();

        JLabel lblimage = new JLabel(new ImageIcon(target));
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(lblimage);

// add more components here
        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);

    }

}
