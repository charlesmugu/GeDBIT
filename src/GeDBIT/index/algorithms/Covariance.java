/**
 * GeDBIT.index.algorithms.Covariance
 * 
 * Copyright Information:
 * 
 * Change Log:
 * 2012.04: Added by Kewei Ma, Yuanjun Liu
 */
package GeDBIT.index.algorithms;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.doublealgo.Statistic;

public class Covariance {
    /**
     * Use covariance matrix to do select pivots
     * 
     * @param distance
     *            matrix
     * @param pivots
     *            number
     * @return pivots
     */
    public static int[] runCov(DoubleMatrix2D matrix, int numP) {
	// pivots
	int result[] = new int[numP];
	// whether data point is selected as pivot
	boolean isChosen[] = new boolean[matrix.rows()];
	// covariance matrix
	DoubleMatrix2D coMatrix = Statistic.covariance(matrix);

	// choose the first pivot with largest variance
	for (int i = 1; i < coMatrix.rows(); i++) {
	    if (coMatrix.get(i, i) > coMatrix.get(result[0], result[0])) {
		result[0] = i;
	    }
	}
	// set it true
	isChosen[result[0]] = true;

	// select rest pivots
	if (numP > 1) {
	    for (int i = 1; i < numP; i++) {
		double maxCov[] = new double[matrix.rows() - i];
		int maxCovIndics[] = new int[matrix.rows() - i];
		int count = 0;

		for (int k = 0; k < coMatrix.rows(); k++) {
		    if (isChosen[k] == false) {

			for (int j = 0; j < i; j++)

			{
			    maxCovIndics[count] = k;
			    if (Math.abs(coMatrix.get(k, result[j])) > maxCov[count]) {
				maxCov[count] = Math.abs(coMatrix.get(k,
					result[j]));
			    }
			}

			count++;
		    }

		}

		// pick smallest ones as pivots
		int index = 0;
		for (int j = 1; j < maxCovIndics.length; j++) {

		    if (maxCov[j] < maxCov[index])
			index = j;
		}
		result[i] = maxCovIndics[index];
		isChosen[maxCovIndics[index]] = true;
	    }
	}

	return result;

    }
}
