/**
 * GeDBIT.util.MiscellaneousTool 2007.07.23
 *
 * Copyright Information:
 *
 * Change Log:
 * 2007.07.23: Created, by Rui Mao
 */
package GeDBIT.util;

import java.io.*;

/**
 * 
 * @author Rui Mao
 * 
 */
public class MiscellaneousTool {

    /**
     * @param args
     */
    public static void main(String[] args) {
	// System.out.println(", , , ".split(",").length);
	// return;
	try {
	    // multiQueryResultFFTScale();
	    // multiQueryResultNumPivot();
	    // condor();
	    // incCondor();
	    // fftscaleQueryCondor();
	    incQueryCondor();
	    // fftscaleBuildCondor();
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    @SuppressWarnings("unused")
    public static void cleanSingleQueryResult() throws Exception {
	String name1 = "\\\\.PSF\\sharedfolder\\rmao\\workspace\\result\\fftscale\\dim14\\uniform-dim14-100k-3-3-100-pca-clustering-scale";
	String name2 = "\\uniform-dim4-100k-";
	String name3 = "-3-100-pca-clustering-scale20-q";
	String name4 = ".query";
	String name5 = ".build";
	BufferedWriter bWriter = new BufferedWriter(
		new FileWriter(
			"\\\\.PSF\\sharedfolder\\rmao\\workspace\\result\\dim14-build-1stround.txt"));
	BufferedWriter qWriter = new BufferedWriter(
		new FileWriter(
			"\\\\.PSF\\sharedfolder\\rmao\\workspace\\result\\dim14-query-1stround.txt"));
	BufferedWriter cbWriter = new BufferedWriter(
		new FileWriter(
			"\\\\.PSF\\sharedfolder\\rmao\\workspace\\result\\dim14-build-2ndround.condor"));
	BufferedWriter cqWriter = new BufferedWriter(
		new FileWriter(
			"\\\\.PSF\\sharedfolder\\rmao\\workspace\\result\\dim14-query-2ndround.condor"));

	for (int i = 0; i <= 99; i++) // fftscale = 1,..., 100
	{
	    BufferedReader reader = new BufferedReader(new FileReader(name1
		    + Integer.toString(i) + name4));
	    String line = null;
	    for (int j = 0; j < 4; j++) {
		line = reader.readLine();
		if (line == null) // task i is not completed
		{
		    cbWriter.write(buildCondor(i + 1)); // output build condor
							// command
		    cqWriter.write(queryCondor(i + 1)); // output query condor
							// command
		    break;
		}
	    }
	    if (line == null)
		continue;

	    String[] qResult = new String[8];
	    boolean completed = true;
	    for (int j = 0; j < 8; j++) {
		qResult[j] = reader.readLine();
		if (qResult[j] == null || !qResult[j].startsWith("100000,")) {
		    completed = false;
		    cbWriter.write(buildCondor(i + 1)); // output build condor
							// command
		    cqWriter.write(queryCondor(i + 1)); // output query condor
							// command
		    break;
		}
	    }

	    if (!completed)
		continue;

	    // completed, output build result
	    BufferedReader bReader = new BufferedReader(new FileReader(name1
		    + Integer.toString(i) + name5));
	    bWriter.write(bReader.readLine() + "\n");
	    bReader.close();

	    // output query result
	    for (int j = 0; j < 8; j++)
		qWriter.write((i + 1) + ", " + qResult[j] + "\n");
	    reader.close();

	}

	bWriter.close();
	qWriter.close();
	cbWriter.close();
	cqWriter.close();
    }

    private static String buildCondor(int offset) {
	StringBuffer sb = new StringBuffer();
	sb.append("Arguments  = -Xmx1500m -cp .:../jdb2/lib/colt.jar:../jdb2/src GeDBIT.app.BuildVPIndex -n uniformvector-20dim-100k.txt ");
	sb.append("-psm pcaonfft -v 3 -dpm clusteringkmeans -f 3 -m 100 -t vector -g OFF -dim 14 -b 1 -r 4 -i 100000 -a 1000000 -s 100000000  ");
	sb.append("-fftscale "
		+ offset
		+ " -o ../result/db/uni14/uniform-dim14-100k-3-3-100-pca-clustering-scale"
		+ offset + "\n");
	sb.append("Error = ../result/uni14/uniform-dim14-100k-3-3-100-pca-clustering-scale"
		+ offset
		+ ".err\n"
		+ "Output = ../result/uni14/uniform-dim14-100k-3-3-100-pca-clustering-scale"
		+ offset
		+ ".build\n"
		+ "Log = ../result/uni14/uniform-dim14-100k-3-3-100-pca-clustering-scale"
		+ offset
		+ ".log\n"
		+ "nice_user       = False\n"
		+ "notification    = always\n"
		+ "notify_user     = rmao@cs.utexas.edu\n"
		+ "requirements    = Memory >= 2000 && InMastodon && OpSys == \"LINUX\"\n"
		+ "Rank = Memory >= 4000\n" + "Queue\n\n");

	return sb.toString();
    }

    private static String queryCondor(int offset) {
	return "Arguments  = -Xmx1800m -cp .:../jdb2/lib/colt.jar:../jdb2/src GeDBIT.app.QueryVPIndex "
		+ "-d ../result/db/uni14/uniform-dim14-100k-3-3-100-pca-clustering-scale"
		+ offset
		+ "-100000 -q uniformvector-20dim-100k.txt -f 0 -l 999 -i 0.4 -a 0.75 -s 0.05 -t vector -p 0 -v 0 -g 0 -dim 14 -forprint "
		+ offset
		+ "\n"
		+ "Error = ../result/uni14/uniform-dim14-100k-3-3-100-pca-clustering-scale"
		+ offset
		+ "q1k.err\n"
		+ "Output = ../result/uni14/uniform-dim14-100k-3-3-100-pca-clustering-scale"
		+ offset
		+ "q1k.query\n"
		+ "Log = ../result/uni14/uniform-dim14-100k-3-3-100-pca-clustering-scale"
		+ offset
		+ "q1k.log\n"
		+ "nice_user       = False\n"
		+ "notification    = always\n"
		+ "notify_user     = rmao@cs.utexas.edu\n"
		+ "requirements    = Memory >= 2000 && OpSys == \"LINUX\"\n"
		+ "Rank = Memory >= 4000\n" + "Queue\n\n";
    }

    @SuppressWarnings("unused")
    public static void multiQueryResultNumPivot() throws Exception {
	// int [] placeHolder = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
	// 12, 13, 14, 15, 16, 17, 18, 19, 20};
	// final double[] stepR = new double[]{0.00002, 0.001, 0.005, 0.01,
	// 0.01, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02,
	// 0.02, 0.015, 0.015, 0.01, 0.01};
	// final double[] minR = new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	// 0, 0, 0, 0, 0, 0, 0, 0, 0};
	// final double[] maxR = new double[] {0.0002, 0.02, 0.1, 0.2, 0.2,
	// 0.32, 0.4, 0.4, 0.4, 0.4, 0.4, 0.4, 0.4, 0.4, 0.4, 0.4, 0.3, 0.3,
	// 0.25, 0.25};

	int dim = 20;
	final int numR = 26;
	final double step = 0.01;
	final double minR = 0;
	String name1 = "\\\\.PSF\\sharedfolder\\rmao\\workspace\\result\\numpivot\\dim"
		+ dim + "\\uniform-dim" + dim + "-100k-";
	String name2 = "-100-pca-clustering-scale30q";
	String name3 = ".query";
	String name4 = ".query";
	String[] name5 = new String[] { "1-256", "2-16", "3-6", "4-4", "5-3",
		"6-2", "7-2", "8-2" };
	BufferedWriter writer = new BufferedWriter(
		new FileWriter(
			"\\\\.PSF\\sharedfolder\\rmao\\workspace\\result\\numpivot\\uniform-dim"
				+ dim
				+ "-100k-100-pca-clusering-scale30-q5k-query.csv"));
	// int size;
	final int numFile = 8;
	final int numField = 15;
	final int firstField = 4;
	final int numQuerySet = 25;
	final int size = 100000;

	double[][] v = new double[numR][numField];
	String[] segment;

	for (int i = 1; i <= numFile; i++) {
	    // if ( i==7)// || i==53 || i==95)
	    // continue;
	    //
	    // if ( ! ( new File(name1 + Integer.toString(i) + name2 + "1" +
	    // name3) ).exists())
	    // continue;

	    for (int j = 0; j < numR; j++)
		for (int k = 0; k < numField; k++)
		    v[j][k] = 0;

	    for (int j = 0; j < numQuerySet; j++) {
		BufferedReader reader = new BufferedReader(new FileReader(name1
			+ name5[i - 1] + name2 + j + name3));
		String line = reader.readLine();
		line = reader.readLine(); // skip the header
		line = reader.readLine();
		line = reader.readLine();
		// line = reader.readLine();
		for (int k = 0; k < numR; k++) {
		    line = reader.readLine();
		    // System.out.println("i=" + i + ", j=" + j + ", k=" + k +
		    // "" + line);
		    segment = line.split(",");
		    // System.out.println(line + segment.length);
		    for (int t = firstField; t < firstField + numField; t++)
			v[k][t - firstField] += Double.parseDouble(segment[t]);
		}
		// while (line != null)
		// {
		// segment = line.split(",");
		// writer.write(Integer.toString(i) + ", " + line + "\n");
		// line = reader.readLine();
		// }

		reader.close();
	    }
	    for (int j = 0; j < numR; j++)
		for (int k = 0; k < numField; k++)
		    v[j][k] /= numQuerySet;
	    for (int j = 0; j < numR; j++) {
		writer.write(dim + ", " + i + ", " + size + ", "
			+ (minR + step * j));
		for (int k = 0; k < numField; k++)
		    writer.write(", " + v[j][k]);
		writer.write("\n");
	    }

	}

	writer.close();
    }

    public static void condor() throws Exception {
	System.out.println("universe        = vanilla");
	System.out.println("Executable     = /lusr/java5/bin/java");
	System.out.println("+Group = \"GRAD\"");
	System.out.println("+Project = \"COMPUTATIONAL_BIOLOGY\"");
	System.out
		.println("+ProjectDescription = \"build vantage point index tree with different fft scale\"");
	System.out.println("");

	int dim = 20;
	int querySet = 25;
	int queryStep = 200;
	@SuppressWarnings("unused")
	int[] placeHolder = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
		13, 14, 15, 16, 17, 18, 19, 20 };
	final double[] stepR = new double[] { 0.00002, 0.001, 0.005, 0.01,
		0.01, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02,
		0.02, 0.02, 0.015, 0.015, 0.01, 0.01 };
	final double[] minR = new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0 };
	final double[] maxR = new double[] { 0.0002, 0.02, 0.1, 0.2, 0.2, 0.32,
		0.4, 0.4, 0.4, 0.4, 0.4, 0.4, 0.4, 0.4, 0.4, 0.4, 0.3, 0.3,
		0.25, 0.25 };
	String[] pivotString = new String[] { "1-256", "2-16", "3-6", "4-4",
		"5-3", "6-2", "7-2", "8-2" };

	for (int d = 1; d <= dim; d++) {
	    System.out
		    .println("###############################################################################################################");
	    System.out.println("#                         dim " + d);
	    System.out
		    .println("###############################################################################################################");
	    for (int p = 0; p < pivotString.length; p++) {
		System.out.println("### pivot " + (p + 1));
		for (int q = 0; q < querySet; q++) {
		    System.out
			    .print("Arguments  = -Xmx1800m -cp .:../jdb2/lib/colt.jar:../jdb2/src GeDBIT.app.QueryVPIndex -d ../result/db/numpivot/dim"
				    + d);
		    System.out
			    .print("/uniform-dim"
				    + d
				    + "-100k-"
				    + pivotString[p]
				    + "-100-pca-clustering-scale30-100000 -q uniformvector-20dim-100k.txt -f ");
		    System.out.print((q * queryStep) + " -l "
			    + (q * queryStep + queryStep - 1) + " -i "
			    + minR[d - 1] + " -a " + maxR[d - 1] + " -s "
			    + stepR[d - 1]);
		    System.out.println(" -t vector -p 0 -v 0 -g 0 -dim " + d
			    + " -forprint " + d + " -forprint2 " + (p + 1));
		    System.out.println("Error = ../result/numpivot/dim" + d
			    + "/uniform-dim" + d + "-100k-" + pivotString[p]
			    + "-100-pca-clustering-scale30q" + q + ".err");
		    System.out.println("Output = ../result/numpivot/dim" + d
			    + "/uniform-dim" + d + "-100k-" + pivotString[p]
			    + "-100-pca-clustering-scale30q" + q + ".query");
		    System.out.println("Log = ../result/numpivot/dim" + d
			    + "/uniform-dim" + d + "-100k-" + pivotString[p]
			    + "-100-pca-clustering-scale30q" + q + ".log");
		    System.out.println("nice_user       = False");
		    System.out.println("notification    = always");
		    System.out.println("notify_user     = rmao@cs.utexas.edu");
		    System.out
			    .println("requirements    = Memory >= 2000 && OpSys == \"LINUX\"");
		    System.out.println("Rank = Memory >= 4000");
		    System.out.println("Queue");
		    System.out.println("");
		    System.out.println("");

		}
		System.out.println();
	    }
	    System.out.println();
	    System.out.println();
	    System.out.println();
	}

    }

