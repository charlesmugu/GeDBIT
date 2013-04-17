/**
 * GeDBIT.index.VPRangeCursor 2006.05.10 Copyright Information: Change Log: 2006.05.10: Created, by Rui Mao
 */

package GeDBIT.index;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import GeDBIT.dist.Metric;
import GeDBIT.parallel.GlobalIndexPrintTask;
import GeDBIT.parallel.GlobalIndexWorkTask;
import GeDBIT.parallel.Task;
import GeDBIT.parallel.WorkThread;
import GeDBIT.parallel.WorkThreadUtil;
import GeDBIT.type.DoubleIndexObjectPair;
import GeDBIT.type.IndexObject;
import GeDBIT.util.Debug;
import GeDBIT.util.ObjectIOManager;

/**
 * Implements an {@link Cursor}, for range search in vp tree
 * 
 * @author Rui Mao, Willard
 * @version 2006.05.31
 */
public class VPRangeCursor extends Cursor {
    IndexObject q;
    double r;
    int maxListLength;
    int id;

    private Logger logger;

    // statistics
    // global statistics
    int nodeVisited, internalVisited; // leafVisited; //number of index nodes
				      // visited
    int distNum, pivotDistNum, dataDistNum; // number of distance calculation,
					    // calculations with pivots, data
					    // with points
    int resultNode, resultInternalNode, resultLeafNode; // number of nodes
							// containing at least
							// one query result.
    int internalPruned, leafPruned; // number of points pruned in internal
				    // nodes, leaf nodes.

    int resultConfirmedByPathDist; // number of data points that are confirmed
				   // to be results by
				   // path distance list
    // without computing the distance to the query directly. it is a subset of
    // resultWithoutDist.
    int pointPrunedByPivotDist; // number of data points that are pruned by the
				// pivot distance

    int nodePrunedByPivotDist; // number of nodes that are pruned by the pivot
			       // distance
    int pointPrunedByPathDist; // number of data points that are pruned by path
			       // distance list

    int nodeWithoutDist; // number of nodes that are determined to be all
			 // results without
			 // computing distance directly.
    int resultWithoutDist; // number of results that are determined to be
			   // results without
			   // computing distance directly.
    int internalWithoutDist, leafWithoutDist;

    int pivotAsResult; // number of pivots that are also search results
    int pivotRowIDAsResult; // number of row IDs corresponding to those pivots
			    // being search
			    // results

    // layer statistics, computed in temp variables, and finally added to the
    // lists when values are fixed.
    ArrayList<double[]> queryPivotDistance; // distance between the query and
					    // the pivots of each level, -1
					    // means no such value
    // each element is a 1-d array, is the average value of one level, for each
    // pivot (column)
    // computed during processing the layer, and value added to this list at the
    // end of the layer

    ArrayList<Integer> levelNodeNum, levelNodeVisited; // from the perspective
						       // of last level, these
						       // are the total
						       // children node number,
    // number of children visited, and the ratio. thus, these values are
    // computed // during the processing of last
    // layer, and are added to the list at the begin // of the current layer.

    ArrayList<Integer> levelPointNum, levelPointVisited; // number of data
							 // objects of leaf
							 // nodes of each level,
    // number of data object directly compute distance on, ratio. //computed
    // during processing the layer, //and value
    // added to this list at the end of the layer

    // counters, indecies
    private int level; // the id of the current level, top-down from 0
    private int insideLevel; // the id of the node on current level, left to
			     // right from 0
    int levelNodeToVisit; // number of nodes of current layer to be visited. The
			  // value
			  // is initially levelNodeVisited set during previous
			  // layer
    boolean isLeftMost; // whether next node is the first node (left-most) of a
			// layer, i.e., start a new layer.

    // temp variable, for each layer, to be added to the ArrayLists
    double[] tempQueryPivotDistance;
    int[] childWithPivotCounter; // the ith element is the number of index nodes
				 // of the layer
				 // that has ith pivot.
    int tempLevelNode, tempLevelNodeVisited;
    int tempLevelPoint, tempLevelPointVisited;

    LinkedList<RangeQueryTask> task;

    public VPRangeCursor(RangeQuery query, ObjectIOManager oiom, Metric metric,
	    long rootAddress) {
	super(oiom, metric, rootAddress);

	task = new LinkedList<RangeQueryTask>();

	this.q = query.getQueryObject();
	this.r = query.getRadius();
	this.maxListLength = query.getMaxDistanceListSize();

	// add the first task
	task.add(new RangeQueryTask(rootAddress, new double[0]));

	if (Debug.debug) {
	    logger = Logger.getLogger("GeDBIT.index");
	}
	if (Debug.statistics) {
	    initializeStatistics();
	}

    }

    public VPRangeCursor(RangeQuery query, ObjectIOManager[] oioms,
	    Metric metric, long rootAddress) {
	super(oioms, metric, rootAddress);

	task = new LinkedList<RangeQueryTask>();

	this.q = query.getQueryObject();
	this.r = query.getRadius();
	this.id = query.getId();
	this.maxListLength = query.getMaxDistanceListSize();

	// add the first task
	task.add(new RangeQueryTask(-1, rootAddress, new double[0]));

	if (Debug.debug) {
	    logger = Logger.getLogger("GeDBIT.index");
	}
	if (Debug.statistics) {
	    initializeStatistics();
	}

    }

