/**
 * mobios.type.StringTable 2011.07.21 
 * 
 * Change Log: 
 * 2011.07.21: Created by Rui Mao
 */
package GeDBIT.type;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import GeDBIT.dist.Metric;

/**
 * @author Rui Mao
 */
public class StringTable extends Table {

    /**
     * 
     */
    private static final long serialVersionUID = 7630078213101669086L;

    /**
     * The first line of the file should have two integers, separated by white
     * space. The first is the dimension of the DoubleVector, the second is the
     * total number of data points. Each following line is a DoubleVector, with
     * each dimension separated by white space.
     * 
     * @param fileName
     *            the filename of the source file
     * @param size
     *            number of data points to read
     * @throws IOException
     */
    public StringTable(String fileName, String indexPrefix, int size,
	    Metric metric) throws IOException {
	super(fileName, indexPrefix, size, metric);
	BufferedReader reader = new BufferedReader(new FileReader(fileName));
	loadData(reader, size);
    }

    /**
     * @param reader
     * @param maxSize
     * @param dimNum
     * @throws IOException
     */
    void loadData(BufferedReader reader, int maxSize) throws IOException {

	String line;
	ArrayList<Integer> originalRowIDsArrayList = new ArrayList<Integer>();
	ArrayList<StringObject> strings = new ArrayList<StringObject>();
	int numData = 0;

	line = reader.readLine();
	if (line != null)
	    line = line.trim();

	while (line != null && numData < maxSize) {
	    originalRowIDsArrayList.add(numData, numData);
	    strings.add(new StringObject(this, numData, line));

	    line = reader.readLine();
	    if (line != null)
		line = line.trim();

	    numData++;
	}
	strings.trimToSize();
	this.data = strings;

	originalRowIDs = new int[originalRowIDsArrayList.size()];
	for (int i = 0, e = originalRowIDsArrayList.size(); i < e; i++) {
	    originalRowIDs[i] = originalRowIDsArrayList.get(i);
	}
    }
}
