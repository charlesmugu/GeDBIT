package GeDBIT.parallel.rmi;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import GeDBIT.dist.Metric;
import GeDBIT.index.Index;
import GeDBIT.index.RangeQuery;
import GeDBIT.index.TableManager;
import GeDBIT.index.VPRangeCursor;
import GeDBIT.type.DNATable;
import GeDBIT.type.DoubleVectorTable;
import GeDBIT.type.ImageTable;
import GeDBIT.type.IndexObject;
import GeDBIT.type.PeptideTable;
import GeDBIT.type.SpectraWithPrecursorMassTable;
import GeDBIT.type.Table;
import GeDBIT.util.MckoiObjectIOManager;
import GeDBIT.util.ObjectIOManager;

public class GlobalIndexImpl extends UnicastRemoteObject implements GlobalIndex
{
    private static final long serialVersionUID = -6189007607283310933L;
    
    public static List<? extends IndexObject> queries;
    public static ObjectIOManager oiom;
    public static int pathLength = 0;
    
    public static boolean printResults = false;
    public static boolean verify = false;
    public static List<IndexObject> linearIndex = null;
    
    public GlobalIndexImpl() throws RemoteException
    {
        super();
    }

    public void initialize(String[] args, int serverID)
    {
        String queryFileName = "";
        String indexPrefix = "";
        
        String dataType = "sequence";
        
        int firstQuery = 0;
        int lastQuery = 1;
        
        int frag = 6;
        int dim = 2;
        
        for (int i = 0; i < args.length; i = i + 2)
        {
            if (args[i].equalsIgnoreCase("-q"))
                queryFileName = args[i + 1];
            else if (args[i].equalsIgnoreCase("-t"))
                dataType = args[i + 1];
            else if (args[i].equalsIgnoreCase("-f"))
                firstQuery = Integer.parseInt(args[i + 1]);
            else if (args[i].equalsIgnoreCase("-l"))
                lastQuery = Integer.parseInt(args[i + 1]);
            else if (args[i].equalsIgnoreCase("-p"))
                pathLength = Integer.parseInt(args[i + 1]);
            else if (args[i].equalsIgnoreCase("-frag"))
                frag = Integer.parseInt(args[i + 1]);
            else if (args[i].equalsIgnoreCase("-dim"))
                dim = Integer.parseInt(args[i + 1]);
            else if (args[i].equalsIgnoreCase("-v"))
                verify = (Integer.parseInt(args[i + 1]) == 1) ? true : false;
            else if (args[i].equalsIgnoreCase("-pr"))
                printResults = (Integer.parseInt(args[i + 1]) == 1) ? true : false;
            else if (args[i].equalsIgnoreCase("-d"))
                indexPrefix = args[i + 1];
            else 
                continue;
        }
        if (verify)
        {
            Table dataTable = TableManager.getTableManager(indexPrefix).getTable(indexPrefix);
            Index index;
            if (dataTable != null)
                index = dataTable.getIndex();
            else
                throw new Error("index: " + indexPrefix + " does not exist");
            linearIndex = index.getAllPoints();
        }
        Table queryTable = null;
        try
        {
            if (dataType.equalsIgnoreCase("protein"))
                queryTable = new PeptideTable(queryFileName, "", lastQuery, frag);
            else if (dataType.equalsIgnoreCase("vector"))
                queryTable = new DoubleVectorTable(queryFileName, "", lastQuery, dim);
            else if (dataType.equalsIgnoreCase("dna"))
                queryTable = new DNATable(queryFileName, "", lastQuery, frag);
            else if (dataType.equalsIgnoreCase("image"))
                queryTable = new ImageTable(queryFileName, "", lastQuery);
            else if (dataType.equalsIgnoreCase("msms"))
                queryTable = new SpectraWithPrecursorMassTable(queryFileName, "", lastQuery);
            else
                System.err.println("data type not supported! " + dataType);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        List<? extends IndexObject> allQuery = queryTable.getData();
        queries = allQuery.subList((firstQuery < 0)?0: firstQuery, (lastQuery >allQuery.size())?allQuery.size():lastQuery);
        
        oiom = new MckoiObjectIOManager(indexPrefix + "-" + serverID, "000",
                1024 * 1024 * 1024, "Java IO", 4, 128 * 1024, true);
        try
        {
            if (!oiom.open())
            {
                throw new Error("Cannot open store!");
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void queryResult(String forprint)
    {
        if(printResults)
        {
            System.out.println(forprint);
        }
    }
    
    public void query(Metric metric, long rootAddress,
            double[] distList, int counter, double radius, String forprint)
    {
        //System.out.println("Step counter: " + counter);
        int queryCounter = 0;
        RangeQuery query = null;
        Iterator<? extends IndexObject> queryObjects = queries.iterator();
        while (queryObjects.hasNext())
        {
            if(queryCounter != counter) {
                queryCounter++;
                queryObjects.next();
                continue;
            } else {
                query = new RangeQuery((IndexObject) queryObjects.next(), radius, pathLength);
                break;
            }
        }
        
        try
        {
            if(query == null)
            {
                throw new Error("Can't find IndexObject!");
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        
        VPRangeCursor cursor = new VPRangeCursor(query, oiom, metric, rootAddress);
        List<IndexObject> result = cursor.searchRangeQuery(distList);
        if (verify)
        {
            if (!verifyResult(result, query, linearIndex, metric))
            {
                System.out.println("Inconsistent search results! query: " + queryCounter + ", radius: " + radius + " !");
                System.exit(-1);
            } 
        }
        StringBuffer buffer = new StringBuffer(forprint);
        for(IndexObject obj : result)
        {
            buffer.append(obj.toString());
        }
        if(printResults)
        {
            System.out.println(buffer.toString());
        }
    }

    public boolean verifyResult(List<IndexObject> resultList, RangeQuery q, List<IndexObject> linearIndex, Metric metric)
    {
        if (resultList == null)
            resultList = new ArrayList<IndexObject>(0);
        Iterator<IndexObject> p = linearIndex.iterator();
        IndexObject data;
        while (p.hasNext())
        {
            data = p.next();
            if (metric.getDistance(data, q.getQueryObject()) <= q.getRadius())
            {
                if (!resultList.remove(data))
                {
                    System.out.println("Found: linearscan result not in index resultset: " + data.toString() + ", query=" + q.getQueryObject());
                    return false;
                }
            }
        }

        if (resultList.size() != 0)
        {
            System.out.println("Found: index result not returned by linear scan.  Query =" + q.getQueryObject());
            for (IndexObject o : resultList)
                System.out.println(o);
            return false;
        }
        else
            return true;

    }
}
