package electrosphere.data.entity.creature;

import java.util.List;

import electrosphere.data.entity.common.CommonEntityType;
import electrosphere.data.entity.creature.visualattribute.VisualAttribute;

/**
 * A given type of creature
 */
public class CreatureData extends CommonEntityType {

    

    /**
     * The visual attributes that can be configured on this creature type
     */
    List<VisualAttribute> visualAttributes;


    

    /**
     * Gets the configurable visual attributes for this creature type
     * @return The list of visual attribute data
     */
    public List<VisualAttribute> getVisualAttributes(){
        return visualAttributes;
    }

    
    
    
}
