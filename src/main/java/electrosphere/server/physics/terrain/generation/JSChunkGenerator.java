package electrosphere.server.physics.terrain.generation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import electrosphere.client.terrain.cache.ChunkData;
import electrosphere.data.biome.BiomeData;
import electrosphere.data.biome.BiomeSurfaceGenerationParams;

import org.graalvm.polyglot.Value;

import electrosphere.engine.Globals;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.macro.spatial.MacroObject;
import electrosphere.server.physics.terrain.generation.heightmap.EmptySkyGen;
import electrosphere.server.physics.terrain.generation.heightmap.HeightmapGenerator;
import electrosphere.server.physics.terrain.generation.heightmap.HillsGen;
import electrosphere.server.physics.terrain.generation.heightmap.MountainGen;
import electrosphere.server.physics.terrain.generation.heightmap.PlainsGen;
import electrosphere.server.physics.terrain.generation.interfaces.ChunkGenerator;
import electrosphere.server.physics.terrain.generation.interfaces.GeneratedVoxel;
import electrosphere.server.physics.terrain.generation.voxelphase.AnimeMountainsGen;
import electrosphere.server.physics.terrain.generation.voxelphase.HillsVoxelGen;
import electrosphere.server.physics.terrain.generation.voxelphase.MountainVoxelGen;
import electrosphere.server.physics.terrain.generation.voxelphase.VoxelGenerator;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import electrosphere.server.physics.terrain.models.TerrainModel;

/**
 * Dedicated script-based chunk generator
 */
public class JSChunkGenerator implements ChunkGenerator {

    /**
     * The size of the realm for testing generation
     */
    public static final int GENERATOR_REALM_SIZE = 512;

    /**
     * The default biome index
     */
    public static final int DEFAULT_BIOME_INDEX = 1;

    /**
     * The width of the surface in number of voxels
     */
    public static final int SURFACE_VOXEL_WIDTH = 2;

    /**
     * Tag for the test generator
     */
    public static final String SCRIPT_GEN_TEST_TAG = "test";

    /**
     * Controls the default setting for whether to use javascript or not
     */
    public static final boolean DEFAULT_USE_JAVASCRIPT = false;
    
    /**
     * The terreain model for the generator
     */
    TerrainModel terrainModel;

    /**
     * The server world data
     */
    ServerWorldData serverWorldData;

    /**
     * The map of generator tag to the heightmap generator
     */
    Map<String,HeightmapGenerator> tagHeightmapMap = new HashMap<String,HeightmapGenerator>();

    /**
     * The map of generator tag to voxel generator
     */
    Map<String,VoxelGenerator> tagVoxelMap = new HashMap<String,VoxelGenerator>();

    /**
     * Tracks whether to use javascript generation or not
     */
    boolean useJavascript = false;

    /**
     * Constructor
     */
    public JSChunkGenerator(ServerWorldData serverWorldData, boolean useJavascript){
        this.serverWorldData = serverWorldData;
        this.registerHeightmapGenerator(new EmptySkyGen());
        this.registerHeightmapGenerator(new HillsGen());
        this.registerHeightmapGenerator(new PlainsGen());
        this.registerHeightmapGenerator(new MountainGen());
        this.registerVoxelGenerator(new HillsVoxelGen());
        this.registerVoxelGenerator(new AnimeMountainsGen());
        this.registerVoxelGenerator(new MountainVoxelGen());
        this.useJavascript = useJavascript;
    }

    /**
     * Registers a heightmap generator
     * @param generator The heightmap generator
     */
    private void registerHeightmapGenerator(HeightmapGenerator generator){
        tagHeightmapMap.put(generator.getTag(),generator);
    }

    /**
     * Registers a voxel generator
     * @param generator The voxel generator
     */
    private void registerVoxelGenerator(VoxelGenerator generator){
        tagVoxelMap.put(generator.getTag(),generator);
    }

