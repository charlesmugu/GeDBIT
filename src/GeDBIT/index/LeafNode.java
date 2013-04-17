/**
 * GeDBIT.index.LeafNode 2006.05.12
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
 * The base class for all leaf nodes.
 * 
 * @author Rui Mao, Willard
 * @version 2006.05.12
 */

abstract class LeafNode extends IndexNode {
    protected IndexObject[] children;

    public LeafNode() {
	super();
    }

    /**
     * @param pivots
     * @param pivotRowIDs
     * @param children
     * @param childRowIDs
     * @param size
     */
    protected LeafNode(IndexObject[] pivots, IndexObject[] children, int size) {
	super(pivots, size);
	if (children == null)
	    throw new IllegalArgumentException(
		    "LeafNode children cannot be null");
	this.children = children;
    }

    /**
     * @return the number of children.
     */
    int numChildren() {
	return children.length;
    }

    /**
     * Return a distinct key value of children.
     * 
     * @param dataIndex
     *            index of the distinct key value to be return
     * @return the key value
     */
    IndexObject getChild(int dataIndex) {
	return children[dataIndex];
    }

    public void writeExternal(ObjectOutput out) throws IOException {
	super.writeExternal(out);
	out.writeInt(children.length);
	for (int i = 0; i < children.length; i++) {
	    out.writeObject(children[i]);
	}
    }

    public void readExternal(ObjectInput in) throws IOException,
	    ClassNotFoundException {
	super.readExternal(in);
	children = new IndexObject[in.readInt()];
	for (int i = 0; i < children.length; i++) {
	    children[i] = (IndexObject) in.readObject();
	}
    }
}