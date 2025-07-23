package electrosphere.server.physics.terrain.generation.macro;

import electrosphere.server.physics.terrain.models.TerrainModel;

/**
 * Populates a terrain model's macro data
 */
public interface MacroGenerator {

    /**
     * Generates the macro data in a terrain model
     * @param model The terrain model
     */
    public void generate(TerrainModel model);
    
}
