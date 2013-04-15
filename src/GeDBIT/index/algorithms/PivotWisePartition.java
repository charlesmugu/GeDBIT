/**
 * GeDBIT.app.algorithms.PivotWisePartition.java 2006.06.28
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.06.28: Created, by Rui Mao
 */
package GeDBIT.index.algorithms;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.Collections;

import GeDBIT.dist.Metric;
import GeDBIT.index.VPInternalNode;
import GeDBIT.type.IndexObject;
import GeDBIT.util.Debug;
import GeDBIT.util.Histogram;

/**
 * This is a utility class of data partition algorithm.  It partitions data pivot by pivot.
 * Which pivot to partition with depends on the result of partition based on that pivot
 * @author Rui Mao
 * @version 2006.06.28
 */
class PivotWisePartition implements PartitionMethod
{
    int MaxLS = 0;
    Logger logger = null;
    int SVF = 0;
    double [][] distance = null;
    double MaxRadius = 0; 
    double HistogramScale = 10;

    public void setMaxRadius(double R)
    {
        this.MaxRadius = R;
    }

    public PartitionResults partition(Metric metric, IndexObject[] pivots, List<? extends IndexObject> data, int numPartitions, int maxLS)
    {
        return partition(metric, pivots, data, 0, data.size(), numPartitions, maxLS);
    }

