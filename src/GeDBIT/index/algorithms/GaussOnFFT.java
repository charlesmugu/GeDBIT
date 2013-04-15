package GeDBIT.index.algorithms;

import java.util.List;

import GeDBIT.dist.Metric;
import GeDBIT.type.IndexObject;
//import GeDBIT.index.algorithms.Eigen;

@SuppressWarnings("serial")
public class GaussOnFFT implements PivotSelectionMethod, java.io.Serializable
{
    //private static final long    serialVersionUID = 6928847050089693231L;
    @SuppressWarnings("unused")
    private final int FFTScale;
    
    public GaussOnFFT(int scale)
    {
        this.FFTScale = scale;
    }

    public int[] selectPivots(Metric metric, List<? extends IndexObject> data, int numPivots)
    {
        final int dataSize = data.size();

        if (numPivots >= dataSize)
        {
            int [] pivots = new int [dataSize];
            for (int i= 0; i<dataSize ; i++)
                pivots[i] = i;       
            
            return IncrementalSelection.removeDuplicate(metric, data, pivots);
        }

        /*
        // run fft to get a candidate set
        int[] fftResult = PivotSelectionMethods.FFT.selectPivots(metric, data, numPivots * FFTScale);

        // compute the distance matrix
        if (fftResult.length <= Math.min(dataSize, numPivots))
            return fftResult;
         */

        //DoubleMatrix2D matrix = LargeDenseDoubleMatrix2D.createDoubleMatrix2D(dataSize, fftResult.length);
        double matrix[][] = new double[dataSize][dataSize];
        for (int col = 0; col < dataSize; col++)
            for (int row = 0; row < dataSize; row++)
                //matrix.set(row, col, metric.getDistance(data.get(row), data.get(fftResult[col])));
                matrix[row][col] = metric.getDistance(data.get(row), data.get(col));

        // compute eigen value for the number of pivots
        Eigen eigen = new Eigen(matrix);
        eigen.calEigen();
        int pNum = eigen.calPivotNumber();
        //System.out.println("Eigen : ");
        //eigen.printBiggestEigen(10);
        //eigen.printSortResult();
        //System.out.println();
        
        Gauss gauss = new Gauss(matrix, dataSize);
        gauss.calSolution();
        
        System.out.println("Gauss : ");
        gauss.printSolutionVectorDouble();
        System.out.println();
        
        int[] result = gauss.getSolution(pNum);
        
        
        System.out.println(result.length);
        for(int i=0; i<result.length; i++)
            System.out.println(result[i]);
        System.out.println();
        
        return result;
    }

    /* (non-Javadoc)
     * @see GeDBIT.index.algorithms.PivotSelectionMethod#selectPivots(GeDBIT.dist.Metric, java.util.List, int, int, int)
     */
    public int[] selectPivots(Metric metric, List<? extends IndexObject> data, int first, int dataSize, int numPivots)
    {
        int[] result = selectPivots(metric, data.subList(first, dataSize), numPivots);
        for (int i = 0; i < result.length; i++)
            result[i] += first;

        return result;
    }

}
