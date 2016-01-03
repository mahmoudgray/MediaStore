package fr.thumbnailsdb.lsh;

import fr.thumbnailsdb.candidates.Candidate;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptorIF;

import java.util.List;

/**
 * Created by mohannad on 03/01/16.
 */
public interface LSHManagerIF {
    void buildLSH(boolean force);

    int[] getLSHStatus();

    void clear();

    List<Candidate> findCandidatesUsingLSH(MediaFileDescriptorIF mediaFileDescriptorIF);

    int size();
}
