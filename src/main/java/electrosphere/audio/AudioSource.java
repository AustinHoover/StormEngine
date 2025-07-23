package electrosphere.audio;

import org.joml.Vector3f;
import org.lwjgl.openal.AL11;

import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;

import org.lwjgl.openal.AL10;

/**
 * A source of audio in the aural space
 */
public class AudioSource {
    
    /**
     * An undefined source's id
     */
    static final int UNDEFINED_ID = -1;

    /**
     * This id is being sent for some reason, and it is an invalid id
     */
    static final int INVALID_ID = 0;

    /**
     * The id for the source
     */
    private int sourceId = UNDEFINED_ID;
    
    /**
     * Creates an audio source object
     * @param loop if true, will loop audio, otherwise will not
     */
    protected static AudioSource create(boolean loop){
        AudioSource rVal = new AudioSource();
        rVal.sourceId = AL10.alGenSources();
        if(rVal.sourceId == AudioSource.INVALID_ID){
            throw new Error("Was allocated an invalid id from openAL! " + rVal.sourceId);
        }
        Globals.audioEngine.checkError();
        if(loop){
            AL10.alSourcei(rVal.sourceId, AL10.AL_LOOPING, AL10.AL_TRUE);
            Globals.audioEngine.checkError();
        }
        Globals.audioEngine.registerSource(rVal);
        return rVal;
    }
    
    /**
     * Sets the buffer that this source pulls from
     * @param bufferId The id of the buffer
     */
    public void setBuffer(int bufferId) {
        this.stop();
        if(this.isAllocated()){
            AL10.alSourcei(sourceId, AL10.AL_BUFFER, bufferId);
            Globals.audioEngine.checkError();
        }
    }

    /**
     * Sets the position of the audio source
     * @param position the position
     */
    public void setPosition(Vector3f position) {
        if(this.isAllocated()){
            AL10.alSource3f(sourceId, AL10.AL_POSITION, position.x, position.y, position.z);
            Globals.audioEngine.checkError();
        }
    }

    /**
     * Sets the speed of the audio source
     * @param speed the speed
     */
    public void setSpeed(Vector3f speed) {
        if(this.isAllocated()){
            AL10.alSource3f(sourceId, AL10.AL_VELOCITY, speed.x, speed.y, speed.z);
            Globals.audioEngine.checkError();
        }
    }

    /**
     * Sets whether this audio is spatial or not
     * @param isSpatial true for spatial audio, false otherwise
     */
    public void setSpatial(boolean isSpatial){
        int val = isSpatial ? AL10.AL_FALSE : AL10.AL_TRUE;
        AL10.alSourcei(sourceId, AL10.AL_SOURCE_RELATIVE, val);
    }

    /**
     * Sets the temporal offset of the source (ie how far into the clip to start playingf)
     * @param time The time in seconds
     */
    public void setOffset(float time){
        if(this.isAllocated()){
            AL10.alSourcef(sourceId, AL11.AL_SEC_OFFSET, time);
            Globals.audioEngine.checkError();
        }
    }

    /**
     * Sets the gain of the audio source
     * @param gain the gain
     */
    public void setGain(float gain) {
        if(this.isAllocated()){
            LoggerInterface.loggerAudio.DEBUG("Set Gain: " + gain);
            AL10.alSourcef(sourceId, AL10.AL_GAIN, gain);
            if(Globals.audioEngine.checkError()){
                LoggerInterface.loggerAudio.WARNING("Audio source id for error: " + sourceId);
            }
        }
    }

    /**
     * Sets an arbitrary property on the audio source
     * @param param The param flag
     * @param value The value to set the param to
     */
    public void setProperty(int param, float value) {
        if(this.isAllocated()){
            AL10.alSourcef(sourceId, param, value);
            Globals.audioEngine.checkError();
        }
    }

    /**
     * Plays the audio source
     */
    public void play() {
        if(this.isAllocated()){
            AL10.alSourcePlay(sourceId);
            Globals.audioEngine.checkError();
        }
    }

    /**
     * Gets whether the audio source is currently playing or not
     * @return True if it is playing, false otherwise
     */
    public boolean isPlaying() {
        boolean isPlaying = false;
        if(this.isAllocated()){
            isPlaying = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
            Globals.audioEngine.checkError();
        }
        return isPlaying;
    }

    /**
     * Pauses the audio source
     */
    public void pause() {
        if(this.isAllocated()){
            AL10.alSourcePause(sourceId);
            Globals.audioEngine.checkError();
        }
    }

    /**
     * Stops the audio source
     */
    public void stop() {
        if(this.isAllocated()){
            AL10.alSourceStop(sourceId);
            Globals.audioEngine.checkError();
        }
    }

    /**
     * Cleans up the source
     */
    public void cleanup() {
        this.stop();
        int oldId = sourceId;
        sourceId = UNDEFINED_ID;
        AL10.alDeleteSources(oldId);
        Globals.audioEngine.checkError();
    }

    /**
     * Checks if the audio source is allocated or not
     * @return true if allocated, false otherwise
     */
    private boolean isAllocated(){
        return sourceId != UNDEFINED_ID && this.sourceId != INVALID_ID;
    }
}
