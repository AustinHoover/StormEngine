import { ClientPlayer } from "/Scripts/client/player/player";
import { HookManager } from "/Scripts/engine/hooks/hook-manager";
import { SceneLoader } from "/Scripts/engine/scene/scene-loader";
import { ChunkGeneratorManager } from "/Scripts/server/chunk/chunkgeneratormanager";
import { SingletonsMap } from "/Scripts/types/host/singletons";
import { StaticClasses } from "/Scripts/types/host/static-classes";


/**
 * The host context that contains all core engine functions
 */
export interface Engine {

    /**
     * The host's view of the scripting engine
     */
    readonly classes: StaticClasses,

    /**
     * The singletons available to the script engine
     */
    readonly singletons: SingletonsMap,

    /**
     * Manages all script-defined hooks in the engine
     */
    readonly hookManager: HookManager,
    
    /**
     * Tracks and loads scenes
     */
    readonly sceneLoader: SceneLoader,

    /**
     * The chunk generator manager
     */
    readonly chunkGeneratorManager: ChunkGeneratorManager,

    /**
     * State of the client player
     */
    playerState: ClientPlayer,

}

