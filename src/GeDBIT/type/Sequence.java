/**
 * GeDBIT.type.Sequence 2006.05.24 
 * 
 * Change Log: 
 * 2006.05.24: Modified from the original jdb package, by Willard
 */

package GeDBIT.type;

import java.io.Serializable;

/**
 * A compact representation of sequences of small alphabets whose indices fit
 * within a valid
 * <code>byte</byte> range. The general contract of {@link Sequence} implementations in this package is that all instances be immutable. This class has constructors that make
 * references to external objects in instances. <em>It is the responsibility 
 * of the user to ensure that the external objects are immutable!</em>
 * 
 * @author Jack, Neha, Rui Mao, Weijia Xu, Willard
 * @version 2003.06.05
 */
public abstract class Sequence implements Serializable {
    private static final long serialVersionUID = 6518919187067691379L;

    String sequenceID;
    protected byte[] data;

    /**
     * TODO
     * 
     * @param sequenceID
     * @param sequence
     */
    protected Sequence(String sequenceID, String sequence) {
	this.sequenceID = sequenceID;
	int stringLength = sequence.length();
	this.data = new byte[stringLength];
    }

    /**
     * @return the size of the {@link Sequence}.
     */
    public final int size() {
	return data.length;
    }

    /**
     * The number of fragments a given {@link Sequence} can be divided up into.
     * 
     * @param fragmentLength
     *            the length of the fragments to split the {@link Sequence} into
     * @return the number of fragments for the given {@link Sequence}
     */
    public int numFragments(int fragmentLength) {
	return data.length - fragmentLength;
    }

    /**
     * @param index
     *            an integer index value
     * @return the {@link Symbol} corresponding to that index.
     */
    public abstract Symbol get(int index);

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public abstract String toString();

    /**
     * @return
     */
    public abstract Alphabet getAlphabet();
}
