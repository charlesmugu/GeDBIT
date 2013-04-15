/**
 * GeDBIT.util.ObjectIOManager 2006.05.09
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.05.09 Copied from jdb1, by Rui Mao
 */

package GeDBIT.util;

import java.util.Iterator;

/**
 * This is an interface for object input/output management. An implementation of this interface
 * should be backed by a stream (file, memory, socket...). It should be able to do I/O of object on
 * the stream.
 * 
 * @author Rui Mao, Willard
 * @version 2006.05.12
 */
public interface ObjectIOManager {
    /**
     * Opens the backing stream. Makes the manager ready for I/O operations.
     * 
     * @return boolean true is sucessfully opened, otherwise false
     */
    public boolean open();

    /**
     * Flushes the backing stream. Saves all unsaved modifications.
     */
    public void flush() throws java.io.IOException;

    /**
     * Closes the backing stream.
     */
    public void close();

    /**
     * Reads a previously written object at a given position.
     * 
     * @param pointer
     *        the pointer to the object to be read from the underlying stream. The value is returned
     *        when the object is first written to the stream with
     *        {@link ObjectIOManager#writeObject}
     * @return the object that has been read
     */
    public Object readObject(final long pointer) throws java.io.IOException,
            java.lang.ClassNotFoundException, InstantiationException, IllegalAccessException;

    /**
     * Reads a previously written object at a given position, and holds this object in memory for
     * the duration of the program.
     * 
     * @param pointer
     *        the pointer to the object to be read in the underlying stream. The value is returned
     *        when the object is first written to the stream with
     *        {@link ObjectIOManager#writeObject}
     * @return the object that has been read
     */
    public Object readPersistObject(final long pointer) throws java.io.IOException,
            java.lang.ClassNotFoundException, InstantiationException, IllegalAccessException;

    /**
     * Writes an object to a given position in the underlying stream. This method modifies a
     * previously written object at the given position.
     * 
     * @param object
     *        the object to be written, cannot be null.
     * @param pointer
     *        pointer to the underlying stream
     * @return the original object that is overwritten.
     */
    public Object writeObject(Object object, final long pointer) throws java.io.IOException,
            java.lang.ClassNotFoundException;

    /**
     * Writes a new object to the underlying stream.
     * 
     * @param object
     *        the object to be added to the stream
     * @return the pointer to the object in the stream.
     */
    public long writeObject(Object object) throws java.io.IOException;

    /**
     * Deletes an object from the underlying stream.
     * 
     * @param pointer
     *        pointer to the object to be deleted in the underlying stream.
     * @return the object be deleted
     */
    public Object removeObject(final long pointer) throws java.io.IOException,
            java.lang.ClassNotFoundException, InstantiationException, IllegalAccessException;

    /**
     * @return the number of objects in the underlying stream
     */
    public long size() throws java.io.IOException;

    /**
     * @return an {@link Iterator} over all of the {@link GeDBIT.mckoi.store.Area}s in the store
     */
    @SuppressWarnings("rawtypes")
    public Iterator iterator();
}
