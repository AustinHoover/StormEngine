package electrosphere.engine.assetmanager;

import electrosphere.audio.AudioBuffer;
import electrosphere.collision.CollisionBodyCreation;
import electrosphere.collision.CollisionEngine;
import electrosphere.collision.collidable.Collidable;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.queue.QueuedAsset;
import electrosphere.engine.assetmanager.queue.QueuedShader;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.buffer.HomogenousInstancedArray;
import electrosphere.renderer.buffer.HomogenousUniformBuffer;
import electrosphere.renderer.loading.ModelLoader;
import electrosphere.renderer.model.Mesh;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.shader.ComputeShader;
import electrosphere.renderer.shader.VisualShader;
import electrosphere.renderer.texture.Texture;
import electrosphere.renderer.texture.TextureMap;
import electrosphere.server.entity.poseactor.PoseModel;
import electrosphere.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.lwjgl.assimp.AIScene;
import org.ode4j.ode.DBody;

/**
 * Manages all assets loaded into the engine including initially loading and destructing
 */
public class AssetManager {

    /**
     * Lock for thread-safing the work
     */
    ReentrantLock lock = new ReentrantLock();
    
    Map<String,Model> modelsLoadedIntoMemory = new HashMap<String,Model>();
    List<String> modelsInQueue = new LinkedList<String>();
    List<String> modelsInDeleteQueue = new LinkedList<String>();
    List<MeshShaderOverride> shaderOverrides = new LinkedList<MeshShaderOverride>();
    
    Map<String,Texture> texturesLoadedIntoMemory = new HashMap<String,Texture>();
    List<String> texturesInQueue = new LinkedList<String>();
    List<String> texturesInDeleteQueue = new LinkedList<String>();
    
    Map<String,AudioBuffer> audioLoadedIntoMemory = new HashMap<String,AudioBuffer>();
    List<String> audioInQueue = new LinkedList<String>();

    Map<String,VisualShader> shadersLoadedIntoMemory = new HashMap<String,VisualShader>();
    List<String> shadersInDeleteQueue = new LinkedList<String>();
    List<QueuedShader> shadersInQueue = new LinkedList<QueuedShader>();

    //
    //Compute shader related
    //
    Map<String,ComputeShader> computeShadersLoadedIntoMemory = new HashMap<String,ComputeShader>();
    List<String> computeShadersInQueue = new LinkedList<String>();

    Map<String,DBody> physicsMeshesLoadedIntoMemory = new HashMap<String,DBody>();
    List<PhysicsMeshQueueItem> physicsMeshesToLoad = new LinkedList<PhysicsMeshQueueItem>();

    Map<String,PoseModel> poseModelsLoadedIntoMemory = new HashMap<String,PoseModel>();
    List<String> poseModelsInQueue = new LinkedList<String>();
    List<String> poseModelsInDeleteQueue = new LinkedList<String>();

    //A queue of homogenous buffers to allocate this render frame
    List<HomogenousUniformBuffer> homogenousBufferAllocationQueue = new LinkedList<HomogenousUniformBuffer>();

    //A queue of homogenous buffers to allocate this render frame
    List<HomogenousInstancedArray> instanceArrayBufferAllocationQueue = new LinkedList<HomogenousInstancedArray>();


    //assets queued to be loaded
    List<QueuedAsset<?>> queuedAssets = new LinkedList<QueuedAsset<?>>();

    /**
     * Maximum number of queued assets that can be loaded per frame
     */
    public static final int MAX_ASSETS_PER_FRAME = 50;
    
    
    
    
    //
    //General asset manager stuff
    //
    
