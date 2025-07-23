package electrosphere.entity.types.foliage;

import electrosphere.data.entity.foliage.AmbientAudio;
import electrosphere.data.entity.foliage.FoliageType;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.entity.state.client.ambientaudio.ClientAmbientAudioTree;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.entity.types.tree.ProceduralTree;
import electrosphere.net.parser.net.message.EntityMessage;
import electrosphere.net.parser.net.message.NetworkMessage;
import electrosphere.net.server.player.Player;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.utils.ServerEntityTagUtils;

import org.joml.Quaterniond;
import org.joml.Vector3d;

/**
 * Utilities for generating foliage
 */
public class FoliageUtils {

    /**
     * Default seed
     */
    public static final long DEFAULT_SEED = 0;

    /**
     * Spawns a basic foliage object
     * @param type The type of foliage object
     * @return The entity for the foliage
     */
    public static Entity clientSpawnBasicFoliage(String type){
        FoliageType rawType = Globals.gameConfigCurrent.getFoliageMap().getType(type);
        Entity rVal;
        if(
            rawType.getGraphicsTemplate().getProceduralModel() != null &&
            rawType.getGraphicsTemplate().getProceduralModel().getTreeModel()!=null
        ){
            rVal = ProceduralTree.clientGenerateProceduralTree(type, DEFAULT_SEED);
        } else {
            rVal = EntityCreationUtils.createClientSpatialEntity();
        }
        //
        //
        //Common entity transforms
        //
        //
        CommonEntityUtils.clientApplyCommonEntityTransforms(rVal, rawType);

        //
        //
        //Foliage specific transforms
        //
        //
        rVal.putData(EntityDataStrings.FOLIAGE_TYPE, rawType);
        rVal.putData(EntityDataStrings.FOLIAGE_IS_SEEDED, true);

        //audio
        if(rawType.getAmbientAudio()!=null){
            AmbientAudio ambientAudio = rawType.getAmbientAudio();
            ClientAmbientAudioTree.attachTree(rVal, ambientAudio);
        }

        return rVal;
    }

    
    /**
     * Spawns a tree foliage on the server
     * @param realm The realm to spawn on
     * @param position The position of the tree
     * @param type the type of tree
     * @param seed the seed for the tree
     * @return the tree entity
     */
    public static Entity serverSpawnTreeFoliage(Realm realm, Vector3d position, String type){
        FoliageType rawType = Globals.gameConfigCurrent.getFoliageMap().getType(type);
        Entity rVal;
        if(
            rawType.getGraphicsTemplate().getProceduralModel() != null &&
            rawType.getGraphicsTemplate().getProceduralModel().getTreeModel()!=null
        ){
            rVal = ProceduralTree.serverGenerateProceduralTree(realm, position, rawType, DEFAULT_SEED);
        } else {
            rVal = EntityCreationUtils.createServerEntity(realm, position);
        }

        //
        //
        //Common entity transforms
        //
        //
        CommonEntityUtils.serverApplyCommonEntityTransforms(realm, position, rVal, rawType);

        //
        //
        //Foliage specific transforms
        //
        //
        //
        ServerEntityTagUtils.attachTagToEntity(rVal, EntityTags.FOLIAGE);
        rVal.putData(EntityDataStrings.FOLIAGE_TYPE, rawType);
        rVal.putData(EntityDataStrings.FOLIAGE_IS_SEEDED, true);

        //position entity
        //this needs to be called at the end of this function.
        //Burried underneath this is function call to initialize a server side entity.
        //The server initialization logic checks what type of entity this is, if this function is called prior to its type being stored
        //the server will not be able to synchronize it properly.
        ServerEntityUtils.initiallyPositionEntity(realm,rVal,position);

        return rVal;
    }

    /**
     * Gets the type of foliage
     * @param entity the entity
     * @return The type
     */
    public static FoliageType getFoliageType(Entity entity){
        return (FoliageType)entity.getData(EntityDataStrings.FOLIAGE_TYPE);
    }

    /**
     * Gets whether the entity is foliage or not
     * @param entity The entity
     * @return true if is foliage, false otherwise
     */
    public static boolean isFoliage(Entity entity){
        return CommonEntityUtils.getEntityType(entity) == EntityType.FOLIAGE;
    }

    /**
     * Gets whether the entity has a foliage seed or not
     * @param entity The entity
     * @return true if is has seed, false otherwise
     */
    public static boolean hasSeed(Entity entity){
        return entity.getData(EntityDataStrings.FOLIAGE_IS_SEEDED)!=null;
    }


    /**
     * Sends a given foliage entity to a given player
     * @param player The player
     * @param foliage The foliage entity
     */
    public static void sendFoliageToPlayer(Player player, Entity foliage){
        int id = foliage.getId();
        FoliageType type = FoliageUtils.getFoliageType(foliage);
        Vector3d position = EntityUtils.getPosition(foliage);
        Quaterniond rotation = EntityUtils.getRotation(foliage);
        if(FoliageUtils.hasSeed(foliage)){
            NetworkMessage message = EntityMessage.constructCreateMessage(
                id,
                EntityType.FOLIAGE.getValue(),
                type.getId(),
                DEFAULT_SEED + "",
                position.x,
                position.y,
                position.z,
                rotation.x,
                rotation.y,
                rotation.z,
                rotation.w
            );
            player.addMessage(message);
        } else {
            //still needs to be implemented with new network message
            throw new UnsupportedOperationException("Tried to spawn a foliage object that doesn't have a seed.");
        }
    }
    
    
}
