package electrosphere.renderer.model;

import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.actor.ActorBoneRotator;
import electrosphere.renderer.actor.ActorStaticMorph;
import electrosphere.renderer.actor.mask.ActorMeshMask;
import electrosphere.renderer.actor.mask.ActorTextureMask;
import electrosphere.renderer.anim.AnimChannel;
import electrosphere.renderer.anim.Animation;
import electrosphere.renderer.loading.ModelPretransforms;
import electrosphere.renderer.meshgen.MeshLoader;
import electrosphere.renderer.anim.AnimNode;
import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.mem.JomlPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joml.Matrix4d;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.joml.Sphered;
import org.joml.Vector3d;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AIBone;
import org.lwjgl.assimp.AINode;

/**
 * A model
 * Contains a list of meshes to draw
 * Contains animations to apply to those meshes
 */
public class Model {

    /**
     * The model matrix of the model
     */
    private Matrix4d modelMatrix = new Matrix4d();

    /**
     * the real world coordinates of this object (not model space)
     */
    private Vector3d worldPos = new Vector3d();

    /**
     * an optional global transform applied to the parent bone. Typically found in models loaded from files
     */
    private Matrix4d rootTransform = new Matrix4d();

    /**
     * The inverse root transform
     */
    private Matrix4d globalInverseTransform = new Matrix4d();

    /**
     * The list of meshes in the model
     */
    private List<Mesh> meshes = new ArrayList<Mesh>();

    /**
     * The materials of the model
     */
    private List<Material> materials = new ArrayList<Material>();

    /**
     * The list of all bones
     */
    private List<Bone> bones = new ArrayList<Bone>();

    /**
     * The map of name -> bone
     */
    private Map<String,Bone> boneMap = new HashMap<String,Bone>();
    
    /**
     * The root animation node
     */
    private AnimNode rootAnimNode;

    /**
     * The map of animation node name -> node
     */
    private Map<String,AnimNode> nodeMap = new HashMap<String,AnimNode>();

    /**
     * The animations in the model
     */
    private List<Animation> animations = new ArrayList<Animation>();

    /**
     * The map of animation name -> animation
     */
    private Map<String,Animation> animMap = new HashMap<String,Animation>();

    /**
     * A mask that overwrites meshes themsselves
     */
    private ActorMeshMask meshMask;
    
    /**
     * A mask that overwrites textures on a given mesh
     */
    private Map<String,ActorTextureMask> textureMap = new HashMap<String,ActorTextureMask>();

    /**
     * The bounding sphere for this particular model
     */
    private Sphered boundingSphere = new Sphered();
    

    
    
    /**
     * Constructor
     */
    public Model(){
    }

