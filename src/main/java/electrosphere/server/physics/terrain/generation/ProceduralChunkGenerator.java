package electrosphere.server.physics.terrain.generation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.AABBd;
import org.joml.Vector3d;

import electrosphere.client.terrain.cache.ChunkData;
import electrosphere.data.biome.BiomeData;
import electrosphere.data.biome.BiomeSurfaceGenerationParams;
import electrosphere.data.voxel.sampler.SamplerFile;
import electrosphere.engine.Globals;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.macro.civilization.road.Road;
import electrosphere.server.macro.civilization.town.Town;
import electrosphere.server.macro.region.MacroRegion;
import electrosphere.server.macro.spatial.MacroObject;
import electrosphere.server.macro.structure.VirtualStructure;
import electrosphere.server.physics.terrain.generation.heightmap.EmptySkyGen;
import electrosphere.server.physics.terrain.generation.heightmap.HeightmapGenerator;
import electrosphere.server.physics.terrain.generation.heightmap.HeightmapNoiseGen;
import electrosphere.server.physics.terrain.generation.heightmap.HillsGen;
import electrosphere.server.physics.terrain.generation.heightmap.MountainGen;
import electrosphere.server.physics.terrain.generation.heightmap.PlainsGen;
import electrosphere.server.physics.terrain.generation.interfaces.ChunkGenerator;
import electrosphere.server.physics.terrain.generation.interfaces.GeneratedVoxel;
import electrosphere.server.physics.terrain.generation.interfaces.GenerationContext;
import electrosphere.server.physics.terrain.generation.voxelphase.AnimeMountainsGen;
import electrosphere.server.physics.terrain.generation.voxelphase.HillsVoxelGen;
import electrosphere.server.physics.terrain.generation.voxelphase.MountainVoxelGen;
import electrosphere.server.physics.terrain.generation.voxelphase.NoiseVoxelGen;
import electrosphere.server.physics.terrain.generation.voxelphase.VoxelGenerator;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import electrosphere.server.physics.terrain.models.TerrainModel;
import electrosphere.util.math.GeomUtils;
import electrosphere.util.noise.OpenSimplex2S;
import io.github.studiorailgun.MathUtils;

/**
 * A generator for testing terrain generation
 */
public class ProceduralChunkGenerator implements ChunkGenerator {

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
     * The width of the foundation generated underneath structures
     */
    public static final int VIRTUAL_STRUCTURE_FOUNDATION_WIDTH = 2;

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
    public ProceduralChunkGenerator(ServerWorldData serverWorldData, boolean useJavascript){
        this.serverWorldData = serverWorldData;
        this.registerAllGenerators();
        this.useJavascript = useJavascript;
    }

