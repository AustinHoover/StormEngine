package electrosphere.entity.types.item;

import java.util.List;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.ode4j.ode.DBody;

import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.collision.collidable.Collidable;
import electrosphere.data.entity.item.EquipData;
import electrosphere.data.entity.item.EquipWhitelist;
import electrosphere.data.entity.item.Item;
import electrosphere.data.entity.item.ItemAudio;
import electrosphere.data.entity.item.WeaponData;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.entity.ClientEntityUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.entity.state.AnimationPriorities;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.state.gravity.ServerGravityTree;
import electrosphere.entity.state.hitbox.HitboxCollectionState;
import electrosphere.entity.state.inventory.InventoryUtils;
import electrosphere.entity.state.inventory.RelationalInventoryState;
import electrosphere.entity.state.inventory.UnrelationalInventoryState;
import electrosphere.entity.state.item.ClientChargeState;
import electrosphere.entity.state.item.ServerChargeState;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.EntityMessage;
import electrosphere.net.parser.net.message.InventoryMessage;
import electrosphere.net.parser.net.message.NetworkMessage;
import electrosphere.net.server.player.Player;
import electrosphere.renderer.actor.Actor;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.utils.ServerEntityTagUtils;
import electrosphere.server.entity.poseactor.PoseActor;

/**
 * Utilities for working with items
 */
public class ItemUtils {

    /**
     * Spawns an item on the client
     * @param name The name of the item type
     * @return The item entity
     */
    public static Entity clientSpawnBasicItem(String name){
        Item item = Globals.gameConfigCurrent.getItemMap().getItem(name);
        Entity rVal = EntityCreationUtils.createClientSpatialEntity();

        //
        //
        //Common entity transforms
        //
        //
        CommonEntityUtils.clientApplyCommonEntityTransforms(rVal, item);

        //
        //
        //Item specific transforms
        //
        //
        if(item.getWeaponData() != null){
            rVal.putData(EntityDataStrings.ITEM_IS_WEAPON, true);
            WeaponData weaponData = item.getWeaponData();
            if(weaponData.getHitboxes() != null){
                HitboxCollectionState.attachHitboxState(Globals.clientState.clientSceneWrapper.getHitboxManager(), false, rVal, weaponData.getHitboxes());
            }
            rVal.putData(EntityDataStrings.ITEM_WEAPON_CLASS,weaponData.getWeaponClass());
            rVal.putData(EntityDataStrings.ITEM_WEAPON_DATA_RAW,weaponData);
        }
        if(item.getIdleAnim() != null){
            rVal.putData(EntityDataStrings.ANIM_IDLE,item.getIdleAnim());
        }
        if(item.getIconPath() != null && !item.getIconPath().equals("")){
            rVal.putData(EntityDataStrings.ITEM_ICON,item.getIconPath());
        } else {
            rVal.putData(EntityDataStrings.ITEM_ICON,AssetDataStrings.UI_TEXTURE_ITEM_ICON_GENERIC);
        }
        if(item.getItemAudio() != null){
            ItemAudio audio = item.getItemAudio();
            if(audio.getUIGrabAudio() != null && audio.getUIGrabAudio() != ""){
                Globals.assetManager.addAudioPathToQueue(audio.getUIGrabAudio());
            }
            if(audio.getUIReleaseAudio() != null && audio.getUIReleaseAudio() != ""){
                Globals.assetManager.addAudioPathToQueue(audio.getUIReleaseAudio());
            }
        }

        //
        //
        // Equip data
        //
        //
        if(item.getEquipData() != null){
            EquipData equipData = item.getEquipData();
            if(equipData.getEquipWhitelist() != null){
                rVal.putData(EntityDataStrings.ITEM_EQUIP_WHITELIST, equipData.getEquipWhitelist());
            }
            if(equipData.getEquipClass() != null){
                rVal.putData(EntityDataStrings.ITEM_EQUIP_CLASS,equipData.getEquipClass());
            }
        }
        rVal.putData(EntityDataStrings.ITEM_IS_IN_INVENTORY, false);


        //
        // Fab data
        //
        if(item.getFabData() != null){
            rVal.putData(EntityDataStrings.ITEM_FAB_DATA, item.getFabData());
        }

        //
        //stacking behavior
        //
        if(item.getMaxStack() != null){
            ClientChargeState.attachTree(rVal, item.getMaxStack());
        }
        
        Globals.clientState.clientSceneWrapper.getScene().registerEntityToTag(rVal, EntityTags.ITEM);
        return rVal;
    }


