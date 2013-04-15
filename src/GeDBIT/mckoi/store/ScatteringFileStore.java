/**
 * edu.utexas.GeDBIT.mckoi.store.ScatteringFileStore  24 Jan 2003
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
 * 2005.05.24: Added method to print statistics about the buffer manager, including hits, misses, etc., by Willard
 * 
 */

package GeDBIT.mckoi.store;

import java.util.ArrayList;
import java.io.*;

/**
 * An implementation of AbstractStore that scatters a store over one or more
 * files in the filesystem.  If the amount of data stored in one store exceeds
 * a given threshold the data grows into a new file.
 * <p>
 * A ScatteringFileStore has a buffering strategy for accessing the underlying
 * files that is implementation defined (by the BufferManager).
 *
 * @author Tobias Downer
 */

public final class ScatteringFileStore extends AbstractStore {

  /**
   * The path of this store in the file system.
   */
  private final File path;
  
  /**
   * The name of the file in the file system minus the extension.
   */
  private final String file_name;
  
  /**
   * The extension of the first file in the sliced set.
   */
  private final String first_ext;

  /**
   * The maximum size a file slice can grow too before a new slice is created.
   */
  private final long max_slice_size;
  
  /**
   * The buffering strategy for accessing the data in an underlying file.
   */
  private final BufferManager buffer_manager;

  /**
   * The list of RandomAccessFile objects for each file that represents a
   * slice of the store.  (FileSlice objects)
   */
  @SuppressWarnings("rawtypes")
private ArrayList slice_list;

  /**
   * Set whenever a write operation occurs and reset when a synch occurs.
   */
  private boolean has_written;

  /**
   * The total length of all the slices of the store combined.
   */
  private long total_length;
  
  /**
   * Constructs the ScatteringFileStore.
   */
  public ScatteringFileStore(File path,
                         String file_name, String first_ext,
                         long max_slice_size,
                         BufferManager buffer_manager,
                         boolean read_only) {
    super(read_only);
    this.path = path;
    this.file_name = file_name;
    this.first_ext = first_ext;
    this.max_slice_size = max_slice_size;
    this.buffer_manager = buffer_manager;
  }

  /**
   * Given an index value, this will return a File object for the nth slice in
   * the file system.  For example, given '4' will return [file name].004,
   * given 1004 will return [file name].1004, etc.
   */
  private File slicePartFile(int i) {
    if (i == 0) {
      return new File(path, file_name + "." + first_ext);
    }
    StringBuffer fn = new StringBuffer();
    fn.append(file_name);
    fn.append(".");
    if (i < 10) {
      fn.append("00");
    }
    else if (i < 100) {
      fn.append("0");
    }
    fn.append(i);
    return new File(path, fn.toString());
  }

  /**
   * Given a file, this will convert to a scattering file store with files
   * no larger than the maximum slice size.
   */
  public void convertToScatteringStore(File f) throws IOException {

    int BUFFER_SIZE = 65536;
    
    @SuppressWarnings("resource")
    RandomAccessFile src = new RandomAccessFile(f, "rw");
    long file_size = f.length();
    long current_p = max_slice_size;
    long to_write = Math.min(file_size - current_p, max_slice_size);
    int write_to_part = 1;

    byte[] copy_buffer = new byte[BUFFER_SIZE];

    while (to_write > 0) {

      src.seek(current_p);

      File to_f = slicePartFile(write_to_part);
      if (to_f.exists()) {
        throw new IOException("Copy error, slice already exists.");
      }
      FileOutputStream to_raf = new FileOutputStream(to_f);

      while (to_write > 0) {
        int size_to_copy = (int) Math.min(BUFFER_SIZE, to_write);

        src.read(copy_buffer, 0, size_to_copy);
        to_raf.write(copy_buffer, 0, size_to_copy);
        
        current_p += size_to_copy;
        to_write -= size_to_copy;
      }

      to_raf.flush();
      to_raf.close();

      to_write = Math.min(file_size - current_p, max_slice_size);
      ++write_to_part;
    }

    // Truncate the source file
    if (file_size > max_slice_size) {
      src.seek(0);
      src.setLength(max_slice_size);
    }
    src.close();

  }
  
