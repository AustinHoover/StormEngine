package electrosphere.net.parser.net.message;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import electrosphere.net.parser.util.ByteStreamUtils;
import java.util.Map;
import java.util.function.BiConsumer;

public class CharacterMessage extends NetworkMessage {

    /**
     * The types of messages available in this category.
     */
    public enum CharacterMessageType {
        REQUESTCHARACTERLIST,
        RESPONSECHARACTERLIST,
        REQUESTCREATECHARACTER,
        RESPONSECREATECHARACTERSUCCESS,
        RESPONSECREATECHARACTERFAILURE,
        REQUESTSPAWNCHARACTER,
        RESPONSESPAWNCHARACTER,
        EDITORSWAP,
    }

    /**
     * The type of this message in particular.
     */
    CharacterMessageType messageType;
    String data;

    /**
     * Constructor
     * @param messageType The type of this message
     */
    private CharacterMessage(CharacterMessageType messageType){
        this.type = MessageType.CHARACTER_MESSAGE;
        this.messageType = messageType;
    }

    /**
     * Constructor
     */
    protected CharacterMessage(){
        this.type = MessageType.CHARACTER_MESSAGE;
    }

    public CharacterMessageType getMessageSubtype(){
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
     * Parses a message of type RequestCharacterList
     */
    public static CharacterMessage parseRequestCharacterListMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 0){
            return null;
        }
        CharacterMessage rVal = (CharacterMessage)pool.get(MessageType.CHARACTER_MESSAGE);
        rVal.messageType = CharacterMessageType.REQUESTCHARACTERLIST;
        return rVal;
    }

    /**
     * Constructs a message of type RequestCharacterList
     */
    public static CharacterMessage constructRequestCharacterListMessage(){
        CharacterMessage rVal = new CharacterMessage(CharacterMessageType.REQUESTCHARACTERLIST);
        return rVal;
    }

    /**
     * Parses a message of type ResponseCharacterList
     */
    public static CharacterMessage parseResponseCharacterListMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 4){
            return null;
        }
        int lenAccumulator = 0;
        int datalen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + datalen;
        if(byteBuffer.remaining() < 4 + lenAccumulator){
            return null;
        }
        CharacterMessage rVal = (CharacterMessage)pool.get(MessageType.CHARACTER_MESSAGE);
        rVal.messageType = CharacterMessageType.RESPONSECHARACTERLIST;
        if(datalen > 0){
            rVal.setdata(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, datalen));
        }
        return rVal;
    }

    /**
     * Constructs a message of type ResponseCharacterList
     */
    public static CharacterMessage constructResponseCharacterListMessage(String data){
        CharacterMessage rVal = new CharacterMessage(CharacterMessageType.RESPONSECHARACTERLIST);
        rVal.setdata(data);
        return rVal;
    }

    /**
     * Parses a message of type RequestCreateCharacter
     */
    public static CharacterMessage parseRequestCreateCharacterMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 4){
            return null;
        }
        int lenAccumulator = 0;
        int datalen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + datalen;
        if(byteBuffer.remaining() < 4 + lenAccumulator){
            return null;
        }
        CharacterMessage rVal = (CharacterMessage)pool.get(MessageType.CHARACTER_MESSAGE);
        rVal.messageType = CharacterMessageType.REQUESTCREATECHARACTER;
        if(datalen > 0){
            rVal.setdata(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, datalen));
        }
        return rVal;
    }

    /**
     * Constructs a message of type RequestCreateCharacter
     */
    public static CharacterMessage constructRequestCreateCharacterMessage(String data){
        CharacterMessage rVal = new CharacterMessage(CharacterMessageType.REQUESTCREATECHARACTER);
        rVal.setdata(data);
        return rVal;
    }

    /**
     * Parses a message of type ResponseCreateCharacterSuccess
     */
    public static CharacterMessage parseResponseCreateCharacterSuccessMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 0){
            return null;
        }
        CharacterMessage rVal = (CharacterMessage)pool.get(MessageType.CHARACTER_MESSAGE);
        rVal.messageType = CharacterMessageType.RESPONSECREATECHARACTERSUCCESS;
        return rVal;
    }

    /**
     * Constructs a message of type ResponseCreateCharacterSuccess
     */
    public static CharacterMessage constructResponseCreateCharacterSuccessMessage(){
        CharacterMessage rVal = new CharacterMessage(CharacterMessageType.RESPONSECREATECHARACTERSUCCESS);
        return rVal;
    }

    /**
     * Parses a message of type ResponseCreateCharacterFailure
     */
    public static CharacterMessage parseResponseCreateCharacterFailureMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 0){
            return null;
        }
        CharacterMessage rVal = (CharacterMessage)pool.get(MessageType.CHARACTER_MESSAGE);
        rVal.messageType = CharacterMessageType.RESPONSECREATECHARACTERFAILURE;
        return rVal;
    }

    /**
     * Constructs a message of type ResponseCreateCharacterFailure
     */
    public static CharacterMessage constructResponseCreateCharacterFailureMessage(){
        CharacterMessage rVal = new CharacterMessage(CharacterMessageType.RESPONSECREATECHARACTERFAILURE);
        return rVal;
    }

    /**
     * Parses a message of type RequestSpawnCharacter
     */
    public static CharacterMessage parseRequestSpawnCharacterMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 4){
            return null;
        }
        int lenAccumulator = 0;
        int datalen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + datalen;
        if(byteBuffer.remaining() < 4 + lenAccumulator){
            return null;
        }
        CharacterMessage rVal = (CharacterMessage)pool.get(MessageType.CHARACTER_MESSAGE);
        rVal.messageType = CharacterMessageType.REQUESTSPAWNCHARACTER;
        if(datalen > 0){
            rVal.setdata(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, datalen));
        }
        return rVal;
    }

    /**
     * Constructs a message of type RequestSpawnCharacter
     */
    public static CharacterMessage constructRequestSpawnCharacterMessage(String data){
        CharacterMessage rVal = new CharacterMessage(CharacterMessageType.REQUESTSPAWNCHARACTER);
        rVal.setdata(data);
        return rVal;
    }

    /**
     * Parses a message of type ResponseSpawnCharacter
     */
    public static CharacterMessage parseResponseSpawnCharacterMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 4){
            return null;
        }
        int lenAccumulator = 0;
        int datalen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + datalen;
        if(byteBuffer.remaining() < 4 + lenAccumulator){
            return null;
        }
        CharacterMessage rVal = (CharacterMessage)pool.get(MessageType.CHARACTER_MESSAGE);
        rVal.messageType = CharacterMessageType.RESPONSESPAWNCHARACTER;
        if(datalen > 0){
            rVal.setdata(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, datalen));
        }
        return rVal;
    }

    /**
     * Constructs a message of type ResponseSpawnCharacter
     */
    public static CharacterMessage constructResponseSpawnCharacterMessage(String data){
        CharacterMessage rVal = new CharacterMessage(CharacterMessageType.RESPONSESPAWNCHARACTER);
        rVal.setdata(data);
        return rVal;
    }

    /**
     * Parses a message of type EditorSwap
     */
    public static CharacterMessage parseEditorSwapMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 0){
            return null;
        }
        CharacterMessage rVal = (CharacterMessage)pool.get(MessageType.CHARACTER_MESSAGE);
        rVal.messageType = CharacterMessageType.EDITORSWAP;
        return rVal;
    }

    /**
     * Constructs a message of type EditorSwap
     */
    public static CharacterMessage constructEditorSwapMessage(){
        CharacterMessage rVal = new CharacterMessage(CharacterMessageType.EDITORSWAP);
        return rVal;
    }

    @Deprecated
    @Override
    void serialize(){
        byte[] intValues = new byte[8];
        byte[] stringBytes;
        switch(this.messageType){
            case REQUESTCHARACTERLIST:
                rawBytes = new byte[2];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_CHARACTER;
                //entity messaage header
                rawBytes[1] = TypeBytes.CHARACTER_MESSAGE_TYPE_REQUESTCHARACTERLIST;
                break;
            case RESPONSECHARACTERLIST:
                rawBytes = new byte[2+4+data.length()];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_CHARACTER;
                //entity messaage header
                rawBytes[1] = TypeBytes.CHARACTER_MESSAGE_TYPE_RESPONSECHARACTERLIST;
                intValues = ByteStreamUtils.serializeIntToBytes(data.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                stringBytes = data.getBytes();
                for(int i = 0; i < data.length(); i++){
                    rawBytes[6+i] = stringBytes[i];
                }
                break;
            case REQUESTCREATECHARACTER:
                rawBytes = new byte[2+4+data.length()];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_CHARACTER;
                //entity messaage header
                rawBytes[1] = TypeBytes.CHARACTER_MESSAGE_TYPE_REQUESTCREATECHARACTER;
                intValues = ByteStreamUtils.serializeIntToBytes(data.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                stringBytes = data.getBytes();
                for(int i = 0; i < data.length(); i++){
                    rawBytes[6+i] = stringBytes[i];
                }
                break;
            case RESPONSECREATECHARACTERSUCCESS:
                rawBytes = new byte[2];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_CHARACTER;
                //entity messaage header
                rawBytes[1] = TypeBytes.CHARACTER_MESSAGE_TYPE_RESPONSECREATECHARACTERSUCCESS;
                break;
            case RESPONSECREATECHARACTERFAILURE:
                rawBytes = new byte[2];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_CHARACTER;
                //entity messaage header
                rawBytes[1] = TypeBytes.CHARACTER_MESSAGE_TYPE_RESPONSECREATECHARACTERFAILURE;
                break;
            case REQUESTSPAWNCHARACTER:
                rawBytes = new byte[2+4+data.length()];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_CHARACTER;
                //entity messaage header
                rawBytes[1] = TypeBytes.CHARACTER_MESSAGE_TYPE_REQUESTSPAWNCHARACTER;
                intValues = ByteStreamUtils.serializeIntToBytes(data.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                stringBytes = data.getBytes();
                for(int i = 0; i < data.length(); i++){
                    rawBytes[6+i] = stringBytes[i];
                }
                break;
            case RESPONSESPAWNCHARACTER:
                rawBytes = new byte[2+4+data.length()];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_CHARACTER;
                //entity messaage header
                rawBytes[1] = TypeBytes.CHARACTER_MESSAGE_TYPE_RESPONSESPAWNCHARACTER;
                intValues = ByteStreamUtils.serializeIntToBytes(data.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                stringBytes = data.getBytes();
                for(int i = 0; i < data.length(); i++){
                    rawBytes[6+i] = stringBytes[i];
                }
                break;
            case EDITORSWAP:
                rawBytes = new byte[2];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_CHARACTER;
                //entity messaage header
                rawBytes[1] = TypeBytes.CHARACTER_MESSAGE_TYPE_EDITORSWAP;
                break;
        }
        serialized = true;
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        switch(this.messageType){
            case REQUESTCHARACTERLIST: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_CHARACTER);
                stream.write(TypeBytes.CHARACTER_MESSAGE_TYPE_REQUESTCHARACTERLIST);
                
                //
                //Write body of packet
            } break;
            case RESPONSECHARACTERLIST: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_CHARACTER);
                stream.write(TypeBytes.CHARACTER_MESSAGE_TYPE_RESPONSECHARACTERLIST);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, data.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeString(stream, data);
            } break;
            case REQUESTCREATECHARACTER: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_CHARACTER);
                stream.write(TypeBytes.CHARACTER_MESSAGE_TYPE_REQUESTCREATECHARACTER);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, data.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeString(stream, data);
            } break;
            case RESPONSECREATECHARACTERSUCCESS: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_CHARACTER);
                stream.write(TypeBytes.CHARACTER_MESSAGE_TYPE_RESPONSECREATECHARACTERSUCCESS);
                
                //
                //Write body of packet
            } break;
            case RESPONSECREATECHARACTERFAILURE: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_CHARACTER);
                stream.write(TypeBytes.CHARACTER_MESSAGE_TYPE_RESPONSECREATECHARACTERFAILURE);
                
                //
                //Write body of packet
            } break;
            case REQUESTSPAWNCHARACTER: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_CHARACTER);
                stream.write(TypeBytes.CHARACTER_MESSAGE_TYPE_REQUESTSPAWNCHARACTER);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, data.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeString(stream, data);
            } break;
            case RESPONSESPAWNCHARACTER: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_CHARACTER);
                stream.write(TypeBytes.CHARACTER_MESSAGE_TYPE_RESPONSESPAWNCHARACTER);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, data.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeString(stream, data);
            } break;
            case EDITORSWAP: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_CHARACTER);
                stream.write(TypeBytes.CHARACTER_MESSAGE_TYPE_EDITORSWAP);
                
                //
                //Write body of packet
            } break;
        }
    }

}
