/**
 * edu.utexas.GeDBIT.type.Fragment 2006.05.24
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.05.24: Created, by Willard
 */
package GeDBIT.type;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import GeDBIT.index.TableManager;

/**
 * @author Willard
 *
 */
public class Fragment extends IndexObject
{
    /**
     * 
     */
    private static final long serialVersionUID = -7087849259510041868L;

    /**
     * 
     */
    private SequenceTable     sTable;

    /**
     * 
     */
    public Fragment()
    {
    }

    /**
     * @param table
     * @param rowID
     */
    public Fragment(SequenceTable table, int rowID)
    {
        super(rowID);
        this.sTable = table;
    }

    /**
     * @param i
     * @return
     */
    public Symbol get(int i)
    {
        return sTable.alphabet.get(sTable.sequences[sTable.originalRowIDs[rowIDStart]].data[sTable.fragmentOffsets[rowIDStart] + i]);
    }
    
    /* (non-Javadoc)
     * @see GeDBIT.type.IndexObject#size()
     */
    public int size()
    {
        return sTable.fragmentLength;
    }

    /* (non-Javadoc)
     * @see GeDBIT.type.IndexObject#expand()
     */
    public IndexObject[] expand()
    {
        IndexObject[] dbO = new IndexObject[rowIDLength];
        for (int i = 0; i < rowIDLength; i++)
        {
            dbO[i] = new Fragment(sTable, rowIDStart + i);
        }
        return dbO;
    }

    /* (non-Javadoc)
     * @see GeDBIT.type.IndexObject#compareTo(GeDBIT.type.IndexObject)
     */
    public int compareTo(IndexObject oThat)
    {
        if (!(oThat instanceof Fragment))
            throw new Error("not compatible");
        Fragment that = (Fragment) oThat;
        if (this == that)
            return 0;

        if (this.size() < that.size())
            return -1;
        else if (this.size() > that.size())
            return 1;
        else
        {
            for (int i = 0; i < sTable.fragmentLength; i++)
            {
                byte byte1 = sTable.sequences[sTable.originalRowIDs[rowIDStart]].data[sTable.fragmentOffsets[rowIDStart] + i];
                byte byte2 = that.sTable.sequences[that.sTable.originalRowIDs[that.rowIDStart]].data[that.sTable.fragmentOffsets[that.rowIDStart] + i];
                if (byte1 < byte2)
                    return -1;
                else if (byte1 > byte2)
                    return 1;
            }
            return 0;
        }

    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public final boolean equals(Object object)
    {
        if (object == null)
            return false;

        if (!(object instanceof Fragment))
            return false;

        Fragment that = (Fragment) object;

        if (this.size() != that.size())
            return false;

        if (!this.sTable.alphabet.equals(that.sTable.alphabet))
            return false;

        for (int i = 0; i < size(); ++i)
            if (this.get(i) != that.get(i))
                return false;
        return true;
    }
    
    //  taken from Joshua Bloch's Effective Java
    public int hashCode() {
        int result = super.hashCode();
        result = 37*result+size();
        result = 37*result+sTable.alphabet.hashCode();
        for (int i = 0; i < size(); ++i) {
            result=37*result+get(i).hashCode();
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer sourceSequence = new StringBuffer(rowIDLength);
        for (int i = 0; i < rowIDLength; i++)
        {
            sourceSequence.append(sTable.sequences[sTable.getOriginalRowID(rowIDStart + i)].sequenceID).append(", offset: ").append(sTable.getFragmentOffset(rowIDStart + i)).append(" ");
        }
        StringBuffer fragment = new StringBuffer(sTable.fragmentLength);
        for (int i = 0; i < sTable.fragmentLength; i++)
        {
            fragment.append(get(i));
        }
        return "fragment: " + fragment.toString() + " source: " + sourceSequence.toString();
    }

    /* (non-Javadoc)
     * @see GeDBIT.type.IndexObject#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        super.readExternal(in);
        String indexPrefix = (String) in.readObject();
        sTable = (SequenceTable) TableManager.getTableManager(indexPrefix).getTable(in.readInt());
    }

    /* (non-Javadoc)
     * @see GeDBIT.type.IndexObject#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException
    {
        super.writeExternal(out);
        out.writeObject(sTable.getIndexPrefix());
        out.writeInt(sTable.getTableLocation());
    }
}
