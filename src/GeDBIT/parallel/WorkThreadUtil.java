package GeDBIT.parallel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

public class WorkThreadUtil
{
    private static Map<Integer, Runnable> threadspool = null;
    private static boolean waitEachQueryFinished = false;
    private static WorkThreadUtil workThreadUtil = null;
    private static CountDownLatch latch = null;
    
    
    public static void setWaitEachQueryFinished(boolean waitEachQueryFinished)
    {
        WorkThreadUtil.waitEachQueryFinished = waitEachQueryFinished;
    }

    public static boolean isWaitEachQueryFinished()
    {
        return waitEachQueryFinished;
    }

    private WorkThreadUtil(int threads)
    {
        threadspool = new HashMap<Integer, Runnable>();
        latch = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++)
        {
            LinkedList<Task> tasks = new LinkedList<Task>();
            WorkThread thread = new WorkThread(tasks, i, latch);
            threadspool.put(i, thread);
            thread.start();
        }
    }
    
    public static WorkThreadUtil newInstance()
    {
        if(workThreadUtil == null)
        {
            workThreadUtil = new WorkThreadUtil(1);
        }
        return workThreadUtil;
    }
    
    public static WorkThreadUtil newInstance(int threads)
    {
        if(workThreadUtil == null)
        {
            workThreadUtil = new WorkThreadUtil(threads);
        }
        return workThreadUtil;
    }

    public static Map<Integer, Runnable> getThreadsPool()
    {
        return threadspool;
    }
    
    public static void setFinishedStatus()
    {
        Iterator<Entry<Integer, Runnable>> it = threadspool.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry<Integer, Runnable> entry = it.next();
            WorkThread thread = (WorkThread)entry.getValue();
            thread.setFinished(true);
            LinkedList<Task> queue = thread.getQueue();
            synchronized (queue)
            {
                queue.addLast(new GlobalIndexEndTask());
                queue.notify();
            }
        }
    }
    public static void waitAllQueryFinished()
    {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
