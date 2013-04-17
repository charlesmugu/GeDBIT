/**
 * edu.utexas.GeDBIT.util.Pair 2006.06.16
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.06.16: Modified from jdb 1.0, by Rui Mao
 */
package GeDBIT.type;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Comparator;

/**
 * A simple wrapper to wrap two object together.
 * 
 * @author Rui Mao
 * @version 2003.07.27
 */
@SuppressWarnings("rawtypes")
public class Pair implements Serializable, Comparable {
    /**
     * 
     */
    private static final long serialVersionUID = 7226016227786370437L;

    private Object first;
    private Object second;

    /**
     * Constructor.
     */
    public Pair(Object first, Object second) {
	this.first = first;
	this.second = second;
    }

    /**
     * @return the first object wrapped
     */
    public Object first() {
	return first;
    }

    /**
     * @return the second object wrapped.
     */
    public Object second() {
	return second;
    }

    public String toString() {
	java.text.DecimalFormat format = new java.text.DecimalFormat("#.######");
	format.setMaximumFractionDigits(6);

	String firstString = (first instanceof Double) ? format
		.format(((Double) first).doubleValue()) : first.toString();
	String secondString = (second instanceof Double) ? format
		.format(((Double) second).doubleValue()) : second.toString();

	return "(" + firstString + ", " + secondString + ")";
    }

    /**
     * method for making the class {@link Serializable}. writes information
     * about the object to the {@link ObjectOutputStream} provided in the
     * parameter, so that it can be read from the file and reconstructed later.
     * 
     * @throws IOException
     *             if there is any error in writing
     **/
    private void writeObject(ObjectOutputStream objectStream)
	    throws IOException {
	objectStream.defaultWriteObject();
    }

    /**
     * method for making the class {@link Serializable}. reads information about
     * the object from the {@link ObjectInputStream}
     * 
     * @throws IOException
     *             if there is any error in reading
     */
    private void readObject(ObjectInputStream objectStream) throws IOException,
	    ClassNotFoundException {
	objectStream.defaultReadObject();
    }

    public static final Comparator<Object> FirstComparator = new Comparator<Object>() {
	@SuppressWarnings("unchecked")
	public int compare(Object first, Object second) {
	    return ((Comparable) (((Pair) first).first()))
		    .compareTo(((Pair) second).first());
	}
    };

    public static final Comparator SecondComparator = new Comparator() {
	@SuppressWarnings("unchecked")
	public int compare(Object first, Object second) {
	    return ((Comparable) ((Pair) first).second())
		    .compareTo(((Pair) second).second());
	}
    };

    public int compareTo(Object o) {
	return FirstComparator.compare(this, (Pair) o);
    }

    public static void main(String[] args) {
	int last = Integer.parseInt(args[0]);
	for (int i = last; i > 1; i--)
	    System.out.println("1/" + i + " : " + f(1 / (double) i));// + ",  "
								     // +
								     // f1(1/(double)i));

	for (int i = 1; i <= last; i++)
	    System.out.println(i + " : " + f2(i));// + ",   " + f1(m));

	/*
	 * for (int i=0; i<last; i++) { double m = 0.7392276 + i*0.0000001/last;
	 * System.out.println(m + " : " + f(m) + ",   " + f1(m)); }
	 */

    }

    static double f(double m) {
	return (m / 2 + 1) * Math.log(m + 2) + (m / 2 - 1) * Math.log(m);

    }

    static double f1(double m) {
	return Math.log((m + 2) * m) / 2 + 1 - 1 / m;
    }

    static double f2(double m) {
	return (m + 1.5) * Math.log(1 + 2 / m) - 1;
    }

}
