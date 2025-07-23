package electrosphere.net.parser.net.message;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import electrosphere.net.parser.util.ByteStreamUtils;
import java.util.Map;
import java.util.function.BiConsumer;

public class LoreMessage extends NetworkMessage {

    /**
     * The types of messages available in this category.
     */
    public enum LoreMessageType {
        REQUESTRACES,
        RESPONSERACES,
        TEMPORALUPDATE,
    }

    /**
     * The type of this message in particular.
     */
    LoreMessageType messageType;
    String data;

    /**
     * Constructor
     * @param messageType The type of this message
     */
    private LoreMessage(LoreMessageType messageType){
        this.type = MessageType.LORE_MESSAGE;
        this.messageType = messageType;
    }

    /**
     * Constructor
     */
    protected LoreMessage(){
        this.type = MessageType.LORE_MESSAGE;
    }

    public LoreMessageType getMessageSubtype(){
        return this.messageType;
    }

    /**
     * Gets data
     */
    public String getdata() {
        return data;
    }

    /**
     * Sets data
     */
    public void setdata(String data) {
        this.data = data;
    }

    /**
     * Parses a message of type RequestRaces
     */
    public static LoreMessage parseRequestRacesMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 0){
            return null;
        }
        LoreMessage rVal = (LoreMessage)pool.get(MessageType.LORE_MESSAGE);
        rVal.messageType = LoreMessageType.REQUESTRACES;
        return rVal;
    }

    /**
     * Constructs a message of type RequestRaces
     */
    public static LoreMessage constructRequestRacesMessage(){
        LoreMessage rVal = new LoreMessage(LoreMessageType.REQUESTRACES);
        return rVal;
    }

    /**
     * Parses a message of type ResponseRaces
     */
    public static LoreMessage parseResponseRacesMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 4){
            return null;
        }
        int lenAccumulator = 0;
        int datalen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + datalen;
        if(byteBuffer.remaining() < 4 + lenAccumulator){
            return null;
        }
        LoreMessage rVal = (LoreMessage)pool.get(MessageType.LORE_MESSAGE);
        rVal.messageType = LoreMessageType.RESPONSERACES;
        if(datalen > 0){
            rVal.setdata(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, datalen));
        }
        return rVal;
    }

    /**
     * Constructs a message of type ResponseRaces
     */
    public static LoreMessage constructResponseRacesMessage(String data){
        LoreMessage rVal = new LoreMessage(LoreMessageType.RESPONSERACES);
        rVal.setdata(data);
        return rVal;
    }

    /**
     * Parses a message of type TemporalUpdate
     */
    public static LoreMessage parseTemporalUpdateMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 4){
            return null;
        }
        int lenAccumulator = 0;
        int datalen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + datalen;
        if(byteBuffer.remaining() < 4 + lenAccumulator){
            return null;
        }
        LoreMessage rVal = (LoreMessage)pool.get(MessageType.LORE_MESSAGE);
        rVal.messageType = LoreMessageType.TEMPORALUPDATE;
        if(datalen > 0){
            rVal.setdata(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, datalen));
        }
        return rVal;
    }

    /**
     * Constructs a message of type TemporalUpdate
     */
    public static LoreMessage constructTemporalUpdateMessage(String data){
        LoreMessage rVal = new LoreMessage(LoreMessageType.TEMPORALUPDATE);
        rVal.setdata(data);
        return rVal;
    }

    @Deprecated
    @Override
    void serialize(){
        byte[] intValues = new byte[8];
        byte[] stringBytes;
        switch(this.messageType){
            case REQUESTRACES:
                rawBytes = new byte[2];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_LORE;
                //entity messaage header
                rawBytes[1] = TypeBytes.LORE_MESSAGE_TYPE_REQUESTRACES;
                break;
            case RESPONSERACES:
                rawBytes = new byte[2+4+data.length()];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_LORE;
                //entity messaage header
                rawBytes[1] = TypeBytes.LORE_MESSAGE_TYPE_RESPONSERACES;
                intValues = ByteStreamUtils.serializeIntToBytes(data.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                stringBytes = data.getBytes();
                for(int i = 0; i < data.length(); i++){
                    rawBytes[6+i] = stringBytes[i];
                }
                break;
            case TEMPORALUPDATE:
                rawBytes = new byte[2+4+data.length()];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_LORE;
                //entity messaage header
                rawBytes[1] = TypeBytes.LORE_MESSAGE_TYPE_TEMPORALUPDATE;
                intValues = ByteStreamUtils.serializeIntToBytes(data.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                stringBytes = data.getBytes();
                for(int i = 0; i < data.length(); i++){
                    rawBytes[6+i] = stringBytes[i];
                }
                break;
        }
        serialized = true;
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        switch(this.messageType){
            case REQUESTRACES: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_LORE);
                stream.write(TypeBytes.LORE_MESSAGE_TYPE_REQUESTRACES);
                
                //
                //Write body of packet
            } break;
            case RESPONSERACES: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_LORE);
                stream.write(TypeBytes.LORE_MESSAGE_TYPE_RESPONSERACES);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, data.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeString(stream, data);
            } break;
            case TEMPORALUPDATE: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_LORE);
                stream.write(TypeBytes.LORE_MESSAGE_TYPE_TEMPORALUPDATE);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, data.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeString(stream, data);
            } break;
        }
    }

}
