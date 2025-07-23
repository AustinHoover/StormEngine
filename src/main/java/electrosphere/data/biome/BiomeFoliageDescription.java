package electrosphere.data.biome;

import java.util.List;

import org.graalvm.polyglot.HostAccess.Export;

/**
 * Describes behavior for spawning a specific type of foliage in the biome
 */
public class BiomeFoliageDescription {
    
    /**
     * The list of entity IDs of this foliage type in particular
     */
    @Export
    List<String> entityIDs;

    /**
     * How regular the placement of foliage is. Low values will create very uneven foliage, while high values will place them along a grid.
     */
    @Export
    Double regularity;

    /**
     * The percentage of the ground to cover with foliage
     */
    @Export
    Double threshold;

    /**
     * The priority of this floor element in particular
     */
    @Export
    Double priority;

    /**
     * The scale of the noise used to place foliage
     */
    @Export
    Double scale;

    /**
     * Gets the entity ids for this foliage description
     * @return The list of entity ids
     */
    public List<String> getEntityIDs(){
        return this.entityIDs;
    }

    /**
     * Gets the regularity of the foliage placement
     * @return The regularity
     */
    public Double getRegularity(){
        return regularity;
    }

    /**
     * Gets the percentage of the ground to cover with foliage
     * @return The percentage of the ground to cover with foliage
     */
    public Double getThreshold(){
        return threshold;
    }

    /**
     * Gets the priority of this floor element in particular
     * @return The priority of this floor element in particular
     */
    public Double getPriority(){
        return priority;
    }

    /**
     * Gets the scale of the noise used to place foliage
     * @return The scale of the noise used to place foliage
     */
    public Double getScale(){
        return scale;
    }

}