    /**
     * Loads a model from an ai scene object
     * @param path The path of the model
     * @param scene the ai scene
     * @return The model object
     */
    public static Model createModelFromAiscene(String path, AIScene scene){
        Model rVal = new Model();

        ModelPretransforms.ModelMetadata modelMetadata = Globals.modelPretransforms.getModel(path);
        ModelPretransforms.GlobalTransform globalTransform = null;
        if(modelMetadata != null){
            globalTransform = modelMetadata.getGlobalTransform();
        }


        //
        //Load materials
        //
        if(scene.mNumMaterials() > 0){
            rVal.materials = new ArrayList<Material>();
            PointerBuffer material_buffer = scene.mMaterials();
            while(material_buffer.hasRemaining()){
                rVal.materials.add(Material.loadMaterialFromAIMaterial(path,scene,AIMaterial.create(material_buffer.get())));
            }
        }

        
        //
        //load meshes
        //
        PointerBuffer meshesBuffer = scene.mMeshes();
        rVal.meshes = new ArrayList<Mesh>();
        while(meshesBuffer.hasRemaining()){
            AIMesh aiMesh = AIMesh.create(meshesBuffer.get());
            ModelPretransforms.MeshMetadata meshMetadata = null;
            if(modelMetadata != null){
                meshMetadata = modelMetadata.getMesh(aiMesh.mName().dataString());
            }
            Mesh currentMesh = MeshLoader.createMeshFromAIScene(aiMesh, meshMetadata);
            rVal.addMesh(currentMesh);

            //conditionally add material
            int materialIndex = aiMesh.mMaterialIndex();
            if(materialIndex < rVal.materials.size()){
                currentMesh.setMaterial(rVal.materials.get(materialIndex));
            }
        }
        
        //
        //register bones
        //
        rVal.bones = new ArrayList<Bone>();
        rVal.boneMap = new HashMap<String, Bone>();
        meshesBuffer.rewind();
        while(meshesBuffer.hasRemaining()){
            AIMesh currentMeshData = AIMesh.createSafe(meshesBuffer.get());
            LoggerInterface.loggerRenderer.DEBUG("mesh name:" + currentMeshData.mName().dataString());
            PointerBuffer boneBuffer = currentMeshData.mBones();
            for(int j = 0; j < currentMeshData.mNumBones(); j++){
                AIBone currentBone = AIBone.createSafe(boneBuffer.get());
                String currentBoneName = currentBone.mName().dataString();
                if(!rVal.boneMap.containsKey(currentBoneName)){
                    Bone boneObject = new Bone(currentBone);
                    rVal.boneMap.put(currentBoneName, boneObject);
                    rVal.bones.add(boneObject);
                }
            }
        }
        Iterator<Mesh> meshIterator = rVal.meshes.iterator();
        rVal.bones = new ArrayList<Bone>();
        rVal.boneMap = new HashMap<String,Bone>();
        rVal.nodeMap = new HashMap<String, AnimNode>();
        while(meshIterator.hasNext()){
            Mesh currentMesh = meshIterator.next();
            Iterator<Bone> boneIterator = currentMesh.getBones().iterator();
            ArrayList<Bone> to_remove_queue = new ArrayList<Bone>();
            ArrayList<Bone> to_add_queue = new ArrayList<Bone>();
            while(boneIterator.hasNext()){
                Bone currentBone = boneIterator.next();
                if(!rVal.boneMap.containsKey(currentBone.boneID)){
                    rVal.bones.add(currentBone);
                    rVal.boneMap.put(currentBone.boneID, currentBone);
                }
            }
            boneIterator = to_remove_queue.iterator();
            while(boneIterator.hasNext()){
                currentMesh.getBones().remove(boneIterator.next());
            }
            boneIterator = to_add_queue.iterator();
            while(boneIterator.hasNext()){
                currentMesh.getBones().add(boneIterator.next());
            }
        }

        //
        //parse animation nodes and form hierarchy
        //
        AINode rootNode = scene.mRootNode();
        //The mOffsetMatrix, inverted, is the bind pose matrix that we want to apply at the top of all anims: https://github.com/assimp/assimp/issues/4364
        //This version of assimp doesn't support it, unfortunately
        rVal.rootTransform = electrosphere.util.Utilities.convertAIMatrixd(rootNode.mTransformation());
        if(globalTransform != null){
            rVal.globalInverseTransform = new Matrix4d(rVal.rootTransform).invert().scale(globalTransform.getScale());
            rVal.rootTransform.scale(globalTransform.getScale());
        }
        LoggerInterface.loggerRenderer.DEBUG("Global Inverse Transform");
        LoggerInterface.loggerRenderer.DEBUG(rVal.rootTransform + "");
        rVal.rootAnimNode = rVal.buildAnimNodeMap(scene.mRootNode(),null);

        //
        //load animations
        //
        int animCount = scene.mNumAnimations();
        PointerBuffer animBuffer = scene.mAnimations();
        rVal.animations = new ArrayList<Animation>();
        rVal.animMap = new HashMap<String,Animation>();
        for(int i = 0; i < animCount; i++){
            Animation newAnim = new Animation(AIAnimation.create(animBuffer.get(i)));
            rVal.animations.add(newAnim);
            rVal.animMap.put(newAnim.name,newAnim);
        }

        return rVal;
    }
    
    /**
     * Draws the model
     * @param renderPipelineState the render pipeline state
     */
    public void draw(RenderPipelineState renderPipelineState, OpenGLState openGLState){
        Globals.profiler.beginAggregateCpuSample("Model.draw");
        Iterator<Mesh> mesh_Iterator = meshes.iterator();
        while(mesh_Iterator.hasNext()){
            Mesh currentMesh = mesh_Iterator.next();
            if(meshMask == null || (meshMask != null && !meshMask.isBlockedMesh(currentMesh.getMeshName()))){
                //set texture mask
                if(this.textureMap != null && textureMap.containsKey(currentMesh.getMeshName())){
                    currentMesh.setTextureMask(textureMap.get(currentMesh.getMeshName()));
                }
                //draw
                currentMesh.complexDraw(renderPipelineState, openGLState);
                //reset texture mask
                currentMesh.setTextureMask(null);
            }
        }
        if(meshMask != null){
            for(Mesh toDraw : meshMask.getToDrawMeshes()){
                toDraw.setBones(bones);
                toDraw.setParent(this);
                //draw
                toDraw.complexDraw(renderPipelineState, openGLState);
            }
        }
        Globals.profiler.endCpuSample();
    }

    /**
     * Draws a specific mesh
     * @param i The index of the mesh
     */
    public void drawMesh(RenderPipelineState renderPipelineState, OpenGLState openGLState, int i){
        if(i < 0 || i >= this.meshes.size()){
            throw new Error("Invalid mesh! " + i);
        }
        this.meshes.get(i).complexDraw(renderPipelineState, openGLState);
    }

