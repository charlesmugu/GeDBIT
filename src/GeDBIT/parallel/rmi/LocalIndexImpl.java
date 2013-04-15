package GeDBIT.parallel.rmi;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;

import GeDBIT.app.QueryVPIndex;
import GeDBIT.index.Index;
import GeDBIT.index.TableManager;
import GeDBIT.type.DNATable;
import GeDBIT.type.DoubleVectorTable;
import GeDBIT.type.ImageTable;
import GeDBIT.type.PeptideTable;
import GeDBIT.type.SpectraWithPrecursorMassTable;
import GeDBIT.type.Table;

public class LocalIndexImpl extends UnicastRemoteObject implements LocalIndex
{
    private static final long serialVersionUID = 1061237826684535271L;

    public LocalIndexImpl() throws RemoteException
    {
        super();
    }
    
    public void query(String[] args) throws RemoteException
    {
        String indexName = "";
        String queryFileName = "";
        String forPrint = "";

        int firstQuery = 0;
        int lastQuery = 1;

        double minRadius = 0.0;
        double maxRadius = 10.0;
        double step = 1.0;

        boolean verify = false;
        Level debug = Level.OFF;

        int frag = 6;
        int dim = 2;

        int pathLength = 0;
        String dataType = "sequence";
        String resultsFileName = null;

        for (int i = 0; i < args.length; i = i + 2)
        {
            if (args[i].equalsIgnoreCase("-d"))
                indexName = args[i + 1];

            else if (args[i].equalsIgnoreCase("-q"))
                queryFileName = args[i + 1];

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
            else if (args[i].equalsIgnoreCase("-st")|| args[i].equalsIgnoreCase("-sv"))
                continue;
            else
                throw new IllegalArgumentException("Invalid option " + args[i]);
        }

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

        Table dataTable = TableManager.getTableManager(indexName).getTable(
                indexName);
        Index index;
        if (dataTable != null)
            index = dataTable.getIndex();
        else
            throw new Error("index: " + indexName + " does not exist");

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
        } catch (IOException e1)
        {
            e1.printStackTrace();
        }
        QueryVPIndex evaluator = new QueryVPIndex(index, queryTable, minRadius,
                maxRadius, step, verify, debug, pathLength, frag,
                resultsFileName, firstQuery, lastQuery, forPrint);

        evaluator.evaluate();
    }

}
