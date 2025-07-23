package electrosphere.renderer.anim;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.joml.Quaterniond;
import org.joml.Vector3f;

import electrosphere.logger.LoggerInterface;

/**
 * A single channel of keyframes in an animation
 */
public class AnimChannel {

    /**
     * The current time of the channel
     */
    private double timeCurrent = 0;

    /**
     * The total time of the channel
     */
    private double timeTotal;

    /**
     * The ticks per second of the channel
     */
    private double ticksPerSecond;

    /**
     * The bone id associated with the channel
     */
    private String nodeID;

    /**
     * All position frames
     */
    private TreeMap<Double,Keyframe> positionFrameTree;

    /**
     * All rotation frames
     */
    private TreeMap<Double,Keyframe> rotationFrameTree;
    
    /**
     * All scale frames
     */
    private TreeMap<Double,Keyframe> scaleFrameTree;
    
    
    /**
     * Creates an anim channel
     * @param nodeId The node ID
     * @param maxTime The max time of the channel
     * @param ticksPerSecond The ticks per second
     */
    public AnimChannel(String nodeId, double maxTime, double ticksPerSecond){
        timeTotal = maxTime;
        this.nodeID = nodeId;
        this.ticksPerSecond = ticksPerSecond;
        positionFrameTree = new TreeMap<Double,Keyframe>();
        rotationFrameTree = new TreeMap<Double,Keyframe>();
        scaleFrameTree = new TreeMap<Double,Keyframe>();
    }
    
    /**
     * Gets the current position of this anim channel
     * @return The position
     */
    public Vector3f getCurrentPosition(){
        Vector3f rVal = new Vector3f();

        Entry<Double,Keyframe> previousEntry = positionFrameTree.floorEntry(timeCurrent);
        Entry<Double,Keyframe> nextEntry = positionFrameTree.ceilingEntry(timeCurrent);
        Keyframe previousFrame = null;
        Keyframe nextFrame = null;

        //error checking
        if(previousEntry == null && nextEntry == null){
            LoggerInterface.loggerEngine.WARNING("Animation channel with no keys!");
            return rVal;
        }

        //find the frames from the enties in the tree
        if(previousEntry != null && nextEntry != null && previousEntry.getValue() == nextEntry.getValue()){
            nextEntry = positionFrameTree.higherEntry(timeCurrent);
        }
        if(previousEntry != null){
            previousFrame = previousEntry.getValue();
        }
        if(nextEntry != null){
            nextFrame = nextEntry.getValue();
        }

        //error checking
        if(previousFrame != null && previousFrame == nextFrame){
            LoggerInterface.loggerEngine.WARNING("Animation channel with same frames for different enties?!");
            LoggerInterface.loggerEngine.WARNING("entry comparison: " + (nextEntry == previousEntry));
            LoggerInterface.loggerEngine.WARNING("previous entry: " + nextEntry);
            LoggerInterface.loggerEngine.WARNING("next entry: " + previousEntry);
            LoggerInterface.loggerEngine.WARNING("frame comparison: " + (previousFrame == nextFrame));
            LoggerInterface.loggerEngine.WARNING("previous frame: " + previousFrame);
            LoggerInterface.loggerEngine.WARNING("next frame: " + nextFrame);
            this.validate();
        }

        //calculate position
        if(previousFrame != null && nextFrame != null && previousFrame != nextFrame){
            double percent_Next = ((timeCurrent - previousFrame.time) / (nextFrame.time - previousFrame.time));
            rVal = new Vector3f().add(new Vector3f().add(previousFrame.position).mul((float)(1.0 - percent_Next))).add(new Vector3f().add(nextFrame.position).mul((float)(percent_Next)));
        } else if(previousFrame != null){
            rVal = new Vector3f().add(previousFrame.position);
        } else if(nextFrame != null){
            rVal = new Vector3f().add(nextFrame.position);
        }

        //error checking
        if(this.timeCurrent >= 0 && previousFrame == null && nextFrame == null){
            LoggerInterface.loggerEngine.WARNING("previousFrame: " + previousFrame);
            LoggerInterface.loggerEngine.WARNING("nextFrame: " + nextFrame);
            LoggerInterface.loggerEngine.WARNING("timeCurrent: " + timeCurrent);
            LoggerInterface.loggerEngine.WARNING("timeTotal: " + timeTotal);
            LoggerInterface.loggerEngine.WARNING("positionFrameTree.size(): " + positionFrameTree.size());
            throw new IllegalStateException("Anim channel with time set has no available frames!");
        }

        if(!Float.isFinite(rVal.x)){
            LoggerInterface.loggerEngine.WARNING("Frame comparison: " + (previousEntry == nextEntry));
            LoggerInterface.loggerEngine.WARNING("previousFrame: " + previousFrame);
            LoggerInterface.loggerEngine.WARNING("previousFrame.position: " + previousFrame.position);
            LoggerInterface.loggerEngine.WARNING("nextFrame: " + nextFrame);
            LoggerInterface.loggerEngine.WARNING("nextFrame.position: " + nextFrame.position);
            if(previousFrame != null && nextFrame != null){
                LoggerInterface.loggerEngine.WARNING("percent_Next: " + ((timeCurrent - previousFrame.time) / (nextFrame.time - previousFrame.time)));
            }
            LoggerInterface.loggerEngine.WARNING("Position: " + rVal);
            throw new IllegalStateException("Anim channel position is not finite!");
        }

        return rVal;
    }
    
