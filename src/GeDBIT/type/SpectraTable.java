/**
 * GeDBIT.type.SpectraTable 2006.07.24 
 * 
 * Copyright Information:
 *  
 * Change Log: 
 * 2006.07.24: Added, by Willard
 */
package GeDBIT.type;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import GeDBIT.dist.Metric;

/**
 * @author Willard
 */
public class SpectraTable extends Table {
    /**
     * 
     */
    private static final long serialVersionUID = -6528032331962266589L;

    /**
     * @param fileName
     * @param maxSize
     * @param metric
     * @throws IOException
     */
    public SpectraTable(String fileName, String indexPrefix, int maxSize,
	    Metric metric) throws IOException {
	super(fileName, indexPrefix, maxSize, metric);

	BufferedReader reader = new java.io.BufferedReader(
		new java.io.FileReader(fileName));
	loadData(reader, maxSize);
    }

    /**
     * @param reader
     * @param maxSize
     */
    private void loadData(BufferedReader reader, int maxSize) {
	String line;
	ArrayList<Spectra> spectra = null;
	System.out.println("Loading... ");
	try {
	    // read sequences from file
	    line = reader.readLine(); // read the first line
	    if (line != null)
		line = line.trim();
	    // get total rows from first line and allocate the ArrayList
	    int numSpectra = java.lang.Integer.parseInt(line);
	    spectra = new ArrayList<Spectra>(numSpectra);
	    originalRowIDs = new int[numSpectra];

	    // read line byte line
	    line = reader.readLine();
	    if (line != null)
		line = line.trim();

	    int count = 0;
	    while (line != null && count < maxSize) {
		if (count % 1000 == 0) {
		    System.out.print(count + ".. ");
		}
		String[] row = line.split(" ", 2);
		originalRowIDs[count] = new Integer(row[0]).intValue();
		spectra.add(new Spectra(this, count, row[1]));

		line = reader.readLine();
		if (line != null)
		    line = line.trim();
		count++;
	    }
	} catch (java.io.IOException e) {
	    e.printStackTrace();
	    throw new java.lang.IllegalStateException(
		    "Error occured when reading ms file: " + reader
			    + " error message returned: " + e.getMessage());
	} catch (NumberFormatException e) {
	    e.printStackTrace();
	    /* Ignore strings with invalid characters. */
	}
	spectra.trimToSize();
	data = spectra;
    }

}