    /**
     * Applies transforms to an entity to make it an item entity
     * @param realm The realm of the entity
     * @param position The position of the entity
     * @param rVal The entity itself
     * @param item The item's type
     */
    public static void serverApplyItemEntityTransforms(Realm realm, Vector3d position, Entity rVal, Item item){

        //error check inputs
        if(realm == null || position == null || rVal == null || item == null){
            throw new Error("Provided bad data to item transform! " + realm + " " + position + " " + rVal + " " + item);
        }

        //
        //
        //Common entity transforms
        //
        //
        CommonEntityUtils.serverApplyCommonEntityTransforms(realm, position, rVal, item);

        //
        //
        //Item specific transforms
        //
        //
        if(item.getWeaponData() != null){
            WeaponData weaponData = item.getWeaponData();
            if(weaponData.getHitboxes() != null){
                HitboxCollectionState.attachHitboxState(realm.getHitboxManager(), true, rVal, weaponData.getHitboxes());
            }
        }
        //tokens
        if(item.getTokens() != null){
            for(String token : item.getTokens()){
                switch(token){
                    case "GRAVITY":
                        Collidable collidable = (Collidable)rVal.getData(EntityDataStrings.PHYSICS_COLLIDABLE);
                        DBody collisionObject = PhysicsEntityUtils.getDBody(rVal);
                        ServerGravityTree.attachTree(rVal, collidable, collisionObject, 30);
                        rVal.putData(EntityDataStrings.GRAVITY_ENTITY, true);
                        break;
                    case "TARGETABLE":
                        ServerEntityTagUtils.attachTagToEntity(rVal, EntityTags.TARGETABLE);
                        break;
                    case "OUTLINE":
                    rVal.putData(EntityDataStrings.DRAW_OUTLINE, true);
                    break;
                }
            }
        }
        if(item.getIdleAnim() != null){
            rVal.putData(EntityDataStrings.ANIM_IDLE,item.getIdleAnim());
        }


        //make all non-positional, non-physics data transform here
        ItemUtils.serverApplyItemDataTransforms(item,rVal);

    }

    /**
     * [SERVER ONLY] Applies transforms that we would expect to be applied to an entity that is an item inside an inventory
     * @param item The item data
     * @param itemEnt The item entity
     */
    private static void serverApplyItemDataTransforms(Item item, Entity itemEnt){
        if(item == null){
            throw new Error("Item data is null");
        }
        if(itemEnt == null){
            throw new Error("Item entity is null");
        }
        
        //
        //
        //Item specific transforms
        //
        //
        if(item.getWeaponData() != null){
            WeaponData weaponData = item.getWeaponData();
            itemEnt.putData(EntityDataStrings.ITEM_IS_WEAPON, true);
            itemEnt.putData(EntityDataStrings.ITEM_WEAPON_CLASS,weaponData.getWeaponClass());
            itemEnt.putData(EntityDataStrings.ITEM_WEAPON_DATA_RAW,weaponData);
        }
        //tokens
        if(item.getTokens() != null){
            for(String token : item.getTokens()){
                switch(token){
                }
            }
        }
        if(item.getIconPath() != null && !item.getIconPath().equals("")){
            itemEnt.putData(EntityDataStrings.ITEM_ICON,item.getIconPath());
        } else {
            itemEnt.putData(EntityDataStrings.ITEM_ICON,AssetDataStrings.UI_TEXTURE_ITEM_ICON_GENERIC);
        }
        //
        //
        // Equip data
        //
        //
        if(item.getEquipData() != null){
            EquipData equipData = item.getEquipData();
            if(equipData.getEquipWhitelist() != null){
                itemEnt.putData(EntityDataStrings.ITEM_EQUIP_WHITELIST, equipData.getEquipWhitelist());
            }
            if(equipData.getEquipClass() != null){
                itemEnt.putData(EntityDataStrings.ITEM_EQUIP_CLASS,equipData.getEquipClass());
            }
        }
        itemEnt.putData(EntityDataStrings.ITEM_IS_IN_INVENTORY, false);

        //
        // Fab data
        //
        if(item.getFabData() != null){
            itemEnt.putData(EntityDataStrings.ITEM_FAB_DATA, item.getFabData());
        }

        //
        //stacking behavior
        //
        if(item.getMaxStack() != null){
            ServerChargeState.attachTree(itemEnt, item.getMaxStack());
        }
    }

