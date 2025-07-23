package electrosphere.server.entity.poseactor;

import electrosphere.engine.Globals;

/**
 * Utilities for working with pose actors
 */
public class PoseActorUtils {
    
    /**
     * Creates a pose actor object and queues the model path
     * @param modelPath The model path
     * @return The PoseActor object
     */
    public static PoseActor createPoseActorFromModelPath(String modelPath){
        PoseActor rVal = new PoseActor(modelPath);
        Globals.assetManager.addPoseModelPathToQueue(modelPath);
        return rVal;
    }

}
