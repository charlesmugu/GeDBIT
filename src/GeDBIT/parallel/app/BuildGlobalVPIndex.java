package GeDBIT.parallel.app;

import java.io.IOException;
import java.util.logging.Level;

import GeDBIT.app.BuildVPIndex;
import GeDBIT.dist.CountedMetric;
import GeDBIT.dist.Metric;
import GeDBIT.index.VPIndex;
import GeDBIT.index.algorithms.PartitionMethod;
import GeDBIT.index.algorithms.PartitionMethods;
import GeDBIT.index.algorithms.PivotSelectionMethod;
import GeDBIT.index.algorithms.PivotSelectionMethods;
import GeDBIT.type.Table;


public class BuildGlobalVPIndex
{
    /**
     * This is the main entry point for building a VP Index to parallel query. It can build either one one {@link VPIndex}, 
     * or a series of {@link VPIndex}es.
     * 
     * The basic command line options are:
     * 
     * -n [data file name]
     * -psm [the pivot selection selection method: random, fft, center, pcaonfft, pca]
     * -v [number of pivots in an index node]
     * -dpm [data partition method: balanced, clusteringkmeans, clusteringboundary]
     * -f [fanout of a pivot]
     * -m [maximum number of children in leaf nodes]
     * -pl [path length, default 0]
     * -st [subtree number for parallel query]
     * -t [data type, one of "protein", "dna", "vector", "image", "msms"]
     * -g [debug level]
     * -frag [fragment length, only meaningful for sequences]
     * -dim [dimension of vector data to load]
     * -b [whether bucketing will be used, 1: use]
     * -r maximum radius for partition
     * -seta the value of A in the incremental pivot selection
     * -setn the value of N in the incremental pivot selection
     * -o the prefix of the output index file
     * -fftscale use for pcaonfft pivot selection method
     * 
     * For build multiple databases, use the following options:
     * -i [size of smallest database]
     * -a [size of largest database]
     * -s [step size of databases]
     * 
     * For using multiple regression algorithms, use the following options:
     * 
     * -sa [select algorithm]
     *  forward for forward selection, backward for backward selection, enumerate for enumerate selection
     * -ym [the method to calculate y array]
     *  standard for standard deviation method, average for average method
     * -tkind [test kind]
     *  ftest for F-test, rss for comparing rss
     * The {@link Metric} is hardcoded for each data type.
     * @author Rui Mao, Miaojie Feng
     * @version 2012.12.29
     */
    
