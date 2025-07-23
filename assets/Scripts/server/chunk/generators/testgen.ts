import { Engine } from "/Scripts/types/engine";
import { CHUNK_WIDTH, ChunkGenerator, GeneratedVoxel, VoxelFunction } from "/Scripts/types/host/server/chunk/chunkgenerator";
import { BiomeData } from "/Scripts/types/host/server/data/biomedata";


/**
 * Converts a chunk coordinate to a real coordinate
 * @param chunk The chunk pos
 * @param world The world pos
 * @returns The real pos
 */
const voxelToReal = (chunk: number, world: number) => {
    return world * CHUNK_WIDTH + chunk
}

/**
     * Gets the voxel on the surface
     * @return The voxel
     */
const getSurfaceVoxel = (
    voxel: GeneratedVoxel,
    worldX: number, worldY: number, worldZ: number,
    chunkX: number, chunkY: number, chunkZ: number,
    realX: number, realY: number, realZ: number,
    surfacePercent: number,
    surfaceHeight: number,
    surfaceBiome: BiomeData
) => {
    voxel.weight = surfacePercent * 2 - 1;
    voxel.type = 2;
    return voxel;
}

/**
 * Gets the voxel below the surface
 * @return The voxel
 */
const getSubsurfaceVoxel = (
    voxel: GeneratedVoxel,
    worldX: number, worldY: number, worldZ: number,
    chunkX: number, chunkY: number, chunkZ: number,
    realX: number, realY: number, realZ: number,
    surfacePercent: number,
    surfaceHeight: number,
    surfaceBiome: BiomeData
) => {
    if(realY < surfaceHeight - 5){
        voxel.weight = 1;
        voxel.type = 6;
    } else {
        voxel.weight = 1;
        voxel.type = 1;
    }
    return voxel;
}

/**
 * Gets the voxel above the service
 * @return The voxel
 */
const getOverSurfaceVoxel = (
    voxel: GeneratedVoxel,
    worldX: number, worldY: number, worldZ: number,
    chunkX: number, chunkY: number, chunkZ: number,
    realX: number, realY: number, realZ: number,
    surfacePercent: number,
    surfaceHeight: number,
    surfaceBiome: BiomeData
) => {
    voxel.weight = -1;
    voxel.type = 0;
    return voxel;
}

/**
 * A test generator
 */
export const TestGen: ChunkGenerator = {

    /**
     * Gets the tag for this generator
     * @returns The tag
     */
    getTag: () => "test",

    /**
     * The elevation function
     * @param worldX The world x coordinate
     * @param worldZ The world z coordinate
     * @param chunkX The chunk x coordinate
     * @param chunkZ The chunk z coordinate
     */
    getElevation: (worldX: number, worldZ: number, chunkX: number, chunkZ: number): number => {
        return 1
    },

    /**
     * Gets the function to actually get voxels
     * @param engine The engine reference
     */
    getVoxelFunction: (
        engine: Engine
     ): VoxelFunction => {
        const rVal = (
            voxel: GeneratedVoxel,
            worldX: number, worldY: number, worldZ: number,
            chunkX: number, chunkY: number, chunkZ: number,
            stride: number,
            surfaceHeight: number,
            surfaceBiome: BiomeData
        ): void => {

            const realX = voxelToReal(chunkX,worldX)
            const realY = voxelToReal(chunkY,worldY)
            const realZ = voxelToReal(chunkZ,worldZ)
            const strideMultiplier = engine.classes.math.static.pow(2,stride)
            // const strideMultiplier = 1
            const heightDiff = realY - surfaceHeight
            const surfacePercent = (surfaceHeight - realY) / strideMultiplier

            if(heightDiff < -strideMultiplier){
                getSubsurfaceVoxel(
                    voxel,
                    worldX, worldY, worldZ,
                    chunkX, chunkY, chunkZ,
                    realX, realY, realZ,
                    surfacePercent,
                    surfaceHeight,
                    surfaceBiome
                );
            } else if(heightDiff > 0) {
                getOverSurfaceVoxel(
                    voxel,
                    worldX, worldY, worldZ,
                    chunkX, chunkY, chunkZ,
                    realX, realY, realZ,-
                    surfacePercent,
                    surfaceHeight,
                    surfaceBiome
                );
            } else {
                getSurfaceVoxel(
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
        return rVal
    },

}