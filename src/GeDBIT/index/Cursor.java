/**
 * GeDBIT.index.Cursor 2006.05.09
 *
 * Copyright Information:
 *
 * Change Log:
 * 2006.05.09: Copied from the original GeDBIT package, by Rui Mao
 */
 
package GeDBIT.index ;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import GeDBIT.dist.Metric;
import GeDBIT.type.DoubleIndexObjectPair;
import GeDBIT.type.IndexObject;
import GeDBIT.util.ObjectIOManager;

/**
 * Implements an {@link Iterator}, iterating on the results set.
 * @author Rui Mao, Willard
 * @version 2006.05.31
 */
@SuppressWarnings("rawtypes")
public abstract class Cursor implements Iterator
{
    ObjectIOManager[] oioms;
    ObjectIOManager oiom;
    Metric metric;
    long rootAddress;
    
    LinkedList <DoubleIndexObjectPair> result;
    
    protected Cursor(ObjectIOManager oiom, Metric metric, long rootAddress)
    {
        this.oiom = oiom;
        this.oioms = null;
        this.metric = metric;
        this.rootAddress = rootAddress;
        
        result = new LinkedList<DoubleIndexObjectPair>();
    }
    protected Cursor(ObjectIOManager[] oioms, Metric metric, long rootAddress)
    {
        this.oiom = null;
        this.oioms = oioms;
        this.metric = metric;
        this.rootAddress = rootAddress;
        
        result = new LinkedList<DoubleIndexObjectPair>();
    }
    public boolean hasNext()
    {
        if (result.size() > 0)
            return true;
        else
        {
            continueSearch();
            return (result.size() > 0);
        }
    }

    public DoubleIndexObjectPair next()
    {
        if (result.size() > 0)
            return expandNext();
        else
        {
            continueSearch();
            if (result.size() > 0)
                return expandNext();
            else
                throw new java.util.NoSuchElementException("The search is already done!");
        }
    }
    
    public DoubleIndexObjectPair expandNext() 
    {
        DoubleIndexObjectPair dioPair = result.remove();
        IndexObject iObject = dioPair.getObject();
        if (iObject instanceof IndexObject) 
        {
            double _double = dioPair.getDouble();
            IndexObject[] cioArray = ((IndexObject) dioPair.getObject()).expand();
            //System.out.println("length=" + cioArray.length);
            for (int i = 0; i < cioArray.length; i++) 
            {
                result.addFirst(new DoubleIndexObjectPair(_double, cioArray[i]));
            }
            return result.remove();
        }
        return dioPair;
    }
    
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
    
    abstract void continueSearch();
    
