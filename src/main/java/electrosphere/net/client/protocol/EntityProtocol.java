package electrosphere.net.client.protocol;

import java.util.Arrays;
import java.util.List;

import org.joml.Quaterniond;
import org.joml.Vector3d;

import electrosphere.data.entity.creature.CreatureData;
import electrosphere.data.entity.creature.ViewModelData;
import electrosphere.engine.Globals;
import electrosphere.entity.ClientEntityUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.state.physicssync.ClientPhysicsSyncTree;
import electrosphere.entity.types.EntityTypes;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.foliage.FoliageUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.EntityMessage;
import electrosphere.net.parser.net.message.EntityMessage.EntityMessageType;
import electrosphere.net.synchronization.transport.StateCollection;
import electrosphere.net.template.ClientProtocolTemplate;
import electrosphere.server.datacell.utils.EntityLookupUtils;
import electrosphere.util.Utilities;

/**
 * Client entity network protocol
 */
public class EntityProtocol implements ClientProtocolTemplate<EntityMessage> {

    //Messages to ignore when complaining about messages that have nonexistant entity associated
    static List<EntityMessageType> idModifyingMessages = Arrays.asList(new EntityMessageType[]{
        EntityMessageType.CREATE,
        EntityMessageType.DESTROY,
    });

    @Override
    public EntityMessage handleAsyncMessage(EntityMessage message) {
        return message;
    }

    @Override
    public void handleSyncMessage(EntityMessage message) {
        Globals.profiler.beginAggregateCpuSample("EntityProtocol.handleSyncMessage");
        LoggerInterface.loggerNetworking.DEBUG_LOOP("Parse entity message of type " + message.getMessageSubtype());

        if(Globals.clientState.clientScene != null && Globals.clientState.clientSynchronizationManager.isDeleted(message.getentityID())){
            Globals.profiler.endCpuSample();
            return;
        }

        //error check
        if(Globals.clientState.clientScene != null && Globals.clientState.clientSceneWrapper.getEntityFromServerId(message.getentityID()) == null && !idModifyingMessages.contains(message.getMessageSubtype())){
            LoggerInterface.loggerNetworking.WARNING("Client received packet for entity that is not in the client scene!"); 
            Globals.clientState.clientSceneWrapper.dumpTranslationLayerStatus();
            Globals.clientState.clientSceneWrapper.dumpIdData(message.getentityID());
            Entity serverEntity = EntityLookupUtils.getEntityById(message.getentityID());
            LoggerInterface.loggerNetworking.WARNING("Entity type: " + CommonEntityUtils.getEntityType(serverEntity));
            LoggerInterface.loggerNetworking.WARNING("Entity subtype: " + CommonEntityUtils.getEntitySubtype(serverEntity));
            LoggerInterface.loggerNetworking.WARNING("Message type: " + message.getMessageSubtype());
        }

        switch(message.getMessageSubtype()){


            //
            //
            //  SPAWNING STUFF IN
            //
            //
            case CREATE: {
                LoggerInterface.loggerNetworking.DEBUG(
                    "Spawn ID " + message.getentityID() + " of type " + message.getentityCategory() + " subtype " + message.getentitySubtype() + 
                    " @ " + message.getpositionX() + " " + message.getpositionY() + " " + message.getpositionZ()
                );
                EntityType type = EntityTypes.fromInt(message.getentityCategory());
                switch(type){
                    case CREATURE: {
                        EntityProtocol.spawnCreature(message);
                    } break;
                    case ITEM: {
                        EntityProtocol.spawnItem(message);
                    } break;
                    case FOLIAGE: {
                        EntityProtocol.spawnFoliage(message);
                    } break;
                    case COMMON: {
                        EntityProtocol.spawnCommon(message);
                    } break;
                    case ENGINE: {
                        throw new Error("Unsupported entity type!");
                    }
                }
                Globals.clientState.clientConnection.release(message);
            } break;

            //
            //
            // UPDATING PROPERTIES
            //
            //
            case SETPROPERTY: {
                if(Globals.clientState.clientSceneWrapper.serverToClientMapContainsId(message.getentityID())){
                    if(message.getpropertyType() == 0){
                        EntityProtocol.setPlayerEntity(message);
                    }
                } else {
                    //TODO: bounce message
                    LoggerInterface.loggerNetworking.WARNING("Received property packet for entity that does not exist on client!");
                }
                Globals.clientState.clientConnection.release(message);
            } break;
            case ATTACHENTITYTOENTITY: {
                Entity child = Globals.clientState.clientSceneWrapper.getEntityFromServerId(message.getentityID());
                Entity parent = Globals.clientState.clientSceneWrapper.getEntityFromServerId(message.gettargetID());
                LoggerInterface.loggerNetworking.DEBUG("Attach " + message.getentityID() + " to " + message.gettargetID() + " on bone " + message.getbone());
                if(child != null && parent != null){
                    AttachUtils.clientAttachEntityToEntityAtBone(
                        parent,
                        child,
                        message.getbone(),
                        new Vector3d(),
                        new Quaterniond()
                    );
                }
                Globals.clientState.clientConnection.release(message);
            } break;
            case MOVEUPDATE: {
                Entity target = Globals.clientState.clientSceneWrapper.getEntityFromServerId(message.getentityID());
                if(target != null){
                    CreatureUtils.clientAttachEntityMessageToMovementTree(target,message);
                }
            } break;
            case ATTACKUPDATE: {
                LoggerInterface.loggerNetworking.WARNING("Received deprecated attack update message!");
            } break;

            case SYNCPHYSICS: {
                Entity entity = Globals.clientState.clientSceneWrapper.getEntityFromServerId(message.getentityID());
                if(entity != null && ClientPhysicsSyncTree.hasTree(entity)){
                    ClientPhysicsSyncTree.getTree(entity).setMessage(message);
                }
            } break;


            //
            //
            // DESTROYING AND DESTRUCTING STUFF
            //
            //
            case DESTROY: {
                Entity entity = Globals.clientState.clientSceneWrapper.getEntityFromServerId(message.getentityID());
                if(entity != null){
                    ClientEntityUtils.destroyEntity(entity);
                }
                Globals.clientState.clientSynchronizationManager.addDeletedId(message.getentityID());
                Globals.clientState.clientConnection.release(message);
            } break;



            case INTERACT:
            case UPDATEENTITYVIEWDIR:
            case KILL:
                //to be implemented
                throw new UnsupportedOperationException();
            case STARTATTACK:
                //silently ignore
                break;
        }
        Globals.profiler.endCpuSample();
    }

