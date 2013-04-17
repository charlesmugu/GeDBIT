/**
 * edu.utexas.GeDBIT.type.Peptide 2003.06.06
 *
 * Copyright Information:
 *
 * Change Log:
 * 2003.06.06: Created, by Rui Mao
 * 2003.11.07: Tuned for performance, by Willard Briggs
 */
package GeDBIT.type;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import GeDBIT.dist.SymmetricSubstitutionWeightMatrix;
import GeDBIT.dist.WeightMatrix;

/**
 * A <code>Peptide</code> is a compact representation of a AminoAcids
 * sequence(Peptide). Each AminoAcids is stored as a byte.
 * 
 * @author Rui Mao
 * @version 2003.06.06
 */
public class Peptide extends Sequence {
    private static final long serialVersionUID = 8791814013616626066L;

    public static enum AminoAcid implements Symbol {
	A("Ala", "Alanine", (byte) 0), R("Arg", "Arginine", (byte) 1), N("Asn",
		"Asparagine", (byte) 2), D("Asp", "Aspartic acid", (byte) 3), C(
		"Cys", "Cysteine", (byte) 4), Q("Gln", "Glutamine", (byte) 5), E(
		"Glu", "Glutamic acid", (byte) 6), G("Gly", "Glycine", (byte) 7), H(
		"His", "Histidine", (byte) 8), I("Ile", "Isoleucine", (byte) 9), L(
		"Leu", "Leucine", (byte) 10), K("Lys", "Lysine", (byte) 11), M(
		"Met", "Methionine", (byte) 12), F("Phe", "Phenylalanine",
		(byte) 13), P("Pro", "Proline", (byte) 14), S("Ser", "Serine",
		(byte) 15), T("Thr", "Threonine", (byte) 16), W("Trp",
		"Tryptophan", (byte) 17), Y("Tyr", "Tyrosine", (byte) 18), V(
		"Val", "Valine", (byte) 19), B("Unk", "Unknown", (byte) 20), Z(
		"Unk", "Unknown", (byte) 20), U("Unk", "Unknown", (byte) 20), X(
		"Unk", "Unknown", (byte) 20);

	static final long serialVersionUID = 7287648197182385655L;

	private String abbreviation;
	private String description;
	private byte byteValue;

	AminoAcid() {
	}

	AminoAcid(String abbreviation, String description, byte byteValue) {
	    this.abbreviation = abbreviation;
	    this.description = description;
	    this.byteValue = byteValue;
	}

	public String abbreviation() {
	    return abbreviation;
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

	public final String getString(int maxLength) {
	    if (maxLength < 1)
		throw new IllegalArgumentException("not positive " + maxLength);
	    if (maxLength < 3)
		return name();
	    if (maxLength < name().length())
		return abbreviation;
	    return description;
	}

	public void writeExternal(ObjectOutput out) throws IOException {
	    out.writeChars(abbreviation);
	    out.writeInt(description.length());
	    out.writeChars(description);
	    out.writeByte(byteValue);
	}

	public void readExternal(ObjectInput in) throws IOException,
		ClassNotFoundException {
	    char[] charAbbreviation = new char[3];
	    for (int i = 0; i < charAbbreviation.length; i++) {
		charAbbreviation[i] = in.readChar();
	    }
	    abbreviation = String.copyValueOf(charAbbreviation);
	    char[] charDescription = new char[in.readInt()];
	    for (int i = 0; i < charDescription.length; i++) {
		charDescription[i] = in.readChar();
	    }
	    description = String.copyValueOf(charDescription);
	    byteValue = in.readByte();

	}

	public static int distinctSize() {
	    return 21;
	}
    }

    public static final Alphabet ALPHABET = new Alphabet(AminoAcid.values(),
	    AminoAcid.distinctSize());

    public Alphabet getAlphabet() {
	return ALPHABET;
    }

    public Peptide(String sequenceID, String sequence) {
	super(sequenceID, sequence);
	for (int i = 0; i < data.length; i++) {
	    data[i] = (AminoAcid.valueOf(sequence.substring(i, i + 1)))
		    .byteValue();
	}
    }

    public Peptide(String sequence) {
	this(null, sequence);
    }

    // main method, for test
    public static void main(String[] args) {
	Peptide d = new Peptide("agtwc");
	System.out.println(d);
    }

