/**
 * edu.utexas.GeDBIT.util.LargeDenseDoubleMatrix2D, 2006.06.15 
 * 
 * Copyright Information: 
 * Copyright ??? 1999 CERN - European Organization for Nuclear Research. Permission to use, copy, modify, distribute and
 * sell this software and its documentation for any purpose is hereby granted without fee, provided that the above
 * copyright notice appear in all copies and that both that copyright notice and this permission notice appear in
 * supporting documentation. CERN makes no representations about the suitability of this software for any purpose. It is
 * provided "as is" without expressed or implied warranty.
 * 
 * Change Log: 2006.06.15: Modified from the DenseDoubleMatrix2D.java in the colt package, by Rui Mao
 */

package GeDBIT.util;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix1DProcedure;
// import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.AbstractMatrix2D;
import cern.colt.function.DoubleDoubleFunction;
import cern.colt.function.DoubleFunction;
import cern.colt.function.IntIntDoubleFunction;
import cern.colt.list.IntArrayList;
import cern.colt.list.DoubleArrayList;

import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.doublealgo.Statistic;
import cern.colt.matrix.doublealgo.Statistic.VectorVectorFunction;

/**
 * Large Dense 2-d matrix holding <tt>double</tt> elements. It aims at huge
 * matrix whose elements can not be stored in a single array. For matrices that
 * are not that large, consider {@link DenseDoubleMatrix2D}.
 * <p>
 * <b>Implementation:</b>
 * <p>
 * Internally holds a two-dimensional double array, consistent to the topology
 * of the matrix . Note that this implementation is not synchronized.
 * <p>
 * 
 * @author Rui Mao
 * @version 2006.06.15
 */
public class LargeDenseDoubleMatrix2D extends DoubleMatrix2D {
    public static final long sizeGate = 100000000; // 100M

    static final long serialVersionUID = 1020177651L;
    /**
     * The elements of this matrix.
     */
    protected double[][] elements;

    /**
     * Constructs a matrix with a copy of the given values. <tt>values</tt> is
     * required to have the form <tt>values[row][column]</tt> and have exactly
     * the same number of columns in every row.
     * <p>
     * The values are copied. So subsequent changes in <tt>values</tt> are not
     * reflected in the matrix, and vice-versa.
     * 
     * @param values
     *            The values to be filled into the new matrix.
     * @throws IllegalArgumentException
     *             if
     *             <tt>for any 1 &lt;= row &lt; values.length: values[row].length != values[row-1].length</tt>
     *             .
     */
    public LargeDenseDoubleMatrix2D(double[][] values) {
	this(values.length, values.length == 0 ? 0 : values[0].length);
	assign(values);
    }

    /**
     * Constructs a matrix with a given number of rows and columns. All entries
     * are initially <tt>0</tt>.
     * 
     * @param rows
     *            the number of rows the matrix shall have.
     * @param columns
     *            the number of columns the matrix shall have.
     * @throws IllegalArgumentException
     *             if
     *             <tt>rows<0 || columns<0 || (double)columns*rows > Integer.MAX_VALUE</tt>
     *             .
     */
    public LargeDenseDoubleMatrix2D(int rows, int columns) {
	// setUp(rows, columns);
	this.elements = new double[rows][columns];
    }

    /**
     * Constructs a view with the given parameters.
     * 
     * @param rows
     *            the number of rows the matrix shall have.
     * @param columns
     *            the number of columns the matrix shall have.
     * @param elements
     *            the cells.
     * @param rowZero
     *            the position of the first element.
     * @param columnZero
     *            the position of the first element.
     * @param rowStride
     *            the number of elements between two rows, i.e.
     *            <tt>index(i+1,j)-index(i,j)</tt>.
     * @param columnStride
     *            the number of elements between two columns, i.e.
     *            <tt>index(i,j+1)-index(i,j)</tt>.
     * @throws IllegalArgumentException
     *             if
     *             <tt>rows<0 || columns<0 || (double)columns*rows > Integer.MAX_VALUE</tt>
     *             or flip's are illegal.
     */
    protected LargeDenseDoubleMatrix2D(int rows, int columns,
	    double[] elements, int rowZero, int columnZero, int rowStride,
	    int columnStride) {
	/*
	 * setUp(rows, columns, rowZero, columnZero, rowStride, columnStride);
	 * this.elements = elements; this.isNoView = false;
	 */
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    /**
     * Sets all cells to the state specified by <tt>values</tt>. <tt>values</tt>
     * is required to have the form <tt>values[row][column]</tt> and have
     * exactly the same number of rows and columns as the receiver.
     * <p>
     * The values are copied. So subsequent changes in <tt>values</tt> are not
     * reflected in the matrix, and vice-versa.
     * 
     * @param values
     *            the values to be filled into the cells.
     * @return <tt>this</tt> (for convenience only).
     * @throws IllegalArgumentException
     *             if
     *             <tt>values.length != rows() || for any 0 &lt;= row &lt; rows(): values[row].length != columns()</tt>
     *             .
     */
    public DoubleMatrix2D assign(double[][] values) {
	/*
	 * if (this.isNoView) { if (values.length != rows) throw new
	 * IllegalArgumentException("Must have same number of rows:
	 * rows=" + values.length + "rows()=" + rows()); int i = columns * (rows
	 * - 1); for (int row = rows; --row >= 0;) { double[] currentRow =
	 * values[row]; if (currentRow.length != columns) throw new
	 * IllegalArgumentException("Must have same number of columns in every
	 * row: columns=" + currentRow.length + "columns()=" + columns());
	 * System.arraycopy(currentRow, 0, this.elements, i, columns); i -=
	 * columns; } } else { super.assign(values); }
	 */
	if (values.length != rows())
	    throw new IllegalArgumentException("values.length != rows()");
	for (double[] row : values)
	    if (row.length != columns())
		throw new IllegalArgumentException("row.length != columns()");

	this.elements = (double[][]) values.clone();
	return this;
    }

    /**
     * Sets all cells to the state specified by <tt>value</tt>.
     * 
     * @param value
     *            the value to be filled into the cells.
     * @return <tt>this</tt> (for convenience only).
     */
    public DoubleMatrix2D assign(double value) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");

	/*
	 * final double[] elems = this.elements; int index = index(0, 0); int cs
	 * = this.columnStride; int rs = this.rowStride; for (int row = rows;
	 * --row >= 0;) { for (int i = index, column = columns; --column >= 0;)
	 * { elems[i] = value; i += cs; } index += rs; } return this;
	 */
    }

