package GeDBIT.parallel.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class LocalIndexRMIServer {
    public static void main(String[] args) {
	try {
	    LocateRegistry.createRegistry(1099);
	    LocalIndex index = new LocalIndexImpl();
	    Naming.rebind("local", index);
	    System.out.println("Local RMI server is ready!!");

	} catch (RemoteException e) {
	    e.printStackTrace();
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	}

    }
}
