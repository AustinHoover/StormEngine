import { Namespace } from "/Scripts/types/namespace";


/**
 * The client namespace type
 */
export interface NamespaceClient extends Namespace {
    // inventory: ClientInventory,
}

/**
 * The client namespace (should contain all client callbacks, data, etc)
 */
export const Client: NamespaceClient = {
    // inventory: {
    //     onMoveItemContainer: onMoveItemContainer,
    //     onEquipItem: onEquipItem,
    //     onUnequipItem: onUnequipItem,
    // },
}

