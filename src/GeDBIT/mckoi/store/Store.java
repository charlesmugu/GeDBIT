/**
 * edu.utexas.GeDBIT.mckoi.store.Store  30 Aug 2002
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

import java.util.List;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * A store is a resource where areas can be allocated and freed to store
 * objects.  A store can be backed by a file or main memory, or a combination of
 * the two.
 * <p>
 * Some characteristics of implementations of Store may be separately
 * specified.  For example, a file based store that is intended to persistently
 * store objects may have robustness as a primary requirement.  A main memory
 * based store, or other type of volatile store, may not require robustness.
 *
 * @author Tobias Downer
 */

public interface Store {

  /**
   * Allocates a block of memory out of the store of the specified size, and
   * returns a pointer to the block.  If the store can not allocate an area of
   * memory large enough for the request, a StoreException exception is thrown.
   * This also throws a StoreException if the backing store is read-only.
   *
   * @param size the amount of memory to allocate.
   * @return a 64-bit pointer to the allocated memory.
   * @throws IOException is the allocation can not happen.
   */
  long alloc(long size) throws IOException;
  
  /**
   * Frees a block of memory from the store that was previously allocated via
   * 'alloc'.  Freeing an invalid pointer is implementation defined, however the
   * ideal behaviour is for a StoreException to be thrown.
   *
   * @param pointer a 64-bit pointer to the memory to free.
   */
  void free(long pointer) throws IOException;

  /**
   * Returns an Area object that represents the 64 byte fixed area as can be
   * changed by the 'getFixedArea' and 'setFixedArea' methods.
   */
  Area getFixedArea() throws IOException;
  
  /**
   * Returns an InputStream for reading from the area of the store allocated
   * via the 'alloc' method.  Trying to read from an invalid pointer is
   * implementation defined, however the ideal behaviour is for a StoreException
   * to be thrown.
   * <p>
   * The returned stream does not perform buffering.  For efficiency, it is a
   * good idea to wrap the returned steam with a BufferedInputStream.
   *
   * @param pointer a 64-bit pointer to the memory to read.
   * @return an InputStream for reading from the allocated resource.
   */
  InputStream getInputStream(long pointer) throws IOException;

  /**
   * Returns an OutputStream for writing to the area of the store allocated
   * via the 'alloc' method.  Trying to write to an invalid pointer is
   * implementation defined, however the ideal behaviour is for a StoreException
   * to be thrown.  This also throws a StoreException if the backing store is
   * read-only.
   * <p>
   * The returned stream does not perform buffering.  For efficiency, it is a
   * good idea to wrap the returned steam with a BufferedOutputStream.
   *
   * @param pointer a 64-bit pointer to the memory to write.
   * @return an OutputStream for writing to the allocated resource.
   */
  OutputStream getOutputStream(long pointer) throws IOException;

  /**
   * Returns an Area object that can be used to manipulate an area allocated
   * from the store.  The returned Area performs buffering.
   *
   * @param pointer a 64-bit pointer to the area (allocated via 'alloc').
   * @return an Area object for manipulating the area.
   */
  Area getArea(long pointer) throws IOException;

  /**
   * Flushes any changes to the underlying storage device if the store
   * implements memory mapping or buffering.
   */
  void flush() throws IOException;
  
  /**
   * Flushes and synchronizes any changes made to the store with the underlying
   * persistent device.  This is only necessary for stores based on files.
   * After an 'alloc', 'free' or write operation has occured, this method should
   * be called if this level of robustness is necessary.
   */
  void synch() throws IOException;
  
  // ---------- Diagnostic ----------

  /**
   * Returns true if the store was closed cleanly.  This is important
   * information that may need to be considered when reading information from
   * the store.  This is typically used to issue a scan on the data in the
   * store when it is not closed cleanly.
   */
  boolean lastCloseClean();

  /**
   * Returns a complete list of pointers to all areas in the Store as Long
   * objects sorted from lowest pointer to highest.  This should be used for
   * diagnostics only because it may be difficult for this to be generated
   * with some implementations.  It is useful in a repair tool to determine if
   * a pointer is valid or not.
   */
  @SuppressWarnings("rawtypes")
List getAllAreas() throws IOException;
  
}

