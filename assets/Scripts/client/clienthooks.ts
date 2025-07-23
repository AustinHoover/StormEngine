import { clientUIButtonHook } from "/Scripts/client/uihooks";
import { Engine } from "/Scripts/types/engine";
import { Hook } from "/Scripts/types/hook";
import { AreaSelection } from "/Scripts/types/host/client/client-area-utils";

/**
 * The client-wide hooks
 */
export const clientHooks: Hook[] = [
    {
        signal: "OPEN_VOXEL",
        callback: (engine: Engine) => {
            engine.classes.menuUtils.static.openVoxel()
        }
    },
    {
        signal: "ADD_VOXEL",
        callback: (engine: Engine) => {
            engine.classes.voxelUtils.static.applyEdit()
        }
    },
    {
        signal: "OPEN_SPAWN_SELECT",
        callback: (engine: Engine) => {
            engine.classes.menuUtils.static.openSpawnSelection()
        }
    },
    {
        signal: "LEVEL_EDIT_SPAWN",
        callback: (engine: Engine) => {
            engine.classes.levelEditorUtils.static.spawnEntity()
        }
    },
    {
        signal: "INSPECTOR",
        callback: (engine: Engine) => {
            engine.classes.levelEditorUtils.static.inspectEntity()
        }
    },
    {
        signal: "SPAWN_WATER",
        callback: (engine: Engine) => {
            engine.classes.voxelUtils.static.spawnWater()
        }
    },
    {
        signal: "DIG",
        callback: (engine: Engine) => {
            engine.classes.voxelUtils.static.dig()
        }
    },
    {
        signal: "SELECT_FAB",
        callback: (engine: Engine) => {
            engine.classes.menuUtils.static.openFabSelection()
        }
    },
    {
        signal: "PLACE_FAB",
        callback: (engine: Engine) => {
            engine.classes.voxelUtils.static.placeFab()
        }
    },
    {
        signal: "ROOM_TOOL_ACTION",
        callback: (engine: Engine) => {
            switch(engine.playerState.controlState.roomTool.currentState){
                case 'DetectRoom': {
                } break;
                case 'SelectFurniture': {
                } break;
                case 'SelectRoom': {
                    const areaSelection: AreaSelection = engine.classes.areaUtils.static.selectAreaRectangular()
                    console.log(areaSelection.getRectStart())
                    console.log(JSON.stringify(areaSelection.getRectStart()))
                    engine.classes.areaUtils.static.makeSelectionVisible(areaSelection)
                } break;
                case 'ShowFurniture': {
                } break;
                case 'ShowRoom': {
                } break;
            }
        }
    },
    {
        signal: "ROOM_TOOL_MENU",
        callback: (engine: Engine) => {
            engine.classes.menuUtils.static.openDialog('Data/menu/room/roomToolConfig.html')
        }
    },
    clientUIButtonHook,
]