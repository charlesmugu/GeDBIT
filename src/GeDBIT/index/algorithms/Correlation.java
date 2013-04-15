package GeDBIT.index.algorithms;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.doublealgo.Statistic;

public class Correlation
{
    public static int[] runCor(DoubleMatrix2D matrix, int numP)
    {
        int result[] = new int[numP];
        boolean isChosen[] = new boolean[matrix.rows()];
        DoubleMatrix2D coMatrix = Statistic.covariance(matrix);
        for (int i = 1; i < coMatrix.rows(); i++)
        {
            if (coMatrix.get(i, i) > coMatrix.get(result[0], result[0]))
            {
                result[0] = i;
            }
        }
        isChosen[result[0]] = true;
        if (numP > 1)
        {
            Statistic.correlation(coMatrix);
            for (int i = 1; i < numP; i++)
            {
                double minCor[] = new double[i];
                int minCorIndics[] = new int[i];
                for (int j = 0; j < i; j++)
                {
                    for (int k = 0; k < coMatrix.rows(); k++)
                    {
                        if (isChosen[k] == false)
                        {
                            if (Math.abs(coMatrix.get(k, result[j])) < Math
                                    .abs(coMatrix.get(minCorIndics[j],
                                            result[j])))
                            {
                                minCorIndics[j] = k;
                                minCor[j] = Math
                                        .abs(coMatrix.get(k, result[j]));
                            }
                        }
                    }
                }
                int index = 0;
                for (int j = 1; j < minCorIndics.length; j++)
                {
                    
                    if(minCor[j]>minCor[index])
                        index = j;
                }
                result[i] = minCorIndics[index];
                isChosen[minCorIndics[index]]=true;
            }
        }

        return result;

    }
}
