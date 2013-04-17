/**
 * edu.utexas.GeDBIT.dist.MSMetric 2004.03.17
 *
 * Copyright Information:
 * 
 * Change Log:
 * 2004.05.31; Modified for performance, replaced ArrayLists with arrays, by Willard
 * 2004.08.02; Modified to implement MSDataConstants, by Smriti Ramakrishnan
 */

package GeDBIT.dist;

import GeDBIT.dist.Metric;
import GeDBIT.type.DoubleVector;
import GeDBIT.type.IndexObject;
import GeDBIT.type.SpectraWithPrecursorMass;
import static GeDBIT.dist.MSMSConstants.*;

/**
 * MSMSMetric is an implementation of a fuzzy cosine distance metric for
 * comparing tandem spectra signatures. Elements of the vectors are equal,
 * within a given tolerance.
 * 
 * @author Smriti Ramakrishnan, Willard
 * @version 2004.11.29
 */
public class MSMSMetric implements Metric {
    public MSMSMetric(int min, int max, double step, double tol) {
	// super(min, max, step);
	this.min = min;
	this.max = max;
	this.step = step;
	this.tol = tol;

	mscosdist = 0.0;
	absMassDiff = 0.0;
	massDiffTerm = 0.0;
    }

    /**
     * default constructor provides default values for min, max, step, tolerance
     * min = 0; max = 0; step = 0; tol = MSMSConstants.MS_TOLERANCE
     */
    public MSMSMetric() {
	this.min = 0;
	this.max = 0;
	this.step = 0;
	this.tol = MS_TOLERANCE;

	mscosdist = 0.0;
	absMassDiff = 0.0;
	massDiffTerm = 0.0;
    }

    /**
     * @param v1
     *            the {@link IndexObject} over which the keys are defined.
     * @param v2
     *            the other {@link IndexObject} over which the keys are defined.
     */
    public double getDistance(IndexObject v1, IndexObject v2) {
	return getDistance((SpectraWithPrecursorMass) v1,
		(SpectraWithPrecursorMass) v2);
    }

    /**
     * @param v1
     *            the {@link SpectraWithPrecursorMass} over which the keys are
     *            defined.
     * @param v2
     *            the other {@link SpectraWithPrecursorMass} over which the keys
     *            are defined.
     */

    public double getDistance(SpectraWithPrecursorMass v1,
	    SpectraWithPrecursorMass v2) {
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
	 * System.out.println("MSTOL = " + tol); System.out.println("MSTOL = " +
	 * MS_PRECURSOR_TOLERANCE); System.out.println("MSDKO-1 : " + v1);
	 * System.out.println("MSDKO-2 : " + v2);
	 */

	// ********************************************
	mscosdist = Math.acos(cos);
	massDiffTerm = getAbsPrecursorMassDiff(v1, v2);
	double dist = massDiffTerm + mscosdist;
	// ********************************************

	/*
	 * System.out.println("K1 = " + v1); System.out.println("K2 = " + v2);
	 * System.out.println("mscosDist = " + mscosdist + ", Cosine = " + cos +
	 * ", MassDiff = " + massDiff); System.out.println("Total Dist = " +
	 * dist); System.out.println();
	 */
	return dist;

	// smriti - 30 sep
	// return (1 - cos);
	/*
	 * //extended jaccard double[] one = v1.getData(); double[] two =
	 * v2.getData(); double spc = getInnerProduct(one, two); double dist = 1
	 * - (spc / (getMagnitude(one) + getMagnitude(two) - spc)); return dist;
	 */
	// getCosine(,v2));//weijia
    }

    /**
     * Returns absolute difference between precursor masses - within a tolerance
     */
    private double getAbsPrecursorMassDiff(SpectraWithPrecursorMass v1,
	    SpectraWithPrecursorMass v2) {
	double m1 = v1.getPrecursorMass();
	double m2 = v2.getPrecursorMass();

	absMassDiff = Math.abs(m1 - m2);

	if (absMassDiff < COS_THRESHOLD)
	    absMassDiff = 0.0;

	if (absMassDiff <= MS_PRECURSOR_TOLERANCE)
	    return 0.0;
	else
	    return (absMassDiff);
    }

    /**
     * @param one
     * @param two
     * @return
     */
    private double getCosine(DoubleVector one, DoubleVector two) {
	double[] v1 = one.getData();
	double[] v2 = two.getData();

	return getInnerProduct(v1, v2) / (getMagnitude(v1) * getMagnitude(v2));

	// smriti - 30sep
	/*
	 * double numer = getInnerProduct(v1, v2) ; double denom =
	 * getMagnitude(v1) * getMagnitude(v2); double denom = getMagnitude(v1)
	 * * getMagnitude(v1); double d2 = getMagnitude(v2) * getMagnitude(v2);
	 * denom = (denom + d2) / 2; return numer/denom;
	 */
    }

    /**
     * Computes inner product within a certain tolerance-- "fuzzy" inner
     * product.
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
	// System.out.println("INNER PROD = " + dist);
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
     */
    /*
     * public double[] getAll(DataVector one, DataVector two) { double[] v1 =
     * one.getData(); double[] v2 = two.getData(); double[] result = new
     * double[3]; result[0] = getInnerProduct(v1, v2); result[1] = result[0] /
     * (getMagnitude(v1) * getMagnitude(v2)); result[2] = Math.(result[1]);
     * return result; }
     */

    /**
     * @param {@link VectorData}
     */

    /**
     * Computes the magnitude of the double array by treating it as a *sparse*
     * binary vector (store only 1's)
     * 
     * @param d
     * @return
     */
    private double getMagnitude(double[] d) {
	return Math.sqrt(d.length);
	// return d.length;
    }

    /**
     * Convert information of the instance into a {@link String} that contains
     * the number of dimension and meaning for each dimension
     */
    public String printDistance(SpectraWithPrecursorMass k1,
	    SpectraWithPrecursorMass k2) {
	// set values of mscosdist and massDiff
	getDistance(k1, k2);
	java.text.DecimalFormat frm = new java.text.DecimalFormat(
		"####.########");

	StringBuffer outStr = new StringBuffer(20);
	// outStr.append("k1: " + k1);
	// outStr.append("k2: " + k2 + "\n");
	outStr.append("MSCOSDIST = " + frm.format(mscosdist)
		+ ", MASS_DIFF_TERM = " + frm.format(massDiffTerm)
		+ " (abs mass diff = " + frm.format(absMassDiff) + ")\n");

	return outStr.toString();
    }

    // legacy
    int min, max; // min, max are mass ranges
    double step; // Same as Tolerance ?
    // end-legacy

    double mscosdist;
    double absMassDiff;
    double massDiffTerm;

    double tol;
    private final double COS_THRESHOLD = 0.00005;
    static final long serialVersionUID = 8368326281379099335L;
}
