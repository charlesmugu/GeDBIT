/**
 * GeDBIT.index.KNNQuery 2006.07.09
 * 
 * Copyright Information:
 *
 * Change Log:
 * 2006.07.09: created by Weijia Xu 
 */

package GeDBIT.index;

import GeDBIT.type.IndexObject;

/**
 * This class contains implmentation for K nearest neighbor search (KNN) and
 * approximate K nearest neighbor search (AKNN) queries. A KNN query is in the
 * form of (q, k). Given (q, k), the search returns the k closest objects to
 * query object q. An aproximate KNN query is in the form of (q, k, r, sp),
 * where r is a limiting radius and sp is the approximation policy. The search
 * proceed accroding to the value of r and sp. 1) When both r and sp is omit,
 * the search proceeds as KNN search 2) When r is given and sp is omit or sp is
 * the value of KNNQuery.RADIUSLIMIRED, the search returns upto k objects that
 * are the closest ones to query q within radius r. 3) When a non-negative value
 * is given for sp, an approximate set of k objects is returned. Only those
 * objects within spsmallest distance to query q are gurantee to be returned in
 * the result set. The results of search with larger sp value have better
 * accuracy than those of search with lower sp value and take longer to compute.
 * When a radius value is also given, the results are further limited to those
 * within distance r to query q.
 * 
 * @author Weijia Xu
 * @version 2006.07.09
 */
public class KNNQuery implements Query {
    final static int KNNSEARCH = -1;
    final static int RADIUSLIMITED = -2;
    final static int RANGESEARCH = -3;

    int max_result;
    int search_policy;

    /**
     * Initializes the strict KNN query object
     * 
     * @param center
     *            the {@link Object} that serves as the query object
     * @param k
     *            the number of nearest neighbors to return
     **/
    public KNNQuery(IndexObject center, int k) {
	this(center, k, Double.MAX_VALUE, KNNSEARCH, 0);
    }

    public KNNQuery(IndexObject center, int k, double radius) {
	this(center, k, radius, RADIUSLIMITED, 0);
    }

    public KNNQuery(IndexObject center, int k, double radius, int sp) {
	this(center, k, radius, sp, 0);
    }

    public KNNQuery(IndexObject center, int k, double radius, int sp,
	    int listSize) {
	if (radius < 0.0)
	    throw new IllegalArgumentException("radius < 0: " + radius);

	if (listSize < 0)
	    throw new IllegalArgumentException("max distance list size < 0: "
		    + listSize);

	this.radius = radius;
	this.center = center;
	this.listSize = listSize;
	this.max_result = k;
	this.search_policy = sp;
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
     * Returns the search radius of the proximity query.
     */
    final public double getRadius() {
	return radius;
    }

    public int getMaxDistanceListSize() {
	return listSize;
    }

    public int getK() {
	return max_result;
    }

    public int getSearchPolicy() {
	return search_policy;
    }

    private double radius;
    private IndexObject center;
    private int listSize;

    public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("KNNQUERY: ");
	buffer.append(center.toString());
	buffer.append(" Search_Policy:" + search_policy);
	buffer.append(" Max_Results:" + max_result);
	buffer.append(" Max_Radius:" + radius);
	return buffer.toString();
    }
}
