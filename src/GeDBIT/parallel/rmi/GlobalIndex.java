package GeDBIT.parallel.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import GeDBIT.dist.Metric;

public interface GlobalIndex extends Remote
{
    public void initialize(String[] args, int serverID) throws RemoteException;
    public void queryResult(String forprint) throws RemoteException;
    public void query(Metric metric, long rootAddress,
            double[] distList, int counter, double radius, String forprint)
            throws RemoteException;
}
