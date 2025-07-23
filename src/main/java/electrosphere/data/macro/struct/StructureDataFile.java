package electrosphere.data.macro.struct;

import java.util.List;

/**
 * A file that stores structure data
 */
public class StructureDataFile {
    
    /**
     * The list of structures
     */
    List<StructureData> data;

    /**
     * All child files of this one
     */
    List<String> files;

    /**
     * Gets the structure data in this file
     * @return The structure data in this file
     */
    public List<StructureData> getData() {
        return data;
    }

    /**
     * Sets the structure data in this file
     * @param structure The structure data in this file
     */
    public void setData(List<StructureData> data) {
        this.data = data;
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
