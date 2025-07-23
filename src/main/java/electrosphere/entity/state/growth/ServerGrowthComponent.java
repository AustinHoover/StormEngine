package electrosphere.entity.state.growth;


import org.joml.Vector3d;

import electrosphere.data.entity.foliage.GrowthData;
import electrosphere.engine.Globals;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.net.synchronization.enums.FieldIdEnums;
import electrosphere.server.datacell.utils.DataCellSearchUtils;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.life.ServerLifeTree;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;


/**
 * Tree for playing an idle animation when an entity isn't doing anything
 */
@SynchronizedBehaviorTree(name = "serverGrowth", isServer = true, correspondingTree = "clientGrowth")
public class ServerGrowthComponent implements BehaviorTree {

    /**
     * The current growth amount
     */
    @SyncedField
    private int status;

    /**
     * The parent entity
     */
    Entity parent;

    /**
     * The data
     */
    GrowthData data;

    /**
     * Creates a server idle tree
     * @param e The entity to attach it to
     */
    public ServerGrowthComponent(Entity e, Object ... params){
        parent = e;
        this.data = (GrowthData)params[0];
    }

    @Override
    public void simulate(float deltaTime){
        if(status < data.getGrowthMax()){
            this.setStatus(status + 1);
        }
        if(status == data.getGrowthMax() && this.data.getMaxGrowthLoot() != null){
            ServerLifeTree.setLootPool(parent, this.data.getMaxGrowthLoot());
        }
        float percentage = this.status / (float)data.getGrowthMax();
        Vector3d targetScale = new Vector3d(data.getScaleMax() * percentage);
        ServerEntityUtils.setScale(parent, targetScale);
    }

    /**
     * Maxes out the growth for this component
     */
    public void maxGrowth(){
        this.setStatus(this.data.getGrowthMax());
        if(this.data.getMaxGrowthLoot() != null){
            ServerLifeTree.setLootPool(parent, this.data.getMaxGrowthLoot());
        }
        Vector3d targetScale = new Vector3d(data.getScaleMax() * 1.0f);
        ServerEntityUtils.setScale(parent, targetScale);
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
    public static ServerGrowthComponent attachTree(Entity parent, Object ... params){
        ServerGrowthComponent rVal = new ServerGrowthComponent(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        ServerBehaviorTreeUtils.attachBTreeToEntity(parent, rVal);
        parent.putData(EntityDataStrings.TREE_SERVERGROWTH, rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_SERVERGROWTH_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_SERVERGROWTH_ID);
    }

    /**
     * <p>
     * Gets the ServerGrowthComponent of the entity
     * </p>
     * @param entity the entity
     * @return The ServerGrowthComponent
     */
    public static ServerGrowthComponent getServerGrowthComponent(Entity entity){
        return (ServerGrowthComponent)entity.getData(EntityDataStrings.TREE_SERVERGROWTH);
    }

    /**
     * <p>
     * Checks if the entity has a ServerGrowthComponent component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasServerGrowthComponent(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_SERVERGROWTH);
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets status and handles the synchronization logic for it.
     * </p>
     * @param status The value to set status to.
     */
    public void setStatus(int status){
        this.status = status;
        if(DataCellSearchUtils.getEntityDataCell(parent) != null){
            DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(SynchronizationMessage.constructUpdateClientIntStateMessage(parent.getId(), BehaviorTreeIdEnums.BTREE_SERVERGROWTH_ID, FieldIdEnums.TREE_SERVERGROWTH_SYNCEDFIELD_STATUS_ID, status));
        }
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets status.
     * </p>
     */
    public int getStatus(){
        return status;
    }

}