    public static void incCondor() throws Exception {
	System.out.println("universe        = vanilla");
	System.out.println("Executable     = /lusr/java5/bin/java");
	System.out.println("+Group = \"GRAD\"");
	System.out.println("+Project = \"COMPUTATIONAL_BIOLOGY\"");
	System.out
		.println("+ProjectDescription = \"build vantage point index tree with different fft scale\"");
	System.out.println("");

	String dataSet = "uv14";
	String input = "uniformvector-20dim-100k.txt";
	int v = 4;
	String type = "vector";
	int seta = 9000;
	String aString = "9k";
	int setn = 10;
	int dim = 14;

	// int dim = 20;
	// int querySet = 25;
	// int queryStep =200;
	// int [] placeHolder = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
	// 12, 13, 14, 15, 16, 17, 18, 19, 20};
	// final double[] stepR = new double[]{0.00002, 0.001, 0.005, 0.01,
	// 0.01, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02,
	// 0.02, 0.015, 0.015, 0.01, 0.01};
	// final double[] minR = new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	// 0, 0, 0, 0, 0, 0, 0, 0, 0};
	// final double[] maxR = new double[] {0.0002, 0.02, 0.1, 0.2, 0.2,
	// 0.32, 0.4, 0.4, 0.4, 0.4, 0.4, 0.4, 0.4, 0.4, 0.4, 0.4, 0.3, 0.3,
	// 0.25, 0.25};
	// String [] pivotString = new String [] {"1-256", "2-16", "3-6", "4-4",
	// "5-3", "6-2", "7-2", "8-2"};

	System.out
		.print("Arguments  = -Xmx1800m -cp .:../jdb2/lib/colt.jar:../jdb2/src GeDBIT.app.BuildVPIndex -n "
			+ input);
	System.out.print(" -psm incremental -v " + v
		+ " -dpm clusteringkmeans -f 3 -m 100 -t " + type
		+ " -g OFF -dim " + dim);
	System.out
		.print(" -b 1 -r 4 -i 100000 -a 1000000 -s 100000000 -o ../result/db/inc/"
			+ dataSet + "-" + v + "-3-100-inc-clustering-a");
	System.out.println(aString + "-n" + setn + " -seta " + seta + " -setn "
		+ setn + " -forprint " + aString + " -forprint " + setn);
	System.out.println("Error = ../result/inc/" + dataSet + "-" + v
		+ "-3-100-inc-clustering-a" + aString + "-n" + setn
		+ "-100k.err");
	System.out.println("Output = ../result/inc/" + dataSet + "-" + v
		+ "-3-100-inc-clustering-a" + aString + "-n" + setn
		+ "-100k.build");
	System.out.println("Log = ../result/inc/" + dataSet + "-" + v
		+ "-3-100-inc-clustering-a" + aString + "-n" + setn
		+ "-100k.log");
	System.out.println("nice_user       = False");
	System.out.println("notification    = always");
	System.out.println("notify_user     = rmao@cs.utexas.edu");
	System.out
		.println("requirements    = Memory >= 2000 && OpSys == \"LINUX\"");
	System.out
		.println("Memory >= 2000 && InMastodon && OpSys == \"LINUX\"");
	System.out.println("Queue");
	System.out.println("");
	System.out.println("");

    }

