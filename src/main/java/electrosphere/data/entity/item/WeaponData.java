package electrosphere.data.entity.item;

import java.util.List;

import electrosphere.data.entity.collidable.HitboxData;

/**
 * Data about a weapon
 */
public class WeaponData {
    
    //the class of weapon (ie sword, bow, etc)
    String weaponClass;
    
    //the hitboxes associated with the weapon
    List<HitboxData> hitboxes;

    //the damage the weapon does
    int damage;

    //the model for the projectile
    String projectileModel;

    //The movespeed penalty applied when this weapon is used to perform an action
    Double weaponActionMovePenalty;

    //The base move penalty applied when having the weapon equipped in the first place
    Double weaponBaseMovePenalty;

    /**
     * Gets the weapon class
     * @return the weapon class
     */
    public String getWeaponClass(){
        return weaponClass;
    }

    /**
     * Gets the list of hitbox data
     * @return the list of hitbox data
     */
    public List<HitboxData> getHitboxes(){
        return hitboxes;
    }

    /**
     * Gets the projectile model
     * @return the projectile model
     */
    public String getProjectileModel(){
        return projectileModel;
    }

    /**
     * Gets the damage dealt
     * @return the damage dealt
     */
    public int getDamage(){
        return damage;
    }

    /**
     * Gets the movement penalty (a percentage) applied when an action is performed with this weapon
     * @return The movement penalty percentage (ie 0.7 means you should be 70% as fast)
     */
    public Double getWeaponActionMovePenalty(){
        return weaponActionMovePenalty;
    }

    /**
     * Gets the movement penalty (a percentage) applied when this weapon is equipped
     * @return The movementy penalty percentage (ie a 0.7 means you should be 70% as fast)
     */
    public Double getWeaponBaseMovePenalty(){
        return weaponBaseMovePenalty;
    }

}
