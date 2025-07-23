package electrosphere.engine.assetmanager;

import electrosphere.collision.CollisionEngine;

/**
 * Item in a queue of physics meshes to be loaded from disk. Relates world to model path.
 */
public class PhysicsMeshQueueItem {
    
    CollisionEngine collisionEngine;
    String modelPath;

    /**
     * Constructor
     * @param collisionEngine The collision engine
     * @param modelPath The path to the model
     */
    public PhysicsMeshQueueItem(CollisionEngine collisionEngine, String modelPath){
        this.collisionEngine = collisionEngine;
        this.modelPath = modelPath;
    }

}
