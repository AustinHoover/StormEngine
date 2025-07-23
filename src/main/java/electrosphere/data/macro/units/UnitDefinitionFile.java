package electrosphere.data.macro.units;

import java.util.List;

/**
 * A file defining a whole bunch of units
 */
public class UnitDefinitionFile {
    
    /**
     * The unit definitions contained in this file
     */
    List<UnitDefinition> units;

    /**
     * Gets the units contained in this file
     * @return The list of units
     */
    public List<UnitDefinition> getUnits(){
        return units;
    }

}
