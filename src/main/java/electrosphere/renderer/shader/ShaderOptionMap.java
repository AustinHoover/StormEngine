package electrosphere.renderer.shader;

import java.util.List;
import java.util.Map;

import electrosphere.logger.LoggerInterface;

public class ShaderOptionMap {
    
    protected Map<String,List<String>> shaderAlternativesMap;

    public List<String> getAlternativesForFile(String filePath){
        return shaderAlternativesMap.get(filePath);
    }

    public void debug(){
        LoggerInterface.loggerRenderer.DEBUG("============================");
        LoggerInterface.loggerRenderer.DEBUG("Debug shader alternative map");
        LoggerInterface.loggerRenderer.DEBUG("============================");
        for(String key : shaderAlternativesMap.keySet()){
            LoggerInterface.loggerRenderer.DEBUG(key);
            for(String alternative : shaderAlternativesMap.get(key)){
                LoggerInterface.loggerRenderer.DEBUG(" - " + alternative);
            }
        }
    }

}
