package electrosphere.entity;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3f;

import electrosphere.engine.Globals;
import electrosphere.renderer.actor.ActorUtils;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerDataCell;
import electrosphere.server.datacell.utils.EntityLookupUtils;
import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;
import electrosphere.server.datacell.utils.ServerEntityTagUtils;
import electrosphere.server.entity.poseactor.PoseActorUtils;

public class EntityCreationUtils {

    /**
     * Spawns an entity that has a position in the world, but isn't necessarily drawable
     * @return the entity
     */
    protected static Entity spawnSpatialEntity(){
        Entity rVal = new Entity();
        rVal.putData(EntityDataStrings.DATA_STRING_POSITION, new Vector3d(0,0,0));
        rVal.putData(EntityDataStrings.DATA_STRING_ROTATION, new Quaterniond().identity());
        rVal.putData(EntityDataStrings.DATA_STRING_SCALE, new Vector3f(1,1,1));
        return rVal;
    }
    
    /**
     * Creates a server entity in the given realm and position. This uses spatial entity as a server entity can't (currently) exist outside of a realm.
     * @param realm The realm to attach the entity to
     * @param position The position to place the entity at
     * @return The entity
     */
    public static Entity createServerEntity(Realm realm, Vector3d position){
        Entity rVal = EntityCreationUtils.spawnSpatialEntity();
        //register to global entity id lookup table
        EntityLookupUtils.registerServerEntity(rVal);
        //assign to realm
        Globals.serverState.realmManager.mapEntityToRealm(rVal, realm);
        //init data cell if it doesn't exist
        ServerDataCell cell = realm.getDataCellManager().getDataCellAtPoint(position);
        if(cell == null){
            //initialize server datacell tracking of this entity
            cell = realm.getDataCellManager().tryCreateCellAtPoint(position);
        }
        //If a server data cell was not created, this is considered illegal state
        if(cell == null){
            throw new IllegalStateException("Failed to create a server data cell");
        }
        //register to entity data cell mapper
        Globals.serverState.entityDataCellMapper.registerEntity(rVal, cell);
        //enable behavior tree tracking
        ServerBehaviorTreeUtils.registerEntity(rVal);

        if(Globals.serverState.entityDataCellMapper.getEntityDataCell(rVal) == null){
            throw new Error("Failed to map entity to cell!");
        }

        return rVal;
    }

    /**
     * Creates a server entity in the given realm and position. This uses spatial entity as a server entity can't (currently) exist outside of a realm.
     * @param realm The realm to attach the entity to
     * @return The entity
     */
    public static Entity createServerInventoryEntity(Realm realm){
        Entity rVal = EntityCreationUtils.spawnSpatialEntity();
        //register to global entity id lookup table
        EntityLookupUtils.registerServerEntity(rVal);
        //assign to realm
        Globals.serverState.realmManager.mapEntityToRealm(rVal, realm);
        //init data cell if it doesn't exist
        ServerDataCell cell = realm.getInventoryCell();
        //If a server data cell was not created, this is considered illegal state
        if(cell == null){
            throw new IllegalStateException("Realm inventory data cell undefined!");
        }
        //register to entity data cell mapper
        Globals.serverState.entityDataCellMapper.registerEntity(rVal, cell);
        //enable behavior tree tracking
        ServerBehaviorTreeUtils.registerEntity(rVal);

        if(Globals.serverState.entityDataCellMapper.getEntityDataCell(rVal) == null){
            throw new Error("Failed to map entity to cell!");
        }

        return rVal;
    }

    /**
     * Spawns an entity that is not attached to a realm (for instance an item in an inventory)
     * @return The entity
     */
    public static Entity createRealmlessServerEntity(){
        Entity rVal = new Entity();
        //register to global entity id lookup table
        EntityLookupUtils.registerServerEntity(rVal);
        //enable behavior tree tracking
        ServerBehaviorTreeUtils.registerEntity(rVal);
        return rVal;
    }

    /**
     * Creates an entity for the client
     * @return The entity
     */
    public static Entity createClientSpatialEntity(){
        Entity rVal = EntityCreationUtils.spawnSpatialEntity();
        Globals.clientState.clientSceneWrapper.getScene().registerEntity(rVal);
        return rVal;
    }

