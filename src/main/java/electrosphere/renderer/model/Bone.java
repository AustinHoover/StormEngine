package electrosphere.renderer.model;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4d;
import org.lwjgl.assimp.AIBone;


/**
 * Keeps track of bone data
 */
public class Bone {

    /**
     * the name of the bone
     */
    public String boneID;

    /**
     * the number of vertices affected by this bone
     */
    private int numWeights;

    /**
     * The map of index of vertex to weight of this bone on that vertex
     */
    private Map<Integer,Float> weights = new HashMap<Integer,Float>();

    /**
     * the mOffsetMatrix -- transforms from mesh space to bone space in bind pose
     */
    private Matrix4d mOffsetMatrix;

    /**
     * the current deform value of the bone
     */
    private Matrix4d deform = new Matrix4d();

    /**
     * the final transform that is used for drawing, data, etc
     */
    private Matrix4d finalTransform = new Matrix4d();

    /**
     * the raw data for the bone
     */
    public AIBone raw_data;

    /**
     * Cnostructor
     */
    public Bone(){
    }

    /**
     * Constructor
     * @param raw_data The raw assimp data
     */
    public Bone(AIBone raw_data){
        boneID = raw_data.mName().dataString();
        mOffsetMatrix = electrosphere.util.Utilities.convertAIMatrixd(raw_data.mOffsetMatrix());
        numWeights = raw_data.mNumWeights();
        this.raw_data = raw_data;
    }

    /**
     * Stores a bone weight
     * @param index the index of the bone in the bone array in shader
     * @param weight the weight for the given bone
     */
    public void putWeight(int index, float weight){
        weights.put(index,weight);
    }

    /**
     * Returns the weight map
     */
    public Map<Integer,Float> getWeights(){
        return weights;
    }

    /**
     * Gets the offset matrix for this bone
     * @return The offset matrix
     */
    public Matrix4d getMOffset(){
        return mOffsetMatrix;
    }

    /**
     * Sets the offset matrix for this bone
     * @param mTransform the offset matrix
     */
    public void setMOffset(Matrix4d mOffset){
        this.mOffsetMatrix.set(mOffset);
    }

    /**
     * Gets the deform matrix of the bone
     * @return The deform matrix
     */
    public Matrix4d getDeform(){
        return deform;
    }

    /**
     * Sets the deform matrix of the bone
     * @param deform The deform matrix
     */
    public void setDeform(Matrix4d deform){
        this.deform.set(deform);
    }

    /**
     * Gets the final transform of the bone
     * @return The final transform
     */
    public Matrix4d getFinalTransform(){
        return finalTransform;
    }

    /**
     * Sets the final transform of the bone
     * @param finalTransform The final transform
     */
    public void setFinalTransform(Matrix4d finalTransform){
        this.finalTransform.set(finalTransform);
    }

    /**
     * Gets the number of weights
     * @return The number of weights
     */
    public int getNumWeights(){
        return numWeights;
    }
}
