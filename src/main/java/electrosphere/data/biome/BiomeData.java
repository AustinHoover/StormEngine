package electrosphere.data.biome;

import java.util.List;

import org.graalvm.polyglot.HostAccess.Export;

/**
 * Data about a given biome
 */
public class BiomeData {
    
    /**
     * The id of the biome
     */
    @Export
    String id;

    /**
     * The display name of the biome
     */
    @Export
    String displayName;

    /**
     * The regions available to the biome
     */
    List<BiomeRegion> regions;

    /**
     * True if this region applies above the surface
     */
    @Export
    Boolean isAerial;

    /**
     * True if this region applies to the surface
     */
    @Export
    Boolean isSurface;

    /**
     * True if this region applies below the surface
     */
    @Export
    Boolean isSubterranean;

    /**
     * The surface generation params
     */
    @Export
    BiomeSurfaceGenerationParams surfaceGenerationParams;

    

    /**
     * Gets the id of the biome
     * @return The id of the biome
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the display name of the biome
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the regions of the biome
     * @return The regions
     */
    public List<BiomeRegion> getRegions(){
        return regions;
    }

    /**
     * Gets whether the biome is a surface biome or not
     * @return true if is a surface biome, false otherwise
     */
    public Boolean isSurface(){
        return isSurface;
    }

    /**
     * Gets whether the biome is an aerial biome or not
     * @return true if is an aerial biome, false otherwise
     */
    public Boolean isAerial(){
        return isAerial;
    }

    /**
     * Gets whether the biome is a subterreanean biome or not
     * @return true if is a subterreanean biome, false otherwise
     */
    public Boolean isSubterranean(){
        return isSubterranean;
    }

    /**
     * Gets the surface generation params
     * @return The surface generation params
     */
    public BiomeSurfaceGenerationParams getSurfaceGenerationParams(){
        return surfaceGenerationParams;
    }

}