    /**
     * Creates a non-spatial entity for the client
     * @return The entity
     */
    public static Entity createClientNonSpatialEntity(){
        Entity rVal = new Entity();
        Globals.clientState.clientSceneWrapper.getScene().registerEntity(rVal);
        return rVal;
    }


    /**
     * Makes an already created entity a poseable entity by backing it with a PoseActor
     * @param entity The entity
     * @param modelPath The model path for the model to back the pose actor
     */
    public static void makeEntityPoseable(Entity entity, String modelPath){
        entity.putData(EntityDataStrings.POSE_ACTOR, PoseActorUtils.createPoseActorFromModelPath(modelPath));
        if(!entity.containsKey(EntityDataStrings.DATA_STRING_POSITION)){
            entity.putData(EntityDataStrings.DATA_STRING_POSITION, new Vector3d(0,0,0));
        }
        entity.putData(EntityDataStrings.DATA_STRING_ROTATION, new Quaterniond().identity());
        entity.putData(EntityDataStrings.DATA_STRING_SCALE, new Vector3f(1,1,1));
        entity.putData(EntityDataStrings.DATA_STRING_DRAW, true);
        entity.putData(EntityDataStrings.DRAW_SOLID_PASS, true);
        ServerEntityTagUtils.attachTagToEntity(entity, EntityTags.POSEABLE);
    }


    /**
     * MAkes an already created entity a drawable entity (client only) by backing it with an Actor
     * @param entity The entity
     * @param modelPath The model path for the model to back the actor
     */
    public static void makeEntityDrawable(Entity entity, String modelPath){
        entity.putData(EntityDataStrings.DATA_STRING_ACTOR, ActorUtils.createActorFromModelPath(modelPath));
        if(!entity.containsKey(EntityDataStrings.DATA_STRING_POSITION)){
            entity.putData(EntityDataStrings.DATA_STRING_POSITION, new Vector3d(0,0,0));
        }
        entity.putData(EntityDataStrings.DATA_STRING_ROTATION, new Quaterniond().identity());
        entity.putData(EntityDataStrings.DATA_STRING_SCALE, new Vector3f(1,1,1));
        entity.putData(EntityDataStrings.DATA_STRING_DRAW, true);
        entity.putData(EntityDataStrings.DRAW_SOLID_PASS, true);
        Globals.clientState.clientScene.registerEntityToTag(entity, EntityTags.DRAWABLE);
        Globals.clientState.clientScene.registerEntityToTag(entity, EntityTags.DRAW_VOLUMETIC_SOLIDS_PASS);
        Globals.clientState.clientScene.registerEntityToTag(entity, EntityTags.DRAW_CAST_SHADOW);
    }

    /**
     * MAkes an already created entity a drawable entity (client only) by backing it with an Actor
     * @param entity The entity
     * @param modelPath The model path for the model to back the actor
     */
    public static void makeEntityDrawablePreexistingModel(Entity entity, String modelPath){
        entity.putData(EntityDataStrings.DATA_STRING_ACTOR, ActorUtils.createActorOfLoadingModel(modelPath));
        entity.putData(EntityDataStrings.DATA_STRING_DRAW, true);
        entity.putData(EntityDataStrings.DRAW_SOLID_PASS, true);
        Globals.clientState.clientScene.registerEntityToTag(entity, EntityTags.DRAWABLE);
        Globals.clientState.clientScene.registerEntityToTag(entity, EntityTags.DRAW_VOLUMETIC_SOLIDS_PASS);
        Globals.clientState.clientScene.registerEntityToTag(entity, EntityTags.DRAW_CAST_SHADOW);
    }

    /**
     * Alerts the entity to bypass the volumetrics pipeline
     * @param entity The entity
     */
    public static void bypassVolumetics(Entity entity){
        Globals.clientState.clientScene.removeEntityFromTag(entity, EntityTags.DRAW_VOLUMETIC_SOLIDS_PASS);
    }

    /**
     * Alerts the entity to bypass the shadow casting pipeline
     * @param entity The entity
     */
    public static void bypassShadowPass(Entity entity){
        Globals.clientState.clientScene.removeEntityFromTag(entity, EntityTags.DRAW_CAST_SHADOW);
    }

    /**
     * Creates an entity for testing
     * @return The entity
     */
    public static Entity TEST_createEntity(){
        return new Entity();
    }

}
