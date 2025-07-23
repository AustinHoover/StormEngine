package electrosphere.collision.collidable;

import electrosphere.entity.Entity;
import electrosphere.entity.state.collidable.Impulse;

import java.util.Arrays;
import java.util.List;

import org.joml.Vector3d;


/**
 * Stores the type of the collidable object as well as the impulses currently applied to it
 */
public class Collidable {

    /**
     * Max impulses that can be applied to a collidable
     */
    public static final int MAX_IMPULSES = 5;
    
    /**
     * The entity this collidable is attached to
     */
    private Entity parent;

    /**
     * The type of collidable
     */
    private String type;

    /**
     * If true, once impulses are applied to the collidable, have the parent entity resynchronize its position with the collidable
     */
    private boolean parentTracksCollidable = true;
    
    /**
     * The impulses to be applied to this collidable
     */
    private Impulse[] impulses = new Impulse[MAX_IMPULSES];

    /**
     * The number of impulses stored in the collidable
     */
    private int impulseCount = 0;

    /**
     * The params for the surface of this collidable when a collision occurs
     */
    private SurfaceParams surfaceParams;

    /**
     * Tracks whether this collidable has been simulated or not
     */
    private boolean ready = false;
    
    //these should have corresponding category bits along with them
    public static final String TYPE_STATIC = "static";
    public static final long TYPE_STATIC_BIT = 0x1;

    public static final String TYPE_CREATURE = "creature";
    public static final long TYPE_CREATURE_BIT = 0x4;

    public static final String TYPE_OBJECT = "object";
    public static final long TYPE_OBJECT_BIT = 0x40;

    public static final String TYPE_WORLD_BOUND = "worldBound";
    public static final long TYPE_WORLD_BOUND_BIT = 0x100;

    /**
     * A ray casting mask to exclude terrain
     */
    public static final List<String> MASK_NO_TERRAIN = Arrays.asList(new String[]{
        TYPE_STATIC,
        TYPE_CREATURE,
        TYPE_OBJECT,
        TYPE_WORLD_BOUND,
    });

    
    /**
     * Constructor
     * @param parent The parent entity
     * @param type The type of collidable
     * @param parentTracksCollidable true if the parent should have the same position as the collidable, false otherwise
     */
    public Collidable(Entity parent, String type, boolean parentTracksCollidable){
        this.parent = parent;
        this.type = type;
        this.parentTracksCollidable = parentTracksCollidable;
        this.surfaceParams = new SurfaceParams();
        for(int i = 0; i < MAX_IMPULSES; i++){
            this.impulses[i] = new Impulse();
        }
    }

    /**
     * Sets the surface params for the collidable
     * @param surfaceParams The surface params
     */
    public void setSurfaceParams(SurfaceParams surfaceParams){
        this.surfaceParams = surfaceParams;
    }

    /**
     * Gets the surface params for the collidable
     * @return The surface params
     */
    public SurfaceParams getSurfaceParams(){
        return this.surfaceParams;
    }

    /**
     * Gets the array of impulses
     * @return The array of impulses
     */
    public Impulse[] getImpulses() {
        return impulses;
    }

    /**
     * Adds an impulse the collidable
     * @param impulse The impulse
     */
    public void addImpulse(Impulse impulse) {
        if(this.impulseCount < MAX_IMPULSES){
            impulses[this.impulseCount].setCollisionPoint(impulse.getCollisionPoint());
            impulses[this.impulseCount].setDirection(impulse.getDirection());
            impulses[this.impulseCount].setWorldPoint(impulse.getWorldPoint());
            impulses[this.impulseCount].setType(impulse.getType());
            impulses[this.impulseCount].setForce(impulse.getForce());
            this.impulseCount++;
        }
    }

    /**
     * Adds an impulse the collidable
     * @param impulse The impulse
     */
    public void addImpulse(Vector3d direction, Vector3d collisionPoint, Vector3d worldPoint, double force, String type){
        if(this.impulseCount < MAX_IMPULSES){
            impulses[this.impulseCount].setCollisionPoint(collisionPoint);
            impulses[this.impulseCount].setDirection(direction);
            impulses[this.impulseCount].setWorldPoint(worldPoint);
            impulses[this.impulseCount].setType(type);
            impulses[this.impulseCount].setForce(force);
            this.impulseCount++;
        }
    }

    /**
     * Adds an impulse the collidable
     * @param impulse The impulse
     */
    public void addImpulse(String type){
        if(this.impulseCount < MAX_IMPULSES){
            impulses[this.impulseCount].setType(type);
            impulses[this.impulseCount].setForce(0);
            this.impulseCount++;
        }
    }

    public Entity getParent() {
        return parent;
    }

    public String getType() {
        return type;
    }

    /**
     * Gets whether the parent tracks the collidable's position
     * @return True if the parent tracks the collidable's position, false otherwise
     */
    public boolean getParentTracksCollidable(){
        return parentTracksCollidable;
    }

    public void overrideType(String type){
        this.type = type;
    }
    
    /**
     * Clears the impulses
     */
    public void clear(){
        for(int i = 0; i < MAX_IMPULSES; i++){
            impulses[i].clear();
        }
        this.impulseCount = 0;
    }

    /**
     * Gets whether the collidable is ready or not
     * @return true if it is ready, false otherwise
     */
    public boolean getReady() {
        return ready;
    }

    /**
     * Sets the ready status of collidable
     * @param ready true if the collidable is ready, false otherwise
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    /**
     * Gets the number of impulses stored in the collidable
     * @return The number of impulses
     */
    public int getImpulseCount(){
        return this.impulseCount;
    }

    
    
    
    
}
