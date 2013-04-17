/**
 * GeDBIT.app.QueryEvaluator 2006.06.27
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.06.27: Copied from jdb 1.0, by Rui Mao
 */
package GeDBIT.mapreduce.app;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import GeDBIT.dist.Metric;
import GeDBIT.index.Index;
import GeDBIT.index.RangeQuery;
import GeDBIT.index.VPRangeCursor;
import GeDBIT.mapreduce.app.IndexMapReduce.radiusData;
import GeDBIT.type.DoubleIndexObjectPair;
import GeDBIT.type.DoubleVector;
import GeDBIT.type.IndexObject;
import GeDBIT.type.Sequence;
import GeDBIT.type.Table;
import GeDBIT.type.TableMR;
/**
 * This is a utility class to query a VPIndex. It, taking command line
 * parameters, runs a set of query on the given index and compute the average
 * performance.
 * 
 * main function to evaluate the query performance of an {@link Index} The
 * eveluation is done by run a set of query on the Index, and compute the
 * average performance statistics, such as number of distance calculation,
 * number of index node visited (#I/O), and search time. The command line
 * interface to bulkload one {@link Index}, or a series of {@link Index}es for
 * scalability study.
 * 
 * -d [name of index, should be a prefix of the actual file name containing serialized database] 
 * -q [query file name] 
 * -f [offset of first query to be used in the query file, start from 0, inclusive, default 0] 
 * -l [offset of
 * last query to be used in the query file, exclusive, default 1] 
 * -i [minimum search radius, default 0] 
 * -a [maximum search radius, default 10] 
 * -s [step size for search radii, default 1] 
 * -t [data type, "vector", "protein", "dna", "image", "mass"] 
 * -p [length of the path distance list] 
 * -v [1 if search results are to be verified against a linear scan and 0 otherwise, default 0]
 * -g [debug level, default 0] 
 * -frag [fragment length, only meaningful for {@link Sequence}s] 
 * -dim [dimension of vector data to load, only meaningful for {@link DoubleVector}s] 
 * -res [output results to the given filename]
 * 
 * The {@link Metric} is hardcoded for each give data type.
 * 
 * @author Rui Mao, Willard
 * @version 2006.06.27
 */
public class QueryVPIndexMR {
    String forPrint;
    // data fields for arguments
    final Metric metric;

    final Index index;

    final int indexSize; // number of data objects in the
			 // index

    TableMR query;
    int firstQuery;
    int lastQuery;

    int querySize; // number of queries

    final double minRadius;

    final double maxRadius;

    final int pathLength;

    final double step;

    final int numRun; // number of different radii to run

    final boolean verify;

    final List<IndexObject> linearIndex;

    final Level debug;

    final int frag;

    boolean outputToFile; // indicates whether to output
			  // statistics to a file or

    // to the terminal
    String outputFile; // output filename

    // data fields to store statistics, each array's first dimension is the
    // dimension for different
    // search radius, start from the min one.
    // global statistics
    double[] distNum; // number of distance calculation,
		      // calculations with

    // centers,
    int[] minDistNum, minDistNumID, maxDistNum, maxDistNumID;

    double[] centerDistNum; // with
			    // bottom
			    // data

    // objects.
    int[] minCenterDistNum, minCenterDistNumID, maxCenterDistNum,
	    maxCenterDistNumID;

    double[] dataDistNum;

    int[] minDataDistNum, minDataDistNumID, maxDataDistNum, maxDataDistNumID;

    double[] nodeVisited; // number

    // of
    // index
    // nodes
    // visited
    int[] minNodeVisited, minNodeVisitedID, maxNodeVisited, maxNodeVisitedID;

    double[] internalVisited;
    double[] resultInternalNode; // number of internal nodes containing at least
				 // one query result;
    double[] resultLeafNode; // number of leaf nodes containing at least one
			     // query result;
    double[] resultNode; // number of nodes containing at least one query
			 // result;
    // double[] pivotDistNum;
    double[] pivotAsResult;
    double[] resultWithoutDistance;
    double[] internalPruned;
    double[] leafPruned;
    double[] internalWithoutDist;
    double[] leafWithoutDist;

    int[] minInternalVisited, minInternalVisitedID, maxInternalVisited,
	    maxInternalVisitedID;

    double[] leafVisited;

    int[] minLeafVisited, minLeafVisitedID, maxLeafVisited, maxLeafVisitedID;

    double[] time, minTime, maxTime; // search

    // time
    int[] minTimeID, maxTimeID;

    double[] result; // number

    // of
    // search
    // result
    int[] minResult, minResultID, maxResult, maxResultID;

    // layer statistics, computed in temp variables, and finally added to the
    // lists when values are
    // fixed.

    // distance between the query and the centers(center of VP) of each level,
    // -1 means no such value
    double[][][] queryCenterDistance;

    // each element is a 1-d array, is the average value of one layer, for each
    // center/vp (column)
    // computed during processing the layer, and value added to this list at the
    // end of the layer

