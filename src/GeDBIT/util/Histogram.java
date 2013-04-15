/**
 * GeDBIT.util.Histogram 2006.05.31
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.05.31: Copied from original jdb package, by Rui Mao
 */

package GeDBIT.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.TreeMap;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.math.BigInteger;

/**
 * Given values, 1-d or 2-d, count the histogram
 * 
 * @author Rui Mao
 * @version 2006.05.31
 */
public class Histogram {
    static final double Delta = 0.0000000000001;

    public static class BinInfo {
        final double center; // center value of the bin
        double       lower, upper; // lower and upper bounds of the bin
        int          size;        // number of points in the bin.
        final double width;       // left inclusive, right exclusive

        public BinInfo(double center, double width) {
            if (width <= 0)
                throw new IllegalArgumentException("Bin width must be positive!");

            this.center = center;
            this.width = width;
            this.lower = Double.POSITIVE_INFINITY;
            this.upper = Double.NEGATIVE_INFINITY;
            this.size = 0;
        }

        public BinInfo addPoint(double value) {
            if ((value - (center - width / 2) < -Delta) || (value - (center + width / 2) >= Delta))
                throw new IllegalArgumentException("value not in this bin!");

            if (lower > value)
                lower = value;
            if (upper < value)
                upper = value;
            size++;
            return this;
        }

        public int size() {
            return size;
        }

        public double lower() {
            return lower;
        }

        public double upper() {
            return upper;
        }

        public boolean cover(double value) {
            if ((value < center - width / 2) || (value >= center + width / 2))
                return false;
            return true;
        }
    }

    /**
     * Computes a one-dimensional histogram
     * 
     * @param start
     *        the starting boundary of bins, bins can be left to this starting point.
     * @param width
     *        width of bins, must be positive
     * @param dist
     *        the array of distances to compute histogram
     * @param first
     *        inclusive
     * @param last
     *        exclusive
     * @return a List of BinInfo, non-empty, non-overlaping. sorted asendingly by the center value.
     */
    public static ArrayList<BinInfo> completeOneDHistogram(double start, double width,
            double[] dist, int first, int last) {
        TreeMap<Integer, BinInfo> map = new TreeMap<Integer, BinInfo>();
        for (int i = first; i < last; i++) {
            double temp = (dist[i] - start) / width;
            int offset = (Math.abs(temp - Math.ceil(temp)) <= Delta) ? (int) Math.ceil(temp)
                    : (int) Math.floor(temp);
            if (map.containsKey(offset)) {
                map.put(offset, map.get(offset).addPoint(dist[i]));
            } else
                map.put(offset, (new BinInfo(start + (offset + 0.5) * width, width))
                        .addPoint(dist[i]));
        }

        ArrayList<BinInfo> result = new ArrayList<BinInfo>(map.size());
        result.addAll(map.values());
        return result;
    }

    /**
     * Compute one-dimensional histogram
     * 
     * @param start
     *        the starting boundary of bins
     * @param width
     *        width of bins
     * @param dist
     *        the array of distances to compute histogram
     * @return a 2-d double array of 2 rows. the first row is the upper bound of each bin, the
     *         second row is the corresponding bin size
     */
    public static double[][] oneDHistogram(double start, double width, double[] dist) {
        TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
        for (int i = 0; i < dist.length; i++) {
            int offset = (int) Math.floor((dist[i] - start) / width);
            if (map.containsKey(offset)) {
                map.put(offset, map.get(offset) + 1);
            } else
                map.put(offset, 1);
        }

        int first = map.firstKey();
        int last = map.lastKey();
        double[][] histogram = new double[2][last - first + 1];
        for (int i = 0; i < histogram[0].length; i++) {
            histogram[0][i] = start + width * (i + first + 1);
            histogram[1][i] = 0;
        }

        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            histogram[1][entry.getKey() - first] = entry.getValue();
        }

