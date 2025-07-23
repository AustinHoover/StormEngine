package electrosphere.script.translation;

import org.joml.Vector3d;

import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.entity.types.creature.CreatureUtils;

/**
 * Server utilities provided to the js context
 */
public class JSServerUtils {
    
    /**
     * Spawns a creature
     * @param creatureType The creature
     */
    public static void spawnCreature(int sceneInstanceId, String creatureType, Vector3d position){
        //TODO: find realm from scene id
        CreatureUtils.serverSpawnBasicCreature(null, position, creatureType, null);
    }

    /**
     * Gets the position of an entity
     * @param entity The entity
     * @return The position of the entity
     */
    public static Vector3d getPosition(Entity entity){
        return EntityUtils.getPosition(entity);
    }

    /**
     * Sets the position of an entity
     * @param entity The entity
     * @param vector THe new position of the entity
     */
    public static void setPosition(Entity entity, Vector3d vector){
        ServerEntityUtils.repositionEntity(entity, vector);
    }

}