    public static void fftscaleBuildCondor() throws Exception {
	System.out.println("universe        = vanilla");
	System.out.println("Executable     = /lusr/java5/bin/java");
	System.out.println("+Group = \"GRAD\"");
	System.out.println("+Project = \"COMPUTATIONAL_BIOLOGY\"");
	System.out
		.println("+ProjectDescription = \"build vantage point index tree with different fft scale\"");
	System.out.println("");

	// int [] scale = new int []{94, 59, 58, 53, 52, 50};
	// String input = "normal-dim10-100k.txt";
	// String dir = "normal8";
	// String name = "normal8";
	int[] scale = new int[] { 43 };
	String input = "exponential-dim10-100k.txt";
	String name = "exp8";
	String dir = "exp8";
	// int [] scale = new int []{96};
	// String input = "uniformvector-20dim-100k.txt";
	// String name = "uniform-dim8";
	// String dir = "dim8";

	for (int s = 0; s < scale.length; s++) {
	    System.out
		    .println("Arguments  = -Xmx1800m -cp .:../jdb2/lib/colt.jar:../jdb2/src GeDBIT.app.BuildVPIndex -n "
			    + input
			    + " -psm pcaonfft -v 4 -dpm clusteringkmeans -f 3 -m 100 -t vector -g OFF -dim 8 -b 1 -r 4 -i 100000 -a 1000000 -s 100000000"
			    + " -fftscale "
			    + scale[s]
			    + " -o ../result/db/fftscale/"
			    + dir
			    + "/"
			    + name
			    + "-100k-4-3-100-pca-clustering-scale"
			    + scale[s]
			    + " -forprint " + scale[s]);
	    System.out.println("Error = ../result/fftscale/" + dir + "/" + name
		    + "-100k-4-3-100-pca-clustering-scale" + scale[s] + ".err");
	    System.out.println("Output = ../result/fftscale/" + dir + "/"
		    + name + "-100k-4-3-100-pca-clustering-scale" + scale[s]
		    + ".build");
	    System.out.println("Log = ../result/fftscale/" + dir + "/" + name
		    + "-100k-4-3-100-pca-clustering-scale" + scale[s] + ".log");
	    System.out.println("nice_user       = False");
	    System.out.println("notification    = always");
	    System.out.println("notify_user     = rmao@cs.utexas.edu");
	    System.out
		    .println("requirements    = Memory >= 2000 && OpSys == \"LINUX\"");
	    System.out
		    .println("Memory >= 2000 && InMastodon && OpSys == \"LINUX\"");
	    System.out.println("Queue");
	    System.out.println("");
	    System.out.println("");
	}

    }

