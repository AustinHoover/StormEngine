package electrosphere.server.macro.civilization.town;

import electrosphere.server.macro.structure.VirtualStructure;

/**
 * A job that a town has queued
 */
public class TownJob {
    
    /**
     * Types of jobs
     */
    public static enum TownJobType {
        /**
         * Build a structure
         */
        BUILD_STRUCTURE,
    }

    /**
     * The type of the job
     */
    private TownJobType type;

    /**
     * The structure to target
     */
    private VirtualStructure structureTarget;

    /**
     * Private constructor
     */
    private TownJob(){ }

    /**
     * Creates a job to build a structure
     * @param structureTarget The virtual structure
     * @return The job
     */
    public static TownJob createBuildStructure(VirtualStructure structureTarget){
        if(structureTarget == null){
            throw new Error("Target is null!");
        }
        TownJob rVal = new TownJob();
        rVal.type = TownJobType.BUILD_STRUCTURE;
        rVal.structureTarget = structureTarget;
        return rVal;
    }

    /**
     * Gets the type of job
     * @return The type of job
     */
    public TownJobType getType() {
        return type;
    }

    /**
     * Gets the structure that is the target of the job
     * @return The structure
     */
    public VirtualStructure getStructureTarget() {
        return structureTarget;
    }

}
