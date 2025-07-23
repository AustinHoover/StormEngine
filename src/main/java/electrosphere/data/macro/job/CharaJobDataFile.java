package electrosphere.data.macro.job;

import java.util.List;

/**
 * A file that stores job data
 */
public class CharaJobDataFile {
    
    /**
     * The list of job definitions
     */
    List<CharaJob> data;

    /**
     * All child files of this one
     */
    List<String> files;

    /**
     * Gets the job data in this file
     * @return The job data in this file
     */
    public List<CharaJob> getData() {
        return data;
    }

    /**
     * Sets the job data in this file
     * @param data The job data in this file
     */
    public void setData(List<CharaJob> data) {
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
