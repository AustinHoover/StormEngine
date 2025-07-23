package electrosphere.net.template;

/**
 * A client protocol interface
 */
public interface ClientProtocolTemplate<T> {
    
    /**
     * Handles a message asynchronously
     * @param message The message
     */
    public T handleAsyncMessage(T message);

    /**
     * Handles a message synchronously
     * @param message The message
     */
    public void handleSyncMessage(T message);

}
