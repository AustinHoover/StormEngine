package electrosphere.engine.signal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import electrosphere.engine.service.Service;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.logger.LoggerInterface;

/**
 * The core messaging system
 */
public class SignalSystem implements Service {

    /**
     * A map of signal type -> services that consume that type of signal
     */
    Map<SignalType,Set<SignalService>> typeServiceMap;

    /**
     * The lock for thread-safing the system
     */
    ReentrantLock systemLock;

    /**
     * Initializes the signal system
     */
    public void init(){
        typeServiceMap = new HashMap<SignalType,Set<SignalService>>();
        systemLock = new ReentrantLock();
    }

    /**
     * Destroys the signal system
     */
    public void destroy(){
        typeServiceMap = null;
        systemLock = null;
    }

    @Override
    public String getName(){
        return "SignalSystem";
    }

    /**
     * Registers a service to a type of signal
     * @param signalType The type of signal
     * @param service The service to associate with that signal type
     */
    public void registerService(SignalService service){
        systemLock.lock();
        LoggerInterface.loggerEngine.DEBUG("[SignalSystem] Register signal service " + service.getName());
        for(SignalType signalType : service.getSubscriptionTargets()){
            if(typeServiceMap.containsKey(signalType)){
                Set<SignalService> services = this.typeServiceMap.get(signalType);
                if(!services.contains(service)){
                    services.add(service);
                }
            } else {
                Set<SignalService> services = new HashSet<SignalService>();
                services.add(service);
                this.typeServiceMap.put(signalType, services);
            }
        }
        systemLock.unlock();
    }

    /**
     * Registers a service to a type of signal
     * @param signalType The type of signal
     * @param service The service to associate with that signal type
     */
    public void registerServiceToSignal(SignalType signalType, SignalService service){
        systemLock.lock();
        LoggerInterface.loggerEngine.DEBUG("[SignalSystem] Register signal service " + service.getName());
        if(typeServiceMap.containsKey(signalType)){
            Set<SignalService> services = this.typeServiceMap.get(signalType);
            if(!services.contains(service)){
                services.add(service);
            }
        } else {
            Set<SignalService> services = new HashSet<SignalService>();
            services.add(service);
            this.typeServiceMap.put(signalType, services);
        }
        systemLock.unlock();
    }

    /**
     * Deregisters a service from a signal type
     * @param signalType The type of signal
     * @param service The signal service to unassociate from that signal type
     */
    public void deregisterServiceToSignal(SignalType signalType, SignalService service){
        systemLock.lock();
        LoggerInterface.loggerEngine.DEBUG("[SignalSystem] Deregister signal service " + service.getName());
        if(typeServiceMap.containsKey(signalType)){
            Set<SignalService> services = this.typeServiceMap.get(signalType);
            services.remove(service);
        } else {
            //there are no services mapped to this signal type
        }
        systemLock.unlock();
    }

    /**
     * Posts a signal
     * @param type The type of signal
     * @param data The data associated with the signal
     */
    public void post(SignalType type, Object data){
        systemLock.lock();
        if(typeServiceMap.containsKey(type)){
            LoggerInterface.loggerEngine.DEBUG("[SignalSystem] Post signal " + type);
            Signal signal = Signal.create(type, data);
            Set<SignalService> services = this.typeServiceMap.get(type);
            for(SignalService service : services){
                service.post(signal);
            }
        } else {
            //there are no services mapped to this signal type
        }
        systemLock.unlock();
    }

    /**
     * Posts a signal
     * @param type The type of signal
     */
    public void post(SignalType type){
        this.post(type,null);
    }

    /**
     * Posts a signal
     * @param type The type of signal
     */
    public void post(SignalType type, Runnable runnable){
        this.post(type,(Object)runnable);
    }

    @Override
    public void unloadScene(){
    }
    
}
