/**
 * edu.utexas.GeDBIT.mckoi.store.NIOBufferedFile  28 Jan 2003
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
import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;

/**
 * An implementation of AbstractBufferedFile that uses the NIO API to map pages
 * of the underlying file into memory.
 * 
 * @author Tobias Downer
 */

class NIOBufferedFile extends AbstractBufferedFile {

    /**
     * The FileChannel object.
     */
    private FileChannel access_file_channel;

    /**
     * The mode of the mapped byte buffer.
     */
    private FileChannel.MapMode map_mode;

    /**
     * The current size of the file.
     */
    private long file_size;

    /**
     * The constructor.
     */
    public NIOBufferedFile(int unique_id, RandomAccessFile file, String mode,
	    BufferManager manager) throws IOException {
	super(unique_id, file, manager);
	if (mode.equals("r")) {
	    map_mode = FileChannel.MapMode.READ_ONLY;
	} else if (mode.equals("rw")) {
	    map_mode = FileChannel.MapMode.READ_WRITE;
	} else {
	    throw new RuntimeException("Unrecognised access mode.");
	}
	file_size = file.length();
	access_file_channel = file.getChannel();
    }

    // ---------- Implemented from AbstractBufferedFile ----------

    public PageBuffer createPageFor(long position, int len) {
	return new NIOPageBuffer(position, len);
    }

    public void sizeChange(long old_size, long new_size) throws IOException {
	file_size = new_size;
	// If the store has a page buffer created over the given area, we
	// flush and reinitialize the page.
	NIOPageBuffer page_buffer = (NIOPageBuffer) getPageFromCache((old_size - 1)
		/ page_size);
	if (page_buffer != null) {
	    page_buffer.flushImpl(-1, -1);
	    page_buffer.initImpl();
	}
    }

    // ---------- Inner classes ----------

    /**
     * This implementation of a page buffer initializes the page by reading the
     * page into a byte[] array in memory.
     */
    class NIOPageBuffer extends PageBuffer {

	private long position;
	private int length;
	private MappedByteBuffer buffer;

	public NIOPageBuffer(long position, int length) {
	    this.position = position;
	    this.length = length;
	}

	public void initImpl() throws IOException {
	    int map_size = (int) Math.min(file_size - position, (long) length);
	    if (map_size < 0) {
		System.out.println("length = " + length);
		System.out.println("file_size = " + file_size);
		System.out.println("position = " + position);
	    }
	    buffer = access_file_channel.map(map_mode, position, map_size);
	}

	public byte readImpl(int position) {
	    return buffer.get(position);
	}

	public void readImpl(int position, byte[] b, int off, int len) {
	    buffer.position(position);
	    buffer.get(b, off, len);
	}

	public void writeImpl(int position, byte b) {
	    buffer.put(position, b);
	}

	public void writeImpl(int position, byte[] b, int off, int len) {
	    buffer.position(position);
	    buffer.put(b, off, len);
	}

	public void flushImpl(int p1, int p2) throws IOException {
	    buffer.force();
	}

	public void disposeImpl() {
	    buffer = null;
	}

    }

}