    public static void fftscaleQueryCondor() throws Exception {
	System.out.println("universe        = vanilla");
	System.out.println("Executable     = /lusr/java5/bin/java");
	System.out.println("+Group = \"GRAD\"");
	System.out.println("+Project = \"COMPUTATIONAL_BIOLOGY\"");
	System.out
		.println("+ProjectDescription = \"build vantage point index tree with different fft scale\"");
	System.out.println("");

	int dim = 8;
	int querySet = 10;
	int queryStep = 500;
	int fftscale = 100;
	final double stepR = 0.02;
	final double minR = 0.5;
	final double maxR = 0.7;

	for (int q = 0; q < querySet; q++) {
	    for (int scale = 1; scale <= fftscale; scale++) {
		if ((scale != 40) && (scale != 53))
		    continue;

		System.out
			.print("Arguments  = -Xmx1800m -cp .:../jdb2/lib/colt.jar:../jdb2/src GeDBIT.app.QueryVPIndex -d ../result/db/fftscale/dim"
				+ dim);
		System.out.print("/uniform-dim" + dim
			+ "-100k-4-3-100-pca-clustering-scale" + scale
			+ "-100000 -q uniformvector-20dim-100k.txt -f ");
		System.out.print((q * queryStep) + " -l "
			+ (q * queryStep + queryStep - 1) + " -i " + minR
			+ " -a " + maxR + " -s " + stepR);
		System.out.println(" -t vector -p 0 -v 0 -g 0 -dim " + dim
			+ " -forprint " + dim + " -forprint " + scale);
		System.out.println("Error = ../result/fftscale/dim" + dim
			+ "/uniform-dim" + dim
			+ "-100k-4-3-100-pca-clustering-scale" + scale + "q"
			+ q + ".err");
		System.out.println("Output = ../result/fftscale/dim" + dim
			+ "/uniform-dim" + dim
			+ "-100k-4-3-100-pca-clustering-scale" + scale + "q"
			+ q + ".query");
		System.out.println("Log = ../result/fftscale/dim" + dim
			+ "/uniform-dim" + dim
			+ "-100k-4-3-100-pca-clustering-scale" + scale + "q"
			+ q + ".log");
		System.out.println("nice_user       = False");
		System.out.println("notification    = always");
		System.out.println("notify_user     = rmao@cs.utexas.edu");
		System.out
			.println("requirements    = Memory >= 2000 && OpSys == \"LINUX\"");
		System.out.println("Rank = Memory >= 4000");
		System.out.println("Queue");
		System.out.println("");
		System.out.println("");

	    }
	    System.out.println();
	}

    }

