/**
 * edu.utexas.GeDBIT.dist.ImageMetric 2006.06.19
 *
 * Copyright Information:
 * 
 * Change Log:
 * 2006.06.19: Copied from jdb 1.0, by Rui Mao

 */

package GeDBIT.dist;

import GeDBIT.type.Image;
import GeDBIT.type.IndexObject;

/**
 * Computes the distance between images. Designed for the GeDBIT image dataset,
 * <code>ImageMetric</code> may not work for other datasets.
 * 
 * @author Wenguo Liu, Rui Mao
 * @version 2005.10.31
 */

public class ImageMetric implements Metric {
    /**
     * 
     */
    private static final long serialVersionUID = 3442536718895037166L;

    /**
     * Feature number for each image. We have three features for each image:
     * structure, texture, histogram.
     */
    final private int feaNum = 3;

    /**
     * Number of floats that can represent each feature.
     */
    final private int feaLength[] = { 3, 48, 15 };

    /**
     * Distance function selection for each feature.
     */
    final private boolean minBool[] = { false, false, false };

    /**
     * The weights of each feature in the computation of distance.
     */
    final private double weights[] = { 0.333333, 0.333333, 0.333333 };

    /**
     * The max distance for each feature.
     */
    final private double maxDist[] = { 1.0, 60.0, 1.0 };

    /**
     * @param one
     *            the {@link Object} over which the keys are defined.
     * @param two
     *            the other {@link Object} over which the keys are defined.
     */
    public double getDistance(IndexObject one, IndexObject two) {
	return getDistance((Image) one, (Image) two);
    }

    /**
     * @param one
     *            the {@link Image} over which the keys are defined.
     * @param two
     *            the other {@link Image} over which the keys are defined.
     */
    public double getDistance(Image one, Image two) {
	double dist = 0.0;
	for (int i = 0; i < feaNum; i++) {
	    dist += (getDistance_Fea(one, two, i) / maxDist[i] * weights[i]);
	    // dist += ( 2.0*getDistance_Fea(one, two, i) /
	    // (one.getMaxDist(i)+two.getMaxDist(i)) *
	    // weights[i] ) ;
	}
	return dist;
    }

    /**
     * @param one
     *            the {@link Image} over which the keys are defined.
     * @param two
     *            the other {@link Image} over which the keys are defined.
     * @param FeaIndex
     *            the feature on which distance is to be computed.
     */
    public double getDistance_Fea(Image one, Image two, int FeaIndex) {
	int StartIndex = 0, EndIndex = 0, cnt;
	double dist = 0.0, tempval = 0.0;

	for (int i = 0; i < FeaIndex; i++)
	    StartIndex += feaLength[i];
	EndIndex = StartIndex + feaLength[FeaIndex] - 1;

	// The first method for computing image object distance.
	if (minBool[FeaIndex]) {
	    for (cnt = StartIndex; cnt <= EndIndex; cnt++) {
		dist += Math.min(one.getFeature(cnt), two.getFeature(cnt));
		tempval += one.getFeature(cnt);
		// to make it a symmetric Metric space, add the following line
		// tempval += two.m_Feas[cnt] ;
	    }
	    dist = Math.abs(1.0 - (dist / tempval));
	} else { // The second method for computing image object distance.
	    for (cnt = StartIndex; cnt <= EndIndex; cnt++) {
		tempval = (one.getFeature(cnt) - two.getFeature(cnt));
		dist += (tempval * tempval);
	    }
	    dist = Math.sqrt(dist);
	}

	return dist;
    }

}