    public PartitionResults partition(Metric metric, IndexObject[] pivots, List<? extends IndexObject> data, int first,
            int size, int numPartitions, int maxLS) 
    {
        double [][] distance = new double[pivots.length][size];
        
        for (int i=first; i< first+size; i++ )
            for (int j=0; j< pivots.length; j++)
                distance[j][i-first] = metric.getDistance(data.get(i), pivots[j]);

        return partition(distance, pivots, data.subList(first, first+size), numPartitions, maxLS, Logger.getLogger("GeDBIT.index"), this.MaxRadius);
    }
    /**
     * given pivots, this method partition the dataset pivot by pivot. 
     *
     * @param distance distances from each data point (column) to each piovt(row)
     * @param pivot the pivots array, each element can be computed distance on
     * @param data the source data list to split, each element is a {@link RecordObject}
     * @param SVF partition number induced by each vantage point
     * @param maxLS max leaf size, if a cluster has less size, don't partition further
     * @return a list, the first element is a List [], which contains lists of data of each child,
     * the second element is of type double [][], which is the lowerRange, the min distance from each child
     * to each vantage point, child*VP, the third element is of type double [][], which is the upperRange, 
     * the max distance from each child to each vantage point, child*vp
     */
     public PartitionResults partition (double [][] distance, IndexObject[] pivot, List<? extends IndexObject> data, 
             final int SVF, final int maxLS, Logger logger, double R)
    {
        if (Debug.debug)
            logger.finer("Pivot-wise Partition");
        
        this.logger = logger;
        this.MaxLS = maxLS;
        this.SVF = SVF;
        this.distance = distance;
        this.MaxRadius = R;
        //compute all the distance
        final int numP = pivot.length;
        
        
        //maintain a list of partition task, each task contains: 
        // 1. the first last offset of the cluster data in the data array, two Integers
        // 2. the distance ranges to all vps, two 1-d double array the first is the lower bound, then the upper bound
        //    if the upper bound to a pivot is -1, then the pivot is not used
        
        LinkedList<PartitionTask> taskList = new LinkedList<PartitionTask>();
        taskList.addFirst( new PartitionTask(data, pivot) );
        
        //maintain a list of partitions that are done.  finally, use these completed partitions to create an index node.
        LinkedList <PartitionTask> completedTask = new LinkedList<PartitionTask>();
         
        // the loop to partition each cluster
        while( !taskList.isEmpty())
        {
            PartitionTask task = taskList.removeFirst();
            
            //if task is finished or is a leaf, move it to completed task list.
            if ( task.isDone() || task.isLeaf(distance, maxLS))
            {
                completedTask.add(task);
                continue;
            }
                
            //otherwise, process a partition task
            //1. select a best pivot
            //2. partition based on this pivot
            //3. put new tasks into task list
            //4. sort the data list and distance array.
            taskList.addAll(0, processTask(task) );
        }
        
        //now partition is done, return result's in required format.
        final int childrenNumber = completedTask.size();  //may need to check whether cluster number ==1
        //if (childrenNumber ==1)
            //System.out.println("cluster can not be partitioned!");

        List<List<? extends IndexObject>> subDataList = new ArrayList<List<? extends IndexObject>>(childrenNumber);
        double [][]allLower = new double [childrenNumber][numP];
        double [][] allUpper = new double [childrenNumber][numP];

        for (int i = 0; i < childrenNumber; i++) 
        {
            PartitionTask task = completedTask.get(i);
            subDataList.add(data.subList(task.first, task.last));
            for (int j=0; j<numP; j++)
            {
                allLower[i][j] =task.lower[j];
                allUpper[i][j] = task.upper[j];
            }
        }

        VPInternalNode predicate = new VPInternalNode(pivot, allLower, allUpper, data.size(), new long[childrenNumber]);
        PartitionResults partitionResult = new PartitionResults(subDataList, predicate);
        
        return partitionResult;
    }
    
    
    /**
     * process a partition task.  
     * Note 1: the task should be checked whether can be a leaf node before calling this method.
     * Note 2: the task should also be checked wheter all the points are identical 
     * 1. select a best pivot
     * 2. partition based on this pivot
     * 3. create new tasks and return
     * 4. sort the data list and distance array, put data and distances belongs to the same sub-clusters together.
     */
    private List<PartitionTask> processTask(PartitionTask task)
    {
        final int pivotNum = task.pivot.length;
        
        double obj = Double.NEGATIVE_INFINITY;  //object function value of partition, the larger the better.
        int pivot = 0; 
        double largestRange = 0;
        double [] clusterLeftBound = null;  //inclusive
        double [] clusterRightBound = null; //inclusive
        double [] clusterFirstOffsetDouble = null;   //inclusive, will cast to integer
        
        //select pivot to partition with
        double tempR = this.MaxRadius;
        while ( (obj == Double.NEGATIVE_INFINITY) && !task.isDone())
        {
            for (int i=0; i< pivotNum; i++)
            {
                //skip used pivot
                if (task.upper[i] != -1)
                    continue;
                
                //partition by one pivot, return a 2-d double array.  no empty cluster allowed 
                //0th row: consists of only one element, the objective function value, the larger the better. can be the pruning rate etc.
                //1st row: cluster left bound, left inclusive
                //2nd row: range, left and right
                //3rd row: cluster first offset
                //4th row: cluster right bound, right inclusive
                double [][] result = partitionByOnePivot(i, tempR, task);
                
                if (obj < result[0][0])
                {
                    obj = result[0][0];
                    clusterLeftBound = result[1];
                    clusterRightBound = result[4];
                    clusterFirstOffsetDouble = result[3];
                    pivot = i;
                }

                if (result[2][0] == result[2][1])
                {
                    task.lower[i] = result[2][0];
                    task.upper[i] = result[2][1];
                }
                else
                {
                    largestRange = (largestRange > result[2][1] - result[2][0]) ? largestRange:result[2][1] - result[2][0]; 
                }
            }
            
            if (obj != Double.NEGATIVE_INFINITY)
                break;
            
            tempR = largestRange/4;
            
        }
        
        //partition by the pivot selected
        int [] clusterFirstOffset = new int [clusterFirstOffsetDouble.length];
        for (int i=0; i< clusterFirstOffset.length; i++)
            clusterFirstOffset[i] = (int) clusterFirstOffsetDouble[i];
        
        sort(clusterLeftBound, clusterFirstOffset, task, pivot );
        
        //create partition tasks and then return
        ArrayList<PartitionTask> children = new ArrayList<PartitionTask>(clusterFirstOffset.length);
        for (int i=0; i< clusterFirstOffset.length; i++)
        {
            //skip empty cluster
            if (  ( (i== clusterFirstOffset.length-1) && (clusterFirstOffset[i] == task.last) )
                ||( (i < clusterFirstOffset.length-1) && (clusterFirstOffset[i] == clusterFirstOffset[i+1]) ) )
                continue;
                
            double [] l = (double []) task.lower.clone();
            double [] u = (double []) task.upper.clone();
            l[pivot] = clusterLeftBound[i];
            u[pivot] = clusterRightBound[i];
            if (i == clusterFirstOffset.length -1)
                children.add( new PartitionTask(task.data, clusterFirstOffset[i], task.last, task.pivot, l, u));
            else
                children.add( new PartitionTask(task.data, clusterFirstOffset[i], clusterFirstOffset[i+1], task.pivot, l, u));
        }
        
        return children;

    }
    
