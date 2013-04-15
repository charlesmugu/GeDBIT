/**
 * GeDBIT.type.Symbol 2006.05.24
 * 
 * Copyright Information:
 *
 * Change Log:
 * 2006.05.24: Modified from the original jdb package, by Willard
 */
package GeDBIT.type;

import java.io.Externalizable;

/**
 * Interface <code>Symbol</code> is implemented for the characters of a biosequence alphabet.
 * Since biosequences often have a long life in an external representation, <code>Symbol</code>
 * alphabets should have a similarly long life, suggesting that they be immutable and easily
 * serializable. In this package, this is accomplished by implementing <code>Symbol</code> sets as
 * java enums. In addition, {@link Sequence}s using <code>Symbol</code> alphabets include a
 * <code>static public final</code> {@link Alphabet} instance, named <code>ALPHABET</code>,
 * that references the enumeration. For example, class {@link Peptide} contains the the class
 * {@link Peptide.AminoAcid} that implements <code>Symbol</code>for the common amino acids.
 * 
 * @author Jack, Rui Mao, Willard
 * @version 2003.06.04
 */
public interface Symbol extends Externalizable
{

    /**
     * @param s
     * @return
     */
    public Symbol getSymbol(String s);

    /**
     * @return
     */
    public byte byteValue();

    /**
     * @return
     */
    public String stringValue();
}
