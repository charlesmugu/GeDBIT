/**
 * GeDBIT.index.algorithms.PartitionResults 2006.06.16
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.01.25: Added, by Willard
 */
package GeDBIT.index.algorithms;

import java.util.List;

import GeDBIT.type.IndexObject;
import GeDBIT.index.InternalNode;

/**
 * @author Willard
 */
public class PartitionResults {

    private List<List<? extends IndexObject>> listOfPartitions;
    private InternalNode iNode;

    @SuppressWarnings("unused")
    private PartitionResults() {
    }

    /**
     * @param subDataList
     * @param iNode
     */
    public PartitionResults(List<List<? extends IndexObject>> subDataList,
	    InternalNode iNode) {
	this.listOfPartitions = subDataList;
	this.iNode = iNode;
    }

    /**
     * @return the number of partitions
     */
    public int size() {
	return listOfPartitions.size();
    }

    /**
     * @param partition
     */
    public void addPartition(List<IndexObject> partition) {
	listOfPartitions.add(partition);
    }

    /**
     * @param iNode
     */
    public void setNode(InternalNode iNode) {
	this.iNode = iNode;
    }

    /**
     * @param index
     * @return the partition at the given index (a partition is a {@link List}
     *         of {@link IndexObject}s
     * @throws IndexOutOfBoundsException
     */
    public List<? extends IndexObject> getPartition(int index)
	    throws IndexOutOfBoundsException {
	return listOfPartitions.get(index);
    }

    /**
     * TODO javadoc
     * 
     * @return
     */
    public InternalNode getInternalNode() {
	return iNode;
    }

}