    /**
     * initialize the statistics, not the temp variables. this method only be
     * called once at the begining, where the temp variables will be initialized
     * each time at the begining of a new layer.
     */
    private void initializeStatistics() {
	level = -1; // before the root (0,0), the level should be -1
	insideLevel = 0;
	levelNodeToVisit = 1; // node number of root level is 1
	isLeftMost = true;

	// global statistics
	nodeVisited = 0;
	internalVisited = 0;

	resultNode = 0;
	resultInternalNode = 0;
	resultLeafNode = 0;

	distNum = 0;
	pivotDistNum = 0;

	this.resultConfirmedByPathDist = 0;
	this.pointPrunedByPathDist = 0;
	this.pivotAsResult = 0;
	this.pivotRowIDAsResult = 0;
	this.nodeWithoutDist = 0;
	this.resultWithoutDist = 0;
	this.internalWithoutDist = 0;
	this.leafWithoutDist = 0;
	this.nodePrunedByPivotDist = 0;
	this.pointPrunedByPivotDist = 0;

	this.dataDistNum = 0;
	this.internalPruned = 0;
	this.leafPruned = 0;

	// level statistics
	queryPivotDistance = new ArrayList<double[]>();
	levelNodeNum = new ArrayList<Integer>();
	levelNodeVisited = new ArrayList<Integer>();
	levelPointNum = new ArrayList<Integer>();
	levelPointVisited = new ArrayList<Integer>();

	// temp variables, these variables are supposed to be computed during
	// the processing of last layer
	tempLevelNode = 1;
	tempLevelNodeVisited = 1;
    }

    /**
     * the method to be called at the begining of each layer. Actions includes:
     * 1. add the temp varaibles to the list, which should be add at the
     * begining of a layer 2. set related counters 3. initialize temp variables
     * for the new layer
     */
    protected void layerBegining() {
	// step1. add temp variables to lists
	levelNodeNum.add(tempLevelNode);
	levelNodeVisited.add(tempLevelNodeVisited);

	// step2. set counters
	level++;
	insideLevel = 0;
	levelNodeToVisit = tempLevelNodeVisited;

	// step3. initialize temp variables
	tempQueryPivotDistance = new double[1];
	tempQueryPivotDistance[0] = 0;
	childWithPivotCounter = new int[1];
	childWithPivotCounter[0] = 0;

	tempLevelNode = 0;
	tempLevelNodeVisited = 0;

	tempLevelPoint = 0;
	tempLevelPointVisited = 0;

    }

    /**
     * to be called at the end of a layer. actions includes: 1. add temp
     * variables, which should be added to lists at the end of a layer, to lists
     * 2. set related counters
     */
    protected void layerEnd() {
	// step1. add temp variables
	for (int i = 0; i < tempQueryPivotDistance.length; i++)
	    tempQueryPivotDistance[i] /= childWithPivotCounter[i];
	queryPivotDistance.add(tempQueryPivotDistance);

	levelPointNum.add(tempLevelPoint);
	levelPointVisited.add(tempLevelPointVisited);

	// step2. set related counters
	isLeftMost = true;
    }

    /**
     * to be call at the begining of visiting a node
     */
    protected void nodeBegining() {
	if (isLeftMost) {
	    isLeftMost = false;
	    layerBegining();
	}

	nodeVisited++;
    }

    /**
     * to be called at the end of visiting a node
     */
    protected void nodeEnd() {
	insideLevel++;
	// System.out.println("level=" + level + ", layer = " + layer +
	// ", layersize=" + layerSize);

	if (insideLevel == levelNodeToVisit)
	    layerEnd();
    }

    // methods to return statistics
    /**
     * @return the height the of index tree
     */
    public int getHeight() {
	return level + 1;
    }

    /**
     * Return the number of index node visited during the search
     * 
     * @return an int array of length 2, the first element is the total number
     *         of index nodes visited during the search, the second is the
     *         number of internal nodes visited.
     */
    public int[] getNodeVisitedNumber() {
	return new int[] { nodeVisited, internalVisited };
    }

    public int getResultNodeNumber() {
	return resultNode;
    }

    public int getResultInternalNodeNumber() {
	return resultInternalNode;
    }

    public int getResultLeafNodeNumber() {
	return resultLeafNode;
    }

    public int getInternalPruned() {
	return this.internalPruned;
    }

    public int getLeafPruned() {
	return this.leafPruned;
    }

    public int getPivotDistNum() {
	return this.pivotDistNum;
    }

    public int getDistNum() {
	return this.distNum;
    }

    public int getDataDistNum() {
	return this.dataDistNum;
    }

    public int getResultWithoutDist() {
	return this.resultWithoutDist;
    }

    public int getInternalWithoutDist() {
	return this.internalWithoutDist;
    }

    public int getLeafWithoutDist() {
	return this.leafWithoutDist;
    }

    public int getPivotAsResult() {
	return this.pivotAsResult;
    }

