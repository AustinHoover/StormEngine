package electrosphere.client.collision;

import org.joml.Vector3d;

import electrosphere.data.entity.collidable.HitboxData;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.logger.LoggerInterface;

/**
 * Client methods for handling hitbox collisions reported by the server
 */
public class ClientNetworkHitboxCollision {
    
    /**
     * Performs client logic for a collision that the server reports
     * @param position The real-space position of the collision
     * @param hitboxType The type of hitbox
     * @param hurtboxType The type of hurtbox
     */
    public static void handleHitboxCollision(Entity senderEntity, Entity receiverEntity, Vector3d position, String hitboxType, String hurtboxType){
        switch(hitboxType){
            case HitboxData.HITBOX_TYPE_HIT_CONNECTED:
            case HitboxData.HITBOX_TYPE_HIT: {
                switch(hurtboxType){
                    case HitboxData.HITBOX_TYPE_HURT:
                    case HitboxData.HITBOX_TYPE_HURT_CONNECTED: {
                        Globals.audioEngine.hitboxAudioService.playAudioPositional(senderEntity, receiverEntity, hitboxType, hurtboxType, position);
                        ClientNetworkHitboxCollision.conditionallySpawnParticles(senderEntity, receiverEntity, hitboxType, hurtboxType, position);
                    } break;
                    case HitboxData.HITBOX_TYPE_BLOCK_CONNECTED: {
                        Globals.audioEngine.hitboxAudioService.playAudioPositional(senderEntity, receiverEntity, hitboxType, hurtboxType, position);
                        ClientNetworkHitboxCollision.conditionallySpawnParticles(senderEntity, receiverEntity, hitboxType, hurtboxType, position);
                    } break;
                    default: {
                        LoggerInterface.loggerEngine.WARNING("Client handling undefined hurtbox type: " + hurtboxType);
                    } break;
                }
            } break;
            default: {
                LoggerInterface.loggerEngine.WARNING("Client handling undefined hitbox type: " + hitboxType);
            } break;
        }
    }


    /**
     * Figures out what particles to spawn based on a collisionn
     * @param senderEntity The entity initiating the collision
     * @param receiverEntity The entity receiving the collision
     * @param hitboxType The hitbox type
     * @param hurtboxType The hurtbox type
     * @param position The position of the collision
     */
    private static void conditionallySpawnParticles(Entity senderEntity, Entity receiverEntity, String hitboxType, String hurtboxType, Vector3d position){
        boolean isBlockSound = false;
        boolean isDamageSound = false;
        switch(hitboxType){
            case HitboxData.HITBOX_TYPE_HIT:
            case HitboxData.HITBOX_TYPE_HIT_CONNECTED: {
                switch(hurtboxType){
                    case HitboxData.HITBOX_TYPE_HIT:
                    case HitboxData.HITBOX_TYPE_HIT_CONNECTED: {
                        isBlockSound = true;
                    } break;
                    case HitboxData.HITBOX_TYPE_BLOCK_CONNECTED: {
                        isBlockSound = true;
                    } break;
                    case HitboxData.HITBOX_TYPE_HURT:
                    case HitboxData.HITBOX_TYPE_HURT_CONNECTED: {
                        isDamageSound = true;
                    } break;
                }
            } break;
            case HitboxData.HITBOX_TYPE_BLOCK_CONNECTED: {
                switch(hurtboxType){
                    case HitboxData.HITBOX_TYPE_HIT:
                    case HitboxData.HITBOX_TYPE_HIT_CONNECTED: {
                        isBlockSound = true;
                    } break;
                    case HitboxData.HITBOX_TYPE_BLOCK_CONNECTED: {
                        isBlockSound = true;
                    } break;
                    case HitboxData.HITBOX_TYPE_HURT:
                    case HitboxData.HITBOX_TYPE_HURT_CONNECTED: {
        
                    } break;
                }
            } break;
            case HitboxData.HITBOX_TYPE_HURT:
            case HitboxData.HITBOX_TYPE_HURT_CONNECTED: {

            } break;
        }
        if(ItemUtils.isWeapon(senderEntity)){
            if(CreatureUtils.isCreature(receiverEntity)){
                if(isBlockSound){
                    //TODO: handle
                } else if(isDamageSound){
                    // ParticleEffects.spawnBloodsplats(position);
                }
            } else if(ItemUtils.isWeapon(receiverEntity)){
                if(isBlockSound){
                    //TODO: handle
                }
            } else {
                String message = "Getting audio for unhandled hurtbox collision type!\n" + 
                "Is creature: " + CreatureUtils.isCreature(receiverEntity) + "\n" +
                "Is item: " + ItemUtils.isItem(receiverEntity) + "\n" +
                "Is weapon: " + ItemUtils.isWeapon(receiverEntity)
                ;
                if(ItemUtils.isItem(receiverEntity)){
                    message = message + "\nItem Type: " + ItemUtils.getType(receiverEntity);
                }
                LoggerInterface.loggerEngine.WARNING(message);
            }
        } else {
            LoggerInterface.loggerEngine.WARNING("Getting audio for unhandled hitbox collision type!");
        }
    }

}
