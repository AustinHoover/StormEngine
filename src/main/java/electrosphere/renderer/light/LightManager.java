package electrosphere.renderer.light;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.data.entity.common.light.PointLightDescription;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.entity.Entity;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.buffer.ShaderStorageBuffer;
import electrosphere.renderer.buffer.BufferEnums.BufferAccess;
import electrosphere.renderer.buffer.BufferEnums.BufferUsage;
import electrosphere.renderer.shader.ComputeShader;
import electrosphere.renderer.shader.MemoryBarrier;
import electrosphere.renderer.shader.MemoryBarrier.Barrier;

/**
 * Manages the light sources in the engine
 */
public class LightManager {

    /**
     * Maximum number of lights per cluster
     */
    static final int MAX_LIGHTS_PER_CLUSTER = 100;

    /**
     * Size of the light cluster structure
     */
    static final int CLUSTER_STRUCT_SIZE = 16 + 16 + 4 + (4 * MAX_LIGHTS_PER_CLUSTER);

    /**
     * The width of the light cluster grid's x dimension
     */
    public static final int LIGHT_CLUSTER_WIDTH_X = 12;

    /**
     * The width of the light cluster grid's y dimension
     */
    public static final int LIGHT_CLUSTER_WIDTH_Y = 12;
    
    /**
     * The width of the light cluster grid's z dimension
     */
    public static final int LIGHT_CLUSTER_WIDTH_Z = 24;

    /**
     * The size of a single point light struct
     */
    static final int POINT_LIGHT_STRUCT_SIZE = 16 + 16 + 4 + 4 + 4 + 4;

    /**
     * The maximum number of point lights
     */
    static final int MAX_POINT_LIGHTS = 2048;

    /**
     * The size of the point light buffer
     */
    static final int POINT_LIGHT_BUFFER_SIZE = POINT_LIGHT_STRUCT_SIZE * MAX_POINT_LIGHTS;

    /**
     * The number of "threads" to run for light culling
     */
    static final int CULL_LOCAL_SIZE = 128;

    /**
     * The number of work groups to dispatch for the light culling phase
     * !!note!! DISPATCH_LOCAL_COUNT * CULL_LOCAL_SIZE must equal (LIGHT_CLUSTER_WIDTH_X * LIGHT_CLUSTER_WIDTH_Y * LIGHT_CLUSTER_WIDTH_Z)
     */
    static final int DISPATCH_LOCAL_COUNT = 27;

    /**
     * Size of the direct light buffer
     */
    static final int DIRECT_LIGHT_BUFFER_SIZE = 16 + 16 + 16;

    /**
     * Bind point for the cluster ssbo
     */
    public static final int CLUSTER_SSBO_BIND_POINT = 1;

    /**
     * Bind point for the point light ssbo
     */
    public static final int POINT_LIGHT_SSBO_BIND_POINT = 2;

    /**
     * Bind point for the direct light ssbo
     */
    public static final int DIRECT_LIGHT_SSBO_BIND_POINT = 3;

    /**
     * The cluster grid ssbo
        Light cluster struct:
        {
            vec4 minPoint; //16 bytes
            vec4 maxPoint; //16 bytes
            unsigned int count; //4 bytes
            unsigned int lightIndices[MAX_LIGHTS_PER_CLUSTER]; //assuming MAX_LIGHTS_PER_CLUSTER is 100, 400 bytes
        }
        Totals to 436 bytes
     */
    private ShaderStorageBuffer clusterGridSSBO;

    /**
     * The map of all entities to point lights
     */
    private Map<Entity,PointLight> entityPointLightMap;

    /**
     * The buffer of all point light data
    
        Point light struct:
        {
            vec4 position; //16 bytes
            vec4 color; //16 bytes
            float constant; //4 bytes
            float linear; // 4 bytes
            float quadratic; // 4 bytes
            float radius; //4 bytes
        }


     */
    private ShaderStorageBuffer pointLightSSBO;


    /**
     * The direct light ssbo
     * 
     struct DirectLight {
        vec4 direction; //16 bytes
        vec4 color; //16 bytes
        vec4 ambientColor; //16 bytes
    };
     */
    private ShaderStorageBuffer dirLightSSBO;

    /**
     * The directional light
     */
    private DirectionalLight directionalLight;

    /**
     * Color of the ambient light
     */
    private Vector3f ambientLightColor = new Vector3f(0.5f);

    /**
     * Constructor
     */
    private LightManager(){
    }

