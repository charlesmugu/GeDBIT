/**
 * GeDBIT.index.InternalNode 2006.05.12
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.05.12: created, by Rui Mao, Willard
 */

package GeDBIT.index;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import GeDBIT.type.IndexObject;

/**
 * The interface for all internal nodes.
 * 
 * @author Rui Mao, Willard
 * @version 2006.05.12
 */

public abstract class InternalNode extends IndexNode {
    protected long[] childAddresses;

    public InternalNode() {
	super();
    }

    protected InternalNode(IndexObject[] pivots, int size, long[] childAddresses) {
	super(pivots, size);
	if (childAddresses == null)
	    throw new IllegalArgumentException(
		    "InternalNode childAddresses cannot be null");
	this.childAddresses = childAddresses;
    }

    /**
     * @return the number of children.
     */
    int numChildren() {
	return childAddresses.length;
    }

    /**
     * Return the address of a child
     * 
     * @param childIndex
     *            index of the child to be return
     * @return the address of the desired child node in the node file.
     **/
    long getChildAddress(int childIndex) {
	return childAddresses[childIndex];
    }

    public void setChildAddress(int index, long childAddress) {
	childAddresses[index] = childAddress;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
	super.writeExternal(out);
	out.writeInt(childAddresses.length);
	for (int i = 0; i < childAddresses.length; i++) {
	    out.writeLong(childAddresses[i]);
	}
    }

    public void readExternal(ObjectInput in) throws IOException,
	    ClassNotFoundException {
	super.readExternal(in);
	childAddresses = new long[in.readInt()];
	for (int i = 0; i < childAddresses.length; i++) {
	    childAddresses[i] = in.readLong();
	}
    }
}
