import { Engine } from "/Scripts/types/engine";
import { Hook } from "/Scripts/types/hook";
import { Scene } from "/Scripts/types/scene";



/**
 * Loads scenes
 */
export class SceneLoader {


    
    /**
     * The parent engine object
     */
    engine: Engine

    /**
     * The list of loaded scenes
     */
    loadedScenes: Scene[] = [ ]

    /**
     * A record of tracked scene id to tracked scene object
     */
    sceneIdMap: Record<number,Scene> = { }

    /**
     * A scene 
     */
    sceneIdIncrementer: number = 0

    /**
     * Loads a scene
     * @param sceneName The scene to load
     * @returns The id assigned to the instance of the scene
     */
    loadScene(sceneName: string): number {
        //load and instantiate scene
        //@ts-ignore
        const SourceSceneClass = require(sceneName).default
        const sceneInstance: Scene = new SourceSceneClass()

        //creates an instance of the scene
        let sceneInstanceId: number = this.createInstance(sceneInstance,true)

        //call on init for scene
        if(sceneInstance.onCreate){
            sceneInstance.onCreate(sceneInstanceId)
        }

        return sceneInstanceId
    }

    /**
     * Registers all hooks in a scene to the hook manager
     * @param scene The scene
     * @returns The id assigned to the instance of the scene
     */
    createInstance(scene: Scene, isServerScene: boolean): number {
        //add to the list of tracked scenes
        const trackedScene: Scene = {
            instanceId: this.sceneIdIncrementer++,
            ...scene,
        }
        this.loadedScenes.push(trackedScene)
        this.sceneIdMap[trackedScene.instanceId] = trackedScene

        //load all hooks from the scene
        scene?.hooks?.forEach((hook: Hook) => {
            this.engine.hookManager.registerHook(trackedScene,hook,isServerScene)
        })

        return trackedScene.instanceId
    }

    /**
     * Deregisters all hooks in a scene from the hook manager
     * @param scene The scene
     */
    deregisterScene(scene: Scene){
        throw new Error("Unsupported Operation!")
    }

    /**
     * Gets a tracked scene by its id
     * @param sceneId The tracked scene
     * @returns The tracked scene if it exists, null otherwise
     */
    getScene(sceneId: number){
        return this.sceneIdMap[sceneId]
    }
    
}
