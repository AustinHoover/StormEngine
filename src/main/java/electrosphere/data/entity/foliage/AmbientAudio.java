package electrosphere.data.entity.foliage;

/**
 * Parameters for ambient audio generation by this foliage
 */
public class AmbientAudio {

    //the path to the audio file to play in response to wind
    String responseWindAudioFilePath;
    //if true, the wind response will be set to loop
    boolean responseWindLoops;
    //if true, when it starts playing it will randomize where in the file it starts playing
    //this is useful for instance for trees starting to play audio where they aren't all playing the exact same file at the exact same point in time
    boolean randomizeOffset;
    //multiplies the gain by an amount
    float gainMultiplier;
    //the spatial offset from the origin of the attached entity to place the audio emitter
    float emitterSpatialOffset[];

    /**
     * the path to the audio file to play in response to wind
     * @return
     */
    public String getResponseWindAudioFilePath(){
        return responseWindAudioFilePath;
    }

    /**
     * if true, the wind response will be set to loop
     * @return
     */
    public boolean getResponseWindLoops(){
        return responseWindLoops;
    }

    /**
     * if true, when it starts playing it will randomize where in the file it starts playing
     * this is useful for instance for trees starting to play audio where they aren't all playing the exact same file at the exact same point in time
     * @return
     */
    public boolean getRandomizeOffset(){
        return randomizeOffset;
    }

    /**
     * multiplies the gain by an amount
     * @return
     */
    public float getGainMultiplier(){
        return gainMultiplier;
    }

    /**
     * The offset to place the emitter relative to the parent
     * @return The offset
     */
    public float[] getEmitterSpatialOffset(){
        return emitterSpatialOffset;
    }

}
