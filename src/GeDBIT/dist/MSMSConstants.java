/**
 * edu.utexas.GeDBIT.dist.MSDataConstants_MSMS  02 Aug 2004
 *
 * Copyright Info:
 * 
 * 
 *
 * Change Log:
 */

package GeDBIT.dist;

/**
 *
 * Constant static default values for building indices on mass spectrometer data
 * Code uses these as default, "tolerance" etc. also configurable via constructors. 
 * 
 * @author Smriti Ramakrishnan
 * @version 2004.08.02
 */

public class MSMSConstants {
    private MSMSConstants() {};
    
  /**
   * The default tolerance, tried values are 0.2 Da, 1.0 Da 
   */
  public static final double MS_TOLERANCE = 0.2;
  //public static final double MS_TOLERANCE = 0; // for exact tcd, exact tcd_alpha, exact mscosdist
  //public static final double MS_TOLERANCE = 0.8;
	
  public static final double MS_PRECURSOR_TOLERANCE = 2.0;
  //public static final double MS_PRECURSOR_TOLERANCE = 0; //for exact tcd_alpha, exact tcd
  //public static final double MS_PRECURSOR_TOLERANCE = 1.4;
  
  public static final double TOL = MS_TOLERANCE + MS_PRECURSOR_TOLERANCE; // for tcd and tcd_alpha, 7prot
  //public static final double TOL = 2.6; // for alpha_precuror alone
  //public static final double TOL = java.lang.Math.PI/2 + 2*MS_PRECURSOR_TOLERANCE; // ideal TOL for TCD from paper
  //public static final double TOL = 4.0; // for tcd_alpha, jquery
  //public static final double TOL = 1.5; // for precursor only - this TL to adjust semi-metric an be reduced, when keeping PM_TOL = 2.0 
  //public static final double TOL = java.lang.Math.PI/16; // for 1-e^-x
  //public static final double TOL = java.lang.Math.PI/2; // for exact and fuzzy cosine distance
  
  public static final int MS_MI_MASS = 0;
  public static final int MS_AVG_MASS = 1;
  public static final int MS_MASS_TYPE = MS_MI_MASS;
  
  //public static final double MS_TOLERANCE = 1.0;
  //public static final double MS_TOLERANCE = 80.0;

  /**
   * The maximum length in characters of the string that represents the name
   * of a privaledge group.
   */
  public static final double MS_STD_DEV = MS_TOLERANCE/2.0;

}
