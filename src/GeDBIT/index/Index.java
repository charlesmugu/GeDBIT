/**
 * GeDBIT.index.Index 2006.05.09
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.05.09: Created, by Rui Mao, Willard
 */

package GeDBIT.index;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import GeDBIT.type.IndexObject;
import GeDBIT.dist.Metric;

/**
 * The primary interface for distance-based index. Through Index, the user can build a database
 * index, or read a pre-built index from a file, and then do the search.
 * 
 * @author Rui Mao, Willard Saint Willard
 * @version 2006.05.09
 */
public interface Index extends Serializable {

    /**
     * @return the metric used to build the index.
     */
    public Metric getMetric();

    /**
     * @return the total number of data objects contained in the index.
     */
    public int size();

    /**
     * When a lot of queries are to be answered, for the sake of performance, it is a good idea to
     * have the top levels of the index tree reside in memory. This method loads the top levels into
     * memory.
     * 
     * @param level
     *        number of levels, from the root, to be pre-loaded into memory.
     */
    public void preLoad(int level);

    /**
     * @param query
     * @param radius
     *        the radius of the range query.
     * @return
     */

    /**
     * Executes a range query. For a range query (q,r), the results should be all database points
     * satisfying d(q,x)<=r.
     * 
     * @param query
     *        the {@link Query} object
     * @return a {@link Cursor} over the results of the query
     */
    public Cursor search(Query query);

    /**
     * Returns all the data points in the index
     */
    List<IndexObject> getAllPoints();

    /**
     * Closes anything used internally that needs to be closed. It is a good idea to close it when
     * it is no longer in use.
     * 
     * @throws IOException
     */
    public void close() throws IOException;

    /**
     * Deletes the index from the file, and also release it from memory. This method should only be
     * called when the index will never be used again!
     * 
     * @throws IOException
     */
    public void destroy() throws IOException;
}