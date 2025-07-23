package electrosphere.engine.signal;

import java.util.Collection;

import electrosphere.engine.service.Service;
import electrosphere.engine.signal.Signal.SignalType;

/**
 * A service that can receive a signal
 */
public interface SignalService extends Service {
    
    /**
     * Posts a signal to the service
     * @param signal The signal
     */
    public void post(Signal signal);

    /**
     * Gets the collection of signal types that this service wants to subscribe to
     * @return The collection of signal types
     */
    public Collection<SignalType> getSubscriptionTargets();

}
