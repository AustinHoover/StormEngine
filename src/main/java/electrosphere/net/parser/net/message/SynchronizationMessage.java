package electrosphere.net.parser.net.message;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import electrosphere.net.parser.util.ByteStreamUtils;
import java.util.Map;
import java.util.function.BiConsumer;

public class SynchronizationMessage extends NetworkMessage {

    /**
     * The types of messages available in this category.
     */
    public enum SynchronizationMessageType {
        UPDATECLIENTSTATE,
        UPDATECLIENTSTRINGSTATE,
        UPDATECLIENTINTSTATE,
        UPDATECLIENTLONGSTATE,
        UPDATECLIENTFLOATSTATE,
        UPDATECLIENTDOUBLESTATE,
        CLIENTREQUESTBTREEACTION,
        SERVERNOTIFYBTREETRANSITION,
        ATTACHTREE,
        DETATCHTREE,
        LOADSCENE,
    }

    /**
     * The type of this message in particular.
     */
    SynchronizationMessageType messageType;
    int entityId;
    int bTreeId;
    int fieldId;
    int bTreeValue;
    String stringValue;
    int intValue;
    long longValue;
    float floatValue;
    double doubleValue;

    /**
     * Constructor
     * @param messageType The type of this message
     */
    private SynchronizationMessage(SynchronizationMessageType messageType){
        this.type = MessageType.SYNCHRONIZATION_MESSAGE;
        this.messageType = messageType;
    }

    /**
     * Constructor
     */
    protected SynchronizationMessage(){
        this.type = MessageType.SYNCHRONIZATION_MESSAGE;
    }

    public SynchronizationMessageType getMessageSubtype(){
        return this.messageType;
    }

    /**
     * Gets entityId
     */
    public int getentityId() {
        return entityId;
    }

    /**
     * Sets entityId
     */
    public void setentityId(int entityId) {
        this.entityId = entityId;
    }

    /**
     * Gets bTreeId
     */
    public int getbTreeId() {
        return bTreeId;
    }

    /**
     * Sets bTreeId
     */
    public void setbTreeId(int bTreeId) {
        this.bTreeId = bTreeId;
    }

    /**
     * Gets fieldId
     */
    public int getfieldId() {
        return fieldId;
    }

    /**
     * Sets fieldId
     */
    public void setfieldId(int fieldId) {
        this.fieldId = fieldId;
    }

    /**
     * Gets bTreeValue
     */
    public int getbTreeValue() {
        return bTreeValue;
    }

    /**
     * Sets bTreeValue
     */
    public void setbTreeValue(int bTreeValue) {
        this.bTreeValue = bTreeValue;
    }

    /**
     * Gets stringValue
     */
    public String getstringValue() {
        return stringValue;
    }

    /**
     * Sets stringValue
     */
    public void setstringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    /**
     * Gets intValue
     */
    public int getintValue() {
        return intValue;
    }

    /**
     * Sets intValue
     */
    public void setintValue(int intValue) {
        this.intValue = intValue;
    }

    /**
     * Gets longValue
     */
    public long getlongValue() {
        return longValue;
    }

    /**
     * Sets longValue
     */
    public void setlongValue(long longValue) {
        this.longValue = longValue;
    }

    /**
     * Gets floatValue
     */
    public float getfloatValue() {
        return floatValue;
    }

    /**
     * Sets floatValue
     */
    public void setfloatValue(float floatValue) {
        this.floatValue = floatValue;
    }

    /**
     * Gets doubleValue
     */
    public double getdoubleValue() {
        return doubleValue;
    }

