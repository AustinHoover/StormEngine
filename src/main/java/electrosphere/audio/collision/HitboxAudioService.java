package electrosphere.audio.collision;

import java.util.Random;

import org.joml.Vector3d;

import electrosphere.audio.VirtualAudioSourceManager.VirtualAudioSourceType;
import electrosphere.data.entity.collidable.HitboxData;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.logger.LoggerInterface;

/**
 * Client service to play audio when hitbox collisions happen
 */
public class HitboxAudioService {

    /**
     * Default audio files to play. Eventually should probably refactor into service that determines audio based on materials
     */
    static String[] defaultHitboxAudio = new String[]{
        "Audio/weapons/collisions/FleshWeaponHit1.wav",
        // "Audio/weapons/collisions/Massive Punch B.wav",
        // "Audio/weapons/collisions/Massive Punch C.wav",
    };

    /**
     * Default audio files to play. Eventually should probably refactor into service that determines audio based on materials
     */
    static String[] defaultBlockboxAudio = new String[]{
        "Audio/weapons/collisions/Sword Hit A.wav",
        "Audio/weapons/collisions/Sword Hit B.wav",
        "Audio/weapons/collisions/Sword Hit C.wav",
        "Audio/weapons/collisions/Sword Hit D.wav",
        "Audio/weapons/collisions/Sword Hit E.wav",
    };

    /**
     * The random for selecting which file to play
     */
    Random random = new Random();

    /**
     * Plays an interaction
     * @param voxelType The voxel type
     * @param type The interaction type
     */
    public void playAudio(Entity senderEntity, Entity receiverEntity, String hitboxType, String hurtboxType){
        String audioPath = this.getAudioPath(senderEntity, receiverEntity, hitboxType, hurtboxType);
        if(audioPath != null){
            Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(audioPath, VirtualAudioSourceType.CREATURE, false);
        }
    }

    /**
     * Plays an interaction at a given position
     * @param voxelType The voxel type
     * @param type The interaction type
     * @param position The position of the audio
     */
    public void playAudioPositional(Entity senderEntity, Entity receiverEntity, String hitboxType, String hurtboxType, Vector3d position){
        String audioPath = this.getAudioPath(senderEntity, receiverEntity, hitboxType, hurtboxType);
        if(audioPath != null && Globals.audioEngine != null){
            Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(audioPath, VirtualAudioSourceType.CREATURE, false, position);
        }
    }

    /**
     * Gets the audio file to play when the given entities collide
     * @param senderEntity The entity with the hitbox
     * @param receiverEntity The entity with the hurtbox
     * @param hitboxType The hitbox type
     * @param hurtboxType The hurthox type
     * @return The audio file to play
     */
    protected String getAudioPath(Entity senderEntity, Entity receiverEntity, String hitboxType, String hurtboxType){
        boolean isBlockSound = false;
        boolean isDamageSound = false;
        if(hitboxType == null){
            return null;
        }
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
                    return this.getWeaponOnBlock();
                } else if(isDamageSound){
                    return this.getWeaponOnCreature();
                }
            } else if(ItemUtils.isWeapon(receiverEntity)){
                if(isBlockSound){
                    return this.getWeaponOnBlock();
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
        return null;
    }

    /**
     * Gets the audio to play when a weapon collides with a creature
     * @return The audio file to play
     */
    private String getWeaponOnCreature(){
        //return the audio
        int roll = random.nextInt(defaultHitboxAudio.length);
        return defaultHitboxAudio[roll];
    }

    /**
     * Gets the audio to play when a weapon collides with a block box
     * @return The audio file to play
     */
    private String getWeaponOnBlock(){
        int roll = random.nextInt(defaultBlockboxAudio.length);
        return defaultBlockboxAudio[roll];
    }

}
