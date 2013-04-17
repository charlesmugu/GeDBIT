/**
 * GeDBIT.index.algorithms.LLE 2012.03.30
 * 
 * Copyright Information:
 * 
 * Change Log:
 * 2012.06: Added by Kewei Ma
 */

package GeDBIT.index.algorithms;

import mdsj.MDSJ;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.doublealgo.Transform;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

@SuppressWarnings("deprecation")
public class MDS {
    private static Algebra ag = new Algebra();

    /**
     * Use MSDJ to run MDS
     * 
     * @param matrix
     * @param numP
     * @return pivots
     */
    public static DoubleMatrix2D runMDS(DoubleMatrix2D matrix, int numP) {
	double[][] output = MDSJ.classicalScaling(matrix.toArray(), numP); // apply
									   // MDS
	DoubleMatrix2D outputMatrix = new DenseDoubleMatrix2D(output);
	return outputMatrix;
    }

    public static int[] selectByCov(DoubleMatrix2D matrix,
	    DoubleMatrix2D evMatrix) {
	int[] result = new int[evMatrix.columns()];
	for (int i = 0; i < evMatrix.columns(); i++) {
	    double[] r = new double[matrix.columns()];
	    double evAvg = evMatrix.viewColumn(i).zSum() / evMatrix.rows();
	    for (int j = 0; j < matrix.columns(); j++) {
		double matAvg = matrix.viewColumn(j).zSum() / matrix.rows();
		double m, n = 0.0;
		m = Transform.mult(
			Transform.minus(evMatrix.viewColumn(i).copy(), evAvg),
			Transform.minus(matrix.viewColumn(j).copy(), matAvg))
			.zSum();
		n = Math.sqrt(Transform.pow(
			Transform.minus(evMatrix.viewColumn(i).copy(), evAvg),
			2).zSum())
			* Math.sqrt(Transform.pow(
				Transform.minus(matrix.viewColumn(j).copy(),
					matAvg), 2).zSum());
		r[j] = m / n;
	    }
	    result[i] = getMaxIndex(r);
	}
	return result;
    }

    /**
     * Select some of the axes as pivots according to the angle between them ,
     * no duplicate axes will be selected.
     * 
     * @param the
     *            original distance matrix
     * @param matrix
     *            of eigenvector from runLLE
     * @return result indices
     */
    public static int[] selectFromResult(DoubleMatrix2D matrix,
	    DoubleMatrix2D evMatrix) {
	int[] result = new int[evMatrix.columns()];
	double[] evModule = module(evMatrix);
	double[] ogModule = module(matrix);
	for (int i = 0; i < evMatrix.columns(); i++) {
	    double[] angle = new double[matrix.columns()];
	    for (int j = 0; j < matrix.columns(); j++) {
		double a = evMatrix.viewColumn(i).zDotProduct(
			matrix.viewColumn(j));
		double b = evModule[i] * ogModule[j];
		angle[j] = a / b;
	    }
	    result[i] = getMaxIndex(angle);
	}
	return result;
    }

    /**
     * function used to compute the module of each column vector
     * 
     * @param matrix
     * @return module of each column vector
     */
    private static double[] module(DoubleMatrix2D m) {
	double[] result = new double[m.columns()];
	for (int i = 0; i < result.length; i++)
	    result[i] = ag.norm2(m.viewColumn(i));
	return result;

    }

    /**
     * function used to find the index of the max element in a double sequence
     * 
     * @param array
     * @return the maxIndex
     */
    private static int getMaxIndex(double[] a) {
	int max = 0;
	for (int i = 1; i < a.length; i++)
	    if (a[max] <= a[i])
		max = i;
	return max;

    }
}
