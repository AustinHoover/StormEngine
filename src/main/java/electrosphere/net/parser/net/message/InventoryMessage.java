package electrosphere.net.parser.net.message;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import electrosphere.net.parser.util.ByteStreamUtils;
import java.util.Map;
import java.util.function.BiConsumer;

public class InventoryMessage extends NetworkMessage {

    /**
     * The types of messages available in this category.
     */
    public enum InventoryMessageType {
        ADDITEMTOINVENTORY,
        REMOVEITEMFROMINVENTORY,
        CLIENTREQUESTEQUIPITEM,
        SERVERCOMMANDMOVEITEMCONTAINER,
        SERVERCOMMANDEQUIPITEM,
        SERVERCOMMANDUNEQUIPITEM,
        CLIENTREQUESTUNEQUIPITEM,
        CLIENTREQUESTSTOREITEM,
        SERVERCOMMANDSTOREITEM,
        CLIENTREQUESTWATCHINVENTORY,
        CLIENTREQUESTUNWATCHINVENTORY,
        CLIENTREQUESTADDTOOLBAR,
        CLIENTREQUESTADDNATURAL,
        CLIENTUPDATETOOLBAR,
        CLIENTREQUESTPERFORMITEMACTION,
        CLIENTREQUESTCRAFT,
        SERVERUPDATEITEMCHARGES,
    }

    /**
     * The type of this message in particular.
     */
    InventoryMessageType messageType;
    String itemTemplate;
    String equipPointId;
    int entityId;
    int itemEntId;
    int targetEntId;
    int equipperId;
    int containerType;
    int toolbarId;
    int itemActionCode;
    int itemActionCodeState;
    double viewTargetX;
    double viewTargetY;
    double viewTargetZ;
    int stationId;
    int recipeId;
    int charges;

    /**
     * Constructor
     * @param messageType The type of this message
     */
    private InventoryMessage(InventoryMessageType messageType){
        this.type = MessageType.INVENTORY_MESSAGE;
        this.messageType = messageType;
    }

    /**
     * Constructor
     */
    protected InventoryMessage(){
        this.type = MessageType.INVENTORY_MESSAGE;
    }

    public InventoryMessageType getMessageSubtype(){
        return this.messageType;
    }

    /**
     * Gets itemTemplate
     */
    public String getitemTemplate() {
        return itemTemplate;
    }

    /**
     * Sets itemTemplate
     */
    public void setitemTemplate(String itemTemplate) {
        this.itemTemplate = itemTemplate;
    }

    /**
     * Gets equipPointId
     */
    public String getequipPointId() {
        return equipPointId;
    }