    // from the perspective of the last layer, these are the total children node
    // number
    int[][] layerNode;

    // number of childre visited, and the ratio. thus, these values are computed
    int[][] layerNodeVisited;

    // during the processing of last layer, and are added to the list at the
    // begin // of the current
    // layer.

    // number of data objects of leaf nodes of each level,
    int[][] layerData;

    // number of data object directly compute distance on, ratio
    int[][] layerDataVisited;

    // computed during processing the layer, and value added to this list at the
    // end of the layer

    // name of file to save results to.
    private String resultsFileName;

    /**
     * @param args
     */
    // Honglong Xu
    /*
     * public static void main(String[] args) { // arguments and default values
     * String indexName = ""; String queryFileName = ""; String forPrint = "";
     * 
     * int firstQuery = 0; int lastQuery = 1;
     * 
     * double minRadius = 0.0; double maxRadius = 10.0; double step = 1.0;
     * 
     * boolean verify = false; Level debug = Level.OFF;
     * 
     * int frag = 6; int dim = 2;
     * 
     * int pathLength = 0;
     * 
     * String dataType = "sequence";
     * 
     * String resultsFileName = null;
     * 
     * // parse arguments, and set values for (int i = 0; i < args.length; i = i
     * + 2) { if (args[i].equalsIgnoreCase("-d")) indexName = args[i + 1];
     * 
     * else if (args[i].equalsIgnoreCase("-q")) queryFileName = args[i + 1];
     * 
     * else if (args[i].equalsIgnoreCase("-t")) dataType = args[i + 1];
     * 
     * else if (args[i].equalsIgnoreCase("-forprint")) forPrint += args[i + 1] +
     * ", ";
     * 
     * else if (args[i].equalsIgnoreCase("-f")) firstQuery =
     * Integer.parseInt(args[i + 1]);
     * 
     * else if (args[i].equalsIgnoreCase("-l")) lastQuery =
     * Integer.parseInt(args[i + 1]);
     * 
     * else if (args[i].equalsIgnoreCase("-p")) pathLength =
     * Integer.parseInt(args[i + 1]);
     * 
     * else if (args[i].equalsIgnoreCase("-i")) minRadius =
     * Double.parseDouble(args[i + 1]);
     * 
     * else if (args[i].equalsIgnoreCase("-a")) maxRadius =
     * Double.parseDouble(args[i + 1]);
     * 
     * else if (args[i].equalsIgnoreCase("-s")) step = Double.parseDouble(args[i
     * + 1]);
     * 
     * else if (args[i].equalsIgnoreCase("-g")) debug = Level.parse(args[i +
     * 1]);
     * 
     * else if (args[i].equalsIgnoreCase("-frag")) frag =
     * Integer.parseInt(args[i + 1]);
     * 
     * else if (args[i].equalsIgnoreCase("-dim")) dim = Integer.parseInt(args[i
     * + 1]);
     * 
     * else if (args[i].equalsIgnoreCase("-v")) verify =
     * (Integer.parseInt(args[i + 1]) == 1) ? true : false; else if
     * (args[i].equalsIgnoreCase("-res")) resultsFileName = args[i + 1]; else
     * throw new IllegalArgumentException("Invalid option " + args[i]); }
     * 
     * // check arguments if (indexName == "") throw new
     * IllegalArgumentException("Invalid Index file name!");
     * 
     * if (queryFileName == "") throw new
     * IllegalArgumentException("Invalid Query file name!");
     * 
     * if ((firstQuery < 0) || (lastQuery < 0) || (lastQuery < firstQuery))
     * throw new
     * IllegalArgumentException("Invalid first query index or last query index!"
     * );
     * 
     * if ((minRadius < 0) || (maxRadius < 0) || (maxRadius < minRadius) ||
     * (step <= 0)) throw new IllegalArgumentException(
     * "Invalid min radius, max radius, or radius increasement unit!");
     * 
     * // load index from file TableMR dataTable =
     * TableManager.getTableManager(indexName).getTable(indexName); Index index;
     * if (dataTable != null) index = dataTable.getIndex(); else throw new
     * Error("index: " + indexName + " does not exist");
     * 
     * // load queryData from file TableMR queryTable = null; double[][] data =
     * null; try { if (dataType.equalsIgnoreCase("protein")) queryTable = new
     * PeptideTable(queryFileName, "", lastQuery, frag); else if
     * (dataType.equalsIgnoreCase("vector")) queryTable = new
     * DoubleVectorTableMR(queryFileName, "", lastQuery, dim, data); else if
     * (dataType.equalsIgnoreCase("dna")) queryTable = new
     * DNATable(queryFileName, "", lastQuery, frag); else if
     * (dataType.equalsIgnoreCase("image")) queryTable = new
     * ImageTable(queryFileName, "", lastQuery); else if
     * (dataType.equalsIgnoreCase("msms")) queryTable = new
     * SpectraWithPrecursorMassTable(queryFileName, "", lastQuery); else
     * System.err.println("data type not supported! " + dataType); } catch
     * (IOException e1) { // TODO Auto-generated catch block
     * e1.printStackTrace(); }
     * 
     * // evaluate QueryVPIndexMR evaluator = new QueryVPIndexMR(index,
     * queryTable, minRadius, maxRadius, step, verify , debug, pathLength, frag,
     * resultsFileName, firstQuery, lastQuery, forPrint);
     * 
     * //evaluator.evaluate();
     * 
     * // TODO close index? }
     */

