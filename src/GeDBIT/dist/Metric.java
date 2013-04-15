/**
 * edu.utexas.GeDBIT.dist.Metric 2006.05.24
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.05.04: Copied from jdb package, by Rui Mao
 * 2006.05.24: Changed to serializable interface, by Willard
 */

package GeDBIT.dist;

import java.io.Serializable;

import GeDBIT.type.IndexObject;

/**
 * <code>Metric</code> is a distance oracle. It specifies a binary function to compute distance
 * between two data points in metric space. The distance should have the metric properties, i.e.
 * non-negativity, symmetry and triangle inequality.
 * 
 * @author Jack, Rui Mao, Willard
 * @version 2005.10.31
 */
public interface Metric extends Serializable {

    /**
     * Computes the distance between two objects.
     * 
     * @param one
     *        the first {@link IndexObject} to compute distance on
     * @param two
     *        the second {@link IndexObject} to compute distance on
     * @return the distance between the two objects
     */
    public double getDistance(IndexObject one, IndexObject two);
}
