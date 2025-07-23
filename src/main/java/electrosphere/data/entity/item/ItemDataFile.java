package electrosphere.data.entity.item;

import java.util.List;

/**
 * Contains a list of item data definitions and a list of sub files
 */
public class ItemDataFile {

    /**
     * The item data in this file
     */
    List<Item> items;

    /**
     * All child files of this one
     */
    List<String> files;

    /**
     * Gets the item data in this file
     * @return The item data in this file
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * Sets the item data in this file
     * @param item The item data in this file
     */
    public void setItems(List<Item> items) {
        this.items = items;
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
