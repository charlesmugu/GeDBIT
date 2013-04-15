package GeDBIT.parallel.app;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

import GeDBIT.parallel.LocalIndexWorkTask;
import GeDBIT.parallel.Task;
import GeDBIT.parallel.WorkThread;
import GeDBIT.parallel.WorkThreadUtil;


public class QueryLocalVPIndex
{
    public static String[] servers = null;

    public static void main(String[] args) {
        int subtree = 1;
        String file = "";
        
        for (int i = 0; i < args.length; i = i + 2)
        {
            if (args[i].equalsIgnoreCase("-st"))
                subtree = Integer.parseInt(args[i + 1]);
            else if (args[i].equalsIgnoreCase("-sv"))
                file = args[i + 1];
            else 
                continue;
        }
        BufferedReader br;
        servers = new String[subtree];
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String data = null;
            int counter = 0;
            while((data = br.readLine()) != null && counter < subtree)
            {
                servers[counter++] = data;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        WorkThreadUtil.newInstance(subtree);
        final double startTime = System.currentTimeMillis();
        for (int i = 0; i < subtree; i++) {
            WorkThread thread = (WorkThread) WorkThreadUtil.getThreadsPool().get(i);
            Task newTask = new LocalIndexWorkTask(servers[i], args);
            LinkedList<Task> queue = thread.getQueue();
            synchronized (queue)
            {
                queue.addLast(newTask);
                queue.notify();
            }
        }
        WorkThreadUtil.setFinishedStatus();
        WorkThreadUtil.waitAllQueryFinished();
        final double endTime = System.currentTimeMillis();
        final double t = (endTime - startTime) / 1000;
        System.out.println("Total search time: " + t);
    }
}
