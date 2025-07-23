/**
 * Utilities for editing levels
 */
export interface ClientLevelEditorUtils {

    /**
     * Spawns the selected entity
     */
    readonly spawnEntity: () => void

    /**
     * Inspects the entity that the player's cursor is hovering over
     */
    readonly inspectEntity: () => void

}