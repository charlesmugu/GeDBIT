/**
 * GeDBIT.index.IndexNode 2006.05.12
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.05.12: Modified from jdb v1.0, by Rui Mao
 */

package GeDBIT.index;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import GeDBIT.type.IndexObject;

/**
 * An index node is a node of a database index tree. This interface is the base
 * class for all different index structures, such as radius-base(RBT), general
 * hyper-plane (GHT) and vantage point (VP).
 * 
 * @author Rui Mao, Neha Singh, Willard
 * @version 2006.05.12
 */

abstract class IndexNode implements Externalizable {
    private IndexObject[] pivots;
    private int size;

    public IndexNode() {
    }

    public IndexNode(IndexObject[] pivots, int size) {
	if (pivots == null)
	    throw new IllegalArgumentException("vps cannot be null");
	this.pivots = pivots;

	if (size < 0)
	    throw new IllegalArgumentException("size cannot be less than '0'");
	this.size = size;

    }

    /**
     * @return the number of pivots.
     */
    int numPivots() {
	return pivots.length;
    }

    /**
     * @return the number of data points in the subtree with the current node as
     *         the root.
     */
    int size() {
	return size;
    }

    /**
     * Return a reference to a pivot
     * 
     * @param pivotIndex
     *            index of the pivot to be return
     * @return the key value of the pivot
     */
    IndexObject getPivot(int pivotIndex) {
	return pivots[pivotIndex];
    }

    public void writeExternal(ObjectOutput out) throws IOException {
	out.writeInt(pivots.length);
	for (int i = 0; i < pivots.length; i++) {
	    out.writeObject(pivots[i]);
	}
	out.writeInt(size);
    }

    public void readExternal(ObjectInput in) throws IOException,
	    ClassNotFoundException {
	pivots = new IndexObject[in.readInt()];
	for (int i = 0; i < pivots.length; i++) {
	    pivots[i] = (IndexObject) in.readObject();
	}
	size = in.readInt();
    }
}
