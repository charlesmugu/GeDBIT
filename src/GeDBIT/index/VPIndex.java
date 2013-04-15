/**
 * GeDBIT.index.VPIndex 2006.05.09 
 *
 * Copyright Information: 
 *
 * Change Log: 
 * 2006.05.09: Created, by Rui Mao, Willard
 * 2006.07.09: added KNN search, by Weijia Xu
 */

package GeDBIT.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import GeDBIT.dist.Metric;
import GeDBIT.index.algorithms.PartitionMethod;
import GeDBIT.index.algorithms.PartitionMethods;
import GeDBIT.index.algorithms.PartitionResults;
import GeDBIT.index.algorithms.PivotSelectionMethod;
import GeDBIT.index.Query;
import GeDBIT.index.RangeQuery;
import GeDBIT.type.IndexObject;
import GeDBIT.util.Debug;

/**
 * A Vantage Point Tree (VPT) index.
 * 
 * @author Rui Mao, Willard, Weijia Xu
 * @version 2006.05.09
 */
public class VPIndex extends AbstractIndex
{
    private static final long             serialVersionUID = 6928847050089693231L;

    private PivotSelectionMethod          psm;
    private int                           numPivot;
    private PartitionMethod               pm;
    private int                           singlePivotFanout;
    private int                           maxLeafSize;
    private int                           maxPathLength;

    private transient ArrayList<LoadTask> taskList;

    public transient int                  numLeaf;
    public transient int                  numInternal;

