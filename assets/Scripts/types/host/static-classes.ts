import { ClientAreaUtils } from "/Scripts/types/host/client/client-area-utils";
import { ClientLevelEditorUtils } from "/Scripts/types/host/client/client-level-editor-utils";
import { ClientVoxelUtils } from "/Scripts/types/host/client/client-voxel-utils";
import { Entity } from "/Scripts/types/host/entity/entity";
import { MenuUtils } from "/Scripts/types/host/renderer/ui/menus";
import { TutorialUtils } from "/Scripts/types/host/renderer/ui/tutorial";
import { Vector } from "/Scripts/types/spatial";


/**
 * Static classes provided to the script environment
 */
export interface StaticClasses {

    /**
     * Utility functions for doing math
     */
    readonly mathUtils?: any,

    /**
     * Simulation processing related functions
     */
    readonly simulation?: Class<SimulationClass>,

    /**
     * Tutorial ui utils
     */
    readonly tutorialUtils?: Class<TutorialUtils>,

    /**
     * Utilities for performing actions on the server
     */
    readonly serverUtils?: Class<ServerUtils>,

    /**
     * Utilities for interacting with menus on client
     */
    readonly menuUtils?: Class<MenuUtils>,

    /**
     * Utilities for interacting with voxels on the client
     */
    readonly voxelUtils?: Class<ClientVoxelUtils>,

    /**
     * Utilities for level editing
     */
    readonly levelEditorUtils?: Class<ClientLevelEditorUtils>,

    /**
     * Math functions
     */
    readonly math?: Class<Math>

    /**
     * Utilities for area management on the client
     */
    readonly areaUtils?: Class<ClientAreaUtils>,

}

/**
 * A class file
 */
export interface Class<T> {
    /**
     * The static values of the class
     */
    static: T,
}

/**
 * The simulation processing class
 */
export interface SimulationClass {

    /**
     * Sets the simulation frame count
     * 0 - Do not simulate any frames
     * 1 - Simulate a single frame
     * 2+ - Simulate indefinitely
     * Values lower than 0 are ignored
     * @param value The frame count
     */
    readonly setFramestep: (value: number) => void,

}

/**
 * Utilities for core functionality on the server
 */
export interface ServerUtils {

    /**
     * Spawns a creature
     * @param creatureType The type of creature 
     * @returns The entity created on the server
     */
    readonly spawnCreature: (sceneInstanceId: number, creatureType: string, position: Vector) => Entity

    /**
     * Gets the position of an entity
     * @param entity The entity
     * @returns The position
     */
    readonly getPosition: (entity: Entity) => Vector

    /**
     * Sets the position of an entity
     * @param entity The entity
     * @param position The position
     */
    readonly setPosition: (entity: Entity, position: Vector) => void

}
