package electrosphere.data.entity.creature.rotator;

public class RotatorConstraint {
    
    boolean followsView;
    boolean followsBone;
    String parentBone;
    float allowedMarginPitch;
    float allowedMarginYaw;

    public boolean getFollowsView(){
        return followsView;
    }

    public boolean getFollowsBone(){
        return followsBone;
    }

    public String getParentBone(){
        return parentBone;
    }

    public float getAllowedMarginPitch(){
        return allowedMarginPitch;
    }

    public float getAllowedMarginYaw(){
        return allowedMarginYaw;
    }

}
