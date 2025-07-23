package electrosphere.collision.hitbox;

import electrosphere.collision.CollisionEngine;
import electrosphere.collision.CollisionEngine.CollisionResolutionCallback;
import electrosphere.engine.Globals;
import electrosphere.entity.state.hitbox.HitboxCollectionState;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages all hitboxes on either the server or client
 */
public class HitboxManager {
    
    /**
     * The list of all hitboxes
     */
    private List<HitboxCollectionState> hitboxes = new LinkedList<HitboxCollectionState>();

    /**
     * The collision engine for the hitbox manager
     */
    private CollisionEngine collisionEngine;

    /**
     * Lock for hitbox collections
     */
    private ReentrantLock lock = new ReentrantLock();
    
    /**
     * Constructor
     * @param resolutionCallback The callback that fires when a collision occurs
     */
    public HitboxManager(CollisionResolutionCallback resolutionCallback){
        collisionEngine = new CollisionEngine("hitbox");
        collisionEngine.setCollisionResolutionCallback(resolutionCallback);
    }
    
    /**
     * Registers a hitbox to the manager
     * @param hitbox the hitbox to register
     */
    public void registerHitbox(HitboxCollectionState hitbox){
        lock.lock();
        hitboxes.add(hitbox);
        lock.unlock();
    }
    
    /**
     * Gets all hitboxes in the manager
     * @return all hitboxes in the manager
     */
    public List<HitboxCollectionState> getAllHitboxes(){
        lock.lock();
        List<HitboxCollectionState> rVal = Collections.unmodifiableList(hitboxes);
        lock.unlock();
        return rVal;
    }

    /**
     * Deregisters a hitbox from the manager
     * @param hitbox the hitbox to deregister
     */
    public void deregisterHitbox(HitboxCollectionState hitbox){
        lock.lock();
        hitboxes.remove(hitbox);
        lock.unlock();
    }

    /**
     * Gets the collision engine associated with the hitbox manager
     * @return The collision engine
     */
    public CollisionEngine getCollisionEngine(){
        return this.collisionEngine;
    }

    /**
     * Runs all per frame functions of the hitbox manager
     */
    public void simulate(){
        //update all positions
        Globals.profiler.beginCpuSample("Update hitbox positions");
        lock.lock();
        for(HitboxCollectionState state : hitboxes){
            state.clearCollisions();
            state.updateHitboxPositions(this.collisionEngine);
        }
        lock.unlock();
        Globals.profiler.endCpuSample();
        //collide hitboxes
        Globals.profiler.beginCpuSample("Collide hitboxes");
        this.collisionEngine.collide();
        this.collisionEngine.clearCollidableImpulseLists();
        Globals.profiler.endCpuSample();
    }



}