    /**
     * return some counters
     * 
     * @return an array of counters: 0: int resultConfirmedByPathDist; //number
     *         of data points that are confirmed to be results by path distance
     *         list //without computing the distance directly. it is a subset of
     *         resultWithoutDist. 1: int pointPrunedByPivotDist; //number of
     *         data points that are pruned by the pivot distance 2: int
     *         nodePrunedByPivotDist; //number of nodes that are pruned by the
     *         pivot distance 3: int pointPrunedByPathDist; //number of data
     *         points that are pruned by path distance list 4: int
     *         nodeWithoutDist; //number of nodes that are determined to be all
     *         results without computing distance directly. 5: int
     *         resultWithoutDist; //number of results that are determined to be
     *         results without computing distance directly. 6: int
     *         pivotAsResult; //number of pivots that are also search results 7:
     *         int pivotRowIDAsResult; //number of row IDs corresponding to
     *         those pivots being search results
     */
    public int[] getCounters() {
	int result[] = new int[8];
	result[0] = this.resultConfirmedByPathDist;
	result[1] = this.pointPrunedByPathDist;
	result[2] = this.nodePrunedByPivotDist;
	result[3] = this.pointPrunedByPivotDist;
	result[4] = this.nodeWithoutDist;
	result[5] = this.resultWithoutDist;
	result[6] = this.pivotAsResult;
	result[7] = this.pivotRowIDAsResult;

	return result;
    }

    /**
     * return the number of distance calculations during the search
     * 
     * @return an int array of length 2, the first element is total number of
     *         distance calculations during the search, the second is the number
     *         of distance calculations between the query and the pivots
     */
    public int[] getDistanceCalculationNumber() {
	return new int[] { distNum, pivotDistNum, dataDistNum };
    }

    /**
     * return the average distance between the query and the pivots for each
     * layer.
     * 
     * @return a 2-d double array, average distance between the query to the
     *         pivots (column) on each level (row). the length of a row is the
     *         max number of pivots in the level.
     */
    public double[][] getQueryPivotDistance() {
	double[][] result = new double[level + 1][];
	for (int i = 0; i <= level; i++)
	    result[i] = (double[]) queryPivotDistance.get(i);

	return result;
    }

    /**
     * return the number of nodes visited in each level of the index tree.
     * 
     * @return a 2-d int array, each row is the statistics of a level, starting
     *         from level 0, the root. each row is an int array of length 2, the
     *         first is the number of index nodes of the level that belong to
     *         the parent nodes of the nodes counted; the second is the number
     *         of nodes visited in the layer,
     */
    public int[][] getLevelNodeVisited() {
	int[][] result = new int[level + 1][2];
	for (int i = 0; i <= level; i++) {
	    result[i][0] = levelNodeNum.get(i);
	    result[i][1] = levelNodeVisited.get(i);
	}

	return result;
    }

    /**
     * return the number of data objects that the query directly compute
     * distance with in the leaf nodes of each level of the index tree.
     * 
     * @return a 2-d int array, each row is the statistics of a level, starting
     *         from level 0, the root. each row is an int array of length 2, the
     *         first the total number of data objects in the leaf node visited
     *         of the level, the second is the number of data object that the
     *         query directly compute distance with. Note that the total number
     *         of index nodes of a level can not be found during the search.
     *         Also note that the first element can be 0.
     */
    public int[][] getLevelPointVisited() {
	int[][] result = new int[level + 1][2];
	for (int i = 0; i <= level; i++) {
	    result[i][0] = levelPointNum.get(i);
	    result[i][1] = levelPointVisited.get(i);
	}

	return result;
    }

    /**
     * @return a string representation of the Visitor
     */
    public String toString() {
	StringBuffer result = new StringBuffer();

	result.append("BFSProximityVisitor:\n" + "Query: metric="
		+ metric.toString() + ", center=" + q.toString() + ", radius="
		+ r + "\nOIOM: " + oiom.toString() + ", height = "
		+ getHeight() + "\n");

	int[] temp1 = getDistanceCalculationNumber();
	result.append("#Distance calculation: " + temp1[0] + " (internal: "
		+ temp1[1] + " , leaf: " + temp1[2] + " ), ");

	temp1 = getNodeVisitedNumber();
	result.append("#Node visited: " + temp1[0] + " (center: " + temp1[1]
		+ " , data object: " + temp1[2] + " )\n");

	double[][] temp2 = getQueryPivotDistance();
	int[][] temp3 = getLevelNodeVisited();
	int[][] temp4 = getLevelPointVisited();

	result.append("Layer statistics: layer id, node visited(total), data object visited(total), average query-center distances:\n");
	for (int i = 0; i < getHeight(); i++) {
	    result.append(i + ":  " + temp3[i][1] + " ( " + temp3[i][0]
		    + " ),    " + temp4[i][1] + " ( " + temp4[i][0]
		    + " ),    [");
	    for (int j = 0; j < temp2[i].length; j++)
		result.append(temp2[i][j] + ", ");
	    result.append("]\n");
	}

	return result.toString();
    }

    protected void visit(QueryTask t) {
	// TODO
	if (!(t instanceof VPRangeCursor.RangeQueryTask))
	    throw new UnsupportedOperationException(
		    "Only RangeQueryTask is supported by VPRangeCursor, not "
			    + t.getClass());

	RangeQueryTask task = (RangeQueryTask) t;

	Object node = null;
	try {
	    node = oiom.readObject(new Long(task.nodeAddress));
	} catch (Exception e) {
	    e.printStackTrace();
	}

	if (node instanceof VPInternalNode)
	    visitInternalNode((VPInternalNode) node, task.distList);
	else if (node instanceof VPLeafNode)
	    visitLeafNode((VPLeafNode) node, task.distList);
	else
	    throw new UnsupportedOperationException(
		    "only VPInternalNode or VPLeafNode is supported by VPRangerCursor, not "
			    + node.getClass());
    }

