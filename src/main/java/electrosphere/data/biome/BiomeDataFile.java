package electrosphere.data.biome;

import java.util.List;

/**
 * The data file containing all biome data
 */
public class BiomeDataFile {
    
    /**
     * The biome data in this file
     */
    List<BiomeData> biomes;

    /**
     * All child files of this one
     */
    List<String> files;

    /**
     * Gets the biome data in this file
     * @return The biome data in this file
     */
    public List<BiomeData> getBiomes() {
        return biomes;
    }

    /**
     * Sets the biome data in this file
     * @param biomes The biome data in this file
     */
    public void setBiomes(List<BiomeData> biomes) {
        this.biomes = biomes;
    }

    /**
     * Gets all child files of this one
     * @return All child files of this one
     */
    public List<String> getFiles() {
        return files;
    }

    /**
     * Sets all child files of this one
     * @param files All child files of this one
     */
    public void setFiles(List<String> files) {
        this.files = files;
    }

}
