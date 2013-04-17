/**
 * GeDBIT.mckoi.store.AbstractBufferedFile  24 Jan 2003
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
 * 
 * 
 */

package GeDBIT.mckoi.store;

import java.io.RandomAccessFile;
import java.io.IOException;

/**
 * An abstract implementation of a FileBufferAccessor that implements a paging
 * strategy for caching access to the underlying RandomAccessFile. This
 * abstraction is designed such that the actual mechanism for fetching and
 * caching the pages are left to derived classes. This allows us to implement a
 * paging system that can exploit the Java 1.4 NIO memory mapping facilities as
 * well as an implementation that remains compatible with the 1.2 API.
 * 
 * @author Tobias Downer
 */

abstract class AbstractBufferedFile implements FileBufferAccessor {

    /**
     * The size of the hashmap.
     */
    private static final int HASH_SIZE = 64;

    /**
     * A unique id given to this object by the buffer manager.
     */
    protected final int unique_id;

    /**
     * The RandomAccessFile whose access to we want to buffer.
     */
    protected final RandomAccessFile file;

    /**
     * A reference to the BufferManager object that manages the memory
     * allocation for the pages in this buffer.
     */
    protected final BufferManager buffer_manager;

    /**
     * The page size of pages in this buffered file.
     */
    protected final int page_size;

    /**
     * A hash containing the pages that are currently cached for this file.
     */
    private final PageBuffer[] page_map;

    /**
     * A lock mutex used when accessing the page_map.
     */
    private final Object page_map_lock = new Object();

    /**
     * Constructs the buffered file.
     */
    public AbstractBufferedFile(int unique_id, RandomAccessFile file,
	    BufferManager manager) {
	this.unique_id = unique_id;
	this.file = file;
	this.buffer_manager = manager;
	this.page_size = buffer_manager.getPageSizeFor(unique_id, file);
	page_map = new PageBuffer[HASH_SIZE];
    }

    /**
     * Returns a new PageBuffer implementation that contains the information in
     * the given page in the file. For example, if the page size is 4096 and the
     * page number is 0, this will return a buffer that contains all the
     * information between position 0 and 4095 in the file.
     */
    protected abstract PageBuffer createPageFor(long position, int length);

    /**
     * Asks if the given page is in the page map and if it is returns it, or
     * otherwise returns null.
     */
    protected final PageBuffer getPageFromCache(long page_number) {
	synchronized (page_map_lock) {
	    // The (very simple) hashing algorithm.
	    int hash_index = (int) (page_number % HASH_SIZE);
	    PageBuffer page = page_map[hash_index];
	    PageBuffer prev = null;
	    // Search the hash key list for the page
	    while (page != null && page.getPageNumber() != page_number) {
		prev = page;
		page = page.next_page_in_hash;
	    }
	    // If the page is found then move the page to the start of the list.
	    if (prev != null && page != null) {
		// Move this page to the start of this hash key
		prev.next_page_in_hash = page.next_page_in_hash;
		page.next_page_in_hash = page_map[hash_index];
		page_map[hash_index] = page;
	    }
	    return page;
	}
    }

    /**
     * Puts the given page into the cache.
     */
    private final void putPageInCache(long page_number, PageBuffer page) {
	synchronized (page_map_lock) {
	    // The (very simple) hashing algorithm.
	    int hash_index = (int) (page_number % HASH_SIZE);
	    page.next_page_in_hash = page_map[hash_index];
	    page_map[hash_index] = page;
	}
    }

    /**
     * Fetches the PageBuffer for the given page from the underlying file. If
     * the page is cached it is fetched from the cache. If it's not cached the
     * page is created and added to the cache. This always adds a new reference
     * to the reference count.
     */
    private final PageBuffer fetchPage(long page_num_val) throws IOException {

	boolean page_created = false;
	PageBuffer page;

	synchronized (page_map_lock) {
	    // Get the page from the cache
	    page = getPageFromCache(page_num_val);
	    // If it's not in the cache ...
	    if (page == null) {
		// ... then create it
		page = createPageFor(page_num_val * page_size, page_size);
		page.setPageNumber(page_num_val);
		page.setFileUniqueID(unique_id);
		// Add 2 to the reference count
		// 1 reference for the cache and one for the fetch
		page.referenceAdd();
		page.referenceAdd();
		// and put it in the cache
		putPageInCache(page_num_val, page);
		page_created = true;
	    } else {
		// This will cause a block until other read/write operations on
		// the
		// page have completed.
		synchronized (page) {
		    // If the page is not in use then setup an initial
		    // reference.
		    if (page.notInUse()) {
			page.referenceAdd();
			page_created = true;
		    }
		    // Make a reference for this fetch
		    page.referenceAdd();
		}
	    }

	}

	// Notify the buffer manager that this page has been created or has been
	// accessed.
	if (page_created) {
	    buffer_manager.pageCreated(page);
	} else {
	    buffer_manager.pageAccessed(page);
	}

	return page;
    }

