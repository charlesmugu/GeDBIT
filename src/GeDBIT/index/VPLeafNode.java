/**
 * GeDBIT.index.VPLeafNode 2006.05.12
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
 * @author Rui Mao, Willard
 */
public class VPLeafNode extends LeafNode {
    private static final long serialVersionUID = -2816878994630817839L;

    private double[][] distance;
    private double[][] pathDistance;

    public VPLeafNode() {
	super();
    }

    /**
     * TODO javadoc
     * 
     * @param pivots
     * @param children
     * @param size
     * @param distance
     *            distances from each distinct data point (row) to each pivot
     * @param pathDistance
     *            distances from each distinct data point (row) to each
     *            pivot(column) on the path from the root to this node.
     */
    public VPLeafNode(IndexObject[] pivots, IndexObject[] children, int size,
	    double[][] distance, double[][] pathDistance) {
	super(pivots, children, size);

	if (distance == null)
	    throw new IllegalArgumentException("distance array cannot be null!");
	this.distance = distance;

	if (pathDistance == null)
	    throw new IllegalArgumentException(
		    "pathDistance array cannot be null!");

	this.pathDistance = pathDistance;
    }

    /**
     * @param childIndex
     * @return the distances from a child to all the pivots.
     */
    public double[] getDataPointPivotDistance(int childIndex) {
	return distance[childIndex];
    }

    /**
     * @param childIndex
     * @return the path distance list, consisting of the distances from a child
     *         to pivots on the path from the root to the node, can be of lenght
     *         0, but can not be null.
     */
    public double[] getDataPointPathDistance(int childIndex) {
	return pathDistance[childIndex];
    }

    public void writeExternal(ObjectOutput out) throws IOException {
	super.writeExternal(out);
	out.writeInt(distance.length);
	for (int i = 0; i < distance.length; i++) {
	    out.writeInt(distance[i].length);
	    for (int j = 0; j < distance[i].length; j++) {
		out.writeDouble(distance[i][j]);
	    }
	}
	out.writeInt(pathDistance.length);
	for (int i = 0; i < pathDistance.length; i++) {
	    out.writeInt(pathDistance[i].length);
	    for (int j = 0; j < pathDistance[i].length; j++) {
		out.writeDouble(pathDistance[i][j]);
	    }
	}
    }

    public void readExternal(ObjectInput in) throws IOException,
	    ClassNotFoundException {
	super.readExternal(in);
	distance = new double[in.readInt()][];
	for (int i = 0; i < distance.length; i++) {
	    distance[i] = new double[in.readInt()];
	    for (int j = 0; j < distance[i].length; j++) {
		distance[i][j] = in.readDouble();
	    }
	}
	pathDistance = new double[in.readInt()][];
	for (int i = 0; i < pathDistance.length; i++) {
	    pathDistance[i] = new double[in.readInt()];
	    for (int j = 0; j < pathDistance[i].length; j++) {
		pathDistance[i][j] = in.readDouble();
	    }
	}
    }
}
