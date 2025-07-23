package electrosphere.renderer.anim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.joml.Quaterniond;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AINodeAnim;
import org.lwjgl.assimp.AIQuatKey;
import org.lwjgl.assimp.AIVectorKey;

import electrosphere.logger.LoggerInterface;

/**
 * An animation on a model
 */
public class Animation {
    
    //common animations
    public static final String ANIMATION_MOVEMENT_STARTUP = "WalkStart";
    public static final String ANIMATION_MOVEMENT_MOVE = "Walk";
    public static final String ANIMATION_SWING_PRIMARY = "SwingWeapon";
    public static final String ANIMATION_SPRINT_STARTUP = "RunStart";
    public static final String ANIMATION_SPRINT = "Run";
    public static final String ANIMATION_SPRINT_WINDDOWN = "RunStart";
    public static final String ANIMATION_WALK_RIGHT = "WalkStrafeRight";
    public static final String ANIMATION_WALK_LEFT = "WalkStrafeLeft";
    
    
    
    /**
     * The anim data associated with the animation
     */
    AIAnimation animData;

    /**
     * The name of the animation
     */
    public String name;

    /**
     * The channels that contain the animation data
     */
    public ArrayList<AnimChannel> channels;

    /**
     * The duration of the animation
     */
    public double duration = 0;

    /**
     * The current time of the animation
     */
    public double timeCurrent = 0;

    /**
     * The ticks per second of animation
     */
    public double ticksPerSecond;

    /**
     * The map of bone name to animation channel
     */
    private Map<String, AnimChannel> channelMap;

    /**
     * Creates an animation
     * @param animData The data for the animation
     */
    public Animation(AIAnimation animData){
        //
        //Create structures
        //
        channelMap = new HashMap<String, AnimChannel>();

        //
        //Read in metadata
        //
        this.animData = animData;
        name = animData.mName().dataString();
        this.ticksPerSecond = animData.mTicksPerSecond();
        this.duration = animData.mDuration() / this.ticksPerSecond;

        //
        //Read in anim channels (bone modifications)
        //
        int channelCount = animData.mNumChannels();
        channels = new ArrayList<AnimChannel>();
        if(channelCount > 0){
            for(int i = 0; i < channelCount; i++){
                AINodeAnim currentChannelData = AINodeAnim.create(animData.mChannels().get(i));
                
                //Create channel
                String nodeId = currentChannelData.mNodeName().dataString();
                AnimChannel currentChannel = new AnimChannel(nodeId,duration,ticksPerSecond);
                channels.add(currentChannel);

                channelMap.put(nodeId,currentChannel);
                
                
                //get channel data
                
                if(currentChannelData.mNumPositionKeys() > 0){
                    org.lwjgl.assimp.AIVectorKey.Buffer buff = currentChannelData.mPositionKeys();
                    if(buff != null && buff.hasRemaining()){
                        AIVectorKey key = buff.get();
                        double time = key.mTime() / this.ticksPerSecond;
                        Keyframe currentFrame = new Keyframe(time);
                        Vector4d positionRaw = new Vector4d(
                            key.mValue().x(),
                            key.mValue().y(),
                            key.mValue().z(),
                            1
                        );
                        currentFrame.position = new Vector3f((float)positionRaw.x,(float)positionRaw.y,(float)positionRaw.z);
                        currentChannel.addPositionFrame(time,currentFrame);
                        
                        Keyframe previousFrame;
                        while(buff.hasRemaining()){
                            previousFrame = currentFrame;
                            key = buff.get();
                            time = key.mTime() / this.ticksPerSecond;
                            currentFrame = new Keyframe(time);
                            positionRaw = new Vector4d(
                                key.mValue().x(),
                                key.mValue().y(),
                                key.mValue().z(),
                                1
                            );
                            currentFrame.position = new Vector3f((float)positionRaw.x,(float)positionRaw.y,(float)positionRaw.z);
                            //check if duplicate
                            if(     previousFrame.position.x == currentFrame.position.x &&
                                    previousFrame.position.y == currentFrame.position.y &&
                                    previousFrame.position.z == currentFrame.position.z
                                    ){
                            } else {
                                currentChannel.addPositionFrame(time,currentFrame);
                            }
                        }
                    }
                }
                
                if(currentChannelData.mNumRotationKeys() > 0){
                    org.lwjgl.assimp.AIQuatKey.Buffer buff = currentChannelData.mRotationKeys();
                    if(buff != null && buff.hasRemaining()){
                        AIQuatKey key = buff.get();
                        double time = key.mTime() / this.ticksPerSecond;
                        Keyframe currentFrame = new Keyframe(time);
                        currentFrame.rotation = new Quaterniond();
                        currentFrame.rotation.set(key.mValue().x(), key.mValue().y(), key.mValue().z(), key.mValue().w());
                        currentChannel.addRotationFrame(time,currentFrame);
                        
                        Keyframe previousFrame;
                        while(buff.hasRemaining()){
                            previousFrame = currentFrame;
                            key = buff.get();
                            time = key.mTime() / this.ticksPerSecond;
                            currentFrame = new Keyframe(time);
                            currentFrame.rotation = new Quaterniond();
                            currentFrame.rotation.set(key.mValue().x(), key.mValue().y(), key.mValue().z(), key.mValue().w());
                            //check for duplicate
                            if(     previousFrame.rotation.w == currentFrame.rotation.w &&
                                    previousFrame.rotation.x == currentFrame.rotation.x &&
                                    previousFrame.rotation.y == currentFrame.rotation.y &&
                                    previousFrame.rotation.z == currentFrame.rotation.z
                                    ){
                            } else {
                                currentChannel.addRotationFrame(time,currentFrame);
                            }
                        }
                    }
                }
                
                if(currentChannelData.mNumScalingKeys() > 0){
                    org.lwjgl.assimp.AIVectorKey.Buffer buff = currentChannelData.mScalingKeys();
                    if(buff != null && buff.hasRemaining()){
                        AIVectorKey key = buff.get();
                        double time = key.mTime() / this.ticksPerSecond;
                        Keyframe currentFrame = new Keyframe(time);
                        currentFrame.scale = new Vector3f(
                            (float)(key.mValue().x()),
                            (float)(key.mValue().y()),
                            (float)(key.mValue().z())
                        );
                        currentChannel.addScaleFrame(time,currentFrame);
                        Keyframe previousFrame;
                        while(buff.hasRemaining()){
                            previousFrame = currentFrame;
                            key = buff.get();
                            time = key.mTime() / this.ticksPerSecond;
                            currentFrame = new Keyframe(time);
                            currentFrame.scale = new Vector3f(
                                (float)(key.mValue().x()),
                                (float)(key.mValue().y()),
                                (float)(key.mValue().z())
                            );
                            if(     currentFrame.scale.x == previousFrame.scale.x &&
                                    currentFrame.scale.y == previousFrame.scale.y &&
                                    currentFrame.scale.z == previousFrame.scale.z
                                    ){
                            } else {
                                currentChannel.addScaleFrame(time,currentFrame);
                            }
                        }
                    }
                }
                
                
            }
        }
    }

