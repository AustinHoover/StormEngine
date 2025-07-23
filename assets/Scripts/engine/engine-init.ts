import { loggerScripts } from '/Scripts/compiler/host_access'
import { Client, NamespaceClient } from '/Scripts/client/client'
import { HookManager } from '/Scripts/engine/hooks/hook-manager'
import { SceneLoader } from '/Scripts/engine/scene/scene-loader'
import { Engine } from '/Scripts/types/engine'
import { clientHooks } from '/Scripts/client/clienthooks'
import { ChunkGeneratorManager } from '/Scripts/server/chunk/chunkgeneratormanager'
import { defaultPlayerState } from '/Scripts/client/player/player'

/**
 * The core engine values
 */
export const engine: Engine = {
    classes: {},
    singletons: {},
    hookManager: new HookManager(),
    sceneLoader: new SceneLoader(),
    chunkGeneratorManager: new ChunkGeneratorManager(),
    playerState: defaultPlayerState,
}

//store engine in globalThis
globalThis.engine = engine;

/**
 * Called when the script engine first initializes
 */
export const ENGINE_onInit = () => {
    loggerScripts.INFO('Script Engine Beginning Initialization')

    //load namespaces
    let client: NamespaceClient = Client
    engine.sceneLoader.engine = engine
    engine.hookManager.engine = engine
    engine.chunkGeneratorManager.engine = engine

    //load global hooks
    clientHooks.forEach(hook => {
        engine.hookManager.registerGlobalHook(hook,false)
    })

    loggerScripts.INFO('Script Engine Initialized')
}