    /**
     * Builds an idex over the specified Table.
     * 
     * @param fileName
     *            the fileName to give the index.
     * @param data
     *            the {@link List} of {@link IndexObject}s to build the index
     *            over
     * @param metric
     *            the {@link Metric} to use when building the index.
     * @param psm
     *            the {@link PivotSelectionMethod} to use when building the
     *            index.
     * @param numPivot
     *            the number of pivots to use.
     * @param pm
     *            the {@link PartitionMethod} to use when building the index.
     * @param singlePivotFanout
     *            the fanout for a single pivot. the total fanout is
     *            singlePivotFanout^numPivot
     * @param maxLeafSize
     *            the maximum number of data points in a leaf node.
     * @param maxPathLength
     *            the number of previous distance calculations to store in a
     *            given node.
     * @param debugLevel
     *            the debug level.
     */
    public VPIndex(String fileName, List<? extends IndexObject> data,
            Metric metric, PivotSelectionMethod psm, int numPivot,
            PartitionMethod pm, int singlePivotFanout, int maxLeafSize,
            int maxPathLength, Level debugLevel)
    {

        // 1.a. initialize the object io manager
        super(fileName, data, metric, debugLevel);

        // 1.b. initialize fields
        this.psm = psm;
        this.numPivot = numPivot;
        this.pm = pm;
        this.singlePivotFanout = singlePivotFanout;
        this.maxLeafSize = maxLeafSize;
        this.maxPathLength = maxPathLength;

        // 1.c. initialize two lists
        taskList = new ArrayList<LoadTask>();

        // 2. do the actual loading,
        this.numInternal = 0;
        this.numLeaf = 0;
        this.root = load(data);

        // 4. set oiom to be read only
        try
        {
            close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        initOIOM(true);
        openOIOM();
    }

    public VPIndex(String fileName, List<? extends IndexObject> data,
            Metric metric, PivotSelectionMethod psm, int numPivot,
            PartitionMethod pm, int singlePivotFanout, int maxLeafSize,
            int maxPathLength, Level debugLevel, int subtree)
    {

        // 1.a. initialize the object io manager
        super(fileName, data, metric, debugLevel, subtree);

        // 1.b. initialize fields
        this.psm = psm;
        this.numPivot = numPivot;
        this.pm = pm;
        this.singlePivotFanout = singlePivotFanout;
        this.maxLeafSize = maxLeafSize;
        this.maxPathLength = maxPathLength;

        // 1.c. initialize two lists
        taskList = new ArrayList<LoadTask>();

        // 2.a. do the actual loading
        this.numInternal = 0;
        this.numLeaf = 0;
        this.root = loadRoot(data);

        // 3.a. set oiom to be read only
        try
        {
            for (int i = 0; i < subtree + 1; i++)
            {
                close(i);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        for (int i = 0; i < subtree + 1; i++)
        {
            initOIOM(true, i);
            openOIOM(i);
        }
    }

    /**
     * 
     * @param data
     * @return
     */
    private long load(List<? extends IndexObject> data)
    {
        // step 1: initialize the stack, put the root task into it.
        LoadTask task = new LoadTask(data, "0", -1, 0,
                new ArrayList<IndexObject>(maxPathLength));

        logger.info("building "
                + task.description
                + ", size: "
                + task.size
                + ", heap size: "
                + Runtime.getRuntime().totalMemory()
                / 1024
                + "K, used size: "
                + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
                        .freeMemory()) / 1024 + "K");

        // at the root, you always want to create an internal node

        // step 2.a: select pivots
        int numPivots = (numPivot >= task.size) ? task.size : numPivot;

        int[] pivots = psm.selectPivots(metric, task.compressedData, numPivots);

        // step 2.b: rearrange data list, put pivots at end:

        task.groupPivotsAtEnd(pivots);
        taskList.add(task);
        createAnInternalNode(task, pm, singlePivotFanout, maxLeafSize, 0);
        if (0 != PartitionMethods.countRN)
        {
            // CountRN added by Kewei Ma
            countRN(data, task, PartitionMethods.r, PartitionMethods.pm);
            System.out
                    .println("Please ignore the Exceptions below. It's caused by manually return a wrong value 0 to end the rest of building process");
            return 0;
        } else
        {
            // step 2: do the DFS bulkloading
            int taskToGet;
            while ((taskToGet = taskList.size() - 1) > 0)
            {
                task = taskList.get(taskToGet);
                if (task.node == null)
                {
                    // step 2.a: select pivots
                    numPivots = (numPivot >= task.size) ? task.size : numPivot;

                    logger.finer("VP Selection Method:" + psm.toString());
                    logger.finer("selecting VP using " + psm.toString()
                            + " selection.., data size= " + task.size
                            + ", vp number= " + numPivots);

                    pivots = psm.selectPivots(metric, task.compressedData,
                            numPivots);

                    // step 2.b: rearrange data list, put pivots at end:
                    task.groupPivotsAtEnd(pivots);

                    // step 2.c: create either an internal node or a leaf node
                    // depending on the maximum
                    // leaf size allowed
                    if (task.size - task.numPivots > maxLeafSize)
                        createAnInternalNode(task, pm, singlePivotFanout,
                                maxLeafSize, taskToGet);
                    else
                        createAndWriteLeafNode(taskList.remove(taskToGet));
                } else
                {
                    writeInternalNode(taskList.remove(taskToGet));
                }
            }

            // step 3: save the root node to the ObjectIOManager
            return writeRootNode(taskList.remove(0));
        }
    }

    private long loadRoot(List<? extends IndexObject> data)
    {
        LoadTask task = new LoadTask(data, "0", -1, 0,
                new ArrayList<IndexObject>(maxPathLength));
        int numPivots = (numPivot >= task.size) ? task.size : numPivot;
        int[] pivots = psm.selectPivots(metric, task.compressedData, numPivots);
        task.groupPivotsAtEnd(pivots);
        taskList.add(task);
        createAnInternalNode(task, pm, singlePivotFanout, maxLeafSize, 0);
        int myIndex;
        int taskToGet;
        HashMap<Integer, Long> subtrees = new HashMap<Integer, Long>();
        while ((taskToGet = taskList.size() - 1) > 0)
        {
            task = taskList.remove(taskToGet);
            myIndex = task.getMyIndex() % super.getSubtree();
            subtrees.put(task.getMyIndex(), loadSubtree(task, myIndex));
        }
        return writeAllSubRoots(taskList.remove(0), subtrees);
    }

    private long writeAllSubRoots(LoadTask task, HashMap<Integer, Long> subtrees)
    {
        final InternalNode iNode = (InternalNode) task.node;
        final int childNumber = iNode.numChildren();
        for (int j = 0; j < childNumber; j++)
            if (iNode.getChildAddress(j) == -1)
                iNode.setChildAddress(j, subtrees.get(j));
        long pointer = -1;
        try
        {
            pointer = oioms[0].writeObject(iNode);
        } catch (Exception e)
        {
            System.out.println("Error while writing root address! ");
            e.printStackTrace();
        }
        return pointer;
    }

    private long loadSubtree(LoadTask task, int myIndex)
    {
        int i;
        int[] pivots;
        int numPivots;
        ArrayList<LoadTask> lists = new ArrayList<LoadTask>();
        lists.add(task);
        boolean first = true;
        while ((i = lists.size() - 1) > 0 || first)
        {
            first = false;
            task = lists.get(i);
            if (task.node == null)
            {
                numPivots = (numPivot >= task.size) ? task.size : numPivot;
                pivots = psm.selectPivots(metric, task.compressedData,
                        numPivots);
                task.groupPivotsAtEnd(pivots);
                if (task.size - task.numPivots > maxLeafSize)
                {
                    createAnSubtreeInternalNode(task, pm, singlePivotFanout,
                            maxLeafSize, i, lists);
                } else
                {
                    final long pointer = createAndWriteSubtreeLeafNode(
                            lists.remove(i), myIndex);
                    LoadTask parentNodeTask = lists.get(task.parentNodeIndex);
                    if (parentNodeTask != null)
                    {
                        InternalNode parentNode = (InternalNode) parentNodeTask.node;
                        parentNode.setChildAddress(task.getMyIndex(), pointer);
                    }
                }
            } else
            {
                final long pointer = writeSubtreeInternalNode(lists.remove(i),
                        myIndex);
                LoadTask parentNodeTask = lists.get(task.parentNodeIndex);
                if (parentNodeTask != null)
                {
                    InternalNode parent = (InternalNode) parentNodeTask.node;
                    parent.setChildAddress(task.getMyIndex(), pointer);
                }
            }
        }
        return writeSubtreeRootNode(lists.remove(0), myIndex);
    }

    private long writeSubtreeRootNode(LoadTask task, int subtree)
    {
        final InternalNode iNode = (InternalNode) task.node;
        final int childNumber = iNode.numChildren();
        for (int j = 0; j < childNumber; j++)
            if (iNode.getChildAddress(j) == -1)
                System.out.println("current node: " + iNode.toString()
                        + ", child " + j + " is null!");
        long pointer = -1;
        try
        {
            pointer = oioms[subtree + 1].writeObject(iNode);
        } catch (Exception e)
        {
            System.out.println("error writing index node to ObjectIOManager!");
            e.printStackTrace();
        }
        return pointer;
    }

    private long createAndWriteSubtreeLeafNode(LoadTask task, int subtree)
    {
        IndexObject[] children = task.getDataPoints();
        IndexObject[] pivots = task.getPivots();

        double[][] distance = new double[children.length][task.numPivots];
        for (int i = 0; i < children.length; i++)
        {
            final IndexObject currentChild = children[i];
            for (int j = 0; j < task.numPivots; j++)
                distance[i][j] = metric.getDistance(currentChild, pivots[j]);
        }

        final int pivotHistoryListSize = task.pivotHistoryList.size();
        double[][] pathDistance = new double[children.length][pivotHistoryListSize];
        for (int i = 0; i < children.length; i++)
            for (int j = 0; j < pivotHistoryListSize; j++)
                pathDistance[i][j] = metric.getDistance(children[i],
                        task.pivotHistoryList.get(j));
        long pointer = -1;
        try
        {
            VPLeafNode node = new VPLeafNode(pivots, children, task.size,
                    distance, pathDistance);
            pointer = oioms[subtree + 1].writeObject(node);
        } catch (Exception e)
        {
            System.out.print("Exception in creating HeavyLeafNode: "
                    + e.toString());
            e.printStackTrace();
        }
        return pointer;
    }

    private long writeSubtreeInternalNode(LoadTask task, int subtree)
    {
        final InternalNode iNode = (InternalNode) task.node;
        final int childNumber = iNode.numChildren();
        for (int j = 0; j < childNumber; j++)
            if (iNode.getChildAddress(j) == -1)
                System.out.println("current node: " + iNode.toString()
                        + ", child " + j + " is null!");

        long pointer = -1;
        try
        {
            pointer = oioms[subtree + 1].writeObject(iNode);
        } catch (Exception e)
        {
            System.out.println("error writing index node to ObjectIOManager!");
            e.printStackTrace();
        }
        return pointer;
    }

    private void createAnSubtreeInternalNode(LoadTask task, PartitionMethod pm,
            int singlePivotFanout, int maxLeafSize, int nodeIndex,
            ArrayList<LoadTask> lists)
    {
        IndexObject[] pivots = task.getPivots();
        for (int i = 0; i < pivots.length; i++)
        {
            if (task.pivotHistoryList.size() < maxPathLength)
                task.pivotHistoryList.add(pivots[i]);
            else
                break;
        }
        PartitionResults partitionResults = pm.partition(metric, pivots,
                task.compressedData, 0, (task.size - task.numPivots),
                singlePivotFanout, maxLeafSize);

        int childrenNumber = partitionResults.size();

        // step 3. create an internal node, and save it to nodeList
        task.node = partitionResults.getInternalNode();

        // step 4: add tasks for each child into the stack
        for (int i = childrenNumber - 1; i >= 0; i--)
        {
            task.node.setChildAddress(i, -1);
            LoadTask newTask;
            if (pivots.length < maxPathLength)
                newTask = new LoadTask(partitionResults.getPartition(i),
                        task.description + "." + i, nodeIndex, i,
                        new ArrayList<IndexObject>(task.pivotHistoryList));
            else
                newTask = new LoadTask(partitionResults.getPartition(i),
                        task.description + "." + i, nodeIndex, i,
                        task.pivotHistoryList);
            lists.add(newTask);
        }
    }

    private void createAnInternalNode(LoadTask task, PartitionMethod pm,
            int singlePivotFanout, int maxLeafSize, int nodeIndex)
    {
        logger.finest("Create an internal node...");

        this.numInternal++; // counter for internal node

        // get pivots
        IndexObject[] pivots = task.getPivots();
        for (int i = 0; i < pivots.length; i++)
        {
            if (task.pivotHistoryList.size() < maxPathLength)
                task.pivotHistoryList.add(pivots[i]);
            else
                break;

        }

        PartitionResults partitionResults = pm.partition(metric, pivots,
                task.compressedData, 0, (task.size - task.numPivots),
                singlePivotFanout, maxLeafSize);

        int childrenNumber = partitionResults.size();

        // step 3. create an internal node, and save it to nodeList
        task.node = partitionResults.getInternalNode();

        // step 4: add tasks for each child into the stack
        for (int i = childrenNumber - 1; i >= 0; i--)
        {
            task.node.setChildAddress(i, -1);
            LoadTask newTask;
            // TODO why this?
            // if (pivots.length < maxPathLength)
            if (task.pivotHistoryList.size() < maxPathLength)
                newTask = new LoadTask(partitionResults.getPartition(i),
                        task.description + "." + i, nodeIndex, i,
                        new ArrayList<IndexObject>(task.pivotHistoryList));
            else
                newTask = new LoadTask(partitionResults.getPartition(i),
                        task.description + "." + i, nodeIndex, i,
                        task.pivotHistoryList);

            taskList.add(newTask);
        }
    }

    private void createAndWriteLeafNode(LoadTask task)
    {
        this.numLeaf++; // counter for leaf node

        IndexObject[] children = task.getDataPoints();
        IndexObject[] pivots = task.getPivots();

        double[][] distance = new double[children.length][task.numPivots];
        for (int i = 0; i < children.length; i++)
        {
            final IndexObject currentChild = children[i];
            for (int j = 0; j < task.numPivots; j++)
                distance[i][j] = metric.getDistance(currentChild, pivots[j]);
        }

        if (Debug.debug)
        {
            logger.finest("create a leaf node, distances(VP*children):");
            for (int i = 0; i < task.numPivots; i++)
            {
                for (int j = 0; j < children.length; j++)
                    logger.finest(distance[i][j] + ", ");
                logger.finest("");
            }
        }

        final int pivotHistoryListSize = task.pivotHistoryList.size();
        double[][] pathDistance = new double[children.length][pivotHistoryListSize];
        for (int i = 0; i < children.length; i++)
            for (int j = 0; j < pivotHistoryListSize; j++)
                pathDistance[i][j] = metric.getDistance(children[i],
                        task.pivotHistoryList.get(j));

        try
        {
            VPLeafNode node = new VPLeafNode(pivots, children, task.size,
                    distance, pathDistance);

            // write this node to the index file
            final long pointer = oiom.writeObject(node);
            InternalNode parentNode = taskList.get(task.parentNodeIndex).node;
            parentNode.setChildAddress(task.myIndex, pointer);
        } catch (Exception e)
        {
            System.out.print("Exception in creating HeavyLeafNode: "
                    + e.toString());
            e.printStackTrace();
        }
    }

    private long writeInternalNode(LoadTask task)
    {
        final InternalNode iNode = (InternalNode) task.node;
        // check whether each child address has already been set.
        final int childNumber = iNode.numChildren();
        for (int j = 0; j < childNumber; j++)
            if (iNode.getChildAddress(j) == -1)
                System.out.println("current node: " + iNode.toString()
                        + ", child " + j + " is null!");

        // write current node, and set its parent
        long pointer = -1;
        try
        {
            pointer = oiom.writeObject(iNode);
        } catch (Exception e)
        {
            System.out.println("error writing index node to ObjectIOManager!");
            e.printStackTrace();
        }
        ;
        LoadTask parentNodeTask = taskList.get(task.parentNodeIndex);
        InternalNode parent = (InternalNode) parentNodeTask.node;
        parent.setChildAddress(task.myIndex, pointer);

        return pointer;
    }

    /**
     * This method counts the R neighborhood of the tree's top layer, only
     * executes once in building time
     * 
     * @param data
     *            All data objects
     * @param task
     *            LoadTask, contains building time information
     * @author Kewei Ma
     */
    private void countRN(List<? extends IndexObject> data, LoadTask task,
            double r, String dpm)
    {

        // number of points in r neighborhood
        int rn = 0;
        final double[][] lows = ((VPInternalNode) task.node).getLowerRange();
        final double[][] highs = ((VPInternalNode) task.node).getUpperRange();

        // for all data except pivots
        for (int i = 0; i < task.size - numPivot; i++)
        {
            // number of partitions to search
            int searchTimes = 0;
            // for all partitions
            for (int j = 0; j < lows.length; j++)
            {
                boolean search = true;
                if ("CLUSTERINGKMEANS".equalsIgnoreCase(dpm)) // MVPT
                {
                    // for all pivots
                    for (int k = 0; k < numPivot; k++)
                    {
                        // distance of current data point and current pivot
                        final double dist = metric.getDistance(data.get(i),
                                task.getPivots()[k]);
                        if (dist < lows[j][k] - r || dist > highs[j][k] + r)
                        {
                            search = false;
                            break;
                        }
                    }
                } else if ("CGHT".equalsIgnoreCase(dpm))// CGHT
                {
                    // d1 + d2
                    final double dist1 = metric.getDistance(data.get(i),
                            task.getPivots()[0])
                            + metric.getDistance(data.get(i),
                                    task.getPivots()[1]);
                    // d1 - d2
                    final double dist2 = metric.getDistance(data.get(i),
                            task.getPivots()[0])
                            - metric.getDistance(data.get(i),
                                    task.getPivots()[1]);
                    if (dist1 < lows[j][0] - r * 2
                            || dist1 > highs[j][0] + r * 2
                            || dist2 < lows[j][1] - r * 2
                            || dist2 > highs[j][1] + r * 2)
                        search = false;

                } else
                // GHT
                {
                    // d1 - d2
                    final double dist = metric.getDistance(data.get(i),
                            task.getPivots()[0])
                            - metric.getDistance(data.get(i),
                                    task.getPivots()[1]);
                    if (dist < -2 * r || dist > 2 * r)
                        search = false;
                }
                if (true == search)
                {
                    searchTimes++;
                    if (2 <= searchTimes)
                    {
                        rn++;
                        break;
                    }
                }

            }
        }
        System.out.println("R neighborhood: " + rn);
        // TODO Delete below
        /*
         * System.out.print("low[][0]: "); for (int i = 0; i < lows.length; i++)
         * System.out.print(" " + lows[i][0]); System.out.println();
         * System.out.print("low[][1]: "); for (int i = 0; i < lows.length; i++)
         * System.out.print(" " + lows[i][1]); System.out.println();
         * System.out.print("high[][0]: "); for (int i = 0; i < highs.length;
         * i++) System.out.print(" " + highs[i][0]); System.out.println();
         * System.out.print("high[][1]: "); for (int i = 0; i < highs.length;
         * i++) System.out.print(" " + highs[i][1]); System.out.println();
         */

    }

    private long writeRootNode(LoadTask task)
    {
        final InternalNode iNode = (InternalNode) task.node;
        // check whether each child address has already been set.
        final int childNumber = iNode.numChildren();
        for (int j = 0; j < childNumber; j++)
            if (iNode.getChildAddress(j) == -1)
                System.out.println("current node: " + iNode.toString()
                        + ", child " + j + " is null!");

        // write current node, and set its parent
        long pointer = -1;
        try
        {
            pointer = oiom.writeObject(iNode);
        } catch (Exception e)
        {
            System.out.println("error writing index node to ObjectIOManager!");
            e.printStackTrace();
        }
        ;
        return pointer;
    }

    private class LoadTask
    {
        private List<? extends IndexObject> compressedData;
        private String                      description;
        private int                         parentNodeIndex;
        private int                         myIndex;

        private int                         size;
        private int                         numPivots;
        public List<IndexObject>            pivotHistoryList;
        private InternalNode                node;

        LoadTask(List<? extends IndexObject> compressedData,
                String description, int parentIndex, int myIndex,
                List<IndexObject> pivotHistoryList)
        {
            this.compressedData = compressedData;
            this.description = description;
            this.parentNodeIndex = parentIndex;
            this.myIndex = myIndex;
            this.pivotHistoryList = pivotHistoryList;

            this.size = compressedData.size();
            this.numPivots = -1;
        }

        private void groupPivotsAtEnd(int[] pivots)
        {
            numPivots = pivots.length;
            if (numPivots < size)
            {
                int futurePivotIndex = size - 1;
                // put all the pivots at the end
                for (int i = 0; i < numPivots; i++)
                {
                    // System.out.println("numPivots = "+numPivots);
                    // System.out.println("size = "+size);
                    // System.out.println("future = "+futurePivotIndex);
                    // System.out.println("i = "+i);
                    // System.out.println("pivots[i] = "+pivots[i]);
                    // System.out.println("compress size = "+compressedData.size());
                    Collections.swap(compressedData, futurePivotIndex,
                            pivots[i]);
                    // if we just swapped this pivot with another pivot, then we
                    // need to change that
                    // pivot's value
                    for (int j = i + 1; j < numPivots; j++)
                    {
                        if (pivots[j] == futurePivotIndex)
                        {
                            pivots[j] = pivots[i];
                        }
                    }
                    // pivots[i] = futurePivotIndex; don't need this since
                    // pivots[] is not used
                    // after this.
                    futurePivotIndex--;
                }
            }
        }

        // get the pivots (sans rowIDs); used as input for
        // PivotSelectMethod.selectPivots()
        public IndexObject[] getPivots()
        {
            // TODO replace with context specific error
            if (numPivots == -1)
            {
                throw new Error("pivots have not yet been chosen!!!");
            }
            IndexObject[] pivots = new IndexObject[numPivots];
            int start = size - numPivots;
            for (int i = 0; i < numPivots; i++)
            {
                pivots[i] = compressedData.get(start);
                start++;
            }
            return pivots;
        }

        public int getMyIndex()
        {
            return this.myIndex;
        }

        public IndexObject[] getDataPoints()
        {
            final int mySize;
            if (numPivots == -1)
                mySize = size;
            else
                mySize = size - numPivots;
            IndexObject[] dataPoints = new IndexObject[mySize];
            for (int i = 0; i < mySize; i++)
            {
                dataPoints[i] = compressedData.get(i);
            }
            return dataPoints;
        }
    }

    public Cursor search(Query q)
    {
        if (q instanceof RangeQuery)
        {
            if (super.getFlag())
            {
                return new VPRangeCursor((RangeQuery) q, oioms, metric, root);
            } else
            {
                return new VPRangeCursor((RangeQuery) q, oiom, metric, root);
            }
        } else if (q instanceof KNNQuery)
            return new VPKNNCursor((KNNQuery) q, oiom, metric, root);
        else
            throw new UnsupportedOperationException("Unsupported query type "
                    + q.getClass());
    }
}
