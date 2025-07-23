

/**
 * Overall state of the player's controls
 */
export class PlayerControlState {
    /**
     * State of the room tool
     */
    roomTool: RoomToolState = new RoomToolState()

}

/**
 * State for the room tool
 */
export class RoomToolState {
    /**
     * The currently selected functionality of the room tool
     */
    currentState: 'SelectRoom' | 'ShowRoom' | 'SelectFurniture' | 'ShowFurniture' | 'DetectRoom' = 'SelectRoom'

    /**
     * Updates the state of the room tool
     * @param value The new value
     */
    updateState(value: 'SelectRoom' | 'ShowRoom' | 'SelectFurniture' | 'ShowFurniture' | 'DetectRoom'){
        this.currentState = value
    }
}

/**
 * Overall state for the client player
 */
export interface ClientPlayer {
    /**
     * State of controls for the player
     */
    controlState: PlayerControlState,
}

/**
 * Actual player control state
 */
export const defaultPlayerState: ClientPlayer = {
    controlState: new PlayerControlState()
}