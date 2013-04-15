/**
 * GeDBIT.type.DoubleVector 2006.06.16 
 * 
 * Copyright Information: 
 * 
 * Change Log:
 * 2006.06.16: Modified from jdb 1.0, by Ru Mao
 */
package GeDBIT.type;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

/**
 * This class represents space vectors, where each element is a double.
 * 
 * @author Rui Mao, Willard
 * @version 2003.06.06
 */
public class DoubleVectorMR extends IndexObject
{
    /**
     * 
     */
    private static final long serialVersionUID = 1275897899226331100L;

    /**
     * 
     */
    TableMR                     table;

    /** double array to store the data */
    double[]                  data;

    public DoubleVectorMR()
    {
    }

    /**
     * Builds an instance from a space-separated {@link String} of doubles.
     * 
     * @param table
     * @param rowID
     * @param dataString
     */
    public DoubleVectorMR(TableMR table, int rowID, String dataString)
    {
        super(rowID);
        this.table = table;
        String[] row = dataString.split("\\s+");
        data = new double[row.length];
        for (int i = 0; i < row.length; i++)
        {
            data[i] = Double.parseDouble(row[i]);
        }
    }

    /**
     * Builds an instance from a double array.
     * 
     * @param rowID
     * @param data
     *            the double array containning all the elements. cannot be null
     */
    public DoubleVectorMR(TableMR table, int rowID, double[] data)
    {
        super(rowID);
        if (data == null)
            throw new IllegalArgumentException("null data constructing DoubleVector");
        this.table = table;
        this.data = (double[]) data.clone();
    }

    /**
     * @return the double array
     */
    public double[] getData()
    {
        return data;
    }

    /**
     * @return the dimension ( length) of the vector
     */
    public int size()
    {
        return data.length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.type.IndexObject#expand()
     */
    public IndexObject[] expand()
    {
        IndexObject[] dbO = new IndexObject[rowIDLength];
        for (int i = 0; i < rowIDLength; i++)
        {
            dbO[i] = new DoubleVectorMR(table, rowIDStart + i, data);
        }
        return dbO;
    }

    /*
     * (non-Javadoc)
     * 
     * @see GeDBIT.type.IndexObject#compareTo(GeDBIT.type.IndexObject)
     */
    public int compareTo(IndexObject oThat)
    {
        if (!(oThat instanceof DoubleVectorMR))
            throw new ClassCastException("not compatible");
        return compareTo((DoubleVectorMR) oThat);
    }

    /**
     * @param oThat
     * @return
     */
    public int compareTo(DoubleVectorMR oThat)
    {
        DoubleVectorMR that = (DoubleVectorMR) oThat;
        if (this == that)
            return 0;

        if (this.size() < that.size())
            return -1;
        else if (this.size() > that.size())
            return 1;
        else
        {
            for (int i = 0; i < this.size(); i++)
            {
                double double1 = data[i];
                double double2 = that.data[i];
                if (double1 < double2)
                    return -1;
                else if (double1 > double2)
                    return 1;
            }
            return 0;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object that)
    {
        if (!(that instanceof DoubleVectorMR))
            return false;
        return Arrays.equals(this.data, ((DoubleVectorMR) that).data);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    // taken from Joshua Bloch's Effective Java
    public int hashCode()
    {
        int result = 17;
        for (int i = 0; i < data.length; i++)
        {
            long _long = Double.doubleToLongBits(data[i]);
            result = 37 * result + (int) (_long ^ (_long >>> 32));
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer("[DoubleVectorMR, length:");
        sb.append(data.length).append(" data:").append(data[0]);
        for (int i = 1; i < data.length; i++)
            sb.append(", ").append(data[i]);
        sb.append("]");
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        super.readExternal(in);
        data = new double[in.readInt()];
        for (int i = 0; i < data.length; i++)
        {
            data[i] = in.readDouble();
        }
        @SuppressWarnings("unused")
        String indexPrefix = (String) in.readObject();
        //table = TableManager.getTableManager(indexPrefix).getTable(in.readInt());  //Honglong Xu
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException
    {
        super.writeExternal(out);
        out.writeInt(data.length);
        for (int i = 0; i < data.length; i++)
        {
            out.writeDouble(data[i]);
        }
        out.writeObject(table.getIndexPrefix());
        out.writeInt(table.getTableLocation());
    }
}
