package electrosphere.data.biome;

import java.util.List;

/**
 * A region type that can generate inside a biome
 * Examples:
 * A field
 * A natural trail
 * A grove of trees
 * A field of stalactites
 * 
 * The idea of having regions is to allow spatially isolating different generation components within a larger biome.
 * The prime example of this is generating a large tree within a larger forest biome.
 * You might want to spatially separate the tree so that you can apply special generation rules around it in particular.
 * IE, generate roots, but only around the tree.
 */
public class BiomeRegion {
    
    /**
     * The frequency of this region within the biome
     */
    Double frequency;

    /**
     * The base floor voxel
     * This is populated by default, then overridden if any of the floor variants trigger/supercede it
     */
    Integer baseFloorVoxel;

    /**
     * The different floor elements
     */
    List<BiomeFloorElement> floorVariants;

    /**
     * The list of foliage descriptions available to this biome type
     */
    List<BiomeFoliageDescription> foliageDescriptions;

    

}
