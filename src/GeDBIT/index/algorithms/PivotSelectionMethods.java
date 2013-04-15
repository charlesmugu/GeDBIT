/**
 * GeDBIT.index.algorithms.PivotSelectionMethods 2006.06.16
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.01.25: Added, by Willard
 */
package GeDBIT.index.algorithms;

import java.util.List;
import java.util.Random;

import GeDBIT.dist.Metric;
import GeDBIT.type.IndexObject;
import GeDBIT.util.FindPivotWithLargestVar;
import GeDBIT.util.LargeDenseDoubleMatrix2D;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

/**
 * All the built-in pivot selection methods. FFT: use Farthest-First-Traversal
 * to find the corners of the data. Linear time. CENTER: choose the centers of
 * the internal clusters, a method similar to CLARA. Slow RANDOM: select pivots
 * randomly, first, but no performance guarantee. FFTANDPCA: a combination of
 * FFT and Principal Component Analysis. Slow, but performs the best
 * 
 * @author Rui Mao, Willlard
 * @version 2006.08.03
 */
public enum PivotSelectionMethods implements PivotSelectionMethod
{

    FFT
    {
        public int[] selectPivots(Metric metric,
                List<? extends IndexObject> data, int numPivots)
        {
            return selectPivots(metric, data, 0, data.size(), numPivots);
        }

        public int[] selectPivots(Metric metric,
                List<? extends IndexObject> data, int first, int dataSize,
                int numPivots)
        {
            int firstPivot = first;
            // if ffopt == 0, use the old version FFT
            if (0 != fftopt)
                firstPivot = FindPivotWithLargestVar.findPivotByVarold(metric,
                        data);
            // firstPivot = FindPivotWithLargestVar.findPivotByVar(metric,
            // data, first, dataSize);
            // Math.floor(first + Math.random() *
            // dataSize);
            return selectPivots(metric, data, first, dataSize, numPivots,
                    firstPivot);
        }

        public int[] selectPivots(Metric metric,
                List<? extends IndexObject> data, int first, int dataSize,
                int numPivots, int firstPivot)
        {
            if (numPivots >= dataSize)
            {
                int[] pivots = new int[dataSize];
                for (int i = first; i < dataSize + first; i++)
                    pivots[i - first] = i;

                return IncrementalSelection.removeDuplicate(metric, data,
                        pivots);
            }

            boolean[] isCenter = new boolean[dataSize];
            double[] minDist = new double[dataSize];
            for (int i = 0; i < dataSize; i++)
            {
                isCenter[i] = false;
                minDist[i] = Double.POSITIVE_INFINITY;
            }

            isCenter[firstPivot] = true;

            int[] indices; // offsets of the pivots in the original data list
            if (2 > fftopt) // if fft == 2, then choose the third point got from
                            // FFT as the second povit
                indices = new int[numPivots];
            else
                indices = new int[numPivots + 1];

            indices[0] = firstPivot;
            for (int i = 1; i < indices.length; i++)
                indices[i] = -1;
            // array counter init to 1 since the first point is found already
            for (int centerSize = 1; centerSize < indices.length; centerSize++)
            {
                double currMaxDist = Double.NEGATIVE_INFINITY;
                final IndexObject lastCenter = data
                        .get(indices[centerSize - 1]);

                for (int i = 0; i < dataSize; i++)
                {
                    if (isCenter[i] == false) // if point is not a center
                    {
                        double tempDist = metric.getDistance(
                                data.get(i + first), lastCenter);

                        minDist[i] = (tempDist < minDist[i]) ? tempDist
                                : minDist[i];

                        // TODO
                        if (minDist[i] > currMaxDist)
                        {
                            indices[centerSize] = i; // save the index the
                                                     // current farthest
                                                     // point
                            currMaxDist = minDist[i];
                        }

                    }
                }

                if (indices[centerSize] == -1)
                    break;
                else
                    isCenter[indices[centerSize]] = true;
            }
            int returnSize = 0;
            while ((returnSize < indices.length) && (indices[returnSize] >= 0))
                returnSize++;

            if (returnSize < indices.length)
            {
                int[] result = new int[returnSize];
                System.arraycopy(indices, 0, result, 0, returnSize);
                return result;
            } else if (2 > fftopt) // if fft == 2, then choose the third point
                                   // got from FFT as the second povit
                return indices;
            else
            {
                int[] reindecs = { 0, 0 };
                reindecs[0] = indices[0];
                reindecs[1] = indices[2];
                return reindecs;
            }
        }
    },
    CENTER
    {
        public int[] selectPivots(Metric metric,
                List<? extends IndexObject> data, int numPivots)
        {
            return selectPivots(metric, data, 0, data.size(), numPivots);
        }

        public int[] selectPivots(Metric metric,
                List<? extends IndexObject> data, int first, int dataSize,
                int numPivots)
        {
            // TODO
            return null;
        }
    },
    RANDOM
    {
        public int[] selectPivots(Metric metric,
                List<? extends IndexObject> data, int numPivots)
        {
            return selectPivots(metric, data, 0, data.size(), numPivots);
        }

        public int[] selectPivots(Metric metric,
                List<? extends IndexObject> data, int first, int dataSize,
                final int numPivots)
        {
            return selectPivots(metric, data, first, dataSize, numPivots, false);
        }

        public int[] selectPivots(Metric metric,
                List<? extends IndexObject> data, final int first,
                int dataSize, final int numPivots, boolean debug)
        {
            return randomPivot(metric, data.subList(first, first + dataSize),
                    numPivots);
        }

        /**
         * select pivots randomly. No duplicates are allowed in return.
         * 
         * @param metric
         * @param data
         * @param numP
         *            number of pivots to select
         * @return an int array of subscripts of the pivots in the input data
         *         array.
         */
        int[] randomPivot(Metric metric, List<? extends IndexObject> data,
                int numP)
        {
            // final boolean debug = true;
            final int LoopConstant = 5;
            final int size = data.size();

            // if number of pivots to select is not smaller than the dataset
            // size
            // return all the not identical points
            if (numP >= size)
            {
                int[] result = new int[size];
                int counter = 1; // number of pivots selected
                result[0] = 0;
                for (int i = 1; i < size; i++)
                {
                    if (!containsZeroDistance(metric, data, result,
                            data.get(i), 0, counter))
                    {
                        result[counter] = i;
                        counter++;
                    }
                }

                if (counter == size) // no duplicate
                    return result;
                else
                {
                    int[] r = new int[counter];
                    System.arraycopy(result, 0, r, 0, counter);
                    return r;
                }

            }

            // number of pivots is less than dataset size. linear scan to
            // randomly choose pivots, be
            // careful to the duplicate.
            int counter = 0; // number of pivots selected
            int[] result = new int[numP];
            Random r = new Random();
            for (int j = 0; j < LoopConstant; j++)
            {
                for (int i = 0; i < size; i++)
                {
                    double d = (double) (numP - counter) / size;
                    double nd = r.nextDouble();
                    // System.out.println("d =" + d + ", nd = " + nd);
                    if ((d > nd)
                            && !containsZeroDistance(metric, data, result,
                                    data.get(i), 0, counter))
                    {
                        result[counter] = i;
                        counter++;
                        if (counter >= numP)
                            break;
                    }
                }

                if (counter >= numP)
                    break;
            }

            // if enough number of pivots are found, just return it.
            if (counter >= numP)
                return result;

            // otherwise, which means too much duplicate, scan it again.
            int[] subscript = new int[size];
            for (int i = 0; i < size; i++)
                subscript[i] = i;

            int remain = size; // number of points among which to select
                               // pivots, the duplicates
            // that have already been identified are not included.

            int temp = 0;
            while ((counter < numP) && (remain > 0))
            {
                for (int i = 0; i < remain; i++)
                {
                    if (r.nextDouble() < (double) (numP - counter) / remain)
                    {
                        if (containsZeroDistance(metric, data, result,
                                data.get(subscript[i]), 0, counter))
                        {
                            remain--;
                            if (remain <= 0)
                                break;

                            temp = subscript[i];
                            subscript[i] = subscript[remain];
                            subscript[remain] = temp;
                        } else
                        {
                            result[counter] = subscript[i];
                            counter++;
                            if (counter >= numP)
                                break;
                        }
                    }
                }
                if (counter >= numP)
                    break;
            }

            // if enough pivots are found, return it
            if (counter >= numP)
                return result;

            // otherwise, return all the pivots found
            int[] rr = new int[counter];
            System.arraycopy(result, 0, rr, 0, counter);
            return rr;
        }

        /**
         * @param metric
         * @param data
         * @param subscript
         * @param probe
         * @param first
         * @param last
         * @return
         */
        boolean containsZeroDistance(Metric metric,
                List<? extends IndexObject> data, int[] subscript,
                IndexObject probe, int first, int last)
        {
            if (data == null)
                return false;

            boolean contains = false;

            int i = first;
            while ((i < last) && !contains)
            {
                if (metric.getDistance(data.get(subscript[i]), probe) == 0)
                {
                    contains = true;
                    break;
                } else
                    i++;
            }

            return contains;
        }

    },
    PCA
    {

        public int[] selectPivots(Metric metric,
                List<? extends IndexObject> data, int numPivots)
        {
            return selectPivots(metric, data, 0, data.size(), numPivots);
        }

        public int[] selectPivots(Metric metric,
                List<? extends IndexObject> data, int first, int dataSize,
                int numPivots)
        {
            // compute the distance matrix
            DoubleMatrix2D matrix = GeDBIT.index.algorithms.PCA
                    .pairWiseDistance(metric, data);

            // compute PCA with EM method
            matrix = GeDBIT.index.algorithms.PCA.EMPCA(matrix, numPivots);

            // select pivots from the pca result
            return GeDBIT.index.algorithms.PCA.pivotSelectionByPCAResultAngle(
                    matrix, numPivots);
        }
    },
    EPCAF
    {
        final int FFTScale       = 10;

        final int NumPCScale     = 2;

        final int NumPivotEachPC = 2;

        public int[] selectPivots(Metric metric,
                List<? extends IndexObject> data, int first, int dataSize,
                int numPivots)
        {
            int[] result = selectPivots(metric, data.subList(first, dataSize),
                    numPivots);
            for (int i = 0; i < result.length; i++)
                result[i] += first;

            return result;
        }

        public int[] selectPivots(Metric metric,
                List<? extends IndexObject> data, int numPivots)
        {
            final int dataSize = data.size();

            // run fft to get a candidate set
            int[] fftResult = FFT.selectPivots(metric, data, numPivots
                    * FFTScale);
            for (int i = 0; i < fftResult.length; i++)
                System.out.print(fftResult[i] + "  ");
            System.out.println();

            // compute the distance matrix
            if (fftResult.length <= Math.min(dataSize, numPivots))
                return fftResult;

            DoubleMatrix2D dataMatrix = LargeDenseDoubleMatrix2D
                    .createDoubleMatrix2D(dataSize, fftResult.length);
            for (int col = 0; col < fftResult.length; col++)
                for (int row = 0; row < dataSize; row++)
                    dataMatrix.set(
                            row,
                            col,
                            metric.getDistance(data.get(row),
                                    data.get(fftResult[col])));

            // compute PCA with EM method, dataMatrix is centerized after the
            // operation.
            DoubleMatrix2D pcaResult = GeDBIT.index.algorithms.PCA.EMPCA(
                    dataMatrix, numPivots * NumPCScale);

            // select pivots from the pca result
            int[] result = GeDBIT.index.algorithms.PCA
                    .pivotSelectionByPCAResultProjection(dataMatrix, pcaResult,
                            numPivots * NumPCScale, numPivots * NumPCScale
                                    * NumPivotEachPC);
            for (int i = 0; i < result.length; i++)
                System.out.print(result[i] + "  ");
            System.out.println();

            return result;
        }
    },
    LLEONFFT
    {
        final int FFTScale = 100;
        @SuppressWarnings("unused")
        int       count    = 1;

        public int[] selectPivots(Metric metric,
                List<? extends IndexObject> data, int first, int dataSize,
                int numPivots)
        {
            // run fft to get a candidate set
            int[] fftResult = FFT.selectPivots(metric, data, numPivots
                    * FFTScale);
            for (int i = 0; i < fftResult.length; i++)

                // compute the distance matrix
                if (fftResult.length <= Math.min(dataSize, numPivots))
                    return fftResult;

            DoubleMatrix2D dataMatrix = new DenseDoubleMatrix2D(dataSize,
                    fftResult.length);
            for (int col = 0; col < fftResult.length; col++)
                for (int row = 0; row < dataSize; row++)
                    dataMatrix.set(
                            row,
                            col,
                            metric.getDistance(data.get(row),
                                    data.get(fftResult[col])));
            DoubleMatrix2D lleResult = GeDBIT.index.algorithms.LLE.runLLE(
                    dataMatrix.viewDice(), 2);
            int result[] = GeDBIT.index.algorithms.LLE.selectFromResult(
                    dataMatrix.viewDice(), lleResult);
            for (int i = 0; i < result.length; i++)
                result[i] += first;
            return result;

        }

        public int[] selectPivots(Metric metric,
                List<? extends IndexObject> data, int numPivots)
        {
            final int dataSize = data.size();
            System.out.println(dataSize);
            if (data.size() > 18)
            {
                return selectPivots(metric, data, 0, data.size(), numPivots);
            } else
            {
                return FFT
                        .selectPivots(metric, data, 0, data.size(), numPivots);

            }

        }
    },

