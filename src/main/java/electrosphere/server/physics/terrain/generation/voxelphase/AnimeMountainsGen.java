package electrosphere.server.physics.terrain.generation.voxelphase;

import electrosphere.data.biome.BiomeData;
import electrosphere.data.biome.BiomeSurfaceGenerationParams;
import electrosphere.engine.Globals;
import electrosphere.server.physics.terrain.generation.interfaces.GeneratedVoxel;
import electrosphere.server.physics.terrain.generation.interfaces.GenerationContext;
import io.github.studiorailgun.MathUtils;
import io.github.studiorailgun.RandUtils;

/**
 * Generates anime-style mountains
 */
public class AnimeMountainsGen implements VoxelGenerator {

    /**
     * The width of the surface in number of voxels
     */
    public static final int SURFACE_VOXEL_WIDTH = 2;

    /**
     * Size of the large cells that generate mountains
     */
    public static final int LARGE_CELL_SIZE = 1024;

    /**
     * How much to sink the cell into the surface
     */
    public static final int CELL_VERTICAL_OFFSET = - (int)((LARGE_CELL_SIZE) * 2.5 / 5.0);

    /**
     * The variance in scale of mountains
     */
    public static final double MOUNTAIN_SCALE_VARIANCE = 0.2;

    /**
     * The width of the mountain
     */
    public static final double MOUNTAIN_WIDTH = 0.4;

    /**
     * The center x point of the cell
     */
    public static final double CELL_CENTER_X = 0.5;

    /**
     * The center y point of the cell
     */
    public static final double CELL_CENTER_Y = 0.5;

    /**
     * The center z point of the cell
     */
    public static final double CELL_CENTER_Z = 0.5;

    /**
     * Amount the vertical rotation offset can vary
     */
    public static final double VERTICAL_ROTATION_OFFSET_VARIANCE = 0.2;

    /**
     * The seed for the generator
     */
    long seed = 0;

