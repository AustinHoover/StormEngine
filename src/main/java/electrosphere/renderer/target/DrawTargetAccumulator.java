package electrosphere.renderer.target;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4d;
import org.joml.Vector3d;

import electrosphere.entity.Entity;

/**
 * Accumulates non-spatially identical draw calls to batch to gpu
 */
public class DrawTargetAccumulator {
    
    /**
     * Structure that maps model path -> accumulator data for that model
     */
    private Map<String,ModelAccumulatorData> modelPathAccumulatorMap = new HashMap<String,ModelAccumulatorData>();

    /**
     * Groups terrain entities
     */
    private List<Entity> terrainEntities = new LinkedList<Entity>();

    /**
     * Groups block entities
     */
    private List<Entity> blockEntities = new LinkedList<Entity>();


    /**
     * Adds a call to draw a given model
     * @param modelPath The path to the model
     * @param position The position to draw the model at
     * @param transform The transform to apply to the model
     */
    public void addCall(String modelPath, Vector3d position, Matrix4d transform){
        if(modelPathAccumulatorMap.containsKey(modelPath)){
            ModelAccumulatorData data = modelPathAccumulatorMap.get(modelPath);
            data.addCall(position, transform);
        } else {
            ModelAccumulatorData data = new ModelAccumulatorData(modelPath);
            data.addCall(position, transform);
            modelPathAccumulatorMap.put(modelPath,data);
        }
    }

    /**
     * Adds a terrain entity
     * @param entity The entity
     */
    public void addTerrainCall(Entity entity){
        this.terrainEntities.add(entity);
    }

    /**
     * Adds a block entity
     * @param entity The entity
     */
    public void addBlockCall(Entity entity){
        this.blockEntities.add(entity);
    }

    /**
     * Gets the calls to make
     * @return The calls to make
     */
    public Collection<ModelAccumulatorData> getCalls(){
        return this.modelPathAccumulatorMap.values();
    }

    /**
     * Clears calls for all models
     */
    public void clearCalls(){
        for(ModelAccumulatorData data : modelPathAccumulatorMap.values()){
            data.count = 0;
        }
        this.terrainEntities.clear();
        this.blockEntities.clear();
    }

    /**
     * Gets the list of terrain entities
     * @return The list of terrain entities
     */
    public List<Entity> getTerrainEntities(){
        return this.terrainEntities;
    }

    /**
     * Gets the list of block entities
     * @return The list of block entities
     */
    public List<Entity> getBlockEntities(){
        return this.blockEntities;
    }

    /**
     * Accumulates data about draw calls to be made for a specific model
     */
    public static class ModelAccumulatorData {

        /**
         * The list of transform matricies to draw
         */
        private List<Matrix4d> transforms = new ArrayList<Matrix4d>();

        /**
         * The list of positions to draw
         */
        private List<Vector3d> positions = new ArrayList<Vector3d>();

        /**
         * The path to the model to draw
         */
        private String modelPath;

        /**
         * The count of this model to draw
         */
        private int count = 0;

        /**
         * Constructor
         * @param path The model path
         */
        public ModelAccumulatorData(String path){
            this.modelPath = path;
        }

        /**
         * Adds a call
         * @param position The position of the call
         * @param transform The transform of the call
         */
        public void addCall(Vector3d position, Matrix4d transform){
            if(transforms.size() > count){
                Matrix4d transformObj = transforms.get(this.count);
                Vector3d positionObj = positions.get(this.count);
                transformObj.set(transform);
                positionObj.set(position);
            } else {
                transforms.add(new Matrix4d(transform));
                positions.add(new Vector3d(position));
            }
            this.count++;
        }

        /**
         * Gets the model path to draw
         * @return The model path to draw
         */
        public String getModelPath(){
            return modelPath;
        }

        /**
         * Gets the count to draw
         * @return The count to draw
         */
        public int getCount(){
            return count;
        }

        /**
         * Gets the list of transforms
         * @return The list of transforms
         */
        public List<Matrix4d> getTransforms(){
            return transforms;
        }

        /**
         * Gets the list of positions
         * @return The list of positions
         */
        public List<Vector3d> getPositions(){
            return positions;
        }

    }

}
