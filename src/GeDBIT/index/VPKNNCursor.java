/**
 * GeDBIT.index.VPKNNCursor
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.07.09: created by Weijia Xu 
 */

package GeDBIT.index;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import GeDBIT.type.DoubleIndexObjectPair;
import GeDBIT.util.ObjectIOManager;
import GeDBIT.util.Debug;
import GeDBIT.type.IndexObject;
import GeDBIT.dist.Metric;

/**
 * Implements an {@link Cursor}, for KNN search in vp tree
 * @author Weijia Xu
 * @version 2006.07.09
 */
public class VPKNNCursor extends Cursor
{
    IndexObject         q;
    double              r;
    int                 maxListLength;
    private Logger  logger;
    LinkedList<KNNQueryTask> task;
    
    // statistics
    // global statistics
    int                 nodeVisited, internalVisited; // leafVisited; //number of index nodes visited
    int                 distNum, pivotDistNum;        // dataDistNum; //number of distance calculation, calculations
                                                        // with pivots

    int                 resultConfirmedByPathDist;   // number of data points that are confirmed to be results by
                                                        // path distance list
    // without computing the distance to the query directly. it is a subset of resultWithoutDist.
    int                 pointPrunedByPivotDist;      // number of data points that are pruned by the pivot distance

    int                 nodePrunedByPivotDist;       // number of nodes that are pruned by the pivot distance
    int                 pointPrunedByPathDist;       // number of data points that are pruned by path distance list

    int                 nodeWithoutDist;             // number of nodes that are determined to be all results without
                                                        // computing distance directly.
    int                 resultWithoutDist;           // number of results that are determined to be results without
                                                        // computing distance directly.

    int                 pivotAsResult;               // number of pivots that are also search results
    int                 pivotRowIDAsResult;          // number of row IDs corresponding to those pivots being search
                                                        // results

    // layer statistics, computed in temp variables, and finally added to the lists when values are fixed.
    ArrayList<double[]> queryPivotDistance;          // distance between the query and the pivots of each level, -1
                                                        // means no such value
    // each element is a 1-d array, is the average value of one level, for each pivot (column)
    // computed during processing the layer, and value added to this list at the end of the layer

    ArrayList<Integer>  levelNodeNum, levelNodeVisited; // from the perspective of last level, these are the total
                                                        // children node number,
    // number of children visited, and the ratio. thus, these values are computed // during the processing of last
    // layer, and are added to the list at the begin // of the current layer.

    ArrayList<Integer>  levelPointNum, levelPointVisited; // number of data objects of leaf nodes of each level,
    // number of data object directly compute distance on, ratio. //computed during processing the layer, //and value
    // added to this list at the end of the layer

    // counters, indecies
    private int         level;                           // the id of the current level, top-down from 0
    private int         insideLevel;                     // the id of the node on current level, left to right from 0
    int                 levelNodeToVisit;                // number of nodes of current layer to be visited. The value
                                                            // is initially levelNodeVisited set during previous layer
    boolean             isLeftMost;                      // whether next node is the first node (left-most) of a
                                                            // layer, i.e., start a new layer.

    // temp variable, for each layer, to be added to the ArrayLists
    double[]            tempQueryPivotDistance;
    int[]               childWithPivotCounter;           // the ith element is the number of index nodes of the layer
                                                            // that has ith pivot.
    int                 tempLevelNode, tempLevelNodeVisited;
    int                 tempLevelPoint, tempLevelPointVisited;

    int search_policy;
    int guranteeNN;
    int queue_size;
    double targetRadius, cachedRadius;
    LinkedList<DoubleIndexObjectPair> cachedResults;
    int result_needed, nnFinished;
    boolean foundResult;
    private final double EPSILON=0.001;
    
