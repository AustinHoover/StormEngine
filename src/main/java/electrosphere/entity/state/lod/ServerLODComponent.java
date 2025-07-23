package electrosphere.entity.state.lod;


import org.joml.Vector3d;

import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.data.entity.common.CommonEntityType;
import electrosphere.engine.Globals;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.net.synchronization.enums.FieldIdEnums;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.utils.DataCellSearchUtils;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.entity.Entity;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;

/**
 * Creates a server LOD component
 */
@SynchronizedBehaviorTree(name = "serverLODTree", isServer = true, correspondingTree="clientLODTree")
public class ServerLODComponent implements BehaviorTree {

    /**
     * Radius after which we reduce LOD
     */
    public static final int LOD_RADIUS = 32;

    /**
     * Full resolution LOD
     */
    public static final int FULL_RES = 1;

    /**
     * Low resolution
     */
    public static final int LOW_RES = 0;
    
    /**
     * The current LOD level
     */
    @SyncedField
    private int lodLevel;

    /**
     * The parent entity
     */
    private Entity parent;

    @Override
    public void simulate(float deltaTime) {
        Vector3d parentLoc = EntityUtils.getPosition(this.parent);
        boolean fullRes = Globals.serverState.lodEmitterService.isFullLod(parentLoc);
        if(fullRes){
            if(this.lodLevel != FULL_RES){
                //make full res
                this.setLodLevel(FULL_RES);
                Realm realm = Globals.serverState.realmManager.getEntityRealm(this.parent);
                CommonEntityType type = CommonEntityUtils.getCommonData(this.parent);
                if(type.getCollidable() != null){
                    PhysicsEntityUtils.serverDestroyPhysics(this.parent);
                    PhysicsEntityUtils.serverAttachCollidableTemplate(realm, this.parent, type.getCollidable(),EntityUtils.getPosition(parent));
                }
            }
        } else {
            if(this.lodLevel != LOW_RES){
                //make low res
                this.setLodLevel(LOW_RES);
                PhysicsEntityUtils.serverDestroyPhysics(this.parent);
                Realm realm = Globals.serverState.realmManager.getEntityRealm(parent);
                CommonEntityType type = CommonEntityUtils.getCommonData(this.parent);
                if(type.getCollidable() != null){
                    if(CreatureUtils.hasControllerPlayerId(parent)){
                        throw new Error("Should not be attaching a geometry to a player entity!");
                    }
                    PhysicsEntityUtils.serverAttachGeom(realm, parent, type.getCollidable(), EntityUtils.getPosition(parent));
                }
            }
        }
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
    public static ServerLODComponent attachTree(Entity parent, Object ... params){
        ServerLODComponent rVal = new ServerLODComponent(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        ServerBehaviorTreeUtils.attachBTreeToEntity(parent, rVal);
        parent.putData(EntityDataStrings.TREE_SERVERLODTREE, rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_SERVERLODTREE_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_SERVERLODTREE_ID);
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
    private ServerLODComponent(Entity parent, Object ... params){
        this.parent = parent;
        this.lodLevel = ServerLODComponent.FULL_RES;
    }

    /**
     * <p>
     * Gets the ServerLODComponent of the entity
     * </p>
     * @param entity the entity
     * @return The ServerLODComponent
     */
    public static ServerLODComponent getServerLODComponent(Entity entity){
        return (ServerLODComponent)entity.getData(EntityDataStrings.TREE_SERVERLODTREE);
    }

    /**
     * <p>
     * Checks if the entity has a ServerLODComponent component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasServerLODComponent(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_SERVERLODTREE);
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets lodLevel and handles the synchronization logic for it.
     * </p>
     * @param lodLevel The value to set lodLevel to.
     */
    public void setLodLevel(int lodLevel){
        this.lodLevel = lodLevel;
        if(DataCellSearchUtils.getEntityDataCell(parent) != null){
            DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(SynchronizationMessage.constructUpdateClientIntStateMessage(parent.getId(), BehaviorTreeIdEnums.BTREE_SERVERLODTREE_ID, FieldIdEnums.TREE_SERVERLODTREE_SYNCEDFIELD_LODLEVEL_ID, lodLevel));
        }
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets lodLevel.
     * </p>
     */
    public int getLodLevel(){
        return lodLevel;
    }

}
