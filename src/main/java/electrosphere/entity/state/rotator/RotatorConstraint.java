package electrosphere.entity.state.rotator;

public class RotatorConstraint {
    
    boolean followsView;
    boolean followsBone;
    String parentBone;
    float allowedMarginPitch;
    float allowedMarginYaw;

    public RotatorConstraint(electrosphere.data.entity.creature.rotator.RotatorConstraint data){
        followsBone = data.getFollowsBone();
        followsView = data.getFollowsView();
        parentBone = data.getParentBone();
        allowedMarginPitch = data.getAllowedMarginPitch();
        allowedMarginYaw = data.getAllowedMarginYaw();
    }

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
