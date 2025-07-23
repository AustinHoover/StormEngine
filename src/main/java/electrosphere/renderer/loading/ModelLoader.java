package electrosphere.renderer.loading;

import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.model.Material;
import electrosphere.renderer.model.Mesh;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.texture.TextureMap;
import electrosphere.renderer.texture.TextureMap.MeshTextureData;
import electrosphere.util.FileUtils;

import java.io.File;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.Assimp;

/**
 * Main model loading class
 */
public class ModelLoader {
    
    /**
     * Loads a model via assimp
     * @param path The path to the model
     * @return The model if it exists, null otherwise
     */
    public static AIScene loadAIScene(String path){
        AIScene rVal;
        File toRead = FileUtils.getAssetFile(path);
        rVal = Assimp.aiImportFile(toRead.getAbsolutePath(), 
            Assimp.aiProcess_GenSmoothNormals | 
            Assimp.aiProcess_JoinIdenticalVertices | 
            Assimp.aiProcess_Triangulate | 
            Assimp.aiProcess_FixInfacingNormals | 
            Assimp.aiProcess_LimitBoneWeights |
            Assimp.aiProcess_GlobalScale |
            Assimp.aiProcess_GenBoundingBoxes
        );
        if(rVal == null){
            LoggerInterface.loggerRenderer.ERROR(new IllegalStateException(Assimp.aiGetErrorString()));
        }
        return rVal;
    }

    /**
     * Creates a model object from an ai scene
     * @param scene The ai scene
     * @param localTextureMap The local texture map stored next to the model file
     * @param path The path to the model
     * @return The model object
     */
    public static Model createModelFromAiScene(AIScene scene, TextureMap localTextureMap, String path){
        Model rVal = null;
        if(scene != null){
            rVal = Model.createModelFromAiscene(path, scene);
            ModelLoader.attemptAddTexturesFromPathname(path, localTextureMap, rVal);
            LoggerInterface.loggerRenderer.DEBUG("Finished loading model " + path);
        }
        return rVal;
    }
    

    /**
     * Attempt to assign textures to meshes on this model based on texture map entries
     * @param path The path to the model
     * @param localTextureMap The local texture map
     * @param m The model
     */
    private static void attemptAddTexturesFromPathname(String path, TextureMap localTextureMap, Model m){
        
        LoggerInterface.loggerRenderer.DEBUG("Load textures for " + path);

        if(Globals.textureMapDefault.containsModel(path)){
            //
            //load from global map
            //
            MeshTextureData defaultMeshData = Globals.textureMapDefault.getDefaultMeshTextures(path);
            for(Mesh mesh : m.getMeshes()){
                MeshTextureData meshTextureData = Globals.textureMapDefault.getMeshTextures(path, mesh.getMeshName());
                if(meshTextureData != null){
                    ModelLoader.setMaterial(mesh,meshTextureData);
                } else if(defaultMeshData != null){
                    ModelLoader.setMaterial(mesh,defaultMeshData);
                } else {
                    LoggerInterface.loggerRenderer.WARNING("Model " + path + " does not have texture data defined for \"" + mesh.getMeshName() + "\"");
                }
            }
        } else if(localTextureMap != null && localTextureMap.containsModel(path)) {
            //
            //load from local folder for model
            //
            MeshTextureData defaultMeshData = localTextureMap.getDefaultMeshTextures(path);
            for(Mesh mesh : m.getMeshes()){
                MeshTextureData meshTextureData = localTextureMap.getMeshTextures(path, mesh.getMeshName());
                if(meshTextureData != null){
                    ModelLoader.setMaterial(mesh,meshTextureData);
                } else if(defaultMeshData != null){
                    ModelLoader.setMaterial(mesh,defaultMeshData);
                } else {
                    LoggerInterface.loggerRenderer.WARNING("Model " + path + " does not have texture data defined for \"" + mesh.getMeshName() + "\"");
                }
            }
        } else {
            // LoggerInterface.loggerRenderer.WARNING("Trying to get textures for model that doesn't have local or global texture map entries! " + path);
        }
    }

    /**
     * Sets the material for the mesh
     * @param mesh The mesh
     * @param meshTextureData The texture data for the mesh
     */
    private static void setMaterial(Mesh mesh, MeshTextureData meshTextureData){
        Material finalMat = new Material();

        //set diffuse
        String diffusePath = meshTextureData.getDiffuse();
        LoggerInterface.loggerRenderer.DEBUG(mesh.getMeshName() + "->" + diffusePath);
        if(diffusePath == null){
            diffusePath = AssetDataStrings.TEXTURE_DEFAULT;
        }

        //set specular
        String specularPath = meshTextureData.getSpecular();
        if(specularPath == null){
            specularPath = AssetDataStrings.TEXTURE_DEFAULT;
        }
        finalMat = Material.create(diffusePath, specularPath);
        //once we've either added default textures or actual textures,
        //set the current mesh's material to this new one
        mesh.setMaterial(finalMat);
    }

}
