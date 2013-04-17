/**
 * edu.utexas.GeDBIT.mckoi.store.PageBuffer  24 Jan 2003
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

import java.io.IOException;

/**
 * Abstract class that defines a page of data from the underlying file. We
 * should be able to read and write data to/from a PageBuffer with as little
 * overhead as possible.
 * <p>
 * This class is used by implementations of AbstractBufferedFile and the
 * BufferManager.
 * 
 * @author Tobias Downer
 */

abstract class PageBuffer {

    /**
     * When stored in a page hash map for a file, points to the next page with
     * this hash key.
     */
    PageBuffer next_page_in_hash;

    /**
     * The unique id of the file this page is part of.
     */
    private int file_unique_id;

    /**
     * The page number that this represents in the buffer.
     */
    private long page_number;

    /**
     * The time this page was last accessed. This value is reset each time the
     * page is requested.
     */
    private long t;

    /**
     * The number of times this page has been accessed since it was created.
     */
    private int access_count;

    /**
     * The first position in the buffer that was last written.
     */
    private int first_write_position;

    /**
     * The last position in the buffer that was last written.
     */
    private int last_write_position;

    /**
     * The number of references on this page.
     */
    private int reference_count;

    /**
     * Set to true when the page is initialized.
     */
    private boolean initialized;

    /**
     * Constructs the page.
     */
    public PageBuffer() {
	reference_count = 0;
	initialized = false;
    }

    /**
     * Sets the t value (the time when this buffer was last accessed).
     */
    final void setT(long t) {
	this.t = t;
    }

    /**
     * Returns t (the last time this page was accessed).
     */
    final long getT() {
	return t;
    }

    /**
     * Increment the access counter.
     */
    final void incrementAccessCounter() {
	++access_count;
    }

    /**
     * Return the current access counter (the number of accesses to this page).
     */
    final int getAccessCounter() {
	return access_count;
    }

    /**
     * Sets the page number for this page.
     */
    final void setPageNumber(long page_number) {
	this.page_number = page_number;
    }

    /**
     * Returns the page number for this page.
     */
    final long getPageNumber() {
	return page_number;
    }

    /**
     * Sets the unique_id of the file this page is part of.
     */
    final void setFileUniqueID(int unique_id) {
	this.file_unique_id = unique_id;
    }

    /**
     * Gets the unique_id of the file this page is part of.
     */
    final int getFileUniqueID() {
	return file_unique_id;
    }

    /**
     * Adds 1 to the reference counter on this page.
     */
    final void referenceAdd() {
	++reference_count;
    }

    /**
     * Removes 1 from the reference counter on this page.
     */
    final void referenceRemove() {
	--reference_count;
    }

    /**
     * Returns true if this PageBuffer is not in use (has 0 reference count and
     * is not inialized.
     */
    final boolean notInUse() {
	return (reference_count <= 0 && !initialized);
    }

    /**
     * Initializes the page buffer. If the buffer is already initialized then we
     * just return. If it's not initialized we set up any internal structures
     * that are required to be set up for access to this page.
     */
    final void initialize() throws IOException {
	if (!initialized) {
	    initImpl();
	    initialized = true;

	    access_count = 0;
	    first_write_position = Integer.MAX_VALUE;
	    last_write_position = -1;

	}
    }

    /**
     * Disposes of the page buffer if it can be disposed (there are no
     * references to the page and the page is initialized). When disposed any
     * internal resources are reclaimed. The page may later be initialized again
     * if required.
     */
    final void dispose() throws IOException {
	referenceRemove();
	if (reference_count <= 0) {
	    if (initialized) {
		flush();
		disposeImpl();
		initialized = false;
	    } else {
		throw new RuntimeException(
			"Assertion failed: tried to dispose an uninitialized page.");
	    }
	}
    }

    /**
     * Returns a byte from this page at the given position.
     */
    final byte read(int position) throws IOException {
	return readImpl(position);
    }

    /**
     * Reads a byte array from this page and copies the information into the
     * buffer at the given offset.
     */
    final void read(int position, byte[] buf, int off, int len)
	    throws IOException {
	readImpl(position, buf, off, len);
    }

    /**
     * Writes a byte to the given position in this page.
     */
    final void write(int position, byte val) throws IOException {
	first_write_position = Math.min(position, first_write_position);
	last_write_position = Math.max(position + 1, last_write_position);

	writeImpl(position, val);
    }

    /**
     * Writes a byte array to this page at the given position.
     */
    final void write(int position, byte[] buf, int off, int len)
	    throws IOException {
	first_write_position = Math.min(position, first_write_position);
	last_write_position = Math.max(position + len, last_write_position);

	writeImpl(position, buf, off, len);
    }

    /**
     * Flushes the information buffered by this page to the disk. This may write
     * nothing if it is determined that no data in the page was changed.
     */
    final void flush() throws IOException {
	if (initialized) {
	    if (last_write_position >= 0) {
		flushImpl(first_write_position, last_write_position);
		first_write_position = Integer.MAX_VALUE;
		last_write_position = -1;
	    }
	}
    }

    public String toString() {
	StringBuffer buf = new StringBuffer();
	buf.append('[');
	buf.append(page_number);
	buf.append('-');
	buf.append(access_count);
	buf.append('-');
	buf.append(t);
	buf.append(']');
	return new String(buf);
    }

    // ---------- Abstract methods ----------
    // These aren't commented because they are simply the implementation calls
    // from the above methods.

    protected abstract void initImpl() throws IOException;

    protected abstract byte readImpl(int position) throws IOException;

    protected abstract void readImpl(int position, byte[] buf, int off, int len)
	    throws IOException;

    protected abstract void writeImpl(int position, byte val)
	    throws IOException;

    protected abstract void writeImpl(int position, byte[] buf, int off, int len)
	    throws IOException;

    // Flush the buffer between p1 (inclusive) and p2 (exclusive)
    protected abstract void flushImpl(int p1, int p2) throws IOException;

    /**
     * Releases and disposes of any resources claimed by this object. This is
     * called when old cached items are removed from the cache.
     */
    protected abstract void disposeImpl() throws IOException;

}
