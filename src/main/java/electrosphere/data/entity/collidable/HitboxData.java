package electrosphere.data.entity.collidable;

import java.util.List;

import electrosphere.collision.hitbox.HitboxUtils.HitboxPositionCallback;
import electrosphere.entity.Entity;

/**
 * Data about a hitbox
 */
public class HitboxData {

    /**
     * A hitbox sphere that teleports to its new position between frames
     */
    public static final String HITBOX_TYPE_HIT = "hit";

    /**
     * A hurtbox sphere that teleports to its new position between frames
     */
    public static final String HITBOX_TYPE_HURT = "hurt";
    
    /**
     * A hitbox sphere that is connected to its previous position by a capsule. The capsule is used for collision checks
     */
    public static final String HITBOX_TYPE_HIT_CONNECTED = "hit_connected";

    /**
     * A hurtbox sphere that is connected to its previous position by a capsule. The capsule is used for collision checks
     */
    public static final String HITBOX_TYPE_HURT_CONNECTED = "hurt_connected";

    /**
     * A block sphere that is connected to its previous position by a capsule. The capsule is used for collision checks
     */
    public static final String HITBOX_TYPE_BLOCK_CONNECTED = "block_connected";

    /**
     * A hitbox with extra effect (ie more damage)
     */
    public static final String HITBOX_SUBTYPE_SWEET = "sweet";

    /**
     * A hitbox with normal effect
     */
    public static final String HITBOX_SUBTYPE_REUGLAR = "regular";

    /**
     * A hitbox with less effect (ie reduced damange)
     */
    public static final String HITBOX_SUBTYPE_SOUR = "sour";
    

    /**
     * Used for debugging -- to show whether a hitbox is colliding with it or not
     */
    public static final String HITBOX_TYPE_STATIC_CAPSULE = "static_capsule";

    /**
     * The type of hitbox
     */
    String type;

    /**
     * The subtype of hitbox (ie, sweetspot, sour spot, critical spot, armor spot, etc)
     */
    String subType;

    /**
     * The bone it is attached to
     */
    String bone;

    /**
     * The radius of the hitbox
     */
    float radius;

    /**
     * The length of a static capsule hitbox
     */
    float length;

    /**
     * Controls whether the hitbox is active or not
     */
    boolean active = false;

    /**
     * Override when block state is active. Used to make hitboxes function as blockboxes while blocking
     */
    boolean blockOverride = false;

    /**
     * Used for more advanced hitbox spawning to find hitbox position on frame update
     */
    HitboxPositionCallback positionCallback;

    /**
     * Used to filter this hitbox to hitting only certain parent entities
     */
    List<Entity> filter;

    /**
     * The offset from the bone
     */
    List<Double> offset;

    /**
     * The damage this hitbox can do (ie if it is tied directly to a creature)
     */
    int damage;

    /**
     * Gets the type of hitbox
     * @return the type of hitbox
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the subtype of the hitbox
     * @return the subtype of hitbox
     */
    public String getSubType() {
        return subType;
    }

    /**
     * Gets the type of bone
     * @return the type of bone
     */
    public String getBone() {
        return bone;
    }
    
    /**
     * Gets the radius of the hitbox
     * @return the radius of the hitbox
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Gets the length of hitbox if applicable
     * @return The length
     */
    public float getLength(){
        return length;
    }

    /**
     * Returns whether the hitbox is active or not
     * @return true if the hitbox is active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Toggles the active status
     * @param active if true, the hitbox will be active, if false the hitbox will be inactive
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Gets the block override status
     * @return true if should override hitboxes with block, false otherwise
     */
    public boolean isBlockOverride(){
        return blockOverride;
    }

    /**
     * Sets the status of the block override
     * @param blockOverride true if should override hitboxes with block, false otherwise
     */
    public void setBlockOverride(boolean blockOverride){
        this.blockOverride = blockOverride;
    }

    /**
     * Sets the bone this hitbox is attached to
     * @param bone the bone to attach the hitbox to
     */
    public void setBone(String bone) {
        this.bone = bone;
    }

    /**
     * Sets the type of the hitbox
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Sets the subtype of the hitbox
     * @param subType the subtype
     */
    public void setSubType(String subType) {
        this.subType = subType;
    }

    /**
     * Sets the radius of the hitbox
     * @param radius The radius
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }

    /**
     * Gets the position callback
     * @return The position callback
     */
    public HitboxPositionCallback getPositionCallback(){
        return positionCallback;
    }

    /**
     * Sets the position callback
     * @param positionCallback The position callback
     */
    public void setPositionCallback(HitboxPositionCallback positionCallback){
        this.positionCallback = positionCallback;
    }

    /**
     * Sets an entity filter on the hitbox
     * @param filter The list of parent entities to exclude from collisions
     */
    public void setEntityFilter(List<Entity> filter){
        this.filter = filter;
    }

    /**
     * Gets the entity filter
     * @return The list of parent entities to exclude from collisions
     */
    public List<Entity> getEntityFilter(){
        return filter;
    }

    /**
     * Gets the offset for the hitbox
     * @return The offset
     */
    public List<Double> getOffset(){
        return this.offset;
    }

    /**
     * Sets the offset for the hitbox
     * @param offset The offset
     */
    public void setOffset(List<Double> offset){
        this.offset = offset;
    }

    /**
     * Gets the damage of this hitbox
     * @return The damage
     */
    public int getDamage(){
        return this.damage;
    }
    
    
}
