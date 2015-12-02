package fr.thumbnailsdb;

import fr.thumbnailsdb.bktree.BKTree;
import fr.thumbnailsdb.bktree.RMSEDistance;
import fr.thumbnailsdb.dbservices.DBManager;
import fr.thumbnailsdb.dcandidate.CandidateIterator;
import fr.thumbnailsdb.dcandidate.CandidatePriorityQueue;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptorBuilder;
import fr.thumbnailsdb.lshbuilders.LSHManager;
import fr.thumbnailsdb.utils.Configuration;
import fr.thumbnailsdb.utils.ProgressBar;
import fr.thumbnailsdb.vptree.VPTree;
import fr.thumbnailsdb.vptree.VPTreeBuilder;
import fr.thumbnailsdb.vptree.distances.VPRMSEDistance;
import org.perf4j.LoggingStopWatch;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimilarImageFinder {


    //indicate whether we use the full path in the cache
    //or rely on indexes for lower memory footprint
    public static boolean USE_FULL_PATH = false;


    protected DBManager thumbstore;
    protected LSHManager lshManager;

    protected MediaFileDescriptorBuilder mediaFileDescriptorBuilder;


    protected BKTree<MediaFileDescriptor> bkTree;// = new BKTree<String>(new RMSEDistance());

    protected VPTree vpTree;

    //for parallel comparison of images
    private ExecutorService executorService = Executors.newFixedThreadPool(10);


    public SimilarImageFinder(DBManager c, MediaFileDescriptorBuilder mediaFileDescriptorBuilder , LSHManager lshManager) {
        this.thumbstore = c;
        this.mediaFileDescriptorBuilder = mediaFileDescriptorBuilder;
        this.lshManager = lshManager;
    }

    public Collection<MediaFileDescriptor> findSimilarMedia(String source, int max) {
        MediaIndexer tg = new MediaIndexer(null, this.mediaFileDescriptorBuilder);
        MediaFileDescriptor id = tg.buildMediaDescriptor(new File(source));
       // Collection<MediaFileDescriptor> result = this.findSimilarImage(id, max);
        if (id==null) {
            System.err.println("Error cannot load image "  + source);
        }

        Collection<MediaFileDescriptor> result = this.findSimilarImageUsingLSH(id, max);
            return result;
    }


    protected BKTree<MediaFileDescriptor> getPreloadedDescriptorsBKTree() {
        if (bkTree == null) {
            int size = thumbstore.size();
            bkTree = new BKTree<MediaFileDescriptor>(new RMSEDistance());
            ResultSet res = thumbstore.getAllInDataBase();
                try {
                    while (res.next()) {
                        String path = res.getString("path");
                        byte[] d = res.getBytes("data");
                        if (d != null) {
                            int[] idata = Utils.toIntArray(d);
                            if (idata != null) {

                                MediaFileDescriptor imd = new MediaFileDescriptor();
                                imd.setPath(path);
                                //TODO: handle signature here
                                //  imd.setData(idata);
                                bkTree.add(imd);
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

        }
        System.out.println("SimilarImageFinder.getPreloadedDescriptors records in BKTree : " + bkTree.size());
        return bkTree;
    }

    protected VPTree getPreloadedDescriptorsVPTree() {
        if (vpTree == null) {
            int size = thumbstore.size();
            vpTree = new VPTree();
            VPTreeBuilder builder = new VPTreeBuilder(new VPRMSEDistance(),this.thumbstore);
            ArrayList<MediaFileDescriptor> al = new ArrayList<MediaFileDescriptor>(size);
            ResultSet res = thumbstore.getAllInDataBase();
                try {
                    while (res.next()) {
                        String path = res.getString("path");
                        //  byte[] d = res.getBytes("data");
                        String s = res.getString("hash");
                        if (s != null) {

                            MediaFileDescriptor imd = new MediaFileDescriptor();
                            imd.setPath(path);
                            imd.setHash(s);
//                                imd.setData(idata);
                            //TODO: handle signature here

                            al.add(imd);

                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            System.out.println("SimilarImageFinder.getPreloadedDescriptors array list built , creating tree");
            vpTree = builder.buildVPTree(al);
        }
        System.out.println("SimilarImageFinder.getPreloadedDescriptors records in VPTree : " + vpTree);
        return vpTree;


    }


    protected Collection<MediaFileDescriptor> findSimilarImageUsingLSH(MediaFileDescriptor id, int max) {

        List<Candidate> al = this.lshManager.findCandidatesUsingLSH(id);
        Iterator<Candidate> it = al.iterator();
        LoggingStopWatch watch = null;
        if (Configuration.timing()) {
            watch = new LoggingStopWatch("findSimilarImageUsingLSH");
            watch.start();
        }
        Status.getStatus().setStringStatus(Status.FIND_SIMILAR + " using LSH");
        CandidatePriorityQueue queue = new CandidatePriorityQueue(max);
        String sourceHash=id.getHash();
        while (it.hasNext()) {
            Candidate current = it.next();
            String sig = current.getHash();
            if (sig == null) {
                continue;
            }
            double distance = ImageComparator.compareUsingHammingDistance(sourceHash, sig);
            queue.add(current,distance);
        }
        Status.getStatus().setStringStatus(Status.IDLE);
        CandidateIterator it2 = queue.iterator();
        ArrayList<MediaFileDescriptor> finalArray = new ArrayList<>() ;
        while (it2.hasNext()) {
             Candidate c= it2.next();
             MediaFileDescriptor md = mediaFileDescriptorBuilder.getMediaFileDescriptor(c.getIndex());
            if (md!=null) {
             md.setDistance(it2.distance());
             finalArray.add(md);
            }
        }
            Collections.sort(finalArray,new Comparator<MediaFileDescriptor>(){
//

            public int compare(MediaFileDescriptor o1, MediaFileDescriptor o2) {
                double e1 = o1.getDistance();
                double e2 = o2.getDistance();
                return Double.compare(e1, e2);
            }
        });

        if (Configuration.timing()) {
            watch.stop();
        }

        return finalArray; //Arrays.asList(arr);

    }

    protected Collection<MediaFileDescriptor> findSimilarImage(MediaFileDescriptor id, int max) {

        PriorityQueue<MediaFileDescriptor> queue = new PriorityQueue<MediaFileDescriptor>(max, new Comparator<MediaFileDescriptor>() {
            //	@Override
            public int compare(MediaFileDescriptor o1, MediaFileDescriptor o2) {
                double e1 = o1.getDistance();
                double e2 = o2.getDistance();
                //Sorted in reverse order
                return Double.compare(e2, e1);
            }
        });

        Iterator<MediaFileDescriptor> it = thumbstore.getPreloadedDescriptors().iterator();
        Status.getStatus().setStringStatus(Status.FIND_SIMILAR);
        int size = thumbstore.getPreloadedDescriptors().size();
        int processed = 0;
        ProgressBar pb = new ProgressBar(0, size, size / 100);
        int increment = size / 100;

        int processedSinceLastTick = 0;

        while (it.hasNext()) {
            MediaFileDescriptor current = it.next();
            String sig = current.getHash();
            if (sig == null) {
                continue;
            }
            double distance = ImageComparator.compareUsingHammingDistance(id.getHash(), sig);
            processed++;
            processedSinceLastTick++;

            if (processedSinceLastTick >= increment) {
                pb.tick(processed);
                Status.getStatus().setStringStatus(Status.FIND_SIMILAR + " " + pb.getPercent() + "%");
                processedSinceLastTick = 0;
            }

            if (queue.size() == max) {
                MediaFileDescriptor df = queue.peek();
                if (df.distance > distance) {
                    queue.poll();
                    MediaFileDescriptor imd = new MediaFileDescriptor();
                    imd.setPath(current.getPath());
                    imd.setDistance(distance);
                    imd.setHash(current.getHash());
                    imd.setConnection(current.getConnection());
                    imd.setId(current.getId());
                    queue.add(imd);
                }
            } else {
                MediaFileDescriptor imd = new MediaFileDescriptor();
                imd.setPath(current.getPath());
                imd.setDistance(distance);
                imd.setHash(current.getHash());
                imd.setConnection(current.getConnection());
                imd.setId(current.getId());
                queue.add(imd);
            }
        }

        System.out.println("SimilarImageFinder.findSimilarImage resulting queue has size " + queue.size());
        Status.getStatus().setStringStatus(Status.IDLE);

        MediaFileDescriptor[] arr = queue.toArray(new MediaFileDescriptor[]{});
        Arrays.sort(arr, new Comparator<MediaFileDescriptor>() {
            public int compare(MediaFileDescriptor o1, MediaFileDescriptor o2) {
                double e1 = o1.getDistance();
                double e2 = o2.getDistance();
                return Double.compare(e1, e2);
            }
        });
        return Arrays.asList(arr);
    }

    public void prettyPrintSimilarResults(TreeSet<MediaFileDescriptor> ts, int maxResults) {
        int i = 0;
        for (Iterator iterator = ts.iterator(); iterator.hasNext(); ) {
            i++;
            MediaFileDescriptor imageDescriptor = (MediaFileDescriptor) iterator.next();
            System.out.printf("%1.5f  %s\n", imageDescriptor.getDistance(), imageDescriptor.getPath());
            if (i >= maxResults) {
                break;
            }
        }
    }

    public ArrayList<MediaFileDescriptor> findIdenticalMedia(String source) {

        MediaIndexer tg = new MediaIndexer(null, this.mediaFileDescriptorBuilder);
        MediaFileDescriptor id = tg.buildMediaDescriptor(new File(source)); // ImageDescriptor.readFromDisk(s);
//        System.out.println(id.md5Digest);
        ArrayList<MediaFileDescriptor> al = new ArrayList<MediaFileDescriptor>();
        return thumbstore.getDuplicatesMD5(id);
    }


    public void prettyPrintIdenticalResults(ArrayList<MediaFileDescriptor> findIdenticalMedia) {
        Iterator<MediaFileDescriptor> it = findIdenticalMedia.iterator();
        while (it.hasNext()) {
            MediaFileDescriptor mediaFileDescriptor = (MediaFileDescriptor) it.next();
            System.out.println(mediaFileDescriptor.getPath() + " " + mediaFileDescriptor.getSize());
        }
    }

    public String prettyStringIdenticalResults(ArrayList<MediaFileDescriptor> findIdenticalMedia, int max) {
        Iterator<MediaFileDescriptor> it = findIdenticalMedia.iterator();
        String result = "";
        int i = 0;
        while (it.hasNext() && i < max) {
            MediaFileDescriptor mediaFileDescriptor = (MediaFileDescriptor) it.next();
            i++;
            result += mediaFileDescriptor.getPath() + " " + mediaFileDescriptor.getSize();
        }

        return result;
    }


    public void testFindSimilarImages(DBManager tb, String path) {
        System.out.println("DBManager.test() reading descriptor from disk ");
        //String s = "/user/fhuet/desktop/home/workspaces/rechercheefficaceimagessimilaires/images/original.jpg";
        System.out.println("DBManager.testFindSimilarImages() Reference Image " + path);

        MediaIndexer tg = new MediaIndexer(tb, this.mediaFileDescriptorBuilder);
        MediaFileDescriptor id = tg.buildMediaDescriptor(new File(path));
        MediaFileDescriptorBuilder mediaFileDescriptorBuilder = new MediaFileDescriptorBuilder();
        LSHManager lshManager = new LSHManager(tb);
        SimilarImageFinder sif = new SimilarImageFinder(tb,mediaFileDescriptorBuilder,lshManager );
        sif.findSimilarImageUsingLSH(id,20);
        //this.prettyPrintSimilarResults(this.findSimilarImage(id, 2), 2);
    }

    public static void main(String[] args) {
        MediaFileDescriptorBuilder mediaFileDescriptorBuilder = new MediaFileDescriptorBuilder();
        DBManager tb = new DBManager(null,mediaFileDescriptorBuilder);
        LSHManager lshManager = new LSHManager(tb);
        SimilarImageFinder si = new SimilarImageFinder(tb,mediaFileDescriptorBuilder,lshManager );
        si.testFindSimilarImages(tb, args[0]);
    }

}
