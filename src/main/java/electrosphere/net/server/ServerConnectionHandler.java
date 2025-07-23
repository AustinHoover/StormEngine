package electrosphere.net.server;

import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.AuthMessage;
import electrosphere.net.parser.net.message.NetworkMessage;
import electrosphere.net.parser.net.message.ServerMessage;
import electrosphere.net.parser.net.raw.NetworkParser;
import electrosphere.net.server.player.Player;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * A connection to the server
 */
public class ServerConnectionHandler implements Runnable {

    //thresholds for determining when to send pings and when a client has disconnected
    static final long SEND_PING_THRESHOLD = 3000;
    static final long PING_DISCONNECT_THRESHOLD = 60 * 1000;

    /**
     * local carrier variables
     */
    boolean local = false;

    /**
     * socket carrier variables
     */
    Socket socket;

    //the streams for the connection
//    CryptoInputStream inputStream;
//    CryptoOutputStream outputStream;

    /**
     * The input stream for packets
     */
    InputStream inputStream;

    /**
     * The output stream for packets
     */
    OutputStream outputStream;

    /**
     * the network parser for the streams
     */
    NetworkParser networkParser;

    /**
     * initialized status
     */
    boolean initialized;

    /**
     * authentication status
     */
    boolean isAuthenticated = false;

    /**
     * the player id
     */
    int playerID;

    /**
     * the player's entity id
     */
    int playerEntityID;

    /**
     * The id of the character that is associated with the player's entity
     */
    int characterId;

    /**
     * Tracks whether this connection is still communicating with the client
     */
    boolean isConnected = true;

    /**
     * the creature template associated with this player
     */
    ObjectTemplate currentCreatureTemplate;
    
    /**
     * the server protocol object associated with this player
     */
    MessageProtocol messageProtocol;

    /**
     * Keeps track of the last time this connection received a ping from the client
     */
    long lastPingTime = 0;
    /**
     * Keeps track of the last time this connection received a pong from the client
     */
    long lastPongTime = 0;

    /**
     * flag to disconnect due to pipe break
     */
    boolean socketException = false;


    /**
     * debug netmonitor stuff
     */
    String netMonitorHandle;

    /**
     * Used to copy messages from network parser to NetMonitor
     */
    List<NetworkMessage> netMonitorCache = new LinkedList<NetworkMessage>();

    /**
     * the lock used for synchronizing the synchronous message queue
     */
    Semaphore synchronousMessageLock = new Semaphore(1);
    
    /**
     * Constructs a connection from a socket
     * @param socket the socket
     */
    public ServerConnectionHandler(Socket socket) {
        this.socket = socket;
        this.playerID = Player.getNewId();
        LoggerInterface.loggerNetworking.INFO("[SERVER] Player ID: " + playerID);
        this.messageProtocol = new MessageProtocol(this);
    }

    /**
     * Constructs a connection from an arbitrary input and output stream
     * @param serverInputStream the input stream
     * @param serverOutputStream the output stream
     */
    public ServerConnectionHandler(InputStream serverInputStream, OutputStream serverOutputStream){
        this.local = true;
        this.playerID = Player.getNewId();
        LoggerInterface.loggerNetworking.INFO("[SERVER] Player ID: " + playerID);
        inputStream = serverInputStream;
        outputStream = serverOutputStream;
        this.messageProtocol = new MessageProtocol(this);
    }

    @Override
    public void run() {
        LoggerInterface.loggerNetworking.INFO("ServerConnectionHandler start");
        initialized = false;


        ///
        ///
        ///       SETUP
        ///
        ///


        //startup is different whether need to set socket streams or received in construction of object (ie if creating local "connection")
        if(this.local){
            //run if serverconnectionHandler is created by passing in input/output streams
            networkParser = new NetworkParser(inputStream,outputStream);
            networkParser.setReleaseOnSend(false);
            messageProtocol = new MessageProtocol(this);
        } else {
            //run if ServerConnectionHandler is created by passing in a socket
            try {
                socket.setSoTimeout(100);
            } catch (SocketException ex) {
                ex.printStackTrace();
            }

            //TODO: use this commented block of code as a reference for implementing encryption on top of the game connection
            //        final SecretKeySpec key = new SecretKeySpec(("1234567890123456").getBytes(),"AES");
//        final Properties properties = new Properties();
//        final RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(4096, BigInteger.probablePrime(4000, new Random()));
//        try {
//            inputStream = new CryptoInputStream("AES/ECB/PKCS5Padding",properties,socket.getInputStream(),key,spec);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            System.exit(1);
//        }
//        try {
//            outputStream = new CryptoOutputStream("AES/ECB/PKCS5Padding",properties,socket.getOutputStream(),key,spec);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            System.exit(1);
//        }
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                networkParser = new NetworkParser(inputStream,outputStream);
                messageProtocol = new MessageProtocol(this);
            } catch (IOException ex) {
                ex.printStackTrace();
                LoggerInterface.loggerNetworking.ERROR("", ex);
            }
        }
        
