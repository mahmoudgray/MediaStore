package fr.thumbnailsdb;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import fr.thumbnailsdb.dbservices.DBManager;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptorBuilder;
import fr.thumbnailsdb.hash.ImageHash;
import fr.thumbnailsdb.utils.MD5Generator;
import fr.thumbnailsdb.treewalker.TreeWalker;
import fr.thumbnailsdb.utils.Configuration;
import fr.thumbnailsdb.utils.LimitedQueue;
import fr.thumbnailsdb.utils.Logger;

public class MediaIndexer {

    protected boolean debug;
    protected boolean software = true;
    protected DBManager dbManager;
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
    protected MediaFileDescriptorBuilder mediaFileDescriptorBuilder;
    protected MD5Generator md5Generator;


    public MediaIndexer(DBManager t, MediaFileDescriptorBuilder mediaFileDescriptorBuilder) {
        maxThreads = Configuration.getMaxIndexerThreads();
        System.out.println("MediaIndexer.MediaIndexer Max Threads = " + maxThreads);
        executorService= new ThreadPoolExecutor(maxThreads, maxThreads, 0L, TimeUnit.MILLISECONDS,
                new LimitedQueue<Runnable>(50));
        this.dbManager = t;
        this.mediaFileDescriptorBuilder = mediaFileDescriptorBuilder;
        this.md5Generator = new MD5Generator();
    }
    public void generateAndSave(File f) {
        System.out.print(".");
        currentProgressSize+=f.length()/1024;
        try {
            MediaFileDescriptor mf = mediaFileDescriptorBuilder.getMediaFileDescriptorFromDB(f.getCanonicalPath());
            Logger.getLogger().err("MediaIndexer.generateAndSave " + f + " descriptor: " + mf);
            if ((mf != null) && (f.lastModified() == mf.getMtime())) {
                //Descriptor exists with same mtime
                Logger.getLogger().err("MediaIndexer.generateImageDescriptor() Already in DB, ignoring with same mtime");
                Logger.getLogger().err("MediaIndexer.generateImageDescriptor() In   DB : " + mf.getMtime());
                Logger.getLogger().err("MediaIndexer.generateImageDescriptor() On Disk : " + f.lastModified());
                boolean update = false;
                if (forceGPSUpdate) {
                    MetaDataFinder mdf = new MetaDataFinder(f);
                    double latLon[] = mdf.getLatLong();
                    Logger.getLogger().err("MediaIndexer.generateAndSave working on " + f);
                    if (latLon != null) {
                        mf.setLat(latLon[0]);
                        mf.setLon(latLon[1]);
                        Logger.getLogger().err("MediaIndexer : forced update for GPS data for " + f);
                        this.dbManager.updateToDB(mf);
                        update = true;
                    }
                }
                if (forceHashUpdate || (mf.getHash() == null)) {
                    if (Utils.isValideImageName(f.getName())) {
                        mf.setHash(new ImageHash().generateSignature(f.getCanonicalPath()));
                        this.dbManager.updateToDB(mf);
                        update = true;
                    }

                }
                this.fileCreatedUpdated(false, update);
            } else {
                Logger.getLogger().err("MediaIndexer.generateAndSave building descriptor");
                MediaFileDescriptor id = mediaFileDescriptorBuilder.buildMediaDescriptor(f);
                if (id != null) {
                    if ((mf != null) && (f.lastModified() != mf.getMtime())) {
                        //we need to update it
                        this.dbManager.updateToDB(id);
                        this.fileCreatedUpdated(false, true);
                    } else {
                        this.dbManager.saveToDB(id);
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
    public void process(String path) {
        try {
            this.process(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void process(File fd) throws IOException {
        if (Utils.isValideFile(fd)) {
            this.generateAndSave(fd);
            // }
        } else {
            if (fd.isDirectory()) {
                String entries[] = fd.list();
                if (entries != null) {
                    for (int i = 0; i < entries.length; i++) {
                        File f = new File(fd.getCanonicalPath() + "/" + entries[i]);
                        if (Utils.isValideFile(fd)) {
                            this.generateAndSave(f);
                        } else {
                            this.process(f);
                        }
                    }
                }
            }
        }
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
            dbManager.addIndexPath(new File(path).getCanonicalPath());
            System.out.println("MediaIndexer.processMTRoot()" + path);
            System.out.println("MediaIndexer.processMTRoot() started at time " + dateFormat.format(date));
            System.out.println("MediaIndexer.processMTRoot() computing number of files...");
            totalNumberOfFiles = Utils.countFilesInFolder(path);
            lastProgressTime = System.currentTimeMillis();
            System.out.println("Number of files to explore " + totalNumberOfFiles);
            if (executorService.isShutdown()) {
                executorService = new ThreadPoolExecutor(maxThreads, maxThreads, 0L, TimeUnit.MILLISECONDS,
                        new LimitedQueue<Runnable>(50));
            }
            this.processedFiles=0;
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
        System.out.println("MediaIndexer.processMTRoot() total " + dbManager.size() + " files");
        System.out.println("MediaIndexer.processMTRoot took " + (t1-t0)/1000 + " s");
    }
    public void asyncProcessing(File f) {
        executorService.submit(new RunnableProcess(f));
    }
    public void refreshIndexedPaths() {
        this.refreshIndexedPaths(dbManager.getIndexedPaths().toArray(new String[]{}));
    }
    public void refreshIndexedPaths(String[] al) {

        for (int i = 0; i < al.length; i++) {
            String s = al[i];
            File f = new File(s);
            Status.getStatus().setStringStatus("Updating folder " + s);

            if (f.exists()) {
                Logger.getLogger().log("MediaIndexer.refreshIndexedPaths updating " + s);
                processMTRoot(s);
            } else {
                System.out.println("MediaIndexer.refreshIndexedPaths path " + s + " not reachable, ignoring");
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
        MediaFileDescriptorBuilder mediaFileDescriptorBuilder = new MediaFileDescriptorBuilder();
        DBManager ts = new DBManager(pathToDB,mediaFileDescriptorBuilder );
        MediaIndexer tb = new MediaIndexer(ts,mediaFileDescriptorBuilder );
        File fs = new File(source);
            tb.processMTRoot(source);

    }
}
