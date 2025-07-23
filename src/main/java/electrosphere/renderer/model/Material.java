package electrosphere.renderer.model;

import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.queue.QueuedTexture;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.texture.Texture;
import electrosphere.util.FileUtils;

import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMaterialProperty;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.AITexture;
import org.lwjgl.assimp.Assimp;
import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

/**
 * A material
 */
public class Material {

    /**
     * Default shininess
     */
    public static final double DEFAULT_SHININESS = 1.0f;

    /**
     * The default reflectivity
     */
    public static final double DEFAULT_REFLECTIVITY = 0.0f;
    

    /**
     * The diffuse texture
     */
    private Texture diffuse;

    /**
     * The specular texture
     */
    private Texture specular;

    /**
     * used to continuously poll for an existing texture (that maybe is still being loaded in) to assign to this material
     */
    private String existingPath = null;

    /**
     * The shininess value
     */
    private double shininess = Material.DEFAULT_SHININESS;

    /**
     * The reflectivity value
     */
    private double reflectivity = Material.DEFAULT_REFLECTIVITY;

    /**
     * The albedo of the model
     */
    private Vector3f albedo = new Vector3f(1);
    
    /**
     * A material that contains textures
     */
    public Material(){
    }

    /**
     * Creates a material with a supplied diffuse path
     * @param diffuse The diffuse path
     * @return The material
     */
    public static Material create(String diffuse){
        if(diffuse == null){
            throw new Error("Diffuse undefined");
        }
        Material rVal = new Material();
        Globals.assetManager.queuedAsset(QueuedTexture.createFromPath(diffuse, (Texture tex) -> {
            if(tex == null){
                throw new Error("Failed to load " + diffuse);
            }
            rVal.diffuse = tex;
        }));
        return rVal;
    }

    /**
     * Creates a material with a supplied diffuse path
     * @param diffuse The diffuse path
     * @return The material
     */
    public static Material createExisting(String diffuse){
        if(diffuse == null){
            throw new Error("Diffuse undefined");
        }
        Material rVal = new Material();
        rVal.existingPath = diffuse;
        return rVal;
    }

    /**
     * Creates a material with a supplied diffuse and specular path
     * @param diffuse The diffuse path
     * @param specular The specular path
     * @return The material
     */
    public static Material create(String diffuse, String specular){
        if(diffuse == null){
            throw new Error("Diffuse undefined");
        }
        if(specular == null){
            throw new Error("Diffuse undefined");
        }
        Material rVal = new Material();
        Globals.assetManager.queuedAsset(QueuedTexture.createFromPath(diffuse, (Texture tex) -> {
            if(tex == null){
                throw new Error("Failed to load " + diffuse);
            }
            rVal.diffuse = tex;
        }));
        Globals.assetManager.queuedAsset(QueuedTexture.createFromPath(specular, (Texture tex) -> {
            if(tex == null){
                throw new Error("Failed to load " + specular);
            }
            rVal.specular = tex;
        }));
        return rVal;
    }

    /**
     * Creates a material with a supplied diffuse and specular path
     * @param diffuse The diffuse path
     * @param specular The specular path
     * @return The material
     */
    public static Material create(Texture diffuse, Texture specular){
        if(diffuse == null){
            throw new Error("Diffuse undefined");
        }
        if(specular == null){
            throw new Error("Diffuse undefined");
        }
        Material rVal = new Material();
        rVal.diffuse = diffuse;
        rVal.specular = specular;
        return rVal;
    }

