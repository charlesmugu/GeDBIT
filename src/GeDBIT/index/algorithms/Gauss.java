package GeDBIT.index.algorithms;

public class Gauss {
    double gauss_matrix[][];
    float solution_float_vector[];
    long solution_int_vector[];
    double solution_double_vector[];
    double sort_result[];
    int rank[];
    int row, col;

    // construction function

    public Gauss(double[][] m_x, int r) {
	// gauss_matrix = m_x;
	row = r;
	col = row;
	gauss_matrix = new double[row][row];
	for (int i = 0; i < row; i++)
	    for (int j = 0; j < row; j++)
		gauss_matrix[i][j] = m_x[row - i - 1][j];
	solution_float_vector = new float[row];
	solution_int_vector = new long[row];
	solution_double_vector = new double[row];
	sort_result = new double[row];
	rank = new int[row];
    }

    public int[] getSolution(int numPivot) {
	int result[] = new int[numPivot];
	for (int i = 0; i < numPivot; i++) {
	    result[i] = rank[i];
	}
	return result;
    }

    public void printSolutionMatrix() {
	for (int i = 0; i < row; i++) {
	    for (int j = 0; j < col; j++)
		System.out.print(gauss_matrix[i][j] + " ");
	    System.out.println();
	}
    }

    public void printSolutionBiggerThan(float t) {
	for (int i = 0; i < row; i++) {
	    if (solution_float_vector[i] > t)
		System.out.printf("%.4f\n", solution_float_vector[i]);
	}
    }

    public void printSolutionVectorDouble() {
	for (int i = 0; i < row; i++) {
	    System.out.printf("%.4f\n", solution_double_vector[i]);
	}
    }

    public void printSolutionVectorFloat() {
	for (int i = 0; i < row; i++) {
	    System.out.println(solution_float_vector[i]);
	}
    }

    public void printSolutionVectorInt() {
	for (int i = 0; i < row; i++) {
	    System.out.println(solution_int_vector[i]);
	}
    }

    public int sort() {
	int n = row;
	double temp = 0;
	int t = 0;
	for (int i = 0; i < n; i++) {
	    sort_result[i] = solution_double_vector[i];
	    rank[i] = i;
	}
	for (int i = 0; i < n; i++)
	    for (int j = i + 1; j < n; j++)
		if (sort_result[i] < sort_result[j]) {
		    temp = sort_result[i];
		    sort_result[i] = sort_result[j];
		    sort_result[j] = temp;
		    // rank[i] here means the original number of ith num.
		    t = rank[i];
		    rank[i] = rank[j];
		    rank[j] = t;
		}
	return 0;
    }

    public void calSolution() {
	// printSolutionMatrix();
	// Forward
	for (int i = 0; i < row; i++) {
	    // deal with the problem of multiple by 0
	    double pivot = gauss_matrix[i][i];
	    if (pivot != 0)
		for (int j = i + 1; j < row; j++) {
		    // count the times of the a[j][i] and a[i][i]
		    double multiple = gauss_matrix[j][i] / pivot;
		    for (int k = i; k < col; k++) {
			double temp = gauss_matrix[i][k] * multiple;
			gauss_matrix[j][k] -= temp;
		    }
		    // handle gauss_matrix[j][i], pivot
		}
	}

	// Backward
	for (int i = row - 1; i >= 0; i--) {
	    double pivot = gauss_matrix[i][i];
	    if (pivot != 0)
		for (int j = i - 1; j >= 0; j--) {
		    double multiple = gauss_matrix[j][i] / pivot;
		    for (int k = i; k >= 0; k--) {
			double temp = gauss_matrix[i][k] * multiple;
			gauss_matrix[j][k] -= temp;
		    }
		}
	}

	for (int i = 0; i < row; i++) {
	    solution_double_vector[i] = gauss_matrix[i][i];
	    if (solution_double_vector[i] < 0)
		solution_double_vector[i] = -solution_double_vector[i];
	    Double temp_double = gauss_matrix[i][i];
	    Float temp_float = temp_double.floatValue();
	    solution_float_vector[i] = temp_float;
	    solution_int_vector[i] = temp_float.longValue();
	}

	sort();
    }

    /*
     * public Gauss(double[][] m_x, int r) { row = r; col = row+1; gauss_matrix
     * = new double[row][row+1]; for(int i=0; i<row; i++) //for every rows {
     * double sum = 0; for(int j=0; j<row; j++) { gauss_matrix[i][j] =
     * m_x[row-i-1][j]; sum+=gauss_matrix[i][j]; } gauss_matrix[i][col-1] = sum;
     * } solution_float_vector = new float[row]; solution_int_vector = new
     * long[row]; solution_double_vector = new double[row]; }
     */
    /*
     * public void calSolution() { for(int i=0; i<row; i++) { double pivot =
     * gauss_matrix[i][i]; if(pivot!=0) for(int j=i+1; j<row; j++) { //count the
     * times of the a[j][i] and a[i][i] double multiple = gauss_matrix[j][i] /
     * pivot; for(int k=i; k<col; k++) { double temp = gauss_matrix[i][k]*
     * multiple; gauss_matrix[j][k] -= temp; } //handle gauss_matrix[j][i],
     * pivot } }
     * 
     * for(int i = row-1; i>=0; i--) { double temp = 0; for(int j=i+1; j<row;
     * j++) temp+=gauss_matrix[i][j]*solution_double_vector[j]; temp -=
     * gauss_matrix[i][col-1]; solution_double_vector[i] =
     * (-temp)/gauss_matrix[i][i]; }
     * 
     * for(int i=0; i<row; i++) { if(solution_double_vector[i]<0)
     * solution_double_vector[i] = -solution_double_vector[i];
     * 
     * Double temp_double = solution_double_vector[i]; solution_float_vector[i]
     * = temp_double.floatValue(); Float temp_float = solution_float_vector[i];
     * solution_int_vector[i] = temp_float.longValue(); } }
     */
}