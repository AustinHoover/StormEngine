package electrosphere.renderer.actor.mask;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Map of mesh -> uniforms to apply to that mesh
 */
public class ActorUniformMap {

    /**
     * Map of mesh -> uniforms to push to that mesh
     */
    private Map<String,List<UniformValue>> meshMap = new HashMap<String,List<UniformValue>>();

    /**
     * Sets the value of a uniform for a given mesh
     * @param meshName The name of the mesh
     * @param uniformName The name of the uniform
     * @param value The value of the uniform
     */
    public void setUniform(String meshName, String uniformName, Object value){
        List<UniformValue> uniforms = null;
        if(meshMap.containsKey(meshName)){
            uniforms = meshMap.get(meshName);
        } else {
            uniforms = new LinkedList<UniformValue>();
            meshMap.put(meshName,uniforms);
        }
        uniforms.add(new UniformValue(uniformName,value));
    }

    /**
     * Gets the list of uniforms to apply to a given mesh
     * @param meshName The name of the mesh
     * @return The list of uniforms to apply to the mesh, or null if no uniforms are defined
     */
    public List<UniformValue> getUniforms(String meshName){
        return meshMap.get(meshName);
    }

    /**
     * Gets the set of names of meshes that have uniforms set
     * @return The set of mesh names that have uniforms set
     */
    public Set<String> getMeshes(){
        return meshMap.keySet();
    }

    /**
     * Checks if the uniform map is empty
     * @return true if it is empty, false otherwise
     */
    public boolean isEmpty(){
        return this.meshMap.isEmpty();
    }


    /**
     * A uniform value
     */
    public static class UniformValue {

        /**
         * The name of the uniform
         */
        String uniformName;

        /**
         * The value of the uniform
         */
        Object value;

        /**
         * Constructor
         * @param uniformName Name of the uniform
         * @param value Value of the uniform
         */
        public UniformValue(String uniformName, Object value){
            this.uniformName = uniformName;
            this.value = value;
        }

        /**
         * Gets the name of the uniform
         * @return The name of the uniform
         */
        public String getUniformName() {
            return uniformName;
        }

        /**
         * Gets the value of the uniform
         * @return The value of the uniform
         */
        public Object getValue() {
            return value;
        }

    }

}
