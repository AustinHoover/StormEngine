package electrosphere.server.macro;

import java.io.File;

import electrosphere.data.block.fab.BlockFab;
import electrosphere.engine.Globals;
import electrosphere.server.macro.structure.VirtualStructure;
import electrosphere.util.FileUtils;

/**
 * Loads macro data
 */
public class MacroDataLoader {
    
    /**
     * Loads macro data from a save
     * @param saveName The name of the save
     * @return The macro data
     */
    public static MacroData loadFromSave(String saveName){
        
        MacroData rVal = FileUtils.loadObjectFromSavePath(saveName, "macro.json", MacroData.class);

        //preload and assign structure fabs
        for(VirtualStructure structure : rVal.getStructures()){
            File fabFile = FileUtils.getAssetFile(structure.getFabPath());
            if(!fabFile.exists()){
                throw new Error("Failed to locate structure that does not exist! " + fabFile.getAbsolutePath());
            }
            BlockFab fab = BlockFab.read(fabFile);
            if(fab == null){
                throw new Error("Failed to read fab!");
            }
            structure.setFab(fab);
        }

        //rebuild datastructures
        rVal.rebuildDatastructures();

        return rVal;
    }

    /**
     * Loads all alive characters
     */
    public static void loadAllAliveCharacters(){
        Globals.profiler.beginCpuSample("MacroDataLoader.loadAllAliveCharacters");
        Globals.serverState.characterService.getAllCharacters();
        Globals.profiler.endCpuSample();
    }

}