    /**
     * Loads materials from the ai material object
     * @param path The path to the file we're loading materials from
     * @param input The input scene
     * @param input The input ai material
     * @return The resulting engine material
     */
    public static Material loadMaterialFromAIMaterial(String path, AIScene scene, AIMaterial input){
        Material rVal = new Material();

        AIString aiPathString = AIString.calloc();

        //read props
        try(MemoryStack stack = MemoryStack.stackPush()){
            //grab specific values
            rVal.reflectivity = Material.readFloatProp(stack, Assimp.AI_MATKEY_REFLECTIVITY, input, Material.DEFAULT_REFLECTIVITY);
            rVal.shininess = Material.readFloatProp(stack, Assimp.AI_MATKEY_SHININESS, input, Material.DEFAULT_SHININESS);
        }

        //read textures
        PointerBuffer texturePtrBuff = scene.mTextures();
        String[] texPaths = new String[scene.mNumTextures()];
        for(int i = 0; i < scene.mNumTextures(); i++){
            AITexture tex = AITexture.create(texturePtrBuff.get());
            texPaths[i] = tex.mFilename().dataString();
        }
        //discover diffuse
        boolean discovered = Material.tryDiscoverDiffuse(path,scene,input,texPaths,aiPathString,Assimp.aiTextureType_DIFFUSE,rVal);
        if(!discovered){
            discovered = Material.tryDiscoverDiffuse(path,scene,input,texPaths,aiPathString,Assimp.aiTextureType_BASE_COLOR,rVal);
        }
        if(!discovered){
            discovered = Material.tryDiscoverDiffuse(path,scene,input,texPaths,aiPathString,Assimp.aiTextureType_EMISSIVE,rVal);
        }
        if(!discovered){
            discovered = Material.tryDiscoverDiffuse(path,scene,input,texPaths,aiPathString,Assimp.aiTextureType_AMBIENT,rVal);
        }
        if(!discovered){
            discovered = Material.tryDiscoverDiffuse(path,scene,input,texPaths,aiPathString,Assimp.aiTextureType_EMISSION_COLOR,rVal);
        }
        
        if(!discovered){
            LoggerInterface.loggerRenderer.DEBUG("Failed to find diffuse path for mesh in " + path);
        }

        //free mem
        aiPathString.free();

        return rVal;
    }

    /**
     * Tries to discover a material at a given texture type
     * @param path The path to the model
     * @param scene The scene of the model
     * @param input The input material
     * @param texPaths The texture paths
     * @param aiPathString The ai string buffer
     * @param mat The material object
     * @return true if a diffuse was found, false otherwise
     */
    private static boolean tryDiscoverDiffuse(String path, AIScene scene, AIMaterial input, String[] texPaths, AIString aiPathString, int texType, Material mat){
        boolean foundDiffuse = false;
        int textureCount = Assimp.aiGetMaterialTextureCount(input, texType);
        if(textureCount > 0){
            //for the time being, only load the first diffuse
            int textureIndex = 0;
            int retCode = Assimp.aiGetMaterialTexture(input, texType, textureIndex, aiPathString, (IntBuffer)null, null, null, null, null, null);
            if(retCode != Assimp.aiReturn_SUCCESS){
                throw new Error("Failed to read diffuse! " + textureCount + " " + Assimp.aiGetErrorString());
            }
            String texturePath = aiPathString.dataString();
            if(texturePath == null || texturePath.length() <= 0){
                throw new Error("Texture path is empty " + texturePath);
            }
            if(texturePath.length() == 2 && texturePath.startsWith("*")){
                //older versions of Assimp require you to read the INDEX of the texture from the material, then look up that texture in the scene itself
                //format looks like "*<index>"   ie "*0"
                int indexInLoadedTexturePaths = Integer.parseInt(texturePath.substring(1));
                if(indexInLoadedTexturePaths >= texPaths.length){
                    throw new Error("Index discovered is outside the array's length " + indexInLoadedTexturePaths + " " + texPaths.length);
                }
                String resolved = Material.resolveTexturePath(path, texPaths[indexInLoadedTexturePaths]);
                if(resolved != null && resolved.length() > 0){
                    Globals.assetManager.queuedAsset(QueuedTexture.createFromPath(resolved, (Texture tex) -> {
                        if(tex == null){
                            throw new Error("Failed to load " + resolved);
                        }
                        mat.diffuse = tex;
                    }));
                    foundDiffuse = true;
                }
            } else {
                String resolved = Material.resolveTexturePath(path, texturePath);
                if(resolved != null && resolved.length() > 0){
                    Globals.assetManager.queuedAsset(QueuedTexture.createFromPath(resolved, (Texture tex) -> {
                        if(tex == null){
                            throw new Error("Failed to load " + resolved);
                        }
                        mat.diffuse = tex;
                    }));
                    foundDiffuse = true;
                }
            }
        }
        return foundDiffuse;
    }

    /**
     * Describes the properties of a material
     * @param material The material
     */
    public static void listMaterialProps(AIMaterial material){
        LoggerInterface.loggerRenderer.WARNING("Describing material");
        for(int i = 0; i < material.mNumProperties(); i++){
            AIMaterialProperty prop = AIMaterialProperty.create(material.mProperties().get(i));
            String key = prop.mKey().dataString();
            int propType = prop.mSemantic();
            if(propType == Assimp.aiTextureType_NONE){
                //non-texture prop
                LoggerInterface.loggerRenderer.WARNING("Prop \"" + key + "\" is not a texture");
            } else {
                LoggerInterface.loggerRenderer.WARNING("Prop \"" + key + "\" is a texture");
            }
        }
    }

