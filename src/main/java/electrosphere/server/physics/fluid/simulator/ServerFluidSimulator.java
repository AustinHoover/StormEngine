package electrosphere.server.physics.fluid.simulator;

import java.util.List;

import electrosphere.server.physics.fluid.manager.ServerFluidChunk;

/**
 * A system capable of simulating a server fluid chunk for a single frame
 */
public interface ServerFluidSimulator {
    
    /**
     * Simulates the chunks for single step
     * @param fluidChunks The list of fluid chunks to simulate
     */
    public void simulate(List<ServerFluidChunk> fluidChunks, List<ServerFluidChunk> broadcastQueue);

}
