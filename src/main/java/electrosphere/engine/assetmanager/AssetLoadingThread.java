package electrosphere.engine.assetmanager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

public class AssetLoadingThread implements Runnable {

    List<String> pathQueueList = new CopyOnWriteArrayList<String>();

    Map<String,String> pathContentMap = new ConcurrentHashMap<String,String>();

    Semaphore pathTransitionLock = new Semaphore(1);

    @Override
    public void run() {
        boolean running = true;

        while(running){
            
        }
    }

    public boolean containsPath(String path){
        return pathContentMap.containsKey(path);
    }

    public String getContent(String path){
        return pathContentMap.get(path);
    }

    public void queuePath(String path){
        pathTransitionLock.acquireUninterruptibly();
        if(!pathContentMap.containsKey(path) && !pathQueueList.contains(path)){
            pathQueueList.add(path);
        }
        pathTransitionLock.release();
    }
    
}
