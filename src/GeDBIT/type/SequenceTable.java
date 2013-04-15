/**
 * GeDBIT.type.SequenceTable 2006.07.24 
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
import java.util.Collections;

import GeDBIT.dist.SequenceFragmentMetric;
import GeDBIT.index.algorithms.PartitionMethod;
import GeDBIT.index.algorithms.PivotSelectionMethod;

/**
 * 
 * @author Willard
 * 
 */
public abstract class SequenceTable extends Table
{
    private static final long serialVersionUID = -1519446228237674948L;

    protected Alphabet        alphabet;

    protected int             fragmentLength;

    protected Sequence[]      sequences;

    protected int[]           fragmentOffsets;

    /**
     * @param fileName
     * @param maxSize
     * @param metric
     * @param fragmentLength
     * @throws IOException
     */
    protected SequenceTable(String fileName, String indexPrefix, int maxSize, SequenceFragmentMetric metric, int fragmentLength) throws IOException
    {
        super(fileName, indexPrefix, maxSize, metric);
        if (fragmentLength <= 0)
            throw new IllegalArgumentException("fragment length must be greater than zero!");

        this.alphabet = metric.getWeightMatrix().getAlphabet();
        this.fragmentLength = fragmentLength;

        BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(fileName));
        loadData(reader, maxSize);
        initFragmentList(maxSize);
    }

    /**
     * @param reader
     * @param maxSize
     */
    protected abstract void loadData(BufferedReader reader, int maxSize);

    /**
     * @param size
     */
    private void initFragmentList(int size)
    {
        int count = 0;
        // first figure out how long arrays are going to be;
        for (int i = 0; i < sequences.length; i++)
        {
            int numFragments = sequences[i].numFragments(fragmentLength);
            for (int j = 0; j < numFragments; j++)
            {
                if (count < size)
                {
                    count++;
                }
            }
        }
        // init rowIDs list;
        ArrayList<Fragment> fragmentList = new ArrayList<Fragment>(count);
        originalRowIDs = new int[count];
        fragmentOffsets = new int[count];

        // reset count;
        count = 0;
        for (int i = 0; i < sequences.length; i++)
        {
            int numFragments = sequences[i].numFragments(fragmentLength);
            for (int j = 0; j < numFragments; j++)
            {
                if (count < size)
                {
                    this.originalRowIDs[count] = i;
                    this.fragmentOffsets[count] = j;
                    Fragment frag = new Fragment(this, count);
                    fragmentList.add(frag);
                    count++;
                }
            }
        }

        fragmentList.trimToSize();
        data = fragmentList;
    }

    // TODO make better
    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.type.Table#compressData()
     */
    public void compressData()
    {
        // first sort the list according to the data points.
        Collections.sort(data);

        // then, make a list of the unique dataPoints.
        final int dataSize = data.size();
        ArrayList<IndexObject> compressedData = new ArrayList<IndexObject>(dataSize);
        int[] rowIDs2 = new int[dataSize];
        int[] dataOffset2 = new int[dataSize];

        IndexObject dataPoint1 = data.get(0);
        int tempSize = 1;

        IndexObject dataPoint2;
        for (int i = 1; i < dataSize; i++)
        {
            dataPoint2 = (IndexObject) data.get(i);
            if (dataPoint1.equals(dataPoint2))
            {
                tempSize++;
            }
            else
            {
                if (tempSize > 1)
                {
                    for (int j = i - tempSize; j < i; j++)
                    {
                        int rowID = data.get(j).getRowID();
                        rowIDs2[j] = originalRowIDs[rowID];
                        dataOffset2[j] = fragmentOffsets[rowID];
                    }
                    dataPoint1.setRowID(i - tempSize);
                    dataPoint1.setRowIDLength(tempSize);
                }
                else
                {
                    int rowID = data.get(i - 1).getRowID();
                    rowIDs2[i - 1] = originalRowIDs[rowID];
                    dataOffset2[i - 1] = fragmentOffsets[rowID];
                    dataPoint1.setRowID(i - 1);
                }
                compressedData.add(dataPoint1);
                dataPoint1 = dataPoint2;
                tempSize = 1;
            }
        }

        if (tempSize > 1)
        {
            for (int i = dataSize - tempSize; i < dataSize; i++)
            {
                int rowID = data.get(i).getRowID();
                rowIDs2[i] = originalRowIDs[rowID];
                dataOffset2[i] = fragmentOffsets[rowID];
            }
            dataPoint1.setRowID(dataSize - tempSize);
            dataPoint1.setRowIDLength(tempSize);
        }
        else
        {
            int rowID = data.get(dataSize - 1).getRowID();
            rowIDs2[dataSize - 1] = originalRowIDs[rowID];
            dataOffset2[dataSize - 1] = fragmentOffsets[rowID];
            dataPoint1.setRowID(dataSize - 1);
        }
        compressedData.add(dataPoint1);

        compressedData.trimToSize();
        //System.out.println("original size: " + dataSize + " compressed data size: " + compressedData.size());
        data = compressedData;
        originalRowIDs = rowIDs2;
        fragmentOffsets = dataOffset2;
    }

    /**
     * @return
     */
    public int getFragmentLength()
    {
        return fragmentLength;
    }

    /**
     * @param rowID
     * @return
     */
    public int getFragmentOffset(int rowID)
    {
        return fragmentOffsets[rowID];
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.type.Table#createIndexFileName(GeDBIT.index.algorithms.PivotSelectionMethod,
     *      int, GeDBIT.index.algorithms.PartitionMethod, int, int, int,
     *      boolean)
     */
    protected String createIndexFileName(PivotSelectionMethod psm, int numPivots, PartitionMethod pm, int singlePivotFanout, int maxLeafSize,
            int maxPathLength, boolean bucket)
    {
        String psmName;
        if (psm instanceof GeDBIT.index.algorithms.IncrementalSelection)
            psmName = "incremental";
        else
            psmName = psm.toString();

        StringBuffer myFileName = new StringBuffer(sourceFileName + "-" + psmName + "-" + numPivots + "-" + pm + "-" + singlePivotFanout + "-MLS-"
                + maxLeafSize + "-MPL-" + maxPathLength + "-FL-" + fragmentLength);

        if (maxSize > 0)
        {
            myFileName.append("-S-" + maxSize);
        }
        if (bucket == true)
        {
            myFileName.append("-b-");
        }
        return myFileName.toString();
    }
}
