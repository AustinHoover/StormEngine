package electrosphere.audio;

import electrosphere.audio.collision.HitboxAudioService;
import electrosphere.audio.movement.MovementAudioService;
import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.util.math.SpatialMathUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.SOFTHRTF;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.openal.ALC10.alcDestroyContext;
import static org.lwjgl.openal.ALC10.alcCloseDevice;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Main class that handles audio processing
 */
public class AudioEngine {

    /**
     * Controls whether the engine initialized or not
     */
    private boolean initialized = false;

    /**
     * openal device
     */
    private long device;

    /**
     * openal context
     */
    private long context;

    /**
     * the listener data for the audio landscape
     */
    private AudioListener listener;
    
    /**
     * the current gain level of the engine
     */
    private float engineGain = 1.0f;

    /**
     * The current device
     */
    private String currentDevice = "";

    /**
     * the default device
     */
    private String defaultDevice = "";

    /**
     * if true, hrtf present and active
     */
    private boolean hasHRTF = false;

    /**
     * if true, efx present and active
     */
    private boolean hasEFX = false;

    /**
     * The list of sources being tracked
     */
    private List<AudioSource> openALSources = new CopyOnWriteArrayList<AudioSource>();

    /**
     * The virtual audio source manager
     */
    public final VirtualAudioSourceManager virtualAudioSourceManager = new VirtualAudioSourceManager();

    /**
     * The movement audio service
     */
    public final MovementAudioService movementAudioService = new MovementAudioService();

    /**
     * The hitbox audio service
     */
    public final HitboxAudioService hitboxAudioService = new HitboxAudioService();
    

    /**
     * Creates an audio engine
     */
    public AudioEngine() {
    }

    /**
     * Initializes the audio engine
     */
    public void init() {
        try {
            this.initDevice();
            this.echoJavaAudioSupport();
        } catch (Exception ex) {
            LoggerInterface.loggerEngine.ERROR("Error initializing audio device", ex);
        }
        if(initialized){
            //recursively load all audio files
            listener = new AudioListener();
        }
    }

    /**
     * Lists all available devices
     */
    public void listAllDevices(){
        currentDevice = ALC11.alcGetString(NULL,ALC11.ALC_ALL_DEVICES_SPECIFIER);
        LoggerInterface.loggerAudio.INFO("AL device: " + currentDevice);
        defaultDevice = ALC11.alcGetString(NULL,ALC11.ALC_DEFAULT_ALL_DEVICES_SPECIFIER);
        LoggerInterface.loggerAudio.INFO("AL default device: " + defaultDevice);
    }
    
    /**
     * Initializes audio devices
     * @throws Exception Thrown if there are no audio devices or fails to create openal context
     */
    private void initDevice() throws Exception {
        //create device
        LoggerInterface.loggerAudio.DEBUG("Open ALC device");
        this.device = ALC10.alcOpenDevice((ByteBuffer) null);
        if (device == NULL) {
            throw new IllegalStateException("Failed to open the default OpenAL device.");
        }
        //create capabilities
        LoggerInterface.loggerAudio.DEBUG("Create device capabilities");
        ALCCapabilities deviceCaps = ALC.createCapabilities(device);
        //create context
        LoggerInterface.loggerAudio.DEBUG("Create context");
        IntBuffer attrBuffer = getContextAttrs(deviceCaps);
        this.context = ALC10.alcCreateContext(device, attrBuffer);
        MemoryUtil.memFree(attrBuffer);
        if (context == NULL) {
            throw new IllegalStateException("Failed to create OpenAL context.");
        }
        LoggerInterface.loggerAudio.DEBUG("Make Context Current");
        ALC10.alcMakeContextCurrent(context);
        AL.createCapabilities(deviceCaps);
        this.initialized = true;
    }

    /**
     * Gets the attrs buffer for creating a context
     * @param deviceCaps The device capabilities
     * @return The buffer (may be null if no desired extensions present)
     */
    private IntBuffer getContextAttrs(ALCCapabilities deviceCaps){
        int bufferSize = 0;
        //check for available extensions
        if(deviceCaps.ALC_EXT_EFX){
            LoggerInterface.loggerAudio.INFO("EFX PRESENT");
            hasEFX = true;
        } else {
            LoggerInterface.loggerAudio.INFO("EFX NOT PRESENT");
        }
        if(deviceCaps.ALC_SOFT_HRTF){
            LoggerInterface.loggerAudio.INFO("SOFT HRTF PRESENT");
            // hasHRTF = true;
            // bufferSize++;
        } else {
            LoggerInterface.loggerAudio.INFO("SOFT HRTF NOT PRESENT");
        }
        IntBuffer rVal = null;
        //construct buffer if any were found
        if(bufferSize > 0 ){
            rVal = BufferUtils.createIntBuffer(bufferSize * 2 + 1);
            if(deviceCaps.ALC_SOFT_HRTF){
                rVal.put(SOFTHRTF.ALC_HRTF_SOFT);
                rVal.put(ALC11.ALC_TRUE);
            }
            rVal.put(0);
            rVal.flip();
        }
        LoggerInterface.loggerAudio.INFO("Create attributes with size: " + bufferSize);
        return rVal;
    }

