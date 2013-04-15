/**
 * GeDBIT.index.algorithms.PivotSelectionMethod 2006.06.16
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.01.25: Added, by Willard
 */
package GeDBIT.index.algorithms;

import java.util.List;

import GeDBIT.dist.Metric;
import GeDBIT.type.IndexObject;

/**
 * @author Willard
 */
public interface PivotSelectionMethod {
    /**
     * @param metric        X
     * @param data
     * @param numPivots
     * @return
     */
    int[] selectPivots(Metric metric, List<? extends IndexObject> data, final int numPivots);

    /**
     * @param metric
     * @param data
     * @param first
     * @param dataSize
     * @param numPivots
     * @return
     */
    int[] selectPivots(Metric metric, List<? extends IndexObject> data, int first, int dataSize,
            final int numPivots);
}
