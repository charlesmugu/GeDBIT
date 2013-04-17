/**
 * GeDBIT.type.ImageTable 2006.07.24 
 * 
 * Change Log: 
 * 2006.07.24: Added, by Willard
 */
package GeDBIT.type;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import GeDBIT.dist.ImageMetric;
import GeDBIT.dist.Metric;

import GeDBIT.type.Table;

/**
 * @author Willard
 */
public class ImageTable extends Table {
    private static final long serialVersionUID = 1574357904350312666L;

    private static final Metric DEFAULT_METRIC = new GeDBIT.dist.ImageMetric();

    public ImageTable(String dirName, String indexPrefix, int maxSize)
	    throws FileNotFoundException {
	this(dirName, indexPrefix, maxSize, DEFAULT_METRIC);
    }

    /**
     * Load image data from file with max distance information. This data file
     * contains floating numbers for features of the images. Each line contains:
     * Image file name, Feature value 1, Feature value 2, ..., Feature value m.
     * 
     * @param reader
     *            the {@link BufferedReader} for the image data file.
     * @param readerMaxInfo
     *            the {@link BufferedReader} for the image max distance
     *            information.
     * @param size
     *            represents how many rows of data to be loaded.
     * @throws FileNotFoundException
     */
    private ImageTable(String dirName, String indexPrefix, int size,
	    Metric metric) throws FileNotFoundException {
	super(dirName, indexPrefix, size, metric);

	BufferedReader reader, readerMaxInfo = null;
	reader = new BufferedReader(new FileReader(dirName + "/allfeas.dat"));
	try {
	    readerMaxInfo = new BufferedReader(new FileReader(dirName
		    + "/maxinfo.dat"));
	} catch (FileNotFoundException e1) {
	    System.err
		    .println("maxinfo.dat not found. loading data without Max information");
	    loadData(reader, size);
	}
	loadData(reader, readerMaxInfo, size);
    }

    private void loadData(BufferedReader reader, int size) {
	String line;
	int count = 0;
	ArrayList<Image> images = new ArrayList<Image>(imageNum);
	try {
	    // read values from the text file.
	    line = reader.readLine();
	    if (line != null)
		line = line.trim();
	    while (line != null && count <= size) {
		float[] aList = new float[totalFeaLen];
		String[] lineSegments = line.split(" ");
		for (int i = 1; i <= totalFeaLen; i++) {
		    aList[i - 1] = Float.parseFloat(lineSegments[i]);
		}
		images.add(new Image(this, count, aList));
		originalRowIDs[count] = count;
		count++;
		line = reader.readLine();
		if (line != null)
		    line = line.trim();
	    }
	} catch (java.io.IOException e) {
	    e.printStackTrace();
	    throw new java.lang.IllegalStateException(
		    "Error occured when reading Image features file: " + reader);
	}
	images.trimToSize();
	data = images;
    }

    private void loadData(BufferedReader reader, BufferedReader readerMaxInfo,
	    int size) {
	String line;
	String lineMaxInfo;
	int count = 0;
	ArrayList<Image> images = new ArrayList<Image>(imageNum);
	ArrayList<Integer> originalRowIDsArrayList = new ArrayList<Integer>();

	try {
	    // read values from the text file.
	    line = reader.readLine();
	    if (line != null)
		line = line.trim();
	    lineMaxInfo = readerMaxInfo.readLine();
	    if (lineMaxInfo != null)
		lineMaxInfo = lineMaxInfo.trim();
	    while (line != null && lineMaxInfo != null && count <= size) {
		float[] aList = new float[totalFeaLen];
		double[] maxDist = new double[feaLength.length];
		String[] lineSegments = line.split(" ");
		String[] lineMaxInfoSegments = lineMaxInfo.split(" ");
		for (int i = 1; i <= totalFeaLen; i++) {
		    aList[i - 1] = Float.parseFloat(lineSegments[i]);
		}
		for (int i = 0; i < feaLength.length; i++) {
		    maxDist[i] = java.lang.Double
			    .parseDouble(lineMaxInfoSegments[i]);
		}
		images.add(new Image(this, count, aList, maxDist));
		originalRowIDsArrayList.add(count);
		count++;
		line = reader.readLine();
		if (line != null)
		    line = line.trim();
		lineMaxInfo = readerMaxInfo.readLine();
		if (lineMaxInfo != null)
		    lineMaxInfo = lineMaxInfo.trim();
	    }
	} catch (java.io.IOException e) {
	    e.printStackTrace();
	    throw new java.lang.IllegalStateException(
		    "Error occured when reading Image features file: " + reader);
	}
	images.trimToSize();
	data = images;

	originalRowIDs = new int[originalRowIDsArrayList.size()];
	for (int i = 0, e = originalRowIDsArrayList.size(); i < e; i++) {
	    originalRowIDs[i] = originalRowIDsArrayList.get(i);
	}

    }

    /**
     * A utility method to create max distance information for each image.
     * Output has a row for each image, which corresponds to the maximum
     * distance of this image to the other images.
     */
    @SuppressWarnings("rawtypes")
    public void createMaxInfo(List r) {
	ImageMetric metric = new ImageMetric();
	int sz = r.size();
	double maxDist;
	double tempVal;
	for (int k1 = 0; k1 < sz; k1++) {
	    Image ob1 = (Image) r.get(k1);
	    for (int FeaIndex = 0; FeaIndex < feaLength.length; FeaIndex++) {
		maxDist = 0.0;
		for (int k2 = 0; k2 < sz; k2++) {
		    Image ob2 = (Image) r.get(k2);
		    tempVal = metric.getDistance_Fea(ob1, ob2, FeaIndex);
		    if (tempVal > maxDist) {
			maxDist = tempVal;
		    }
		}
		System.out.print(maxDist);
		System.out.print(" ");
	    }
	    System.out.println();
	}
    }

    final public int totalFeaLen = 66;

    final public int feaLength[] = { 3, 48, 15 };

    final public int imageNum = 10221;
}
