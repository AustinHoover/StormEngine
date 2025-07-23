package electrosphere.renderer.anim;

import org.joml.Quaterniond;
import org.joml.Vector3f;

/**
 * A single keyframe of a single node within an animation
 */
public class Keyframe implements Comparable<Keyframe>{

    /**
     * The time the keyframe occurs at
     */
    protected double time;

    /**
     * The position of the keyframe
     */
    protected Vector3f position;

    /**
     * The rotation of the keyframe
     */
    protected Quaterniond rotation;

    /**
     * The scale of the keyframe
     */
    protected Vector3f scale;
    
    /**
     * Creates a keyframe
     * @param time The time the keyframe occurs at
     */
    public Keyframe(double time){
        this.time = time;
    }
    
    @Override
    public int compareTo(Keyframe frame) {
        if(time > frame.getTime()){
            return 1;
        } else if(time < frame.getTime()){
            return -1;
        } else {
            return 0;
        }
    }
    
    /**
     * Gets the time of the keyframe
     * @return The time
     */
    public double getTime(){
        return time;
    }
    
    @Override
    public String toString(){
        String rVal = "hash[" + this.hashCode() + "] ";
        rVal = rVal + "time[" + time + "]";
        if(position != null){
            rVal = rVal + " position:" + position.toString();
        }
        if(rotation != null){
            rVal = rVal + " rotation:" + rotation.toString();
        }
        if(scale != null){
            rVal = rVal + " scale:" + scale.toString();
        }
        return rVal;
    }
}
