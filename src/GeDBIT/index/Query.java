/**
 * GeDBIT.index.Query 2006.05.09
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.05.09: Modified from the original GeDBIT package, Predicate.java, by Rui Mao
 */

package GeDBIT.index;

import GeDBIT.type.IndexObject;

/**
 * Base interface for queries, consisting of all the information necessary to
 * search the database.
 * 
 * @author Rui Mao, Willard
 * @version 2006.05.09
 */
public interface Query {
    /**
     * @return the query object.
     */
    IndexObject getQueryObject();
}
