/*$Id: MSMetric.java,v 1.1 2013/01/24 02:01:04 hpc Exp $*/

/* Smriti Ramakrishnan, 2004.03.21 
 * This computed modified cosine distance (elements of the vectors are equal - within a particular tolerance)
 * vectors are CCS form of binary vectors - stored in MSDataKeyObjects
 * this ideally should extend BinaryVectorMetric, but the getCosine implementation seems faster here. 
 * If this is wrong, just extend from it and calls remain the same, but need to add 
 * 1) DataVector.
 * 2) Change InnerProduct to use 'Modified' contains(..)
 * 3) Add min, max,step to constructor 
 * 4) Does a metric have to have a equals method ?
 *
 * Requires to implement java.io.Serializable - since index.Index needs to be serializable 
 */

/**
 * edu.utexas.GeDBIT.dist.MSMetric 2004.03.17
 *
 * Copyright Information:
 * 
 * Change Log:
 * 2004.05.31; Modified for performance, replaced ArrayLists with arrays, by Willard Briggs
 * 2004.08.02; Modified to implement MSDataConstants, by Smriti Ramakrishnan
 */

package GeDBIT.dist;

import GeDBIT.dist.Metric;
import GeDBIT.type.IndexObject;
import GeDBIT.type.Spectra;
import static GeDBIT.dist.MSMSConstants.MS_TOLERANCE;

/**
 * MSDataMetric implements the {@link Metric} interface.
 * 
 * @author Smriti Ramakrishnan
 * @version 2004.03.25
 **/

public class MSMetric implements Metric {
    static final long serialVersionUID = 179878801379011989L;

    public MSMetric(int min, int max, double step, double tol) {
	// super(min, max, step);
	this.min = min;
	this.max = max;
	this.step = step;
	this.tol = tol;
    }

    /**
     * default constructor - is a temporary solution providing default values
     * for min, max, step, tolerance
     * 
     */
    public MSMetric() {
	this.min = 0;
	this.max = 0;
	this.step = 0;
	this.tol = MS_TOLERANCE;
    }

    /**
     * @param v1
     *            the {@link Object} over which the keys are defined.
     * @param v2
     *            the other {@link Object} over which the keys are defined.
     */
    public double getDistance(IndexObject v1, IndexObject v2) {
	return getDistance((Spectra) v1, (Spectra) v2);
    }

    /**
     * @param v1
     *            the {@link MSKeyObject} over which the keys are defined.
     * @param v2
     *            the other {@link MSKeyObject} over which the keys are defined.
     */
    public double getDistance(Spectra v1, Spectra v2) {
	// now compute cosine distance
	double cos = getCosine(v1, v2);

	// from BinaryVectorMetric:getDistance()
	// if ( Math.abs(cos) > 1) {; Smriti

	if (Math.abs(Math.abs(cos) - 1) < COS_THRESHOLD) { // precision
	    if (cos > 0)
		cos = 1; // very similar
	    else
		cos = -1; // very unsimilar
	} else if (Math.abs(cos) > 1) {
	    System.out.println("COS_THRESHOLD = " + COS_THRESHOLD + ", cos = "
		    + cos);
	    System.out.println("got cosine > 1, cosine=" + cos + ", :"
		    + v1.toString() + ", v2:" + v2.toString() + "Quitting.");
	    System.exit(0);
	}
	// }
	/*
	 * System.out.println("MSTOL = " + MS_TOLERANCE);
	 * System.out.println("MSDKO-1 : " + v1);
	 * System.out.println("MSDKO-2 : " + v2); System.out.println("Dist = " +
	 * Math.acos(cos) + ",Cosine = " + cos); System.out.println();
	 * System.out.println();
	 */
	return Math.acos(cos);

	// smriti - 30 sep
	// return (1 - cos);
	/*
	 * //extended jaccard
	 * 
	 * double[] one = v1.getData(); double[] two = v2.getData();
	 * 
	 * double spc = getInnerProduct(one, two); double dist = 1 - (spc /
	 * (getMagnitude(one) + getMagnitude(two) - spc)); return dist;
	 */
	// getCosine(,v2));//weijia
    }

    private double getCosine(Spectra one, Spectra two) {
	double[] v1 = one.getData();
	double[] v2 = two.getData();

	return getInnerProduct(v1, v2) / (getMagnitude(v1) * getMagnitude(v2));

	// smriti - 30sep
	/*
	 * double numer = getInnerProduct(v1, v2) ; double denom =
	 * getMagnitude(v1) * getMagnitude(v2);
	 * 
	 * double denom = getMagnitude(v1) * getMagnitude(v1); double d2 =
	 * getMagnitude(v2) * getMagnitude(v2); denom = (denom + d2) / 2;
	 * 
	 * return numer/denom;
	 */
    }

    /**
     * This is the method that replaces calls to super.getInnerProduct() Compute
     * FuzzyInnerProduct withing a certain tolerance.
     */
    private int getInnerProduct(double[] v1, double[] v2) {
	int dist = 0;
	int i = 0, j = 0;
	double val1;
	double val2;

	// System.out.println("TOL = " + tol);
	while (i < v1.length && j < v2.length) {
	    val1 = v1[i];
	    val2 = v2[j];
	    // new version -- gets rid of Math.abs()
	    if (val1 <= val2) {
		if (val2 - val1 <= tol) {
		    dist++;
		    i++;
		    j++;
		} else {
		    i++;
		}
	    } else {
		if (val1 - val2 <= tol) {
		    dist++;
		    i++;
		    j++;
		} else {
		    j++;
		}
	    }

	    /*
	     * old code if (Math.abs(val1 - val2) <= tol) { dist++; i++; j++; }
	     * else { if (val1 < val2) i++; else j++; }
	     */
	    // debug
	    // System.out.println("i = " + i + ", j = " + j);
	}

	return dist;
    }

    /**
     * For experiment purpose only Method to compute inner product, cosine value
     * and distance of v2 {@link VectorData} objects by treating them as binary
     * vectors in a multidimensional space.The Value will be stored in a {@link
     * double[]} with the correspoding order.
     * 
     * @param - {@link VectorData}
     * @param v2
     *            - {@link vectorData}
     * @return {@link double []}
     **/
    /*
     * public double[] getAll(DataVector one, DataVector two) { double[] v1 =
     * one.getData(); double[] v2 = two.getData(); double[] result = new
     * double[3]; result[0] = getInnerProduct(v1, v2); result[1] = result[0] /
     * (getMagnitude(v1) * getMagnitude(v2)); result[2] = Math.(result[1]);
     * 
     * return result; }
     */

    /**
     * Computes the magnitude of the {@link VectorData} object by treating it as
     * a binary vector
     * 
     * @param {@link VectorData}
     **/
    private double getMagnitude(double[] d) {
	return Math.sqrt(d.length);
	// return d.length;
    }

    /**
     * Convert information of the instance into a {@link String} that contains
     * the number of dimension and meaning for each dimension
     **/
    /*
     * public String toString() { String result="incomplete implementation";
     * return result; }
     */

    // -- Changing this for mSQL serializability reqmt - dont need custom
    // functions here anyway
    // legacy
    int min, max; // min, max are mass ranges
    double step; // Same as Tolerance ?
    // end-legacy

    double tol;
    private final double COS_THRESHOLD = 0.00005;
}