    protected void visitRoot(QueryTask t, int root) {
	// TODO
	if (!(t instanceof VPRangeCursor.RangeQueryTask))
	    throw new UnsupportedOperationException(
		    "Only RangeQueryTask is supported by VPRangeCursor, not "
			    + t.getClass());

	RangeQueryTask task = (RangeQueryTask) t;

	Object node = null;
	try {
	    node = oioms[root].readObject(new Long(task.nodeAddress));
	} catch (Exception e) {
	    e.printStackTrace();
	}

	if (node instanceof VPInternalNode)
	    visitRootInternalNode((VPInternalNode) node, task.distList);
	else
	    throw new UnsupportedOperationException(
		    "only VPInternalNode or VPLeafNode is supported by VPRangerCursor, not "
			    + node.getClass());
	distributeTasks();
    }

    boolean contains(ObjectIOManager oiom, long address, IndexObject query) {
	List<IndexObject> linearIndex = getAllPoints(oiom, address);
	for (IndexObject o : linearIndex)
	    if (this.metric.getDistance(o, query) == 0)
		return true;
	return false;
    }

    private class RangeQueryTask extends QueryTask {
	RangeQueryTask(long address, double[] distList) {
	    this(-1, address, distList);
	}

	RangeQueryTask(int childNum, long address, double[] distList) {
	    this.childNum = childNum;
	    this.nodeAddress = address;
	    this.distList = distList;
	}

	// TODO
	long nodeAddress;
	double[] distList;
	int childNum;
    }

