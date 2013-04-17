package GeDBIT.mapreduce.app;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import GeDBIT.dist.CountedMetric;
import GeDBIT.index.algorithms.PartitionMethod;
import GeDBIT.index.algorithms.PartitionMethods;
import GeDBIT.index.algorithms.PivotSelectionMethod;
import GeDBIT.index.algorithms.PivotSelectionMethods;
import GeDBIT.type.DoubleVectorTableMR;
import GeDBIT.type.IndexObject;
import GeDBIT.type.TableMR;

public class IndexMapReduce {
    // Build index parameters
    static int pNum = 3;
    static int sf = 3;
    static int mls = 100;
    static int initialSize = 100000;
    static int finalSize = 1000000;
    static int stepSize = 100000;
    static Level debug = Level.OFF;
    static int pathLength = 0;
    static PartitionMethod dpm = PartitionMethods.valueOf("BALANCED");
    static PivotSelectionMethod psm = PivotSelectionMethods.valueOf("CENTER");
    static int frag = 6;
    static int dim = 2;
    static boolean bucket = true;
    static double maxR = 0.1;

    static int setA = 10000;
    static int setN = 50;
    static int fftScale = 100;

    static String dataType = "vector";
    static String psmName = "";
    static String forPrint = "";

    static String selectAlgorithm = "";
    static String testKind = "";
    static String yMethod = "";

    // Query parameters
    static String indexName = "";
    static String queryFilePath = "";
    static String queryResultPath = "";

    static int firstQuery = 0;
    static int lastQuery = 1;

    static double minRadius = 0.0;
    static double maxRadius = 10.0;
    static double step = 1.0;

    static boolean verify = false;

    static String resultsFileName = null;

    static public class radiusData {
	public double radius;
	public int size;

	public radiusData(double radius, int size) {
	    this.radius = radius;
	    this.size = size;
	}
    }

