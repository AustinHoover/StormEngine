package electrosphere.net.client;

import electrosphere.client.block.BlockChunkData;
import electrosphere.client.terrain.cache.ChunkData;
import electrosphere.client.terrain.data.TerrainChunkDataPool;
import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.mem.BlockChunkPool;
import electrosphere.net.parser.net.message.NetworkMessage;
import electrosphere.net.parser.net.message.ServerMessage;
import electrosphere.net.parser.net.message.TerrainMessage;
import electrosphere.net.parser.net.message.TypeBytes;
import electrosphere.net.parser.net.message.NetworkMessage.MessageType;
import electrosphere.net.parser.net.message.ServerMessage.ServerMessageType;
import electrosphere.net.parser.net.raw.NetworkParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Client networking thread
 */
public class ClientNetworking implements Runnable {

    /**
     * Milliseconds after which reading is considered slow enough to be warning-worthy
     */
    static final int SOCKET_READ_WARNING_THRESHOLD = 1000;
    
    /**
     * The server's address
     */
    String address;

    /**
     * The port on the server
     */
    int port;

    /**
     * Controls whether the connection is formed locally (with lists) or not (with sockets)
     */
    boolean local = false;
    
    /**
     * The socket for the connection
     */
    public Socket socket;


//    CryptoInputStream inputStream;
//    CryptoOutputStream outputStream;
    InputStream inputStream;
    OutputStream outputStream;
    boolean initialized = false;
    NetworkParser parser;
    
    /**
     * The client protocol
     */
    MessageProtocol messageProtocol = new MessageProtocol();

    /**
     * Maximum number of times to try connecting to a server
     */
    static final int MAX_CONNECTION_ATTEMPTS = 10;

    //set to true to also get ping and pong messages in debug logging
    boolean echoPings = false;

    //thresholds for when to send pings and to determine when we've disconnected
    static final long SEND_PING_THRESHOLD = 3000;
    static final long PING_DISCONNECT_THRESHOLD = 60 * 1000;
    //times for calculating ping-pong
    long lastPingTime = 0;
    long lastPongTime = 0;

    //debugging stuff
    String netMonitorHandle = null;

    //Signals the thread to stop
    boolean shouldDisconnect = false;
    
    /**
     * Creates a ClientNetworking object with a server address and port
     * @param address The address of the server
     * @param port The port to connect to on the server
     */
    public ClientNetworking(String address, int port){
        this.shouldDisconnect = false;
        this.address = address;
        this.port = port;
    }

    /**
     * Creates a ClientNetworking object with a pair of streams
     * @param clientInputStream The input stream
     * @param clientOutputStream The output stream
     */
    public ClientNetworking(InputStream clientInputStream, OutputStream clientOutputStream){
        this.local = true;
        this.shouldDisconnect = false;
        this.inputStream = clientInputStream;
        this.outputStream = clientOutputStream;
    }
    
    
    
    @Override
    public void run(){
        initialized = false;
            //        final SecretKeySpec key = new SecretKeySpec(("1234567890123456").getBytes(),"AES");
//        final Properties properties = new Properties();
//        final RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(4096, BigInteger.probablePrime(4000, new Random()));
//        try {
//            inputStream = new CryptoInputStream("AES/ECB/PKCS5Padding",properties,socket.getInputStream(),key,spec);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        try {
//            outputStream = new CryptoOutputStream("AES/ECB/PKCS5Padding",properties,socket.getOutputStream(),key,spec);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }

        //Used to copy messages from network parser to NetMonitor
        // List<NetworkMessage> netMonitorCache = new LinkedList<NetworkMessage>();

        ///
        ///
        ///        SETUP
        ///
        ///


        if(!this.local) {
            //attempt connection
            int connectionAttempts = 0;
            boolean connected = false;
            while(!connected){
                try {
                    this.socket = new Socket(address,port);
                    connected = true;
                } catch (IOException ex) {
                    LoggerInterface.loggerNetworking.ERROR("Client failed to connect!", ex);
                }
                if(!connected){
                    try {
                        TimeUnit.MILLISECONDS.sleep(50);
                    } catch (InterruptedException e) {}
                    connectionAttempts++;
                }
                if(connectionAttempts > MAX_CONNECTION_ATTEMPTS){
                    LoggerInterface.loggerNetworking.ERROR("Max client connection attempts!", new Exception());
                }
            }

            if(connected && Globals.netMonitor != null){
                this.netMonitorHandle = Globals.netMonitor.registerConnection();
            }

            //grab input/output streams
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException ex) {
                LoggerInterface.loggerNetworking.ERROR("Error on client socket", ex);
            }
        }

        //create parser
        parser = new NetworkParser(inputStream,outputStream);