    private void visitInternalNode(VPInternalNode node, double[] distList) {
	/*
	 * for (int i=0; i<node.numChildren(); i++) if (contains(this.oiom,
	 * node.getChildAddress(i), this.q) ) System.out.println("chid " + i +
	 * " contains the query");
	 */

	// check whether this is the first node of a layer, and set some
	// statistics.
	if (Debug.debug) // operation of statistics
	{
	    logger.finest("\n(" + level + ", " + insideLevel
		    + "): [d(q,pivot) : ");
	}
	if (Debug.statistics) // operation of statistics
	{
	    nodeBegining();
	    internalVisited++;
	}

	// ArrayList toSearch = new ArrayList(); //the children to further
	// search

	// calculate distances from the query to all pivots
	final int numPivot = node.numPivots();

	if (Debug.statistics) // operation of statistics
	{
	    // calculate statistics: queryCenterDistance
	    if (tempQueryPivotDistance.length < numPivot) // check whether more
							  // centers/vantage
							  // points emerge.
	    {
		double[] temp1 = tempQueryPivotDistance;
		int[] temp2 = childWithPivotCounter;

		tempQueryPivotDistance = new double[numPivot];
		childWithPivotCounter = new int[numPivot];

		System.arraycopy(temp1, 0, tempQueryPivotDistance, 0,
			temp1.length);
		System.arraycopy(temp2, 0, childWithPivotCounter, 0,
			temp2.length);
		for (int i = temp1.length; i < numPivot; i++) {
		    tempQueryPivotDistance[i] = 0;
		    childWithPivotCounter[i] = 0;
		}
	    }

	    // set the statistics
	    for (int i = 0; i < numPivot; i++)
		childWithPivotCounter[i]++;

	    pivotDistNum += numPivot;
	    distNum += numPivot;
	}

	// search step1: compute distances between pivot and query
	double queryPivotDistance[] = new double[numPivot];

	// add more distance to the distance list if necessary
	int oldDistListLength = distList.length;
	if (distList.length < this.maxListLength) {
	    double[] temp = distList;
	    distList = new double[((oldDistListLength + numPivot) <= this.maxListLength) ? (oldDistListLength + numPivot)
		    : this.maxListLength];
	    System.arraycopy(temp, 0, distList, 0, oldDistListLength);
	}

	boolean gotResult = false;

	for (int pivot = 0; pivot < numPivot; ++pivot) {
	    queryPivotDistance[pivot] = metric.getDistance(q,
		    node.getPivot(pivot));

	    if (Debug.debug) // operation of statistics
	    {
		logger.finest("Checking pivot " + pivot + ",  d(query, pivot["
			+ pivot + "] ) =" + queryPivotDistance);
	    }
	    if (Debug.statistics) // operation of statistics
	    {
		childWithPivotCounter[pivot]++;
		tempQueryPivotDistance[pivot] += queryPivotDistance[pivot];
	    }

	    // return pivot as query result if it is satisfies the range query
	    if (queryPivotDistance[pivot] <= r) {
		this.result.add(new DoubleIndexObjectPair(
			queryPivotDistance[pivot], node.getPivot(pivot)));

		// statistics, number of (internal) nodes containing at least
		// one query result
		if (!gotResult) {
		    gotResult = true;
		    this.resultNode++;
		    this.resultInternalNode++;
		}
		if (Debug.statistics) // operation of statistics
		{
		    this.pivotAsResult++;
		}

	    } else {
		if (Debug.debug) // operation of statistics
		{
		    logger.finest(", not satisfying the query.");
		}
	    }

	    // add query pivot distance to list if list is not long enough
	    if (oldDistListLength + pivot < distList.length)
		distList[oldDistListLength + pivot] = queryPivotDistance[pivot];

	}

	// hack: if node has cght partition, transform distances to d1+d2 an
	// d1-d2
	if (node.GHTDegree < 0) {
	    // for now, must be only two pivots
	    if (numPivot != 2)
		throw new IllegalArgumentException(
			"for CGHT partition, there should be only two pivots!");

	    // transformation to d1+d2, d1-d2
	    queryPivotDistance[0] = queryPivotDistance[0]
		    + queryPivotDistance[1];
	    queryPivotDistance[1] = queryPivotDistance[0]
		    - queryPivotDistance[1] * 2;
	}

	// search step 2: check each child node.
	final int numChild = node.numChildren();

	if (Debug.debug) // operation of statistics
	{
	    logger.finest("], children : ");
	}
	if (Debug.statistics) // operation of statistics
	{
	    tempLevelNode += numChild;
	}

	// check range for each child and prune
	if (node.GHTDegree != -1) {
	    for (int i = 0; i < numChild; ++i) {
		boolean done = false;
		double[][] range = node.getChildPredicate(i);

		if (Debug.debug) // operation of statistics
		{
		    logger.finest("[" + i + ":");
		}

		for (int j = 0; j < numPivot && !done; ++j) {
		    // 1st rule: if upper(child,p) + d(p,q) <= r, then all
		    // points of child are results.
		    // for cght, something similar can be done. for now, it is
		    // omitted for simplicity
		    if (!done && (node.GHTDegree >= 0)
			    && (range[1][j] + queryPivotDistance[j] <= r)) {
			done = true;
			List<IndexObject> allResult = getAllPoints(this.oiom,
				node.getChildAddress(i));
			resultNode += getNodeNumber(this.oiom,
				node.getChildAddress(i));
			resultInternalNode += getInternalNodeNumber(this.oiom,
				node.getChildAddress(i));
			resultLeafNode += getLeafNodeNumber(this.oiom,
				node.getChildAddress(i));

			if (Debug.statistics) // operation of statistics
			{
			    this.nodeWithoutDist++;
			    int temp = allResult.size();
			    this.resultWithoutDist += temp;
			    this.internalWithoutDist += temp;
			}
			if (Debug.debug) // operation of statistics
			{
			    logger.finest("(" + queryPivotDistance[j] + "+"
				    + range[1][j] + ">" + r
				    + ") : all are results], ");
			}

			for (IndexObject o : allResult)
			    result.add(new DoubleIndexObjectPair(-1, o));
		    }

		    // 2nd rule:
		    // for cght, should use 2r
		    if (!done
			    && ((queryPivotDistance[j]
				    + ((node.GHTDegree < 0) ? 2 * r : r) < range[0][j]) || (queryPivotDistance[j]
				    - ((node.GHTDegree < 0) ? 2 * r : r) > range[1][j]))) {
			done = true;

			if (Debug.statistics) // operation of statistics
			{
			    this.nodePrunedByPivotDist++;
			    int temp = getAllPoints(this.oiom,
				    node.getChildAddress(i)).size();
			    this.pointPrunedByPivotDist += temp;
			    this.internalPruned += temp;
			}
			if (Debug.debug) // operation of statistics
			{
			    logger.finest("(" + queryPivotDistance[j] + "+" + r
				    + "<" + range[0][j] + ") : pruned], ");
			}
		    }
		}

		// add to task list to be searched if the child has not been
		// pruned
		if (!done) {
		    // TODO
		    if (oldDistListLength < distList.length)
			this.task.add(new RangeQueryTask(node
				.getChildAddress(i), (double[]) distList
				.clone()));
		    else
			task.add(new RangeQueryTask(node.getChildAddress(i),
				distList));

		    if (Debug.debug) // operation of statistics
		    {
			logger.finest("to search], ");
		    }
		    if (Debug.statistics) // operation of statistics
		    {
			tempLevelNodeVisited += 1;
		    }
		}

	    }
	} else // ght search
	{
	    // prune right: d1 > d2, can be pruned if d(q,p1)+2r <= d(q,p2)
	    if (queryPivotDistance[1] <= -2 * r) {
		if (Debug.statistics) // operation of statistics
		{
		    this.nodePrunedByPivotDist++;
		    int temp = getAllPoints(this.oiom, node.getChildAddress(1))
			    .size();
		    this.pointPrunedByPivotDist += temp;
		    this.internalPruned += temp;
		}
		if (Debug.debug) // operation of statistics
		{
		    logger.finest("(" + queryPivotDistance[1] + "<= -2*" + r
			    + ") : pruned], ");
		}

	    } else {
		// TODO
		if (oldDistListLength < distList.length)
		    this.task.add(new RangeQueryTask(node.getChildAddress(1),
			    (double[]) distList.clone()));
		else
		    task.add(new RangeQueryTask(node.getChildAddress(1),
			    distList));

		if (Debug.debug) // operation of statistics
		{
		    logger.finest("to search], ");
		}
		if (Debug.statistics) // operation of statistics
		{
		    tempLevelNodeVisited += 1;
		}

	    }

	    // prune left: d1 <= d2, can be pruned if d(q,p1) > d(q,p2) + 2r
	    if (queryPivotDistance[1] > 2 * r) {
		if (Debug.statistics) // operation of statistics
		{
		    this.nodePrunedByPivotDist++;
		    int temp = getAllPoints(this.oiom, node.getChildAddress(0))
			    .size();
		    this.pointPrunedByPivotDist += temp;
		    this.internalPruned += temp;
		}
		if (Debug.debug) // operation of statistics
		{
		    logger.finest("(" + queryPivotDistance[0] + "> 2*" + r
			    + ") : pruned], ");
		}

	    } else {
		// TODO
		if (oldDistListLength < distList.length)
		    this.task.add(new RangeQueryTask(node.getChildAddress(0),
			    (double[]) distList.clone()));
		else
		    task.add(new RangeQueryTask(node.getChildAddress(0),
			    distList));

		if (Debug.debug) // operation of statistics
		{
		    logger.finest("to search], ");
		}
		if (Debug.statistics) // operation of statistics
		{
		    tempLevelNodeVisited += 1;
		}

	    }
	}

	if (Debug.debug) // operation of statistics
	{
	    logger.finest("");
	}
	if (Debug.statistics) // operation of statistics
	{
	    // check whether this is the last node of the layer, and set
	    // statistics
	    nodeEnd();
	}

    }

