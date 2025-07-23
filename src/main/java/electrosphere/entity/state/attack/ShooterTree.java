package electrosphere.entity.state.attack;

import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.projectile.ProjectileUtils;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.utils.EntityLookupUtils;

public class ShooterTree implements BehaviorTree {

    public static enum ShooterTreeState {
        ATTACK,
        COOLDOWN,
        IDLE,
    }

    ShooterTreeState state;

    Entity parent;

    int ammoAvailable = 1;
    int ammoMax = 1;

    int cooldownCurrent = 0;
    int cooldownMax = 4;

    public ShooterTree(Entity parent){
        this.parent = parent;
        this.state = ShooterTreeState.IDLE;
    }

    @Override
    public void simulate(float deltaTime) {
        switch(state){
            case ATTACK: {
                //
                Vector3d parentPosition = EntityUtils.getPosition(parent);
                Vector3d movementDir = CreatureUtils.getFacingVector(parent);
                if(EntityLookupUtils.isServerEntity(parent)){
                    Realm parentRealm = Globals.serverState.realmManager.getEntityRealm(parent);
                    ProjectileUtils.serverSpawnProjectile(parentRealm, "missile1", parentPosition, new Vector3d(movementDir),parent);
                } else {
                    ProjectileUtils.clientSpawnProjectile("missile1", parentPosition, new Vector3d(movementDir),parent);
                }
                this.state = ShooterTreeState.COOLDOWN;
                cooldownCurrent = cooldownMax;
            } break;
            case COOLDOWN: {
                //
                if(cooldownCurrent > 0){
                    cooldownCurrent--;
                } else {
                    state = ShooterTreeState.IDLE;
                }
            } break;
            case IDLE: {
                //
            } break;
        }
    }

    public void fire(){
        boolean canFire = true;
        if(ammoAvailable <= 0){
            canFire = false;
        }
        if(state != ShooterTreeState.IDLE){
            canFire = false;
        }
        if(canFire){
            //fire
            this.state = ShooterTreeState.ATTACK;
        }
    }

    public static void setShooterTree(Entity entity, ShooterTree shooterTree){
        entity.putData(EntityDataStrings.SHOOTER_TREE, shooterTree);
    }

    public static ShooterTree getShooterTree(Entity entity){
        return (ShooterTree)entity.getData(EntityDataStrings.SHOOTER_TREE);
    }
    
}
