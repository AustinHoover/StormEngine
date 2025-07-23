package electrosphere.net.server;

import electrosphere.engine.Globals;
import electrosphere.engine.threads.LabeledThread.ThreadLabel;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.NetUtils;
import electrosphere.net.parser.net.message.NetworkMessage;
import electrosphere.net.server.player.Player;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Lowest level networking class for the server
 */
public class Server implements Runnable {

    /**
     * tracks whether the server is open or not
     */
    private boolean isOpen = false;
    
    /**
     * the port the server is running on
     */
    private int port;
    
    /**
     * the socket for the server
     */
    private ServerSocket serverSocket;
    
    /**
     * Used to synchronize additions/subtractions to the connections stored by this server
     */
    private Semaphore connectListLock = new Semaphore(1);

    /**
     * map of socket->connection
     */
    private Map<Socket,ServerConnectionHandler> socketConnectionMap = new HashMap<Socket,ServerConnectionHandler>();

    /**
     * the list of active connections
     */
    private List<ServerConnectionHandler> activeConnections = new LinkedList<ServerConnectionHandler>();
    
    /**
     * The list of connections to clean up
     */
    private List<ServerConnectionHandler> connectionsToCleanup = new CopyOnWriteArrayList<ServerConnectionHandler>();
    
    
    /**
     * Inits the server
     */
    private void initServer(){
        // clientMap = new HashMap<String,ServerConnectionHandler>();
    }
    
    /**
     * Constructor
     * @param port The port to run the server on
     */
    public Server(int port){
        this.port = port;
    }

    @Override
    public void run() {
        this.initServer();
        try {
            serverSocket = new ServerSocket(port);
            //if we set port to 0, java searches for any available port to open
            //This then explicitly alerts NetUtils of the real port
            if(port == 0){
                NetUtils.setPort(serverSocket.getLocalPort());
            }
            this.isOpen = true;
        } catch(BindException ex){
            LoggerInterface.loggerNetworking.ERROR("Failed to bind server socket!",ex);
        } catch (IOException ex) {
            LoggerInterface.loggerNetworking.ERROR("Failed to start server socket!",ex);
        }
        while(Globals.engineState.threadManager.shouldKeepRunning() && !serverSocket.isClosed()){
            Socket newSocket;
            try {
                newSocket = serverSocket.accept();
                connectListLock.acquireUninterruptibly();
                ServerConnectionHandler newClient = new ServerConnectionHandler(newSocket);
                // clientMap.put(newSocket.getInetAddress().getHostAddress(), newClient);
                socketConnectionMap.put(newSocket, newClient);
                activeConnections.add(newClient);
                Globals.engineState.threadManager.start(ThreadLabel.NETWORKING_SERVER, new Thread(newClient));
                connectListLock.release();
            } catch (SocketException ex){
                LoggerInterface.loggerNetworking.DEBUG("Server Socket closed!",ex);
            } catch (IOException ex) {
                LoggerInterface.loggerNetworking.ERROR("Socket error on client socket!",ex);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                LoggerInterface.loggerEngine.DEBUG("Failed to sleep", e);
            }
        }
        this.close();
        //null out global state
        Globals.serverState.server = null;
        LoggerInterface.loggerNetworking.INFO("Server socket thread ended");
    }

    /**
     * Synchronously handles queued packets for each client connection
     */
    public void synchronousPacketHandling(){
        connectListLock.acquireUninterruptibly();
        for(ServerConnectionHandler connectionHandler : activeConnections){
            connectionHandler.handleSynchronousPacketQueue();
        }
        connectListLock.release();
    }
    
    /**
     * Closes the server socket
     */
    public void close(){
        try {
            if(serverSocket != null){
                serverSocket.close();
            }
            this.isOpen = false;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Broadcasts a message to all clients
     * @param message The message to broadcast
     */
    public void broadcastMessage(NetworkMessage message){
        connectListLock.acquireUninterruptibly();
        for(ServerConnectionHandler client : activeConnections){
            client.addMessagetoOutgoingQueue(message);
        }
        connectListLock.release();
    }

    /**
     * Adds a connection created manually by two streams instead of receiving the streams from the server socket
     * @param serverInputStream The input stream
     * @param serverOutputStream The output stream
     * @return The connection object for the provided streams
     */
    public ServerConnectionHandler addLocalPlayer(InputStream serverInputStream, OutputStream serverOutputStream){
        connectListLock.acquireUninterruptibly();
        ServerConnectionHandler newClient = new ServerConnectionHandler(serverInputStream,serverOutputStream);
        activeConnections.add(newClient);
        Globals.engineState.threadManager.start(ThreadLabel.NETWORKING_SERVER, new Thread(newClient));
        connectListLock.release();
        return newClient;
    }

    /**
     * Adds a client to the queue of connections to cleanup
     * @param serverConnectionHandler The connection
     */
    public void addClientToCleanup(ServerConnectionHandler serverConnectionHandler){
        this.connectListLock.acquireUninterruptibly();
        this.connectionsToCleanup.add(serverConnectionHandler);
        this.connectListLock.release();
    }

    /**
     * Gets the first connection
     * @return The first connection
     */
    public ServerConnectionHandler getFirstConnection(){
        ServerConnectionHandler firstCon = null;
        connectListLock.acquireUninterruptibly();
        if(this.activeConnections.size() > 0){
            firstCon = this.activeConnections.get(0);
        }
        connectListLock.release();
        return firstCon;
    }

    /**
     * Cleans up dead connections on the server
     */
    public void cleanupDeadConnections(){
        this.connectListLock.acquireUninterruptibly();
        for(ServerConnectionHandler connection : this.connectionsToCleanup){
            //tell all clients to destroy the entity
            Player player = connection.getPlayer();
            if(player != null && player.getPlayerEntity() != null){
                ServerEntityUtils.destroyEntity(player.getPlayerEntity());
            }
            this.activeConnections.remove(connection);
            if(connection.getSocket() != null){
                this.socketConnectionMap.remove(connection.getSocket());
            }
        }
        this.connectListLock.release();
    }

    /**
     * Saves state from the server connections and shuts down the connections
     */
    public void saveAndClose(){
        this.connectListLock.acquireUninterruptibly();
        //store each player's character
        for(ServerConnectionHandler connection : this.activeConnections){
            connection.disconnect();
        }
        //close the server
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            LoggerInterface.loggerNetworking.ERROR(e);
        }
        this.connectListLock.release();
    }

    /**
     * Gets whether the server is open or not
     * @return true if is open, false otherwise
     */
    public boolean isOpen(){
        return isOpen;
    }


}
