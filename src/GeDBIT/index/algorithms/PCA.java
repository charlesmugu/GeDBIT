/**
 * GeDBIT.util.PCA 2006.06.16
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.01.25: MOdified from jdb 1.0, by Rui Mao
 */

package GeDBIT.index.algorithms;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.ObjectMatrix2D;
import cern.colt.matrix.impl.DenseObjectMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.doublealgo.Statistic;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.Functions;

import GeDBIT.dist.Metric;
import GeDBIT.dist.WHDGlobalSequenceFragmentMetric;
import GeDBIT.dist.WeightMatrix;
import GeDBIT.type.Alphabet;
import GeDBIT.type.DNATable;
import GeDBIT.type.DoubleVector;
import GeDBIT.type.DoubleVectorTable;
import GeDBIT.type.ImageTable;
import GeDBIT.type.Peptide;
import GeDBIT.type.DNA;
import GeDBIT.type.IndexObject;
import GeDBIT.type.SpectraWithPrecursorMassTable;
import GeDBIT.type.Table;
import GeDBIT.type.Pair;
import GeDBIT.util.LargeDenseDoubleMatrix2D;

import java.util.List;
import java.util.Random;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * Do the Principal Component Analysis (PCA), using the Colt library. PCA no
 * longer supports ShiftSize
 * 
 * @author Rui Mao
 * @version 2006.06.23
 * 
 */
public class PCA {
    static public EigenvalueDecomposition runPCA(DoubleMatrix2D matrix) {
	return runPCA(matrix, true);
    }

    static public EigenvalueDecomposition runPCA(DoubleMatrix2D matrix,
	    boolean print) {
	if (print)
	    System.out.println("Input matrix: " + matrix + "\n");

	// 1. compute the mean of each column
	final int col = matrix.columns();
	final int row = matrix.rows();
	double[] columnMean = new double[col];
	for (int i = 0; i < col; i++)
	    columnMean[i] = matrix.viewColumn(i).zSum() / row;
	if (print)
	    System.out.println("column means: "
		    + new DenseDoubleMatrix1D(columnMean) + "\n");

	// 2. center the matrix
	for (int i = 0; i < col; i++)
	    for (int j = 0; j < row; j++)
		matrix.set(j, i, matrix.get(j, i) - columnMean[i]);

	if (print)
	    System.out.println("centered matrix: " + matrix + "\n");

	// 3. compute the covariance matrix
	DoubleMatrix2D cov = Statistic.covariance(matrix);

	if (print)
	    System.out.println("Covariance matrix: " + cov + "\n");

	// 4. Compute eigenvalues and eigenvectors of the covariance matrix.
	EigenvalueDecomposition evd = new EigenvalueDecomposition(cov);

	if (print) {
	    // java.text.DecimalFormat format = new
	    // java.text.DecimalFormat("0.000000;0.000000");
	    System.out
		    .println("PCA (Eigenvalue decomposition of covariance matrix) of input matrix:\nEigenvalues:"
			    + evd.getRealEigenvalues().viewFlip()
			    + "\nEigenvectors: " + evd.getV().viewColumnFlip());
	    System.out.println();
	    DoubleMatrix1D lamda = evd.getRealEigenvalues();

	    System.out
		    .println("Variance of each dimension(column) by each PC(row):");
	    DoubleMatrix2D var = new DenseDoubleMatrix2D(col, col);
	    DoubleMatrix2D vector = new DenseDoubleMatrix2D(col, col);
	    vector.assign(evd.getV()).assign(Functions.square);
	    for (int i = 0; i < col; i++)
		for (int j = 0; j < col; j++) {
		    var.set(i, j, vector.get(j, i) * lamda.get(i));
		}
	    System.out.println(var + "\n");

	    Object[] title = new Object[1];
	    title[0] = "PC";
	    System.out.println("row summary of var: "
		    + rowSummaryMatrix(var, false, title).viewRowFlip() + "\n");
	    title[0] = "point";
	    System.out.println("column summary of var: "
		    + rowSummaryMatrix(var.viewDice(), true, title)
			    .viewRowFlip().viewDice() + "\n");

	    System.out
		    .println("\nprojection of each point(row) on each PC(column): ");
	    DoubleMatrix2D projection = (new Algebra())
		    .mult(matrix, evd.getV()).assign(Functions.abs);
	    System.out.println(projection.viewColumnFlip());
	    System.out.println();

	    title[0] = "point";
	    System.out.println("row summary of projection: "
		    + rowSummaryMatrix(projection, true, title) + "\n");
	    title[0] = "PC";
	    System.out.println("column summary of projection: "
		    + rowSummaryMatrix(projection.viewDice(), false, title)
			    .viewDice());

	    System.out.print("\npercentage of the largest PCs: ");
	    double sum = 0;
	    for (int i = lamda.size() - 1; i >= 0; i--) {
		sum += lamda.get(i);
		System.out.print(sum / lamda.zSum() + ", ");// "(" +
							    // (lamda.size() - i
							    // - 1) + "), ");
	    }
	    System.out.println();
	}

	return evd;
    }

    static ObjectMatrix2D rowSummaryMatrix(DoubleMatrix2D matrix,
	    boolean sortSum, Object[] title) {
	final int row = matrix.rows();
	final int col = matrix.columns();
	final int newCol = col + 5;

	Object[][] data = new Object[row][newCol];
	double sum, min, max, std;

	for (int i = 0; i < row; i++) {
	    for (int j = 0; j < col; j++)
		data[i][j] = new Pair(new Double(matrix.get(i, j)),
			new Integer(j));

	    Arrays.sort(data[i], 0, col, Pair.FirstComparator);
	    sum = matrix.viewRow(i).zSum();
	    min = ((Double) ((Pair) data[i][0]).first()).doubleValue();
	    max = ((Double) ((Pair) data[i][col - 1]).first()).doubleValue();
	    std = Math.sqrt(matrix.viewRow(i).aggregate(Functions.plus,
		    Functions.square)
		    / col - Math.pow(sum / col, 2));

	    if (title == null)
		data[i][newCol - 1] = new Integer(i);
	    else if ((title.length == 1) && (title[0] instanceof String))
		data[i][newCol - 1] = (String) title[0] + i;
	    else
		data[i][newCol - 1] = title[i];

	    data[i][newCol - 2] = new Pair(new Double(sum), new Integer(i));
	    data[i][newCol - 3] = new Pair(new Double(sum / col), new Double(
		    std));
	    data[i][newCol - 4] = new Pair(new Double(min), new Double(max));
	    data[i][newCol - 5] = new String("*");
	}

	if (sortSum)
	    return new DenseObjectMatrix2D(data).viewSorted(newCol - 2)
		    .viewColumnFlip();
	else
	    return new DenseObjectMatrix2D(data).viewColumnFlip();

    }

