/**
 * GeDBIT.dist.WeightMatrix 2006.05.24
 * $Revision: 1.1 $
 * $Date: 2013/01/24 02:01:04 $
 
 * Copyright Information:
 *
 * Change Log:
 * 2006.05.24: Modified from the original GeDBIT package, by Willard; original class was called:AlphabetMetric
 */
package GeDBIT.dist;

import java.io.Serializable;

import GeDBIT.type.Alphabet;
import GeDBIT.type.Symbol;
import GeDBIT.type.Sequence; // for javadoc
import GeDBIT.type.Fragment; // for javadoc

/**
 * A <code>WeightMatrix</code> is the substitution matrix of an {@link Alphabet}
 * of {@link Symbol} objects. It defines the distance between each pair of
 * <code>Symbol</code>s of the <code>Alphabet</code.. Using a
 * <code>WeightMatrix</code>, a weighted edit distance metric can be defined on
 * {@link Sequence} or {@link Fragment} objects.
 * 
 * @author Jack, Rui Mao, Weijia Xu, Willard
 * @version 2004.03.02
 */
public interface WeightMatrix extends Serializable {
    /**
     * @return the {@link Alphabet} over which this matrix is defined.
     */
    public Alphabet getAlphabet();

    /**
     * @return the distance between two {@link Symbol} objects in the
     *         {@link Alphabet} over which this matrix is defined.
     */
    public double getDistance(Symbol one, Symbol two);

}
