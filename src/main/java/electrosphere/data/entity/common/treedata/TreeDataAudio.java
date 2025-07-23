package electrosphere.data.entity.common.treedata;

/**
 * Audio data to use when running a given tree state
 */
public class TreeDataAudio {
    
    /**
     * The path to the audio file to play if it exists, null otherwise
     */
    String audioPath;


    /**
     * Gets the audio path
     */
    public String getAudioPath(){
        return audioPath;
    }

}
