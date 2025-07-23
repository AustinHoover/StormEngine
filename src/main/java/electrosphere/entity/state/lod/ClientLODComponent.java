package electrosphere.entity.state.lod;


import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.collision.PhysicsUtils;
import electrosphere.data.entity.collidable.CollidableTemplate;
import electrosphere.data.entity.common.CommonEntityType;
import electrosphere.engine.Globals;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.ClientEntityUtils;
import electrosphere.entity.Entity;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;

/**
 * Creates a client LOD component
 */
@SynchronizedBehaviorTree(name = "clientLODTree", isServer = false, correspondingTree="serverLODTree")
public class ClientLODComponent implements BehaviorTree {
    
    /**
     * The current LOD level
     */
    @SyncedField
    private int lodLevel;

    /**
     * The cached lod level from the most recent call
     */
    private int cachedLodLevel;

    /**
     * The parent entity
     */
    private Entity parent;

    @Override
    public void simulate(float deltaTime) {
        if(this.lodLevel == ServerLODComponent.FULL_RES){
            CommonEntityType commonData = CommonEntityUtils.getCommonData(this.parent);
            if(
                commonData.getCollidable() != null && 
                PhysicsEntityUtils.getCollidable(this.parent) == null && 
                (
                    //IE actually running real game client
                    (
                        Globals.clientState.playerEntity != null && 
                        EntityUtils.getPosition(Globals.clientState.playerEntity).distance(EntityUtils.getPosition(this.parent)) < ServerLODComponent.LOD_RADIUS
                    ) ||
                    //IE in viewport
                    Globals.clientState.playerEntity == null
                )
            ){
                CollidableTemplate physicsTemplate = commonData.getCollidable();
                PhysicsEntityUtils.clientAttachCollidableTemplate(this.parent, physicsTemplate);
                ClientEntityUtils.repositionEntity(parent, EntityUtils.getPosition(parent), EntityUtils.getRotation(parent));
            }
        }
        if(cachedLodLevel != lodLevel){
            cachedLodLevel = lodLevel;
            if(cachedLodLevel == ServerLODComponent.FULL_RES){
                CommonEntityType type = CommonEntityUtils.getCommonData(this.parent);
                if(type.getCollidable() != null && PhysicsEntityUtils.getCollidable(this.parent) == null){
                    PhysicsEntityUtils.clientAttachCollidableTemplate(parent, type.getCollidable());
                    ClientEntityUtils.repositionEntity(parent, EntityUtils.getPosition(parent), EntityUtils.getRotation(parent));
                }
            } else if(cachedLodLevel == ServerLODComponent.LOW_RES){
                if(PhysicsEntityUtils.containsDBody(this.parent)){
                    PhysicsUtils.destroyPhysicsPair(
                        Globals.clientState.clientSceneWrapper.getCollisionEngine(),
                        PhysicsEntityUtils.getDBody(this.parent),
                        PhysicsEntityUtils.getCollidable(this.parent)
                    );
                }
                CommonEntityType type = CommonEntityUtils.getCommonData(this.parent);
                if(type.getCollidable() != null){
                    if(CreatureUtils.hasControllerPlayerId(parent)){
                        throw new Error("Should not be attaching a geometry to a player entity!");
                    }
                    PhysicsEntityUtils.clientAttachGeom(parent, type.getCollidable(), EntityUtils.getPosition(parent));
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
    public static ClientLODComponent attachTree(Entity parent, Object ... params){
        ClientLODComponent rVal = new ClientLODComponent(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        parent.putData(EntityDataStrings.TREE_CLIENTLODTREE, rVal);
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_CLIENTLODTREE_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_CLIENTLODTREE_ID);
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
    private ClientLODComponent(Entity parent, Object ... params){
        this.parent = parent;
        this.lodLevel = ServerLODComponent.FULL_RES;
        this.cachedLodLevel = ServerLODComponent.FULL_RES;
    }

    /**
     * <p>
     * Gets the ClientLODComponent of the entity
     * </p>
     * @param entity the entity
     * @return The ClientLODComponent
     */
    public static ClientLODComponent getClientLODComponent(Entity entity){
        return (ClientLODComponent)entity.getData(EntityDataStrings.TREE_CLIENTLODTREE);
    }

    /**
     * <p>
     * Checks if the entity has a ClientLODComponent component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasClientLODComponent(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_CLIENTLODTREE);
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