    /**
     * Reads a single byte from this buffered file at the given position.
     */
    public final int readByte(long position) throws IOException {
	final long page_number = position / page_size;
	int v;

	PageBuffer page = fetchPage(page_number);
	synchronized (page) {
	    try {
		page.initialize();
		v = ((int) page.read((int) (position % page_size))) & 0x0FF;
	    } finally {
		page.dispose();
	    }
	}

	return v;
    }

    /**
     * Reads an array from this buffered file at the given position.
     */
    public final int readByteArray(long position, byte[] buf, int off, int len)
	    throws IOException {

	final int orig_len = len;
	long page_number = position / page_size;
	int start_offset = (int) (position % page_size);
	int to_read = Math.min(len, page_size - start_offset);

	PageBuffer page = fetchPage(page_number);
	synchronized (page) {
	    try {
		page.initialize();
		page.read(start_offset, buf, off, to_read);
	    } finally {
		page.dispose();
	    }
	}

	len -= to_read;
	while (len > 0) {
	    off += to_read;
	    position += to_read;
	    ++page_number;
	    to_read = Math.min(len, page_size);

	    page = fetchPage(page_number);
	    synchronized (page) {
		try {
		    page.initialize();
		    page.read(0, buf, off, to_read);
		} finally {
		    page.dispose();
		}
	    }
	    len -= to_read;
	}

	return orig_len;
    }

    /**
     * Writes a single byte to this buffered file at the given position.
     */
    public final void writeByte(long position, byte val) throws IOException {
	final long page_number = position / page_size;

	PageBuffer page = fetchPage(page_number);
	synchronized (page) {
	    try {
		page.initialize();
		page.write((int) (position % page_size), val);
	    } finally {
		page.dispose();
	    }
	}

    }

    /**
     * Writes an array to the buffered file at the given position.
     */
    public final int writeByteArray(long position, byte[] buf, int off, int len)
	    throws IOException {

	final int orig_len = len;
	long page_number = position / page_size;
	int start_offset = (int) (position % page_size);
	int to_write = Math.min(len, page_size - start_offset);

	PageBuffer page = fetchPage(page_number);
	synchronized (page) {
	    try {
		page.initialize();
		page.write(start_offset, buf, off, to_write);
	    } finally {
		page.dispose();
	    }
	}
	len -= to_write;

	while (len > 0) {
	    off += to_write;
	    position += to_write;
	    ++page_number;
	    to_write = Math.min(len, page_size);

	    page = fetchPage(page_number);
	    synchronized (page) {
		try {
		    page.initialize();
		    page.write(0, buf, off, to_write);
		} finally {
		    page.dispose();
		}
	    }
	    len -= to_write;
	}

	return orig_len;

    }

    /**
     * Flushes any pending bytes that were written to the buffer to the
     * underlying disk.
     */
    public final void flush() throws IOException {
	synchronized (page_map_lock) {
	    for (int i = 0; i < HASH_SIZE; ++i) {
		PageBuffer prev = null;
		PageBuffer page = page_map[i];
		while (page != null) {
		    synchronized (page) {
			// Flush the page
			page.flush();
			// Remove this page if it is not in use
			if (page.notInUse()) {
			    if (prev == null) {
				page_map[i] = page.next_page_in_hash;
			    } else {
				prev.next_page_in_hash = page.next_page_in_hash;
			    }
			}
		    }
		    prev = page;
		    page = page.next_page_in_hash;
		}
	    }
	}
    }

    /**
     * Notifies this file that the underlying RandomAccessFile has changed size.
     * This would normally cause the last page in the cache to be purged but the
     * behaviour may be different depending on the implementation.
     */
    public abstract void sizeChange(long old_size, long new_size)
	    throws IOException;

}
