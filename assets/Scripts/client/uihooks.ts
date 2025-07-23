import { GLOBAL_SCENE_ID } from "/Scripts/engine/hooks/hook-manager";
import { Engine } from "/Scripts/types/engine";
import { Hook } from "/Scripts/types/hook";

/**
 * The hook that fires every time a dynamic button in the ui is clicked
 */
export const clientUIButtonHook: Hook =
{
    signal: "uiButton",
    callback: (engine: Engine, data: string) => {
        if(data.length > 4 && data.substring(0,9) === "openDiag("){
            engine.classes.menuUtils.static.openDialog(data.substring(9,data.length - 1))
        } else if(data.length > 4 && data.substring(0,5) === "hook("){
            engine.hookManager.fireSignal(GLOBAL_SCENE_ID,data.substring(5,data.length - 1))
        } else {
            console.log("button clicked " + data)
        }
    }
}