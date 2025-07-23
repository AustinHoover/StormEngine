package electrosphere.net.parser.net.raw;

import electrosphere.net.parser.net.message.MessagePool;
import electrosphere.net.parser.net.message.NetworkMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

/**
 * The main message parser. This is used to serialize/deserialize messages to/from the provided streams.
 */
public class NetworkParser {

    /**
     * The size of the read buffer
     */
    static final int READ_BLOCK_SIZE = 16 * 1024 * 1024;

    /**
     * The size of the circular buffer
     */
    static final int CIRCULAR_BUFFER_SIZE = 64 * 1024 * 1024;
    
    /**
     * The input stream for the parser
     */
    private InputStream incomingStream;

    /**
     * The output stream for the parser
     */
    private OutputStream outgoingStream;
    
    /**
     * The queue of incoming messages that have been parsed
     */
    private CopyOnWriteArrayList<NetworkMessage> incomingMessageQueue = new CopyOnWriteArrayList<NetworkMessage>();

    /**
     * The queue of outgoing messages that have yet to be sent
     */
    private CopyOnWriteArrayList<NetworkMessage> outgoingMessageQueue = new CopyOnWriteArrayList<NetworkMessage>();

    /**
     * Message object pool
     */
    private MessagePool pool = new MessagePool();
    
    /**
     * The byte buffer for storing incoming bytes
     */
    private ByteBuffer incomingByteBuffer = ByteBuffer.allocate(CIRCULAR_BUFFER_SIZE);

    /**
     * The block array used to read blocks of bytes in
     */
    private byte[] readBuffer = new byte[READ_BLOCK_SIZE];

    /**
     * The number of bytes read
     */
    private long totalBytesRead = 0;

    /**
     * Number of bytes preserved between read calls
     */
    private int existingBytes = 0;

    /**
     * If set to true, the parser will automatically release messages on send.
     * Otherwise, will not release when the message is sent.
     */
    private boolean releaseOnSend = true;

    /**
     * The map of messasge type -> custom function to produce the message from a byte stream
     */
    private Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap = new HashMap<Short,BiConsumer<NetworkMessage,ByteBuffer>>();

    /**
     * Stores the most recent message type for debugging purposes
     */
    private NetworkMessage mostRecentMessage = null;
    
    
    /**
     * Constructor
     * @param incomingStream The stream of incoming bytes
     * @param outgoingStream The stream of outgoing bytes
     */
    public NetworkParser(InputStream incomingStream, OutputStream outgoingStream){
        this.incomingStream = incomingStream;
        this.outgoingStream = outgoingStream;
    }

    /**
     * Reads messages from the input stream
     */
    public void readMessagesIn() throws IOException {
        //read in bytes
        int bytesRead = 0;
        if(incomingStream.available() > 0){
            //error check
            if(incomingByteBuffer.position() > 0){
                String message = "Invalid position!\n" +
                "position: " + incomingByteBuffer.position() + "\n" +
                "limit: " + incomingByteBuffer.limit() + "\n" +
                "remaining: " + incomingByteBuffer.remaining() + "\n" +
                "byte [0]: " + incomingByteBuffer.get(0) + "\n" +
                "byte [1]: " + incomingByteBuffer.get(1) + "\n" +
                "";
                throw new Error(message);
            }

            //make sure we have room to write
            incomingByteBuffer.limit(CIRCULAR_BUFFER_SIZE);
            incomingByteBuffer.position(existingBytes);

            //read bytes into buffer
            bytesRead = incomingStream.read(readBuffer, 0, READ_BLOCK_SIZE);
            incomingByteBuffer.put(readBuffer, 0, bytesRead);

            //fake flip
            existingBytes = existingBytes + bytesRead;
            incomingByteBuffer.position(0);
            incomingByteBuffer.limit(existingBytes);

            //tracking
            totalBytesRead = totalBytesRead + bytesRead;
        }
        //parse byte queue for messages
        //for each message, append to clientIncomingMessageQueue
        NetworkMessage newMessage;
        if(existingBytes > 0 || bytesRead > 0){
            try {
                newMessage = NetworkMessage.parseBytestreamForMessage(incomingByteBuffer,this.pool,this.customParserMap);
                while(newMessage != null){
                    mostRecentMessage = newMessage;
                    incomingMessageQueue.add(newMessage);
                    newMessage = NetworkMessage.parseBytestreamForMessage(incomingByteBuffer,this.pool,this.customParserMap);
                }
            } catch (Error e){
                throw new Error(mostRecentMessage + " failed to parse!",e);
            }

            //compact the byte buffer
            incomingByteBuffer.compact();
            existingBytes = incomingByteBuffer.position();
            incomingByteBuffer.position(0);
            incomingByteBuffer.limit(existingBytes);

            //error check
            if(CIRCULAR_BUFFER_SIZE - existingBytes < READ_BLOCK_SIZE){
                String message = "Failed to parse messages!\n" +
                "position: " + incomingByteBuffer.position() + "\n" +
                "limit: " + incomingByteBuffer.limit() + "\n" +
                "remaining: " + incomingByteBuffer.remaining() + "\n" +
                "byte [0]: " + incomingByteBuffer.get(0) + "\n" +
                "byte [1]: " + incomingByteBuffer.get(1) + "\n" +
                "";
                throw new Error(message);
            }
        }
    }
    