    private void visitRootInternalNode(VPInternalNode node, double[] distList) {
	// calculate distances from the query to all pivots
	final int numPivot = node.numPivots();
	// search step1: compute distances between pivot and query
	double queryPivotDistance[] = new double[numPivot];

	// add more distance to the distance list if necessary
	int oldDistListLength = distList.length;
	if (distList.length < this.maxListLength) {
	    double[] temp = distList;
	    distList = new double[((oldDistListLength + numPivot) <= this.maxListLength) ? (oldDistListLength + numPivot)
		    : this.maxListLength];
	    System.arraycopy(temp, 0, distList, 0, oldDistListLength);
	}

	boolean gotResult = false;

	for (int pivot = 0; pivot < numPivot; ++pivot) {
	    queryPivotDistance[pivot] = metric.getDistance(q,
		    node.getPivot(pivot));

	    // return pivot as query result if it is satisfies the range query
	    if (queryPivotDistance[pivot] <= r) {
		this.result.add(new DoubleIndexObjectPair(
			queryPivotDistance[pivot], node.getPivot(pivot)));

		// statistics, number of (internal) nodes containing at least
		// one query result
		if (!gotResult) {
		    gotResult = true;
		    this.resultNode++;
		    this.resultInternalNode++;
		}
	    }

	    // add query pivot distance to list if list is not long enough
	    if (oldDistListLength + pivot < distList.length)
		distList[oldDistListLength + pivot] = queryPivotDistance[pivot];

	}

	if (node.GHTDegree < 0) {
	    // for now, must be only two pivots
	    if (numPivot != 2)
		throw new IllegalArgumentException(
			"for CGHT partition, there should be only two pivots!");

	    // transformation to d1+d2, d1-d2
	    queryPivotDistance[0] = queryPivotDistance[0]
		    + queryPivotDistance[1];
	    queryPivotDistance[1] = queryPivotDistance[0]
		    - queryPivotDistance[1] * 2;
	}

	// search step 2: check each child node.
	final int numChild = node.numChildren();

	// check range for each child and prune
	if (node.GHTDegree != -1) {
	    for (int i = 0; i < numChild; ++i) {
		boolean done = false;
		double[][] range = node.getChildPredicate(i);

		for (int j = 0; j < numPivot && !done; ++j) {
		    // 1st rule: if upper(child,p) + d(p,q) <= r, then all
		    // points of child are results.
		    // for cght, something similar can be done. for now, it is
		    // omitted for simplicity
		    if (!done && (node.GHTDegree >= 0)
			    && (range[1][j] + queryPivotDistance[j] <= r)) {
			done = true;
			List<IndexObject> allResult = getAllPoints(
				this.oioms[i + 1], node.getChildAddress(i));

			for (IndexObject o : allResult)
			    this.result.add(new DoubleIndexObjectPair(-1, o));
		    }

		    // 2nd rule:
		    // for cght, should use 2r
		    if (!done
			    && ((queryPivotDistance[j]
				    + ((node.GHTDegree < 0) ? 2 * r : r) < range[0][j]) || (queryPivotDistance[j]
				    - ((node.GHTDegree < 0) ? 2 * r : r) > range[1][j]))) {
			done = true;
		    }
		}

		// add to task list to be searched if the child has not been
		// pruned
		if (!done) {
		    if (oldDistListLength < distList.length)
			this.task.add(new RangeQueryTask(i, node
				.getChildAddress(i), (double[]) distList
				.clone()));
		    else
			this.task.add(new RangeQueryTask(i, node
				.getChildAddress(i), distList));
		}

	    }
	} else // ght search
	{
	    // prune right: d1 > d2, can be pruned if d(q,p1)+2r <= d(q,p2)
	    if (queryPivotDistance[1] > -2 * r) {
		if (oldDistListLength < distList.length)
		    this.task.add(new RangeQueryTask(1,
			    node.getChildAddress(1), (double[]) distList
				    .clone()));
		else
		    this.task.add(new RangeQueryTask(1,
			    node.getChildAddress(1), distList));
	    }

	    // prune left: d1 <= d2, can be pruned if d(q,p1) > d(q,p2) + 2r
	    if (queryPivotDistance[1] <= 2 * r) {
		if (oldDistListLength < distList.length)
		    this.task.add(new RangeQueryTask(0,
			    node.getChildAddress(0), (double[]) distList
				    .clone()));
		else
		    this.task.add(new RangeQueryTask(0,
			    node.getChildAddress(0), distList));
	    }
	}
    }

