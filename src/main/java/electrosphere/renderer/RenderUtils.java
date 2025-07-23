package electrosphere.renderer;

import electrosphere.engine.Globals;

import java.util.LinkedList;
import java.util.List;

import electrosphere.renderer.actor.mask.ActorTextureMask;
import electrosphere.renderer.model.Mesh;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.texture.Texture;

/**
 * Utilities to assist with rendering
 */
public class RenderUtils {

    /**
     * Generates a volumetric texture mask
     * @param meshName The name of the mesh to mask
     * @return The texture mask to replace that mesh's texture with a volumetric texture
     */
    public static ActorTextureMask generateVolumetricTextureMask(String meshName){
        List<Texture> textureList = new LinkedList<Texture>();
        textureList.add(Globals.renderingEngine.getVolumeFrontfaceTexture());
        textureList.add(Globals.renderingEngine.getVolumeBackfaceTexture());
        List<String> uniformList = new LinkedList<String>();
        uniformList.add("volumeDepthFrontface");
        uniformList.add("volumeDepthBackface");
        return new ActorTextureMask(meshName,textureList,uniformList);
    }

    /**
     * Wraps a mesh in a model
     * @param mesh The mesh
     * @return The model that wraps the mesh
     */
    public static Model wrapMeshInModel(Mesh mesh){
        Model model = new Model();
        model.addMesh(mesh);
        return model;
    }

    
}
