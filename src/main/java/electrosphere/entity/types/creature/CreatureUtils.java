package electrosphere.entity.types.creature;

import java.util.Map.Entry;

import org.joml.Quaterniond;
import org.joml.Vector3d;

import electrosphere.data.entity.creature.CreatureData;
import electrosphere.data.entity.creature.visualattribute.AttributeVariant;
import electrosphere.data.entity.creature.visualattribute.VisualAttribute;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.attack.ClientAttackTree;
import electrosphere.entity.state.attack.ServerAttackTree;
import electrosphere.entity.state.client.firstPerson.FirstPersonTree;
import electrosphere.entity.state.equip.ClientEquipState;
import electrosphere.entity.state.equip.ClientToolbarState;
import electrosphere.entity.state.equip.ServerEquipState;
import electrosphere.entity.state.equip.ServerToolbarState;
import electrosphere.entity.state.idle.ClientIdleTree;
import electrosphere.entity.state.inventory.ClientInventoryState;
import electrosphere.entity.state.inventory.ServerInventoryState;
import electrosphere.entity.state.movement.groundmove.ClientGroundMovementTree;
import electrosphere.entity.state.movement.groundmove.ServerGroundMovementTree;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.NetUtils;
import electrosphere.net.parser.net.message.EntityMessage;
import electrosphere.net.parser.net.message.NetworkMessage;
import electrosphere.net.server.player.Player;
import electrosphere.renderer.actor.Actor;
import electrosphere.renderer.actor.ActorStaticMorph;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.utils.EntityLookupUtils;
import electrosphere.server.entity.poseactor.PoseActor;
import electrosphere.server.entity.serialization.ContentSerialization;
import electrosphere.server.entity.serialization.EntitySerialization;
import electrosphere.util.Utilities;

/**
 * Utilities for creating creatures on the client and server
 */
public class CreatureUtils {
    
