package fr.thumbnailsdb.vptree;

import fr.thumbnailsdb.distance.Distance;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


/**
 * @author Paolo Ciccarese
 */
public class VPTreeSeeker {

    private static final boolean DEBUG = false;

    VPTree tree;
    Distance distance;

    public VPTreeSeeker(Distance distance, VPTree tree) {
        this.distance = distance;
        this.tree = tree;
    }

    public Set<Serializable> range(Serializable query, double range) {
        if (DEBUG) System.out.println("--------------- " + query + " " + range);
        return rangeTraversal(query, range, tree.getRoot(), new HashSet<Serializable>());
    }

    private Set<Serializable> rangeTraversal(Serializable query, double range, TNode tNode, Set<Serializable> results) {

        if (DEBUG) System.out.println("> " + tNode);
        
        if (tNode != null) {
            double distance = this.distance.getDistance(query, tNode.get());

            if (distance <= range) {
                if (DEBUG) System.out.println("*** add ***");
                results.add(tNode.get());
            }

            if ((distance + range) < tNode.getMedian()) {
                if (DEBUG) System.out.println("left: " + distance + " + " + range + " < " + tNode.getMedian());
                rangeTraversal(query, range, tNode.getLeft(), results);
            } else if ((distance - range) > tNode.getMedian()) {
                if (DEBUG) System.out.println("right: " + distance + " + " + range + " > " + tNode.getMedian());
                rangeTraversal(query, range, tNode.getRight(), results);
            } else {
                if (DEBUG) System.out.println("left & right: " + distance + " + " + range + " = " + tNode.getMedian());
                rangeTraversal(query, range, tNode.getLeft(), results);
                rangeTraversal(query, range, tNode.getRight(), results);
            }
        }

        if (DEBUG) System.out.println("< " + tNode);
        
        return results;
    }

}