        NetworkMessage pingMessage = ServerMessage.constructPingMessage();
        NetworkMessage authRequestMessage = AuthMessage.constructAuthRequestMessage();

        //net monitor registration
        if(Globals.netMonitor != null){
            this.netMonitorHandle = Globals.netMonitor.registerConnection();
            Globals.netMonitor.logMessage(netMonitorHandle, pingMessage, false);
            Globals.netMonitor.logMessage(netMonitorHandle, authRequestMessage, false);
        }

        networkParser.addOutgoingMessage(pingMessage);
        networkParser.addOutgoingMessage(authRequestMessage);




        ///
        ///
        ///      MAIN    LOOP
        ///
        ///


        initialized = true;
        while(Globals.engineState.threadManager.shouldKeepRunning() && this.isConnected == true && Globals.serverState.server != null && Globals.serverState.server.isOpen()){

            boolean receivedMessageThisLoop = false;
            //
            // Main Loop
            //
            //parse messages both incoming and outgoing
            try {
                LoggerInterface.loggerNetworking.DEBUG_LOOP("[SERVER] Try to read messages in");
                receivedMessageThisLoop = this.parseMessages();
            } catch (SocketException e) {
                //if we get a SocketException broken pipe (basically the client dc'd without telling us)
                //set flag to disconnect client
                //TODO: fix, this doesn't actually catch the socket exception which is exceedingly obnoxious
                socketException = true;
                LoggerInterface.loggerNetworking.ERROR("Client disconnected", e);
                this.disconnect();
                break;
            } catch (IOException e){
                //if we get a SocketException broken pipe (basically the client dc'd without telling us)
                //set flag to disconnect client
                //TODO: fix, this doesn't actually catch the socket exception which is exceedingly obnoxious
                socketException = true;
                LoggerInterface.loggerNetworking.ERROR("Client disconnected", e);
                this.disconnect();
                break;
            }

            //
            // Timeout logic
            //
            //mark as alive if a message was received from client
            if(receivedMessageThisLoop){
                this.markReceivedPongMessage();
            }
            //ping logic
            long currentTime = System.currentTimeMillis();
            //basically if we haven't sent a ping in a while, send one
            if(currentTime - lastPingTime > SEND_PING_THRESHOLD){
                this.addMessagetoOutgoingQueue(ServerMessage.constructPingMessage());
                lastPingTime = currentTime;
                if(lastPongTime == 0){
                    lastPongTime = lastPingTime;
                }
            }

            //
            // Disconnections
            //
            //check if we meet disconnection criteria
            //has it been too long since the last ping?
            //have we had a socket exception?
            if(lastPingTime - lastPongTime > PING_DISCONNECT_THRESHOLD){
                //disconnected from the server
                LoggerInterface.loggerNetworking.WARNING("Client timeout");
                //run disconnect routine
                this.disconnect();
                break;
            }
            if(this.socketException == true){
                //disconnected from the server
                LoggerInterface.loggerNetworking.WARNING("Client disconnected");
                //run disconnect routine
                this.disconnect();
                break;
            }
        }

        if(this.socket != null){
            try {
                this.socket.close();
            } catch (IOException e) {
                LoggerInterface.loggerNetworking.ERROR(e);
            }
        }

