package electrosphere.server.macro.utils;

import org.joml.Vector3d;

import electrosphere.data.macro.struct.StructureData;
import electrosphere.server.macro.MacroData;

/**
 * Utilities for placing structures
 */
public class StructurePlacementUtils {
    
    /**
     * Gets an optimal position to place a structure
     * @param macroData The macro data
     * @param structureData The data for the structure
     * @param approxLocation The location to start searching from
     * @return The position
     */
    public static Vector3d getPlacementPosition(MacroData macroData, StructureData structureData, Vector3d approxLocation){
        return new Vector3d(approxLocation);
    }

}
