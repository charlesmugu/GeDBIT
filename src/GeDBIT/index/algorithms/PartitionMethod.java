/**
 * GeDBIT.index.algorithms.PartitionMethod 2006.06.16
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.05.25: Added, by Willard
 */
package GeDBIT.index.algorithms;

import java.util.List;

import GeDBIT.dist.Metric;
import GeDBIT.type.IndexObject;

/**
 * 
 * @author Willard
 *
 */
public interface PartitionMethod
{
    PartitionResults partition(Metric metric, IndexObject[] pivots, List<? extends IndexObject> data, int numPartitions, int maxLS);
    PartitionResults partition(Metric metric, IndexObject[] pivots, List<? extends IndexObject> data, int first, int size, int numPartitions, int maxLS);
    void setMaxRadius(double R);
}
