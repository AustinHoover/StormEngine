package electrosphere.server.datacell.gridded;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

import electrosphere.engine.Globals;
import electrosphere.engine.threads.ThreadCounts;
import electrosphere.logger.LoggerInterface;

/**
 * Manages loading/unloading data cells for the gridded data cell manager
 */
public class GriddedDataCellLoaderService {

    /**
     * Used for loading/unloading the cells
     */
    private ExecutorService ioThreadService = null;

    /**
     * Lock for structures in this service
     */
    private static final ReentrantLock lock = new ReentrantLock();

    /**
     * Map of cell key -> job queued for that cell
     */
    private static final Map<Long,Future<?>> queuedWorkLock = new HashMap<Long,Future<?>>();

    /**
     * Map of a job to its corresponding work
     */
    private static final Map<Long,Runnable> jobOperationMap = new HashMap<Long,Runnable>();

    /**
     * Constructor
     */
    public GriddedDataCellLoaderService(){
        this.ioThreadService = Globals.engineState.threadManager.requestFixedThreadPool(ThreadCounts.GRIDDED_DATACELL_LOADING_THREADS);
    }

    /**
     * Queues an operation that requires a read or write of a location's file data.
     * Guarantees that all operations will be run in order without losing any work.
     * @param key The key for the cell
     * @param operation The operation to perform
     */
    protected void queueLocationBasedOperation(long key, Runnable operation){
        lock.lock();
        //if there is a job queued and we couldn't cancel it, wait
        Future<?> job = queuedWorkLock.get(key);
        Runnable opCallback = () -> {
            //work here
            operation.run();
            //let the service know we've finished this job
            lock.lock();
            queuedWorkLock.remove(key);
            jobOperationMap.remove(key);
            lock.unlock();
        };
        jobOperationMap.put(key,operation);
        if(job != null){
            if(!job.cancel(false)){
                try {
                    Globals.profiler.beginCpuSample("Waiting for cell io job to finish");
                    job.get();
                    Globals.profiler.endCpuSample();
                } catch (InterruptedException e) {
                    LoggerInterface.loggerEngine.ERROR("Failed to wait for previous job for cell!", e);
                } catch (ExecutionException e) {
                    LoggerInterface.loggerEngine.ERROR("Previous job for cell threw an error!", e);
                }
            }
            if(job.isCancelled()){
                Runnable oldOp = jobOperationMap.remove(key);
                //queue job to run the old operation first, then the new one
                Runnable currentOp = () -> {
                    oldOp.run();
                    operation.run();
                };
                opCallback = () -> {
                    currentOp.run();
                    //let the service know we've finished this job
                    lock.lock();
                    queuedWorkLock.remove(key);
                    jobOperationMap.remove(key);
                    lock.unlock();
                };
                jobOperationMap.put(key,currentOp);
            }
        }
        //queue job to do the operation
        Future<?> newJob = ioThreadService.submit(opCallback);
        queuedWorkLock.put(key, newJob);
        lock.unlock();
    }

    /**
     * Halts the threads for the data cell loader service
     */
    public void haltThreads(){
        ioThreadService.shutdownNow();
    }
    
}
