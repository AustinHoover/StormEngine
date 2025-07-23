package electrosphere.server.simulation.temporal;

import electrosphere.data.macro.temporal.MacroTemporalData;
import electrosphere.engine.Globals;
import electrosphere.net.parser.net.message.LoreMessage;
import electrosphere.server.datacell.Realm;
import electrosphere.util.SerializationUtils;

/**
 * Temporal macro data simulator
 */
public class TemporalSimulator {

    /**
     * Number of temporal ticks per sim frame
     */
    private static final int TEMPORAL_TICKS_PER_SIM_FRAME = 1;

    /**
     * The rate at which to send synchronization packets to clients to update temporal data
     */
    public static final int TEMPORAL_SYNC_RATE = 600;
    
    /**
     * Simulates the temporal macro data
     * @param macroData The macro data
     */
    public static void simulate(Realm realm){
        MacroTemporalData temporalData = realm.getMacroData().getTemporalData();
        temporalData.increment(TemporalSimulator.TEMPORAL_TICKS_PER_SIM_FRAME);
        if(temporalData.getTime() % TEMPORAL_SYNC_RATE == 0){
            String data = SerializationUtils.serialize(temporalData);
            Globals.serverState.server.broadcastMessage(LoreMessage.constructTemporalUpdateMessage(data));
        }
    }

}
