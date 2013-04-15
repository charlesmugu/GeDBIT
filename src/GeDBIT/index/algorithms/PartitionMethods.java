package GeDBIT.index.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import GeDBIT.dist.Metric;
import GeDBIT.index.VPInternalNode;
import GeDBIT.util.Debug;
import GeDBIT.type.IndexObject;
import GeDBIT.type.DoubleIndexObjectPair;

/**
 * All the built-in data partition methods. Balance: all the partitions have
 * similar sizes. Fast but performs worse CLUSTERINGKMEANS: partition the data
 * according to the intrinsic clustering, use k-means for each dimension.
 * CLUSTERINGBOUNDARY: partition according to the intrinsic clustering, use a
 * boundary-oriented algorithm for each dimension.
 */
public enum PartitionMethods implements PartitionMethod
{
    BALANCED
    {
        /**
         * 
         * @param R
         */
        public void setMaxRadius(double R)
        {
        }

        /**
         * @param metric
         * @param pivots
         * @param data
         * @param numPartitions
         * @return
         */
        public PartitionResults partition(final Metric metric,
                final IndexObject[] pivots, List<? extends IndexObject> data,
                final int numPartitions, int maxLS)
        {
            return partition(metric, pivots, data, 0, data.size(),
                    numPartitions, maxLS);
        }

        /**
         * @param metric
         * @param pivots
         * @param data
         * @param first
         * @param size
         * @param numPartitions
         * @return
         */
        public PartitionResults partition(Metric metric, IndexObject[] pivots,
                List<? extends IndexObject> data, int first, int size,
                int numPartitions, int maxLS)
        {
            return partition(metric, pivots, data, first, size, numPartitions,
                    maxLS, Logger.getLogger("GeDBIT.index"));
        }

        /**
         * @param metric
         * @param pivots
         * @param data
         * @param first
         * @param size
         * @param numPartitions
         * @param logger
         * @return
         */
        public PartitionResults partition(Metric metric, IndexObject[] pivots,
                List<? extends IndexObject> data, int first, int size,
                int numPartitions, int maxLS, Logger logger)
        {

            final int numPivots = pivots.length;
            final int fanout = (int) Math.pow(numPartitions, numPivots);

            if (Debug.debug)
                logger.finer("Start of splitData(), data size= " + size
                        + ", VPNumber= " + numPivots + ", fanout= " + fanout);

            // the lower and upper bound of distances from each child to each
            // vantage point
            double[][] lower = new double[fanout][numPivots];
            double[][] upper = new double[fanout][numPivots];

            DoubleIndexObjectPair[] wrapper = new DoubleIndexObjectPair[size];
            for (int i = first; i < size; i++)
                wrapper[i] = new DoubleIndexObjectPair(0, data.get(i));

            // split data.
            int clusterNumber = 1; // total cluster number when partition based
                                   // on each vp, SVF ^ i
            int clusterCardinality = fanout; // number of final cluster in each
                                             // of current cluster

            // offset of the first point in current cluster in wrapper, the
            // whole data list
            // this array has an additional element at the end of value size,
            // serving as a loop guard
            // the first element of this array is always 0
            int[] clusterOffset = new int[2];
            clusterOffset[0] = 0;
            clusterOffset[1] = size;

            for (int i = 0; i < numPivots; i++)
            {
                if (Debug.debug)
                    logger.finer("\nStart spliting vp:" + i
                            + ", cluster number:" + clusterNumber
                            + ", clusterCardinality =" + clusterCardinality
                            + ", computing distances to the vp...");

                // compute distance to the current VP
                for (int j = 0; j < size; j++)
                    wrapper[j].setDouble(metric.getDistance(pivots[i],
                            ((IndexObject) wrapper[j].getObject())));

                if (Debug.debug)
                    logger.finer("Sorting the new computed distances...:");

                // sort each part
                for (int j = 0; j < clusterNumber; j++)
                {
                    if (Debug.debug)
                        logger.finer("[" + j + ": " + clusterOffset[j] + ", "
                                + clusterOffset[j + 1] + "], ");

                    Arrays.sort(wrapper, clusterOffset[j],
                            clusterOffset[j + 1],
                            DoubleIndexObjectPair.DoubleComparator);
                }

                final int nextClusterNumber = clusterNumber * numPartitions;
                int[] nextClusterOffset = new int[nextClusterNumber + 1];
                nextClusterOffset[0] = 0;
                nextClusterOffset[nextClusterNumber] = size;

                int nextClusterCardinality = clusterCardinality / numPartitions;

                // split each current cluster into SVF sub-clusters based on the
                // distance to current VP
                for (int j = 0; j < clusterNumber; j++)
                {
                    // size of current cluster (number of points)
                    final int clusterSize = clusterOffset[j + 1]
                            - clusterOffset[j];

                    // if this cluster is empty, set all its sub-cluster to be
                    // empty
                    if (clusterSize == 0)
                    {
                        for (int k = 0; k < numPartitions; k++)
                            nextClusterOffset[j * numPartitions + k + 1] = clusterOffset[j + 1];

                        // jump to next cluster
                        continue;
                    }

                    if (Debug.debug)
                    {
                        logger.finer("Partitioning the " + j
                                + "th cluster, size=" + clusterSize
                                + ", Distances: ");
                        for (int temp = clusterOffset[j]; temp < clusterOffset[j + 1]; temp++)
                            logger.finer(wrapper[temp].getDouble() + ", ");
                        logger.finer("");
                    }

                    // find the last indices of each distinct distance value in
                    // wrapper, which is already sorted
                    ArrayList<Integer> tempIndex = new ArrayList<Integer>();
                    ArrayList<Double> tempValue = new ArrayList<Double>();

                    // the distinct distance value in check, and the number of
                    // points with this distance
                    double currentDistance = wrapper[clusterOffset[j]]
                            .getDouble();
                    int sum = 0;

                    for (int k = clusterOffset[j]; k < clusterOffset[j + 1]; k++)
                    {
                        final double nextDistance = wrapper[k].getDouble();

                        if (nextDistance != currentDistance) // find next
                                                             // distinct
                                                             // distance value
                        {
                            tempIndex.add(sum);
                            tempValue.add(currentDistance);
                            currentDistance = nextDistance;
                        }

                        sum++;
                    }
                    // put the last distinct value into the list
                    tempIndex.add(sum);
                    tempValue.add(currentDistance);

                    final int distinctSize = tempIndex.size();

                    // index of first point with current distinct distance
                    // value,
                    // this is the offset in current cluster, not the index in
                    // wrapper
                    // distinct distance values
                    int[] firstPointWithDistinctDistance = new int[distinctSize + 1];
                    double[] distinctDistance = new double[distinctSize];
                    firstPointWithDistinctDistance[0] = 0;
                    firstPointWithDistinctDistance[distinctSize] = clusterSize;
                    distinctDistance[0] = wrapper[clusterOffset[j]].getDouble();

                    for (int k = 1; k < distinctSize; k++)
                    {
                        firstPointWithDistinctDistance[k] = ((Integer) tempIndex
                                .get(k - 1)).intValue();
                        distinctDistance[k] = ((Double) tempValue.get(k))
                                .doubleValue();
                    }

                    if (Debug.debug)
                    {
                        logger.finer("distinct distances(" + distinctSize
                                + "): ");
                        for (int temp = 0; temp < distinctSize; temp++)
                            logger.finer("[" + temp + ": "
                                    + distinctDistance[temp] + ", "
                                    + firstPointWithDistinctDistance[temp]
                                    + "], ");
                        logger.finer("");
                    }

                    // assign the total distinctSize set of points with
                    // identical distance value
                    // to at most SVF sub-clusters, which is actually split
                    // current cluster

                    // number of distinct set that are already been assigned
                    int startingDistinctSet = 0;

                    // if distince set number is greater than SVF, assign them,
                    // otherwise,
                    // just assign one set to each sub-cluster, remain
                    // sub-clusters are all empty
                    int k = 0; // k is the current sub-cluster to assign
                               // distinct set to
                    while ((k < numPartitions - 1)
                            && (distinctSize - startingDistinctSet > numPartitions
                                    - k))
                    {
                        // assign sets based on their cardinality, prefer
                        // balance sub-cluster
                        final int median = (clusterSize - firstPointWithDistinctDistance[startingDistinctSet])
                                / (numPartitions - k);

                        // find the distince set that contains the median point
                        int t = startingDistinctSet;
                        while (firstPointWithDistinctDistance[t + 1] < median
                                + firstPointWithDistinctDistance[startingDistinctSet])
                            t++;

                        // if median falls in the first distinct set, assign
                        // this set to current cluster
                        if (t != startingDistinctSet)
                            t = (firstPointWithDistinctDistance[t + 1]
                                    - median
                                    - firstPointWithDistinctDistance[startingDistinctSet] >= median
                                    + firstPointWithDistinctDistance[startingDistinctSet]
                                    - firstPointWithDistinctDistance[t]) ? t - 1
                                    : t;

                        // now startingDistinctSet is the index of the first
                        // distinct set, and t is the index
                        // of the last distinct set, to be assinged to current
                        // sub-cluster
                        // set the sub-cluster offset, lower, upper bound
                        nextClusterOffset[j * numPartitions + k + 1] = clusterOffset[j]
                                + firstPointWithDistinctDistance[t + 1];

                        final int firstChild = j * clusterCardinality + k
                                * nextClusterCardinality;

                        for (int temp = firstChild; temp < firstChild
                                + nextClusterCardinality; temp++)
                        {
                            lower[temp][i] = distinctDistance[startingDistinctSet];
                            upper[temp][i] = distinctDistance[t];
                        }

                        if (Debug.debug)
                        {
                            logger.finer("computing " + k
                                    + "th sub-cluster, median=" + median
                                    + ", assigned distinct set:"
                                    + startingDistinctSet + ", last set:" + t
                                    + ", first child =" + firstChild + ", i="
                                    + i + ", j=" + j + ", k=" + k);
                            logger.finer("next cluster offset:");
                            for (int temp = 0; temp < nextClusterOffset.length; temp++)
                                logger.finer("[" + temp + ":"
                                        + nextClusterOffset[temp] + "],");

                            logger.finer("\nlower, upper:");
                            for (int temp = 0; temp < fanout; temp++)
                                logger.finer("[" + temp + ": " + lower[temp][i]
                                        + ", " + upper[temp][i] + "], ");
                            logger.finer("");
                        }

                        startingDistinctSet = t + 1;
                        k++;
                    }

                    // if reaches the last sub-cluster, assign all remain set to
                    // it
                    if (k == numPartitions - 1)
                    {
                        // set the sub-cluster offset, lower, upper bound
                        nextClusterOffset[j * numPartitions + k + 1] = clusterOffset[j + 1];

                        final int firstChild = j * clusterCardinality + k
                                * nextClusterCardinality;
                        for (int temp = firstChild; temp < firstChild
                                + nextClusterCardinality; temp++)
                        {
                            lower[temp][i] = distinctDistance[startingDistinctSet];
                            upper[temp][i] = distinctDistance[distinctSize - 1];
                        }
                    }

                    // remain set number is not greater than remain sub-cluster
                    // number,
                    // assign one set to each sub-cluster
                    else
                    {
                        if (Debug.debug)
                        {
                            logger.finer("less distinct set:"
                                    + (distinctSize - startingDistinctSet)
                                    + ", remain sub-cluster:"
                                    + (numPartitions - k));
                        }

                        for (int t = startingDistinctSet; t < distinctSize; t++)
                        {
                            nextClusterOffset[j * numPartitions + k + 1] = clusterOffset[j]
                                    + firstPointWithDistinctDistance[t + 1];

                            final int firstChild = j * clusterCardinality + k
                                    * nextClusterCardinality;
                            for (int temp = firstChild; temp < firstChild
                                    + nextClusterCardinality; temp++)
                            {
                                lower[temp][i] = distinctDistance[t];
                                upper[temp][i] = distinctDistance[t];
                            }

                            k++;
                        }

                        if (k < numPartitions) // if there are still
                                               // sub-cluster, set them to be
                                               // null
                        {
                            for (; k < numPartitions; k++)
                                nextClusterOffset[j * numPartitions + k + 1] = clusterOffset[j + 1];
                        }
                    }
                } // end of a loop for each cluster

                clusterOffset = nextClusterOffset;
                clusterCardinality = nextClusterCardinality;
                clusterNumber = nextClusterNumber;

            } // end of loop for each vantage point

            // compute non-empty cluster number
            int childrenNumber = 0;
            for (int i = 0; i < fanout; i++)
            {
                if (clusterOffset[i] < clusterOffset[i + 1])
                    childrenNumber++;
            }

            if (Debug.debug)
                logger.finer("final children number: " + childrenNumber
                        + ", fanout=" + fanout);

            if (childrenNumber < fanout) // if there are some empty clusters,
                                         // delete them)
            {
                double[][] newLower = new double[childrenNumber][];
                double[][] newUpper = new double[childrenNumber][];
                int[] newOffset = new int[childrenNumber + 1];
                newOffset[childrenNumber] = size;

                int j = 0;
                for (int i = 0; i < fanout; i++)
                {
                    if (clusterOffset[i] < clusterOffset[i + 1])
                    {
                        newLower[j] = lower[i];
                        newUpper[j] = upper[i];
                        newOffset[j] = clusterOffset[i];
                        j++;
                    }
                }

                lower = newLower;
                upper = newUpper;
                clusterOffset = newOffset;
            }

            // assign data to subDataList
            List<List<? extends IndexObject>> subDataList = new ArrayList<List<? extends IndexObject>>(
                    childrenNumber);
            for (int i = 0; i < childrenNumber; i++)
            {
                ArrayList<IndexObject> subList = new ArrayList<IndexObject>(
                        clusterOffset[i + 1] - clusterOffset[i]);

                for (int j = clusterOffset[i]; j < clusterOffset[i + 1]; j++)
                    subList.add((IndexObject) wrapper[j].getObject());

                if (subList.size() == 0)
                    System.out.println("sub list :" + i + " is empty!");

                subDataList.add(subList);
            }

            VPInternalNode predicate = new VPInternalNode(pivots, lower, upper,
                    data.size(), new long[childrenNumber]);
            PartitionResults partitionResult = new PartitionResults(
                    subDataList, predicate);

            return partitionResult;

        }
    },
    CLUSTERINGKMEANS
    {
        private int GHTDegree = 0; // -1: ght, -2:cght

        /**
         * @param R
         */
        public void setMaxRadius(double R)
        {
            this.GHTDegree = (int) R;
        }

        /**
         * @author Rui Mao
         */
        class ClusteringKMeansTask
        {

            private int       first;

            private int       last;

            private double[]  lower;

            private double[]  upper;

            private boolean[] toUse;

            public ClusteringKMeansTask(int first, int last, double[] lower,
                    double[] upper, boolean[] toUse)
            {
                this.first = first;
                this.last = last;
                this.lower = lower;
                this.upper = upper;
                this.toUse = toUse;
            }

        }

        /**
         * @param metric
         * @param pivots
         * @param data
         * @param numPartitions
         * @return
         */
        public PartitionResults partition(Metric metric, IndexObject[] pivots,
                List<? extends IndexObject> data, int numPartitions, int maxLS)
        {
            return partition(metric, pivots, data, 0, data.size(),
                    numPartitions, maxLS);
        }

        /**
         * @param metric
         * @param pivots
         * @param data
         * @param first
         * @param size
         * @param numPartitions
         * @return
         */
        public PartitionResults partition(Metric metric, IndexObject[] pivots,
                List<? extends IndexObject> data, int first, int size,
                int numPartitions, int maxLS)
        {
            return partition(metric, pivots, data, first, size, numPartitions,
                    maxLS, Logger.getLogger("GeDBIT.index"));
        }

        /**
         * given vantage points, this method partition the dataset based on its
         * intrinsic clustering.
         * 
         * @param METRIC
         *            the {@link Metric} to compute distance with
         * @param data
         *            the source data list to split, each element is a
         *            {@link RecordObject}
         * @param VP
         *            the vantage points array, each element can be computed
         *            distance on
         * @param SVF
         *            partition number induced by each vantage point
         * @param maxLS
         *            max leaf size, if a cluster has less size, don't partition
         *            further
         * @return a list, the first element is a List [], which contains lists
         *         of data of each child, the second element is of type double
         *         [][], which is the lowerRange, the min distance from each
         *         child to each vantage point, child*VP, the third element is
         *         of type double [][], which is the upperRange, the max
         *         distance from each child to each vantage point, child*vp
         */
        PartitionResults partition(Metric metric, IndexObject[] pivots,
                List<? extends IndexObject> data, int first, int size,
                final int SVF, final int maxLS, Logger logger)
        {
            if (Debug.debug)
                logger.finer("clusteringPartition");

            // compute all the distance
            final int numPivots = pivots.length;

            double[][] distance = new double[numPivots][size];

            for (int i = 0; i < size; i++)
                for (int j = 0; j < numPivots; j++)
                    distance[j][i] = metric.getDistance(data.get(i), pivots[j]);

            // hack: if this is ght,cght partition, transform distances to d1+d2
            // an d1-d2
            if (this.GHTDegree < 0)
            {
                // for now, must be only two pivots
                if (distance.length != 2)
                    throw new IllegalArgumentException(
                            "for CGHT partition, there should be only two pivots!");

                // transformation to d1+d2, d1-d2
                for (int i = 0; i < size; i++)
                {
                    distance[0][i] = distance[0][i] + distance[1][i];
                    distance[1][i] = distance[0][i] - distance[1][i] * 2;
                }
            }

            // matain a list of clusters to be partitioned. each list item
            // contains:
            // 1. the first last offset of the cluster in the data array, two
            // Integers
            // 2. the distance ranges to all vps, two 1-d double array the first
            // is the lower bound,
            // then the upper bound
            // 3. a boolean array corresponding to all vps, true means this vp
            // is to partition on.
            // if all the list item's boolean array are all false, the partition
            // is done.
            // therefore, when add a list item to the list, if its boolean array
            // is all false, add
            // it to the end, otherwise to the begining
            // thus if the first list item's boolean array is all false, then
            // the partition is done.

            if (this.GHTDegree != -1)
            {
                LinkedList<ClusteringKMeansTask> taskList = new LinkedList<ClusteringKMeansTask>();
                boolean[] toUse = new boolean[numPivots];
                for (int i = 0; i < numPivots; i++)
                {
                    toUse[i] = true;
                }
                ClusteringKMeansTask ckmTask = new ClusteringKMeansTask(0,
                        size - 1, new double[numPivots], new double[numPivots],
                        toUse);
                taskList.addFirst(ckmTask);

                boolean done = false;
                // the loop to partition each cluster
                while (true)
                {
                    ckmTask = taskList.getFirst();
                    done = true;
                    for (int i = 0; i < numPivots; i++)
                        if (ckmTask.toUse[i])
                        {
                            done = false;
                            break;
                        }

                    // if done, the first list item's boolean is all false, the
                    // partition is done, ready
                    // to return
                    if (done)
                        break;

                    // otherwise, partition the current cluster, select a best
                    // vp,
                    // partition based on
                    // this vp, put new sub-clusters into task list, arrange
                    // data
                    // list, distance array.
                    partitionACluster(data, distance, taskList, SVF, maxLS,
                            logger);
                }

                // now partition is done, return result's in required format.
                final int childrenNumber = taskList.size(); // may need to check
                // whether cluster
                // number
                // ==1
                // if (childrenNumber ==1)
                // System.out.println("cluster can not be partitioned!");

                List<List<? extends IndexObject>> subDataList = new ArrayList<List<? extends IndexObject>>(
                        childrenNumber);
                double[][] allLower = new double[childrenNumber][numPivots];
                double[][] allUpper = new double[childrenNumber][numPivots];
                final int taskListSize = taskList.size();
                for (int i = 0; i < taskListSize; i++)
                {
                    ckmTask = taskList.get(i);
                    subDataList.add(data.subList(ckmTask.first,
                            ckmTask.last + 1));
                    for (int j = 0; j < numPivots; j++)
                    {
                        allLower[i][j] = ckmTask.lower[j];
                        allUpper[i][j] = ckmTask.upper[j];
                    }
                }

                VPInternalNode predicate = new VPInternalNode(pivots, allLower,
                        allUpper, size, new long[childrenNumber],
                        this.GHTDegree);
                PartitionResults partitionResult = new PartitionResults(
                        subDataList, predicate);

                return partitionResult;
            }
            //else ght partition
            {
                final int childrenNumber = 2;
                List<List<? extends IndexObject>> subDataList = new ArrayList<List<? extends IndexObject>>(
                        childrenNumber);
                double[][] allLower = new double[childrenNumber][numPivots];
                double[][] allUpper = new double[childrenNumber][numPivots];
                
                int head = 0, tail = size -1;
                double temp;
                while(head <= tail)
                {
                    while ((head < size) && (distance[1][head] <= 0)) head++;
                    while ((tail >= 0  ) && (distance[1][tail] >  0)) tail--;
                    if (head <= tail)
                    {
                        Collections.swap(data, head, tail);
                        temp = distance[1][head];
                        distance[1][head] = distance[1][tail];
                        distance[1][tail] = temp;
                    }
                }
                subDataList.add(data.subList(0, head));
                subDataList.add(data.subList(head, size));

                VPInternalNode predicate = new VPInternalNode(pivots, allLower, allUpper, size, new long[childrenNumber],
                        this.GHTDegree);
                PartitionResults partitionResult = new PartitionResults(subDataList, predicate);

                return partitionResult;
                
            }
        }

        /**
         * partition the first cluster in the task list, select a best vp,
         * partition based on this vp, put new sub-clusters back into task list,
         * if the sub-clusters don't have further partition, append them to the
         * end, otherwise insert to the head arrange data list, distance array,
         * put data and distances belongs to the same sub-clusters together.
         * 
         * @param data
         *            list of data set
         * @param distance
         *            distance values from each data point to each vantage point
         * @param taskList
         *            a {@link LinkedList} of all the clusters to be
         *            partitioned.
         * @param SVF
         *            single fanout
         * @param maxLS
         *            max leaf size, if a cluster has less size, don't partition
         *            further
         * @param logger
         */
        private void partitionACluster(List<? extends IndexObject> data,
                double[][] distance, LinkedList<ClusteringKMeansTask> taskList,
                final int SVF, final int maxLS, Logger logger)
        {
            ClusteringKMeansTask task = taskList.removeFirst();
            final int first = task.first;
            final int last = task.last;

            boolean[] toUse = task.toUse;
            // if current cluster can be fit in a leaf node, don't partition
            // further
            if (last - first + 1 <= maxLS)
            {
                double min = Double.POSITIVE_INFINITY;
                double max = Double.NEGATIVE_INFINITY;

                double[] lower = task.lower;
                double[] upper = task.upper;
                
                // set distance range for unused vps
                for (int i = 0; i < toUse.length; i++)
                    if (toUse[i])
                    {
                        for (int j = first; j <= last; j++)
                        {
                            if (min > distance[i][j])
                                min = distance[i][j];

                            if (max < distance[i][j])
                                max = distance[i][j];
                        }

                        lower[i] = min;
                        upper[i] = max;
                        toUse[i] = false;
                    }
                taskList.addLast(task);
                return;
            }

            // if the cluster can not be fit in a leaf node, go on to partition

            int childrenNumber = 1;
            int minVP = 0; // the index of the vp with the min variance
            double minVar = Double.POSITIVE_INFINITY; // min value of variance
            double var = 0; // temp variable for variance
            double[] means = null; // means of k-means
            double[] split = null; // split values
            double[] minSplit = null; // the set of split values with the min
            // variance
            int[] bucketSize = null;
            int[] minBucketSize = null; // the bucketsize array of the vp with
            // min variance
            double[] lower = null;
            double[] minLower = null;
            double[] upper = null;
            double[] minUpper = null; // lower, upper bound of distances of
            // each sub-cluster
            for (int i = 0; i < toUse.length; i++)
            {
                // if the vp is already used, go to the next one
                if (!toUse[i])
                    continue;

                // find the initial clustering to run k-means, each item in
                // means should be a
                // different real distance value that exists
                // thus, if the length of means is less than SVF, all the
                // distinct distance values
                // have been returned, no need to run k-means
                means = bucketInitialClustering(distance[i], first, last, SVF);

                if (means.length < SVF)
                {
                    if (childrenNumber > means.length) // some other vp can
                        // partition the cluster
                        // into more sub-clusters, ignore current vp
                        continue;
                } else
                // run the k-means with means as the initial clustering
                {
                    kMeans(distance[i], first, last, means, logger);
                }

                split = new double[means.length - 1];
                Arrays.sort(means);
                for (int j = 0; j < split.length; j++)
                    split[j] = (means[j] + means[j + 1]) / 2;

                // split values are available, compute the variance
                bucketSize = new int[split.length + 1];
                for (int j = 0; j < bucketSize.length; j++)
                    bucketSize[j] = 0;

                lower = new double[split.length + 1];
                upper = new double[split.length + 1];
                for (int j = 0; j < lower.length; j++)
                {
                    lower[j] = Double.POSITIVE_INFINITY;
                    upper[j] = Double.NEGATIVE_INFINITY;
                }
                for (int j = first; j <= last; j++) // for each point, find
                // which bucket it belongs
                // to. left bound inclusive, right bound
                // exclusive
                {
                    int k = 0;
                    while ((k < split.length) && (distance[i][j] >= split[k]))
                        k++;

                    bucketSize[k]++;
                    if (lower[k] > distance[i][j])
                        lower[k] = distance[i][j];

                    if (upper[k] < distance[i][j])
                        upper[k] = distance[i][j];
                }

                var = 0;
                // varx = E(x^2) - (Ex)^2, since Ex is fixed, we dont compute
                // it.
                for (int j = 0; j < bucketSize.length; j++)
                    var += bucketSize[j] * bucketSize[j];

                // compare with currnet min variance
                if ((bucketSize.length > childrenNumber)
                        || ((bucketSize.length == childrenNumber) && (minVar > var)))
                {
                    minVar = var;
                    minSplit = split;
                    minVP = i;
                    minBucketSize = bucketSize;
                    minLower = lower;
                    minUpper = upper;
                }
                if (bucketSize.length > childrenNumber)
                {
                    childrenNumber = bucketSize.length;
                }

            }// end of loop for each vp

            // the best vp is found , split and bucketsize array are ready,
            // partition the cluster
            // now
            // if childrenNumber ==1, the cluster can not be partitioned by any
            // vp, add a finished
            // task to task list
            if (childrenNumber == 1)
            {
                lower = task.lower;
                upper = task.upper;
                for (int i = 0; i < toUse.length; i++)
                    if (toUse[i])
                    {
                        lower[i] = distance[i][first];
                        upper[i] = distance[i][first];
                        toUse[i] = false;
                    }

                ClusteringKMeansTask newCKMTask = new ClusteringKMeansTask(
                        first, last, lower, upper, toUse);
                taskList.addLast(newCKMTask);
                return;
            }

            // if childrennumber != 1, continue further process
            // set bucketFirst array, bucketFirst[i] is the offset of the first
            // element of bucket i
            int[] bucketFirst = new int[childrenNumber + 1];
            System.arraycopy(minBucketSize, 0, bucketFirst, 0, childrenNumber);
            for (int i = 1; i < childrenNumber; i++)
                bucketFirst[i] += bucketFirst[i - 1];
            System.arraycopy(bucketFirst, 0, bucketFirst, 1, childrenNumber);
            bucketFirst[0] = 0;
            for (int i = 0; i <= childrenNumber; i++)
                bucketFirst[i] += first;

            // bucketPointer[i] points to the first place of bucket i to be
            // sorted
            int[] bucketPointer = new int[childrenNumber];
            System.arraycopy(bucketFirst, 0, bucketPointer, 0, childrenNumber);

            // ready to arrange, for each bucket, for each value, if it doesn't
            // belong to the
            // bucket,
            // exchange it with an element in the correct bucket
            double tempDouble;
            for (int i = 0; i < childrenNumber; i++)
            {
                for (int j = bucketPointer[i]; j < bucketFirst[i + 1]; j++)
                {
                    while (true)
                    {
                        // compute the bucket id of current point
                        int k = 0; // k is the correct id
                        while ((k < minSplit.length)
                                && (distance[minVP][j] >= minSplit[k]))
                            k++;

                        // if current point belongs to current bucket, go to
                        // next point
                        if (k == i)
                            break;

                        // exchange the point object
                        Collections.swap(data, j, bucketPointer[k]);

                        // exchange the distance values to all vps
                        for (int t = 0; t < distance.length; t++)
                        {
                            tempDouble = distance[t][j];
                            distance[t][j] = distance[t][bucketPointer[k]];
                            distance[t][bucketPointer[k]] = tempDouble;
                        }

                        bucketPointer[k]++;
                    } // end of while
                }
            }

            // the data list and distance array have been re-arranged, now
            // create sub-clusters, and
            // put them into task list
            toUse[minVP] = false;
            boolean done = true;
            // check whether the sub-clusters need further partition
            for (int i = 0; i < toUse.length; i++)
                if (toUse[i])
                {
                    done = false;
                    break;
                }

            if (done) // if no further partition (all vp are used), add to the
            // end of the task
            // list
            {
                for (int i = 0; i < childrenNumber; i++)
                {
                    lower = task.lower.clone();
                    lower[minVP] = minLower[i];
                    upper = task.upper.clone();
                    upper[minVP] = minUpper[i];

                    ClusteringKMeansTask newCKMTask = new ClusteringKMeansTask(
                            bucketFirst[i], bucketFirst[i + 1] - 1, lower,
                            upper, toUse.clone());
                    taskList.addLast(newCKMTask);
                }
            } else
            // further partition is needed, add to the head of the task list
            {
                for (int i = childrenNumber - 1; i >= 0; i--)
                {
                    lower = task.lower.clone();
                    lower[minVP] = minLower[i];
                    upper = task.upper.clone();
                    upper[minVP] = minUpper[i];

                    ClusteringKMeansTask newCKMTask = new ClusteringKMeansTask(
                            bucketFirst[i], bucketFirst[i + 1] - 1, lower,
                            upper, toUse.clone());
                    taskList.addFirst(newCKMTask);
                }
            }

        }

        /**
         * find the initial clustering to run k-means, each item in retuned
         * array should be a real distinct distance value that exists, thus, if
         * the length of returned array is less than SVF, all the distinct
         * distance values have been returned
         * 
         * @param distance
         *            the array containing all the double value to find means on
         * @param first
         *            offset of the first element
         * @param last
         *            offset of the last element
         * @param SVF
         *            number of means/distinct values to find
         * @return an array of means, each element is different from others, if
         *         its length is shorter than SVF, all distinct values are in it
         */
        private double[] bucketInitialClustering(double[] distance,
                final int first, final int last, final int SVF)
        {
            final int bucketNumber = Math.max(Math.min((last - first + 1) / 10,
                    50 * SVF), SVF);

            double min, max; // min , max distance to a vp
            min = Double.POSITIVE_INFINITY;
            max = Double.NEGATIVE_INFINITY;
            for (int j = first; j <= last; j++)
            {
                if (distance[j] > max)
                    max = distance[j];

                if (distance[j] < min)
                    min = distance[j];
            }

            // if min == max, can not partition by current vp.
            if (max == min)
            {
                double[] result = new double[1];
                result[0] = distance[first];
                return result;
            }

            // compute the bucket size
            int[] bucketSize = new int[bucketNumber];
            for (int i = 0; i < bucketNumber; i++)
                bucketSize[i] = 0;

            final double bucketWidth = (max - min) / bucketNumber;
            for (int i = first; i <= last; i++)
            {
                int temp = (int) ((distance[i] - min) / bucketWidth);
                if (temp >= bucketNumber)
                    temp = bucketNumber - 1;
                bucketSize[temp]++;
            }

            // find the buckets whose size is local max
            boolean[] isLocalMax = new boolean[bucketNumber];
            isLocalMax[0] = (bucketSize[0] >= bucketSize[1]) ? true : false;
            isLocalMax[bucketNumber - 1] = (bucketSize[bucketNumber - 1] >= bucketSize[bucketNumber - 2]) ? true
                    : false;
            for (int i = 1; i <= bucketNumber - 2; i++)
                isLocalMax[i] = ((bucketSize[i] >= bucketSize[i - 1]) && (bucketSize[i] >= bucketSize[i + 1])) ? true
                        : false;

            // remove consecutive local max bucket
            int loop = 0;
            while (loop < bucketNumber)
            {
                if (!isLocalMax[loop])
                    loop++;
                else
                {
                    int lastMax = loop + 1;
                    while ((lastMax < bucketNumber) && isLocalMax[lastMax])
                        lastMax++;
                    for (int i = loop; i < lastMax; i++)
                        isLocalMax[i] = false;
                    isLocalMax[(loop + lastMax - 1) / 2] = true;
                    loop = lastMax + 1;
                }
            }

            int localMaxBucketNumber = 0; // number of positive-size local max
            // bucket
            for (int i = 0; i < bucketNumber; i++)
                if (isLocalMax[i] && (bucketSize[i] > 0))
                    localMaxBucketNumber++;

            if (localMaxBucketNumber >= SVF) // there are enough bins, find
            // svf largest ones,
            // return the middle point of them
            {
                boolean[] isLargest = new boolean[bucketNumber];
                for (int i = 0; i < bucketNumber; i++)
                    isLargest[i] = false;

                for (int i = 0; i < SVF; i++)
                {
                    int maxSize = 0;
                    int maxId = 0;
                    for (int j = 0; j < bucketNumber; j++)
                    {
                        if (isLocalMax[j] && !isLargest[j]
                                && (bucketSize[j] > maxSize))
                        {
                            maxSize = bucketSize[j];
                            maxId = j;
                        }
                    }
                    isLargest[maxId] = true;
                }
                double[] result = new double[SVF];
                int counter = 0;
                for (int i = 0; i < bucketNumber; i++)
                {
                    if (isLargest[i])
                    {
                        result[counter] = min + (i + 0.5) * bucketWidth;
                        counter++;
                    }
                }

                return result;
            } else
            // no enough local max bucket, for each local max bin, find a value
            // in it, then
            // find dist
            {
                double[] result = new double[SVF];
                int counter = 0;
                // for each local max bucket, find a value in it.
                for (int i = first; i <= last; i++)
                {
                    int temp = (int) ((distance[i] - min) / bucketWidth);
                    if (temp >= bucketNumber)
                        temp = bucketNumber - 1;
                    if (isLocalMax[temp])
                    {
                        result[counter] = distance[i];
                        isLocalMax[temp] = false;
                        counter++;
                        if (counter >= localMaxBucketNumber)
                            break;
                    }
                }

                // find distinct values
                for (int i = first; i <= last; i++)
                {
                    boolean isDistinct = true;
                    for (int j = 0; j < counter; j++)
                        if (distance[i] == result[j])
                        {
                            isDistinct = false;
                            break;
                        }

                    if (isDistinct)
                    {
                        result[counter] = distance[i];
                        counter++;
                        if (counter >= SVF)
                            break;
                    }
                }

                if (counter < SVF) // no enough distinct values
                {
                    double[] finalResult = new double[counter];
                    System.arraycopy(result, 0, finalResult, 0, counter);
                    return finalResult;
                } else
                    return result;
            }

        }

        /**
         * run the k-means given the initial clustering. after running, the
         * results are stored in the argument, means
         * 
         * @param distance
         *            the array containing all the double value to run on
         * @param first
         *            offset of the first element
         * @param last
         *            offset of the last element
         * @param means
         *            double array of initial values of means, after the method
         *            runs, its values are the final means
         * @param logger
         */
        private void kMeans(double[] distance, final int first, final int last,
                double[] means, Logger logger)
        {
            final double stop = 0.1;
            final int size = last - first + 1;
            final int clusterNumber = means.length;
            short[] clusterId = new short[size];
            double[] split = new double[clusterNumber - 1];

            double sum = 0, newSum = 0;

            double[] clusterSum = new double[clusterNumber];
            int[] clusterSize = new int[clusterNumber];
            int counter = 0;
            while ((counter < 2) || Math.abs(newSum - sum) / sum > stop)
            {
                sum = newSum;

                // set the cluster id of each value
                Arrays.sort(means);
                for (int j = 0; j < split.length; j++)
                    split[j] = (means[j] + means[j + 1]) / 2;

                for (int i = 0; i < size; i++)
                {
                    clusterId[i] = 0; // k is the correct id
                    while ((clusterId[i] < split.length)
                            && (distance[first + i] >= split[clusterId[i]]))
                        clusterId[i]++;
                }

                // compute new mean and new sum
                for (int i = 0; i < clusterNumber; i++)
                {
                    clusterSum[i] = 0;
                    clusterSize[i] = 0;
                }
                for (int i = 0; i < size; i++)
                {
                    clusterSum[clusterId[i]] += distance[first + i];
                    clusterSize[clusterId[i]]++;
                }

                newSum = 0;
                for (int i = 0; i < clusterNumber; i++)
                {
                    means[i] = clusterSum[i] / clusterSize[i];
                    newSum += means[i];
                }

                counter++;
                if ((counter > 100) && (counter % 100 == 0))
                    System.out.println("counter= " + counter + ", too large!");

                if (Debug.debug)
                    logger.finer("counter= " + counter + ",  sum= " + sum
                            + ",  new sum= " + newSum);
            }

        }
    },
    EXCLUDEDMIDDLE
    {
        double maxR = 0;

        /**
         * @param R
         */
        public void setMaxRadius(double R)
        {
            this.maxR = R;
        }

        /**
         * @param metric
         * @param pivots
         * @param data
         * @param numPartitions
         * @return
         */
        public PartitionResults partition(Metric metric, IndexObject[] pivots,
                List<? extends IndexObject> data, int numPartitions, int maxLS)
        {
            return partition(metric, pivots, data, 0, data.size(),
                    numPartitions, maxLS);
        }

        /**
         * @param metric
         * @param pivots
         * @param data
         * @param first
         * @param size
         * @param numPartitions
         * @return
         */
        public PartitionResults partition(Metric metric, IndexObject[] pivots,
                List<? extends IndexObject> data, int first, int size,
                int numPartitions, int maxLS)
        {
            PivotWisePartition pm = new PivotWisePartition();
            pm.setMaxRadius(maxR);
            return pm.partition(metric, pivots, data, first, size,
                    numPartitions, maxLS);
        }
    },
    CGHT
    {
        /**
         * @param R
         */
        public void setMaxRadius(double R)
        {
        }

        /**
         * @param metric
         * @param pivots
         * @param data
         * @param numPartitions
         * @return
         */
        public PartitionResults partition(Metric metric, IndexObject[] pivots, List<? extends IndexObject> data, int numPartitions, int maxLS)
        {
            return null;
        }

        /**
         * @param metric
         * @param pivots
         * @param data
         * @param first
         * @param size
         * @param numPartitions
         * @return
         */
        public PartitionResults partition(Metric metric, IndexObject[] pivots, List<? extends IndexObject> data, int first, int size,
                int numPartitions, int maxLS)
        {
            return null;
        }
    },
    GHT
    {
        /**
         * @param R
         */
        public void setMaxRadius(double R)
        {
        }

        /**
         * @param metric
         * @param pivots
         * @param data
         * @param numPartitions
         * @return
         */
        public PartitionResults partition(Metric metric, IndexObject[] pivots, List<? extends IndexObject> data, int numPartitions, int maxLS)
        {
            return null;
        }

        /**
         * @param metric
         * @param pivots
         * @param data
         * @param first
         * @param size
         * @param numPartitions
         * @return
         */
        public PartitionResults partition(Metric metric, IndexObject[] pivots, List<? extends IndexObject> data, int first, int size,
                int numPartitions, int maxLS)
        {
            return null;
        }
    };
    /*
     * constant used to hold input parameters
     * Added by Kewei Ma
     */
    public static String pm;
    public static double r;
    public static int countRN = 0;
}