        //
        //register custom message parsers
        parser.registerCustomParser(TypeBytes.MESSAGE_TYPE_TERRAIN, TypeBytes.TERRAIN_MESSAGE_TYPE_SENDREDUCEDBLOCKDATA, (NetworkMessage message, ByteBuffer buff) -> {
            TerrainMessage castMessage = (TerrainMessage)message;
            //get meta data
            castMessage.setworldX(buff.getInt());
            castMessage.setworldY(buff.getInt());
            castMessage.setworldZ(buff.getInt());
            castMessage.setchunkResolution(buff.getInt());
            castMessage.sethomogenousValue(buff.getInt());

            //get main data blob
            if(castMessage.gethomogenousValue() == BlockChunkData.NOT_HOMOGENOUS){
                //read types from byte stream
                short[] types = BlockChunkPool.getShort();
                for(int i = 0; i < BlockChunkData.TOTAL_DATA_WIDTH; i++){
                    types[i] = buff.getShort();
                }
                //read metadata from byte stream
                short[] metadata = BlockChunkPool.getShort();
                for(int i = 0; i < BlockChunkData.TOTAL_DATA_WIDTH; i++){
                    metadata[i] = buff.getShort();
                }
                List<Object> extraData = new LinkedList<Object>();
                extraData.add(types);
                extraData.add(metadata);
                castMessage.setExtraData(extraData);
            } else {
                byte errorCheckByte = buff.get();
                if(errorCheckByte != -1){
                    LoggerInterface.loggerNetworking.WARNING("Error byte failed! " + errorCheckByte);
                }
            }
        });
        parser.registerCustomParser(TypeBytes.MESSAGE_TYPE_TERRAIN, TypeBytes.TERRAIN_MESSAGE_TYPE_SENDREDUCEDCHUNKDATA, (NetworkMessage message, ByteBuffer buff) -> {
            TerrainMessage castMessage = (TerrainMessage)message;
            //get meta data
            castMessage.setworldX(buff.getInt());
            castMessage.setworldY(buff.getInt());
            castMessage.setworldZ(buff.getInt());
            castMessage.setchunkResolution(buff.getInt());
            castMessage.sethomogenousValue(buff.getInt());

            //construct extra data
            ChunkData chunk = TerrainChunkDataPool.getData();
            chunk.setWorldX(castMessage.getworldX());
            chunk.setWorldY(castMessage.getworldY());
            chunk.setWorldZ(castMessage.getworldZ());
            chunk.setStride(castMessage.getchunkResolution());
            chunk.setHomogenousValue(castMessage.gethomogenousValue());

            //get main data blob
            if(castMessage.gethomogenousValue() == BlockChunkData.NOT_HOMOGENOUS){
                for(int x = 0; x < ChunkData.CHUNK_DATA_SIZE; x++){
                    for(int z = 0; z < ChunkData.CHUNK_DATA_SIZE; z++){
                        for(int y = 0; y < ChunkData.CHUNK_DATA_SIZE; y++){
                            chunk.setWeight(x, y, z, buff.getFloat());
                        }
                    }
                }
                int firstType = -1;
                boolean homogenous = true;
                for(int x = 0; x < ChunkData.CHUNK_DATA_SIZE; x++){
                    for(int z = 0; z < ChunkData.CHUNK_DATA_SIZE; z++){
                        for(int y = 0; y < ChunkData.CHUNK_DATA_SIZE; y++){
                            int typeCurr = buff.getInt();
                            chunk.setType(x, y, z, typeCurr);
                            if(firstType == -1){
                                firstType = typeCurr;
                            } else if(homogenous && firstType == typeCurr){
                                homogenous = false;
                            }
                        }
                    }
                }
            } else {
                buff.get();
            }

            //attach extra data
            List<Object> extraData = new LinkedList<Object>();
            extraData.add(chunk);
            castMessage.setExtraData(extraData);
        });
        


        ///
        ///
        ///      MAIN     LOOP
        ///
        ///