    /**
     * Spawns an item on the server
     * @param realm the realm to spawn in
     * @param position the position to spawn at
     * @param name the name of the item to spawn
     * @return The item entity
     */
    public static Entity serverSpawnBasicItem(Realm realm, Vector3d position, String name){
        Item item = Globals.gameConfigCurrent.getItemMap().getItem(name);

        if(item == null){
            throw new Error("Failed to resolve item with name " + name);
        }

        //must correct the position such that it spawns inside the realm
        Vector3d correctedPosition = ServerEntityUtils.guaranteePositionIsInBounds(realm, position);
        Entity rVal = EntityCreationUtils.createServerEntity(realm, correctedPosition);
        
        //apply item transforms to an entity
        ItemUtils.serverApplyItemEntityTransforms(realm, correctedPosition, rVal, item);


        //position entity
        //this needs to be called at the end of this function.
        //Burried underneath this is function call to initialize a server side entity.
        //The server initialization logic checks what type of entity this is, if this function is called prior to its type being stored
        //the server will not be able to synchronize it properly.
        ServerEntityUtils.initiallyPositionEntity(realm,rVal,correctedPosition);

        //error checking
        if(Globals.serverState.realmManager.getEntityRealm(rVal) == null){
            LoggerInterface.loggerEngine.ERROR(new IllegalStateException("Created item without it being assigned to a realm!"));
        }

        return rVal;
    }
    
    public static void removePhysics(Entity item){
        /*
        rVal.putData(EntityDataStrings.PHYSICS_COLLISION_BODY, rigidBody);
                    rVal.putData(EntityDataStrings.PHYSICS_COLLISION_BODY_OFFSET, new Vector3f(physicsTemplate.getOffsetX(),physicsTemplate.getOffsetY(),physicsTemplate.getOffsetZ()));
                    rVal.putData(EntityDataStrings.PHYSICS_MODEL_TEMPLATE, physicsTemplate);
                    rVal.putData(EntityDataStrings.PHYSICS_COLLIDABLE,collidable);
                    rVal.putData(EntityDataStrings.COLLIDABLE_TREE, new CollidableTree(rVal,collidable,rigidBody));
        */
        if(item.containsKey(EntityDataStrings.PHYSICS_COLLISION_BODY)){
            item.removeData(EntityDataStrings.PHYSICS_COLLISION_BODY);
        }
        if(item.containsKey(EntityDataStrings.PHYSICS_MODEL_TEMPLATE)){
            item.removeData(EntityDataStrings.PHYSICS_MODEL_TEMPLATE);
        }
        if(item.containsKey(EntityDataStrings.PHYSICS_COLLIDABLE)){
            item.removeData(EntityDataStrings.PHYSICS_COLLIDABLE);
        }
        if(item.containsKey(EntityDataStrings.SERVER_COLLIDABLE_TREE)){
            item.removeData(EntityDataStrings.SERVER_COLLIDABLE_TREE);
        }
        if(item.containsKey(EntityDataStrings.CLIENT_COLLIDABLE_TREE)){
            item.removeData(EntityDataStrings.CLIENT_COLLIDABLE_TREE);
        }
    }
    
    /**
     * Updates an item to play its idle animation
     * @param item THe item entity
     */
    public static void updateItemActorAnimation(Entity item){
        Actor actor = EntityUtils.getActor(item);
        if(actor != null && item.getData(EntityDataStrings.ANIM_IDLE) != null){
            String idleAnim = (String)item.getData(EntityDataStrings.ANIM_IDLE);
            if(!actor.getAnimationData().isPlayingAnimation(idleAnim)){
                actor.getAnimationData().playAnimation(idleAnim,AnimationPriorities.getValue(AnimationPriorities.INTERACTION));
                actor.getAnimationData().incrementAnimationTime(0.0001);
            }
        }
    }

