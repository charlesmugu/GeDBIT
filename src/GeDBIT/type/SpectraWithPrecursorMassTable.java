package GeDBIT.type;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import GeDBIT.dist.MSMSMetric;
import GeDBIT.dist.Metric;

public class SpectraWithPrecursorMassTable extends Table
{
    /**
     * 
     */
    private static final long  serialVersionUID = -6784466397812636659L;

    /**
     * 
     */
    public static final Metric DEFAULT_METRIC   = new MSMSMetric();

    /**
     * @param fileName
     * @param maxDataSize
     * @throws FileNotFoundException
     */
    public SpectraWithPrecursorMassTable(String fileName, String indexPrefix, int maxDataSize) throws FileNotFoundException
    {
        this(fileName, indexPrefix, maxDataSize, DEFAULT_METRIC);
    }

    /**
     * @param fileName
     * @param size
     * @param metric
     * @throws FileNotFoundException
     */
    public SpectraWithPrecursorMassTable(String fileName, String indexPrefix, int size, Metric metric) throws FileNotFoundException
    {
        super(fileName, indexPrefix, size, metric);

        BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(fileName));
        loadData(reader, size);
    }

    /**
     * @param reader
     * @param size
     */
    private void loadData(BufferedReader reader, int size)
    {

        String line;
        ArrayList<SpectraWithPrecursorMass> spectraWPM = null;
        ArrayList<Integer> originalRowIDsArrayList = null;
        // System.out.println("Loading... ");
        try
        {
            // read sequences from file
            line = reader.readLine(); // read the first line
            if (line != null)
                line = line.trim();
            // get total rows from first line and allocate the ArrayList
            int numSpectra = java.lang.Integer.parseInt(line);
            spectraWPM = new ArrayList<SpectraWithPrecursorMass>(numSpectra);
            originalRowIDsArrayList = new ArrayList<Integer>(numSpectra);

            // read line byte line
            line = reader.readLine();
            if (line != null)
                line = line.trim();

            int count = 0;
            while (line != null && count < size)
            {
                if (count % 1000 == 0)
                {
                    // System.out.print(count+".. ");
                }
                String[] row = line.split(" ", 3);
                originalRowIDsArrayList.add(count, new Integer(row[0]).intValue());
                spectraWPM.add(new SpectraWithPrecursorMass(this, count, new Double(row[1]).doubleValue(), row[2]));

                line = reader.readLine();
                if (line != null)
                    line = line.trim();
                count++;
            }
        }
        catch (java.io.IOException e)
        {
            e.printStackTrace();
            throw new java.lang.IllegalStateException("Error occured when reading msms file: " + reader + " error message returned: "
                    + e.getMessage());
        }
        catch (NumberFormatException e)
        {
            e.printStackTrace();
            /* Ignore strings with invalid characters. */
        }

        // debug
        // System.out.println(seqs);
        spectraWPM.trimToSize();
        data = spectraWPM;

        originalRowIDs = new int[originalRowIDsArrayList.size()];
        for (int i = 0, e = originalRowIDsArrayList.size(); i < e; i++)
        {
            originalRowIDs[i] = originalRowIDsArrayList.get(i);
        }
    }

}