    /**
     * Creates a light manager
     * @return The light manager
     */
    public static LightManager create(){
        LightManager rVal = new LightManager();

        //
        //create the cluster ssbo
        rVal.clusterGridSSBO = new ShaderStorageBuffer(
            CLUSTER_STRUCT_SIZE * LIGHT_CLUSTER_WIDTH_X * LIGHT_CLUSTER_WIDTH_Y * LIGHT_CLUSTER_WIDTH_Z,
            BufferUsage.STATIC,
            BufferAccess.COPY
        );

        //
        //create pointlight structures
        rVal.entityPointLightMap = new HashMap<Entity,PointLight>();
        rVal.pointLightSSBO = new ShaderStorageBuffer(POINT_LIGHT_BUFFER_SIZE, BufferUsage.DYNAMIC, BufferAccess.DRAW);

        //
        //create direct light ssbo
        rVal.dirLightSSBO = new ShaderStorageBuffer(DIRECT_LIGHT_BUFFER_SIZE, BufferUsage.DYNAMIC, BufferAccess.DRAW);
        rVal.directionalLight = new DirectionalLight(new Vector3f(0,-1,0));
        rVal.directionalLight.setColor(new Vector3f(0.5f));


        return rVal;
    }

    /**
     * Computes the light culling clusters based on a camera
     * @param renderPipelineState The render pipeline state
     * @param openGLState The opengl state
     * @param camera The camera
     */
    private void computeLightCulling(RenderPipelineState renderPipelineState, OpenGLState openGLState, Entity camera){

        openGLState.glViewport(Globals.WINDOW_WIDTH, Globals.WINDOW_HEIGHT);

        //
        //Build AABBs
        ComputeShader clusterComp = Globals.assetManager.fetchComputeShader(AssetDataStrings.COMPUTE_LIGHT_CLUSTER);
        if(clusterComp != null){
            openGLState.setActiveShader(renderPipelineState, clusterComp);
            clusterComp.setUniform(openGLState, "zNear", CameraEntityUtils.getNearClip(camera));
            clusterComp.setUniform(openGLState, "zFar", CameraEntityUtils.getFarClip(camera));
            clusterComp.setUniform(openGLState, "inverseProjection", new Matrix4d(Globals.renderingEngine.getProjectionMatrix()).invert());
            clusterComp.setUniform(openGLState, "gridSize", new Vector3i(LIGHT_CLUSTER_WIDTH_X,LIGHT_CLUSTER_WIDTH_Y,LIGHT_CLUSTER_WIDTH_Z));
            clusterComp.setUniform(openGLState, "screenDimensions", openGLState.getViewport());

            clusterComp.dispatch(LIGHT_CLUSTER_WIDTH_X,LIGHT_CLUSTER_WIDTH_Y,LIGHT_CLUSTER_WIDTH_Z);
            MemoryBarrier.glMemoryBarrier(Barrier.GL_SHADER_STORAGE_BARRIER_BIT);
        }

        //
        //cull lights
        ComputeShader lightCull = Globals.assetManager.fetchComputeShader(AssetDataStrings.COMPUTE_LIGHT_CULL);
        openGLState.setActiveShader(renderPipelineState, lightCull);
        lightCull.setUniform(openGLState, "viewMatrix", Globals.renderingEngine.getViewMatrix());
        lightCull.setUniform(openGLState, "lightCount", this.entityPointLightMap.values().size());

        int dispatchLocal = (LIGHT_CLUSTER_WIDTH_X * LIGHT_CLUSTER_WIDTH_Y * LIGHT_CLUSTER_WIDTH_Z) / CULL_LOCAL_SIZE;
        lightCull.dispatch(dispatchLocal, ComputeShader.DEFAULT_LOCAL_SIZE, ComputeShader.DEFAULT_LOCAL_SIZE);
        MemoryBarrier.glMemoryBarrier(Barrier.GL_SHADER_STORAGE_BARRIER_BIT);
    }

