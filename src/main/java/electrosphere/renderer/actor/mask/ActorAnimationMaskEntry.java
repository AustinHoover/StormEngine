package electrosphere.renderer.actor.mask;

import java.util.LinkedList;
import java.util.List;

/**
 * An animation mask. Combines an animation name, priority, and a list of bones to apply that animation to
 */
public class ActorAnimationMaskEntry implements Comparable<ActorAnimationMaskEntry> {

    /**
     * The priority of the mask
     */
    private int priority;

    /**
     * The name of the animation
     */
    private String animationName;

    /**
     * The current time of this mask
     */
    private double time;

    /**
     * The maximum time to play this mask for
     */
    private double timeMax;

    /**
     * The mask of bones to apply this animation to
     */
    private List<String> boneMask;

    /**
     * The number of frames to freeze this animation mask for
     */
    private int freezeFrames = 0;

    /**
     * Constructor
     * @param priority
     * @param animationName
     * @param time
     * @param timeMax
     * @param boneMask
     */
    public ActorAnimationMaskEntry(int priority, String animationName, double time, double timeMax, List<String> boneMask){
        this.priority = priority;
        this.animationName = animationName;
        this.time = time;
        this.timeMax = timeMax;
        this.boneMask = boneMask;
    }

    /**
     * Constructor
     * @param priority
     * @param animationName
     * @param timeMax
     * @param boneMask
     */
    public ActorAnimationMaskEntry(int priority, String animationName, double timeMax, List<String> boneMask){
        this.priority = priority;
        this.animationName = animationName;
        this.timeMax = timeMax;
        this.boneMask = boneMask;
    }

    /**
     * Constructor
     * @param priority
     * @param animationName
     * @param time
     * @param timeMax
     */
    public ActorAnimationMaskEntry(int priority, String animationName, double time, double timeMax){
        this(priority, animationName, time, timeMax, new LinkedList<String>());
    }

    /**
     * Constructor
     * @param priority
     * @param animationName
     * @param timeMax
     */
    public ActorAnimationMaskEntry(int priority, String animationName, double timeMax){
        this(priority, animationName, 0, timeMax, new LinkedList<String>());
    }

    /**
     * Adds a bone to the mask
     * @param boneName The name of the bone
     */
    public void addBone(String boneName){
        boneMask.add(boneName);
    }

    /**
     * Gets the list of bones this mask applies to
     * @return The list of bones
     */
    public List<String> getBones(){
        return boneMask;
    }

    /**
     * Gets the name of the animation this mask uses
     * @return The name of the animation
     */
    public String getAnimationName(){
        return animationName;
    }

    /**
     * Gets the priority of the mask
     * @return The priority
     */
    public int getPriority(){
        return priority;
    }

    /**
     * Gets the time of the mask
     * @return The time
     */
    public double getTime(){
        return time;
    }

    /**
     * Sets the time of this mask
     * @param time The time
     */
    public void setTime(double time){
        this.time = time;
    }

    /**
     * Gets the duration of the mask
     * @return The duration
     */
    public double getDuration(){
        return timeMax;
    }

    /**
     * Gets the current freeze frame count
     * @return The number of freeze frames
     */
    public int getFreezeFrames(){
        return freezeFrames;
    }

    /**
     * Sets the number of freeze frames remaining in the mask
     * @param frameCount The number of frames
     */
    public void setFreezeFrames(int frameCount){
        this.freezeFrames = frameCount;
    }

    @Override
    public int compareTo(ActorAnimationMaskEntry o) {
        ActorAnimationMaskEntry otherMask = (ActorAnimationMaskEntry)o;
        if(otherMask.priority > this.priority){
            return -1;
        } else if(otherMask.priority < this.priority){
            return 1;
        } else {
            return 0;
        }
    }
    
}
