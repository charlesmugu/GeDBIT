/**
 * edu.utexas.GeDBIT.util.DataLoader 2006.05.24
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.05.24: Copied from the original jdb package, by Willard
 */
 
package GeDBIT.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import GeDBIT.type.Table;

/**
 * This is a general interface that can be used for loading data from files  
 * into programs.  The data from the file 
 * is read sequentially and the resulting objects are packaged into a 
 * {@link List}, which is returned by the method.
 *
 * @author Neha Singh, Rui Mao, Willard
 * @version 2005.10.31
 */
public interface DataLoader 
{
    /**
     * @return a {@link List} of all data objects contained in the {@link BufferedReader}
     */
    public Table loadData (BufferedReader reader) throws IOException ;

    /**
     * @return a {@link List} of first given number of data objects contained in the {@link BufferedReader}
     */
    public Table loadData (BufferedReader reader, int size)  throws IOException ;
}
