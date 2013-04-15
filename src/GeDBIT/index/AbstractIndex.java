/**
 * GeDBIT.index.AbstractIndex 2006.05.09
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.05.09: Created, by Rui Mao, Willard
 */
package GeDBIT.index;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import GeDBIT.dist.Metric;
import GeDBIT.type.IndexObject;
import GeDBIT.util.MckoiObjectIOManager;
import GeDBIT.util.ObjectIOManager;

/**
 * The primary interface for distance-based index. Through AbstractIndex, the user can build a database
 * index, or read a pre-built index from a file, and then do the search.
 * 
 * @author Rui Mao, Willard
 * @version 2006.07.24
 */
public abstract class AbstractIndex implements Index {
    static final long                   serialVersionUID       = 2659219513493819541L;

    // name of the index node file
    protected String          fileName;
    transient protected static String   indexFileNameExtension = "index";
    transient protected static String   nodeFileNameExtension  = "000";
    transient protected ObjectIOManager oiom;
    transient protected Logger          logger;
    // metric that this index is built over.
    protected Metric                    metric;
    // total number of data objects
    protected int                       total_size;
    // address of the index root node in the index node file
    protected long                      root;

    // parallel computing support
    private int                         subtree = 0;
    transient protected ObjectIOManager[] oioms = null;
    private boolean                     flag = false;
    
    public AbstractIndex(String fileName, List<? extends IndexObject> data, Metric metric,
            Level debugLevel) {
        if (fileName == null)
            throw new IllegalArgumentException("fileName cannot be null!");
        if (metric == null)
            throw new IllegalArgumentException("metric cannot be null!");
        if (data == null)
            throw new IllegalArgumentException("data list cannot be null!");
        if (debugLevel == null) {
            System.err.println("using default Debug Level: java.util.logging.Level.INFO");
            debugLevel = Level.INFO;
        }
        this.flag = false;
        this.fileName = fileName;
        this.metric = metric;
        total_size = data.size();

        // 1. create the io manager, writeable
        initOIOM(false);
        openOIOM();

        // 2. create the logger
        logger = Logger.getLogger("GeDBIT.index");
        // set the logger level;
        logger.setLevel(debugLevel);
        // init the Handler: currently use a console handler, in the future will probably use the
        // FileHandler, or both
        logger.addHandler(new ConsoleHandler());
        /*
         * try { logger.addHandler(new FileHandler(fileName+".log")); } catch (SecurityException e) {
         * e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }
         */
    }

    // parallel computing support
    public AbstractIndex(String fileName, List<? extends IndexObject> data,
            Metric metric, Level debugLevel, int subtree)
    {
        if (fileName == null)
            throw new IllegalArgumentException("fileName cannot be null!");
        if (metric == null)
            throw new IllegalArgumentException("metric cannot be null!");
        if (data == null)
            throw new IllegalArgumentException("data list cannot be null!");
        if (debugLevel == null)
        {
            System.err
                    .println("using default Debug Level: java.util.logging.Level.INFO");
            debugLevel = Level.INFO;
        }
        
        this.flag = true;
        this.fileName = fileName;
        this.metric = metric;
        this.subtree = subtree;
        
        total_size = data.size();
        // 1. create the io manager, writeable
        if(oioms == null) {
            oioms = new MckoiObjectIOManager[subtree + 1];
        }
        for (int i = 0; i < subtree + 1; i++)
        {
            initOIOM(false, i);
            openOIOM(i);
        }
        
        // 2. create the logger
        logger = Logger.getLogger("GeDBIT.index");
        // set the logger level;
        logger.setLevel(debugLevel);
        // init the Handler: currently use a console handler, in the future will
        // probably use the
        // FileHandler, or both
        logger.addHandler(new ConsoleHandler());
        /*
         * try { logger.addHandler(new FileHandler(fileName+".log")); } catch
         * (SecurityException e) { e.printStackTrace(); } catch (IOException e)
         * { e.printStackTrace(); }
         */
    }
    
    protected void initOIOM(boolean readOnly) {
        oiom = new MckoiObjectIOManager(fileName, nodeFileNameExtension, 1024 * 1024 * 1024,
                "Java IO", 4, 128 * 1024, readOnly);
    }

    protected void initOIOM(boolean readOnly, int i)
    {
        oioms[i] = new MckoiObjectIOManager(fileName + "-" + i,
                nodeFileNameExtension, 1024 * 1024 * 1024, "Java IO", 4,
                128 * 1024, readOnly);
    }
    