    /**
     * Updates the item's pose actor to play its idle animation on the server
     * @param item The item entity
     */
    public static void updateItemPoseActorAnimation(Entity item){
        PoseActor actor = EntityUtils.getPoseActor(item);
        if(actor != null && item.getData(EntityDataStrings.ANIM_IDLE) != null){
            String idleAnim = (String)item.getData(EntityDataStrings.ANIM_IDLE);
            if(!actor.isPlayingAnimation(idleAnim)){
                actor.playAnimation(idleAnim,AnimationPriorities.getValue(AnimationPriorities.INTERACTION));
                actor.incrementAnimationTime(Globals.engineState.timekeeper.getSimFrameTime());
            }
        }
    }
    
    public static void sendEntityToPlayer(Player player, Entity item){
        boolean shouldSend = ItemUtils.itemShouldBeSentToClient(item);
        if(shouldSend){
            int id = item.getId();
            String type = ItemUtils.getType(item);
            Vector3d position = EntityUtils.getPosition(item);
            Quaterniond rotation = EntityUtils.getRotation(item);
            //construct the spawn message and attach to player
            NetworkMessage message = EntityMessage.constructCreateMessage(
                id,
                EntityType.ITEM.getValue(),
                type,
                "",
                position.x,
                position.y,
                position.z,
                rotation.x,
                rotation.y,
                rotation.z,
                rotation.w
            );
            player.addMessage(message);
        }
    }
    

    /**
     * Checks if this entity is an item
     * @param e the entity
     * @return true if it is an item, false otherwise
     */
    public static boolean isItem(Entity e){
        if(e == null){
            return false;
        }
        return CommonEntityUtils.getEntityType(e) == EntityType.ITEM;
    }

    /**
     * Checks if an item should be sent to a client on synchronization
     * @param item The item
     * @return true if should be sent, false otherwise
     */
    public static boolean itemShouldBeSentToClient(Entity item){
        //if the item is attached to a creature, don't send a dedicated item
        //instead, the creature template should include the item
        if(AttachUtils.hasParent(item)){
            return false;
        }
        return true;
    }

    /**
     * Checks if an item should be serialized to disk when saving a chunk
     * @param item The item
     * @return true if should be sent, false otherwise
     */
    public static boolean itemShouldBeSerialized(Entity item){
        //if the item is attached to a creature, don't save a dedicated item
        //instead, the creature template should include the item
        if(AttachUtils.hasParent(item)){
            return false;
        }
        return true;
    }
    
    /**
     * Gets the type of item
     * @param item The entity
     * @return The type
     */
    public static String getType(Entity item){
        if(item == null){
            return null;
        }
        return CommonEntityUtils.getEntitySubtype(item);
    }
    
    public static boolean isWeapon(Entity item){
        return item.containsKey(EntityDataStrings.ITEM_IS_WEAPON);
    }

    public static boolean isArmor(Entity item){
        return item.containsKey(EntityDataStrings.ITEM_IS_ARMOR);
    }

    /**
     * Checks if the entity has an equip list
     * @param item The item entity
     * @return true if it has an equip list, false otherwise
     */
    public static boolean hasEquipList(Entity item){
        if(item == null){
            return false;
        }
        return item.containsKey(EntityDataStrings.ITEM_EQUIP_WHITELIST);
    }

    @SuppressWarnings("unchecked")
    public static List<EquipWhitelist> getEquipWhitelist(Entity item){
        return (List<EquipWhitelist>)item.getData(EntityDataStrings.ITEM_EQUIP_WHITELIST);
    }

    /**
     * Checks if the item is inside an inventory container
     * @param item The item entity to check
     * @return true if the item IS in an inventory container, otherwise false
     */
    public static boolean itemIsInInventory(Entity item){
        if(!item.containsKey(EntityDataStrings.ITEM_IS_IN_INVENTORY)){
            return false;
        }
        return (boolean)item.getData(EntityDataStrings.ITEM_IS_IN_INVENTORY);
    }

    /**
     * Sets the parent that contains this item
     * @param item The item entity
     * @param parent The parent which contains the item entity
     */
    public static void setContainingParent(Entity item, Entity parent){
        item.putData(EntityDataStrings.ITEM_CONTAINING_PARENT, parent);
    }

    /**
     * Gets the parent that contains the item
     * @param item The item
     * @return The parent that contains the item
     */
    public static Entity getContainingParent(Entity item){
        return (Entity)item.getData(EntityDataStrings.ITEM_CONTAINING_PARENT);
    }

