package electrosphere.server.entity.poseactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AIBone;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AIScene;

import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.mem.JomlPool;
import electrosphere.renderer.actor.ActorBoneRotator;
import electrosphere.renderer.actor.ActorStaticMorph;
import electrosphere.renderer.anim.AnimChannel;
import electrosphere.renderer.anim.AnimNode;
import electrosphere.renderer.anim.Animation;
import electrosphere.renderer.loading.ModelPretransforms;
import electrosphere.renderer.model.Bone;

/**
 * Used server side to load model data for positioning hitboxes based on animations
 */
public class PoseModel {

    List<Bone> bones;
    Map<String,Bone> boneMap;
    Matrix4d globalInverseTransform;
    Matrix4d rootTransform;
    Map<String,AnimNode> nodeMap;
    AnimNode rootAnimNode;
    List<Animation> animations;
    Map<String, Animation> animMap;
    

    /**
     * Private constructor for static creation methods
     */
    private PoseModel(){
    }

    /**
     * Constructor
     * @param path Path on disk to this posemodel
     * @param scene The AI Scene parsed from the file on disk
     */
    public PoseModel(String path, AIScene scene){
        ModelPretransforms.ModelMetadata modelMetadata = Globals.modelPretransforms.getModel(path);
        ModelPretransforms.GlobalTransform globalTransform = null;
        if(modelMetadata != null){
            globalTransform = modelMetadata.getGlobalTransform();
        }

        bones = new ArrayList<Bone>();
        boneMap = new HashMap<String, Bone>();
        nodeMap = new HashMap<String, AnimNode>();
        animations = new ArrayList<Animation>();
        animMap = new HashMap<String, Animation>();
        
        //
        //parse bones
        //
        PointerBuffer meshesBuffer = scene.mMeshes();
        while(meshesBuffer.hasRemaining()){
            AIMesh currentMeshData = AIMesh.createSafe(meshesBuffer.get());
            LoggerInterface.loggerRenderer.DEBUG("mesh name:" + currentMeshData.mName().dataString());
            PointerBuffer boneBuffer = currentMeshData.mBones();
            for(int j = 0; j < currentMeshData.mNumBones(); j++){
                AIBone currentBone = AIBone.createSafe(boneBuffer.get());
                String currentBoneName = currentBone.mName().dataString();
                if(!boneMap.containsKey(currentBoneName)){
                    Bone boneObject = new Bone(currentBone);
                    boneMap.put(currentBoneName, boneObject);
                    bones.add(boneObject);
                }
            }
        }

        //
        //parse animation nodes and form hierarchy
        //
        AINode rootNode = scene.mRootNode();
        rootTransform = electrosphere.util.Utilities.convertAIMatrixd(rootNode.mTransformation());
        if(globalTransform != null){
            globalInverseTransform = new Matrix4d(rootTransform).invert().scale(globalTransform.getScale());
            globalInverseTransform.scale(globalTransform.getScale());
        } else {
            globalInverseTransform = new Matrix4d();
        }
        rootAnimNode = buildAnimNodeMap(scene.mRootNode(),null);

        //
        //load animations
        //
        int animCount = scene.mNumAnimations();
        PointerBuffer animBuffer = scene.mAnimations();
        animations = new ArrayList<Animation>();
        animMap = new HashMap<String,Animation>();
        for(int i = 0; i < animCount; i++){
            Animation newAnim = new Animation(AIAnimation.create(animBuffer.get(i)));
            animations.add(newAnim);
            animMap.put(newAnim.name,newAnim);
        }
    }

    /**
     * Constructor
     * @param path Path on disk to this posemodel
     * @param scene The AI Scene parsed from the file on disk
     */
    public static PoseModel createEmpty(){
        PoseModel rVal = new PoseModel();
        rVal.bones = new ArrayList<Bone>();
        rVal.boneMap = new HashMap<String, Bone>();
        rVal.nodeMap = new HashMap<String, AnimNode>();
        rVal.animations = new ArrayList<Animation>();
        rVal.animMap = new HashMap<String, Animation>();
        return rVal;
    }


    /**
     * Applies an animation to a certain set of bones
     * @param animationName The name of the animation
     * @param time The time in the animation to apply
     * @param mask The set of bones to apply the animation to
     */
    public void applyAnimationMask(String animationName, double time, List<String> mask){
        Animation animationCurrent = animMap.get(animationName);
        if(animationCurrent != null){
            for(String boneName : mask){
                AnimChannel currentChannel = animationCurrent.getChannel(boneName);
                Bone currentBone = boneMap.get(currentChannel.getNodeID());
                currentChannel.setTime(time);
                // System.out.println(currentChannel + " " + currentBone);
                if(currentBone != null){
                    // System.out.println("Applying to bone");
                    //T * S * R
                    Matrix4d deform = new Matrix4d();
                    deform.translate(currentChannel.getCurrentPosition());
                    deform.rotate(currentChannel.getCurrentRotation());
                    deform.scale(new Vector3d(currentChannel.getCurrentScale()));
                    currentBone.setDeform(deform);
                }
            }
        }
    }


