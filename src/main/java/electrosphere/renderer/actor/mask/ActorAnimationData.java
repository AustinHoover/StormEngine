package electrosphere.renderer.actor.mask;

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
import electrosphere.renderer.actor.Actor;
import electrosphere.renderer.actor.ActorBoneRotator;
import electrosphere.renderer.actor.ActorStaticMorph;
import electrosphere.renderer.model.Bone;
import electrosphere.renderer.model.Model;

/**
 * The data about animations for a given actor
 */
public class ActorAnimationData {

    /**
     * Returned when the current time is requested of an animation that the actor is not playing
     */
    public static final int INVALID_ANIMATION = -1;

    /**
     * The parent actor
     */
    private final Actor parent;

    /**
     * scales the time that animations are played at
     */
    private float animationScalar = 1.0f;
    
    /**
     * The stack of animations being applied to a given actor
     */
    private Set<ActorAnimationMaskEntry> animationQueue = new TreeSet<ActorAnimationMaskEntry>();

    /**
     * Used for caching animation masks that should be removed
     */
    private List<ActorAnimationMaskEntry> toRemoveMasks = new LinkedList<ActorAnimationMaskEntry>();

    /**
     * The list of bone groups
     */
    private List<BoneGroup> boneGroups;

    /**
     * static morph for this specific actor
     */
    private ActorStaticMorph staticMorph;

    /**
     * bone rotators
     */
    private Map<String,ActorBoneRotator> boneRotators = new HashMap<String,ActorBoneRotator>();

    //
    //
    //           DATA        CACHING
    //
    //

    /**
     * Stores the positions of bones as they are updated
     */
    private Map<String,Vector3d> bonePositionMap = new HashMap<String,Vector3d>();

    /**
     * Stores the rotations of bones as they are updated
     */
    private Map<String,Quaterniond> boneRotationMap = new HashMap<String,Quaterniond>();






    /**
     * Constructor
     * @param parent The parent actor
     */
    public ActorAnimationData(Actor parent){
        this.parent = parent;
    }

    /**
     * Increments the animation time of the actor
     * @param deltaTime The amount of time to increment by
     */
    public void incrementAnimationTime(double deltaTime){
        toRemoveMasks.clear();
        for(ActorAnimationMaskEntry mask : animationQueue){
            if(mask.getFreezeFrames() > 0){
                mask.setFreezeFrames(mask.getFreezeFrames() - 1);
            } else {
                mask.setTime(mask.getTime() + deltaTime * animationScalar);
                if(mask.getTime() > mask.getDuration()){
                    toRemoveMasks.add(mask);
                }
            }
        }
        for(ActorAnimationMaskEntry mask : toRemoveMasks){
            animationQueue.remove(mask);
        }
    }
    
    /**
     * Gets the current time of the given animation that is being played on this actor
     * @param animation The animation's name
     * @return The time into the animation, -1 if the animation is not being played
     */
    public double getAnimationTime(String animation){
        ActorAnimationMaskEntry mask = this.getAnimationMask(animation);
        if(mask != null){
            return mask.getTime();
        }
        return INVALID_ANIMATION;
    }

