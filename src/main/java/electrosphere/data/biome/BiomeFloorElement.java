package electrosphere.data.biome;

import electrosphere.util.noise.NoiseMapperElement;

/**
 * Describes how a given voxel type may be used to populate the floor of the biome
 */
public class BiomeFloorElement implements NoiseMapperElement {
    
    /**
     * The id of the voxel type for this element in particular
     */
    int voxelId;

    /**
     * The frequency of this element in particular
     */
    Double frequency;

    /**
     * The scale of the noise used to generate this element
     */
    Double dispersion;

    /**
     * The priority of this floor element in particular
     */
    Double priority;

    /**
     * Gets the voxel id of this floor element
     * @return The voxel id
     */
    public int getVoxelId(){
        return voxelId;
    }

    @Override
    public float getFrequency() {
        return (float)(double)this.frequency;
    }

}