    public static void main(String[] args)
    {
        int pNum = 3;
        int sf = 3;
        int mls = 100;
        int initialSize = 100000;
        int finalSize = 1000000;
        int stepSize = 100000;
        Level debug = Level.OFF;
        int pathLength = 0;
        PartitionMethod dpm = PartitionMethods.valueOf("BALANCED");
        PivotSelectionMethod psm = PivotSelectionMethods.valueOf("FFT");
        int frag = 6;
        int dim = 2;
        boolean bucket = true;
        double maxR = 0.1;

        int setA = 10000;
        int setN = 50;
        int fftScale = 100;
        String indexPrefix = "vpindex";
        String fileName = "1m.vector";
        String dataType = "vector";
        String psmName = "";
        String forPrint = "";

        String selectAlgorithm = "";
        String testKind = "";
        String yMethod = "";
        
        // default subtree number 1 client/1 server
        int subtree = 1;
        
        for (int i = 0; i < args.length; i = i + 2)
        {
            if (args[i].equalsIgnoreCase("-n"))
                fileName = args[i + 1];
            
            else if (args[i].equals("-st"))
            {
                subtree = Integer.parseInt(args[i + 1]);
                if(subtree < 0)
                {
                    try
                    {
                        throw new IllegalArgumentException("Invalid option -st");
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            
            else if (args[i].equalsIgnoreCase("-forprint"))
                forPrint += args[i + 1] + ", ";

            else if (args[i].equalsIgnoreCase("-v"))
                pNum = Integer.parseInt(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-seta"))
                setA = Integer.parseInt(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-setn"))
                setN = Integer.parseInt(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-f"))
                sf = Integer.parseInt(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-fftscale"))
                fftScale = Integer.parseInt(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-pl"))
                pathLength = Integer.parseInt(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-psm"))
                psmName = args[i + 1];

            else if (args[i].equalsIgnoreCase("-dpm"))
                dpm = PartitionMethods.valueOf(args[i + 1].toUpperCase());

            else if (args[i].equalsIgnoreCase("-m"))
                mls = Integer.parseInt(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-o"))
                indexPrefix = args[i + 1];

            else if (args[i].equalsIgnoreCase("-t"))
                dataType = args[i + 1];

            else if (args[i].equalsIgnoreCase("-i"))
                initialSize = Integer.parseInt(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-a"))
                finalSize = Integer.parseInt(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-s"))
                stepSize = Integer.parseInt(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-b"))
                bucket = (Integer.parseInt(args[i + 1]) == 1);

            else if (args[i].equalsIgnoreCase("-frag"))
                frag = Integer.parseInt(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-dim"))
                dim = Integer.parseInt(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-r"))
                maxR = Double.parseDouble(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-g"))
                debug = Level.parse(args[i + 1]);
            
            else if (args[i].equalsIgnoreCase("-sa"))
                selectAlgorithm = args[i+1];
            
            else if (args[i].equalsIgnoreCase("-ym"))
                yMethod = args[i+1];
            
            else if (args[i].equalsIgnoreCase("-tkind"))
                testKind = args[i+1];

            else
                throw new IllegalArgumentException("Invalid option " + args[i]);
        }

        dpm.setMaxRadius(maxR);
        
        //hack, if cght, use clustering partition, and set maxr to -1 to denote it
        if (dpm == PartitionMethods.CGHT)
        {
            dpm = PartitionMethods.CLUSTERINGKMEANS;
            dpm.setMaxRadius(-2);
        }
        if (dpm == PartitionMethods.GHT)
        {
            dpm = PartitionMethods.CLUSTERINGKMEANS;
            dpm.setMaxRadius(-1);
        }

        if (psmName.equalsIgnoreCase("incremental"))
            psm = new GeDBIT.index.algorithms.IncrementalSelection(setA, setN);
        else if (psmName.equalsIgnoreCase("pcaonfft"))
            psm = new GeDBIT.index.algorithms.PCAOnFFT(fftScale);
        else if (psmName.equalsIgnoreCase("selectiononfft"))
            psm = new GeDBIT.index.algorithms.SelectionOnFFT(fftScale, testKind, yMethod, selectAlgorithm);
        else if (psmName.equalsIgnoreCase("eigen"))
            psm = new GeDBIT.index.algorithms.EigenOnFFT(fftScale);
        else if (psmName.equalsIgnoreCase("gauss"))
            psm = new GeDBIT.index.algorithms.GaussOnFFT(fftScale);
        else
            psm = PivotSelectionMethods.valueOf(psmName.toUpperCase());
        
        //bulk load all the files
        if(subtree == 0)
        {
            BuildVPIndex.batchBulkLoad(fileName, indexPrefix, dataType, dim, frag, initialSize, finalSize, stepSize, mls, pNum, sf, debug, pathLength, psm, dpm,
                    bucket, forPrint);
        } else if(subtree >= 1) {
            batchBulkLoad(fileName, indexPrefix, dataType, dim, frag, initialSize, finalSize, stepSize, mls, pNum, sf, debug, pathLength, psm, dpm,
                    bucket, forPrint, subtree);
        }

    }
    
    protected static void batchBulkLoad(String fileName, String indexPrefix, String dataType, int dim, int frag, int initialSize, int finalSize, int stepSize,
            int mls, int pNum, int sf, Level debug, int pathLength, PivotSelectionMethod psm, PartitionMethod dpm, boolean bucket, String forPrint, int subtree)
    {
        //statistics
        double startTime, endTime;
        final int indexNum = (finalSize - initialSize) / stepSize + 1;
        double[] buildTimes = new double[indexNum];
        int[] distCalNum = new int[indexNum];
        
        for (int size = initialSize, i = 0; (size <= initialSize) & (i < indexNum); size += stepSize, i++)
        {
            String currentIndexPrefix = indexPrefix + "-" + size;
            if (!BuildVPIndex.removeFilesWithHeader(currentIndexPrefix + "-"))
            {
                System.out.println("Can not remove old files!");
                System.exit(-1);
            }

            Table table = null;
            try
            {
                table = BuildVPIndex.getTable(dataType, fileName, currentIndexPrefix, size, frag, dim);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            CountedMetric countMetric = new CountedMetric(table.getMetric());

            if (debug != Level.OFF)
                System.out.println(" building size:" + table.size() + "..., ");

            startTime = System.currentTimeMillis();
            table.setMetric(countMetric);

            table.buildGlobalVPIndex(psm, pNum, dpm, sf, mls, pathLength, bucket, debug, forPrint, subtree);

            endTime = System.currentTimeMillis();

            buildTimes[i] = (endTime - startTime) / 1000.00;
            distCalNum[i] = countMetric.getCounter();
            countMetric.clear();
        }

        //all had been built, now out put the statistics.
        System.out.println("Database size, building time(seconds), #distance calculation:");
        for (int i = 0; i < indexNum; ++i)
            System.out.println((initialSize + stepSize * i) + ", " + buildTimes[i] + ", " + distCalNum[i]);
    }
}
