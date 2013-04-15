/**
 * GeDBIT.app.BuildVPIndex.java 2006.06.25
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.06.25: Modified from jdb 1.0 VPBulkLoader.java, by Rui Mao
 * 2007.07.12: added incremental pivot selection, by Rui Mao
 */

package GeDBIT.app;

import java.util.logging.Level;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import GeDBIT.dist.Metric;
import GeDBIT.dist.CountedMetric;
import GeDBIT.type.DNATable;
import GeDBIT.type.DoubleVectorTable;
import GeDBIT.type.ImageTable;
import GeDBIT.type.PeptideTable;
import GeDBIT.type.RNATable;
import GeDBIT.type.SpectraTable;
import GeDBIT.type.SpectraWithPrecursorMassTable;
import GeDBIT.type.StringTable;
import GeDBIT.type.Table;
import GeDBIT.index.algorithms.PartitionMethods;
import GeDBIT.index.algorithms.PivotSelectionMethods;
import GeDBIT.index.algorithms.PartitionMethod;
import GeDBIT.index.algorithms.PivotSelectionMethod;
import GeDBIT.index.VPIndex; //for javadoc

/**
 * This is the main entry point for building a VP Index. It can build either one one {@link VPIndex}, 
 * or a series of {@link VPIndex}es.
 * 
 * The basic commandline options are:
 * 
 * -n [data file name]
 * -psm [the pivot selection selection method: random, fft, center, pcaonfft, pca]
 * -v [number of pivots in an index node]
 * -dpm [data partition method: balanced, clusteringkmeans, clusteringboundary]
 * -f [fanout of a pivot]
 * -m [maximum number of children in leaf nodes]
 * -pl [path length, default 0]
 * -t [data type, one of "protein", "dna", "vector", "image", "msms","string"]
 * -g [debug level]
 * -frag [fragment length, only meaningful for sequences]
 * -dim [dimension of vector data to load]
 * -b [whether bucketing will be used, 1: use]
 * -r maximum radius for partition
 * -seta the value of A in the incremental pivot selection
 * -setn the value of N in the incremental pivot selection
 * -o the prefix of the output index file
 * -fftscale use for pcaonfft pivot selection method
 * -fftopt [0:use old version FFT, 1: use new version FFT but don't choose the third point got from FFT as the second pivot, 2:use new version FFT and choose the third point got from FFT as the second pivot. Default 0]
 * -rn [whether count r neighborhood, 1: use, default 0]
 * 
 * For build multiple databases, use the following options:
 * 
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
 * @author Rui Mao, Willard
 * @version 2007.07.12
 */

public class BuildVPIndex
{

