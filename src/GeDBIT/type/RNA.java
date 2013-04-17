/**
 * edu.utexas.GeDBIT.type.RNA 2003.06.05
 *
 * Copyright Information:
 *
 * Change Log:
 * 2003.06.05: Created, by Rui Mao
 * 2003.11.07: Tuned for performance, by Willard
 * 2004.08.18: Made constructor public, by Willard
 */
package GeDBIT.type;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import GeDBIT.dist.SymmetricSubstitutionWeightMatrix;
import GeDBIT.dist.WeightMatrix;
import GeDBIT.type.DNA.DNASymbol;

/**
 * A compact representation of an RNA sequence.
 * 
 * @author Rui Mao. Willard
 * @version 2003.06.06
 */
public class RNA extends Sequence {
    private static final long serialVersionUID = -3352921445817956726L;

    /**
     * 
     * @author Willard
     * 
     */
    public enum RNASymbol implements Symbol {
	A("Adenine", (byte) 0), C("Cytosine", (byte) 1), G("Guanine", (byte) 2), U(
		"Uracil", (byte) 3), R("Purine", (byte) 4), Y("Pyrimidine",
		(byte) 5), M("C or A", (byte) 6), K("T, U,or G", (byte) 7), W(
		"T, U or A", (byte) 8), S("C or G", (byte) 9), B("not A",
		(byte) 10), D("not C", (byte) 11), H("not G", (byte) 12), V(
		"not T, U", (byte) 13), N("Any base", (byte) 14);

	private String description;
	public byte byteValue;

	RNASymbol() {
	}

	RNASymbol(String description, byte byteValue) {
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

    /**
     * 
     */
    public static final Alphabet ALPHABET = new Alphabet(RNASymbol.values(),
	    RNASymbol.distinctSize());

    /**
     * @param sequenceID
     * @param sequence
     */
    public RNA(String sequenceID, String sequence) {
	super(sequenceID, sequence);
	for (int i = 0; i < data.length; i++) {
	    data[i] = (DNASymbol.valueOf(sequence.substring(i, i + 1)))
		    .byteValue();
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.type.Sequence#getAlphabet()
     */
    public Alphabet getAlphabet() {
	return ALPHABET;
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.type.Sequence#get(int)
     */
    public Symbol get(int index) {
	return ALPHABET.get(data[index]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.type.Sequence#toString()
     */
    public String toString() {
	StringBuffer buffer = new StringBuffer(data.length);
	for (int i = 0; i < data.length; i++)
	    buffer.append(ALPHABET.get(data[i]));
	return buffer.toString();
    }

    /**
     * The SimpleEditDistanceMatrix looks like: {0,1,1,1}, {1,0,1,1}, {1,1,0,1},
     * {1,1,1,0}
     */
    public final static WeightMatrix SimpleEditDistanceMatrix = new SymmetricSubstitutionWeightMatrix(
	    DNA.ALPHABET, new double[][] { { 0, 1, 1, 1 }, { 1, 0, 1, 1 },
		    { 1, 1, 0, 1 }, { 1, 1, 1, 0 } });
}