    LLE
    {
        int count;

        /**
         * @param metric
         * @param data
         * @param numPivots
         * @return
         */
        public int[] selectPivots(Metric metric,
                List<? extends IndexObject> data, int numPivots)
        {
            System.out.println(data.size());
            if (count == 0 || data.size() >= 10000)
            {
                count = 1;
                System.out.println("Using FFT");
                return FFT
                        .selectPivots(metric, data, 0, data.size(), numPivots);
            } else
            {
                if (data.size() > 15)
                    return selectPivots(metric, data, 0, data.size(), numPivots);
                else
                    return FFT.selectPivots(metric, data, 0, data.size(),
                            numPivots);
            }

        }

        /**
         * @param metric
         * @param data
         * @param first
         * @param dataSize
         * @param numPivots
         * @return
         */
        public int[] selectPivots(Metric metric,
                List<? extends IndexObject> data, int first, int dataSize,
                int numPivots)
        {
            // compute the distance matrix
            DoubleMatrix2D matrix = GeDBIT.index.algorithms.LLE
                    .pairWiseDistance(metric, data);
            // compute LLE
            DoubleMatrix2D mat = GeDBIT.index.algorithms.LLE.runLLE(matrix,
                    numPivots);

            // select pivots from the LLE result
            return GeDBIT.index.algorithms.LLE.selectByCov(matrix, mat);
        }
    },
    MDS
    {
        int count;

        /**
         * @param metric
         * @param data
         * @param numPivots
         * @return
         */
        public int[] selectPivots(Metric metric,
                List<? extends IndexObject> data, int numPivots)
        {
            System.out.print("datasize: ");
            System.out.println(data.size());
            if (count == 0 || data.size() > 40000)
            {
                count = 1;
                System.out.println("using FFT");
                return FFT
                        .selectPivots(metric, data, 0, data.size(), numPivots);
            } else
                return selectPivots(metric, data, 0, data.size(), numPivots);

        }

        /**
         * @param metric
         * @param data
         * @param first
         * @param dataSize
         * @param numPivots
         * @return
         */
        public int[] selectPivots(Metric metric,
                List<? extends IndexObject> data, int first, int dataSize,
                int numPivots)
        {
            // compute the distance matrix
            DoubleMatrix2D matrix = GeDBIT.index.algorithms.LLE
                    .pairWiseDistance(metric, data);
            // compute MDS
            DoubleMatrix2D mat = GeDBIT.index.algorithms.MDS.runMDS(matrix,
                    numPivots);
            // select pivots from the MDS result
            return GeDBIT.index.algorithms.MDS.selectByCov(matrix,
                    mat.viewDice());
        }
    },
    COV
    {
        int count;

        /**
         * @param metric
         * @param data
         * @param numPivots
         * @return
         */
        public int[] selectPivots(Metric metric,
                List<? extends IndexObject> data, int numPivots)
        {
            if (count == 0 || data.size() > 10000)
            {
                count = 1;
                return FFT
                        .selectPivots(metric, data, 0, data.size(), numPivots);
            } else
                return selectPivots(metric, data, 0, data.size(), numPivots);
        }

        /**
         * @param metric
         * @param data
         * @param first
         * @param dataSize
         * @param numPivots
         * @return
         */
        public int[] selectPivots(Metric metric,
                List<? extends IndexObject> data, int first, int dataSize,
                int numPivots)
        {
            // compute the distance matrix
            DoubleMatrix2D matrix = GeDBIT.index.algorithms.LLE
                    .pairWiseDistance(metric, data);
            return GeDBIT.index.algorithms.Correlation
                    .runCor(matrix, numPivots);

        }
    },
    COR
    {
        int count;

        /**
         * @param metric
         * @param data
         * @param numPivots
         * @return
         */
        public int[] selectPivots(Metric metric,
                List<? extends IndexObject> data, int numPivots)
        {
            if (count == 0 || data.size() > 10000)
            {
                count = 1;
                return FFT
                        .selectPivots(metric, data, 0, data.size(), numPivots);
            } else
                return selectPivots(metric, data, 0, data.size(), numPivots);
        }

        /**
         * @param metric
         * @param data
         * @param first
         * @param dataSize
         * @param numPivots
         * @return
         */
        public int[] selectPivots(Metric metric,
                List<? extends IndexObject> data, int first, int dataSize,
                int numPivots)
        {
            // compute the distance matrix
            DoubleMatrix2D matrix = GeDBIT.index.algorithms.LLE
                    .pairWiseDistance(metric, data);
            return GeDBIT.index.algorithms.Covariance.runCov(matrix, numPivots);
        }
    };
    public static int fftopt = 0;

}