/**
 * edu.utexas.GeDBIT.util.MckoiObjectIOManager 2003.07.17
 *
 * Copyright Information:
 *
 * Change Log:
 * 2003.07.17: Created by Rui Mao
 * 2003.07.25: Add iterator(), modify open(), close(), by Rui Mao
 * 2003.07.27: Add another constructor, construct from filename, by Rui Mao
 * 2003.07.28: Modify the argument of constructor, only provide whole file name, no path, by Rui Mao
 * 2003.07.29: Modify the iterator, now its next() return the actual Object stored, by Rui Mao
 */

package GeDBIT.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import GeDBIT.mckoi.store.Store;
import GeDBIT.mckoi.store.AbstractStore;
import GeDBIT.mckoi.store.Area;
import GeDBIT.mckoi.store.ScatteringFileStore;
import GeDBIT.mckoi.store.BufferManager;

/**
 * An implementation of {@link ObjectIOManager} based on the
 * Mckoi(http://www.mckoi.com/database) I/O mechanism. It is backed by a
 * {@link Store} (defined in Mckoi), and is able to perform cached page-based
 * I/O on a file on disk or in memory. The serialized form of an object is read
 * from/written to the stream. In Mckoi, a <code>Store</code> manages a
 * collection of {@link Area}s. In this class, each Area saves the serialized
 * format of an {@link Object}. Therefore, an Object that can be read/written by
 * this class should be serializable. For example, the following code creates a
 * {@link MckoiObjectIOManager} that works on multiple disk files, using the new
 * java.nio package, with cache and page-based access mechanism.
 * <p>
 * <code>
 *  //Create a buffer manager, using java.nio mechanism, max page number:65536, page size:4096 bytes, 
 * you can also use java IO mechsnism, by providing "java IO", which is recommended:<p>
 *  BufferManager myBM = new BufferManager("Java NIO", 65536, 4096);<p>
 * 
 *  //Create a ScatteringFileStore, the disk files are on current directory, file names start with "test"
 *  if one file grows too big, it will be split, all files have extension names start from "0",
 *  max file size is 1G. The buffer manager is the one just created, and the files are in read/write mode
 * If the last argument is true, then the file is read only:<p>
 *  Store myStore = new ScatteringFileStore("", "test", "0", 1024*1024*1024, myBM, false);<p>
 * 
 *  // create a MckoiObjectIOManager with the store instance just created<p>
 *  ObjectIOManager myManager = new MckoiObjectIOManager( mystore); <br>
 *  if (myManger.open() == false)<br>
 *      System.out.println("Error opening ObjectIOManager: myManager!");<br>
 *  </code>
 * 
 * @author Rui Mao
 * @version 2005.11.01
 */
public class MckoiObjectIOManager implements ObjectIOManager {
    private final Store store;
    private HashMap<Long, Object> hm;

    /**
     * Constructor.
     * 
     * @param store
     *            the mckoi store to work on
     */
    public MckoiObjectIOManager(Store store) {
	if (store == null)
	    throw new IllegalArgumentException("Store is null!");

	this.store = store;
	hm = new HashMap<Long, Object>();
	// this.total = 0;
	// this.writeCounter = 0;
    }

