package electrosphere.client.player;

import org.joml.Vector3i;

import electrosphere.logger.LoggerInterface;

public class ClientPlayerData {
    Vector3i worldPos;
    int simulationRadius = 3;
    
    boolean loaded = false;
    
    public ClientPlayerData(){
    }
    
    public boolean hasLoaded(){
        return loaded;
    }
    
    public void setWorldPos(Vector3i worldPos){
        this.worldPos = worldPos;
        LoggerInterface.loggerGameLogic.INFO("Loaded client data");
        loaded = true;
    }

    public Vector3i getWorldPos() {
        return worldPos;
    }
    
    
}