    /**
     * Spawns a client-side creature entity
     * @param type The type of creature
     * @param template The creature template if applicable
     * @return The creature entity
     */
    public static Entity clientSpawnBasicCreature(String type, ObjectTemplate template){
        CreatureData rawType = Globals.gameConfigCurrent.getCreatureTypeLoader().getType(type);
        Entity rVal = EntityCreationUtils.createClientSpatialEntity();

        //
        //perform common transforms
        //
        CommonEntityUtils.clientApplyCommonEntityTransforms(rVal, rawType);
        
        //
        //Perform creature-specific transforms
        //
        Actor creatureActor = EntityUtils.getActor(rVal);

        //variants
        ObjectTemplate storedTemplate = ObjectTemplate.create(EntityType.CREATURE, rawType.getId());
        if(rawType.getVisualAttributes() != null){
            ActorStaticMorph staticMorph = null;
            for(VisualAttribute attributeType : rawType.getVisualAttributes()){
                if(attributeType.getType().equals("remesh")){
                    if(attributeType.getVariants() != null && attributeType.getVariants().size() > 0){
                        AttributeVariant variant = attributeType.getVariants().get(0);
                        //if the template isn't null, try to find the variant from the template in the variant list
                        //if the variant is found, set the variable "variant" to the searched for variant
                        if(template != null && template.getAttributeValue(attributeType.getAttributeId()) != null){
                            String variantId = template.getAttributeValue(attributeType.getAttributeId()).getVariantId();
                            for(AttributeVariant searchVariant : attributeType.getVariants()){
                                if(searchVariant.getId().equals(variantId)){
                                    variant = searchVariant;
                                    //if we find the variant, store in on-creature template as well
                                    storedTemplate.putAttributeValue(attributeType.getAttributeId(), variantId);
                                    break;
                                }
                            }
                        }
                        //make sure stored template contains creature data
                        if(storedTemplate.getAttributeValue(attributeType.getAttributeId())==null){
                            storedTemplate.putAttributeValue(attributeType.getAttributeId(), attributeType.getVariants().get(0).getId());
                        }
                        // attributeType.getAttributeId();
                        // variant.getId();
                        rVal.putData(EntityDataStrings.CREATURE_ATTRIBUTE_VARIANT + attributeType.getAttributeId(), variant.getId());
                        Globals.assetManager.addModelPathToQueue(variant.getModel());
                        for(String mesh : variant.getMeshes()){
                            creatureActor.getMeshMask().queueMesh(variant.getModel(), mesh);
                        }
                    }
                }
                if(attributeType.getType().equals("bone")){
                    if(staticMorph == null){
                        staticMorph = new ActorStaticMorph();
                        creatureActor.getAnimationData().setActorStaticMorph(staticMorph);
                    }
                    if(attributeType.getPrimaryBone() != null && staticMorph.getBoneTransforms(attributeType.getPrimaryBone()) == null){
                        staticMorph.initBoneTransforms(attributeType.getPrimaryBone());
                        //if the template isn't null, set the value of the morph
                        if(template != null && template.getAttributeValue(attributeType.getAttributeId()) != null){
                            float templateValue = template.getAttributeValue(attributeType.getAttributeId()).getValue();
                            staticMorph.updateValue(attributeType.getSubtype(), attributeType.getPrimaryBone(), templateValue);
                        }
                    }
                    if(attributeType.getMirrorBone() != null && staticMorph.getBoneTransforms(attributeType.getMirrorBone()) == null){
                        staticMorph.initBoneTransforms(attributeType.getMirrorBone());
                        //if the template isn't null, set the value of the morph
                        if(template != null && template.getAttributeValue(attributeType.getAttributeId()) != null){
                            float templateValue = template.getAttributeValue(attributeType.getAttributeId()).getValue();
                            staticMorph.updateValue(attributeType.getSubtype(), attributeType.getMirrorBone(), templateValue);
                        }
                    }
                    //make sure stored template contains creature data
                    if(template != null && template.getAttributeValue(attributeType.getAttributeId()) != null) {
                        storedTemplate.putAttributeValue(attributeType.getAttributeId(), template.getAttributeValue(attributeType.getAttributeId()).getValue());
                    } else {
                        float midpoint = (attributeType.getMaxValue() - attributeType.getMinValue())/2.0f + attributeType.getMinValue();
                        storedTemplate.putAttributeValue(attributeType.getAttributeId(), midpoint);
                    }
                }
            }
        }
        //set race
        storedTemplate.objectType = rawType.getId();
        //store template on creature
        CreatureUtils.setCreatureTemplate(rVal, storedTemplate);

        //register to correct tag
        Globals.clientState.clientScene.registerEntityToTag(rVal, EntityTags.CREATURE);
        return rVal;
    }

    /**
     * Applies a creature template's item transforms to the creature (ie attaches items that should be attached)
     * @param realm The realm
     * @param creature The creature
     * @param template The template
     */
    public static void clientApplyTemplate(Entity creature, ObjectTemplate template){
        //
        //must happen after the player is attached to the entity, or server won't send packet to add item to player's entity
        //now that creature has been spawned, need to create all attached items
        if(template != null){
            if(template.getInventoryData() != null){
                ObjectInventoryData inventoryData = template.getInventoryData();
                for(Entry<String,EntitySerialization> toolbarItem : inventoryData.getToolbarItems()){
                    EntitySerialization serialization = toolbarItem.getValue();
                    String toolbarSlot = toolbarItem.getKey();
                    //add the item to the creature's inventory
                    Entity itemInWorld = ContentSerialization.clientHydrateEntitySerialization(serialization);
                    Entity itemInInventory = ItemUtils.clientRecreateContainerItem(itemInWorld, creature);

                    //equip the item to the slot defined in the template
                    ClientToolbarState clientToolbarState = ClientToolbarState.getClientToolbarState(creature);
                    clientToolbarState.attemptAddToToolbar(itemInInventory, Integer.parseInt(toolbarSlot));

                    //map the constructed item to its server id
                    Globals.clientState.clientSceneWrapper.mapIdToId(itemInInventory.getId(), inventoryData.getToolbarId(toolbarSlot));
                }
                for(Entry<String,EntitySerialization> equippedItem : inventoryData.getEquipItems()){
                    EntitySerialization serialization = equippedItem.getValue();
                    String equipSlot = equippedItem.getKey();

                    //add the item to the creature's inventory
                    Entity itemInInventory = ClientInventoryState.clientConstructInInventoryItem(creature,serialization.getSubtype());

                    //equip the item to the slot defined in the template
                    ClientEquipState clientEquipState = ClientEquipState.getEquipState(creature);
                    clientEquipState.attemptEquip(itemInInventory, clientEquipState.getEquipPoint(equipSlot));

                    //map the constructed item to its server id
                    Globals.clientState.clientSceneWrapper.mapIdToId(itemInInventory.getId(), inventoryData.getEquippedId(equipSlot));
                }
                int i = 0;
                for(EntitySerialization naturalItem : inventoryData.getNaturalItems()){
                    //add the item to the creature's inventory
                    Entity itemInInventory = ClientInventoryState.clientConstructInInventoryItem(creature,naturalItem.getSubtype());

                    //map the constructed item to its server id
                    Globals.clientState.clientSceneWrapper.mapIdToId(itemInInventory.getId(), inventoryData.getNaturalId(i));
                    i++;
                }
            }
        }
    }


