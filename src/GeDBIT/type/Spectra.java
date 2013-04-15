/**
 * edu.utexas.GeDBIT.type.MSData 2003.08.13 
 * 
 * Copyright Information: 
 * 
 * Change Log: 2003.08.13: Modified from the original GeDBIT package, by Rui Mao 
 * 2003.11.06: Modified toString method for performance, by Willard 
 * 2004.05.13: Modified for performance, Willard 
 * 2004.05.12: Modified to use arrays instead of ArrayList, Willard
 * 2004.08.15: Modified constructor to fix initialization bug with String input data, by Smriti Ramakrishnan 
 * 2004.09.01: Added sortAsc() method, Smriti Ramakrishnan 
 * 2004.09.11: Added call to sortAsc() method in all constructors - all objects are are sorted mandatorily, by Smriti Ramakrishnan
 */
package GeDBIT.type;

import java.util.Arrays;
import java.util.logging.Logger;

import GeDBIT.util.Debug;

/**
 * @author Smriti Ramakrishnan, Rui Mao, Willard
 *
 */
public class Spectra extends DoubleVector {
    private static final long serialVersionUID = -8300375927493085758L;

    /**
     * 
     */
    public Spectra() {
    }

    /**
     * @param table
     * @param rowID
     * @param spectra
     */
    public Spectra(Table table, int rowID, String spectra) {
        super(table, rowID, spectra);
        // make sure query is sorted
        sortAsc();
    }

    /**
     * @param table
     * @param rowID
     * @param spectra
     */
    public Spectra(Table table, int rowID, double[] spectra) {
        super(table, rowID, spectra);
        // make sure query is sorted
        sortAsc();
    }

    /**
     * 
     */
    public void sortAsc() {
        try {
            Arrays.sort(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Debug.debug) {
            Logger.getLogger("GeDBIT.index").finer("Sorted Spectra = " + toString());
        }
    }

    /**
     * @return
     */
    public double getMin() {
        return data[0];
    }

    /**
     * @return
     */
    public double getMax() {
        return data[data.length - 1];
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.type.DoubleVector#expand()
     */
    public IndexObject[] expand() {
        IndexObject[] dbO = new IndexObject[rowIDLength];
        for (int i = 0; i < rowIDLength; i++) {
            dbO[i] = new Spectra(table, rowIDStart + i, data);
        }
        return dbO;
    }
}
