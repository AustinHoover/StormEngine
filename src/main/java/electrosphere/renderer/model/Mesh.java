package electrosphere.renderer.model;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.RenderingEngine;
import electrosphere.renderer.actor.instance.InstanceData;
import electrosphere.renderer.actor.mask.ActorTextureMask;
import electrosphere.renderer.light.LightManager;
import electrosphere.renderer.shader.StandardUniformManager;
import electrosphere.renderer.shader.VisualShader;
import electrosphere.renderer.texture.Texture;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Sphered;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4d;
import org.joml.Vector4f;

import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryStack;

/**
 * A mesh, a collection of buffer data on the GPU
 */
public class Mesh {

    /**
     * The default index for the vertex attribute
     */
    public static final int DEFAULT_VERTEX_ATTRIB_INDEX = 0;

    /**
     * The default index for the normal attribute
     */
    public static final int DEFAULT_NORMAL_ATTRIB_INDEX = 1;

    /**
     * The default index for the bone weights attribute
     */
    public static final int DEFAULT_BONE_WEIGHTS_ATTRIB_INDEX = 2;

    /**
     * The default index for the bone indices attribute
     */
    public static final int DEFAULT_BONE_INDICES_ATTRIB_INDEX = 3;

    /**
     * The default index for the texture attribute
     */
    public static final int DEFAULT_TEXTURE_ATTRIB_INDEX = 4;

    /**
     * Static matrix used for draw calls
     */
    private static final Matrix4d drawMat4 = new Matrix4d();

    /**
     * Static vector used for draw calls
     */
    private static final Vector3f drawVec3f = new Vector3f();

    /**
     * Static vector used for draw calls
     */
    private static final Vector3i drawVec3i = new Vector3i();

    /**
     * The name of this mesh
     */
    private String meshName;

    /**
     * The parent model
     * <p>
     * THIS IS NOT GUARANTEED TO BE THE PARENT MODEL THAT THIS WAS LOADED IN
     * THIS CAN BE POST-LOAD SET IN MODEL VIA MODELMASK BEHAVIOR
     * </p>
     */
    private Model parent;

    /**
     * Pointer to the vertex buffer
     */
    private int vertexBuffer;

    /**
     * Pointer to the normal buffer
     */
    private int normalBuffer;

    /**
     * Pointer to the element array buffer
     */
    private int elementArrayBuffer;

    /**
     * pulls double duty as both the element array count as well as the direct array count, based on whichever is used
     */
    private int elementCount;

    /**
     * Pointer to the VAO
     */
    private int vertexArrayObject;

    /**
     * Pointer to the bone weight buffer
     */
    private int boneWeightBuffer;

    /**
     * Pointer to the bone index buffer
     */
    private int boneIndexBuffer;

    /**
     * Pointer to the texture coordinate buffer
     */
    private int textureCoordBuffer;

    /**
     * Tracks whether the mesh uses element instances or not
     */
    private boolean useElementArray = true;


    /**
     * The list of all bones in the mesh
     * <p>
     * THIS IS NOT GUARANTEED TO BE THE PARENT MODEL THAT THIS WAS LOADED IN
     * THIS CAN BE POST-LOAD SET IN MODEL VIA MODELMASK BEHAVIOR
     * </p>
     */
    private List<Bone> bones = new ArrayList<Bone>();

    /**
     * The list of bone names
     */
    private ArrayList<String> boneIdList = new ArrayList<String>();
    
    /**
     * the texture mask that may or may not be masking the mesh
     */
    private ActorTextureMask textureMask;
    
    /**
     * The main shader for the mesh
     */
    private VisualShader shader;

    /**
     * The OIT shader
     */
    private VisualShader oitShader;

    /**
     * the uniforms to be sent to the gpu
     */
    private HashMap<String,Object> uniforms = new HashMap<String,Object>();
    
    /**
     * the material currently associated with the mesh
     */
    private Material material;

    /**
     * The color to apply to the mesh
     */
    private Vector4d color = new Vector4d(1);

    /**
     * the bounding sphere for this mesh
     */
    private Sphered boundingSphere;
    
    /**
     * Creates a mesh (does not initialize any data)
     * @param name The name of the mesh
     */
    public Mesh(String name){
        this.meshName = name;
        this.boundingSphere = new Sphered();
    }

