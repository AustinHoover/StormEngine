package electrosphere.renderer.shader;

import java.nio.ByteBuffer;

import org.joml.Matrix4d;
import org.joml.Vector3d;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.engine.Globals;
import electrosphere.renderer.buffer.ShaderStorageBuffer;
import electrosphere.renderer.buffer.BufferEnums.BufferAccess;
import electrosphere.renderer.buffer.BufferEnums.BufferUsage;

/**
 * Manages the standard uniforms
 */
public class StandardUniformManager {

    /**
     * Bind point of the standard uniform buffer
     */
    public static final int STANDARD_UNIFORM_BUFFER_BIND_POINT = 4;

    /**
     * Size of the standard uniform ssbo
     */
    public static final int STANDARD_UNIFORM_SSBO_SIZE = 220;
    
    /**
     * The standard uniform ssbo
        Standard uniform struct:
        {
            mat4 view; //64 bytes
            mat4 projection; //64 bytes
            mat4 lightSpaceMatrix; //64 bytes
            vec4 viewPos; //16 bytes
            unsigned int frame; //4 bytes
            float time; //4 bytes
            float timeOfDay; //4 bytes
        }
        Totals to 220 bytes
     */
    private ShaderStorageBuffer standardUniformSSBO;

    /**
     * Private constructor
     */
    private StandardUniformManager(){}

    /**
     * Creates the standard uniform manager
     * @return The standard uniform manager
     */
    public static StandardUniformManager create(){
        StandardUniformManager rVal = new StandardUniformManager();
        rVal.standardUniformSSBO = new ShaderStorageBuffer(STANDARD_UNIFORM_SSBO_SIZE, BufferUsage.DYNAMIC, BufferAccess.DRAW);
        return rVal;
    }

    /**
     * Updates the standard uniform manager
     */
    public void update(){
        ByteBuffer buff = standardUniformSSBO.getBuffer();

        //put the view matrix
        Matrix4d viewMat = Globals.renderingEngine.getViewMatrix();
        buff.putFloat((float)viewMat.m00());
        buff.putFloat((float)viewMat.m01());
        buff.putFloat((float)viewMat.m02());
        buff.putFloat((float)viewMat.m03());

        buff.putFloat((float)viewMat.m10());
        buff.putFloat((float)viewMat.m11());
        buff.putFloat((float)viewMat.m12());
        buff.putFloat((float)viewMat.m13());

        buff.putFloat((float)viewMat.m20());
        buff.putFloat((float)viewMat.m21());
        buff.putFloat((float)viewMat.m22());
        buff.putFloat((float)viewMat.m23());

        buff.putFloat((float)viewMat.m30());
        buff.putFloat((float)viewMat.m31());
        buff.putFloat((float)viewMat.m32());
        buff.putFloat((float)viewMat.m33());

        //put the projection matrix
        Matrix4d projectionMat = Globals.renderingEngine.getProjectionMatrix();
        buff.putFloat((float)projectionMat.m00());
        buff.putFloat((float)projectionMat.m01());
        buff.putFloat((float)projectionMat.m02());
        buff.putFloat((float)projectionMat.m03());

        buff.putFloat((float)projectionMat.m10());
        buff.putFloat((float)projectionMat.m11());
        buff.putFloat((float)projectionMat.m12());
        buff.putFloat((float)projectionMat.m13());

        buff.putFloat((float)projectionMat.m20());
        buff.putFloat((float)projectionMat.m21());
        buff.putFloat((float)projectionMat.m22());
        buff.putFloat((float)projectionMat.m23());

        buff.putFloat((float)projectionMat.m30());
        buff.putFloat((float)projectionMat.m31());
        buff.putFloat((float)projectionMat.m32());
        buff.putFloat((float)projectionMat.m33());

        //put the light space matrix
        Matrix4d lightSpaceMat = Globals.renderingEngine.getLightDepthMatrix();
        buff.putFloat((float)lightSpaceMat.m00());
        buff.putFloat((float)lightSpaceMat.m01());
        buff.putFloat((float)lightSpaceMat.m02());
        buff.putFloat((float)lightSpaceMat.m03());

        buff.putFloat((float)lightSpaceMat.m10());
        buff.putFloat((float)lightSpaceMat.m11());
        buff.putFloat((float)lightSpaceMat.m12());
        buff.putFloat((float)lightSpaceMat.m13());

        buff.putFloat((float)lightSpaceMat.m20());
        buff.putFloat((float)lightSpaceMat.m21());
        buff.putFloat((float)lightSpaceMat.m22());
        buff.putFloat((float)lightSpaceMat.m23());

        buff.putFloat((float)lightSpaceMat.m30());
        buff.putFloat((float)lightSpaceMat.m31());
        buff.putFloat((float)lightSpaceMat.m32());
        buff.putFloat((float)lightSpaceMat.m33());

        //put the view pos
        Vector3d viewPos = CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera);
        if(viewPos != null){
            buff.putFloat((float)viewPos.x);
            buff.putFloat((float)viewPos.y);
            buff.putFloat((float)viewPos.z);
            buff.putFloat((float)1);
        } else {
            buff.putFloat((float)1);
            buff.putFloat((float)0);
            buff.putFloat((float)0);
            buff.putFloat((float)1);
        }

        //put the frame count
        buff.putInt((int)Globals.engineState.timekeeper.getNumberOfRenderFramesElapsed());

        //put the engine time
        buff.putFloat((float)Globals.engineState.timekeeper.getCurrentRendererTime());

        //put time of day
        buff.putFloat(Globals.clientState.clientTemporalService.getTime());


        buff.flip();
        standardUniformSSBO.upload(Globals.renderingEngine.getOpenGLState());
    }

    /**
     * Gets the standard uniform ssbo
     * @return The standard uniform ssbo
     */
    public ShaderStorageBuffer getStandardUnifomSSBO(){
        return this.standardUniformSSBO;
    }

}
