package electrosphere.entity.state.movement;

import org.joml.Quaterniond;

import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;

public class ApplyRotationTree implements BehaviorTree {
    
    public static enum ApplyRotationTreeState {
        ROTATE,
        NO_ROTATE,
    }


    Quaterniond rotationToApply;
    Entity parent;
    ApplyRotationTreeState state = ApplyRotationTreeState.ROTATE;

    public ApplyRotationTree(Entity parent, Quaterniond rotationToApply){
        this.parent = parent;
        this.rotationToApply = rotationToApply;
    }

    public void start(){
        state = ApplyRotationTreeState.ROTATE;
    }

    public void stop(){
        state = ApplyRotationTreeState.NO_ROTATE;
    }

    @Override
    public void simulate(float deltaTime){
        switch(state){
            case ROTATE:
            EntityUtils.getRotation(parent).mul(rotationToApply).normalize();
            break;
            case NO_ROTATE:
            break;
        }
    }

    

}
