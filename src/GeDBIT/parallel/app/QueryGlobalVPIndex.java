package GeDBIT.parallel.app;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import GeDBIT.dist.Metric;
import GeDBIT.index.Index;
import GeDBIT.index.RangeQuery;
import GeDBIT.index.TableManager;
import GeDBIT.index.VPIndex;
import GeDBIT.index.VPRangeCursor;
import GeDBIT.parallel.WorkThreadUtil;
import GeDBIT.parallel.rmi.GlobalIndex;
import GeDBIT.type.DNATable;
import GeDBIT.type.DoubleVector;
import GeDBIT.type.DoubleVectorTable;
import GeDBIT.type.ImageTable;
import GeDBIT.type.IndexObject;
import GeDBIT.type.PeptideTable;
import GeDBIT.type.Sequence;
import GeDBIT.type.SpectraWithPrecursorMassTable;
import GeDBIT.type.Table;

public class QueryGlobalVPIndex
{
    /**
     * This is a utility class to query a VPIndex in parallel, rmi client. It, taking command line
     * parameters, runs a set of query on the given index and compute the average
     * performance.
     * 
     * main function to evaluate the query performance of an {@link Index} The
     * evaluation is done by run a set of query on the Index, and compute total time used.
     * 
     * -d [name of index, should be a prefix of the actual file name containing serialized database] 
     * -q [query file name] 
     * -f [offset of first query to be used in the query file, start from 0, inclusive, default 0] 
     * -l [offset of last query to be used in the query file, exclusive, default 1] 
     * -i [minimum search radius, default 0.0] 
     * -a [maximum search radius, default 10.0] 
     * -s [step size for search radius, default 1] 
     * -t [data type, "vector", "protein", "dna", "image", "mass"] 
     * -p [length of the path distance list] 
     * -v [1 if search results are to be verified against a linear scan and 0 otherwise, default 0]
     * -g [debug level, default 0] 
     * -frag [fragment length, only meaningful for {@link Sequence}s] 
     * -dim [dimension of vector data to load, only meaningful for {@link DoubleVector}s] 
     * -res [output results to the given filename]
     * -st [subtree number for parallel query]
     * -sv [subtree servers ip configuration file name]
     * -pr [subtree servers print result objects or not, default 0]
     * -qw [wait each query finish or not, default 0]
     * The {@link Metric} is hardcoded for each give data type.
     * 
     * @author Rui Mao, Miaojie Feng
     * @version 2012.12.29
     */
    public static String                        servers[]    = null;
    public static HashMap<Integer, GlobalIndex> globalIndexs = null;

    Table                                       query;
    String                                      forPrint;
    final Metric                                metric;
    final Index                                 index;
    final int                                   indexSize;
    int                                         firstQuery;
    int                                         lastQuery;
    final double                                minRadius;
    final int                                   frag;
    final double                                maxRadius;
    final int                                   pathLength;
    int                                         querySize;
    final int                                   numRun;
    final double                                step;
    
    final Level                                 debug;
    final boolean                               verify;
    
