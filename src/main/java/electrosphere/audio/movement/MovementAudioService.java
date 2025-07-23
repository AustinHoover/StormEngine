package electrosphere.audio.movement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.joml.Vector3d;

import electrosphere.audio.VirtualAudioSourceManager.VirtualAudioSourceType;
import electrosphere.client.terrain.sampling.ClientVoxelSampler;
import electrosphere.data.audio.SurfaceAudioCollection;
import electrosphere.data.audio.SurfaceAudioType;
import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;

/**
 * Service that provides utilities and management for playing audio under different movement conditions
 */
public class MovementAudioService {

    /**
     * Different types of interactions with the surface
     */
    public static enum InteractionType {

        /**
         * A bare footstep
         */
        STEP_BARE_REG,

        /**
         * A bare, heavy footstep
         */
        STEP_BARE_HEAVY,

        /**
         * A shoed foostep
         */
        STEP_SHOE_REG,

        /**
         * A shoed, heavy foostep
         */
        STEP_SHOE_HEAVY,

        /**
         * Jumping from the surface
         */
        JUMP,

        /**
         * Landing on the surface
         */
        LAND,
    }
    
    //The default surface audio data
    SurfaceAudioType defaultSurfaceAudio;

    //Maps voxel types to the corresponding surface audio
    Map<Integer,SurfaceAudioType> surfaceAudioMap = new HashMap<Integer,SurfaceAudioType>();

    //The voxel types to ignore
    List<Integer> ignoredVoxelTypes;

    /**
     * The random used to pick an audio file
     */
    Random random = new Random();

    
    /**
     * Initializes the movement audio service
     */
    public void init(){
        SurfaceAudioCollection surfaceAudioCollection = Globals.gameConfigCurrent.getSurfaceAudioCollection();
        this.ignoredVoxelTypes = surfaceAudioCollection.getIgnoredVoxelTypes();
        this.defaultSurfaceAudio = surfaceAudioCollection.getDefaultSurfaceAudio();
        for(SurfaceAudioType audioType : surfaceAudioCollection.getSurfaceAudio()){
            for(int voxelType : audioType.getVoxelTypeIds()){
                if(surfaceAudioMap.containsKey(voxelType)){
                    LoggerInterface.loggerAudio.ERROR(new IllegalStateException("Duplicate voxel definitions in the surface audio definitions! " + voxelType));
                }
                surfaceAudioMap.put(voxelType,audioType);
            }
        }
        //queue all audio to be loaded
        this.loadSurfaceAudio(this.defaultSurfaceAudio);
        for(SurfaceAudioType audioType : surfaceAudioCollection.getSurfaceAudio()){
            this.loadSurfaceAudio(audioType);
        }
    }

    /**
     * Gets the audio path to play
     * @param voxelType The type of voxel
     * @param type The interaction type
     * @return The path to the audio file to play
     */
    public String getAudioPath(int voxelType, InteractionType type){
        String rVal = null;
        SurfaceAudioType surfaceAudio = this.defaultSurfaceAudio;

        if(voxelType == ClientVoxelSampler.INVALID_POSITION){
            return null;
        }

        //Check if ignored
        if(this.ignoredVoxelTypes != null){
            for(int ignoredVoxelType : this.ignoredVoxelTypes){
                if(ignoredVoxelType == voxelType){
                    return null;
                }
            }
        }

        //gets the surface audio definition
        if(surfaceAudioMap.containsKey(voxelType)){
            surfaceAudio = surfaceAudioMap.get(voxelType);
        } else {
            LoggerInterface.loggerAudio.WARNING("Surface undefined in the surface audio collection! " + voxelType);
        }

        //gets the list to pull from
        if(surfaceAudio != null){
            List<String> availableFiles = surfaceAudio.getFootstepRegularBareAudioPaths();
            switch(type){
                case STEP_BARE_REG: {
                    availableFiles = surfaceAudio.getFootstepRegularBareAudioPaths();
                } break;
                case STEP_BARE_HEAVY: {
                    availableFiles = surfaceAudio.getFootstepHeavyBareAudioPaths();
                } break;
                case STEP_SHOE_REG: {
                    availableFiles = surfaceAudio.getFootstepRegularShoeAudioPaths();
                } break;
                case STEP_SHOE_HEAVY: {
                    availableFiles = surfaceAudio.getFootstepHeavyShoeAudioPaths();
                } break;
                case JUMP: {
                    availableFiles = surfaceAudio.getJumpAudioPaths();
                } break;
                case LAND: {
                    availableFiles = surfaceAudio.getLandAudioPaths();
                } break;
            }
            int roll = random.nextInt(availableFiles.size());
            rVal = availableFiles.get(roll);
        }

        return rVal;
    }

    /**
     * Plays an interaction
     * @param voxelType The voxel type
     * @param type The interaction type
     */
    public void playAudio(int voxelType, InteractionType type){
        if(EngineState.EngineFlags.RUN_AUDIO){
            String audioPath = this.getAudioPath(voxelType, type);
            if(audioPath != null){
                Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(audioPath, VirtualAudioSourceType.CREATURE, false);
            }
        }
    }

    /**
     * Plays an interaction at a given position
     * @param voxelType The voxel type
     * @param type The interaction type
     * @param position The position of the audio
     */
    public void playAudioPositional(int voxelType, InteractionType type, Vector3d position){
        if(EngineState.EngineFlags.RUN_AUDIO){
            String audioPath = this.getAudioPath(voxelType, type);
            if(audioPath != null){
                Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(audioPath, VirtualAudioSourceType.CREATURE, false, position);
            }
        }
    }

    /**
     * Loads a given surface audio definition into memory
     * @param surfaceAudioType The surface audio definition
     */
    private void loadSurfaceAudio(SurfaceAudioType surfaceAudioType){
        for(String audioPath : surfaceAudioType.getFootstepRegularBareAudioPaths()){
            Globals.assetManager.addAudioPathToQueue(audioPath);
        }
        for(String audioPath : surfaceAudioType.getFootstepHeavyBareAudioPaths()){
            Globals.assetManager.addAudioPathToQueue(audioPath);
        }
        for(String audioPath : surfaceAudioType.getFootstepRegularShoeAudioPaths()){
            Globals.assetManager.addAudioPathToQueue(audioPath);
        }
        for(String audioPath : surfaceAudioType.getFootstepHeavyShoeAudioPaths()){
            Globals.assetManager.addAudioPathToQueue(audioPath);
        }
        for(String audioPath : surfaceAudioType.getJumpAudioPaths()){
            Globals.assetManager.addAudioPathToQueue(audioPath);
        }
        for(String audioPath : surfaceAudioType.getLandAudioPaths()){
            Globals.assetManager.addAudioPathToQueue(audioPath);
        }
    }

}