    /**
     * @param args
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
        //final String indexExtName = ".index";
        String fileName = "1m.vector";
        String dataType = "vector";
        String psmName = "";
        String forPrint = "";

        String selectAlgorithm = "";
        String testKind = "";
        String yMethod = "";
        
        for (int i = 0; i < args.length; i = i + 2)
        {
            if (args[i].equalsIgnoreCase("-n"))
                fileName = args[i + 1];

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
            {
                psmName = args[i + 1];
            }

            else if (args[i].equalsIgnoreCase("-dpm"))
            {
                dpm = PartitionMethods.valueOf(args[i + 1].toUpperCase());
                PartitionMethods.pm = args[i + 1].toUpperCase();
            }

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

            else if (args[i].equalsIgnoreCase("-r")){
                maxR = Double.parseDouble(args[i + 1]);
                PartitionMethods.r = Double.parseDouble(args[i + 1]);
                }

            else if (args[i].equalsIgnoreCase("-g"))
                debug = Level.parse(args[i + 1]);
            
            else if (args[i].equalsIgnoreCase("-sa"))
                selectAlgorithm = args[i+1];
            
            else if (args[i].equalsIgnoreCase("-ym"))
                yMethod = args[i+1];
            
            else if (args[i].equalsIgnoreCase("-tkind"))
                testKind = args[i+1];
            else if (args[i].equalsIgnoreCase("-fftopt"))
                PivotSelectionMethods.fftopt = Integer.parseInt(args[i+1]);
            else if (args[i].equalsIgnoreCase("-rn"))
                PartitionMethods.countRN = Integer.parseInt(args[i + 1]);

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
            //������Ѳ����ȥ
            psm = new GeDBIT.index.algorithms.SelectionOnFFT(fftScale, testKind, yMethod, selectAlgorithm);
            //psm = new GeDBIT.index.algorithms.SelectionOnFFT(fftScale);
        else if (psmName.equalsIgnoreCase("eigen"))
            psm = new GeDBIT.index.algorithms.EigenOnFFT(fftScale);
        else if (psmName.equalsIgnoreCase("gauss"))
            psm = new GeDBIT.index.algorithms.GaussOnFFT(fftScale);
        else
            psm = PivotSelectionMethods.valueOf(psmName.toUpperCase());
        
//        String forPrint = Integer.toString(fftScale);

        //bulk load all the files
        batchBulkLoad(fileName, indexPrefix, dataType, dim, frag, initialSize, finalSize, stepSize, mls, pNum, sf, debug, pathLength, psm, dpm,
                bucket, forPrint);

    }

    /**
     * @param fileName
     * @param dataType
     * @param dim
     * @param frag
     * @param initialSize
     * @param finalSize
     * @param stepSize
     * @param indexName
     * @param mls
     * @param pNum
     * @param sf
     * @param debug
     * @param pathLength
     * @param psm
     * @param dpm
     * @param bucket
     */
    public static void batchBulkLoad(String fileName, String indexPrefix, String dataType, int dim, int frag, int initialSize, int finalSize, int stepSize,
            int mls, int pNum, int sf, Level debug, int pathLength, PivotSelectionMethod psm, PartitionMethod dpm, boolean bucket, String forPrint)
    {
        //statistics
        double startTime, endTime;
        final int indexNum = (finalSize - initialSize) / stepSize + 1;
        double[] buildTimes = new double[indexNum];
        int[] distCalNum = new int[indexNum];

        for (int size = initialSize, i = 0; (size <= finalSize) & (i < indexNum); size += stepSize, i++)
        {
            String currentIndexPrefix = indexPrefix + "-" + size;

            if (!removeFilesWithHeader(currentIndexPrefix + "."))
            {
                System.out.println("Can not remove old files!");
                System.exit(-1);
            }

            if (!removeFilesWithHeader(currentIndexPrefix + "-"))
            {
                System.out.println("Can not remove old files!");
                System.exit(-1);
            }

            Table table = null;
            try
            {
                table = getTable(dataType, fileName, currentIndexPrefix, size, frag, dim);
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

            table.buildVPIndex(psm, pNum, dpm, sf, mls, pathLength, bucket, debug, forPrint);

            endTime = System.currentTimeMillis();

            buildTimes[i] = (endTime - startTime) / 1000.00;
            distCalNum[i] = countMetric.getCounter();
            countMetric.clear();
        }

        //all indecies had been built, now out put the statistics.
        System.out.println("Database size, building time(seconds), #distance calculation:");
        for (int i = 0; i < indexNum; ++i)
            System.out.println((initialSize + stepSize * i) + ", " + buildTimes[i] + ", " + distCalNum[i]);
    }

    /**
     * remove all files with the given header, a utility method, not to be used by user.
     * @param header header of file name, can have directory name, all files can be expressed as header* are deleted
     * @return true if runs successfully
     */
    public static boolean removeFilesWithHeader(String header)
    {
        if (header == null)
            return true;

        class HeaderFilter implements FileFilter
        {
            String header;

            HeaderFilter(File f)
            {
                header = f.getAbsolutePath();
            }

            public boolean accept(File pathName)
            {
                String input = pathName.getAbsolutePath();
                return input.startsWith(header);
            }
        }
        ;

        File f = new File(header);
        File parent = (new File(f.getAbsolutePath())).getParentFile();
        //System.out.println("Parent:" + parent.getAbsolutePath() );

        File[] toDelete = parent.listFiles(new HeaderFilter(f));
        if (toDelete == null)
            return true;
        
        for (int i = 0; i < toDelete.length; i++)
        {
            if (!toDelete[i].delete())
            {
                System.out.println("Could not delete file:" + toDelete[i].getAbsolutePath());
                return false;
            }
            //System.out.println("File: " + toDelete[i].getAbsolutePath() + " deleted.");
        }

        return true;
    }

    public static Table getTable(String dataType, String fileName, String indexPrefix, int maxDataSize, int fragLength, int dim) throws IOException
    {
        Table table = null;

        //TODO remove the null
        if (dataType.equalsIgnoreCase("ms"))
        {
            table = new SpectraTable(fileName, indexPrefix, maxDataSize, null);
        }
        else if (dataType.equalsIgnoreCase("msms"))
        {
            table = new SpectraWithPrecursorMassTable(fileName, indexPrefix, maxDataSize);
        }
        else if (dataType.equalsIgnoreCase("dna"))
        {
            table = new DNATable(fileName, indexPrefix, maxDataSize, fragLength);
        }
        else if (dataType.equalsIgnoreCase("rna"))
        {
            table = new RNATable(fileName, indexPrefix, maxDataSize, fragLength);
        }
        else if (dataType.equalsIgnoreCase("protein"))
        {
            table = new PeptideTable(fileName, indexPrefix, maxDataSize, fragLength);
        }
        else if (dataType.equalsIgnoreCase("vector"))
        {
            table = new DoubleVectorTable(fileName, indexPrefix, maxDataSize, dim);
        }
        else if (dataType.equalsIgnoreCase("image"))
        {
            table = new ImageTable(fileName, indexPrefix, maxDataSize);
        }else if(dataType.equalsIgnoreCase("string")){
        table = new StringTable(fileName, indexPrefix, maxDataSize, new GeDBIT.dist.EditDistance());
        }
        else
        {
            throw new Error("Invalid dataType!");
        }
        return table;
    }

}
