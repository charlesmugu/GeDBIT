/**
 * GeDBIT.type.Table 2006.07.24 
 * 
 * Copyright Information:
 *  
 * Change Log: 
 * 2006.07.24: Added, by Willard
 */
package GeDBIT.type;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import GeDBIT.dist.Metric;
import GeDBIT.index.Index;
import GeDBIT.index.VPIndex;
import GeDBIT.index.algorithms.PartitionMethod;
import GeDBIT.index.algorithms.PivotSelectionMethod;

/**
 * Represents a set of data points in a database, including it's corresponding
 * metric and any indexes that have been built over it.
 * 
 * @author Willard, Rui Mao
 */
public abstract class TableMR implements Serializable {
    private static final long serialVersionUID = 8791814013616626066L;

    protected String sourceFileName;

    protected int maxSize;

    protected String indexPrefix;

    protected Metric metric;

    protected Index index;

    int[] originalRowIDs;

    transient List<? extends IndexObject> data;

    private int tableLocation = -1;

    /**
     * Constructor
     * 
     * @param fileName
     *            the name of the source data file.
     * @param maxDataSize
     *            the maximum number of data points to read from the file.
     * @param metric
     *            the metric to use when comparing two datapoints or building an
     *            index.
     */
    TableMR(String fileName, String indexPrefix, int maxSize, Metric metric) {
	if (fileName == null)
	    throw new IllegalArgumentException("fileName cannot be null!");
	if (maxSize <= 0)
	    throw new IllegalArgumentException(
		    "maxDataSize must be greater than zero!");
	if (metric == null)
	    throw new IllegalArgumentException("Metric cannot be null!");
	this.sourceFileName = fileName;
	this.indexPrefix = indexPrefix;
	this.maxSize = maxSize;
	this.metric = metric;
    }

    /**
     * @return the number of datapoints in the table.
     */
    public int size() {
	if (index == null)
	    return data.size();
	else
	    return index.size();
    }

    /**
     * @return the name of the file used to construct this table.
     */
    public String getSourceFileName() {
	return sourceFileName;
    }

    /**
     * @return the list of datapoints in this table.
     */
    public List<? extends IndexObject> getData() {
	if (index == null)
	    return data;
	else
	    return index.getAllPoints();
    }

    /**
     * @param rowID
     * @return
     */
    public int getOriginalRowID(int rowID) {
	return originalRowIDs[rowID];
    }

    /**
     * @return the index built over this table.
     */
    public Index getIndex() {
	return index;
    }

    /**
     * @return the metric used when comparing two objects in this table.
     */
    public Metric getMetric() {
	return metric;
    }

    /**
     * @param metric
     */
    public void setMetric(Metric metric) {
	this.metric = metric;
    }

    // TODO
    /**
     * 
     */
    public void compressData() {
	// first sort the list according to the data points.
	Collections.sort(data);

	// then, make a list of the unique dataPoints.
	final int dataSize = data.size();
	ArrayList<IndexObject> compressedData = new ArrayList<IndexObject>(
		dataSize);
	int[] rowIDs2 = new int[dataSize];

	IndexObject dataPoint1 = data.get(0);
	int tempSize = 1;

	IndexObject dataPoint2;
	for (int i = 1; i < dataSize; i++) {
	    dataPoint2 = (IndexObject) data.get(i);
	    if (dataPoint1.equals(dataPoint2)) {
		tempSize++;
	    } else {
		if (tempSize > 1) {
		    for (int j = i - tempSize; j < i; j++) {
			int rowID = data.get(j).getRowID();
			rowIDs2[j] = originalRowIDs[rowID];
		    }
		    dataPoint1.setRowID(i - tempSize);
		    dataPoint1.setRowIDLength(tempSize);
		} else {
		    int rowID = data.get(i - 1).getRowID();
		    rowIDs2[i - 1] = originalRowIDs[rowID];
		    dataPoint1.setRowID(i - 1);
		}
		compressedData.add(dataPoint1);
		dataPoint1 = dataPoint2;
		tempSize = 1;
	    }
	}

	if (tempSize > 1) {
	    for (int i = dataSize - tempSize; i < dataSize; i++) {
		int rowID = data.get(i).getRowID();
		rowIDs2[i] = originalRowIDs[rowID];
	    }
	    dataPoint1.setRowID(dataSize - tempSize);
	    dataPoint1.setRowIDLength(tempSize);
	} else {
	    int rowID = data.get(dataSize - 1).getRowID();
	    rowIDs2[dataSize - 1] = originalRowIDs[rowID];
	    dataPoint1.setRowID(dataSize - 1);
	}
	compressedData.add(dataPoint1);

	compressedData.trimToSize();
	// System.out.println("original size: " + dataSize +
	// " compressed data size: " + compressedData.size());
	data = compressedData;
	originalRowIDs = rowIDs2;
    }

