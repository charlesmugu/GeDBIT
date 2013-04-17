/**
 * GeDBIT.type.IndexObject 2006.07.24 
 * 
 * Change Log: 
 * 2006.07.24: Added, by Willard
 */
package GeDBIT.type;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * 
 * @author Willard
 */
public abstract class IndexObject implements Externalizable,
	Comparable<IndexObject> {
    int rowIDStart;
    int rowIDLength;

    public IndexObject() {
    }

    public IndexObject(int rowID) {
	this.rowIDStart = rowID;
	this.rowIDLength = 1;
    }

    public void setRowID(int rowID) {
	this.rowIDStart = rowID;
    }

    public int getRowID() {
	return rowIDStart;
    }

    public void setRowIDLength(int length) {
	rowIDLength = length;
    }

    public abstract int size();

    public abstract IndexObject[] expand();

    public abstract int compareTo(IndexObject oThat);

    public void writeExternal(ObjectOutput out) throws IOException {
	out.writeInt(this.rowIDStart);
	out.writeInt(this.rowIDLength);
    }

    public void readExternal(ObjectInput in) throws IOException,
	    ClassNotFoundException {
	rowIDStart = in.readInt();
	rowIDLength = in.readInt();
    }

}
