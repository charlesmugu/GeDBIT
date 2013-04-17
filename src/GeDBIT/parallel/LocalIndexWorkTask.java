package GeDBIT.parallel;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import GeDBIT.parallel.rmi.LocalIndex;

public class LocalIndexWorkTask implements Task {
    private String server;
    private String[] args;

    public LocalIndexWorkTask(String server, String[] args) {
	this.server = server;
	this.args = args;
    }

    public void execute() {
	try {
	    LocalIndex index = (LocalIndex) Naming.lookup("rmi://" + server
		    + "/local");
	    index.query(args);
	} catch (RemoteException e) {
	    e.printStackTrace();
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	} catch (NotBoundException e) {
	    e.printStackTrace();
	}
    }
}