    abstract void searchResults();
    /**
     * Return a list of all the data points in the subtree rooted at the address
     * @param oiom
     * @param address
     * @return each data point is an IndexObject, consist of a row ID and an index key
     */
    static List<IndexObject> getAllPoints(ObjectIOManager oiom, long address)
    {
        IndexNode node = null;
        try
        {
            node = (IndexNode) oiom.readObject(address);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        LinkedList<IndexObject> result = new LinkedList<IndexObject>();
        for(int pivot = 0; pivot< node.numPivots(); pivot ++)
        {
                result.add(node.getPivot(pivot));
        }
        
        if (node instanceof InternalNode)
        {
            InternalNode iNode = (InternalNode) node;
            for (int child =0; child < iNode.numChildren(); child ++)
                result.addAll(getAllPoints(oiom, iNode.getChildAddress(child)));
        }
        else if (node instanceof LeafNode)
        {
            LeafNode lNode = (LeafNode) node;
            for (int child = 0; child<lNode.numChildren(); child ++) {
                    result.add(lNode.getChild(child));
            }
        }
        else
        {
            throw new UnsupportedOperationException("Node type: " + node.getClass() + " is not supported!");
        }
        
        //decompress
        LinkedList<IndexObject> expandedList = new LinkedList<IndexObject>();
        Iterator<IndexObject> p = result.iterator();
        while (p.hasNext())
        {
            IndexObject o = p.next();
            if ( o instanceof IndexObject)
            {
                IndexObject[] expanded = ((IndexObject) o).expand();
                for( IndexObject temp: expanded)
                    expandedList.addFirst(temp);
            }
            else
                expandedList.addFirst(o);
        }
        return expandedList;
    }
    
    /**
     * Return the number of stored distance values in the subtree rooted at the address
     * @param oiom
     * @param address
     * @return each data point is an IndexObject, consist of a row ID and an index key
     */
    static int getStoredDistanceNumber(ObjectIOManager oiom, long address)
    {
        IndexNode node = null;
        try
        {
            node = (IndexNode) oiom.readObject(address);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        int result = 0;
        if (node instanceof InternalNode)
        {
            VPInternalNode iNode = (VPInternalNode) node;
            result = iNode.numChildren() *iNode.numPivots()*2;
            for (int child =0; child < iNode.numChildren(); child ++)
                result +=getStoredDistanceNumber(oiom, iNode.getChildAddress(child));
                
        }
        else if (node instanceof VPLeafNode)
        {
            VPLeafNode lNode = (VPLeafNode) node;
//            System.out.println(lNode.size());
            if (lNode.numChildren()>0)
                result = lNode.numChildren()*(lNode.getDataPointPathDistance(0).length + lNode.getDataPointPivotDistance(0).length);
            else
                result = 0;
        }
        else
        {
            throw new UnsupportedOperationException("Node type: " + node.getClass() + " is not supported!");
        }
        
        
        return result;
    }
    
    static int getStoredDistanceNumbers(ObjectIOManager[] oioms, long rootAddress, int subtree)
    {
        IndexNode node = null;
        try
        {
            node = (IndexNode) oioms[0].readObject(rootAddress);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        int result = 0;
        if (node instanceof InternalNode)
        {
            VPInternalNode iNode = (VPInternalNode) node;
            result = iNode.numChildren() *iNode.numPivots()*2;
            for (int child =0; child < iNode.numChildren(); child ++)
                result +=getStoredDistanceNumber(oioms[child % subtree + 1], iNode.getChildAddress(child));
        }
        else if (node instanceof VPLeafNode)
        {
            VPLeafNode lNode = (VPLeafNode) node;
            if (lNode.numChildren()>0)
                result = lNode.numChildren()*(lNode.getDataPointPathDistance(0).length + lNode.getDataPointPivotDistance(0).length);
            else
                result = 0;
        }
        else
        {
            throw new UnsupportedOperationException("Node type: " + node.getClass() + " is not supported!");
        }
        return result;
    }
    
    /**
     * Return the total number of nodes in the subtree rooted at the address
     * @param oiom
     * @param address
     * @return 
     */
    static int getNodeNumber(ObjectIOManager oiom, long address)
    {
        int result = 1;
        IndexNode node = null;
        try
        {
            node = (IndexNode) oiom.readObject(address);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        if (node instanceof InternalNode)
        {
            InternalNode iNode = (InternalNode) node;
            for (int child =0; child < iNode.numChildren(); child ++)
                result += getNodeNumber(oiom, iNode.getChildAddress(child));
        }
        
        return result;
    }
    
    /**
     * Return the total number of internal index node in the subtree rooted at the address
     * @param oiom
     * @param address
     * @return 
     */
    static int getInternalNodeNumber(ObjectIOManager oiom, long address)
    {
        int result = 0;
        IndexNode node = null;
        try
        {
            node = (IndexNode) oiom.readObject(address);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        if (node instanceof InternalNode)
        {
            result ++;
            InternalNode iNode = (InternalNode) node;
            for (int child =0; child < iNode.numChildren(); child ++)
                result += getInternalNodeNumber(oiom, iNode.getChildAddress(child));
        }
        return result;
    }
    
    /**
     * Return the total number of leaf index node in the subtree rooted at the address
     * @param oiom
     * @param address
     * @return 
     */
    static int getLeafNodeNumber(ObjectIOManager oiom, long address)
    {
        int result = 0;
        IndexNode node = null;
        try
        {
            node = (IndexNode) oiom.readObject(address);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        if (node instanceof InternalNode)
        {
            InternalNode iNode = (InternalNode) node;
            for (int child =0; child < iNode.numChildren(); child ++)
                result += getLeafNodeNumber(oiom, iNode.getChildAddress(child));
        }
        else if (node instanceof LeafNode)
        {
            return 1;
        }
        else
        {
            throw new UnsupportedOperationException("Node type: " + node.getClass() + " is not supported!");
        }
        return result;
    }
    

    abstract protected void visit(QueryTask t);
    
    protected class QueryTask{}
}
