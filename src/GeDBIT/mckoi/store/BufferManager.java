/**
 * edu.utexas.GeDBIT.mckoi.store.BufferManager  27 Jan 2003
 *
 * Mckoi SQL Database ( http://www.mckoi.com/database )
 * Copyright (C) 2000, 2001, 2002  Diehl and Associates, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * Version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License Version 2 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 2 along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Change Log:
 * 2005.05.24: Added new methods to compute buffer statistics, by Willard
 * 
 */

package GeDBIT.mckoi.store;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * A centralized manager that manages one or more AbstractBufferedFile
 * implementstions. This object manages memory usage of the children buffers and
 * implements a caching strategy for purging old pages from memory.
 * 
 * @author Tobias Downer
 */

@SuppressWarnings("rawtypes")
public final class BufferManager {

    /**
     * A timer that represents the T value in buffer pages.
     */
    private long current_T;

    /**
     * The complete list of PageBuffer objects that have been created by the
     * children file buffers.
     */
    private ArrayList page_list;

    /**
     * A unique id key counter given to AbstractBufferedFile implementations
     * created for this manager.
     */
    private int unique_id_seq;

    /**
     * The paging implementation (either 'Java IO' or 'Java NIO') to use.
     */
    private final String paging_implementation;

    /**
     * The maximum number of pages that should be kept in memory before pages
     * are purged from memory.
     */
    private final int max_pages;

    /**
     * The size of each page.
     */
    private final int page_size;

    /**
     * The current number of pages that are in memory.
     */
    private int current_page_count;

    /**
     * The number of cache hits.
     */
    private long cache_hit_count;

    /**
     * The number of cache misses.
     */
    private long cache_miss_count;

    /**
     * Added by Willard, 2005.05.24
     */
    @SuppressWarnings("unused")
    private PageBuffer page_with_root;

    /**
     * Added by Willard, 2005.05.24
     */
    private ArrayList write_times;

    /**
     * Added by Willard, 2005.05.24
     */
    private ArrayList read_times;

    /**
     * Constructs the manager.
     */
    public BufferManager(String paging_implementation, int max_pages,
	    int page_size) {
	this.paging_implementation = paging_implementation;
	this.max_pages = max_pages;
	this.page_size = page_size;

	current_T = 0;
	page_list = new ArrayList();
	unique_id_seq = 0;

	cache_hit_count = 0;
	cache_miss_count = 0;

	page_with_root = null;
	write_times = new ArrayList();
	read_times = new ArrayList();
    }

    /**
     * Creates and returns a FileBufferAccess object that is used to access the
     * RandomAccessFile through this buffer manager.
     */
    public synchronized FileBufferAccessor createBufferedAccessor(
	    RandomAccessFile file, String mode) throws IOException {
	int id_val = unique_id_seq;
	++unique_id_seq;

	if (paging_implementation.equals("Java IO")) {
	    return new IOBufferedFile(id_val, file, this);
	} else if (paging_implementation.equals("Java NIO")) {
	    return new NIOBufferedFile(id_val, file, mode, this);
	} else {
	    throw new RuntimeException(
		    "No not know how to create a buffered accessor for paging "
			    + "implementation: " + paging_implementation);
	}
    }

    /**
     * Returns the page size to use for the given RandomAccessFile with the
     * given unique id.
     */
    int getPageSizeFor(int unique_id, RandomAccessFile file) {
	return page_size;
    }

