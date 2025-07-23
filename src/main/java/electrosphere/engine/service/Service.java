package electrosphere.engine.service;

/**
 * A service
 */
public interface Service {
    
    /**
     * Initializes the service
     */
    public void init();

    /**
     * Destroys the service
     */
    public void destroy();

    /**
     * Resets the service on closing the client and/or server
     */
    public void unloadScene();

    /**
     * Returns the name of the service
     * @return
     */
    public String getName();

}
