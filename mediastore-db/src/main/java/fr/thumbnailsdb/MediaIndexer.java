package fr.thumbnailsdb;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import fr.thumbnailsdb.hash.ImageHash;
import fr.thumbnailsdb.treewalker.TreeWalker;
import fr.thumbnailsdb.utils.Configuration;
import fr.thumbnailsdb.utils.Logger;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

public class MediaIndexer {

    protected boolean debug;
    protected boolean software = true;
    protected ThumbStore ts;

    protected boolean forceGPSUpdate = Configuration.forceGPS();
    protected boolean forceHashUpdate = Configuration.forceUpdate();


    protected Logger log = Logger.getLogger();

    protected int newFiles = 0;
    protected int updatedFiles = 0;
    protected int recentModifications = 0;
    protected int modificationsBeforeReport = 100;
    protected int processedFiles;
    protected long lastProgressTime;
    protected int currentProgressSize;

    protected int totalNumberOfFiles;

    //default value for the number of threads in the pool.
    protected int maxThreads;

    protected ThreadPoolExecutor executorService;

    private boolean dryRun = Configuration.dryRun();


    public MediaIndexer(ThumbStore t) {

        maxThreads = Configuration.getMaxIndexerThreads();
        System.out.println("MediaIndexer.MediaIndexer Max Threads = " + maxThreads);
        executorService= new ThreadPoolExecutor(maxThreads, maxThreads, 0L, TimeUnit.MILLISECONDS,
                new LimitedQueue<Runnable>(50));
        this.ts = t;
    }

    private class LimitedQueue<E> extends LinkedBlockingQueue<E> {
        public LimitedQueue(int maxSize) {
            super(maxSize);
        }

        @Override
        public boolean add(E e) {
            return super.add(e);
        }

        @Override
        public boolean offer(E e) {
            //	System.out.println("MediaIndexer.LimitedQueue.offer() " + this.size());
            // turn offer() and add() into a blocking calls (unless interrupted)
            try {
                put(e);
                return true;
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            // System.out.println("MediaIndexer.LimitedQueue.offer()   ... done");
            return false;
        }

        @Override
        public E take() throws InterruptedException {
            return super.take();
        }
    }

    /**
     * Load the image and resize it if necessary
     * @param bi
     * @return
     * @throws IOException
     */
    public BufferedImage downScaleImageToGray(BufferedImage bi, int nw, int nh) throws IOException {
        if (debug) {
            System.out.println("MediaIndexer.downScaleImageToGray()  original image is " + bi.getWidth() + "x"
                    + bi.getHeight());
        }
        BufferedImage scaledBI = null;
        if (debug) {
            System.out.println("MediaIndexer.downScaleImageToGray() to " + nw + "x" + nh);
        }
        if (debug) {
            System.out.println("resizing to " + nw + "x" + nh);
        }
        scaledBI = new BufferedImage(nw, nh, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = scaledBI.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(bi, 0, 0, nw, nh, null);
        g.dispose();
        return scaledBI;
    }

    public String generateMD5(File f) throws IOException {
        InputStream fis = new BufferedInputStream(new FileInputStream(f));
        String s = this.generateMD5(fis);
        fis.close();
        return s;
    }

   public String generateMD5(InputStream fi) throws IOException {
       byte[] buffer = DigestUtils.md5(fi);
       String s = DigestUtils.md5Hex(buffer);
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
                dest = this.downScaleImageToGray(source, 10, 10);
            }
            data1 = new int[dest.getWidth() * dest.getHeight()];
            dest.getRGB(0, 0, dest.getWidth(), dest.getHeight(), data1, 0, dest.getWidth());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return data1;
    }

