package electrosphere.renderer;

import org.joml.FrustumIntersection;
import org.joml.Matrix4d;
import org.joml.Matrix4f;

import electrosphere.renderer.actor.instance.InstanceData;

/**
 * Contains all the currently up to date state of how the render pipeline is configured.
 * Used in lots of different places to pass information up/down the render pipeline call stack and configure variables for passes.
 */
public class RenderPipelineState {
    
    /**
     * Enum of different phases of the pipeline
     */
    public static enum RenderPipelineStateEnum {
        SHADOW_MAP,
        MAIN_OIT,
        MAIN_NON_TRANSPARENT,
        NORMALS,
        COMPOSITE,
    }

    /**
     * Enum of different categories of shaders that can be selected
     */
    public static enum SelectedShaderEnum {
        PRIMARY,
        OIT,
    }

    //The current phase of the render pipeline
    RenderPipelineStateEnum state = RenderPipelineStateEnum.SHADOW_MAP;
    //The currently selected category of shader for the current render pipeline phase
    SelectedShaderEnum selectedShader = SelectedShaderEnum.PRIMARY;

    //Whether to use a mesh's default shader
    boolean useMeshShader = false;
    //Whether to buffer standard uniforms (model matrix, view matrix, etc)
    boolean bufferStandardUniforms = false;
    //Whether to buffer any nonstandard uniforms that may be in the mesh uniform map
    boolean bufferNonStandardUniforms = false;
    //Whether to use the material attached to the mesh or not
    boolean useMaterial = false;
    //Whether to use the shadow map or not
    boolean useShadowMap = false;
    //Whether to update and use bones
    boolean useBones = false;
    //Whether to use the light buffer or not
    boolean useLight = false;
    //Whether the current rendering call is of an instanced object
    boolean instanced = false;
    //The instance data for rendering an instanced object
    InstanceData instanceData;
    //the number of instances to draw
    int instanceCount = 0;

    //The pointer to the current shader program bound
    int currentShaderPointer;

    //Should actors frustum check (should be turned off in instancing for instance)
    boolean frustumCheck = true;

    //JOML-provided object to perform frustum culling
    FrustumIntersection frustumInt = new FrustumIntersection();

    public boolean getUseMeshShader(){
        return this.useMeshShader;
    }

    public void setUseMeshShader(boolean useMeshShader){
        this.useMeshShader = useMeshShader;
    }

    public boolean getBufferStandardUniforms(){
        return this.bufferStandardUniforms;
    }

    public void setBufferStandardUniforms(boolean bufferStandardUniforms){
        this.bufferStandardUniforms = bufferStandardUniforms;
    }

    public boolean getBufferNonStandardUniforms(){
        return this.bufferNonStandardUniforms;
    }

    public void setBufferNonStandardUniforms(boolean bufferNonStandardUniforms){
        this.bufferNonStandardUniforms = bufferNonStandardUniforms;
    }

    public boolean getUseMaterial(){
        return this.useMaterial;
    }

    public void setUseMaterial(boolean useMaterial){
        this.useMaterial = useMaterial;
    }

    public boolean getUseShadowMap(){
        return this.useShadowMap;
    }

    public void setUseShadowMap(boolean useShadowMap){
        this.useShadowMap = useShadowMap;
    }

    public boolean getUseBones(){
        return useBones;
    }

    public void setUseBones(boolean useBones){
        this.useBones = useBones;
    }

    public boolean getUseLight(){
        return this.useLight;
    }

    public void setUseLight(boolean useLight){
        this.useLight = useLight;
    }

    public SelectedShaderEnum getSelectedShader(){
        return selectedShader;
    }

    public void setSelectedShader(SelectedShaderEnum selectedShader){
        this.selectedShader = selectedShader;
    }

    public boolean getInstanced(){
        return this.instanced;
    }

    public void setInstanced(boolean instanced){
        this.instanced = instanced;
    }

    public InstanceData getInstanceData(){
        return this.instanceData;
    }

    public void setInstanceData(InstanceData instanceData){
        this.instanceData = instanceData;
    }

    public void setInstanceCount(int count){
        this.instanceCount = count;
    }

    public int getInstanceCount(){
        return this.instanceCount;
    }

    public int getCurrentShaderPointer(){
        return currentShaderPointer;
    }

    public void setCurrentShaderPointer(int currentShaderPointer){
        this.currentShaderPointer = currentShaderPointer;
    }

    /**
     * Updates the frustum intersection with the provided projection and view matrices
     * @param projectionMatrix the projection matrix
     * @param viewMatrix the view matrix
     */
    public void updateFrustumIntersection(Matrix4d projectionMatrix, Matrix4d viewMatrix){
        Matrix4f projectionViewMatrix = new Matrix4f();
        projectionViewMatrix.set(projectionMatrix);
        projectionViewMatrix.mul(new Matrix4f(viewMatrix));
        this.frustumInt.set(projectionViewMatrix);
    }

    /**
     * Gets the current render pipeline's frustum intersection object
     * @return The frustum intersection object
     */
    public FrustumIntersection getFrustumIntersection(){
        return frustumInt;
    }

    /**
     * Sets whether actors should frustum check or not
     * @param frustumCheck if true, will frustum check, otherwise will not
     */
    public void setFrustumCheck(boolean frustumCheck){
        this.frustumCheck = frustumCheck;
    }

    /**
     * Returns whether actors should frustum check or not
     * @return If true, frustum check, otherwise do not
     */
    public boolean shouldFrustumCheck(){
        return this.frustumCheck;
    }

}
