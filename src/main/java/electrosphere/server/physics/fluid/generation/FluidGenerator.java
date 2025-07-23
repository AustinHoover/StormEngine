package electrosphere.server.physics.fluid.generation;

import electrosphere.server.physics.fluid.manager.ServerFluidChunk;
import electrosphere.server.physics.fluid.models.FluidModel;

/**
 * Generates fluid
 */
public interface FluidGenerator {
    
    /**
     * Generates a chunk given an x, y, and z
     * @param worldX The x component
     * @param worldY The y component
     * @param worldZ The z component
     * @return The chunk
     */
    public ServerFluidChunk generateChunk(int worldX, int worldY, int worldZ);

    /**
     * Sets the fluid model for the generation algorithm
     * @param model The fluid model
     */
    public void setModel(FluidModel model);

}
