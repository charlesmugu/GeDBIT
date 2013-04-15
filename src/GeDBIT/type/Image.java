/**
 * edu.utexas.GeDBIT.type.ImageKeyObject 2006.06.19 Copyright Information:
 * Change Log: 2006.06.19: Copied from jdb 1.0, by Rui Mao
 */
package GeDBIT.type;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import GeDBIT.index.TableManager;

/**
 * This is the key of Image Object. This object stores an array of floating
 * numbers representing the key for image object. It is designed only for GeDBIT
 * image dataset, might not work for other datasets.
 * 
 * @author Wenguo Liu, Ru Mao, Willard
 * @version 2006.07.26
 */
public class Image extends IndexObject {
    
    /**
     * 
     */
    private static final long serialVersionUID = -6207971406392822640L;

    private Table table;
    
    /**
     * Floating numbers represent the features for an image.
     */
    private float[]           m_Feas;

    private double[]          max_Dist         = { 1.0, 60.0, 1.0 };

    public Image(){}

    public Image(Table table, int rowID, float[] feas)
    {
        this(table, rowID, feas, null);
    }
    
    /**
     * @param table
     * @param rowID
     * @param feas an array of floats over which the feature values are defined.
     * @param maxDist
     */
    public Image(Table table, int rowID, float[] feas, double[] maxDist)
    {
        super(rowID);
        this.table = table;
        m_Feas = new float[feas.length];
        for (int i = 0; i < feas.length; i++)
            m_Feas[i] = feas[i];
        if (maxDist != null)
        {
            max_Dist = new double[maxDist.length];
            for (int i = 0; i < max_Dist.length; i++)
                max_Dist[i] = maxDist[i];
        }
    }

    /**
     * @param index
     * @return
     */
    public float getFeature(int index)
    {
        return m_Feas[index];
    }

    @Override
    //TODO javadoc
    public int size()
    {
        return m_Feas.length;
    }

    /* (non-Javadoc)
     * @see GeDBIT.type.IndexObject#expand()
     */
    @Override
    public IndexObject[] expand()
    {
        IndexObject[] dbO = new IndexObject[rowIDLength];
        for (int i = 0; i < rowIDLength; i++)
        {
            dbO[i] = new Image(table, rowIDStart+i, m_Feas, max_Dist);
        }
        return dbO;
    }


    /* (non-Javadoc)
     * @see GeDBIT.type.IndexObject#compareTo(GeDBIT.type.IndexObject)
     */
    @Override
    public int compareTo(IndexObject oThat)
    {
        if (!(oThat instanceof Image))
            throw new Error("not compatible");
        Image that = (Image) oThat;
        if (this == that)
            return 0;
        if (this.m_Feas.length < that.m_Feas.length)
            return -1;
        else if (this.m_Feas.length > that.m_Feas.length)
            return 1;
        else
        {
            for (int i = 0; i < m_Feas.length; i++)
            {
                if (m_Feas[i] < that.m_Feas[i])
                    return -1;
                else if (m_Feas[i] > that.m_Feas[i])
                    return 1;
            }
            return 0;
        }

    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object other)
    {
        if (other == this)
            return true;
        if (!(other instanceof Image))
            return false;
        Image image = (Image) other;
        if (this.m_Feas.length != image.m_Feas.length)
            return false;
        for (int i = 0; i < this.m_Feas.length; i++)
            if (Math.abs(m_Feas[i] - image.m_Feas[i]) > 1.0e-10)
            {
                return false;
            }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        int result = 17;
        for (int i = 0; i < m_Feas.length; i++)
        {
            result = 37 * result + Float.floatToIntBits(m_Feas[i]);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer result = new StringBuffer("ImageKeyObject, length :");
        result.append(m_Feas.length).append(", offset : ");
        for (int i = 0; i < m_Feas.length; i++)
            result.append(m_Feas[i]).append(", ");
        return result.toString();
    }
    
    /* (non-Javadoc)
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException
    {
        super.writeExternal(out);
        out.writeInt(m_Feas.length);
        for (int i = 0; i < m_Feas.length; ++i)
            out.writeFloat(m_Feas[i]);
        out.writeInt(max_Dist.length);
        for (int i = 0; i < max_Dist.length; i++)
        {
            out.writeDouble(max_Dist[i]);
        }
        out.writeObject(table.getIndexPrefix());
        out.writeInt(table.getTableLocation());
    }
    

    /* (non-Javadoc)
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        super.readExternal(in);
        this.m_Feas = new float[in.readInt()];
        for (int i = 0; i < m_Feas.length; ++i)
            m_Feas[i] = in.readFloat();
        this.max_Dist = new double[in.readInt()];
        for (int i = 0; i < max_Dist.length; i++)
        {
            max_Dist[i] = in.readDouble();
        }
        String indexPrefix = (String) in.readObject();
        table = TableManager.getTableManager(indexPrefix).getTable(in.readInt());
    }
}
