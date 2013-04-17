/**
 * GeDBIT.dist.WHDGlobalSequenceMetric 2006.05.24
 *
 * Change Log:
 * 2006.05.24: Modified from the original GeDBIT package, by Willard
 */

package GeDBIT.dist;

import GeDBIT.type.Fragment;

/**
 * This class computes global alignment with weighted Hamming distance
 * 
 * @author Weijia Xu, Rui Mao
 * @version 2004.03.02
 */

public class WHDGlobalFragmentMetric extends SequenceFragmentMetric {

    public WHDGlobalFragmentMetric(WeightMatrix weightMatrix) {
	super(weightMatrix);
    }

    private static final long serialVersionUID = 7847936320149830952L;

    /**
     * compute the distance
     * 
     * @param one
     *            input {@link Sequence}
     * @param two
     *            input {@link Sequence}
     * @return the distance
     */
    public double getDistance(Fragment one, Fragment two) {
	int firstSize;
	if ((firstSize = one.size()) != two.size()) {
	    System.out.println("stop! Two fragments must have the same length");
	}
	double distance = 0.0;
	for (int i = 0; i < firstSize; i++) {
	    // XXX remove this check
	    // if (weightMatrix.getDistance(one.get(i),
	    // two.get(i))!=weightMatrix.getDistance(two.get(i), one.get(i)))
	    // throw new Error("Error in the weightMatrix!");
	    distance += weightMatrix.getDistance(one.get(i), two.get(i));
	}
	return distance;
    }

    public WeightMatrix getWeightMatrix() {
	return weightMatrix;
    }

}
