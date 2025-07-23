package electrosphere.server.physics.terrain.generation.macro;

import electrosphere.data.biome.BiomeData;
import electrosphere.data.biome.BiomeTypeMap;
import electrosphere.engine.Globals;
import electrosphere.server.physics.terrain.models.TerrainModel;

/**
 * Generates a world that is a single biome
 */
public class HomogenousMacroGenerator implements MacroGenerator {

    /**
     * ID of the biome to generate with
     */
    int biomeId = 0;

    /**
     * Constructor
     * @param biomeId The biome to use when creating the world
     */
    public HomogenousMacroGenerator(String biomeId){
        BiomeTypeMap biomeTypeMap = Globals.gameConfigCurrent.getBiomeMap();
        BiomeData biomeData = biomeTypeMap.getType(biomeId);
        this.biomeId = biomeTypeMap.getIndexOfBiome(biomeData);
    }

    @Override
    public void generate(TerrainModel model) {
        int DIM = model.getDiscreteArrayDimension();
        float[][] elevation = model.getElevation();
        short[][] biome = model.getBiome();

        for(int x = 0; x < DIM; x++){
            for(int y = 0; y < DIM; y++){
                elevation[x][y] = 1;
                biome[x][y] = (short)this.biomeId;
            }
        }

        model.setElevationArray(elevation);
        model.setBiome(biome);
    }
    
}
