/**
 * GeDBIT.index.algorithms.LLE 2012.03.30
 * 
 * Copyright Information:
 * 
 * Change Log:
 * 2012.03.30: Added by Kewei Ma
 */
package GeDBIT.index.algorithms;

import hep.aida.bin.BinFunction1D;
import hep.aida.bin.DynamicBin1D;

import java.util.ArrayList;
import java.util.List;

import GeDBIT.dist.Metric;
import GeDBIT.type.IndexObject;
import cern.colt.function.IntComparator;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.doublealgo.Sorting;
import cern.colt.matrix.doublealgo.Statistic;
import cern.colt.matrix.doublealgo.Transform;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.EigenvalueDecomposition;

/**
 * Locally Linear Embedding(LLE), using the Colt library.
 * 
 * @author Kewei Ma
 * @version 2012.03.30
 */

@SuppressWarnings("deprecation")
public class LLE {
    private static Algebra ag = new Algebra();

    /**
     * compute LLE on distance matrix and output the matrix of eigenvector
     * 
     * @param distance
     *            matrix
     * @param number
     *            of pivots
     * @return matrix of eigenvector
     */
    public static DoubleMatrix2D runLLE(DoubleMatrix2D matrix, int numP) {
	final int d = numP; // number of pivots, also the output dimension
	final int k = 18; // number of nearest neighbors
	final double r = 0.001; // regularization parameter

	int rowNumber = matrix.rows();
	int columnNumber = matrix.columns();
	DoubleMatrix2D weightMatrix = new SparseDoubleMatrix2D(rowNumber,
		rowNumber).assign(0);
	DoubleMatrix2D E = new DenseDoubleMatrix2D(k, k).assign(1);

	for (int row = 0; row < rowNumber; row++) {
	    // 1. find k nearest neighbors
	    DoubleMatrix2D matrixTemp = matrix.copy();
	    for (int i = 0; i < rowNumber; i++) {
		for (int j = 0; j < columnNumber; j++) {
		    double temp = matrix.getQuick(i, j)
			    - matrix.getQuick(row, j);
		    matrixTemp.setQuick(i, j, temp);
		}
	    }
	    @SuppressWarnings("rawtypes")
	    List tempList = new Sort().sortWithIndex2D(matrixTemp,
		    new BinFunction1D() {

			public String name() {
			    return "sum of power method";
			}

			public double apply(DynamicBin1D x) {
			    return x.sumOfPowers(2);
			}
		    });
	    int[] nnbIndexsTemp = (int[]) tempList.get(0);
	    DoubleMatrix2D sortedMatrix = (DoubleMatrix2D) tempList.get(1);
	    DoubleMatrix2D nbrMatrix = ag.subMatrix(sortedMatrix, 1, k, 0,
		    columnNumber - 1);

	    // 2. Solve for reconstruction weight matrix
	    DoubleMatrix2D Q = ag.mult(nbrMatrix, (nbrMatrix.viewDice()));
	    for (int Qi = 0; Qi < k; Qi++)
		Q.setQuick(Qi, Qi, Q.getQuick(Qi, Qi) + r * ag.trace(Q));

	    DoubleMatrix2D wTemp = ag.solve(Q, E);
	    DoubleMatrix1D w = wTemp.viewColumn(0);
	    double wSum = w.zSum();
	    for (int i = 0; i < w.size(); i++)
		w.setQuick(i, (w.getQuick(i) / wSum));

	    for (int i = 0; i < k; i++)
		weightMatrix.setQuick(nnbIndexsTemp[i + 1], row, w.getQuick(i));

	}
	// 3. map to lower dimension
	for (int i = 0; i < weightMatrix.rows(); i++)
	    weightMatrix.setQuick(i, i, weightMatrix.getQuick(i, i) - 1);

	DoubleMatrix2D M = ag.mult(weightMatrix, weightMatrix.viewDice());
	for (int i = 0; i < M.rows(); i++)
	    M.setQuick(i, i, M.getQuick(i, i) + 0.1);

	EigenvalueDecomposition ed = new EigenvalueDecomposition(M);
	DoubleMatrix1D m = ed.getRealEigenvalues();
	m.assign(cern.jet.math.Functions.abs);
	int[] indexs = new Sort().sortIndex(m);
	int[] columnIndexes = new int[d];
	int[] rowIndexes = new int[rowNumber];
	DoubleMatrix2D evecs = ed.getV();
	for (int i = 0; i <= d - 1; i++)
	    columnIndexes[i] = indexs[i + 1];
	for (int i = 0; i < rowNumber; i++)
	    rowIndexes[i] = i;
	evecs = evecs.viewSelection(rowIndexes, columnIndexes);
	return evecs;

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
     * @return
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

    /**
     * modified from cern.colt.matrix.doublealgo.Sorting
     * 
     * used to return indices in original matrix after the matrix is sorted
     * 
     * @author MarkNV
     * 
     */
    private static class Sort extends Sorting {
	private static final long serialVersionUID = 6387339595343747222L;

	@SuppressWarnings("rawtypes")
	public List sortWithIndex2D(DoubleMatrix2D matrix,
		hep.aida.bin.BinFunction1D aggregate) {
	    DoubleMatrix2D tmp = matrix.like(1, matrix.rows());
	    hep.aida.bin.BinFunction1D[] func = { aggregate };
	    Statistic.aggregate(matrix.viewDice(), func, tmp);
	    double[] aggr = tmp.viewRow(0).toArray();
	    return sortWithIndex2D(matrix, aggr);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List sortWithIndex2D(DoubleMatrix2D matrix,
		final double[] aggregates) {
	    int rows = matrix.rows();
	    if (aggregates.length != rows)
		throw new IndexOutOfBoundsException(
			"aggregates.length != matrix.rows()");

	    // set up index reordering
	    final int[] indexes = new int[rows];
	    for (int i = rows; --i >= 0;)
		indexes[i] = i;

	    // compares two aggregates at a time
	    cern.colt.function.IntComparator comp = new cern.colt.function.IntComparator() {
		public int compare(int x, int y) {
		    double a = aggregates[x];
		    double b = aggregates[y];
		    if (a != a || b != b)
			return compareNaN(a, b); // swap NaNs to the end
		    return a < b ? -1 : (a == b) ? 0 : 1;
		}
	    };
	    // swaps aggregates and reorders indexes
	    cern.colt.Swapper swapper = new cern.colt.Swapper() {
		public void swap(int x, int y) {
		    int t1;
		    double t2;
		    t1 = indexes[x];
		    indexes[x] = indexes[y];
		    indexes[y] = t1;
		    t2 = aggregates[x];
		    aggregates[x] = aggregates[y];
		    aggregates[y] = t2;
		}
	    };

	    // sort indexes and aggregates
	    runSort(0, rows, comp, swapper);

	    // view the matrix according to the reordered row indexes
	    // take all columns in the original order
	    List result = new ArrayList();
	    result.add(0, indexes);
	    result.add(1, matrix.viewSelection(indexes, null));
	    return result;
	}

	private final int compareNaN(double a, double b) {
	    if (a != a) {
		if (b != b)
		    return 0; // NaN equals NaN
		else
		    return 1; // e.g. NaN > 5
	    }
	    return -1; // e.g. 5 < NaN
	}

	protected void runSort(int[] a, int fromIndex, int toIndex,
		IntComparator c) {
	    cern.colt.Sorting.quickSort(a, fromIndex, toIndex, c);
	}

	protected void runSort(int fromIndex, int toIndex, IntComparator c,
		cern.colt.Swapper swapper) {

	    cern.colt.GenericSorting.quickSort(fromIndex, toIndex, c, swapper);
	}

	public int[] sortIndex(final DoubleMatrix1D vector) {
	    int[] indexes = new int[vector.size()]; // row indexes to reorder
						    // instead of matrix itself
	    for (int i = indexes.length; --i >= 0;)
		indexes[i] = i;

	    IntComparator comp = new IntComparator() {
		public int compare(int a, int b) {
		    double av = vector.getQuick(a);
		    double bv = vector.getQuick(b);
		    if (av != av || bv != bv)
			return compareNaN(av, bv); // swap NaNs to the end
		    return av < bv ? -1 : (av == bv ? 0 : 1);
		}
	    };

	    runSort(indexes, 0, indexes.length, comp);

	    return indexes;
	}

    }

    /**
     * modified from GeDBIT.index.algorithms.PCA.pairWiseDistance get pairwise
     * distance matrix
     * 
     * @param metric
     * @param data
     * @return pairwise distance matrix
     */
    public static DoubleMatrix2D pairWiseDistance(Metric metric,
	    List<? extends IndexObject> data) {
	DoubleMatrix2D distance = new DenseDoubleMatrix2D(data.size(),
		data.size());
	for (int i = 0; i < data.size(); i++) {
	    distance.set(i, i, 0);
	    for (int j = 0; j < i; j++) {
		distance.set(i, j, metric.getDistance(data.get(i), data.get(j)));
		distance.set(j, i, distance.get(i, j));
	    }
	}

	return distance;
    }

    // for local test only, pls modify k to 3
    public static void main(String[] args) {
	DoubleMatrix2D matrix = new DenseDoubleMatrix2D(11, 3);

	double[][] a = { { 30, 2, 15 }, { 29, 31, 6 }, { 7, 17, 9 },
		{ 24, 25, 12 }, { 26, 14, 23 }, { 18, 32, 1 }, { 3, 20, 21 },
		{ 22, 4, 10 }, { 19, 26, 13 }, { 28, 8, 16 }, { 20, 17, 5 } };
	matrix.assign(a);
	System.out.println(LLE.runLLE(matrix, 2));
	int[] b = LLE.selectFromResult(matrix, LLE.runLLE(matrix, 2));
	for (int i = 0; i < b.length; i++) {
	    System.out.println(b[i]);
	}
    }
}
