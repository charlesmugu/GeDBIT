/**
 * GeDBIT.dist.WHDGlobalSequenceMetric 2006.05.24
 *
 * Change Log:
 * 2006.05.24: Modified from the original GeDBIT package, by Willard
 */

package GeDBIT.dist;

import GeDBIT.type.Fragment;

/**
 * Computes global alignment on {@link Fragment}s with Weighted Hamming Distance
 * 
 * @author Weijia Xu, Rui Mao, Willard
 * @version 2004.03.02
 */

public class WHDGlobalSequenceFragmentMetric extends SequenceFragmentMetric {
    /**
     * 
     */
    private static final long serialVersionUID = 7847936320149830952L;

    /**
     * @param weightMatrix
     */
    public WHDGlobalSequenceFragmentMetric(WeightMatrix weightMatrix) {
	super(weightMatrix);
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.dist.SequenceFragmentMetric#getDistance(GeDBIT.type.Fragment,
     * GeDBIT.type.Fragment)
     */
    public double getDistance(Fragment one, Fragment two) {
	int firstSize;
	if ((firstSize = one.size()) != two.size()) {
	    System.out.println("stop! Two fragments must have the same length");
	}
	double distance = 0.0;
	for (int i = 0; i < firstSize; i++) {
	    distance += weightMatrix.getDistance(one.get(i), two.get(i));
	}
	return distance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.dist.SequenceFragmentMetric#getWeightMatrix()
     */
    public WeightMatrix getWeightMatrix() {
	return weightMatrix;
    }
}
