package electrosphere.data.entity.item;

/**
 * Defines what a given use action does with an item
 */
public class ItemUsage {
    
    /**
     * If defined, this item will spawn the provided entity id on use
     */
    String spawnEntityId;

    /**
     * If defined, this item will place the block type on use
     */
    Integer blockId;

    /**
     * If defined, this item will place the voxel type on use
     */
    Integer voxelId;

    /**
     * The hook to fire on the client when this item is used
     */
    String clientHook;

    /**
     * Used to suppress sending a request to the server to use the item (ie if firing a client hook)
     */
    Boolean suppressServerRequest;

    /**
     * Controls whether this usage only fires on mouse down
     */
    Boolean onlyOnMouseDown;

    /**
     * Gets the spawn entity id of the item usage
     * @return The spawn entity id
     */
    public String getSpawnEntityId(){
        return spawnEntityId;
    }

    /**
     * Sets the spawn entity id of the item usage
     * @param spawnEntityId The spawn entity id
     */
    public void setSpawnEntityId(String spawnEntityId) {
        this.spawnEntityId = spawnEntityId;
    }

    /**
     * Gets the block type id of the item usage
     * @return The block type id
     */
    public Integer getBlockId() {
        return blockId;
    }

    /**
     * Sets the block type id of the item usage
     * @param spawnEntityId The block type id
     */
    public void setBlockId(Integer blockId) {
        this.blockId = blockId;
    }

    /**
     * Gets whether this usage only fires on mouse down
     * @return if true, the usage only fires on mouse down
     */
    public Boolean getOnlyOnMouseDown() {
        return onlyOnMouseDown;
    }

    /**
     * Sets whether this usage only fires on mouse down
     * @param onlyOnMouseDown true to only fire on mouse down
     */
    public void setOnlyOnMouseDown(Boolean onlyOnMouseDown) {
        this.onlyOnMouseDown = onlyOnMouseDown;
    }

    /**
     * Gets the client hook to fire when this item is used
     * @return The client hook to fire
     */
    public String getClientHook() {
        return clientHook;
    }

    /**
     * Sets the client hook to fire when this item is used
     * @param clientHook The client hook to fire
     */
    public void setClientHook(String clientHook) {
        this.clientHook = clientHook;
    }

    /**
     * Gets whether the server request should be suppressed or not
     * @return true if it should be suppressed, false otherwise
     */
    public Boolean getSuppressServerRequest() {
        return suppressServerRequest;
    }

    /**
     * Sets whether the server request should be suppressed or not
     * @param suppressServerRequest true if it should be suppressed, false otherwise
     */
    public void setSuppressServerRequest(Boolean suppressServerRequest) {
        this.suppressServerRequest = suppressServerRequest;
    }

    /**
     * Gets the voxel id
     * @return The voxel id
     */
    public Integer getVoxelId() {
        return voxelId;
    }

    /**
     * Sets the voxel id
     * @param voxelId The voxel id
     */
    public void setVoxelId(Integer voxelId) {
        this.voxelId = voxelId;
    }
    
    

}