    /**
     * Checks if an animation is being played on any meshes
     * @param animationName The animation name
     * @return true if the animation is being played on any meshes, false if the provided animation name is null or the animation is not being played on any meshes
     */
    public boolean isPlayingAnimation(String animationName){
        if(animationName == null){
            return false;
        }
        for(ActorAnimationMaskEntry mask : animationQueue){
            if(mask.getAnimationName().equals(animationName)){
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if an animation is being played on any meshes
     * @param animationData The animation data
     * @return true if the animation is being played on any meshes, false if the provided animation name is null or the animation is not being played on any meshes
     */
    public boolean isPlayingAnimation(TreeDataAnimation animationData){
        if(animationData == null){
            return false;
        }
        for(ActorAnimationMaskEntry mask : animationQueue){
            if(animationData.getNameFirstPerson() != null && mask.getAnimationName().contains(animationData.getNameFirstPerson())){
                return true;
            }
            if(animationData.getNameThirdPerson() != null && mask.getAnimationName().contains(animationData.getNameThirdPerson())){
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the actor is playing an animation
     * @return true if it is playing an animation, false otherwise
     */
    public boolean isPlayingAnimation(){
        return animationQueue.size() > 0;
    }

    /**
     * Stops playing an animation on the actor
     * @param animationName The name of the animation
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
     * Plays an animation provided as a string with a given priority
     * @param animationName The name of the animation
     * @param priority The priority of the animation
     */
    public void playAnimation(String animationName, int priority){
        Model model = Globals.assetManager.fetchModel(parent.getBaseModelPath());
        if(model != null && model.getAnimation(animationName) != null){
            double length = model.getAnimation(animationName).duration;
            ActorAnimationMaskEntry animMask = new ActorAnimationMaskEntry(priority, animationName, 0, length);
            for(Bone bone : model.getBones()){
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
    public void playAnimation(TreeDataAnimation animation, boolean isThirdPerson){

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

                if(group != null && isThirdPerson == true && group.getBoneNamesThirdPerson() != null && group.getBoneNamesThirdPerson().size() > 0){
                    boneMask.addAll(group.getBoneNamesThirdPerson());
                }
                if(group != null && isThirdPerson == false && group.getBoneNamesFirstPerson() != null &&  group.getBoneNamesFirstPerson().size() > 0){
                    boneMask.addAll(group.getBoneNamesFirstPerson());
                }
            }
        } else if(animation.getBoneGroups() != null && this.boneGroups == null){
            LoggerInterface.loggerRenderer.WARNING(
                "Trying to play animation on Actor that uses bone groups, but the Actor's bone group isn't defined!\n" +
                "Model path: " + parent.getBaseModelPath() + "\n" +
                "Animation name: " + animationName + "\n"
            );
        } else if(animation.getBoneGroups() == null){
            Model model = Globals.assetManager.fetchModel(parent.getBaseModelPath());
            if(model != null){
                boneMask = new LinkedList<String>();
                for(Bone bone : model.getBones()){
                    boneMask.add(bone.boneID);
                }
            }
        }


        Model model = Globals.assetManager.fetchModel(parent.getBaseModelPath());
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
     * Plays an animation on a specified set of bones
     * @param animationName The name of the animation
     * @param priority The priority of the animation
     * @param boneMask The set of bones to play the animation on
     */
    public void playAnimationWithMask(String animationName, int priority, List<String> boneMask){
        Model model = Globals.assetManager.fetchModel(parent.getBaseModelPath());
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
     * Gets the animation queue
     * @return The animation queue
     */
    public Set<ActorAnimationMaskEntry> getAnimationQueue(){
        return animationQueue;
    }

    /**
     * Applies the animation masks in this actor to the provided model
     * @param model The model
     */
    public void applyAnimationMasks(Model model){
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
     * Calculates the node transforms for the actor
     * @param model The model that backs the actor
     */
    public void calculateNodeTransforms(Model model){
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
    
    public void setAnimationScalar(float animationScalar) {
        this.animationScalar = animationScalar;
    }

    /**
     * Gets the animation mask for a given animation
     * @param animationName The animation's name
     * @return The animation mask if the actor is playing the animation, null otherwise
     */
    public ActorAnimationMaskEntry getAnimationMask(String animationName){
        for(ActorAnimationMaskEntry mask : this.getAnimationQueue()){
            if(mask.getAnimationName().equals(animationName)){
                return mask;
            } else if(mask.getAnimationName().equalsIgnoreCase(animationName)){
                LoggerInterface.loggerEngine.WARNING("Animation mask failed to find, but there is an animation with a very similar name! " + animationName + " vs " + mask.getAnimationName());
            }
        }
        return null;
    }

    /**
     * Adds a rotator to a bone on this actor
     * @param bone The name of the bone
     * @param rotator The rotator
     */
    public void addBoneRotator(String bone, ActorBoneRotator rotator){
        boneRotators.put(bone, rotator);
    }

    /**
     * Gets the rotator to apply to a bone
     * @param bone The name of the bone
     * @return The rotator to apply to that bone if it exists, null otherwise
     */
    public ActorBoneRotator getBoneRotator(String bone){
        return boneRotators.get(bone);
    }

    /**
     * Sets the static morph for this actor
     * @param staticMorph The static morph
     */
    public void setActorStaticMorph(ActorStaticMorph staticMorph){
        this.staticMorph = staticMorph;
    }

    /**
     * Gets the static morph for this actor
     * @return The static morph for this actor
     */
    public ActorStaticMorph getStaticMorph(){
        return this.staticMorph;
    }

    /**
     * Sets the bone groups in the actor
     * @param boneGroups The bone groups
     */
    public void setBoneGroups(List<BoneGroup> boneGroups){
        this.boneGroups = boneGroups;
    }

    /**
     * Sets the number of freeze frames for a given animation path if the animation is being played
     * @param animationPath The path to the animation
     * @param numFrames The number of frames to freeze for
     */
    public void setFreezeFrames(String animationPath, int numFrames){
        if(numFrames < 1){
            throw new Error("Num frames less than 1 !" + numFrames);
        }
        for(ActorAnimationMaskEntry mask : this.animationQueue){
            if(mask.getAnimationName().contains(animationPath)){
                mask.setFreezeFrames(numFrames);
                break;
            }
        }
    }

    /**
     * Gets the position of a bone in local space
     * @param boneName The name of the bone
     * @return The vector3d containing the position of the bone, or a vector of (0,0,0) if the model lookup fails
     * //TODO: refactor to make failure more transparent (both for model not existing and bone not existing)
     */
    public Vector3d getBonePosition(String boneName){
        if(bonePositionMap.containsKey(boneName)){
            return bonePositionMap.get(boneName);
        }
        Vector3d rVal = new Vector3d();
        Model model = Globals.assetManager.fetchModel(parent.getBaseModelPath());
        if(model != null){
            this.applyAnimationMasks(model);
            this.calculateNodeTransforms(model);
            Bone currentBone = model.getBoneMap().get(boneName);
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
                throw new Error(message);
            }
        }
        if(!Double.isFinite(rVal.x)){
            throw new Error("Bone position that is not finite!");
        }
        return rVal;
    }
    
    /**
     * Gets the rotation of a bone in local space
     * @param boneName The name of the bone
     * @return The Quaterniond containing the rotation of the bone, or an identity Quaterniond if the lookup fails
     */
    public Quaterniond getBoneRotation(String boneName){
        if(boneRotationMap.containsKey(boneName)){
            return boneRotationMap.get(boneName);
        }
        Quaterniond rVal = new Quaterniond();
        Model model = Globals.assetManager.fetchModel(parent.getBaseModelPath());
        if(model != null){
            this.applyAnimationMasks(model);
            this.calculateNodeTransforms(model);
            Bone currentBone = model.getBoneMap().get(boneName);
            if(currentBone != null){
                Quaterniond rotation = new Matrix4d(currentBone.getFinalTransform()).getNormalizedRotation(new Quaterniond());
                rVal.set(rotation);
            } else {
                String message = "Trying to get rotation of bone that does not exist on model!\n" +
                "boneName: " + boneName;
                throw new IllegalArgumentException(message);
            }
        }
        if(!Double.isFinite(rVal.x)){
            throw new IllegalStateException("Bone rotation that is not finite!");
        }
        return rVal;
    }
    
    /**
     * Gets the bone transform for a bone on this actor
     * @param boneName The name of the bone
     * @return The transform
     */
    public Matrix4d getBoneTransform(String boneName){
        Matrix4d rVal = new Matrix4d();
        Model model = Globals.assetManager.fetchModel(parent.getBaseModelPath());
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
        Model model = Globals.assetManager.fetchModel(parent.getBaseModelPath());
        if(model != null){
            applyAnimationMasks(model);
            calculateNodeTransforms(model);
            return model.getBones();
        }
        return null;
    }

    /**
     * Gets the value of a single bone
     * @return the bone
     */
    public Bone getBone(String boneName){
        Model model = Globals.assetManager.fetchModel(parent.getBaseModelPath());
        if(model != null){
            return model.getBoneMap().get(boneName);
        }
        return null;
    }

    /**
     * Checks if the actor contains a bone
     * @param boneName The name of the bone
     * @return true if it exists, false otherwise
     */
    public boolean containsBone(String boneName){
        Model model = Globals.assetManager.fetchModel(parent.getBaseModelPath());
        if(model != null){
            return model.getBoneMap().containsKey(boneName);
        }
        return false;
    }

}
