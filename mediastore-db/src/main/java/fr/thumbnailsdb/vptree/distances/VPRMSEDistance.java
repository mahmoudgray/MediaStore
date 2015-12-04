package fr.thumbnailsdb.vptree.distances;

import fr.thumbnailsdb.MediaFileDescriptor;
import fr.thumbnailsdb.hash.ImageHash;
import fr.thumbnailsdb.utils.ImageComparator;

/**
 * Created with IntelliJ IDEA.
 * User: fhuet
 * Date: 31/10/12
 * Time: 16:41
 * To change this template use File | Settings | File Templates.
 */
public class VPRMSEDistance extends Distance {

    @Override
    public double d(Object x, Object y) {
        MediaFileDescriptor mf1 = (MediaFileDescriptor) x;
        MediaFileDescriptor mf2 = (MediaFileDescriptor) y ;
        return ImageComparator.compareUsingHammingDistance(mf1.getHash(),mf2.getHash());
    }
}
