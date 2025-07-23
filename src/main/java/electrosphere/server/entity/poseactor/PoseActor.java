package electrosphere.server.entity.poseactor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector4d;

import electrosphere.data.entity.common.treedata.TreeDataAnimation;
import electrosphere.data.entity.creature.bonegroups.BoneGroup;
import electrosphere.engine.Globals;
import electrosphere.entity.state.AnimationPriorities;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.actor.ActorBoneRotator;
import electrosphere.renderer.actor.ActorStaticMorph;
import electrosphere.renderer.actor.mask.ActorAnimationMaskEntry;
import electrosphere.renderer.model.Bone;

/**
 * An actor that references a posemodel
 */
public class PoseActor {

    /**
     * The path of the model for the pose actor
     */
    String modelPath;

    /**
     * Scalar on the speed of animation playback
     */
    float animationScalar = 1.0f;

    /**
     * Priority queue of animations to play. Allows masking a higher priority animation over a lower priority one.
     */
    Set<ActorAnimationMaskEntry> animationQueue = new TreeSet<ActorAnimationMaskEntry>();

    /**
     * Bone rotation map. Used to apply rotator functionality to bones (think hair, cloth, and camera rotation on looking)
     */
    Map<String,ActorBoneRotator> boneRotators = new HashMap<String,ActorBoneRotator>();
    
    /**
     * Static morph used to apply an initial, static modification to the layout of bones in the pose model
     */
    ActorStaticMorph staticMorph;

    /**
     * The bone groups for this pose actor
     */
    List<BoneGroup> boneGroups;

    /**
     * Stores the positions of bones as they are updated
     */
    Map<String,Vector3d> bonePositionMap = new HashMap<String,Vector3d>();

    /**
     * Stores the rotations of bones as they are updated
     */
    Map<String,Quaterniond> boneRotationMap = new HashMap<String,Quaterniond>();


    /**
     * Constructor
     * @param modelPath The path on disk of this poseactor's posemodel
     */
    public PoseActor(String modelPath){
        this.modelPath = modelPath;
    }


    //Used to keep track of which animations have completed and therefore should be removed
    //Separate variable so no concurrent modification to anim lists/maps
    List<ActorAnimationMaskEntry> toRemoveMasks = new LinkedList<ActorAnimationMaskEntry>();
    
    /**
     * Increments time of all currently played animations
     * @param deltaTime
     */
    public void incrementAnimationTime(double deltaTime){
        toRemoveMasks.clear();
        for(ActorAnimationMaskEntry mask : animationQueue){
            mask.setTime(mask.getTime() + deltaTime * animationScalar);
            if(mask.getTime() > mask.getDuration()){
                toRemoveMasks.add(mask);
            }
        }
        for(ActorAnimationMaskEntry mask : toRemoveMasks){
            animationQueue.remove(mask);
        }
    }

    /**
     * Gets the current time of a given animation
     * @param animationName The animation name
     * @return The current time of the animation if it exists, -1.0f otherwise
     */
    public double getAnimationTime(String animationName){
        for(ActorAnimationMaskEntry mask : animationQueue){
            if(mask.getAnimationName().contains(animationName)){
                return mask.getTime();
            }
        }
        return -1.0f;
    }

    /**
     * Gets whether the animation is currently playing or not
     * @param animationName The animation name
     * @return True if the animation is playing, false otherwise
     */
    public boolean isPlayingAnimation(String animationName){
        for(ActorAnimationMaskEntry mask : animationQueue){
            if(mask.getAnimationName().contains(animationName)){
                return true;
            }
        }
        return false;
    }

    /**
     * Gets whether the animation is currently playing or not
     * @param animationData The animation
     * @return True if the animation is playing, false otherwise
     */
    public boolean isPlayingAnimation(TreeDataAnimation animationData){
        if(animationData == null){
            return false;
        }
        for(ActorAnimationMaskEntry mask : animationQueue){
            if(mask.getAnimationName().contains(animationData.getNameThirdPerson())){
                return true;
            }
        }
        return false;
    }

    /**
     * Gets whether the actor is playing ANY animation
     * @return True if the actor is playing ANY animation, false otherwise
     */
    public boolean isPlayingAnimation(){
        return animationQueue.size() > 0;
    }

    /**
     * Stops playing a specific animation 
     * @param animationName The name of the animation to stop playing
     */
    public void stopAnimation(String animationName){
        List<ActorAnimationMaskEntry> toRemove = new LinkedList<ActorAnimationMaskEntry>();
        for(ActorAnimationMaskEntry mask : animationQueue){
            if(mask.getAnimationName().contains(animationName)){
                toRemove.add(mask);
            }
        }
        for(ActorAnimationMaskEntry mask : toRemove){
            animationQueue.remove(mask);
        }
    }
    