        //start parsing messages
        initialized = true;
        while(Globals.engineState.threadManager.shouldKeepRunning() && !this.shouldDisconnect){

            //
            //attempt poll incoming messages
            long readStart = System.currentTimeMillis();
            try {
                LoggerInterface.loggerNetworking.DEBUG_LOOP("[CLIENT] Try to read messages in");
                parser.readMessagesIn();
            } catch (IOException e) {
                LoggerInterface.loggerNetworking.ERROR(e);
            }
            if(System.currentTimeMillis() - readStart > SOCKET_READ_WARNING_THRESHOLD){
                LoggerInterface.loggerNetworking.WARNING("Client is slow to read from network!   Delay: " + (System.currentTimeMillis() - readStart) + "   Number of total bytes read(mb): " + (parser.getNumberOfBytesRead() / 1024 / 1024));
            }



            //
            //outgoing messages
            try {
                LoggerInterface.loggerNetworking.DEBUG_LOOP("[CLIENT] Try to write messages out");
                parser.pushMessagesOut();
            } catch(IOException e){
                LoggerInterface.loggerNetworking.ERROR(e);
            }




            //
            //parses messages asynchronously
            LoggerInterface.loggerNetworking.DEBUG_LOOP("[CLIENT] Parse asynchronous messages");
            boolean foundMessages = this.parseMessagesAsynchronously();



            //timeout logic
            //if received message from server, can't have timed out
            if(foundMessages){
                this.markReceivedPongMessage();
            }
            long currentTime = System.currentTimeMillis();
            //basically if we haven't sent a ping in a while, send one
            if(currentTime - lastPingTime > SEND_PING_THRESHOLD){
                this.queueOutgoingMessage(ServerMessage.constructPingMessage());
                lastPingTime = currentTime;
                if(lastPongTime == 0){
                    lastPongTime = lastPingTime;
                }
            }
            if(lastPingTime - lastPongTime > PING_DISCONNECT_THRESHOLD){
                //disconnected from the server
                LoggerInterface.loggerNetworking.WARNING("Disconnected from server");
                //close socket
                if(socket != null && socket.isConnected()){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        LoggerInterface.loggerNetworking.ERROR("Error closing socket", e);
                    }
                }
                //TODO: kick us back to the main menu
                break;
            }
            try {
                //sleep
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException ex) {
                //silently ignore
                // CodeUtils.todo(ex, "Handle sleep interrupt on server connection");
            }
        }

        if(this.socket != null){
            try {
                this.socket.close();
            } catch (IOException e) {
                LoggerInterface.loggerNetworking.ERROR(e);
            }
        }

        //null out global state
        Globals.clientState.clientConnection = null;

        LoggerInterface.loggerNetworking.INFO("Client networking thread ended");
        
    }

    /**
     * Gets the delay across the connection
     * @return The delay
     */
    public int getDelay(){
        return (int)(lastPongTime - lastPingTime);
    }



    /**
     * Parses messages asynchronously
     */
    public boolean parseMessagesAsynchronously(){
        boolean foundMessages = false;
        if(initialized){
            while(parser.hasIncomingMessaage()){
                NetworkMessage message = parser.popIncomingMessage();
                //net monitor
                if(Globals.netMonitor != null && this.netMonitorHandle != null){
                    Globals.netMonitor.logMessage(this.netMonitorHandle, message, true);
                }
                //print network message
                printMessage(message);
                //do something
                Globals.profiler.beginCpuSample("ClientProtocol.handleMessage");
                this.messageProtocol.handleAsyncMessage(message);
                foundMessages = true;
                Globals.profiler.endCpuSample();
            }
        }
        return foundMessages;
    }
    
    
    /**
     * Parses messages from the client
     */
    public void parseMessagesSynchronous(){
        this.messageProtocol.handleSyncMessages();
    }

    /**
     * Print out the network message type, this only prints ping and pong if echoPings is true
     */
    void printMessage(NetworkMessage message){
        //only print ping and pong if echoPings is true
        if(message.getType() == MessageType.SERVER_MESSAGE){
            if((((ServerMessage)message).getMessageSubtype()) == ServerMessageType.PING ||
               (((ServerMessage)message).getMessageSubtype()) == ServerMessageType.PONG
                ){
                        if(this.echoPings == true){
                    LoggerInterface.loggerNetworking.DEBUG_LOOP("[CLIENT] New message " + message.getType());
                }
            } else {
                LoggerInterface.loggerNetworking.DEBUG_LOOP("[CLIENT] New message " + message.getType());
            }
        } else {
            LoggerInterface.loggerNetworking.DEBUG_LOOP("[CLIENT] New message " + message.getType());
        }
    }
    
    /**
     * Queues an outgoing message
     * @param message The message to send to the server
     */
    public void queueOutgoingMessage(NetworkMessage message){
        //net monitor stuff
        if(Globals.netMonitor != null && this.netMonitorHandle != null){
            Globals.netMonitor.logMessage(this.netMonitorHandle, message, false);
        }
        //actually queue
        parser.addOutgoingMessage(message);
    }
    
    /**
     * Releases a network message to the object pool
     * @param message The message
     */
    public void release(NetworkMessage message){
        this.parser.release(message);
    }
    
    /**
     * Gets the message protocol
     * @return The message protocol inside this object
     */
    public MessageProtocol getMessageProtocol(){
        return messageProtocol;
    }

    public void markReceivedPongMessage(){
        lastPongTime = System.currentTimeMillis();
    }

    /**
     * Alerts the client networking that it should stop
     * @param shouldDisconnect true to disconnect, false to stay connected
     */
    public void setShouldDisconnect(boolean shouldDisconnect){
        this.shouldDisconnect = shouldDisconnect;
    }

    /**
     * Gets whether the client networking is intiialized or not
     * @return true if initialized, false otherwise
     */
    public boolean isInitialized(){
        return initialized;
    }

    /**
     * Gets the total number of bytes read by this connection
     * @return The total number of bytes
     */
    public long getNumBytesRead(){
        if(this.parser == null){
            return 0;
        }
        return this.parser.getNumberOfBytesRead();
    }
    
    
}
