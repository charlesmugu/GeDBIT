/**
 * edu.utexas.GeDBIT.dist.SequenceFragmentMetric 2006.05.24
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.05.24: Modified from the original jdb package, by Willard
 */
package GeDBIT.dist;

import GeDBIT.type.Fragment;
import GeDBIT.type.IndexObject;

/**
 * Computes distance on two {@link Fragment}s.
 * 
 * @author Jack, Rui Mao, Willard
 * @version 2006.05.31
 */
@SuppressWarnings("serial")
public abstract class SequenceFragmentMetric implements Metric {
    /**
     * 
     */
    protected WeightMatrix weightMatrix;

    /**
     * @param weightMatrix
     */
    public SequenceFragmentMetric(WeightMatrix weightMatrix) {
	this.weightMatrix = weightMatrix;
    }

    /**
     * @return the {@link WeightMatrix} used when computing the distance between
     *         two fragments using this metric.
     */
    public WeightMatrix getWeightMatrix() {
	return weightMatrix;
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.dist.Metric#getDistance(GeDBIT.type.IndexObject,
     * GeDBIT.type.IndexObject)
     */
    public double getDistance(IndexObject one, IndexObject two) {
	return getDistance((Fragment) one, (Fragment) two);
    }

    /**
     * Computes the distance between two {@link Fragment}s
     * 
     * @param one
     *            the first {@link Fragment} to compute distance on
     * @param two
     *            the second {@link Fragment} to compute distance on
     * @return the distance between the two {@link Fragment}s
     */
    public abstract double getDistance(Fragment one, Fragment two);
}