    /**
     * A callback from an AbstractBufferedFile notifying the manager that a new
     * page has been created and added to the page cache.
     */
    @SuppressWarnings("unchecked")
    synchronized void pageCreated(PageBuffer buffer) throws IOException {
	cacheMiss();

	buffer.setT(current_T);
	++current_T;

	++current_page_count;
	page_list.add(buffer);
	// Below is the page purge algorithm. If the maximum number of pages
	// has been created we sort the page list weighting each page by time
	// since
	// last accessed and total number of accesses and clear the bottom 25%
	// of this list.

	// Check if we should purge old pages and purge some if we do...
	if (current_page_count > max_pages) {
	    write_times.add(new Double(System.currentTimeMillis()));
	    // keep root node in cache

	    /*
	     * if (page_with_root == null) { int size = page_list.size();
	     * PageBuffer page; PageBuffer root_page = (PageBuffer)
	     * page_list.get(0); int root_page_location = 0; for (int i = 1; i <
	     * size; i++) { page = (PageBuffer) page_list.get(i); if
	     * (page.getPageNumber() > root_page.getPageNumber()) { root_page =
	     * page; root_page_location = i; } } //page_with_root = //
	     * (PageBuffer)page_list.remove(root_page_location); page_with_root
	     * = (PageBuffer) page_list.get(root_page_location);
	     * System.out.println("number of pages:" +
	     * page_with_root.getPageNumber()); }
	     */

	    // Purge 25% of the cache
	    // Sort the pages by the current formula,
	    // ( 1 / page_access_count ) * (current_t - page_t)
	    // See the 'pageEnumValue' value for weighting algorithm
	    // implementation.
	    Object[] pages = page_list.toArray();
	    Arrays.sort(pages, new PageCacheComparator());
	    int purge_size = Math.max((int) (pages.length * 0.20f), 2);

	    for (int i = 0; i < purge_size; ++i) {
		PageBuffer page = (PageBuffer) pages[pages.length - (i + 1)];
		synchronized (page) {
		    page.dispose();
		}
	    }

	    // System.out.println("Clearing " + purge_size + " pages.");
	    /*
	     * System.out.print("Top pages = "); for (int n = 0; n < 32; ++n) {
	     * System.out.print(pages[n]); System.out.print(", "); }
	     * System.out.println(); System.out.println("Cache hits: " +
	     * cache_hit_count); System.out.println("Cache misses: " +
	     * cache_miss_count);/* System.out.print("Bottom pages = "); for
	     * (int n = pages.length-1; n > 8; n--) {
	     * System.out.print(pages[n]); System.out.print(", "); }
	     */

	    // Remove all the elements from page_list and set it with the sorted
	    // list (minus the elements we removed).
	    page_list.clear();
	    for (int i = 0; i < pages.length - purge_size; ++i) {
		page_list.add(pages[i]);
	    }

	    current_page_count -= purge_size;
	    write_times.add(new Double(System.currentTimeMillis()));
	}

    }

    /**
     * A callback from an AbstractBufferedFile notifying the manager that a page
     * has been accessed.
     */
    void pageAccessed(PageBuffer buffer) {
	synchronized (this) {
	    cacheHit();
	    buffer.setT(current_T);
	    ++current_T;
	    buffer.incrementAccessCounter();
	}
    }

    /**
     * Updates the cache hit statistic.
     */
    final void cacheHit() {
	synchronized (this) {
	    ++cache_hit_count;
	    // System.out.print(" cache hit: "+cache_hit_count+"
	    // cur_pages_in_memory: "+current_page_count);
	}
    }

    /**
     * Updates the cache miss statistic.
     */
    final void cacheMiss() {
	synchronized (this) {
	    ++cache_miss_count;
	}
    }

    /**
     * The calculation for finding the 'weight' of a page in the cache. A
     * heavier page is sorted lower and is therefore cleared from the cache
     * faster.
     */
    private final float pageEnumValue(PageBuffer page) {
	// We fix the access counter so it can not exceed 10000 accesses. I'm
	// a little unsure if we should put this constant in the equation but it
	// ensures that some old but highly accessed page will not stay in the
	// cache forever.
	return (1f / Math.min(page.getAccessCounter(), 10000))
		* (current_T - page.getT());
    }

    // ---------- Inner classes ----------

    /**
     * A Comparator used to sort cache entries.
     */
    private class PageCacheComparator implements Comparator {