    /**
     * Builds an AnimNode map based on an AINode from assimp
     * @param node The AINode from assimp
     * @param parent The parent AnimNode if it exists
     * @return The AnimNode map relative to this node
     */
    public final AnimNode buildAnimNodeMap(AINode node, AnimNode parent){
        AnimNode node_object = new AnimNode(node.mName().dataString(), parent, node);
        nodeMap.put(node_object.id, node_object);
        if(boneMap.containsKey(node_object.id)){
            node_object.is_bone = true;
        }
        int num_children = node.mNumChildren();
        for(int i = 0; i < num_children; i++){
            AnimNode temp_child = buildAnimNodeMap(AINode.create(node.mChildren().get(i)),node_object);
            node_object.children.add(temp_child);
        }
        return node_object;
    }


    /**
     * Updates node transforms based on bone rotators and static morph
     * @param boneRotators The bone rotators
     * @param staticMorph The static morph
     */
    public void updateNodeTransform(Map<String,ActorBoneRotator> boneRotators, ActorStaticMorph staticMorph){
        if(this.rootAnimNode != null){
            this.updateNodeTransform(this.rootAnimNode,boneRotators,staticMorph);
        }
    }

    /**
     * Gets the map of bone name -> bone
     * @return the map
     */
    public Map<String,Bone> getBoneMap(){
        return boneMap;
    }

    /**
     * Internal recursive method behind the public updateNodeTransform
     * @param boneRotators The bone rotators
     * @param staticMorph The static morph
     */
    private void updateNodeTransform(AnimNode n, Map<String,ActorBoneRotator> boneRotators, ActorStaticMorph staticMorph){
        Matrix4d poolMat = JomlPool.getMat();
        //grab parent transform if exists
        if(n.parent != null){
            n.parent.getTransform(poolMat);
        }
        //if this is a bone, calculate the transform for the bone
        if(n.is_bone){
            Bone target_bone = boneMap.get(n.id);
            if(target_bone == null){
                String message = "Failed to locate bone!\n";
                message = message + "bone id: " + n.id + "\n";
                message = message + "bone map size: " + boneMap.size() + "\n";
                message = message + "bone map key set: " + boneMap.keySet() + "\n";
                throw new Error(message);
            }
            poolMat.mul(target_bone.getDeform());
            if(boneRotators.containsKey(target_bone.boneID)){
                poolMat.rotate(boneRotators.get(target_bone.boneID).getRotation());
            }
            n.setTransform(poolMat);
            if(staticMorph != null && staticMorph.getBoneTransforms(n.id) != null){
                poolMat.mul(staticMorph.getBoneTransforms(n.id).getTransform());
            }
            //
            //Calculate final offset from initial bone
            //https://stackoverflow.com/a/59869381
            poolMat.mul(target_bone.getMOffset());
            poolMat = globalInverseTransform.mul(poolMat, poolMat);
            target_bone.setFinalTransform(poolMat);
        } else {
            //not a bone, so use transform directly from data
            n.setTransform(poolMat.mul(electrosphere.util.Utilities.convertAIMatrix(n.raw_data.mTransformation())));
        }
        //update all children accordingly
        Iterator<AnimNode> node_iterator = n.children.iterator();
        while(node_iterator.hasNext()){
            AnimNode current_node = node_iterator.next();
            this.updateNodeTransform(current_node,boneRotators,staticMorph);
        }
        JomlPool.release(poolMat);
    }

    /**
     * Gets an animation with a specific name
     * @param animName The name of the animation
     * @return The animation if it exists, null otherwise
     */
    public Animation getAnimation(String animName){
        return animMap.get(animName);
    }

    /**
     * Logs all animations for a given model
     */
    public void describeAllAnimations(){
        if(animations.size() > 0){
            LoggerInterface.loggerRenderer.DEBUG("=====================");
            LoggerInterface.loggerRenderer.DEBUG(animations.size() + " animations available in model!");
            Iterator<Animation> animIterator = animations.iterator();
            while(animIterator.hasNext()){
                Animation currentAnim = animIterator.next();
                currentAnim.describeAnimation();
            }
        }
    }

    /**
     * Gets the list of bones in this pose model
     * @return The list of bones
     */
    public List<Bone> getBones(){
        return this.bones;
    }

    /**
     * Delete - currently does nothing
     */
    public void delete(){

    }

}
