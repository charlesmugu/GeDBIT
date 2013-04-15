package GeDBIT.web;

import java.io.IOException;
import java.util.logging.Level;

import GeDBIT.app.QueryVPIndex;
import GeDBIT.index.Index;
import GeDBIT.index.TableManager;
import GeDBIT.type.DoubleVectorTable;
import GeDBIT.type.Table;

public class VectorQuery
{

    public void runQuery(double radius)
    {
        String resultsFileName = "D:/data/vector/vresult.txt";
        String queryFileName = "D:/data/vector/uniformvector-20dim-1m.txt";
        String indexName = "D:/data/vector/index/vindex-1000";
        boolean verify = false;
        Level debug = Level.OFF;
        int frag = 6;
        int dim = 3;
        double step = 0.1;
        int firstQuery = 0;
        int lastQuery = 1;
        int pathLength = 0;
        String forPrint = "";

        Table dataTable = TableManager.getTableManager(indexName).getTable(indexName);
        Index index;
        if (dataTable != null)
            index = dataTable.getIndex();
        else
            throw new Error("index: " + indexName + " does not exist");
        Table queryTable = null;
        try
        {
            queryTable = new DoubleVectorTable(queryFileName, "", lastQuery,
                    dim);
        } catch (IOException e1)
        {
            e1.printStackTrace();
        }

        QueryVPIndex evaluator = new QueryVPIndex(index, queryTable, radius,
                radius, step, verify, debug, pathLength, frag,
                resultsFileName, firstQuery, lastQuery, forPrint);
        evaluator.outputToFile = true;
        evaluator.evaluate();
    }
    public void runqq(double radius)
    {
    	
    }
    

}
