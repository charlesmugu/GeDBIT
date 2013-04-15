/**
 * GeDBIT.type.DoubleVectorTable 2006.07.24 
 * 
 * Change Log: 
 * 2006.07.24: Added, by Willard
 */
package GeDBIT.type;

import java.io.IOException;
import java.util.ArrayList;

import GeDBIT.dist.LMetric;
import GeDBIT.dist.Metric;

/**
 * @author willard
 */
public class DoubleVectorTableMR extends TableMR
{

    /**
     * 
     */
    private static final long   serialVersionUID = 7630078213101669086L;

    /**
     * 
     */
    private static final Metric DEFAULT_METRIC   = LMetric.EuclideanDistanceMetric;

    /**
     * @param fileName
     *            the filename of the source file
     * @param size
     *            number of data points to read
     * @param dimNum
     *            number of dimensions to read from data.
     * @throws IOException
     */
    public DoubleVectorTableMR(String fileName, String indexPrefix, int size, int dimNum, double[][] data) throws IOException
    {
        this(fileName, indexPrefix, size, dimNum, DEFAULT_METRIC, data);
    }

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
     * @param dimNum
     *            number of dimensions to read from data.
     * @throws IOException
     */
    public DoubleVectorTableMR(String fileName, String indexPrefix, int size, int dimNum, Metric metric, double[][] data) throws IOException
    {
        super(fileName, indexPrefix, size, metric);
        //BufferedReader reader = new BufferedReader(new FileReader(fileName));
        loadData(/*reader, */size, dimNum, data);
    }

    /**
     * @param reader
     * @param maxSize
     * @param dimNum
     * @throws IOException
     */
    void loadData(/*BufferedReader reader, */int maxSize, int dimNum, double[][] data) throws IOException
    {

        //String line;
        ArrayList<DoubleVectorMR> doubleVectors = new ArrayList<DoubleVectorMR>();
        ArrayList<Integer> originalRowIDsArrayList = new ArrayList<Integer>();
        // read vector values from file
        //line = reader.readLine(); // read the first line
        /*if (line != null)
            line = line.trim();

        String[] metaData = line.split("[ \t]+");

        if (metaData.length != 2)
        {
            System.out.println("Error: Cannot parse the data file.");
            System.exit(-1);
        }

        final int dim = java.lang.Integer.parseInt(metaData[0]); // dimension
        if (dim < dimNum)
            dimNum = dim;
        // size = java.lang.Integer.parseInt(metaData[1]); // total number of
        // data

        if (Debug.debug)
        {
            Logger.getLogger("GeDBIT.index").info("dim: " + dim);
            Logger.getLogger("GeDBIT.index").info("size: " + maxSize);
        }

        int numData = 0;*/
        double[] lineData = new double[dimNum];

        /*line = reader.readLine();
        if (line != null)
            line = line.trim();

        while (line != null && numData < maxSize)
        {
            String[] row = line.split("[ \t]+");

            if (Debug.debug)
            {
                for (int i = 0; i < row.length; i++)
                    Logger.getLogger("GeDBIT.index").finer("row[" + i + "]: " + row[i]);
            }

            for (int i = 0; i < dimNum; i++)
                data[i] = java.lang.Double.parseDouble(row[i]);

            // System.out.println(new DoubleVector(new Integer(numData), data));

            originalRowIDsArrayList.add(numData, numData);
            doubleVectors.add(new DoubleVector(this, numData, data));

            line = reader.readLine();
            if (line != null)
                line = line.trim();

            numData++;
        }*/
        
        for(int i = 0; i < maxSize; i ++)
        {
            originalRowIDsArrayList.add(i, i);
            
            for(int j = 0; j < dimNum; j ++)
            {
               lineData[j] =  data[i][j];
            }
            doubleVectors.add(new DoubleVectorMR(this, i, lineData));
        }
        
        doubleVectors.trimToSize();
        this.data = doubleVectors;

        originalRowIDs = new int[originalRowIDsArrayList.size()];
        for (int i = 0, e = originalRowIDsArrayList.size(); i < e; i++)
        {
            originalRowIDs[i] = originalRowIDsArrayList.get(i);
        }
    }
}
