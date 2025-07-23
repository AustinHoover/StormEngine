package electrosphere.data.entity.creature.visualattribute;

import java.util.List;

public class AttributeVariant {
    String id;
    String model;
    List<String> meshes;

    public String getId(){
        return id;
    }

    public String getModel(){
        return model;
    }

    public List<String> getMeshes(){
        return meshes;
    }

}