  // ---------- ScatteringFileStore methods ----------

  /**
   * Counts the number of files in the file store that represent this store.
   */
  private int countStoreFiles() {
    int i = 0;
    File f = slicePartFile(i);
    while (f.exists()) {
      ++i;
      f = slicePartFile(i);
    }
    return i;
  }
  
  /**
   * Deletes this store from the file system.  This operation should only be
   * used when the store is NOT open.
   */
  public boolean delete() throws IOException {
    // The number of files
    int count_files = countStoreFiles();
    // Delete each file from back to front
    for (int i = count_files - 1; i >= 0; --i) {
      File f = slicePartFile(i);
      if (!f.delete()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if this store exists in the file system.
   */
  public boolean exists() throws IOException {
    return slicePartFile(0).exists();
  }
  
  // ---------- Implemented from AbstractStore ----------
  
  /**
   * Internally opens the backing area.  If 'read_only' is true then the
   * store is openned in read only mode.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
protected void internalOpen(boolean read_only) throws IOException {
    slice_list = new ArrayList();
    
    String mode = read_only ? "r" : "rw";
    // Does the file exist?
    File f = slicePartFile(0);
    boolean open_existing = f.exists();
    
    // If the file already exceeds the threshold and there isn't a secondary
    // file then we need to convert the file.
    if (open_existing && f.length() > max_slice_size) {
      File f2 = slicePartFile(1);
      if (f2.exists()) {
        throw new IOException(
                            "File length exceeds maximum slice size setting.");
      }
      // We need to scatter the file.
      if (!read_only) {
        convertToScatteringStore(f);
      }
      else {
        throw new IOException(
                  "Unable to convert to a scattered store because read-only.");
      }
    }

    // Setup the first file slice
    FileSlice slice = new FileSlice();
    slice.name = f;
    slice.file = new RandomAccessFile(f, mode);
    slice.buffered_file = buffer_manager.createBufferedAccessor(slice.file, mode);
    slice.length = slice.file.length();
    slice_list.add(slice);
    total_length = slice.length;

    // If we are opening a store that exists already, there may be other
    // slices we need to setup.
    if (open_existing) {
      int i = 1;
      File slice_part = slicePartFile(i);
      while (slice_part.exists()) {
        // Create the new slice information for this part of the file.
        slice = new FileSlice();
        slice.name = slice_part;
        slice.file = new RandomAccessFile(slice_part, mode);
        slice.buffered_file = buffer_manager.createBufferedAccessor(slice.file, mode);
        slice.length = slice.file.length();
        slice_list.add(slice);
        
        total_length += slice.length;
        ++i;
        slice_part = slicePartFile(i);
      }
    }
    
  }
  
  /**
   * Internally closes the backing area.
   */
  protected void internalClose() throws IOException {
    int sz = slice_list.size();

    has_written = true;
    synch();

    // We should assert that the file tally is kept correct with the file
    // size.
    for (int i = 0; i < sz; ++i) {
      FileSlice slice_part = (FileSlice) slice_list.get(i);
      if (slice_part.length != slice_part.file.length()) {
        throw new IOException(
            "Error: file size tally is incorrect for file: " + slice_part.name +
            "val = " + slice_part.length + " act = " + slice_part.file.length());
      }
    }

    // Go through the slice list and close each file.
    for (int i = 0; i < sz; ++i) {
      FileSlice slice_part = (FileSlice) slice_list.get(i);
      slice_part.file.close();
    }

  }


  protected synchronized int readByteFrom(long position) throws IOException {
    int file_i = (int) (position / max_slice_size);
    long file_p = (position % max_slice_size);
    FileSlice slice = (FileSlice) slice_list.get(file_i);
    return slice.buffered_file.readByte(file_p);
  }
  
  protected synchronized int readByteArrayFrom(long position,
                           byte[] buf, int off, int len) throws IOException {

    int read_tally = 0;

    // Reads the array (potentially across multiple slices).
    while (len > 0) {
      int file_i = (int) (position / max_slice_size);
      long file_p = (position % max_slice_size);
      int file_len = (int) Math.min((long) len, max_slice_size - file_p);
      
      FileSlice slice = (FileSlice) slice_list.get(file_i);
      int read_count = slice.buffered_file.readByteArray(file_p,
                                                         buf, off, file_len);
      read_tally += read_count;
      
      position += file_len;
      off += file_len;
      len -= file_len;
    }

    return read_tally;

  }
  
  protected synchronized void writeByteTo(long position, int b)
                                                         throws IOException {
    int file_i = (int) (position / max_slice_size);
    long file_p = (position % max_slice_size);
    FileSlice slice = (FileSlice) slice_list.get(file_i);
    slice.buffered_file.writeByte(file_p, (byte) b);
  }

  protected synchronized void writeByteArrayTo(long position,
                           byte[] buf, int off, int len) throws IOException {
    // Writes the array (potentially across multiple slices).
    while (len > 0) {
      int file_i = (int) (position / max_slice_size);
      long file_p = (position % max_slice_size);
      int file_len = (int) Math.min((long) len, max_slice_size - file_p);
      
      FileSlice slice = (FileSlice) slice_list.get(file_i);
      slice.buffered_file.writeByteArray(file_p, buf, off, file_len);
      
      position += file_len;
      off += file_len;
      len -= file_len;
    }
    has_written = true;
  }

  protected synchronized long endOfDataAreaPointer() throws IOException {
    return total_length;
  }

  @SuppressWarnings("unchecked")
protected synchronized void setDataAreaSize(long length) throws IOException {

    // The size we need to grow the data area
    long total_size_to_grow = length - total_length;
    // Assert that we aren't shrinking the data area size.
    if (total_size_to_grow < 0) {
      throw new IOException(
          "Unable to make the data area size smaller for this type of store.");
    }
    
    while (total_size_to_grow > 0) {
      // Grow the last slice by this size
      int last = slice_list.size() - 1;
      FileSlice slice = (FileSlice) slice_list.get(last);
      final long old_slice_length = slice.length;
      long to_grow = Math.min(total_size_to_grow,
                              (max_slice_size - slice.length));

      // Flush the buffer and set the length of the file
      slice.buffered_file.flush();
      slice.length = slice.length + to_grow;
      slice.file.setLength(slice.length);
      // Synchronize the file change.  XP appears to defer a file size change
      // and it can result in errors if the JVM is terminated.
      slice.file.getFD().sync();
      // Inform the buffer manager of the file size change
      slice.buffered_file.sizeChange(old_slice_length, slice.length);

      total_size_to_grow -= to_grow;
      // Create a new empty slice if we need to extend the data area
      if (total_size_to_grow > 0) {
        File slice_file = slicePartFile(last + 1);
        slice = new FileSlice();
        slice.name = slice_file;
        slice.file = new RandomAccessFile(slice_file, "rw");
        String mode = read_only ? "r" : "rw";
        slice.buffered_file = buffer_manager.createBufferedAccessor(slice.file, mode);

        slice.length = slice.file.length();
        slice_list.add(slice);
      }
    }

    total_length = length;
    has_written = true;
    
  }

  public synchronized void flush() throws IOException {
    int sz = slice_list.size();
    for (int i = 0; i < sz; ++i) {
      FileSlice slice = (FileSlice) slice_list.get(i);
      slice.buffered_file.flush();
    }
  }
  
  public synchronized void synch() throws IOException {
    if (has_written) {
      int sz = slice_list.size();
      for (int i = 0; i < sz; ++i) {
        FileSlice slice = (FileSlice) slice_list.get(i);
        slice.buffered_file.flush();
        slice.file.getFD().sync();
      }
      has_written = false;
    }
  }

  // ---------- Inner classes ----------
  
  /**
   * An object that contains information about a file slice.  The information
   * includes the name of the file, the RandomAccessFile that represents the
   * slice, and the size of the file.
   */
  private static class FileSlice {

    File name;

    long length;

    RandomAccessFile file;

    FileBufferAccessor buffered_file;

  }

/**
 * Prints statistics about the buffer manager. Statistics include number of hits, misses,
 * amount of time spent in reading data from disk, amount of time spent in writing data to disk,
 * etc.
 * Added by Willard, 2005.05.24
 */
public void printBufferStatistics() {
	buffer_manager.printStatistics();
}


}