    /**
     * Gets the current rotation of this anim channel
     * @return The rotation
     */
    public Quaterniond getCurrentRotation(){
        Quaterniond rVal = new Quaterniond();

        Entry<Double,Keyframe> previousEntry = rotationFrameTree.floorEntry(timeCurrent);
        Entry<Double,Keyframe> nextEntry = rotationFrameTree.ceilingEntry(timeCurrent);
        Keyframe previousFrame = null;
        Keyframe nextFrame = null;

        //error checking
        if(previousEntry == null && nextEntry == null){
            LoggerInterface.loggerEngine.WARNING("Animation channel with no keys!");
            return rVal;
        }

        //find the frames from the enties in the tree
        if(previousEntry != null && nextEntry != null && previousEntry.getValue() == nextEntry.getValue()){
            nextEntry = rotationFrameTree.higherEntry(timeCurrent);
        }
        if(previousEntry != null){
            previousFrame = previousEntry.getValue();
        }
        if(nextEntry != null){
            nextFrame = nextEntry.getValue();
        }

        //error checking
        if(previousFrame != null && previousFrame == nextFrame){
            LoggerInterface.loggerEngine.WARNING("Animation channel with same frames for different enties?!");
            LoggerInterface.loggerEngine.WARNING("entry comparison: " + (nextEntry == previousEntry));
            LoggerInterface.loggerEngine.WARNING("previous entry: " + nextEntry);
            LoggerInterface.loggerEngine.WARNING("next entry: " + previousEntry);
            LoggerInterface.loggerEngine.WARNING("frame comparison: " + (previousFrame == nextFrame));
            LoggerInterface.loggerEngine.WARNING("previous frame: " + previousFrame);
            LoggerInterface.loggerEngine.WARNING("next frame: " + nextFrame);
            this.validate();
        }


        //calculate rotation
        if(previousFrame != null && nextFrame != null && previousFrame != nextFrame){
            double percent_Next = ((timeCurrent - previousFrame.time) / (nextFrame.time - previousFrame.time));
            rVal = new Quaterniond(previousFrame.rotation).slerp(nextFrame.rotation, (float)percent_Next);
        } else if(previousFrame != null){
            rVal = new Quaterniond(previousFrame.rotation);
        } else if(nextFrame != null){
            rVal = new Quaterniond(nextFrame.rotation);
        }

        //error checking
        if(this.timeCurrent >= 0 && previousFrame == null && nextFrame == null){
            LoggerInterface.loggerEngine.WARNING("previousFrame: " + previousFrame);
            LoggerInterface.loggerEngine.WARNING("nextFrame: " + nextFrame);
            LoggerInterface.loggerEngine.WARNING("timeCurrent: " + timeCurrent);
            LoggerInterface.loggerEngine.WARNING("timeTotal: " + timeTotal);
            LoggerInterface.loggerEngine.WARNING("rotationFrameTree.size(): " + rotationFrameTree.size());
            throw new IllegalStateException("Anim channel with time set has no available frames!");
        }

        if(!Double.isFinite(rVal.x)){
            LoggerInterface.loggerEngine.WARNING("Frame comparison: " + (previousEntry == nextEntry));
            LoggerInterface.loggerEngine.WARNING("previousFrame: " + previousFrame);
            LoggerInterface.loggerEngine.WARNING("previousFrame.rotation: " + previousFrame.rotation);
            LoggerInterface.loggerEngine.WARNING("nextFrame: " + nextFrame);
            LoggerInterface.loggerEngine.WARNING("nextFrame.rotation: " + nextFrame.rotation);
            if(previousFrame != null && nextFrame != null){
                LoggerInterface.loggerEngine.WARNING("percent_Next: " + ((timeCurrent - previousFrame.time) / (nextFrame.time - previousFrame.time)));
            }
            LoggerInterface.loggerEngine.WARNING("Rotation: " + rVal);
            throw new IllegalStateException("Anim channel rotation is not finite!");
        }

        return rVal;
    }
    
