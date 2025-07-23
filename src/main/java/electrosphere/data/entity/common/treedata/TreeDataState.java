package electrosphere.data.entity.common.treedata;

/**
 * A simple tree state
 */
public class TreeDataState {
    
    /**
     * The animation to play for the tree's state
     */
    TreeDataAnimation animation;

    /**
     * The audio to play when running this state of the tree
     */
    TreeDataAudio audioData;

    /**
     * Gets the animation data
     * @return The animation data if it exists, null otherwise
     */
    public TreeDataAnimation getAnimation(){
        return animation;
    }

    /**
     * Gets the audio data
     * @return The audio data if it exists, null otherwise
     */
    public TreeDataAudio getAudioData(){
        return audioData;
    }

}