    /**
     * Sets doubleValue
     */
    public void setdoubleValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }

    /**
     * Parses a message of type UpdateClientState
     */
    public static SynchronizationMessage parseUpdateClientStateMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 16){
            return null;
        }
        SynchronizationMessage rVal = (SynchronizationMessage)pool.get(MessageType.SYNCHRONIZATION_MESSAGE);
        rVal.messageType = SynchronizationMessageType.UPDATECLIENTSTATE;
        rVal.setentityId(byteBuffer.getInt());
        rVal.setbTreeId(byteBuffer.getInt());
        rVal.setfieldId(byteBuffer.getInt());
        rVal.setbTreeValue(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type UpdateClientState
     */
    public static SynchronizationMessage constructUpdateClientStateMessage(int entityId,int bTreeId,int fieldId,int bTreeValue){
        SynchronizationMessage rVal = new SynchronizationMessage(SynchronizationMessageType.UPDATECLIENTSTATE);
        rVal.setentityId(entityId);
        rVal.setbTreeId(bTreeId);
        rVal.setfieldId(fieldId);
        rVal.setbTreeValue(bTreeValue);
        return rVal;
    }

    /**
     * Parses a message of type UpdateClientStringState
     */
    public static SynchronizationMessage parseUpdateClientStringStateMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 16){
            return null;
        }
        int lenAccumulator = 0;
        int stringValuelen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + stringValuelen;
        if(byteBuffer.remaining() < 16 + lenAccumulator){
            return null;
        }
        SynchronizationMessage rVal = (SynchronizationMessage)pool.get(MessageType.SYNCHRONIZATION_MESSAGE);
        rVal.messageType = SynchronizationMessageType.UPDATECLIENTSTRINGSTATE;
        rVal.setentityId(byteBuffer.getInt());
        rVal.setbTreeId(byteBuffer.getInt());
        rVal.setfieldId(byteBuffer.getInt());
        if(stringValuelen > 0){
            rVal.setstringValue(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, stringValuelen));
        }
        return rVal;
    }

    /**
     * Constructs a message of type UpdateClientStringState
     */
    public static SynchronizationMessage constructUpdateClientStringStateMessage(int entityId,int bTreeId,int fieldId,String stringValue){
        SynchronizationMessage rVal = new SynchronizationMessage(SynchronizationMessageType.UPDATECLIENTSTRINGSTATE);
        rVal.setentityId(entityId);
        rVal.setbTreeId(bTreeId);
        rVal.setfieldId(fieldId);
        rVal.setstringValue(stringValue);
        return rVal;
    }

    /**
     * Parses a message of type UpdateClientIntState
     */
    public static SynchronizationMessage parseUpdateClientIntStateMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 16){
            return null;
        }
        SynchronizationMessage rVal = (SynchronizationMessage)pool.get(MessageType.SYNCHRONIZATION_MESSAGE);
        rVal.messageType = SynchronizationMessageType.UPDATECLIENTINTSTATE;
        rVal.setentityId(byteBuffer.getInt());
        rVal.setbTreeId(byteBuffer.getInt());
        rVal.setfieldId(byteBuffer.getInt());
        rVal.setintValue(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type UpdateClientIntState
     */
    public static SynchronizationMessage constructUpdateClientIntStateMessage(int entityId,int bTreeId,int fieldId,int intValue){
        SynchronizationMessage rVal = new SynchronizationMessage(SynchronizationMessageType.UPDATECLIENTINTSTATE);
        rVal.setentityId(entityId);
        rVal.setbTreeId(bTreeId);
        rVal.setfieldId(fieldId);
        rVal.setintValue(intValue);
        return rVal;
    }

    /**
     * Parses a message of type UpdateClientLongState
     */
    public static SynchronizationMessage parseUpdateClientLongStateMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 20){
            return null;
        }
        SynchronizationMessage rVal = (SynchronizationMessage)pool.get(MessageType.SYNCHRONIZATION_MESSAGE);
        rVal.messageType = SynchronizationMessageType.UPDATECLIENTLONGSTATE;
        rVal.setentityId(byteBuffer.getInt());
        rVal.setbTreeId(byteBuffer.getInt());
        rVal.setfieldId(byteBuffer.getInt());
        rVal.setlongValue(byteBuffer.getLong());
        return rVal;
    }

    /**
     * Constructs a message of type UpdateClientLongState
     */
    public static SynchronizationMessage constructUpdateClientLongStateMessage(int entityId,int bTreeId,int fieldId,long longValue){
        SynchronizationMessage rVal = new SynchronizationMessage(SynchronizationMessageType.UPDATECLIENTLONGSTATE);
        rVal.setentityId(entityId);
        rVal.setbTreeId(bTreeId);
        rVal.setfieldId(fieldId);
        rVal.setlongValue(longValue);
        return rVal;
    }

    /**
     * Parses a message of type UpdateClientFloatState
     */
    public static SynchronizationMessage parseUpdateClientFloatStateMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 16){
            return null;
        }
        SynchronizationMessage rVal = (SynchronizationMessage)pool.get(MessageType.SYNCHRONIZATION_MESSAGE);
        rVal.messageType = SynchronizationMessageType.UPDATECLIENTFLOATSTATE;
        rVal.setentityId(byteBuffer.getInt());
        rVal.setbTreeId(byteBuffer.getInt());
        rVal.setfieldId(byteBuffer.getInt());
        rVal.setfloatValue(byteBuffer.getFloat());
        return rVal;
    }

    /**
     * Constructs a message of type UpdateClientFloatState
     */
    public static SynchronizationMessage constructUpdateClientFloatStateMessage(int entityId,int bTreeId,int fieldId,float floatValue){
        SynchronizationMessage rVal = new SynchronizationMessage(SynchronizationMessageType.UPDATECLIENTFLOATSTATE);
        rVal.setentityId(entityId);
        rVal.setbTreeId(bTreeId);
        rVal.setfieldId(fieldId);
        rVal.setfloatValue(floatValue);
        return rVal;
    }

    /**
     * Parses a message of type UpdateClientDoubleState
     */
    public static SynchronizationMessage parseUpdateClientDoubleStateMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 20){
            return null;
        }
        SynchronizationMessage rVal = (SynchronizationMessage)pool.get(MessageType.SYNCHRONIZATION_MESSAGE);
        rVal.messageType = SynchronizationMessageType.UPDATECLIENTDOUBLESTATE;
        rVal.setentityId(byteBuffer.getInt());
        rVal.setbTreeId(byteBuffer.getInt());
        rVal.setfieldId(byteBuffer.getInt());
        rVal.setdoubleValue(byteBuffer.getDouble());
        return rVal;
    }

    /**
     * Constructs a message of type UpdateClientDoubleState
     */
    public static SynchronizationMessage constructUpdateClientDoubleStateMessage(int entityId,int bTreeId,int fieldId,double doubleValue){
        SynchronizationMessage rVal = new SynchronizationMessage(SynchronizationMessageType.UPDATECLIENTDOUBLESTATE);
        rVal.setentityId(entityId);
        rVal.setbTreeId(bTreeId);
        rVal.setfieldId(fieldId);
        rVal.setdoubleValue(doubleValue);
        return rVal;
    }

    /**
     * Parses a message of type ClientRequestBTreeAction
     */
    public static SynchronizationMessage parseClientRequestBTreeActionMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 12){
            return null;
        }
        SynchronizationMessage rVal = (SynchronizationMessage)pool.get(MessageType.SYNCHRONIZATION_MESSAGE);
        rVal.messageType = SynchronizationMessageType.CLIENTREQUESTBTREEACTION;
        rVal.setentityId(byteBuffer.getInt());
        rVal.setbTreeId(byteBuffer.getInt());
        rVal.setbTreeValue(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type ClientRequestBTreeAction
     */
    public static SynchronizationMessage constructClientRequestBTreeActionMessage(int entityId,int bTreeId,int bTreeValue){
        SynchronizationMessage rVal = new SynchronizationMessage(SynchronizationMessageType.CLIENTREQUESTBTREEACTION);
        rVal.setentityId(entityId);
        rVal.setbTreeId(bTreeId);
        rVal.setbTreeValue(bTreeValue);
        return rVal;
    }

    /**
     * Parses a message of type ServerNotifyBTreeTransition
     */
    public static SynchronizationMessage parseServerNotifyBTreeTransitionMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 16){
            return null;
        }
        SynchronizationMessage rVal = (SynchronizationMessage)pool.get(MessageType.SYNCHRONIZATION_MESSAGE);
        rVal.messageType = SynchronizationMessageType.SERVERNOTIFYBTREETRANSITION;
        rVal.setentityId(byteBuffer.getInt());
        rVal.setbTreeId(byteBuffer.getInt());
        rVal.setfieldId(byteBuffer.getInt());
        rVal.setbTreeValue(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type ServerNotifyBTreeTransition
     */
    public static SynchronizationMessage constructServerNotifyBTreeTransitionMessage(int entityId,int bTreeId,int fieldId,int bTreeValue){
        SynchronizationMessage rVal = new SynchronizationMessage(SynchronizationMessageType.SERVERNOTIFYBTREETRANSITION);
        rVal.setentityId(entityId);
        rVal.setbTreeId(bTreeId);
        rVal.setfieldId(fieldId);
        rVal.setbTreeValue(bTreeValue);
        return rVal;
    }

    /**
     * Parses a message of type AttachTree
     */
    public static SynchronizationMessage parseAttachTreeMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 8){
            return null;
        }
        SynchronizationMessage rVal = (SynchronizationMessage)pool.get(MessageType.SYNCHRONIZATION_MESSAGE);
        rVal.messageType = SynchronizationMessageType.ATTACHTREE;
        rVal.setentityId(byteBuffer.getInt());
        rVal.setbTreeId(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type AttachTree
     */
    public static SynchronizationMessage constructAttachTreeMessage(int entityId,int bTreeId){
        SynchronizationMessage rVal = new SynchronizationMessage(SynchronizationMessageType.ATTACHTREE);
        rVal.setentityId(entityId);
        rVal.setbTreeId(bTreeId);
        return rVal;
    }

    /**
     * Parses a message of type DetatchTree
     */
    public static SynchronizationMessage parseDetatchTreeMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 8){
            return null;
        }
        SynchronizationMessage rVal = (SynchronizationMessage)pool.get(MessageType.SYNCHRONIZATION_MESSAGE);
        rVal.messageType = SynchronizationMessageType.DETATCHTREE;
        rVal.setentityId(byteBuffer.getInt());
        rVal.setbTreeId(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type DetatchTree
     */
    public static SynchronizationMessage constructDetatchTreeMessage(int entityId,int bTreeId){
        SynchronizationMessage rVal = new SynchronizationMessage(SynchronizationMessageType.DETATCHTREE);
        rVal.setentityId(entityId);
        rVal.setbTreeId(bTreeId);
        return rVal;
    }

    /**
     * Parses a message of type LoadScene
     */
    public static SynchronizationMessage parseLoadSceneMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 4){
            return null;
        }
        int lenAccumulator = 0;
        int stringValuelen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + stringValuelen;
        if(byteBuffer.remaining() < 4 + lenAccumulator){
            return null;
        }
        SynchronizationMessage rVal = (SynchronizationMessage)pool.get(MessageType.SYNCHRONIZATION_MESSAGE);
        rVal.messageType = SynchronizationMessageType.LOADSCENE;
        if(stringValuelen > 0){
            rVal.setstringValue(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, stringValuelen));
        }
        return rVal;
    }

    /**
     * Constructs a message of type LoadScene
     */
    public static SynchronizationMessage constructLoadSceneMessage(String stringValue){
        SynchronizationMessage rVal = new SynchronizationMessage(SynchronizationMessageType.LOADSCENE);
        rVal.setstringValue(stringValue);
        return rVal;
    }

    @Deprecated
    @Override
    void serialize(){
        byte[] intValues = new byte[8];
        byte[] stringBytes;
        switch(this.messageType){
            case UPDATECLIENTSTATE:
                rawBytes = new byte[2+4+4+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION;
                //entity messaage header
                rawBytes[1] = TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTSTATE;
                intValues = ByteStreamUtils.serializeIntToBytes(entityId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(bTreeId);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(fieldId);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(bTreeValue);
                for(int i = 0; i < 4; i++){
                    rawBytes[14+i] = intValues[i];
                }
                break;
            case UPDATECLIENTSTRINGSTATE:
                rawBytes = new byte[2+4+4+4+4+stringValue.length()];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION;
                //entity messaage header
                rawBytes[1] = TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTSTRINGSTATE;
                intValues = ByteStreamUtils.serializeIntToBytes(entityId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(bTreeId);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(fieldId);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(stringValue.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[14+i] = intValues[i];
                }
                stringBytes = stringValue.getBytes();
                for(int i = 0; i < stringValue.length(); i++){
                    rawBytes[18+i] = stringBytes[i];
                }
                break;
            case UPDATECLIENTINTSTATE:
                rawBytes = new byte[2+4+4+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION;
                //entity messaage header
                rawBytes[1] = TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTINTSTATE;
                intValues = ByteStreamUtils.serializeIntToBytes(entityId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(bTreeId);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(fieldId);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(intValue);
                for(int i = 0; i < 4; i++){
                    rawBytes[14+i] = intValues[i];
                }
                break;
            case UPDATECLIENTLONGSTATE:
                rawBytes = new byte[2+4+4+4+8];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION;
                //entity messaage header
                rawBytes[1] = TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTLONGSTATE;
                intValues = ByteStreamUtils.serializeIntToBytes(entityId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(bTreeId);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(fieldId);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeLongToBytes(longValue);
                for(int i = 0; i < 8; i++){
                    rawBytes[14+i] = intValues[i];
                }
                break;
            case UPDATECLIENTFLOATSTATE:
                rawBytes = new byte[2+4+4+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION;
                //entity messaage header
                rawBytes[1] = TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTFLOATSTATE;
                intValues = ByteStreamUtils.serializeIntToBytes(entityId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(bTreeId);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(fieldId);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeFloatToBytes(floatValue);
                for(int i = 0; i < 4; i++){
                    rawBytes[14+i] = intValues[i];
                }                break;
            case UPDATECLIENTDOUBLESTATE:
                rawBytes = new byte[2+4+4+4+8];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION;
                //entity messaage header
                rawBytes[1] = TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTDOUBLESTATE;
                intValues = ByteStreamUtils.serializeIntToBytes(entityId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(bTreeId);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(fieldId);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(doubleValue);
                for(int i = 0; i < 8; i++){
                    rawBytes[14+i] = intValues[i];
                }
                break;
            case CLIENTREQUESTBTREEACTION:
                rawBytes = new byte[2+4+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION;
                //entity messaage header
                rawBytes[1] = TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_CLIENTREQUESTBTREEACTION;
                intValues = ByteStreamUtils.serializeIntToBytes(entityId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(bTreeId);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(bTreeValue);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                break;
            case SERVERNOTIFYBTREETRANSITION:
                rawBytes = new byte[2+4+4+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION;
                //entity messaage header
                rawBytes[1] = TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_SERVERNOTIFYBTREETRANSITION;
                intValues = ByteStreamUtils.serializeIntToBytes(entityId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(bTreeId);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(fieldId);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(bTreeValue);
                for(int i = 0; i < 4; i++){
                    rawBytes[14+i] = intValues[i];
                }
                break;
            case ATTACHTREE:
                rawBytes = new byte[2+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION;
                //entity messaage header
                rawBytes[1] = TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_ATTACHTREE;
                intValues = ByteStreamUtils.serializeIntToBytes(entityId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(bTreeId);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                break;
            case DETATCHTREE:
                rawBytes = new byte[2+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION;
                //entity messaage header
                rawBytes[1] = TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_DETATCHTREE;
                intValues = ByteStreamUtils.serializeIntToBytes(entityId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(bTreeId);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                break;
            case LOADSCENE:
                rawBytes = new byte[2+4+stringValue.length()];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION;
                //entity messaage header
                rawBytes[1] = TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_LOADSCENE;
                intValues = ByteStreamUtils.serializeIntToBytes(stringValue.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                stringBytes = stringValue.getBytes();
                for(int i = 0; i < stringValue.length(); i++){
                    rawBytes[6+i] = stringBytes[i];
                }
                break;
        }
        serialized = true;
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        switch(this.messageType){
            case UPDATECLIENTSTATE: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION);
                stream.write(TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTSTATE);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityId);
                ByteStreamUtils.writeInt(stream, bTreeId);
                ByteStreamUtils.writeInt(stream, fieldId);
                ByteStreamUtils.writeInt(stream, bTreeValue);
            } break;
            case UPDATECLIENTSTRINGSTATE: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION);
                stream.write(TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTSTRINGSTATE);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, stringValue.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityId);
                ByteStreamUtils.writeInt(stream, bTreeId);
                ByteStreamUtils.writeInt(stream, fieldId);
                ByteStreamUtils.writeString(stream, stringValue);
            } break;
            case UPDATECLIENTINTSTATE: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION);
                stream.write(TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTINTSTATE);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityId);
                ByteStreamUtils.writeInt(stream, bTreeId);
                ByteStreamUtils.writeInt(stream, fieldId);
                ByteStreamUtils.writeInt(stream, intValue);
            } break;
            case UPDATECLIENTLONGSTATE: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION);
                stream.write(TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTLONGSTATE);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityId);
                ByteStreamUtils.writeInt(stream, bTreeId);
                ByteStreamUtils.writeInt(stream, fieldId);
                ByteStreamUtils.writeLong(stream, longValue);
            } break;
            case UPDATECLIENTFLOATSTATE: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION);
                stream.write(TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTFLOATSTATE);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityId);
                ByteStreamUtils.writeInt(stream, bTreeId);
                ByteStreamUtils.writeInt(stream, fieldId);
                ByteStreamUtils.writeFloat(stream, floatValue);
            } break;
            case UPDATECLIENTDOUBLESTATE: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION);
                stream.write(TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTDOUBLESTATE);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityId);
                ByteStreamUtils.writeInt(stream, bTreeId);
                ByteStreamUtils.writeInt(stream, fieldId);
                ByteStreamUtils.writeDouble(stream, doubleValue);
            } break;
            case CLIENTREQUESTBTREEACTION: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION);
                stream.write(TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_CLIENTREQUESTBTREEACTION);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityId);
                ByteStreamUtils.writeInt(stream, bTreeId);
                ByteStreamUtils.writeInt(stream, bTreeValue);
            } break;
            case SERVERNOTIFYBTREETRANSITION: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION);
                stream.write(TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_SERVERNOTIFYBTREETRANSITION);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityId);
                ByteStreamUtils.writeInt(stream, bTreeId);
                ByteStreamUtils.writeInt(stream, fieldId);
                ByteStreamUtils.writeInt(stream, bTreeValue);
            } break;
            case ATTACHTREE: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION);
                stream.write(TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_ATTACHTREE);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityId);
                ByteStreamUtils.writeInt(stream, bTreeId);
            } break;
            case DETATCHTREE: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION);
                stream.write(TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_DETATCHTREE);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityId);
                ByteStreamUtils.writeInt(stream, bTreeId);
            } break;
            case LOADSCENE: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION);
                stream.write(TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_LOADSCENE);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, stringValue.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeString(stream, stringValue);
            } break;
        }
    }

}