    public static void main(String[] args)
    {
        String indexName = "";
        String queryFileName = "";
        String forPrint = "";

        int firstQuery = 0;
        int lastQuery = 1;

        double minRadius = 0.0;
        double maxRadius = 10.0;
        double step = 1.0;

        int frag = 6;
        int dim = 2;

        boolean qw = false;
        int pathLength = 0;
        int subtree = 1;
        
        String serversFileName = "";
        String dataType = "sequence";

        // don't support yet
        String resultsFileName = null;
        Level debug = Level.OFF;
        boolean verify = false;

        // parse arguments, and set values
        for (int i = 0; i < args.length; i = i + 2)
        {
            if (args[i].equalsIgnoreCase("-d"))
                indexName = args[i + 1];

            else if (args[i].equalsIgnoreCase("-q"))
                queryFileName = args[i + 1];

            else if (args[i].equalsIgnoreCase("-st"))
                subtree = Integer.parseInt(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-qw"))
                qw = (Integer.parseInt(args[i + 1]) == 1) ? true : false;
            
            else if (args[i].equalsIgnoreCase("-sv"))
                serversFileName = args[i + 1];

            else if (args[i].equalsIgnoreCase("-t"))
                dataType = args[i + 1];

            else if (args[i].equalsIgnoreCase("-forprint"))
                forPrint += args[i + 1] + ", ";

            else if (args[i].equalsIgnoreCase("-f"))
                firstQuery = Integer.parseInt(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-l"))
                lastQuery = Integer.parseInt(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-p"))
                pathLength = Integer.parseInt(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-i"))
                minRadius = Double.parseDouble(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-a"))
                maxRadius = Double.parseDouble(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-s"))
                step = Double.parseDouble(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-g"))
                debug = Level.parse(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-frag"))
                frag = Integer.parseInt(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-dim"))
                dim = Integer.parseInt(args[i + 1]);

            else if (args[i].equalsIgnoreCase("-v"))
                verify = (Integer.parseInt(args[i + 1]) == 1) ? true : false;

            else if (args[i].equalsIgnoreCase("-res"))
                resultsFileName = args[i + 1];

            else if (args[i].equalsIgnoreCase("-pr"))
                continue;

            else
                throw new IllegalArgumentException("Invalid option " + args[i]);
        }

        // check arguments
        if (indexName == "")
            throw new IllegalArgumentException("Invalid Index file name!");

        if (queryFileName == "")
            throw new IllegalArgumentException("Invalid Query file name!");

        if ((firstQuery < 0) || (lastQuery < 0) || (lastQuery < firstQuery))
            throw new IllegalArgumentException(
                    "Invalid first query index or last query index!");

        if ((minRadius < 0) || (maxRadius < 0) || (maxRadius < minRadius)
                || (step <= 0))
            throw new IllegalArgumentException(
                    "Invalid min radius, max radius, or radius increasement unit!");

        // load index from file
        Table dataTable = TableManager.getTableManager(indexName).getTable(
                indexName);
        Index index;
        if (dataTable != null)
            index = dataTable.getIndex();
        else
            throw new Error("index: " + indexName + " does not exist");
        if (index instanceof VPIndex)
        {
            if (((VPIndex) index).getSubtree() != subtree)
            {
                throw new Error("index: " + indexName + " with wrong subtree");
            }
        } else
            throw new Error("index: " + indexName + " does not support");

        Table queryTable = null;
        try
        {
            if (dataType.equalsIgnoreCase("protein"))
                queryTable = new PeptideTable(queryFileName, "", lastQuery,
                        frag);
            else if (dataType.equalsIgnoreCase("vector"))
                queryTable = new DoubleVectorTable(queryFileName, "",
                        lastQuery, dim);
            else if (dataType.equalsIgnoreCase("dna"))
                queryTable = new DNATable(queryFileName, "", lastQuery, frag);
            else if (dataType.equalsIgnoreCase("image"))
                queryTable = new ImageTable(queryFileName, "", lastQuery);
            else if (dataType.equalsIgnoreCase("msms"))
                queryTable = new SpectraWithPrecursorMassTable(queryFileName,
                        "", lastQuery);
            else
                System.err.println("data type not supported! " + dataType);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        // initialize remote servers and thread pool
        initializeServers(serversFileName, subtree, qw, args);

        QueryGlobalVPIndex evaluator = new QueryGlobalVPIndex(index, queryTable, minRadius, maxRadius, step, verify
                , debug, pathLength, frag, resultsFileName, firstQuery, lastQuery, forPrint);
        evaluator.evaluate();
    }

    public QueryGlobalVPIndex(Index index, Table query, double minRadius, double maxRadius, double step, boolean verify, Level debug, int pathLength,
            int frag, String resultsFileName, int firstQuery, int lastQuery, String forPrint)
    {
        // check argument
        if (index == null)
            throw new IllegalArgumentException(" The Index is null!");

        if (query == null)
            throw new IllegalArgumentException(" The query list is null!");

        if ((minRadius < 0) || (maxRadius < 0) || (maxRadius < minRadius)
                || (step <= 0))
            throw new IllegalArgumentException(
                    "Invalid min radius, max radius, or radius increasement unit!");

        this.metric = index.getMetric();
        this.index = index;
        this.indexSize = index.size();
        
        this.query = query;
        this.querySize = query.size();
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
        this.step = step;
        this.numRun = (int) Math.round((maxRadius - minRadius) / step) + 1;
        this.frag = frag;
        
        this.firstQuery = firstQuery;
        this.lastQuery = lastQuery;

        this.pathLength = pathLength;
        this.forPrint = forPrint;
        
        // don't support yet
        this.verify = verify;
        this.debug = debug;
    }

    private static void initializeServers(String file, int subtree, boolean qw,
            String[] args)
    {
        try
        {
            servers = new String[subtree];
            globalIndexs = new HashMap<Integer, GlobalIndex>();
            @SuppressWarnings("resource")
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file)));
            String data = null;
            int counter = 0;
            while ((data = br.readLine()) != null && counter < subtree)
            {
                servers[counter++] = data;
            }

            GlobalIndex index = null;
            for (int i = 0; i < subtree; i++)
            {
                index = (GlobalIndex) Naming.lookup("rmi://" + servers[i]
                        + "/global");
                index.initialize(args, (i + 1));
                globalIndexs.put(i, index);
            }
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (NotBoundException e)
        {
            e.printStackTrace();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        WorkThreadUtil.newInstance(subtree);
        WorkThreadUtil.setWaitEachQueryFinished(qw);
    }
    
    public void evaluate()
    {
        
        // final double delta = 1e-6;
        //DecimalFormat fmt = new DecimalFormat("0.#####"); // for 3 decimal places
        final double startTotal = System.currentTimeMillis();
        // run queries for each query and radius
        for (int i=0; i<this.numRun; i++)
        {
            System.out.println("Evaluating Radius " + (this.minRadius + this.step*i));
            evaluateRadius(this.minRadius + this.step*i);
        }
        WorkThreadUtil.setFinishedStatus();
        WorkThreadUtil.waitAllQueryFinished();
        final double endTotal = System.currentTimeMillis();
        
        // print out total time used
        System.out.println("Index size, Search Time");
        System.out.println(forPrint + indexSize + ", " + ((endTotal - startTotal) / 1000)/ this.querySize);
    }
    
    public void evaluateRadius(double radius)
    {
        // start running each query
        List<? extends IndexObject> allQuery = query.getData();
        List<? extends IndexObject> query = allQuery.subList((firstQuery < 0)?0: firstQuery, (lastQuery >allQuery.size())?allQuery.size():lastQuery);
        Iterator<? extends IndexObject> p = query.iterator();
        int queryCounter = -1;
        while (p.hasNext())
        {
            queryCounter++;
            RangeQuery q = new RangeQuery(queryCounter, (IndexObject) p.next(), radius, pathLength);
            VPRangeCursor cursor = (VPRangeCursor) index.search(q);
            cursor.searchResults();
        } // end of while for all queries
    }
}
