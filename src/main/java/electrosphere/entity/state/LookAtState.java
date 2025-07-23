package electrosphere.entity.state;

import electrosphere.entity.Entity;

/**
 * State for looking at items
 */
public class LookAtState {
    
    Entity parent;
    
    float pitch;
    float pitchMax;
    float pitchMin;
    float pitchNeutral;
    String rotatorBoneName;
    
    
    public LookAtState(Entity parent, String rotatorBoneName, float pitchMax, float pitchMin, float pitchNeutral){
        this.parent = parent;
        this.pitchMax = pitchMax;
        this.pitchMin = pitchMin;
        this.pitchNeutral = pitchNeutral;
        this.rotatorBoneName = rotatorBoneName;
    }
    
    public void setPitch(float pitch){
        this.pitch = pitch;
    }
    
    public float getPitch(){
        return pitch;
    }
    
    public String getRotatorBoneName() {
        return rotatorBoneName;
    }
    
}