    /**
     * Resolves the filepath of the texture
     * @param path The path of the ai scene itself
     * @param filename The name of the file
     * @return The full path to load
     */
    private static String resolveTexturePath(String path, String filename){
        File fileObj = FileUtils.getAssetFile(path);
        File parentDir = fileObj.getParentFile();
        File[] contents = parentDir.listFiles();
        File discovered = null;
        for(File child : contents){
            String name = child.getName();
            String nameNoExt = child.getName().replaceFirst("[.][^.]+$", "");
            if(name.equals(filename) || nameNoExt.equals(filename)){
                discovered = child;
            }
        }

        if(discovered == null){
            LoggerInterface.loggerRenderer.WARNING("Failed to find texture \"" + filename + "\" for model " + path);
            return null;
        } else {
            Path relative = new File("./assets").toPath().relativize(discovered.toPath());
            return relative.toString().replace("\\","/");
        }
    }

    /**
     * Gets the path for the diffuse of the material
     * @return The path for the diffuse texture
     */
    public Texture getDiffuse(){
        return diffuse;
    }

    /**
     * Gets the path for the specular of the material
     * @return The path for the specular texture
     */
    public Texture getSpecular(){
        return specular;
    }

    /**
     * Sets the diffuse texture
     * @param t The texture path
     */
    public void setDiffuse(Texture t){
        diffuse = t;
    }

    /**
     * Sets the specular texture path
     * @param t The specular texture path
     */
    public void setSpecular(Texture t){
        specular = t;
    }

    /**
     * Sets the albedo of the material
     * @param albedo The albedo
     */
    public void setAlbedo(Vector3f albedo){
        this.albedo.set(albedo);
    }

    /**
     * Gets the albedo of the material
     * @return The albedo
     */
    public Vector3f getAlbedo(){
        return this.albedo;
    }

    /**
     * Reads a float property from a material
     * @param stack the memory stack
     * @param key The key for the property
     * @param input The input material
     * @param defaultVal The default value
     * @return The float value
     */
    private static float readFloatProp(MemoryStack stack, String key, AIMaterial input, double defaultVal){
        int numFloats = 1;
        FloatBuffer buff = stack.callocFloat(numFloats);
        IntBuffer floatCountBuff = stack.ints(numFloats);
        ByteBuffer keyBuff = stack.ASCII(key);
        if(Assimp.aiReturn_SUCCESS == Assimp.aiGetMaterialFloatArray(input, keyBuff, Assimp.aiTextureType_NONE, 0, buff, floatCountBuff)){
            int numFloatsFound = floatCountBuff.get();
            if(numFloatsFound > 0){
                return buff.get();
            }
        }
        return (float)defaultVal;
    }
    
    /**
     * Applies the material
     */
    public void applyMaterial(OpenGLState openGLState){
        if(this.existingPath != null){
            Texture tex = Globals.assetManager.fetchTexture(existingPath);
            if(tex != null){
                this.diffuse = tex;
                this.existingPath = null;
            }
        }
        //Controls whether the texturePointer should be resolved by looking up the diffuse in asset manager or using the texture pointer already set in this material
        if(diffuse != null){
            diffuse.bind(openGLState,0);
            Globals.renderingEngine.checkError();
            openGLState.getActiveShader().setUniform(openGLState, "material.diffuse", 0);
            Globals.renderingEngine.checkError();
        }
        if(specular != null){
            specular.bind(openGLState,1);
            Globals.renderingEngine.checkError();
            openGLState.getActiveShader().setUniform(openGLState, "material.specular", 1);
            Globals.renderingEngine.checkError();
        }
        //send physical properties
        openGLState.getActiveShader().setUniform(openGLState, "material.shininess", (float)this.shininess);
        Globals.renderingEngine.checkError();
        openGLState.getActiveShader().setUniform(openGLState, "material.reflectivity", (float)this.reflectivity);
        Globals.renderingEngine.checkError();
        openGLState.getActiveShader().setUniform(openGLState, "material.albedo", this.albedo);
        Globals.renderingEngine.checkError();
    }

    /**
     * Frees the material
     */
    public void free(){
        if(this.diffuse != null){
            GL45.glDeleteTextures(this.diffuse.getTexturePointer());
        }
        if(this.specular != null){
            GL45.glDeleteTextures(this.specular.getTexturePointer());
        }
    }
}