    /**
     * Registers all generators
     */
    public void registerAllGenerators(){
        tagHeightmapMap.clear();
        tagVoxelMap.clear();
        this.registerHeightmapGenerator(new EmptySkyGen());
        this.registerHeightmapGenerator(new HillsGen());
        this.registerHeightmapGenerator(new PlainsGen());
        this.registerHeightmapGenerator(new MountainGen());
        this.registerVoxelGenerator(new HillsVoxelGen());
        this.registerVoxelGenerator(new AnimeMountainsGen());
        this.registerVoxelGenerator(new MountainVoxelGen());
        for(SamplerFile samplerFile : Globals.gameConfigCurrent.getSamplerFiles()){
            this.registerHeightmapGenerator(new HeightmapNoiseGen(samplerFile));
            this.registerVoxelGenerator(new NoiseVoxelGen(samplerFile));
        }
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

            //stride value
            int strideValue = (int)Math.pow(2,stride);

            //presolve heightfield
            double[][] heightfield = new double[ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE][ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE];
            BiomeData[][] surfaceBiomeMap = new BiomeData[ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE][ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE];
            this.populateElevation(heightfield,surfaceBiomeMap,worldX,worldZ,strideValue);

            double[][] gradientField = new double[ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE][ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE];
            for(int x = 0; x < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE; x++){
                for(int z = 0; z < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE; z++){
                    double deltaX = 0;
                    double deltaZ = 0;
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

            VoxelGenerator voxelGenerator = this.tagVoxelMap.get("test1");

            int firstType = -2;
            boolean homogenous = true;
            GeneratedVoxel voxel = new GeneratedVoxel();
            GenerationContext generationContext = new GenerationContext();
            generationContext.setServerWorldData(serverWorldData);

            //
            // Generate the voxels directly
            //
            for(int x = 0; x < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE; x++){
                int finalWorldX = worldX + ((x * strideValue) / ServerTerrainChunk.CHUNK_DIMENSION);
                int finalChunkX = (x * strideValue) % ServerTerrainChunk.CHUNK_DIMENSION;
                double realX = ServerWorldData.convertVoxelToRealSpace(finalChunkX,finalWorldX);

                for(int z = 0; z < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE; z++){
                    BiomeData surfaceBiome = surfaceBiomeMap[x][z];
                    BiomeSurfaceGenerationParams surfaceParams = surfaceBiome.getSurfaceGenerationParams();


                    int finalWorldZ = worldZ + ((z * strideValue) / ServerTerrainChunk.CHUNK_DIMENSION);
                    int finalChunkZ = (z * strideValue) % ServerTerrainChunk.CHUNK_DIMENSION;
                    double realZ = ServerWorldData.convertVoxelToRealSpace(finalChunkZ,finalWorldZ);
                    double surfaceHeight = heightfield[x][z];
                    double gradient = gradientField[x][z];
                    double surfaceSelection = this.calculateSurfaceNoise(surfaceParams, finalWorldX, finalWorldZ, finalChunkX, finalChunkZ, strideValue, this.terrainModel.getSeed());

                    for(int y = 0; y < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE; y++){
                        int finalWorldY = worldY + ((y * strideValue) / ServerTerrainChunk.CHUNK_DIMENSION);
                        int finalChunkY = (y * strideValue) % ServerTerrainChunk.CHUNK_DIMENSION;
                        double realY = ServerWorldData.convertVoxelToRealSpace(finalChunkY,finalWorldY);
                        
                        voxelGenerator.getVoxel(
                            voxel,
                            finalWorldX, finalWorldY, finalWorldZ,
                            finalChunkX, finalChunkY, finalChunkZ,
                            realX, realY, realZ,
                            stride,
                            surfaceHeight, gradient, surfaceSelection,
                            surfaceBiome, surfaceParams,
                            generationContext
                        );
                        if(voxel != null){
                            weights[x][y][z] = voxel.weight;
                            values[x][y][z] = voxel.type;
                        }
                        //apply macro data
                        if(macroData != null && this.applyMacroData(macroData, realX, realY, realZ, surfaceHeight, voxel)){
                            weights[x][y][z] = voxel.weight;
                            values[x][y][z] = voxel.type;
                        }
                        if(firstType == -2){
                            firstType = values[x][y][z];
                        } else if(
                            homogenous &&
                            (
                                firstType != values[x][y][z] ||
                                (weights[x][y][z] > -1.0f && weights[x][y][z] < 1.0f)
                            )
                        ){
                            homogenous = false;
                        }
                    }
                }
            }


            //
            //Homogenous logic
            //
            if(homogenous){
                rVal.setHomogenousValue(firstType);
            } else {
                rVal.setHomogenousValue(ChunkData.NOT_HOMOGENOUS);
            }
            rVal.setWeights(weights);
            rVal.setValues(values);
        } catch(Exception ex){
            ex.printStackTrace();
        }
        Globals.profiler.endCpuSample();
        return rVal;
    }

    /**
     * Applies macro data to the voxel
     * @param objects The object
     * @param realX The real x position
     * @param realY The real y position
     * @param realZ The real z position
     * @param voxel The voxel
     */
    private boolean applyMacroData(
        List<MacroObject> objects,
        double realX, double realY, double realZ,
        double surfaceHeight,
        GeneratedVoxel voxel
    ){
        boolean rVal = false;
        Vector3d realPt = new Vector3d(realX, realY, realZ);
        for(MacroObject object : objects){
            if(object instanceof Road){
                Road road = (Road)object;
                //broad phase intersection
                if(Math.abs(realY - surfaceHeight) < 3){
                    if(road.getAABB().testPoint(realX, realY, realZ)){
                        if(GeomUtils.pointIntersectsLineSegment(realPt, road.getPoint1(), road.getPoint2(), road.getRadius())){
                            if(voxel.type != ServerTerrainChunk.VOXEL_TYPE_AIR){
                                voxel.type = 1;
                                rVal = true;
                            }
                        }
                    }
                }
            } else if(object instanceof VirtualStructure){
                VirtualStructure struct = (VirtualStructure)object;
                AABBd aabb = struct.getAABB();
                //create a foundation underneath the structure
                if(realX >= aabb.minX && realX <= aabb.maxX && realZ >= aabb.minZ && realZ <= aabb.maxZ){
                    //check if within foundation range
                    double vertDist = aabb.minY - realY;
                    if(vertDist > 0 && vertDist < VIRTUAL_STRUCTURE_FOUNDATION_WIDTH){
                        voxel.type = 1;
                        voxel.weight = 0.95f; //not setting it to 1 to combat z-fighting being caused by the terrain mesh perfectly overlapping the structure mesh
                        rVal = true;
                    } else if(vertDist < 0){
                        voxel.type = 0;
                        voxel.weight = -1;
                        rVal = true;
                    }
                }
            } else if(object instanceof Town){
            } else if(object instanceof MacroRegion region){
                if(Math.abs(realY - surfaceHeight) < 3){
                    if(region.getRegion().getAABB().testPoint(realPt)){
                        if(region.getRegion().intersects(realPt)){
                            if(voxel.type != ServerTerrainChunk.VOXEL_TYPE_AIR){
                                voxel.type = 9;
                                rVal = true;
                            }
                        }
                    }
                }
            } else {
                throw new Error("Unsupported object type " + object);
            }
        }
        return rVal;
    }

    /**
     * Populates the heightfield
     * @param heightfield The heightfield to populate
     * @param surfaceBiomeMap The surface biome map
     * @param worldX The world x position
     * @param worldZ The world z position
     * @param strideValue The stride value
     */
    private void populateElevation(double[][] heightfield, BiomeData[][] surfaceBiomeMap, int worldX, int worldZ, int strideValue){
        for(int x = 0; x < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE; x++){
            for(int z = 0; z < ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE; z++){
                int finalWorldX = worldX + ((x * strideValue) / ServerTerrainChunk.CHUNK_DIMENSION);
                int finalWorldZ = worldZ + ((z * strideValue) / ServerTerrainChunk.CHUNK_DIMENSION);
                int finalChunkX = (x * strideValue) % ServerTerrainChunk.CHUNK_DIMENSION;
                int finalChunkZ = (z * strideValue) % ServerTerrainChunk.CHUNK_DIMENSION;

                if(finalWorldX > this.serverWorldData.getWorldSizeDiscrete() || finalWorldZ > this.serverWorldData.getWorldSizeDiscrete()){
                    throw new Error("Invalid world dim! " + finalWorldX + " " + finalWorldZ);
                }

                heightfield[x][z] = this.getMultisampleElevation(finalWorldX, finalWorldZ, finalChunkX, finalChunkZ);

                //calculate real pos
                double realX = ServerWorldData.convertVoxelToRealSpace(finalChunkX, finalWorldX);
                double realZ = ServerWorldData.convertVoxelToRealSpace(finalChunkZ, finalWorldZ);

                //clamped macro pos
                int macroDataScale = terrainModel.getMacroDataScale();
                double macroWorldPosX = ServerWorldData.convertWorldToReal(serverWorldData.clampWorldToMacro(finalWorldX));
                double macroWorldPosZ = ServerWorldData.convertWorldToReal(serverWorldData.clampWorldToMacro(finalWorldZ));
                double macroWidth = this.terrainModel.getMacroWidthInRealTerms();
                double percent1 = (realX - macroWorldPosX) / macroWidth;
                double percent2 = (realZ - macroWorldPosZ) / macroWidth;

                //solve dominant surface biome
                if(percent1 > 0.5){
                    if(percent2 > 0.5){
                        surfaceBiomeMap[x][z] = this.terrainModel.getMacroData(finalWorldX / macroDataScale + 1, finalWorldZ / macroDataScale + 1);
                    } else {
                        surfaceBiomeMap[x][z] = this.terrainModel.getMacroData(finalWorldX / macroDataScale + 1, finalWorldZ / macroDataScale);
                    }
                } else {
                    if(percent2 > 0.5){
                        surfaceBiomeMap[x][z] = this.terrainModel.getMacroData(finalWorldX / macroDataScale, finalWorldZ / macroDataScale + 1);
                    } else {
                        surfaceBiomeMap[x][z] = this.terrainModel.getMacroData(finalWorldX / macroDataScale, finalWorldZ / macroDataScale);
                    }
                }
            }
        }
    }

    /**
     * Calculates the surface noise of the surface at the provided position
     * @param worldX
     * @param worldZ
     * @param chunkX
     * @param chunkZ
     * @param strideValue
     * @param seed
     * @return
     */
    private double calculateSurfaceNoise(BiomeSurfaceGenerationParams surfaceParams, int worldX, int worldZ, int chunkX, int chunkZ, int strideValue, long seed){
        double realX = ServerWorldData.convertVoxelToRealSpace(chunkX, worldX);
        double realZ = ServerWorldData.convertVoxelToRealSpace(chunkZ, worldZ);
        float noiseScale = surfaceParams.getNoiseScale();
        float warpScale = surfaceParams.getWarpScale();

        double warpX = OpenSimplex2S.noise3_ImproveXY(seed, realX, realZ, 0);
        double warpZ = OpenSimplex2S.noise3_ImproveXY(seed, realX, realZ, 1);
        double valueRaw = OpenSimplex2S.noise2(seed, realX * noiseScale + warpX * warpScale, realZ * noiseScale + warpZ * warpScale);
        double fixed = (MathUtils.clamp(valueRaw, -1, 1) + 1) / 2.0;
        if(fixed < 0){
            throw new Error("Failed to clamp value properly! " + valueRaw + " " + fixed);
        }
        return fixed;
    }

    /**
     * Gets the elevation of a given position by sampling all four surrounding biome generators
     * @param finalWorldX The world x coordinate
     * @param finalWorldZ The world z coordinate
     * @param finalChunkX The chunk x coordinate
     * @param finalChunkZ The chunk z coordinate
     * @return The elevation of the world at that position
     */
    private double getMultisampleElevation(int finalWorldX, int finalWorldZ, int finalChunkX, int finalChunkZ){
        double rVal = 0;

        //biome of the current chunk
        double weight = 0;
        BiomeData surfaceBiome = null;
        BiomeSurfaceGenerationParams surfaceParams = null;
        HeightmapGenerator heightmapGen = null;

        //calculate real pos
        double realX = ServerWorldData.convertVoxelToRealSpace(finalChunkX, finalWorldX);
        double realZ = ServerWorldData.convertVoxelToRealSpace(finalChunkZ, finalWorldZ);

        //clamped macro pos
        int macroDataScale = terrainModel.getMacroDataScale();
        double macroWorldPosX = ServerWorldData.convertWorldToReal(serverWorldData.clampWorldToMacro(finalWorldX));
        double macroWorldPosZ = ServerWorldData.convertWorldToReal(serverWorldData.clampWorldToMacro(finalWorldZ));
        double macroWidth = this.terrainModel.getMacroWidthInRealTerms();
        double percent1 = (realX - macroWorldPosX) / macroWidth;
        double percent2 = (realZ - macroWorldPosZ) / macroWidth;

        //sample 1
        {
            weight = (1.0 - percent1) * (1.0 - percent2);
            surfaceBiome = this.terrainModel.getMacroData(finalWorldX / macroDataScale, finalWorldZ / macroDataScale);
            surfaceParams = surfaceBiome.getSurfaceGenerationParams();
            heightmapGen = this.tagHeightmapMap.get(surfaceParams.getSurfaceGenTag());
            if(heightmapGen == null){
                throw new Error("Undefined heightmap generator in biome! " + surfaceBiome.getId() + " " + surfaceBiome.getDisplayName() + " " + surfaceParams.getSurfaceGenTag());
            }

            rVal = rVal + heightmapGen.getHeight(
                this.terrainModel.getSeed(),
                realX,
                realZ
            ) * weight;
        }


        //sample 2
        {
            weight = percent1 * (1.0 - percent2);
            surfaceBiome = this.terrainModel.getMacroData(finalWorldX / macroDataScale + 1, finalWorldZ / macroDataScale);
            surfaceParams = surfaceBiome.getSurfaceGenerationParams();
            heightmapGen = this.tagHeightmapMap.get(surfaceParams.getSurfaceGenTag());
            if(heightmapGen == null){
                throw new Error("Undefined heightmap generator in biome! " + surfaceBiome.getId() + " " + surfaceBiome.getDisplayName() + " " + surfaceParams.getSurfaceGenTag());
            }

            rVal = rVal + heightmapGen.getHeight(
                this.terrainModel.getSeed(),
                realX,
                realZ
            ) * weight;
        }

        //sample 3
        {
            weight = (1.0 - percent1) * percent2;
            surfaceBiome = this.terrainModel.getMacroData(finalWorldX / macroDataScale, finalWorldZ / macroDataScale + 1);
            surfaceParams = surfaceBiome.getSurfaceGenerationParams();
            heightmapGen = this.tagHeightmapMap.get(surfaceParams.getSurfaceGenTag());
            if(heightmapGen == null){
                throw new Error("Undefined heightmap generator in biome! " + surfaceBiome.getId() + " " + surfaceBiome.getDisplayName() + " " + surfaceParams.getSurfaceGenTag());
            }

            rVal = rVal + heightmapGen.getHeight(
                this.terrainModel.getSeed(),
                realX,
                realZ
            ) * weight;
        }

        //sample 4
        {
            weight = percent1 * percent2;
            surfaceBiome = this.terrainModel.getMacroData(finalWorldX / macroDataScale + 1, finalWorldZ / macroDataScale + 1);
            surfaceParams = surfaceBiome.getSurfaceGenerationParams();
            heightmapGen = this.tagHeightmapMap.get(surfaceParams.getSurfaceGenTag());
            if(heightmapGen == null){
                throw new Error("Undefined heightmap generator in biome! " + surfaceBiome.getId() + " " + surfaceBiome.getDisplayName() + " " + surfaceParams.getSurfaceGenTag());
            }

            rVal = rVal+ heightmapGen.getHeight(
                this.terrainModel.getSeed(),
                realX,
                realZ
            ) * weight;
        }
        if(rVal < 0){
            rVal = 0;
        }

        return rVal;
    }

    @Override
    public double getElevation(int worldX, int worldZ, int chunkX, int chunkZ){
        return this.getMultisampleElevation(worldX,worldZ,chunkX,chunkZ);
    }

    @Override
    public void setModel(TerrainModel model) {
        this.terrainModel = model;
        for(HeightmapGenerator generator : this.tagHeightmapMap.values()){
            generator.setSeed(model.getSeed());
        }
        for(VoxelGenerator generator : this.tagVoxelMap.values()){
            generator.setSeed(model.getSeed());
        }
    }

}