    /**
     * @param index
     *            the {@link Index} to be evaluated
     * @param query
     *            the {@link Table} to query the index with.
     * @param minRadius
     *            the minimum range query radius to run
     * @param maxRadius
     *            the maximum range query radius to run
     * @param step
     *            the increament unit of the range query radius
     * @param verify
     *            if true, search results of each query will be verified by a
     *            linear scan
     * @param debug
     *            decide how much execution log will be output, 0 means nothing.
     * @param pathLength
     * @param frag
     * @param resultsFileName
     *            the name of the file to save the results to.
     */

    // public QueryVPIndex(Index index, Table query, double minRadius, double
    // maxRadius, double step, boolean verify, Level debug, int pathLength,
    // int frag, String resultsFileName)
    // {
    // this(index, query, minRadius, maxRadius)
    // }

    public QueryVPIndexMR(Index index, TableMR query, double minRadius,
	    double maxRadius, double step, boolean verify, Level debug,
	    int pathLength, int frag, String resultsFileName, int firstQuery,
	    int lastQuery, String forPrint) {
	// check argument
	if (index == null)
	    throw new IllegalArgumentException(" The Index is null!");

	if (query == null)
	    throw new IllegalArgumentException(" The query list is null!");

	if ((minRadius < 0) || (maxRadius < 0) || (maxRadius < minRadius)
		|| (step <= 0))
	    throw new IllegalArgumentException(
		    "Invalid min radius, max radius, or radius increasement unit!");

	this.metric = index.getMetric();
	this.index = index;
	this.indexSize = index.size();
	this.query = query;
	this.querySize = query.size();
	this.minRadius = minRadius;
	this.maxRadius = maxRadius;
	this.step = step;
	this.verify = verify;
	this.numRun = (int) Math.round((maxRadius - minRadius) / step) + 1;
	this.frag = frag;
	this.resultsFileName = resultsFileName;

	this.outputToFile = false;
	this.outputFile = "";
	this.firstQuery = firstQuery;
	this.lastQuery = lastQuery;

	this.forPrint = forPrint;

	if (verify)
	    this.linearIndex = index.getAllPoints();
	else
	    this.linearIndex = null;

	this.debug = debug;

	this.pathLength = pathLength;

	// allocate space for statistics
	// number of distance calculation
	distNum = new double[numRun];
	minDistNum = new int[numRun];
	minDistNumID = new int[numRun];
	maxDistNum = new int[numRun];
	maxDistNumID = new int[numRun];

	centerDistNum = new double[numRun];
	minCenterDistNum = new int[numRun];
	minCenterDistNumID = new int[numRun];
	maxCenterDistNum = new int[numRun];
	maxCenterDistNumID = new int[numRun];

	dataDistNum = new double[numRun];
	minDataDistNum = new int[numRun];
	minDataDistNumID = new int[numRun];
	maxDataDistNum = new int[numRun];
	maxDataDistNumID = new int[numRun];

	// number of index node visited
	nodeVisited = new double[numRun];
	minNodeVisited = new int[numRun];
	minNodeVisitedID = new int[numRun];
	maxNodeVisited = new int[numRun];
	maxNodeVisitedID = new int[numRun];

	// number of nodes containing at least on query result
	resultNode = new double[numRun];
	resultInternalNode = new double[numRun];
	resultLeafNode = new double[numRun];
	// pivotDistNum = new double[numRun];
	pivotAsResult = new double[numRun];
	resultWithoutDistance = new double[numRun];
	internalWithoutDist = new double[numRun];
	leafWithoutDist = new double[numRun];
	internalPruned = new double[numRun];
	leafPruned = new double[numRun];

	internalVisited = new double[numRun];
	minInternalVisited = new int[numRun];
	minInternalVisitedID = new int[numRun];
	maxInternalVisited = new int[numRun];
	maxInternalVisitedID = new int[numRun];

	leafVisited = new double[numRun];
	minLeafVisited = new int[numRun];
	minLeafVisitedID = new int[numRun];
	maxLeafVisited = new int[numRun];
	maxLeafVisitedID = new int[numRun];

	// search time
	time = new double[numRun];
	minTime = new double[numRun];
	minTimeID = new int[numRun];
	maxTime = new double[numRun];
	maxTimeID = new int[numRun];

	// result number
	result = new double[numRun];
	minResult = new int[numRun];
	minResultID = new int[numRun];
	maxResult = new int[numRun];
	maxResultID = new int[numRun];

	queryCenterDistance = new double[numRun][][];

	layerNode = new int[numRun][];
	layerNodeVisited = new int[numRun][];
	layerData = new int[numRun][];
	layerDataVisited = new int[numRun][];

    }

