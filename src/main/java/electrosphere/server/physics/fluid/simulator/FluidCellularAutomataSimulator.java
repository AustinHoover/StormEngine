package electrosphere.server.physics.fluid.simulator;

import java.util.List;

import electrosphere.server.physics.fluid.manager.ServerFluidChunk;

/**
 * Simulates server fluid chunks via cellular automata
 */
public class FluidCellularAutomataSimulator implements ServerFluidSimulator {

    static final float MAX_WEIGHT = 1.0f;

    static final float GRAVITY_DIFF = 0.04f;

    @Override
    public void simulate(List<ServerFluidChunk> fluidChunks, List<ServerFluidChunk> broadcastQueue) {
        for(ServerFluidChunk chunk : fluidChunks){
            boolean updated = this.simulateChunk(chunk);
            if(updated){
                broadcastQueue.add(chunk);
            }
        }
    }

    /**
     * Simulates a single chunk
     * @param fluidChunk The fluid chunk
     * @return true if the chunk was updated, false otherwise
     */
    public boolean simulateChunk(ServerFluidChunk fluidChunk) {
        
        float[][][] terrainWeights = null;//terrainChunk.getWeights();

        //if true, alerts the server data cell to broadcast a new update message to all clients within it
        boolean update = false;

        for(int x = ServerFluidChunk.TRUE_DATA_OFFSET; x < ServerFluidChunk.TRUE_DATA_DIM + ServerFluidChunk.TRUE_DATA_OFFSET; x++){
            for(int y = ServerFluidChunk.TRUE_DATA_OFFSET; y < ServerFluidChunk.TRUE_DATA_DIM + ServerFluidChunk.TRUE_DATA_OFFSET; y++){
                for(int z = ServerFluidChunk.TRUE_DATA_OFFSET; z < ServerFluidChunk.TRUE_DATA_DIM + ServerFluidChunk.TRUE_DATA_OFFSET; z++){
                    if(fluidChunk.getWeight(x, y, z) <= 0){
                        continue;
                    } else {
                        if(y > ServerFluidChunk.TRUE_DATA_OFFSET && fluidChunk.getWeight(x, y - 1, z)  < MAX_WEIGHT){
                            update = true;
                            fluidChunk.setWeight(x, y, z, fluidChunk.getWeight(x, y, z) - GRAVITY_DIFF);
                            fluidChunk.setWeight(x, y - 1, z, fluidChunk.getWeight(x, y - 1, z) + GRAVITY_DIFF);
                        } else {
                            //propagate sideways
                            int[] offsetX = new int[]{-1,1,0,0};
                            int[] offsetZ = new int[]{0,0,-1,1};
                            for(int i = 0; i < 4; i++){
                                int realX = x + offsetX[i];
                                int realZ = z + offsetZ[i];
                                if(realX > ServerFluidChunk.TRUE_DATA_OFFSET && realX < ServerFluidChunk.TRUE_DATA_DIM + ServerFluidChunk.TRUE_DATA_OFFSET - 1 &&
                                    realZ > ServerFluidChunk.TRUE_DATA_OFFSET && realZ < ServerFluidChunk.TRUE_DATA_DIM + ServerFluidChunk.TRUE_DATA_OFFSET - 1
                                ){
                                    if(
                                        fluidChunk.getWeight(realX, y, realZ) < fluidChunk.getWeight(x, y, z) && 
                                        (
                                            terrainWeights == null ||
                                            (
                                                terrainWeights != null &&
                                                terrainWeights[realX][y][realZ] < MAX_WEIGHT
                                            )
                                        )
                                    ){
                                        update = true;
                                        fluidChunk.setWeight(x, y, z, fluidChunk.getWeight(x, y, z) - GRAVITY_DIFF);
                                        fluidChunk.setWeight(realX, y, realZ, fluidChunk.getWeight(realX, y, realZ) + GRAVITY_DIFF);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return update;
    }
    

}
