package electrosphere.net.parser.net.message;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * A network message
 */
public abstract class NetworkMessage {
    
    /**
     * The different categories of network messages
     */
    public enum MessageType {
        ENTITY_MESSAGE,
        LORE_MESSAGE,
        PLAYER_MESSAGE,
        TERRAIN_MESSAGE,
        SERVER_MESSAGE,
        AUTH_MESSAGE,
        CHARACTER_MESSAGE,
        INVENTORY_MESSAGE,
        SYNCHRONIZATION_MESSAGE,
        COMBAT_MESSAGE,
    }
    
    /**
     * The type of this message
     */
    MessageType type;

    /**
     * Tracks whether the message has been serialized to bytes or not
     */
    boolean serialized;

    /**
     * The raw bytes contained in the message
     */
    byte[] rawBytes;

    /**
     * Extra data that can be attached to a message optionally (used for reading in messages, does not affect ougoing messages).
     */
    private List<Object> extraData;

    /**
     * Gets the type of the message
     * @return The type of the message
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Gets the raw bytes of the message
     * @return The raw bytes
     */
    public byte[] getRawBytes() {
        return rawBytes;
    }
    
    /**
     * Parses the byte stream for the next message
     * @param byteBuffer The byte buffer
     * @param pool The message pool
     * @param customParserMap The map of message type/subtype to parser
     * @return The message if one is at the front of the byte stream, null otherwise
     */
    public static NetworkMessage parseBytestreamForMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        NetworkMessage rVal = null;
        byte firstByte;
        byte secondByte;
        int initialPosition = byteBuffer.position();
        if(byteBuffer.remaining() >= 2){
            firstByte = byteBuffer.get();
            switch(firstByte){
                case TypeBytes.MESSAGE_TYPE_ENTITY:
                    secondByte = byteBuffer.get();
                    switch(secondByte){
                    case TypeBytes.ENTITY_MESSAGE_TYPE_CREATE:
                        rVal = EntityMessage.parseCreateMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.ENTITY_MESSAGE_TYPE_MOVEUPDATE:
                        rVal = EntityMessage.parsemoveUpdateMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.ENTITY_MESSAGE_TYPE_ATTACKUPDATE:
                        rVal = EntityMessage.parseattackUpdateMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.ENTITY_MESSAGE_TYPE_STARTATTACK:
                        rVal = EntityMessage.parsestartAttackMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.ENTITY_MESSAGE_TYPE_KILL:
                        rVal = EntityMessage.parseKillMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.ENTITY_MESSAGE_TYPE_DESTROY:
                        rVal = EntityMessage.parseDestroyMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.ENTITY_MESSAGE_TYPE_SETPROPERTY:
                        rVal = EntityMessage.parsesetPropertyMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.ENTITY_MESSAGE_TYPE_ATTACHENTITYTOENTITY:
                        rVal = EntityMessage.parseattachEntityToEntityMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.ENTITY_MESSAGE_TYPE_UPDATEENTITYVIEWDIR:
                        rVal = EntityMessage.parseupdateEntityViewDirMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.ENTITY_MESSAGE_TYPE_SYNCPHYSICS:
                        rVal = EntityMessage.parsesyncPhysicsMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.ENTITY_MESSAGE_TYPE_INTERACT:
                        rVal = EntityMessage.parseinteractMessage(byteBuffer,pool,customParserMap);
                        break;
                }
                break;
                case TypeBytes.MESSAGE_TYPE_LORE:
                    secondByte = byteBuffer.get();
                    switch(secondByte){
                    case TypeBytes.LORE_MESSAGE_TYPE_REQUESTRACES:
                        rVal = LoreMessage.parseRequestRacesMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.LORE_MESSAGE_TYPE_RESPONSERACES:
                        rVal = LoreMessage.parseResponseRacesMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.LORE_MESSAGE_TYPE_TEMPORALUPDATE:
                        rVal = LoreMessage.parseTemporalUpdateMessage(byteBuffer,pool,customParserMap);
                        break;
                }
                break;
                case TypeBytes.MESSAGE_TYPE_PLAYER:
                    secondByte = byteBuffer.get();
                    switch(secondByte){
                    case TypeBytes.PLAYER_MESSAGE_TYPE_SET_ID:
                        rVal = PlayerMessage.parseSet_IDMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.PLAYER_MESSAGE_TYPE_SETINITIALDISCRETEPOSITION:
                        rVal = PlayerMessage.parseSetInitialDiscretePositionMessage(byteBuffer,pool,customParserMap);
                        break;
                }
                break;
                case TypeBytes.MESSAGE_TYPE_TERRAIN:
                    secondByte = byteBuffer.get();
                    switch(secondByte){
                    case TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTMETADATA:
                        rVal = TerrainMessage.parseRequestMetadataMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.TERRAIN_MESSAGE_TYPE_RESPONSEMETADATA:
                        rVal = TerrainMessage.parseResponseMetadataMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTEDITVOXEL:
                        rVal = TerrainMessage.parseRequestEditVoxelMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.TERRAIN_MESSAGE_TYPE_UPDATEVOXEL:
                        rVal = TerrainMessage.parseUpdateVoxelMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTUSETERRAINPALETTE:
                        rVal = TerrainMessage.parseRequestUseTerrainPaletteMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTDESTROYTERRAIN:
                        rVal = TerrainMessage.parseRequestDestroyTerrainMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.TERRAIN_MESSAGE_TYPE_SPAWNPOSITION:
                        rVal = TerrainMessage.parseSpawnPositionMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTCHUNKDATA:
                        rVal = TerrainMessage.parseRequestChunkDataMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.TERRAIN_MESSAGE_TYPE_SENDCHUNKDATA:
                        rVal = TerrainMessage.parsesendChunkDataMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTREDUCEDCHUNKDATA:
                        rVal = TerrainMessage.parseRequestReducedChunkDataMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.TERRAIN_MESSAGE_TYPE_SENDREDUCEDCHUNKDATA:
                        rVal = TerrainMessage.parseSendReducedChunkDataMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTREDUCEDBLOCKDATA:
                        rVal = TerrainMessage.parseRequestReducedBlockDataMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.TERRAIN_MESSAGE_TYPE_SENDREDUCEDBLOCKDATA:
                        rVal = TerrainMessage.parseSendReducedBlockDataMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.TERRAIN_MESSAGE_TYPE_UPDATEBLOCK:
                        rVal = TerrainMessage.parseUpdateBlockMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTFLUIDDATA:
                        rVal = TerrainMessage.parseRequestFluidDataMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.TERRAIN_MESSAGE_TYPE_SENDFLUIDDATA:
                        rVal = TerrainMessage.parsesendFluidDataMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.TERRAIN_MESSAGE_TYPE_UPDATEFLUIDDATA:
                        rVal = TerrainMessage.parseupdateFluidDataMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTEDITBLOCK:
                        rVal = TerrainMessage.parseRequestEditBlockMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTPLACEFAB:
                        rVal = TerrainMessage.parseRequestPlaceFabMessage(byteBuffer,pool,customParserMap);
                        break;
                }
                break;
                case TypeBytes.MESSAGE_TYPE_SERVER:
                    secondByte = byteBuffer.get();
                    switch(secondByte){
                    case TypeBytes.SERVER_MESSAGE_TYPE_PING:
                        rVal = ServerMessage.parsePingMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.SERVER_MESSAGE_TYPE_PONG:
                        rVal = ServerMessage.parsePongMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.SERVER_MESSAGE_TYPE_DISCONNECT:
                        rVal = ServerMessage.parseDisconnectMessage(byteBuffer,pool,customParserMap);
                        break;
                }
                break;
                case TypeBytes.MESSAGE_TYPE_AUTH:
                    secondByte = byteBuffer.get();
                    switch(secondByte){
                    case TypeBytes.AUTH_MESSAGE_TYPE_AUTHREQUEST:
                        rVal = AuthMessage.parseAuthRequestMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.AUTH_MESSAGE_TYPE_AUTHDETAILS:
                        rVal = AuthMessage.parseAuthDetailsMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.AUTH_MESSAGE_TYPE_AUTHSUCCESS:
                        rVal = AuthMessage.parseAuthSuccessMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.AUTH_MESSAGE_TYPE_AUTHFAILURE:
                        rVal = AuthMessage.parseAuthFailureMessage(byteBuffer,pool,customParserMap);
                        break;
                }
                break;
                case TypeBytes.MESSAGE_TYPE_CHARACTER:
                    secondByte = byteBuffer.get();
                    switch(secondByte){
                    case TypeBytes.CHARACTER_MESSAGE_TYPE_REQUESTCHARACTERLIST:
                        rVal = CharacterMessage.parseRequestCharacterListMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.CHARACTER_MESSAGE_TYPE_RESPONSECHARACTERLIST:
                        rVal = CharacterMessage.parseResponseCharacterListMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.CHARACTER_MESSAGE_TYPE_REQUESTCREATECHARACTER:
                        rVal = CharacterMessage.parseRequestCreateCharacterMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.CHARACTER_MESSAGE_TYPE_RESPONSECREATECHARACTERSUCCESS:
                        rVal = CharacterMessage.parseResponseCreateCharacterSuccessMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.CHARACTER_MESSAGE_TYPE_RESPONSECREATECHARACTERFAILURE:
                        rVal = CharacterMessage.parseResponseCreateCharacterFailureMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.CHARACTER_MESSAGE_TYPE_REQUESTSPAWNCHARACTER:
                        rVal = CharacterMessage.parseRequestSpawnCharacterMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.CHARACTER_MESSAGE_TYPE_RESPONSESPAWNCHARACTER:
                        rVal = CharacterMessage.parseResponseSpawnCharacterMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.CHARACTER_MESSAGE_TYPE_EDITORSWAP:
                        rVal = CharacterMessage.parseEditorSwapMessage(byteBuffer,pool,customParserMap);
                        break;
                }
                break;
                case TypeBytes.MESSAGE_TYPE_INVENTORY:
                    secondByte = byteBuffer.get();
                    switch(secondByte){
                    case TypeBytes.INVENTORY_MESSAGE_TYPE_ADDITEMTOINVENTORY:
                        rVal = InventoryMessage.parseaddItemToInventoryMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.INVENTORY_MESSAGE_TYPE_REMOVEITEMFROMINVENTORY:
                        rVal = InventoryMessage.parseremoveItemFromInventoryMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTEQUIPITEM:
                        rVal = InventoryMessage.parseclientRequestEquipItemMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.INVENTORY_MESSAGE_TYPE_SERVERCOMMANDMOVEITEMCONTAINER:
                        rVal = InventoryMessage.parseserverCommandMoveItemContainerMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.INVENTORY_MESSAGE_TYPE_SERVERCOMMANDEQUIPITEM:
                        rVal = InventoryMessage.parseserverCommandEquipItemMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.INVENTORY_MESSAGE_TYPE_SERVERCOMMANDUNEQUIPITEM:
                        rVal = InventoryMessage.parseserverCommandUnequipItemMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTUNEQUIPITEM:
                        rVal = InventoryMessage.parseclientRequestUnequipItemMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTSTOREITEM:
                        rVal = InventoryMessage.parseclientRequestStoreItemMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.INVENTORY_MESSAGE_TYPE_SERVERCOMMANDSTOREITEM:
                        rVal = InventoryMessage.parseserverCommandStoreItemMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTWATCHINVENTORY:
                        rVal = InventoryMessage.parseclientRequestWatchInventoryMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTUNWATCHINVENTORY:
                        rVal = InventoryMessage.parseclientRequestUnwatchInventoryMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTADDTOOLBAR:
                        rVal = InventoryMessage.parseclientRequestAddToolbarMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTADDNATURAL:
                        rVal = InventoryMessage.parseclientRequestAddNaturalMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTUPDATETOOLBAR:
                        rVal = InventoryMessage.parseclientUpdateToolbarMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTPERFORMITEMACTION:
                        rVal = InventoryMessage.parseclientRequestPerformItemActionMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.INVENTORY_MESSAGE_TYPE_CLIENTREQUESTCRAFT:
                        rVal = InventoryMessage.parseclientRequestCraftMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.INVENTORY_MESSAGE_TYPE_SERVERUPDATEITEMCHARGES:
                        rVal = InventoryMessage.parseserverUpdateItemChargesMessage(byteBuffer,pool,customParserMap);
                        break;
                }
                break;
                case TypeBytes.MESSAGE_TYPE_SYNCHRONIZATION:
                    secondByte = byteBuffer.get();
                    switch(secondByte){
                    case TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTSTATE:
                        rVal = SynchronizationMessage.parseUpdateClientStateMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTSTRINGSTATE:
                        rVal = SynchronizationMessage.parseUpdateClientStringStateMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTINTSTATE:
                        rVal = SynchronizationMessage.parseUpdateClientIntStateMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTLONGSTATE:
                        rVal = SynchronizationMessage.parseUpdateClientLongStateMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTFLOATSTATE:
                        rVal = SynchronizationMessage.parseUpdateClientFloatStateMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTDOUBLESTATE:
                        rVal = SynchronizationMessage.parseUpdateClientDoubleStateMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_CLIENTREQUESTBTREEACTION:
                        rVal = SynchronizationMessage.parseClientRequestBTreeActionMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_SERVERNOTIFYBTREETRANSITION:
                        rVal = SynchronizationMessage.parseServerNotifyBTreeTransitionMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_ATTACHTREE:
                        rVal = SynchronizationMessage.parseAttachTreeMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_DETATCHTREE:
                        rVal = SynchronizationMessage.parseDetatchTreeMessage(byteBuffer,pool,customParserMap);
                        break;
                    case TypeBytes.SYNCHRONIZATION_MESSAGE_TYPE_LOADSCENE:
                        rVal = SynchronizationMessage.parseLoadSceneMessage(byteBuffer,pool,customParserMap);
                        break;
                }
                break;
                case TypeBytes.MESSAGE_TYPE_COMBAT:
                    secondByte = byteBuffer.get();
                    switch(secondByte){
                    case TypeBytes.COMBAT_MESSAGE_TYPE_SERVERREPORTHITBOXCOLLISION:
                        rVal = CombatMessage.parseserverReportHitboxCollisionMessage(byteBuffer,pool,customParserMap);
                        break;
                }
                break;
                default:
                throw new Error("Unsupported message type! " + firstByte);
            }
            if(rVal == null){
                //failed to read the message
                byteBuffer.position(initialPosition);
            }
        }
        return rVal;
    }

    /**
     * Writes this message to the output stream
     * @param stream The stream
     */
    public abstract void write(OutputStream stream) throws IOException;
    
    /**
     * Checks if this message is serialized or not
     * @return true if it is serialized, false otherwise
     */
    public boolean isSerialized(){
        return serialized;
    }
    
    /**
     * Serializes the message
     */
    abstract void serialize();

    /**
     * Gets the extra data attached to the message
     * @return The extra data if it exists, null otherwise
     */
    public List<Object> getExtraData(){
        return this.extraData;
    }

    /**
     * Sets the extra data on the message
     * @param extraData The extra data
     */
    public void setExtraData(List<Object> extraData){
        this.extraData = extraData;
    }
    
}