    /**
     * the primary method to do the evaluation. It first run the queries for
     * each query, during which the statistics are set, then output.
     */
    public void evaluate(List<IndexObject> resultList,
	    List<radiusData> queryRadiusData) {
	// final double delta = 1e-6;

	// run queries for each query, and so that set the statistics
	double radius = 0;
	for (int i = 0; i < this.numRun; i++)
	// for (double radius = minRadius; radius <= maxRadius; radius += step)
	{
	    radius = this.minRadius + this.step * i;
	    evaluateRadius(radius, resultList);
	    queryRadiusData.add(new radiusData(radius, resultList.size()));
	}

	// output
	/*
	 * System.out.println("index size, radius, #distance calculation(center,
	 * data object), #node visited(internal, leaf), search time, #result");
	 * for (int i=0; i< numRun; i++) System.out.println(indexSize + ", " +
	 * (minRadius + i*step) + ", " + distNum[i] + ", (," + centerDistNum[i]
	 * + ", " + dataDistNum[i] + ",), " + nodeVisited[i] + ", (," +
	 * internalVisited[i] + ", " + leafVisited[i] + ",), " + time[i] + ", "
	 * + result[i] + ", "); System.out.println(); System.out.println();
	 */

	DecimalFormat fmt = new DecimalFormat("0.#####"); // for 3 decimal
							  // places

	// output detail statitstics
	if (outputToFile && !outputFile.equals("")) {
	    // open file for output
	    PrintWriter outputStream = null;

	    try {
		outputStream = new PrintWriter(new FileOutputStream(outputFile));

		outputStream
			.println("index size, radius, #distance calculation(center, data object), #node visited(internal, leaf), search time, #result, #layerNodeVisited, #layerDataVisited, queryCenterDistance");
		for (int i = 0; i < numRun; i++) {
		    outputStream.println();

		    outputStream.println(indexSize + ", "
			    + fmt.format((minRadius + i * step))
			    + ", #dist:[, " + distNum[i] + " ,(, "
			    + (minDistNum[i]) + " ,:, " + (minDistNumID[i])
			    + " ,) (, " + (maxDistNum[i]) + " ,:, "
			    + (maxDistNumID[i]) + " ,)] " + "{ [, "
			    + (centerDistNum[i]) + " ,(, "
			    + (minCenterDistNum[i]) + " ,:, "
			    + (minCenterDistNumID[i]) + " ,) (, "
			    + (maxCenterDistNum[i]) + " ,:, "
			    + (maxCenterDistNumID[i]) + " ,)] " + "[, "
			    + (dataDistNum[i]) + " ,(, " + (minDataDistNum[i])
			    + " ,:, " + (minDataDistNumID[i]) + " ,) (, "
			    + (maxDataDistNum[i]) + " ,:, "
			    + (maxDataDistNumID[i]) + " ,)]}");

		    outputStream.println(indexSize + ", "
			    + (minRadius + i * step) + ", #node: [, "
			    + nodeVisited[i] + " ,(, " + minNodeVisited[i]
			    + " ,:, " + minNodeVisitedID[i] + " ,) (, "
			    + maxNodeVisited[i] + " ,:, " + maxNodeVisitedID[i]
			    + " ,)] " + "{ [, " + internalVisited[i] + " ,(, "
			    + minInternalVisited[i] + " ,:, "
			    + minInternalVisitedID[i] + " ,) (, "
			    + maxInternalVisited[i] + " ,:, "
			    + maxInternalVisitedID[i] + " ,)]  " + "[, "
			    + leafVisited[i] + " ,(, " + minLeafVisited[i]
			    + " ,:, " + minLeafVisitedID[i] + " ,) (, "
			    + maxLeafVisited[i] + " ,:, " + maxLeafVisitedID[i]
			    + " ,) ]}");

		    outputStream.println(indexSize + ", "
			    + (minRadius + i * step) + ", time: [, " + time[i]
			    + " ,(, " + minTime[i] + " ,:, " + minTimeID[i]
			    + " ,) (, " + maxTime[i] + " ,:, " + maxTimeID[i]
			    + " ,)]");

		    outputStream.println(indexSize + ", "
			    + (minRadius + i * step) + ", #result: [, "
			    + result[i] + " ,(, " + minResult[i] + " ,:, "
			    + minResultID[i] + " ,)(, " + maxResult[i]
			    + " ,:, " + maxResultID[i] + " ,)]");

		    outputStream.print(indexSize + ", "
			    + (minRadius + i * step) + ", #layer node: [ ");

		    for (int j = 0; j < layerNode[i].length; j++)
			outputStream.print("( " + j + ":, "
				+ layerNodeVisited[i][j] + " / "
				+ layerNode[i][j] + "), ");

		    outputStream.println("]");

		    outputStream.print(indexSize + ", "
			    + (minRadius + i * step)
			    + ", #layer data object: [ ");
		    for (int j = 0; j < layerData[i].length; j++)
			outputStream.print("(" + j + ":, "
				+ layerDataVisited[i][j] + " / "
				+ layerData[i][j] + "), ");

		    outputStream.println("]");

		    outputStream.print(indexSize + ", "
			    + (minRadius + i * step)
			    + ", query-center dist: [ ");
		    for (int j = 0; j < queryCenterDistance[i].length; j++) {
			outputStream.print(j + ": (,");
			for (int k = 0; k < queryCenterDistance[i][j].length; k++)
			    outputStream.print(fmt
				    .format(queryCenterDistance[i][j][k])
				    + ", ");
			outputStream.print(") ");
		    }

		    outputStream.println(" ]");
		}

		outputStream.close();

	    } catch (IOException e) {

		e.printStackTrace();
		System.out.println("\nError: Problem creating output file \""
			+ outputFile + "\". Program aborted.");
		System.exit(0);
	    }

	    System.out.println("Finished writing statistics file \""
		    + outputFile + "\" to disk.");

	} else {
	    System.out
		    .println("index size, radius, #distance calculation, #node visited, search time, #result,,#result node, #result internal node, #result leaf node, internalPruned, leafPruned, pivotDistNum, dataDistNum, pivotAsResult, ResultWithoutDist, internalWithoutDist, leafWithoutDist");
	    for (int i = 0; i < numRun; i++) {
		System.out.println(forPrint + indexSize + ", "
			+ fmt.format((minRadius + i * step)) + ", "
			+ distNum[i] + ", " + nodeVisited[i] + ", " + time[i]
			+ ", " + result[i] + ", " + resultNode[i] + ", "
			+ resultInternalNode[i] + ", " + resultLeafNode[i]
			+ ", " + internalPruned[i] + ", " + leafPruned[i]
			+ ", " + centerDistNum[i] + ", " + dataDistNum[i]
			+ ", " + pivotAsResult[i] + ", "
			+ resultWithoutDistance[i] + ", "
			+ internalWithoutDist[i] + ", " + leafWithoutDist[i]);
	    }
	} // end of else

    }

