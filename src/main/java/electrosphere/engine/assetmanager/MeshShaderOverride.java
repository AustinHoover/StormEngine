package electrosphere.engine.assetmanager;

public class MeshShaderOverride {

    String modelName;
    String meshName;
    String vertPath;
    String fragPath;

    public MeshShaderOverride(String modelName, String meshName, String vertPath, String fragPath){
        this.modelName = modelName;
        this.meshName = meshName;
        this.vertPath = vertPath;
        this.fragPath = fragPath;
    }

    public String getModelName(){
        return modelName;
    }

    public String getMeshName(){
        return meshName;
    }

    public String getVertPath(){
        return vertPath;
    }

    public String getFragPath(){
        return fragPath;
    }

}
