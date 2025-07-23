package electrosphere.net.parser.net.message;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import electrosphere.net.parser.util.ByteStreamUtils;
import java.util.Map;
import java.util.function.BiConsumer;

public class CombatMessage extends NetworkMessage {

    /**
     * The types of messages available in this category.
     */
    public enum CombatMessageType {
        SERVERREPORTHITBOXCOLLISION,
    }

    /**
     * The type of this message in particular.
     */
    CombatMessageType messageType;
    int entityID;
    int receiverEntityID;
    double positionX;
    double positionY;
    double positionZ;
    double rotationX;
    double rotationY;
    double rotationZ;
    double rotationW;
    long time;
    String hitboxType;
    String hurtboxType;

    /**
     * Constructor
     * @param messageType The type of this message
     */
    private CombatMessage(CombatMessageType messageType){
        this.type = MessageType.COMBAT_MESSAGE;
        this.messageType = messageType;
    }

    /**
     * Constructor
     */
    protected CombatMessage(){
        this.type = MessageType.COMBAT_MESSAGE;
    }

    public CombatMessageType getMessageSubtype(){
        return this.messageType;
    }

    /**
     * Gets entityID
     */
    public int getentityID() {
        return entityID;
    }

    /**
     * Sets entityID
     */
    public void setentityID(int entityID) {
        this.entityID = entityID;
    }

    /**
     * Gets receiverEntityID
     */
    public int getreceiverEntityID() {
        return receiverEntityID;
    }

    /**
     * Sets receiverEntityID
     */
    public void setreceiverEntityID(int receiverEntityID) {
        this.receiverEntityID = receiverEntityID;
    }

    /**
     * Gets positionX
     */
    public double getpositionX() {
        return positionX;
    }

    /**
     * Sets positionX
     */
    public void setpositionX(double positionX) {
        this.positionX = positionX;
    }

    /**
     * Gets positionY
     */
    public double getpositionY() {
        return positionY;
    }

    /**
     * Sets positionY
     */
    public void setpositionY(double positionY) {
        this.positionY = positionY;
    }

    /**
     * Gets positionZ
     */
    public double getpositionZ() {
        return positionZ;
    }

    /**
     * Sets positionZ
     */
    public void setpositionZ(double positionZ) {
        this.positionZ = positionZ;
    }

    /**
     * Gets rotationX
     */
    public double getrotationX() {
        return rotationX;
    }

    /**
     * Sets rotationX
     */
    public void setrotationX(double rotationX) {
        this.rotationX = rotationX;
    }

    /**
     * Gets rotationY
     */
    public double getrotationY() {
        return rotationY;
    }

    /**
     * Sets rotationY
     */
    public void setrotationY(double rotationY) {
        this.rotationY = rotationY;
    }

    /**
     * Gets rotationZ
     */
    public double getrotationZ() {
        return rotationZ;
    }

    /**
     * Sets rotationZ
     */
    public void setrotationZ(double rotationZ) {
        this.rotationZ = rotationZ;
    }

    /**
     * Gets rotationW
     */
    public double getrotationW() {
        return rotationW;
    }

    /**
     * Sets rotationW
     */
    public void setrotationW(double rotationW) {
        this.rotationW = rotationW;
    }

    /**
     * Gets time
     */
    public long gettime() {
        return time;
    }

    /**
     * Sets time
     */
    public void settime(long time) {
        this.time = time;
    }

    /**
     * Gets hitboxType
     */
    public String gethitboxType() {
        return hitboxType;
    }

    /**
     * Sets hitboxType
     */
    public void sethitboxType(String hitboxType) {
        this.hitboxType = hitboxType;
    }

    /**
     * Gets hurtboxType
     */
    public String gethurtboxType() {
        return hurtboxType;
    }

    /**
     * Sets hurtboxType
     */
    public void sethurtboxType(String hurtboxType) {
        this.hurtboxType = hurtboxType;
    }

