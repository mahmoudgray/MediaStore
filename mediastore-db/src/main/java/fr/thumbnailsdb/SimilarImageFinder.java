package fr.thumbnailsdb;

import fr.thumbnailsdb.candidates.Candidate;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptor;
import fr.thumbnailsdb.dbservices.DBManager;
import fr.thumbnailsdb.candidates.CandidateIterator;
import fr.thumbnailsdb.candidates.CandidatePriorityQueue;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptorBuilder;
import fr.thumbnailsdb.lsh.LSHManager;
import fr.thumbnailsdb.utils.Configuration;
import fr.thumbnailsdb.utils.ImageComparator;
import fr.thumbnailsdb.utils.ProgressBar;
import org.perf4j.LoggingStopWatch;
import java.io.*;
import java.util.*;

public class SimilarImageFinder {


    //indicate whether we use the full path in the cache
    //or rely on indexes for lower memory footprint
    public static boolean USE_FULL_PATH = false;


    protected DBManager dbManager;
    protected LSHManager lshManager;
    protected MediaFileDescriptorBuilder mediaFileDescriptorBuilder;


    public SimilarImageFinder(DBManager c, MediaFileDescriptorBuilder mediaFileDescriptorBuilder , LSHManager lshManager) {
        this.dbManager = c;
        this.mediaFileDescriptorBuilder = mediaFileDescriptorBuilder;
        this.lshManager = lshManager;
    }
    public Collection<MediaFileDescriptor> findSimilarImages(String source, int max) {
        MediaFileDescriptor mediaFileDescriptor = mediaFileDescriptorBuilder.buildMediaDescriptor(new File(source));
        if (mediaFileDescriptor==null) {
            System.err.println("Error cannot load image "  + source);
        }
        Collection<MediaFileDescriptor> result = this.findSimilarImageUsingLSH(mediaFileDescriptor, max);
        return result;
    }
    protected Collection<MediaFileDescriptor> findSimilarImageUsingLSH(MediaFileDescriptor sourceMediaFileDescriptor, int max) {
        List<Candidate> candidateList = this.lshManager.findCandidatesUsingLSH(sourceMediaFileDescriptor);
        Iterator<Candidate> lshIterator = candidateList.iterator();
        LoggingStopWatch watch = null;
        if (Configuration.timing()) {
            watch = new LoggingStopWatch("findSimilarImageUsingLSH");
            watch.start();
        }
        Status.getStatus().setStringStatus(Status.FIND_SIMILAR + " using LSH");
        CandidatePriorityQueue candidatePriorityQueue = new CandidatePriorityQueue(max);
        String sourceHash=sourceMediaFileDescriptor.getHash();
        while (lshIterator.hasNext()) {
            Candidate candidate = lshIterator.next();
            String candidateHash = candidate.getHash();
            if (candidateHash == null) {
                continue;
            }
            double distance = ImageComparator.compareUsingHammingDistance(sourceHash, candidateHash);
            candidatePriorityQueue.add(candidate,distance);
        }
        Status.getStatus().setStringStatus(Status.IDLE);
        CandidateIterator candidateIterator = candidatePriorityQueue.iterator();
        ArrayList<MediaFileDescriptor> finalCandidatesList = new ArrayList<>() ;
        while (candidateIterator.hasNext()) {
             Candidate c= candidateIterator.next();
             MediaFileDescriptor mDescriptor = mediaFileDescriptorBuilder.getMediaFileDescriptorFromDB(c.getIndex());
            if (mDescriptor!=null) {
                 mDescriptor.setDistance(candidateIterator.distance());
                 finalCandidatesList.add(mDescriptor);
            }
        }
            Collections.sort(finalCandidatesList,new Comparator<MediaFileDescriptor>(){
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

        return finalCandidatesList;

    }
    public ArrayList<MediaFileDescriptor> findIdenticalMedia(String source) {
        MediaFileDescriptor mediaFileDescriptor = mediaFileDescriptorBuilder.buildMediaDescriptor(new File(source));
        return dbManager.getDuplicatesMD5(mediaFileDescriptor);
    }
    public void prettyPrintIdenticalResults(ArrayList<MediaFileDescriptor> findIdenticalMedia) {
        Iterator<MediaFileDescriptor> it = findIdenticalMedia.iterator();
        while (it.hasNext()) {
            MediaFileDescriptor mediaFileDescriptor = it.next();
            System.out.println(mediaFileDescriptor.getPath() + " " + mediaFileDescriptor.getSize());
        }
    }
    protected Collection<MediaFileDescriptor> findSimilarImage(MediaFileDescriptor mediaFileDescriptor, int max) {

        PriorityQueue<MediaFileDescriptor> queue = new PriorityQueue<MediaFileDescriptor>(max, new Comparator<MediaFileDescriptor>() {
            //	@Override
            public int compare(MediaFileDescriptor o1, MediaFileDescriptor o2) {
                double e1 = o1.getDistance();
                double e2 = o2.getDistance();
                //Sorted in reverse order
                return Double.compare(e2, e1);
            }
        });

        Iterator<MediaFileDescriptor> it = PreloadedDescriptors.getPreloadedDescriptors(dbManager).iterator();
        Status.getStatus().setStringStatus(Status.FIND_SIMILAR);
        int size = PreloadedDescriptors.getPreloadedDescriptors(dbManager).size();
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
            double distance = ImageComparator.compareUsingHammingDistance(mediaFileDescriptor.getHash(), sig);
            processed++;
            processedSinceLastTick++;

            if (processedSinceLastTick >= increment) {
                pb.tick(processed);
                Status.getStatus().setStringStatus(Status.FIND_SIMILAR + " " + pb.getPercent() + "%");
                processedSinceLastTick = 0;
            }

            if (queue.size() == max) {
                MediaFileDescriptor df = queue.peek();
                if (df.getDistance() > distance) {
                    queue.poll();
                    MediaFileDescriptor imd = new MediaFileDescriptor(this.dbManager);
                    imd.setPath(current.getPath());
                    imd.setDistance(distance);
                    imd.setHash(current.getHash());
                    imd.setConnection(current.getConnection());
                    imd.setId(current.getId());
                    queue.add(imd);
                }
            } else {
                MediaFileDescriptor imd = new MediaFileDescriptor(this.dbManager);
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
    public void testFindSimilarImages(DBManager dbManager, String path) {
        System.out.println("DBManager.test() reading descriptor from disk ");
        System.out.println("DBManager.testFindSimilarImages() Reference Image " + path);
        MediaFileDescriptor mediaFileDescriptor = mediaFileDescriptorBuilder.buildMediaDescriptor(new File(path));
        MediaFileDescriptorBuilder mediaFileDescriptorBuilder = new MediaFileDescriptorBuilder();
        LSHManager lshManager = new LSHManager(dbManager);
        SimilarImageFinder sif = new SimilarImageFinder(dbManager,mediaFileDescriptorBuilder,lshManager );
        sif.findSimilarImageUsingLSH(mediaFileDescriptor,20);
    }
    public static void main(String[] args) {
        MediaFileDescriptorBuilder mediaFileDescriptorBuilder = new MediaFileDescriptorBuilder();
        DBManager tb = new DBManager(null,mediaFileDescriptorBuilder);
        LSHManager lshManager = new LSHManager(tb);
        SimilarImageFinder si = new SimilarImageFinder(tb,mediaFileDescriptorBuilder,lshManager );
        si.testFindSimilarImages(tb, args[0]);
    }

}
