package electrosphere.server.physics.terrain.generation.macro;

import java.util.List;
import java.util.Random;

import electrosphere.data.biome.BiomeData;
import electrosphere.data.biome.BiomeTypeMap;
import electrosphere.engine.Globals;
import electrosphere.server.physics.terrain.models.TerrainModel;

/**
 * The default approach to generating macro data
 */
public class DefaultMacroGenerator implements MacroGenerator {

    /**
     * The default surface biome
     */
    static final short DEFAULT_SURFACE_BIOME = 1;

    @Override
    public void generate(TerrainModel model) {
        int DIM = model.getDiscreteArrayDimension();
        float[][] elevation = model.getElevation();
        short[][] biomeArr = model.getBiome();
        long seed = model.getSeed();
        Random rand = new Random(seed);
        BiomeTypeMap biomeTypeMap = Globals.gameConfigCurrent.getBiomeMap();
        List<BiomeData> biomes = biomeTypeMap.getSurfaceBiomes();

        for(int x = 0; x < DIM; x++){
            for(int y = 0; y < DIM; y++){
                BiomeData biome = biomes.get(rand.nextInt(biomes.size()));
                elevation[x][y] = 1;
                biomeArr[x][y] = (short)(int)(biomeTypeMap.getIndexOfBiome(biome));
            }
        }

        model.setElevationArray(elevation);
        model.setBiome(biomeArr);
    }
    
}
