package electrosphere.net.parser.net.message;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.BiConsumer;

public class ServerMessage extends NetworkMessage {

    /**
     * The types of messages available in this category.
     */
    public enum ServerMessageType {
        PING,
        PONG,
        DISCONNECT,
    }

    /**
     * The type of this message in particular.
     */
    ServerMessageType messageType;

    /**
     * Constructor
     * @param messageType The type of this message
     */
    private ServerMessage(ServerMessageType messageType){
        this.type = MessageType.SERVER_MESSAGE;
        this.messageType = messageType;
    }

    /**
     * Constructor
     */
    protected ServerMessage(){
        this.type = MessageType.SERVER_MESSAGE;
    }

    public ServerMessageType getMessageSubtype(){
        return this.messageType;
    }

    /**
     * Parses a message of type Ping
     */
    public static ServerMessage parsePingMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 0){
            return null;
        }
        ServerMessage rVal = (ServerMessage)pool.get(MessageType.SERVER_MESSAGE);
        rVal.messageType = ServerMessageType.PING;
        return rVal;
    }

    /**
     * Constructs a message of type Ping
     */
    public static ServerMessage constructPingMessage(){
        ServerMessage rVal = new ServerMessage(ServerMessageType.PING);
        return rVal;
    }

    /**
     * Parses a message of type Pong
     */
    public static ServerMessage parsePongMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 0){
            return null;
        }
        ServerMessage rVal = (ServerMessage)pool.get(MessageType.SERVER_MESSAGE);
        rVal.messageType = ServerMessageType.PONG;
        return rVal;
    }

    /**
     * Constructs a message of type Pong
     */
    public static ServerMessage constructPongMessage(){
        ServerMessage rVal = new ServerMessage(ServerMessageType.PONG);
        return rVal;
    }

    /**
     * Parses a message of type Disconnect
     */
    public static ServerMessage parseDisconnectMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 0){
            return null;
        }
        ServerMessage rVal = (ServerMessage)pool.get(MessageType.SERVER_MESSAGE);
        rVal.messageType = ServerMessageType.DISCONNECT;
        return rVal;
    }

    /**
     * Constructs a message of type Disconnect
     */
    public static ServerMessage constructDisconnectMessage(){
        ServerMessage rVal = new ServerMessage(ServerMessageType.DISCONNECT);
        return rVal;
    }

    @Deprecated
    @Override
    void serialize(){
        switch(this.messageType){
            case PING:
                rawBytes = new byte[2];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_SERVER;
                //entity messaage header
                rawBytes[1] = TypeBytes.SERVER_MESSAGE_TYPE_PING;
                break;
            case PONG:
                rawBytes = new byte[2];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_SERVER;
                //entity messaage header
                rawBytes[1] = TypeBytes.SERVER_MESSAGE_TYPE_PONG;
                break;
            case DISCONNECT:
                rawBytes = new byte[2];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_SERVER;
                //entity messaage header
                rawBytes[1] = TypeBytes.SERVER_MESSAGE_TYPE_DISCONNECT;
                break;
        }
        serialized = true;
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        switch(this.messageType){
            case PING: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_SERVER);
                stream.write(TypeBytes.SERVER_MESSAGE_TYPE_PING);
                
                //
                //Write body of packet
            } break;
            case PONG: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_SERVER);
                stream.write(TypeBytes.SERVER_MESSAGE_TYPE_PONG);
                
                //
                //Write body of packet
            } break;
            case DISCONNECT: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_SERVER);
                stream.write(TypeBytes.SERVER_MESSAGE_TYPE_DISCONNECT);
                
                //
                //Write body of packet
            } break;
        }
    }

}
