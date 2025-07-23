package electrosphere.collision;

import electrosphere.client.scene.ClientWorldData;
import electrosphere.client.terrain.manager.ClientTerrainManager;
import electrosphere.engine.Globals;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.physics.terrain.manager.ServerTerrainManager;

import org.joml.Vector3f;

public class CollisionWorldData {
    
    
    
    ClientTerrainManager clientTerrainManager;
    
    
    
    ServerWorldData serverWorldData;
    ServerTerrainManager serverTerrainManager;

    public CollisionWorldData(){
        
    }
    
    public CollisionWorldData(ServerWorldData serverWorldData){
        this.serverWorldData = serverWorldData;
    }
    
    
    public Vector3f getWorldBoundMin(){
        if(Globals.clientState.clientWorldData != null){
            return Globals.clientState.clientWorldData.getWorldBoundMin();
        } else {
            return serverWorldData.getWorldBoundMin();
        }
    }
    
    public Vector3f getWorldBoundMax(){
        if(Globals.clientState.clientWorldData != null){
            return Globals.clientState.clientWorldData.getWorldBoundMax();
        } else {
            return serverWorldData.getWorldBoundMax();
        }
    }
    
    public int convertRealToWorld(double real){
        if(Globals.clientState.clientWorldData != null){
            return ClientWorldData.convertRealToChunkSpace(real);
        } else {
            return ServerWorldData.convertRealToChunkSpace(real);
        }
    }

    public double convertWorldToReal(int world){
        if(Globals.clientState.clientWorldData != null){
            return ClientWorldData.convertChunkToRealSpace(world);
        } else {
            return ServerWorldData.convertChunkToRealSpace(world);
        }
    }

    public int getDynamicInterpolationRatio(){
        if(Globals.clientState.clientWorldData != null){
            return Globals.clientState.clientWorldData.getWorldDiscreteSize();
        } else {
            return serverWorldData.getDynamicInterpolationRatio();
        }
    }

    public int getWorldDiscreteSize(){
        if(Globals.clientState.clientWorldData != null){
            return Globals.clientState.clientWorldData.getWorldDiscreteSize();
        } else {
            return serverWorldData.getWorldSizeDiscrete();
        }
    }

}
