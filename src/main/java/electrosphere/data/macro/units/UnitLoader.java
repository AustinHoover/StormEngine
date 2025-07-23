package electrosphere.data.macro.units;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads the unit definition and provides utilities for extracting specific units
 */
public class UnitLoader {
    
    /**
     * Map of unit id -> unit data
     */
    Map<String,UnitDefinition> unitMap = new HashMap<String,UnitDefinition>();

    /**
     * Creates a unit loader from a raw definition file
     * @param rawData The raw file
     * @return The unit loader
     */
    public static UnitLoader create(UnitDefinitionFile rawData){
        UnitLoader rVal = new UnitLoader();
        for(UnitDefinition unit : rawData.getUnits()){
            rVal.putUnit(unit);
        }
        return rVal;
    }

    /**
     * Adds a unit to the loader
     * @param unit The unit
     */
    public void putUnit(UnitDefinition unit){
        unitMap.put(unit.getId(),unit);
    }

    /**
     * Gets a unit by its id
     * @param unitId The id of the unit
     * @return The unit if it exists, null otherwise
     */
    public UnitDefinition getUnit(String unitId){
        return unitMap.get(unitId);
    }

    /**
     * Gets the collection of all units
     * @return The collection of all units
     */
    public Collection<UnitDefinition> getUnits(){
        return unitMap.values();
    }

}
