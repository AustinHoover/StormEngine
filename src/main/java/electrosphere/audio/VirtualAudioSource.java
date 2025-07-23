package electrosphere.audio;

import org.joml.Vector3d;

import electrosphere.audio.VirtualAudioSourceManager.VirtualAudioSourceType;
import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;

/**
 * Represents an audio emitter that is being tracked by the game engine. Does not map 1-to-1 with audio that is actually played.
 * This allows the engine to have 500 "audio sources" while only having a select few actually emit audio.
 */
public class VirtualAudioSource implements Comparable<VirtualAudioSource> {

    //The priority of this virtual audio source
    int priority = 100;

    //the gain to play this audio source at
    float gain = 1.0f;

    //the path of the audio source
    String filePath;

    //whether the audio source loops or not
    boolean loops = false;

    //the position of the audio source
    Vector3d position = null;

    //the total time this audio source has played
    float totalTimePlayed = 0;

    //the rate to raise lower gain each frame
    float fadeRate = 0;

    //the modifier applied to gain based on fade rate
    float fadeModifier = 1.0f;

    //The type of virtual audio source
    VirtualAudioSourceType type;

    /**
     * Plays an absolute (non-relative to listener) audio
     * @param filePath The filepath of the audio source
     * @param type The type of virtual audio source
     * @param loops if true, loop the audio source
     */
    protected VirtualAudioSource(String filePath, VirtualAudioSourceType type, boolean loops){
        this.filePath = filePath;
        this.type = type;
        this.loops = loops;
    }

    /**
     * Plays an relative audio source
     * @param filePath The filepath of the audio source
     * @param type The type of virtual audio source
     * @param loops of true, loop the audio source
     * @param position The position to play the audio source at
     */
    protected VirtualAudioSource(String filePath, VirtualAudioSourceType type, boolean loops, Vector3d position){
        this.filePath = filePath;
        this.type = type;
        this.loops = loops;
        this.position = position;
    }

    /**
     * Updates the audio source's current state
     * @param deltaTime the time that has elapsed since the last frame
     * @return true if the source is still playing, false otherwise
     */
    protected boolean update(float deltaTime){
        boolean isStillPlaying = true;
        this.totalTimePlayed += deltaTime;
        AudioBuffer buffer = Globals.assetManager.fetchAudio(filePath);
        // LoggerInterface.loggerAudio.DEBUG("Increment virtual audio source " + deltaTime);
        if(buffer != null){
            if(this.totalTimePlayed >= buffer.getLength()){
                if(loops){
                    this.totalTimePlayed = this.totalTimePlayed % buffer.getLength();
                } else {
                    isStillPlaying = false;
                    LoggerInterface.loggerAudio.DEBUG("Virtual Audio Source Timeout " + totalTimePlayed + " > " + buffer.getLength());
                }
            }
        }
        if(gain <= 0){
            LoggerInterface.loggerAudio.DEBUG("Virtual Audio Source Gainout " + gain);
            isStillPlaying = false;
        }
        //gradually fade sources
        if(fadeRate == 0){

        } else if(fadeRate < 0){
            this.fadeModifier += fadeRate;
            if(this.fadeModifier < 0){
                isStillPlaying = false;
                fadeRate = 0;
                this.fadeModifier = 1.0f;
            }
        } else if(fadeRate == 1){
            this.fadeModifier = 1.0f;
            this.fadeRate = 0;
        } else if(fadeRate > 0){
            this.fadeModifier += fadeRate;
            if(this.fadeModifier > 1){
                this.fadeModifier = 1;
                this.fadeRate = 0;
            }
        }
        return isStillPlaying;
    }

    /**
     * Sets the priority of this virtual audio source
     * @param priority The priority
     */
    public void setPriority(int priority){
        this.priority = priority;
    }

    /**
     * Sets the gain of this audio source
     * @param gain The gain
     */
    public void setGain(float gain){
        this.gain = gain;
    }

    /**
     * Sets the position of this virtual source
     * @param position The position of this source
     */
    public void setPosition(Vector3d position){
        this.position.set(position);
    }

    /**
     * Sets the total time that this virtual audio source has played
     * @param totalTimePlayed The time
     */
    public void setTotalTimePlayed(float totalTimePlayed){
        this.totalTimePlayed = totalTimePlayed;
    }

    /**
     * Gets whether this virtual audio source is relative to the speak or should be played non-spatially
     * @return True if relative to speaker, false otherwise
     */
    public boolean isRelative(){
        return this.position != null;
    }

    /**
     * Sets the source to start to fade out
     * @param fadeRate The rate to lower gain each frame
     */
    public void setFadeRate(float fadeRate){
        this.fadeRate = fadeRate;
    }

    /**
     * Gets the position of this source
     * @return the position
     */
    public Vector3d getPosition(){
        return position;
    }

    /**
     * Gets the priority of this source
     * @return The priority
     */
    public int getPriority(){
        return priority;
    }

    /**
     * Gets the type of this source
     * @return The type
     */
    public VirtualAudioSourceType getType(){
        return type;
    }

    /**
     * Gets the gain of the source
     * @return The gain
     */
    public float getGain(){
        return gain * fadeModifier;
    }

    /**
     * Gets the total time this virtual audio source has played for
     * @return The total time
     */
    public float getTotalTimePlayed(){
        return totalTimePlayed;
    }

    /**
     * Gets the length of the buffer this virtual audio source relates to
     * @return The buffer length
     */
    public float getBufferLength(){
        AudioBuffer buffer = Globals.assetManager.fetchAudio(filePath);
        if(buffer != null){
            return buffer.getLength();
        }
        return 0;
    }



    @Override
    public int compareTo(VirtualAudioSource o) {
        return this.priority - o.priority;
    }
    
}
