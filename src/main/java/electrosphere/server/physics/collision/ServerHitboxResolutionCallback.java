package electrosphere.server.physics.collision;

import org.joml.Vector3d;
import org.ode4j.ode.DContactGeom;
import org.ode4j.ode.DGeom;

import electrosphere.collision.CollisionEngine.CollisionResolutionCallback;
import electrosphere.collision.collidable.Collidable;
import electrosphere.entity.Entity;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.state.attack.ServerAttackTree;
import electrosphere.entity.state.hitbox.HitboxCollectionState;
import electrosphere.entity.state.hitbox.HitboxCollectionState.HitboxState;
import electrosphere.entity.state.hitbox.HitboxCollectionState.HitboxType;
import electrosphere.entity.state.life.ServerLifeTree;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.logger.LoggerInterface;

/**
 * Callback for managing collisions on the server
 */
public class ServerHitboxResolutionCallback implements CollisionResolutionCallback {

    @Override
    public void resolve(DContactGeom contactGeom, DGeom impactorGeom, DGeom receiverGeom, Collidable impactor, Collidable receiver, Vector3d normal, Vector3d localPosition, Vector3d worldPos, float magnitude) {
        Entity impactorEntity = impactor.getParent();
        Entity receiverEntity = receiver.getParent();
        HitboxCollectionState impactorState = HitboxCollectionState.getHitboxState(impactorEntity);
        HitboxCollectionState receiverState = HitboxCollectionState.getHitboxState(receiverEntity);
        HitboxState impactorShapeStatus = impactorState.getShapeStatus(impactorGeom);
        HitboxState receiverShapeStatus = receiverState.getShapeStatus(receiverGeom);

        //basic error checking
        if(impactorEntity == null){
            throw new Error("Impactor's entity is null");
        }
        if(receiverEntity == null){
            throw new Error("Receiver's entity is null");
        }
        if(!HitboxCollectionState.hasHitboxState(impactorEntity)){
            throw new Error("Impactor state is null");
        }
        if(!HitboxCollectionState.hasHitboxState(receiverEntity)){
            throw new Error("Receiver state is null");
        }
        if(impactorGeom == null){
            throw new Error("Impactor geom is null");
        }
        if(receiverGeom == null){
            throw new Error("Receiver geom is null");
        }
        if(!impactorState.getGeometries().contains(impactorGeom)){
            boolean receiverStateContainsGeom = receiverState.getGeometries().contains(impactorGeom);
            String message = "Impactor geom has wrong parent assigned!\n" +
            "Problem geom: " + impactorGeom + "\n" +
            "All geometries tracked: " + impactorState.getGeometries() + "\n" +
            "Receiver contains impactor: " + receiverStateContainsGeom + "\n"
            ;
            throw new Error(message);
        }
        if(impactorShapeStatus == null){
            String message = "Impactor shape status is null\n" +
            "Problem geom: " + impactorGeom + "\n" +
            "All geometries tracked: " + impactorState.getGeometries() + "n\""
            ;
            throw new Error(message);
        }
        if(receiverShapeStatus == null){
            String message = "Receiver shape status is null\n" +
            "Problem geom: " + receiverGeom + "\n" +
            "All geometries tracked: " + receiverState.getGeometries() + "n\""
            ;
            throw new Error(message);
        }

        
        boolean impactorShapeStatusIsNull = impactorShapeStatus == null;
        boolean receiverShapeStatusIsNull = receiverShapeStatus == null;
        boolean impactorIsHit = impactorShapeStatus != null && impactorShapeStatus.getType() == HitboxType.HIT;
        boolean receiverIsHurt = receiverShapeStatus != null && receiverShapeStatus.getType() == HitboxType.HURT;
        boolean receiverIsBlock = receiverShapeStatus != null && (receiverShapeStatus.getType() == HitboxType.BLOCK || (receiverShapeStatus.getType() == HitboxType.HIT && receiverShapeStatus.isBlockOverride()));
        boolean parentsAreDifferent = AttachUtils.getParent(impactorEntity) != receiverEntity;
        boolean impactorCollisionBlocked = false;

        //check if the impactor thinks it can collide with the receiver

        //
        //sword has attack tree
        if(impactorEntity != null && ServerAttackTree.getServerAttackTree(impactorEntity) != null){
            ServerAttackTree impactorAttackTree = ServerAttackTree.getServerAttackTree(impactorEntity);

            //
            //sword-on-sword check
            //if we collide with the creature directly
            if(!impactorAttackTree.canCollideEntity(receiverEntity)){
                impactorCollisionBlocked = true;
            }

            //
            //sword-on-creature check
            //if we collide with an item attached to the creature
            if(AttachUtils.hasParent(receiverEntity) && !impactorAttackTree.canCollideEntity(AttachUtils.getParent(receiverEntity))){
                impactorCollisionBlocked = true;
            }

        //
        //creature has attack tree
        } else if(impactorEntity != null && AttachUtils.hasParent(impactorEntity) && AttachUtils.getParent(impactorEntity) != null && ServerAttackTree.getServerAttackTree(AttachUtils.getParent(impactorEntity)) != null){
            ServerAttackTree impactorAttackTree = ServerAttackTree.getServerAttackTree(AttachUtils.getParent(impactorEntity));

            //
            //creature-on-sword check
            //if we collide with the creature directly
            if(!impactorAttackTree.canCollideEntity(receiverEntity)){
                impactorCollisionBlocked = true;
            }

            //
            //creature-on-creature check
            //if we collide with an item attached to the creature
            if(AttachUtils.hasParent(receiverEntity) && !impactorAttackTree.canCollideEntity(AttachUtils.getParent(receiverEntity))){
                impactorCollisionBlocked = true;
            }
        }

        //
        //check if is damage event
        boolean isDamageEvent = 
            !impactorShapeStatusIsNull &&
            !receiverShapeStatusIsNull &&
            impactorIsHit &&
            receiverIsHurt &&
            parentsAreDifferent &&
            !impactorCollisionBlocked
        ;

        //
        //check if is block event
        boolean isBlockEvent = 
            !impactorShapeStatusIsNull &&
            !receiverShapeStatusIsNull &&
            impactorIsHit &&
            receiverIsBlock &&
            parentsAreDifferent &&
            !impactorCollisionBlocked
        ;
        

        if(!impactorShapeStatusIsNull){
            impactorShapeStatus.setHadCollision(true);
        }
        if(!receiverShapeStatusIsNull){
            receiverShapeStatus.setHadCollision(true);
        }

        if(isDamageEvent){
            //if the entity is attached to is an item, we need to compare with the parent of the item
            //to make sure you don't stab yourself for instance
            boolean isItem = ItemUtils.isItem(impactorEntity);
            Entity hitboxAttachParent = AttachUtils.getParent(impactorEntity);

            //if the impactor is the creature itself, need separate handling
            boolean isCreature = CreatureUtils.isCreature(impactorEntity);

            //
            //handle receiver
            if(isItem){
                if(hitboxAttachParent != receiverEntity){
                    ServerLifeTree serverLifeTree = ServerLifeTree.getServerLifeTree(receiverEntity);
                    if(serverLifeTree != null){
                        serverLifeTree.addCollisionEvent(impactorEntity, impactorShapeStatus, receiverShapeStatus, worldPos, isDamageEvent, isBlockEvent);
                    }
                }
            } else if(isCreature){
                ServerLifeTree serverLifeTree = ServerLifeTree.getServerLifeTree(receiverEntity);
                if(serverLifeTree != null){
                    serverLifeTree.addCollisionEvent(impactorEntity, impactorShapeStatus, receiverShapeStatus, worldPos, isDamageEvent, isBlockEvent);
                }
            }

            //
            //handle attacker
            this.handleAttackerCollision(impactorEntity,receiverEntity, isBlockEvent);
        }

        if(isBlockEvent){
            //
            //handle receiver
            boolean receiverIsItem = ItemUtils.isItem(receiverEntity);
            boolean receiverHasParent = AttachUtils.hasParent(receiverEntity);
            if(receiverIsItem && receiverHasParent){
                //item is equipped to something
                ServerLifeTree serverLifeTree = ServerLifeTree.getServerLifeTree(AttachUtils.getParent(receiverEntity));
                if(serverLifeTree != null){
                    serverLifeTree.addCollisionEvent(impactorEntity, impactorShapeStatus, receiverShapeStatus, worldPos, isDamageEvent, isBlockEvent);
                }
            } else {
                //attacking an item that is not equipped to anything
                ServerLifeTree serverLifeTree = ServerLifeTree.getServerLifeTree(receiverEntity);
                if(serverLifeTree != null){
                    serverLifeTree.addCollisionEvent(impactorEntity, impactorShapeStatus, receiverShapeStatus, worldPos, isDamageEvent, isBlockEvent);
                }
            }
            
            //
            //handle attacker
            this.handleAttackerCollision(impactorEntity,receiverEntity, isBlockEvent);
        }
    }

