package electrosphere.server.ai.services;

/**
 * A service
 */
public interface AIService {
    
    /**
     * Executes the service
     */
    public void exec();

    /**
     * Shuts down the service
     */
    public void shutdown();

}