    public VPKNNCursor(KNNQuery query, ObjectIOManager oiom, Metric metric, long rootAddress)
    {
        super(oiom, metric, rootAddress);

        task = new LinkedList<KNNQueryTask>();
        
        this.q = query.getQueryObject();
        this.r = query.getRadius();
        this.maxListLength = query.getMaxDistanceListSize();
	this.search_policy= query.getSearchPolicy();
	if (search_policy==KNNQuery.KNNSEARCH || search_policy==KNNQuery.RADIUSLIMITED)
		this.guranteeNN=Integer.MAX_VALUE;
	else
		this.guranteeNN=search_policy;
	this.queue_size = query.getK();
	this.targetRadius=0;
	this.cachedRadius = Double.MAX_VALUE;
	cachedResults = new LinkedList<DoubleIndexObjectPair>();
	
	this.result_needed=query.getK();	
	this.nnFinished=0;
	this.foundResult=false;
        // add the first task
        task.add(new KNNQueryTask(rootAddress, new double[0], 0));

        if (Debug.debug) {
            logger = Logger.getLogger("GeDBIT.index");
	    try
	    {
		    logger.addHandler(new FileHandler("knn.log"));
		    logger.setLevel(Level.FINEST);
	    } catch (Exception e)
	    {
		    System.out.println("Exception when creating logging file ");
		    e.printStackTrace();
		    System.exit(-1);
	    }
        }
        if (Debug.statistics) {
            initializeStatistics();
        }

    }

    /**
     * initialize the statistics, not the temp variables. this method only be called once at the begining, where the
     * temp variables will be initialized each time at the begining of a new layer.
     */
    private void initializeStatistics()
    {
        level = -1; // before the root (0,0), the level should be -1
        insideLevel = 0;
        levelNodeToVisit = 1; // node number of root level is 1
        isLeftMost = true;

        // global statistics
        nodeVisited = 0;
        internalVisited = 0;

        distNum = 0;
        pivotDistNum = 0;

        this.resultConfirmedByPathDist = 0;
        this.pointPrunedByPathDist = 0;
        this.pivotAsResult = 0;
        this.pivotRowIDAsResult = 0;
        this.nodeWithoutDist = 0;
        this.resultWithoutDist = 0;
        this.nodePrunedByPivotDist = 0;
        this.pointPrunedByPivotDist = 0;

        // level statistics
        queryPivotDistance = new ArrayList<double[]>();
        levelNodeNum = new ArrayList<Integer>();
        levelNodeVisited = new ArrayList<Integer>();
        levelPointNum = new ArrayList<Integer>();
        levelPointVisited = new ArrayList<Integer>();

        // temp variables, these variables are supposed to be computed during the processing of last layer
        tempLevelNode = 1;
        tempLevelNodeVisited = 1;
    }

