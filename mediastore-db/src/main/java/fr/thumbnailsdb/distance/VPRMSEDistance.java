package fr.thumbnailsdb.distance;

import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptor;;
import fr.thumbnailsdb.utils.ImageComparator;


/**
 * Created with IntelliJ IDEA.
 * User: fhuet
 * Date: 31/10/12
 * Time: 16:41
 * To change this template use File | Settings | File Templates.
 */
public class VPRMSEDistance implements Distance {

    @Override
    public double getDistance(Object x, Object y) {
        MediaFileDescriptor mf1 = (MediaFileDescriptor) x;
        MediaFileDescriptor mf2 = (MediaFileDescriptor) y ;
        return ImageComparator.compareUsingHammingDistance(mf1.getHash(),mf2.getHash());
    }
}
