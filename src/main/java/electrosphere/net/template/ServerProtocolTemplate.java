package electrosphere.net.template;

import electrosphere.net.server.ServerConnectionHandler;

/**
 * A server protocol interface
 */
public interface ServerProtocolTemplate<T> {

    /**
     * Handles a message asynchronously
     * @param connectionHandler The connection to the server 
     * @param message The message
     */
    public T handleAsyncMessage(ServerConnectionHandler connectionHandler, T message);

    /**
     * Handles a message synchronously
     * @param connectionHandler The connection to the server
     * @param message The message
     */
    public void handleSyncMessage(ServerConnectionHandler connectionHandler, T message);
    
}
