/**
 * Utilities for ui menu interactions
 */
export interface MenuUtils {

    /**
     * Opens the voxel selection menu
     */
    readonly openVoxel: () => void

    /**
     * Opens the menu to select what to spawn
     */
    readonly openSpawnSelection: () => void

    /**
     * Opens the menu to select what fab to use
     */
    readonly openFabSelection: () => void

    /**
     * Opens the dialog specied at the given path
     * @param path The path to the dialog to open
     */
    readonly openDialog: (path: string) => void

}