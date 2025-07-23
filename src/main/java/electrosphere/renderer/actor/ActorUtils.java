package electrosphere.renderer.actor;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;

/**
 * Utils for dealing with actors
 */
public class ActorUtils {
    
    /**
     * Creates an actor object and queues the model path
     * @param modelPath The model path
     * @return The actor object
     */
    public static Actor createActorFromModelPath(String modelPath){
        Actor rVal = new Actor(modelPath);
        Globals.assetManager.addModelPathToQueue(modelPath);
        return rVal;
    }

    /**
     * Creates an actor from an already-loading path
     * @param modelPath The path
     * @return The actor
     */
    public static Actor createActorOfLoadingModel(String modelPath){
        Actor rVal = new Actor(modelPath);
        return rVal;
    }

    /**
     * Gets the static morph of the actor on the entity
     * @param actorEntity The entity
     * @return The static morph if it exists, null otherwise
     */
    public static ActorStaticMorph getStaticMorph(Entity actorEntity){
        Actor entityActor = EntityUtils.getActor(actorEntity);
        return entityActor.getAnimationData().getStaticMorph();
    }
    
    /**
     * Queues the model underlying an actor for deletion
     * @param actorEntity The entity
     */
    public static void queueActorForDeletion(Entity actorEntity){
        Actor actor = EntityUtils.getActor(actorEntity);
        Globals.assetManager.queueModelForDeletion(actor.getBaseModelPath());
    }
    
}
