package fr.thumbnailsdb;

import fr.thumbnailsdb.candidates.Candidate;
import fr.thumbnailsdb.dbservices.DBManagerIF;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptor;
import fr.thumbnailsdb.dbservices.DBManager;
import fr.thumbnailsdb.candidates.CandidateIterator;
import fr.thumbnailsdb.candidates.CandidatePriorityQueue;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptorBuilder;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptorIF;
import fr.thumbnailsdb.lsh.LSHManager;
import fr.thumbnailsdb.lsh.LSHManagerIF;
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


    protected DBManagerIF dbManagerIF;
    protected LSHManagerIF lshManagerIF;
    protected MediaFileDescriptorBuilder mediaFileDescriptorBuilder;


    public SimilarImageFinder(DBManagerIF c, MediaFileDescriptorBuilder mediaFileDescriptorBuilder , LSHManagerIF lshManagerIF) {
        this.dbManagerIF = c;
        this.mediaFileDescriptorBuilder = mediaFileDescriptorBuilder;
        this.lshManagerIF = lshManagerIF;
    }
    public Collection<MediaFileDescriptorIF> findSimilarImages(String source, int max) {
        MediaFileDescriptorIF mediaFileDescriptorIF = mediaFileDescriptorBuilder.buildMediaDescriptor(new File(source));
        if (mediaFileDescriptorIF ==null) {
            System.err.println("Error cannot load image "  + source);
        }
        Collection<MediaFileDescriptorIF> result = this.findSimilarImageUsingLSH(mediaFileDescriptorIF, max);
        return result;
    }
    protected Collection<MediaFileDescriptorIF> findSimilarImageUsingLSH(MediaFileDescriptorIF sourceMediaFileDescriptorIF, int max) {
        List<Candidate> candidateList = this.lshManagerIF.findCandidatesUsingLSH(sourceMediaFileDescriptorIF);
        Iterator<Candidate> lshIterator = candidateList.iterator();
        LoggingStopWatch watch = null;
        if (Configuration.timing()) {
            watch = new LoggingStopWatch("findSimilarImageUsingLSH");
            watch.start();
        }
        Status.getStatus().setStringStatus(Status.FIND_SIMILAR + " using LSH");
        CandidatePriorityQueue candidatePriorityQueue = new CandidatePriorityQueue(max);
        String sourceHash= sourceMediaFileDescriptorIF.getHash();
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
        ArrayList<MediaFileDescriptorIF> finalCandidatesList = new ArrayList<>() ;
        while (candidateIterator.hasNext()) {
             Candidate c= candidateIterator.next();
             MediaFileDescriptorIF mDescriptor = mediaFileDescriptorBuilder.getMediaFileDescriptorFromDB(c.getIndex());
            if (mDescriptor!=null) {
                 mDescriptor.setDistance(candidateIterator.distance());
                 finalCandidatesList.add(mDescriptor);
            }
        }
            Collections.sort(finalCandidatesList,new Comparator<MediaFileDescriptorIF>(){
//

            public int compare(MediaFileDescriptorIF o1, MediaFileDescriptorIF o2) {
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
    public ArrayList<MediaFileDescriptorIF> findIdenticalMedia(String source) {
        MediaFileDescriptorIF mediaFileDescriptorIF = mediaFileDescriptorBuilder.buildMediaDescriptor(new File(source));
        return dbManagerIF.getDuplicatesMD5(mediaFileDescriptorIF);
    }
    public void prettyPrintIdenticalResults(ArrayList<MediaFileDescriptorIF> findIdenticalMedia) {
        Iterator<MediaFileDescriptorIF> it = findIdenticalMedia.iterator();
        while (it.hasNext()) {
            MediaFileDescriptorIF mediaFileDescriptorIF = it.next();
            System.out.println(mediaFileDescriptorIF.getPath() + " " + mediaFileDescriptorIF.getSize());
        }
    }
    protected Collection<MediaFileDescriptorIF> findSimilarImage(MediaFileDescriptorIF mediaFileDescriptorIF, int max) {

        PriorityQueue<MediaFileDescriptorIF> queue = new PriorityQueue<MediaFileDescriptorIF>(max, new Comparator<MediaFileDescriptorIF>() {
            //	@Override
            public int compare(MediaFileDescriptorIF o1, MediaFileDescriptorIF o2) {
                double e1 = o1.getDistance();
                double e2 = o2.getDistance();
                //Sorted in reverse order
                return Double.compare(e2, e1);
            }
        });

        Iterator<MediaFileDescriptorIF> it = PreloadedDescriptors.getPreloadedDescriptors(dbManagerIF).iterator();
        Status.getStatus().setStringStatus(Status.FIND_SIMILAR);
        int size = PreloadedDescriptors.getPreloadedDescriptors(dbManagerIF).size();
        int processed = 0;
        ProgressBar pb = new ProgressBar(0, size, size / 100);
        int increment = size / 100;

        int processedSinceLastTick = 0;

        while (it.hasNext()) {
            MediaFileDescriptorIF current = it.next();
            String sig = current.getHash();
            if (sig == null) {
                continue;
            }
            double distance = ImageComparator.compareUsingHammingDistance(mediaFileDescriptorIF.getHash(), sig);
            processed++;
            processedSinceLastTick++;

            if (processedSinceLastTick >= increment) {
                pb.tick(processed);
                Status.getStatus().setStringStatus(Status.FIND_SIMILAR + " " + pb.getPercent() + "%");
                processedSinceLastTick = 0;
            }

            if (queue.size() == max) {
                MediaFileDescriptorIF df = queue.peek();
                if (df.getDistance() > distance) {
                    queue.poll();
                    MediaFileDescriptorIF imd = new MediaFileDescriptor(this.dbManagerIF);
                    imd.setPath(current.getPath());
                    imd.setDistance(distance);
                    imd.setHash(current.getHash());
                    imd.setConnection(current.getConnection());
                    imd.setId(current.getId());
                    queue.add(imd);
                }
            } else {
                MediaFileDescriptorIF imd = new MediaFileDescriptor(this.dbManagerIF);
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

        MediaFileDescriptorIF[] arr = queue.toArray(new MediaFileDescriptorIF[]{});
        Arrays.sort(arr, new Comparator<MediaFileDescriptorIF>() {
            public int compare(MediaFileDescriptorIF o1, MediaFileDescriptorIF o2) {
                double e1 = o1.getDistance();
                double e2 = o2.getDistance();
                return Double.compare(e1, e2);
            }
        });
        return Arrays.asList(arr);
    }
    public void testFindSimilarImages(DBManagerIF dbManagerIF, String path) {
        System.out.println("DBManager.test() reading descriptor from disk ");
        System.out.println("DBManager.testFindSimilarImages() Reference Image " + path);
        MediaFileDescriptorIF mediaFileDescriptorIF = mediaFileDescriptorBuilder.buildMediaDescriptor(new File(path));
        MediaFileDescriptorBuilder mediaFileDescriptorBuilder = new MediaFileDescriptorBuilder();
        LSHManagerIF lshManagerIF = new LSHManager(dbManagerIF);
        SimilarImageFinder sif = new SimilarImageFinder(dbManagerIF,mediaFileDescriptorBuilder, lshManagerIF);
        sif.findSimilarImageUsingLSH(mediaFileDescriptorIF,20);
    }
    public static void main(String[] args) {
        MediaFileDescriptorBuilder mediaFileDescriptorBuilder = new MediaFileDescriptorBuilder();
        DBManagerIF tb = new DBManager(null,mediaFileDescriptorBuilder);
        LSHManagerIF lshManagerIF = new LSHManager(tb);
        SimilarImageFinder si = new SimilarImageFinder(tb,mediaFileDescriptorBuilder, lshManagerIF);
        si.testFindSimilarImages(tb, args[0]);
    }

}