    /**
     * Emits an entity which represents the item inside a container
     * @param item The item to recreate
     * @param containingParent The parent that contains the item
     */
    public static Entity clientRecreateContainerItem(Entity item, Entity containingParent){
        if(ItemUtils.isItem(item)){
            Item itemData = Globals.gameConfigCurrent.getItemMap().getItem(ItemUtils.getType(item));
            Entity rVal = EntityCreationUtils.createClientNonSpatialEntity();
            if(ItemUtils.getEquipWhitelist(item) != null){
                rVal.putData(EntityDataStrings.ITEM_EQUIP_WHITELIST, getEquipWhitelist(item));
            }

            //
            //weapon data
            //
            if(itemData.getWeaponData() != null){
                rVal.putData(EntityDataStrings.ITEM_IS_WEAPON, true);
                WeaponData weaponData = itemData.getWeaponData();
                rVal.putData(EntityDataStrings.ITEM_WEAPON_CLASS,weaponData.getWeaponClass());
                rVal.putData(EntityDataStrings.ITEM_WEAPON_DATA_RAW,weaponData);
            }

            //
            //stacking behavior
            //
            if(itemData.getMaxStack() != null){
                ClientChargeState.attachTree(rVal, itemData.getMaxStack());
            }

            //
            //icon
            if(itemData.getIconPath() != null && !itemData.getIconPath().equals("")){
                rVal.putData(EntityDataStrings.ITEM_ICON,itemData.getIconPath());
            } else {
                rVal.putData(EntityDataStrings.ITEM_ICON,AssetDataStrings.UI_TEXTURE_ITEM_ICON_GENERIC);
            }


            rVal.putData(EntityDataStrings.ITEM_EQUIP_CLASS, item.getData(EntityDataStrings.ITEM_EQUIP_CLASS));
            CommonEntityUtils.setEntityType(rVal, EntityType.ITEM);
            rVal.putData(EntityDataStrings.ITEM_IS_IN_INVENTORY, true);
            ItemUtils.setContainingParent(rVal, containingParent);
            CommonEntityUtils.setEntitySubtype(rVal, CommonEntityUtils.getEntitySubtype(item));
            Globals.clientState.clientSceneWrapper.getScene().registerEntity(rVal);
            Globals.clientState.clientSceneWrapper.getScene().registerEntityToTag(rVal, EntityTags.ITEM);
            return rVal;
        } else {
            return null;
        }
    }

    /**
     * [SERVER ONLY] Applies transforms that we would expect to be applied to an entity that is an item inside an inventory
     * @param itemData The item data
     * @param item The item entity
     * @param containingParent The containing parent entity
     */
    private static void serverApplyInInventoryItemTransforms(Item itemData, Entity itemEnt, Entity containingParent){
        if(itemData == null){
            throw new Error("Item data is null");
        }
        if(itemEnt == null){
            throw new Error("Item entity is null");
        }
        if(containingParent == null){
            throw new Error("Containing parent is null");
        }
        itemEnt.putData(EntityDataStrings.ITEM_IS_IN_INVENTORY, true);
        ItemUtils.setContainingParent(itemEnt, containingParent);
        CommonEntityUtils.setEntityType(itemEnt, EntityType.ITEM);
        CommonEntityUtils.setEntitySubtype(itemEnt, itemData.getId());
        CommonEntityUtils.setCommonData(itemEnt, itemData);
    }


    /**
     * Emits an entity which represents the item inside a container
     * @param item The item to recreate
     * @param containingParent The parent that contains the item
     */
    public static Entity serverRecreateContainerItem(Entity item, Entity containingParent){
        if(ItemUtils.isItem(item)){
            Item itemData = Globals.gameConfigCurrent.getItemMap().getItem(ItemUtils.getType(item));
            Realm realm = Globals.serverState.realmManager.getEntityRealm(containingParent);
            Entity rVal = EntityCreationUtils.createServerInventoryEntity(realm);
            
            //apply normal item transforms
            ItemUtils.serverApplyItemDataTransforms(itemData, rVal);

            //apply in-inventory transforms
            ItemUtils.serverApplyInInventoryItemTransforms(itemData, rVal, containingParent);

            //error checking
            if(Globals.serverState.realmManager.getEntityRealm(rVal) == null){
                LoggerInterface.loggerEngine.ERROR(new IllegalStateException("Created item without it being assigned to a realm!"));
            }
            if(Globals.serverState.entityDataCellMapper.getEntityDataCell(rVal) == null){
                throw new Error("Failed to get data cell or the entity");
            }
            if(ServerGravityTree.hasServerGravityTree(rVal)){
                throw new Error("In-inventory item has gravity tree!");
            }

            return rVal;
        } else {
            return null;
        }
    }

