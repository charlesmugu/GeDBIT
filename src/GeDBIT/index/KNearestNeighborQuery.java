/**
 * GeDBIT.index.KNearestNeighborQuery 2006.05.09
 * 
 * Copyright Information:
 *
 * Change Log:
 * 2006.05.09: created, by Rui Mao
 */

package GeDBIT.index;

import GeDBIT.type.IndexObject;

/**
 * Given a query object and k, return the k points in the database with the
 * smallest disances to the query.
 * 
 * @author Rui Mao, Willard
 * @version 2006.05.09
 */
public class KNearestNeighborQuery implements Query {
    /**
     * Initializes the range query object
     * 
     * @param center
     *            the {@link Object} that serves as the query object
     * @param radius
     *            the search radius of the range query.
     **/
    public KNearestNeighborQuery(IndexObject center, int k) {
	if (k < 1)
	    throw new IllegalArgumentException("k < 1: " + k);

	this.k = k;
	this.center = center;
    }

    /**
     * Return a reference to the query object
     * 
     * @return a reference to the query object
     */
    public IndexObject getQueryObject() {
	return center;
    }

    /**
     * Returns the k.
     */
    final public int getK() {
	return k;
    }

    private int k;
    private IndexObject center;

}