    public static class Map extends
	    Mapper<Text, BytesWritable, IntWritable, Text> {
	public void map(Text key, BytesWritable value, Context context)
		throws IOException, InterruptedException {
	    @SuppressWarnings("unused")
	    String line = value.toString();
	    @SuppressWarnings("unused")
	    double startTime, endTime;
	    // final int indexNum = (finalSize - initialSize) / stepSize + 1;
	    // double[] buildTimes = new double[indexNum];
	    // int[] distCalNum = new int[indexNum];

	    Text t = new Text(value.getBytes());
	    String str = t.toString();
	    int lineBegin = 0;
	    int lineNum = 0;
	    int k = 0;
	    String lineString = "";
	    String[] strList;

	    for (int i = 0; i < str.length(); i++) {
		if ((i > 0) && (str.charAt(i) == '\n')) {
		    lineNum++;
		}
	    }

	    int initialSize = lineNum;
	    int finalSize = lineNum;
	    int stepSize = lineNum;
	    final int indexNum = (finalSize - initialSize) / stepSize + 1;
	    @SuppressWarnings("unused")
	    double[] buildTimes = new double[indexNum];
	    int[] distCalNum = new int[indexNum];
	    double[][] data = new double[lineNum][dim];
	    List<IndexObject> resultList = new ArrayList<IndexObject>();
	    List<radiusData> queryRadiusData = new ArrayList<radiusData>();
	    int radiusNum = 0;

	    String fileName = "1m.vector";
	    String indexPrefix = "vpindex";
	    String queryFileName = "";

	    for (int i = 0; i < str.length(); i++) {
		if ((i > 0) && (str.charAt(i) == '\n')) {
		    lineString = str.substring(lineBegin, i - 1);
		    lineString = lineString.trim();
		    strList = lineString.split("[ \t]+");
		    for (int j = 0; j < dim; j++) {
			data[k][j] = java.lang.Double.parseDouble(strList[j]);
		    }
		    lineBegin = i + 1;
		    k++;
		}
	    }

	    for (int size = initialSize, i = 0; (size <= initialSize)
		    & (i < indexNum); size += stepSize, i++) {
		String currentIndexPrefix = indexPrefix + "-" + size;

		if (!removeFilesWithHeader(currentIndexPrefix + ".")) {
		    System.out.println("Can not remove old files!");
		    System.exit(-1);
		}

		if (!removeFilesWithHeader(currentIndexPrefix + "-")) {
		    System.out.println("Can not remove old files!");
		    System.exit(-1);
		}

		TableMR table = null;
		try {
		    table = getTable(dataType, fileName, currentIndexPrefix,
			    size, frag, dim, data);
		} catch (IOException e) {
		    e.printStackTrace();
		}

		CountedMetric countMetric = new CountedMetric(table.getMetric());

		if (debug != Level.OFF)
		    System.out.println(" building size:" + table.size()
			    + "..., ");

		startTime = System.currentTimeMillis();
		table.setMetric(countMetric);

		table.buildVPIndex(psm, pNum, dpm, sf, mls, pathLength, bucket,
			debug, forPrint);

		endTime = System.currentTimeMillis();

		// buildTimes[i] = (endTime - startTime) / 1000.00;
		distCalNum[i] = countMetric.getCounter();
		countMetric.clear();

		// Query
		TableMR queryTable = null;
		try {
		    /*
		     * if (dataType.equalsIgnoreCase("protein")) queryTable =
		     * new PeptideTable(queryFileName, "", lastQuery, frag);
		     * else
		     */if (dataType.equalsIgnoreCase("vector"))
			queryTable = new DoubleVectorTableMR(queryFileName, "",
				lastQuery, dim, data);
		    /*
		     * else if (dataType.equalsIgnoreCase("dna")) queryTable =
		     * new DNATable(queryFileName, "", lastQuery, frag); else if
		     * (dataType.equalsIgnoreCase("image")) queryTable = new
		     * ImageTable(queryFileName, "", lastQuery); else if
		     * (dataType.equalsIgnoreCase("msms")) queryTable = new
		     * SpectraWithPrecursorMassTable(queryFileName, "",
		     * lastQuery);
		     */
		    else
			System.err.println("data type not supported! "
				+ dataType);
		} catch (IOException e1) {
		    // TODO Auto-generated catch block
		    e1.printStackTrace();
		}

		QueryVPIndexMR evaluator = new QueryVPIndexMR(table.getIndex(),
			queryTable, minRadius, maxRadius, step, verify, debug,
			pathLength, frag, resultsFileName, firstQuery,
			lastQuery, forPrint);

		evaluator.evaluate(resultList, queryRadiusData);
	    }
	    for (int i = 0; i < resultList.size(); i++) {
		// context.write(null, new
		// IntWritable(resultList.get(i).getRowID()));
		StringBuffer sb = new StringBuffer();
		sb.append("radius: " + queryRadiusData.get(radiusNum).radius
			+ "  data: ");
		if (i == queryRadiusData.get(radiusNum + 1).size) {
		    radiusNum++;
		}

		for (int j = 0; j < data[resultList.get(i).getRowID()].length; j++) {
		    sb.append(data[resultList.get(i).getRowID()][j]);
		    sb.append("  ");
		}

		context.write(null, new Text(sb.toString()));
	    }

	}
    }

    public static class Reduce extends
	    Reducer<IntWritable, Text, IntWritable, Text> {
	// private static IntWritable linenum = new IntWritable(1);
	public void reduce(IntWritable key, Text value, Context context)
		throws IOException, InterruptedException {
	    // context.write(null, new Text(value.toString()));
	    context.write(null, value);
	}
    }

