package electrosphere.client.terrain.data;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import electrosphere.entity.state.collidable.TriGeomData;

/**
 * The data required to generate a texture 
 */
public class TerrainChunkData implements TriGeomData {

    /**
     * The vertices
     */
    float[] vertices;

    /**
     * The normals
     */
    float[] normals;

    /**
     * The indices of the faces
     */
    int[] faceElements;

    /**
     * The UVs
     */
    float[] uvs;

    /**
     * what textures in the atlas to sample
     */
    float[] textureSamplers;

    /**
     * HOW MUCH of each texture in the atlas to sample
     */
    float[] textureRatioVectors;

    /**
     * The list of vertices as vectors
     */
    List<Vector3f> vertList;

    /**
     * The list of normals as vectors
     */
    List<Vector3f> normalList;

    /**
     * The list of number of triangles that share a vert
     */
    List<Integer> triangleSharingVertList;

    /**
     * List of texture sampler values
     */
    List<Vector3f> samplerTriangles;

    /**
     * The various buffers of data to send to the gpu
     */
    FloatBuffer vertexArrayBufferData = null;
    FloatBuffer normalArrayBufferData = null;
    FloatBuffer textureArrayBufferData = null;
    IntBuffer elementArrayBufferData = null;
    FloatBuffer samplerBuffer = null;
    FloatBuffer ratioBuffer = null;


    /**
     * The LOD of the model
     */
    int lod;

    /**
     * Creates an object to hold data required to generate a chunk
     * @param vertices
     * @param normals
     * @param faceElements
     * @param uvs
     * @param textureSamplers
     * @param lod The LOD of the model
     */
    public TerrainChunkData(float[] vertices, float[] normals, int[] faceElements, float[] uvs, float[] textureSamplers, float[] textureRatioVectors, int lod){
        this.vertices = vertices;
        this.normals = normals;
        this.faceElements = faceElements;
        this.uvs = uvs;
        this.textureSamplers = textureSamplers;
        this.textureRatioVectors = textureRatioVectors;
        this.lod = lod;
    }

    /**
     * Constructor used by the pool
     */
    protected TerrainChunkData(){
        this.vertList = new LinkedList<Vector3f>();
        this.normalList = new LinkedList<Vector3f>();
        this.triangleSharingVertList = new LinkedList<Integer>();
        this.samplerTriangles = new LinkedList<Vector3f>();
    }

    /**
     * Allocates and fills the buffers to send to the gpu
     */
    public void constructBuffers(){
        vertexArrayBufferData = BufferUtils.createFloatBuffer(vertices.length);
        vertexArrayBufferData.put(vertices);

        normalArrayBufferData = BufferUtils.createFloatBuffer(normals.length);
        normalArrayBufferData.put(normals);

        textureArrayBufferData = BufferUtils.createFloatBuffer(uvs.length);
        textureArrayBufferData.put(uvs);

        elementArrayBufferData = BufferUtils.createIntBuffer(faceElements.length);
        elementArrayBufferData.put(faceElements);

        samplerBuffer = BufferUtils.createFloatBuffer(textureSamplers.length);
        samplerBuffer.put(this.textureSamplers);
        
        ratioBuffer = BufferUtils.createFloatBuffer(textureRatioVectors.length);
        ratioBuffer.put(this.textureRatioVectors);
    }

    @Override
    public float[] getVertices(){
        return vertices;
    }

    /**
     * Gets the normal data
     * @return the normal data
     */
    public float[] getNormals(){
        return normals;
    }

    @Override
    public int[] getFaceElements(){
        return faceElements;
    }

    /**
     * Gets the uv data
     * @return the uv data
     */
    public float[] getUVs(){
        return uvs;
    }

    /**
     * Gets the texture sampler data
     * @return the texture sampler data
     */
    public float[] getTextureSamplers(){
        return textureSamplers;
    }

    /**
     * Gets the texture ratio vector data
     * @return the texture ratio vector data
     */
    public float[] getTextureRatioVectors(){
        return textureRatioVectors;
    }

    /**
     * Gets the LOD of the model
     * @return The LOD
     */
    public int getLOD(){
        return lod;
    }

    /**
     * Gets the vertex buffer
     * @return The buffer
     */
    public FloatBuffer getVertexArrayBufferData() {
        return vertexArrayBufferData;
    }

    /**
     * Gets the normal buffer
     * @return The buffer
     */
    public FloatBuffer getNormalArrayBufferData() {
        return normalArrayBufferData;
    }

    /**
     * Gets the texture array buffer
     * @return The buffer
     */
    public FloatBuffer getTextureArrayBufferData() {
        return textureArrayBufferData;
    }

    /**
     * Gets the index buffer
     * @return The buffer
     */
    public IntBuffer getElementArrayBufferData() {
        return elementArrayBufferData;
    }

    /**
     * Gets the sampler index buffer
     * @return The buffer
     */
    public FloatBuffer getSamplerBuffer() {
        return samplerBuffer;
    }

    /**
     * Gets the sampler ratio buffer
     * @return The buffer
     */
    public FloatBuffer getRatioBuffer() {
        return ratioBuffer;
    }

    /**
     * Gets the vertex list
     * @return The vertex list
     */
    public List<Vector3f> getVertList() {
        return vertList;
    }

    /**
     * Gets the normal list
     * @return The normal list
     */
    public List<Vector3f> getNormalList() {
        return normalList;
    }

    /**
     * Gets the triangle sharing vert list
     * @return The triangle sharing vert list
     */
    public List<Integer> getTriangleSharingVertList() {
        return triangleSharingVertList;
    }

    /**
     * Gets the sampler triangles list
     * @return The sampler triangles list
     */
    public List<Vector3f> getSamplerTriangles() {
        return samplerTriangles;
    }
    
    

}