    /**
     * Adds an animation to play at a specific priority level
     * @param animationName The name of the animation to play
     * @param priority The priority to play the animation at
     */
    public void playAnimation(String animationName, int priority){
        PoseModel model = Globals.assetManager.fetchPoseModel(modelPath);
        if(model != null && model.getAnimation(animationName) != null){
            double length = model.getAnimation(animationName).duration;
            ActorAnimationMaskEntry animMask = new ActorAnimationMaskEntry(priority, animationName, 0, length);
            for(Bone bone : model.bones){
                animMask.addBone(bone.boneID);
            }
            toRemoveMasks.clear();
            for(ActorAnimationMaskEntry currentMask : animationQueue){
                if(currentMask.getPriority() == animMask.getPriority()){
                    toRemoveMasks.add(currentMask);
                    break;
                }
            }
            for(ActorAnimationMaskEntry currentMask : toRemoveMasks){
                animationQueue.remove(currentMask);
            }
            animationQueue.add(animMask);
        }
    }

    /**
     * Plays animation data
     * @param animation The animation data
     * @param isThirdPerson true if is third person, false if is first person
     */
    public void playAnimation(TreeDataAnimation animation){

        //Get the animation's name
        String animationName = animation.getNameThirdPerson();

        //Get the animation's priority
        int priority = AnimationPriorities.getValue(AnimationPriorities.DEFAULT);
        if(animation.getPriority() != null){
            priority = animation.getPriority();
        }
        if(animation.getPriorityCategory() != null){
            priority = AnimationPriorities.getValue(animation.getPriorityCategory());
        }

        //Gets the mask
        List<String> boneMask = null;
        if(animation.getBoneGroups() != null && this.boneGroups != null){
            boneMask = new LinkedList<String>();
            for(String boneGroupName : animation.getBoneGroups()){
                BoneGroup group = null;

                for(BoneGroup currentGroup : this.boneGroups){
                    if(currentGroup.getId().equals(boneGroupName)){
                        group = currentGroup;
                        break;
                    }
                }

                if(group != null){
                    boneMask.addAll(group.getBoneNamesThirdPerson());
                }
            }
        } else if(animation.getBoneGroups() != null && this.boneGroups == null){
            LoggerInterface.loggerRenderer.WARNING(
                "Trying to play animation on PoseActor that uses bone groups, but the PoseActor's bone group isn't defined!\n" +
                "Model path: " + modelPath + "\n" +
                "Animation name: " + animationName + "\n"
            );
        } else if(animation.getBoneGroups() == null){
            PoseModel model = Globals.assetManager.fetchPoseModel(this.modelPath);
            if(model != null){
                boneMask = new LinkedList<String>();
                for(Bone bone : model.getBones()){
                    boneMask.add(bone.boneID);
                }
            }
        }


        PoseModel model = Globals.assetManager.fetchPoseModel(modelPath);
        if(model != null && model.getAnimation(animationName) != null){

            //get data from the actual animation in the model
            double length = model.getAnimation(animationName).duration;

            //construct the animation mask
            ActorAnimationMaskEntry animMask;
            if(boneMask == null){
                animMask = new ActorAnimationMaskEntry(
                    priority,
                    animationName,
                    length
                );
            } else {
                animMask = new ActorAnimationMaskEntry(
                    priority,
                    animationName,
                    length,
                    boneMask
                );
            }

            //if a mask wasn't defined, apply this mask to all animations
            if(boneMask == null){
                for(Bone bone : model.getBones()){
                    animMask.addBone(bone.boneID);
                }
            }


            //clear existing masks that are lower priority
            toRemoveMasks.clear();
            for(ActorAnimationMaskEntry currentMask : animationQueue){
                if(currentMask.getPriority() == animMask.getPriority()){
                    toRemoveMasks.add(currentMask);
                    break;
                }
            }
            for(ActorAnimationMaskEntry currentMask : toRemoveMasks){
                animationQueue.remove(currentMask);
            }
            animationQueue.add(animMask);
        }
    }

