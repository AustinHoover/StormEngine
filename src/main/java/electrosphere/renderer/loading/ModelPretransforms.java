package electrosphere.renderer.loading;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Quaterniond;
import org.joml.Vector3d;

/**
 * Holds metadata for all models to pretransform their meshes while they're
 * being loaded from disk
 * TODO: Validate all data read from disk (ie all rotations in MeshMetadatas have at least 4 doubles in them)
 * TODO: Transform Animations
 */
public class ModelPretransforms {

    /**
     * List of models as read from disk, not used after being init'd
     */
    private List<ModelMetadata> models;

    /**
     * Map relating path->model metadata
     */
    private Map<String,ModelMetadata> modelDataMap;

    /**
     * Initializes the model pretransform storage
     */
    public void init(){
        this.modelDataMap = new HashMap<String,ModelMetadata>();
        for(ModelMetadata metadata : models){
            modelDataMap.put(metadata.getPath(), metadata);
            metadata.init();
        }
    }

    /**
     * Fetches the model metadata for a given path
     * @param path The path to search for
     * @return The model metadata if found, or null if not found
     */
    public ModelMetadata getModel(String path){
        return modelDataMap.get(path);
    }

    /**
     * Stores metadata about a single model
     */
    public class ModelMetadata {
        /**
         * The path of the model
         */
        private String path;

        /**
         * List of meshes as read from disk, not used after being init'd
         */
        private List<MeshMetadata> meshes;

        /**
         * Map relating path->mesh metadata
         */
        private Map<String,MeshMetadata> meshDataMap;

        /**
         * Optional global transform
         */
        private GlobalTransform globalTransform;

        /**
         * Initializes the ModelMetadata object
         */
        public void init(){
            this.meshDataMap = new HashMap<String,MeshMetadata>();
            for(MeshMetadata metadata : meshes){
                meshDataMap.put(metadata.getMeshName(), metadata);
            }
        }

        /**
         * Fetches the mesh metadata for a given mesh name
         * @param meshName The name to search for
         * @return The mesh metadata if found, or null if not found
         */
        public MeshMetadata getMesh(String meshName){
            return meshDataMap.get(meshName);
        }

        /**
         * Gets the path of this model
         * @return The path of the model
         */
        public String getPath(){
            return path;
        }

        /**
         * Gets the global transform metadata
         * @return The global transform metadata
         */
        public GlobalTransform getGlobalTransform(){
            return globalTransform;
        }
    }

    /**
     * Holds metadata for pretransforming a mesh while it is being loaded from disk
     */
    public class MeshMetadata {
        String meshName;
        List<Double> rotation;
        List<Double> offset;
        List<Double> scale;

        /**
         * Gets the name of the mesh
         * @return The name of the mesh
         */
        public String getMeshName(){
            return meshName;
        }

        /**
         * gets the rotation of the transform as a JOML Quaterniond
         * !!WARNING!! unsafe: If there aren't at least 4 doubles in the array it out of bounds
         * @return The rotation of the transform
         */
        public Quaterniond getRotation(){
            return new Quaterniond(rotation.get(0),rotation.get(1),rotation.get(2),rotation.get(3));
        }

        /**
         * gets the offset of the transform as a JOML vector3d
         * !!WARNING!! unsafe: If there aren't at least 3 doubles in the array it out of bounds
         * @return The offset of the transform
         */
        public Vector3d getOffset(){
            return new Vector3d(offset.get(0),offset.get(1),offset.get(2));
        }

        /**
         * gets the scale of the transform as a JOML vector3d
         * !!WARNING!! unsafe: If there aren't at least 3 doubles in the array it out of bounds
         * @return The scale of the transform
         */
        public Vector3d getScale(){
            return new Vector3d(scale.get(0),scale.get(1),scale.get(2));
        }
    }


    /**
     * Holds metadata for pretransforming all meshes and potentially all animations
     */
    public class GlobalTransform {
        List<Double> rotation;
        List<Double> offset;
        List<Double> scale;
        boolean applyToAnimations;

        /**
         * gets the rotation of the transform as a JOML Quaterniond
         * !!WARNING!! unsafe: If there aren't at least 4 doubles in the array it out of bounds
         * @return The rotation of the transform
         */
        public Quaterniond getRotation(){
            return new Quaterniond(rotation.get(0),rotation.get(1),rotation.get(2),rotation.get(3));
        }

        /**
         * gets the offset of the transform as a JOML vector3d
         * !!WARNING!! unsafe: If there aren't at least 3 doubles in the array it out of bounds
         * @return The offset of the transform
         */
        public Vector3d getOffset(){
            return new Vector3d(offset.get(0),offset.get(1),offset.get(2));
        }

        /**
         * gets the scale of the transform as a JOML vector3d
         * !!WARNING!! unsafe: If there aren't at least 3 doubles in the array it out of bounds
         * @return The scale of the transform
         */
        public Vector3d getScale(){
            return new Vector3d(scale.get(0),scale.get(1),scale.get(2));
        }

        /**
         * Gets whether the global transform should be applied to animations
         * @return True if should apply to animations, false otherwise
         */
        public boolean getApplyToAnimations(){
            return applyToAnimations;
        }
    }
}