    /**
     * partition by the distances to one pivot, return a double array. no empty cluster allowed
     * 0th row consists of only one element, the objective function value, the larger the better. can be the pruning rate etc.
     * 1st row: cluster left bound,left inclusive, right exclusive
     * 2nd row: range, left and right
     * 3rd row: cluster first offset
     * 4th row: cluster right bound, left exclusive, right inclusive
     * 
     */
    double [][] partitionByOnePivot(int pivot, double R, PartitionTask task)
    {
        ArrayList<Histogram.BinInfo> bin = Histogram.completeOneDHistogram(-R/this.HistogramScale/2, 
                R/this.HistogramScale, this.distance[pivot], task.first, task.last);
        
        double [][] result = new double [5][];
        result[2] = new double[]{bin.get(0).lower(), bin.get(bin.size()-1).upper()};
        
        //return if range is not large enough
        if ( (bin.size() < 3) || ( bin.get(bin.size()-2).upper() - bin.get(1).lower()) <= 2*R )
        {
            partitionSmallRange(result, bin, task);
            return result;
        }
        
        //range is large enough, find the pruning rate for all the possible 3-partitions.
        //if the size of the 3 clusters are a, b, c, where b has widht 2R, then the pruning rate is:
        //r = 2ac/(a+b+c)^2,  since a+b+c is constant for all partitions, we can just use r=ac for comparison
        int bestLeftBoundary =0;   //the offset of the first bin in the middle part
        int bestRightBoundary = 0; //the offset of the last bin in the middle part
        int bestA=0, bestB=0;      //cluster size of the best partition.
        double maxR = -1;  //the max value of r=ac, for comparison.  the larger the better.
        
        double a=0, b=0;
        int rightBoundary = 0;
        for(int leftBoundary = 1; leftBoundary< bin.size()-1; leftBoundary++)
        {
            if (bin.get(bin.size()-2).upper() - bin.get(leftBoundary).lower() <2*R)//already reach the right ends
                break;
            
            //compute a
            a = 0;
            for (int i=0; i<leftBoundary; i++)
                a+=bin.get(i).size();
            
            //find right boundary
            rightBoundary = leftBoundary;
            b = bin.get(rightBoundary).size();
            while( (rightBoundary< bin.size()-2) && ( (bin.get(rightBoundary).upper() - bin.get(leftBoundary).lower())<2*R) )
            {
                rightBoundary++;
                b += bin.get(rightBoundary).size();
            }
            
            //already reach the right ends.  already check at the beginning of the loop, just for safety
            if (rightBoundary == bin.size()-1) 
                break;
            
            //comparison with the best-so-far
            if (maxR < a*(task.last - task.first -b -a) )
            {
                maxR = a*(task.last - task.first - b - a);
                bestLeftBoundary = leftBoundary;
                bestRightBoundary = rightBoundary;
                bestA = (int)a;
                bestB = (int)b;
            }
        }
        
        //set the cluster information and return
        result[0] = new double []{maxR};
        result[1] = new double [3];
        result[3] = new double [3];
        result[4] = new double [3];
        
        result[1][0] = bin.get(0).lower();
        result[3][0] = task.first;
        result[4][0] = bin.get(bestLeftBoundary-1).upper();
        
        result[1][1] = bin.get(bestLeftBoundary).lower();
        result[3][1] = task.first + bestA;
        result[4][1] = bin.get(bestRightBoundary).upper();
        
        result[1][2] = bin.get(bestRightBoundary+1).lower();
        result[3][2] = task.first + bestA + bestB;
        result[4][2] = bin.get(bin.size()-1).upper();
        
        return result;
        
    }
    
