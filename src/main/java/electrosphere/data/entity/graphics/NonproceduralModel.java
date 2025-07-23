package electrosphere.data.entity.graphics;

import java.util.Map;

import org.joml.Vector3f;

import electrosphere.data.entity.creature.IdleData;

/**
 * A non-procedural model
 */
public class NonproceduralModel {

    /**
     * The path to the model
     */
    private String path;

    /**
     * The idle data for the model
     */
    private IdleData idleData;

    /**
     * Uniform values set ahead of time
     */
    private Map<String,Map<String,Object>> uniforms;

    /**
     * The map of mesh to color to apply to that mesh
     */
    private Map<String,Vector3f> meshColorMap;

    /**
     * The path to the lower-resolution model
     */
    private String lodPath;

    /**
     * Gets the path of the model
     * @return The path of the model
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path of the model
     * @param path The path of the model
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Gets the idle data of the model
     * @return The idle data of the model
     */
    public IdleData getIdleData() {
        return idleData;
    }

    /**
     * Sets the idle data of the model
     * @param idleData The idle data of the model
     */
    public void setIdleData(IdleData idleData) {
        this.idleData = idleData;
    }

    /**
     * Gets the uniforms set on the model
     * @return The map of uniform name -> uniform value, or null if no uniforms are set
     */
    public Map<String,Map<String,Object>> getUniforms(){
        return uniforms;
    }

    /**
     * Sets the uniform map for the model
     * @param uniforms The map of uniform name -> uniform value
     */
    public void setUniforms(Map<String,Map<String,Object>> uniforms){
        this.uniforms = uniforms;
    }

    /**
     * Gets the map of mesh to color to apply to that mesh
     * @return The map of mesh to color to apply to that mesh
     */
    public Map<String,Vector3f> getMeshColorMap() {
        return meshColorMap;
    }

    /**
     * Sets the map of mesh to color to apply to that mesh
     * @param meshColorMap The map of mesh to color to apply to that mesh
     */
    public void setMeshColorMap(Map<String,Vector3f> meshColorMap) {
        this.meshColorMap = meshColorMap;
    }

    /**
     * Gets the path to the lower resolution model
     * @return The path to the lower resolution model
     */
    public String getLODPath(){
        return lodPath;
    }

    /**
     * Sets the path to the lower resolution model
     * @param lodPath The path to the lower resolution model
     */
    public void setLODPath(String lodPath){
        this.lodPath = lodPath;
    }
    

}
