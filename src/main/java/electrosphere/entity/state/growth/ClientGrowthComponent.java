package electrosphere.entity.state.growth;


import org.joml.Vector3d;

import electrosphere.data.entity.foliage.GrowthData;
import electrosphere.engine.Globals;
import electrosphere.entity.EntityDataStrings;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.entity.ClientEntityUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;

/**
 * Tree for doing the inbetween work to grow an entity into a new entity
 */
@SynchronizedBehaviorTree(name = "clientGrowth", isServer = false, correspondingTree="serverGrowth")
public class ClientGrowthComponent implements BehaviorTree {

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
     * Creates an idle tree
     * @param e the entity to attach the tree to
     */
    private ClientGrowthComponent(Entity e, Object ... params){
        parent = e;
        this.data = (GrowthData)params[0];
    }

    @Override
    public void simulate(float deltaTime){
        float percentage = this.status / (float)data.getGrowthMax();
        Vector3d targetScale = new Vector3d(data.getScaleMax() * percentage);
        ClientEntityUtils.setScale(parent, targetScale);
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
    public static ClientGrowthComponent attachTree(Entity parent, Object ... params){
        ClientGrowthComponent rVal = new ClientGrowthComponent(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        parent.putData(EntityDataStrings.TREE_CLIENTGROWTH, rVal);
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_CLIENTGROWTH_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_CLIENTGROWTH_ID);
    }

    /**
     * <p>
     * Gets the ClientGrowthComponent of the entity
     * </p>
     * @param entity the entity
     * @return The ClientGrowthComponent
     */
    public static ClientGrowthComponent getClientGrowthComponent(Entity entity){
        return (ClientGrowthComponent)entity.getData(EntityDataStrings.TREE_CLIENTGROWTH);
    }

    /**
     * <p>
     * Checks if the entity has a ClientGrowthComponent component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasClientGrowthComponent(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_CLIENTGROWTH);
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