    /**
     * Sets the player's entity
     * @param message the network message to parse
     */
    static void setPlayerEntity(EntityMessage message){
        Entity target = Globals.clientState.clientSceneWrapper.getEntityFromServerId(message.getentityID());
        if(target != null){
            LoggerInterface.loggerNetworking.DEBUG("Set player entity id for entity: " + target.getId() + " to player: " + message.getpropertyValue());
            CreatureUtils.setControllerPlayerId(target, message.getpropertyValue());
            String creatureTypeRaw = CreatureUtils.getType(target);
            CreatureData creatureType = Globals.gameConfigCurrent.getCreatureTypeLoader().getType(creatureTypeRaw);
            ViewModelData viewModelData = creatureType.getViewModelData();
            if(Globals.clientState.clientPlayer != null && message.getpropertyValue() == Globals.clientState.clientPlayer.getId()){
                LoggerInterface.loggerNetworking.DEBUG("Set this player's entity id!");
                Globals.clientState.playerEntity = target;
                if(viewModelData != null && viewModelData.getFirstPersonModelPath() != null){
                    Globals.clientState.firstPersonEntity = CreatureUtils.clientCreateViewModel(
                        creatureTypeRaw,
                        null
                    );
                }
            } else {
                //setting player id on entity that is not this player's
            }
        } else {
            LoggerInterface.loggerNetworking.WARNING("Tried to set player entity property on an entity that doesn't exist!");
            Globals.clientState.clientSceneWrapper.dumpTranslationLayerStatus();
        }
    }

