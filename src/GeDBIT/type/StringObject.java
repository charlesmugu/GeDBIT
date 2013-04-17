/**
 * mobios.type.StringObject 2011.07.21 
 * 
 * Copyright Information: 
 * 
 * Change Log:
 * 2011.07.21: Created by Rui Mao
 */
package GeDBIT.type;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import GeDBIT.index.TableManager;

/**
 * This class is a wrapper of over String to implement IndexObject
 * 
 * @author Rui Mao
 * @version 2011.07.21
 */
public class StringObject extends IndexObject {
    /**
     * 
     */
    private static final long serialVersionUID = 1275897899226331100L;

    /**
     * 
     */
    Table table;

    String data;

    public StringObject(String input) {
	this.data = input;
    }

    public StringObject() {
    }

    /**
     * Builds an instance from a double array.
     * 
     * @param rowID
     * @param data
     *            the double array containning all the elements. cannot be null
     */
    public StringObject(Table table, int rowID, String input) {
	super(rowID);
	if (input == null)
	    throw new IllegalArgumentException(
		    "null data constructing StringObject");
	this.table = table;
	this.data = input;
    }

    /**
     * @return the string
     */
    public String getData() {
	return data;
    }

    /**
     * @return the dimension ( length) of the vector
     */
    public int size() {
	return data.length();
    }

    /*
     * (non-Javadoc)
     * 
     * @see mobios.type.IndexObject#expand()
     */
    public IndexObject[] expand() {
	IndexObject[] dbO = new IndexObject[rowIDLength];
	for (int i = 0; i < rowIDLength; i++) {
	    dbO[i] = new StringObject(table, rowIDStart + i, data);
	}
	return dbO;
    }

    /*
     * (non-Javadoc)
     * 
     * @see mobios.type.IndexObject#compareTo(mobios.type.IndexObject)
     */
    public int compareTo(IndexObject oThat) {
	if (!(oThat instanceof StringObject))
	    throw new ClassCastException("not compatible");
	return compareTo((StringObject) oThat);
    }

    /**
     * @param oThat
     * @return
     */
    public int compareTo(StringObject oThat) {
	StringObject that = (StringObject) oThat;
	if (this == that)
	    return 0;

	return this.data.compareTo(that.data);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object that) {
	if (!(that instanceof StringObject))
	    return false;
	return this.data.equals(((StringObject) that).data);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
	return data;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException,
	    ClassNotFoundException {
	super.readExternal(in);
	data = (String) in.readObject();
	String indexPrefix = (String) in.readObject();
	table = TableManager.getTableManager(indexPrefix)
		.getTable(in.readInt());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
	super.writeExternal(out);
	out.writeObject(data);
	out.writeObject(table.getIndexPrefix());
	out.writeInt(table.getTableLocation());
    }

    // taken from Joshua Bloch's Effective Java
    public int hashCode() {
	int result = super.hashCode();
	result = 37 * (37 * result + data.length()) + data.hashCode();
	return result;
    }
}
