/**
 * GeDBIT.index.VPInternalNode 2006.05.12
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
public class VPInternalNode extends InternalNode {
    private static final long serialVersionUID = 2951629361525110954L;

    private double[][] lowerRange;
    private double[][] upperRange;

    public int GHTDegree = 0;

    public VPInternalNode() {
	super();
    }

    /**
     * @param pivots
     * @param lowerRange
     *            lower range from each child (row) to each pivot (column)
     * @param upperRange
     *            upper range from each child (row) to each pivot (column)
     * @param size
     * @param childAddress
     */
    public VPInternalNode(IndexObject[] pivots, double[][] lowerRange,
	    double[][] upperRange, int size, long[] childAddress) {
	this(pivots, lowerRange, upperRange, size, childAddress, 0);
    }

    public VPInternalNode(IndexObject[] pivots, double[][] lowerRange,
	    double[][] upperRange, int size, long[] childAddress, int degree) {
	super(pivots, size, childAddress);

	if (lowerRange == null || upperRange == null)
	    throw new IllegalArgumentException(
		    "lowerRange and upperRange distance arrays cannot be null");

	this.lowerRange = lowerRange;
	this.upperRange = upperRange;
	this.GHTDegree = degree;
    }

    /**
     * Returns the predicate, the ranges from the child to each piovt, of a
     * child node.
     * 
     * @param childIndex
     * @return a 2-d array of the lower ranges (first row) and the upper ranges
     *         (second row) of the child to each pivot.
     */
    public double[][] getChildPredicate(int childIndex) {
	double[][] result = new double[2][];
	result[0] = lowerRange[childIndex];
	result[1] = upperRange[childIndex];

	return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
	super.writeExternal(out);
	out.writeInt(lowerRange.length);
	for (int i = 0; i < lowerRange.length; i++) {
	    out.writeInt(lowerRange[i].length);
	    for (int j = 0; j < lowerRange[i].length; j++) {
		out.writeDouble(lowerRange[i][j]);
	    }
	}
	out.writeInt(upperRange.length);
	for (int i = 0; i < upperRange.length; i++) {
	    out.writeInt(upperRange[i].length);
	    for (int j = 0; j < upperRange[i].length; j++) {
		out.writeDouble(upperRange[i][j]);
	    }
	}
	out.writeInt(GHTDegree);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException,
	    ClassNotFoundException {
	super.readExternal(in);
	lowerRange = new double[in.readInt()][];
	for (int i = 0; i < lowerRange.length; i++) {
	    lowerRange[i] = new double[in.readInt()];
	    for (int j = 0; j < lowerRange[i].length; j++) {
		lowerRange[i][j] = in.readDouble();
	    }
	}
	upperRange = new double[in.readInt()][];
	for (int i = 0; i < upperRange.length; i++) {
	    upperRange[i] = new double[in.readInt()];
	    for (int j = 0; j < upperRange[i].length; j++) {
		upperRange[i][j] = in.readDouble();
	    }
	}
	this.GHTDegree = in.readInt();
    }

    public double[][] getLowerRange() {
	return lowerRange;
    }

    public void setLowerRange(double[][] lowerRange) {
	this.lowerRange = lowerRange;
    }

    public double[][] getUpperRange() {
	return upperRange;
    }

    public void setUpperRange(double[][] upperRange) {
	this.upperRange = upperRange;
    }

}