    /**
     * Sets equipPointId
     */
    public void setequipPointId(String equipPointId) {
        this.equipPointId = equipPointId;
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
     * Gets itemEntId
     */
    public int getitemEntId() {
        return itemEntId;
    }

    /**
     * Sets itemEntId
     */
    public void setitemEntId(int itemEntId) {
        this.itemEntId = itemEntId;
    }

    /**
     * Gets targetEntId
     */
    public int gettargetEntId() {
        return targetEntId;
    }

    /**
     * Sets targetEntId
     */
    public void settargetEntId(int targetEntId) {
        this.targetEntId = targetEntId;
    }

    /**
     * Gets equipperId
     */
    public int getequipperId() {
        return equipperId;
    }

    /**
     * Sets equipperId
     */
    public void setequipperId(int equipperId) {
        this.equipperId = equipperId;
    }

    /**
     * Gets containerType
     */
    public int getcontainerType() {
        return containerType;
    }

    /**
     * Sets containerType
     */
    public void setcontainerType(int containerType) {
        this.containerType = containerType;
    }

    /**
     * Gets toolbarId
     */
    public int gettoolbarId() {
        return toolbarId;
    }

    /**
     * Sets toolbarId
     */
    public void settoolbarId(int toolbarId) {
        this.toolbarId = toolbarId;
    }

    /**
     * Gets itemActionCode
     */
    public int getitemActionCode() {
        return itemActionCode;
    }

    /**
     * Sets itemActionCode
     */
    public void setitemActionCode(int itemActionCode) {
        this.itemActionCode = itemActionCode;
    }

    /**
     * Gets itemActionCodeState
     */
    public int getitemActionCodeState() {
        return itemActionCodeState;
    }

    /**
     * Sets itemActionCodeState
     */
    public void setitemActionCodeState(int itemActionCodeState) {
        this.itemActionCodeState = itemActionCodeState;
    }

    /**
     * Gets viewTargetX
     */
    public double getviewTargetX() {
        return viewTargetX;
    }

    /**
     * Sets viewTargetX
     */
    public void setviewTargetX(double viewTargetX) {
        this.viewTargetX = viewTargetX;
    }

    /**
     * Gets viewTargetY
     */
    public double getviewTargetY() {
        return viewTargetY;
    }

    /**
     * Sets viewTargetY
     */
    public void setviewTargetY(double viewTargetY) {
        this.viewTargetY = viewTargetY;
    }

    /**
     * Gets viewTargetZ
     */
    public double getviewTargetZ() {
        return viewTargetZ;
    }

    /**
     * Sets viewTargetZ
     */
    public void setviewTargetZ(double viewTargetZ) {
        this.viewTargetZ = viewTargetZ;
    }

    /**
     * Gets stationId
     */
    public int getstationId() {
        return stationId;
    }

    /**
     * Sets stationId
     */
    public void setstationId(int stationId) {
        this.stationId = stationId;
    }

    /**
     * Gets recipeId
     */
    public int getrecipeId() {
        return recipeId;
    }

    /**
     * Sets recipeId
     */
    public void setrecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    /**
     * Gets charges
     */
    public int getcharges() {
        return charges;
    }

    /**
     * Sets charges
     */
    public void setcharges(int charges) {
        this.charges = charges;
    }

    /**
     * Parses a message of type addItemToInventory
     */
    public static InventoryMessage parseaddItemToInventoryMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 12){
            return null;
        }
        int lenAccumulator = 0;
        int itemTemplatelen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + itemTemplatelen;
        if(byteBuffer.remaining() < 12 + lenAccumulator){
            return null;
        }
        InventoryMessage rVal = (InventoryMessage)pool.get(MessageType.INVENTORY_MESSAGE);
        rVal.messageType = InventoryMessageType.ADDITEMTOINVENTORY;
        rVal.settargetEntId(byteBuffer.getInt());
        rVal.setitemEntId(byteBuffer.getInt());
        if(itemTemplatelen > 0){
            rVal.setitemTemplate(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, itemTemplatelen));
        }
        return rVal;
    }

    /**
     * Constructs a message of type addItemToInventory
     */
    public static InventoryMessage constructaddItemToInventoryMessage(int targetEntId,int itemEntId,String itemTemplate){
        InventoryMessage rVal = new InventoryMessage(InventoryMessageType.ADDITEMTOINVENTORY);
        rVal.settargetEntId(targetEntId);
        rVal.setitemEntId(itemEntId);
        rVal.setitemTemplate(itemTemplate);
        return rVal;
    }

    /**
     * Parses a message of type removeItemFromInventory
     */
    public static InventoryMessage parseremoveItemFromInventoryMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 4){
            return null;
        }
        InventoryMessage rVal = (InventoryMessage)pool.get(MessageType.INVENTORY_MESSAGE);
        rVal.messageType = InventoryMessageType.REMOVEITEMFROMINVENTORY;
        rVal.setentityId(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type removeItemFromInventory
     */
    public static InventoryMessage constructremoveItemFromInventoryMessage(int entityId){
        InventoryMessage rVal = new InventoryMessage(InventoryMessageType.REMOVEITEMFROMINVENTORY);
        rVal.setentityId(entityId);
        return rVal;
    }

    /**
     * Parses a message of type clientRequestEquipItem
     */
    public static InventoryMessage parseclientRequestEquipItemMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 8){
            return null;
        }
        int lenAccumulator = 0;
        int equipPointIdlen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + equipPointIdlen;
        if(byteBuffer.remaining() < 8 + lenAccumulator){
            return null;
        }
        InventoryMessage rVal = (InventoryMessage)pool.get(MessageType.INVENTORY_MESSAGE);
        rVal.messageType = InventoryMessageType.CLIENTREQUESTEQUIPITEM;
        if(equipPointIdlen > 0){
            rVal.setequipPointId(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, equipPointIdlen));
        }
        rVal.setentityId(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type clientRequestEquipItem
     */
    public static InventoryMessage constructclientRequestEquipItemMessage(String equipPointId,int entityId){
        InventoryMessage rVal = new InventoryMessage(InventoryMessageType.CLIENTREQUESTEQUIPITEM);
        rVal.setequipPointId(equipPointId);
        rVal.setentityId(entityId);
        return rVal;
    }

    /**
     * Parses a message of type serverCommandMoveItemContainer
     */
    public static InventoryMessage parseserverCommandMoveItemContainerMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 12){
            return null;
        }
        int lenAccumulator = 0;
        int equipPointIdlen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + equipPointIdlen;
        if(byteBuffer.remaining() < 12 + lenAccumulator){
            return null;
        }
        InventoryMessage rVal = (InventoryMessage)pool.get(MessageType.INVENTORY_MESSAGE);
        rVal.messageType = InventoryMessageType.SERVERCOMMANDMOVEITEMCONTAINER;
        rVal.setentityId(byteBuffer.getInt());
        rVal.setcontainerType(byteBuffer.getInt());
        if(equipPointIdlen > 0){
            rVal.setequipPointId(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, equipPointIdlen));
        }
        return rVal;
    }

    /**
     * Constructs a message of type serverCommandMoveItemContainer
     */
    public static InventoryMessage constructserverCommandMoveItemContainerMessage(int entityId,int containerType,String equipPointId){
        InventoryMessage rVal = new InventoryMessage(InventoryMessageType.SERVERCOMMANDMOVEITEMCONTAINER);
        rVal.setentityId(entityId);
        rVal.setcontainerType(containerType);
        rVal.setequipPointId(equipPointId);
        return rVal;
    }

    /**
     * Parses a message of type serverCommandEquipItem
     */
    public static InventoryMessage parseserverCommandEquipItemMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 20){
            return null;
        }
        int lenAccumulator = 0;
        int equipPointIdlen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + equipPointIdlen;
        int itemTemplatelen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + itemTemplatelen;
        if(byteBuffer.remaining() < 20 + lenAccumulator){
            return null;
        }
        InventoryMessage rVal = (InventoryMessage)pool.get(MessageType.INVENTORY_MESSAGE);
        rVal.messageType = InventoryMessageType.SERVERCOMMANDEQUIPITEM;
        rVal.setequipperId(byteBuffer.getInt());
        rVal.setcontainerType(byteBuffer.getInt());
        if(equipPointIdlen > 0){
            rVal.setequipPointId(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, equipPointIdlen));
        }
        rVal.setentityId(byteBuffer.getInt());
        if(itemTemplatelen > 0){
            rVal.setitemTemplate(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, itemTemplatelen));
        }
        return rVal;
    }

    /**
     * Constructs a message of type serverCommandEquipItem
     */
    public static InventoryMessage constructserverCommandEquipItemMessage(int equipperId,int containerType,String equipPointId,int entityId,String itemTemplate){
        InventoryMessage rVal = new InventoryMessage(InventoryMessageType.SERVERCOMMANDEQUIPITEM);
        rVal.setequipperId(equipperId);
        rVal.setcontainerType(containerType);
        rVal.setequipPointId(equipPointId);
        rVal.setentityId(entityId);
        rVal.setitemTemplate(itemTemplate);
        return rVal;
    }

    /**
     * Parses a message of type serverCommandUnequipItem
     */
    public static InventoryMessage parseserverCommandUnequipItemMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 12){
            return null;
        }
        int lenAccumulator = 0;
        int equipPointIdlen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + equipPointIdlen;
        if(byteBuffer.remaining() < 12 + lenAccumulator){
            return null;
        }
        InventoryMessage rVal = (InventoryMessage)pool.get(MessageType.INVENTORY_MESSAGE);
        rVal.messageType = InventoryMessageType.SERVERCOMMANDUNEQUIPITEM;
        rVal.setequipperId(byteBuffer.getInt());
        rVal.setcontainerType(byteBuffer.getInt());
        if(equipPointIdlen > 0){
            rVal.setequipPointId(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, equipPointIdlen));
        }
        return rVal;
    }

    /**
     * Constructs a message of type serverCommandUnequipItem
     */
    public static InventoryMessage constructserverCommandUnequipItemMessage(int equipperId,int containerType,String equipPointId){
        InventoryMessage rVal = new InventoryMessage(InventoryMessageType.SERVERCOMMANDUNEQUIPITEM);
        rVal.setequipperId(equipperId);
        rVal.setcontainerType(containerType);
        rVal.setequipPointId(equipPointId);
        return rVal;
    }

    /**
     * Parses a message of type clientRequestUnequipItem
     */
    public static InventoryMessage parseclientRequestUnequipItemMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 4){
            return null;
        }
        int lenAccumulator = 0;
        int equipPointIdlen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + equipPointIdlen;
        if(byteBuffer.remaining() < 4 + lenAccumulator){
            return null;
        }
        InventoryMessage rVal = (InventoryMessage)pool.get(MessageType.INVENTORY_MESSAGE);
        rVal.messageType = InventoryMessageType.CLIENTREQUESTUNEQUIPITEM;
        if(equipPointIdlen > 0){
            rVal.setequipPointId(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, equipPointIdlen));
        }
        return rVal;
    }

    /**
     * Constructs a message of type clientRequestUnequipItem
     */
    public static InventoryMessage constructclientRequestUnequipItemMessage(String equipPointId){
        InventoryMessage rVal = new InventoryMessage(InventoryMessageType.CLIENTREQUESTUNEQUIPITEM);
        rVal.setequipPointId(equipPointId);
        return rVal;
    }

    /**
     * Parses a message of type clientRequestStoreItem
     */
    public static InventoryMessage parseclientRequestStoreItemMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 16){
            return null;
        }
        int lenAccumulator = 0;
        int equipPointIdlen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + equipPointIdlen;
        if(byteBuffer.remaining() < 16 + lenAccumulator){
            return null;
        }
        InventoryMessage rVal = (InventoryMessage)pool.get(MessageType.INVENTORY_MESSAGE);
        rVal.messageType = InventoryMessageType.CLIENTREQUESTSTOREITEM;
        rVal.settargetEntId(byteBuffer.getInt());
        rVal.setcontainerType(byteBuffer.getInt());
        if(equipPointIdlen > 0){
            rVal.setequipPointId(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, equipPointIdlen));
        }
        rVal.setitemEntId(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type clientRequestStoreItem
     */
    public static InventoryMessage constructclientRequestStoreItemMessage(int targetEntId,int containerType,String equipPointId,int itemEntId){
        InventoryMessage rVal = new InventoryMessage(InventoryMessageType.CLIENTREQUESTSTOREITEM);
        rVal.settargetEntId(targetEntId);
        rVal.setcontainerType(containerType);
        rVal.setequipPointId(equipPointId);
        rVal.setitemEntId(itemEntId);
        return rVal;
    }

    /**
     * Parses a message of type serverCommandStoreItem
     */
    public static InventoryMessage parseserverCommandStoreItemMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 20){
            return null;
        }
        int lenAccumulator = 0;
        int itemTemplatelen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + itemTemplatelen;
        int equipPointIdlen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + equipPointIdlen;
        if(byteBuffer.remaining() < 20 + lenAccumulator){
            return null;
        }
        InventoryMessage rVal = (InventoryMessage)pool.get(MessageType.INVENTORY_MESSAGE);
        rVal.messageType = InventoryMessageType.SERVERCOMMANDSTOREITEM;
        rVal.settargetEntId(byteBuffer.getInt());
        rVal.setitemEntId(byteBuffer.getInt());
        if(itemTemplatelen > 0){
            rVal.setitemTemplate(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, itemTemplatelen));
        }
        rVal.setcontainerType(byteBuffer.getInt());
        if(equipPointIdlen > 0){
            rVal.setequipPointId(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, equipPointIdlen));
        }
        return rVal;
    }

    /**
     * Constructs a message of type serverCommandStoreItem
     */
    public static InventoryMessage constructserverCommandStoreItemMessage(int targetEntId,int itemEntId,String itemTemplate,int containerType,String equipPointId){
        InventoryMessage rVal = new InventoryMessage(InventoryMessageType.SERVERCOMMANDSTOREITEM);
        rVal.settargetEntId(targetEntId);
        rVal.setitemEntId(itemEntId);
        rVal.setitemTemplate(itemTemplate);
        rVal.setcontainerType(containerType);
        rVal.setequipPointId(equipPointId);
        return rVal;
    }

    /**
     * Parses a message of type clientRequestWatchInventory
     */
    public static InventoryMessage parseclientRequestWatchInventoryMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 4){
            return null;
        }
        InventoryMessage rVal = (InventoryMessage)pool.get(MessageType.INVENTORY_MESSAGE);
        rVal.messageType = InventoryMessageType.CLIENTREQUESTWATCHINVENTORY;
        rVal.settargetEntId(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type clientRequestWatchInventory
     */
    public static InventoryMessage constructclientRequestWatchInventoryMessage(int targetEntId){
        InventoryMessage rVal = new InventoryMessage(InventoryMessageType.CLIENTREQUESTWATCHINVENTORY);
        rVal.settargetEntId(targetEntId);
        return rVal;
    }

    /**
     * Parses a message of type clientRequestUnwatchInventory
     */
    public static InventoryMessage parseclientRequestUnwatchInventoryMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 4){
            return null;
        }
        InventoryMessage rVal = (InventoryMessage)pool.get(MessageType.INVENTORY_MESSAGE);
        rVal.messageType = InventoryMessageType.CLIENTREQUESTUNWATCHINVENTORY;
        rVal.settargetEntId(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type clientRequestUnwatchInventory
     */
    public static InventoryMessage constructclientRequestUnwatchInventoryMessage(int targetEntId){
        InventoryMessage rVal = new InventoryMessage(InventoryMessageType.CLIENTREQUESTUNWATCHINVENTORY);
        rVal.settargetEntId(targetEntId);
        return rVal;
    }

    /**
     * Parses a message of type clientRequestAddToolbar
     */
    public static InventoryMessage parseclientRequestAddToolbarMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 8){
            return null;
        }
        InventoryMessage rVal = (InventoryMessage)pool.get(MessageType.INVENTORY_MESSAGE);
        rVal.messageType = InventoryMessageType.CLIENTREQUESTADDTOOLBAR;
        rVal.setentityId(byteBuffer.getInt());
        rVal.settoolbarId(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type clientRequestAddToolbar
     */
    public static InventoryMessage constructclientRequestAddToolbarMessage(int entityId,int toolbarId){
        InventoryMessage rVal = new InventoryMessage(InventoryMessageType.CLIENTREQUESTADDTOOLBAR);
        rVal.setentityId(entityId);
        rVal.settoolbarId(toolbarId);
        return rVal;
    }

    /**
     * Parses a message of type clientRequestAddNatural
     */
    public static InventoryMessage parseclientRequestAddNaturalMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 4){
            return null;
        }
        InventoryMessage rVal = (InventoryMessage)pool.get(MessageType.INVENTORY_MESSAGE);
        rVal.messageType = InventoryMessageType.CLIENTREQUESTADDNATURAL;
        rVal.setentityId(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type clientRequestAddNatural
     */
    public static InventoryMessage constructclientRequestAddNaturalMessage(int entityId){
        InventoryMessage rVal = new InventoryMessage(InventoryMessageType.CLIENTREQUESTADDNATURAL);
        rVal.setentityId(entityId);
        return rVal;
    }

    /**
     * Parses a message of type clientUpdateToolbar
     */
    public static InventoryMessage parseclientUpdateToolbarMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 4){
            return null;
        }
        InventoryMessage rVal = (InventoryMessage)pool.get(MessageType.INVENTORY_MESSAGE);
        rVal.messageType = InventoryMessageType.CLIENTUPDATETOOLBAR;
        rVal.settoolbarId(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type clientUpdateToolbar
     */
    public static InventoryMessage constructclientUpdateToolbarMessage(int toolbarId){
        InventoryMessage rVal = new InventoryMessage(InventoryMessageType.CLIENTUPDATETOOLBAR);
        rVal.settoolbarId(toolbarId);
        return rVal;
    }

    /**
     * Parses a message of type clientRequestPerformItemAction
     */
    public static InventoryMessage parseclientRequestPerformItemActionMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 36){
            return null;
        }
        int lenAccumulator = 0;
        int equipPointIdlen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + equipPointIdlen;
        if(byteBuffer.remaining() < 36 + lenAccumulator){
            return null;
        }
        InventoryMessage rVal = (InventoryMessage)pool.get(MessageType.INVENTORY_MESSAGE);
        rVal.messageType = InventoryMessageType.CLIENTREQUESTPERFORMITEMACTION;
        if(equipPointIdlen > 0){
            rVal.setequipPointId(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, equipPointIdlen));
        }
        rVal.setitemActionCode(byteBuffer.getInt());
        rVal.setitemActionCodeState(byteBuffer.getInt());
        rVal.setviewTargetX(byteBuffer.getDouble());
        rVal.setviewTargetY(byteBuffer.getDouble());
        rVal.setviewTargetZ(byteBuffer.getDouble());
        return rVal;
    }

    /**
     * Constructs a message of type clientRequestPerformItemAction
     */
    public static InventoryMessage constructclientRequestPerformItemActionMessage(String equipPointId,int itemActionCode,int itemActionCodeState,double viewTargetX,double viewTargetY,double viewTargetZ){
        InventoryMessage rVal = new InventoryMessage(InventoryMessageType.CLIENTREQUESTPERFORMITEMACTION);
        rVal.setequipPointId(equipPointId);
        rVal.setitemActionCode(itemActionCode);
        rVal.setitemActionCodeState(itemActionCodeState);
        rVal.setviewTargetX(viewTargetX);
        rVal.setviewTargetY(viewTargetY);
        rVal.setviewTargetZ(viewTargetZ);
        return rVal;
    }

    /**
     * Parses a message of type clientRequestCraft
     */
    public static InventoryMessage parseclientRequestCraftMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 12){
            return null;
        }
        InventoryMessage rVal = (InventoryMessage)pool.get(MessageType.INVENTORY_MESSAGE);
        rVal.messageType = InventoryMessageType.CLIENTREQUESTCRAFT;
        rVal.setentityId(byteBuffer.getInt());
        rVal.setstationId(byteBuffer.getInt());
        rVal.setrecipeId(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type clientRequestCraft
     */
    public static InventoryMessage constructclientRequestCraftMessage(int entityId,int stationId,int recipeId){
        InventoryMessage rVal = new InventoryMessage(InventoryMessageType.CLIENTREQUESTCRAFT);
        rVal.setentityId(entityId);
        rVal.setstationId(stationId);
        rVal.setrecipeId(recipeId);
        return rVal;
    }

    /**
     * Parses a message of type serverUpdateItemCharges
     */
    public static InventoryMessage parseserverUpdateItemChargesMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 8){
            return null;
        }
        InventoryMessage rVal = (InventoryMessage)pool.get(MessageType.INVENTORY_MESSAGE);
        rVal.messageType = InventoryMessageType.SERVERUPDATEITEMCHARGES;
        rVal.setentityId(byteBuffer.getInt());
        rVal.setcharges(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type serverUpdateItemCharges
     */
    public static InventoryMessage constructserverUpdateItemChargesMessage(int entityId,int charges){
        InventoryMessage rVal = new InventoryMessage(InventoryMessageType.SERVERUPDATEITEMCHARGES);
        rVal.setentityId(entityId);
        rVal.setcharges(charges);
        return rVal;
    }

    @Deprecated
    @Override
    void serialize(){
        byte[] intValues = new byte[8];
        byte[] stringBytes;
        switch(this.messageType){
            case ADDITEMTOINVENTORY:
                rawBytes = new byte[2+4+4+4+itemTemplate.length()];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_INVENTORY;
                //entity messaage header
                rawBytes[1] = TypeBytes.INVENTORY_MESSAGE_TYPE_ADDITEMTOINVENTORY;
                intValues = ByteStreamUtils.serializeIntToBytes(targetEntId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(itemEntId);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(itemTemplate.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                stringBytes = itemTemplate.getBytes();
                for(int i = 0; i < itemTemplate.length(); i++){
                    rawBytes[14+i] = stringBytes[i];
                }
                break;
            case REMOVEITEMFROMINVENTORY:
                rawBytes = new byte[2+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_INVENTORY;
                //entity messaage header
                rawBytes[1] = TypeBytes.INVENTORY_MESSAGE_TYPE_REMOVEITEMFROMINVENTORY;
                intValues = ByteStreamUtils.serializeIntToBytes(entityId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                break;
            case CLIENTREQUESTEQUIPITEM:
                rawBytes = new byte[2+4+equipPointId.length()+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_INVENTORY;
                //entity messaage header
                rawBytes[1] = TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTEQUIPITEM;
                intValues = ByteStreamUtils.serializeIntToBytes(equipPointId.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                stringBytes = equipPointId.getBytes();
                for(int i = 0; i < equipPointId.length(); i++){
                    rawBytes[6+i] = stringBytes[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(entityId);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+equipPointId.length()+i] = intValues[i];
                }
                break;
            case SERVERCOMMANDMOVEITEMCONTAINER:
                rawBytes = new byte[2+4+4+4+equipPointId.length()];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_INVENTORY;
                //entity messaage header
                rawBytes[1] = TypeBytes.INVENTORY_MESSAGE_TYPE_SERVERCOMMANDMOVEITEMCONTAINER;
                intValues = ByteStreamUtils.serializeIntToBytes(entityId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(containerType);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(equipPointId.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                stringBytes = equipPointId.getBytes();
                for(int i = 0; i < equipPointId.length(); i++){
                    rawBytes[14+i] = stringBytes[i];
                }
                break;
            case SERVERCOMMANDEQUIPITEM:
                rawBytes = new byte[2+4+4+4+equipPointId.length()+4+4+itemTemplate.length()];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_INVENTORY;
                //entity messaage header
                rawBytes[1] = TypeBytes.INVENTORY_MESSAGE_TYPE_SERVERCOMMANDEQUIPITEM;
                intValues = ByteStreamUtils.serializeIntToBytes(equipperId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(containerType);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(equipPointId.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                stringBytes = equipPointId.getBytes();
                for(int i = 0; i < equipPointId.length(); i++){
                    rawBytes[14+i] = stringBytes[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(entityId);
                for(int i = 0; i < 4; i++){
                    rawBytes[14+equipPointId.length()+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(itemTemplate.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[18+equipPointId.length()+i] = intValues[i];
                }
                stringBytes = itemTemplate.getBytes();
                for(int i = 0; i < itemTemplate.length(); i++){
                    rawBytes[22+equipPointId.length()+i] = stringBytes[i];
                }
                break;
            case SERVERCOMMANDUNEQUIPITEM:
                rawBytes = new byte[2+4+4+4+equipPointId.length()];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_INVENTORY;
                //entity messaage header
                rawBytes[1] = TypeBytes.INVENTORY_MESSAGE_TYPE_SERVERCOMMANDUNEQUIPITEM;
                intValues = ByteStreamUtils.serializeIntToBytes(equipperId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(containerType);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(equipPointId.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                stringBytes = equipPointId.getBytes();
                for(int i = 0; i < equipPointId.length(); i++){
                    rawBytes[14+i] = stringBytes[i];
                }
                break;
            case CLIENTREQUESTUNEQUIPITEM:
                rawBytes = new byte[2+4+equipPointId.length()];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_INVENTORY;
                //entity messaage header
                rawBytes[1] = TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTUNEQUIPITEM;
                intValues = ByteStreamUtils.serializeIntToBytes(equipPointId.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                stringBytes = equipPointId.getBytes();
                for(int i = 0; i < equipPointId.length(); i++){
                    rawBytes[6+i] = stringBytes[i];
                }
                break;
            case CLIENTREQUESTSTOREITEM:
                rawBytes = new byte[2+4+4+4+equipPointId.length()+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_INVENTORY;
                //entity messaage header
                rawBytes[1] = TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTSTOREITEM;
                intValues = ByteStreamUtils.serializeIntToBytes(targetEntId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(containerType);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(equipPointId.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                stringBytes = equipPointId.getBytes();
                for(int i = 0; i < equipPointId.length(); i++){
                    rawBytes[14+i] = stringBytes[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(itemEntId);
                for(int i = 0; i < 4; i++){
                    rawBytes[14+equipPointId.length()+i] = intValues[i];
                }
                break;
            case SERVERCOMMANDSTOREITEM:
                rawBytes = new byte[2+4+4+4+itemTemplate.length()+4+4+equipPointId.length()];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_INVENTORY;
                //entity messaage header
                rawBytes[1] = TypeBytes.INVENTORY_MESSAGE_TYPE_SERVERCOMMANDSTOREITEM;
                intValues = ByteStreamUtils.serializeIntToBytes(targetEntId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(itemEntId);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(itemTemplate.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                stringBytes = itemTemplate.getBytes();
                for(int i = 0; i < itemTemplate.length(); i++){
                    rawBytes[14+i] = stringBytes[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(containerType);
                for(int i = 0; i < 4; i++){
                    rawBytes[14+itemTemplate.length()+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(equipPointId.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[18+itemTemplate.length()+i] = intValues[i];
                }
                stringBytes = equipPointId.getBytes();
                for(int i = 0; i < equipPointId.length(); i++){
                    rawBytes[22+itemTemplate.length()+i] = stringBytes[i];
                }
                break;
            case CLIENTREQUESTWATCHINVENTORY:
                rawBytes = new byte[2+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_INVENTORY;
                //entity messaage header
                rawBytes[1] = TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTWATCHINVENTORY;
                intValues = ByteStreamUtils.serializeIntToBytes(targetEntId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                break;
            case CLIENTREQUESTUNWATCHINVENTORY:
                rawBytes = new byte[2+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_INVENTORY;
                //entity messaage header
                rawBytes[1] = TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTUNWATCHINVENTORY;
                intValues = ByteStreamUtils.serializeIntToBytes(targetEntId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                break;
            case CLIENTREQUESTADDTOOLBAR:
                rawBytes = new byte[2+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_INVENTORY;
                //entity messaage header
                rawBytes[1] = TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTADDTOOLBAR;
                intValues = ByteStreamUtils.serializeIntToBytes(entityId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(toolbarId);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                break;
            case CLIENTREQUESTADDNATURAL:
                rawBytes = new byte[2+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_INVENTORY;
                //entity messaage header
                rawBytes[1] = TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTADDNATURAL;
                intValues = ByteStreamUtils.serializeIntToBytes(entityId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                break;
            case CLIENTUPDATETOOLBAR:
                rawBytes = new byte[2+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_INVENTORY;
                //entity messaage header
                rawBytes[1] = TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTUPDATETOOLBAR;
                intValues = ByteStreamUtils.serializeIntToBytes(toolbarId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                break;
            case CLIENTREQUESTPERFORMITEMACTION:
                rawBytes = new byte[2+4+equipPointId.length()+4+4+8+8+8];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_INVENTORY;
                //entity messaage header
                rawBytes[1] = TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTPERFORMITEMACTION;
                intValues = ByteStreamUtils.serializeIntToBytes(equipPointId.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                stringBytes = equipPointId.getBytes();
                for(int i = 0; i < equipPointId.length(); i++){
                    rawBytes[6+i] = stringBytes[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(itemActionCode);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+equipPointId.length()+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(itemActionCodeState);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+equipPointId.length()+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(viewTargetX);
                for(int i = 0; i < 8; i++){
                    rawBytes[14+equipPointId.length()+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(viewTargetY);
                for(int i = 0; i < 8; i++){
                    rawBytes[22+equipPointId.length()+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(viewTargetZ);
                for(int i = 0; i < 8; i++){
                    rawBytes[30+equipPointId.length()+i] = intValues[i];
                }
                break;
            case CLIENTREQUESTCRAFT:
                rawBytes = new byte[2+4+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_INVENTORY;
                //entity messaage header
                rawBytes[1] = TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTCRAFT;
                intValues = ByteStreamUtils.serializeIntToBytes(entityId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(stationId);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(recipeId);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                break;
            case SERVERUPDATEITEMCHARGES:
                rawBytes = new byte[2+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_INVENTORY;
                //entity messaage header
                rawBytes[1] = TypeBytes.INVENTORY_MESSAGE_TYPE_SERVERUPDATEITEMCHARGES;
                intValues = ByteStreamUtils.serializeIntToBytes(entityId);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(charges);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                break;
        }
        serialized = true;
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        switch(this.messageType){
            case ADDITEMTOINVENTORY: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_INVENTORY);
                stream.write(TypeBytes.INVENTORY_MESSAGE_TYPE_ADDITEMTOINVENTORY);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, itemTemplate.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, targetEntId);
                ByteStreamUtils.writeInt(stream, itemEntId);
                ByteStreamUtils.writeString(stream, itemTemplate);
            } break;
            case REMOVEITEMFROMINVENTORY: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_INVENTORY);
                stream.write(TypeBytes.INVENTORY_MESSAGE_TYPE_REMOVEITEMFROMINVENTORY);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityId);
            } break;
            case CLIENTREQUESTEQUIPITEM: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_INVENTORY);
                stream.write(TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTEQUIPITEM);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, equipPointId.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeString(stream, equipPointId);
                ByteStreamUtils.writeInt(stream, entityId);
            } break;
            case SERVERCOMMANDMOVEITEMCONTAINER: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_INVENTORY);
                stream.write(TypeBytes.INVENTORY_MESSAGE_TYPE_SERVERCOMMANDMOVEITEMCONTAINER);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, equipPointId.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityId);
                ByteStreamUtils.writeInt(stream, containerType);
                ByteStreamUtils.writeString(stream, equipPointId);
            } break;
            case SERVERCOMMANDEQUIPITEM: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_INVENTORY);
                stream.write(TypeBytes.INVENTORY_MESSAGE_TYPE_SERVERCOMMANDEQUIPITEM);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, equipPointId.getBytes().length);
                ByteStreamUtils.writeInt(stream, itemTemplate.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, equipperId);
                ByteStreamUtils.writeInt(stream, containerType);
                ByteStreamUtils.writeString(stream, equipPointId);
                ByteStreamUtils.writeInt(stream, entityId);
                ByteStreamUtils.writeString(stream, itemTemplate);
            } break;
            case SERVERCOMMANDUNEQUIPITEM: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_INVENTORY);
                stream.write(TypeBytes.INVENTORY_MESSAGE_TYPE_SERVERCOMMANDUNEQUIPITEM);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, equipPointId.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, equipperId);
                ByteStreamUtils.writeInt(stream, containerType);
                ByteStreamUtils.writeString(stream, equipPointId);
            } break;
            case CLIENTREQUESTUNEQUIPITEM: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_INVENTORY);
                stream.write(TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTUNEQUIPITEM);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, equipPointId.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeString(stream, equipPointId);
            } break;
            case CLIENTREQUESTSTOREITEM: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_INVENTORY);
                stream.write(TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTSTOREITEM);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, equipPointId.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, targetEntId);
                ByteStreamUtils.writeInt(stream, containerType);
                ByteStreamUtils.writeString(stream, equipPointId);
                ByteStreamUtils.writeInt(stream, itemEntId);
            } break;
            case SERVERCOMMANDSTOREITEM: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_INVENTORY);
                stream.write(TypeBytes.INVENTORY_MESSAGE_TYPE_SERVERCOMMANDSTOREITEM);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, itemTemplate.getBytes().length);
                ByteStreamUtils.writeInt(stream, equipPointId.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, targetEntId);
                ByteStreamUtils.writeInt(stream, itemEntId);
                ByteStreamUtils.writeString(stream, itemTemplate);
                ByteStreamUtils.writeInt(stream, containerType);
                ByteStreamUtils.writeString(stream, equipPointId);
            } break;
            case CLIENTREQUESTWATCHINVENTORY: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_INVENTORY);
                stream.write(TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTWATCHINVENTORY);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, targetEntId);
            } break;
            case CLIENTREQUESTUNWATCHINVENTORY: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_INVENTORY);
                stream.write(TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTUNWATCHINVENTORY);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, targetEntId);
            } break;
            case CLIENTREQUESTADDTOOLBAR: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_INVENTORY);
                stream.write(TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTADDTOOLBAR);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityId);
                ByteStreamUtils.writeInt(stream, toolbarId);
            } break;
            case CLIENTREQUESTADDNATURAL: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_INVENTORY);
                stream.write(TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTADDNATURAL);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityId);
            } break;
            case CLIENTUPDATETOOLBAR: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_INVENTORY);
                stream.write(TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTUPDATETOOLBAR);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, toolbarId);
            } break;
            case CLIENTREQUESTPERFORMITEMACTION: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_INVENTORY);
                stream.write(TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTPERFORMITEMACTION);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, equipPointId.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeString(stream, equipPointId);
                ByteStreamUtils.writeInt(stream, itemActionCode);
                ByteStreamUtils.writeInt(stream, itemActionCodeState);
                ByteStreamUtils.writeDouble(stream, viewTargetX);
                ByteStreamUtils.writeDouble(stream, viewTargetY);
                ByteStreamUtils.writeDouble(stream, viewTargetZ);
            } break;
            case CLIENTREQUESTCRAFT: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_INVENTORY);
                stream.write(TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTCRAFT);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityId);
                ByteStreamUtils.writeInt(stream, stationId);
                ByteStreamUtils.writeInt(stream, recipeId);
            } break;
            case SERVERUPDATEITEMCHARGES: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_INVENTORY);
                stream.write(TypeBytes.INVENTORY_MESSAGE_TYPE_SERVERUPDATEITEMCHARGES);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, entityId);
                ByteStreamUtils.writeInt(stream, charges);
            } break;
        }
    }

}
