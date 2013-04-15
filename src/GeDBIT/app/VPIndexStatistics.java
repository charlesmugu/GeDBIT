/**
 * GeDBIT.app.QueryEvaluator 2006.06.27
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.06.27: Copied from jdb 1.0, by Rui Mao
 */
package GeDBIT.app;

import GeDBIT.index.Index;
import GeDBIT.index.TableManager;
import GeDBIT.type.Table;

/**
 * 
 * @author Rui Mao
 * @version 2007.08.02
 */
public class VPIndexStatistics
{
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        // arguments and default values
        String indexName = "";

        // parse arguments, and set values
        for (int i = 0; i < args.length; i = i + 2)
        {
            if (args[i].equalsIgnoreCase("-d"))
                indexName = args[i + 1];

            else
                throw new IllegalArgumentException("Invalid option " + args[i]);
        }

        // check arguments
        if (indexName == "")
            throw new IllegalArgumentException("Invalid Index file name!");

        // load index from file
        Table dataTable = TableManager.getTableManager(indexName).getTable(indexName);
        Index index;
        if (dataTable != null)
            index = dataTable.getIndex();
        else
            throw new Error("index: " + indexName + " does not exist");
        
        System.out.println( ((GeDBIT.index.AbstractIndex)index).getStoredDistanceNumber());
    }
}
