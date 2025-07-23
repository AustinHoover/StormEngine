package electrosphere.util;

import electrosphere.engine.Main;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.texture.TextureMap;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.lwjgl.assimp.AIMatrix4x4;

/**
 * Generic utilities
 */
public class Utilities {

    static {
        gson = new Gson();
    }
    
    static Gson gson;
    
    public static Matrix4f convertAIMatrix(AIMatrix4x4 mat){
        Matrix4f rVal = new Matrix4f();
        //Old, wrong approach:
//        mat.set(
//                mat.a1(),
//                mat.b1(),
//                mat.c1(),
//                mat.d1(),
//                mat.a2(),
//                mat.b2(),
//                mat.c2(),
//                mat.d2(),
//                mat.a3(),
//                mat.b3(),
//                mat.c3(),
//                mat.d3(),
//                mat.a4(),
//                mat.b4(),
//                mat.c4(),
//                mat.d4()
//                );
        //as demo'd in https://github.com/lwjglgamedev/lwjglbook/blob/master/chapter27/c27-p2/src/main/java/org/lwjglb/engine/loaders/assimp/AnimMeshesLoader.java
        rVal.m00(mat.a1());
        rVal.m10(mat.a2());
        rVal.m20(mat.a3());
        rVal.m30(mat.a4());
        rVal.m01(mat.b1());
        rVal.m11(mat.b2());
        rVal.m21(mat.b3());
        rVal.m31(mat.b4());
        rVal.m02(mat.c1());
        rVal.m12(mat.c2());
        rVal.m22(mat.c3());
        rVal.m32(mat.c4());
        rVal.m03(mat.d1());
        rVal.m13(mat.d2());
        rVal.m23(mat.d3());
        rVal.m33(mat.d4());
        return rVal;
    }

    public static Matrix4d convertAIMatrixd(AIMatrix4x4 mat){
        Matrix4d rVal = new Matrix4d();
        //Old, wrong approach:
//        mat.set(
//                mat.a1(),
//                mat.b1(),
//                mat.c1(),
//                mat.d1(),
//                mat.a2(),
//                mat.b2(),
//                mat.c2(),
//                mat.d2(),
//                mat.a3(),
//                mat.b3(),
//                mat.c3(),
//                mat.d3(),
//                mat.a4(),
//                mat.b4(),
//                mat.c4(),
//                mat.d4()
//                );
        //as demo'd in https://github.com/lwjglgamedev/lwjglbook/blob/master/chapter27/c27-p2/src/main/java/org/lwjglb/engine/loaders/assimp/AnimMeshesLoader.java
        rVal.m00(mat.a1());
        rVal.m10(mat.a2());
        rVal.m20(mat.a3());
        rVal.m30(mat.a4());
        rVal.m01(mat.b1());
        rVal.m11(mat.b2());
        rVal.m21(mat.b3());
        rVal.m31(mat.b4());
        rVal.m02(mat.c1());
        rVal.m12(mat.c2());
        rVal.m22(mat.c3());
        rVal.m32(mat.c4());
        rVal.m03(mat.d1());
        rVal.m13(mat.d2());
        rVal.m23(mat.d3());
        rVal.m33(mat.d4());
        return rVal;
    }
    
    
    
    @Deprecated
    public static void saveTestTextureMapToLocation(String s){
        TextureMap t = new TextureMap();
        // t.add_model("model1");
        // t.add_mesh_to_model("model1", "mesh1");
        // t.add_mesh_to_model("model1", "mesh2");
        // t.add_mesh_to_model("model1", "mesh3");
        // t.add_model("model2");
        // t.add_mesh_to_model("model2", "mesh1");
        // t.add_mesh_to_model("model2", "mesh2");
        // t.add_model("model3");
        // t.add_mesh_to_model("model3", "mesh1");
        // t.add_mesh_to_model("model3", "mesh2");
        // t.add_mesh_to_model("model3", "mesh3");
        // t.add_mesh_to_model("model3", "mesh4");
        Gson gson = new Gson();
        try {
            Files.write(new File(s).toPath(), gson.toJson(t).getBytes());
        } catch (IOException ex) {
            ex.printStackTrace(); // just for testing :thinking:
        }
    }
    
    public static void saveObjectToBakedJsonFile(String fileName, Object object){
        URL resourceUrl = Main.class.getResource(fileName);
        File file = new File("");
        try {
            file = new File(resourceUrl.toURI());
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
        Gson gson = new Gson();
        try {
            Files.write(file.toPath(), gson.toJson(object).getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
            LoggerInterface.loggerFileIO.ERROR(fileName, ex);
        }
    }

    /**
     * Serializes an object to json
     * @param object The object
     * @return The corresponding json
     */
    public static String stringify(Object object){
        return gson.toJson(object);
    }

    /**
     * Deserializes an object from json
     * @param <T> The class of the object
     * @param object The raw json string
     * @param className The class to deserialize to
     * @return The object
     */
    public static <T>T deserialize(String object, Class<T> className){
        return gson.fromJson(object, className);
    }

}
