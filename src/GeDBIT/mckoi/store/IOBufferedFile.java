/**
 * edu.utexas.GeDBIT.mckoi.store.IOBufferedFile  27 Jan 2003
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
 * 2005.05.24: Two lines added to compute length of time to read pages in from disk, by Willard
 * 
 */

package GeDBIT.mckoi.store;

import java.io.RandomAccessFile;
import java.io.IOException;

/**
 * An implementation of AbstractBufferedFile that implements a paging
 * strategy that copies pages from the underlying RandomAccessFile into
 * byte[] arrays in memeory.
 *
 * @author Tobias Downer
 */

class IOBufferedFile extends AbstractBufferedFile {

  /**
   * The constructor.
   */
  public IOBufferedFile(int unique_id, RandomAccessFile file,
                        BufferManager manager) {
    super(unique_id, file, manager);
  }

  // ---------- Implemented from AbstractBufferedFile ----------

  public PageBuffer createPageFor(long position, int len) {
    return new IOPageBuffer(position, len);
  }

  public void sizeChange(long old_size, long new_size) throws IOException {
    // This doesn't need to do anything for this implementation.
  }

  // ---------- Inner classes ----------

  /**
   * This implementation of a page buffer initializes the page by reading the
   * page into a byte[] array in memory.
   */
  class IOPageBuffer extends PageBuffer {
    
    private long position;
    private int length;
    private byte[] buf;

    public IOPageBuffer(long position, int length) {
      this.position = position;
      this.length = length;
    }

    public void initImpl() throws IOException {
      buf = new byte[length];
      synchronized (file) {
      //added by Willard 2005.05.24 to compute statistics on how long it takes to read a page in from disk
      	buffer_manager.addTime(System.currentTimeMillis());
        // Read the page from the file into memory.
        file.seek(position);
        file.read(buf, 0, length);
       //added by Willard 2005.05.24 to compute statistics on how long it takes to read a page in from disk
        buffer_manager.addTime(System.currentTimeMillis());
      }
    }

    public byte readImpl(int position) {
      return buf[position];
    }

    public void readImpl(int position, byte[] b, int off, int len) {
      System.arraycopy(buf, position, b, off, len);
    }

    public void writeImpl(int position, byte b) {
      buf[position] = b;
    }

    public void writeImpl(int position, byte[] b, int off, int len) {
      System.arraycopy(b, off, buf, position, len);
    }

    public void flushImpl(int p1, int p2) throws IOException {
      synchronized (file) {
        // Read the page from the file into memory.
        file.seek(position + p1);
        file.write(buf, p1, p2 - p1);
      }
    }

    public void disposeImpl() {
      buf = null;
    }

  }

}

