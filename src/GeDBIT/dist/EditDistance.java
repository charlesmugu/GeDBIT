package GeDBIT.dist;

/**
 * mobios.dist.EditDistance, 2011.07.21
 *
 * Copyright Information:
 * 
 * Change Log:
 * 2011.07.21: Modified from http://www.merriampark.com/ld.htm#FLAVORS, by Rui Mao.
 */

import GeDBIT.dist.Metric;
import GeDBIT.type.IndexObject;
import GeDBIT.type.StringObject;

/**
 * This class computes edit distance between two {@link String}s. Insert and
 * delete cost are considered to be the same, called gap cost.
 * 
 * @author Rui Mao
 * @version 2011.07.21
 */
public class EditDistance implements Metric {
    final int gapCost;
    final int substitutionCost;

    private static final long serialVersionUID = -4658067077491099474L;

    /**
     * Default constructor, gap cost (insert/delete cost) =1, substitution cost
     * =1;
     */
    public EditDistance() {
	this(1, 1);
    }

    /**
     * Constructor, the two arguments should satisfy gapCost/2 <=
     * substitutionCost <= gapCost *2
     * 
     * @param gapCost
     *            insert/delete cost
     * @param substitutionCost
     *            substitution cost.
     */
    public EditDistance(int gapCost, int substitutionCost) {
	if ((gapCost > substitutionCost * 2)
		|| (substitutionCost > gapCost * 2))
	    throw new IllegalArgumentException(
		    "the two arguments should satisfy gapCost/2 <= substitutionCost <= gapCost *2!");

	this.gapCost = gapCost;
	this.substitutionCost = substitutionCost;
    }

    // ****************************
    // Get minimum of three values
    // ****************************

    private int minimum(int a, int b, int c) {
	int min = (a <= b) ? a : b;

	return (min <= c) ? min : c;
    }

    /**
     * Computes the distance, the two arguments should be of type {@link String}
     */
    public double getDistance(Object one, Object two) {
	if (one == null)
	    throw new IllegalArgumentException(
		    "the first object to compute distance is null!");

	if (two == null)
	    throw new IllegalArgumentException(
		    "the second object to compute distance is null!");

	return getDistance((String) one, (String) two);
    }

    public double getDistance(IndexObject one, IndexObject two) {
	if (one == null)
	    throw new IllegalArgumentException(
		    "the first object to compute distance is null!");

	if (two == null)
	    throw new IllegalArgumentException(
		    "the second object to compute distance is null!");

	return getDistance((StringObject) one, (StringObject) two);
    }

    /**
     * Computes the distance between two {@link String}s
     * 
     * @param first
     *            the first String to compute edit distance
     * @param second
     *            the second String to compute edit distance
     * @return the edit distance between the two arguments
     */
    public double getDistance(StringObject first, StringObject second) {
	return getDistance(first.getData(), second.getData());
    }

    public double getDistance(String first, String second) {
	int matrix[][]; // the dynamic programming matrix

	final int firstSize = first.length(); // length of first
	final int secondSize = second.length(); // length of second

	// Step 1
	if (firstSize == 0)
	    return secondSize * gapCost;

	if (secondSize == 0)
	    return firstSize * gapCost;

	matrix = new int[firstSize + 1][secondSize + 1];

	// Step 2, initialize the matrix( first row and first column)
	for (int i = 0; i <= firstSize; i++)
	    matrix[i][0] = i * gapCost;
	for (int j = 0; j <= secondSize; j++)
	    matrix[0][j] = j * gapCost;

	// Step 3, fill the matrix
	char firstChar;
	int j;
	int cost;
	for (int i = 1; i <= firstSize; i++) {
	    firstChar = first.charAt(i - 1);
	    for (j = 1; j <= secondSize; j++) {
		cost = (firstChar == second.charAt(j - 1)) ? 0
			: substitutionCost;
		matrix[i][j] = minimum(matrix[i - 1][j] + gapCost,
			matrix[i][j - 1] + gapCost, matrix[i - 1][j - 1] + cost);
	    }
	}

	return matrix[firstSize][secondSize];

    }

    /**
     * main method, for test purpose.
     * 
     * @param args
     */
    public static void main(String[] args) {
	System.out.print("EditDistance(\"" + args[0] + "\", \"" + args[1]
		+ "\" ) = ");
	EditDistance metric = new EditDistance();
	System.out.println(metric.getDistance(args[0], args[1]));
    }

}