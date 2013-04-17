/**
 * GeDBIT.type.TandemSpectra 2003.08.13
 *
 * The GeDBIT Library
 * Copyright (c) 2006 The GeDBIT Group and The University of Texas at Austin.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * Change Log:
 * 2003.08.13: Modified from the original GeDBIT package, by Rui Mao
 * 2003.11.06: Modified toString method for performance, by Willard 
 * 2004.05.13: Modified for performance, Willard 
 * 2004.05.12: Modified to use arrays instead of ArrayList, Willard 
 * 2004.08.15: Modified constructor to fix initialization bug with String input data, by Smriti Ramakrishnan
 * 2004.09.01: Added sortAsc() method, Smriti Ramakrishnan
 * 2004.09.11: Added call to sortAsc() method in all constructors - all objects are are sorted mandatorily, by Smriti Ramakrishnan
 */

package GeDBIT.type;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

import GeDBIT.type.Table;

//import GeDBIT.db.TandemSpectraTable;

/**
 * TandemSpectra represents a given spectra and its attached precursor mass.
 * 
 * @author Smriti Ramakrishnan, Willard
 * @version 2004.11.29
 */
public class TandemSpectra extends Spectra {
    private static final long serialVersionUID = 1247643270939539788L;

    private double precursorMass;

    /**
     * Necessary for readExternal() and writeExternal().
     */
    public TandemSpectra() {
    }

/**
     * Constructs a TandemSpectra object from a {@link String representing
     * the tandem spectra. The only difference between a Spectra and a
     * TandemSpectra is that a TandemSpectra includes the precursor mass.
     * 
     * @param table
     *        the corresponding {@link TandemSpectraTable} for this object
     * @param rowID
     *        the rowID in the {@link TandemSpectraTable}.
     * @param precursorMass
     *        the precursor mass of the spectra itself.
     * @param spectra
     *        a space-seperated {@link String} representation of a single fragmentation spectra.
     */
    public TandemSpectra(Table table, int rowID, double precursorMass,
	    String spectra) {
	super(table, rowID, spectra);
	this.precursorMass = precursorMass;
    }

    /**
     * Main constructor using an array of doubles to define the Spectra.
     * 
     * @param table
     *            the corresponding {@link TandemSpectraTable} for this object
     * @param rowID
     *            the rowID in the {@link TandemSpectraTable}.
     * @param precursorMass
     *            the precursor mass of the spectra itself.
     * @param spectra
     */
    public TandemSpectra(Table table, int rowID, double precursorMass,
	    double[] spectra) {
	super(table, rowID, spectra);
	this.precursorMass = precursorMass;
    }

    /**
     * @return the precursor mass for this tandem spectra.
     */
    public double getPrecursorMass() {
	return precursorMass;
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.type.Spectra#expand()
     */
    public IndexObject[] expand() {
	IndexObject[] dbO = new IndexObject[rowIDLength];
	for (int i = 0; i < rowIDLength; i++) {
	    dbO[i] = new TandemSpectra(table, rowIDStart + i, precursorMass,
		    data);
	}
	return dbO;
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.type.DoubleVector#compareTo(GeDBIT.type.IndexObject)
     */
    public int compareTo(IndexObject oThat) {
	if (!(oThat instanceof TandemSpectra))
	    throw new Error("not compatible");
	TandemSpectra that = (TandemSpectra) oThat;
	if (this == that)
	    return 0;

	if (this.precursorMass < that.precursorMass)
	    return -1;
	if (this.precursorMass > that.precursorMass)
	    return 1;
	else {
	    if (this.size() < that.size())
		return -1;
	    else if (this.size() > that.size())
		return 1;
	    else {
		for (int i = 0; i < this.size(); i++) {
		    double double1 = data[i];
		    double double2 = that.data[i];
		    if (double1 < double2)
			return -1;
		    else if (double1 > double2)
			return 1;
		}
		return 0;
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.type.DoubleVector#equals(java.lang.Object)
     */
    public boolean equals(Object that) {
	if (!(that instanceof TandemSpectra))
	    return false;
	else {
	    TandemSpectra sWPMass = (TandemSpectra) that;
	    if (this.precursorMass != sWPMass.precursorMass)
		return false;
	    return Arrays.equals(this.data, ((TandemSpectra) that).data);
	}
    }

    // taken from Joshua Bloch's Effective Java
    public int hashCode() {
	int result = super.hashCode();
	long _long = Double.doubleToLongBits(precursorMass);
	return 37 * result + (int) (_long ^ (_long >>> 32));
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.type.DoubleVector#toString()
     */
    public String toString() {
	StringBuffer rowIDs = new StringBuffer("rowIDs: ");

	for (int i = 0; i < rowIDLength; i++) {
	    rowIDs.append(table.getOriginalRowID(rowIDStart + i));
	}
	final int dataSize = data.length;
	rowIDs.append("data(size=" + dataSize + ", pMass= " + precursorMass
		+ ") :[");
	for (int i = 0; i < dataSize; i++)
	    rowIDs.append(data[i]).append(", ");
	rowIDs.append("]\n");
	return rowIDs.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.type.DoubleVector#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws ClassNotFoundException,
	    IOException {
	super.readExternal(in);
	precursorMass = in.readDouble();
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.type.DoubleVector#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
	super.writeExternal(out);
	out.writeDouble(precursorMass);
    }
}
