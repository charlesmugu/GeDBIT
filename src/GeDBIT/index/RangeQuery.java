/**
 * GeDBIT.index.RangeQuery 2006.05.09
 * 
 * Copyright Information:
 *
 * Change Log:
 * 2006.05.09: Modified from the original GeDBIT package, ProximityPredicate.java, by Rui Mao
 */

package GeDBIT.index;

import GeDBIT.type.IndexObject;

/**
 * A range query. Given (q,r), the results should be all database points x such
 * that d(x,q) <= r
 * 
 * @author Rui Mao, Willard
 * @version 2006.05.09
 */
public class RangeQuery implements Query {
    /**
     * Initializes the range query object
     * 
     * @param center
     *            the {@link Object} that serves as the query object
     * @param radius
     *            the search radius of the range query.
     **/
    public RangeQuery(IndexObject center, double radius) {
	this(center, radius, 0);
    }

    public RangeQuery(IndexObject center, double radius, int listSize) {
	this(-1, center, radius, listSize);
    }

    public RangeQuery(int id, IndexObject center, double radius, int listSize) {
	if (radius < 0.0)
	    throw new IllegalArgumentException("radius < 0: " + radius);

	if (listSize < 0)
	    throw new IllegalArgumentException("max distance list size < 0: "
		    + listSize);
	this.id = id;
	this.radius = radius;
	this.center = center;
	this.listSize = listSize;
    }

    /**
     * Return a reference to the query object
     * 
     * @return a reference to the query object
     */
    public IndexObject getQueryObject() {
	return center;
    }

    public int getId() {
	return id;
    }

    /**
     * Returns the search radius of the proximity query.
     */
    final public double getRadius() {
	return radius;
    }

    public int getMaxDistanceListSize() {
	return listSize;
    }

    private double radius;
    private IndexObject center;
    private int listSize;
    private int id;
}
