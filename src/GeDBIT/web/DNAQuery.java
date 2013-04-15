package GeDBIT.web;

import java.io.IOException;
import java.util.logging.Level;

import GeDBIT.app.QueryVPIndex;
import GeDBIT.index.Index;
import GeDBIT.index.TableManager;
import GeDBIT.type.DNATable;
import GeDBIT.type.Table;

public class DNAQuery
{
    public void runQuery(double radius)
    {
        boolean verify = false;
        Level debug = Level.OFF;
        int frag = 18;
        double step = 1;
        int firstQuery = 0;
        int lastQuery = 1;
        int pathLength = 0;
        String forPrint = "";

        String resultsFileName = "D:/data/dna/dresult.txt";
        String queryFileName = "D:/data/dna/arab/arab1.con";
        String indexName = "D:/data/dna/index/dindex-1000";

        TableManager tm = TableManager.getTableManager(indexName);
        Table dataTable = tm.getTable(indexName);
        Index index;
        if (dataTable != null)
            index = dataTable.getIndex();
        else
            throw new Error("index: " + indexName + " does not exist");

        Table queryTable = null;
        try
        {
            queryTable = new DNATable(queryFileName, "", lastQuery, frag);
        } catch (IOException e1)
        {
            e1.printStackTrace();
        }

        QueryVPIndex evaluator = new QueryVPIndex(index, queryTable, radius,
                radius, step, verify, debug, pathLength, frag, resultsFileName,
                firstQuery, lastQuery, forPrint);
        evaluator.outputToFile = true;
        evaluator.evaluate();
        
    }
}