    /**
     * the method to be called at the begining of each layer. Actions includes: 1. add the temp varaibles to the list,
     * which should be add at the begining of a layer 2. set related counters 3. initialize temp variables for the new
     * layer
     */
    protected void layerBegining()
    {
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
     * to be called at the end of a layer. actions includes: 1. add temp variables, which should be added to lists at
     * the end of a layer, to lists 2. set related counters
     */
    protected void layerEnd()
    {
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
    protected void nodeBegining()
    {
        if (isLeftMost)
        {
            isLeftMost = false;
            layerBegining();
        }

        nodeVisited++;
    }

    /**
     * to be called at the end of visiting a node
     */
    protected void nodeEnd()
    {
        insideLevel++;
        // System.out.println("level=" + level + ", layer = " + layer + ", layersize=" + layerSize);

        if (insideLevel == levelNodeToVisit)
            layerEnd();
    }


    // methods to return statistics
    /**
     * @return the height the of index tree
     */
    public int getHeight()
    {
        return level + 1;
    }

    /**
     * Return the number of index node visited during the search
     * @return an int array of length 2, the first element is the total number of index nodes visited during the search,
     *         the second is the number of internal nodes visited.
     */
    public int[] getNodeVisitedNumber()
    {
        return new int[]{nodeVisited, internalVisited};
    }

    /**
     * return some counters
     * @return an array of counters: 0: int resultConfirmedByPathDist; //number of data points that are confirmed to be
     *         results by path distance list //without computing the distance directly. it is a subset of
     *         resultWithoutDist. 1: int pointPrunedByPivotDist; //number of data points that are pruned by the pivot
     *         distance 2: int nodePrunedByPivotDist; //number of nodes that are pruned by the pivot distance 3: int
     *         pointPrunedByPathDist; //number of data points that are pruned by path distance list 4: int
     *         nodeWithoutDist; //number of nodes that are determined to be all results without computing distance
     *         directly. 5: int resultWithoutDist; //number of results that are determined to be results without
     *         computing distance directly. 6: int pivotAsResult; //number of pivots that are also search results 7: int
     *         pivotRowIDAsResult; //number of row IDs corresponding to those pivots being search results
     */
    public int[] getCounters()
    {
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
     * @return an int array of length 2, the first element is total number of distance calculations during the search,
     *         the second is the number of distance calculations between the query and the pivots
     */
    public int[] getDistanceCalculationNumber()
    {
        return new int[]{distNum, pivotDistNum};
    }

    /**
     * return the average distance between the query and the pivots for each layer.
     * @return a 2-d double array, average distance between the query to the pivots (column) on each level (row). the
     *         length of a row is the max number of pivots in the level.
     */
    public double[][] getQueryPivotDistance()
    {
        double[][] result = new double[level + 1][];
        for (int i = 0; i <= level; i++)
            result[i] = (double[]) queryPivotDistance.get(i);

        return result;
    }


    /**
     * return the number of nodes visited in each level of the index tree.
     * @return a 2-d int array, each row is the statistics of a level, starting from level 0, the root. each row is an
     *         int array of length 2, the first is the number of index nodes of the level that belong to the parent
     *         nodes of the nodes counted; the second is the number of nodes visited in the layer,
     */
    public int[][] getLevelNodeVisited()
    {
        int[][] result = new int[level + 1][2];
        for (int i = 0; i <= level; i++)
        {
            result[i][0] = levelNodeNum.get(i);
            result[i][1] = levelNodeVisited.get(i);
        }

        return result;
    }

    /**
     * return the number of data objects that the query directly compute distance with in the leaf nodes of each level
     * of the index tree.
     * @return a 2-d int array, each row is the statistics of a level, starting from level 0, the root. each row is an
     *         int array of length 2, the first the total number of data objects in the leaf node visited of the level,
     *         the second is the number of data object that the query directly compute distance with. Note that the
     *         total number of index nodes of a level can not be found during the search. Also note that the first
     *         element can be 0.
     */
    public int[][] getLevelPointVisited()
    {
        int[][] result = new int[level + 1][2];
        for (int i = 0; i <= level; i++)
        {
            result[i][0] = levelPointNum.get(i);
            result[i][1] = levelPointVisited.get(i);
        }

        return result;
    }

    /**
     * @return a string representation of the Visitor
     */
    public String toString()
    {
        StringBuffer result = new StringBuffer();

        result.append("BFSProximityVisitor:\n" + "Query: metric=" + metric.toString() + ", center=" + q.toString()
                + ", radius=" + r + "\nOIOM: " + oiom.toString() + ", height = " + getHeight() + "\n");

        int[] temp1 = getDistanceCalculationNumber();
        result.append("#Distance calculation: " + temp1[0] + " (internal: " + temp1[1] + " , leaf: " + temp1[2]
                + " ), ");

        temp1 = getNodeVisitedNumber();
        result.append("#Node visited: " + temp1[0] + " (center: " + temp1[1] + " , data object: " + temp1[2] + " )\n");

        double[][] temp2 = getQueryPivotDistance();
        int[][] temp3 = getLevelNodeVisited();
        int[][] temp4 = getLevelPointVisited();

        result
                .append("Layer statistics: layer id, node visited(total), data object visited(total), average query-center distances:\n");
        for (int i = 0; i < getHeight(); i++)
        {
            result.append(i + ":  " + temp3[i][1] + " ( " + temp3[i][0] + " ),    " + temp4[i][1] + " ( " + temp4[i][0]
                    + " ),    [");
            for (int j = 0; j < temp2[i].length; j++)
                result.append(temp2[i][j] + ", ");
            result.append("]\n");
        }

        return result.toString();
    }

    
    protected void visit(QueryTask t)
    {
        if (!(t instanceof VPKNNCursor.KNNQueryTask))
            throw new UnsupportedOperationException("Only KNNQueryTask is supported by VPKNNCursor, not "
                    + t.getClass());
        visit((KNNQueryTask) t);
    }
    
    protected void visit(KNNQueryTask task)
    {
 
	//if already k results has been returned stop the search.
	if (result_needed<0)
		return;
	
	double distance = task.estimation;
	if (nnFinished >= guranteeNN) {
		targetRadius = distance;
	} else if (distance > targetRadius) {
		targetRadius = distance;
		if (foundResult) {
			//nnFinished = true;
			//changed 021506
			nnFinished ++;
			foundResult =false; //to track next results found event.
		}
		//to do, 
		//if cachedRadius <= target Radius, return good results. 
	}


        Object node = null;
        try
        {
            node = oiom.readObject(new Long(task.nodeAddress));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (node instanceof VPInternalNode)
            visitInternalNode((VPInternalNode) node, task.distList);
        else if (node instanceof VPLeafNode)
            visitLeafNode((VPLeafNode) node, task.distList);
        else
            throw new UnsupportedOperationException(
                    "only VPInternalNode or VPLeafNode is supported by VPKNNCursor, not " + node.getClass());
    }
    
    boolean contains(ObjectIOManager oiom, long address, IndexObject query)
    {
        List<IndexObject> linearIndex = getAllPoints(oiom, address);
        for(IndexObject o: linearIndex)
            if (this.metric.getDistance(o, query) == 0)
                return true;
        return false;
    }

    void continueSearch()
    {
        while ( (task.size()>0) && (result.size() == 0))
        {
            visit( (QueryTask) task.remove() );
        }
	if (task.size()==0 )
		getCachedResult();
    }
    
    private class KNNQueryTask extends QueryTask
    {
        KNNQueryTask(long address, double[] distList, double lb)
        {
            this.nodeAddress = address;
            this.distList = distList;
	    this.estimation =lb;
        }

        // TODO
        long     nodeAddress;
        double[] distList;
        double estimation;
    }

    private void visitInternalNode(VPInternalNode node, double[] distList)
    {
        //comment out debuging code 
        /*for (int i=0; i<node.numChildren(); i++)
            if (contains(this.oiom, node.getChildAddress(i), this.q) )
                System.out.println("chid " + i + " contains the query");
        */
        // check whether this is the first node of a layer, and set some statistics.
        if (Debug.debug) // operation of statistics
        {
            logger.finest("\n(" + level + ", " + insideLevel + "): [d(q,pivot) : ");
        }
        if (Debug.statistics) // operation of statistics
        {
            nodeBegining();
            internalVisited++;
        }
    
        // calculate distances from the query to all pivots
        final int numPivot = node.numPivots();

        if (Debug.statistics) // operation of statistics
        {
            // calculate statistics: queryCenterDistance
            if (tempQueryPivotDistance.length < numPivot) // check whether more centers/vantage points emerge.
            {
                double[] temp1 = tempQueryPivotDistance;
                int[] temp2 = childWithPivotCounter;

                tempQueryPivotDistance = new double[numPivot];
                childWithPivotCounter = new int[numPivot];

                System.arraycopy(temp1, 0, tempQueryPivotDistance, 0, temp1.length);
                System.arraycopy(temp2, 0, childWithPivotCounter, 0, temp2.length);
                for (int i = temp1.length; i < numPivot; i++)
                {
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
        if (distList.length < this.maxListLength)
        {
            double[] temp = distList;
            distList = new double[((oldDistListLength + numPivot) <= this.maxListLength)
                    ? (oldDistListLength + numPivot)
                    : this.maxListLength];
            System.arraycopy(temp, 0, distList, 0, oldDistListLength);
        }

        for (int pivot = 0; pivot < numPivot; ++pivot)
        {
            queryPivotDistance[pivot] = metric.getDistance(q, node.getPivot(pivot));

            if (Debug.debug) // operation of statistics
            {
                logger.finest("Checking pivot " + pivot + ",  d(query, pivot[" + pivot + "] ) ="
                        + queryPivotDistance);
            }
            if (Debug.statistics) // operation of statistics
            {
                childWithPivotCounter[pivot]++;
                tempQueryPivotDistance[pivot] += queryPivotDistance[pivot];
            }

            // return pivot as query result if it is satisfies the limiting radius
            if (queryPivotDistance[pivot] <= r)
            {
                
		if (Debug.statistics) // operation of statistics
                {
                    this.pivotAsResult++;
                }		
		queueResults(node.getPivot(pivot), queryPivotDistance[pivot] );
            }
            else
            {
                if (Debug.debug) // operation of statistics
                {
                    logger.finest(", not satisfying the query.");
                }
            }

            // add query pivot distance to list if list is not long enough
            if (oldDistListLength + pivot < distList.length)
                distList[oldDistListLength + pivot] = queryPivotDistance[pivot];

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
	
        boolean done = false;
        // check range for each child and prune
        for (int i = 0; i < numChild; ++i)
        {
            done = false;
            double[][] range = node.getChildPredicate(i);
	    double pruneAt = targetRadius;
            if (Debug.debug) // operation of statistics
            {
                logger.finest("[" + i + ":");
            }

            for (int j = 0; j < numPivot && !done; ++j)
            {
	
                // 1st rule: if upper(child,p) + d(p,q) <= r, then all points of child are results.
                if (!done && (range[1][j] + queryPivotDistance[j] <= r))
                {
                    done = true;
                    List<IndexObject> allResult = getAllPoints(this.oiom, node.getChildAddress(i));

                    if (Debug.statistics) // operation of statistics
                    {
                        this.nodeWithoutDist++;
                        this.resultWithoutDist += allResult.size();
                    }
                    if (Debug.debug) // operation of statistics
                    {
                        logger.finest("(" + queryPivotDistance[j] + "+" + range[1][j] + ">" + r
                                + ") : all are results], ");
                    }

                    for (IndexObject o:allResult)
		    {
			queueResults(o, metric.getDistance(q, o));
		    }
                }
		
		if (!done)
		{
			double ld = range[0][j] - queryPivotDistance[j];
			double ud = queryPivotDistance[j] - range[1][j];
			double d = ld>ud?ld:ud;
			pruneAt = d > pruneAt?d:pruneAt;

			// 2nd rule:
			//if (!done && ((queryPivotDistance[j] + r < range[0][j]) || (queryPivotDistance[j] - r > range[1][j])))
			if (pruneAt>r)
			{
				done = true;

				if (Debug.statistics) // operation of statistics
				{
					this.nodePrunedByPivotDist++;
					this.pointPrunedByPivotDist += getAllPoints(this.oiom, node.getChildAddress(i)).size();
				}
				if (Debug.debug) // operation of statistics
				{
					logger.finest("(" + queryPivotDistance[j] + "+" + r + "<" + range[0][j] + ") : pruned], ");
				}
			}
		}
            }

            // add to task list to be searched if the child has not been pruned
            if (!done)
            {
                // TODO
                if (oldDistListLength < distList.length)
                    queueTask(new KNNQueryTask(node.getChildAddress(i), (double[]) distList.clone(), pruneAt));
                else
                    queueTask(new KNNQueryTask(node.getChildAddress(i), distList, pruneAt));

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
            // check whether this is the last node of the layer, and set statistics
            nodeEnd();
        }
	if (task.isEmpty())
		getCachedResult();
    }

    private void visitLeafNode(VPLeafNode node, double[] distList)
    {
        if (Debug.debug) // operation of statistics
        {
            logger.finest("entering a vp leaf node (" + level + ", " + insideLevel + ")\n" + node);
        }
        if (Debug.statistics) // operation of statistics
        {
            // check whether this is the first node of a layer, and set some statistics.
            nodeBegining();

            // set statistics
            tempLevelPoint += node.size();
            // final int size = node.size();

            if (tempQueryPivotDistance.length < node.numPivots()) // check whether more centers/vantage points emerge.
            {
                double[] temp1 = tempQueryPivotDistance;
                int[] temp2 = childWithPivotCounter;

                tempQueryPivotDistance = new double[node.numPivots()];
                childWithPivotCounter = new int[node.numPivots()];

                System.arraycopy(temp1, 0, tempQueryPivotDistance, 0, temp1.length);
                System.arraycopy(temp2, 0, childWithPivotCounter, 0, temp2.length);
                for (int i = temp1.length; i < node.numPivots(); i++)
                {
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

        for (int pivot = 0; pivot < node.numPivots(); ++pivot)
        {
            queryPivotDistance[pivot] = metric.getDistance(q, node.getPivot(pivot));

            if (Debug.statistics) // operation of statistics
            {
                childWithPivotCounter[pivot]++;
                tempQueryPivotDistance[pivot] += queryPivotDistance[pivot];
            }
            if (Debug.debug) // operation of statistics
            {
                logger.finest("Checking pivot " + pivot + ",  d(query, pivot[" + pivot + "] ) ="
                        + queryPivotDistance);
            }

            // return pivot as query result if it is satisfies the range query
            if (queryPivotDistance[pivot] <= r)
            {
		queueResults(node.getPivot(pivot), queryPivotDistance[pivot]);
                if (Debug.statistics) // operation of statistics
                {
                    pivotAsResult++;
                }

            }
            else
            {
                if (Debug.debug) // operation of statistics
                {
                    logger.finest(", not satisfying the query.");
                }
            }

        }


        for (int child = 0; child < distinctSize; child++)
        {
            boolean done = false;
            // 1. try to prune by the path distances
            double[] pathDist = node.getDataPointPathDistance(child);

            for (int pivot = 0; pivot < distList.length && pivot < pathDist.length; pivot++)
            {
                // if | d(p,c) - d(p,q) | > r, then c can be pruned.
                if (Math.abs(pathDist[pivot] - distList[pivot]) > r)
                {
                    if (Debug.statistics) // operation of statistics
                    {
                        this.pointPrunedByPathDist++;
                    }
                    if (Debug.debug) // operation of statistics
                    {
                        logger.finest("child: " + child + ", path distance: " + pivot + ": | " + pathDist[pivot]
                                + " - " + distList[pivot] + " | > " + r + ", pruned!");
                    }

                    done = true;
                    break;
                }

                // if d(p,c) + d(p,q) <= r, then c is a result;
                if (pathDist[pivot] + distList[pivot] <= r)
                {
                    if (Debug.statistics) // operation of statistics
                    {
                        this.resultConfirmedByPathDist++;
                        this.resultWithoutDist++;
                    }
                    if (Debug.debug) // operation of statistics
                    {
                        logger.finest("child: " + child + ", path distance: " + pivot + ": " + pathDist[pivot]
                                + " + " + distList[pivot] + " <= " + r + ", is a result!");
                    }

                    done = true;
		    queueResults(node.getChild(child), metric.getDistance(q, node.getChild(child)));
                    break;
                }

            }

            if (done)
                continue;

            // 2, try to prune by each point's distance to the node pivot, check in the order of pivot
            // compute distance with each pivot, then try to prune
            for (int pivot = 0; pivot < node.numPivots(); pivot++)
            {
                double[] dataPivotDistance = node.getDataPointPivotDistance(child);

                // if | d(p,c) - d(p,q) | > r, then c can be pruned.
                if (Math.abs(dataPivotDistance[pivot] - queryPivotDistance[pivot]) > r)
                {
                    if (Debug.statistics) // operation of statistics
                    {
                        this.pointPrunedByPivotDist++;
                    }
                    if (Debug.debug) // operation of statistics
                    {
                        logger.finest("child: " + child + ", pivot distance: " + pivot + ": | "
                                + dataPivotDistance[pivot] + " - " + queryPivotDistance[pivot] + " | > " + r
                                + ", pruned!");
                    }

                    done = true;
                    break;
                }

                // if d(p,c) + d(p,q) <= r, then c is a result;
                if (dataPivotDistance[pivot] + queryPivotDistance[pivot] <= r)
                {
                    if (Debug.statistics) // operation of statistics
                    {
                        this.resultWithoutDist++;
                    }
                    if (Debug.debug) // operation of statistics
                    {
                        logger.finest("child: " + child + ", pivot distance: " + pivot + ": "
                                + dataPivotDistance[pivot] + " + " + queryPivotDistance[pivot] + " <= " + r
                                + ", is a result!");
                    }

                    done = true;
		    queueResults(node.getChild(child), metric.getDistance(q, node.getChild(child)));
                    break;
                }
            }

            if (done)
                continue;

            // 3 the data can not be pruned or sure to be a result, so, compute distance directly.
            double distance = metric.getDistance(q, node.getChild(child));

            if (Debug.statistics) // operation of statistics
            {
                distNum++;
                tempLevelPointVisited++;
            }

            if (distance <= r)
            {
                if (Debug.debug) // operation of statistics
                {
                    logger.finest("Point: " + child + ":  " + distance + " <= " + r + " ), is a result. ");
                }
		queueResults(node.getChild(child), distance);
            }
            else
            {
                if (Debug.debug) // operation of statistics
                {
                    logger.finest("Point: " + child + ": " + distance + " > " + r + " ), is not a result.");
                }
            }

        }

        if (Debug.statistics) // operation of statistics
        {
            // check whether this is the last node of a layer, and set statistics
            nodeEnd();
        }

	getCachedResult();

    }
    
    	/**
	 *  Queue the results found during the search
	 *
	 *@param  obj       matching object
	 *@param  distance  distance of this matches
	 */
	public void queueResults(IndexObject obj, double distance) {
		if (Debug.debug) // operation of statistics
		{
			logger.finest("queue cached Result "+ cachedResults.size()+" to "+result.size()+ " "+result_needed+"results needed");
		}
		if (nnFinished >= guranteeNN) {
			//no queue needed
			cachedResults.add(new DoubleIndexObjectPair(distance, obj));

		} else {
			//need queue results in order of distance
			if (cachedResults.isEmpty()) {
				cachedResults.add(new DoubleIndexObjectPair(distance, obj));
				cachedRadius = distance;
			} else {
				double p = ((DoubleIndexObjectPair) cachedResults.getLast()).getDouble();
				if (distance >= p) {
					cachedResults.add(new DoubleIndexObjectPair(distance, obj));//
				} else {
					ListIterator<DoubleIndexObjectPair> i = cachedResults.listIterator();					
					int index =0;
					boolean stop = false;
					while (!stop && i.hasNext()){
						p= ((DoubleIndexObjectPair) i.next()).getDouble();
						//temprorarly changed should change back to <=
						if (distance <= p){
							stop = true;
							cachedResults.add(index, new DoubleIndexObjectPair(distance, obj));
							if ( index ==0){
								cachedRadius = distance;
							}
						}else
							index++;
					}
						
				}
			}
			if (cachedResults.size()>result_needed){
				double d = Math.ceil(((DoubleIndexObjectPair) cachedResults.removeLast()).getDouble())-EPSILON;
				if (d<r) r=d;
			}
		}		
	}


	/**
	 *  Gets the results that have been cached based on current search stratagey, target radius and search status. 
	 * move qulaified results from cahcedResults to result. 
	 *
	 */
	public void getCachedResult() {
		if (Debug.debug) // operation of statistics
		{
			logger.finest("getting cached Result "+ cachedResults.size()+" to "+result.size());
		}
 		//queue is empty or cachedRadus > targetRadius, no results to return.
		if (cachedResults.isEmpty()) {
			return;
		}

		// return all results in queue if there is no more nodes to visit. 
		if (task.isEmpty ())
		{
			//return cachedResults;
			result.addAll(cachedResults);
			result_needed -=cachedResults.size();
			if (Debug.debug) // operation of statistics
			{
				logger.finest("return all cached Result: result queue("+result.size()+ ") results needed " +result_needed);
			}
			
 			return;
		}
		
		ListIterator<DoubleIndexObjectPair> lir = cachedResults.listIterator();

		if (search_policy >= 0 && nnFinished >= guranteeNN) {
			//no queue return all founded
			while (lir.hasNext()) {
				result.add(lir.next());				
				result_needed--;
				lir.remove();
				if (!foundResult) foundResult=true;
			}
		}else if ( cachedRadius <= targetRadius)
		{	//need return results in order of distance
            //TODO what is index for?
			boolean stop = false;
			DoubleIndexObjectPair tempPair;
			while (lir.hasNext() && !stop) {
				tempPair = (DoubleIndexObjectPair) lir.next();
				double p = tempPair.getDouble();
				if (p > targetRadius) {
					stop = true;
					cachedRadius = p;
				} else {
					result.add(tempPair);
					lir.remove();
					result_needed--;
					if (!foundResult) foundResult=true;
				}
			}
			if (!stop) {
				cachedRadius = Double.MAX_VALUE;
			}
		}
		if (Debug.debug) // operation of statistics
		{
			logger.finest("got cached Result "+ cachedResults.size()+" to "+result.size()+ " "+result_needed+" results needed"
			+"task size("+task.size()+")");
		}
		
	}

	//find the first index in targetStack that <= pruneAt
	private final int getInsertionIndex(int from, int to, double pruneAt)
	{
		
		if (from >to)
			return from;
		double p = ((KNNQueryTask) task.get(to)).estimation;
		if (pruneAt > p)
			return to+1;
		p = ((KNNQueryTask) task.get(from)).estimation;
		if (pruneAt <=p)
			return from;
		int mid = (to+from)>>1;
		p = ((KNNQueryTask) task.get(mid)).estimation;
		if (pruneAt >p)
			return getInsertionIndex(mid+1, to-1, pruneAt);
		else 
			return getInsertionIndex(from+1, mid-1, pruneAt);		
	}
	
	
	/**
	 *  This method is used to queue the current node in target list
	 *
	 *@param  obj  target node 
	 */
	private final void queueTask(KNNQueryTask obj) {
		double pruneAt =  obj.estimation;
		if (task.isEmpty()) {
			task.add(obj);
		} else {
			double p = ((KNNQueryTask) task.getLast()).estimation;
			if (pruneAt > p) {
				task.add(obj);
			} else {
			
				int index = getInsertionIndex(0, task.size()-2, pruneAt);
				task.add(index, obj);
			}
		}
	}

    @Override
    void searchResults(){}
	

}
