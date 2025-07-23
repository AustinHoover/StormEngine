package electrosphere.audio;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.openal.AL11;

import electrosphere.engine.Globals;
import electrosphere.util.math.SpatialMathUtils;

import static org.lwjgl.openal.AL10.*;

/**
 * Encapsulates the listening position in the audio engine
 */
public class AudioListener {
    
    //The position of the listener
    Vector3d position = new Vector3d();

    //eye vector for listener
    Vector3f eye = SpatialMathUtils.getOriginVectorf();
    
    //up vector for listener
    Vector3f up = new Vector3f(0,1,0);

    //buffers used to fetch values
    float xB[] = new float[1];
    float yB[] = new float[1];
    float zB[] = new float[1];
    float vecB[] = new float[6];
    
    /**
     * Constructor
     */
    protected AudioListener() {
        this(new Vector3d(0, 0, 0));
    }

    /**
     * Constructor
     * @param position The position of the listener
     */
    protected AudioListener(Vector3d position) {
        this.position = position;
        alListener3f(AL_POSITION, (float)this.position.x, (float)this.position.y, (float)this.position.z);
        Globals.audioEngine.checkError();
        alListener3f(AL_VELOCITY, 0, 0, 0);
        Globals.audioEngine.checkError();
    }

    /**
     * Sets the speed of the listener
     * @param speed the speed
     */
    protected void setSpeed(Vector3f speed) {
        alListener3f(AL_VELOCITY, speed.x, speed.y, speed.z);
        Globals.audioEngine.checkError();
    }

    /**
     * Sets the position of the listener
     * @param position the position
     */
    protected void setPosition(Vector3d position) {
        AL11.alListener3f(AL_POSITION, (float)position.x, (float)position.y, (float)position.z);
        Globals.audioEngine.checkError();
        AL11.alGetListener3f(AL11.AL_POSITION, xB, yB, zB);
        Globals.audioEngine.checkError();
        this.position.set(xB[0],yB[0],zB[0]);
    }

    /**
     * Sets the orientation of the listener
     * @param at the forward vector of the camera
     * @param up The up vector of the camera
     */
    protected void setOrientation(Vector3f at, Vector3f up) {
        float[] data = new float[6];
        data[0] = at.x;
        data[1] = at.y;
        data[2] = at.z;
        data[3] = up.x;
        data[4] = up.y;
        data[5] = up.z;
        alListenerfv(AL_ORIENTATION, data);
        Globals.audioEngine.checkError();
        AL11.alGetListenerfv(AL11.AL_ORIENTATION, vecB);
        Globals.audioEngine.checkError();
        this.eye.set(vecB[0],vecB[1],vecB[2]);
        this.up.set(vecB[3],vecB[4],vecB[5]);
    }

    /**
     * Gets the position of the listener
     * @return The position
     */
    public Vector3d getPosition(){
        return position;
    }

    /**
     * Gets the eye vector
     * @return the eye vector
     */
    public Vector3f getEyeVector(){
        return eye;
    }

    /**
     * Gets the up vector
     * @return the up vector
     */
    public Vector3f getUpVector(){
        return up;
    }
    
    
}
