package electrosphere.entity.state.equip;


import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.state.client.firstPerson.FirstPersonTree;
import electrosphere.entity.state.gravity.GravityUtils;
import electrosphere.entity.state.inventory.InventoryUtils;
import electrosphere.entity.state.inventory.RelationalInventoryState;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.item.ItemUtils;

import java.util.List;

import electrosphere.client.ui.menu.ingame.ToolbarPreviewWindow;
import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.controls.cursor.CursorState;
import electrosphere.data.block.fab.BlockFab;
import electrosphere.data.entity.common.treedata.TreeDataAnimation;
import electrosphere.data.entity.creature.equip.EquipPoint;
import electrosphere.data.entity.creature.equip.ToolbarData;
import electrosphere.data.entity.item.EquipWhitelist;
import electrosphere.data.entity.item.Item;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.entity.ClientEntityUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.renderer.actor.Actor;
import electrosphere.renderer.actor.mask.ActorMeshMask;
import electrosphere.util.FileUtils;
import electrosphere.net.parser.net.message.InventoryMessage;
import electrosphere.net.parser.net.message.NetworkMessage;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;

/**
 * The state of the toolbar on the client's side
 */
@SynchronizedBehaviorTree(name = "clientToolbarState", isServer = false, correspondingTree="serverToolbarState")
public class ClientToolbarState implements BehaviorTree {

    /**
     * The maximum number of toolbar slots
     */
    public static final int MAX_TOOLBAR_SIZE = 10;
    
    /**
     * The selected toolbar slot
     */
    @SyncedField
    int selectedSlot;

    /**
     * The slot selected the previous frame -- used to detect changes
     */
    int previousFrameSlot;

    /**
     * The parent entity
     */
    Entity parent;

    /**
     * The toolbar data
     */
    ToolbarData toolbarData;

    /**
     * The equipped entity
     */
    Entity equippedEntity = null;

    /**
     * Attempts to add an item to the toolbar
     * @param item The item
     */
    public void attemptAddToToolbar(Entity item, int toolbarSlot){
        if(item != null){
            NetworkMessage requestUnequipMessage = InventoryMessage.constructclientRequestAddToolbarMessage(Globals.clientState.clientSceneWrapper.mapClientToServerId(item.getId()), toolbarSlot);
            Globals.clientState.clientConnection.queueOutgoingMessage(requestUnequipMessage);
        }
    }

