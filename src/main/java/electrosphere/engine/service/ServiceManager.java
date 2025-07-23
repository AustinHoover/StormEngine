package electrosphere.engine.service;

import java.util.LinkedList;
import java.util.List;

import electrosphere.engine.Globals;
import electrosphere.engine.signal.SignalService;
import electrosphere.logger.LoggerInterface;

/**
 * Manages all services
 */
public class ServiceManager {
    
    /**
     * The services that this manager created
     */
    public List<Service> trackedServices;

    /**
     * Private constructor
     */
    private ServiceManager(){ }

    /**
     * Initializes the service manager
     */
    public static ServiceManager create(){
        LoggerInterface.loggerEngine.DEBUG("[ServiceManager] Create");
        ServiceManager rVal = new ServiceManager();
        rVal.trackedServices = new LinkedList<Service>();
        return rVal;
    }

    /**
     * Registers a type of service to be created
     * @param serviceClass The class of the service
     * @return The service
     */
    public Service registerService(Service service){
        LoggerInterface.loggerEngine.DEBUG("[ServiceManager] Register service " + service.getName());
        trackedServices.add(service);
        return service;
    }

    /**
     * Instantiates all registered service types
     */
    public void instantiate(){
        for(Service service : trackedServices){
            LoggerInterface.loggerEngine.DEBUG("[ServiceManager] Instantiate service " + service.getName());
            service.init();
        }
        for(Service service : trackedServices){
            if(service instanceof SignalService){
                Globals.engineState.signalSystem.registerService((SignalService)service);
            }
        }
    }

    /**
     * Destroys the service manager and all tracked services
     */
    public void destroy(){
        for(Service service : trackedServices){
            LoggerInterface.loggerEngine.DEBUG("[ServiceManager] Destroy service " + service.getName());
            service.destroy();
        }
        this.trackedServices.clear();
    }

    /**
     * Fires all functions required to unload a scene.
     */
    public void unloadScene(){
        if(this.trackedServices != null){
            for(Service serivce : this.trackedServices){
                serivce.unloadScene();
            }
        }
    }

}
