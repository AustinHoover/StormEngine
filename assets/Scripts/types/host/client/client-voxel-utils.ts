/**
 * Utilities for interating with voxels on the client
 */
export interface ClientVoxelUtils {

    /**
     * Applies the current voxel palette where the player's cursor is looking
     */
    readonly applyEdit: () => void

    /**
     * Spawns water at the player's cursor
     */
    readonly spawnWater: () => void

    /**
     * Tries to dig with whatever tool is equipped
     */
    readonly dig: () => void

    /**
     * Places the currently selected fab
     */
    readonly placeFab: () => void

}