package electrosphere.util;

import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class TransformUtils {
    

    public Quaternionf calculateQuaternionFromPoints(Vector3d origin, Vector3d direction){
        return new Quaternionf().rotateTo(new Vector3f(0,1,0), new Vector3f((float)(direction.x-origin.x),(float)(direction.y-origin.y),(float)(direction.z-origin.z)));
    }

    public Quaternionf calculateQuaternionFromPoints(Vector3d direction){
        return new Quaternionf().rotateTo(new Vector3f(0,1,0), new Vector3f((float)direction.x,(float)direction.y,(float)direction.z));
    }

    public Quaternionf calculateQuaternionFromPoints(Vector3f origin, Vector3f direction){
        return new Quaternionf().rotateTo(new Vector3f(0,1,0), new Vector3f(direction.x-origin.x,direction.y-origin.y,direction.z-origin.z));
    }

    public Quaternionf calculateQuaternionFromPoints(Vector3f direction){
        return new Quaternionf().rotateTo(new Vector3f(0,1,0), new Vector3f(direction));
    }

}