    // changed the RW value from 3 to 4 by Weijia Xu July0404
    public final static WeightMatrix mPAM250aExtendedWeightMatrix = new SymmetricSubstitutionWeightMatrix(
	    ALPHABET, new double[][] {
		    { 0, 2, 2, 2, 3, 2, 2, 2, 2, 2, 2, 2, 2, 3, 2, 2, 2, 5, 4,
			    2, 7 },// A
		    { 2, 0, 2, 2, 4, 2, 2, 2, 2, 3, 3, 2, 2, 4, 2, 2, 2, 4, 4,
			    3, 7 },// R
		    { 2, 2, 0, 2, 4, 2, 2, 2, 2, 3, 3, 2, 2, 4, 2, 2, 2, 5, 4,
			    2, 7 },// N
		    { 2, 2, 2, 0, 4, 2, 2, 2, 2, 3, 3, 2, 3, 4, 2, 2, 2, 6, 4,
			    2, 7 },// D
		    { 3, 4, 4, 4, 0, 4, 4, 3, 4, 3, 4, 4, 4, 4, 3, 3, 3, 7, 3,
			    3, 7 },// C
		    { 2, 2, 2, 2, 4, 0, 2, 2, 2, 3, 3, 2, 2, 4, 2, 2, 2, 5, 4,
			    3, 7 },// Q
		    { 2, 2, 2, 2, 4, 2, 0, 2, 2, 3, 3, 2, 3, 4, 2, 2, 2, 6, 4,
			    2, 7 },// E
		    { 2, 2, 2, 2, 3, 2, 2, 0, 2, 2, 3, 2, 2, 4, 2, 2, 2, 6, 4,
			    2, 7 },// G
		    { 2, 2, 2, 2, 4, 2, 2, 2, 0, 3, 3, 2, 3, 3, 2, 2, 2, 5, 3,
			    3, 7 },// H
		    { 2, 3, 3, 3, 3, 3, 3, 2, 3, 0, 1, 3, 2, 2, 2, 2, 2, 5, 3,
			    2, 7 },// I
		    { 2, 3, 3, 3, 4, 3, 3, 3, 3, 1, 0, 3, 1, 2, 3, 3, 2, 4, 2,
			    1, 7 },// L
		    { 2, 2, 2, 2, 4, 2, 2, 2, 2, 3, 3, 0, 2, 4, 2, 2, 2, 4, 4,
			    3, 7 },// K
		    { 2, 2, 2, 3, 4, 2, 3, 2, 3, 2, 1, 2, 0, 2, 2, 2, 2, 4, 3,
			    2, 7 },// M
		    { 3, 4, 4, 4, 4, 4, 4, 4, 3, 2, 2, 4, 2, 0, 4, 3, 3, 3, 1,
			    2, 7 },// F
		    { 2, 2, 2, 2, 3, 2, 2, 2, 2, 2, 3, 2, 2, 4, 0, 2, 2, 5, 4,
			    2, 7 },// P
		    { 2, 2, 2, 2, 3, 2, 2, 2, 2, 2, 3, 2, 2, 3, 2, 0, 2, 5, 4,
			    2, 7 },// S
		    { 2, 2, 2, 2, 3, 2, 2, 2, 2, 2, 2, 2, 2, 3, 2, 2, 0, 5, 3,
			    2, 7 },// T
		    { 5, 4, 5, 6, 7, 5, 6, 6, 5, 5, 4, 4, 4, 3, 5, 5, 5, 0, 4,
			    5, 7 },// W
		    { 4, 4, 4, 4, 3, 4, 4, 4, 3, 3, 2, 4, 3, 1, 4, 4, 3, 4, 0,
			    3, 7 },// Y
		    { 2, 3, 2, 2, 3, 3, 2, 2, 3, 2, 1, 3, 2, 2, 2, 2, 2, 5, 3,
			    0, 7 },// V
		    { 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
			    7, 0 } // OTHER
	    });

    @Override
    public Symbol get(int index) {
	return ALPHABET.get(data[index]);
    }

    @Override
    public String toString() {
	StringBuffer buffer = new StringBuffer(data.length);
	for (int i = 0; i < data.length; i++)
	    buffer.append(ALPHABET.get(data[i]));
	return buffer.toString();
    }
}