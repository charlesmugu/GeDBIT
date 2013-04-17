package GeDBIT.parallel;

import java.rmi.RemoteException;
import java.util.concurrent.CountDownLatch;

import GeDBIT.dist.Metric;
import GeDBIT.parallel.app.QueryGlobalVPIndex;
import GeDBIT.parallel.rmi.GlobalIndex;

public class GlobalIndexWorkTask implements Task {
    private int threadID;
    private Metric metric;
    private long rootAddress;
    private double[] distList;

    private int counter;
    private double radius;
    private String forprint;

    private CountDownLatch latch;

    public GlobalIndexWorkTask(int threadID, Metric metric, long rootAddress,
	    double[] distList, int counter, double radius, String forprint) {
	this(threadID, metric, rootAddress, distList, counter, radius,
		forprint, null);
    }

    public GlobalIndexWorkTask(int threadID, Metric metric, long rootAddress,
	    double[] distList, int counter, double radius, String forprint,
	    CountDownLatch latch) {
	this.threadID = threadID;
	this.metric = metric;
	this.rootAddress = rootAddress;
	this.distList = distList;
	this.counter = counter;
	this.radius = radius;
	this.forprint = forprint;
	this.latch = latch;
    }

    public void execute() {
	try {
	    GlobalIndex index = QueryGlobalVPIndex.globalIndexs.get(threadID);
	    index.query(metric, rootAddress, distList, counter, radius,
		    forprint);
	} catch (RemoteException e) {
	    e.printStackTrace();
	}
	if (latch != null) {
	    latch.countDown();
	}
    }
}