    public static void main(String[] args) throws Exception {
	int pNumArgs = 3;
	int sfArgs = 3;
	int mlsArgs = 100;
	@SuppressWarnings("unused")
	Level debugArgs = Level.OFF;
	int pathLengthArgs = 0;
	PartitionMethod dpmArgs = PartitionMethods.valueOf("BALANCED");
	PivotSelectionMethod psmArgs = PivotSelectionMethods.valueOf("FFT");
	@SuppressWarnings("unused")
	int fragArgs = 6;
	int dimArgs = 2;
	boolean bucketArgs = true;
	double maxRArgs = 0.1;

	int setAArgs = 10000;
	int setNArgs = 50;
	int fftScaleArgs = 100;

	String dataTypeArgs = "vector";
	String psmNameArgs = "";
	@SuppressWarnings("unused")
	String forPrintArgs = "";

	String selectAlgorithmArgs = "";
	String testKindArgs = "";
	String yMethodArgs = "";

	// Query parameters
	@SuppressWarnings("unused")
	String indexNameArgs = "";
	String queryFilePathArgs = "";
	String queryResultPathArgs = "";

	int firstQueryArgs = 0;
	int lastQueryArgs = 1;

	double minRadiusArgs = 0.0;
	double maxRadiusArgs = 10.0;
	double stepArgs = 1.0;

	boolean verifyArgs = false;

	@SuppressWarnings("unused")
	String resultsFileNameArgs = null;

	// parse arguments, and set values
	for (int i = 0; i < args.length; i = i + 2) {
	    if (args[i].equalsIgnoreCase("-q"))
		queryFilePathArgs = args[i + 1];

	    else if (args[i].equalsIgnoreCase("-o"))
		queryResultPathArgs = args[i + 1];

	    else if (args[i].equalsIgnoreCase("-dpm")) {
		dpmArgs = PartitionMethods.valueOf(args[i + 1].toUpperCase());
	    }

	    else if (args[i].equalsIgnoreCase("-t"))
		dataTypeArgs = args[i + 1];

	    else if (args[i].equalsIgnoreCase("-dim"))
		dimArgs = Integer.parseInt(args[i + 1]);

	    else if (args[i].equalsIgnoreCase("-psm")) {
		psmNameArgs = args[i + 1];
	    }

	    else if (args[i].equalsIgnoreCase("-p"))
		pNumArgs = Integer.parseInt(args[i + 1]);

	    else if (args[i].equalsIgnoreCase("-sf"))
		sfArgs = Integer.parseInt(args[i + 1]);

	    else if (args[i].equalsIgnoreCase("-m"))
		mlsArgs = Integer.parseInt(args[i + 1]);

	    else if (args[i].equalsIgnoreCase("-pl"))
		pathLengthArgs = Integer.parseInt(args[i + 1]);

	    else if (args[i].equalsIgnoreCase("-b"))
		bucketArgs = (Integer.parseInt(args[i + 1]) == 1);

	    else if (args[i].equalsIgnoreCase("-r"))
		maxRArgs = Double.parseDouble(args[i + 1]);

	    else if (args[i].equalsIgnoreCase("-fftscale"))
		fftScaleArgs = Integer.parseInt(args[i + 1]);

	    else if (args[i].equalsIgnoreCase("-l"))
		lastQueryArgs = Integer.parseInt(args[i + 1]);

	    else if (args[i].equalsIgnoreCase("-i"))
		minRadiusArgs = Double.parseDouble(args[i + 1]);

	    else if (args[i].equalsIgnoreCase("-a"))
		maxRadiusArgs = Double.parseDouble(args[i + 1]);

	    else if (args[i].equalsIgnoreCase("-s"))
		stepArgs = Double.parseDouble(args[i + 1]);

	    else if (args[i].equalsIgnoreCase("-v"))
		verifyArgs = (Integer.parseInt(args[i + 1]) == 1) ? true
			: false;

	    else if (args[i].equalsIgnoreCase("-f"))
		firstQueryArgs = Integer.parseInt(args[i + 1]);

	    else if (args[i].equalsIgnoreCase("-forprint"))
		forPrintArgs += args[i + 1] + ", ";

	    else if (args[i].equalsIgnoreCase("-seta"))
		setAArgs = Integer.parseInt(args[i + 1]);

	    else if (args[i].equalsIgnoreCase("-setn"))
		setNArgs = Integer.parseInt(args[i + 1]);

	    else if (args[i].equalsIgnoreCase("-frag"))
		fragArgs = Integer.parseInt(args[i + 1]);

	    else if (args[i].equalsIgnoreCase("-g"))
		debugArgs = Level.parse(args[i + 1]);

	    else if (args[i].equalsIgnoreCase("-sa"))
		selectAlgorithmArgs = args[i + 1];

	    else if (args[i].equalsIgnoreCase("-ym"))
		yMethodArgs = args[i + 1];

	    else if (args[i].equalsIgnoreCase("-tkind"))
		testKindArgs = args[i + 1];

	    else if (args[i].equalsIgnoreCase("-d"))
		indexNameArgs = args[i + 1];

	    else if (args[i].equalsIgnoreCase("-res"))
		resultsFileNameArgs = args[i + 1];

	    else
		throw new IllegalArgumentException("Invalid option " + args[i]);
	}

	dpmArgs.setMaxRadius(maxR);

	// hack, if cght, use clustering partition, and set maxr to -1 to denote
	// it
	if (dpmArgs == PartitionMethods.CGHT) {
	    dpmArgs = PartitionMethods.CLUSTERINGKMEANS;
	    dpmArgs.setMaxRadius(-2);
	}
	if (dpmArgs == PartitionMethods.GHT) {
	    dpmArgs = PartitionMethods.CLUSTERINGKMEANS;
	    dpmArgs.setMaxRadius(-1);
	}

	if (psmNameArgs.equalsIgnoreCase("incremental"))
	    psmArgs = new GeDBIT.index.algorithms.IncrementalSelection(
		    setAArgs, setNArgs);
	else if (psmNameArgs.equalsIgnoreCase("pcaonfft"))
	    psmArgs = new GeDBIT.index.algorithms.PCAOnFFT(fftScaleArgs);
	else if (psmNameArgs.equalsIgnoreCase("selectiononfft"))
	    psmArgs = new GeDBIT.index.algorithms.SelectionOnFFT(fftScaleArgs,
		    testKindArgs, yMethodArgs, selectAlgorithmArgs);
	else if (psmNameArgs.equalsIgnoreCase("eigen"))
	    psmArgs = new GeDBIT.index.algorithms.EigenOnFFT(fftScaleArgs);
	else if (psmNameArgs.equalsIgnoreCase("gauss"))
	    psmArgs = new GeDBIT.index.algorithms.GaussOnFFT(fftScaleArgs);
	else
	    psmArgs = PivotSelectionMethods.valueOf(psmNameArgs.toUpperCase());

	run(queryFilePathArgs, queryResultPathArgs, dpmArgs, dataTypeArgs,
		dimArgs, psmArgs, pNumArgs, sfArgs, mlsArgs, pathLengthArgs,
		bucketArgs, maxRArgs, fftScaleArgs, lastQueryArgs,
		minRadiusArgs, maxRadiusArgs, stepArgs, verifyArgs,
		firstQueryArgs);
    }

