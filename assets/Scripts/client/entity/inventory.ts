import { Entity } from "/Scripts/types/host/entity/entity"


/**
 * A type of entity
 */
export type Item = Entity

/**
 * An inventory
 */
export interface Inventory {
    
}


/**
 * Fires any time an item changes containers
 */
export const SIGNAL_MOVE_ITEM: string = "moveItem"

/**
 * A callback that fires when an item is moved from one container to another
 */
export type MoveItemCallback = (item: Item) => void