    /**
     * Generates the VAO for this mesh
     */
    public void generateVAO(OpenGLState openGLState){
        vertexArrayObject = GL45.glGenVertexArrays();
        openGLState.glBindVertexArray(vertexArrayObject);
    }

    /**
     * Frees this mesh
     */
    public void free(){
        GL45.glDeleteBuffers(new int[]{
            vertexBuffer,
            normalBuffer,
            elementArrayBuffer,
            boneWeightBuffer,
            boneIndexBuffer,
            textureCoordBuffer,
        });
        GL45.glDeleteVertexArrays(vertexArrayObject);
    }
    
    /**
     * Buffers vertex data to the gpu under this mesh container
     * @param verticies the vertex buffer
     * @param vertexDimension the dimensionality of the data (2d vectors, 3d vectors, 4d vectors, etc)
     */
    public void bufferVertices(FloatBuffer verticies, int vertexDimension){
        if(!EngineState.EngineFlags.HEADLESS){
            vertexBuffer = this.bufferCustomFloatAttribArray(verticies,vertexDimension,Mesh.DEFAULT_VERTEX_ATTRIB_INDEX);
        }
    }
    
    /**
     * Buffers normals to the gpu under this mesh container
     * @param normals the normal data
     * @param normalDimension the dimensionality of the data (2d vector, 3d vector, 4d vector)
     */
    public void bufferNormals(FloatBuffer normals, int normalDimension){
        if(!EngineState.EngineFlags.HEADLESS){
            normalBuffer = this.bufferCustomFloatAttribArray(normals,normalDimension,Mesh.DEFAULT_NORMAL_ATTRIB_INDEX);
        }
    }
    
    /**
     * Buffers faces to the GPU
     * @param faces The face data
     * @param elementCount The number of faces
     */
    public void bufferFaces(IntBuffer faces, int elementCount){
        if(elementCount < 1){
            throw new Error("Sending mesh with 0 faces!");
        }
        if(!EngineState.EngineFlags.HEADLESS){
            elementArrayBuffer = GL45.glGenBuffers();
            GL45.glBindBuffer(GL45.GL_ELEMENT_ARRAY_BUFFER, elementArrayBuffer);
            GL45.glBufferData(GL45.GL_ELEMENT_ARRAY_BUFFER, faces, GL45.GL_STATIC_DRAW);
            this.elementCount = elementCount;
        }
    }

    /**
     * Sets the number of elements in the directly referenced arrays underneath this mesh
     * @param directArraySize The number of elements (ie the number of vertices, or normals, etc)
     */
    public void setDirectArraySize(int directArraySize){
        this.elementCount = directArraySize;
    }
    
    /**
     * Buffers texture coordinates to the gpu
     * @param coords the texture coordinates data
     * @param textureDimension The dimensionality of the texture coordinate data (3d vec, 4d vec, etc)
     */
    public void bufferTextureCoords(FloatBuffer coords, int textureDimension){
        if(!EngineState.EngineFlags.HEADLESS){
            textureCoordBuffer = this.bufferCustomFloatAttribArray(coords, textureDimension, Mesh.DEFAULT_TEXTURE_ATTRIB_INDEX);
        }
    }