    /**
     * Gets the number of meshes in the model
     * @return The number of meshes
     */
    public int getMeshCount(){
        return this.meshes.size();
    }
    

    /**
     * Applies an animation mask to the model
     * @param animationName The name of the animation
     * @param time The temporal location in the animation to mask
     * @param mask The mask
     */
    public void applyAnimationMask(String animationName, double time, List<String> mask){
        Animation animationCurrent = animMap.get(animationName);
        if(animationCurrent != null){
            for(String boneName : mask){
                AnimChannel currentChannel = animationCurrent.getChannel(boneName);
                if(currentChannel == null){
                    //this happens when we have an animation key for a bone that is not on this model
                    //this can happen if, for example, you model a character, animate it, then delete a bone after the fact
                    //if the animation key hasn't explicitly been removed in blender, it will show up here for the non-existant bone
                    continue;
                }
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
     * Deletes this model
     */
    public void delete(){
        for(Mesh mesh : this.meshes){
            mesh.free();
        }
        for(Material material : this.materials){
            material.free();
        }
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
     * Logs all animations for a given model including individual key values
     */
    public void describeAllAnimationsFully(){
        if(animations.size() > 0){
            LoggerInterface.loggerRenderer.DEBUG("=====================");
            LoggerInterface.loggerRenderer.DEBUG(animations.size() + " animations available in model!");
            Iterator<Animation> animIterator = animations.iterator();
            while(animIterator.hasNext()){
                Animation currentAnim = animIterator.next();
                currentAnim.fullDescribeAnimation();
            }
        }
    }
    
    /**
     * Recursively builds the bone tree
     * @param node The current assimp bone to operate on
     * @param parent The parent bone
     * @return The current bone
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
     * Updates the position of all bones to their correct locations given bone rotators, static morph, and current animation
     * @param boneRotators The bone rotators to apply
     * @param staticMorph The static morph to apply
     */
    public void updateNodeTransform(Map<String,ActorBoneRotator> boneRotators, ActorStaticMorph staticMorph){
        if(this.rootAnimNode != null){
            updateNodeTransform(this.rootAnimNode,boneRotators,staticMorph);
        }
        //if the model doesn't have bones, rootAnimNode won't be defined (think terrain)
    }

    /**
     * Recursively updates the position of all bones to their correct locations given bone rotators, static morph, and current animation
     * @param n the current node to operate on
     * @param boneRotators The bone rotators to apply
     * @param staticMorph The static morph to apply
     */
    private void updateNodeTransform(AnimNode n, Map<String,ActorBoneRotator> boneRotators, ActorStaticMorph staticMorph){
        Matrix4d poolMat = JomlPool.getMat();
        //grab parent transform if exists
        if(n.parent != null){
            n.parent.getTransform(poolMat);
        }
        if(n.is_bone){
            //
            //bone rotators (turrets, hair, etc)
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
            //
            //static morph (changing nose size, eye distance, etc)
            if(staticMorph != null && staticMorph.getBoneTransforms(n.id) != null){
                poolMat.mul(staticMorph.getBoneTransforms(n.id).getTransform());
            }

            //
            n.setTransform(poolMat);
            //
            //Calculate final offset from initial bone
            //https://stackoverflow.com/a/59869381
            poolMat.mul(target_bone.getMOffset());
            poolMat = globalInverseTransform.mul(poolMat, poolMat);
            target_bone.setFinalTransform(poolMat);
        } else {
            n.setTransform(poolMat.mul(electrosphere.util.Utilities.convertAIMatrix(n.raw_data.mTransformation())));
        }
        Iterator<AnimNode> node_iterator = n.children.iterator();
        while(node_iterator.hasNext()){
            AnimNode current_node = node_iterator.next();
            updateNodeTransform(current_node,boneRotators,staticMorph);
        }
        JomlPool.release(poolMat);
    }

    /**
     * Draws a ui model
     */
    public void drawUI(){
        for(Mesh m : meshes){
            m.complexDraw(Globals.renderingEngine.getRenderPipelineState(),Globals.renderingEngine.getOpenGLState());
        }
    }
    

    /**
     * Pushes a uniform to a given mesh
     * @param meshName The name of the mesh
     * @param uniformKey The uniform key
     * @param uniform The value of the uniform
     */
    public void pushUniformToMesh(String meshName, String uniformKey, Object uniform){
        for(Mesh m : meshes){
            if(m.getMeshName().equals(meshName)){
                m.setUniform(uniformKey, uniform);
            }
        }
    }
    
    /**
     * Logs all bone IDs (names)
     */
    public void listAllBoneIDs(){
        for(String id : boneMap.keySet()){
            LoggerInterface.loggerRenderer.DEBUG(id);
        }
    }
    
    /**
     * Gets a given mesh by its name
     * @param meshName the name of the mesh
     * @return The mesh if it exists, or null
     */
    public Mesh getMesh(String meshName){
        for(Mesh mesh : meshes){
            if(mesh.getMeshName().matches(meshName)){
                return mesh;
            }
        }
        return null;
    }

    /**
     * Gets an animation by its name
     * @param animName The name of the animation
     * @return the animation if it exists, or null
     */
    public Animation getAnimation(String animName){
        return animMap.get(animName);
    }
    
    /**
     * Describes the model at a high level
     */
    public void describeHighLevel(){
        LoggerInterface.loggerRenderer.WARNING("Meshes: ");
        for(Mesh mesh : meshes){
            LoggerInterface.loggerRenderer.WARNING(mesh.getMeshName());
        }
        LoggerInterface.loggerRenderer.WARNING("Animations: ");
        for(Animation anim : animations){
            LoggerInterface.loggerRenderer.WARNING(anim.name);
        }
        LoggerInterface.loggerRenderer.WARNING("Bones:");
        for(Bone bone : bones){
            LoggerInterface.loggerRenderer.WARNING(bone.boneID);
        }
    }



    //
    // Pure getters and setters
    //
    

    /**
     * Gets the meshes in this model
     * @return the list of meshes
     */
    public List<Mesh> getMeshes(){
        return Collections.unmodifiableList(meshes);
    }

    /**
     * Adds a mesh to the model
     * @param mesh The mesh
     */
    public void addMesh(Mesh mesh){
        this.meshes.add(mesh);
        mesh.setParent(this);
        if(this.meshes.size() == 1){
            this.boundingSphere.r = mesh.getBoundingSphere().r;
            this.boundingSphere.x = mesh.getBoundingSphere().x;
            this.boundingSphere.y = mesh.getBoundingSphere().y;
            this.boundingSphere.z = mesh.getBoundingSphere().z;
        } else if(mesh.getBoundingSphere().r > this.boundingSphere.r){
            this.boundingSphere.r = mesh.getBoundingSphere().r;
        }
    }

    /**
     * Sets the model matrix for this model
     * @param modelMatrix the model matrix
     */
    public void setModelMatrix(Matrix4d modelMatrix){
        this.modelMatrix = modelMatrix;
    }

    /**
     * Gets the model matrix for this model
     * @return the model matrix
     */
    public Matrix4d getModelMatrix(){
        return modelMatrix;
    }

    /**
     * Sets the world position of the model
     * @param worldPos the world pos
     */
    public void setWorldPos(Vector3d worldPos){
        this.worldPos = worldPos;
    }

    /**
     * Gets the world position stored in this model
     * @return The world pos
     */
    public Vector3d getWorldPos(){
        return this.worldPos;
    }

    /**
     * Gets the list of bones
     * @return the list of bones
     */
    public List<Bone> getBones(){
        return bones;
    }

    /**
     * Gets the map of bone name -> bone
     * @return the map
     */
    public Map<String,Bone> getBoneMap(){
        return boneMap;
    }

    /**
     * Gets the list of materials in this model
     * @return The list of materials
     */
    public List<Material> getMaterials(){
        return materials;
    }

    /**
     * Sets the mesh mask
     * @param meshMask the mesh mask to set
     */
    public void setMeshMask(ActorMeshMask meshMask){
        this.meshMask = meshMask;
    }

    /**
     * Sets the texture mask
     * @param textureMask the texture mask
     */
    public void setTextureMask(Map<String,ActorTextureMask> textureMask){
        this.textureMap = textureMask;
    }


    /**
     * Utility method to attempt overwriting the model's meshes with a new material
     * @param material The material
     */
    public void tryOverwriteMaterial(Material material){
        for(Mesh mesh : meshes){
            mesh.setMaterial(material);
        }
    }

    /**
     * Gets the bounding sphere for this model
     * @return The bounding sphere
     */
    public Sphered getBoundingSphere(){
        return boundingSphere;
    }

    /**
     * Sets the bounding sphere
     * @param boundingSphere The bounding sphere to be stored
     */
    public void setBoundingSphere(Sphered boundingSphere){
        this.boundingSphere = boundingSphere;
    }

    /**
     * Sets the bounding sphere
     * @param x The x position
     * @param y The y position
     * @param z The z position
     * @param r The radius of the sphere
     */
    public void setBoundingSphere(double x, double y, double z, double r){
        this.boundingSphere = new Sphered(x,y,z,r);
    }

    /**
     * Gets the list of all animations
     * @return The list of all animations
     */
    public List<Animation> getAnimations(){
        return this.animations;
    }
}
