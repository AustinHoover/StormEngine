package electrosphere.server.physics.fluid.generation;

import electrosphere.server.physics.fluid.manager.ServerFluidChunk;
import electrosphere.server.physics.fluid.models.FluidModel;

public class DefaultFluidGenerator implements FluidGenerator {

    @Override
    public ServerFluidChunk generateChunk(int worldX, int worldY, int worldZ) {
        ServerFluidChunk chunk = new ServerFluidChunk(worldX, worldY, worldZ);

        return chunk;
    }

    @Override
    public void setModel(FluidModel model) {
        throw new UnsupportedOperationException("Unimplemented method 'setModel'");
    }
    
}