    /**
     * evaluate the index with one fixed range query radius and all the queries.
     * each query is run with the given radius, search statistics are collect,
     * and compute average and min, max values. the average, min and max values
     * are store in the corresponding data fields
     * 
     * @param radius
     *            the search radius
     */
    @SuppressWarnings("rawtypes")
    void evaluateRadius(double radius, List<IndexObject> resultList) {
	// System.out.println(radius);
	// if (radius ==2)
	// System.out.println(radius);
	final int offset = (int) Math.round((radius - minRadius) / step); // the
									  // ordered
									  // id
									  // of
	// this run among all
	// the runs.

	// initialize statistics
	distNum[offset] = 0; // distance calculation number
	minDistNum[offset] = Integer.MAX_VALUE;
	maxDistNum[offset] = Integer.MIN_VALUE;

	centerDistNum[offset] = 0;
	minCenterDistNum[offset] = Integer.MAX_VALUE;
	maxCenterDistNum[offset] = Integer.MIN_VALUE;

	dataDistNum[offset] = 0;
	minDataDistNum[offset] = Integer.MAX_VALUE;
	maxDataDistNum[offset] = Integer.MIN_VALUE;

	resultNode[offset] = 0;
	resultInternalNode[offset] = 0;
	resultLeafNode[offset] = 0;
	// pivotDistNum[offset] = 0;
	// dataDistNum[offset] = 0;
	internalPruned[offset] = 0;
	leafPruned[offset] = 0;
	pivotAsResult[offset] = 0;
	resultWithoutDistance[offset] = 0;
	internalWithoutDist[offset] = 0;
	leafWithoutDist[offset] = 0;

	nodeVisited[offset] = 0; // index node visited number
	minNodeVisited[offset] = Integer.MAX_VALUE;
	maxNodeVisited[offset] = Integer.MIN_VALUE;

	internalVisited[offset] = 0;
	minInternalVisited[offset] = Integer.MAX_VALUE;
	maxInternalVisited[offset] = Integer.MIN_VALUE;

	leafVisited[offset] = 0;
	minLeafVisited[offset] = Integer.MAX_VALUE;
	maxLeafVisited[offset] = Integer.MIN_VALUE;

	time[offset] = 0; // search time
	minTime[offset] = Double.POSITIVE_INFINITY;
	maxTime[offset] = Double.NEGATIVE_INFINITY;

	result[offset] = 0; // search result number
	minResult[offset] = Integer.MAX_VALUE;
	maxResult[offset] = Integer.MIN_VALUE;

	queryCenterDistance[offset] = new double[1][1];

	layerNode[offset] = new int[1];
	layerNodeVisited[offset] = new int[1];
	layerData[offset] = new int[1];
	layerDataVisited[offset] = new int[1];

	PrintWriter resultsFile = null;
	if (resultsFileName != null) {
	    try {
		resultsFile = new PrintWriter(new BufferedWriter(
			new FileWriter(resultsFileName)));
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	// start running each query
	List allQuery = query.getData();
	List query2 = allQuery.subList((firstQuery < 0) ? 0 : firstQuery,
		(lastQuery > allQuery.size()) ? allQuery.size() : lastQuery);
	Iterator p = query2.iterator();
	int queryCounter = -1;
	while (p.hasNext()) {
	    queryCounter++;
	    RangeQuery q = new RangeQuery((IndexObject) p.next(), radius,
		    pathLength);
	    if (resultsFile != null) {
		resultsFile.println(queryCounter + ": " + q.getQueryObject());
	    }
	    // List<IndexObject> resultList = new ArrayList<IndexObject>();
	    final double startTime = System.currentTimeMillis();
	    VPRangeCursor cursor = (VPRangeCursor) index.search(q);
	    @SuppressWarnings("unused")
	    int numResults = 0;
	    while (cursor.hasNext()) {
		IndexObject iObject = ((DoubleIndexObjectPair) cursor.next())
			.getObject();
		if (resultsFile != null) {
		    resultsFile.println(iObject);
		}
		// print each result
		resultList.add(iObject);
		numResults++;
	    }

	    final double endTime = System.currentTimeMillis();

	    // set statistics
	    final double t = (endTime - startTime) / 1000; // search time
	    time[offset] += t;
	    if (minTime[offset] > t) {
		minTime[offset] = t;
		minTimeID[offset] = queryCounter;
	    }
	    if (maxTime[offset] < t) {
		maxTime[offset] = t;
		maxTimeID[offset] = queryCounter;
	    }

	    final int r = resultList.size(); // result number
	    result[offset] += r;
	    if (minResult[offset] > r) {
		minResult[offset] = r;
		minResultID[offset] = queryCounter;
	    }
	    if (maxResult[offset] < r) {
		maxResult[offset] = r;
		maxResultID[offset] = queryCounter;
	    }

	    int[] temp1 = cursor.getDistanceCalculationNumber(); // distance
								 // calculation
								 // number
	    distNum[offset] += temp1[0];
	    if (minDistNum[offset] > temp1[0]) {
		minDistNum[offset] = temp1[0];
		minDistNumID[offset] = queryCounter;
	    }
	    if (maxDistNum[offset] < temp1[0]) {
		maxDistNum[offset] = temp1[0];
		maxDistNumID[offset] = queryCounter;
	    }

	    centerDistNum[offset] += temp1[1];
	    if (minCenterDistNum[offset] > temp1[1]) {
		minCenterDistNum[offset] = temp1[1];
		minCenterDistNumID[offset] = queryCounter;
	    }
	    if (maxCenterDistNum[offset] < temp1[1]) {
		maxCenterDistNum[offset] = temp1[1];
		maxCenterDistNumID[offset] = queryCounter;
	    }

	    int temp12 = temp1[2];
	    dataDistNum[offset] += temp12;
	    if (minDataDistNum[offset] > temp12) {
		minDataDistNum[offset] = temp12;
		minDataDistNumID[offset] = queryCounter;
	    }
	    if (maxDataDistNum[offset] < temp12) {
		maxDataDistNum[offset] = temp12;
		maxDataDistNumID[offset] = queryCounter;
	    }

	    temp1 = cursor.getNodeVisitedNumber(); // node visited number
	    nodeVisited[offset] += temp1[0];
	    if (minNodeVisited[offset] > temp1[0]) {
		minNodeVisited[offset] = temp1[0];
		minNodeVisitedID[offset] = queryCounter;
	    }
	    if (maxNodeVisited[offset] < temp1[0]) {
		maxNodeVisited[offset] = temp1[0];
		maxNodeVisitedID[offset] = queryCounter;
	    }

	    internalVisited[offset] += temp1[1];
	    if (minInternalVisited[offset] > temp1[1]) {
		minInternalVisited[offset] = temp1[1];
		minInternalVisitedID[offset] = queryCounter;
	    }
	    if (maxInternalVisited[offset] < temp1[1]) {
		maxInternalVisited[offset] = temp1[1];
		maxInternalVisitedID[offset] = queryCounter;
	    }

	    temp12 = temp1[0] - temp1[1];
	    leafVisited[offset] += temp12;
	    if (minLeafVisited[offset] > temp12) {
		minLeafVisited[offset] = temp12;
		minLeafVisitedID[offset] = queryCounter;
	    }
	    if (maxLeafVisited[offset] < temp12) {
		maxLeafVisited[offset] = temp12;
		maxLeafVisitedID[offset] = queryCounter;
	    }

	    resultNode[offset] += cursor.getResultNodeNumber();
	    resultInternalNode[offset] += cursor.getResultInternalNodeNumber();
	    resultLeafNode[offset] += cursor.getResultLeafNodeNumber();
	    pivotAsResult[offset] += cursor.getPivotAsResult();
	    resultWithoutDistance[offset] += cursor.getResultWithoutDist();
	    internalWithoutDist[offset] += cursor.getInternalWithoutDist();
	    leafWithoutDist[offset] += cursor.getLeafWithoutDist();
	    internalPruned[offset] += cursor.getInternalPruned();
	    leafPruned[offset] += cursor.getLeafPruned();
	    /*
	     * double [][] temp2 = cursor.getQueryPivotDistance();
	     * //query-center distance if ( temp2.length >
	     * queryCenterDistance[offset].length) //re-allocate memory if
	     * necessary { double [][]temp = queryCenterDistance[offset];
	     * queryCenterDistance[offset] = new double [temp2.length][]; for
	     * (int i=0; i< temp.length; i++) { if ( temp2[i].length >
	     * temp[i].length) { queryCenterDistance[offset][i] = new double [
	     * temp2[i].length ]; System.arraycopy(temp[i], 0,
	     * queryCenterDistance[offset][i], 0, temp[i].length); for (int j=
	     * temp[i].length; j< temp2[i].length; j++)
	     * queryCenterDistance[offset][i][j] = 0; } else
	     * queryCenterDistance[offset][i] = temp[i]; } for (int i=
	     * temp.length; i< temp2.length; i++) {
	     * queryCenterDistance[offset][i] = new double [ temp2[i].length ];
	     * for (int j=0; j< temp2[i].length; j++)
	     * queryCenterDistance[offset][i][j] = 0; } } for (int i=0; i<
	     * temp2.length; i++) { if ( temp2[i].length >
	     * queryCenterDistance[offset][i].length) //reallocate memory if
	     * necessary { double [] temp = queryCenterDistance[offset][i];
	     * queryCenterDistance[offset][i] = new double [ temp2[i].length ];
	     * System.arraycopy(temp, 0, queryCenterDistance[offset][i], 0,
	     * temp.length); for (int j= temp.length; j< temp2[i].length; j++)
	     * queryCenterDistance[offset][i][j] = 0; } for (int j=0; j<
	     * temp2[i].length; j++) { //System.out.println(queryCounter + ": "
	     * + queryCenterDistance.length + ": " + offset + ", " +
	     * queryCenterDistance[offset].length + ": " + temp2.length + ": " +
	     * i + ", " + queryCenterDistance[offset][i].length + ": " +
	     * temp2[i].length + ": " + j); queryCenterDistance[offset][i][j] +=
	     * temp2[i][j]; } } int[][] temp3 = cursor.getLevelNodeVisited(); //
	     * layer node visited if ( layerNode[offset].length < temp3.length)
	     * //re-allocate if necessary { int [] temp = layerNode[offset];
	     * layerNode[offset] = new int[ temp3.length ];
	     * System.arraycopy(temp, 0, layerNode[offset], 0, temp.length); for
	     * (int i= temp.length; i< temp3.length; i++) layerNode[offset][i] =
	     * 0; temp = layerNodeVisited[offset]; layerNodeVisited[offset] =
	     * new int [temp3.length]; System.arraycopy(temp, 0,
	     * layerNodeVisited[offset], 0, temp.length); for (int
	     * i=temp.length; i<temp3.length; i++) layerNodeVisited[offset][i] =
	     * 0; } for (int i=0;i<temp3.length; i++) { layerNode[offset][i] +=
	     * temp3[i][0]; layerNodeVisited[offset][i] += temp3[i][1]; } temp3
	     * = cursor.getLevelPointVisited(); // layer data visited if (
	     * layerData[offset].length < temp3.length) //re-allocate if
	     * necessary { int [] temp = layerData[offset]; layerData[offset] =
	     * new int[ temp3.length ]; System.arraycopy(temp, 0,
	     * layerData[offset], 0, temp.length); for (int i= temp.length; i<
	     * temp3.length; i++) layerData[offset][i] = 0; temp =
	     * layerDataVisited[offset]; layerDataVisited[offset] = new int
	     * [temp3.length]; System.arraycopy(temp, 0,
	     * layerDataVisited[offset], 0, temp.length); for (int
	     * i=temp.length; i<temp3.length; i++) layerDataVisited[offset][i] =
	     * 0; } for (int i=0;i<temp3.length; i++) { layerData[offset][i] +=
	     * temp3[i][0]; layerDataVisited[offset][i] += temp3[i][1]; }
	     */

	    // verify the search results
	    if (verify)
		if (!verifyResult(resultList, q)) {
		    System.out.println("Inconsistent search results! query: "
			    + queryCounter + ", radius: " + radius + " !");

		    System.exit(-1);
		}

	} // end of while for all queries

	querySize = queryCounter + 1;
	// System.out.println("query number=" + querySize);
	// compute average values for all statistics
	distNum[offset] /= querySize;
	centerDistNum[offset] /= querySize;
	dataDistNum[offset] /= querySize;

	nodeVisited[offset] /= querySize;
	internalVisited[offset] /= querySize;
	leafVisited[offset] /= querySize;

	resultNode[offset] /= querySize;
	resultInternalNode[offset] /= querySize;
	resultLeafNode[offset] /= querySize;
	internalPruned[offset] /= querySize;
	leafPruned[offset] /= querySize;
	pivotAsResult[offset] /= querySize;
	resultWithoutDistance[offset] /= querySize;
	internalWithoutDist[offset] /= querySize;
	leafWithoutDist[offset] /= querySize;

	time[offset] /= querySize;

	result[offset] /= querySize;

	for (int j = 0; j < queryCenterDistance[offset].length; j++)
	    for (int k = 0; k < queryCenterDistance[offset][j].length; k++)
		queryCenterDistance[offset][j][k] /= querySize;

	for (int j = 0; j < layerData[offset].length; j++) {
	    layerData[offset][j] /= querySize;
	    layerDataVisited[offset][j] /= querySize;
	}

	for (int j = 0; j < layerNode[offset].length; j++) {
	    layerNode[offset][j] /= querySize;
	    layerNodeVisited[offset][j] /= querySize;
	}

	if (resultsFileName != null) {
	    resultsFile.flush();
	    resultsFile.close();
	    System.out.println("results saved to: " + resultsFileName);
	}
    }

    /**
     * verify the search results of a query by a linear scan
     * 
     * @param resultList
     *            the results of query
     * @param predicate
     *            the query
     */
    boolean verifyResult(List<IndexObject> resultList, RangeQuery q) {
	if (resultList == null)
	    resultList = new ArrayList<IndexObject>(0);

	/*
	 * System.out.println("result list number:" + resultList.size());
	 * for(IndexObject o: resultList) System.out.println(o);
	 */

	// System.out.println("linear index size=" + linearIndex.size());
	Iterator<IndexObject> p = linearIndex.iterator();
	IndexObject data;
	while (p.hasNext()) {
	    data = p.next();
	    // System.out.println(data);
	    // System.out.println(resultList.get(0).getClass());
	    // System.out.println(data.getClass());
	    if (metric.getDistance(data, q.getQueryObject()) <= q.getRadius())
		if (!resultList.remove(data)) {
		    System.out
			    .println("Found: linearscan result not in index resultset: "
				    + data.toString()
				    + ", query="
				    + q.getQueryObject());
		    return false;
		}
	}

	if (resultList.size() != 0) {
	    System.out
		    .println("Found: index result not returned by linear scan.  Query ="
			    + q.getQueryObject());
	    for (IndexObject o : resultList)
		System.out.println(o);
	    return false;
	} else
	    return true;

    }

    /*
     * static public Pair loadSequenceQuery(String fileName, final int first,
     * final int last) { Metric metric =
     * Metrics.globalSequenceMetric(Metrics.mPAM250aExtendedAminoAcidsMetric);
     * DataLoader loader = new edu.utexas.GeDBIT.util.SegmentLoader(
     * edu.utexas.GeDBIT.type.Sequences.ExtendedAminoAcidsAlphabet ); //load
     * data from file List query = null; try { query = loader.loadData( new
     * java.io.BufferedReader( new java.io.FileReader(fileName)), last+1 ); }
     * catch (Exception e) { e.printStackTrace(); } query = query.subList(first,
     * last+1); //SegmentPairLoader return a list of Pairs List temp = query;
     * query = new ArrayList( temp.size() ); Iterator p = temp.iterator(); while
     * ( p.hasNext() ) query.add ( ( (Pair) p.next() ).first() ); return new
     * Pair( metric, query); } static public Pair loadVectorQuery(String
     * fileName, final int first, final int last) { Metric metric =
     * edu.utexas.GeDBIT.dist.Metrics.EuclideanDistanceMetric; DataLoader loader
     * = new edu.utexas.GeDBIT.util.DoubleVectorLoader() ; //load data from file
     * List query = null; try { query = loader.loadData( new
     * java.io.BufferedReader( new java.io.FileReader(fileName)), last+1 ); }
     * catch (Exception e) { e.printStackTrace(); } query = query.subList(first,
     * last+1); //SegmentPairLoader return a list of Pairs List temp = query;
     * query = new ArrayList( temp.size() ); Iterator p = temp.iterator(); while
     * ( p.hasNext() ) query.add ( ( (Pair) p.next() ).first() ); return new
     * Pair( metric, query); } static public Pair loadHammingQuery(String
     * fileName, final int first, final int last) { Metric metric = new
     * edu.utexas.GeDBIT.dist.SimpleHammingDistance(); DataLoader loader = new
     * edu.utexas.GeDBIT.util.LineStringDataLoader(); //load data from file List
     * query = null; try { query = loader.loadData( new java.io.BufferedReader(
     * new java.io.FileReader(fileName)), last+2 ); } catch (Exception e) {
     * e.printStackTrace(); } query = query.subList(first+1, last+2);
     * //SegmentPairLoader return a list of Pairs List temp = query; query = new
     * ArrayList( temp.size() ); Iterator p = temp.iterator(); while (
     * p.hasNext() ) query.add ( new java.math.BigInteger( (String) p.next(),2 )
     * ); return new Pair( metric, query); }
     */
}
