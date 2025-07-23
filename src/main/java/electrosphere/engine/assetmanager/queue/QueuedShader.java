package electrosphere.engine.assetmanager.queue;

/**
 * A shader queued to be created
 */
public class QueuedShader {
    
    /**
     * The vertex shader's path
     */
    private String vertexShaderPath;

    /**
     * The fragment shader's path
     */
    private String fragmentShaderPath;

    /**
     * A queued shader
     * @param vertexShaderPath The vertex shader path
     * @param fragmentShaderPath The fragment shader path
     */
    public QueuedShader(String vertexShaderPath, String fragmentShaderPath) {
        this.vertexShaderPath = vertexShaderPath;
        this.fragmentShaderPath = fragmentShaderPath;
    }

    /**
     * Gets the vertex shader's path
     * @return The vertex shader's path
     */
    public String getVertexShaderPath() {
        return vertexShaderPath;
    }

    /**
     * Gets the fragment shader's path
     * @return The fragment shader's path
     */
    public String getFragmentShaderPath() {
        return fragmentShaderPath;
    }

    

}