    /**
     * Spawns a server-side creature
     * @param realm The realm to spawn the creature in
     * @param position The position of the creature in that realm
     * @param type The type of creature
     * @param template The creature template to use
     * @return The creature entity
     */
    public static Entity serverSpawnBasicCreature(Realm realm, Vector3d position, String type, ObjectTemplate template){

        double posX = position.x;
        double posY = position.y;
        double posZ = position.z;

        CreatureData rawType = Globals.gameConfigCurrent.getCreatureTypeLoader().getType(type);
        Entity rVal = EntityCreationUtils.createServerEntity(realm, position);

        //
        //
        //Common transforms
        //
        //
        CommonEntityUtils.serverApplyCommonEntityTransforms(realm, position, rVal, rawType);

        //
        //
        //Creature specific transforms
        //
        //
        PoseActor creatureActor = EntityUtils.getPoseActor(rVal);

        
        //variants
        ObjectTemplate storedTemplate = ObjectTemplate.create(EntityType.CREATURE, rawType.getId());
        if(rawType.getVisualAttributes() != null){
            ActorStaticMorph staticMorph = null;
            for(VisualAttribute attributeType : rawType.getVisualAttributes()){
                if(attributeType.getType().equals("remesh")){
                    if(attributeType.getVariants() != null && attributeType.getVariants().size() > 0){
                        //if the template isn't null, try to find the variant from the template in the variant list
                        //if the variant is found, set the variable "variant" to the searched for variant
                        if(template != null && template.getAttributeValue(attributeType.getAttributeId()) != null){
                            String variantId = template.getAttributeValue(attributeType.getAttributeId()).getVariantId();
                            for(AttributeVariant searchVariant : attributeType.getVariants()){
                                if(searchVariant.getId().equals(variantId)){
                                    //if we find the variant, store in on-creature template as well
                                    storedTemplate.putAttributeValue(attributeType.getAttributeId(), variantId);
                                    break;
                                }
                            }
                        }
                        //make sure stored template contains creature data
                        if(storedTemplate.getAttributeValue(attributeType.getAttributeId())==null){
                            storedTemplate.putAttributeValue(attributeType.getAttributeId(), attributeType.getVariants().get(0).getId());
                        }
                    }
                }
                if(attributeType.getType().equals("bone")){
                    if(staticMorph == null){
                        staticMorph = new ActorStaticMorph();
                        creatureActor.setStaticMorph(staticMorph);
                    }
                    if(attributeType.getPrimaryBone() != null && staticMorph.getBoneTransforms(attributeType.getPrimaryBone()) == null){
                        staticMorph.initBoneTransforms(attributeType.getPrimaryBone());
                        //if the template isn't null, set the value of the morph
                        if(template != null && template.getAttributeValue(attributeType.getAttributeId()) != null){
                            float templateValue = template.getAttributeValue(attributeType.getAttributeId()).getValue();
                            staticMorph.updateValue(attributeType.getSubtype(), attributeType.getPrimaryBone(), templateValue);
                        }
                    }
                    if(attributeType.getMirrorBone() != null && staticMorph.getBoneTransforms(attributeType.getMirrorBone()) == null){
                        staticMorph.initBoneTransforms(attributeType.getMirrorBone());
                        //if the template isn't null, set the value of the morph
                        if(template != null && template.getAttributeValue(attributeType.getAttributeId()) != null){
                            float templateValue = template.getAttributeValue(attributeType.getAttributeId()).getValue();
                            staticMorph.updateValue(attributeType.getSubtype(), attributeType.getMirrorBone(), templateValue);
                        }
                    }
                    //make sure stored template contains creature data
                    if(template != null && template.getAttributeValue(attributeType.getAttributeId()) != null) {
                        storedTemplate.putAttributeValue(attributeType.getAttributeId(), template.getAttributeValue(attributeType.getAttributeId()).getValue());
                    } else {
                        float midpoint = (attributeType.getMaxValue() - attributeType.getMinValue())/2.0f + attributeType.getMinValue();
                        storedTemplate.putAttributeValue(attributeType.getAttributeId(), midpoint);
                    }
                }
            }
        }
        //set race
        storedTemplate.objectType = rawType.getId();
        //store template on creature
        CreatureUtils.setCreatureTemplate(rVal, storedTemplate);

        //error check position modification
        if(posX != position.x || posY != position.y || posZ != position.z){
            throw new Error("Creature has mutated position! " + posX + "," + posY + "," + posZ + "     " + position.x + "," + position.y + "," + position.z);
        }
        

        //position entity
        //this needs to be called at the end of this function.
        //Burried underneath this is function call to initialize a server side entity.
        //The server initialization logic checks what type of entity this is, if this function is called prior to its type being stored
        //the server will not be able to synchronize it properly.
        ServerEntityUtils.initiallyPositionEntity(realm,rVal,position);

        //error checking
        if(Globals.serverState.realmManager.getEntityRealm(rVal) == null){
            LoggerInterface.loggerEngine.ERROR(new IllegalStateException("Created creature without it being assigned to a realm!"));
        }
        //make sure position hasn't changed
        if(posX != position.x || posY != position.y || posZ != position.z){
            throw new Error("Creature has mutated position! " + posX + "," + posY + "," + posZ + "     " + position.x + "," + position.y + "," + position.z);
        }

        return rVal;
    }

