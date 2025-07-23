package electrosphere.client.service;

import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.server.simulation.temporal.TemporalSimulator;
import electrosphere.util.math.BasicMathUtils;

import java.util.concurrent.locks.ReentrantLock;

import electrosphere.data.macro.temporal.MacroTemporalData;
import electrosphere.engine.signal.SignalServiceImpl;

/**
 * Synchronizes and interpolates temporal data between server and client
 */
public class ClientTemporalService extends SignalServiceImpl {

    /**
     * Lerp rate for synchronization
     */
    private static final double LERP_RATE = 1.0 / (double)TemporalSimulator.TEMPORAL_SYNC_RATE;

    /**
     * The client's stored temporal data
     */
    private MacroTemporalData clientTemporalData = new MacroTemporalData();

    /**
     * The latest temporal data from the server
     */
    private MacroTemporalData latestServerData = new MacroTemporalData();

    /**
     * Lock for thread-safeing the service
     */
    private ReentrantLock lock = new ReentrantLock();

    /**
     * Constructor
     */
    public ClientTemporalService() {
        super("ClientTemporalService", new SignalType[]{
        });
    }

    /**
     * Simulates the service
     */
    public void simulate(){
        lock.lock();
        clientTemporalData.setTime((long)BasicMathUtils.lerp((double)clientTemporalData.getTime(), (double)latestServerData.getTime(), LERP_RATE));
        lock.unlock();
    }

    /**
     * Sets the latest server data
     * @param serverData The latest server data
     */
    public void setLatestData(MacroTemporalData serverData){
        lock.lock();
        this.latestServerData = serverData;
        lock.unlock();
    }

    /**
     * Gets the time of day of the service
     * @return The time of day of the service
     */
    public float getTime(){
        return (float)((double)clientTemporalData.getTime() / (double)MacroTemporalData.TIME_PER_DAY) % 1.0f;
    }
    
}
