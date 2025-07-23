package electrosphere.client.collision;

import org.joml.Vector3d;
import org.ode4j.ode.DContactGeom;
import org.ode4j.ode.DGeom;

import electrosphere.collision.collidable.Collidable;
import electrosphere.entity.Entity;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.state.attack.ClientAttackTree;
import electrosphere.entity.state.hitbox.HitboxCollectionState;
import electrosphere.entity.state.hitbox.HitboxCollectionState.HitboxState;
import electrosphere.entity.state.hitbox.HitboxCollectionState.HitboxType;

public class ClientLocalHitboxCollision {
    
    /**
     * Handles a damage collision on the client
     * @param impactor the entity initiating the collision
     * @param receiver the entity receiving the collision
     */
    public static void clientDamageHitboxColision(DContactGeom contactGeom, DGeom impactorGeom, DGeom receiverGeom, Collidable impactor, Collidable receiver, Vector3d normal, Vector3d localPosition, Vector3d worldPos, float magnitude){

        Entity impactorParent = impactor.getParent();
        Entity receiverParent = receiver.getParent();
        HitboxCollectionState impactorState = HitboxCollectionState.getHitboxState(impactorParent);
        HitboxCollectionState receiverState = HitboxCollectionState.getHitboxState(receiverParent);
        HitboxState impactorShapeStatus = impactorState.getShapeStatus(impactorGeom);
        HitboxState receiverShapeStatus = receiverState.getShapeStatus(receiverGeom);

        
        //currently, impactor needs to be an item, and the receiver must not be an item
        boolean isDamageEvent = 
            impactorShapeStatus != null &&
            receiverShapeStatus != null &&
            impactorShapeStatus.getType() == HitboxType.HIT &&
            receiverShapeStatus.getType() == HitboxType.HURT &&
            AttachUtils.getParent(impactorParent) != receiverParent
        ;

        if(impactorShapeStatus != null){
            impactorShapeStatus.setHadCollision(true);
        }
        if(receiverShapeStatus != null){
            receiverShapeStatus.setHadCollision(true);
        }
        
        if(isDamageEvent){
            if(AttachUtils.hasParent(impactorParent)){
                Entity parent = AttachUtils.getParent(impactorParent);
                if(ClientAttackTree.getClientAttackTree(parent) != null){
                    ClientAttackTree clientAttackTree = ClientAttackTree.getClientAttackTree(parent);
                    if(clientAttackTree.canCollideEntity(receiverParent)){
                        clientAttackTree.collideEntity(receiverParent);
                        clientAttackTree.freezeFrame();
                    }
                }
            }
        }

        // Entity hitboxParent = (Entity)impactor.getData(EntityDataStrings.COLLISION_ENTITY_DATA_PARENT);
        // Entity hurtboxParent = (Entity)receiver.getData(EntityDataStrings.COLLISION_ENTITY_DATA_PARENT);
        
        //if the entity is attached to is an item, we need to compare with the parent of the item
        //to make sure you don't stab yourself for instance
        // boolean isItem = ItemUtils.isItem(hitboxParent);//hitboxParent.containsKey(EntityDataStrings.ITEM_IS_ITEM);
        // Entity hitboxAttachParent = AttachUtils.getParent(hitboxParent);
        
        // if(isItem){
        //     if(hitboxAttachParent != hurtboxParent){
        //         Vector3d hurtboxPos = EntityUtils.getPosition(receiver);
        //         ParticleEffects.spawnBloodsplats(new Vector3f((float)hurtboxPos.x,(float)hurtboxPos.y,(float)hurtboxPos.z).add(0,0.1f,0), 20, 40);
        //     }
        // } else {

        //     //client no longer manages damage; however, keeping this code around for the moment to show how we
        //     //might approach adding client-side effects as soon as impact occurs (ie play a sound, shoot sparks, etc)
        //     //before the server responds with a valid collision event or not

        //     // int damage = 0;
        //     // //for entities using attacktree
        //     // if(CreatureUtils.clientGetAttackTree(hitboxParent) != null){
        //     //     damage = ItemUtils.getWeaponDataRaw(hitboxParent).getDamage();
        //     // } else {
        //     //     //for entities using shooter tree
        //     //     if(ProjectileTree.getProjectileTree(hitboxParent) != null){
        //     //         damage = (int)ProjectileTree.getProjectileTree(hitboxParent).getDamage();
        //     //     }
        //     // }
        //     // LifeUtils.getLifeState(hurtboxParent).damage(damage);
        //     // if(!LifeUtils.getLifeState(hurtboxParent).isIsAlive()){
        //     //     EntityUtils.getPosition(hurtboxParent).set(Globals.spawnPoint);
        //     //     LifeUtils.getLifeState(hurtboxParent).revive();
        //     // }
        // }
    }

}
