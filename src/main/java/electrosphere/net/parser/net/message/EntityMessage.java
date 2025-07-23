package electrosphere.net.parser.net.message;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import electrosphere.net.parser.util.ByteStreamUtils;
import java.util.Map;
import java.util.function.BiConsumer;

public class EntityMessage extends NetworkMessage {

    /**
     * The types of messages available in this category.
     */
    public enum EntityMessageType {
        CREATE,
        MOVEUPDATE,
        ATTACKUPDATE,
        STARTATTACK,
        KILL,
        DESTROY,
        SETPROPERTY,
        ATTACHENTITYTOENTITY,
        UPDATEENTITYVIEWDIR,
        SYNCPHYSICS,
        INTERACT,
    }

    /**
     * The type of this message in particular.
     */
    EntityMessageType messageType;
    int entityCategory;
    String entitySubtype;
    int entityID;
    String creatureTemplate;
    double positionX;
    double positionY;
    double positionZ;
    double rotationX;
    double rotationY;
    double rotationZ;
    double rotationW;
    double linVelX;
    double linVelY;
    double linVelZ;
    double angVelX;
    double angVelY;
    double angVelZ;
    double linForceX;
    double linForceY;
    double linForceZ;
    double angForceX;
    double angForceY;
    double angForceZ;
    double yaw;
    double pitch;
    double velocity;
    int treeState;
    int propertyType;
    int propertyValue;
    long time;
    String bone;
    int targetID;
    int bTreeID;
    int propertyValueInt;
    boolean bodyEnabled;
    String interactionSignal;

    /**
     * Constructor
     * @param messageType The type of this message
     */
    private EntityMessage(EntityMessageType messageType){
        this.type = MessageType.ENTITY_MESSAGE;
        this.messageType = messageType;
    }

    /**
     * Constructor
     */
    protected EntityMessage(){
        this.type = MessageType.ENTITY_MESSAGE;
    }

    public EntityMessageType getMessageSubtype(){
        return this.messageType;
    }

    /**
     * Gets entityCategory
     */
    public int getentityCategory() {
        return entityCategory;
    }

    /**
     * Sets entityCategory
     */
    public void setentityCategory(int entityCategory) {
        this.entityCategory = entityCategory;
    }

    /**
     * Gets entitySubtype
     */
    public String getentitySubtype() {
        return entitySubtype;
    }