        LoggerInterface.loggerNetworking.INFO("Server connection thread ended");
    }

    /**
     * Had to wrap the message parsing block in a function to throw a SocketException
     * without my linter freaking out
     * @throws SocketException
     * @return true if connection is alive, false otherwise
     */
    private boolean parseMessages() throws SocketException, IOException {
        boolean rVal = false;
        //
        //Read in messages
        //
        //attempt poll incoming messages
        LoggerInterface.loggerNetworking.DEBUG_LOOP("[SERVER] Try to read messages in");
        networkParser.readMessagesIn();
        rVal = networkParser.hasIncomingMessaage();


        //
        //Net monitor (debug)
        //
        //net monitor
        if(Globals.netMonitor != null){
            //incoming
            netMonitorCache.clear();
            networkParser.copyIncomingMessages(netMonitorCache);
            for(NetworkMessage message : netMonitorCache){
                Globals.netMonitor.logMessage(netMonitorHandle, message, true);
            }
        }
        
        //
        //Parse messages
        //
        //ponder incoming messages
        while(networkParser.hasIncomingMessaage()){
            LoggerInterface.loggerNetworking.DEBUG_LOOP("[SERVER] Handle async messages");
            NetworkMessage message = networkParser.popIncomingMessage();
            this.messageProtocol.handleAsyncMessage(message);
        }


        //
        //Net monitor (debug)
        //
        if(Globals.netMonitor != null){
            //outgoing
            netMonitorCache.clear();
            networkParser.copyOutgoingMessages(netMonitorCache);
            for(NetworkMessage message : netMonitorCache){
                Globals.netMonitor.logMessage(netMonitorHandle, message, false);
            }
        }

        //
        //Send out messages
        //
        //push outgoing message
        LoggerInterface.loggerNetworking.DEBUG_LOOP("[SERVER] Try to write messages out");
        networkParser.pushMessagesOut();
        try {
            //sleep
            TimeUnit.MILLISECONDS.sleep(1);
        } catch (InterruptedException ex) {
            //silently ignore
            // CodeUtils.todo(ex, "Handle sleep interrupt on server connection");
        }
        return rVal;
    }

    /**
     * Handles synchronous packets in the queue
     */
    public void handleSynchronousPacketQueue(){
        this.messageProtocol.handleSyncMessages();
    }

    /**
     * Gets the player's id
     * @return The player's id
     */
    public int getPlayerId(){
        return playerID;
    }

    /**
     * Sets the player's entity's id
     * @param id
     */
    public void setPlayerEntityId(int id){
        LoggerInterface.loggerNetworking.DEBUG("Set player(" + this.playerID + ")'s entity ID to be " + id);
        playerEntityID = id;
    }

    /**
     * Gets the player's entity's id
     * @return The id
     */
    public int getPlayerEntityId(){
        return playerEntityID;
    }

    /**
     * Gets the player associated with this connection
     * @return The player object
     */
    public Player getPlayer(){
        return Globals.serverState.playerManager.getPlayerFromId(playerID);
    }

    /**
     * Gets the ip address of the connection
     * @return The ip address
     */
    public String getIPAddress(){
        if(local){
            return "127.0.0.1";
        } else {
            return socket.getRemoteSocketAddress().toString();
        }
    }

    /**
     * Gets the socket of the connection, if it is a socket based connection. Otherwise returns null.
     * @return The socket if it exists, null otherwise
     */
    public Socket getSocket(){
        return this.socket;
    }
    
    /**
     * Adds a message to the outgoing queue
     * @param message The message
     */
    public void addMessagetoOutgoingQueue(NetworkMessage message){
        networkParser.addOutgoingMessage(message);
    }

    /**
     * Sets the current creature template for the connection
     * @param currentCreatureTemplate The new creature template
     */
    public void setCreatureTemplate(ObjectTemplate currentCreatureTemplate){
        this.currentCreatureTemplate = currentCreatureTemplate;
    }

    /**
     * Gets the current creature template for the connection
     * @return The current template
     */
    public ObjectTemplate getCurrentCreatureTemplate(){
        return this.currentCreatureTemplate;
    }

    /**
     * Marks that this connection received a pong message
     */
    public void markReceivedPongMessage(){
        lastPongTime = System.currentTimeMillis();
    }

    /**
     * Routine to run when the client disconnects
     */
    protected void disconnect(){
        //close socket
        this.synchronousMessageLock.acquireUninterruptibly();

        //queue message to tell client it disconnected
        this.networkParser.addOutgoingMessage(ServerMessage.constructDisconnectMessage());
        
        //flush outgoing messages
        try {
            this.networkParser.pushMessagesOut();
        } catch (IOException e) {
            LoggerInterface.loggerNetworking.ERROR(e);
        }

        //close the socket
        if(socket != null && socket.isConnected()){
            try {
                socket.close();
            } catch (IOException e) {
                LoggerInterface.loggerNetworking.ERROR("Error closing socket", e);
            }
        }

        this.synchronousMessageLock.release();
        this.isConnected = false;
        //add connection to server list of connections to cleanup
        if(Globals.serverState.server != null){
            Globals.serverState.server.addClientToCleanup(this);
        }
    }

    /**
     * Gets the total number of bytes read by this connection
     * @return The total number of bytes
     */
    public long getNumBytesRead(){
        if(this.networkParser == null){
            return 0;
        }
        return this.networkParser.getNumberOfBytesRead();
    }

    /**
     * Gets the id of the character associated with the player's entity
     * @return The character's id
     */
    public int getCharacterId() {
        return characterId;
    }

    /**
     * Sets the id of the character associated with the player's entity
     * @param characterId The character's id
     */
    public void setCharacterId(int characterId) {
        this.characterId = characterId;
    }

    /**
     * Gets the network parser
     * @return The network parser
     */
    public NetworkParser getNetworkParser(){
        return this.networkParser;
    }

    

}