    /**
     * Gets the current scale of this anim channel
     * @return The scale
     */
    public Vector3f getCurrentScale(){
        Vector3f rVal = new Vector3f();

        Entry<Double,Keyframe> previousEntry = scaleFrameTree.floorEntry(timeCurrent);
        Entry<Double,Keyframe> nextEntry = scaleFrameTree.ceilingEntry(timeCurrent);
        Keyframe previousFrame = null;
        Keyframe nextFrame = null;


        //find the frames from the enties in the tree
        if(previousEntry != null && nextEntry != null && previousEntry.getValue() == nextEntry.getValue()){
            nextEntry = scaleFrameTree.higherEntry(timeCurrent);
        }
        if(previousEntry != null){
            previousFrame = previousEntry.getValue();
        }
        if(nextEntry != null){
            nextFrame = nextEntry.getValue();
        }


        //error checking
        if(previousFrame != null && previousFrame == nextFrame){
            LoggerInterface.loggerEngine.WARNING("Animation channel with same frames for different enties?!");
            LoggerInterface.loggerEngine.WARNING("entry comparison: " + (nextEntry == previousEntry));
            LoggerInterface.loggerEngine.WARNING("previous entry: " + nextEntry);
            LoggerInterface.loggerEngine.WARNING("next entry: " + previousEntry);
            LoggerInterface.loggerEngine.WARNING("frame comparison: " + (previousFrame == nextFrame));
            LoggerInterface.loggerEngine.WARNING("previous frame: " + previousFrame);
            LoggerInterface.loggerEngine.WARNING("next frame: " + nextFrame);
            this.validate();
        }



        //calculate scale
        if(previousFrame != null && nextFrame != null && previousFrame != nextFrame){
            double percent_Next = ((timeCurrent - previousFrame.time) / (nextFrame.time - previousFrame.time));
            rVal = new Vector3f().add(new Vector3f().add(previousFrame.scale).mul((float)(1.0 - percent_Next))).add(new Vector3f().add(nextFrame.scale).mul((float)(percent_Next)));
        } else if(previousFrame != null){
            rVal = new Vector3f().add(previousFrame.scale);
        } else if(nextFrame != null){
            rVal = new Vector3f().add(nextFrame.scale);
        }

        //error checking
        if(this.timeCurrent >= 0 && previousFrame == null && nextFrame == null){
            LoggerInterface.loggerEngine.WARNING("previousFrame: " + previousFrame);
            LoggerInterface.loggerEngine.WARNING("nextFrame: " + nextFrame);
            LoggerInterface.loggerEngine.WARNING("timeCurrent: " + timeCurrent);
            LoggerInterface.loggerEngine.WARNING("timeTotal: " + timeTotal);
            LoggerInterface.loggerEngine.WARNING("scaleFrameTree.size(): " + scaleFrameTree.size());
            throw new IllegalStateException("Anim channel with time set has no available frames!");
        }

        if(!Float.isFinite(rVal.x)){
            LoggerInterface.loggerEngine.WARNING("Frame comparison: " + (previousEntry == nextEntry));
            LoggerInterface.loggerEngine.WARNING("previousFrame: " + previousFrame);
            LoggerInterface.loggerEngine.WARNING("previousFrame.scale: " + previousFrame.scale);
            LoggerInterface.loggerEngine.WARNING("nextFrame: " + nextFrame);
            LoggerInterface.loggerEngine.WARNING("nextFrame.scale: " + nextFrame.scale);
            if(previousFrame != null && nextFrame != null){
                LoggerInterface.loggerEngine.WARNING("percent_Next: " + ((timeCurrent - previousFrame.time) / (nextFrame.time - previousFrame.time)));
            }
            LoggerInterface.loggerEngine.WARNING("Scale: " + rVal);
            throw new IllegalStateException("Anim channel scale is not finite!");
        }

        return rVal;
    }
    
    /**
     * Increments time on the channel
     * @param incrementValue The amount to increment by
     */
    public void incrementTime(double incrementValue){
        timeCurrent = timeCurrent + incrementValue;
    }

    /**
     * Sets the current time of the channel
     * @param time The time
     */
    public void setTime(double time){
        this.timeCurrent = time;
    }
    
    /**
     * Rewinds the channel
     */
    public void rewind(){
        timeCurrent = 0;
    }
    
    /**
     * Gets the ticks per second
     * @return The ticks per second
     */
    public double getTicksPerSecond(){
        return this.ticksPerSecond;
    }

