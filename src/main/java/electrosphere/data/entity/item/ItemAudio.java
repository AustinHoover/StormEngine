package electrosphere.data.entity.item;

/**
 * Audio data related to the item
 */
public class ItemAudio {
    
    /**
     * The audio to play on grabbing the item icon in the ui
     */
    String uiGrabAudio;

    /**
     * The audio to play on releasing the item icon in the ui
     */
    String uiReleaseAudio;

    /**
     * Gets the audio to play on grabbing the item icon in the ui
     * @return The audio path if it exists, null otherwise
     */
    public String getUIGrabAudio(){
        return uiGrabAudio;
    }

    /**
     * Gets the audio to play on releasing the item icon in the ui
     * @return The audio path if it exists, null otherwise
     */
    public String getUIReleaseAudio(){
        return uiReleaseAudio;
    }

}