    /**
     * Creates a new item in the parent's inventory
     * @param parent The parent that contains the item
     * @param itemData The item data
     */
    public static Entity serverCreateContainerItem(Entity parent, Item itemData){
        if(parent == null || itemData == null){
            throw new Error("Provided bad data! " + parent + " " + itemData);
        }
        //make sure there's an inventory to store the item
        UnrelationalInventoryState naturalInventory = InventoryUtils.getNaturalInventory(parent);
        RelationalInventoryState toolbarInventory = InventoryUtils.getToolbarInventory(parent);
        if(naturalInventory == null && toolbarInventory == null){
            throw new Error("Trying to store an item in an entity with no inventories!");
        }
        Realm realm = Globals.serverState.realmManager.getEntityRealm(parent);
        


        //must correct the position such that it spawns inside the realm
        Entity rVal = EntityCreationUtils.createServerInventoryEntity(realm);
        
        //apply normal item transforms
        ItemUtils.serverApplyItemDataTransforms(itemData, rVal);

        //apply item transforms to an entity
        ItemUtils.serverApplyInInventoryItemTransforms(itemData, rVal, parent);

        //store the item in its actual inventory
        naturalInventory.addItem(rVal);

        //error checking
        if(Globals.serverState.realmManager.getEntityRealm(rVal) == null){
            LoggerInterface.loggerEngine.ERROR(new IllegalStateException("Created item without it being assigned to a realm!"));
        }
        if(Globals.serverState.entityDataCellMapper.getEntityDataCell(rVal) == null){
            throw new Error("Failed to get data cell or the entity");
        }

        //send entity to client
        if(CreatureUtils.hasControllerPlayerId(parent)){
            int playerId = CreatureUtils.getControllerPlayerId(parent);
            Player player = Globals.serverState.playerManager.getPlayerFromId(playerId);
            player.addMessage(InventoryMessage.constructaddItemToInventoryMessage(parent.getId(), rVal.getId(), itemData.getId()));
        }

        return rVal;
    }

    /**
     * Try destroying a client item in the world
     * @param item The item to destroy
     */
    public static void clientDestroyInWorldItem(Entity item){
        if(ItemUtils.isItem(item)){
            //destroy physics
            if(PhysicsEntityUtils.containsDBody(item) && item.containsKey(EntityDataStrings.PHYSICS_COLLIDABLE)){
                //destroy physics
                //this deregisters from all four & unhooks rigid bodies from the physics runtime
                Globals.clientState.clientSceneWrapper.getCollisionEngine().destroyPhysics(item);
                //destroy graphics
                ClientEntityUtils.destroyEntity(item);
            }
        }
    }

    public static String getWeaponClass(Entity item){
        return (String)item.getData(EntityDataStrings.ITEM_WEAPON_CLASS);
    }

    public static String getItemIcon(Entity item){
        return (String)item.getData(EntityDataStrings.ITEM_ICON);
    }

    /**
     * Gets the equip class of the item
     * @param item The item
     * @return The equip class of the item. Null if null is passed in
     */
    public static String getEquipClass(Entity item){
        if(item == null){
            return null;
        }
        return (String)item.getData(EntityDataStrings.ITEM_EQUIP_CLASS);
    }

    public static void setRealWorldEntity(Entity inInventory, Entity inWorld){
        inInventory.putData(EntityDataStrings.ITEM_IN_WORLD_REPRESENTATION, inWorld);
    }

    public static Entity getRealWorldEntity(Entity inInventory){
        return (Entity) inInventory.getData(EntityDataStrings.ITEM_IN_WORLD_REPRESENTATION);
    }

    /**
     * Gets the raw weapon data
     * @param item The item entity
     * @return The weapon data if it exists, null otherwise
     */
    public static WeaponData getWeaponDataRaw(Entity item){
        return (WeaponData) item.getData(EntityDataStrings.ITEM_WEAPON_DATA_RAW);
    }

}