    /**
     * Applies a creature template's item transforms to the creature (ie attaches items that should be attached)
     * @param realm The realm
     * @param creature The creature
     * @param template The template
     */
    public static void serverApplyTemplate(Realm realm, Entity creature, ObjectTemplate template){
        //
        //must happen after the player is attached to the entity, or server won't send packet to add item to player's entity
        //now that creature has been spawned, need to create all attached items
        if(template != null){
            if(template.getInventoryData() != null){
                ObjectInventoryData inventoryData = template.getInventoryData();
                for(Entry<String,EntitySerialization> toolbarItem : inventoryData.getToolbarItems()){
                    EntitySerialization serialization = toolbarItem.getValue();
                    String toolbarSlot = toolbarItem.getKey();
                    //add the item to the creature's inventory
                    Entity itemInWorld = ContentSerialization.serverHydrateEntitySerialization(realm, serialization);

                    //add the item to the creature's inventory
                    Entity itemInInventory = ServerInventoryState.attemptStoreItemAnyInventory(creature, EntityLookupUtils.getEntityById(itemInWorld.getId()));

                    //equip the item to the slot defined in the template
                    ServerToolbarState serverToolbarState = ServerToolbarState.getServerToolbarState(creature);
                    serverToolbarState.attemptEquip(itemInInventory, Integer.parseInt(toolbarSlot));
                }
                for(Entry<String,EntitySerialization> equippedItem : inventoryData.getEquipItems()){
                    EntitySerialization serialization = equippedItem.getValue();
                    String equipSlot = equippedItem.getKey();

                    //add the item to the creature's inventory
                    Entity itemInWorld = ItemUtils.serverSpawnBasicItem(realm, EntityUtils.getPosition(creature), serialization.getSubtype());

                    //add the item to the creature's inventory
                    Entity itemInInventory = ServerInventoryState.attemptStoreItemAnyInventory(creature, EntityLookupUtils.getEntityById(itemInWorld.getId()));

                    //equip the item to the slot defined in the template
                    ServerEquipState serverEquipState = ServerEquipState.getEquipState(creature);
                    serverEquipState.commandAttemptEquip(itemInInventory,serverEquipState.getEquipPoint(equipSlot));
                }
                for(EntitySerialization naturalItem : inventoryData.getNaturalItems()){
                    //add the item to the creature's inventory
                    Entity itemInWorld = ItemUtils.serverSpawnBasicItem(realm, EntityUtils.getPosition(creature), naturalItem.getSubtype());

                    //add the item to the creature's inventory
                    ServerInventoryState.attemptStoreItemAnyInventory(creature, EntityLookupUtils.getEntityById(itemInWorld.getId()));
                }
            }
        }
    }

