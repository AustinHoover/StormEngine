package electrosphere.server.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joml.Vector3d;

import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.logger.LoggerInterface;
import electrosphere.engine.Globals;
import electrosphere.engine.signal.SignalServiceImpl;
import electrosphere.net.server.player.Player;

/**
 * Service that scans areas where players are placing blocks to see if they have formed a complete structure or not
 */
public class StructureScanningService extends SignalServiceImpl {

    /**
     * Number of frames to offset jobs by
     */
    static final int FRAME_OFFSET_FOR_JOBS = 50;

    /**
     * Map of players to the jobs that are scheduled for them
     */
    Map<Player,ScanningJob> playerTimeoutMap = new HashMap<Player,ScanningJob>();

    /**
     * Constructor
     */
    public StructureScanningService(){
        super("StructureScanningService", new SignalType[]{
        });
    }


    /**
     * Queues a job to scan a player's construction to see if it is a structure
     * @param player The player
     * @param position The position
     */
    public void queue(Player player, Vector3d position){
        ScanningJob existing = this.playerTimeoutMap.get(player);
        if(existing == null){
            existing = new ScanningJob(Globals.engineState.timekeeper.getNumberOfRenderFramesElapsed() + StructureScanningService.FRAME_OFFSET_FOR_JOBS, position);
        } else {
            //debounce
            existing.targetFrame = Globals.engineState.timekeeper.getNumberOfRenderFramesElapsed() + StructureScanningService.FRAME_OFFSET_FOR_JOBS;
            existing.position = position;
        }
        this.playerTimeoutMap.put(player,existing);
    }
    
    /**
     * Simulates the service
     */
    public void simulate(){
        Set<Entry<Player,ScanningJob>> jobs = playerTimeoutMap.entrySet();
        for(Entry<Player,ScanningJob> job : jobs){
            if(job.getValue().targetFrame <= Globals.engineState.timekeeper.getNumberOfSimFramesElapsed()){
                //run this job
                playerTimeoutMap.remove(job.getKey());
                StructureScanningService.scanForStructure(job.getValue());
            }
        }
    }

    /**
     * Executes a scanning job
     * @param job The job
     */
    protected static void scanForStructure(ScanningJob job){
        LoggerInterface.loggerEngine.WARNING("Scan structure at " + job.position);
    }

    /**
     * A job to scan a region
     */
    static class ScanningJob {

        /**
         * Frame to perform this job
         */
        long targetFrame;

        /**
         * The position to begin scanning from
         */
        Vector3d position;

        /**
         * Constructor
         * @param targetFrame
         * @param position
         */
        public ScanningJob(long targetFrame, Vector3d position){
            this.targetFrame = targetFrame;
            this.position = position;
        }

    }

}