	public int compare(Object ob1, Object ob2) {
	    float v1 = pageEnumValue((PageBuffer) ob1);
	    float v2 = pageEnumValue((PageBuffer) ob2);
	    if (v1 > v2) {
		return 1;
	    } else if (v1 < v2) {
		return -1;
	    }
	    return 0;
	}

    }

    /**
     * @return Returns the cache_hit_count.
     */
    public long getCache_hit_count() {
	return cache_hit_count;
    }

    /**
     * @return Returns the cache_miss_count.
     */
    public long getCache_miss_count() {
	return cache_miss_count;
    }

    /**
     * @param l
     */
    @SuppressWarnings("unchecked")
    public void addTime(long l) {

	read_times.add(new Double(l));

    }

    private double total_write_time;
    private double avg_write_time;
    private double total_read_time;
    private double avg_read_time;
    private double highest_read_time;
    private double lowest_read_time;
    private double total_i_o;

    @SuppressWarnings("unchecked")
    private void computeStatistics() {
	int write_times_length = write_times.size();
	int read_times_length = read_times.size();
	ArrayList put_times = new ArrayList(write_times_length / 2);
	ArrayList get_times = new ArrayList(read_times_length / 2);

	// get read times//
	double start_read = 0;
	double end_read = 0;
	for (int i = 0; i < read_times_length; i += 2) {
	    start_read = ((Double) read_times.get(i)).doubleValue();
	    end_read = ((Double) read_times.get(i + 1)).doubleValue();
	    get_times.add(new Double(end_read - start_read));
	}
	total_read_time = 0;
	int get_length = get_times.size();
	highest_read_time = 0;
	lowest_read_time = 1000000;
	double this_read_time;
	for (int i = 0; i < get_length; i++) {
	    this_read_time = ((Double) get_times.get(i)).doubleValue();
	    if (this_read_time > highest_read_time) {
		highest_read_time = this_read_time;
	    }
	    if (this_read_time < lowest_read_time) {
		lowest_read_time = this_read_time;
	    }
	    total_read_time += this_read_time;
	}
	avg_read_time = total_read_time / get_length;

	// get write times//
	if (write_times_length > 0) {
	    double start_write = 0;
	    double end_write = 0;
	    for (int i = 0; i < write_times_length; i += 2) {
		start_write = ((Double) write_times.get(i)).doubleValue();
		end_write = ((Double) write_times.get(i + 1)).doubleValue();
		put_times.add(new Double(end_write - start_write));
	    }
	    total_write_time = 0;
	    int put_length = put_times.size();
	    for (int i = 0; i < put_length; i++) {
		total_write_time += ((Double) put_times.get(i)).doubleValue();
	    }
	    avg_write_time = total_write_time / put_length;
	    total_i_o = total_read_time + total_write_time;
	} else {
	    total_write_time = 0;
	    avg_write_time = 0;
	    total_i_o = total_read_time;
	}
    }

    /**
     * Added by Willard, 2005.05.24
     */
    public void printStatistics() {
	computeStatistics();
	System.out.println("Hits: " + (cache_hit_count));
	System.out.println("Misses: " + (cache_miss_count));
	System.out.println("Total Write Time:" + (total_write_time / 1000)
		+ " seconds.");
	System.out.println("Avg Write Time:" + (avg_write_time / 1000)
		+ " seconds.");
	System.out.println("Total Read Time:" + (total_read_time / 1000)
		+ " seconds.");
	System.out.println("Avg Read Time:" + (avg_read_time / 1000)
		+ " seconds.");
	System.out.println("Highest Read Time:" + (highest_read_time / 1000)
		+ " seconds.");
	System.out.println("Lowest Read Time:" + (lowest_read_time / 1000)
		+ " seconds.");
	System.out
		.println("Total I/O Time:" + (total_i_o / 1000) + " seconds.");
    }
}