    private void visitLeafNode(VPLeafNode node, double[] distList) {
	boolean gotResult = false;
	if (Debug.debug) // operation of statistics
	{
	    logger.finest("entering a vp leaf node (" + level + ", "
		    + insideLevel + ")\n" + node);
	}
	if (Debug.statistics) // operation of statistics
	{
	    // check whether this is the first node of a layer, and set some
	    // statistics.
	    nodeBegining();

	    // set statistics
	    tempLevelPoint += node.size();
	    // final int size = node.size();

	    if (tempQueryPivotDistance.length < node.numPivots()) // check
								  // whether
								  // more
								  // centers/vantage
								  // points
								  // emerge.
	    {
		double[] temp1 = tempQueryPivotDistance;
		int[] temp2 = childWithPivotCounter;

		tempQueryPivotDistance = new double[node.numPivots()];
		childWithPivotCounter = new int[node.numPivots()];

		System.arraycopy(temp1, 0, tempQueryPivotDistance, 0,
			temp1.length);
		System.arraycopy(temp2, 0, childWithPivotCounter, 0,
			temp2.length);
		for (int i = temp1.length; i < node.numPivots(); i++) {
		    tempQueryPivotDistance[i] = 0;
		    childWithPivotCounter[i] = 0;
		}
	    }
	}

	final int distinctSize = node.numChildren();

	// check the pivots
	double[] queryPivotDistance = new double[node.numPivots()];

	if (Debug.statistics) // operation of statistics
	{
	    pivotDistNum += node.numPivots();
	    distNum += node.numPivots();

	}

	for (int pivot = 0; pivot < node.numPivots(); ++pivot) {
	    queryPivotDistance[pivot] = metric.getDistance(q,
		    node.getPivot(pivot));

	    if (Debug.statistics) // operation of statistics
	    {
		childWithPivotCounter[pivot]++;
		tempQueryPivotDistance[pivot] += queryPivotDistance[pivot];
	    }
	    if (Debug.debug) // operation of statistics
	    {
		logger.finest("Checking pivot " + pivot + ",  d(query, pivot["
			+ pivot + "] ) =" + queryPivotDistance);
	    }

	    // return pivot as query result if it is satisfies the range query
	    if (queryPivotDistance[pivot] <= r) {
		gotResult = true;
		this.result.add(new DoubleIndexObjectPair(
			queryPivotDistance[pivot], node.getPivot(pivot)));
		if (Debug.statistics) // operation of statistics
		{
		    pivotAsResult++;
		}

	    } else {
		if (Debug.debug) // operation of statistics
		{
		    logger.finest(", not satisfying the query.");
		}
	    }

	}

	// int doneNum = 0; //number of distinct data points that are already
	// pruned.

	for (int child = 0; child < distinctSize; child++) {
	    boolean done = false;
	    // 1. try to prune by the path distances
	    double[] pathDist = node.getDataPointPathDistance(child);

	    for (int pivot = 0; pivot < distList.length
		    && pivot < pathDist.length; pivot++) {
		// if | d(p,c) - d(p,q) | > r, then c can be pruned.
		if (Math.abs(pathDist[pivot] - distList[pivot]) > r) {
		    if (Debug.statistics) // operation of statistics
		    {
			this.pointPrunedByPathDist++;
		    }
		    if (Debug.debug) // operation of statistics
		    {
			logger.finest("child: " + child + ", path distance: "
				+ pivot + ": | " + pathDist[pivot] + " - "
				+ distList[pivot] + " | > " + r + ", pruned!");
		    }

		    done = true;
		    break;
		}

		// if d(p,c) + d(p,q) <= r, then c is a result;
		if (pathDist[pivot] + distList[pivot] <= r) {
		    if (Debug.statistics) // operation of statistics
		    {
			this.resultConfirmedByPathDist++;
			this.resultWithoutDist++;
			this.leafWithoutDist++;
		    }
		    if (Debug.debug) // operation of statistics
		    {
			logger.finest("child: " + child + ", path distance: "
				+ pivot + ": " + pathDist[pivot] + " + "
				+ distList[pivot] + " <= " + r
				+ ", is a result!");
		    }

		    done = true;

		    gotResult = true;

		    this.result.add(new DoubleIndexObjectPair(-1, node
			    .getChild(child)));

		    break;
		}

	    }

	    if (done)
		continue;

	    // 2, try to prune by each point's distance to the node pivot, check
	    // in the order of pivot
	    // compute distance with each pivot, then try to prune
	    for (int pivot = 0; pivot < node.numPivots(); pivot++) {
		double[] dataPivotDistance = node
			.getDataPointPivotDistance(child);

		// if | d(p,c) - d(p,q) | > r, then c can be pruned.
		if (Math.abs(dataPivotDistance[pivot]
			- queryPivotDistance[pivot]) > r) {
		    if (Debug.statistics) // operation of statistics
		    {
			this.pointPrunedByPivotDist++;
			this.leafPruned++;
		    }
		    if (Debug.debug) // operation of statistics
		    {
			logger.finest("child: " + child + ", pivot distance: "
				+ pivot + ": | " + dataPivotDistance[pivot]
				+ " - " + queryPivotDistance[pivot] + " | > "
				+ r + ", pruned!");
		    }

		    done = true;
		    break;
		}

		// if d(p,c) + d(p,q) <= r, then c is a result;
		if (dataPivotDistance[pivot] + queryPivotDistance[pivot] <= r) {
		    if (Debug.statistics) // operation of statistics
		    {
			this.resultWithoutDist++;
			this.leafWithoutDist++;
		    }
		    if (Debug.debug) // operation of statistics
		    {
			logger.finest("child: " + child + ", pivot distance: "
				+ pivot + ": " + dataPivotDistance[pivot]
				+ " + " + queryPivotDistance[pivot] + " <= "
				+ r + ", is a result!");
		    }

		    done = true;

		    gotResult = true;

		    this.result.add(new DoubleIndexObjectPair(-1, node
			    .getChild(child)));

		    break;
		}
	    }

	    if (done)
		continue;

	    // 3 the data can not be pruned or sure to be a result, so, compute
	    // distance directly.
	    double distance = metric.getDistance(q, node.getChild(child));

	    if (Debug.statistics) // operation of statistics
	    {
		distNum++;
		this.dataDistNum++;
		tempLevelPointVisited++;
	    }

	    if (distance <= r) {
		if (Debug.debug) // operation of statistics
		{
		    logger.finest("Point: " + child + ":  " + distance + " <= "
			    + r + " ), is a result. ");
		}

		gotResult = true;

		this.result.add(new DoubleIndexObjectPair(distance, node
			.getChild(child)));

	    } else {
		if (Debug.debug) // operation of statistics
		{
		    logger.finest("Point: " + child + ": " + distance + " > "
			    + r + " ), is not a result.");
		}
	    }

	}

	if (gotResult) {
	    resultNode++;
	    resultLeafNode++;
	}

	if (Debug.statistics) // operation of statistics
	{
	    // check whether this is the last node of a layer, and set
	    // statistics
	    nodeEnd();
	}

    }

