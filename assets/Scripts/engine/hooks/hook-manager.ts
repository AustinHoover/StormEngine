import { Engine } from "/Scripts/types/engine"
import { Hook } from "/Scripts/types/hook"
import { Scene } from "/Scripts/types/scene"
import { loggerScripts } from '/Scripts/compiler/host_access'

/**
 * The global scene id
 */
export const GLOBAL_SCENE_ID: number = -1

/**
 * The scope that the hook is firing from
 */
export enum HookScope {
    CORE,
    SCRIPT,
    SCENE_CLIENT,
    SCENE_SERVER,
}

/**
 * A hook that is tracked by the manager
 */
export interface TrackedHook extends Hook {

    /**
     * The scope that this hook was defined at
     */
    readonly scope: HookScope,

    /**
     * The scene that added the hook
     */
    readonly scene?: Scene,

}

/**
 * Manages hooks for the engine
 */
export class HookManager {

    /**
     * The list of all hooks currently tracked by this manager
     */
    readonly hooks: Array<TrackedHook> = []

    /**
     * A map of engine signal to the list of hooks that should be called when that signal fires
     */
    readonly signalHookMap: Record<string,Array<TrackedHook>> = { }

    /**
     * The list of all scenes tracked by the manager
     */
    readonly trackedScenes: Array<Scene> = []

    //The parent engine object
    engine: Engine

    /**
     * Registers a hook
     * @param scene The scene introducing the hook
     * @param hook The hook
     */
    registerHook(scene: Scene, hook: Hook, isServerScene: boolean){
        const trackedHook: TrackedHook = {
            ...hook,
            scope: isServerScene ? HookScope.SCENE_SERVER : HookScope.SCENE_CLIENT,
            scene: scene,
        }
        //add to flat array
        this.hooks.push(trackedHook)
        //add to signal array
        const hookSignal: string = hook.signal
        const signalArray: Array<TrackedHook> = this.signalHookMap?.[hookSignal] ? this.signalHookMap?.[hookSignal] : []
        signalArray.push(trackedHook)
        this.signalHookMap[hookSignal] = signalArray
        loggerScripts.DEBUG('register signal hook map')
        loggerScripts.DEBUG(hookSignal)
        loggerScripts.DEBUG(Object.keys(this.signalHookMap) + '')
        loggerScripts.DEBUG(this.signalHookMap[hookSignal] + '')
        //
        //Scene related structures
        //
        //track scene if it isn't already being tracked
        let filteredArr = this.trackedScenes.filter(trackedScene => trackedScene.instanceId === scene.instanceId)
        if(filteredArr.length < 1){
            this.trackedScenes.push(scene)
        }
        //add to scene tracking structures
        scene.sceneHooks.push(trackedHook)
        const sceneSignalArray: Array<TrackedHook> = scene.signalHookMap?.[hookSignal] ? scene.signalHookMap?.[hookSignal] : []
        sceneSignalArray.push(trackedHook)
        scene.signalHookMap[hookSignal] = sceneSignalArray
    }

    /**
     * Registers a global hook
     * @param hook The hook
     */
    registerGlobalHook(hook: Hook, isServerScene: boolean){
        const trackedHook: TrackedHook = {
            ...hook,
            scope: isServerScene ? HookScope.SCENE_SERVER : HookScope.SCENE_CLIENT,
        }
        //add to flat array
        this.hooks.push(trackedHook)
        //add to signal array
        const hookSignal: string = hook.signal
        const signalArray: Array<TrackedHook> = this.signalHookMap?.[hookSignal] ? this.signalHookMap?.[hookSignal] : []
        signalArray.push(trackedHook)
        this.signalHookMap[hookSignal] = signalArray
        loggerScripts.DEBUG('register signal hook map')
        loggerScripts.DEBUG(hookSignal)
        loggerScripts.DEBUG(Object.keys(this.signalHookMap) + '')
        loggerScripts.DEBUG(this.signalHookMap[hookSignal] + '')
    }

    /**
     * Deregisters a hook
     * @param scene The scene which introduced the hook
     * @param hook The hook
     */
    deregisterHook(scene: Scene, hook: Hook){
        throw new Error("Supported operation!")
    }

    /**
     * Fires a signal scoped to a specific scene instance
     * @param instanceId The scene instance id to fire the signal within
     * @param signal The signal
     * @param value The value associated with the signal
     */
    fireSignal(instanceId: number, signal: string, ...value: any){
        //parse from host
        const argsRaw: any[] = value?.[0]

        //try running global hooks for the signal
        const globalHooks: Array<TrackedHook> = this.signalHookMap[signal]
        if(!!globalHooks){
            globalHooks.forEach(trackedHook => {
                loggerScripts.DEBUG("Called global hook")
                trackedHook.callback(this.engine, ...argsRaw)
            })
        } else {
            //There isn't a hook registered for this signal at the global level
            loggerScripts.DEBUG("No global hooks for signal " + signal)
        }

        if(instanceId !== GLOBAL_SCENE_ID){
            //try firing at scene scope
            const scene: Scene = this.engine.sceneLoader.getScene(instanceId)
            const sceneHooks: Array<TrackedHook> = scene.signalHookMap[signal]
            if(!!sceneHooks){
                sceneHooks.forEach(trackeHook => {
                    loggerScripts.DEBUG("Called local hook")
                    trackeHook.callback(this.engine, ...argsRaw)
                })
            } else {
                //There isn't a hook registered for this signal at the scene level
                loggerScripts.DEBUG("No scene hooks for signal " + signal)
            }
        }
    }

}

