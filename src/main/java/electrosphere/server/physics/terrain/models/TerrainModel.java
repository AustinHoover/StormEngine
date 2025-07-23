package electrosphere.server.physics.terrain.models;

import electrosphere.data.biome.BiomeData;
import electrosphere.engine.Globals;
import electrosphere.server.physics.terrain.generation.ProceduralChunkGenerator;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import electrosphere.util.annotation.Exclude;

/**
 * The model of the terrain
 */
public class TerrainModel {

    /**
     * Maximum number of positions that macro data can be interpolated from
     */
    public static final int MAX_SAMPLEABLE_MACRO_POSITIONS = 2048;

    /**
     * Maximum size of the macro data (must be a power of 2 plus 1 -- the plus 1 is so that we have a macro value for the chunks at the very edge of the world)
     */
    public static final int MAX_MACRO_DATA_SIZE = MAX_SAMPLEABLE_MACRO_POSITIONS + 2;


    /**
     * The scale of the macro data
     */
    public static final int DEFAULT_MACRO_DATA_SCALE = 32;

    /**
     * The maximum discrete world size.
     */
    public static final int MAX_WORLD_SIZE_DISCRETE = MAX_SAMPLEABLE_MACRO_POSITIONS * DEFAULT_MACRO_DATA_SCALE;
    

    /**
     * The discrete array dimension of the model (must be a power of 2 plus 1 -- the plus 1 is so that we have a macro value for the chunks at the very edge of the world)
     */
    int discreteArrayDimension = MAX_MACRO_DATA_SIZE;

    /**
     * The macro level elevation data
     */
    @Exclude
    private float[][] elevation;

    /**
     * The macro level biome data
     */
    @Exclude
    private short[][] biome;
    
    /**
     * The real coordinate mountain threshold
     */
    float realMountainThreshold;

    /**
     * The real coordinate ocean threshold
     */
    float realOceanThreshold;

    /**
     * The seed of the terrain
     */
    long seed = 0;

    /**
     * The macro data scale
     * <p>
     * !!NOTE!!: macroDataScale * biome.length must be greater than or equal to the number of chunks that are generateable
     * </p>
     */
    int macroDataScale = DEFAULT_MACRO_DATA_SCALE;
    
    /**
     * Private constructor
     */
    TerrainModel() {
    }

    /**
     * Creates the default terrain model
     * @return The default terrain model
     */
    public static TerrainModel create(long seed){
        TerrainModel rVal = new TerrainModel();
        rVal.elevation = new float[rVal.discreteArrayDimension][rVal.discreteArrayDimension];
        rVal.biome = new short[rVal.discreteArrayDimension][rVal.discreteArrayDimension];
        rVal.seed = seed;
        return rVal;
    }
    
    /**
     * Constructs a terrain model
     * @param dimension The dimension of the terrain model
     * @param dynamicInterpolationRatio The dynamic interpolation ratio
     * @return The terrain model
     */
    public static TerrainModel constructTerrainModel(int dimension){
        TerrainModel rVal = new TerrainModel();
        rVal.discreteArrayDimension = dimension;
        return rVal;
    }

    /**
     * Generates a test terrain model
     * @return The test terrain model
     */
    public static TerrainModel generateTestModel(){
        TerrainModel rVal = new TerrainModel();
        rVal.discreteArrayDimension = ProceduralChunkGenerator.GENERATOR_REALM_SIZE;
        int macroDataImageScale = ProceduralChunkGenerator.GENERATOR_REALM_SIZE / DEFAULT_MACRO_DATA_SCALE + 1;
        rVal.biome = new short[macroDataImageScale][macroDataImageScale];
        for(int x = 0; x < macroDataImageScale; x++){
            for(int z = 0; z < macroDataImageScale; z++){
                rVal.biome[x][z] = ProceduralChunkGenerator.DEFAULT_BIOME_INDEX;
            }
        }
        rVal.biome[1][0] = 0;
        return rVal;
    }
    
    /**
     * Gets the macro elevation data for the terrain model
     * @return The macro elevation data
     */
    public float[][] getElevation(){
        return elevation;
    }

    /**
     * Gets the macro biome data for the terrain model
     * @return The macro biome data
     */
    public short[][] getBiome(){
        return biome;
    }

    /**
     * Sets the biome array
     * @param biome The biome array
     */
    public void setBiome(short[][] biome){
        this.biome = biome;
    }

    /**
     * Gets the real coordinate mountain threshold
     * @return The threshold
     */
    public float getRealMountainThreshold() {
        return realMountainThreshold;
    }

    /**
     * Gets the real coordinate ocean threshold
     * @return The threshold
     */
    public float getRealOceanThreshold() {
        return realOceanThreshold;
    }

    /**
     * Sets the elevation array (For instance when read from save file on loading a save)
     * @param elevation The elevation array to set to
     */
    public void setElevationArray(float[][] elevation){
        this.elevation = elevation;
    }

    /**
     * Gets the surface biome for a given world position
     * @param worldX The world X
     * @param worldZ The world Z
     * @return The biome
     */
    public BiomeData getClosestSurfaceBiome(int worldX, int worldZ){
        //essentially, select the closest macro data point, not just floor
        int offsetX = worldX % macroDataScale > (macroDataScale / 2.0) ? 1 : 0;
        int offsetZ = worldZ % macroDataScale > (macroDataScale / 2.0) ? 1 : 0;


        int macroX = worldX / macroDataScale + offsetX;
        int macroZ = worldZ / macroDataScale + offsetZ;
        int surfaceBiomeIndex = this.biome[macroX][macroZ];
        BiomeData biome = Globals.gameConfigCurrent.getBiomeMap().getBiomeByIndex(surfaceBiomeIndex);
        return biome;
    }

    /**
     * Gets the surface biome for a given macro position
     * @param worldX The macro X
     * @param worldZ The macro Z
     * @return The biome
     */
    public BiomeData getMacroData(int macroX, int macroZ){
        int surfaceBiomeIndex = this.biome[macroX][macroZ];
        BiomeData biome = Globals.gameConfigCurrent.getBiomeMap().getBiomeByIndex(surfaceBiomeIndex);
        return biome;
    }

    /**
     * Gets the seed of the terrain model
     * @return The seed
     */
    public long getSeed(){
        return seed;
    }

    /**
     * Sets the seed of the terrain model
     * @param seed The seed
     */
    public void setSeed(long seed){
        this.seed = seed;
    }

    /**
     * Sets the scale of the macro data
     * <p>
     * !!NOTE!!: macroDataScale * biome.length must be greater than or equal to the number of chunks that are generateable
     * </p>
     * @param scale The scale
     */
    public void setMacroDataScale(int scale){
        this.macroDataScale = scale;
    }

    /**
     * Gets the scale of the macro data
     * @return The scale
     */
    public int getMacroDataScale(){
        return this.macroDataScale;
    }

    /**
     * Gets the width of the macro data in real terms
     * @return The width of the macro data in real terms
     */
    public double getMacroWidthInRealTerms(){
        return this.macroDataScale * ServerTerrainChunk.CHUNK_DIMENSION;
    }

    /**
     * Gets the size of the discrete arrays
     * @return The size of the discrete arrays
     */
    public int getDiscreteArrayDimension(){
        return discreteArrayDimension;
    }

}