    public static void incQueryCondor() throws Exception {
	System.out.println("universe        = vanilla");
	System.out.println("Executable     = /lusr/java5/bin/java");
	System.out.println("+Group = \"GRAD\"");
	System.out.println("+Project = \"COMPUTATIONAL_BIOLOGY\"");
	System.out
		.println("+ProjectDescription = \"build vantage point index tree with different fft scale\"");
	System.out.println("");

	// String name = "uv8";
	// String radius = " -i 0.28 -a 0.3 -s 0.005 ";
	// String dim = "-dim 8";
	// String type = "vector";
	// String query = "uniformvector-20dim-100k.txt";
	// String dbPrefix = "uv8-4-3-100-inc-clustering-a9k-n10-db";
	// int v=4;
	//
	// String name = "uv4";
	// String radius = " -i 0.065 -a 0.07 -s 0.001 ";
	// String dim = "-dim 4";
	// String type = "vector";
	// String query = "uniformvector-20dim-100k.txt";
	// String dbPrefix = "uv4-4-3-100-inc-clustering-a9k-n10-db";
	// int v=4;
	//
	// String name = "uv14";
	// String radius = " -i 0.6 -a 0.625 -s 0.005 ";
	// String dim = "-dim 14";
	// String type = "vector";
	// String query = "uniformvector-20dim-100k.txt";
	// String dbPrefix = "uv14-4-3-100-inc-clustering-a9k-n10-db";
	// int v=4;
	//
	// String name = "alltp";
	// String radius = " -i 0 -a 4 -s 1 ";
	// String dim = "-frag 6";
	// String type = "protein";
	// String query = "alltp.fasta";
	// String dbPrefix = "alltp-4-3-100-inc-clustering-a9k-n10-db";
	// int v=4;
	//
	// String name = "arab1";
	// String radius = " -i 0 -a 5 -s 1 ";
	// String dim = "-frag 18";
	// String type = "dna";
	// String query = "arab1.con";
	// String dbPrefix = "arab1-4-3-100-inc-clustering-a9k-n10-db";
	// int v=4;
	//
	// String name = "image";
	// String radius = " -i 0 -a 0.09 -s 0.03 ";
	// String dim = "";
	// String type = "image";
	// String query = ".";
	// String dbPrefix = "image-4-3-100-inc-clustering-a5k-n25-db";
	// int v=4;
	//
	// String name = "mass";
	// String radius = " -i 1.05 -a 1.3 -s 0.05 ";
	// String dim = "";
	// String type = "msms";
	// String query = "zero_mimz.data";
	// String dbPrefix = "mass-2-3-100-inc-clustering-a20k-n10-db";
	// int v=2;

	// String name = "exp8";
	// String radius = " -i 0.5 -a 0.7 -s 0.02 ";
	// String dim = "-dim 8";
	// String type = "vector";
	// String query = "exponential-dim10-100k.txt";
	// String dbPrefix = "exp8-4-3-100-inc-clustering-a20k-n10-db";
	// int v = 4;

	// String name = "normal8";
	// String radius = " -i 1 -a 1.5 -s 0.05 ";
	// String dim = "-dim 8";
	// String type = "vector";
	// String query = "normal-dim10-100k.txt";
	// String dbPrefix = "normal8-4-3-100-inc-clustering-a9k-n10-db";
	// int v = 4;

	// String name = "texas";
	// String radius = " -i 0.015 -a 0.05 -s 0.005 ";
	// String dim = "-dim 2";
	// String type = "vector";
	// String query = "texas-distinct-randomized.txt";
	// String dbPrefix = "texas-3-3-100-inc-clustering-a30k-n10-db";
	// int v = 3;

	String name = "hawii";
	String radius = " -i 0.006 -a 0.01 -s 0.001 ";
	String dim = "-dim 2";
	String type = "vector";
	String query = "hawii-distinct-randomized.txt";
	String dbPrefix = "hawii-2-3-100-inc-clustering-a5k-n15-db";
	int v = 2;

	int querySet = 18;
	int queryStep = 516;
	int db = 10;

	// 0-100000.000
	// normal8-4-3-100-inc-clustering-a9k-n10-db0-100000.000
	// texas-3-3-100-inc-clustering-a30k-n10-db0-100000.000

	for (int q = 0; q < querySet; q++) {
	    for (int d = 0; d < db; d++) {
		System.out
			.println("Arguments  = -Xmx1800m -cp .:../jdb2/lib/colt.jar:../jdb2/src GeDBIT.app.QueryVPIndex -d ../result/db/inc/"
				+ dbPrefix
				+ d
				+ "-100000"
				+ " -q "
				+ query
				+ " -f "
				+ (q * queryStep)
				+ " -l "
				+ (q * queryStep + queryStep - 1)
				+ radius
				+ " -t "
				+ type
				+ " -p 0 -v 0 -g 0 "
				+ dim
				+ " -forprint db" + d);
		System.out.println("Error = ../result/inc/" + name + "-" + v
			+ "-3-100-inc-clustering-db" + d + "q" + q + ".err");
		System.out.println("Output = ../result/inc/" + name + "-" + v
			+ "-3-100-inc-clustering-db" + d + "q" + q + ".query");
		System.out.println("Log = ../result/inc/" + name + "-" + v
			+ "-3-100-inc-clustering-db" + d + "q" + q + ".log");
		System.out.println("nice_user       = False");
		System.out.println("notification    = always");
		System.out.println("notify_user     = rmao@cs.utexas.edu");
		System.out
			.println("requirements    = Memory >= 2000 && OpSys == \"LINUX\"");
		System.out.println("Rank = Memory >= 4000");
		System.out.println("Queue");
		System.out.println("");
		System.out.println("");

	    }
	    System.out.println();
	}

    }