    /**
     * Interrupts an animation, thereby causing it to stop playing
     * @param animation The animation to interrupt
     */
    public void interruptAnimation(TreeDataAnimation animation, boolean isThirdPerson){
        //Get the animation's name
        String animationName = "";
        if(isThirdPerson){
            animationName = animation.getNameThirdPerson();
        } else {
            animationName = animation.getNameFirstPerson();
        }

        //Get the animation's priority
        int priority = AnimationPriorities.getValue(AnimationPriorities.DEFAULT);
        if(animation.getPriority() != null){
            priority = animation.getPriority();
        }
        if(animation.getPriorityCategory() != null){
            priority = AnimationPriorities.getValue(animation.getPriorityCategory());
        }

        toRemoveMasks.clear();
        for(ActorAnimationMaskEntry mask : this.animationQueue){
            if(mask.getAnimationName() == animationName && mask.getPriority() == priority){
                toRemoveMasks.add(mask);
            }
        }
        for(ActorAnimationMaskEntry currentMask : toRemoveMasks){
            animationQueue.remove(currentMask);
        }
    }

    /**
     * Play an animation with a mask that makes the animation only apply to specific bones
     * @param animationName The name of the animation
     * @param priority The priority to play it at
     * @param boneMask The mask of bones that the animation should apply to
     */
    public void playAnimationWithMask(String animationName, int priority, List<String> boneMask){
        PoseModel model = Globals.assetManager.fetchPoseModel(modelPath);
        if(model != null){
            double length = model.getAnimation(animationName).duration;
            ActorAnimationMaskEntry animMask = new ActorAnimationMaskEntry(priority, animationName, 0, length, boneMask);
            toRemoveMasks.clear();
            for(ActorAnimationMaskEntry currentMask : animationQueue){
                if(currentMask.getPriority() == animMask.getPriority()){
                    toRemoveMasks.add(currentMask);
                    break;
                }
            }
            for(ActorAnimationMaskEntry currentMask : toRemoveMasks){
                animationQueue.remove(currentMask);
            }
            animationQueue.add(animMask);
        }
    }

    /**
     * Applies an animation mask to the PoseModel
     * @param model The posemodel to apply the mask to
     */
    private void applyAnimationMasks(PoseModel model){
        List<String> bonesUsed = new LinkedList<String>();
        List<String> currentAnimationMask = new LinkedList<String>();
        for(ActorAnimationMaskEntry mask : animationQueue){
            currentAnimationMask.clear();
            for(String currentBone : mask.getBones()){
                if(!bonesUsed.contains(currentBone)){
                    bonesUsed.add(currentBone);
                    currentAnimationMask.add(currentBone);
                }
            }
            model.applyAnimationMask(mask.getAnimationName(), mask.getTime(), currentAnimationMask);
        }
    }

    /**
     * Gets the animation queue
     * @return The animation queue
     */
    public Set<ActorAnimationMaskEntry> getAnimationQueue(){
        return animationQueue;
    }

    /**
     * Calculates all node transforms for the PoseModel based on bone rotators and the static morph of this PoseActor
     * @param model The PoseModel to calculate transforms for
     */
    private void calculateNodeTransforms(PoseModel model){
        model.updateNodeTransform(boneRotators,staticMorph);
        for(Bone bone : model.getBones()){
            //store position
            Matrix4d betweenMat = new Matrix4d(bone.getMOffset()).invert();
            Vector4d result = betweenMat.transform(new Vector4d(0,0,0,1));
            betweenMat.set(bone.getFinalTransform());
            result = betweenMat.transform(result);
            this.bonePositionMap.put(bone.boneID,new Vector3d(result.x,result.y,result.z));
            //store rotation
            Quaterniond rotation = new Matrix4d(bone.getFinalTransform()).getNormalizedRotation(new Quaterniond());
            this.boneRotationMap.put(bone.boneID,rotation);
        }
    }

    /**
     * Updates the transform cache for this actor
     */
    public void updateTransformCache(){
        PoseModel model = Globals.assetManager.fetchPoseModel(modelPath);
        if(model != null){
            this.applyAnimationMasks(model);
            this.calculateNodeTransforms(model);
        }
    }

    /**
     * Sets the animation scalar
     * @param animationScalar The new animation scalar value
     */
    public void setAnimationScalar(float animationScalar) {
        this.animationScalar = animationScalar;
    }

    /**
     * Sets the static morph of this pose actor
     * @param staticMorph The static morph to set
     */
    public void setStaticMorph(ActorStaticMorph staticMorph){
        this.staticMorph = staticMorph;
    }


    /**
     * Adds a bone rotator to the pose actor
     * @param boneRotator The bone rotator
     */
    public void addBoneRotator(String boneName, ActorBoneRotator boneRotator){
        boneRotators.put(boneName, boneRotator);
    }

