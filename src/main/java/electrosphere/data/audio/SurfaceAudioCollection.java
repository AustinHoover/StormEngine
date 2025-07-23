package electrosphere.data.audio;

import java.util.List;

/**
 * Audio for different types of interactions with the ground
 */
public class SurfaceAudioCollection {
    
    /**
     * The audio to play by default (ie for undefined surfaces)
     */
    SurfaceAudioType defaultSurfaceAudio;

    /**
     * The audio to play for different collections of surfaces
     */
    List<SurfaceAudioType> surfaceAudio;

    /**
     * The voxel types to not play audio for
     */
    List<Integer> ignoredVoxelTypes;

    /**
     * Gets the default audio for all surfaces
     * @return The default audio
     */
    public SurfaceAudioType getDefaultSurfaceAudio(){
        return this.defaultSurfaceAudio;
    }

    /**
     * Gets the audio to play for different surfaces
     * @return The list of surface audio data
     */
    public List<SurfaceAudioType> getSurfaceAudio(){
        return this.surfaceAudio;
    }

    /**
     * Gets the list of voxel types to not play audio for
     * @return The list of voxel types
     */
    public List<Integer> getIgnoredVoxelTypes(){
        return this.ignoredVoxelTypes;
    }

}