    protected void openOIOM() {
        String indexFileName = fileName + "." + indexFileNameExtension;
        String nodeFileName = fileName + "." + nodeFileNameExtension;
        try {
            if (!oiom.open()) {
                throw new Error("Cannot open store for " + indexFileName + ":" + nodeFileName);
            }
            //System.out.println("OIOM.size = " + oiom.size() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void openOIOM(int i)
    {
        String indexFileName = fileName + "-" + i + "." + indexFileNameExtension;
        String nodeFileName = fileName + "-" + i + "." + nodeFileNameExtension;
        try
        {
            if (!oioms[i].open())
            {
                throw new Error("Cannot open store for " + indexFileName + ":"
                        + nodeFileName);
            }
            // System.out.println("OIOM.size = " + oiom.size() + "\n");
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.index.Index#getMetric()
     */
    public Metric getMetric() {
        return this.metric;
    }

    public int getSubtree()
    {
        return this.subtree;
    }
    
    public boolean getFlag()
    {
        return flag;
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.index.Index#size()
     */
    public int size() {
        return total_size;
    }


    /* (non-Javadoc)
     * @see GeDBIT.index.Index#preLoad(int)
     */
    public void preLoad(int level) {
        double startTime = 0;
        double endTime = 0;
        long memoryUsed = 0;
        System.out.println("Start the preloading database into memory");
        Runtime rt = Runtime.getRuntime();
        memoryUsed = rt.totalMemory() - rt.freeMemory();
        startTime = System.currentTimeMillis();
        // hashing internal nodes first
        LinkedList<PreLoadTask> nodeList = new LinkedList<PreLoadTask>();
        PreLoadTask task = new PreLoadTask(root, 0);
        nodeList.add(task);
        int numPreLoadedNodes = 0;
        while (!nodeList.isEmpty()) {
            task = nodeList.removeFirst();
            Object node = null;
            try {
                node = oiom.readPersistObject(task.nodeAddress);
                numPreLoadedNodes++;
                if (task.currentLevel < level) {
                    if (node instanceof InternalNode) {
                        for (int i = 0; i < ((InternalNode) node).childAddresses.length; i++)
                            nodeList.add(new PreLoadTask(((InternalNode) node).childAddresses[i],
                                    task.currentLevel + 1));
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        endTime = (System.currentTimeMillis() - startTime) / 1000;
        rt = Runtime.getRuntime();
        memoryUsed = rt.totalMemory() - rt.freeMemory() - memoryUsed;
        System.out.println("finish the preloading database into memory " + numPreLoadedNodes
                + " nodes loaded using " + endTime + " seconds " + memoryUsed + " byte memory");
    }

    private class PreLoadTask {
        private long nodeAddress;
        private int  currentLevel;

        private PreLoadTask(long nodeAddress, int currentLevel) {
            this.nodeAddress = nodeAddress;
            this.currentLevel = currentLevel;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.index.Index#getAllPoints()
     */
    public List<IndexObject> getAllPoints() {
        return Cursor.getAllPoints(oiom, root);
    }

    public int getStoredDistanceNumber() {
        return Cursor.getStoredDistanceNumber(oiom, root);
    }

    public int getStoredDistanceNumbers()
    {
        return Cursor.getStoredDistanceNumbers(oioms, root, subtree);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.index.Index#close()
     */
    public void close() throws IOException {
        oiom.close();
    }

    public void close(int i) throws IOException
    {
        oioms[i].close();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.index.Index#destroy()
     */
    public void destroy() throws IOException {
        if(flag)
        {
            for (int i = 0; i < subtree + 1; i++)
            {
                oioms[i].close();
                // delete the oiom
                boolean success = (new File(fileName + "-" + i + "." + nodeFileNameExtension)).delete();
                if (!success) {
                    throw new IOException("Deletion failed.");
                }
            }
        } else {
            oiom.close();
            // delete the oiom
            boolean success = (new File(fileName + nodeFileNameExtension)).delete();
            if (!success) {
                throw new IOException("Deletion failed.");
            }
            // delete the index file
            success = (new File(fileName + indexFileNameExtension)).delete();
            if (!success) {
                throw new IOException("Deletion failed.");
            }
        }
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        if(flag)
        {
            if(oioms == null) {
                oioms = new MckoiObjectIOManager[subtree + 1];
            }
            
            for(int i=0; i < subtree; i++)
            {
                File file = new File(fileName + "-" + i + "." + nodeFileNameExtension);
                if(file.exists())
                {
                    initOIOM(true, i);
                    openOIOM(i);
                }
            }
        } else {
            initOIOM(true);
            openOIOM();
        }
    }
}
