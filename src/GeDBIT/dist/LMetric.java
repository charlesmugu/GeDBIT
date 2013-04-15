/**
 * edu.utexas.GeDBIT.dist.LMetric 2006.06.16 
 * 
 * Copyright Information: 
 * 
 * Change Log: 2006.06.16: Modified from jdb 1.0, by Rui Mao
 */

package GeDBIT.dist;

import GeDBIT.type.DoubleVector;
import GeDBIT.type.IndexObject;

/**
 * This class computes the L family distance function on two vectors. Given two vectors
 * x(x1,x2,...,xk), y(y1,y2,...,yk), the distance between x,y: Ls(x,y) = sum( |xi-yi|^s )^(1/s), i=
 * 1,..,k where s=1,2,...,infinity. For infinite distance, L(x,y) = max( |xi-yi| ), i = 1,...,k. L1
 * is called Manhattan distance, L2 is called Euclidean distance. In this implementation, L0 is used
 * to represent L infinity.
 * 
 * @author Rui Mao, Wenguo Liu, Willard
 * @version 2005.10.31
 */
public class LMetric implements Metric {
    /**
     * 
     */
    private static final long   serialVersionUID        = -4658067077491099474L;

    /**
     * L1 distance (Manhattan distance) metric for two non-null double arrays of the same length.
     * Given two vectors x(x1,x2,...,xk), y(y1,y2,...,yk), the distance between x,y: L1(x,y) =
     * sum(|xi-yi| ), i= 1,..,k
     */
    public final static LMetric ManhattanDistanceMetric = new LMetric(1);

    /**
     * L2 distance (Euclidean distance) metric for two non-null double arrays of the same length.
     * Given two vectors x(x1,x2,...,xk), y(y1,y2,...,yk), the distance between x,y: L2(x,y) =
     * sum(|xi-yi|^2 )^(1/2), i= 1,..,k
     */
    public final static LMetric EuclideanDistanceMetric = new LMetric(2);

    /**
     * L infinity distance metric for two non-null double arrays of the same length. Given two
     * vectors x(x1,x2,...,xk), y(y1,y2,...,yk), the distance between x,y: L infinity (x,y) = max(
     * |xi-yi| ), i= 1,..,k
     */
    public final static LMetric LInfinityDistanceMetric = new LMetric(0);

    /** Dimension of the metric */
    private int                 dim;

    /**
     * Constructor. Takes the dimension of the distance function as an argument. (the s, but not the
     * dimension of the vectors to compute distance) Use 0 to represent infinity.
     * 
     * @param dim
     *        dimension of the distance function
     */
    public LMetric(int dim) {
        if (dim < 0)
            throw new IllegalArgumentException("dimension of LMetric is negative:" + dim);

        this.dim = dim;
    }

    /**
     * Computes the distance between two objects. The two objects should be two double arrays or
     * {@link DoubleVector}s of the same length.
     * 
     * @param o1
     *        the first {@link IndexObject} to compute distance on
     * @param o2
     *        the second {@link IndexObject} to compute distance on
     * @return the distance between the two objects
     */
    public double getDistance(IndexObject o1, IndexObject o2) {
        if (o1 instanceof DoubleVector && o2 instanceof DoubleVector)
            return getDistance((DoubleVector) o1, (DoubleVector) o2);
        else
            throw new IllegalArgumentException("LMetric cannot compute distance on "
                    + o1.getClass() + " and " + o2.getClass());
    }

    public double getDistance(DoubleVector dv1, DoubleVector dv2) {
        return getDistance(dv1.getData(), dv2.getData());
    }

    /**
     * Computes the distance between two double arrays with the same dimension.
     * 
     * @param a1
     *        the first double array
     * @param a2
     *        the second double array
     * @return the distance between the two arrays
     */
    public double getDistance(double[] a1, double[] a2) {
        // check arguments
        if (a1 == null)
            throw new IllegalArgumentException(
                    "the first argument is null calling getDistance() of LMetric");

        if (a2 == null)
            throw new IllegalArgumentException(
                    "the second argument is null calling getDistance() of LMetric");

        if (a1.length != a2.length)
            throw new IllegalArgumentException("the two arraies are of different lengths ("
                    + a1.length + ", " + a2.length +") calling getDistance() of LMetric");

        final int length = a1.length;
        double distance = 0;

        // infinite distance
        if (dim == 0) {
            for (int i = 0; i < length; i++)
                distance = Math.max(distance, Math.abs(a1[i] - a2[i]));
        }

        // else finite distance
        else {
            for (int i = 0; i < length; i++)
                distance += Math.pow(Math.abs(a1[i] - a2[i]), dim);

            distance = Math.pow(distance, 1 / (double) dim);
        }

        return distance;
    }

    /**
     * @return the dimension of the metric
     */
    public int getDimension() {
        return dim;
    }

    /**
     * main method, for test, 3 arguments argument 1: dimension of the metric argument 2,3: the two
     * vectors, dimension by dimension, seperated by comma, no space.
     */
    public static void main(String args[]) {
        if (args.length != 3) {
            System.out.println("3 arguments: argument 1: dimension of the metric, argument 2,3: "
                    + "the two vectors, dimension by dimension, seperated by comma, no space.");
            return;
        }

        final int dim = Integer.parseInt(args[0]);
        String[] a1String = args[1].split(",");
        String[] a2String = args[2].split(",");
        double[] a1 = new double[a1String.length];
        for (int i = 0; i < a1.length; i++)
            a1[i] = Double.parseDouble(a1String[i]);

        double[] a2 = new double[a2String.length];
        for (int i = 0; i < a2.length; i++)
            a2[i] = Double.parseDouble(a2String[i]);

        System.out.println("distance = " + (new LMetric(dim)).getDistance(a1, a2));
    }

}
