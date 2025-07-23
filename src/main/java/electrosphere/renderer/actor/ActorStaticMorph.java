package electrosphere.renderer.actor;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A static morph that is applied to an actor if it is defined in a pipeline creating an actor.
 * For instance, if you provide a static morph for the model in an item in creatures.json, this is the structure that will actually hold that data
 */
public class ActorStaticMorph {
    
    Map<String,StaticMorphTransforms> boneTransformMap = new HashMap<String,StaticMorphTransforms>();

    public void initBoneTransforms(String boneName){
        boneTransformMap.put(boneName, new StaticMorphTransforms());
    }

    public StaticMorphTransforms getBoneTransforms(String boneName){
        return boneTransformMap.get(boneName);
    }

    void setYaw(String boneName, float yaw){
        if(boneTransformMap.containsKey(boneName)){
            boneTransformMap.get(boneName).yaw = yaw;
        }
    }

    void setPitch(String boneName, float pitch){
        if(boneTransformMap.containsKey(boneName)){
            boneTransformMap.get(boneName).pitch = pitch;
        }
    }

    void setRoll(String boneName, float roll){
        if(boneTransformMap.containsKey(boneName)){
            boneTransformMap.get(boneName).roll = roll;
        }
    }

    void setOffsetX(String boneName, float offsetX){
        if(boneTransformMap.containsKey(boneName)){
            boneTransformMap.get(boneName).offset.x = offsetX;
        }
    }

    void setOffsetY(String boneName, float offsetY){
        if(boneTransformMap.containsKey(boneName)){
            boneTransformMap.get(boneName).offset.y = offsetY;
        }
    }

    void setOffsetZ(String boneName, float offsetZ){
        if(boneTransformMap.containsKey(boneName)){
            boneTransformMap.get(boneName).offset.z = offsetZ;
        }
    }

    void setScaleX(String boneName, float scaleX){
        if(boneTransformMap.containsKey(boneName)){
            boneTransformMap.get(boneName).scale.x = scaleX;
        }
    }

    void setScaleY(String boneName, float scaleY){
        if(boneTransformMap.containsKey(boneName)){
            boneTransformMap.get(boneName).scale.y = scaleY;
        }
    }

    void setScaleZ(String boneName, float scaleZ){
        if(boneTransformMap.containsKey(boneName)){
            boneTransformMap.get(boneName).scale.z = scaleZ;
        }
    }

    void setScale(String boneName, float scale){
        if(boneTransformMap.containsKey(boneName)){
            boneTransformMap.get(boneName).scale.set(scale);
        }
    }

    /**
     * Instead of having this code be duplicated every time we want to update a static morph, putting it in here
     */
    public void updateValue(String subtype, String bone, float value){
        switch(subtype){
            case "yaw":
            this.setYaw(bone, value);
            break;
            case "pitch":
            this.setPitch(bone, value);
            break;
            case "roll":
            this.setRoll(bone, value);
            break;
            case "scalex":
            this.setScaleX(bone, value);
            break;
            case "scaley":
            this.setScaleY(bone, value);
            break;
            case "scalez":
            this.setScaleZ(bone, value);
            break;
            case "offx":
            this.setOffsetX(bone, value);
            break;
            case "offy":
            this.setOffsetY(bone, value);
            break;
            case "offz":
            this.setOffsetZ(bone, value);
            break;
            case "scale":
            this.setScale(bone, value);
            break;
            case "offl":
            //TODO
            break;
        }
    }



    public class StaticMorphTransforms {
        
        float yaw = 0.0f;
        float pitch = 0.0f;
        float roll = 0.0f;
        Vector3f offset = new Vector3f(0,0,0);
        Vector3f scale = new Vector3f(1,1,1);

        Matrix4f transform = null;

        public Quaternionf getRotation(){
            return new Quaternionf().rotateXYZ(yaw, pitch, roll);
        }

        public Vector3f getOffset(){
            return offset;
        }

        public Vector3f getScale(){
            return scale;
        }

        public Matrix4f getTransform(){
            if(transform == null){
                transform = new Matrix4f();
            }
            transform.translationRotateScale(offset, getRotation(), scale);
            return transform;
        }
    }

}