        return histogram;

    }

    /**
     * Given the 2-d (x-y) values, ranges and number of bins, out put the (non-zero) number of
     * occurances of each 2-d value. A large 2-d matrix will be used internally, therefore, might
     * not work if numbers of bins are too large.
     * 
     * @param xMin
     *        minimum value of x
     * @param xMax
     *        maximum value of x
     * @param xSize
     *        number of bins for x, or more accurately, how many parts the x value range will be
     *        divided into
     * @param xValue
     *        values of x
     * @param yMin
     *        minimum value of y
     * @param yMax
     *        maximum value of y
     * @param ySize
     *        number of bins for y, or more accurately, how many parts the y value range will be
     *        divided into
     * @param yValue
     *        values of y
     * @return the occurances //3-d vector, first row are x-values, second y-values, third row
     *         consists of occurances
     */
    public static int[][] TwoDHist(double xMin, double xMax, int xSize, double[] xValue,
            double yMin, double yMax, int ySize, double[] yValue) {
        // check argument
        if (xValue.length != yValue.length)
            throw new IllegalArgumentException("x, y values of different length!");

        final double xBinWidth = (xMax - xMin) / xSize;
        final double yBinWidth = (yMax - yMin) / ySize;

        int[][] counter = new int[xSize + 1][ySize + 1]; // allocate 1 more row and column to
                                                            // allow the maximum value
        for (int i = 0; i < xSize + 1; i++)
            for (int j = 0; j < ySize + 1; j++)
                counter[i][j] = 0;

        // count the occurances
        for (int i = 0; i < xValue.length; i++)
            counter[(int) ((xValue[i] - xMin) / xBinWidth)][(int) ((yValue[i] - yMin) / yBinWidth)]++;

        // count number of non-zero occurance
        /*
         * int length =0; for (int i=0; i< xSize+1; i++) for (int j=0; j<ySize+1; j++) if
         * (counter[i][j] > 0) length ++; //out put double [][] result = new double [3][length];
         * length =0; for (int i=0; i< xSize; i++) for (int j=0; j<ySize; j++) if (counter[i][j]
         * >0) { result[0][length] = xMin +i*xBinWidth; result[1][length] = yMin +j*yBinWidth;
         * result[2][length] = counter[i][j]; length ++; }
         */

        return counter;
    }

    /**
     * Reads a file consisting of coordiantes of points in pivot-space, i.e. distances to each pivot.
     * the file should be a text file. The first part, separted by whitespace, of the first line
     * should be a number, the number of pivots. then each following line is the description of a
     * pivot, in the order. then an empty line. then, the first part of next line should be a
     * number, number of points then each following line should be the coordinates of a point,
     * separted by comma. in the command line, the first parameter should be the file name, then a
     * series of numbers or chars, x0, x1, ..., xn, corresponding to dimensions d0, d1, ..., dn,
     * less number of number/chars can be provided, they will start at dim 0, dims not specified
     * will be ignored. if xi is '-', then dimension i is ignored, if xi is '.', then the histogram
     * on dimension i will be computed, if xi is a number, then the value on dimension i should be
     * fixed as that number, other values are ignored. min, max values of each dimension to compute
     * histogram will be reported. if only one dimension to compute histogram, the histogram will be
     * out put in one line, if two, then a matrix, if more, then each non-zero tuple will be output.
     * the out put is to the screen.
     * 
     * @param args
     *        the first should be the file name, then a series of numbers or chars, x0, x1, ..., xn,
     *        corresponding to dimensions d0, d1, ..., dn.
     */
    @SuppressWarnings("rawtypes")
    public static void pivotSpaceHistogram(String[] args) throws Exception {
        // read file
        BufferedReader fileReader = new BufferedReader(new FileReader(args[0]));

        // read the pivots
        String line = fileReader.readLine().trim();
        String[] lineSegment = line.split("[ \t\n\f\r]");
        final int pivotNumber = Integer.parseInt(lineSegment[0]);

        // read all the pivots and the empty line following them
        String[] pivot = new String[pivotNumber];
        for (int i = 0; i < pivotNumber; i++)
            pivot[i] = fileReader.readLine();

        fileReader.readLine(); // empty line

        // read dataset size
        line = fileReader.readLine().trim();
        lineSegment = line.split("[ \t\n\f\r]");
        final int dataSize = Integer.parseInt(lineSegment[0]);

        // parse the argument, set fixed values, and dims to compute histogram
        int[] fixedValue = new int[args.length - 1];
        boolean[] isFixed = new boolean[pivotNumber];
        boolean[] toCompute = new boolean[pivotNumber];
        for (int i = 0; i < pivotNumber; i++) {
            isFixed[i] = false;
            toCompute[i] = false;
        }
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("."))
                toCompute[i - 1] = true;
            else if (!args[i].equals("-")) {
                isFixed[i - 1] = true;
                fixedValue[i - 1] = Integer.parseInt(args[i]);
            }
        }

        // read each line of coordinates and compute the histogram
        TreeMap<String, BigInteger> map = new TreeMap<String, BigInteger>();
        String key = new String("");
        StringBuffer keyBuffer = null;

        boolean valid = false; // whether current line is consistent with the fixed values
        // System.out.println(pivotNumber + " " + dataSize);
        for (int j = 0; j < dataSize; j++) {
            line = fileReader.readLine().trim();
            lineSegment = line.split("[ \t\n\f\r]*,[ \t\n\f\r]*");

            // check consistency
            valid = true;
            for (int i = 0; i < pivotNumber; i++)
                if (isFixed[i] && ((int) Double.parseDouble(lineSegment[i]) != fixedValue[i])) {
                    valid = false;
                    break;
                }

            if (!valid)
                continue;

            // valid, continue to compute histogram
            keyBuffer = new StringBuffer();
            for (int i = 0; i < pivotNumber; i++)
                if (toCompute[i])
                    keyBuffer.append((char) (int) Double.parseDouble(lineSegment[i]));

            key = new String(keyBuffer);
            if (map.containsKey(key))
                map.put(key, ((BigInteger) map.get(key)).add(BigInteger.ONE));
            else
                map.put(key, new BigInteger("1"));
        }

        // finish reading file, output
        fileReader.close();

        System.out.print("#Fixed pivot-distances: [ ");
        for (int i = 0; i < pivotNumber; i++)
            if (isFixed[i])
                System.out.print("pivot " + pivot[i] + " = " + fixedValue[i] + ", ");
        System.out.print(" ],     ");

        // find the min, max value
        int histDim = 0; // number of dimensions to compute histogram
        for (int i = 0; i < pivotNumber; i++)
            if (toCompute[i])
                histDim++;
        int[] mapping = new int[histDim]; // mapping[i] the dim number of the ith dim to compute
                                            // histogram
        int j = 0;
        for (int i = 0; i < pivotNumber; i++)
            if (toCompute[i]) {
                mapping[j] = i;
                j++;
            }

        int[] min = new int[histDim];
        int[] max = new int[histDim];
        for (int i = 0; i < histDim; i++) {
            min[i] = Integer.MAX_VALUE;
            max[i] = 0;
        }

        Iterator p = map.entrySet().iterator();
        Map.Entry entry = null;
        double var = 0; // variance
        double maximum = 0; // maximum value in histogram
        double sum = 0; // total number of points in histogram
        while (p.hasNext()) {
            entry = (Map.Entry) p.next();
            key = (String) entry.getKey();
            for (int i = 0; i < histDim; i++) {
                if ((int) key.charAt(i) < min[i])
                    min[i] = (int) key.charAt(i);
                if ((int) key.charAt(i) > max[i])
                    max[i] = (int) key.charAt(i);
            }
            double temp = ((BigInteger) entry.getValue()).intValue();
            sum += temp;
            var += temp * temp;
            if (maximum < temp)
                maximum = temp;
        }

        System.out.println("map size  = " + map.size() + ",   dataset size = " + sum
                + ",  pivot number = " + pivotNumber);
        // var /= map.size();

        System.out.print("#[min, max] values of each dim of histogram: ");
        for (int i = 0; i < histDim; i++)
            System.out
                    .print("pivot " + pivot[mapping[i]] + ": [" + min[i] + ", " + max[i] + " ], ");
        System.out.println();

        // out put the histogram
        int[][] histogram2D = null;
        int[] histogram1D = null;
        if (histDim == 2) {
            if (((max[0] - min[0] + 1) > 0) && ((max[1] - min[1] + 1) > 0)) {
                histogram2D = new int[max[0] - min[0] + 1][max[1] - min[1] + 1];
                for (int i = 0; i < max[0] - min[0] + 1; i++)
                    for (j = 0; j < max[1] - min[1] + 1; j++)
                        histogram2D[i][j] = 0;
                System.out.println("#Histogram: "
                        + " std. dev. = "
                        + Math.sqrt(var / (histogram2D.length * histogram2D[0].length)
                                - Math.pow(sum / (histogram2D.length * histogram2D[0].length), 2))
                        + ", std. dev. of non-zero buckets = "
                        + Math.sqrt(var / map.size() - Math.pow(sum / map.size(), 2))
                        + ", maximum of histogram:" + maximum);
            }
        } else if (histDim == 1) {
            if ((max[0] - min[0] + 1) > 0) {
                histogram1D = new int[max[0] - min[0] + 1];
                for (int i = 0; i < max[0] - min[0] + 1; i++)
                    histogram1D[i] = 0;
                System.out.println("#Histogram: "
                        + " std. dev. = "
                        + Math.sqrt(var / histogram1D.length
                                - Math.pow(sum / histogram1D.length, 2))
                        + ", std. dev. of non-zero buckets = "
                        + Math.sqrt(var / map.size() - Math.pow(sum / map.size(), 2))
                        + ", maximum of histogram:" + maximum);
            }
        } else
            System.out.println("#Histogram: " + " std. dev. of non-zero buckets = "
                    + Math.sqrt(var / map.size() - Math.pow(sum / map.size(), 2))
                    + ", maximum of histogram:" + maximum);

        p = map.entrySet().iterator();
        while (p.hasNext()) {
            entry = (Map.Entry) p.next();
            key = (String) entry.getKey();
            if (histDim == 1) {
                histogram1D[(int) key.charAt(0) - min[0]] = ((BigInteger) entry.getValue())
                        .intValue();
            } else if (histDim == 2) {
                histogram2D[(int) key.charAt(0) - min[0]][(int) key.charAt(1) - min[1]] = ((BigInteger) entry
                        .getValue()).intValue();
            } else {
                System.out.print("(");
                for (int i = 0; i < histDim; i++)
                    System.out.print((int) key.charAt(i) + ", ");
                System.out.println("): " + entry.getValue());
            }
        }

        // output the 2-d or 1-d histogram
        if (histDim == 2) {
            if (histogram2D != null) {
                for (int i = 0; i < histogram2D.length; i++) {
                    // System.out.print("#, ");
                    for (j = 0; j < histogram2D[0].length; j++)
                        System.out.print(histogram2D[i][j] + ",\t");
                    System.out.println();
                }

                /*
                 * System.out.println("# for gnuplot"); if ( (min[0] > 0 ) || (min[1] >0) )
                 * System.out.println("# warning! min is not 0!!"); for (int i=0; i<histogram2D.length;
                 * i++) { for ( j=0; j< histogram2D[0].length; j++) if (histogram2D[i][j] >0)
                 * System.out.println( (i +min[0]) + " " + (j+ min[1]) + " " + histogram2D[i][j]); }
                 */

            }
        } else if (histDim == 1) {
            if (histogram1D != null) {
                // System.out.print("#, ");
                for (int i = 0; i < histogram1D.length; i++)
                    System.out.print((i + min[0]) + ": " + histogram1D[i] + ", ");
                System.out.println();

                /*
                 * System.out.println("# for gnuplot"); if ( min[0] > 0 ) System.out.println("#
                 * warning! min is not 0!!"); for (int i=0; i<histogram1D.length; i++)
                 * System.out.println( (i + min[0]) + " " + histogram1D[i] );
                 */
            }
        }

        System.out.println();

    }

    /**
     * Read a file consisting of coordiantes of points in pivot-space, i.e. distances to each pivot.
     * The file format is the same as those required by other methods. the file should be a text
     * file. The first part, separted by whitespace, of the first line should be a number, the
     * number of pivots. then each following line is the description of a pivot, in the order. then
     * an empty line. then, the first part of next line should be a number, number of points then
     * each following line should be the coordinates of a point, separted by comma. in the command
     * line, the first parameter should be the file name, the second and third should be sequential
     * id of dimensions to compute histogram, start from 0. the output is the 2-d matrix histogram
     * of the two designated dimensions the out put is to the screen.
     * 
     * @param args
     *        the first should be the file name, the second and third should be sequential id of
     *        dimensions to compute histogram, start from 0.
     */
    public static void pivotSpaceTwoDHistogram(String[] args) throws Exception {
        // read file
        final int xDim = Integer.parseInt(args[1]);
        final int yDim = Integer.parseInt(args[2]);

        @SuppressWarnings("resource")
        BufferedReader fileReader = new BufferedReader(new FileReader(args[0]));

        // read the pivots
        String line = fileReader.readLine().trim();
        String[] lineSegment = line.split("[ \t\n\f\r]");
        final int pivotNumber = Integer.parseInt(lineSegment[0]);

        // skip all the pivots and the empty line following them
        for (int i = 0; i < pivotNumber + 1; i++)
            fileReader.readLine();

        // read dataset size
        line = fileReader.readLine().trim();
        lineSegment = line.split("[ \t\n\f\r]");
        final int dataSize = Integer.parseInt(lineSegment[0]);

        double[] xValue = new double[dataSize];
        double[] yValue = new double[dataSize];

        for (int j = 0; j < dataSize; j++) {
            line = fileReader.readLine().trim();
            lineSegment = line.split("[ \t\n\f\r]*,[ \t\n\f\r]*");
            xValue[j] = Double.parseDouble(lineSegment[xDim]);
            yValue[j] = Double.parseDouble(lineSegment[yDim]);
        }

        int[][] histogram = TwoDHist(0, 40, 40, xValue, 0, 40, 40, yValue);
        System.out.println("2-d histogram in pivot space, p0 and p2");
        for (int i = 0; i < histogram.length; i++) {
            for (int j = 0; j < histogram[i].length; j++)
                System.out.print(histogram[i][j] + ", ");
            System.out.println();
        }
        System.out.println();
    }

    /**
     * Read a file consisting of coordiantes of points in pivot-space, i.e. distances to each pivot.
     * This distance values are real number, double/float the file should be a text file. The first
     * part, separted by whitespace, of the first line should be a number, the number of pivots.
     * then each following line is the description of a pivot, in the order. then an empty line.
     * then, the first part of next line should be a number, number of points then each following
     * line should be the coordinates of a point, separted by comma. in the command line, the first
     * parameter should be the file name, then a series of sets of parameters for each dimension
     * sequentially, if not enough sets of parameters are provided, later dimensions are ignored. a
     * set of parameters for a dimension can be of one of the 3 formats: format 1: two double values
     * separated by white space, means this dimension is fixed, in the range provided by the two
     * doubles, first should not be larger than the second, left end inclusive, right end exclusive.
     * format 2: "-", indicating this dimension is ignored format 3: "." followed by a number,
     * indicating that this dimension is subject to histogram computation, and the number is the
     * width of the bin. min, max values of each dimension to compute histogram will be reported. if
     * only one dimension to compute histogram, the histogram will be out put in one line, if two,
     * then a matrix, if more, then each non-zero tuple will be output. the out put is to the
     * screen.
     * 
     * @param args
     *        the first should be the file name, then width of bin. then a series of numbers or
     *        chars, x0, x1, ..., xn, corresponding to dimensions d0, d1, ..., dn.
     */
    public static String continuousPivotSpaceHistogram(String[] args) throws Exception {
        return continuousPivotSpaceHistogram(args, true);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static String continuousPivotSpaceHistogram(String[] args, boolean print)
            throws Exception {
        // read file
        BufferedReader fileReader = new BufferedReader(new FileReader(args[0]));

        // read the pivots
        String line = fileReader.readLine().trim();
        String[] lineSegment = line.split("[ \t\n\f\r]");
        final int pivotNumber = Integer.parseInt(lineSegment[0]);

        // read all the pivots and the empty line following them
        String[] pivot = new String[pivotNumber];
        for (int i = 0; i < pivotNumber; i++)
            pivot[i] = fileReader.readLine();

        fileReader.readLine(); // empty line

        // read dataset size
        line = fileReader.readLine().trim();
        lineSegment = line.split("[ \t\n\f\r]");
        final int dataSize = Integer.parseInt(lineSegment[0]);

        // process parameters
        double[] lowerBound = new double[pivotNumber]; // for fixed dims, stores lower(inclusive) /
                                                        // upper(exclusive) bound of the bin,
        double[] upperBound = new double[pivotNumber]; // for dims to computes, stores min / max
                                                        // value.
        double[] width = new double[pivotNumber];
        boolean[] isFixed = new boolean[pivotNumber];
        boolean[] toCompute = new boolean[pivotNumber];

        for (int i = 0; i < pivotNumber; i++) {
            lowerBound[i] = Double.POSITIVE_INFINITY;
            upperBound[i] = Double.NEGATIVE_INFINITY;
            isFixed[i] = false;
            toCompute[i] = false;
        }

        int parameter = 1;
        int pivotInvolved = 0;
        while ((parameter < args.length) && (pivotInvolved < pivotNumber)) {
            if (args[parameter].equalsIgnoreCase(".")) // this is a dimension to compute histogram
            {
                toCompute[pivotInvolved] = true;
                parameter++;
                width[pivotInvolved] = Double.parseDouble(args[parameter]);
            } else if (!args[parameter].equalsIgnoreCase("-")) {
                isFixed[pivotInvolved] = true;
                lowerBound[pivotInvolved] = Double.parseDouble(args[parameter]);
                parameter++;
                upperBound[pivotInvolved] = Double.parseDouble(args[parameter]);
            }

            pivotInvolved++;
            parameter++;

        }

        int fixedDim = 0; // number of fixed dims
        int histDim = 0; // number of dims to compute histogram
        for (int i = 0; i < pivotNumber; i++) {
            if (isFixed[i])
                fixedDim++;
            if (toCompute[i])
                histDim++;
        }

        int[] fixedMapping = new int[fixedDim]; // fixedMapping[i] is the dim id of ith dim that is
                                                // fixed
        int[] histMapping = new int[histDim]; // histMapping[i] is the dim id of ith dim to
                                                // compute histogram
        fixedDim = 0; // serve as loop variable, counter
        histDim = 0;
        for (int i = 0; i < pivotNumber; i++) {
            if (isFixed[i]) {
                fixedMapping[fixedDim] = i;
                fixedDim++;
            }

            if (toCompute[i]) {
                histMapping[histDim] = i;
                histDim++;
            }
        }

        // read (only the necessary part of valid) data into arrays, get lower/upper bounds of the
        // dimensions to compute histogram
        double[][] distance = new double[histDim][dataSize];
        int counter = 0; // the counter on valid points
        boolean valid = false; // whether current point is consistent with the fixed values
        double temp = 0;
        for (int i = 0; i < dataSize; i++) {
            line = fileReader.readLine().trim();
            lineSegment = line.split("[ \t\n\f\r]*,[ \t\n\f\r]*");

            // check consistency
            valid = true;
            for (int j = 0; j < fixedDim; j++) {
                temp = Double.parseDouble(lineSegment[fixedMapping[j]]);
                if ((temp < lowerBound[fixedMapping[j]]) || (temp >= upperBound[fixedMapping[j]])) {
                    valid = false;
                    break;
                }
            }

            if (!valid)
                continue;

            for (int j = 0; j < histDim; j++) {
                distance[j][counter] = Double.parseDouble(lineSegment[histMapping[j]]);
                if (distance[j][counter] < lowerBound[histMapping[j]])
                    lowerBound[histMapping[j]] = distance[j][counter];

                if (distance[j][counter] > upperBound[histMapping[j]])
                    upperBound[histMapping[j]] = distance[j][counter];
            }

            counter++;
        }
        fileReader.close();

        // scan data, compute histogram
        TreeMap<String, BigInteger> map = new TreeMap<String, BigInteger>();
        TreeMap<Integer, BigInteger>[] histMap = new TreeMap[histDim]; // a map for each dimension
                                                                        // to compute histogram.
        for (int i = 0; i < histDim; i++)
            histMap[i] = new TreeMap<Integer, BigInteger>();

        String key = new String("");
        StringBuffer keyBuffer = null;
        int offset = 0; // offset in each dimension
        Integer offsetKey = null;

        for (int i = 0; i < counter; i++) {
            keyBuffer = new StringBuffer();
            for (int j = 0; j < histDim; j++) {
                offset = (int) ((distance[j][i] - lowerBound[histMapping[j]]) / width[histMapping[j]]);
                keyBuffer.append((char) offset);

                offsetKey = new Integer(offset);
                if (histMap[j].containsKey(offsetKey))
                    histMap[j].put(offsetKey, ((BigInteger) histMap[j].get(offsetKey))
                            .add(BigInteger.ONE));
                else
                    histMap[j].put(offsetKey, new BigInteger("1"));

            }

            key = new String(keyBuffer);
            if (map.containsKey(key))
                map.put(key, ((BigInteger) map.get(key)).add(BigInteger.ONE));
            else
                map.put(key, new BigInteger("1"));
        }

        // compute variance of bins
        Iterator p = map.entrySet().iterator();
        Map.Entry entry = null;
        double var = 0; // variance
        int max = 0; // max bin size
        double sum = 0; // total number of points in histogram
        double entropy = 0;
        while (p.hasNext()) {
            entry = (Map.Entry) p.next();
            key = (String) entry.getKey();

            temp = ((BigInteger) entry.getValue()).intValue();
            if (max < temp)
                max = (int) temp;

            sum += temp;
            var += temp * temp;
            entropy += temp * Math.log(temp);
        }

        // compute variances of distances, and entropy, of each dimension
        double[] distVar = new double[histDim];
        double[] distSum = new double[histDim];
        double[] histEntropy = new double[histDim];
        for (int i = 0; i < histDim; i++) {
            distVar[i] = 0;
            distSum[i] = 0;
            histEntropy[i] = 0;
        }

        for (int i = 0; i < counter; i++)
            for (int j = 0; j < histDim; j++) {
                distSum[j] += distance[j][i];
                distVar[j] += distance[j][i] * distance[j][i];
            }

        // compute entropy for each dim

        for (int i = 0; i < histDim; i++) {
            distSum[i] /= counter;
            distVar[i] = Math.sqrt(distVar[i] / counter - Math.pow(distSum[i], 2));

            p = histMap[i].entrySet().iterator();
            while (p.hasNext()) {
                entry = (Map.Entry) p.next();
                temp = ((BigInteger) entry.getValue()).intValue();
                histEntropy[i] += temp * Math.log(temp);
            }

            histEntropy[i] = Math.log(counter) - histEntropy[i] / counter;
        }

        // output: statistics
        StringBuffer result = new StringBuffer();
        StringBuffer dimResult = new StringBuffer(); // results for each dim
        if (print) {
            System.out.println("Fixed pivot-distances: ");
            for (int i = 0; i < fixedDim; i++)
                System.out.println("pivot " + pivot[fixedMapping[i]] + " = ("
                        + lowerBound[fixedMapping[i]] + " ~ " + upperBound[fixedMapping[i]] + ") ");

            System.out
                    .println("[min, max, average, std.dev, entropy]: width values of each dimension to compute histogram: ");
        }

        for (int i = 0; i < histDim; i++) {
            if (print)
                System.out.println("pivot " + pivot[histMapping[i]] + ": ["
                        + lowerBound[histMapping[i]] + ", " + distSum[i] + ", "
                        + upperBound[histMapping[i]] + ", " + distVar[i] + ", " + histEntropy[i]
                        + " ]:  " + width[histMapping[i]]);

            dimResult.append(pivot[histMapping[i]].split(":")[1].trim() + " [ "
                    + (upperBound[histMapping[i]] - lowerBound[histMapping[i]]) + " : "
                    + lowerBound[histMapping[i]] + " " + distSum[i] + " "
                    + upperBound[histMapping[i]] + " " + distVar[i] + " " + histEntropy[i] + " ] ");
        }

        // sum of all variance
        distVar[0] *= distVar[0];
        for (int i = 1; i < histDim; i++) {
            distVar[0] += distVar[i] * distVar[i];
            histEntropy[0] += histEntropy[i];
        }

        if (print) {
            System.out.println("Sum of variance, entropy, on all dimensions =" + distVar[0]
                    + ", e: " + histEntropy[0]);

            System.out.println("map size  = " + map.size() + ",   dataset size = " + (int) sum
                    + ",  pivot number = " + pivotNumber + ", max bin size = " + max);
        }
        result.append(map.size() + " " + max);

        int totalBin = 1;
        for (int i = 0; i < histDim; i++)
            totalBin *= (int) ((upperBound[histMapping[i]] - lowerBound[histMapping[i]]) / width[histMapping[i]]) + 1;

        if (print)
            System.out.println("Std. dev. among non-empty bins = "
                    + Math.sqrt(var / map.size() - Math.pow(sum / map.size(), 2))
                    + ", among all bins = "
                    + Math.sqrt(var / totalBin - Math.pow(sum / totalBin, 2)) + ", 2-raw moment = "
                    + (var / counter) / counter + ", entropy = "
                    + (Math.log(counter) - entropy / counter));

        result.append(" " + (Math.log(counter) - entropy / counter) + " " + histEntropy[0] + " "
                + (var / counter) / counter + " "
                + Math.sqrt(var / map.size() - Math.pow(sum / map.size(), 2)) + " "
                + Math.sqrt(var / totalBin - Math.pow(sum / totalBin, 2)) + " ");
        result.append(dimResult);

        if (!print)
            return result.toString();

        // out put: the histogram
        int[][] histogram2D = null;
        int[] histogram1D = null;
        if (histDim == 2) {
            if ((upperBound[histMapping[0]] >= lowerBound[histMapping[0]])
                    && (upperBound[histMapping[1]] >= lowerBound[histMapping[1]])) {
                histogram2D = new int[(int) ((upperBound[histMapping[0]] - lowerBound[histMapping[0]]) / width[histMapping[0]]) + 1][(int) ((upperBound[histMapping[1]] - lowerBound[histMapping[1]]) / width[histMapping[1]]) + 1];

                for (int i = 0; i < histogram2D.length; i++)
                    for (int j = 0; j < histogram2D[0].length; j++)
                        histogram2D[i][j] = 0;
            }
        } else if (histDim == 1) {
            if (upperBound[histMapping[0]] >= lowerBound[histMapping[0]]) {
                histogram1D = new int[(int) ((upperBound[histMapping[0]] - lowerBound[histMapping[0]]) / width[histMapping[0]]) + 1];

                for (int i = 0; i < histogram1D.length; i++)
                    histogram1D[i] = 0;
            }

        }

        p = map.entrySet().iterator();
        while (p.hasNext()) {
            entry = (Map.Entry) p.next();
            key = (String) entry.getKey();
            if (histDim == 1) {
                histogram1D[(int) key.charAt(0)] = ((BigInteger) entry.getValue()).intValue();
            } else if (histDim == 2) {
                histogram2D[(int) key.charAt(0)][(int) key.charAt(1)] = ((BigInteger) entry
                        .getValue()).intValue();
            } else {
                /*
                 * System.out.print( "("); for (int i=0; i< histDim; i++) System.out.print( (int)
                 * key.charAt(i) + ", "); System.out.println( "): " + entry.getValue());
                 */
            }
        }

        // output the 2-d or 1-d histogram
        if (histDim == 2) {
            if (histogram2D != null) {
                for (int i = 0; i < histogram2D.length; i++) {
                    for (int j = 0; j < histogram2D[0].length; j++)
                        System.out.print(histogram2D[i][j] + ",\t");
                    System.out.println();
                }

            }
        } else if (histDim == 1) {
            if (histogram1D != null) {
                for (int i = 0; i < histogram1D.length; i++)
                    System.out.print(i + ": " + histogram1D[i] + ", ");
                System.out.println();
            }
        }

        System.out.println();
        // System.out.println(result);
        return result.toString();
    }

    static void aminoacid(String[] args) throws Exception {
        String all = "acdefghiklmnpqrstvwy";
        String fileName = args[0];
        StringBuffer result = new StringBuffer();
        StringBuffer parameter = new StringBuffer();
        final int total = 20;

        for (int i = 0; i < total; i++) {
            for (int j = i + 1; j < total; j++) {
                parameter = new StringBuffer(fileName);
                for (int k = 0; k < i; k++)
                    parameter.append(" -");

                parameter.append(" . 1");

                for (int k = i + 1; k < j; k++)
                    parameter.append(" -");

                parameter.append(" . 1");

                result.append(all.charAt(i)).append(all.charAt(j)).append(
                        " : " + continuousPivotSpaceHistogram(parameter.toString().split(" "))
                                + "\n");

                System.out.println(result.toString());

                // System.out.println( all.charAt(i) + all.charAt(j) + " : " + parameter.toString()
                // );
            }
        }

    }

    public static double allPairs(String[] args) throws Exception {
        return allPairs(args, true);
    }

    public static double allPairs(String[] args, boolean print) throws Exception {
        String fileName = args[0];
        final int dim = Integer.parseInt(args[1]);
        final double binWidth = Double.parseDouble(args[2]);
        StringBuffer result = new StringBuffer();
        StringBuffer parameter = new StringBuffer();

        double max = 0, temp;
        String tempString;

        for (int i = 0; i < dim - 1; i++) {
            for (int j = i + 1; j < dim; j++) {
                parameter = new StringBuffer(fileName);
                for (int k = 0; k < i; k++)
                    parameter.append(" -");

                parameter.append(" . ").append(binWidth);

                for (int k = i + 1; k < j; k++)
                    parameter.append(" -");

                parameter.append(" . ").append(binWidth);

                tempString = continuousPivotSpaceHistogram(parameter.toString().split(" "), print);

                temp = Double.parseDouble(tempString.split(" ")[2]);
                max = (temp > max) ? temp : max;

                result.append(i).append(" " + j).append(" : " + tempString + "\n");

                if (print)
                    System.out.println(result.toString());

                // System.out.println( all.charAt(i) + all.charAt(j) + " : " + parameter.toString()
                // );
            }
        }
        if (!print)
            System.out.println(result.toString());

        return max;

    }

    static void aminoacid3(String[] args) throws Exception {
        String all = "acdefghiklmnpqrstvwy";
        String fileName = args[0];
        StringBuffer result = new StringBuffer();
        StringBuffer parameter = new StringBuffer();
        final int total = 20;

        for (int i = 0; i < total; i++) {
            for (int j = i + 1; j < total; j++) {
                for (int k = j + 1; k < total; k++) {
                    parameter = new StringBuffer(fileName);
                    for (int t = 0; t < i; t++)
                        parameter.append(" -");

                    parameter.append(" . 1");

                    for (int t = i + 1; t < j; t++)
                        parameter.append(" -");

                    parameter.append(" . 1");

                    for (int t = j + 1; t < k; t++)
                        parameter.append(" -");

                    parameter.append(" . 1");

                    result.append(all.charAt(i)).append(all.charAt(j)).append(all.charAt(k))
                            .append(
                                    " : "
                                            + continuousPivotSpaceHistogram(parameter.toString()
                                                    .split(" ")) + "\n");

                    System.out.println(result.toString());

                    // System.out.println( all.charAt(i) + all.charAt(j) + " : " +
                    // parameter.toString() );
                }
            }
        }

    }

    public static void main(String[] args) throws Exception {
        double[][] hist = oneDHistogram(3.1, 1, new double[] { 1, 2, 3, 4, 5, 12, 30 });
        for (int i = 0; i < hist[0].length; i++)
            System.out.print(hist[0][i] + ", ");
        System.out.println();
        for (int i = 0; i < hist[1].length; i++)
            System.out.print((int) hist[1][i] + ", ");
        System.out.println();

        // allPairs(args);
        // System.out.println( continuousPivotSpaceHistogram(args) );
        // aminoacid(args);
        // aminoacid3(args);
        // pivotSpaceTwoDHistogram(args);
        /*
         * char [] s = new char[5]; s[0] = (char) 0; s[1] = (char) 1; s[2] = (char) 2; s[3] = (char)
         * 95; s[4] = (char) 102; String ss = new String(s); for (int i=0; i< ss.length(); i++)
         * System.out.println( (int) ss.charAt(i) ); System.out.println(ss + ss.length());
         */
    }
}