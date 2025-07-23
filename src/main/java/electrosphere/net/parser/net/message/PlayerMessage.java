package electrosphere.net.parser.net.message;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import electrosphere.net.parser.util.ByteStreamUtils;
import java.util.Map;
import java.util.function.BiConsumer;

public class PlayerMessage extends NetworkMessage {

    /**
     * The types of messages available in this category.
     */
    public enum PlayerMessageType {
        SET_ID,
        SETINITIALDISCRETEPOSITION,
    }

    /**
     * The type of this message in particular.
     */
    PlayerMessageType messageType;
    int playerID;
    int initialDiscretePositionX;
    int initialDiscretePositionY;
    int initialDiscretePositionZ;

    /**
     * Constructor
     * @param messageType The type of this message
     */
    private PlayerMessage(PlayerMessageType messageType){
        this.type = MessageType.PLAYER_MESSAGE;
        this.messageType = messageType;
    }

    /**
     * Constructor
     */
    protected PlayerMessage(){
        this.type = MessageType.PLAYER_MESSAGE;
    }

    public PlayerMessageType getMessageSubtype(){
        return this.messageType;
    }

    /**
     * Gets playerID
     */
    public int getplayerID() {
        return playerID;
    }

    /**
     * Sets playerID
     */
    public void setplayerID(int playerID) {
        this.playerID = playerID;
    }

    /**
     * Gets initialDiscretePositionX
     */
    public int getinitialDiscretePositionX() {
        return initialDiscretePositionX;
    }

    /**
     * Sets initialDiscretePositionX
     */
    public void setinitialDiscretePositionX(int initialDiscretePositionX) {
        this.initialDiscretePositionX = initialDiscretePositionX;
    }

    /**
     * Gets initialDiscretePositionY
     */
    public int getinitialDiscretePositionY() {
        return initialDiscretePositionY;
    }

    /**
     * Sets initialDiscretePositionY
     */
    public void setinitialDiscretePositionY(int initialDiscretePositionY) {
        this.initialDiscretePositionY = initialDiscretePositionY;
    }

    /**
     * Gets initialDiscretePositionZ
     */
    public int getinitialDiscretePositionZ() {
        return initialDiscretePositionZ;
    }

    /**
     * Sets initialDiscretePositionZ
     */
    public void setinitialDiscretePositionZ(int initialDiscretePositionZ) {
        this.initialDiscretePositionZ = initialDiscretePositionZ;
    }

    /**
     * Parses a message of type Set_ID
     */
    public static PlayerMessage parseSet_IDMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 4){
            return null;
        }
        PlayerMessage rVal = (PlayerMessage)pool.get(MessageType.PLAYER_MESSAGE);
        rVal.messageType = PlayerMessageType.SET_ID;
        rVal.setplayerID(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type Set_ID
     */
    public static PlayerMessage constructSet_IDMessage(int playerID){
        PlayerMessage rVal = new PlayerMessage(PlayerMessageType.SET_ID);
        rVal.setplayerID(playerID);
        return rVal;
    }

    /**
     * Parses a message of type SetInitialDiscretePosition
     */
    public static PlayerMessage parseSetInitialDiscretePositionMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 12){
            return null;
        }
        PlayerMessage rVal = (PlayerMessage)pool.get(MessageType.PLAYER_MESSAGE);
        rVal.messageType = PlayerMessageType.SETINITIALDISCRETEPOSITION;
        rVal.setinitialDiscretePositionX(byteBuffer.getInt());
        rVal.setinitialDiscretePositionY(byteBuffer.getInt());
        rVal.setinitialDiscretePositionZ(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type SetInitialDiscretePosition
     */
    public static PlayerMessage constructSetInitialDiscretePositionMessage(int initialDiscretePositionX,int initialDiscretePositionY,int initialDiscretePositionZ){
        PlayerMessage rVal = new PlayerMessage(PlayerMessageType.SETINITIALDISCRETEPOSITION);
        rVal.setinitialDiscretePositionX(initialDiscretePositionX);
        rVal.setinitialDiscretePositionY(initialDiscretePositionY);
        rVal.setinitialDiscretePositionZ(initialDiscretePositionZ);
        return rVal;
    }

    @Deprecated
    @Override
    void serialize(){
        byte[] intValues = new byte[8];
        switch(this.messageType){
            case SET_ID:
                rawBytes = new byte[2+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_PLAYER;
                //entity messaage header
                rawBytes[1] = TypeBytes.PLAYER_MESSAGE_TYPE_SET_ID;
                intValues = ByteStreamUtils.serializeIntToBytes(playerID);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                break;
            case SETINITIALDISCRETEPOSITION:
                rawBytes = new byte[2+4+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_PLAYER;
                //entity messaage header
                rawBytes[1] = TypeBytes.PLAYER_MESSAGE_TYPE_SETINITIALDISCRETEPOSITION;
                intValues = ByteStreamUtils.serializeIntToBytes(initialDiscretePositionX);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(initialDiscretePositionY);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(initialDiscretePositionZ);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                break;
        }
        serialized = true;
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        switch(this.messageType){
            case SET_ID: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_PLAYER);
                stream.write(TypeBytes.PLAYER_MESSAGE_TYPE_SET_ID);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, playerID);
            } break;
            case SETINITIALDISCRETEPOSITION: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_PLAYER);
                stream.write(TypeBytes.PLAYER_MESSAGE_TYPE_SETINITIALDISCRETEPOSITION);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, initialDiscretePositionX);
                ByteStreamUtils.writeInt(stream, initialDiscretePositionY);
                ByteStreamUtils.writeInt(stream, initialDiscretePositionZ);
            } break;
        }
    }

}
