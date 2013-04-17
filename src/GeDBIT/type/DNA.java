/**
 * edu.utexas.GeDBIT.type.DNA 2006.05.24
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.05.24: Modified from original jdb package, by Willard
 */
package GeDBIT.type;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import GeDBIT.dist.SymmetricSubstitutionWeightMatrix;
import GeDBIT.dist.WeightMatrix;

/**
 * A <code>DNA</code> is a compact representation of a DNA sequence.
 * 
 * @author Rui Mao
 * @version 2003.06.06
 */
public class DNA extends Sequence {
    private static final long serialVersionUID = 1351256505737923337L;

    public static enum DNASymbol implements Symbol {
	A("Adenine", (byte) 0), C("Cytosine", (byte) 1), G("Guanine", (byte) 2), T(
		"Thymine", (byte) 3), R("Purine", (byte) 4), Y("Pyrimidine",
		(byte) 5), M("C or A", (byte) 6), K("T, U,or G", (byte) 7), W(
		"T, U or A", (byte) 8), S("C or G", (byte) 9), B("not A",
		(byte) 10), D("not C", (byte) 11), H("not G", (byte) 12), V(
		"not T, U", (byte) 13), N("Any base", (byte) 14);

	private String description;
	public byte byteValue;

	DNASymbol() {
	}

	DNASymbol(String description, byte byteValue) {
	    this.description = description;
	    this.byteValue = byteValue;
	}

	public String description() {
	    return description;
	}

	public byte byteValue() {
	    return byteValue;
	}

	public String stringValue() {
	    return toString();
	}

	public Symbol getSymbol(String s) {
	    return valueOf(s);
	}

	public void writeExternal(ObjectOutput out) throws IOException {
	    out.writeInt(description.length());
	    out.writeChars(description);
	    out.writeByte(byteValue);
	}

	public void readExternal(ObjectInput in) throws IOException,
		ClassNotFoundException {
	    char[] charDescription = new char[in.readInt()];
	    for (int i = 0; i < charDescription.length; i++) {
		charDescription[i] = in.readChar();
	    }
	    description = String.copyValueOf(charDescription);
	    byteValue = in.readByte();

	}

	public static int distinctSize() {
	    return 15;
	}
    }

    public static final Alphabet ALPHABET = new Alphabet(DNASymbol.values(),
	    DNASymbol.distinctSize());

    public static final Alphabet SIMPLE_ALPHABET = new Alphabet(
	    new DNASymbol[] { DNASymbol.A, DNASymbol.C, DNASymbol.T,
		    DNASymbol.G }, 4);

    public Alphabet getAlphabet() {
	return ALPHABET;
    }

    public DNA(String sequenceID, String sequence) {
	super(sequenceID, sequence);
	for (int i = 0; i < data.length; i++) {
	    data[i] = (DNASymbol.valueOf(sequence.substring(i, i + 1)))
		    .byteValue();
	}
    }

    public Symbol get(int index) {
	return ALPHABET.get(data[index]);
    }

    public String toString() {
	StringBuffer buffer = new StringBuffer(data.length);
	for (int i = 0; i < data.length; i++)
	    buffer.append(ALPHABET.get(data[i]));
	return buffer.toString();
    }

    /**
     * The SimpleDNAEditDistanceMatrix looks like: {0,1,1,1}, {1,0,1,1},
     * {1,1,0,1}, {1,1,1,0}
     */
    public final static WeightMatrix SimpleDNAEditDistanceMatrix = new SymmetricSubstitutionWeightMatrix(
	    DNA.SIMPLE_ALPHABET, new double[][] { { 0, 1, 1, 1 },
		    { 1, 0, 1, 1 }, { 1, 1, 0, 1 }, { 1, 1, 1, 0 } });

    public final static WeightMatrix SimpleWeightedDNAMatrix = new SymmetricSubstitutionWeightMatrix(
	    DNA.SIMPLE_ALPHABET, new double[][] { { 0, 2 / 3, 2 / 3, 2 / 3 },
		    { 2 / 3, 0, 2 / 3, 2 / 3 }, { 2 / 3, 2 / 3, 0, 1 },
		    { 2 / 3, 2 / 3, 1, 0 } });

    public final static WeightMatrix EditDistanceWeightMatrix = new SymmetricSubstitutionWeightMatrix(
	    DNA.ALPHABET,
	    new double[][] {
		    { 0, 1, 1, 1, 0.5, 1, 0.5, 1, 0.5, 1, 1, 0.5, 0.5, 0.5, 0.5 },// A
										  // Adenine
		    { 1, 0, 1, 1, 1, 0.5, 0.5, 1, 1, 0.5, 0.5, 1, 0.5, 0.5, 0.5 },// C
										  // Cytosine
		    { 1, 1, 0, 1, 0.5, 1, 1, 0.5, 1, 0.5, 0.5, 0.5, 1, 0.5, 0.5 },// G
										  // Guanine
		    { 1, 1, 1, 0, 1, 0.5, 1, 0.5, 0.5, 1, 0.5, 0.5, 0.5, 1, 0.5 },// T
										  // Thymine
		    { 0.5, 1, 0.5, 1, 0, 1, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5,
			    0.5, 0.5 }, // R Purine (A or G)
		    { 1, 0.5, 1, 0.5, 1, 0, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5,
			    0.5, 0.5 },// Y Pyrimidine (C, T, or U)
		    { 0.5, 0.5, 1, 1, 0.5, 0.5, 0, 1, 0.5, 0.5, 0.5, 0.5, 0.5,
			    0.5, 0.5 },// M C or A
		    { 1, 1, 0.5, 0.5, 0.5, 0.5, 1, 0, 0.5, 0.5, 0.5, 0.5, 0.5,
			    0.5, 0.5 },// K T, U, or G
		    { 0.5, 1, 1, 0.5, 0.5, 0.5, 0.5, 0.5, 0, 1, 0.5, 0.5, 0.5,
			    0.5, 0.5 },// W T, U, or A
		    { 1, 0.5, 0.5, 1, 0.5, 0.5, 0.5, 0.5, 1, 0, 0.5, 0.5, 0.5,
			    0.5, 0.5 },// S C or G
		    { 1, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0, 0.5,
			    0.5, 0.5, 0.5 },// B C, T, U, or G (not A)
		    { 0.5, 1, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0,
			    0.5, 0.5, 0.5 }, // D A, T, U, or G (not C)
		    { 0.5, 0.5, 1, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5,
			    0, 0.5, 0.5 },// H A, T, U, or C (not G)
		    { 0.5, 0.5, 0.5, 1, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5,
			    0.5, 0, 0.5 }, // V A, C, or G (not T, not U)
		    { 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5,
			    0.5, 0.5, 0.5, 0 } // N Anybase (A,C,G,T,or U)
	    });

}