    /**
     * Performs the actual logic to term meshes on/off when equpping an item
     * @param toEquip The entity to equip
     */
    public void attemptEquip(Entity toEquip){
        //
        //getting data for the attempt
        RelationalInventoryState equipInventoryState = InventoryUtils.getEquipInventory(parent);
        boolean targetHasWhitelist = ItemUtils.hasEquipList(toEquip);
        String equipItemClass = ItemUtils.getEquipClass(toEquip);
        EquipPoint targetPoint = equipInventoryState.getEquipPointFromSlot(toolbarData.getPrimarySlot());
        if(equipItemClass != null && targetPoint.getEquipClassWhitelist() != null && !targetPoint.getEquipClassWhitelist().contains(equipItemClass)){
            targetPoint = equipInventoryState.getEquipPointFromSlot(toolbarData.getCombinedSlot());
        }
        equippedEntity = toEquip;

        //
        //visual transforms
        if(targetHasWhitelist){
            //depends on the type of creature, must be replacing a mesh
            String parentCreatureId = CreatureUtils.getType(parent);
            List<EquipWhitelist> whitelist = ItemUtils.getEquipWhitelist(toEquip);
            for(EquipWhitelist whitelistItem : whitelist){
                if(whitelistItem.getCreatureId().equals(parentCreatureId)){
                    //put in map
                    String modelName = whitelistItem.getModel();
                    Globals.assetManager.addModelPathToQueue(modelName);
                    Actor parentActor = EntityUtils.getActor(parent);
                    //queue meshes from display model to parent actor
                    ActorMeshMask meshMask = parentActor.getMeshMask();
                    for(String toBlock : whitelistItem.getMeshMaskList()){
                        meshMask.blockMesh(modelName, toBlock);
                    }
                    for(String toDraw : whitelistItem.getMeshList()){
                        meshMask.queueMesh(modelName, toDraw);
                    }
                    //attach to parent bone
                    if(parent != Globals.clientState.firstPersonEntity || Globals.controlHandler.cameraIsThirdPerson()){
                        AttachUtils.clientAttachEntityToEntityAtBone(
                            parent,
                            toEquip,
                            targetPoint.getBone(),
                            AttachUtils.getEquipPointVectorOffset(targetPoint.getOffsetVectorThirdPerson()),
                            AttachUtils.getEquipPointRotationOffset(targetPoint.getOffsetRotationThirdPerson())
                        );
                    } else {
                        AttachUtils.clientAttachEntityToEntityAtBone(
                            Globals.clientState.firstPersonEntity,
                            toEquip,
                            targetPoint.getFirstPersonBone(),
                            AttachUtils.getEquipPointVectorOffset(targetPoint.getOffsetVectorFirstPerson()),
                            AttachUtils.getEquipPointRotationOffset(targetPoint.getOffsetRotationFirstPerson())
                        );
                    }
                    //make uncollidable
                    if(PhysicsEntityUtils.containsDBody(toEquip) && toEquip.containsKey(EntityDataStrings.PHYSICS_COLLIDABLE)){
                        Globals.clientState.clientSceneWrapper.getCollisionEngine().destroyPhysics(toEquip);
                    }
                    //make untargetable
                    Globals.clientState.clientSceneWrapper.getScene().removeEntityFromTag(toEquip, EntityTags.TARGETABLE);
                    break;
                }
            }
        } else {
            //does not depend on the type of creature, must be attaching to a bone
            //make sure it's visible
            if(EntityUtils.getActor(toEquip) == null){
                Item itemData = Globals.gameConfigCurrent.getItemMap().getItem(ItemUtils.getType(toEquip));
                EntityCreationUtils.makeEntityDrawable(toEquip, itemData.getGraphicsTemplate().getModel().getPath());
                if(itemData.getIdleAnim() != null){
                    toEquip.putData(EntityDataStrings.ANIM_IDLE,itemData.getIdleAnim());
                }
            }
            //actually equip
            if(parent != Globals.clientState.firstPersonEntity ||  Globals.controlHandler.cameraIsThirdPerson()){
                AttachUtils.clientAttachEntityToEntityAtBone(
                    parent,
                    toEquip,
                    targetPoint.getBone(),
                    AttachUtils.getEquipPointVectorOffset(targetPoint.getOffsetVectorThirdPerson()),
                    AttachUtils.getEquipPointRotationOffset(targetPoint.getOffsetRotationThirdPerson())
                );
            } else {
                AttachUtils.clientAttachEntityToEntityAtBone(
                    Globals.clientState.firstPersonEntity,
                    toEquip,
                    targetPoint.getFirstPersonBone(),
                    AttachUtils.getEquipPointVectorOffset(targetPoint.getOffsetVectorFirstPerson()),
                    AttachUtils.getEquipPointRotationOffset(targetPoint.getOffsetRotationFirstPerson())
                );
            }
            if(PhysicsEntityUtils.containsDBody(toEquip) && toEquip.containsKey(EntityDataStrings.PHYSICS_COLLIDABLE)){
                Globals.clientState.clientSceneWrapper.getCollisionEngine().destroyPhysics(toEquip);
            }
            Globals.clientState.clientSceneWrapper.getScene().removeEntityFromTag(toEquip, EntityTags.TARGETABLE);
            GravityUtils.clientAttemptDeactivateGravity(toEquip);
        }

        //cursor logic
        if(targetPoint != null && parent == Globals.clientState.playerEntity){
            Item itemData = Globals.gameConfigCurrent.getItemMap().getItem(toEquip);
            if(Globals.cursorState.playerCursor != null && Globals.cursorState.playerBlockCursor != null){
                CursorState.hide();
                Globals.cursorState.setClampToExistingBlock(false);
                if(itemData.getTokens().contains(CursorState.CURSOR_TOKEN)){
                    CursorState.makeRealVisible();
                } else if(itemData.getTokens().contains(CursorState.CURSOR_BLOCK_TOKEN)) {
                    Globals.cursorState.setClampToExistingBlock(true);
                    CursorState.makeBlockVisible(AssetDataStrings.TEXTURE_RED_TRANSPARENT);
                } else if(itemData.getFabData() != null){
                    Globals.cursorState.setSelectedFab(BlockFab.read(FileUtils.getAssetFile(itemData.getFabData().getFabPath())));
                    Globals.cursorState.setSelectedFabPath(FileUtils.getAssetFileString(itemData.getFabData().getFabPath()));
                    CursorState.makeFabVisible();
                } else if(itemData.getGridAlignedData() != null){
                    String modelPath = AssetDataStrings.UNITCUBE;
                    if(itemData.getGraphicsTemplate() != null && itemData.getGraphicsTemplate().getModel() != null){
                        modelPath = itemData.getGraphicsTemplate().getModel().getPath();
                    }
                    Globals.cursorState.setGridAlignmentData(itemData.getGridAlignedData());
                    CursorState.makeGridAlignedVisible(modelPath);
                }
            }
        }
    }