    /**
     * For each class/type of asset, load all assets in queue
     */
    public void loadAssetsInQueue(){
        //models
        LoggerInterface.loggerEngine.DEBUG_LOOP("AssetManager - Load models");
        Globals.profiler.beginCpuSample("AssetManager.loadAssetsInQueue - Load Models");
        lock.lock();
        for(String currentPath : modelsInQueue){
            AIScene aiScene = ModelLoader.loadAIScene(currentPath);
            TextureMap textureMap = null;
            if(this.getLocalTextureMapPath(currentPath) != null){
                textureMap = TextureMap.construct(this.getLocalTextureMapPath(currentPath));
            }
            if(aiScene != null){
                modelsLoadedIntoMemory.put(currentPath, ModelLoader.createModelFromAiScene(aiScene,textureMap,currentPath));
                for(PhysicsMeshQueueItem physicsMeshQueueItem : physicsMeshesToLoad){
                    if(physicsMeshQueueItem.modelPath.contains(currentPath)){
                        //create physics
                        physicsMeshesToLoad.remove(physicsMeshQueueItem);
                        physicsMeshesLoadedIntoMemory.put(
                            this.getCollisionMeshMapKey(physicsMeshQueueItem.collisionEngine,currentPath),
                            CollisionBodyCreation.generateRigidBodyFromAIScene(physicsMeshQueueItem.collisionEngine,aiScene,Collidable.TYPE_STATIC_BIT)
                        );
                    }
                }
            }
        }
        modelsInQueue.clear();
        Globals.profiler.endCpuSample();
        //textures from disk to gpu
        LoggerInterface.loggerEngine.DEBUG_LOOP("AssetManager - Load textures");
        Globals.profiler.beginCpuSample("AssetManager.loadAssetsInQueue - Load textures");
        for(String currentPath : texturesInQueue){
            Texture tex = new Texture(Globals.renderingEngine.getOpenGLState(), currentPath);
            texturesLoadedIntoMemory.put(currentPath, tex);
        }
        texturesInQueue.clear();
        Globals.profiler.endCpuSample();
        //audio from disk
        LoggerInterface.loggerEngine.DEBUG_LOOP("AssetManager - Load audio");
        Globals.profiler.beginCpuSample("AssetManager.loadAssetsInQueue - Load audio");
        if(Globals.audioEngine != null && Globals.audioEngine.initialized()){
            for(String currentPath : audioInQueue){
                audioLoadedIntoMemory.put(currentPath, new AudioBuffer(currentPath));
            }
            audioInQueue.clear();
        }
        Globals.profiler.endCpuSample();
        //shaders
        LoggerInterface.loggerEngine.DEBUG_LOOP("AssetManager - Load visual shaders");
        Globals.profiler.beginCpuSample("AssetManager.loadAssetsInQueue - Load visual shaders");
        for(QueuedShader currentShader : shadersInQueue){
            String key = getShaderKey(currentShader.getVertexShaderPath(),currentShader.getFragmentShaderPath());
            shadersLoadedIntoMemory.put(
                key,
                VisualShader.loadSpecificShader(currentShader.getVertexShaderPath(),currentShader.getFragmentShaderPath())
            );
        }
        shadersInQueue.clear();
        Globals.profiler.endCpuSample();
        //compute shaders
        LoggerInterface.loggerEngine.DEBUG_LOOP("AssetManager - Load compute shaders");
        Globals.profiler.beginCpuSample("AssetManager.loadAssetsInQueue - Load compute shaders");
        for(String computePath : computeShadersInQueue){
            String key = getComputeShaderKey(computePath);
            try {
                computeShadersLoadedIntoMemory.put(
                    key, 
                    ComputeShader.create(FileUtils.getAssetFileAsString(computePath))
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        computeShadersInQueue.clear();
        Globals.profiler.endCpuSample();
        //pose models
        LoggerInterface.loggerEngine.DEBUG_LOOP("AssetManager - Load pose models");
        Globals.profiler.beginCpuSample("AssetManager.loadAssetsInQueue - Load pose models");
        for(String currentPath: poseModelsInQueue){
            AIScene scene = ModelLoader.loadAIScene(currentPath);
            poseModelsLoadedIntoMemory.put(currentPath, new PoseModel(currentPath, scene));
        }
        poseModelsInQueue.clear();
        Globals.profiler.endCpuSample();
        //queued assets
        LoggerInterface.loggerEngine.DEBUG_LOOP("AssetManager - Load queued assets");
        Globals.profiler.beginCpuSample("AssetManager.loadAssetsInQueue - Load queued assets");
        if(queuedAssets.size() > MAX_ASSETS_PER_FRAME){
            for(int i = 0; i < MAX_ASSETS_PER_FRAME; i++){
                QueuedAsset<?> queuedAsset = queuedAssets.remove(0);
                queuedAsset.load();
                if(queuedAsset.get() instanceof Model){
                    this.modelsLoadedIntoMemory.put(queuedAsset.getPromisedPath(),(Model)queuedAsset.get());
                } else if(queuedAsset.get() instanceof Texture){
                    this.texturesLoadedIntoMemory.put(queuedAsset.getPromisedPath(),(Texture)queuedAsset.get());
                }
            }
        } else {
            for(QueuedAsset<?> queuedAsset : queuedAssets){
                queuedAsset.load();
                if(queuedAsset.get() instanceof Model){
                    this.modelsLoadedIntoMemory.put(queuedAsset.getPromisedPath(),(Model)queuedAsset.get());
                } else if(queuedAsset.get() instanceof Texture){
                    this.texturesLoadedIntoMemory.put(queuedAsset.getPromisedPath(),(Texture)queuedAsset.get());
                }
            }
            queuedAssets.clear();
        }
        Globals.profiler.endCpuSample();

        //allocate homogenous buffers
        LoggerInterface.loggerEngine.DEBUG_LOOP("AssetManager - Allocate homogenous buffers");
        Globals.profiler.beginCpuSample("AssetManager.loadAssetsInQueue - Allocate homogenous buffers");
        this.allocateHomogenousBuffers();
        Globals.profiler.endCpuSample();

        //allocate instance array buffers
        LoggerInterface.loggerEngine.DEBUG_LOOP("AssetManager - Allocate instance array buffers");
        Globals.profiler.beginCpuSample("AssetManager.loadAssetsInQueue - Allocate instance array buffers");
        this.allocateInstanceArrayBuffers();
        Globals.profiler.endCpuSample();

        //override meshes
        LoggerInterface.loggerEngine.DEBUG_LOOP("AssetManager - Override meshes");
        this.performMeshOverrides();

        lock.unlock();
    }


    //
    //Updating assets
    //
    public void updateAsset(String path){
        LoggerInterface.loggerEngine.DEBUG("AssetManager - updateAsset");
        //models
        lock.lock();
        if(modelsLoadedIntoMemory.containsKey(path)){
            this.queueModelForDeletion(path);
            this.deleteModelsInDeleteQueue();
            this.addModelPathToQueue(path);
        }
        //textures
        if(texturesLoadedIntoMemory.containsKey(path)){
            this.queueTextureForDeletion(path);
            this.deleteTexturesInDeleteQueue();
            this.addTexturePathtoQueue(path);
        }
        //pose models
        if(poseModelsLoadedIntoMemory.containsKey(path)){
            this.queuePoseModelForDeletion(path);
            this.deletePoseModelsInDeleteQueue();
            this.addPoseModelPathToQueue(path);
        }
        if(audioLoadedIntoMemory.containsKey(path)){
            throw new Error("Unhandled asset type! (Audio)");
        }
        if(shadersLoadedIntoMemory.containsKey(path)){
            throw new Error("Unhandled asset type! (Shader - Visual)");
        }
        if(computeShadersLoadedIntoMemory.containsKey(path)){
            throw new Error("Unhandled asset type! (Shader - Compute)");
        }
        lock.unlock();
    }

    /**
     * Handles the delete queues
     */
    public void handleDeleteQueue(){
        lock.lock();
        this.deleteModelsInDeleteQueue();
        this.deletePoseModelsInDeleteQueue();
        this.deleteTexturesInDeleteQueue();
        this.deleteShadersInDeleteQueue();
        lock.unlock();
    }
    
    
    
    
    
    
    
    
    //
    //Models
    //
    
    public void addModelPathToQueue(String path){
        lock.lock();
        if(!modelsInQueue.contains(path) && !modelsLoadedIntoMemory.containsKey(path)){
            modelsInQueue.add(path);
        }
        lock.unlock();
    }
    
    public Model fetchModel(String path){
        Model rVal = null;
        lock.lock();
        if(modelsLoadedIntoMemory.containsKey(path)){
            rVal = modelsLoadedIntoMemory.get(path);
        }
        lock.unlock();
        return rVal;
    }

    /**
     * Queues a model for deletion
     * @param modelPath The path to the model
     */
    public void queueModelForDeletion(String modelPath){
        lock.lock();
        modelsInDeleteQueue.add(modelPath);
        lock.unlock();
    }
    
    /**
    Registers a (presumably generated in code) model with the asset manager
    @returns a random string that represents the model in the asset manager
    */
    public String registerModel(Model m){
        String rVal;
        UUID newUUID = UUID.randomUUID();
        rVal = newUUID.toString();
        lock.lock();
        modelsLoadedIntoMemory.put(rVal,m);
        lock.unlock();
        return rVal;
    }

    /**
     * Registers a (presumably generated in code) model to a given path in the asset manager
     * Used particularly if you have a specific path you want to relate to a specific model (eg terrain chunk code)
     * @param m The model to register
     * @param path The path to register the model to
     */
    public void registerModelWithPath(Model m, String path){
        lock.lock();
        modelsLoadedIntoMemory.put(path, m);
        lock.unlock();
    }

    public void deregisterModelPath(String path){
        lock.lock();
        modelsLoadedIntoMemory.remove(path);
        lock.unlock();
    }
    
    public void queueOverrideMeshShader(String modelName, String meshName, String vertPath, String fragPath){
        lock.lock();
        MeshShaderOverride override = new MeshShaderOverride(modelName,meshName,vertPath,fragPath);
        shaderOverrides.add(override);
        lock.unlock();
    }

    public void performMeshOverrides(){
        List<MeshShaderOverride> toRemove = new LinkedList<MeshShaderOverride>();
        lock.lock();
        for(MeshShaderOverride shaderOverride : shaderOverrides){
            Model model = null;
            if((model = fetchModel(shaderOverride.modelName)) != null){
                for(Mesh mesh : model.getMeshes()){
                    if(mesh.getMeshName().equals(shaderOverride.getMeshName())){
                        mesh.setShader(VisualShader.loadSpecificShader(shaderOverride.vertPath, shaderOverride.fragPath));
                    }
                }
                toRemove.add(shaderOverride);
            }
        }
        for(MeshShaderOverride shaderOverride : toRemove){
            shaderOverrides.remove(shaderOverride);
        }
        lock.unlock();
    }

    /**
     * Nuclear function, reloads all shaders loaded into memory
     */
    public void forceReloadAllModels(){
        lock.lock();
        for(String modelKey : modelsLoadedIntoMemory.keySet()){
            if(modelKey.contains("Models")){
                modelsInQueue.add(modelKey);
                modelsLoadedIntoMemory.remove(modelKey);
            }
        }
        lock.unlock();
    }

    /**
     * Deletes all models in the delete queue
     */
    public void deleteModelsInDeleteQueue(){
        lock.lock();
        for(String modelPath : modelsInDeleteQueue){
            Model model = this.fetchModel(modelPath);
            if(model != null){
                model.delete();
            }
            this.modelsLoadedIntoMemory.remove(modelPath);
        }
        lock.unlock();
    }

    /**
     * Queues all models for deletion
     */
    public void queueAllModelsForDeletion(){
        lock.lock();
        for(String modelPath : this.modelsLoadedIntoMemory.keySet()){
            this.queueModelForDeletion(modelPath);
        }
        lock.unlock();
    }

    
    
    
    



    //
    //Pose Models
    //
    /**
     * Adds a pose model to the list of pose models to load
     * @param path The path to load
     */
    public void addPoseModelPathToQueue(String path){
        lock.lock();
        if(!poseModelsInQueue.contains(path) && !poseModelsLoadedIntoMemory.containsKey(path)){
            poseModelsInQueue.add(path);
        }
        lock.unlock();
    }
    
    /**
     * Fetches a pose model
     * @param path The path to fetch
     * @return The pose model if it exists, null otherwise
     */
    public PoseModel fetchPoseModel(String path){
        PoseModel rVal = null;
        lock.lock();
        if(poseModelsLoadedIntoMemory.containsKey(path)){
            rVal = poseModelsLoadedIntoMemory.get(path);
        }
        lock.unlock();
        return rVal;
    }

    /**
     * Registers a (presumably generated in code) pose model to a given path in the asset manager
     * Used particularly if you have a specific path you want to relate to a specific pose model (eg basic shapes)
     * @param m The pose model to register
     * @param path The path to register the pose model to
     */
    public void registerPoseModelWithPath(PoseModel m, String path){
        lock.lock();
        poseModelsLoadedIntoMemory.put(path, m);
        lock.unlock();
    }

    /**
     * Queues a pose model for deletion
     * @param modelPath The path to the pose model
     */
    public void queuePoseModelForDeletion(String modelPath){
        lock.lock();
        poseModelsInDeleteQueue.add(modelPath);
        lock.unlock();
    }

    /**
     * Deletes all pose models in the delete queue
     */
    public void deletePoseModelsInDeleteQueue(){
        lock.lock();
        for(String modelPath : poseModelsInDeleteQueue){
            PoseModel poseModel = this.fetchPoseModel(modelPath);
            if(poseModel != null){
                poseModel.delete();
            }
            this.poseModelsLoadedIntoMemory.remove(modelPath);
        }
        lock.unlock();
    }
    
    
    
    
    
    
    
    
    
    //
    // Textures
    //
    
    
    public void addTexturePathtoQueue(String path){
        lock.lock();
        if(!texturesInQueue.contains(path) && !texturesLoadedIntoMemory.containsKey(path)){
            texturesInQueue.add(path);
        }
        lock.unlock();
    }

    /**
     * Queues a texture for deletion
     * @param texturePath The path to the texture
     */
    public void queueTextureForDeletion(String texturePath){
        lock.lock();
        texturesInDeleteQueue.add(texturePath);
        lock.unlock();
    }
    
    public Texture fetchTexture(String path){
        Texture rVal = null;
        lock.lock();
        if(texturesLoadedIntoMemory.containsKey(path)){
            rVal = texturesLoadedIntoMemory.get(path);
        }
        lock.unlock();
        return rVal;
    }
    
    public String registerTexture(Texture t){
        String rVal;
        UUID newUUID = UUID.randomUUID();
        rVal = newUUID.toString();
        lock.lock();
        texturesLoadedIntoMemory.put(rVal,t);
        lock.unlock();
        return rVal;
    }

    /**
     * Registers a texture to a path
     * @param t The texture
     * @param path The path
     */
    public void registerTextureToPath(Texture t, String path){
        lock.lock();
        texturesLoadedIntoMemory.put(path,t);
        lock.unlock();
    }

    public boolean hasLoadedTexture(String path){
        lock.lock();
        boolean rVal = texturesLoadedIntoMemory.containsKey(path);
        lock.unlock();
        return rVal;
    }

    /**
     * Gets a local texture map's path from a model's path
     * @param modelPath The model's path
     */
    private String getLocalTextureMapPath(String modelPath){
        lock.lock();
        File modelFile = FileUtils.getAssetFile(modelPath);
        File containingDirectory = modelFile.getParentFile();
        File[] children = containingDirectory.listFiles();
        if(children != null){
            for(File child : children){
                if(child.getName().equals("texturemap.json")){
                    String rVal = child.getPath();
                    String fixed = rVal.replace(".\\assets", "").replace("./assets", "");
                    lock.unlock();
                    return fixed;
                }
            }
        }
        lock.unlock();
        return null;
    }

    /**
     * Deletes all textures in the delete queue
     */
    public void deleteTexturesInDeleteQueue(){
        lock.lock();
        for(String texturePath : texturesInDeleteQueue){
            Texture texture = this.fetchTexture(texturePath);
            if(texture != null){
                texture.free();
            }
            this.texturesLoadedIntoMemory.remove(texturePath);
        }
        lock.unlock();
    }

    /**
     * Queues all textures for deletion
     */
    public void queueAllTexturesForDeletion(){
        lock.lock();
        for(String texturePath : this.texturesLoadedIntoMemory.keySet()){
            this.queueTextureForDeletion(texturePath);
        }
        lock.unlock();
    }
    
    
    
    
    
    
    
    
    
    //
    //AUDIO
    //
    
    public void addAudioPathToQueue(String path){
        lock.lock();
        String sanitizedPath = FileUtils.sanitizeFilePath(path);
        if(!audioInQueue.contains(sanitizedPath) && !audioLoadedIntoMemory.containsKey(sanitizedPath)){
            audioInQueue.add(sanitizedPath);
        }
        lock.unlock();
    }
    
    public AudioBuffer fetchAudio(String path){
        lock.lock();
        AudioBuffer rVal = null;
        String sanitizedPath = FileUtils.sanitizeFilePath(path);
        if(audioLoadedIntoMemory.containsKey(sanitizedPath)){
            rVal = audioLoadedIntoMemory.get(sanitizedPath);
        } else {
            LoggerInterface.loggerAudio.WARNING("Failed to find audio " + sanitizedPath);
        }
        lock.unlock();
        return rVal;
    }

    /**
     * Gets all audio loaded into the engine
     * @return The collection of all audio buffers
     */
    public Collection<AudioBuffer> getAllAudio(){
        lock.lock();
        Collection<AudioBuffer> rVal = Collections.unmodifiableCollection(this.audioLoadedIntoMemory.values());
        lock.unlock();
        return rVal;
    }
    
    






    //
    //SHADERS
    //
    public void addShaderToQueue(String vertexShader, String fragmentShader){
        lock.lock();
        shadersInQueue.add(new QueuedShader(vertexShader,fragmentShader));
        lock.unlock();
    }

    public VisualShader fetchShader(String vertexPath, String fragmentPath){
        lock.lock();
        String path = getShaderKey(vertexPath,fragmentPath);
        VisualShader rVal = null;
        if(shadersLoadedIntoMemory.containsKey(path)){
            rVal = shadersLoadedIntoMemory.get(path);
        }
        lock.unlock();
        return rVal;
    }

    static String getShaderKey(String vertexPath, String fragmentPath){
        return vertexPath + "-" + fragmentPath;
    }

    /**
     * Nuclear function, reloads all shaders loaded into memory
     */
    public void forceReloadAllShaders(){
        lock.lock();
        for(String shaderKey : shadersLoadedIntoMemory.keySet()){
            String shaderPaths[] = shaderKey.split("-");
            shadersInQueue.add(new QueuedShader(shaderPaths[0],shaderPaths[1]));
        }
        shadersLoadedIntoMemory.clear();
        lock.unlock();
    }

    /**
     * Queues a shader for deletion
     * @param shaderPath The path to the shader
     */
    public void queueShaderForDeletion(String shaderPath){
        lock.lock();
        shadersInDeleteQueue.add(shaderPath);
        lock.unlock();
    }

    /**
     * Queues all shaders for deletion
     */
    public void queueAllShadersForDeletion(){
        lock.lock();
        for(String shaderKey : this.shadersLoadedIntoMemory.keySet()){
            this.queueShaderForDeletion(shaderKey);
        }
        lock.unlock();
    }

    /**
     * Deletes all shaders in the delete queue
     */
    public void deleteShadersInDeleteQueue(){
        lock.lock();
        for(String shaderKey : this.shadersInDeleteQueue){
            VisualShader shader = this.shadersLoadedIntoMemory.remove(shaderKey);
            shader.free();
        }
        this.shadersInDeleteQueue.clear();
        lock.unlock();
    }

    //
    //SHADERS
    //
    /**
     * Adds a compute shader to the queue to be loaded
     * @param computePath The path to the source code for the shader
     */
    public void addComputeShaderToQueue(String computePath){
        lock.lock();
        computeShadersInQueue.add(getComputeShaderKey(computePath));
        lock.unlock();
    }

    /**
     * Gets a compute shader
     * @param computePath The path to the source code for the compute shader
     * @return The compute shader if it exists, null otherwise
     */
    public ComputeShader fetchComputeShader(String computePath){
        lock.lock();
        String key = getComputeShaderKey(computePath);
        ComputeShader rVal = null;
        if(computeShadersLoadedIntoMemory.containsKey(key)){
            rVal = computeShadersLoadedIntoMemory.get(key);
        }
        lock.unlock();
        return rVal;
    }

    /**
     * Gets the key for a compute shader
     * @param computePath The path to the shader source
     * @return The key
     */
    static String getComputeShaderKey(String computePath){
        return computePath;
    }
    
    
    
    
    //
    //COLLISION MESH
    //
    public void addCollisionMeshToQueue(PhysicsMeshQueueItem physicsMeshQueueItem){
        lock.lock();
        if(
            !physicsMeshesToLoad.contains(physicsMeshQueueItem) && 
            !physicsMeshesLoadedIntoMemory.containsKey(getCollisionMeshMapKey(
                physicsMeshQueueItem.collisionEngine,
                physicsMeshQueueItem.modelPath
        ))){
            physicsMeshesToLoad.add(physicsMeshQueueItem);
        }
        lock.unlock();
    }

    public DBody fetchCollisionObject(CollisionEngine collisionEngine, String path){
        lock.lock();
        DBody rVal = physicsMeshesLoadedIntoMemory.get(getCollisionMeshMapKey(collisionEngine,path));
        lock.unlock();
        return rVal;
    }

    /**
     * Gets a key based on collision engine object hash and path of model
     * @param collisionEngine collision engine
     * @param path The path
     * @return The key
     */
    private String getCollisionMeshMapKey(CollisionEngine collisionEngine, String path){
        return collisionEngine + path;
    }


    //
    //HOMOGENOUS UNIFORM BUFFERS
    //
    /**
     * Allocates all uniform buffers in queue
     */
    public void allocateHomogenousBuffers(){
        lock.lock();
        for(HomogenousUniformBuffer buffer : homogenousBufferAllocationQueue){
            buffer.allocate();
        }
        homogenousBufferAllocationQueue.clear();
        lock.unlock();
    }

    /**
     * Adds a uniform buffer to the queue to be allocated
     * @param buffer The buffer
     */
    public void addHomogenousBufferToQueue(HomogenousUniformBuffer buffer){
        lock.lock();
        homogenousBufferAllocationQueue.add(buffer);
        lock.unlock();
    }


    //
    //INSTANCE ARRAY BUFFERS
    //
    /**
     * Allocates all instance array buffers in queue
     */
    public void allocateInstanceArrayBuffers(){
        lock.lock();
        for(HomogenousInstancedArray buffer : instanceArrayBufferAllocationQueue){
            buffer.allocate();
        }
        instanceArrayBufferAllocationQueue.clear();
        lock.unlock();
    }

    /**
     * Adds an instance array buffer to the queue to be allocated
     * @param buffer The buffer
     */
    public void addInstanceArrayBufferToQueue(HomogenousInstancedArray buffer){
        lock.lock();
        instanceArrayBufferAllocationQueue.add(buffer);
        lock.unlock();
    }


    //
    //Async generic queued assets
    //
    /**
     * Queues an asset to be loaded on the main thread
     * @param asset the asset
     */
    public String queuedAsset(QueuedAsset<?> asset){
        lock.lock();
        this.queuedAssets.add(asset);

        //promise a specific string for this asset
        String promisedPath;
        if(asset.suppliedPath()){
            promisedPath = asset.getPromisedPath();
            if(promisedPath == null || promisedPath == ""){
                String message = "Queued an asset with an empty promised path!" +
                " " + promisedPath +
                " " + asset
                ;
                throw new Error(message);
            }
        } else {
            UUID newUUID = UUID.randomUUID();
            promisedPath = newUUID.toString();
            asset.setPromisedPath(promisedPath);
        }

        lock.unlock();

        return promisedPath;
    }
    
    
    
}