    /**
     * Sets entitySubtype
     */
    public void setentitySubtype(String entitySubtype) {
        this.entitySubtype = entitySubtype;
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
     * Gets creatureTemplate
     */
    public String getcreatureTemplate() {
        return creatureTemplate;
    }

    /**
     * Sets creatureTemplate
     */
    public void setcreatureTemplate(String creatureTemplate) {
        this.creatureTemplate = creatureTemplate;
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
     * Gets linVelX
     */
    public double getlinVelX() {
        return linVelX;
    }

    /**
     * Sets linVelX
     */
    public void setlinVelX(double linVelX) {
        this.linVelX = linVelX;
    }

    /**
     * Gets linVelY
     */
    public double getlinVelY() {
        return linVelY;
    }

    /**
     * Sets linVelY
     */
    public void setlinVelY(double linVelY) {
        this.linVelY = linVelY;
    }

    /**
     * Gets linVelZ
     */
    public double getlinVelZ() {
        return linVelZ;
    }

    /**
     * Sets linVelZ
     */
    public void setlinVelZ(double linVelZ) {
        this.linVelZ = linVelZ;
    }

    /**
     * Gets angVelX
     */
    public double getangVelX() {
        return angVelX;
    }

    /**
     * Sets angVelX
     */
    public void setangVelX(double angVelX) {
        this.angVelX = angVelX;
    }

    /**
     * Gets angVelY
     */
    public double getangVelY() {
        return angVelY;
    }

    /**
     * Sets angVelY
     */
    public void setangVelY(double angVelY) {
        this.angVelY = angVelY;
    }

    /**
     * Gets angVelZ
     */
    public double getangVelZ() {
        return angVelZ;
    }

    /**
     * Sets angVelZ
     */
    public void setangVelZ(double angVelZ) {
        this.angVelZ = angVelZ;
    }

    /**
     * Gets linForceX
     */
    public double getlinForceX() {
        return linForceX;
    }

    /**
     * Sets linForceX
     */
    public void setlinForceX(double linForceX) {
        this.linForceX = linForceX;
    }

    /**
     * Gets linForceY
     */
    public double getlinForceY() {
        return linForceY;
    }

    /**
     * Sets linForceY
     */
    public void setlinForceY(double linForceY) {
        this.linForceY = linForceY;
    }

    /**
     * Gets linForceZ
     */
    public double getlinForceZ() {
        return linForceZ;
    }

    /**
     * Sets linForceZ
     */
    public void setlinForceZ(double linForceZ) {
        this.linForceZ = linForceZ;
    }

    /**
     * Gets angForceX
     */
    public double getangForceX() {
        return angForceX;
    }

    /**
     * Sets angForceX
     */
    public void setangForceX(double angForceX) {
        this.angForceX = angForceX;
    }

    /**
     * Gets angForceY
     */
    public double getangForceY() {
        return angForceY;
    }

    /**
     * Sets angForceY
     */
    public void setangForceY(double angForceY) {
        this.angForceY = angForceY;
    }

    /**
     * Gets angForceZ
     */
    public double getangForceZ() {
        return angForceZ;
    }

    /**
     * Sets angForceZ
     */
    public void setangForceZ(double angForceZ) {
        this.angForceZ = angForceZ;
    }

    /**
     * Gets yaw
     */
    public double getyaw() {
        return yaw;
    }

    /**
     * Sets yaw
     */
    public void setyaw(double yaw) {
        this.yaw = yaw;
    }

    /**
     * Gets pitch
     */
    public double getpitch() {
        return pitch;
    }

    /**
     * Sets pitch
     */
    public void setpitch(double pitch) {
        this.pitch = pitch;
    }

    /**
     * Gets velocity
     */
    public double getvelocity() {
        return velocity;
    }

    /**
     * Sets velocity
     */
    public void setvelocity(double velocity) {
        this.velocity = velocity;
    }

    /**
     * Gets treeState
     */
    public int gettreeState() {
        return treeState;
    }

    /**
     * Sets treeState
     */
    public void settreeState(int treeState) {
        this.treeState = treeState;
    }

    /**
     * Gets propertyType
     */
    public int getpropertyType() {
        return propertyType;
    }

    /**
     * Sets propertyType
     */
    public void setpropertyType(int propertyType) {
        this.propertyType = propertyType;
    }

    /**
     * Gets propertyValue
     */
    public int getpropertyValue() {
        return propertyValue;
    }

    /**
     * Sets propertyValue
     */
    public void setpropertyValue(int propertyValue) {
        this.propertyValue = propertyValue;
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
     * Gets bone
     */
    public String getbone() {
        return bone;
    }

    /**
     * Sets bone
     */
    public void setbone(String bone) {
        this.bone = bone;
    }

    /**
     * Gets targetID
     */
    public int gettargetID() {
        return targetID;
    }

    /**
     * Sets targetID
     */
    public void settargetID(int targetID) {
        this.targetID = targetID;
    }

    /**
     * Gets bTreeID
     */
    public int getbTreeID() {
        return bTreeID;
    }

    /**
     * Sets bTreeID
     */
    public void setbTreeID(int bTreeID) {
        this.bTreeID = bTreeID;
    }

    /**
     * Gets propertyValueInt
     */
    public int getpropertyValueInt() {
        return propertyValueInt;
    }

    /**
     * Sets propertyValueInt
     */
    public void setpropertyValueInt(int propertyValueInt) {
        this.propertyValueInt = propertyValueInt;
    }

    /**
     * Gets bodyEnabled
     */
    public boolean getbodyEnabled() {
        return bodyEnabled;
    }

    /**
     * Sets bodyEnabled
     */
    public void setbodyEnabled(boolean bodyEnabled) {
        this.bodyEnabled = bodyEnabled;
    }

    /**
     * Gets interactionSignal
     */
    public String getinteractionSignal() {
        return interactionSignal;
    }

    /**
     * Sets interactionSignal
     */
    public void setinteractionSignal(String interactionSignal) {
        this.interactionSignal = interactionSignal;
    }

    /**
     * Parses a message of type Create
     */
    public static EntityMessage parseCreateMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 72){
            return null;
        }
        int lenAccumulator = 0;
        int entitySubtypelen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + entitySubtypelen;
        int creatureTemplatelen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + creatureTemplatelen;
        if(byteBuffer.remaining() < 72 + lenAccumulator){
            return null;
        }
        EntityMessage rVal = (EntityMessage)pool.get(MessageType.ENTITY_MESSAGE);
        rVal.messageType = EntityMessageType.CREATE;
        rVal.setentityID(byteBuffer.getInt());
        rVal.setentityCategory(byteBuffer.getInt());
        if(entitySubtypelen > 0){
            rVal.setentitySubtype(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, entitySubtypelen));
        }
        if(creatureTemplatelen > 0){
            rVal.setcreatureTemplate(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, creatureTemplatelen));
        }
        rVal.setpositionX(byteBuffer.getDouble());
        rVal.setpositionY(byteBuffer.getDouble());
        rVal.setpositionZ(byteBuffer.getDouble());
        rVal.setrotationX(byteBuffer.getDouble());
        rVal.setrotationY(byteBuffer.getDouble());
        rVal.setrotationZ(byteBuffer.getDouble());
        rVal.setrotationW(byteBuffer.getDouble());
        return rVal;
    }

    /**
     * Constructs a message of type Create
     */
    public static EntityMessage constructCreateMessage(int entityID,int entityCategory,String entitySubtype,String creatureTemplate,double positionX,double positionY,double positionZ,double rotationX,double rotationY,double rotationZ,double rotationW){
        EntityMessage rVal = new EntityMessage(EntityMessageType.CREATE);
        rVal.setentityID(entityID);
        rVal.setentityCategory(entityCategory);
        rVal.setentitySubtype(entitySubtype);
        rVal.setcreatureTemplate(creatureTemplate);
        rVal.setpositionX(positionX);
        rVal.setpositionY(positionY);
        rVal.setpositionZ(positionZ);
        rVal.setrotationX(rotationX);
        rVal.setrotationY(rotationY);
        rVal.setrotationZ(rotationZ);
        rVal.setrotationW(rotationW);
        return rVal;
    }

    /**
     * Parses a message of type moveUpdate
     */
    public static EntityMessage parsemoveUpdateMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 84){
            return null;
        }
        EntityMessage rVal = (EntityMessage)pool.get(MessageType.ENTITY_MESSAGE);
        rVal.messageType = EntityMessageType.MOVEUPDATE;
        rVal.setentityID(byteBuffer.getInt());
        rVal.settime(byteBuffer.getLong());
        rVal.setpositionX(byteBuffer.getDouble());
        rVal.setpositionY(byteBuffer.getDouble());
        rVal.setpositionZ(byteBuffer.getDouble());
        rVal.setrotationX(byteBuffer.getDouble());
        rVal.setrotationY(byteBuffer.getDouble());
        rVal.setrotationZ(byteBuffer.getDouble());
        rVal.setrotationW(byteBuffer.getDouble());
        rVal.setvelocity(byteBuffer.getDouble());
        rVal.setpropertyValueInt(byteBuffer.getInt());
        rVal.settreeState(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type moveUpdate
     */
    public static EntityMessage constructmoveUpdateMessage(int entityID,long time,double positionX,double positionY,double positionZ,double rotationX,double rotationY,double rotationZ,double rotationW,double velocity,int propertyValueInt,int treeState){
        EntityMessage rVal = new EntityMessage(EntityMessageType.MOVEUPDATE);
        rVal.setentityID(entityID);
        rVal.settime(time);
        rVal.setpositionX(positionX);
        rVal.setpositionY(positionY);
        rVal.setpositionZ(positionZ);
        rVal.setrotationX(rotationX);
        rVal.setrotationY(rotationY);
        rVal.setrotationZ(rotationZ);
        rVal.setrotationW(rotationW);
        rVal.setvelocity(velocity);
        rVal.setpropertyValueInt(propertyValueInt);
        rVal.settreeState(treeState);
        return rVal;
    }

    /**
     * Parses a message of type attackUpdate
     */
    public static EntityMessage parseattackUpdateMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 72){
            return null;
        }
        EntityMessage rVal = (EntityMessage)pool.get(MessageType.ENTITY_MESSAGE);
        rVal.messageType = EntityMessageType.ATTACKUPDATE;
        rVal.setentityID(byteBuffer.getInt());
        rVal.settime(byteBuffer.getLong());
        rVal.setpositionX(byteBuffer.getDouble());
        rVal.setpositionY(byteBuffer.getDouble());
        rVal.setpositionZ(byteBuffer.getDouble());
        rVal.setrotationX(byteBuffer.getDouble());
        rVal.setrotationY(byteBuffer.getDouble());
        rVal.setrotationZ(byteBuffer.getDouble());
        rVal.setvelocity(byteBuffer.getDouble());
        rVal.settreeState(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type attackUpdate
     */
    public static EntityMessage constructattackUpdateMessage(int entityID,long time,double positionX,double positionY,double positionZ,double rotationX,double rotationY,double rotationZ,double velocity,int treeState){
        EntityMessage rVal = new EntityMessage(EntityMessageType.ATTACKUPDATE);
        rVal.setentityID(entityID);
        rVal.settime(time);
        rVal.setpositionX(positionX);
        rVal.setpositionY(positionY);
        rVal.setpositionZ(positionZ);
        rVal.setrotationX(rotationX);
        rVal.setrotationY(rotationY);
        rVal.setrotationZ(rotationZ);
        rVal.setvelocity(velocity);
        rVal.settreeState(treeState);
        return rVal;
    }

    /**
     * Parses a message of type startAttack
     */
    public static EntityMessage parsestartAttackMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 0){
            return null;
        }
        EntityMessage rVal = (EntityMessage)pool.get(MessageType.ENTITY_MESSAGE);
        rVal.messageType = EntityMessageType.STARTATTACK;
        return rVal;
    }

    /**
     * Constructs a message of type startAttack
     */
    public static EntityMessage constructstartAttackMessage(){
        EntityMessage rVal = new EntityMessage(EntityMessageType.STARTATTACK);
        return rVal;
    }

    /**
     * Parses a message of type Kill
     */
    public static EntityMessage parseKillMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 12){
            return null;
        }
        EntityMessage rVal = (EntityMessage)pool.get(MessageType.ENTITY_MESSAGE);
        rVal.messageType = EntityMessageType.KILL;
        rVal.settime(byteBuffer.getLong());
        rVal.setentityID(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type Kill
     */
    public static EntityMessage constructKillMessage(long time,int entityID){
        EntityMessage rVal = new EntityMessage(EntityMessageType.KILL);
        rVal.settime(time);
        rVal.setentityID(entityID);
        return rVal;
    }

    /**
     * Parses a message of type Destroy
     */
    public static EntityMessage parseDestroyMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 4){
            return null;
        }
        EntityMessage rVal = (EntityMessage)pool.get(MessageType.ENTITY_MESSAGE);
        rVal.messageType = EntityMessageType.DESTROY;
        rVal.setentityID(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type Destroy
     */
    public static EntityMessage constructDestroyMessage(int entityID){
        EntityMessage rVal = new EntityMessage(EntityMessageType.DESTROY);
        rVal.setentityID(entityID);
        return rVal;
    }

    /**
     * Parses a message of type setProperty
     */
    public static EntityMessage parsesetPropertyMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 20){
            return null;
        }
        EntityMessage rVal = (EntityMessage)pool.get(MessageType.ENTITY_MESSAGE);
        rVal.messageType = EntityMessageType.SETPROPERTY;
        rVal.setentityID(byteBuffer.getInt());
        rVal.settime(byteBuffer.getLong());
        rVal.setpropertyType(byteBuffer.getInt());
        rVal.setpropertyValue(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type setProperty
     */
    public static EntityMessage constructsetPropertyMessage(int entityID,long time,int propertyType,int propertyValue){
        EntityMessage rVal = new EntityMessage(EntityMessageType.SETPROPERTY);
        rVal.setentityID(entityID);
        rVal.settime(time);
        rVal.setpropertyType(propertyType);
        rVal.setpropertyValue(propertyValue);
        return rVal;
    }

    /**
     * Parses a message of type attachEntityToEntity
     */
    public static EntityMessage parseattachEntityToEntityMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 12){
            return null;
        }
        int lenAccumulator = 0;
        int bonelen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + bonelen;
        if(byteBuffer.remaining() < 12 + lenAccumulator){
            return null;
        }
        EntityMessage rVal = (EntityMessage)pool.get(MessageType.ENTITY_MESSAGE);
        rVal.messageType = EntityMessageType.ATTACHENTITYTOENTITY;
        rVal.setentityID(byteBuffer.getInt());
        if(bonelen > 0){
            rVal.setbone(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, bonelen));
        }
        rVal.settargetID(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type attachEntityToEntity
     */
    public static EntityMessage constructattachEntityToEntityMessage(int entityID,String bone,int targetID){
        EntityMessage rVal = new EntityMessage(EntityMessageType.ATTACHENTITYTOENTITY);
        rVal.setentityID(entityID);
        rVal.setbone(bone);
        rVal.settargetID(targetID);
        return rVal;
    }

    /**
     * Parses a message of type updateEntityViewDir
     */
    public static EntityMessage parseupdateEntityViewDirMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 32){
            return null;
        }
        EntityMessage rVal = (EntityMessage)pool.get(MessageType.ENTITY_MESSAGE);
        rVal.messageType = EntityMessageType.UPDATEENTITYVIEWDIR;
        rVal.setentityID(byteBuffer.getInt());
        rVal.settime(byteBuffer.getLong());
        rVal.setpropertyType(byteBuffer.getInt());
        rVal.setyaw(byteBuffer.getDouble());
        rVal.setpitch(byteBuffer.getDouble());
        return rVal;
    }

    /**
     * Constructs a message of type updateEntityViewDir
     */
    public static EntityMessage constructupdateEntityViewDirMessage(int entityID,long time,int propertyType,double yaw,double pitch){
        EntityMessage rVal = new EntityMessage(EntityMessageType.UPDATEENTITYVIEWDIR);
        rVal.setentityID(entityID);
        rVal.settime(time);
        rVal.setpropertyType(propertyType);
        rVal.setyaw(yaw);
        rVal.setpitch(pitch);
        return rVal;
    }

    /**
     * Parses a message of type syncPhysics
     */
    public static EntityMessage parsesyncPhysicsMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 168){
            return null;
        }
        EntityMessage rVal = (EntityMessage)pool.get(MessageType.ENTITY_MESSAGE);
        rVal.messageType = EntityMessageType.SYNCPHYSICS;
        rVal.setentityID(byteBuffer.getInt());
        rVal.settime(byteBuffer.getLong());
        rVal.setpositionX(byteBuffer.getDouble());
        rVal.setpositionY(byteBuffer.getDouble());
        rVal.setpositionZ(byteBuffer.getDouble());
        rVal.setrotationX(byteBuffer.getDouble());
        rVal.setrotationY(byteBuffer.getDouble());
        rVal.setrotationZ(byteBuffer.getDouble());
        rVal.setrotationW(byteBuffer.getDouble());
        rVal.setlinVelX(byteBuffer.getDouble());
        rVal.setlinVelY(byteBuffer.getDouble());
        rVal.setlinVelZ(byteBuffer.getDouble());
        rVal.setangVelX(byteBuffer.getDouble());
        rVal.setangVelY(byteBuffer.getDouble());
        rVal.setangVelZ(byteBuffer.getDouble());
        rVal.setlinForceX(byteBuffer.getDouble());
        rVal.setlinForceY(byteBuffer.getDouble());
        rVal.setlinForceZ(byteBuffer.getDouble());
        rVal.setangForceX(byteBuffer.getDouble());
        rVal.setangForceY(byteBuffer.getDouble());
        rVal.setangForceZ(byteBuffer.getDouble());
        rVal.setbodyEnabled(byteBuffer.get() == 1 ? true : false);
        return rVal;
    }

    /**
     * Constructs a message of type syncPhysics
     */
    public static EntityMessage constructsyncPhysicsMessage(int entityID,long time,double positionX,double positionY,double positionZ,double rotationX,double rotationY,double rotationZ,double rotationW,double linVelX,double linVelY,double linVelZ,double angVelX,double angVelY,double angVelZ,double linForceX,double linForceY,double linForceZ,double angForceX,double angForceY,double angForceZ,boolean bodyEnabled){
        EntityMessage rVal = new EntityMessage(EntityMessageType.SYNCPHYSICS);
        rVal.setentityID(entityID);
        rVal.settime(time);
        rVal.setpositionX(positionX);
        rVal.setpositionY(positionY);
        rVal.setpositionZ(positionZ);
        rVal.setrotationX(rotationX);
        rVal.setrotationY(rotationY);
        rVal.setrotationZ(rotationZ);
        rVal.setrotationW(rotationW);
        rVal.setlinVelX(linVelX);
        rVal.setlinVelY(linVelY);
        rVal.setlinVelZ(linVelZ);
        rVal.setangVelX(angVelX);
        rVal.setangVelY(angVelY);
        rVal.setangVelZ(angVelZ);
        rVal.setlinForceX(linForceX);
        rVal.setlinForceY(linForceY);
        rVal.setlinForceZ(linForceZ);
        rVal.setangForceX(angForceX);
        rVal.setangForceY(angForceY);
        rVal.setangForceZ(angForceZ);
        rVal.setbodyEnabled(bodyEnabled);
        return rVal;
    }

    /**
     * Parses a message of type interact
     */
    public static EntityMessage parseinteractMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 8){
            return null;
        }
        int lenAccumulator = 0;
        int interactionSignallen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + interactionSignallen;
        if(byteBuffer.remaining() < 8 + lenAccumulator){
            return null;
        }
        EntityMessage rVal = (EntityMessage)pool.get(MessageType.ENTITY_MESSAGE);
        rVal.messageType = EntityMessageType.INTERACT;
        rVal.setentityID(byteBuffer.getInt());
        if(interactionSignallen > 0){
            rVal.setinteractionSignal(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, interactionSignallen));
        }
        return rVal;
    }

    /**
     * Constructs a message of type interact
     */
    public static EntityMessage constructinteractMessage(int entityID,String interactionSignal){
        EntityMessage rVal = new EntityMessage(EntityMessageType.INTERACT);
        rVal.setentityID(entityID);
        rVal.setinteractionSignal(interactionSignal);
        return rVal;
    }

    @Deprecated
    @Override
    void serialize(){
        byte[] intValues = new byte[8];
        byte[] stringBytes;
        switch(this.messageType){
            case CREATE:
                rawBytes = new byte[2+4+4+4+entitySubtype.length()+4+creatureTemplate.length()+8+8+8+8+8+8+8];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_ENTITY;
                //entity messaage header
                rawBytes[1] = TypeBytes.ENTITY_MESSAGE_TYPE_CREATE;
                intValues = ByteStreamUtils.serializeIntToBytes(entityID);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(entityCategory);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(entitySubtype.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                stringBytes = entitySubtype.getBytes();
                for(int i = 0; i < entitySubtype.length(); i++){
                    rawBytes[14+i] = stringBytes[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(creatureTemplate.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[14+entitySubtype.length()+i] = intValues[i];
                }
                stringBytes = creatureTemplate.getBytes();
                for(int i = 0; i < creatureTemplate.length(); i++){
                    rawBytes[18+entitySubtype.length()+i] = stringBytes[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(positionX);
                for(int i = 0; i < 8; i++){
                    rawBytes[18+entitySubtype.length()+creatureTemplate.length()+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(positionY);
                for(int i = 0; i < 8; i++){
                    rawBytes[26+entitySubtype.length()+creatureTemplate.length()+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(positionZ);
                for(int i = 0; i < 8; i++){
                    rawBytes[34+entitySubtype.length()+creatureTemplate.length()+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(rotationX);
                for(int i = 0; i < 8; i++){
                    rawBytes[42+entitySubtype.length()+creatureTemplate.length()+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(rotationY);
                for(int i = 0; i < 8; i++){
                    rawBytes[50+entitySubtype.length()+creatureTemplate.length()+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(rotationZ);
                for(int i = 0; i < 8; i++){
                    rawBytes[58+entitySubtype.length()+creatureTemplate.length()+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(rotationW);
                for(int i = 0; i < 8; i++){
                    rawBytes[66+entitySubtype.length()+creatureTemplate.length()+i] = intValues[i];
                }
                break;
            case MOVEUPDATE:
                rawBytes = new byte[2+4+8+8+8+8+8+8+8+8+8+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_ENTITY;
                //entity messaage header
                rawBytes[1] = TypeBytes.ENTITY_MESSAGE_TYPE_MOVEUPDATE;
                intValues = ByteStreamUtils.serializeIntToBytes(entityID);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeLongToBytes(time);
                for(int i = 0; i < 8; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(positionX);
                for(int i = 0; i < 8; i++){
                    rawBytes[14+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(positionY);
                for(int i = 0; i < 8; i++){
                    rawBytes[22+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(positionZ);
                for(int i = 0; i < 8; i++){
                    rawBytes[30+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(rotationX);
                for(int i = 0; i < 8; i++){
                    rawBytes[38+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(rotationY);
                for(int i = 0; i < 8; i++){
                    rawBytes[46+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(rotationZ);
                for(int i = 0; i < 8; i++){
                    rawBytes[54+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(rotationW);
                for(int i = 0; i < 8; i++){
                    rawBytes[62+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(velocity);
                for(int i = 0; i < 8; i++){
                    rawBytes[70+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(propertyValueInt);
                for(int i = 0; i < 4; i++){
                    rawBytes[78+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(treeState);
                for(int i = 0; i < 4; i++){
                    rawBytes[82+i] = intValues[i];
                }
                break;
            case ATTACKUPDATE:
                rawBytes = new byte[2+4+8+8+8+8+8+8+8+8+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_ENTITY;
                //entity messaage header
                rawBytes[1] = TypeBytes.ENTITY_MESSAGE_TYPE_ATTACKUPDATE;
                intValues = ByteStreamUtils.serializeIntToBytes(entityID);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeLongToBytes(time);
                for(int i = 0; i < 8; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(positionX);
                for(int i = 0; i < 8; i++){
                    rawBytes[14+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(positionY);
                for(int i = 0; i < 8; i++){
                    rawBytes[22+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(positionZ);
                for(int i = 0; i < 8; i++){
                    rawBytes[30+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(rotationX);
                for(int i = 0; i < 8; i++){
                    rawBytes[38+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(rotationY);
                for(int i = 0; i < 8; i++){
                    rawBytes[46+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(rotationZ);
                for(int i = 0; i < 8; i++){
                    rawBytes[54+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(velocity);
                for(int i = 0; i < 8; i++){
                    rawBytes[62+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(treeState);
                for(int i = 0; i < 4; i++){
                    rawBytes[70+i] = intValues[i];
                }
                break;
            case STARTATTACK:
                rawBytes = new byte[2];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_ENTITY;
                //entity messaage header
                rawBytes[1] = TypeBytes.ENTITY_MESSAGE_TYPE_STARTATTACK;
                break;
            case KILL:
                rawBytes = new byte[2+8+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_ENTITY;
                //entity messaage header
                rawBytes[1] = TypeBytes.ENTITY_MESSAGE_TYPE_KILL;
                intValues = ByteStreamUtils.serializeLongToBytes(time);
                for(int i = 0; i < 8; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(entityID);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                break;
            case DESTROY:
                rawBytes = new byte[2+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_ENTITY;
                //entity messaage header
                rawBytes[1] = TypeBytes.ENTITY_MESSAGE_TYPE_DESTROY;
                intValues = ByteStreamUtils.serializeIntToBytes(entityID);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                break;
            case SETPROPERTY:
                rawBytes = new byte[2+4+8+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_ENTITY;
                //entity messaage header
                rawBytes[1] = TypeBytes.ENTITY_MESSAGE_TYPE_SETPROPERTY;
                intValues = ByteStreamUtils.serializeIntToBytes(entityID);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeLongToBytes(time);
                for(int i = 0; i < 8; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(propertyType);
                for(int i = 0; i < 4; i++){
                    rawBytes[14+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(propertyValue);
                for(int i = 0; i < 4; i++){
                    rawBytes[18+i] = intValues[i];
                }
                break;
            case ATTACHENTITYTOENTITY:
                rawBytes = new byte[2+4+4+bone.length()+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_ENTITY;
                //entity messaage header
                rawBytes[1] = TypeBytes.ENTITY_MESSAGE_TYPE_ATTACHENTITYTOENTITY;
                intValues = ByteStreamUtils.serializeIntToBytes(entityID);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(bone.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                stringBytes = bone.getBytes();
                for(int i = 0; i < bone.length(); i++){
                    rawBytes[10+i] = stringBytes[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(targetID);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+bone.length()+i] = intValues[i];
                }
                break;
            case UPDATEENTITYVIEWDIR:
                rawBytes = new byte[2+4+8+4+8+8];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_ENTITY;
                //entity messaage header
                rawBytes[1] = TypeBytes.ENTITY_MESSAGE_TYPE_UPDATEENTITYVIEWDIR;
                intValues = ByteStreamUtils.serializeIntToBytes(entityID);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeLongToBytes(time);
                for(int i = 0; i < 8; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(propertyType);
                for(int i = 0; i < 4; i++){
                    rawBytes[14+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(yaw);
                for(int i = 0; i < 8; i++){
                    rawBytes[18+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(pitch);
                for(int i = 0; i < 8; i++){
                    rawBytes[26+i] = intValues[i];
                }
                break;
            case SYNCPHYSICS:
                rawBytes = new byte[2+4+8+8+8+8+8+8+8+8+8+8+8+8+8+8+8+8+8+8+8+8+1];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_ENTITY;
                //entity messaage header
                rawBytes[1] = TypeBytes.ENTITY_MESSAGE_TYPE_SYNCPHYSICS;
                intValues = ByteStreamUtils.serializeIntToBytes(entityID);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeLongToBytes(time);
                for(int i = 0; i < 8; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(positionX);
                for(int i = 0; i < 8; i++){
                    rawBytes[14+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(positionY);
                for(int i = 0; i < 8; i++){
                    rawBytes[22+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(positionZ);
                for(int i = 0; i < 8; i++){
                    rawBytes[30+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(rotationX);
                for(int i = 0; i < 8; i++){
                    rawBytes[38+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(rotationY);
                for(int i = 0; i < 8; i++){
                    rawBytes[46+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(rotationZ);
                for(int i = 0; i < 8; i++){
                    rawBytes[54+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(rotationW);
                for(int i = 0; i < 8; i++){
                    rawBytes[62+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(linVelX);
                for(int i = 0; i < 8; i++){
                    rawBytes[70+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(linVelY);
                for(int i = 0; i < 8; i++){
                    rawBytes[78+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(linVelZ);
                for(int i = 0; i < 8; i++){
                    rawBytes[86+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(angVelX);
                for(int i = 0; i < 8; i++){
                    rawBytes[94+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(angVelY);
                for(int i = 0; i < 8; i++){
                    rawBytes[102+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(angVelZ);
                for(int i = 0; i < 8; i++){
                    rawBytes[110+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(linForceX);
                for(int i = 0; i < 8; i++){
                    rawBytes[118+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(linForceY);
                for(int i = 0; i < 8; i++){
                    rawBytes[126+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(linForceZ);
                for(int i = 0; i < 8; i++){
                    rawBytes[134+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(angForceX);
                for(int i = 0; i < 8; i++){
                    rawBytes[142+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(angForceY);
                for(int i = 0; i < 8; i++){
                    rawBytes[150+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(angForceZ);
                for(int i = 0; i < 8; i++){
                    rawBytes[158+i] = intValues[i];
                }
                rawBytes[166] = bodyEnabled ? (byte)1 : (byte)0;
                break;
            case INTERACT:
                rawBytes = new byte[2+4+4+interactionSignal.length()];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_ENTITY;
                //entity messaage header
                rawBytes[1] = TypeBytes.ENTITY_MESSAGE_TYPE_INTERACT;
                intValues = ByteStreamUtils.serializeIntToBytes(entityID);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(interactionSignal.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                stringBytes = interactionSignal.getBytes();
                for(int i = 0; i < interactionSignal.length(); i++){
                    rawBytes[10+i] = stringBytes[i];
                }
                break;
        }
        serialized = true;
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        switch(this.messageType){
            case CREATE: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_ENTITY);
                stream.write(TypeBytes.ENTITY_MESSAGE_TYPE_CREATE);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, entitySubtype.getBytes().length);
                ByteStreamUtils.writeInt(stream, creatureTemplate.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityID);
                ByteStreamUtils.writeInt(stream, entityCategory);
                ByteStreamUtils.writeString(stream, entitySubtype);
                ByteStreamUtils.writeString(stream, creatureTemplate);
                ByteStreamUtils.writeDouble(stream, positionX);
                ByteStreamUtils.writeDouble(stream, positionY);
                ByteStreamUtils.writeDouble(stream, positionZ);
                ByteStreamUtils.writeDouble(stream, rotationX);
                ByteStreamUtils.writeDouble(stream, rotationY);
                ByteStreamUtils.writeDouble(stream, rotationZ);
                ByteStreamUtils.writeDouble(stream, rotationW);
            } break;
            case MOVEUPDATE: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_ENTITY);
                stream.write(TypeBytes.ENTITY_MESSAGE_TYPE_MOVEUPDATE);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityID);
                ByteStreamUtils.writeLong(stream, time);
                ByteStreamUtils.writeDouble(stream, positionX);
                ByteStreamUtils.writeDouble(stream, positionY);
                ByteStreamUtils.writeDouble(stream, positionZ);
                ByteStreamUtils.writeDouble(stream, rotationX);
                ByteStreamUtils.writeDouble(stream, rotationY);
                ByteStreamUtils.writeDouble(stream, rotationZ);
                ByteStreamUtils.writeDouble(stream, rotationW);
                ByteStreamUtils.writeDouble(stream, velocity);
                ByteStreamUtils.writeInt(stream, propertyValueInt);
                ByteStreamUtils.writeInt(stream, treeState);
            } break;
            case ATTACKUPDATE: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_ENTITY);
                stream.write(TypeBytes.ENTITY_MESSAGE_TYPE_ATTACKUPDATE);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityID);
                ByteStreamUtils.writeLong(stream, time);
                ByteStreamUtils.writeDouble(stream, positionX);
                ByteStreamUtils.writeDouble(stream, positionY);
                ByteStreamUtils.writeDouble(stream, positionZ);
                ByteStreamUtils.writeDouble(stream, rotationX);
                ByteStreamUtils.writeDouble(stream, rotationY);
                ByteStreamUtils.writeDouble(stream, rotationZ);
                ByteStreamUtils.writeDouble(stream, velocity);
                ByteStreamUtils.writeInt(stream, treeState);
            } break;
            case STARTATTACK: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_ENTITY);
                stream.write(TypeBytes.ENTITY_MESSAGE_TYPE_STARTATTACK);
                
                //
                //Write body of packet
            } break;
            case KILL: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_ENTITY);
                stream.write(TypeBytes.ENTITY_MESSAGE_TYPE_KILL);
                
                //
                //Write body of packet
                ByteStreamUtils.writeLong(stream, time);
                ByteStreamUtils.writeInt(stream, entityID);
            } break;
            case DESTROY: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_ENTITY);
                stream.write(TypeBytes.ENTITY_MESSAGE_TYPE_DESTROY);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityID);
            } break;
            case SETPROPERTY: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_ENTITY);
                stream.write(TypeBytes.ENTITY_MESSAGE_TYPE_SETPROPERTY);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityID);
                ByteStreamUtils.writeLong(stream, time);
                ByteStreamUtils.writeInt(stream, propertyType);
                ByteStreamUtils.writeInt(stream, propertyValue);
            } break;
            case ATTACHENTITYTOENTITY: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_ENTITY);
                stream.write(TypeBytes.ENTITY_MESSAGE_TYPE_ATTACHENTITYTOENTITY);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, bone.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityID);
                ByteStreamUtils.writeString(stream, bone);
                ByteStreamUtils.writeInt(stream, targetID);
            } break;
            case UPDATEENTITYVIEWDIR: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_ENTITY);
                stream.write(TypeBytes.ENTITY_MESSAGE_TYPE_UPDATEENTITYVIEWDIR);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityID);
                ByteStreamUtils.writeLong(stream, time);
                ByteStreamUtils.writeInt(stream, propertyType);
                ByteStreamUtils.writeDouble(stream, yaw);
                ByteStreamUtils.writeDouble(stream, pitch);
            } break;
            case SYNCPHYSICS: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_ENTITY);
                stream.write(TypeBytes.ENTITY_MESSAGE_TYPE_SYNCPHYSICS);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityID);
                ByteStreamUtils.writeLong(stream, time);
                ByteStreamUtils.writeDouble(stream, positionX);
                ByteStreamUtils.writeDouble(stream, positionY);
                ByteStreamUtils.writeDouble(stream, positionZ);
                ByteStreamUtils.writeDouble(stream, rotationX);
                ByteStreamUtils.writeDouble(stream, rotationY);
                ByteStreamUtils.writeDouble(stream, rotationZ);
                ByteStreamUtils.writeDouble(stream, rotationW);
                ByteStreamUtils.writeDouble(stream, linVelX);
                ByteStreamUtils.writeDouble(stream, linVelY);
                ByteStreamUtils.writeDouble(stream, linVelZ);
                ByteStreamUtils.writeDouble(stream, angVelX);
                ByteStreamUtils.writeDouble(stream, angVelY);
                ByteStreamUtils.writeDouble(stream, angVelZ);
                ByteStreamUtils.writeDouble(stream, linForceX);
                ByteStreamUtils.writeDouble(stream, linForceY);
                ByteStreamUtils.writeDouble(stream, linForceZ);
                ByteStreamUtils.writeDouble(stream, angForceX);
                ByteStreamUtils.writeDouble(stream, angForceY);
                ByteStreamUtils.writeDouble(stream, angForceZ);
                stream.write(bodyEnabled ? (byte)1 : (byte)0);
            } break;
            case INTERACT: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_ENTITY);
                stream.write(TypeBytes.ENTITY_MESSAGE_TYPE_INTERACT);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, interactionSignal.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityID);
                ByteStreamUtils.writeString(stream, interactionSignal);
            } break;
        }
    }

}
