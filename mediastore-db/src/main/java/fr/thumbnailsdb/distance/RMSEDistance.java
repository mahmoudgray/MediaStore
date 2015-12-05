package fr.thumbnailsdb.distance;

import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptor;

/**
 * Created with IntelliJ IDEA.
 * User: fhuet
 * Date: 31/10/12
 * Time: 14:54
 * To change this template use File | Settings | File Templates.
 */
public class RMSEDistance implements Distance {

    public double getDistance(Object object1, Object object2) {
        MediaFileDescriptor mf1 = (MediaFileDescriptor) object1;
        MediaFileDescriptor mf2 = (MediaFileDescriptor) object2 ;
        //TODO fixe for hash
        return 0;
    }
}
