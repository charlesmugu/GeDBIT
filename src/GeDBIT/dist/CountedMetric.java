/**
 * edu.utexas.GeDBIT.index.CountedMetric 2006.05.24
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.05.24: Modified from the original jdb package, by Willard
 */

package GeDBIT.dist;

import GeDBIT.type.IndexObject;

/**
 * Wrapper around a base {@link Metric} that counts the invocations of
 * {@link Metric#getDistance(IndexObject,IndexObject)}. Additional methods to
 * get and clear the counter are provided.
 * 
 * @author Jack, Rui Mao
 * @version 2004.03.05
 */
public class CountedMetric implements Metric {
    private static final long serialVersionUID = 5436226220280070858L;

    final private Metric baseMetric;

    private int counter;

    /**
     * Creates a <code>CountedMetric</code> with a given base {@link Metric},
     * setting the internal counter to zero.
     */
    public CountedMetric(Metric baseMetric) {
	if (baseMetric == null)
	    throw new NullPointerException("object baseMetric cannot be null");
	this.baseMetric = baseMetric;
	this.counter = 0;
    }

    /**
     * Returns the value of {@link Metric#getDistance(IndexObject,IndexObject)}
     * for the base {@link Metric} and increments the internal counter.
     */
    final public double getDistance(IndexObject one, IndexObject two) {
	++counter;
	return baseMetric.getDistance(one, two);
    }

    /**
     * Returns the current value of the internal counter.
     */
    final public int getCounter() {
	return counter;
    }

    /**
     * Sets the internal counter to zero.
     */
    final public void clear() {
	counter = 0;
    }

}