    /**
     * Parses a message of type serverReportHitboxCollision
     */
    public static CombatMessage parseserverReportHitboxCollisionMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 48){
            return null;
        }
        int lenAccumulator = 0;
        int hitboxTypelen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + hitboxTypelen;
        int hurtboxTypelen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + hurtboxTypelen;
        if(byteBuffer.remaining() < 48 + lenAccumulator){
            return null;
        }
        CombatMessage rVal = (CombatMessage)pool.get(MessageType.COMBAT_MESSAGE);
        rVal.messageType = CombatMessageType.SERVERREPORTHITBOXCOLLISION;
        rVal.setentityID(byteBuffer.getInt());
        rVal.setreceiverEntityID(byteBuffer.getInt());
        rVal.settime(byteBuffer.getLong());
        if(hitboxTypelen > 0){
            rVal.sethitboxType(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, hitboxTypelen));
        }
        if(hurtboxTypelen > 0){
            rVal.sethurtboxType(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, hurtboxTypelen));
        }
        rVal.setpositionX(byteBuffer.getDouble());
        rVal.setpositionY(byteBuffer.getDouble());
        rVal.setpositionZ(byteBuffer.getDouble());
        return rVal;
    }

    /**
     * Constructs a message of type serverReportHitboxCollision
     */
    public static CombatMessage constructserverReportHitboxCollisionMessage(int entityID,int receiverEntityID,long time,String hitboxType,String hurtboxType,double positionX,double positionY,double positionZ){
        CombatMessage rVal = new CombatMessage(CombatMessageType.SERVERREPORTHITBOXCOLLISION);
        rVal.setentityID(entityID);
        rVal.setreceiverEntityID(receiverEntityID);
        rVal.settime(time);
        rVal.sethitboxType(hitboxType);
        rVal.sethurtboxType(hurtboxType);
        rVal.setpositionX(positionX);
        rVal.setpositionY(positionY);
        rVal.setpositionZ(positionZ);
        return rVal;
    }

    @Deprecated
    @Override
    void serialize(){
        byte[] intValues = new byte[8];
        byte[] stringBytes;
        switch(this.messageType){
            case SERVERREPORTHITBOXCOLLISION:
                rawBytes = new byte[2+4+4+8+4+hitboxType.length()+4+hurtboxType.length()+8+8+8];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_COMBAT;
                //entity messaage header
                rawBytes[1] = TypeBytes.COMBAT_MESSAGE_TYPE_SERVERREPORTHITBOXCOLLISION;
                intValues = ByteStreamUtils.serializeIntToBytes(entityID);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(receiverEntityID);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeLongToBytes(time);
                for(int i = 0; i < 8; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(hitboxType.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[18+i] = intValues[i];
                }
                stringBytes = hitboxType.getBytes();
                for(int i = 0; i < hitboxType.length(); i++){
                    rawBytes[22+i] = stringBytes[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(hurtboxType.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[22+hitboxType.length()+i] = intValues[i];
                }
                stringBytes = hurtboxType.getBytes();
                for(int i = 0; i < hurtboxType.length(); i++){
                    rawBytes[26+hitboxType.length()+i] = stringBytes[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(positionX);
                for(int i = 0; i < 8; i++){
                    rawBytes[26+hitboxType.length()+hurtboxType.length()+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(positionY);
                for(int i = 0; i < 8; i++){
                    rawBytes[34+hitboxType.length()+hurtboxType.length()+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(positionZ);
                for(int i = 0; i < 8; i++){
                    rawBytes[42+hitboxType.length()+hurtboxType.length()+i] = intValues[i];
                }
                break;
        }
        serialized = true;
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        switch(this.messageType){
            case SERVERREPORTHITBOXCOLLISION: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_COMBAT);
                stream.write(TypeBytes.COMBAT_MESSAGE_TYPE_SERVERREPORTHITBOXCOLLISION);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, hitboxType.getBytes().length);
                ByteStreamUtils.writeInt(stream, hurtboxType.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityID);
                ByteStreamUtils.writeInt(stream, receiverEntityID);
                ByteStreamUtils.writeLong(stream, time);
                ByteStreamUtils.writeString(stream, hitboxType);
                ByteStreamUtils.writeString(stream, hurtboxType);
                ByteStreamUtils.writeDouble(stream, positionX);
                ByteStreamUtils.writeDouble(stream, positionY);
                ByteStreamUtils.writeDouble(stream, positionZ);
            } break;
        }
    }

}
