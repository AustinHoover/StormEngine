

/**
 * Describes a type of foliage that can be generated in a biome
 */
export interface BiomeFoliageDescription {

    /**
     * The list of entity IDs in the type
     */
    entityIDs: string[],

    /**
     * How regular the placement of the foliage is.
     * Low values make the placement more random.
     * High values make the placement more orderly (aligned with a grid).
     */
    regularity: number,

    /**
     * The percentage of the ground to cover with foliage
     */
    threshold: number,

    /**
     * The priority of this type in particular
     */
    priority: number,

    /**
     * The scale of the noise used to place foliage
     */
    scale: number,

}

/**
 * The parameters for generating the surface of a biome
 */
export interface BiomeSurfaceGenerationParams {

    /**
     * The tag for the generation algorithm to generate the surface of the biome
     */
    surfaceGenTag: string,

    /**
     * The offset added to the generated heightfield to get the final height of the surface at a given position.
     * This is most useful for cases like plateaus where you really want to put the surface noise higher than its surroundings.
     */
    heightOffset: number,

    /**
     * The different foliage types that can be generated on this surface
     */
    foliageDescriptions: BiomeFoliageDescription[],

}

/**
 * Biome data
 */
export interface BiomeData {

    /**
     * The id of the biome type
     */
    id: string,

    /**
     * The display name of the biome
     */
    displayName: string,

    /**
     * True if this is an aerial biome
     */
    isAerial: boolean,

    /**
     * True if this is a surface biome
     */
    isSurface: boolean,

    /**
     * True if this is a subterranean biome
     */
    isSubterranean: boolean,

    /**
     * The parameters for generating the surface of this biome
     */
    surfaceGenerationParams: BiomeSurfaceGenerationParams,

}