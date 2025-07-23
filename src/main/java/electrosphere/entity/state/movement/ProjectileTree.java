package electrosphere.entity.state.movement;

import org.joml.Vector3d;

import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;

public class ProjectileTree implements BehaviorTree {

    Entity parent;
    int maxLife;
    int lifeCurrent = 0;
    float damage = 0.0f;
    Vector3d vector;

    public ProjectileTree(Entity parent, int maxLife, Vector3d initialVector, float velocity){
        this.parent = parent;
        this.maxLife = maxLife;
        this.vector = initialVector.mul(velocity);
    }

    /**
     * Used when spawning projectiles from projectiles.json file
     * Separate constructor so can include damage value
     * @param parent Parent entity of the projectile
     * @param maxLife Max flight time of the projectile in frames
     * @param initialVector The initial velocity vector of the projectile
     * @param velocity The intiial velocity magnitude of the projectile
     * @param damage The damage of the projectile
     */
    public ProjectileTree(Entity parent, int maxLife, Vector3d initialVector, float velocity, float damage){
        this.parent = parent;
        this.maxLife = maxLife;
        this.vector = initialVector.mul(velocity);
        this.damage = damage;
    }

    @Override
    public void simulate(float deltaTime) {

        lifeCurrent++;
        if(lifeCurrent >= maxLife){
            // EntityUtils.cleanUpEntity(parent);
            ServerBehaviorTreeUtils.detatchBTreeFromEntity(parent,this);
        }

        Vector3d positionCurrent = EntityUtils.getPosition(parent);
        positionCurrent.add(vector);

    }

    /**
     * Only relevant when spawned from projectiles.json file
     * @return The damage of the projectile as defined in projectiles.json
     */
    public float getDamage(){
        return damage;
    }

    /**
     * Sets the projectile tree of a given entity
     * @param entity The entity
     * @param tree The projectile tree
     */
    public static void setProjectileTree(Entity entity, ProjectileTree tree){
        entity.putData(EntityDataStrings.PROJECTILE_TREE, tree);
    }

    /**
     * Gets the projectile tree of a given entity
     * @param entity The entity
     * @return The projectile tree
     */
    public static ProjectileTree getProjectileTree(Entity entity){
        return (ProjectileTree)entity.getData(EntityDataStrings.PROJECTILE_TREE);
    }
    
}
