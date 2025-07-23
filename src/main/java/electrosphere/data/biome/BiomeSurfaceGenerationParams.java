package electrosphere.data.biome;

import java.util.List;

import org.graalvm.polyglot.HostAccess.Export;

import electrosphere.util.annotation.Exclude;
import electrosphere.util.noise.NoiseMapper;

/**
 * Params for the surface generation algorithm
 */
public class BiomeSurfaceGenerationParams {
    
    /**
     * The tag for the generation algorithm for generating the surface
     */
    @Export
    String surfaceGenTag;

    /**
     * The offset from baseline for height generation with this biome
     */
    @Export
    Float heightOffset;

    /**
     * The different floor elements
     */
    List<BiomeFloorElement> floorVariants;

    /**
     * The list of foliage descriptions available to this biome type
     */
    @Export
    List<BiomeFoliageDescription> foliageDescriptions;

    /**
     * Used to map gradients into floor variants (ie to distribute the floor variants spatially)
     */
    @Exclude
    NoiseMapper<BiomeFloorElement> floorVariantMapper;

    /**
     * The scale of the noise
     */
    Float noiseScale;

    /**
     * The scale of the warp applies to the noise
     */
    Float warpScale;

    /**
     * Precomputes the surface voxel distribution
     */
    protected void precomputeSurfaceDistribution(){
        this.floorVariantMapper = new NoiseMapper<BiomeFloorElement>(floorVariants);
    }

    /**
     * Gets the tag for the generation algorithm for generating the surface
     * @return The tag for the generation algorithm for generating the surface
     */
    public String getSurfaceGenTag() {
        return surfaceGenTag;
    }

    /**
     * Gets the offset from baseline for height generation with this biome
     * @return The offset from baseline for height generation with this biome
     */
    public Float getHeightOffset() {
        return heightOffset;
    }

    /**
     * Gets the list of floor variants
     * @return The list of floor variants
     */
    public List<BiomeFloorElement> getFloorVariants(){
        return floorVariants;
    }

    /**
     * Gets the list of foliage descriptions
     * @return The list of foliage descriptions
     */
    public List<BiomeFoliageDescription> getFoliageDescriptions(){
        return foliageDescriptions;
    }

    /**
     * Gets a floor variant based on a gradient value
     * @param gradientValue The gradient value
     * @return The floor element
     */
    public BiomeFloorElement getFloorVariant(float gradientValue){
        return this.floorVariantMapper.lookup(gradientValue);
    }

    /**
     * Gets the scale of the noise
     * @return The scale of the noise
     */
    public Float getNoiseScale() {
        return noiseScale;
    }

    /**
     * Gets the scale of the warp applies to the noise
     * @return The scale of the warp applies to the noise
     */
    public Float getWarpScale() {
        return warpScale;
    }

    

}
