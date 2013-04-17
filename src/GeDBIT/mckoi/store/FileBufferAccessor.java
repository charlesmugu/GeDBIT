/**
 * edu.utexas.GeDBIT.mckoi.store.FileBufferAccessor  24 Jan 2003
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
 * An abstraction for accessing an underlying file through a buffering strategy.
 * Implementations of this could include a simple RandomAccessFile paging system
 * that uses Java 1.4 memory mapping facilities, or not.
 * 
 * @author Tobias Downer
 */

public interface FileBufferAccessor {

    /**
     * Reads a single byte from the given position in the given RandomAccessFile
     * that has been assigned the given id value.
     */
    int readByte(long position) throws IOException;

    /**
     * Reads a byte array from the given position in the file. Returns the
     * actual number of bytes read. The value returned should ALWAYS be the
     * value given in 'len'.
     */
    int readByteArray(long position, byte[] buf, int off, int len)
	    throws IOException;

    /**
     * Writes a single byte to the given position in the file.
     */
    void writeByte(long position, byte b) throws IOException;

    /**
     * Writes a byte array to the given position in the file. Returns the actual
     * number of bytes written. The value returned should ALWAYS be the value
     * given in 'len'.
     */
    int writeByteArray(long position, byte[] buf, int off, int len)
	    throws IOException;

    /**
     * Flushes any pending contents of the buffer from memory to the file.
     */
    void flush() throws IOException;

    /**
     * Notifies the buffering strategy that the size of the file has changed.
     */
    void sizeChange(long old_file_length, long new_file_length)
	    throws IOException;

}
