package electrosphere.entity.state.gravity;


import org.ode4j.ode.DBody;

import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.collision.collidable.Collidable;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.collidable.Impulse;
import electrosphere.entity.state.movement.fall.ClientFallTree;
import electrosphere.entity.state.movement.jump.ClientJumpTree;
import electrosphere.net.synchronization.annotation.SyncedField;
import electrosphere.net.synchronization.annotation.SynchronizableEnum;
import electrosphere.net.synchronization.annotation.SynchronizedBehaviorTree;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;

@SynchronizedBehaviorTree(name = "clientGravity", isServer = false, correspondingTree="serverGravity")
/**
 * Tree for making the entity fall if there's nothing underneath it
 */
public class ClientGravityTree implements BehaviorTree {
    
    @SynchronizableEnum
    public static enum GravityTreeState {
        ACTIVE,
        NOT_ACTIVE,
    }
    
    @SyncedField
    GravityTreeState state;
    
    Entity parent;

    int frameCurrent = 0;
    int fallFrame = 1;
    
    DBody body;
    Collidable collidable;

    static final float gravityConstant = 0.2f;
    static final float linearDamping = 0.1f;
    
    private ClientGravityTree(Entity e, Object ... params){
        //Collidable collidable, DBody body, int fallFrame
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
        state = GravityTreeState.ACTIVE;
    }
    
    public void stop(){
        state = GravityTreeState.NOT_ACTIVE;
    }

    /**
     * Checks if the gravity tree is active
     * @return true if active, false otherwise
     */
    public boolean isActive(){
        return this.state == GravityTreeState.ACTIVE;
    }
    
    @Override
    public void simulate(float deltaTime){

        //make sure physics is available
        if(this.collidable == null){
            return;
        }
        
        //state machine
        switch(state){
            case ACTIVE:
                if(this.hadGroundCollision() || !this.bodyIsActive() || this.body == null){
                    ClientJumpTree jumpTree;
                    if((jumpTree = ClientJumpTree.getClientJumpTree(parent))!=null && jumpTree.isJumping()){
                        jumpTree.land();
                    }
                    ClientFallTree fallTree;
                    if((fallTree = ClientFallTree.getFallTree(parent))!=null && fallTree.isFalling()){
                        fallTree.land();
                    }
                    frameCurrent = 0;
                } else {
                    //animation nonsense
                    if(frameCurrent == fallFrame){
                        ClientFallTree fallTree;
                        if((fallTree = ClientFallTree.getFallTree(parent))!=null){
                            fallTree.start();
                        }
                    }
                    frameCurrent++;
                }
                break;
            case NOT_ACTIVE:
                //nothing here atm
                //eventually want to check if need to re-activate somehow
                break;
        }
    }
    
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
     * <p> (initially) Automatically generated </p>
     * <p>
     * Attaches this tree to the entity.
     * </p>
     * @param entity The entity to attach to
     * @param tree The behavior tree to attach
     * @param params Optional parameters that will be provided to the constructor
     */
    public static ClientGravityTree attachTree(Entity parent, Object ... params){
        ClientGravityTree rVal = new ClientGravityTree(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        parent.putData(EntityDataStrings.TREE_CLIENTGRAVITY, rVal);
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(rVal);
        Globals.serverState.entityValueTrackingService.attachTreeToEntity(parent, BehaviorTreeIdEnums.BTREE_CLIENTGRAVITY_ID);
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
        Globals.serverState.entityValueTrackingService.detatchTreeFromEntity(entity, BehaviorTreeIdEnums.BTREE_CLIENTGRAVITY_ID);
    }
    /**
     * <p>
     * Gets the ClientGravityTree of the entity
     * </p>
     * @param entity the entity
     * @return The ClientGravityTree
     */
    public static ClientGravityTree getClientGravityTree(Entity entity){
        return (ClientGravityTree)entity.getData(EntityDataStrings.TREE_CLIENTGRAVITY);
    }
    /**
     * <p> Automatically generated </p>
     * <p>
     * Converts this enum type to an equivalent short value
     * </p>
     * @param enumVal The enum value
     * @return The short value
     */
    public static short getGravityTreeStateEnumAsShort(GravityTreeState enumVal){
        switch(enumVal){
            case ACTIVE:
                return 0;
            case NOT_ACTIVE:
                return 1;
            default:
                return 0;
        }
    }
    /**
     * <p> Automatically generated </p>
     * <p>
     * Converts a short to the equivalent enum value
     * </p>
     * @param shortVal The short value
     * @return The enum value
     */
    public static GravityTreeState getGravityTreeStateShortAsEnum(short shortVal){
        switch(shortVal){
            case 0:
                return GravityTreeState.ACTIVE;
            case 1:
                return GravityTreeState.NOT_ACTIVE;
            default:
                return GravityTreeState.ACTIVE;
        }
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
    }
    /**
     * <p>
     * Checks if the entity has a ClientGravityTree component
     * </p>
     * @param entity the entity
     * @return true if the entity contains the component, false otherwise
     */
    public static boolean hasClientGravityTree(Entity entity){
        return entity.containsKey(EntityDataStrings.TREE_CLIENTGRAVITY);
    }

}