    public List<IndexObject> searchRangeQuery(double[] distList) {
	// add the first task
	this.task.clear();
	this.task.add(new RangeQueryTask(this.rootAddress, distList));

	List<IndexObject> resultList = new ArrayList<IndexObject>();
	while (this.hasNext()) {
	    IndexObject iobject = ((DoubleIndexObjectPair) this.next())
		    .getObject();
	    resultList.add(iobject);
	}
	return resultList;
    }

    void continueSearch() {
	while ((task.size() > 0) && (result.size() == 0)) {
	    visit((QueryTask) task.remove());
	}
    }

    // start from root node
    public void searchResults() {
	visitRoot((QueryTask) task.removeFirst(), 0);
    }

    // distribute task from tasklist to subtrees
    void distributeTasks() {
	Map<Integer, Runnable> threadspool = WorkThreadUtil.getThreadsPool();

	CountDownLatch latch = null;
	RangeQueryTask queryTask = null;
	StringBuffer forprint = new StringBuffer("");
	if (WorkThreadUtil.isWaitEachQueryFinished()) {
	    int numLatch;
	    if (this.task.isEmpty()) {
		numLatch = this.result.isEmpty() ? 0 : 1;
	    } else {
		numLatch = this.task.size();
	    }
	    latch = new CountDownLatch(numLatch);
	}
	if (!this.result.isEmpty()) {
	    while (!this.result.isEmpty()) {
		DoubleIndexObjectPair pair = this.result.removeFirst();
		forprint.append(pair.getObject());
	    }
	    if (this.task.isEmpty()) {
		WorkThread thread = (WorkThread) threadspool.get(0);
		Task newTask = new GlobalIndexPrintTask(0, forprint.toString(),
			latch);
		LinkedList<Task> queue = thread.getQueue();
		synchronized (queue) {
		    queue.addLast(newTask);
		    queue.notify();
		}
	    } else {
		queryTask = this.task.removeFirst();
		WorkThread thread = (WorkThread) threadspool
			.get(queryTask.childNum % threadspool.size());
		Task newTask = new GlobalIndexWorkTask(thread.getThreadID(),
			this.metric, queryTask.nodeAddress, queryTask.distList,
			this.id, this.r, forprint.toString(), latch);
		LinkedList<Task> queue = thread.getQueue();
		synchronized (queue) {
		    queue.addLast(newTask);
		    queue.notify();
		}
	    }
	}

	while (!this.task.isEmpty()) {
	    queryTask = this.task.removeFirst();
	    WorkThread thread = (WorkThread) threadspool.get(queryTask.childNum
		    % threadspool.size());
	    Task newTask = new GlobalIndexWorkTask(thread.getThreadID(),
		    this.metric, queryTask.nodeAddress, queryTask.distList,
		    this.id, this.r, "", latch);
	    LinkedList<Task> queue = thread.getQueue();
	    synchronized (queue) {
		queue.addLast(newTask);
		queue.notify();
	    }
	}

	if (WorkThreadUtil.isWaitEachQueryFinished()) {
	    try {
		latch.await();
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}
    }

}