    /**
     * Pushes messages out across the output stream
     * @throws IOException Thrown if a message fails to serialize or the output stream fails to write
     */
    public void pushMessagesOut() throws IOException {
        for(NetworkMessage message : outgoingMessageQueue){
            outgoingMessageQueue.remove(message);
            message.write(outgoingStream);
            if(this.releaseOnSend){
                this.pool.release(message);
            }
        }
    }
    
    /**
     * Checks if there is a fully parsed incoming message in the queue
     * @return true if there is message in the queue, false otherwise
     */
    public boolean hasIncomingMessaage(){
        return incomingMessageQueue.size() > 0;
    }
    
    /**
     * Pops a fully parsed incoming message from the queue
     * @return The message
     */
    public NetworkMessage popIncomingMessage(){
        return incomingMessageQueue.remove(0);
    }
    
    /**
     * Adds a message to the outgoing queue
     * @param message The message
     */
    public void addOutgoingMessage(NetworkMessage message){
        outgoingMessageQueue.add(message);
    }

    /**
     * Copies the current contents of the incoming messages queue to a provided list
     * @param messages The list to copy the incoming messages to
     */
    public void copyIncomingMessages(List<NetworkMessage> messages){
        messages.addAll(incomingMessageQueue);
    }

    /**
     * Copies the current contents of the outgoing messages queue to a provided list
     * @param messages The list to copy the outgoing messages to
     */
    public void copyOutgoingMessages(List<NetworkMessage> messages){
        messages.addAll(outgoingMessageQueue);
    }

    /**
     * Gets the total number of bytes read by this connection
     * @return The total number of bytes
     */
    public long getNumberOfBytesRead(){
        return totalBytesRead;
    }

    /**
     * Releases a network message object back into the pool
     * @param message The message
     */
    public void release(NetworkMessage message){
        this.pool.release(message);
    }

    /**
     * Gets the message pool
     * @return The message pool
     */
    public MessagePool getMessagePool(){
        return this.pool;
    }

    /**
     * If set to true, the parser will automatically release messages on send.
     * Otherwise, will not release when the message is sent.
     * @param releaseOnSend true to release messages on send, false otherwise
     */
    public void setReleaseOnSend(boolean releaseOnSend){
        this.releaseOnSend = releaseOnSend;
    }

    /**
     * Registers a custom parser for a given message type/subtype
     * @param messageType The type of message
     * @param messageSubtype The subtype of the message
     * @param parserFunc The parser function
     */
    public void registerCustomParser(byte messageType, byte messageSubtype, BiConsumer<NetworkMessage,ByteBuffer> parserFunc){
        short pair = (short)((messageType << 4) | messageSubtype);
        this.customParserMap.put(pair,parserFunc);
    }

}
