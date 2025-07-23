import { Vector } from "/Scripts/types/spatial";

/**
 * An area selection
 */
export interface AreaSelection {
    getType: () => string,
    /**
     * Gets the start rectangle of the area
     */
    getRectStart: () => Vector,
    /**
     * Gets the end rectangle of the area
     */
    getRectEnd: () => Vector,
}

/**
 * Utilities for managing areas on the client
 */
export interface ClientAreaUtils {

    /**
     * Selects a rectangular area
     */
    readonly selectAreaRectangular: () => AreaSelection

    /**
     * Makes a selection visible
     * @param selection The selection
     */
    readonly makeSelectionVisible: (selection: AreaSelection) => void

}