    /**
     * Describes the animation at high level
     */
    public void describeAnimation(){
        LoggerInterface.loggerRenderer.DEBUG("=====================");
        LoggerInterface.loggerRenderer.DEBUG("Name: \"" + name + "\"");
        LoggerInterface.loggerRenderer.DEBUG("Duration: " + duration);
        LoggerInterface.loggerRenderer.DEBUG("Ticks per second: " + ticksPerSecond);
        Iterator<AnimChannel> channelIterator = channels.iterator();
        while(channelIterator.hasNext()){
            AnimChannel channelCurrent = channelIterator.next();
            LoggerInterface.loggerRenderer.DEBUG("=====================");
            channelCurrent.describeChannel();
        }
        LoggerInterface.loggerRenderer.DEBUG("=====================");
    }

    /**
     * Describes the animation is as much detail as possible
     */
    public void fullDescribeAnimation(){
        LoggerInterface.loggerRenderer.DEBUG("=====================");
        LoggerInterface.loggerRenderer.DEBUG("Name: " + name);
        LoggerInterface.loggerRenderer.DEBUG("Duration: " + duration);
        LoggerInterface.loggerRenderer.DEBUG("Ticks per second: " + ticksPerSecond);
        Iterator<AnimChannel> channelIterator = channels.iterator();
        while(channelIterator.hasNext()){
            AnimChannel channelCurrent = channelIterator.next();
            LoggerInterface.loggerRenderer.DEBUG("=====================");
            channelCurrent.fullDescribeChannel();
        }
        LoggerInterface.loggerRenderer.DEBUG("=====================");
    }
    
    /**
     * Increments time on the animation
     * @param time The amount of time to increment by
     * @return true if the animation has completed, false otherwise
     */
    public boolean incrementTime(double time){
        timeCurrent += time;
        if(timeCurrent > duration || timeCurrent < 0){
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Sets the time of the animation
     * @param time The time
     */
    public void setTime(double time){
        timeCurrent = time;
    }

    /**
     * Gets the channel data for a given bone
     * @param name The name of the bone
     * @return The channel data if it exists, null otherwise
     */
    public AnimChannel getChannel(String name){
        return channelMap.get(name);
    }
}