    /**
     * A new constructor, construct from file name and page size.
     * 
     * @param fileHeader
     *            header of back bone file names, may include the path
     * @param fileExt
     *            extension name of back bone files, normally use "000"
     * @param fileSize
     *            max size of a back bone file, if a file is not enough, more
     *            files will be used
     * @param bufferType
     *            type of buffer, should be one of "Java NIO" or "Java IO"
     * @param cacheSize
     *            max number of pages in the buffer, could be 2 if only iterate
     *            the data once.
     * @param pageSize
     *            size of a page, in number of bytes. normally use "4096", could
     *            be larger for sequential read / write, for example: 4M
     *            (4*1024*1024)
     * @param readOnly
     *            if true, the back bone files are read only
     */
    public MckoiObjectIOManager(String fileHeader, String fileExt,
	    long fileSize, String bufferType, int cacheSize, int pageSize,
	    boolean readOnly) {
	final BufferManager BM = new BufferManager(bufferType, cacheSize,
		pageSize);
	final File path = new File(fileHeader);
	String pathName = path.getParent();
	if (pathName == null)
	    pathName = ".";
	final ScatteringFileStore store = new ScatteringFileStore(new File(
		pathName), path.getName(), fileExt, fileSize, BM, readOnly);

	this.store = store;
	hm = new HashMap<Long, Object>();
	// this.total = 0;
	// this.writeCounter = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.util.ObjectIOManager#open()
     */
    public boolean open() {
	boolean b = true;
	try {
	    b = !((AbstractStore) store).open();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return b;
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.util.ObjectIOManager#flush()
     */
    public void flush() throws java.io.IOException {
	store.flush();
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.util.ObjectIOManager#close()
     */
    public void close() {
	// no such method in Mckoi.Store, so just return
	try {
	    ((AbstractStore) store).close();
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.util.ObjectIOManager#readObject(long)
     */
    public Object readObject(final long pointer) throws java.io.IOException,
	    java.lang.ClassNotFoundException, InstantiationException,
	    IllegalAccessException {
	Object object = hm.get(pointer);
	if (object == null)
	    return readUnhashedObject(pointer);
	return object;
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.util.ObjectIOManager#readPersistObject(long)
     */
    public Object readPersistObject(final long pointer) throws IOException,
	    ClassNotFoundException, InstantiationException,
	    IllegalAccessException {
	Object object = readUnhashedObject(pointer);
	// TODO: only put object if not already there.
	if (!hm.containsKey(pointer))
	    hm.put(pointer, object);
	return object;
    }

    private Object readUnhashedObject(final long pointer)
	    throws java.io.IOException, java.lang.ClassNotFoundException,
	    InstantiationException, IllegalAccessException {
	// step1. read the serializaed format (byte stream) out of the store
	Area area = store.getArea(pointer);
	final int s = area.capacity();
	byte[] buffer = new byte[s];
	area.position(0);
	area.get(buffer, 0, s);

	// step2, deserialized the object from the byte stream
	ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
		buffer));

	Object o = ois.readObject();

	ois.close();
	return o;
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.util.ObjectIOManager#writeObject(java.lang.Object, long)
     */
    public Object writeObject(Object object, final long pointer)
	    throws java.io.IOException, java.lang.ClassNotFoundException {
	// step1. read the original serializaed format (byte stream) out of the
	// store
	Area area = store.getArea(pointer);
	final int s = area.capacity();
	byte[] buffer = new byte[s];
	area.position(0);
	area.get(buffer, 0, s);

	// step2, deserialized the original object from the byte stream
	ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
		buffer));
	Object o = ois.readObject();

	// step3. generate the serialized form of the new object
	ByteArrayOutputStream bOut = new ByteArrayOutputStream();
	ObjectOutputStream oOut = new ObjectOutputStream(bOut);
	oOut.writeObject(object);

	// step 4. write the new object into Area
	area.position(0);
	byte[] newBuffer = bOut.toByteArray();

	// for debug
	System.out.println("original size=" + s + ", new size="
		+ newBuffer.length);

	area.put(newBuffer, 0, newBuffer.length);

	// release memory for garbage collector
	area = null;
	buffer = null;
	newBuffer = null;
	ois.close();
	oOut.close();

	ois = null;
	bOut = null;
	oOut = null;

	return o;
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.util.ObjectIOManager#writeObject(java.lang.Object)
     */
    public long writeObject(Object object) throws java.io.IOException {
	// step1. generate the serialized format, get the size
	ByteArrayOutputStream bOut = new ByteArrayOutputStream();
	ObjectOutputStream oOut = new ObjectOutputStream(bOut);
	oOut.writeObject(object);
	oOut.close();

	byte[] buffer = bOut.toByteArray();
	final int s = buffer.length;

	// statistics
	// writeCounter ++;
	// total += s;

	// step2. allocate an Area in Store, write it.
	final long p = store.alloc(s);
	Area area = store.getArea(p);
	area.position(0);
	area.put(buffer, 0, s);

	// release memory for garbage collector
	bOut = null;
	oOut = null;
	buffer = null;
	area = null;

	return p;
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.util.ObjectIOManager#removeObject(long)
     */
    public Object removeObject(final long pointer) throws java.io.IOException,
	    java.lang.ClassNotFoundException, InstantiationException,
	    IllegalAccessException {
	Object o = readObject(pointer);

	store.free(pointer);

	return o;
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.util.ObjectIOManager#size()
     */
    public long size() throws java.io.IOException {
	return store.getAllAreas().size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.util.ObjectIOManager#iterator()
     */
    @SuppressWarnings("rawtypes")
    public Iterator iterator() {
	return new MckoiObjectIOManagerIterator();
    }

    // ------------------- inner class MckoiObjectIOManagerIterator
    // ---------------------------//
    @SuppressWarnings("rawtypes")
    class MckoiObjectIOManagerIterator implements Iterator {
	Iterator p;

	MckoiObjectIOManagerIterator() {
	    p = ((AbstractStore) store).iterator();
	}

	public boolean hasNext() {
	    return p.hasNext();

	}

	public Object next() throws java.util.NoSuchElementException {
	    Object result = null;
	    try {
		Area area = (Area) p.next();
		final int s = area.capacity();
		byte[] buffer = new byte[s];
		area.position(0);
		area.get(buffer, 0, s);

		// step2, deserialized the object from the byte stream
		ObjectInputStream ois = new ObjectInputStream(
			new ByteArrayInputStream(buffer));

		// release memory for garbage collector
		area = null;

		result = ois.readObject();
	    } catch (Exception e) {
		System.out
			.println("Exception in calling MckoiObjectIOManagerIterator.next():"
				+ e.toString());
		e.printStackTrace();
	    }

	    return result;

	}

	public void remove() throws UnsupportedOperationException {
	    throw new UnsupportedOperationException(
		    "remove() in StoreIterator is not supported yet!");
	}

    }
}