    /**
     * Creates a viewmodel entity on the client side
     * @param type The type of creature
     * @param template The template for the creature
     * @return The entity
     */
    public static Entity clientCreateViewModel(
        String type,
        ObjectTemplate template
    ){
        CreatureData rawType = Globals.gameConfigCurrent.getCreatureTypeLoader().getType(type);
        Entity rVal = EntityCreationUtils.createClientSpatialEntity();

        if(rawType.getViewModelData() != null){
            EntityCreationUtils.makeEntityDrawable(rVal, rawType.getViewModelData().getFirstPersonModelPath());
            FirstPersonTree.attachTree(
                rVal,
                rawType.getViewModelData().getHeightFromOrigin(),
                rawType.getViewModelData().getCameraViewDirOffsetY(),
                rawType.getViewModelData().getCameraViewDirOffsetZ()
            );
        }
        Actor creatureActor = EntityUtils.getActor(rVal);

        if(rawType.getBoneGroups() != null){
            creatureActor.getAnimationData().setBoneGroups(rawType.getBoneGroups());
        }

        return rVal;
    }
    
    /**
     * Gets the creature to the player
     * @param player The player to send the creature to
     * @param creature The creature entity
     */
    public static void sendEntityToPlayer(Player player, Entity creature){
        int id = creature.getId();
        Vector3d position = EntityUtils.getPosition(creature);
        Quaterniond rotation = EntityUtils.getRotation(creature);
        String template = Utilities.stringify(CommonEntityUtils.getObjectTemplate(creature));
        NetworkMessage message = EntityMessage.constructCreateMessage(
            id,
            EntityType.CREATURE.getValue(),
            CreatureUtils.getType(creature),
            template,
            position.x,
            position.y,
            position.z,
            rotation.x,
            rotation.y,
            rotation.z,
            rotation.w
        );
        player.addMessage(message);
        if(CreatureUtils.hasControllerPlayerId(creature)){
            LoggerInterface.loggerNetworking.INFO("Sending controller packets");
            player.addMessage(NetUtils.createSetCreatureControllerIdEntityMessage(creature));
            Player entityOwner = Globals.serverState.playerManager.getPlayerFromId(CreatureUtils.getControllerPlayerId(creature));
            if(player.hasSentPlayerEntity()){
                throw new Error("Re-sending player entity to player!");
            }
            if(entityOwner == player){
                player.setHasSentPlayerEntity(true);
            }
        }
    }
    
    public static void setFacingVector(Entity e, Vector3d vector){
        e.putData(EntityDataStrings.DATA_STRING_FACING_VECTOR, vector);
    }
    
    public static Vector3d getFacingVector(Entity e){
        return (Vector3d)e.getData(EntityDataStrings.DATA_STRING_FACING_VECTOR);
    }
    
    public static float getAcceleration(Entity e){
        return (float)e.getData(EntityDataStrings.DATA_STRING_ACCELERATION);
    }
    
    public static void setAcceleration(Entity e, float scalar){
        e.putData(EntityDataStrings.DATA_STRING_ACCELERATION, scalar);
    }

