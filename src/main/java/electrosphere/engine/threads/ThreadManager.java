package electrosphere.engine.threads;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import electrosphere.client.terrain.foliage.FoliageModel;
import electrosphere.engine.Globals;
import electrosphere.engine.loadingthreads.LoadingThread;
import electrosphere.engine.threads.LabeledThread.ThreadLabel;
import electrosphere.entity.types.terrain.BlockChunkEntity;
import electrosphere.entity.types.terrain.TerrainChunk;
import electrosphere.server.ai.services.PathfindingService;
import electrosphere.util.CodeUtils;

/**
 * Manages all running threads
 */
public class ThreadManager {
    
    /**
     * Threadsafes the manager
     */
    private ReentrantLock threadLock;

    /**
     * All threads that are actively running
     */
    private List<LabeledThread> activeThreads;

    /**
     * All loading threads that are actively running
     */
    private List<LoadingThread> loadingThreads;

    /**
     * Used by main thread to alert other threads whether they should keep running or not
     */
    private boolean shouldKeepRunning;

    /**
     * Tracks all executors created
     */
    private static List<ExecutorService> executors = new LinkedList<ExecutorService>();

    /**
     * The default executor service
     */
    private ExecutorService defaultService;


    /**
     * Initializes the thread manager
     */
    public void init(){
        threadLock = new ReentrantLock();
        activeThreads = new LinkedList<LabeledThread>();
        loadingThreads = new LinkedList<LoadingThread>();
        shouldKeepRunning = true;
        this.defaultService = this.requestFixedThreadPool(ThreadCounts.DEFAULT_SERVICE_THREADS);
    }

    /**
     * Updates what threads are being tracked
     */
    public void update(){
        threadLock.lock();
        //
        //remove loading threads
        List<Thread> threadsToRemove = new LinkedList<Thread>();
        for(LoadingThread thread : loadingThreads){
            if(thread.isDone()){
                threadsToRemove.add(thread);
            }
        }
        for(Thread thread : threadsToRemove){
            loadingThreads.remove(thread);
        }

        threadLock.unlock();
    }

    /**
     * Starts a new thread with tracking
     * @param thread The thread to start
     */
    public void start(ThreadLabel label, Thread thread){
        if(label.toString() == null || label.toString().length() < 1){
            throw new Error("Invalid label name! " + label.toString());
        }
        threadLock.lock();
        activeThreads.add(new LabeledThread(label, thread));
        thread.setName(label.toString());
        thread.start();
        threadLock.unlock();
    }

    /**
     * Starts a new loading thread with tracking
     * @param thread The loading thread to start
     */
    public void start(LoadingThread thread){
        threadLock.lock();
        activeThreads.add(new LabeledThread(ThreadLabel.LOADING, thread));
        loadingThreads.add(thread);
        thread.start();
        threadLock.unlock();
    }

    /**
     * Checks if any loading threads are active
     * @return true if there is an active loading thread, false otherwise
     */
    public boolean isLoading(){
        return loadingThreads.size() > 0;
    }

    /**
     * Gets the list of loading threads
     * @return The list of loading threads
     */
    public List<LoadingThread> getLoadingThreads(){
        return Collections.unmodifiableList(loadingThreads);
    }

    /**
     * Waits for a named thread to close
     * @param label the label of the thread
     */
    public void awaitThreadClose(ThreadLabel label){
        boolean running = true;
        while(running){
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                running = false;
            }
            running = this.isRunning(label);
        }
    }

    /**
     * Tries to close all threads
     */
    public void close(){
        this.shouldKeepRunning = false;
        threadLock.lock();

        //for some reason, server must be explicitly closed
        if(Globals.serverState.server != null){
            Globals.serverState.server.close();
        }

        if(Globals.serverState.realmManager != null && Globals.serverState.realmManager.getRealms() != null){
            Globals.serverState.realmManager.reset();
        }

        /**
         * Halds all terrain chunk threads
         */
        TerrainChunk.haltThreads();
        FoliageModel.haltThreads();
        BlockChunkEntity.haltThreads();
        PathfindingService.haltThreads();

        /**
         * Halt all requested executors
         */
        for(ExecutorService service : executors){
            service.shutdownNow();
        }

        //
        //interrupt all threads
        for(int i = 0; i < 3; i++){
            for(LabeledThread thread : activeThreads){
                thread.getThread().interrupt();
                try {
                    thread.getThread().join(10);
                    if(thread.getThread().isAlive()){
                        String errorMessage = "Failed to interrupt thread! " + thread.getLabel();
                        System.err.println(errorMessage);
                        throw new Error(errorMessage);
                    }
                } catch (InterruptedException e) {
                    CodeUtils.todo(e, "Think about how to handle this");
                }
            }
            try {
                TimeUnit.MILLISECONDS.sleep(3);
            } catch (InterruptedException e) {
                CodeUtils.todo(e, "Handle failing to sleep while interrupting all other threads");
            }
        }
        threadLock.unlock();
    }

    /**
     * Checks if the thread should keep running or not
     * @return true if should keep running, false otherwise
     */
    public boolean shouldKeepRunning(){
        return this.shouldKeepRunning;
    }

    /**
     * Interrupts all thread under a label
     * @param label The label
     */
    public void interruptLabel(ThreadLabel label){
        threadLock.lock();
        //
        //interrupt threads
        for(LabeledThread thread : activeThreads){
            if(thread.getLabel() == label){
                thread.getThread().interrupt();
            }
        }
        threadLock.unlock();
    }

    /**
     * Updates the list of active threads
     */
    private void updateActiveThreads(){
        this.threadLock.lock();
        List<LabeledThread> clearQueue = new LinkedList<LabeledThread>();
        for(LabeledThread thread : activeThreads){
            if(!thread.getThread().isAlive()){
                clearQueue.add(thread);
            }
        }
        activeThreads.removeAll(clearQueue);
        this.threadLock.unlock();
    }

    /**
     * Checks if a thread label is running
     * @param label The label
     * @return true if it is actively running, false otherwise
     */
    public boolean isRunning(ThreadLabel label){
        boolean rVal = false;
        threadLock.lock();
        this.updateActiveThreads();
        for(LabeledThread thread : activeThreads){
            if(thread.getLabel() == label){
                rVal = true;
            }
        }
        threadLock.unlock();
        return rVal;
    }

    /**
     * Requests a fixed-size thread pool
     * @param threads The number of threads
     * @return The executor
     */
    public ExecutorService requestFixedThreadPool(int threads){
        if(threads < 1){
            throw new Error("Requested invalid number of threads! " + threads);
        }
        ExecutorService rVal = null;
        threadLock.lock();
        rVal = Executors.newFixedThreadPool(threads);
        executors.add(rVal);
        threadLock.unlock();
        return rVal;
    }

    /**
     * Dispatches a runnable to the default thread service
     * @param runnable The runnable
     */
    public void dispatch(Runnable runnable){
        threadLock.lock();
        this.defaultService.submit(runnable);
        threadLock.unlock();
    }

    /**
     * Dispatches a runnable to the default thread service
     * @param runnable The runnable
     */
    public <T> Future<T> dispatch(Callable<T> runnable){
        threadLock.lock();
        Future<T> rVal = this.defaultService.submit(runnable);
        threadLock.unlock();
        return rVal;
    }

}
