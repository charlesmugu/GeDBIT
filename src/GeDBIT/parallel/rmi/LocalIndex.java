package GeDBIT.parallel.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LocalIndex extends Remote
{
    public void query(String[] args) throws RemoteException;
}
