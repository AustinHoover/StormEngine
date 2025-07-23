package electrosphere.data.entity.item;

import java.util.List;

public class EquipWhitelist {
    
    String creatureId;
    String model;
    List<String> meshList;
    List<String> meshMaskList;

    public String getCreatureId(){
        return creatureId;
    }

    public String getModel(){
        return model;
    }

    public List<String> getMeshList(){
        return meshList;
    }

    public List<String> getMeshMaskList(){
        return meshMaskList;
    }

}
