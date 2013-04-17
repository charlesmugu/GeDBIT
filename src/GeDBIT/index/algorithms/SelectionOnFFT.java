package GeDBIT.index.algorithms;

import java.util.List;

//import cern.colt.matrix.DoubleMatrix2D;

import GeDBIT.index.algorithms.Selection;
import GeDBIT.dist.Metric;
import GeDBIT.type.IndexObject;

//import GeDBIT.util.LargeDenseDoubleMatrix2D;

@SuppressWarnings("serial")
public class SelectionOnFFT implements PivotSelectionMethod,
	java.io.Serializable {
    /*
     * 这个就是照着PCAOnFFT做的界面。 传的参数要完全符合预先设定的值，包括大小写，否则会出错
     * 我感觉这样传参会比较麻烦，先这样吧，再想想有没有什么更好的方法 我给这个类加了3个变量：testKind 用什么标准（现在用的是F-test）
     * yMethod y怎么求得 selectAlgorithm 用的是forward还是backward
     * 
     * testKind:在传参的时候，如果用F-test，就给testKind传"ftest"
     * yMethod:如果用平均值，就给yMethod传"average"。如果是标准差"standard"
     * selectAlgorithm:如果用forward selection,就传"forward"
     */
    private int FFTScale;
    private String testKind;
    private String yMethod;
    private String selectAlgorithm;

    public SelectionOnFFT(int scale, String kind, String method, String alog) {
	FFTScale = scale;
	testKind = kind;
	yMethod = method;
	selectAlgorithm = alog;
    }

    public SelectionOnFFT(int scale)
    // 这个构造函数是作为备用的
    {
	FFTScale = scale;
	testKind = "ftest";
	yMethod = "standard";
	selectAlgorithm = "forward";
    }

    public int[] selectPivots(Metric metric, List<? extends IndexObject> data,
	    int numPivots) {
	final int dataSize = data.size();

	// for(int i=0; i<dataSize; i++)
	// System.out.println(data.get(i)+" ");
	if (numPivots >= dataSize) {
	    int[] pivots = new int[dataSize];
	    for (int i = 0; i < dataSize; i++)
		pivots[i] = i;

	    return IncrementalSelection.removeDuplicate(metric, data, pivots);
	}

	// System.out.println("datasize:"+dataSize);
	// run fft to get a candidate set
	int[] fftResult = PivotSelectionMethods.FFT.selectPivots(metric, data,
		numPivots * FFTScale);
	/*
	 * System.out.println("fft length:"+fftResult.length);
	 * 
	 * System.out.println("fft result: "); for(int i=0; i<fftResult.length;
	 * i++) System.out.print(fftResult[i]+" "); System.out.println();
	 */

	// compute the distance matrix
	if (fftResult.length <= Math.min(dataSize, numPivots))
	    return fftResult;

	// get x
	// x要空出x[0]这一列，为了和后面一致。
	// x[0]这一列赋为1的工作就留到各个selection中的init函数来做
	double[][] matrix = new double[dataSize][fftResult.length + 1];
	// col是fft算出来的列
	// row就是行，表示有row个观察值
	for (int col = 0; col < fftResult.length; col++)
	    for (int row = 0; row < dataSize; row++)
		matrix[row][col + 1] = metric.getDistance(data.get(row),
			data.get(fftResult[col]));
	/*
	 * if(dataSize == fftResult.length) {
	 * System.out.println("here comes matrix:"); for(int i=0; i<dataSize;
	 * i++) { for(int j=1; j<=fftResult.length; j++)
	 * System.out.print(matrix[i][j]+" "); System.out.println(); } }
	 * 
	 * System.out.println("fft scale : "+FFTScale);
	 * System.out.println("fft length : "+fftResult.length);
	 * System.out.println("data size : "+dataSize);
	 */
	double[][] y;
	// get y
	if (yMethod.equalsIgnoreCase("average"))
	    y = getYAvg(matrix, dataSize, fftResult.length);
	else if (yMethod.equalsIgnoreCase("standard"))
	    y = getYStand(matrix, dataSize, fftResult.length);
	else
	    throw new IllegalArgumentException("Invalid option " + yMethod);

	// X和Y都得到，下面进行计算
	Selection select = new Selection(matrix, y, numPivots);

	// 设置检测方法。当selectAlgorithm为enumerate的时候就不用了
	if (testKind.equalsIgnoreCase("ftest"))
	    select.setTestSign(1); // 1 代表F检测
	else if (testKind.equalsIgnoreCase("rss"))
	    select.setTestSign(2); // 2 代表用rss
	else
	    throw new IllegalArgumentException("Invalid option " + testKind);

	if (selectAlgorithm.equalsIgnoreCase("enumerate"))
	    select.setTestSign(1);
	int[] result;
	// 选择使用何种算法来进行selection计算
	if (selectAlgorithm.equalsIgnoreCase("forward"))
	    result = select.forwardSelection();
	else if (selectAlgorithm.equalsIgnoreCase("enumerate"))
	    result = select.enumerateSelection();
	else if (selectAlgorithm.equalsIgnoreCase("backward"))
	    result = select.backwardSelection();
	else
	    throw new IllegalArgumentException("Invalid option "
		    + selectAlgorithm);

	/*
	 * System.out.println("result"); for(int i=0; i<result.length; i++)
	 * System.out.print(result[i]+" "); System.out.println();
	 */
	return result;
    }

    double[][] getYAvg(double[][] matrix, int n, int p) {
	double[][] matrixY = new double[n][1];
	// double sum = 0;
	for (int i = 0; i < n; i++) {
	    double sum = 0;
	    // 计算每行的平均值
	    for (int j = 1; j <= p; j++)
		sum += matrix[i][j];
	    matrixY[i][0] = sum / p;
	}
	return matrixY;

    }

    double[][] getYStand(double[][] matrix, int n, int p) {
	double[][] matrixY = new double[n][1];
	/*
	 * //double sum = 0, sSum = 0; for(int i=0; i<n; i++){ double sum = 0,
	 * sSum = 0, avg = 0; //计算每行的标准差 for(int j=1; j<=p; j++){ sum +=
	 * matrix[i][j]; //和的平方 sSum += matrix[i][j]*matrix[i][j]; } //均值的平方 avg
	 * = sum/p; avg = avg * avg; //平方的均值 sSum = sSum / p; //相减，开根
	 * matrixY[i][0] = Math.sqrt(sSum - sum); }
	 */
	for (int i = 0; i < n; i++)
	// 计算n行的标准差
	{
	    double sum = 0, avg = 0, sSum = 0;
	    for (int j = 1; j <= p; j++)
		sum += matrix[i][j];
	    avg = sum / p;
	    for (int j = 1; j <= p; j++)
		sSum += (matrix[i][j] - avg) * (matrix[i][j] - avg);
	    matrixY[i][0] = Math.sqrt(sSum);
	}
	/*
	 * System.out.println("y matrix:"); for(int i=0; i<n; i++) {
	 * System.out.println(matrixY[i][0]); }
	 */
	return matrixY;
    }

    public int[] selectPivots(Metric metric, List<? extends IndexObject> data,
	    int first, int dataSize, int numPivots) {
	int[] result = selectPivots(metric, data.subList(first, dataSize),
		numPivots);
	for (int i = 0; i < result.length; i++)
	    result[i] += first;

	return result;
    }
}