package electrosphere.server.physics.terrain.generation.interfaces;

import java.util.List;

import electrosphere.server.macro.spatial.MacroObject;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import electrosphere.server.physics.terrain.models.TerrainModel;

/**
 * An interface for generating chunks. Used to isolate different algorithms for getting chunks from one another.
 */
public interface ChunkGenerator {
    
    /**
     * Generates a chunk given an x, y, and z
     * @param macroData The macro data
     * @param worldX The x component
     * @param worldY The y component
     * @param worldZ The z component
     * @param stride The stride of the data
     * @return The chunk
     */
    public ServerTerrainChunk generateChunk(List<MacroObject> macroData, int worldX, int worldY, int worldZ, int stride);

    /**
     * Gets the elevation at a given 2d coordinate
     * @param worldX The world x coordinate
     * @param worldZ The world z coordinate
     * @param chunkX The chunk x coordinate
     * @param chunkZ The chunk z coordinate
     * @return The elevation at that specific coordinate
     */
    public double getElevation(int worldX, int worldZ, int chunkX, int chunkZ);

    /**
     * Sets the terrain model for the generation algorithm
     * @param model The terrain model
     */
    public void setModel(TerrainModel model);

}
