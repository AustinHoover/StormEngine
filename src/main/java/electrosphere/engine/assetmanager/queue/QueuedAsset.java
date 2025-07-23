package electrosphere.engine.assetmanager.queue;

/**
 * An asset that has its data ready to be buffered to gpu
 */
public interface QueuedAsset<T> {
    
    /**
     * Loads the asset
     */
    public void load();

    /**
     * Checks whether the queued asset has been loaded or not
     * @return True if it has been loaded to gpu/wherever, false otherise
     */
    public boolean hasLoaded();

    /**
     * Gets the asset
     * @return The asset
     */
    public T get();

    /**
     * Gets the path the asset manager promises this asset will be stored at
     * @return The promised path
     */
    public String getPromisedPath();

    /**
     * Sets the path the asset manager promises this asset will be stored at
     * @param promisedPath The path
     */
    public void setPromisedPath(String promisedPath);

    /**
     * True if the path to register this asset was supplied while it was being queued, false if the promised path should be generated when it is placed in queue
     * @return true or false
     */
    public boolean suppliedPath();

}
