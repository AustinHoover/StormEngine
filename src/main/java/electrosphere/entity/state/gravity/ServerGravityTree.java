package electrosphere.entity.state.gravity;


import electrosphere.entity.EntityDataStrings;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.server.datacell.utils.DataCellSearchUtils;
import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;

import org.ode4j.ode.DBody;

import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.collision.collidable.Collidable;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.collidable.Impulse;
import electrosphere.entity.state.gravity.ClientGravityTree.GravityTreeState;
import electrosphere.entity.state.movement.fall.ServerFallTree;
import electrosphere.entity.state.movement.jump.ServerJumpTree;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.net.synchronization.enums.FieldIdEnums;

@SynchronizedBehaviorTree(name = "serverGravity", isServer = true, correspondingTree="clientGravity")
/**
 * Tree for making the entity fall if there's nothing underneath it
 */
public class ServerGravityTree implements BehaviorTree {
    
    @SyncedField
    GravityTreeState state;
    
    Entity parent;

    int frameCurrent = 0;
    int fallFrame = 1;
    
    DBody body;
    Collidable collidable;

    static final float gravityConstant = 0.2f;
    static final float linearDamping = 0.1f;
    
    private ServerGravityTree(Entity e, Object ... params){
        state = GravityTreeState.ACTIVE;
        parent = e;
        this.collidable = (Collidable)params[0];
        this.body = (DBody)params[1];
        this.fallFrame = (int)params[2];
    }
    
    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets state.
     * </p>
     */
    public GravityTreeState getState(){
        return state;
    }
    
    public void start(){
        if(this.state == GravityTreeState.NOT_ACTIVE){
            this.setState(GravityTreeState.ACTIVE);
        }
        if(state == GravityTreeState.NOT_ACTIVE){
            frameCurrent = 0;
        }
    }
    
    public void stop(){
        if(this.state == GravityTreeState.ACTIVE){
            this.setState(GravityTreeState.NOT_ACTIVE);
        }
    }

    /**
     * Checks if the gravity tree is active
     * @return true if active, false otherwise
     */
    public boolean isActive(){
        return this.state == GravityTreeState.ACTIVE;
    }
    
    /**
     * Simulates the gravity tree
     * @param deltaTime The time to simulate it for
     */
    public void simulate(float deltaTime){

        //make sure physics is available
        if(collidable == null){
            switch(this.state){
                case ACTIVE: {
                    this.setState(GravityTreeState.NOT_ACTIVE);
                } break;
                case NOT_ACTIVE: {
                } break;
            }
            return;
        }
        
        //state machine
        switch(state){
            case ACTIVE: {
                if(this.hadGroundCollision() || !this.bodyIsActive()){
                    this.setState(GravityTreeState.NOT_ACTIVE);
                    ServerJumpTree jumpTree;
                    if((jumpTree = ServerJumpTree.getServerJumpTree(parent))!=null){
                        jumpTree.land();
                    }
                    ServerFallTree fallTree;
                    if((fallTree = ServerFallTree.getFallTree(parent))!=null){
                        fallTree.land();
                    }
                    frameCurrent = 0;
                } else {
                    //animation nonsense
                    if(frameCurrent == fallFrame){
                        ServerFallTree fallTree;
                        if((fallTree = ServerFallTree.getFallTree(parent))!=null){
                            fallTree.start();
                        }
                    }
                    frameCurrent++;
                }
            } break;
            case NOT_ACTIVE: {
                if(this.hadEntityCollision() && this.bodyIsActive()){
                    this.start();
                }
                //nothing here atm
                //eventually want to check if need to re-activate somehow
            } break;
        }
    }
    
    /**
     * Checks if the gravity tree had a collision with terrain
     * @return true if collided on the most recent frame, false otherwise
     */
    public boolean hadGroundCollision(){
        boolean rVal = false;
        Impulse[] impulses = collidable.getImpulses();
        for(int i = 0; i < collidable.getImpulseCount(); i++){
            Impulse impulse = impulses[i];
            if(impulse.getType().equals(Collidable.TYPE_STATIC)){
                rVal = true;
                break;
            }
        }
        return rVal;
    }

    /**
     * Checks if the physics body is active
     * @return true if it is active (or not present), false otherwise
     */
    private boolean bodyIsActive(){
        if(PhysicsEntityUtils.getDBody(parent) == null){
            return false;
        }
        DBody body = PhysicsEntityUtils.getDBody(parent);
        return body.isEnabled();
    }
    
    /**
     * Checks if the gravity tree had a collision with an entity
     * @return true if collided on the most recent frame, false otherwise
     */
    public boolean hadEntityCollision(){
        boolean rVal = false;
        Impulse[] impulses = collidable.getImpulses();
        for(int i = 0; i < collidable.getImpulseCount(); i++){
            Impulse impulse = impulses[i];
            if(impulse.getType().equals(Collidable.TYPE_CREATURE)){
                rVal = true;
                break;
            }
        }
        return rVal;
    }

    /**
     * Checks if the physics on the tree is valid
     * @return true if it is valid, false otherwise
     */
    public boolean physicsIsValid(){
        return collidable != null && body != null;
    }

    /**
     * Updates the physics pair in this tree
     * @param collidable The collidable
     * @param body The rigid body
     */
    public void updatePhysicsPair(Collidable collidable, DBody body){
        this.collidable = collidable;
        this.body = body;
    }
    
    /**
     * <p> Automatically generated </p>
     * <p>
     * Sets state and handles the synchronization logic for it.
     * </p>
     * @param state The value to set state to.
     */
    public void setState(GravityTreeState state){
        this.state = state;
        int value = ClientGravityTree.getGravityTreeStateEnumAsShort(state);
        if(DataCellSearchUtils.getEntityDataCell(parent) != null){
            DataCellSearchUtils.getEntityDataCell(parent).broadcastNetworkMessage(SynchronizationMessage.constructUpdateClientStateMessage(parent.getId(), BehaviorTreeIdEnums.BTREE_SERVERGRAVITY_ID, FieldIdEnums.TREE_SERVERGRAVITY_SYNCEDFIELD_STATE_ID, value));
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
    public static ServerGravityTree attachTree(Entity parent, Object ... params){
        ServerGravityTree rVal = new ServerGravityTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        ServerBehaviorTreeUtils.attachBTreeToEntity(parent, rVal);
        parent.putData(EntityDataStrings.TREE_SERVERGRAVITY, rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_SERVERGRAVITY_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_SERVERGRAVITY_ID);
    }
    /**
     * <p>
     * Gets the ServerGravityTree of the entity
     * </p>
     * @param entity the entity
     * @return The ServerGravityTree
     */
    public static ServerGravityTree getServerGravityTree(Entity entity){
        return (ServerGravityTree)entity.getData(EntityDataStrings.TREE_SERVERGRAVITY);
    }
    /**
     * <p>
     * Checks if the entity has a ServerGravityTree component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasServerGravityTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_SERVERGRAVITY);
    }

}
