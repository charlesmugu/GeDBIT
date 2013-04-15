package GeDBIT.parallel;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

public class WorkThread extends Thread
{
    private int threadID;
    private boolean finished;
    private CountDownLatch latch;
    private LinkedList<Task> queue = null;
    
    public LinkedList<Task> getQueue()
    {
        return queue;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getThreadID()
    {
        return threadID;
    }

    public WorkThread(LinkedList<Task> queue, int threadID, CountDownLatch latch)
    {
        this.finished = false;
        this.queue = queue;
        this.latch = latch;
        this.threadID = threadID;
    }

    public void run()
    {
        Task task = null;
        while(!queue.isEmpty() || !finished)
        {
            synchronized(queue) {
                while(queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                task = queue.removeFirst();
                task.execute();
            }
        }
        latch.countDown();
    }
}
