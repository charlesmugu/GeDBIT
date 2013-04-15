/**
 * GeDBIT.index.algorithms.PCAOnFFT 2007.07.18
 *
 * Copyright Information:
 *
 * Change Log:
 * 2007.07.18: created by Rui Mao
 */

package GeDBIT.index.algorithms;

import java.util.List;

import cern.colt.matrix.DoubleMatrix2D;

import GeDBIT.dist.Metric;
import GeDBIT.type.IndexObject;
import GeDBIT.util.LargeDenseDoubleMatrix2D;

public class PCAOnFFT implements PivotSelectionMethod, java.io.Serializable
{
    private static final long    serialVersionUID = 6928847050089693231L;
    private final int FFTScale;
    
    public PCAOnFFT(int scale)
    {
        this.FFTScale = scale;
    }
    

    /* (non-Javadoc)
     * @see GeDBIT.index.algorithms.PivotSelectionMethod#selectPivots(GeDBIT.dist.Metric, java.util.List, int)
     */
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
        if (dataSize < 10)
        {
            System.out.println("size = " + dataSize + ", numPivots = " + numPivots);
            for (int i=0;i<dataSize; i++)
            {
                double []v = ( (GeDBIT.type.DoubleVector)data.get(i)).getData();
                for (int j=0; j<v.length; j++)
                    System.out.print(v[j] + "   ");
                System.out.println();
            }
            System.out.println();
        }*/

        // run fft to get a candidate set
        int[] fftResult = PivotSelectionMethods.FFT.selectPivots(metric, data, numPivots * FFTScale);
        /*
        if (dataSize <10)
        {
            System.out.print("fft result: ");
                for (int j=0; j<fftResult.length; j++)
                    System.out.print(fftResult[j] + "   ");
                System.out.println();
        }*/
            

        // compute the distance matrix
        if (fftResult.length <= Math.min(dataSize, numPivots))
            return fftResult;

        DoubleMatrix2D matrix = LargeDenseDoubleMatrix2D.createDoubleMatrix2D(dataSize, fftResult.length);
        for (int col = 0; col < fftResult.length; col++)
            for (int row = 0; row < dataSize; row++)
                matrix.set(row, col, metric.getDistance(data.get(row), data.get(fftResult[col])));
        /*
        if (dataSize <10)
        {
            System.out.println(matrix);
        }*/

        // compute PCA with EM method
        try
        {
            matrix = GeDBIT.index.algorithms.PCA.EMPCA(matrix, numPivots);
        }
        catch (Exception e)
        {
            System.out.println("Exception!  data size =" + dataSize + ", num pivots = " + numPivots);
            e.printStackTrace();
            if (numPivots >= fftResult.length)
            {
                return fftResult;
            }
            else
            {
                int [] pivots = new int [numPivots];
                System.arraycopy(fftResult,0, pivots, 0, numPivots);
                
//                for (int j=0; j<fftResult.length; j++)
//                    System.out.print(fftResult[j] + "   ");
//                System.out.println();

                return pivots;
            }
        }

        // select pivots from the pca result
        int[] result = GeDBIT.index.algorithms.PCA.pivotSelectionByPCAResultAngle(matrix, numPivots);
        for (int i = 0; i < result.length; i++)
            result[i] = fftResult[result[i]];

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
