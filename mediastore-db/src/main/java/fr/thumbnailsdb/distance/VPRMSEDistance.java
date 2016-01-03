package fr.thumbnailsdb.distance;

;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptorIF;
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
        MediaFileDescriptorIF mf1 = (MediaFileDescriptorIF) x;
        MediaFileDescriptorIF mf2 = (MediaFileDescriptorIF) y ;
        return ImageComparator.compareUsingHammingDistance(mf1.getHash(),mf2.getHash());
    }
}
