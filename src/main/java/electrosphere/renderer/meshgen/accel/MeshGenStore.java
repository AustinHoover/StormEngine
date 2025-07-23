package electrosphere.renderer.meshgen.accel;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Store used for meshgen. Relies on arrays to accelerate operations
 */
public class MeshGenStore {


    /**
     * Size of each array in this store
     */
    public static final int ARRAY_SIZE = 50000;





    //
    //object pooling potion of class
    //

    /**
     * List for pooling stores
     */
    private static final List<MeshGenStore> pool = new LinkedList<MeshGenStore>();

    /**
     * Lock for thread-safeing operations
     */
    private static ReentrantLock lock = new ReentrantLock();

    /**
     * Gets a MeshGenStore from the pool. Allocates if no free one is available.
     * @param type The type of the message
     * @return A MeshGenStore
     */
    public static MeshGenStore get(){
        MeshGenStore rVal = null;
        lock.lock();
        if(pool.size() > 0){
            rVal = pool.remove(0);
        } else {
            rVal = new MeshGenStore();
        }
        lock.unlock();
        return rVal;
    }

    /**
     * Releases a MeshGenStore back into the pool
     * @param data The object to release
     */
    public static void release(MeshGenStore data){
        lock.lock();
        MeshGenStore.pool.add(data);
        lock.unlock();
    }














    /**
     * Array for storing verts
     */
    private float[] vertArr = new float[ARRAY_SIZE * 3];

    /**
     * Array for storing normals
     */
    private float[] normalArr = new float[ARRAY_SIZE * 3];

    /**
     * Array for storing face data
     */
    private int[] faceArr = new int[ARRAY_SIZE];

    /**
     * The uv array
     */
    private float[] uvArr = new float[ARRAY_SIZE * 2];

    /**
     * Array for storing custom values
     */
    private float[] custArr1 = new float[ARRAY_SIZE * 3];

    /**
     * Array for storing custom values
     */
    private int[] custArr2 = new int[ARRAY_SIZE * 3];


    /**
     * The number of elements in the vert array
     */
    private int vertCount = 0;

    /**
     * The number of elements in the normal array
     */
    private int normalCount = 0;

    /**
     * The number of elements in the face array
     */
    private int faceCount = 0;

    /**
     * The number of elements in the uv array
     */
    private int uvCount = 0;

    /**
     * The number of elements in the custom array 1
     */
    private int custCount1 = 0;

    /**
     * The number of elements in the custom array 2
     */
    private int custCount2 = 0;

    /**
     * Adds a vert to the store
     */
    public void addVert(float x, float y, float z){
        vertArr[vertCount + 0] = x;
        vertArr[vertCount + 1] = y;
        vertArr[vertCount + 2] = z;
        vertCount = vertCount + 3;
    }

    public void addNormal(float x, float y, float z){
        normalArr[normalCount + 0] = x;
        normalArr[normalCount + 1] = y;
        normalArr[normalCount + 2] = z;
        normalCount = normalCount + 3;
    }

    public void addFace(int x, int y, int z){
        faceArr[faceCount + 0] = x;
        faceArr[faceCount + 1] = y;
        faceArr[faceCount + 2] = z;
        faceCount = faceCount + 3;
    }

    public void addUV(float x, float y){
        uvArr[uvCount + 0] = x;
        uvArr[uvCount + 1] = y;
        uvCount = uvCount + 2;
    }

    public void addCustom1(float x, float y, float z){
        custArr1[custCount1 + 0] = x;
        custArr1[custCount1 + 1] = y;
        custArr1[custCount1 + 2] = z;
        custCount1 = custCount1 + 3;
    }

    public void addCustom1(float x, float y){
        custArr1[custCount1 + 0] = x;
        custArr1[custCount1 + 1] = y;
        custCount1 = custCount1 + 2;
    }

    public void addCustom1(float x){
        custArr1[custCount1 + 0] = x;
        custCount1 = custCount1 + 1;
    }

    public void addCustom2(int x, int y, int z){
        custArr2[custCount2 + 0] = x;
        custArr2[custCount2 + 1] = y;
        custArr2[custCount2 + 2] = z;
        custCount2 = custCount2 + 3;
    }

    public void addCustom2(int x, int y){
        custArr2[custCount2 + 0] = x;
        custArr2[custCount2 + 1] = y;
        custCount2 = custCount2 + 2;
    }

    public void addCustom2(int x){
        custArr2[custCount2 + 0] = x;
        custCount2 = custCount2 + 1;
    }

    public float[] getVertArr() {
        return vertArr;
    }

    public float[] getNormalArr() {
        return normalArr;
    }

    public int[] getFaceArr() {
        return faceArr;
    }

    public float[] getUvArr() {
        return uvArr;
    }

    public float[] getCustArr1() {
        return custArr1;
    }

    public int getVertCount() {
        return vertCount;
    }

    public int getNormalCount() {
        return normalCount;
    }

    public int getFaceCount() {
        return faceCount;
    }

    public int getUvCount() {
        return uvCount;
    }

    public int getCustCount1() {
        return custCount1;
    }


    public int[] getCustArr2() {
        return custArr2;
    }

    public int getCustCount2() {
        return custCount2;
    }

    /**
     * Gets the vert at a given index
     * @param index The index
     * @return The vert
     */
    public float getVert(int index){
        return vertArr[index];
    }

    public int getFace(int index){
        return faceArr[index];
    }

    /**
     * Clears the data in the store
     */
    public void clear(){
        vertCount = 0;
        normalCount = 0;
        faceCount = 0;
        uvCount = 0;
        custCount1 = 0;
        custCount2 = 0;
    }


    
}