    /**
     * @param psm
     * @param numPivots
     * @param pm
     * @param singlePivotFanout
     * @param maxLeafSize
     * @param maxPathLength
     * @param bucket
     * @param debugLevel
     */
    public void buildVPIndex(PivotSelectionMethod psm, int numPivots,
	    PartitionMethod pm, int singlePivotFanout, int maxLeafSize,
	    int maxPathLength, boolean bucket, Level debugLevel, String forPrint) {

	/*
	 * TableManager tableManager =
	 * TableManager.getTableManager(indexPrefix); tableLocation =
	 * tableManager.getLocation(); tableManager.close();
	 */

	double startTime, endTime, buildTime;

	startTime = System.currentTimeMillis();
	// indexFileName = createIndexFileName(psm, numPivots, pm,
	// singlePivotFanout, maxLeafSize, maxPathLength, bucket);

	// step 1: compress the data list
	if (bucket == true) {
	    compressData();
	    if (metric instanceof GeDBIT.dist.CountedMetric)
		((GeDBIT.dist.CountedMetric) metric).clear();

	    /*
	     * temporary use to output distinct points for (int i=0;
	     * i<data.size(); i++) { GeDBIT.type.DoubleVector v =
	     * (GeDBIT.type.DoubleVector)data.get(i); for (int j=0; j<v.size();
	     * j++) System.out.print(v.getData()[j] + "   ");
	     * System.out.println(); } System.exit(1);
	     */
	}
	index = new VPIndex(indexPrefix, data, metric, psm, numPivots, pm,
		singlePivotFanout, maxLeafSize, maxPathLength, debugLevel);
	// System.out.println("Created index...\n");

	endTime = System.currentTimeMillis();
	buildTime = (endTime - startTime) / 1000.00;
	// System.out.println("Db size, building time(seconds), #distance calculation, #leaf, #internal, #node");

	// System.out.println("Time to create index = " + buildTime + "...\n");

	/*
	 * tableManager = TableManager.getTableManager(indexPrefix);
	 * tableManager.putTable(this, tableLocation); tableManager.close();
	 */
	System.out.println(forPrint
		+ data.size()
		+ ", "
		+ buildTime
		+ ", "
		+ ((GeDBIT.dist.CountedMetric) metric).getCounter()
		+ ", "
		+ ((VPIndex) index).numLeaf
		+ ", "
		+ ((VPIndex) index).numInternal
		+ ", "
		+ (((VPIndex) index).numLeaf + ((VPIndex) index).numInternal)
		+ ", "
		+ ((GeDBIT.index.AbstractIndex) index)
			.getStoredDistanceNumber());
    }

    /**
     * @param psm
     * @param numPivots
     * @param pm
     * @param singlePivotFanout
     * @param maxLeafSize
     * @param maxPathLength
     * @param bucket
     * @return
     */
    protected String createIndexFileName(PivotSelectionMethod psm,
	    int numPivots, PartitionMethod pm, int singlePivotFanout,
	    int maxLeafSize, int maxPathLength, boolean bucket) {
	String psmName;
	if (psm instanceof GeDBIT.index.algorithms.IncrementalSelection)
	    psmName = "incremental";
	else
	    psmName = psm.toString();

	StringBuffer myFileName = new StringBuffer(sourceFileName + "-"
		+ psmName + "-" + numPivots + "-" + pm + "-"
		+ singlePivotFanout + "-MLS-" + maxLeafSize + "-MPL-"
		+ maxPathLength);

	if (maxSize > 0) {
	    myFileName.append("-S-" + maxSize);
	}
	if (bucket == true) {
	    myFileName.append("-b-");
	}
	return myFileName.toString();
    }

    /**
     * @return
     */
    public int getTableLocation() {
	return tableLocation;
    }

    /**
     * @return
     */
    public int getMaxSize() {
	return maxSize;
    }

    /**
     * @return
     */
    public String getIndexPrefix() {
	return indexPrefix;
    }
}