    /**
     * Gets a specific bone rotator by name
     * @param bone The name of the bone
     * @return The bone rotator for that bone
     */
    public ActorBoneRotator getBoneRotator(String bone){
        return boneRotators.get(bone);
    }

    /**
     * Gets the current rotation of a specific bone
     * @param boneName The name of the bone
     * @return The rotation quaternion of the bone
     */
    public Quaterniond getBoneRotation(String boneName){
        if(boneRotationMap.containsKey(boneName)){
            return boneRotationMap.get(boneName);
        }
        Quaterniond rVal = new Quaterniond();
        PoseModel model = Globals.assetManager.fetchPoseModel(modelPath);
        if(model != null){
            applyAnimationMasks(model);
            calculateNodeTransforms(model);
                Bone currentBone = model.boneMap.get(boneName);
                if(currentBone != null){
                    Quaterniond rotation = new Matrix4d(currentBone.getFinalTransform()).getNormalizedRotation(new Quaterniond());
                    rVal.set(rotation);
                }
        }
        return rVal;
    }

    /**
     * Checks if the bone exists on the actor
     * @param boneName The name of the bone
     * @return true if it exists, false otherwise
     */
    public boolean containsBone(String boneName){
        PoseModel model = Globals.assetManager.fetchPoseModel(modelPath);
        if(model != null){
            return model.boneMap.containsKey(boneName);
        }
        return false;
    }

    /**
     * Gets the model path associated with the pose actor
     * @return The model path
     */
    public String getModelPath(){
        return modelPath;
    }

    /**
     * Gets the position of a bone currently
     * @param boneName
     * @return
     */
    public Vector3d getBonePosition(String boneName){
        if(bonePositionMap.containsKey(boneName)){
            return bonePositionMap.get(boneName);
        }
        Vector3d rVal = new Vector3d();
        PoseModel model = Globals.assetManager.fetchPoseModel(modelPath);
        if(model != null){
            applyAnimationMasks(model);
            calculateNodeTransforms(model);
            Bone currentBone = model.boneMap.get(boneName);
            if(currentBone != null){
                Matrix4d betweenMat = new Matrix4d(currentBone.getMOffset()).invert();
                Vector4d result = betweenMat.transform(new Vector4d(rVal.x,rVal.y,rVal.z,1));
                betweenMat.set(currentBone.getFinalTransform());
                result = betweenMat.transform(result);
                rVal.x = (float)result.x;
                rVal.y = (float)result.y;
                rVal.z = (float)result.z;
            } else {
                String message = "Trying to get position of bone that does not exist on model!\n" +
                "boneName: " + boneName;
                throw new IllegalArgumentException(message);
            }
        }
        if(!Double.isFinite(rVal.x)){
            throw new IllegalStateException("Bone position that is not finite!");
        }
        return rVal;
    }

    /**
     * Gets the transform of a given bone
     * @param boneName The name of the bone
     * @return The transform
     */
    public Matrix4d getBoneTransform(String boneName){
        Matrix4d rVal = new Matrix4d();
        PoseModel model = Globals.assetManager.fetchPoseModel(modelPath);
        if(model != null){
            applyAnimationMasks(model);
            calculateNodeTransforms(model);
            Bone currentBone = model.getBoneMap().get(boneName);
            if(currentBone != null){
                rVal.set(currentBone.getFinalTransform());
            } else {
                throw new IllegalArgumentException("Trying to get rotation of bone that does not exist on model!");
            }
        }
        if(!Double.isFinite(rVal.m00())){
            throw new IllegalStateException("Bone rotation that is not finite!");
        }
        return rVal;
    }

    /**
     * Gets the list of all bones
     * @return the list of all bones
     */
    public List<Bone> getBoneValues(){
        PoseModel model = Globals.assetManager.fetchPoseModel(modelPath);
        if(model != null){
            applyAnimationMasks(model);
            calculateNodeTransforms(model);
            return model.getBones();
        }
        return null;
    }

    /**
     * Sets the bone groups
     * @param boneGroups The bone groups
     */
    public void setBoneGroups(List<BoneGroup> boneGroups){
        this.boneGroups = boneGroups;
    }

    /**
     * Checks if the pose model is loaded
     * @return True if the pose model is loaded, false otherwise
     */
    public boolean modelIsLoaded(){
        PoseModel model = Globals.assetManager.fetchPoseModel(modelPath);
        if(model != null){
            return true;
        } else {
            return false;
        }
    }
    
}
