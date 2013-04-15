/**
 * edu.utexas.GeDBIT.mckoi.store.Area  02 Sep 2002
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
 * An interface that represents an area of a store that has been allocated.
 * The area object maintains a pointer that can be manipulated and read and
 * written to.  This interface is modelled on the java.nio.Buffer interface
 * and should be easily interchanged with it.
 *
 * @author Tobias Downer
 */

public interface Area {

  /**
   * Returns the current position of the pointer within the area.  The position
   * starts at beginning of the area.
   */
  int position();
  
  /**
   * Returns the capacity of the area.
   */
  int capacity();
  
  /**
   * Sets the position within the area and returns this object.
   */
  Area position(int position) throws IOException;
  
  /**
   * Copies 'size' bytes from the current position of this Area to the
   * destination Area (at the position set in the destination area).  Returns
   * this area.
   */
  Area copyTo(Area destination, int size) throws IOException;
  
  // ---------- Various get/put methods ----------
  
  byte get() throws IOException;
  
  Area put(byte b) throws IOException;
  
  Area get(byte[] buf, int off, int len) throws IOException;
  
  Area put(byte[] buf, int off, int len) throws IOException;
  
  short getShort() throws IOException;
  
  Area putShort(short s) throws IOException;

  int getInt() throws IOException;
  
  Area putInt(int i) throws IOException;
  
  long getLong() throws IOException;
  
  Area putLong(long l) throws IOException;

  char getChar() throws IOException;
  
  Area putChar(char c) throws IOException;
  
}