    @SuppressWarnings("rawtypes")
    static DoubleMatrix2D vectorMain(String[] args) {
	// Metric metric = GeDBIT.dist.LMetric.EuclideanDistanceMetric;
	final int size = Integer.parseInt(args[3]);
	final int dim = Integer.parseInt(args[2]);

	// load data from file
	Table table = null;
	try {
	    if (size == -1)
		table = new DoubleVectorTable(args[1], "", Integer.MAX_VALUE,
			dim);
	    else
		table = new DoubleVectorTable(args[1], "", size, dim);
	} catch (Exception e) {
	    e.printStackTrace();
	}

	List dataList = table.getData();
	// System.out.println( dataList.get(0).getClass());
	// System.out.println( ( (Pair) dataList.get(0) ).first().getClass());
	double[][] data = new double[dataList.size()][((DoubleVector) dataList
		.get(0)).size()];
	for (int i = 0; i < data.length; i++)
	    data[i] = ((DoubleVector) dataList.get(i)).getData();

	DoubleMatrix2D result = LargeDenseDoubleMatrix2D.createDoubleMatrix2D(
		data.length, data[0].length);
	return result.assign(data);
    }

    static DoubleMatrix2D imageMain(String[] args) {
	Table table = null;
	try {
	    if (args[0].equalsIgnoreCase("image"))
		table = new ImageTable(args[1], "", Integer.parseInt(args[2]));
	    else if (args[0].equalsIgnoreCase("msms"))
		table = new SpectraWithPrecursorMassTable(args[1], "",
			Integer.parseInt(args[2]));
	    else
		System.out.println("Unrecognized data type: " + args[0]);
	} catch (NumberFormatException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return pairWiseDistance(table.getMetric(), table.getData());
    }

    static DoubleMatrix2D vectorPMain(String[] args) {
	// System.out.println(vectorMain(args));
	return LargeDenseDoubleMatrix2D.distance(vectorMain(args), true,
		Statistic.EUCLID);
    }

    // for protein in fasta format. argument 0: data type
    // arg 1: file name, arg 2:size, arg 3: fragment length, arg 4: shift size
    @SuppressWarnings("rawtypes")
    static DenseDoubleMatrix2D proteinMain(String[] args) {

	// TODO
	Metric metric = new WHDGlobalSequenceFragmentMetric(
		Peptide.mPAM250aExtendedWeightMatrix);

	// load data from file
	List dataList = null;
	try {
	    // TODO
	    // dataList = ( new FastaSequenceAllDataLoader( Peptide.ALPHABET )
	    // ). loadFragment( new BufferedReader( new FileReader(args[1]) ),
	    // Integer.parseInt(args[2]), Integer.parseInt(args[3]),
	    // Integer.parseInt(args[4]) );
	} catch (Exception e) {
	    e.printStackTrace();
	}

	@SuppressWarnings("null")
	double[][] data = new double[dataList.size()][dataList.size()];
	for (int i = 0; i < data.length; i++) {
	    data[i][i] = 0;
	    for (int j = 0; j < i; j++) {
		// TODO
		data[i][j] = metric.getDistance((IndexObject) dataList.get(i),
			(IndexObject) dataList.get(j));
		data[j][i] = data[i][j];
	    }
	}

	return new DenseDoubleMatrix2D(data);
    }

    // for protein in fasta format. argument 0: data type
    // arg 1: file name, arg 2:size, arg 3: fragment length, arg 4: shift size
    static DenseDoubleMatrix2D DNAMain(String[] args) {

	@SuppressWarnings("rawtypes")
	List dataList = null;
	try {
	    // TODO
	    // dataList = ( new FastaSequenceAllDataLoader( DNA.ALPHABET) ).
	    // loadFragment( new BufferedReader( new FileReader(args[1]) ),
	    // Integer.parseInt(args[2]), Integer.parseInt(args[3]),
	    // Integer.parseInt(args[4]) );
	} catch (Exception e) {
	    e.printStackTrace();
	}

	Metric metric = new WHDGlobalSequenceFragmentMetric(
		DNA.EditDistanceWeightMatrix);

	@SuppressWarnings("null")
	double[][] data = new double[dataList.size()][dataList.size()];
	for (int i = 0; i < data.length; i++) {
	    data[i][i] = 0;
	    for (int j = 0; j < i; j++) {
		// TODO
		data[i][j] = metric.getDistance((IndexObject) dataList.get(i),
			(IndexObject) dataList.get(j));
		data[j][i] = data[i][j];
	    }
	}

	return new DenseDoubleMatrix2D(data);
    }

    static DenseDoubleMatrix2D aminoacids() {

	WeightMatrix matrix = Peptide.mPAM250aExtendedWeightMatrix;
	Alphabet alphabet = Peptide.ALPHABET;

	double[][] data = new double[20][20];
	for (int i = 0; i < 20; i++) {
	    data[i][i] = 0;
	    for (int j = 0; j < i; j++) {
		data[i][j] = matrix.getDistance(alphabet.get(i),
			alphabet.get(j));
		data[j][i] = data[i][j];
	    }
	}

	return new DenseDoubleMatrix2D(data);
    }

    static DoubleMatrix2D aminoacidsP() {
	return Statistic.distance(aminoacids(), Statistic.EUCLID);
    }

    // for protein in fasta format. argument 0: data type: proteind
    // arg 1: file name, arg 2:to-size, arg 3: fragment length; arg 4: shift
    // size, arg 5: from-size, default 0, arg 6: step-size, default 100
    static void proteinDimension(String[] args) {
	final String head = new String(args[0] + " " + args[1] + " ");
	final int toSize = Integer.parseInt(args[2]);
	// final String fragment = new String(" " + args[3] + " " + args[4]);
	final int fragLength = Integer.parseInt(args[3]);
	final int shiftSize = Integer.parseInt(args[4]);
	final int fromSize = (args.length > 5) ? Integer.parseInt(args[5])
		: 100;
	final int stepSize = (args.length > 6) ? Integer.parseInt(args[6])
		: 100;

	final int dimLimit = 100;
	DoubleMatrix1D lamda = null;
	double sum = 0;
	StringBuffer parameter = null;

	for (int i = fromSize; i <= toSize; i += stepSize) {
	    for (int j = 1; j <= fragLength; j++) {
		parameter = new StringBuffer(head);
		parameter.append(i).append(" " + j + " " + shiftSize);
		lamda = runPCA(proteinMain(parameter.toString().split(" ")),
			false).getRealEigenvalues();

		System.out.print(i + "\t" + j);
		sum = 0;
		for (int k = lamda.size() - 1; k >= Math.max(0, lamda.size()
			- dimLimit); k--) {
		    sum += lamda.get(k);
		    System.out.print("\t" + sum / lamda.zSum());
		}
		System.out.println();
	    }

	}
    }

    // argument 0: data type: dnad
    // arg 1: file name, arg 2:to-size, arg 3: fragment length; arg 4: shift
    // size,
    // arg 5: from-size, default 100, arg 6: step-size, default 100,
    // arg 7: start dim, default 3, arg 8: dim step size, default 3
    // SHIFT SIZE is no longer supported!!
    static void DNADimension(String[] args) {
	final int toSize = Integer.parseInt(args[2]);
	final int fragLength = Integer.parseInt(args[3]);
	final int fromSize = (args.length > 5) ? Integer.parseInt(args[5])
		: 100;
	final int stepSize = (args.length > 6) ? Integer.parseInt(args[6])
		: 100;
	final int fromDim = (args.length > 7) ? Integer.parseInt(args[7]) : 3;
	final int stepDim = (args.length > 8) ? Integer.parseInt(args[8]) : 3;

	final int dimLimit = 100;
	DoubleMatrix1D lamda = null;
	double sum = 0;
	Table table = null;

	for (int i = fromSize; i <= toSize; i += stepSize) {
	    for (int j = fromDim; j <= fragLength; j += stepDim) {
		try {
		    table = new DNATable(args[1], "", i, j);
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		lamda = runPCA(
			pairWiseDistance(table.getMetric(), table.getData()),
			false).getRealEigenvalues();

		System.out.print(i + "\t" + j);
		sum = 0;
		for (int k = lamda.size() - 1; k >= Math.max(0, lamda.size()
			- dimLimit); k--) {
		    sum += lamda.get(k);
		    System.out.print("\t" + sum / lamda.zSum());
		}
		System.out.println();
	    }

	}
    }

    // argument 0: data type: dnad
    // arg 1: file name, arg 2:to-size,
    // arg 3: from-size, default 100, arg 4: step-size, default 100,
    static void singleDimDimension(String[] args) {
	final int toSize = Integer.parseInt(args[2]);
	final int fromSize = (args.length > 3) ? Integer.parseInt(args[3])
		: 100;
	final int stepSize = (args.length > 4) ? Integer.parseInt(args[4])
		: 100;

	final int dimLimit = 100;
	DoubleMatrix1D lamda = null;
	double sum = 0;
	Table table = null;

	for (int i = fromSize; i <= toSize; i += stepSize) {
	    try {
		if (args[0].equalsIgnoreCase("imaged"))
		    table = new ImageTable(args[1], "", i);
		else if (args[0].equalsIgnoreCase("msmsd"))
		    table = new SpectraWithPrecursorMassTable(args[1], "", i);
		else
		    System.out.println("Unrecognized data type: " + args[0]);
	    } catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	    lamda = runPCA(
		    pairWiseDistance(table.getMetric(), table.getData()), false)
		    .getRealEigenvalues();

	    System.out.print(i);
	    sum = 0;
	    for (int k = lamda.size() - 1; k >= Math.max(0, lamda.size()
		    - dimLimit); k--) {
		sum += lamda.get(k);
		System.out.print("\t" + sum / lamda.zSum());
	    }
	    System.out.println();

	}
    }

    static void vectorPDimension(String[] args) {
	final int dim = Integer.parseInt(args[2]);
	final int toSize = Integer.parseInt(args[3]);
	final int dimLimit = dim + 20;
	final int fromSize = (args.length > 4) ? Integer.parseInt(args[4]) : 0;
	final int stepSize = (args.length > 5) ? Integer.parseInt(args[5])
		: 100;
	final String head = new String(args[0] + " " + args[1] + " ");
	DoubleMatrix1D lamda = null;
	double sum = 0;
	StringBuffer parameter = null;

	for (int j = fromSize; j <= toSize; j += stepSize) {
	    for (int i = 1; i <= dim; i++) {
		parameter = new StringBuffer(head);
		parameter.append(i).append(" ").append(j);
		// stem.out.println(parameter);
		lamda = runPCA(vectorPMain(parameter.toString().split(" ")),
			false).getRealEigenvalues();

		System.out.print(i + "\t" + j);
		sum = 0;
		for (int k = 0; k < dim - i; k++)
		    System.out.print("\t");
		for (int k = lamda.size() - 1; k >= Math.max(0, lamda.size()
			- dimLimit); k--) {
		    sum += lamda.get(k);
		    System.out.print("\t" + sum / lamda.zSum());
		}
		System.out.println();

	    }
	    // stem.out.println();
	}
    }

    public static DoubleMatrix2D pairWiseDistance(Metric metric,
	    List<? extends IndexObject> data) {
	DoubleMatrix2D distance = LargeDenseDoubleMatrix2D
		.createDoubleMatrix2D(data.size(), data.size());
	for (int i = 0; i < data.size(); i++) {
	    distance.set(i, i, 0);
	    for (int j = 0; j < i; j++) {
		distance.set(i, j, metric.getDistance(data.get(i), data.get(j)));
		distance.set(j, i, distance.get(i, j));
	    }
	}

	return distance;
    }

    /**
     * pivot selection based on the result of PCA. Select some of the axes as
     * piovts, no duplicate axes will be selected. It is the user's
     * reponsibility to guarantee that each axis is different from others.
     * Method: relate pc to axes. Start from the 1st axis of each pc, then 2st,
     * until enough number pivots are selected.
     * 
     * @param pcaResult
     *            PCA result: A {@link DoubleMatrix2D} of size [pcNum x (dim
     *            +1)], the first column is the variance of each PC in
     *            descending order. The remain of each row is a PC
     * @param numP
     *            number of pivots to select, should not be greater than the
     *            number of columns (dim) of the input PCA result matrix.
     * @return an int array of column indecies of the pivots in the input data
     *         array.
     */
    public static int[] pivotSelectionByPCAResultAngle(
	    DoubleMatrix2D pcaResult, int numP) {
	final int pcNum = pcaResult.rows();
	final int dim = pcaResult.columns() - 1;

	DoubleMatrix2D PC = new DenseDoubleMatrix2D(pcNum + 1, dim);
	PC.viewPart(1, 0, pcNum, dim).assign(
		pcaResult.viewPart(0, 1, pcNum, dim));
	PC.assign(Functions.abs);
	for (int i = 0; i < dim; i++)
	    PC.set(0, i, i);

	// System.out.println(PC);
	HashSet<Integer> map = new HashSet<Integer>();

	// for jth pc, find the ith axis, on which the pc has the largest
	// projection
	int[] result = new int[numP];
	int counter = 0;
	DoubleMatrix2D pcView = null;
	for (int i = 0; (i < dim) && (map.size() < numP); i++) {
	    for (int j = 1; (j <= pcNum) && (map.size() < numP); j++) {
		pcView = PC.viewDice().viewSorted(j);
		// System.out.println(pcView);
		int point = (int) pcView.get(dim - 1, 0);
		if (map.add(point))
		    result[counter++] = point;
		pcView.set(dim - 1, j, 0);
	    }
	}

	if (counter < numP) {
	    int[] temp = new int[counter];
	    System.arraycopy(result, 0, temp, 0, counter);
	    return temp;
	} else
	    return result;

    }

    /**
     * Pivot selection based on the result of PCA. select the points that are
     * close (large projection) to the PCs. start from the 1st closest points to
     * the given number of largest PCs, then the 2nd, then the 3rd,until
     * required number of points are selected.
     * 
     * @param data
     *            the data set, each row is a point. Since the PCs are from the
     *            origin, the data should already be centerized.
     * @param pcaResult
     *            PCA result: A {@link DoubleMatrix2D} of size [pcNum x (dim
     *            +1)], the first column is the variance (eigenvalue) of each PC
     *            in descending order. The remain of each row is a PC
     * @param numPC
     *            the number of largest PCs to consider
     * @param numP
     *            number of pivots to select, should not be greater than the
     *            number of columns (dim) of the input PCA result matrix.
     * @return an int array of row indecies of the pivots in the input data
     *         matrix. At most numP pivots will be returned, probably less.
     */
    public static int[] pivotSelectionByPCAResultProjection(
	    DoubleMatrix2D data, DoubleMatrix2D pcaResult, int numPC, int numP) {
	numPC = Math.min(numPC, pcaResult.rows());
	if (pcaResult.columns() != data.columns() + 1)
	    throw new IllegalArgumentException(
		    "Dimension of data and PC are inconsistent!");

	// scan the data, find the closest points to the largest PCs
	final int EachPCScale = 5; // decide the number closest points to be
				   // selected from each PC
	int eachPC = numP / numPC * EachPCScale;
	if (eachPC == 0)
	    eachPC = 3;

	@SuppressWarnings("unchecked")
	TreeMap<Double, Integer>[] closest = new TreeMap[numPC];
	for (int i = 0; i < numPC; i++)
	    closest[i] = new TreeMap<Double, Integer>();
	double p = 0;
	for (int point = 0; point < data.rows(); point++) {
	    for (int pc = 0; pc < numPC; pc++) {
		p = 0;
		for (int dim = 0; dim < data.columns(); dim++)
		    p += data.getQuick(point, dim)
			    * pcaResult.getQuick(pc, dim + 1);
		p = Math.abs(p);
		if (closest[pc].size() < eachPC)
		    closest[pc].put(p, point);
		else if (closest[pc].firstKey() < p) {
		    closest[pc].remove(closest[pc].firstKey());
		    closest[pc].put(p, point);
		}

	    }

	}

	// the closest points to each PC are found, now select distincting
	// points from them
	// ordered by rank of closeness, then by PC
	int[] result = new int[numP];
	int counter = 0;
	HashSet<Integer> set = new HashSet<Integer>();
	for (int rank = 0; rank < eachPC; rank++) {
	    if (set.size() >= numP)
		break;

	    for (int pc = 0; pc < numPC; pc++) {
		int point = closest[pc].remove(closest[pc].lastKey());
		if (set.add(point))
		    result[counter++] = point;
		if (set.size() >= numP)
		    break;
	    }
	}

	if (counter < numP) {
	    int[] temp = new int[counter];
	    System.arraycopy(result, 0, temp, 0, counter);
	    return temp;
	} else
	    return result;
    }

    /**
     * pivot selection by PCA
     * 
     * @param metric
     * @param data
     * @param numPC
     *            how many Principal Components to check
     * @param eachPC
     *            from each PC, how many pivots will be selected by each method.
     *            two methods now.
     * @param print
     *            whether to print debug information
     * @return an int array of offsets of the pivots in the input data array.
     */
    public static int[] pivotSelection(Metric metric,
	    List<? extends IndexObject> data, int numPC, int eachPC,
	    boolean print) {
	return pivotSelection(metric, data, numPC, eachPC, print, null);
    }

    /**
     * pivot selection by PCA
     * 
     * @param metric
     * @param data
     * @param numPC
     *            how many Principal Components to check
     * @param eachPC
     *            from each PC, how many pivots will be selected by each method.
     *            two methods now.
     * @param print
     *            whether to print debug information
     * @param eigenValue
     *            an array to store the sums of eigenvalues
     * @return an int array of offsets of the pivots in the input data array.
     */
    public static int[] pivotSelection(Metric metric,
	    List<? extends IndexObject> data, int numPC, int eachPC,
	    boolean print, double[] eigenValue) {
	DoubleMatrix2D matrix = pairWiseDistance(metric, data);

	if (print)
	    System.out.println("Input matrix: " + matrix + "\n");

	// 1. compute the mean of each column
	final int col = matrix.columns();
	final int row = matrix.rows();
	double[] columnMean = new double[col];
	for (int i = 0; i < col; i++)
	    columnMean[i] = matrix.viewColumn(i).zSum() / row;

	if (print)
	    System.out.println("column means: "
		    + new DenseDoubleMatrix1D(columnMean) + "\n");

	// 2. center the matrix
	for (int i = 0; i < col; i++)
	    for (int j = 0; j < row; j++)
		matrix.set(j, i, matrix.get(j, i) - columnMean[i]);

	if (print)
	    System.out.println("centered matrix: " + matrix + "\n");

	// 3. compute the covariance matrix
	DoubleMatrix2D cov = Statistic.covariance(matrix);

	if (print)
	    System.out.println("Covariance matrix: " + cov + "\n");

	// 4. Compute eigenvalues and eigenvectors of the covariance matrix.
	EigenvalueDecomposition evd = new EigenvalueDecomposition(cov);

	DoubleMatrix1D lamda = evd.getRealEigenvalues();

	// return the eigenvalues
	if (eigenValue != null) {
	    eigenValue[0] = lamda.get(lamda.size() - 1) / lamda.zSum();
	    for (int i = 1; i < lamda.size(); i++)
		eigenValue[i] = eigenValue[i - 1]
			+ lamda.get(lamda.size() - i - 1) / lamda.zSum();
	}

	DoubleMatrix2D vector = new DenseDoubleMatrix2D(col, col);
	vector.assign(evd.getV()).assign(Functions.square);

	// variance of each axis(column) from each pc(row, small to large)
	DoubleMatrix2D var = new DenseDoubleMatrix2D(col, col);
	for (int i = 0; i < col; i++)
	    for (int j = 0; j < col; j++) {
		var.set(i, j, vector.get(j, i) * lamda.get(i));
	    }

	numPC = (numPC > col) ? col : numPC;
	eachPC = (eachPC > col) ? col : eachPC;

	Object[] title = new Object[1];
	title[0] = "PC";
	ObjectMatrix2D varRowSummary = rowSummaryMatrix(var, false, title);
	int[] result = new int[2 * numPC * eachPC];

	// get the axes that have large variance from the large PCs
	for (int i = 0; i < numPC; i++)
	    for (int j = 0; j < eachPC; j++) {
		result[i * eachPC + j] = ((Integer) ((Pair) varRowSummary.get(
			col - 1 - i, varRowSummary.columns() - col + j))
			.second()).intValue();
		// System.out.println(result[i*eachPC + j]);
	    }

	// projection of each data point on each PC(column, small to large)
	DoubleMatrix2D projection = (new Algebra()).mult(matrix, evd.getV())
		.assign(Functions.abs);
	ObjectMatrix2D projectionColumnSummary = rowSummaryMatrix(
		projection.viewDice(), false, title);

	// get the points that have large projection on the large PCs
	for (int i = 0; i < numPC; i++)
	    for (int j = 0; j < eachPC; j++) {
		result[numPC * eachPC + i * eachPC + j] = ((Integer) ((Pair) projectionColumnSummary
			.get(col - 1 - i, projectionColumnSummary.columns()
				- col + j)).second()).intValue();
		// System.out.println(result[numPC*eachPC + i*eachPC + j]);
	    }

	if (print) {
	    // java.text.DecimalFormat format = new
	    // java.text.DecimalFormat("0.000000;0.000000");
	    System.out
		    .println("PCA (Eigenvalue decomposition of covariance matrix) of input matrix:\n"
			    + evd);
	    System.out.println();

	    System.out
		    .println("Variance of each dimension(column) by each PC(row):");
	    System.out.println(var + "\n");

	    System.out.println("row summary of var: "
		    + varRowSummary.viewRowFlip() + "\n");
	    title[0] = "point";
	    System.out.println("column summary of var: "
		    + rowSummaryMatrix(var.viewDice(), true, title)
			    .viewRowFlip().viewDice() + "\n");

	    System.out
		    .println("\nprojection of each point(row) on each PC(column): ");
	    System.out.println(projection.viewColumnFlip());
	    System.out.println();

	    title[0] = "point";
	    System.out.println("row summary of projection: "
		    + rowSummaryMatrix(projection, true, title).viewRowFlip()
		    + "\n");
	    System.out.println("column summary of projection: "
		    + projectionColumnSummary.viewRowFlip().viewDice());

	    System.out.print("\npercentage of the largest PCs: ");
	    double sum = 0;
	    for (int i = lamda.size() - 1; i >= 0; i--) {
		sum += lamda.get(i);
		System.out.print(sum / lamda.zSum() + "("
			+ (lamda.size() - i - 1) + "), ");
	    }
	    System.out.println();
	}

	return result;
    }

    // given the eigenvalues(sum-ups), estimate the intrinsic dimension
    // each line is a series of dimensions, have several numbers as headers, all
    // headers should be no less than 1.
    // estimate for every line, and give out put for every line
    @SuppressWarnings({ "unchecked", "rawtypes" })
    static void dimMain(String[] args) throws Exception {
	final double Dim1CutOff = 0.95;

	@SuppressWarnings("resource")
	BufferedReader reader = new BufferedReader(new FileReader(args[1]));
	String line = reader.readLine();
	String[] segment = null;
	int first = 0;
	int length = 0;
	double[] data = null;
	DoubleMatrix2D matrix = null;
	Object[] title = new String[] { "sum(e)", "e", "d(e)", "d(e)/e",
		"dd(e)", "e/e", "entropy", "sum(entropy)", "d(entropy)",
		"d(entropy)/entropy", "dd(entropy)", "entropy/entropy" };

	ArrayList[] result = new ArrayList[8];
	String[] resultTitle = new String[] { "eigenv-" + Dim1CutOff,
		"eigenv-d(e)/e", "eigenv-dd(e)", "eigenv-e/e",
		"entropy-" + Dim1CutOff, "entropy-d(e)/e", "entropy-dd(e)",
		"entropy-e/e" };
	for (int i = 0; i < result.length; i++) {
	    result[i] = new ArrayList();
	    result[i].add(resultTitle[i]);
	}

	while (line != null) {
	    line = line.trim();
	    System.out.println(line);
	    segment = line.split("[ \t]+");
	    length = segment.length;

	    while (Double.parseDouble(segment[first]) >= 1)
		first++;
	    data = new double[length - first];
	    for (int i = first; i < length; i++)
		data[i - first] = Double.parseDouble(segment[i]);

	    matrix = dim(data);
	    System.out.println(matrix);
	    ObjectMatrix2D summary = rowSummaryMatrix(matrix, false, title);
	    System.out.println(summary);

	    System.out.println();
	    line = reader.readLine();

	    // dim estimation
	    int d = 0;
	    while (matrix.get(0, d) < Dim1CutOff)
		d++;
	    result[0].add(new Integer(d + 1));

	    d = 0;
	    while (matrix.get(7, d) < Dim1CutOff
		    * matrix.get(7, matrix.columns() - 1))
		d++;
	    result[4].add(new Integer(d + 1));

	    result[1].add(new Integer(((Integer) ((Pair) summary.get(3, 5))
		    .second()).intValue() + 1));
	    result[2].add(new Integer(((Integer) ((Pair) summary.get(4, 5))
		    .second()).intValue() + 1));
	    result[3].add(new Integer(((Integer) ((Pair) summary.get(5, 5))
		    .second()).intValue() + 1));
	    result[5].add(new Integer(((Integer) ((Pair) summary.get(9, 5))
		    .second()).intValue() + 1));
	    result[6].add(new Integer(((Integer) ((Pair) summary.get(10, 5))
		    .second()).intValue() + 1));
	    result[7].add(new Integer(((Integer) ((Pair) summary.get(11, 5))
		    .second()).intValue() + 1));

	}

	Object[][] matrixData = new Object[8][];
	for (int i = 0; i < matrixData.length; i++)
	    matrixData[i] = result[i].toArray();
	System.out.println((new DenseObjectMatrix2D(matrixData)));// .viewDice()
								  // );

    }

    // given the eigenvalues, in sum-ups, out put other statistics of them, e.g.
    // delta...
    static DenseDoubleMatrix2D dim(double[] data) {
	final double CutOff = 0.00001;
	int col = 0;
	while ((col < data.length) && (data[col] < 1)
		&& ((col == 0) || (data[col] - data[col - 1] >= CutOff)))
	    col++;
	if (col < data.length)
	    col++; // from offset to size
	// System.out.println("col = " + col);
	DenseDoubleMatrix2D matrix = new DenseDoubleMatrix2D(12, col);

	// row 0: sums of eigenvalues
	matrix.viewRow(0).assign(
		(new DenseDoubleMatrix1D(data)).viewPart(0, col));

	// row 1: real eigenvalues (percentage)
	matrix.set(1, 0, matrix.get(0, 0));
	for (int i = 1; i < col; i++)
	    matrix.set(1, i, matrix.get(0, i) - matrix.get(0, i - 1));

	// row 2: delta, difference of eigenvalues; delta(i) = e(i) - e(i+1);
	for (int i = 0; i < col - 1; i++)
	    matrix.set(2, i, matrix.get(1, i) - matrix.get(1, i + 1));
	matrix.set(2, col - 1, 0); // last one, no meaning.

	// row 3: rate of delta, r(i) = delta(i) / [e(i) + e(i+1) ]
	for (int i = 0; i < col - 1; i++)
	    matrix.set(3, i,
		    matrix.get(2, i)
			    / (matrix.get(1, i) + matrix.get(1, i + 1)));
	matrix.set(3, col - 1, 0);

	// row 4: delta of delta, dd(i) = [d(i-1) - d(i)]/[e(i-1)+2e(i) +
	// e(i+1)]
	for (int i = 1; i < col - 1; i++)
	    matrix.set(
		    4,
		    i,
		    (matrix.get(2, i - 1) - matrix.get(2, i))
			    / (matrix.get(1, i - 1) + 2 * matrix.get(1, i) + matrix
				    .get(1, i + 1)));
	matrix.set(4, 0, 0);
	matrix.set(4, col - 1, 0);

	// row 5: proportion, p(i) = p(i)/p(i+1)
	for (int i = 0; i < col - 1; i++)
	    matrix.set(5, i, matrix.get(1, i) / matrix.get(1, i + 1));
	matrix.set(5, col - 1, 1);

	// row 6: entropy
	for (int i = 0; i < col; i++)
	    matrix.set(
		    6,
		    i,
		    0 - matrix.get(1, i)
			    * Math.log(matrix.get(1, i) / Math.log(2)));

	// row 7: sum of entropy
	matrix.set(7, 0, matrix.get(6, 0));
	for (int i = 1; i < col; i++)
	    matrix.set(7, i, matrix.get(6, i) + matrix.get(7, i - 1));

	// row 8: delta(entropy), d-en(i) = en(i) - en(i+1)
	for (int i = 0; i < col - 1; i++)
	    matrix.set(8, i, matrix.get(6, i) - matrix.get(6, i + 1));
	matrix.set(8, col - 1, 0);

	// row 9: rate of delta(entropy) r-en(i) = d-en(i) / [ en(i) + en(i+1) ]
	for (int i = 0; i < col - 1; i++)
	    matrix.set(9, i,
		    matrix.get(8, i)
			    / (matrix.get(6, i) + matrix.get(6, i + 1)));
	matrix.set(9, col - 1, 0);

	// row 10: delta of delta(entropy), dd-en(i) = [d-en(i-1) - d-en(i)]/[
	// en(i-1) + 2en(i) + en(i+1)]
	for (int i = 1; i < col - 1; i++)
	    matrix.set(
		    10,
		    i,
		    (matrix.get(8, i - 1) - matrix.get(8, i))
			    / (matrix.get(6, i - 1) + 2 * matrix.get(6, i) + matrix
				    .get(6, i + 1)));
	matrix.set(10, col - 1, 0);
	matrix.set(10, 0, 0);

	// row 11; proportion p-en(i) =en(i)/ en(i+1)
	for (int i = 0; i < col - 1; i++)
	    matrix.set(11, i, matrix.get(6, i) / matrix.get(6, i + 1));
	matrix.set(11, col - 1, 1);

	return matrix;
    }

    /**
     * Compute Principal Component Analysis, only find the PC with the largest
     * variance, with Hebbian method, a neural network method.
     * 
     * @param matrix
     * @return
     */
    static DoubleMatrix1D primaryHebbian(DoubleMatrix2D matrix) {
	final int size = matrix.rows();
	final int dim = matrix.columns();

	// center the matrix
	double[] columnMean = new double[dim];
	for (int i = 0; i < dim; i++)
	    columnMean[i] = matrix.viewColumn(i).zSum() / size;

	for (int i = 0; i < dim; i++)
	    for (int j = 0; j < size; j++)
		matrix.set(j, i, matrix.get(j, i) - columnMean[i]);

	final double ing = 0.1;
	// each row is a pc, the first is the one with the largest eigenvalue.
	double[] w = new double[dim + 1];
	double sum2 = 0;
	Random r = new Random();
	for (int i = 0; i < dim; i++)
	    w[i] += r.nextDouble() - 0.5;

	double y = 0;
	for (int row = 0; row < size; row++) {
	    // set y
	    y = 0;
	    for (int j = 0; j < dim; j++)
		y += matrix.get(row, j) * w[j];
	    sum2 += y * y;

	    // set w
	    for (int i = 0; i < dim; i++)
		w[i] += ing * y * (matrix.get(row, i) - y * w[i]);

	}

	// return the results
	w[dim] = sum2 / size;

	return (new DenseDoubleMatrix1D(w));
    }

    static void centerize(DoubleMatrix2D matrix) {
	if (matrix instanceof LargeDenseDoubleMatrix2D) {
	    centerizeLarge((LargeDenseDoubleMatrix2D) matrix);
	    return;
	}

	final int size = matrix.rows();
	final int dim = matrix.columns();

	double[] columnMean = new double[dim];
	for (int i = 0; i < dim; i++)
	    columnMean[i] = matrix.viewColumn(i).zSum() / size;

	for (int i = 0; i < dim; i++)
	    for (int j = 0; j < size; j++)
		matrix.set(j, i, matrix.get(j, i) - columnMean[i]);
    }

    static void centerizeLarge(LargeDenseDoubleMatrix2D matrix) {
	final int size = matrix.rows();
	final int dim = matrix.columns();

	double[] columnMean = new double[dim];
	for (int i = 0; i < dim; i++) {
	    columnMean[i] = 0;
	    for (int j = 0; j < size; j++)
		columnMean[i] += matrix.get(j, i);
	    columnMean[i] /= size;
	}

	for (int i = 0; i < dim; i++)
	    for (int j = 0; j < size; j++)
		matrix.set(j, i, matrix.get(j, i) - columnMean[i]);
    }

    /**
     * Compute Principal Component Analysis with Hebbian method, a neural
     * network method.
     * 
     * @param matrix
     * @param pcNum
     * @param lFactor
     * @param scanNum
     * @return
     */
    static DoubleMatrix2D Hebbian(DoubleMatrix2D matrix, final int pcNum,
	    double lFactor, int scanNum)// , double wRange, double threshold)
    {
	// final double threshhold = 0.001;

	final int size = matrix.rows();
	final int dim = matrix.columns();

	// center the matrix
	centerize(matrix);

	// double lFactor = 0.01;
	// final int scanNum = 3000;
	final double wRange = 1;
	// each row is a pc, the first is the one with the largest eigenvalue.
	double[][] w = new double[pcNum][dim + 1];
	double delta = 0;
	double[] delta2 = new double[pcNum];
	double[] w2 = new double[pcNum];
	double sum2[] = new double[pcNum];
	double[] y = new double[pcNum];
	double[] yw = new double[dim];
	// double mode=0, lastMode = 0;

	// initialization
	Random r = new Random();
	for (int pc = 0; pc < pcNum; pc++) {
	    sum2[pc] = 0;
	    y[pc] = 0;
	    w[pc] = new double[dim + 1];
	    w2[pc] = 0;
	    delta2[pc] = 0;
	    for (int col = 1; col < dim + 1; col++)
		w[pc][col] = (r.nextDouble() - 0.5) * wRange;
	}

	// iteration
	int scan = 0;
	boolean converge = false;
	while ((scan < 1) || (scan < scanNum) && !converge) {
	    for (int row = 0; row < size; row++) {
		// set y
		for (int pc = 0; pc < pcNum; pc++) {
		    y[pc] = 0;
		    for (int col = 0; col < dim; col++)
			y[pc] += matrix.get(row, col) * w[pc][col + 1];

		    sum2[pc] += y[pc] * y[pc];
		}

		for (int col = 0; col < dim; col++)
		    yw[col] = 0;

		// set w
		for (int pc = 0; pc < pcNum; pc++) {
		    w2[pc] = 0;
		    delta2[pc] = 0;
		    for (int col = 0; col < dim; col++) {
			yw[col] += y[pc] * w[pc][col + 1];
			delta = y[pc] * (matrix.get(row, col) - yw[col])
				* lFactor;
			delta2[pc] += delta * delta;
			w[pc][col + 1] += delta;
			w2[pc] += w[pc][col + 1] * w[pc][col + 1];

		    }
		}

		/*
		 * if( (Math.sqrt(delta2/w2)<= threshhold) && (scan > 0) ) {
		 * System.out.println("scan = " + scan); totalSize = scan*size +
		 * row +1; break; }
		 */

	    }
	    scan++;
	    converge = false; // isStandardBasis(w, threshold) ;
	    /*
	     * for (int pc =0; pc<pcNum; pc++) if (Math.sqrt(delta2[pc]/w2[pc])
	     * > threshhold) { converge = false; break; }
	     */

	}

	// System.out.println("Scans: " + scan + ", total rows scaned = " +
	// (scan*size));

	// return the results
	for (int pc = 0; pc < pcNum; pc++)
	    w[pc][0] = sum2[pc] / (scan * size);

	return (new DenseDoubleMatrix2D(w));
    }

    // check whether the row-vectors are standard basis, i.e. norm to 1 and
    // orthogonal
    static boolean isStandardBasis(double[][] data, final double threshold) {
	final int vectorNum = data.length;
	final int dim = data[0].length;
	final double t = threshold * threshold;

	double norm = 0;
	for (int row = 0; row < vectorNum; row++) {
	    norm = 0;
	    for (double d : data[row])
		norm += d * d;

	    if ((norm < (1 - threshold) * (1 - threshold))
		    || (norm > (1 + threshold) * (1 + threshold)))
		return false;

	    for (int previous = 0; previous < row; previous++) {
		norm = 0;
		for (int col = 0; col < dim; col++)
		    norm += data[row][col] * data[previous][col];

		if (Math.abs(norm) > t)
		    return false;
	    }
	}

	return true;
    }

    static DoubleMatrix2D orthonormalization(DoubleMatrix2D matrix) {
	Algebra alg = new Algebra();

	final int rowNum = matrix.rows();
	final int colNum = matrix.columns();
	final int rank = alg.rank(matrix.viewPart(0, 0, rowNum,
		(colNum <= rowNum) ? colNum : rowNum));

	DoubleMatrix2D result = new DenseDoubleMatrix2D(rowNum, rank);
	DoubleMatrix1D tempColumn = new DenseDoubleMatrix1D(rowNum);

	result.viewColumn(0).assign(matrix.viewColumn(0));
	for (int col = 1; col < rank; col++) {
	    result.viewColumn(col).assign(matrix.viewColumn(col));
	    for (int i = 0; i < col; i++)
		result.viewColumn(col).assign(
			tempColumn.assign(
				alg.mult(matrix.viewColumn(col),
					result.viewColumn(i))
					/ alg.norm2(result.viewColumn(i)))
				.assign(result.viewColumn(i), Functions.mult),
			Functions.minus);
	}
	for (int col = 0; col < rank; col++)
	    result.viewColumn(col).assign(
		    tempColumn.assign(Math.sqrt(alg.norm2(result
			    .viewColumn(col)))), Functions.div);

	return result;
    }

    /**
     * Compute Principal Component Analysis with EM method.
     * 
     * @param matrix
     *            the matrix to do PCA, needs not to be centerized already. This
     *            method will centerize it. Each row is a data point.
     * @param pcNum
     * @return a {@link DoubleMatrix2D} of size [pcNum x (dim +1)], the first
     *         column is the variance of each PC in descending order. The remain
     *         of each row is a PC
     */
    static DoubleMatrix2D EMPCA(DoubleMatrix2D matrix, final int pcNum) {
	// System.out.println(matrix);

	if (matrix instanceof LargeDenseDoubleMatrix2D)
	    return EMPCALarge((LargeDenseDoubleMatrix2D) matrix, pcNum);

	final int iterNum = 20;
	// final int size = matrix.rows();
	final int dim = matrix.columns();

	// center the matrix
	// System.out.println(matrix);
	centerize(matrix);
	DoubleMatrix2D data = matrix.viewDice();


	// initialization
	Random r = new Random();
	double[][] CData = new double[dim][pcNum];
	for (int i = 0; i < dim; i++)
	    for (int j = 0; j < pcNum; j++)
		CData[i][j] = r.nextDouble() - 0.5;
	DoubleMatrix2D C = new DenseDoubleMatrix2D(CData);
	DoubleMatrix2D x = null;
	Algebra alg = new Algebra();

	for (int i = 0; i < iterNum; i++) {
	    x = alg.mult(
		    alg.mult(alg.inverse(alg.mult(C.viewDice(), C)),
			    C.viewDice()), data);
	    C = alg.mult(alg.mult(data, x.viewDice()),
		    alg.inverse(alg.mult(x, x.viewDice())));
	}

	C = orthonormalization(C);

	DoubleMatrix2D cov = Statistic.covariance(alg.mult(C.viewDice(), data)
		.viewDice());

	EigenvalueDecomposition evd = new EigenvalueDecomposition(cov);

	DenseDoubleMatrix2D result = new DenseDoubleMatrix2D(dim + 1, pcNum);
	result.viewPart(1, 0, dim, pcNum).assign(
		alg.mult(C, evd.getV().viewColumnFlip()));
	result.viewRow(0).assign(evd.getRealEigenvalues().viewFlip());
	return result.viewDice();
    }

    static DoubleMatrix2D EMPCALarge(LargeDenseDoubleMatrix2D matrix,
	    final int pcNum) {
	final int iterNum = 20;
	final int dim = matrix.columns();

	// center the matrix
	centerize(matrix);
	// initialization
	Random r = new Random();
	double[][] CData = new double[dim][pcNum];
	for (int i = 0; i < dim; i++)
	    for (int j = 0; j < pcNum; j++)
		CData[i][j] = r.nextDouble() - 0.5;
	DoubleMatrix2D C = new DenseDoubleMatrix2D(CData);
	DoubleMatrix2D x = null;
	Algebra alg = new Algebra();

	for (int i = 0; i < iterNum; i++) {
	    x = LargeDenseDoubleMatrix2D.mult(LargeDenseDoubleMatrix2D.mult(alg
		    .inverse(LargeDenseDoubleMatrix2D.mult(C, true, C, false)),
		    false, C, true), false, matrix, true);
	    C = LargeDenseDoubleMatrix2D.mult(LargeDenseDoubleMatrix2D.mult(
		    matrix, true, x, true), false, alg
		    .inverse(LargeDenseDoubleMatrix2D.mult(x, false, x, true)),
		    false);
	}

	C = orthonormalization(C);

	DoubleMatrix2D cov = LargeDenseDoubleMatrix2D
		.covariance(LargeDenseDoubleMatrix2D.mult(C.viewDice(), false,
			matrix, true), true);

	EigenvalueDecomposition evd = new EigenvalueDecomposition(cov);

	DenseDoubleMatrix2D result = new DenseDoubleMatrix2D(dim + 1, pcNum);
	result.viewPart(1, 0, dim, pcNum).assign(
		alg.mult(C, evd.getV().viewColumnFlip()));
	result.viewRow(0).assign(evd.getRealEigenvalues().viewFlip());

	return result.viewDice();
    }

    public static void main(String[] args) throws Exception {
	double startTime = System.currentTimeMillis();
	final boolean print = true;

	if (args[args.length - 1].equalsIgnoreCase("hebb")) {
	    if (args[0].equalsIgnoreCase("vector"))
		System.out.println(Hebbian(vectorMain(args),
			Integer.parseInt(args[args.length - 4]),
			Double.parseDouble(args[args.length - 3]),
			Integer.parseInt(args[args.length - 2])));

	    if (args[0].equalsIgnoreCase("vectorp"))
		System.out.println(Hebbian(vectorPMain(args),
			Integer.parseInt(args[args.length - 4]),
			Double.parseDouble(args[args.length - 3]),
			Integer.parseInt(args[args.length - 2])));

	    if (args[0].equalsIgnoreCase("dna"))
		System.out.println(Hebbian(DNAMain(args),
			Integer.parseInt(args[args.length - 4]),
			Double.parseDouble(args[args.length - 3]),
			Integer.parseInt(args[args.length - 2])));

	    if (args[0].equalsIgnoreCase("protein"))
		System.out.println(Hebbian(proteinMain(args),
			Integer.parseInt(args[args.length - 4]),
			Double.parseDouble(args[args.length - 3]),
			Integer.parseInt(args[args.length - 2])));

	    if ((args[0].equalsIgnoreCase("image"))
		    || (args[0].equalsIgnoreCase("mass")))
		System.out.println(Hebbian(imageMain(args),
			Integer.parseInt(args[args.length - 4]),
			Double.parseDouble(args[args.length - 3]),
			Integer.parseInt(args[args.length - 2])));

	    System.out.println("Running time: "
		    + (System.currentTimeMillis() - startTime) / 1000
		    + " seconds.");
	    return;
	}

	if (args[args.length - 1].equalsIgnoreCase("em")) {
	    if (args[0].equalsIgnoreCase("vector"))
		System.out.println(EMPCA(vectorMain(args),
			Integer.parseInt(args[args.length - 2])));

	    if (args[0].equalsIgnoreCase("vectorp"))
		System.out.println(EMPCA(vectorPMain(args),
			Integer.parseInt(args[args.length - 2])));

	    if (args[0].equalsIgnoreCase("dna"))
		System.out.println(EMPCA(DNAMain(args),
			Integer.parseInt(args[args.length - 2])));

	    if (args[0].equalsIgnoreCase("protein"))
		System.out.println(EMPCA(proteinMain(args),
			Integer.parseInt(args[args.length - 2])));

	    if ((args[0].equalsIgnoreCase("image"))
		    || (args[0].equalsIgnoreCase("mass")))
		System.out.println(EMPCA(imageMain(args),
			Integer.parseInt(args[args.length - 2])));

	    System.out.println("Running time: "
		    + (System.currentTimeMillis() - startTime) / 1000
		    + " seconds.");
	    return;
	}

	if (args[0].equalsIgnoreCase("dim"))
	    dimMain(args);

	if (args[0].equalsIgnoreCase("dna"))
	    runPCA(DNAMain(args), print);

	if (args[0].equalsIgnoreCase("dnad"))
	    DNADimension(args);

	if (args[0].equalsIgnoreCase("protein"))
	    runPCA(proteinMain(args), print);

	if (args[0].equalsIgnoreCase("aminoacids"))
	    runPCA(aminoacids(), print);

	if (args[0].equalsIgnoreCase("aminoacidsp"))
	    runPCA(aminoacidsP(), print);

	if (args[0].equalsIgnoreCase("proteind"))
	    proteinDimension(args);

	if ((args[0].equalsIgnoreCase("image"))
		|| (args[0].equalsIgnoreCase("mass")))
	    runPCA(imageMain(args), print);

	if ((args[0].equalsIgnoreCase("imaged"))
		|| args[0].equalsIgnoreCase("massd"))
	    singleDimDimension(args);

	if (args[0].equalsIgnoreCase("vector"))
	    runPCA(vectorMain(args), print);

	if (args[0].equalsIgnoreCase("vectorp"))
	    runPCA(vectorPMain(args), print);

	if (args[0].equalsIgnoreCase("vectorpd"))
	    vectorPDimension(args);

	/*
	 * if (args[0].equalsIgnoreCase("hamming")) hammingMain(args);
	 * 
	 * if (args[0].equalsIgnoreCase("mass")) massMain(args);
	 */

	System.out
		.println("Running time: "
			+ (System.currentTimeMillis() - startTime) / 1000
			+ " seconds.");
    }

}