    void partitionSmallRange(double [][] result, ArrayList<Histogram.BinInfo> bin, PartitionTask task)
    {
        boolean isDiscrete = true;
        for (Histogram.BinInfo b: bin)
            if (b.upper() != b.lower())
            {
                isDiscrete = false;
                break;
            }
        
        if (isDiscrete) //if discrete, return each discrete value as a cluster
        {
            result[0] = new double[]{0};
            result[1] = new double[bin.size()];
            for (int i=0; i< bin.size(); i++)
                result[1][i] = bin.get(i).lower();
            result[4] = (double []) result[1].clone();
            result[3] = new double[bin.size()];
            result[3][0] = task.first;
            for (int i=1; i<bin.size(); i++)
                result[3][i] = result[3][i-1] + bin.get(i-1).size();
            
        }
        else
        {
            result[0] = new double[]{Double.NEGATIVE_INFINITY};
            result[1] = new double[]{result[2][0]};
            result[4] = new double[]{result[2][1]};
            result[3] = new double[]{task.first};
        }

    }
    
    
    /**
     * sort the array and list into groups, based on given split values and group sizes
     * @param split
     * @param count
     * @param distance
     * @param data
     */
    void sort(double [] clusterLeftBound, int [] clusterFirstOffset, PartitionTask task, int pivot )
    {
        double temp = 0;
        int toCluster = 0;
        final int clusterNum = clusterFirstOffset.length;
        int [] currentOffset = (int []) clusterFirstOffset.clone();
        for (int cluster = 0; cluster<clusterNum; cluster++)
        {
            for(; currentOffset[cluster] < ( (cluster == clusterNum-1)?task.last: clusterFirstOffset[cluster+1] )
                ; currentOffset[cluster]++)
            {
                toCluster = cluster+1;
                while(toCluster!= cluster)
                {
                    //compute tocluster
                    for(toCluster=0; toCluster<clusterNum-1; toCluster++)
                    {
                        if (this.distance[pivot][ currentOffset[cluster] ] < clusterLeftBound[toCluster+1])
                            break;
                    }
                    
                    if (toCluster!= cluster)  //exchange
                    {
                        Collections.swap(task.data, currentOffset[cluster], currentOffset[toCluster]);
                        
                        for (int i=0; i< task.pivot.length; i++)
                        {
                            temp = distance[i][currentOffset[cluster] ];
                            distance[i][ currentOffset[cluster] ] = distance[i][ currentOffset[toCluster] ];
                            distance[i][ currentOffset[toCluster] ] = temp;
                        }
                        currentOffset[toCluster] ++;
                    }//end of exchange
                }//end of while
            }//end of one cluster
        }
        
    }


    class PartitionTask 
    {
        //Metric metric;
        List <? extends IndexObject> data;  //data to partition
        final int first;  //offset of the first point in the data list, inclusive
        final int last;   //offset of the last point in the data list, exclusive.
        IndexObject[] pivot;  //pivots based on distance to which to partition the data
        double [] upper;  //upper.length = lower.length = pivot.length.
        double [] lower;  //upper and lower bounds to used pivots.  computed by previous partition steps.
                          //if upper[i] == -1, then pivot[i] is not used yet.

        /**
         * Constructor of PartitionTask.  Assume no pivots were used
         * @param data data to partition, copy by reference
         * @param pivot pivots to use, copy by reference
         */
        public PartitionTask(List <? extends IndexObject> data, IndexObject[] pivot)
        {
            this(data, 0, data.size(), pivot, new double[pivot.length], new double[pivot.length]);
            for (int i = 0; i <pivot.length; i++)
                upper[i] = -1;
        }
        
        /**
         * Constructor of PartitionTask
         * @param data data to partition, copy by reference
         * @param first offset of the first point in the data list, inclusive
         * @param last offset of the last point in the data list, exclusive.
         * @param pivot pivots to use, copy by reference
         * @param upper upper bounds to used pivots, copy by value
         * @param lower lower bounds to used pivots, copy by value
         */
        public PartitionTask( List <? extends IndexObject> data, int first, int last, IndexObject[] pivot, double [] lower, double [] upper)
        {
            if ( (data == null) || (pivot == null) || (upper == null) || (lower ==null) )
                throw new IllegalArgumentException("Null argument!");
            
            if ( first >= last)
                throw new IllegalArgumentException("Empty data list to partition!");
            
            if ( (pivot.length != upper.length) || (upper.length != lower.length) )
                throw new IllegalArgumentException("Arrays of inconsistent size!");
            
            //this.metric = metric;
            this.data = data;
            this.first = first;
            this.last = last;
            this.pivot = pivot;
            this.upper = (double []) upper.clone();
            this.lower = (double []) lower.clone();

        }
        
        /**
         * check whether there are still pivots to use
         * @return true if no pivots to use
         */
        boolean isDone()
        {
            for (int i=0; i< upper.length; i++)
                if ( upper[i] == -1)
                    return false;
            return true;
        }
        
        /**
         * Check whether this partition task is small enough to form a leaf node.
         * If yes, compute the range to all unused pivots.
         * @param mls maximum leaf node size
         * @param distance distances from each data point (column) to each piovt(row)
         * @return
         */
        boolean isLeaf(double [][] distance, int mls)
        {
            if (last - first > mls)
                return false;
            
            for (int i=0; i<pivot.length; i++)
            {
                if (upper[i] != -1) //pivot i is already used
                    continue;
                
                upper[i] = Double.NEGATIVE_INFINITY;
                lower[i] = Double.POSITIVE_INFINITY;
                for (int j=first; j<last; j++ )
                {
                    upper[i] = (upper[i]> distance[i][j])? upper[i] : distance[i][j];
                    lower[i] = (lower[i]< distance[i][j])? lower[i] : distance[i][j];
                }
            }
            
            return true;
        }
        
    }
}