    @Override
    public ServerTerrainChunk generateChunk(List<MacroObject> macroData, int worldX, int worldY, int worldZ, int stride) {
        Globals.profiler.beginAggregateCpuSample("TestGenerationChunkGenerator.generateChunk");
        ServerTerrainChunk rVal = new ServerTerrainChunk(worldX, worldY, worldZ);
        float[][][] weights = new float[ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE][ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE][ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE];;
        int[][][] values = new int[ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE][ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE][ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE];

        try {
            //biome of the current chunk
            BiomeData surfaceBiome = this.terrainModel.getClosestSurfaceBiome(worldX, worldZ);

            BiomeSurfaceGenerationParams surfaceParams = surfaceBiome.getSurfaceGenerationParams();
            HeightmapGenerator heightmapGen = this.tagHeightmapMap.get(surfaceParams.getSurfaceGenTag());
            heightmapGen = this.tagHeightmapMap.get("hills");
            if(heightmapGen == null){
                throw new Error("Undefined heightmap generator in biome! " + surfaceBiome.getId() + " " + surfaceBiome.getDisplayName() + " " + surfaceParams.getSurfaceGenTag());
            }

            //stride value
            int strideValue = (int)Math.pow(2,stride);

            //presolve heightfield
            float[][] heightfield = new float[ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE][ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE];
            for(int x = 0; x < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE; x++){
                for(int z = 0; z < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE; z++){
                    int finalWorldX = worldX + ((x * strideValue) / ServerTerrainChunk.CHUNK_DIMENSION);
                    int finalWorldZ = worldZ + ((z * strideValue) / ServerTerrainChunk.CHUNK_DIMENSION);
                    int finalChunkX = (x * strideValue) % ServerTerrainChunk.CHUNK_DIMENSION;
                    int finalChunkZ = (z * strideValue) % ServerTerrainChunk.CHUNK_DIMENSION;
                    heightfield[x][z] = heightmapGen.getHeight(
                        this.terrainModel.getSeed(),
                        ServerWorldData.convertVoxelToRealSpace(finalChunkX, finalWorldX),
                        ServerWorldData.convertVoxelToRealSpace(finalChunkZ, finalWorldZ)
                    );
                }
            }

            float[][] gradientField = new float[ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE][ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE];
            for(int x = 0; x < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE; x++){
                for(int z = 0; z < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE; z++){
                    float deltaX = 0;
                    float deltaZ = 0;
                    if(x < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE - 1){
                        deltaX = Math.abs(heightfield[x][z] - heightfield[x+1][z]);
                    } else {
                        deltaX = Math.abs(heightfield[x][z] - heightfield[x-1][z]);
                    }
                    if(z < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE - 1){
                        deltaX = Math.abs(heightfield[x][z] - heightfield[x][z+1]);
                    } else {
                        deltaX = Math.abs(heightfield[x][z] - heightfield[x][z-1]);
                    }
                    gradientField[x][z] = deltaX * deltaX + deltaZ * deltaZ;
                }
            }

            Globals.engineState.scriptEngine.getScriptContext().executeSynchronously(() -> {
                int firstType = -2;
                boolean homogenous = true;
                GeneratedVoxel voxel = new GeneratedVoxel();
                Value getVoxelFunc = Globals.engineState.scriptEngine.getScriptContext().invokeEngineMember("chunkGeneratorManager", "getVoxelFunction", SCRIPT_GEN_TEST_TAG);
                for(int x = 0; x < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE; x++){
                    Globals.profiler.beginAggregateCpuSample("TestGenerationChunkGenerator - Generate slice");
                    for(int y = 0; y < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE; y++){
                        for(int z = 0; z < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE; z++){
                            int finalWorldX = worldX + ((x * strideValue) / ServerTerrainChunk.CHUNK_DIMENSION);
                            int finalWorldY = worldY + ((y * strideValue) / ServerTerrainChunk.CHUNK_DIMENSION);
                            int finalWorldZ = worldZ + ((z * strideValue) / ServerTerrainChunk.CHUNK_DIMENSION);
                            int finalChunkX = (x * strideValue) % ServerTerrainChunk.CHUNK_DIMENSION;
                            int finalChunkY = (y * strideValue) % ServerTerrainChunk.CHUNK_DIMENSION;
                            int finalChunkZ = (z * strideValue) % ServerTerrainChunk.CHUNK_DIMENSION;
                            getVoxelFunc.execute(
                                voxel,
                                finalWorldX, finalWorldY, finalWorldZ,
                                finalChunkX, finalChunkY, finalChunkZ,
                                stride,
                                heightfield[x][z],
                                surfaceBiome
                            );
                            weights[x][y][z] = voxel.weight;
                            values[x][y][z] = voxel.type;
                            if(firstType == -2){
                                firstType = values[x][y][z];
                            } else if(homogenous && firstType != values[x][y][z]){
                                homogenous = false;
                            }
                        }
                    }
                    Globals.profiler.endCpuSample();
                }
                if(homogenous){
                    rVal.setHomogenousValue(firstType);
                } else {
                    rVal.setHomogenousValue(ChunkData.NOT_HOMOGENOUS);
                }
                rVal.setWeights(weights);
                rVal.setValues(values);
            });
        } catch(Exception ex){
            ex.printStackTrace();
        }
        Globals.profiler.endCpuSample();
        return rVal;
    }

    @Override
    public double getElevation(int worldX, int worldZ, int chunkX, int chunkZ){
        BiomeData surfaceBiome = this.terrainModel.getClosestSurfaceBiome(worldX, worldZ);

        BiomeSurfaceGenerationParams surfaceParams = surfaceBiome.getSurfaceGenerationParams();
        HeightmapGenerator heightmapGen = this.tagHeightmapMap.get(surfaceParams.getSurfaceGenTag());
        if(heightmapGen == null){
            throw new Error("Undefined heightmap generator in biome! " + surfaceBiome.getId() + " " + surfaceBiome.getDisplayName() + " " + surfaceParams.getSurfaceGenTag());
        }
        double rVal = heightmapGen.getHeight(
            this.terrainModel.getSeed(),
            ServerWorldData.convertVoxelToRealSpace(chunkX, worldX),
            ServerWorldData.convertVoxelToRealSpace(chunkZ, worldZ)
        );
        return rVal;
    }

    @Override
    public void setModel(TerrainModel model) {
        this.terrainModel = model;
    }

}