    /**
     * Echos the available support for different audio types from JRE itself
     */
    private void echoJavaAudioSupport(){
        LoggerInterface.loggerAudio.INFO("Check JRE-supported audio file types");
        for(AudioFileFormat.Type audioType : AudioSystem.getAudioFileTypes()){
            LoggerInterface.loggerAudio.INFO(audioType.getExtension() + " support: " + AudioSystem.isFileTypeSupported(audioType));
        }
    }
    
    /**
     * Gets the support formats
     * @return The list of file extensions
     */
    public List<String> getSupportedFormats(){
        List<String> rVal = new LinkedList<String>();
        for(AudioFileFormat.Type audioType : AudioSystem.getAudioFileTypes()){
            if(AudioSystem.isFileTypeSupported(audioType)){
                rVal.add(audioType.getExtension());
            }
        }
        return rVal;
    }

    /**
     * Updates the orientation of the listener based on the global player camera
     */
    private void updateListener(){
        if(Globals.clientState.playerCamera != null){
            //position
            Vector3d cameraPos = CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera);
            listener.setPosition(cameraPos);

            //orientation
            Vector3d cameraEye = SpatialMathUtils.getOriginVector().rotate(CameraEntityUtils.getRotationQuat(Globals.clientState.playerCamera)).normalize();
            Vector3d cameraUp = SpatialMathUtils.getUpVector().rotate(CameraEntityUtils.getRotationQuat(Globals.clientState.playerCamera)).normalize();
            listener.setOrientation(new Vector3f((float)cameraEye.x,(float)cameraEye.y,(float)cameraEye.z), new Vector3f((float)cameraUp.x,(float)cameraUp.y,(float)cameraUp.z));
        }
    }

    /**
     * Updates the audio engine
     */
    public void update(){
        this.updateListener();
        this.updateOpenALSources();
        this.virtualAudioSourceManager.update((float)Globals.engineState.timekeeper.getSimFrameTime());
    }

    /**
     * Shuts down the engine
     */
    public void shutdown(){
        alcDestroyContext(context);
        alcCloseDevice(device);
    }

    /**
     * Registers an openal audio source with the engine
     * @param source The audio source
     */
    protected void registerSource(AudioSource source){
        this.openALSources.add(source);
    }

    /**
     * Updates the status of all tracked sources
     */
    private void updateOpenALSources(){
        List<AudioSource> toRemove = new LinkedList<AudioSource>();
        for(AudioSource source : this.openALSources){
            if(!source.isPlaying()){
                toRemove.add(source);
            }
        }
        for(AudioSource source : toRemove){
            this.openALSources.remove(source);
            source.cleanup();
        }
    }

    /**
     * Gets the list of openal sources
     * @return The list of sources
     */
    public List<AudioSource> getOpenALSources(){
        return Collections.unmodifiableList(this.openALSources);
    }
    
    /**
     * Sets the gain of the engine
     * @param gain The gain value
     */
    public void setGain(float gain){
        engineGain = gain;
    }
    
    /**
     * Gets the gain of the engine
     * @return The gain value
     */
    public float getGain(){
        return engineGain;
    }

    /**
     * Gets the current openal device
     * @return The current openal device
     */
    public String getDevice(){
        return currentDevice;
    }

    /**
     * Gets the default openal device
     * @return the default openal device
     */
    public String getDefaultDevice(){
        return defaultDevice;
    }

    /**
     * Gets the HRTF status
     * @return The HRTF status
     */
    public boolean getHRTFStatus(){
        return hasHRTF;
    }

    /**
     * Gets the EFX status
     * @return The EFX status
     */
    public boolean getEFXStatus(){
        return hasEFX;
    }

    /**
     * Gets the listener for the audio engine
     * @return the listener
     */
    public AudioListener getListener(){
        return listener;
    }

    /**
     * Checks if the engine has initialized or not
     * @return true if initialized, false otherwise
     */
    public boolean initialized(){
        return this.initialized;
    }

    /**
     * Checks for an error on the most recent openal call
     */
    public String getLatestErrorMessage(){
        int previousMessage = AL11.alGetError();
        int error = AL11.alGetError();
        while(error != AL11.AL_NO_ERROR){
            previousMessage = error;
            error = AL11.alGetError();
        }
        switch(previousMessage){
            case AL11.AL_NO_ERROR: {
                return null;
            }
            case AL11.AL_INVALID_NAME: {
                return "Bad ID was passed to openal";
            }
            case AL11.AL_INVALID_ENUM: {
                return "Bed enum was passed to openal";
            }
            case AL11.AL_INVALID_VALUE: {
                return "Bad value was passed to openal";
            }
            case AL11.AL_INVALID_OPERATION: {
                return "Bad operation attempted by openal";
            }
            case AL11.AL_OUT_OF_MEMORY: {
                return "Openal is OOM";
            }
            default: {
                return "Unhandled error code! " + error;
            }
        }
    }

    /**
     * Checks for an error
     * @return true if an error was thrown, false otherwise
     */
    public boolean checkError(){
        String latestMessage = this.getLatestErrorMessage();
        if(latestMessage != null){
            LoggerInterface.loggerAudio.ERROR(new IllegalStateException(latestMessage));
            return true;
        }
        return false;
    }
    
    
    
}