     private ByteArrayInputStream readFileToMemory(File f) {
         try {
             return new ByteArrayInputStream(FileUtils.readFileToByteArray(f));
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         return null;
     }

    public MediaFileDescriptor buildMediaDescriptor(File f) {
        MediaFileDescriptor id = new MediaFileDescriptor();
        int[] data;
        String md5;
        try {
            id.setPath(f.getCanonicalPath());
            id.setMtime(f.lastModified());
            id.setSize(f.length());
            // generate thumbnails only for images, not video
            if (Utils.isValideImageName(f.getName())) {
                MetaDataFinder mdf = new MetaDataFinder(f);
                double[] latLon = mdf.getLatLong();
                if (latLon != null) {
                    id.setLat(latLon[0]);
                    id.setLon(latLon[1]);
                }
                //bufferize images in memory, read directly from file for others
                ByteArrayInputStream fbi = this.readFileToMemory(f);
                id.setHash(new ImageHash().generateSignature(fbi));
                fbi.reset();
                md5 = generateMD5(fbi);
                id.setMd5Digest(md5);
            }   else {
            md5 = generateMD5(f);
            id.setMd5Digest(md5);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error processing  file " + f.getName());
            e.printStackTrace();
        }
        return id;
    }

    public void generateAndSave(File f) {
        System.out.print(".");
        currentProgressSize+=f.length()/1024;
        try {
            MediaFileDescriptor mf = ts.getMediaFileDescriptor(f.getCanonicalPath());
            Logger.getLogger().err("MediaIndexer.generateAndSave " + f + " descriptor: " + mf);

            if ((mf != null) && (f.lastModified() == mf.getMtime())) {
                //Descriptor exists with same mtime
                Logger.getLogger().err("MediaIndexer.generateImageDescriptor() Already in DB, ignoring with same mtime");
                Logger.getLogger().err("MediaIndexer.generateImageDescriptor() In   DB : " + mf.getMtime());
                Logger.getLogger().err("MediaIndexer.generateImageDescriptor() On Disk : " + f.lastModified());
                boolean update = false;

                if (forceGPSUpdate) {
                    //  MediaFileDescriptor mfd = ts.getMediaFileDescriptor(f.getCanonicalPath());
                    MetaDataFinder mdf = new MetaDataFinder(f);
                    double latLon[] = mdf.getLatLong();
                    Logger.getLogger().err("MediaIndexer.generateAndSave working on " + f);
                    if (latLon != null) {
                        mf.setLat(latLon[0]);
                        mf.setLon(latLon[1]);
                        Logger.getLogger().err("MediaIndexer : forced update for GPS data for " + f);
                        updateToDB(mf);
                        update = true;
                    }
                }
                if (forceHashUpdate || (mf.getHash() == null)) {
                    if (Utils.isValideImageName(f.getName())) {
                        mf.setHash(new ImageHash().generateSignature(f.getCanonicalPath()));
                        updateToDB(mf);
                        update = true;
                    }

                }
                // if (update) {
                this.fileCreatedUpdated(false, update);
                //}
            } else {
                Logger.getLogger().err("MediaIndexer.generateAndSave building descriptor");
                MediaFileDescriptor id = this.buildMediaDescriptor(f);
                if (id != null) {
                    if ((mf != null) && (f.lastModified() != mf.getMtime())) {
                        //we need to update it
                        updateToDB(id);
                        this.fileCreatedUpdated(false, true);
                    } else {
                        saveToDB(id);
                        this.fileCreatedUpdated(true, false);
                    }
                    if (log.isEnabled()) {
                        log.log(f.getCanonicalPath() + " ..... size  " + (f.length() / 1024) + " KiB OK " + executorService.getActiveCount() + " threads running");
                    }
                } else {
                    this.fileCreatedUpdated(false, false);
                }
            }
        } catch (IOException e) {
            System.err.println("Error processing  file " + f.getName());
            e.printStackTrace();
        }
    }


    private void updateToDB(MediaFileDescriptor id) {
        if (!dryRun) {
           ts.updateToDB(id);
        }
    }

    private void saveToDB(MediaFileDescriptor id) {
        if(!dryRun) {
           ts.saveToDB(id);
        }
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


    public void fileCreatedUpdated(boolean created, boolean modified) {
        if (created) {
            newFiles++;
        }
        if (modified) {
            updatedFiles++;
        }
        recentModifications++;
        processedFiles++;
        //  System.out.println("MediaIndexer.fileCreatedUpdated");
        if (recentModifications > modificationsBeforeReport) {
            System.out.println("\nProcessed files : " + processedFiles + "/" + totalNumberOfFiles);
            System.out.println("Speed : " + (currentProgressSize/(System.currentTimeMillis()-lastProgressTime)) + " MiB/s");
            lastProgressTime=System.currentTimeMillis();
            recentModifications = 0;
            currentProgressSize=0;
        }
    }

    public void processMTRoot(String path) {
        long t0=System.currentTimeMillis();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        try {
            ts.addIndexPath(new File(path).getCanonicalPath());
            System.out.println("MediaIndexer.processMTRoot()" + path);
            System.out.println("MediaIndexer.processMTRoot() started at time " + dateFormat.format(date));
            System.out.println("MediaIndexer.processMTRoot() computing number of files...");
            totalNumberOfFiles = this.countFiles(path);
            lastProgressTime = System.currentTimeMillis();
            System.out.println("Number of files to explore " + totalNumberOfFiles);
            if (executorService.isShutdown()) {
                executorService = new ThreadPoolExecutor(maxThreads, maxThreads, 0L, TimeUnit.MILLISECONDS,
                        new LimitedQueue<Runnable>(50));
            }

            this.processedFiles=0;
           // this.processMT(new File(path));
            TreeWalker t = new TreeWalker(this);
             t.walk(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long t1=System.currentTimeMillis();
        date = new Date();
        System.out.println("MediaIndexer.processMTRoot() finished at time " + dateFormat.format(date));
        System.out.println("MediaIndexer.processMTRoot() found " + newFiles + " new files");
        System.out.println("MediaIndexer.processMTRoot() updated " + updatedFiles + " files");
        System.out.println("MediaIndexer.processMTRoot() total " + ts.size() + " files");
        System.out.println("MediaIndexer.processMTRoot took " + (t1-t0)/1000 + " s");
    }


//    public void processMTV2(String path) {
//        TreeWalker t = new TreeWalker(this);
//        t.walk(path);
//        executorService.shutdown();
//        try {
//            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }


    public int countFiles(String root) {
        CountFile fileProcessor = new CountFile();
        try {
            Files.walkFileTree(Paths.get(root), fileProcessor);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileProcessor.getTotal();
    }

    private static final class CountFile extends SimpleFileVisitor<Path> {
        int total = 0;

        @Override
        public FileVisitResult visitFile(
                Path aFile, BasicFileAttributes aAttrs
        ) throws IOException {
            total++;
            //System.out.println("Processing file:" + aFile);
            return FileVisitResult.CONTINUE;
        }

        public int getTotal() {
            return this.total;
        }

    }


    public void processMT(File fd) throws IOException {
        if (isValideFile(fd)) {
            executorService.submit(new RunnableProcess(fd));
        } else {
            if (fd.isDirectory()) {
                Logger.getLogger().err("MediaIndexer.processMT processing () " + fd);
                String entries[] = fd.list();
                if (entries != null) {
                    for (int i = 0; i < entries.length; i++) {
                        File f = new File(fd.getCanonicalPath() + "/" + entries[i]);
                        if (isValideFile(f)) {
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



    public void updateDB() {
        this.updateDB(ts.getIndexedPaths().toArray(new String[]{}));
    }

    public void updateDB(String[] al) {

        for (int i = 0; i < al.length; i++) {
            String s = al[i];
            File f = new File(s);
            Status.getStatus().setStringStatus("Updating folder " + s);

            if (f.exists()) {
                Logger.getLogger().log("MediaIndexer.updateDB updating " + s);
                processMTRoot(s);
            } else {
                System.out.println("MediaIndexer.updateDB path " + s + " not reachable, ignoring");
            }
        }
        Status.getStatus().setStringStatus(Status.IDLE);
    }


    protected void submit(RunnableProcess rp) {
        executorService.submit(rp);
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
        String pathToDB = "test";
        String source = ".";
        if (args.length == 2 || args.length == 4) {
            for (int i = 0; i < args.length; i++) {
                if ("-db".equals(args[i])) {
                    pathToDB = args[i + 1];
                    i++;
                }
                if ("-source".equals(args[i])) {
                    source = args[i + 1];
                    i++;
                }
            }
        } else {
            System.err.println("Usage: java " + MediaIndexer.class.getName()
                    + "[-db path_to_db] -source folder_or_file_to_process");
            System.exit(0);
        }
        ThumbStore ts = new ThumbStore(pathToDB);
        MediaIndexer tb = new MediaIndexer(ts);
        File fs = new File(source);
            tb.processMTRoot(source);

    }
}
