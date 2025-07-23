package electrosphere.data.entity.projectile;

public class ProjectileType {
    

    String id;
    String modelPath;
    int maxLife;
    float velocity;
    float damage;
    float hitboxRadius;

    public String getId(){
        return id;
    }

    public String getModelPath(){
        return modelPath;
    }

    public int getMaxLife(){
        return maxLife;
    }

    public float getVelocity(){
        return velocity;
    }

    public float getDamage(){
        return damage;
    }

    public float getHitboxRadius(){
        return hitboxRadius;
    }

}