    /**
     * Describes the channel at a high level
     */
    public void describeChannel(){
        LoggerInterface.loggerEngine.INFO("Target object: " + nodeID);
        LoggerInterface.loggerEngine.INFO("Time: " + timeCurrent + "/" + timeTotal);
        LoggerInterface.loggerEngine.INFO(positionFrameTree.size() + " position Frames");
        LoggerInterface.loggerEngine.INFO(rotationFrameTree.size() + " rotation Frames");
        LoggerInterface.loggerEngine.INFO(scaleFrameTree.size() + " scale Frames");
    }
    
    /**
     * Fully describes the channel
     */
    public void fullDescribeChannel(){
        LoggerInterface.loggerEngine.INFO("Target object: " + nodeID);
        LoggerInterface.loggerEngine.INFO("Time: " + timeCurrent + "/" + timeTotal);
        LoggerInterface.loggerEngine.INFO(positionFrameTree.size() + " position Frames");
        Iterator<Map.Entry<Double,Keyframe>> frameIterator = positionFrameTree.entrySet().iterator();
        while(frameIterator.hasNext()){
            LoggerInterface.loggerEngine.INFO(frameIterator.next() + "");
        }
        LoggerInterface.loggerEngine.INFO(rotationFrameTree.size() + " rotation Frames");
        frameIterator = rotationFrameTree.entrySet().iterator();
        while(frameIterator.hasNext()){
            LoggerInterface.loggerEngine.INFO(frameIterator.next() + "");
        }
        LoggerInterface.loggerEngine.INFO(scaleFrameTree.size() + " scale Frames");
        frameIterator = scaleFrameTree.entrySet().iterator();
        while(frameIterator.hasNext()){
            LoggerInterface.loggerEngine.INFO(frameIterator.next() + "");
        }
    }

    /**
     * Validates the data in the anim channel
     */
    public void validate(){
        for(Double key1 : positionFrameTree.keySet()){
            //check if different keys have the same values
            for(Double key2 : positionFrameTree.keySet()){
                if(
                    key1 != key2 &&
                    positionFrameTree.get(key1) == positionFrameTree.get(key2)
                ){
                    LoggerInterface.loggerEngine.WARNING("Different keys in the position frame tree have the same value!");
                    LoggerInterface.loggerEngine.WARNING("key1: " + key1);
                    LoggerInterface.loggerEngine.WARNING(positionFrameTree.get(key1) + "");
                    LoggerInterface.loggerEngine.WARNING("key2: " + key2);
                    LoggerInterface.loggerEngine.WARNING(positionFrameTree.get(key2) + "");
                }

            }
        }

        for(Double key1 : rotationFrameTree.keySet()){
            //check if different keys have the same values
            for(Double key2 : rotationFrameTree.keySet()){
                if(
                    key1 != key2 &&
                    rotationFrameTree.get(key1) == rotationFrameTree.get(key2)
                ){
                    LoggerInterface.loggerEngine.WARNING("Different keys in the position frame tree have the same value!");
                    LoggerInterface.loggerEngine.WARNING("key1: " + key1);
                    LoggerInterface.loggerEngine.WARNING(rotationFrameTree.get(key1) + "");
                    LoggerInterface.loggerEngine.WARNING("key2: " + key2);
                    LoggerInterface.loggerEngine.WARNING(rotationFrameTree.get(key2) + "");
                }
            }
        }

        for(Double key1 : scaleFrameTree.keySet()){
            //check if different keys have the same values
            for(Double key2 : scaleFrameTree.keySet()){
                if(
                    key1 != key2 &&
                    scaleFrameTree.get(key1) == scaleFrameTree.get(key2)
                ){
                    LoggerInterface.loggerEngine.WARNING("Different keys in the position frame tree have the same value!");
                    LoggerInterface.loggerEngine.WARNING("key1: " + key1);
                    LoggerInterface.loggerEngine.WARNING(scaleFrameTree.get(key1) + "");
                    LoggerInterface.loggerEngine.WARNING("key2: " + key2);
                    LoggerInterface.loggerEngine.WARNING(scaleFrameTree.get(key2) + "");
                }
            }
        }
    }

    /**
     * Gets the name of the bone associated with the channel
     * @return The name of the bone
     */
    public String getNodeID(){
        return nodeID;
    }

    /**
     * Adds a position frame to the channel
     * @param time The time the frame occurs at
     * @param frame The frame itself
     */
    public void addPositionFrame(double time, Keyframe frame){
        positionFrameTree.put(time, frame);
    }

    /**
     * Adds a rotation frame to the channel
     * @param time The time the frame occurs at
     * @param frame The frame itself
     */
    public void addRotationFrame(double time, Keyframe frame){
        rotationFrameTree.put(time, frame);
    }

    /**
     * Adds a scale frame to the channel
     * @param time The time the frame occurs at
     * @param frame The frame itself
     */
    public void addScaleFrame(double time, Keyframe frame){
        scaleFrameTree.put(time, frame);
    }
    
}
