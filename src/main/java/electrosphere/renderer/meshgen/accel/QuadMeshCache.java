package electrosphere.renderer.meshgen.accel;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Cache for pooling quad meshes
 */
public class QuadMeshCache {

    /**
     * Quads in a cache instance
     */
    private static final int QUADS_PER_CACHE = 4096;



    /**
     * Structure to store not-in-use objects
     */
    static List<QuadMeshCache> cachePool = new LinkedList<QuadMeshCache>();

    /**
     * Lock for thread-safeing operations
     */
    static ReentrantLock lock = new ReentrantLock();


    /**
     * Gets a QuadMeshCache from the pool. Allocates if no free one is available.
     * @param type The type of the message
     * @return A QuadMeshCache
     */
    public static QuadMeshCache getF(){
        QuadMeshCache rVal = null;
        lock.lock();
        if(cachePool.size() > 0){
            rVal = QuadMeshCache.cachePool.remove(0);
        } else {
            rVal = new QuadMeshCache();
        }
        rVal.reset();
        lock.unlock();
        return rVal;
    }

    /**
     * Releases a QuadMeshCache back into the pool
     * @param data The object to release
     */
    public static void release(QuadMeshCache data){
        lock.lock();
        QuadMeshCache.cachePool.add(data);
        lock.unlock();
    }





    /**
     * Set of quad meshes
     */
    private QuadMesh[] quads = new QuadMesh[QuadMeshCache.QUADS_PER_CACHE];

    /**
     * The number of active quads
     */
    private int activeCount = 0;

    /**
     * Constructor for cache
     */
    public QuadMeshCache(){
        for(int i = 0; i < QUADS_PER_CACHE; i++){
            quads[i] = new QuadMesh();
        }
    }

    /**
     * Resets the cache
     */
    private void reset(){
        for(int i = 0; i < QUADS_PER_CACHE; i++){
            this.quads[i].reset();
        }
        this.activeCount = 0;
    }

    /**
     * Sorts the quad array
     */
    public void sort(){
        Arrays.sort(quads);

        //figure out how many quads are actually active
        for(int i = 0; i < QUADS_PER_CACHE; i++){
            if(!quads[i].active){
                this.activeCount = i;
                break;
            }
        }
    }

    /**
     * Gets a new quad mesh
     * @return The quad mesh
     */
    public QuadMesh getNew(){
        QuadMesh rVal = quads[activeCount];
        rVal.active = true;
        activeCount++;
        return rVal;
    }

    /**
     * Gets a quad mesh
     * @return The quad mesh
     */
    public QuadMesh getActive(int i){
        QuadMesh rVal = quads[i];
        return rVal;
    }

    /**
     * Destroys a quad mesh
     * @param mesh The mesh
     */
    public void destroy(QuadMesh mesh){
        mesh.active = false;
        this.activeCount--;
    }

    /**
     * Gets the active count
     * @return The active count
     */
    public int getActiveCount(){
        return activeCount;
    }

    /**
     * Intermediary structure used during rasterization
     */
    public static class QuadMesh implements Comparable<QuadMesh> {
        public int x;
        public int y;
        public int z;
        public int w;
        public int h;
        public int type;
        public boolean active = false;
        public void set(int x, int y, int z, int w, int h, int type){
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
            this.h = h;
            this.type = type;
            this.active = true;
        }

        public void reset(){
            this.active = false;
        }

        @Override
        public int compareTo(QuadMesh other) {
            if(this.active && !other.active){
                return -1;
            }
            if(other.active && !this.active){
                return 1;
            }
            if(this.y != other.y){
                return this.y - other.y;
            }
            if(this.x != other.x){
                return this.x - other.x;
            }
            if(this.w != other.w){
                return other.w - this.w;
            }
            return other.h - this.h;
        }
    }

}
