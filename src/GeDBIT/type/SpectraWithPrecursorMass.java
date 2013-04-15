/*$Id: SpectraWithPrecursorMass.java,v 1.1 2013/01/24 02:01:03 hpc Exp $*/
/**
 * edu.utexas.GeDBIT.type.MSData 2003.08.13
 *
 * Copyright Information:
 *
 * Change Log:
 * 2003.08.13: Modified from the original GeDBIT package, by Rui Mao
 * 2003.11.06: Modified toString method for performance, by Willard Briggs
 * 2004.05.13: Modified for performance, Willard Briggs
 * 2004.05.12: Modified to use arrays instead of ArrayList, Willard Briggs
 * 2004.08.15: Modified constructor to fix initialization bug with String input data, by Smriti Ramakrishnan
 * 2004.09.01: Added sortAsc() method, Smriti Ramakrishnan
 * 2004.09.11: Added call to sortAsc() method in all constructors - all objects are are sorted mandatorily, by Smriti Ramakrishnan
 */

/**
 * This is a copy of MSDataKeyObject, created for MSMS purposes. One extra added field is precurorMass. 
 * modified by Smriti Ramakrishnan, 2004.11.18
 * MSDataKeyObject_MSMS contains only an array (TBD) of 'mapped' m/z values
 * The mapping from m/z to internal representation is done in MSDataLoader.
 * This class only storesthe internal represenatation
 **/
package GeDBIT.type;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

/**
 * SpectraWithPrecursorMass represents a given spectra and its attached precursor mass.
 * 
 * @author Smriti Ramakrishnan, Willard
 * @version 2004.11.29
 */
public class SpectraWithPrecursorMass extends Spectra {
    private static final long serialVersionUID = 1247643270939539788L;

    private double            precursorMass;

    /**
     * 
     */
    public SpectraWithPrecursorMass() {
    }

    /**
     * Constructs a SpectraWithPrecursorMass object. The only difference between a Spectra and a
     * SpectraWithPrecursorMass is that a SpectraWithPrecursorMass includes the PrecursorMass.
     * 
     * @param table
     *        the corresponding {@link SpectraWithPrecursorMassTable} for this object
     * @param rowID
     *        the rowID in the {@link SpectraWithPrecursorMassTable}.
     * @param precursorMass
     *        the precursor mass of the spectra itself.
     * @param spectra
     *        a space-seperated {@link String} representation of a single fragmentation spectra.
     */
    public SpectraWithPrecursorMass(Table table, int rowID, double precursorMass, String spectra) {
        super(table, rowID, spectra);
        this.precursorMass = precursorMass;
    }

    /**
     * @param table
     * @param rowID
     * @param precursorMass
     * @param spectra
     */
    public SpectraWithPrecursorMass(Table table, int rowID, double precursorMass, double[] spectra) {
        super(table, rowID, spectra);
        this.precursorMass = precursorMass;
    }

    /**
     * @return
     */
    public double getPrecursorMass() {
        return precursorMass;
    }

    /* (non-Javadoc)
     * @see GeDBIT.type.Spectra#expand()
     */
    public IndexObject[] expand() {
        IndexObject[] dbO = new IndexObject[rowIDLength];
        for (int i = 0; i < rowIDLength; i++) {
            dbO[i] = new SpectraWithPrecursorMass(table, rowIDStart + i, precursorMass, data);
        }
        return dbO;
    }

    /* (non-Javadoc)
     * @see GeDBIT.type.DoubleVector#compareTo(GeDBIT.type.IndexObject)
     */
    public int compareTo(IndexObject oThat) {
        if (!(oThat instanceof SpectraWithPrecursorMass))
            throw new Error("not compatible");
        SpectraWithPrecursorMass that = (SpectraWithPrecursorMass) oThat;
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

    /* (non-Javadoc)
     * @see GeDBIT.type.DoubleVector#equals(java.lang.Object)
     */
    public boolean equals(Object that) {
        if (!(that instanceof SpectraWithPrecursorMass))
            return false;
        else {
            SpectraWithPrecursorMass sWPMass = (SpectraWithPrecursorMass) that;
            if (this.precursorMass != sWPMass.precursorMass)
                return false;
            return Arrays.equals(this.data, ((SpectraWithPrecursorMass) that).data);
        }
    }

    // taken from Joshua Bloch's Effective Java
    public int hashCode() {
        int result = super.hashCode();
        long _long = Double.doubleToLongBits(precursorMass);
        return 37 * result + (int) (_long ^ (_long >>> 32));
    }

    /* (non-Javadoc)
     * @see GeDBIT.type.DoubleVector#toString()
     */
    public String toString() {
        StringBuffer rowIDs = new StringBuffer("rowIDs: ");

        for (int i = 0; i < rowIDLength; i++) {
            rowIDs.append(table.getOriginalRowID(rowIDStart + i));
        }
        final int dataSize = data.length;
        rowIDs.append("data(size=" + dataSize + ", pMass= " + precursorMass + ") :[");
        for (int i = 0; i < dataSize; i++)
            rowIDs.append(data[i]).append(", ");
        rowIDs.append("]\n");
        return rowIDs.toString();
    }

    /* (non-Javadoc)
     * @see GeDBIT.type.DoubleVector#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws ClassNotFoundException, IOException {
        super.readExternal(in);
        precursorMass = in.readDouble();
    }

    /* (non-Javadoc)
     * @see GeDBIT.type.DoubleVector#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeDouble(precursorMass);
    }
}