    public static boolean hasVelocity(Entity e){
    return e.containsKey(EntityDataStrings.DATA_STRING_VELOCITY);
    }
    
    public static float getVelocity(Entity e){
        if(!e.containsKey(EntityDataStrings.DATA_STRING_VELOCITY)){
            return 0;
        }
        return (float)e.getData(EntityDataStrings.DATA_STRING_VELOCITY);
    }
    
    public static void setVelocity(Entity e, float scalar){
        e.putData(EntityDataStrings.DATA_STRING_VELOCITY, scalar);
    }
    
    public static float getMaxNaturalVelocity(Entity e){
        return (float)e.getData(EntityDataStrings.DATA_STRING_MAX_NATURAL_VELOCITY);
    }
    
    public static void setMaxNaturalVelocity(Entity e, float scalar){
        e.putData(EntityDataStrings.DATA_STRING_MAX_NATURAL_VELOCITY, scalar);
    }
    
    public static BehaviorTree clientGetEntityMovementTree(Entity e){
        return (BehaviorTree)e.getData(EntityDataStrings.CLIENT_MOVEMENT_BT);
    }

    public static BehaviorTree serverGetEntityMovementTree(Entity e){
        return (BehaviorTree)e.getData(EntityDataStrings.SERVER_MOVEMENT_BT);
    }
    
    public static void clientAttachEntityMessageToMovementTree(Entity e, EntityMessage em){
        BehaviorTree movementTree = clientGetEntityMovementTree(e);
        if(movementTree instanceof ClientGroundMovementTree){
            ((ClientGroundMovementTree)movementTree).addNetworkMessage(em);
        }
    }

    public static void serverAttachEntityMessageToMovementTree(Entity e, EntityMessage em){
        BehaviorTree movementTree = serverGetEntityMovementTree(e);
        if(movementTree instanceof ServerGroundMovementTree){
            ((ServerGroundMovementTree)movementTree).addNetworkMessage(em);
        }
    }
    
    /**
     * Gets the type of creature
     * @param e the entity
     * @return the type
     */
    public static String getType(Entity e){
        return (String)CommonEntityUtils.getEntitySubtype(e);
    }
    
    /**
     * Gets the associated player ID that controls this entity
     * @param e The entity
     * @return The player ID
     */
    public static int getControllerPlayerId(Entity e){
        return (int)e.getData(EntityDataStrings.DATA_STRING_CREATURE_CONTROLLER_PLAYER_ID);
    }
    
    /**
     * Sets the associated player ID that controls this creature
     * @param e The entity
     * @param id The id
     */
    public static void setControllerPlayerId(Entity e, int id){
        e.putData(EntityDataStrings.DATA_STRING_CREATURE_CONTROLLER_PLAYER_ID, id);
    }
    
    /**
     * Checks if this entity has a player that controls it
     * @param e The entity
     * @return true if a player controls it, false otherwise
     */
    public static boolean hasControllerPlayerId(Entity e){
        return e.containsKey(EntityDataStrings.DATA_STRING_CREATURE_CONTROLLER_PLAYER_ID);
    }
    
    /**
     * Checks if this entity is a creature
     * @param e The entity
     * @return true if it is a creature, false otherwise
     */
    public static boolean isCreature(Entity e){
        return CommonEntityUtils.getEntityType(e) == EntityType.CREATURE;
    }
    
    public static ClientAttackTree clientGetAttackTree(Entity e){
        return (ClientAttackTree)e.getData(EntityDataStrings.TREE_CLIENTATTACKTREE);
    }

    public static ServerAttackTree serverGetAttackTree(Entity e){
        return (ServerAttackTree)e.getData(EntityDataStrings.TREE_SERVERATTACKTREE);
    }
    
    public static ClientIdleTree getIdleTree(Entity e){
        return (ClientIdleTree)e.getData(EntityDataStrings.TREE_IDLE);
    }

    public static void setCreatureTemplate(Entity e, ObjectTemplate template){
        e.putData(EntityDataStrings.OBJECT_TEMPLATE, template);
    }
    
    
}
