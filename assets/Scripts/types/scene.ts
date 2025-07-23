import { TrackedHook } from "/Scripts/engine/hooks/hook-manager";
import { Hook } from "/Scripts/types/hook";
import { Namespace } from "/Scripts/types/namespace";

/**
 * A value that is synchronized between client and server
 */
export function SynchronizedType() {
    //called when the synchronized type is evaluated
    return function (target: any, propertyKey: string, descriptor: PropertyDescriptor){
        //called when 
    }
}

/**
 * The namespace of a given scene
 */
export class Scene implements Namespace {

    /**
     * The instance id of this scene
     */
    readonly instanceId?: number

    /**
     * Values that are synchronized between the client and server. They are also stored to disk when the scene saves
     */
    persistentValues: Record<string,any> = { }

    /**
     * The hooks that are provided by this scene
     */
    readonly hooks: Array<Hook> = [ ]

    /**
     * Invoked when the scene is created
     */
    readonly onCreate?: (instanceId: number) => void

    /**
     * The map of signal name -> array of hooks created by this scene to fire on that signal
     */
    readonly signalHookMap: Record<string,Array<TrackedHook>> = { }

    /**
     * Internal use
     */
    readonly sceneHooks: Array<TrackedHook> = [ ]

}