    public static void run(String queryFilePathArgs,
	    String queryResultPathArgs, PartitionMethod dpmArgs,
	    String dataTypeArgs, int dimArgs, PivotSelectionMethod psmArgs,
	    int pNumArgs, int sfArgs, int mlsArgs, int pathLengthArgs,
	    boolean bucketArgs, double maxRArgs, int fftScaleArgs,
	    int lastQueryArgs, double minRadiusArgs, double maxRadiusArgs,
	    double stepArgs, boolean verifyArgs, int firstQueryArgs)
	    throws IOException, InterruptedException, ClassNotFoundException {
	queryFilePath = queryFilePathArgs;
	queryResultPath = queryResultPathArgs;
	dpm = dpmArgs;
	dataType = dataTypeArgs;
	dim = dimArgs;
	psm = psmArgs;
	pNum = pNumArgs;
	sf = sfArgs;
	mls = mlsArgs;
	pathLength = pathLengthArgs;
	bucket = bucketArgs;
	maxR = maxRArgs;
	fftScale = fftScaleArgs;
	lastQuery = lastQueryArgs;
	minRadius = minRadiusArgs;
	maxRadius = maxRadiusArgs;
	step = stepArgs;
	verify = verifyArgs;
	firstQuery = firstQueryArgs;

	Configuration conf = new Configuration();
	if ((queryFilePath == "") | (queryResultPath == "")) {
	    System.err
		    .println("Usage: -q queryFilePath -o queryResultPath and other parameters");
	    System.exit(2);
	}
	Job job = new Job(conf, "IndexMapReduce");
	job.setNumReduceTasks(0);
	job.setInputFormatClass(GeDBIT.mapreduce.fileformat.WholeFileInputFormat.class);
	job.setOutputFormatClass(TextOutputFormat.class);
	job.setJarByClass(GeDBIT.mapreduce.app.IndexMapReduce.class);
	job.setMapperClass(GeDBIT.mapreduce.app.IndexMapReduce.Map.class);
	job.setReducerClass(GeDBIT.mapreduce.app.IndexMapReduce.Reduce.class);
	job.setOutputKeyClass(IntWritable.class);
	job.setOutputValueClass(Text.class);
	FileInputFormat.addInputPath(job, new Path(queryFilePath));
	FileOutputFormat.setOutputPath(job, new Path(queryResultPath));
	System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    public static boolean removeFilesWithHeader(String header) {
	if (header == null)
	    return true;

	class HeaderFilter implements FileFilter {
	    String header;

	    HeaderFilter(File f) {
		header = f.getAbsolutePath();
	    }

	    public boolean accept(File pathName) {
		String input = pathName.getAbsolutePath();
		return input.startsWith(header);
	    }
	}
	;

	File f = new File(header);
	File parent = (new File(f.getAbsolutePath())).getParentFile();
	// System.out.println("Parent:" + parent.getAbsolutePath() );

	File[] toDelete = parent.listFiles(new HeaderFilter(f));
	if (toDelete == null)
	    return true;

	for (int i = 0; i < toDelete.length; i++) {
	    if (!toDelete[i].delete()) {
		System.out.println("Could not delete file:"
			+ toDelete[i].getAbsolutePath());
		return false;
	    }
	    // System.out.println("File: " + toDelete[i].getAbsolutePath() +
	    // " deleted.");
	}

	return true;
    }

    private static TableMR getTable(String dataType, String fileName,
	    String indexPrefix, int maxDataSize, int fragLength, int dim,
	    double[][] data) throws IOException {
	TableMR table = null;

	// TODO remove the null
	/*
	 * if (dataType.equalsIgnoreCase("ms")) { table = new
	 * SpectraTable(fileName, indexPrefix, maxDataSize, null); } else if
	 * (dataType.equalsIgnoreCase("msms")) { table = new
	 * SpectraWithPrecursorMassTable(fileName, indexPrefix, maxDataSize); }
	 * else if (dataType.equalsIgnoreCase("dna")) { table = new
	 * DNATable(fileName, indexPrefix, maxDataSize, fragLength); } else if
	 * (dataType.equalsIgnoreCase("rna")) { table = new RNATable(fileName,
	 * indexPrefix, maxDataSize, fragLength); } else if
	 * (dataType.equalsIgnoreCase("protein")) { table = new
	 * PeptideTable(fileName, indexPrefix, maxDataSize, fragLength); } else
	 */if (dataType.equalsIgnoreCase("vector")) {
	    table = new DoubleVectorTableMR(fileName, indexPrefix, maxDataSize,
		    dim, data);
	}
	/*
	 * else if (dataType.equalsIgnoreCase("image")) { table = new
	 * ImageTable(fileName, indexPrefix, maxDataSize); }
	 */
	else {
	    throw new Error("Invalid dataType!");
	}
	return table;
    }
}
