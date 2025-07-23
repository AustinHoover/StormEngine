package electrosphere.engine.signal;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.logger.LoggerInterface;

/**
 * A signal service implementation to extend
 */
public class SignalServiceImpl implements SignalService {

    /**
     * Thread safe's the service
     */
    Semaphore threadLock = new Semaphore(1);

    /**
     * The list of signals to handle
     */
    List<Signal> signals = new LinkedList<Signal>();

    /**
     * The name of the service
     */
    String serviceName;

    /**
     * The list of signal types that this service wants to listen for
     */
    List<SignalType> targetTypes;


    /**
     * Constructor, used to require certain fields be provided to this service instance
     * @param serviceName The name of the service
     * @param types The types of signals this service wants to subscribe to
     */
    public SignalServiceImpl(String serviceName, SignalType[] types){
        this.serviceName = serviceName;
        this.targetTypes = Arrays.asList(types);
    }

    @Override
    public void init() {
        LoggerInterface.loggerEngine.DEBUG("[" + this.getName() + "] Init");
    }

    @Override
    public void destroy() {
        LoggerInterface.loggerEngine.DEBUG("[" + this.getName() + "] Destroy");
        signals.clear();
        serviceName = null;
    }

    @Override
    public String getName() {
        return this.serviceName;
    }

    @Override
    public void post(Signal signal) {
        threadLock.acquireUninterruptibly();
        LoggerInterface.loggerEngine.DEBUG("[" + this.getName() + "] Post signal " + signal.getId() + " " + signal.getType());
        signals.add(signal);
        threadLock.release();
    }

    /**
     * Handles all signals provided to the main thread
     */
    public void handleAllSignals(){
        //
        //fully copy to this thread
        this.threadLock.acquireUninterruptibly();
        List<Signal> toHandle = new LinkedList<Signal>(signals);
        signals.clear();
        this.threadLock.release();
        //
        //handle signals
        for(Signal signal : toHandle){
            boolean result = this.handle(signal);
            if(!result){
                String message = "Signal provided to service that does not support that signal type!\n" + 
                "Service: " + this.getName() + "\n" +
                "Type: " + signal.getType() + "\n" +
                "ID: " + signal.getId();
                ;
                LoggerInterface.loggerEngine.ERROR(new IllegalStateException(message));
            }
        }
    }

    /**
     * Gets the number of signals that are queued for handling
     * @return The number of signals
     */
    public int getSignalQueueCount(){
        int rVal = 0;
        this.threadLock.acquireUninterruptibly();
        rVal = signals.size();
        this.threadLock.release();
        return rVal;
    }

    /**
     * Handles a signal
     * @param signal The signal
     * @return true if the signal was handled, false if the signal type is not supported
     */
    public boolean handle(Signal signal){
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

    @Override
    public Collection<SignalType> getSubscriptionTargets() {
        return this.targetTypes;
    }

    @Override
    public void unloadScene(){
        this.threadLock.acquireUninterruptibly();
        this.signals.clear();
        this.threadLock.release();
    }
    
}
