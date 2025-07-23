package electrosphere.renderer.shader;

import java.nio.FloatBuffer;
import java.util.Map;

import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4d;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL40;

import electrosphere.engine.Globals;
import electrosphere.renderer.OpenGLState;

/**
 * Utility functions for shaders
 */
public class ShaderUtils {

    /**
     * Size of the buffers
     */
    private static final int BUFF_SIZE = 16;

    /**
     * Private float array for setting uniforms
     */
    private static final FloatBuffer floatBuff = BufferUtils.createFloatBuffer(BUFF_SIZE);

    /**
     * Private double array for setting uniforms
     */
    private static final double[] double3Arr = new double[3];

    /**
     * Private double array for setting uniforms
     */
    private static final int[] int3Arr = new int[3];

    /**
     * Private double array for setting uniforms
     */
    private static final double[] double2Arr = new double[2];

    /**
     * Private double array for setting uniforms
     */
    private static final int[] int2Arr = new int[2];
    
    /**
     * Sets the value of a uniform on this shader
     * @param uniformLocation the uniform location
     * @param value the value
     */
    protected static void setUniform(OpenGLState openGLState, Map<Integer,Object> uniformMap, int uniformLocation, Object value){
        if(
            OpenGLState.DISABLE_CACHING ||
            !uniformMap.containsKey(uniformLocation) ||
            !uniformMap.get(uniformLocation).equals(value)
        ){

            //
            //matrix4f
            if(value instanceof Matrix4f){
                Matrix4f currentUniform = (Matrix4f)value;
                floatBuff.put(currentUniform.m00());
                floatBuff.put(currentUniform.m01());
                floatBuff.put(currentUniform.m02());
                floatBuff.put(currentUniform.m03());
                floatBuff.put(currentUniform.m10());
                floatBuff.put(currentUniform.m11());
                floatBuff.put(currentUniform.m12());
                floatBuff.put(currentUniform.m13());
                floatBuff.put(currentUniform.m20());
                floatBuff.put(currentUniform.m21());
                floatBuff.put(currentUniform.m22());
                floatBuff.put(currentUniform.m23());
                floatBuff.put(currentUniform.m30());
                floatBuff.put(currentUniform.m31());
                floatBuff.put(currentUniform.m32());
                floatBuff.put(currentUniform.m33());
                floatBuff.flip();
                GL40.glUniformMatrix4fv(uniformLocation, false, floatBuff);
                Globals.renderingEngine.checkError();
                if(uniformMap.containsKey(uniformLocation)){
                    ((Matrix4f)uniformMap.get(uniformLocation)).set(currentUniform);
                } else {
                    uniformMap.put(uniformLocation,new Matrix4f(currentUniform)); //create new matrix4f to break pointer-matching with equals on cache check
                }

                //
                //matrix4d
            } else if(value instanceof Matrix4d){
                Matrix4d currentUniform = (Matrix4d)value;
                floatBuff.put((float)currentUniform.m00());
                floatBuff.put((float)currentUniform.m01());
                floatBuff.put((float)currentUniform.m02());
                floatBuff.put((float)currentUniform.m03());
                floatBuff.put((float)currentUniform.m10());
                floatBuff.put((float)currentUniform.m11());
                floatBuff.put((float)currentUniform.m12());
                floatBuff.put((float)currentUniform.m13());
                floatBuff.put((float)currentUniform.m20());
                floatBuff.put((float)currentUniform.m21());
                floatBuff.put((float)currentUniform.m22());
                floatBuff.put((float)currentUniform.m23());
                floatBuff.put((float)currentUniform.m30());
                floatBuff.put((float)currentUniform.m31());
                floatBuff.put((float)currentUniform.m32());
                floatBuff.put((float)currentUniform.m33());
                floatBuff.flip();
                GL40.glUniformMatrix4fv(uniformLocation, false, floatBuff);
                Globals.renderingEngine.checkError();
                if(uniformMap.containsKey(uniformLocation)){
                    ((Matrix4d)uniformMap.get(uniformLocation)).set(currentUniform);
                } else {
                    uniformMap.put(uniformLocation,new Matrix4d(currentUniform)); //create new matrix4f to break pointer-matching with equals on cache check
                }

                //
                //vector4d
            } else if(value instanceof Vector4d){
                Vector4d currentUniform = (Vector4d)value;
                floatBuff.put((float)currentUniform.x);
                floatBuff.put((float)currentUniform.y);
                floatBuff.put((float)currentUniform.z);
                floatBuff.put((float)currentUniform.w);
                floatBuff.flip();
                GL40.glUniform4fv(uniformLocation, floatBuff);
                Globals.renderingEngine.checkError();
                floatBuff.limit(BUFF_SIZE);
                if(uniformMap.containsKey(uniformLocation)){
                    ((Vector4d)uniformMap.get(uniformLocation)).set(currentUniform);
                } else {
                    uniformMap.put(uniformLocation,new Vector4d(currentUniform)); //create new vector3f to break pointer-matching with equals on cache check
                }

                //
                //vector4f
            } else if(value instanceof Vector4f){
                Vector4f currentUniform = (Vector4f)value;
                floatBuff.put(currentUniform.x);
                floatBuff.put(currentUniform.y);
                floatBuff.put(currentUniform.z);
                floatBuff.put(currentUniform.w);
                floatBuff.flip();
                GL40.glUniform4fv(uniformLocation, floatBuff);
                Globals.renderingEngine.checkError();
                floatBuff.limit(BUFF_SIZE);
                if(uniformMap.containsKey(uniformLocation)){
                    ((Vector4f)uniformMap.get(uniformLocation)).set(currentUniform);
                } else {
                    uniformMap.put(uniformLocation,new Vector4f(currentUniform)); //create new vector3f to break pointer-matching with equals on cache check
                }

                //
                //vector3d
            } else if(value instanceof Vector3f){
                Vector3f currentUniform = (Vector3f)value;
                floatBuff.put(currentUniform.x);
                floatBuff.put(currentUniform.y);
                floatBuff.put(currentUniform.z);
                floatBuff.flip();
                GL40.glUniform3fv(uniformLocation, floatBuff);
                Globals.renderingEngine.checkError();
                floatBuff.limit(BUFF_SIZE);
                if(uniformMap.containsKey(uniformLocation)){
                    ((Vector3f)uniformMap.get(uniformLocation)).set(currentUniform);
                } else {
                    uniformMap.put(uniformLocation,new Vector3f(currentUniform)); //create new vector3f to break pointer-matching with equals on cache check
                }

                //
                //vector3d
            } else if(value instanceof Vector3d){
                Vector3d currentUniform = (Vector3d)value;
                double3Arr[0] = currentUniform.x;
                double3Arr[1] = currentUniform.y;
                double3Arr[2] = currentUniform.z;
                GL40.glUniform3dv(uniformLocation, double3Arr);
                Globals.renderingEngine.checkError();
                if(uniformMap.containsKey(uniformLocation)){
                    ((Vector3d)uniformMap.get(uniformLocation)).set(currentUniform);
                } else {
                    uniformMap.put(uniformLocation,new Vector3d(currentUniform)); //create new vector3d to break pointer-matching with equals on cache check
                }

                //
                //vector2d
            } else if(value instanceof Vector2d){
                Vector2d currentUniform = (Vector2d)value;
                double2Arr[0] = currentUniform.x;
                double2Arr[1] = currentUniform.y;
                GL40.glUniform2dv(uniformLocation, double2Arr);
                Globals.renderingEngine.checkError();
                if(uniformMap.containsKey(uniformLocation)){
                    ((Vector2d)uniformMap.get(uniformLocation)).set(currentUniform);
                } else {
                    uniformMap.put(uniformLocation,new Vector2d(currentUniform)); //create new vector2d to break pointer-matching with equals on cache check
                }

                //
                //Vector3i
            } else if(value instanceof Vector3i){
                Vector3i currentUniform = (Vector3i)value;
                int3Arr[0] = currentUniform.x;
                int3Arr[1] = currentUniform.y;
                int3Arr[2] = currentUniform.z;
                GL40.glUniform3uiv(uniformLocation, int3Arr);
                Globals.renderingEngine.checkError();
                if(uniformMap.containsKey(uniformLocation)){
                    ((Vector3i)uniformMap.get(uniformLocation)).set(currentUniform);
                } else {
                    uniformMap.put(uniformLocation,new Vector3i(currentUniform)); //create new vector2d to break pointer-matching with equals on cache check
                }

                //
                //Vector2i
            } else if(value instanceof Vector2i){
                Vector2i currentUniform = (Vector2i)value;
                int2Arr[0] = currentUniform.x;
                int2Arr[1] = currentUniform.y;
                GL40.glUniform2uiv(uniformLocation, int2Arr);
                Globals.renderingEngine.checkError();
                if(uniformMap.containsKey(uniformLocation)){
                    ((Vector2i)uniformMap.get(uniformLocation)).set(currentUniform);
                } else {
                    uniformMap.put(uniformLocation,new Vector2i(currentUniform)); //create new vector2d to break pointer-matching with equals on cache check
                }

                //
                //integer
            } else if(value instanceof Integer){
                GL40.glUniform1i(uniformLocation, (Integer)value);
                Globals.renderingEngine.checkError();
                uniformMap.put(uniformLocation,(Integer)value);

                //
                //float
            } else if(value instanceof Float){
                GL40.glUniform1f(uniformLocation, (Float)value);
                Globals.renderingEngine.checkError();
                uniformMap.put(uniformLocation,(Float)value);

                //
                //double
            } else if(value instanceof Double){
                GL40.glUniform1d(uniformLocation, (Double)value);
                Globals.renderingEngine.checkError();
                uniformMap.put(uniformLocation,(Double)value);

            } else {
                throw new UnsupportedOperationException("Tried to set uniform with unsupported type! " + value);
            }
        }
    }

}