    /**
     * Performs the actual logic to turn meshes on/off when unequipping an item
     */
    public void unequip(){
        if(this.equippedEntity != null){
            boolean targetHasWhitelist = ItemUtils.hasEquipList(this.equippedEntity);
            RelationalInventoryState equipInventoryState = InventoryUtils.getEquipInventory(parent);
            String equipItemClass = ItemUtils.getEquipClass(this.equippedEntity);

            EquipPoint targetPoint = equipInventoryState.getEquipPointFromSlot(toolbarData.getPrimarySlot());
            if(targetPoint.getEquipClassWhitelist() != null && !targetPoint.getEquipClassWhitelist().contains(equipItemClass)){
                targetPoint = equipInventoryState.getEquipPointFromSlot(toolbarData.getCombinedSlot());
            }

            //
            //visual transforms
            if(targetHasWhitelist){
                //depends on the type of creature, must be replacing meshes
                String parentCreatureId = CreatureUtils.getType(parent);
                List<EquipWhitelist> whitelist = ItemUtils.getEquipWhitelist(this.equippedEntity);
                for(EquipWhitelist whitelistItem : whitelist){
                    if(whitelistItem.getCreatureId().equals(parentCreatureId)){
                        //put in map
                        Actor parentActor = EntityUtils.getActor(parent);
                        //queue meshes from display model to parent actor
                        ActorMeshMask meshMask = parentActor.getMeshMask();
                        for(String toUnblock : whitelistItem.getMeshMaskList()){
                            meshMask.unblockMesh(toUnblock);
                        }
                        for(String toDraw : whitelistItem.getMeshList()){
                            meshMask.removeAdditionalMesh(toDraw);
                        }
                        break;
                    }
                }
            } else {
                //does not depend on the type of creature
                AttachUtils.clientDetatchEntityFromEntityAtBone(parent, this.equippedEntity);
                ClientEntityUtils.destroyEntity(this.equippedEntity);
            }

            //interrupt animation
            if(targetPoint != null){
                Actor thirdPersonActor = EntityUtils.getActor(parent);
                if(targetPoint.getEquippedAnimation() != null){
                    TreeDataAnimation animation = targetPoint.getEquippedAnimation();
                    //play third person
                    if(thirdPersonActor.getAnimationData().isPlayingAnimation() && thirdPersonActor.getAnimationData().isPlayingAnimation(animation)){
                        if(animation != null){
                            thirdPersonActor.getAnimationData().interruptAnimation(animation,true);
                        }
                        thirdPersonActor.getAnimationData().incrementAnimationTime(0.0001);
                    }
                    
                    //play first person
                    FirstPersonTree.conditionallyInterruptAnimation(parent, animation);
                }
            }

            if(Globals.cursorState.playerCursor != null){
                Globals.clientState.clientSceneWrapper.getScene().removeEntityFromTag(Globals.cursorState.playerCursor, EntityTags.DRAWABLE);
            }
            if(Globals.cursorState.playerBlockCursor != null){
                Globals.clientState.clientSceneWrapper.getScene().removeEntityFromTag(Globals.cursorState.playerBlockCursor, EntityTags.DRAWABLE);
            }
            
            //hide cursors
            CursorState.hide();

            //null out the attached entity
            this.equippedEntity = null;
        }
    }

