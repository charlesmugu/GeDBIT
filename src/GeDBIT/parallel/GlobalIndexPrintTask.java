package GeDBIT.parallel;

import java.rmi.RemoteException;
import java.util.concurrent.CountDownLatch;

import GeDBIT.parallel.app.QueryGlobalVPIndex;
import GeDBIT.parallel.rmi.GlobalIndex;

public class GlobalIndexPrintTask implements Task
{
    private int threadID;
    private String forprint;
    private CountDownLatch latch;
    
    public GlobalIndexPrintTask(int threadID, String forprint)
    {
        this(threadID, forprint, null);
    }
    
    public GlobalIndexPrintTask(int threadID, String forprint, CountDownLatch latch)
    {
        this.threadID = threadID;
        this.forprint = forprint;
        this.latch = latch;
    }

    public void execute()
    {
        try
        {
            GlobalIndex index = QueryGlobalVPIndex.globalIndexs.get(threadID);
            index.queryResult(forprint);
        } catch (RemoteException e)
        {
            e.printStackTrace();
        }
        if(latch != null)
        {
            latch.countDown();
        }
    }
}
