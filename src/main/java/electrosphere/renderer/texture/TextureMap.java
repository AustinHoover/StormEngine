package electrosphere.renderer.texture;

import java.awt.IllegalComponentStateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import electrosphere.logger.LoggerInterface;
import electrosphere.util.FileUtils;

/**
 * A map of textures
 */
public class TextureMap {
    /**
     * The map of modelPath -> list of texture map entries
     */
    private Map<String,ModelTextureData> textureMap = new HashMap<String,ModelTextureData>();

    /**
     * Reads a texture map from a given path
     * @param rawMapPath The path
     * @return The texture map
     */
    public static TextureMap construct(String rawMapPath){
        TextureMapRaw rawData = FileUtils.loadObjectFromAssetPath(rawMapPath, TextureMapRaw.class);
        if(rawData == null){
            return null;
        }
        return TextureMap.construct(rawData);
    }

    /**
     * Constructs a texture map from raw data
     * @param rawData The raw data
     * @return The texture map
     */
    private static TextureMap construct(TextureMapRaw rawData){
        TextureMap rVal = new TextureMap();
        Map<String,List<MeshTextureData>> rawMap = rawData.getTextureMap();
        Set<String> modelPaths = rawMap.keySet();
        for(String modelPath : modelPaths){
            ModelTextureData modelTextureData = new ModelTextureData(modelPath, rawMap.get(modelPath));
            rVal.textureMap.put(modelPath,modelTextureData);
        }
        return rVal;
    }
    
    /**
     * Gets the texture map entry for a mesh on a given model
     * @param modelPath The model's path
     * @param meshName The mesh's name
     * @return The texture map's entry if it exists, null otherwise
     */
    public MeshTextureData getMeshTextures(String modelPath, String meshName){
        ModelTextureData modelTextureData = textureMap.get(modelPath);
        if(modelTextureData == null){
            LoggerInterface.loggerRenderer.WARNING("Trying to get texture for model that is not in texture map!");
            return null;
        }
        return modelTextureData.getMeshTextureData(meshName);
    }

    /**
     * Gets the texture map entry for a mesh on a given model
     * @param modelPath The model's path
     * @return The texture map's entry if it exists, null otherwise
     */
    public MeshTextureData getDefaultMeshTextures(String modelPath){
        ModelTextureData modelTextureData = textureMap.get(modelPath);
        if(modelTextureData == null){
            LoggerInterface.loggerRenderer.WARNING("Trying to get texture for model that is not in texture map!");
            return null;
        }
        return modelTextureData.getDefaultMeshTextures();
    }

    

    /**
     * Checks if the texture map contains the model
     * @param modelPath The model's path
     * @return true if the texture map contains the model, false otherwise
     */
    public boolean containsModel(String modelPath){
        return textureMap.containsKey(modelPath);
    }


    /**
     * Texture data for a given model
     */
    static class ModelTextureData {

        /**
         * The map of mesh name -> texture data
         */
        private Map<String,MeshTextureData> meshData = new HashMap<String,MeshTextureData>();

        /**
         * The default data to apply
         */
        private MeshTextureData defaultMeshData = null;

        /**
         * Constructs an object to track texture data for a whole model
         * @param rawMeshData The raw mesh texture data
         */
        public ModelTextureData(String modelPath, List<MeshTextureData> rawMeshData){
            for(MeshTextureData rawMesh: rawMeshData){
                meshData.put(rawMesh.getMeshName(),rawMesh);
                if(rawMesh.isDefault && this.defaultMeshData != null){
                    LoggerInterface.loggerEngine.ERROR(new IllegalComponentStateException("Two default meshes are defined in model " + modelPath));
                } else if(rawMesh.isDefault){
                    this.defaultMeshData = rawMesh;
                }
            }
        }

        /**
         * Gets the texture data for a given mesh
         * @param meshName The name of the mesh
         * @return The texture data
         */
        public MeshTextureData getMeshTextureData(String meshName){
            return meshData.get(meshName);
        }

        /**
         * Gets the default mesh texture data
         * @return The default mesh texture data
         */
        public MeshTextureData getDefaultMeshTextures(){
            return this.defaultMeshData;
        }

    }

    /**
     * Data for a mesh in a given model
     */
    public static class MeshTextureData {

        /**
         * The name of the mesh
         */
        private String meshName;

        /**
         * The specular texture's path
         */
        private String specular;

        /**
         * The diffuse texture's path
         */
        private String diffuse;

        /**
         * If this is true, this entry will be used for all meshes that don't have a defined entry
         */
        private boolean isDefault;

        /**
         * Gets the name of the mesh
         * @return The name of the mesh
         */
        public String getMeshName(){
            return meshName;
        }

        /**
         * Gets the specular texture's path
         * @return The specular texture's path
         */
        public String getSpecular(){
            return specular;
        }

        /**
         * Gets the diffuse texture's path
         * @return The diffuse texture's path
         */
        public String getDiffuse(){
            return diffuse;
        }

        /**
         * Checks if this is the default entry or not
         * @return true if is default, false otherwise
         */
        public boolean isDefault(){
            return isDefault;
        }


    }

    /**
     * The raw texture map data on disk
     */
    static class TextureMapRaw {

        /**
         * Raw format on disk
         */
        private Map<String,List<MeshTextureData>> textureMap;

        /**
         * Gets the raw texture map data
         * @return The raw texture map data
         */
        public Map<String,List<MeshTextureData>> getTextureMap(){
            return textureMap;
        }
        
    }
}
