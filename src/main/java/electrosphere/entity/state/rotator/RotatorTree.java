package electrosphere.entity.state.rotator;

import java.util.LinkedList;
import java.util.List;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.view.ViewUtils;
import electrosphere.renderer.actor.Actor;
import electrosphere.renderer.actor.ActorBoneRotator;

public class RotatorTree implements BehaviorTree{

    public static enum RotatorTreeState {
        ACTIVE,
        INACTIVE,
    }

    RotatorTreeState state;

    Entity parent;
    Actor entityActor;

    List<RotatorHierarchyNode> nodes = new LinkedList<RotatorHierarchyNode>();

    public RotatorTree(Entity parent){
        this.parent = parent;
        entityActor = EntityUtils.getActor(parent);
        state = RotatorTreeState.INACTIVE;
    }

    public void setActive(boolean isActive){
        if(isActive){
            state = RotatorTreeState.ACTIVE;
        } else {
            state = RotatorTreeState.INACTIVE;
            //clear all modifications we've made up to this point
            for(RotatorHierarchyNode node : nodes){
                entityActor.getAnimationData().getBoneRotator(node.getBone()).getRotation().identity();
            }
        }
    }

    public void simulate(float deltaTime){
        if(entityActor.modelIsLoaded() && this.state == RotatorTreeState.ACTIVE){
            for(RotatorHierarchyNode node : nodes){
                applyRotatorNode(node);
            }
        }
    }

    public void applyRotatorNode(RotatorHierarchyNode node){
        //apply
        // String nodeBoneName = node.getBone();
        // Quaterniond currentRotation = entityActor.getBoneRotation(nodeBoneName);
        for(RotatorConstraint constraint : node.getRotatorContraints()){
            // float allowedMarginPitch = constraint.getAllowedMarginPitch();
            // float allowedMarginYaw = constraint.getAllowedMarginYaw();
            boolean followsBone = constraint.getFollowsBone();
            boolean followsView = constraint.getFollowsView();
            if(followsBone){
                // String parentBone = constraint.getParentBone();
                // Quaterniond parentBoneRotation = entityActor.getBoneRotation(parentBone);
                // currentRotation.
            }
            if(followsView){
                ActorBoneRotator currentRotator = entityActor.getAnimationData().getBoneRotator(node.getBone());
                //apparently this isn't needed?
                //not sure I understand the math on this one
                // Vector3d facingVector = CreatureUtils.getFacingVector(parent);
                // Vector3f rotationAxis = new Vector3f((float)facingVector.x,(float)facingVector.y,(float)facingVector.z).rotateY((float)Math.PI/2.0f).normalize();
                Vector3f rotationAxis = new Vector3f(1,0,0);
                float rotationRaw = 0.0f;
                if(parent == Globals.clientState.playerEntity){
                    rotationRaw = Globals.cameraHandler.getPitch();
                } else {
                    rotationRaw = ViewUtils.getPitch(parent);
                }
                float rotation = (float)(rotationRaw * Math.PI / 180.0f);
                if(Math.abs(rotation) > constraint.getAllowedMarginPitch()){
                    rotation = (float)Math.copySign(constraint.allowedMarginPitch, rotation);
                }
                currentRotator.getRotation().identity().rotationAxis(rotation, rotationAxis);
            }
        }
        //recurse to children
        // for(RotatorHierarchyNode child : node.getChildren()){
        //     applyRotatorNode(child);
        // }
    }

    public void addRotatorNode(RotatorHierarchyNode node){
        nodes.add(node);
    }

    public float calculateYawOfQuat(Quaternionf quat){
        return (float)Math.atan2(2.0*(quat.y*quat.z + quat.w*quat.x), quat.w*quat.w - quat.x*quat.x - quat.y*quat.y + quat.z*quat.z);
    }

    public float calculatePitchOfQuat(Quaternionf quat){
        return (float)Math.asin(-2.0*(quat.x*quat.z - quat.w*quat.y));
    }

    public static RotatorTree getClientRotatorTree(Entity parent){
        return (RotatorTree)parent.getData(EntityDataStrings.CLIENT_ROTATOR_TREE);
    }
    
}
