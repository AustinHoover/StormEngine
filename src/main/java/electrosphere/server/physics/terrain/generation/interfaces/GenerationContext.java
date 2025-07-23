package electrosphere.server.physics.terrain.generation.interfaces;

import electrosphere.server.datacell.ServerWorldData;

/**
 * The context the generation is happening in. Stores things like biomes to interpolate between.
 */
public class GenerationContext {
    
    /**
     * The world data for the realm we're generating for
     */
    ServerWorldData serverWorldData;

    /**
     * Constructor
     */
    public GenerationContext(){
    }

    /**
     * Gets the world data for the server
     * @return The world data
     */
    public ServerWorldData getServerWorldData(){
        return serverWorldData;
    }

    /**
     * Sets the world data for the context
     * @param serverWorldData The world data
     */
    public void setServerWorldData(ServerWorldData serverWorldData){
        this.serverWorldData = serverWorldData;
    }

}
