package electrosphere.server.physics.terrain.generation.voxelphase;

import electrosphere.data.biome.BiomeData;
import electrosphere.data.biome.BiomeSurfaceGenerationParams;
import electrosphere.engine.Globals;
import electrosphere.server.physics.terrain.generation.interfaces.GeneratedVoxel;
import electrosphere.server.physics.terrain.generation.interfaces.GenerationContext;

public class MountainVoxelGen  implements VoxelGenerator {

    /**
     * The width of the surface in number of voxels
     */
    public static final int SURFACE_VOXEL_WIDTH = 2;

    /**
     * Cutoff after which snow is placed
     */
    public static final int SNOW_CUTOFF = 150;

    /**
     * Gradient cutoff after which dirt is placed
     */
    public static final float GRADIENT_DIRT_CUTOFF = 0.1f;

    /**
     * The seed of the generator
     */
    long seed;

    @Override
    public String getTag(){
        return "mountains";
    }

    @Override
    public void setSeed(long seed){
        this.seed = seed;
    }

    @Override
    public void getVoxel(
        GeneratedVoxel voxel,
        int worldX, int worldY, int worldZ,
        int chunkX, int chunkY, int chunkZ,
        double realX, double realY, double realZ,
        int stride,
        double surfaceHeight, double surfaceGradient, double surfaceSelectionNoise,
        BiomeData surfaceBiome, BiomeSurfaceGenerationParams surfaceParams,
        GenerationContext generationContext
    ){
        Globals.profiler.beginAggregateCpuSample("HillsVoxelGen.getVoxel");

        double strideMultiplier = Math.pow(2,stride);
        double heightDiff = realY - surfaceHeight;
        double surfacePercent = HillsVoxelGen.getSurfaceWeight(surfaceHeight,realY,strideMultiplier);
        Globals.profiler.endCpuSample();
        if(heightDiff < -strideMultiplier * SURFACE_VOXEL_WIDTH){
            this.getSubsurfaceVoxel(
                voxel,
                worldX, worldY, worldZ,
                chunkX, chunkY, chunkZ,
                realX, realY, realZ,
                surfacePercent,
                surfaceHeight, surfaceGradient,
                surfaceBiome
            );
        } else if(heightDiff > 0) {
            this.getOverSurfaceVoxel(
                voxel,
                worldX, worldY, worldZ,
                chunkX, chunkY, chunkZ,
                realX, realY, realZ,-
                surfacePercent,
                surfaceHeight, surfaceGradient,
                surfaceBiome
            );
        } else {
            this.getSurfaceVoxel(
                voxel,
                worldX, worldY, worldZ,
                chunkX, chunkY, chunkZ,
                realX, realY, realZ,
                surfacePercent,
                surfaceHeight, surfaceGradient,
                surfaceBiome
            );
        }
    }

    /**
     * Gets the voxel on the surface
     * @return The voxel
     */
    private void getSurfaceVoxel(
        GeneratedVoxel voxel,
        int worldX, int worldY, int worldZ,
        int chunkX, int chunkY, int chunkZ,
        double realX, double realY, double realZ,
        double surfacePercent,
        double surfaceHeight, double surfaceGradient,
        BiomeData surfaceBiome
    ){
        voxel.weight = (float)surfacePercent * 2 - 1;
        voxel.type = 2;
        if(realY > SNOW_CUTOFF){
            voxel.type = 5;
        } else {
            if(surfaceGradient > GRADIENT_DIRT_CUTOFF){
                voxel.type = 1;
            }
        }
    }

    /**
     * Gets the voxel below the surface
     * @return The voxel
     */
    private void getSubsurfaceVoxel(
        GeneratedVoxel voxel,
        int worldX, int worldY, int worldZ,
        int chunkX, int chunkY, int chunkZ,
        double realX, double realY, double realZ,
        double surfacePercent,
        double surfaceHeight, double surfaceGradient,
        BiomeData surfaceBiome
    ){
        if(realY < surfaceHeight - 5){
            voxel.weight = 1;
            voxel.type = 6;
        } else {
            voxel.weight = 1;
            voxel.type = 1;
        }
    }

    /**
     * Gets the voxel above the service
     * @return The voxel
     */
    private void getOverSurfaceVoxel(
        GeneratedVoxel voxel,
        int worldX, int worldY, int worldZ,
        int chunkX, int chunkY, int chunkZ,
        double realX, double realY, double realZ,
        double surfacePercent,
        double surfaceHeight, double surfaceGradient,
        BiomeData surfaceBiome
    ){
        voxel.weight = -1;
        voxel.type = 0;
    }

    /**
     * Calculates the weight of a voxel on the surface based on the surface height, the position of the voxel, and the stride multiplier
     * @param surfaceHeight The surface height
     * @param realPosY The position of the voxel
     * @param strideMultiplier The stride multiplier
     * @return The weight of the voxel
     */
    protected static double getSurfaceWeight(double surfaceHeight, double realPosY, double strideMultiplier){
        return ((surfaceHeight - realPosY) / strideMultiplier);
    }
    
}