    /**
     * Spawns a creature
     * @param message The message
     * @return The item
     */
    static Entity spawnCreature(EntityMessage message){
        LoggerInterface.loggerNetworking.DEBUG("[CLIENT] Spawn Creature " + message.getentityID() + " at " + message.getpositionX() + " " + message.getpositionY() + " " + message.getpositionZ());
        ObjectTemplate template = Utilities.deserialize(message.getcreatureTemplate(), ObjectTemplate.class);
        Entity newlySpawnedEntity = CreatureUtils.clientSpawnBasicCreature(template.getObjectType(),template);
        ClientEntityUtils.initiallyPositionEntity(
            newlySpawnedEntity,
            new Vector3d(message.getpositionX(),message.getpositionY(),message.getpositionZ()),
            new Quaterniond(message.getrotationX(),message.getrotationY(),message.getrotationZ(),message.getrotationW())
        );
        Globals.clientState.clientSceneWrapper.mapIdToId(newlySpawnedEntity.getId(), message.getentityID());
        //if the creature template includes an equip section, spawn all the equipped items
        CreatureUtils.clientApplyTemplate(newlySpawnedEntity, template);
        //apply state synchronization if present
        if(template != null && template.getStateCollection() != null && template.getStateCollection().getValues() != null){
            StateCollection.clientApplyStateCollection(newlySpawnedEntity, template.getStateCollection());
        }
        return newlySpawnedEntity;
    }

    /**
     * Spawns an item
     * @param message The network message
     * @return The item
     */
    static Entity spawnItem(EntityMessage message){
        LoggerInterface.loggerNetworking.DEBUG("[CLIENT] Spawn Item " + message.getentityID() + " at " + message.getpositionX() + " " + message.getpositionY() + " " + message.getpositionZ());
        //spawn item
        String itemType = message.getentitySubtype();
        Entity newlySpawnedEntity = ItemUtils.clientSpawnBasicItem(itemType);
        //position
        ClientEntityUtils.initiallyPositionEntity(
            newlySpawnedEntity,
            new Vector3d(message.getpositionX(),message.getpositionY(),message.getpositionZ()),
            new Quaterniond(message.getrotationX(),message.getrotationY(),message.getrotationZ(),message.getrotationW())
        );
        Globals.clientState.clientSceneWrapper.mapIdToId(newlySpawnedEntity.getId(), message.getentityID());
        return newlySpawnedEntity;
    }

    /**
     * Spawns a foliage
     * @param message The network message
     * @return The foliage
     */
    static Entity spawnFoliage(EntityMessage message){
        LoggerInterface.loggerNetworking.DEBUG("[CLIENT] Spawn foliage " + message.getentityID() + " at " + message.getpositionX() + " " + message.getpositionY() + " " + message.getpositionZ());
        String type = message.getentitySubtype();
        Entity newlySpawnedEntity = FoliageUtils.clientSpawnBasicFoliage(type);
        ClientEntityUtils.initiallyPositionEntity(
            newlySpawnedEntity,
            new Vector3d(message.getpositionX(),message.getpositionY(),message.getpositionZ()),
            new Quaterniond(message.getrotationX(),message.getrotationY(),message.getrotationZ(),message.getrotationW())
        );
        Globals.clientState.clientSceneWrapper.mapIdToId(newlySpawnedEntity.getId(), message.getentityID());
        return newlySpawnedEntity;
    }

    /**
     * Spawns a common entity
     * @param message The message
     * @return The entity
     */
    static Entity spawnCommon(EntityMessage message){
        LoggerInterface.loggerNetworking.DEBUG("[CLIENT] Spawn object " + message.getentityID() + " at " + message.getpositionX() + " " + message.getpositionY() + " " + message.getpositionZ());
        //spawn item
        String objectType = message.getentitySubtype();
        Entity newlySpawnedEntity = null;
        try {
            newlySpawnedEntity = CommonEntityUtils.clientSpawnBasicObject(objectType);
        } catch (Error e){
            throw new Error("Failed to spawn common entity: " + message.getentityID() + " \"" + objectType + "\"", e);
        }
        //position
        ClientEntityUtils.initiallyPositionEntity(
            newlySpawnedEntity,
            new Vector3d(message.getpositionX(),message.getpositionY(),message.getpositionZ()),
            new Quaterniond(message.getrotationX(),message.getrotationY(),message.getrotationZ(),message.getrotationW())
        );
        Globals.clientState.clientSceneWrapper.mapIdToId(newlySpawnedEntity.getId(), message.getentityID());
        return newlySpawnedEntity;
    }

}
