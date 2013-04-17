/**
 * GeDBIT.type.PeptideTable 2006.07.24 
 * 
 * Change Log: 
 * 2006.07.24: Added, by Willard
 */
package GeDBIT.type;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import GeDBIT.dist.WeightMatrix;
import GeDBIT.dist.SequenceFragmentMetric;
import GeDBIT.dist.WHDGlobalSequenceFragmentMetric;

public class PeptideTable extends SequenceTable {
    /**
     * 
     */
    private static final long serialVersionUID = 5685357333502524772L;

    /**
     * 
     */
    public static final WeightMatrix DEFAULT_WEIGHT_MATRIX = Peptide.mPAM250aExtendedWeightMatrix;

    /**
     * 
     */
    public static final SequenceFragmentMetric DEFAULT_METRIC = new WHDGlobalSequenceFragmentMetric(
	    DEFAULT_WEIGHT_MATRIX);

    /**
     * @param fileName
     * @param maxDataSize
     * @param fragmentLength
     * @throws IOException
     */
    public PeptideTable(String fileName, String indexPrefix, int maxDataSize,
	    int fragmentLength) throws IOException {
	this(fileName, indexPrefix, maxDataSize, DEFAULT_METRIC, fragmentLength);
    }

    /**
     * @param fileName
     * @param maxDataSize
     * @param metric
     * @param fragmentLength
     * @throws IOException
     */
    public PeptideTable(String fileName, String indexPrefix, int maxDataSize,
	    SequenceFragmentMetric metric, int fragmentLength)
	    throws IOException {
	super(fileName, indexPrefix, maxDataSize, metric, fragmentLength);
    }

    @Override
    protected void loadData(BufferedReader reader, int maxSize) {
	String ident = "";
	List<Peptide> seqs = new ArrayList<Peptide>();
	int counter = 0;
	int sequenceLengthCounter = 0;
	try {
	    // read sequences from file
	    StringBuffer currentSequence = new StringBuffer();
	    String line = reader.readLine();
	    if (line != null)
		line = line.trim();

	    while (line != null && counter < maxSize
		    && sequenceLengthCounter < maxSize) {
		if (line.length() >= 1) {
		    if (line.charAt(0) == '>') // beginning of a sequence
		    {
			if (currentSequence.length() != 0) {
			    seqs.add(new Peptide(ident, currentSequence
				    .toString()));
			    counter += currentSequence.length();
			    currentSequence.setLength(0);
			}
			ident = line;
		    } else
		    // begin of a new line of current sequence
		    {
			currentSequence.append(line);
			sequenceLengthCounter = currentSequence.length();
		    }
		}
		line = reader.readLine();
		if (line != null)
		    line = line.trim();
	    }

	    if (currentSequence.length() != 0)
		seqs.add(new Peptide(ident, currentSequence.toString()));
	} catch (java.io.IOException e) {
	    throw new java.lang.IllegalStateException(
		    "Error occured when reading FASTA sequence file: " + reader
			    + " error message returned: " + e.getMessage());
	}
	sequences = new Peptide[seqs.size()];
	seqs.toArray(sequences);
    }
}