    /**
     * Assigns the result of a function to each cell;
     * <tt>x[row,col] = function(x[row,col])</tt>.
     * <p>
     * <b>Example:</b>
     * 
     * <pre>
     *       matrix = 2 x 2 matrix
     *       0.5 1.5      
     *       2.5 3.5
     *  
     *       // change each cell to its sine
     *       matrix.assign(cern.jet.math.Functions.sin);
     *       --&gt;
     *       2 x 2 matrix
     *       0.479426  0.997495 
     *       0.598472 -0.350783
     * </pre>
     * 
     * For further examples, see the <a
     * href="package-summary.html#FunctionObjects">package doc</a>.
     * 
     * @param function
     *            a function object taking as argument the current cell's value.
     * @return <tt>this</tt> (for convenience only).
     * @see cern.jet.math.Functions
     */
    public DoubleMatrix2D assign(cern.colt.function.DoubleFunction function) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");

	/*
	 * final double[] elems = this.elements; if (elems == null) throw new
	 * InternalError(); int index = index(0, 0); int cs = this.columnStride;
	 * int rs = this.rowStride; // specialization for speed if (function
	 * instanceof cern.jet.math.Mult) { // x[i] = mult*x[i] double
	 * multiplicator = ((cern.jet.math.Mult) function).multiplicator; if
	 * (multiplicator == 1) return this; if (multiplicator == 0) return
	 * assign(0); for (int row = rows; --row >= 0;) { // the general case
	 * for (int i = index, column = columns; --column >= 0;) { elems[i] *=
	 * multiplicator; i += cs; } index += rs; } } else { // the general case
	 * x[i] = f(x[i]) for (int row = rows; --row >= 0;) { for (int i =
	 * index, column = columns; --column >= 0;) { elems[i] =
	 * function.apply(elems[i]); i += cs; } index += rs; } } return this;
	 */
    }

    /**
     * Replaces all cell values of the receiver with the values of another
     * matrix. Both matrices must have the same number of rows and columns. If
     * both matrices share the same cells (as is the case if they are views
     * derived from the same matrix) and intersect in an ambiguous way, then
     * replaces <i>as if</i> using an intermediate auxiliary deep copy of
     * <tt>other</tt>.
     * 
     * @param source
     *            the source matrix to copy from (may be identical to the
     *            receiver).
     * @return <tt>this</tt> (for convenience only).
     * @throws IllegalArgumentException
     *             if
     *             <tt>columns() != source.columns() || rows() != source.rows()</tt>
     */
    public DoubleMatrix2D assign(DoubleMatrix2D source) {
	if (columns() != source.columns() || rows() != source.rows())
	    throw new IllegalArgumentException(
		    "columns() != source.columns() || rows() != source.rows()");

	if ((source instanceof LargeDenseDoubleMatrix2D)
		&& ((LargeDenseDoubleMatrix2D) source == this))
	    return this;

	for (int row = 0; row < rows(); row++)
	    for (int col = 0; col < columns(); col++)
		elements[row][col] = source.getQuick(row, col);

	return this;

	/*
	 * // overriden for performance only if (!(source instanceof
	 * DenseDoubleMatrix2D)) { return super.assign(source); }
	 * DenseDoubleMatrix2D other = (DenseDoubleMatrix2D) source; if (other
	 * == this) return this; // nothing to do checkShape(other); if
	 * (this.isNoView && other.isNoView) { // quickest
	 * System.arraycopy(other.elements, 0, this.elements, 0,
	 * this.elements.length); return this; } if (haveSharedCells(other)) {
	 * DoubleMatrix2D c = other.copy(); if (!(c instanceof
	 * DenseDoubleMatrix2D)) { // should not happen return
	 * super.assign(other); } other = (DenseDoubleMatrix2D) c; } final
	 * double[] elems = this.elements; final double[] otherElems =
	 * other.elements; if (elems == null || otherElems == null) throw new
	 * InternalError(); int cs = this.columnStride; int ocs =
	 * other.columnStride; int rs = this.rowStride; int ors =
	 * other.rowStride; int otherIndex = other.index(0, 0); int index =
	 * index(0, 0); for (int row = rows; --row >= 0;) { for (int i = index,
	 * j = otherIndex, column = columns; --column >= 0;) { elems[i] =
	 * otherElems[j]; i += cs; j += ocs; } index += rs; otherIndex += ors; }
	 * return this;
	 */
    }

    /**
     * Assigns the result of a function to each cell;
     * <tt>x[row,col] = function(x[row,col],y[row,col])</tt>.
     * <p>
     * <b>Example:</b>
     * 
     * <pre>
     *       // assign x[row,col] = x[row,col]&lt;sup&gt;y[row,col]&lt;/sup&gt;
     *       m1 = 2 x 2 matrix 
     *       0 1 
     *       2 3
     *  
     *       m2 = 2 x 2 matrix 
     *       0 2 
     *       4 6
     *  
     *       m1.assign(m2, cern.jet.math.Functions.pow);
     *       --&gt;
     *       m1 == 2 x 2 matrix
     *       1   1 
     *       16 729
     * </pre>
     * 
     * For further examples, see the <a
     * href="package-summary.html#FunctionObjects">package doc</a>.
     * 
     * @param y
     *            the secondary matrix to operate on.
     * @param function
     *            a function object taking as first argument the current cell's
     *            value of <tt>this</tt>, and as second argument the current
     *            cell's value of <tt>y</tt>,
     * @return <tt>this</tt> (for convenience only).
     * @throws IllegalArgumentException
     *             if
     *             <tt>columns() != other.columns() || rows() != other.rows()</tt>
     * @see cern.jet.math.Functions
     */
    public DoubleMatrix2D assign(DoubleMatrix2D y,
	    cern.colt.function.DoubleDoubleFunction function) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");

	/*
	 * // overriden for performance only if (!(y instanceof
	 * DenseDoubleMatrix2D)) { return super.assign(y, function); }
	 * DenseDoubleMatrix2D other = (DenseDoubleMatrix2D) y; checkShape(y);
	 * final double[] elems = this.elements; final double[] otherElems =
	 * other.elements; if (elems == null || otherElems == null) throw new
	 * InternalError(); int cs = this.columnStride; int ocs =
	 * other.columnStride; int rs = this.rowStride; int ors =
	 * other.rowStride; int otherIndex = other.index(0, 0); int index =
	 * index(0, 0); // specialized for speed if (function ==
	 * cern.jet.math.Functions.mult) { // x[i] = x[i] * y[i] for (int row =
	 * rows; --row >= 0;) { for (int i = index, j = otherIndex, column =
	 * columns; --column >= 0;) { elems[i] *= otherElems[j]; i += cs; j +=
	 * ocs; } index += rs; otherIndex += ors; } } else if (function ==
	 * cern.jet.math.Functions.div) { // x[i] = x[i] / y[i] for (int row =
	 * rows; --row >= 0;) { for (int i = index, j = otherIndex, column =
	 * columns; --column >= 0;) { elems[i] /= otherElems[j]; i += cs; j +=
	 * ocs; } index += rs; otherIndex += ors; } } else if (function
	 * instanceof cern.jet.math.PlusMult) { double multiplicator =
	 * ((cern.jet.math.PlusMult) function).multiplicator; if (multiplicator
	 * == 0) { // x[i] = x[i] + 0*y[i] return this; } else if (multiplicator
	 * == 1) { // x[i] = x[i] + y[i] for (int row = rows; --row >= 0;) { for
	 * (int i = index, j = otherIndex, column = columns; --column >= 0;) {
	 * elems[i] += otherElems[j]; i += cs; j += ocs; } index += rs;
	 * otherIndex += ors; } } else if (multiplicator == -1) { // x[i] = x[i]
	 * - y[i] for (int row = rows; --row >= 0;) { for (int i = index, j =
	 * otherIndex, column = columns; --column >= 0;) { elems[i] -=
	 * otherElems[j]; i += cs; j += ocs; } index += rs; otherIndex += ors; }
	 * } else { // the general case for (int row = rows; --row >= 0;) { //
	 * x[i] = x[i] + mult*y[i] for (int i = index, j = otherIndex, column =
	 * columns; --column >= 0;) { elems[i] += multiplicator * otherElems[j];
	 * i += cs; j += ocs; } index += rs; otherIndex += ors; } } } else { //
	 * the general case x[i] = f(x[i],y[i]) for (int row = rows; --row >=
	 * 0;) { for (int i = index, j = otherIndex, column = columns; --column
	 * >= 0;) { elems[i] = function.apply(elems[i], otherElems[j]); i += cs;
	 * j += ocs; } index += rs; otherIndex += ors; } } return this;
	 */
    }

    /**
     * Returns the matrix cell value at coordinate <tt>[row,column]</tt>.
     * <p>
     * Provided with invalid parameters this method may return invalid objects
     * without throwing any exception. <b>You should only use this method when
     * you are absolutely sure that the coordinate is within bounds.</b>
     * Precondition (unchecked):
     * <tt>0 &lt;= column &lt; columns() && 0 &lt;= row &lt; rows()</tt>.
     * 
     * @param row
     *            the index of the row-coordinate.
     * @param column
     *            the index of the column-coordinate.
     * @return the value at the specified coordinate.
     */
    public double getQuick(int row, int column) {
	return elements[row][column];
    }

    /**
     * Returns <tt>true</tt> if both matrices share common cells. More formally,
     * returns <tt>true</tt> if <tt>other != null</tt> and at least one of the
     * following conditions is met
     * <ul>
     * <li>the receiver is a view of the other matrix
     * <li>the other matrix is a view of the receiver
     * <li><tt>this == other</tt>
     * </ul>
     */
    protected boolean haveSharedCellsRaw(DoubleMatrix2D other) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");

	/*
	 * if (other instanceof SelectedDenseDoubleMatrix2D) {
	 * SelectedDenseDoubleMatrix2D otherMatrix =
	 * (SelectedDenseDoubleMatrix2D) other; return this.elements ==
	 * otherMatrix.elements; } else if (other instanceof
	 * DenseDoubleMatrix2D) { DenseDoubleMatrix2D otherMatrix =
	 * (DenseDoubleMatrix2D) other; return this.elements ==
	 * otherMatrix.elements; } return false;
	 */
    }

    /**
     * Returns the position of the given coordinate within the (virtual or
     * non-virtual) internal 1-dimensional array.
     * 
     * @param row
     *            the index of the row-coordinate.
     * @param column
     *            the index of the column-coordinate.
     */
    protected int index(int row, int column) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");

	// return super.index(row,column);
	// manually inlined for speed:
	// return rowZero + row * rowStride + columnZero + column *
	// columnStride;
    }

    /**
     * Construct and returns a new empty matrix <i>of the same dynamic type</i>
     * as the receiver, having the specified number of rows and columns. For
     * example, if the receiver is an instance of type
     * <tt>DenseDoubleMatrix2D</tt> the new matrix must also be of type
     * <tt>DenseDoubleMatrix2D</tt>, if the receiver is an instance of type
     * <tt>SparseDoubleMatrix2D</tt> the new matrix must also be of type
     * <tt>SparseDoubleMatrix2D</tt>, etc. In general, the new matrix should
     * have internal parametrization as similar as possible.
     * 
     * @param rows
     *            the number of rows the matrix shall have.
     * @param columns
     *            the number of columns the matrix shall have.
     * @return a new empty matrix of the same dynamic type.
     */
    public DoubleMatrix2D like(int rows, int columns) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
	/*
	 * return new DenseDoubleMatrix2D(rows, columns);
	 */
    }

    /**
     * Construct and returns a new 1-d matrix <i>of the corresponding dynamic
     * type</i>, entirelly independent of the receiver. For example, if the
     * receiver is an instance of type <tt>DenseDoubleMatrix2D</tt> the new
     * matrix must be of type <tt>DenseDoubleMatrix1D</tt>, if the receiver is
     * an instance of type <tt>SparseDoubleMatrix2D</tt> the new matrix must be
     * of type <tt>SparseDoubleMatrix1D</tt>, etc.
     * 
     * @param size
     *            the number of cells the matrix shall have.
     * @return a new matrix of the corresponding dynamic type.
     */
    public DoubleMatrix1D like1D(int size) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");

	/*
	 * return new DenseDoubleMatrix1D(size);
	 */
    }

    /**
     * Construct and returns a new 1-d matrix <i>of the corresponding dynamic
     * type</i>, sharing the same cells. For example, if the receiver is an
     * instance of type <tt>DenseDoubleMatrix2D</tt> the new matrix must be of
     * type <tt>DenseDoubleMatrix1D</tt>, if the receiver is an instance of type
     * <tt>SparseDoubleMatrix2D</tt> the new matrix must be of type
     * <tt>SparseDoubleMatrix1D</tt>, etc.
     * 
     * @param size
     *            the number of cells the matrix shall have.
     * @param zero
     *            the index of the first element.
     * @param stride
     *            the number of indexes between any two elements, i.e.
     *            <tt>index(i+1)-index(i)</tt>.
     * @return a new matrix of the corresponding dynamic type.
     */
    protected DoubleMatrix1D like1D(int size, int zero, int stride) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");

	/*
	 * return new DenseDoubleMatrix1D(size, this.elements, zero, stride);
	 */
    }

    /**
     * Sets the matrix cell at coordinate <tt>[row,column]</tt> to the specified
     * value.
     * <p>
     * Provided with invalid parameters this method may access illegal indexes
     * without throwing any exception. <b>You should only use this method when
     * you are absolutely sure that the coordinate is within bounds.</b>
     * Precondition (unchecked):
     * <tt>0 &lt;= column &lt; columns() && 0 &lt;= row &lt; rows()</tt>.
     * 
     * @param row
     *            the index of the row-coordinate.
     * @param column
     *            the index of the column-coordinate.
     * @param value
     *            the value to be filled into the specified cell.
     */
    public void setQuick(int row, int column, double value) {
	elements[row][column] = value;
    }

    /**
     * Construct and returns a new selection view.
     * 
     * @param rowOffsets
     *            the offsets of the visible elements.
     * @param columnOffsets
     *            the offsets of the visible elements.
     * @return a new view.
     */
    protected DoubleMatrix2D viewSelectionLike(int[] rowOffsets,
	    int[] columnOffsets) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");

	/*
	 * return new SelectedDenseDoubleMatrix2D(this.elements, rowOffsets,
	 * columnOffsets, 0);
	 */
    }

    /**
     * 8 neighbor stencil transformation. For efficient finite difference
     * operations. Applies a function to a moving <tt>3 x 3</tt> window. Does
     * nothing if <tt>rows() < 3 || columns() < 3</tt>.
     * 
     * <pre>
     *       B[i,j] = function.apply(
     *          A[i-1,j-1], A[i-1,j], A[i-1,j+1],
     *          A[i,  j-1], A[i,  j], A[i,  j+1],
     *          A[i+1,j-1], A[i+1,j], A[i+1,j+1]
     *          )
     *  
     *       x x x -     - x x x     - - - - 
     *       x o x -     - x o x     - - - - 
     *       x x x -     - x x x ... - x x x 
     *       - - - -     - - - -     - x o x 
     *       - - - -     - - - -     - x x x
     * </pre>
     * 
     * Make sure that cells of <tt>this</tt> and <tt>B</tt> do not overlap. In
     * case of overlapping views, behaviour is unspecified.
     * 
     * </pre>
     * 
     * <p>
     * <b>Example:</b>
     * 
     * <pre>
     * 
     * final double alpha = 0.25; final double beta = 0.75; // 8 neighbors
     * cern.colt.function.Double9Function f = new cern.colt.function.Double9Function() {
     * &nbsp;&nbsp;&nbsp;public final double apply( &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;double a00,
     * double a01, double a02, &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;double a10, double a11, double
     * a12, &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;double a20, double a21, double a22) {
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return beta*a11 + alpha*(a00+a01+a02 +
     * a10+a12 + a20+a21+a22); &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;} }; A.zAssign8Neighbors(B,f); //
     * 4 neighbors cern.colt.function.Double9Function g = new cern.colt.function.Double9Function() {
     * &nbsp;&nbsp;&nbsp;public final double apply( &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;double a00,
     * double a01, double a02, &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;double a10, double a11, double
     * a12, &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;double a20, double a21, double a22) {
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return beta*a11 + alpha*(a01+a10+a12+a21);
     * &nbsp;&nbsp;&nbsp;} C.zAssign8Neighbors(B,g); // fast, even though it doesn't look like it };
     * 
     * </pre>
     * 
     * @param B
     *            the matrix to hold the results.
     * @param function
     *            the function to be applied to the 9 cells.
     * @throws NullPointerException
     *             if <tt>function==null</tt>.
     * @throws IllegalArgumentException
     *             if <tt>rows() != B.rows() || columns() != B.columns()</tt>.
     */
    public void zAssign8Neighbors(DoubleMatrix2D B,
	    cern.colt.function.Double9Function function) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");

	/*
	 * // 1. using only 4-5 out of the 9 cells in "function" is *not* the
	 * limiting factor for performance. // 2. if the "function" would be
	 * hardwired into the innermost loop, a speedup of 1.5-2.0 would be seen
	 * // but then the multi-purpose interface is gone... if (!(B instanceof
	 * DenseDoubleMatrix2D)) { super.zAssign8Neighbors(B, function); return;
	 * } if (function == null) throw new
	 * NullPointerException("function must not be null."); checkShape(B);
	 * int r = rows - 1; int c = columns - 1; if (rows < 3 || columns < 3)
	 * return; // nothing to do DenseDoubleMatrix2D BB =
	 * (DenseDoubleMatrix2D) B; int A_rs = rowStride; int B_rs =
	 * BB.rowStride; int A_cs = columnStride; int B_cs = BB.columnStride;
	 * double[] elems = this.elements; double[] B_elems = BB.elements; if
	 * (elems == null || B_elems == null) throw new InternalError(); int
	 * A_index = index(1, 1); int B_index = BB.index(1, 1); for (int i = 1;
	 * i < r; i++) { double a00, a01, a02; double a10, a11, a12; double a20,
	 * a21, a22; int B11 = B_index; int A02 = A_index - A_rs - A_cs; int A12
	 * = A02 + A_rs; int A22 = A12 + A_rs; // in each step six cells can be
	 * remembered in registers - they don't need to be reread from slow
	 * memory a00 = elems[A02]; A02 += A_cs; a01 = elems[A02]; // A02+=A_cs;
	 * a10 = elems[A12]; A12 += A_cs; a11 = elems[A12]; // A12+=A_cs; a20 =
	 * elems[A22]; A22 += A_cs; a21 = elems[A22]; // A22+=A_cs; for (int j =
	 * 1; j < c; j++) { // in each step 3 instead of 9 cells need to be read
	 * from memory. a02 = elems[A02 += A_cs]; a12 = elems[A12 += A_cs]; a22
	 * = elems[A22 += A_cs]; B_elems[B11] = function.apply(a00, a01, a02,
	 * a10, a11, a12, a20, a21, a22); B11 += B_cs; // move remembered cells
	 * a00 = a01; a01 = a02; a10 = a11; a11 = a12; a20 = a21; a21 = a22; }
	 * A_index += A_rs; B_index += B_rs; }
	 */

    }

    public DoubleMatrix1D zMult(DoubleMatrix1D y, DoubleMatrix1D z,
	    double alpha, double beta, boolean transposeA) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");

	/*
	 * if (transposeA) return viewDice().zMult(y, z, alpha, beta, false); if
	 * (z == null) z = new DenseDoubleMatrix1D(this.rows); if (!(y
	 * instanceof DenseDoubleMatrix1D && z instanceof DenseDoubleMatrix1D))
	 * return super.zMult(y, z, alpha, beta, transposeA); if (columns !=
	 * y.size || rows > z.size) throw new
	 * IllegalArgumentException("Incompatible args: " + toStringShort() +
	 * ", " + y.toStringShort() + ", " + z.toStringShort());
	 * DenseDoubleMatrix1D yy = (DenseDoubleMatrix1D) y; DenseDoubleMatrix1D
	 * zz = (DenseDoubleMatrix1D) z; final double[] AElems = this.elements;
	 * final double[] yElems = yy.elements; final double[] zElems =
	 * zz.elements; if (AElems == null || yElems == null || zElems == null)
	 * throw new InternalError(); int As = this.columnStride; int ys =
	 * yy.stride; int zs = zz.stride; int indexA = index(0, 0); int indexY =
	 * yy.index(0); int indexZ = zz.index(0); int cols = columns; for (int
	 * row = rows; --row >= 0;) { double sum = 0; // // not loop unrolled
	 * for (int i=indexA, j=indexY, column=columns; --column >= 0; ) { sum
	 * += AElems[i] * yElems[j]; i += As; j += ys; } // // loop unrolled int
	 * i = indexA - As; int j = indexY - ys; for (int k = cols % 4; --k >=
	 * 0;) { sum += AElems[i += As] * yElems[j += ys]; } for (int k = cols /
	 * 4; --k >= 0;) { sum += AElems[i += As] * yElems[j += ys] + AElems[i
	 * += As] * yElems[j += ys] + AElems[i += As] yElems[j += ys] + AElems[i
	 * += As] * yElems[j += ys]; } zElems[indexZ] = alpha * sum + beta *
	 * zElems[indexZ]; indexA += this.rowStride; indexZ += zs; } return z;
	 */
    }

    public DoubleMatrix2D zMult(DoubleMatrix2D B, DoubleMatrix2D C,
	    double alpha, double beta, boolean transposeA, boolean transposeB) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");

	/*
	 * // overriden for performance only if (transposeA) return
	 * viewDice().zMult(B, C, alpha, beta, false, transposeB); if (B
	 * instanceof SparseDoubleMatrix2D || B instanceof RCDoubleMatrix2D) {
	 * // exploit quick sparse mult // A*B = (B' * A')' if (C == null) {
	 * return B.zMult(this, null, alpha, beta, !transposeB,
	 * true).viewDice(); } else { B.zMult(this, C.viewDice(), alpha, beta,
	 * !transposeB, true); return C; } // final RCDoubleMatrix2D transB =
	 * new RCDoubleMatrix2D(B.columns,B.rows); B.forEachNonZero( new
	 * cern.colt.function.IntIntDoubleFunction() { public double apply(int
	 * i, int j, double value) { transB.setQuick(j,i,value); return value; }
	 * } ); return transB.zMult(this.viewDice(),C.viewDice()).viewDice(); //
	 * } if (transposeB) return this.zMult(B.viewDice(), C, alpha, beta,
	 * transposeA, false); int m = rows; int n = columns; int p = B.columns;
	 * if (C == null) C = new DenseDoubleMatrix2D(m, p); if (!(C instanceof
	 * DenseDoubleMatrix2D)) return super.zMult(B, C, alpha, beta,
	 * transposeA, transposeB); if (B.rows != n) throw new
	 * IllegalArgumentException("Matrix2D inner dimensions must
	 * agree:" + toStringShort() + ", " + B.toStringShort()); if (C.rows !=
	 * m || C.columns != p) throw new
	 * IllegalArgumentException("Incompatibel result matrix: " +
	 * toStringShort() + ", " + B.toStringShort() + ", " +
	 * C.toStringShort()); if (this == C || B == C) throw new
	 * IllegalArgumentException("Matrices must not be identical");
	 * DenseDoubleMatrix2D BB = (DenseDoubleMatrix2D) B; DenseDoubleMatrix2D
	 * CC = (DenseDoubleMatrix2D) C; final double[] AElems = this.elements;
	 * final double[] BElems = BB.elements; final double[] CElems =
	 * CC.elements; if (AElems == null || BElems == null || CElems == null)
	 * throw new InternalError(); int cA = this.columnStride; int cB =
	 * BB.columnStride; int cC = CC.columnStride; int rA = this.rowStride;
	 * int rB = BB.rowStride; int rC = CC.rowStride; // A is blocked to hide
	 * memory latency xxxxxxx B xxxxxxx xxxxxxx A xxx xxxxxxx C xxx xxxxxxx
	 * --- ------- xxx xxxxxxx xxx xxxxxxx --- ------- xxx xxxxxxx // final
	 * int BLOCK_SIZE = 30000; // * 8 == Level 2 cache in bytes //if (n+p ==
	 * 0) return C; //int m_optimal = (BLOCK_SIZE - n*p) / (n+p); int
	 * m_optimal = (BLOCK_SIZE - n) / (n + 1); if (m_optimal <= 0) m_optimal
	 * = 1; int blocks = m / m_optimal; int rr = 0; if (m % m_optimal != 0)
	 * blocks++; for (; --blocks >= 0;) { int jB = BB.index(0, 0); int
	 * indexA = index(rr, 0); int jC = CC.index(rr, 0); rr += m_optimal; if
	 * (blocks == 0) m_optimal += m - rr; for (int j = p; --j >= 0;) { int
	 * iA = indexA; int iC = jC; for (int i = m_optimal; --i >= 0;) { int kA
	 * = iA; int kB = jB; double s = 0; // // not unrolled: for (int k = n;
	 * --k >= 0; ) { //s += getQuick(i,k) * B.getQuick(k,j); s += AElems[kA]
	 * * BElems[kB]; kB += rB; kA += cA; } // // loop unrolled kA -= cA; kB
	 * -= rB; for (int k = n % 4; --k >= 0;) { s += AElems[kA += cA] *
	 * BElems[kB += rB]; } for (int k = n / 4; --k >= 0;) { s += AElems[kA
	 * += cA] * BElems[kB += rB] + AElems[kA += cA] * BElems[kB += rB] +
	 * AElems[kA += cA] * BElems[kB += rB] + AElems[kA += cA] * BElems[kB +=
	 * rB]; } CElems[iC] = alpha * s + beta * CElems[iC]; iA += rA; iC +=
	 * rC; } jB += cB; jC += cC; } } return C;
	 */
    }

    /**
     * Returns the sum of all cells; <tt>Sum( x[i,j] )</tt>.
     * 
     * @return the sum.
     */
    public double zSum() {
	double sum = 0;
	for (double[] row : elements)
	    for (double cell : row)
		sum += cell;

	return sum;
    }

    public void ensureCapacity(int minNonZeros) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public void trimToSize() {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public void checkShape(AbstractMatrix2D B) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public void checkShape(AbstractMatrix2D B, AbstractMatrix2D C) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public String toStringShort() {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public double aggregate(DoubleDoubleFunction aggr, DoubleFunction f) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public double aggregate(DoubleMatrix2D other, DoubleDoubleFunction aggr,
	    DoubleDoubleFunction f) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public int cardinality() {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public DoubleMatrix2D copy() {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public boolean equals(double value) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public boolean equals(Object obj) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public DoubleMatrix2D forEachNonZero(IntIntDoubleFunction function) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public double get(int row, int column) {
	return elements[row][column];
    }

    public void getNonZeros(IntArrayList rowList, IntArrayList columnList,
	    DoubleArrayList valueList) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public DoubleMatrix2D like() {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public void set(int row, int column, double value) {
	elements[row][column] = value;
    }

    public double[][] toArray() {
	return elements;
    }

    public String toString() {
	StringBuffer result = new StringBuffer();

	result.append(this.rows() + " x " + this.columns()
		+ " large 2-d dense double matrix\n");
	for (int row = 0; row < this.rows(); row++) {
	    for (int col = 0; col < this.columns(); col++)
		result.append(this.get(row, col) + "\t");
	    result.append("\n");
	}

	return result.toString();

    }

    public DoubleMatrix1D viewColumn(int column) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public DoubleMatrix2D viewColumnFlip() {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public DoubleMatrix2D viewDice() {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public DoubleMatrix2D viewPart(int row, int column, int height, int width) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public DoubleMatrix1D viewRow(int row) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public DoubleMatrix2D viewRowFlip() {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public DoubleMatrix2D viewSelection(DoubleMatrix1DProcedure condition) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public DoubleMatrix2D viewSelection(int[] rowIndexes, int[] columnIndexes) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public DoubleMatrix2D viewSorted(int column) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public DoubleMatrix2D viewStrides(int rowStride, int columnStride) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public DoubleMatrix1D zMult(DoubleMatrix1D y, DoubleMatrix1D z) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public DoubleMatrix2D zMult(DoubleMatrix2D B, DoubleMatrix2D C) {
	throw new UnsupportedOperationException(
		"Unsupported operation in LargeDenseDoubleMatrix2D.java");
    }

    public int columns() {
	return this.elements[0].length;
    }

    public int rows() {
	return this.elements.length;
    }

    public int size() {
	return rows() * columns();
    }

    /**
     * Create a {@link DoubleMatrix2D} of the given size. If the size is huge,
     * create a {@link LargeDenseDoubleMatrix2D}, otherwise create a
     * {@link DenseDoubleMatrix2D}.
     * 
     * @param row
     * @param column
     * @return
     */
    static public DoubleMatrix2D createDoubleMatrix2D(int row, int column) {
	if (((long) row) * ((long) column) <= sizeGate)
	    return new DenseDoubleMatrix2D(row, column);
	else
	    return new LargeDenseDoubleMatrix2D(row, column);
    }

    static public boolean isLarge(int row, int column) {
	return ((long) row) * ((long) column) > sizeGate;
    }

    /**
     * return the matrix production of two matrix.
     * 
     * @param a
     * @param aTrans
     *            whether a's transposition should be used
     * @param b
     * @param bTrans
     *            whether b's transposition should be used
     * @return
     */
    static public DoubleMatrix2D mult(DoubleMatrix2D a, boolean aTrans,
	    DoubleMatrix2D b, boolean bTrans) {
	final int row = aTrans ? a.columns() : a.rows();
	final int col = bTrans ? b.rows() : b.columns();

	if (!(a instanceof LargeDenseDoubleMatrix2D)
		&& !(b instanceof LargeDenseDoubleMatrix2D)) {
	    Algebra alg = new Algebra();
	    return alg.mult((aTrans) ? a.viewDice() : a,
		    (bTrans) ? b.viewDice() : b);
	}

	if ((aTrans ? a.rows() : a.columns()) != (bTrans ? b.columns() : b
		.rows()))
	    throw new IllegalArgumentException(
		    "the dimensions of the two matrix are not match!");

	DoubleMatrix2D result = createDoubleMatrix2D(row, col);
	final int sumNum = aTrans ? a.rows() : a.columns();
	double temp;

	if (aTrans && bTrans) {
	    for (int i = 0; i < row; i++)
		for (int j = 0; j < col; j++) {
		    temp = 0;
		    for (int k = 0; k < sumNum; k++)
			temp += a.get(k, i) * b.get(j, k);
		    result.set(i, j, temp);
		}
	} else if (aTrans && !bTrans) {
	    for (int i = 0; i < row; i++)
		for (int j = 0; j < col; j++) {
		    temp = 0;
		    for (int k = 0; k < sumNum; k++)
			temp += a.get(k, i) * b.get(k, j);
		    result.set(i, j, temp);
		}
	} else if (!aTrans && bTrans) {
	    for (int i = 0; i < row; i++)
		for (int j = 0; j < col; j++) {
		    temp = 0;
		    for (int k = 0; k < sumNum; k++)
			temp += a.get(i, k) * b.get(j, k);
		    result.set(i, j, temp);
		}
	} else if (!aTrans && !bTrans) {
	    for (int i = 0; i < row; i++)
		for (int j = 0; j < col; j++) {
		    temp = 0;
		    for (int k = 0; k < sumNum; k++)
			temp += a.get(i, k) * b.get(k, j);
		    result.set(i, j, temp);
		}
	}

	return result;

    }

    /**
     * Constructs and returns the covariance matrix of the given matrix.
     * (covariance between columns of the given matrix) The covariance matrix is
     * a square, symmetric matrix consisting of nothing but covariance
     * coefficients. The rows and the columns represent the variables, the cells
     * represent covariance coefficients. The diagonal cells (i.e. the
     * covariance between a variable and itself) will equal the variances. The
     * covariance of two column vectors x and y is given by
     * <tt>cov(x,y) = (1/n) * Sum((x[i]-mean(x)) * (y[i]-mean(y)))</tt>. See the
     * <A HREF="http://www.cquest.utoronto.ca/geog/ggr270y/notes/not05efg.html">
     * math definition</A>. Compares two column vectors at a time. Use dice
     * views to compare two row vectors at a time.
     * 
     * @param matrix
     *            any matrix; a column holds the values of a given variable.
     * @param trans
     *            whether the matrix's transposition should be used instead of
     *            the matrix itself
     * @return the covariance matrix (<tt>n x n, n=matrix.columns</tt>).
     */
    static public DoubleMatrix2D covariance(DoubleMatrix2D matrix, boolean trans) {
	if (!(matrix instanceof LargeDenseDoubleMatrix2D))
	    return Statistic.covariance(trans ? matrix.viewDice() : matrix);

	final int row = trans ? matrix.columns() : matrix.rows();
	final int col = trans ? matrix.rows() : matrix.columns();

	DenseDoubleMatrix2D cov = new DenseDoubleMatrix2D(col, col);

	// compute row sum
	double[] colSum = new double[col];

	if (!trans) {
	    for (int i = 0; i < col; i++) {
		colSum[i] = matrix.get(0, i);
		for (int j = 1; j < row; j++)
		    colSum[i] += matrix.get(j, i);
	    }
	} else {
	    for (int i = 0; i < col; i++) {
		colSum[i] = matrix.get(i, 0);
		for (int j = 1; j < row; j++)
		    colSum[i] += matrix.get(i, j);
	    }
	}

	// System.out.println("cov");
	// compute covariance
	double temp = 0;
	if (trans) {
	    for (int c1 = 0; c1 < col; c1++)
		for (int c2 = c1; c2 < col; c2++) {
		    temp = 0;
		    for (int i = 0; i < row; i++)
			temp += matrix.get(c1, i) * matrix.get(c2, i);
		    cov.set(c1, c2, (temp - colSum[c1] * colSum[c2] / row)
			    / row);
		    cov.set(c2, c1, cov.get(c1, c2));
		}

	} else {
	    for (int c1 = 0; c1 < col; c1++)
		for (int c2 = c1; c2 < col; c2++) {
		    temp = 0;
		    for (int i = 0; i < row; i++)
			temp += matrix.get(i, c1) * matrix.get(i, c2);
		    cov.set(c1, c2, (temp - colSum[c1] * colSum[c2] / row)
			    / row);
		    cov.set(c2, c1, cov.get(c1, c2));
		}

	}

	return cov;
    }

    /**
     * Constructs and returns the distance matrix of the given matrix. The
     * distance matrix is a square, symmetric matrix consisting of nothing but
     * distance coefficients. The rows and the columns represent the variables,
     * the cells represent distance coefficients. The diagonal cells (i.e. the
     * distance between a variable and itself) will be zero. Compares two column
     * vectors at a time. Use dice views to compare two row vectors at a time.
     * Note: currently, for LargeDenseDoubleMatrix2D, only support euclidean
     * distance
     * 
     * @param matrix
     *            any matrix; a column holds the values of a given variable
     *            (vector).
     * @param trans
     *            whether the input matrix should be transposed
     * @param distanceFunction
     *            (EUCLID, CANBERRA, ..., or any user defined distance function
     *            operating on two vectors).
     * @return the distance matrix (<tt>n x n, n=matrix.columns</tt>).
     */
    public static DoubleMatrix2D distance(DoubleMatrix2D matrix, boolean trans,
	    VectorVectorFunction distanceFunction) {
	int columns = trans ? matrix.rows() : matrix.columns();
	int rows = (!trans) ? matrix.rows() : matrix.columns();

	if (!(matrix instanceof LargeDenseDoubleMatrix2D)
		&& isLarge(columns, columns)) {
	    if (trans)
		return Statistic.distance(matrix.viewDice(), distanceFunction);
	    else
		return Statistic.distance(matrix, distanceFunction);
	}

	if (distanceFunction != Statistic.EUCLID)
	    throw new UnsupportedOperationException(
		    distanceFunction
			    + " distance for LargeDenseDoubleMatrix2D is not supported!");

	DoubleMatrix2D distance = LargeDenseDoubleMatrix2D
		.createDoubleMatrix2D(columns, columns);

	// System.out.println("distance");
	double temp = 0;
	if (!trans) {
	    for (int row = 0; row < columns; row++) {
		distance.set(row, row, 0);
		for (int col = 1; col < row; col++) {
		    temp = 0;
		    for (int i = 0; i < rows; i++)
			temp += (matrix.get(i, row) - matrix.get(i, col))
				* (matrix.get(i, row) - matrix.get(i, col));
		    distance.set(row, col, Math.sqrt(temp));
		    distance.set(col, row, distance.get(row, col));
		}
	    }
	} else {
	    for (int row = 0; row < columns; row++) {
		distance.set(row, row, 0);
		for (int col = 0; col < row; col++) {
		    temp = 0;
		    for (int i = 0; i < rows; i++)
			temp += (matrix.get(row, i) - matrix.get(col, i))
				* (matrix.get(row, i) - matrix.get(col, i));
		    distance.set(row, col, Math.sqrt(temp));
		    distance.set(col, row, distance.get(row, col));
		}
	    }
	}

	return distance;
    }
}