    @SuppressWarnings("unused")
    public static void multiQueryResultFFTScale() throws Exception {
	String name1 = "\\\\.PSF\\sharedfolder\\rmao\\workspace\\result\\fftscale\\normal8\\normal8-100k-4-3-100-pca-clustering-scale";
	String name2 = "q";
	String name3 = ".query";
	String name4 = ".query";
	BufferedWriter writer = new BufferedWriter(
		new FileWriter(
			"\\\\.PSF\\sharedfolder\\rmao\\workspace\\result\\fftscale\\normal8-100k-4-3-100-pca-clustering-q5k-scale.csv"));
	// int size;
	final int numFile = 100;
	final int numR = 11;
	final int numField = 15;
	final int numQuerySet = 10;
	final int firstField = 4;
	final int size = 100000;
	final double step = 0.05;
	final double minR = 1;

	double[][] v = new double[numR][numField];
	String[] segment;

	for (int i = 1; i <= numFile; i++) {
	    // if ( i==0 || i==94 || i==85 || i==29)
	    // continue;

	    // if ( ! ( new File(name1 + Integer.toString(i) + name2 + "1" +
	    // name3) ).exists())
	    // continue;

	    for (int j = 0; j < numR; j++)
		for (int k = 0; k < numField; k++)
		    v[j][k] = 0;

	    for (int j = 0; j < numQuerySet; j++) {
		BufferedReader reader = new BufferedReader(new FileReader(name1
			+ i + name2 + j + name3));
		String line = reader.readLine();
		line = reader.readLine(); // skip the header
		line = reader.readLine();
		line = reader.readLine();
		// line = reader.readLine();
		for (int k = 0; k < numR; k++) {
		    line = reader.readLine();
		    // System.out.println("i=" + i + ", j=" + j + ", k=" + k +
		    // "" + line);
		    segment = line.split(",");
		    // System.out.println(line + segment.length);
		    for (int t = firstField; t < firstField + numField; t++)
			v[k][t - firstField] += Double.parseDouble(segment[t]);
		}
		// while (line != null)
		// {
		// segment = line.split(",");
		// writer.write(Integer.toString(i) + ", " + line + "\n");
		// line = reader.readLine();
		// }

		reader.close();
	    }
	    for (int j = 0; j < numR; j++)
		for (int k = 0; k < numField; k++)
		    v[j][k] /= numQuerySet;
	    for (int j = 0; j < numR; j++) {
		writer.write(i + ", " + size + ", " + (minR + step * j));
		for (int k = 0; k < numField; k++)
		    writer.write(", " + v[j][k]);
		writer.write("\n");
	    }

	}

	writer.close();
    }

