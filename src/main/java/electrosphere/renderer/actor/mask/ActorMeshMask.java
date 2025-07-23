package electrosphere.renderer.actor.mask;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import electrosphere.engine.Globals;
import electrosphere.renderer.model.Mesh;
import electrosphere.renderer.model.Model;

/**
 * A per-actor object that contains blockers for individual parts of the base meshes of the actor.
 * IE, it will contain the information that says "don't draw the base model's feet, instead draw a pair of boots"
 */
public class ActorMeshMask {



    /**
     * The map of base mesh name -> blocked status
     * <p>
     * Ie "feet" -> true would mean that the base model feet should not be drawn
     */
    private Map<String,Boolean> meshBlockerList = new HashMap<String,Boolean>();

    /**
     * the list of meshes to draw that are stored in this object
     * <p>
     * ie this would contain a mesh for boots
     */
    private Map<String,Mesh> toDrawMesh = new HashMap<String,Mesh>();

    /**
     * lock for queueing blockers
     */
    private ReentrantLock lock = new ReentrantLock();

    /**
     * The set of queued meshes
     */
    private List<MeshDrawQueueItem> queuedMeshes = new LinkedList<MeshDrawQueueItem>();

    /**
     * The set of queued blockers
     */
    private List<MeshDrawQueueItem> queuedBlockers = new LinkedList<MeshDrawQueueItem>();
    


    /**
     * Quques a mesh
     * @param modelName The model's name
     * @param meshName The mesh's name
     */
    public void queueMesh(String modelName, String meshName){
        lock.lock();
        queuedMeshes.add(new MeshDrawQueueItem(modelName, meshName));
        lock.unlock();
    }

    /**
     * Processes all items in the mesh mask queue
     */
    public void processMeshMaskQueue(){
        Model model;
        Mesh mesh;
        if(lock.tryLock()){
            if(queuedMeshes.size() > 0 || queuedBlockers.size() > 0){
                //process queued meshes
                List<MeshDrawQueueItem> toRemove = new LinkedList<MeshDrawQueueItem>();
                for(MeshDrawQueueItem item : queuedMeshes){
                    if((model = Globals.assetManager.fetchModel(item.getModelName())) != null){
                        if((mesh = model.getMesh(item.getMeshName())) != null){
                            toDrawMesh.put(mesh.getMeshName(),mesh);
                            toRemove.add(item);
                        }
                    }
                }
                //remove successfully processed
                for(MeshDrawQueueItem item : toRemove){
                    queuedMeshes.remove(item);
                }
                toRemove.clear();
                //process queued blockers
                for(MeshDrawQueueItem item : queuedBlockers){
                    if((model = Globals.assetManager.fetchModel(item.getModelName())) != null){
                        meshBlockerList.put(item.getMeshName(),true);
                        toRemove.add(item);
                    }
                }
                //remove successfully processed
                for(MeshDrawQueueItem item : toRemove){
                    queuedBlockers.remove(item);
                }
            }
            lock.unlock();
        }
    }

    /**
     * Gets the list of meshes to draw
     * @return The list of meshes to draw
     */
    public List<Mesh> getToDrawMeshes(){
        return toDrawMesh.values().stream().collect(Collectors.toList());
    }

    /**
     * Ejects a mesh to draw
     * @param name The mesh's name
     */
    public void ejectMeshToDraw(String name){
        toDrawMesh.remove(name);
    }

    /**
     * Checks if the mesh mask is empty
     * @return true if it is empty, false otherwise
     */
    public boolean isEmpty(){
        return toDrawMesh.size() > 0;
    }

    /**
     * Removes a mesh from the list of extra (non-base model) meshes to draw
     * @param additionalMeshName the name of the mesh to remove
     */
    public void removeAdditionalMesh(String additionalMeshName){
        toDrawMesh.remove(additionalMeshName);
    }


    //
    //blocking interactions
    //

    /**
     * Checks if this mesh mask contains a mesh by a given name which it should draw
     * @param name The name of the mesh
     * @return true if it should draw, false otherwise
     */
    public boolean containsMeshToDraw(String name){
        return toDrawMesh.containsKey(name);
    }

    /**
     * Blocks a mesh from rendering
     * @param modelName The name of the model
     * @param meshName The name of the mesh
     */
    public void blockMesh(String modelName, String meshName){
        lock.lock();
        queuedBlockers.add(new MeshDrawQueueItem(modelName, meshName));
        lock.unlock();
    }

    /**
     * Checks if a mesh is blocked from rendering
     * @param name The name of the mesh
     * @return true if it is blocked, false otherwise
     */
    public boolean isBlockedMesh(String name){
        return meshBlockerList.containsKey(name);
    }

    /**
     * Unblocks a mesh from rendering
     * @param name The name of the mesh
     */
    public void unblockMesh(String name){
        meshBlockerList.remove(name);
    }

    /**
     * Gets the list of all meshes that are blocked
     * @return The list of all meshes that are blocked
     */
    public List<String> getBlockedMeshes(){
        return meshBlockerList.keySet().stream().collect(Collectors.toList());
    }



    /**
     * A queued mesh blocker.
     * When a creature equips an item that would block a mesh, the mesh may not have already been loaded into memory.
     * This represents an item in a queue that contains a mesh that should be eventually used to block a mesh on the parent actor.
     */
    private class MeshDrawQueueItem {

        /**
         * The name of the model
         */
        private String modelName;

        /**
         * The name of the mesh
         */
        private String meshName;

        /**
         * Constructor
         * @param modelName Model name
         * @param meshName Mesh name
         */
        protected MeshDrawQueueItem(String modelName, String meshName){
            this.modelName = modelName;
            this.meshName = meshName;
        }


        /**
         * Gets the name of the model
         * @return The name of the model
         */
        public String getModelName(){
            return modelName;
        }

        /**
         * Gets the name of the mesh
         * @return The name of the mesh
         */
        public String getMeshName(){
            return meshName;
        }

    }
    

}
