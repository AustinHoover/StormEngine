import { engine } from "/Scripts/engine/engine-init";
import { Engine } from "/Scripts/types/engine";
import { Scene } from "/Scripts/types/scene";
import { Vector } from "/Scripts/types/spatial";

/**
 * The main scene interface
 */
class TestScene1 extends Scene {
    
    /**
     * Called when the scene is created
     * @param instanceId The scene instanceId
     */
    onCreate = (instanceId: number) => {
        console.log('Hello from the scene! My ID is ' + instanceId)
        console.log(Object.keys(this))
    }

    /**
     * All hooks for the scene
     */
    hooks = [
        
        /**
         * Equip item hook
         */
        {
            signal: "equipItem",
            callback: (engine: Engine) => {
                // throw tutorial message
                engine.classes.simulation.static.setFramestep(0)
                engine.classes.tutorialUtils.static.showTutorialHint(
                    "EquippingItems",
                    true,
                    () => {
                        engine.classes.simulation.static.setFramestep(2)
                    }
                )
            }
        },

        /**
         * Move hook
         */
        {
            signal: "entityGroundMove",
            callback: (engine: Engine, newPos: Vector) => {
                // console.log("Entity moved " + entityId + " to " + Vector.toString(newPos))
            }
        },

        /**
         * Storing an item in inventory
         */
        {
            signal: "itemPickup",
            callback: (engine: Engine, inWorldItemEntityId: number, inInventoryItemEntityId: number) => {
                // throw tutorial message
                engine.classes.simulation.static.setFramestep(0)
                engine.classes.tutorialUtils.static.showTutorialHint(
                    "GrabbingItems",
                    true,
                    () => {
                        engine.classes.simulation.static.setFramestep(2)
                    }
                )
            }
        },

    ]

}

/**
 * The scene to export
 */
export default TestScene1