    /**
     * Updates the toolbar state based on the inventory (ie, should call this after editing the inventory directly)
     */
    public void update(){
        RelationalInventoryState inventory = InventoryUtils.getToolbarInventory(this.parent);
        Entity inventoryEquipped = inventory.getItemSlot("" + this.selectedSlot);
        if(inventoryEquipped == null && this.equippedEntity != null){
            this.unequip();
        }
    }

    /**
     * Gets the current primary item if it exists, null otherwise
     * @return The item
     */
    public Entity getCurrentPrimaryItem(){
        return this.equippedEntity;
    }

    /**
     * Attempts to change the selection to a new value
     * @param value The value
     */
    public void attemptChangeSelection(int value){
        while(value < 0){
            value = value + MAX_TOOLBAR_SIZE;
        }
        value = value % MAX_TOOLBAR_SIZE;
        Globals.clientState.clientConnection.queueOutgoingMessage(InventoryMessage.constructclientUpdateToolbarMessage(value));
    }
    

    /**
     * <p> (initially) Automatically generated </p>
     * <p>
     * Attaches this tree to the entity.
     * </p>
     * @param entity The entity to attach to
     * @param tree The behavior tree to attach
     * @param params Optional parameters that will be provided to the constructor
     */
    public static ClientToolbarState attachTree(Entity parent, Object ... params){
        ClientToolbarState rVal = new ClientToolbarState(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        parent.putData(EntityDataStrings.TREE_CLIENTTOOLBARSTATE, rVal);
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_CLIENTTOOLBARSTATE_ID);
        return rVal;
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Detatches this tree from the entity.
     * </p>
     * @param entity The entity to detach to
     * @param tree The behavior tree to detach
     */
    public static void detachTree(Entity entity, BehaviorTree tree){
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_CLIENTTOOLBARSTATE_ID);
    }

    /**
     * <p> (initially) Automatically generated </p>
     * <p> Private constructor to enforce using the attach methods </p>
     * <p>
     * Constructor
     * </p>
     * @param parent The parent entity of this tree
     * @param params Optional parameters that can be provided when attaching the tree. All custom data required for creating this tree should be passed in this varargs.
     */
    private ClientToolbarState(Entity parent, Object ... params){
        this.parent = parent;
        this.toolbarData = (ToolbarData)params[0];
    }

    /**
     * <p>
     * Gets the ClientToolbarState of the entity
     * </p>
     * @param entity the entity
     * @return The ClientToolbarState
     */
    public static ClientToolbarState getClientToolbarState(Entity entity){
        return (ClientToolbarState)entity.getData(EntityDataStrings.TREE_CLIENTTOOLBARSTATE);
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets selectedSlot and handles the synchronization logic for it.
     * </p>
     * @param selectedSlot The value to set selectedSlot to.
     */
    public void setSelectedSlot(int selectedSlot){
        this.selectedSlot = selectedSlot;
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets selectedSlot.
     * </p>
     */
    public int getSelectedSlot(){
        return selectedSlot;
    }

    @Override
    public void simulate(float deltaTime) {
        if(this.previousFrameSlot != this.selectedSlot){
            this.previousFrameSlot = this.selectedSlot;
            if(this.parent == Globals.clientState.playerEntity){
                ToolbarPreviewWindow.reveal();
            }
        }
    }

    /**
     * <p>
     * Checks if the entity has a ClientToolbarState component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasClientToolbarState(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_CLIENTTOOLBARSTATE);
    }

}
