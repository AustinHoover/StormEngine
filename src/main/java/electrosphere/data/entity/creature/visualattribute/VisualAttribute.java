package electrosphere.data.entity.creature.visualattribute;

import java.util.List;

public class VisualAttribute {

    public static final String TYPE_REMESH = "remesh";
    public static final String TYPE_BONE = "bone";

    String attributeId;
    String type; //remesh or bone
    String subtype; //if bone: yaw,pitch,roll,scalex,scaley,scalez,offx,offy,offz,offl
    //offl = length offset, scaling along the offset from the previous bone (as if to elongate a limb)
    //likely not updated, check out ActorStaticMorph for more details on up to date docs if out of date
    List<AttributeVariant> variants;
    float minValue;
    float maxValue;
    String primaryBone;
    String mirrorBone;

    public String getAttributeId(){
        return attributeId;
    }

    public String getType(){
        return type;
    }

    public String getSubtype(){
        return subtype;
    }

    public float getMinValue(){
        return minValue;
    }

    public float getMaxValue(){
        return maxValue;
    }

    public String getPrimaryBone(){
        return primaryBone;
    }

    public String getMirrorBone(){
        return mirrorBone;
    }

    public List<AttributeVariant> getVariants(){
        return variants;
    }

}
