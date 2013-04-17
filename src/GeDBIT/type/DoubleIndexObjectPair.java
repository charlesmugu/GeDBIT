/**
 * edu.utexas.GeDBIT.util.DoubleObjectPair 2003.07.19
 *
 * Copyright Information:
 *
 * Change Log:
 * 2003.07.19: Modified from the original GeDBIT package, by Rui Mao
 * 2004.10.31: add toString(), by Rui Mao
 */
package GeDBIT.type;

import java.util.Comparator;

/**
 * Wraps a <code>double</code> and an {@link IndexObject}. It also contains two
 * {@link Comparator}s to compare the double or the {@link IndexObject}.
 * 
 * @author Rui Mao, Willard
 * @version 2004.10.31
 */
public class DoubleIndexObjectPair {
    private double _double;
    private IndexObject object;

    public DoubleIndexObjectPair(double dd, IndexObject o) {
	this._double = dd;
	this.object = o;
    }

    public DoubleIndexObjectPair() {
	this._double = 0;
	this.object = null;
    }

    public double getDouble() {
	return _double;
    }

    public IndexObject getObject() {
	return object;
    }

    public void setDouble(double d) {
	this._double = d;
    }

    public void setObject(IndexObject o) {
	this.object = o;
    }

    public String toString() {
	return "double =" + _double + ", object= " + object;
    }

    public static final Comparator<DoubleIndexObjectPair> DoubleComparator = new Comparator<DoubleIndexObjectPair>() {
	public int compare(DoubleIndexObjectPair first,
		DoubleIndexObjectPair second) {
	    final double firstDouble = first.getDouble();
	    final double secondDouble = second.getDouble();
	    return firstDouble < secondDouble ? -1
		    : firstDouble > secondDouble ? 1 : 0;
	}
    };

    public static final Comparator<DoubleIndexObjectPair> ObjectComparator = new Comparator<DoubleIndexObjectPair>() {
	public int compare(DoubleIndexObjectPair first,
		DoubleIndexObjectPair second) {
	    return first.getObject().compareTo(second.getObject());
	}
    };
}