    /**
     * Pushes latest data to buffers
     */
    private void pushToBuffers(RenderPipelineState renderPipelineState, OpenGLState openGLState, Entity camera){
        int i = 0;

        //
        //push point lights
        ByteBuffer pointLightBuffer = pointLightSSBO.getBuffer();
        pointLightBuffer.limit(pointLightBuffer.capacity());
        for(PointLight light : this.entityPointLightMap.values()){
            if(i >= MAX_POINT_LIGHTS){
                LoggerInterface.loggerRenderer.WARNING("CLOSE TO MAXIMUM NUMBER OF POINT LIGHTS!");
                break;
            }
            //
            //position
            Vector3d modifiedCameraPos = new Vector3d(light.getPosition()).sub(CameraEntityUtils.getCameraCenter(camera));
            pointLightBuffer.putFloat((float)modifiedCameraPos.x);
            pointLightBuffer.putFloat((float)modifiedCameraPos.y);
            pointLightBuffer.putFloat((float)modifiedCameraPos.z);
            pointLightBuffer.putFloat(1);

            //
            //color
            pointLightBuffer.putFloat(light.getColor().x);
            pointLightBuffer.putFloat(light.getColor().y);
            pointLightBuffer.putFloat(light.getColor().z);
            pointLightBuffer.putFloat(1);

            //
            //const
            pointLightBuffer.putFloat(light.getConstant());
            pointLightBuffer.putFloat(light.getLinear());
            pointLightBuffer.putFloat(light.getQuadratic());
            pointLightBuffer.putFloat(light.getRadius());
        }
        if(pointLightBuffer.position() > 0){
            pointLightBuffer.flip();
            pointLightSSBO.upload(openGLState);
        }

        //
        //push direct light
        ByteBuffer directLightBuffer = dirLightSSBO.getBuffer();
        {
            //
            //direction
            directLightBuffer.putFloat(directionalLight.getDirection().x);
            directLightBuffer.putFloat(directionalLight.getDirection().y);
            directLightBuffer.putFloat(directionalLight.getDirection().z);
            directLightBuffer.putFloat(0);

            //
            //color
            directLightBuffer.putFloat(directionalLight.getColor().x);
            directLightBuffer.putFloat(directionalLight.getColor().y);
            directLightBuffer.putFloat(directionalLight.getColor().z);
            directLightBuffer.putFloat(0);

            //
            //ambient light color
            directLightBuffer.putFloat(ambientLightColor.x);
            directLightBuffer.putFloat(ambientLightColor.y);
            directLightBuffer.putFloat(ambientLightColor.z);
            directLightBuffer.putFloat(0);
        }
        directLightBuffer.flip();
        dirLightSSBO.upload(openGLState);
    }

    /**
     * Updates the light manager
     * @param renderPipelineState The rendering state
     * @param openGLState The opengl state
     * @param camera The camera
     */
    public void update(RenderPipelineState renderPipelineState, OpenGLState openGLState, Entity camera){
        if(camera != null){
            this.pushToBuffers(renderPipelineState, openGLState, camera);
            this.computeLightCulling(renderPipelineState, openGLState, camera);
        }
    }

    /**
     * Gets the cluster grid ssbo
     * @return The cluster grid ssbo
     */
    public ShaderStorageBuffer getClusterGridSSBO(){
        return this.clusterGridSSBO;
    }

    /**
     * Gets the point light ssbo
     * @return The point light ssbo
     */
    public ShaderStorageBuffer getPointLightSSBO(){
        return this.pointLightSSBO;
    }

    /**
     * Gets the direct light ssbo
     * @return The direct light ssbo
     */
    public ShaderStorageBuffer getDirectLightSSBO(){
        return this.dirLightSSBO;
    }

    /**
     * Gets the directional light
     * @return The directional light
     */
    public DirectionalLight getDirectionalLight(){
        return directionalLight;
    }

    /**
     * Checks if the light manager has a light attached to the provided entity
     * @param entity The entity
     * @return true if there is an attached light, false otherwise
     */
    public boolean hasAttachedLight(Entity entity){
        return this.entityPointLightMap.containsKey(entity);
    }

    /**
     * Gets the point light associated with a given entity
     * @param entity The entity
     * @return The point light if it exists, null otherwise
     */
    public PointLight getPointLight(Entity entity){
        return entityPointLightMap.get(entity);
    }

    /**
     * Destroys the light attached to an entity if one is attached
     * @param entity The entity
     */
    public void destroyPointLight(Entity entity){
        if(entityPointLightMap.containsKey(entity)){
            entityPointLightMap.remove(entity);
        }
    }

    /**
     * Creates a point light from a description.
     * <p>
     * Note: If there is a light already assigned to the entity, it will be overwritten by the new light
     * </p>
     * @param entity The entity to assign the light to
     * @param description The description
     * @return The light
     */
    public PointLight createPointLight(Entity entity, PointLightDescription description){
        PointLight light = new PointLight(description);
        this.entityPointLightMap.put(entity, light);
        return light;
    }



}