    /**
     * Handles collision tracking from the impactor's side
     * @param impactorEntity The impactor hitbox's parent entity
     * @param receiverParent The receiver hitbox's parent entity
     * @param isBlock true if this is a block, false otherwise
     */
    private void handleAttackerCollision(Entity impactorEntity, Entity receiverEntity, boolean isBlock){
        boolean receiverIsItem = ItemUtils.isItem(receiverEntity);
        boolean receiverHasParent = AttachUtils.hasParent(receiverEntity);

        //
        //The sword has the attack tree
        if(impactorEntity != null && ServerAttackTree.getServerAttackTree(impactorEntity) != null){
            ServerAttackTree impactorAttackTree = ServerAttackTree.getServerAttackTree(impactorEntity);
            impactorAttackTree.collideEntity(receiverEntity);
            if(isBlock){
                impactorAttackTree.recoilFromBlock();
            }
            //if the receiver is an item that is equipped, collide with parent too
            if(receiverIsItem && receiverHasParent){
                impactorAttackTree.collideEntity(AttachUtils.getParent(receiverEntity));
            } else if(receiverHasParent){
                LoggerInterface.loggerEngine.WARNING("Potentially unhandled case with server collision!");
            }

        //
        //The parent of the sword has the attack tree
        } else if(impactorEntity != null && AttachUtils.hasParent(impactorEntity) && AttachUtils.getParent(impactorEntity) != null && ServerAttackTree.getServerAttackTree(AttachUtils.getParent(impactorEntity)) != null){
            ServerAttackTree impactorAttackTree = ServerAttackTree.getServerAttackTree(AttachUtils.getParent(impactorEntity));
            impactorAttackTree.collideEntity(receiverEntity);
            if(isBlock){
                impactorAttackTree.recoilFromBlock();
            }
            //if the receiver is an item that is equipped, collide with parent too
            if(receiverIsItem && receiverHasParent){
                impactorAttackTree.collideEntity(AttachUtils.getParent(receiverEntity));
            } else if(receiverHasParent){
                LoggerInterface.loggerEngine.WARNING("Potentially unhandled case with server collision!");
            }
        }
    }
    
}
