import { TestGen } from "/Scripts/server/chunk/generators/testgen"
import { Engine } from "/Scripts/types/engine"
import { ChunkGenerator, VoxelFunction } from "/Scripts/types/host/server/chunk/chunkgenerator"



/**
 * Manages all the chunk generators defined script-side
 */
export class ChunkGeneratorManager {

    /**
     * The parent engine object
     */
    engine: Engine

    /**
     * The list of registered chunk generators
     */
    readonly registeredGenerators: ChunkGenerator[] = [
        TestGen,
    ]

    /**
     * Gets the voxel function for the tag
     * @param tag The tag
     * @returns The voxel function if it exists, null otherwise
     */
    readonly getVoxelFunction = (tag: string): VoxelFunction => {
        let rVal: VoxelFunction = null
        this.registeredGenerators.forEach(generator => {
            if(generator.getTag() === tag){
                rVal = generator.getVoxelFunction(this.engine)
            }
        })
        return rVal
    }

}
