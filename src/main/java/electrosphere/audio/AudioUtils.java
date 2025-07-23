package electrosphere.audio;

import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.util.FileUtils;

import org.joml.Vector3d;
import org.joml.Vector3f;

/**
 * Utility functions for playing audio
 */
public class AudioUtils {
    
    /**
     * Plays a audio at a given audio file path at a given position
     * @param audioFile The audio file's path
     * @param position The position to play it at
     * @param loops If true, loops the source
     * @return The audio source
     */
    protected static AudioSource playAudioAtLocation(String audioFile, Vector3f position, boolean loops){
        AudioSource rVal = null;
        AudioBuffer buffer = Globals.assetManager.fetchAudio(audioFile);
        if(buffer != null && Globals.audioEngine.initialized()){
            rVal = AudioSource.create(loops);
            rVal.setBuffer(buffer.getBufferId());
            rVal.setGain(Globals.audioEngine.getGain());
            rVal.setPosition(position);
            rVal.play();
        } else {
            LoggerInterface.loggerEngine.WARNING("Failed to start audio in playAudioAtLocation");
        }
        return rVal;
    }

    /**
     * Plays a audio at a given audio file path at a given position
     * @param audioFile The audio file's path
     * @param position The position to play it at
     * @param loops If true, loops the source
     * @return The audio source
     */
    protected static AudioSource playAudioAtLocation(String audioFile, Vector3d position, boolean loops){
        return AudioUtils.playAudioAtLocation(audioFile,new Vector3f((float)position.x,(float)position.y,(float)position.z),loops);
    }

    /**
     * Plays an audio file
     * @param audioFile The audio file path
     * @param loops If true, loops the source
     * @return The audio source
     */
    protected static AudioSource playAudio(String audioFile, boolean loops){
        AudioSource rVal = null;
        AudioBuffer buffer = Globals.assetManager.fetchAudio(FileUtils.sanitizeFilePath(audioFile));
        if(buffer != null && Globals.audioEngine.initialized()){
            rVal = AudioSource.create(loops);
            rVal.setBuffer(buffer.getBufferId());
            rVal.setGain(Globals.audioEngine.getGain());
            rVal.setSpatial(false);
            rVal.play();
        } else {
            LoggerInterface.loggerEngine.WARNING("Failed to start audio in playAudioAtLocation");
        }
        return rVal;
    }
    
}
