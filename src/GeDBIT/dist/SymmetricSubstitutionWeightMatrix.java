/**
 * GeDBIT.dist.SymmetricSubstitutionWeightMatrix 2006.05.24
 * 
 * Copyright Information:
 *
 * Change Log:
 * 2006.05.24: Modified from the original jdb package, by Willard; original class: WeightedAlphabetMetric 
 */

package GeDBIT.dist;

import GeDBIT.type.Alphabet;
import GeDBIT.type.Symbol;

/**
 * Implements a {@link WeightMatrix} by providing a symmetric substitution matrix of double
 * distances for a given {@link Alphabet}. Thus, this defines a weighted edit distance on an
 * {@link Alphabet}
 * 
 * @author Rui Mao, Willard
 * @version 2004.03.02
 */
public class SymmetricSubstitutionWeightMatrix implements WeightMatrix {
    /**
     * 
     */
    private static final long serialVersionUID = 6295557519158168768L;

    private Alphabet          alphabet;
    private int               alphabetSize;
    private double[][]        distances;

    /**
     * This constructor provides the {@link Alphabet} and an array of double distances between the
     * {@link Symbol} objects. The distance array represents the lower triangle partof a symmetric
     * matrix of distances, The distance between two {@link Symbol} objects with indices <em>I</em>
     * and <em>J</em> will be computed by first computing <em>R=max(I,J)</em> and
     * <em> C=min(I,J) </em>, then indexing the distance array with index <em>R*(R+1)/2+C</em>.
     */
    public SymmetricSubstitutionWeightMatrix(Alphabet alphabet, double[][] distances) {
        this.alphabetSize = alphabet.distinctSize();
        int minLength = alphabetSize;
        if (minLength > distances.length)
            throw new IndexOutOfBoundsException("array \"distances\" is length " + distances.length
                    + "; too small for \"alphabet\" of size " + alphabetSize);
        for (int i = 0; i < distances.length; i++) {
            if (minLength > distances[i].length) {
                throw new IndexOutOfBoundsException("array \"distances\" is length "
                        + distances.length + "; too small for \"alphabet\" of size " + alphabetSize);
            }
        }

        this.alphabet = alphabet;
        this.distances = distances;
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.dist.WeightMatrix#getDistance(GeDBIT.type.Symbol, GeDBIT.type.Symbol)
     */
    public double getDistance(Symbol one, Symbol two) {
        return distances[one.byteValue()][two.byteValue()];
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.dist.WeightMatrix#getAlphabet()
     */
    public Alphabet getAlphabet() {
        return alphabet;
    }
}
