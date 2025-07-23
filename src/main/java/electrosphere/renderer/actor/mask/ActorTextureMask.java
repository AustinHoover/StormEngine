package electrosphere.renderer.actor.mask;

import java.util.List;

import electrosphere.renderer.texture.Texture;

/**
 * A mask that overwrites the texture for a given mesh for a given actor
 */
public class ActorTextureMask {
    
    /**
     * The mesh this mask is overwriting
     */
    private String meshName;

    /**
     * The texture objects used for this mask
     */
    private List<Texture> textures;

    /**
     * The paths to textures to be used for this mask
     */
    private List<String> texturePaths;

    /**
     * The uniform names used for this mask
     */
    private List<String> uniformNames;

    /**
     * Constructor
     * @param meshName The name of the mesh to mask
     * @param textures The texture objects to use to mask (ie diffuse and normal)
     * @param uniformNames The uniform names used for each texture
     */
    public ActorTextureMask(String meshName, List<Texture> textures, List<String> uniformNames){
        this.meshName = meshName;
        this.textures = textures;
        this.uniformNames = uniformNames;
    }

    /**
     * Constructor (does not use non-standard uniform names)
     * @param meshName The name of the mesh to mask
     * @param texturePaths The texture paths to use to mask
     */
    public ActorTextureMask(String meshName, List<String> texturePaths){
        this.meshName = meshName;
        this.texturePaths = texturePaths;
    }

    /**
     * Gets the name of the mesh this is masking
     * @return The name of the mesh
     */
    public String getMeshName(){
        return meshName;
    }

    /**
     * Gets the list of texture objects used to mask
     * @return The list of texture objects
     */
    public List<Texture> getTextures(){
        return textures;
    }

    /**
     * Gets the list of uniform names
     * @return The list of uniform names
     */
    public List<String> getUniformNames(){
        return uniformNames;
    }

    /**
     * Gets the texture paths to use
     * @return The texture paths
     */
    public List<String> getTexturePaths(){
        return this.texturePaths;
    }

}