    /**
     * Buffers bone indices to the GPU
     * @param buffer The buffer containing the bone indices
     */
    public void bufferBoneIndices(FloatBuffer buffer){
        boneIndexBuffer = GL45.glGenBuffers();
        GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, boneIndexBuffer);
        GL45.glBufferData(GL45.GL_ARRAY_BUFFER, buffer, GL45.GL_STATIC_DRAW);
        GL45.glVertexAttribPointer(Mesh.DEFAULT_BONE_INDICES_ATTRIB_INDEX, 4, GL45.GL_FLOAT, false, 0, 0);
        GL45.glEnableVertexAttribArray(3);
    }

    /**
     * Buffers bone weights to the gpu
     * @param buffer The buffer containing the bone weights
     */
    public void bufferBoneWeights(FloatBuffer buffer){
        boneWeightBuffer = GL45.glGenBuffers();
        GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, boneWeightBuffer);
        GL45.glBufferData(GL45.GL_ARRAY_BUFFER, buffer, GL45.GL_STATIC_DRAW);
        GL45.glVertexAttribPointer(Mesh.DEFAULT_BONE_WEIGHTS_ATTRIB_INDEX, 4, GL45.GL_FLOAT, false, 0, 0);
        GL45.glEnableVertexAttribArray(2);
    }
    
    /**
     * Sends a float buffer to the gpu
     * @param buffer The buffer
     * @param bufferDimension The dimensionality of the buffer (2d vector, 3d vector, 4d vector)
     * @param attribIndex The attribute index of the buffer (ie what number will it show up as in the shader)
     * @return The pointer to the opengl buffer created
     */
    public int bufferCustomFloatAttribArray(FloatBuffer buffer, int bufferDimension, int attribIndex){
        int bufferPointer = 0;
        if(!EngineState.EngineFlags.HEADLESS){
            bufferPointer = GL45.glGenBuffers();
            GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, bufferPointer);
            GL45.glBufferData(GL45.GL_ARRAY_BUFFER, buffer, GL45.GL_STATIC_DRAW);
            GL45.glVertexAttribPointer(attribIndex, bufferDimension, GL45.GL_FLOAT, false, 0, 0);
            GL45.glEnableVertexAttribArray(attribIndex);
        }
        return bufferPointer;
    }
    
    /**
     * Sends an int buffer to the gpu
     * @param buffer The buffer
     * @param bufferDimension The dimensionality of the buffer (2d vector, 3d vector, 4d vector)
     * @param attribIndex The attribute index of the buffer (ie what number will it show up as in the shader)
     * @return The pointer to the opengl buffer created
     */
    public int bufferCustomIntAttribArray(IntBuffer buffer, int bufferDimension, int attribIndex){
        int bufferPointer = 0;
        if(!EngineState.EngineFlags.HEADLESS){
            bufferPointer = GL45.glGenBuffers();
            GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, bufferPointer);
            GL45.glBufferData(GL45.GL_ARRAY_BUFFER, buffer, GL45.GL_STATIC_DRAW);
            GL45.glVertexAttribIPointer(attribIndex, bufferDimension, GL45.GL_INT, 0, 0);
            GL45.glEnableVertexAttribArray(attribIndex);
        }
        return bufferPointer;
    }
    

    /**
     * Sends an unsigned int buffer to the gpu
     * @param buffer The buffer
     * @param bufferDimension The dimensionality of the buffer (2d vector, 3d vector, 4d vector)
     * @param attribIndex The attribute index of the buffer (ie what number will it show up as in the shader)
     * @return The pointer to the opengl buffer created
     */
    public int bufferCustomUIntAttribArray(IntBuffer buffer, int bufferDimension, int attribIndex){
        int bufferPointer = 0;
        if(!EngineState.EngineFlags.HEADLESS){
            bufferPointer = GL45.glGenBuffers();
            GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, bufferPointer);
            GL45.glBufferData(GL45.GL_ARRAY_BUFFER, buffer, GL45.GL_STATIC_DRAW);
            GL45.glVertexAttribIPointer(attribIndex, bufferDimension, GL45.GL_UNSIGNED_INT, 0, 0);
            GL45.glEnableVertexAttribArray(attribIndex);
        }
        return bufferPointer;
    }
    
    /**
     * Sets the texture mask for the mesh
     * @param textureMask the texture mask
     */
    public void setTextureMask(ActorTextureMask textureMask){
        this.textureMask = textureMask;
    }
    
    /**
     * Sets the material for the mesh
     * @param material the material
     */
    public void setMaterial(Material material){
        this.material = material;
    }

    /**
     * Sets the shader of this mesh
     * @param shader The shader
     */
    public void setShader(VisualShader shader){
        this.shader = shader;
    }

    /**
     * Gets the shader of this mesh
     * @return The shader
     */
    public VisualShader getShader(){
        return shader;
    }

    /**
     * Sets the order independent transparency shader
     * @param shader The shader
     */
    public void setOITShader(VisualShader shader){
        this.oitShader = shader;
    }
    
    
    /**
     * Sets a uniform on the mesh
     * @param key the uniform key
     * @param o the value to set the uniform to
     */
    public void setUniform(String key, Object o){
        uniforms.put(key, o);
    }
    
    private void bufferAllUniforms(OpenGLState openGLState){
        for(String key : uniforms.keySet()){
            Object currentUniformRaw = uniforms.get(key);
            if(currentUniformRaw instanceof Matrix4f){
                throw new Error("Unsupported data type!");
            }
            if(currentUniformRaw instanceof Matrix4d){
                Matrix4d currentUniform = (Matrix4d)currentUniformRaw;
                openGLState.getActiveShader().setUniform(openGLState, key, currentUniform);
            }
            if(currentUniformRaw instanceof Vector3d){
                throw new Error("Unsupported data type!");
            }
            if(currentUniformRaw instanceof Vector3f){
                Vector3f currentUniform = (Vector3f)currentUniformRaw;
                openGLState.getActiveShader().setUniform(openGLState, key, currentUniform);
            }
            if(currentUniformRaw instanceof Vector4f){
                Vector4f currentUniform = (Vector4f)currentUniformRaw;
                openGLState.getActiveShader().setUniform(openGLState, key, currentUniform);
            }
            if(currentUniformRaw instanceof Integer){
                int currentUniform = (Integer)currentUniformRaw;
                openGLState.getActiveShader().setUniform(openGLState, key, currentUniform);
            }
        }
    }
    

    
    /**
     * Draws the mesh
     * @param renderPipelineState The state of the render pipeline
     */
    public void complexDraw(RenderPipelineState renderPipelineState, OpenGLState openGLState){
        Globals.profiler.beginAggregateCpuSample("Mesh.complexDraw - total");

        //bind vao off the rip
        openGLState.glBindVertexArray(vertexArrayObject);
        Globals.renderingEngine.checkError();
        
        if(renderPipelineState.getUseMeshShader()){
            Globals.profiler.beginAggregateCpuSample("Mesh shader");
            VisualShader selectedProgram = null;
            switch(renderPipelineState.getSelectedShader()){
                case PRIMARY: {
                    selectedProgram = shader;
                } break;
                case OIT: {
                    selectedProgram = oitShader;
                } break;
            }
            if(selectedProgram == null){
                selectedProgram = shader;
            }
            if(selectedProgram == null){
                selectedProgram = Globals.defaultMeshShader;
            }
            openGLState.setActiveShader(renderPipelineState, selectedProgram);
            Globals.profiler.endCpuSample();
        }
        
        if(renderPipelineState.getUseLight()){
            Globals.profiler.beginAggregateCpuSample("Light");
            //Until we switch to uniform buffer objects we will have to buffer lighting data here manually each time we draw
            //side note:    :(
            if(Globals.renderingEngine.getLightManager() == null){
                //don't buffer as the light manager hasn't initialized
            } else {
                LightManager lightManager = Globals.renderingEngine.getLightManager();
                openGLState.glBindBufferBase(LightManager.CLUSTER_SSBO_BIND_POINT, lightManager.getClusterGridSSBO());
                openGLState.glBindBufferBase(LightManager.POINT_LIGHT_SSBO_BIND_POINT, lightManager.getPointLightSSBO());
                openGLState.glBindBufferBase(LightManager.DIRECT_LIGHT_SSBO_BIND_POINT, lightManager.getDirectLightSSBO());

                //set uniforms required
                openGLState.getActiveShader().setUniform(openGLState, "zNear", CameraEntityUtils.getNearClip(Globals.clientState.playerCamera));
                openGLState.getActiveShader().setUniform(openGLState, "zFar", CameraEntityUtils.getFarClip(Globals.clientState.playerCamera));
                openGLState.getActiveShader().setUniform(openGLState, "gridSize", drawVec3i.set(LightManager.LIGHT_CLUSTER_WIDTH_X,LightManager.LIGHT_CLUSTER_WIDTH_Y,LightManager.LIGHT_CLUSTER_WIDTH_Z));
                openGLState.getActiveShader().setUniform(openGLState, "screenDimensions", openGLState.getViewport());
                
            }
            Globals.renderingEngine.checkError();
            Globals.profiler.endCpuSample();
        }
        
        if(renderPipelineState.getUseMaterial() && textureMask == null){
            Globals.profiler.beginAggregateCpuSample("applyMaterial");
            if(material == null){
                Globals.renderingEngine.getDefaultMaterial().applyMaterial(openGLState);
            } else {
                material.applyMaterial(openGLState);
            }
            Globals.renderingEngine.checkError();
            Globals.profiler.endCpuSample();
        }
        
        
        
        
        
        //
        //The texture masking logic
        if(textureMask != null){
            Globals.profiler.beginAggregateCpuSample("Texture mask");
            //
            //path that uses already-defined texture objects
            if(textureMask.getTextures() != null){
                int i = 0;
                for(Texture texture : textureMask.getTextures()){
                    if(texture != null){
                        texture.bind(openGLState,5+i);
                    }
                    openGLState.getActiveShader().setUniform(openGLState, textureMask.getUniformNames().get(i), 5+i);
                    i++;
                }
            } else if(textureMask.getTexturePaths() != null){
                //
                //path that uses paths to textures in the asset manager
                int i = 0;
                for(String texturePath : textureMask.getTexturePaths()){
                    Texture texture = Globals.assetManager.fetchTexture(texturePath);
                    if(texture != null){
                        texture.bind(openGLState, i);
                    }
                    i++;
                }
            }
            Globals.renderingEngine.checkError();
            Globals.profiler.endCpuSample();
        }
        
        if(renderPipelineState.getUseShadowMap()){
            Globals.profiler.beginAggregateCpuSample("Shadow map");
            int shadowMapTextureUnit = 3;
            openGLState.glActiveTexture(GL45.GL_TEXTURE0 + shadowMapTextureUnit);
            Globals.renderingEngine.checkError();
            openGLState.glBindTexture(GL45.GL_TEXTURE_2D, RenderingEngine.lightBufferDepthTexture.getTexturePointer());
            Globals.renderingEngine.checkError();
            openGLState.getActiveShader().setUniform(openGLState, "shadowMap", shadowMapTextureUnit);
            Globals.profiler.endCpuSample();
        }
        
        
        boolean sentBones = false;
        Globals.profiler.beginAggregateCpuSample("Bones");
        drawMat4.identity();
        if(renderPipelineState.getUseBones()){
            //
            //Handle bones
            //
            if(bones != null && !bones.isEmpty()){
                Iterator<String> boneIterator = boneIdList.iterator();
                int incrementer = 0;
                while (boneIterator.hasNext()){
                    String boneName = boneIterator.next();
                    Bone currentBone = parent.getBoneMap().get(boneName);
                    String currentUniform = "bones[" + incrementer + "]";
                    if(currentBone != null){
                        Matrix4d currentMat = currentBone.getFinalTransform();
                        openGLState.getActiveShader().setUniform(openGLState, currentUniform, currentMat);
                    } else {
                        openGLState.getActiveShader().setUniform(openGLState, currentUniform, drawMat4);
                    }
                    incrementer++;
                }
                sentBones = true;
            }
        }
        if(!sentBones){
            for(int i = 0; i < 4; i++){
                String currentUniform = "bones[" + i + "]";
                openGLState.getActiveShader().setUniform(openGLState, currentUniform, drawMat4);
            }
        }
        Globals.profiler.endCpuSample();
        
        
        if(renderPipelineState.getBufferStandardUniforms()){
            Globals.profiler.beginAggregateCpuSample("Buffer standard uniforms");
            //buffer model/view/proj matrices
            try(MemoryStack stack = MemoryStack.stackPush()){
                openGLState.getActiveShader().setUniform(openGLState, "color", this.color);
                openGLState.getActiveShader().setUniform(openGLState, "model", parent.getModelMatrix());
                openGLState.getActiveShader().setUniform(openGLState, "viewPos", CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera));
                drawVec3f.set((float)parent.getWorldPos().x,(float)parent.getWorldPos().y,(float)parent.getWorldPos().z);
                openGLState.getActiveShader().setUniform(openGLState, "modelWorldPos", drawVec3f);
                openGLState.glBindBufferBase(StandardUniformManager.STANDARD_UNIFORM_BUFFER_BIND_POINT, Globals.renderingEngine.getStandardUniformManager().getStandardUnifomSSBO());
            }
            Globals.renderingEngine.checkError();
            Globals.profiler.endCpuSample();
        }
        
        if(renderPipelineState.getBufferNonStandardUniforms()){
            Globals.profiler.beginAggregateCpuSample("Nonstandard uniforms");
            this.bufferAllUniforms(openGLState);
            Globals.renderingEngine.checkError();
            Globals.profiler.endCpuSample();
        }

        if(renderPipelineState.getInstanced()){
            Globals.profiler.beginAggregateCpuSample("Instance logic");
            if(renderPipelineState.getInstanceData()!=null){
                InstanceData instanceData = renderPipelineState.getInstanceData();
                instanceData.upload(openGLState, renderPipelineState);
            }
            Globals.profiler.endCpuSample();
        }
        
        
        Globals.profiler.beginAggregateCpuSample("Mesh.complexDraw - Draw call");
        if(renderPipelineState.getInstanced()){
            if(renderPipelineState.getInstanceCount() > 0){
                GL45.glDrawElementsInstanced(GL45.GL_TRIANGLES, this.elementCount, GL45.GL_UNSIGNED_INT, 0, renderPipelineState.getInstanceCount());
                Globals.renderingEngine.checkError();
            }
        } else if(this.useElementArray){
            if(this.elementCount < 1){
                throw new Error("Failed to render mesh with invalid element count! " + this.getDebugData());
            }
            GL45.glDrawElements(GL45.GL_TRIANGLES, this.elementCount, GL45.GL_UNSIGNED_INT, 0);
            Globals.renderingEngine.checkError();
        } else {
            if(this.elementCount < 1){
                throw new Error("Failed to render mesh with invalid element count! " + this.getDebugData());
            }
            GL45.glDrawArrays(GL45.GL_TRIANGLES, 0, this.elementCount);
            Globals.renderingEngine.checkError();
        }
        Globals.profiler.endCpuSample();
        Globals.profiler.endCpuSample();
    }

    /**
     * Gets debug data for the mesh
     * @return The debug data in a string
     */
    private String getDebugData(){
        String rVal = "\n" +
        "Mesh name: " + this.meshName + "\n" +
        "Vertex buffer: " + this.vertexBuffer + "\n" +
        "Normal buffer: " + this.normalBuffer + "\n" +
        "Texture buffer: " + this.textureCoordBuffer + "\n" +
        "Bone Index buffer: " + this.boneIndexBuffer + "\n" +
        "Bone Weight buffer: " + this.boneWeightBuffer + "\n" +
        "Element buffer: " + this.elementArrayBuffer + "\n" +
        "Element count: " + this.elementCount + "\n" +
        "Use element array: " + this.useElementArray + "\n" +
        "";
        return rVal;
    }

    /**
     * Updates the bounding sphere of the mesh
     * @param x 
     * @param y
     * @param z
     * @param r
     */
    public void updateBoundingSphere(double x, double y, double z, double r){
        this.boundingSphere.x = x;
        this.boundingSphere.y = y;
        this.boundingSphere.z = z;
        this.boundingSphere.r = r;
    }

    /**
     * Gets the bounding sphere of this mesh
     * @return The bounding sphere
     */
    public Sphered getBoundingSphere(){
        return this.boundingSphere;
    }

    /**
     * Gets the material of the mesh
     * @return The material
     */
    public Material getMaterial(){
        return material;
    }

    /**
     * Gets whether this mesh has bones or not
     * @return true if has bones
     */
    public boolean hasBones(){
        return bones.size() > 0;
    }

    /**
     * Gets the name of this mesh
     * @return The name of the mesh
     */
    public String getMeshName(){
        return meshName;
    }

    /**
     * Sets the parent model of this mesh
     * @param parent The parent
     */
    public void setParent(Model parent){
        this.parent = parent;
    }

    /**
     * Gets the bones for this mesh
     * @return The list of bones
     */
    public List<Bone> getBones(){
        return bones;
    }

    /**
     * Sets the bones for this mesh
     * @param bones The list of bones
     */
    public void setBones(List<Bone> bones){
        this.bones = bones;
    }

    /**
     * Registers a bone id
     * @param boneId the bone id
     */
    public void registerBoneId(String boneId){
        this.boneIdList.add(boneId);
    }

    /**
     * Sets whether to use an element array or a direct array
     * @param useElementArray if true, use elements, else use direct array reference
     */
    public void setUseElementArray(boolean useElementArray){
        this.useElementArray = useElementArray;
    }

    /**
     * Gets whether the mesh uses an element array or a direct array
     * @return if true, use elements, else use direct array reference
     */
    public boolean getUseElementArray(){
        return this.useElementArray;
    }

    /**
     * Gets the vertex buffer's pointer
     * @return The vertex buffer's pointer
     */
    public int getVertexBuffer(){
        return vertexBuffer;
    }

    /**
     * Gets the normal buffer's pointer
     * @return The normal buffer's pointer
     */
    public int getNormalBuffer(){
        return normalBuffer;
    }

    /**
     * Gets the texture coord buffer's pointer
     * @return The texture coord buffer's pointer
     */
    public int getTextureCoordBuffer(){
        return textureCoordBuffer;
    }

}
