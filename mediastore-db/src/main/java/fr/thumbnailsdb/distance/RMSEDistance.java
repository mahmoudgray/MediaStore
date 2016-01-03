package fr.thumbnailsdb.distance;

import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptorIF;

/**
 * Created with IntelliJ IDEA.
 * User: fhuet
 * Date: 31/10/12
 * Time: 14:54
 * To change this template use File | Settings | File Templates.
 */
public class RMSEDistance implements Distance {

    public double getDistance(Object object1, Object object2) {
        MediaFileDescriptorIF mf1 = (MediaFileDescriptorIF) object1;
        MediaFileDescriptorIF mf2 = (MediaFileDescriptorIF) object2 ;
        //TODO fixe for hash
        return 0;
    }
}