    public static void multiQueryResultHawii() throws Exception {
	String name1 = "\\\\.PSF\\sharedfolder\\rmao\\workspace\\result\\fftscale\\hawii\\hawii-dr-2-3-100-pca-clustering-scale";
	String name2 = "-q";
	String name3 = "k.query";
	@SuppressWarnings("unused")
	String name4 = ".query";
	BufferedWriter writer = new BufferedWriter(new FileWriter(
		"\\\\.PSF\\sharedfolder\\rmao\\workspace\\result\\hawii.query"));
	// int size;
	double[][] v = new double[5][7];
	String[] segment;

	for (int i = 1; i <= 98; i++) {
	    if (i == 0 || i == 69 || i == 70 || i == 72)
		continue;

	    for (int j = 0; j < 5; j++)
		for (int k = 0; k < 7; k++)
		    v[j][k] = 0;

	    for (int j = 1; j <= 3; j++) {
		BufferedReader reader = new BufferedReader(new FileReader(name1
			+ Integer.toString(i) + name2 + Integer.toString(j)
			+ name3));
		String line = reader.readLine();
		line = reader.readLine(); // skip the header
		line = reader.readLine();
		line = reader.readLine();
		// line = reader.readLine();
		for (int k = 0; k < 5; k++) {
		    line = reader.readLine();
		    segment = line.split(",");
		    // System.out.println(line + segment.length);
		    for (int t = 3; t <= 9; t++)
			v[k][t - 3] += Double.parseDouble(segment[t]);
		}
		// while (line != null)
		// {
		// segment = line.split(",");
		// writer.write(Integer.toString(i) + ", " + line + "\n");
		// line = reader.readLine();
		// }

		reader.close();
	    }
	    for (int j = 0; j < 5; j++)
		for (int k = 0; k < 7; k++)
		    v[j][k] /= 3;
	    for (int j = 0; j < 5; j++) {
		writer.write(i + ", 9290, " + (0.01 * j + 0.06));
		for (int k = 0; k < 7; k++)
		    writer.write(", " + v[j][k]);
		writer.write("\n");
	    }

	}

	writer.close();
    }

}
