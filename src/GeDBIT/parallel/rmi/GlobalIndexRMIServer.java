package GeDBIT.parallel.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class GlobalIndexRMIServer {
    public static void main(String[] args) {
	try {
	    LocateRegistry.createRegistry(1099);
	    GlobalIndex index = new GlobalIndexImpl();
	    Naming.rebind("global", index);
	    System.out.println("Global RMI server is ready!!");
	} catch (RemoteException e) {
	    e.printStackTrace();
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	}
    }
}