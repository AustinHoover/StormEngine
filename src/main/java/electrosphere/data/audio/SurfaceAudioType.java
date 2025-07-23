package electrosphere.data.audio;

import java.util.List;

/**
 * Audio data for a specific surface type
 */
public class SurfaceAudioType {
    
    /**
     * The voxel types that this audio applies to
     */
    List<Integer> voxelTypeIds;

    /**
     * The audio paths for sound effects to play for regular footsteps on these voxel types
     */
    List<String> footstepRegularBareAudioPaths;

    /**
     * The audio paths for sound effects to play for heavy footsteps on these voxel types
     */
    List<String> footstepHeavyBareAudioPaths;

    /**
     * The audio paths for sound effects to play for regular footsteps on these voxel types
     */
    List<String> footstepRegularShoeAudioPaths;

    /**
     * The audio paths for sound effects to play for heavy footsteps on these voxel types
     */
    List<String> footstepHeavyShoeAudioPaths;

    /**
     * The audio paths for sound effects to play for jumps beginning on these voxel types
     */
    List<String> jumpAudioPaths;

    /**
     * The audio paths for sound effects to play for landing animations on these voxel types
     */
    List<String> landAudioPaths;

    /**
     * Gets the voxels ids that use this audio
     * @return The list of voxel ids
     */
    public List<Integer> getVoxelTypeIds(){
        return voxelTypeIds;
    }

    /**
     * Gets the audio to play for regular, bare footsteps on these surfaces
     * @return The audio file paths
     */
    public List<String> getFootstepRegularBareAudioPaths(){
        return footstepRegularBareAudioPaths;
    }

    /**
     * Gets the audio to play for heavy, bare footsteps on these surfaces
     * @return The audio file paths
     */
    public List<String> getFootstepHeavyBareAudioPaths(){
        return footstepHeavyBareAudioPaths;
    }

    /**
     * Gets the audio to play for regular, shoed footsteps on these surfaces
     * @return The audio file paths
     */
    public List<String> getFootstepRegularShoeAudioPaths(){
        return footstepRegularShoeAudioPaths;
    }

    /**
     * Gets the audio to play for heavy, shoed footsteps on these surfaces
     * @return The audio file paths
     */
    public List<String> getFootstepHeavyShoeAudioPaths(){
        return footstepHeavyShoeAudioPaths;
    }

    /**
     * Gets the audio to play for jumping from these surfaces
     * @return The audio file paths
     */
    public List<String> getJumpAudioPaths(){
        return jumpAudioPaths;
    }

    /**
     * Gets the audio to play for landing on these surfaces
     * @return The audio file paths
     */
    public List<String> getLandAudioPaths(){
        return landAudioPaths;
    }

}