    @Override
    public String getTag() {
        return "animeMountain";
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
        int stride, double surfaceHeight, double surfaceGradient, double surfaceSelectionNoise,
        BiomeData surfaceBiome, BiomeSurfaceGenerationParams surfaceParams,
        GenerationContext generationContext
    ) {
        Globals.profiler.beginAggregateCpuSample("AnimeMountainsGen.getVoxel");

        double strideMultiplier = Math.pow(2,stride);
        double heightDiff = realY - surfaceHeight;
        double surfacePercent = AnimeMountainsGen.getSurfaceWeight(surfaceHeight,realY,strideMultiplier);
        Globals.profiler.endCpuSample();
        if(heightDiff < -strideMultiplier * SURFACE_VOXEL_WIDTH){
            this.getSubsurfaceVoxel(
                voxel,
                worldX, worldY, worldZ,
                chunkX, chunkY, chunkZ,
                realX, realY, realZ,
                surfacePercent,
                surfaceHeight,
                surfaceBiome
            );
        } else if(heightDiff > 0) {
            this.getOverSurfaceVoxel(
                voxel,
                worldX, worldY, worldZ,
                chunkX, chunkY, chunkZ,
                realX, realY, realZ,-
                surfacePercent,
                surfaceHeight,
                surfaceBiome
            );
        } else {
            this.getSurfaceVoxel(
                voxel,
                worldX, worldY, worldZ,
                chunkX, chunkY, chunkZ,
                realX, realY, realZ,
                surfacePercent,
                surfaceHeight,
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
        double surfaceHeight,
        BiomeData surfaceBiome
    ){
        voxel.weight = (float)surfacePercent * 2 - 1;
        voxel.type = 2;
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
        double surfaceHeight,
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
        double surfaceHeight,
        BiomeData surfaceBiome
    ){
        //default voxel value
        voxel.weight = -1;
        voxel.type = 0;

        //get the height above the surface
        double heightAboveBaseSurface = realY - surfaceBiome.getSurfaceGenerationParams().getHeightOffset();
        double offsetHeight = heightAboveBaseSurface - CELL_VERTICAL_OFFSET;

        //calculated floored values
        double x_i = Math.floor(realX / LARGE_CELL_SIZE);
        double y_i = Math.floor(offsetHeight / LARGE_CELL_SIZE);
        double z_i = Math.floor(realZ / LARGE_CELL_SIZE);

        //only generate if you're on the first cell from the surface
        if(y_i < 1){
            //calculate values from which cell we're in
            double rotationTheta = RandUtils.rand(x_i, z_i, 0) * Math.PI * 2;
            // double mountainScale = MathUtils.rand(x_i, z_i, 1) * MOUNTAIN_SCALE_VARIANCE + (1.0 - (MOUNTAIN_SCALE_VARIANCE / 2.0));
            // double verticalRotationOffset = MathUtils.rand(x_i, z_i, 2) * VERTICAL_ROTATION_OFFSET_VARIANCE;

            //remainders of the point coordinates
            double x_r = (realX / LARGE_CELL_SIZE) - x_i;
            double y_r = (offsetHeight / LARGE_CELL_SIZE) - y_i;
            double z_r = (realZ / LARGE_CELL_SIZE) - z_i;

            //get the center of the cell
            double cellCenterX = CELL_CENTER_X;
            double cellCenterY = CELL_CENTER_Y;
            double cellCenterZ = CELL_CENTER_Z;

            //delta positions
            double deltaX = (x_r - cellCenterX);
            double deltaY = (y_r - cellCenterY);
            double deltaZ = (z_r - cellCenterZ);

            //rotate around the center
            double rotX = deltaX * Math.cos(rotationTheta) + deltaZ * Math.sin(rotationTheta);
            double rotY = deltaY;
            double rotZ = - deltaX * Math.sin(rotationTheta) + deltaZ * Math.cos(rotationTheta);

            //roation along the arctre
            //ranged [0,2PI]
            double rotationAlongArc = Math.atan2(rotY,rotX);
            //ranges [0,1]
            // double rotationPercent = rotationAlongArc / (Math.PI * 2.0);


            //calculate values from where we are WITHIN the cell
            // double voxelGamma = Math.atan2(
            //     x_r - cellCenterX,
            //     z_r - cellCenterZ
            // ) + Math.PI * 2.0;
            // double voxelTheta = Math.atan2(y_r - cellCenterY, MathUtils.dist(x_r,z_r,cellCenterX,cellCenterZ));
            double distanceFromCenter = MathUtils.dist(rotX,rotY,0,0);
            double distanceFromArcCenter = Math.sqrt(rotZ*rotZ);

            //target distance from center
            double targetDistanceFromArcCenter = Math.sin(rotationAlongArc) * 0.3;
            double targetArcRadius = 0.6;
            double targetArcRadiusWidth = Math.sin(rotationAlongArc) * 0.03;
            

            if(  
                // voxelTheta > 0 &&
                // (voxelGamma - Math.PI/2) < Math.PI &&
                // voxelTheta > Math.max(Math.sin(voxelGamma),0) &&
                // voxelTheta > 0.4 * Math.max(Math.sin(voxelGamma),0) &&
                distanceFromCenter > targetArcRadius - targetArcRadiusWidth &&
                distanceFromCenter < targetArcRadius + targetArcRadiusWidth &&
                distanceFromArcCenter < targetDistanceFromArcCenter
            ){
                voxel.weight = 1.0f;
                voxel.type = 1;
            }
        }

    }

    /**
     * Calculates the weight of a voxel on the surface based on the surface height, the position of the voxel, and the stride multiplier
     * @param surfaceHeight The surface height
     * @param realPosY The position of the voxel
     * @param strideMultiplier The stride multiplier
     * @return The weight of the voxel
     */
    private static double getSurfaceWeight(double surfaceHeight, double realPosY, double strideMultiplier){
        return ((surfaceHeight - realPosY) / strideMultiplier);
    }
    
}
