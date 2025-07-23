package electrosphere.net.parser.net.message;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import electrosphere.net.parser.util.ByteStreamUtils;
import java.util.Map;
import java.util.function.BiConsumer;

public class TerrainMessage extends NetworkMessage {

    /**
     * The types of messages available in this category.
     */
    public enum TerrainMessageType {
        REQUESTMETADATA,
        RESPONSEMETADATA,
        REQUESTEDITVOXEL,
        UPDATEVOXEL,
        REQUESTUSETERRAINPALETTE,
        REQUESTDESTROYTERRAIN,
        SPAWNPOSITION,
        REQUESTCHUNKDATA,
        SENDCHUNKDATA,
        REQUESTREDUCEDCHUNKDATA,
        SENDREDUCEDCHUNKDATA,
        REQUESTREDUCEDBLOCKDATA,
        SENDREDUCEDBLOCKDATA,
        UPDATEBLOCK,
        REQUESTFLUIDDATA,
        SENDFLUIDDATA,
        UPDATEFLUIDDATA,
        REQUESTEDITBLOCK,
        REQUESTPLACEFAB,
    }

    /**
     * The type of this message in particular.
     */
    TerrainMessageType messageType;
    int worldSizeDiscrete;
    int dynamicInterpolationRatio;
    float randomDampener;
    int worldMinX;
    int worldMinY;
    int worldMinZ;
    int worldMaxX;
    int worldMaxY;
    int worldMaxZ;
    float value;
    int worldX;
    int worldY;
    int worldZ;
    int voxelX;
    int voxelY;
    int voxelZ;
    double realLocationX;
    double realLocationY;
    double realLocationZ;
    byte[] chunkData;
    int homogenousValue;
    int chunkResolution;
    float terrainWeight;
    int terrainValue;
    int blockType;
    int blockMetadata;
    int blockEditSize;
    String fabPath;
    int blockRotation;

    /**
     * Constructor
     * @param messageType The type of this message
     */
    private TerrainMessage(TerrainMessageType messageType){
        this.type = MessageType.TERRAIN_MESSAGE;
        this.messageType = messageType;
    }

    /**
     * Constructor
     */
    protected TerrainMessage(){
        this.type = MessageType.TERRAIN_MESSAGE;
    }

    public TerrainMessageType getMessageSubtype(){
        return this.messageType;
    }

    /**
     * Gets worldSizeDiscrete
     */
    public int getworldSizeDiscrete() {
        return worldSizeDiscrete;
    }

    /**
     * Sets worldSizeDiscrete
     */
    public void setworldSizeDiscrete(int worldSizeDiscrete) {
        this.worldSizeDiscrete = worldSizeDiscrete;
    }

    /**
     * Gets dynamicInterpolationRatio
     */
    public int getdynamicInterpolationRatio() {
        return dynamicInterpolationRatio;
    }

    /**
     * Sets dynamicInterpolationRatio
     */
    public void setdynamicInterpolationRatio(int dynamicInterpolationRatio) {
        this.dynamicInterpolationRatio = dynamicInterpolationRatio;
    }

    /**
     * Gets randomDampener
     */
    public float getrandomDampener() {
        return randomDampener;
    }

    /**
     * Sets randomDampener
     */
    public void setrandomDampener(float randomDampener) {
        this.randomDampener = randomDampener;
    }

    /**
     * Gets worldMinX
     */
    public int getworldMinX() {
        return worldMinX;
    }

    /**
     * Sets worldMinX
     */
    public void setworldMinX(int worldMinX) {
        this.worldMinX = worldMinX;
    }

    /**
     * Gets worldMinY
     */
    public int getworldMinY() {
        return worldMinY;
    }

    /**
     * Sets worldMinY
     */
    public void setworldMinY(int worldMinY) {
        this.worldMinY = worldMinY;
    }

    /**
     * Gets worldMinZ
     */
    public int getworldMinZ() {
        return worldMinZ;
    }

    /**
     * Sets worldMinZ
     */
    public void setworldMinZ(int worldMinZ) {
        this.worldMinZ = worldMinZ;
    }

    /**
     * Gets worldMaxX
     */
    public int getworldMaxX() {
        return worldMaxX;
    }

    /**
     * Sets worldMaxX
     */
    public void setworldMaxX(int worldMaxX) {
        this.worldMaxX = worldMaxX;
    }

    /**
     * Gets worldMaxY
     */
    public int getworldMaxY() {
        return worldMaxY;
    }

    /**
     * Sets worldMaxY
     */
    public void setworldMaxY(int worldMaxY) {
        this.worldMaxY = worldMaxY;
    }

    /**
     * Gets worldMaxZ
     */
    public int getworldMaxZ() {
        return worldMaxZ;
    }

    /**
     * Sets worldMaxZ
     */
    public void setworldMaxZ(int worldMaxZ) {
        this.worldMaxZ = worldMaxZ;
    }

    /**
     * Gets value
     */
    public float getvalue() {
        return value;
    }

    /**
     * Sets value
     */
    public void setvalue(float value) {
        this.value = value;
    }

    /**
     * Gets worldX
     */
    public int getworldX() {
        return worldX;
    }

    /**
     * Sets worldX
     */
    public void setworldX(int worldX) {
        this.worldX = worldX;
    }

    /**
     * Gets worldY
     */
    public int getworldY() {
        return worldY;
    }

    /**
     * Sets worldY
     */
    public void setworldY(int worldY) {
        this.worldY = worldY;
    }

    /**
     * Gets worldZ
     */
    public int getworldZ() {
        return worldZ;
    }

    /**
     * Sets worldZ
     */
    public void setworldZ(int worldZ) {
        this.worldZ = worldZ;
    }

    /**
     * Gets voxelX
     */
    public int getvoxelX() {
        return voxelX;
    }

    /**
     * Sets voxelX
     */
    public void setvoxelX(int voxelX) {
        this.voxelX = voxelX;
    }

    /**
     * Gets voxelY
     */
    public int getvoxelY() {
        return voxelY;
    }

    /**
     * Sets voxelY
     */
    public void setvoxelY(int voxelY) {
        this.voxelY = voxelY;
    }

    /**
     * Gets voxelZ
     */
    public int getvoxelZ() {
        return voxelZ;
    }

    /**
     * Sets voxelZ
     */
    public void setvoxelZ(int voxelZ) {
        this.voxelZ = voxelZ;
    }

    /**
     * Gets realLocationX
     */
    public double getrealLocationX() {
        return realLocationX;
    }

    /**
     * Sets realLocationX
     */
    public void setrealLocationX(double realLocationX) {
        this.realLocationX = realLocationX;
    }

    /**
     * Gets realLocationY
     */
    public double getrealLocationY() {
        return realLocationY;
    }

    /**
     * Sets realLocationY
     */
    public void setrealLocationY(double realLocationY) {
        this.realLocationY = realLocationY;
    }

    /**
     * Gets realLocationZ
     */
    public double getrealLocationZ() {
        return realLocationZ;
    }

    /**
     * Sets realLocationZ
     */
    public void setrealLocationZ(double realLocationZ) {
        this.realLocationZ = realLocationZ;
    }

    /**
     * Gets chunkData
     */
    public byte[] getchunkData() {
        return chunkData;
    }

    /**
     * Sets chunkData
     */
    public void setchunkData(byte[] chunkData) {
        this.chunkData = chunkData;
    }

    /**
     * Gets homogenousValue
     */
    public int gethomogenousValue() {
        return homogenousValue;
    }

    /**
     * Sets homogenousValue
     */
    public void sethomogenousValue(int homogenousValue) {
        this.homogenousValue = homogenousValue;
    }

    /**
     * Gets chunkResolution
     */
    public int getchunkResolution() {
        return chunkResolution;
    }

    /**
     * Sets chunkResolution
     */
    public void setchunkResolution(int chunkResolution) {
        this.chunkResolution = chunkResolution;
    }

    /**
     * Gets terrainWeight
     */
    public float getterrainWeight() {
        return terrainWeight;
    }

    /**
     * Sets terrainWeight
     */
    public void setterrainWeight(float terrainWeight) {
        this.terrainWeight = terrainWeight;
    }

    /**
     * Gets terrainValue
     */
    public int getterrainValue() {
        return terrainValue;
    }

    /**
     * Sets terrainValue
     */
    public void setterrainValue(int terrainValue) {
        this.terrainValue = terrainValue;
    }

    /**
     * Gets blockType
     */
    public int getblockType() {
        return blockType;
    }

    /**
     * Sets blockType
     */
    public void setblockType(int blockType) {
        this.blockType = blockType;
    }

    /**
     * Gets blockMetadata
     */
    public int getblockMetadata() {
        return blockMetadata;
    }

    /**
     * Sets blockMetadata
     */
    public void setblockMetadata(int blockMetadata) {
        this.blockMetadata = blockMetadata;
    }

    /**
     * Gets blockEditSize
     */
    public int getblockEditSize() {
        return blockEditSize;
    }

    /**
     * Sets blockEditSize
     */
    public void setblockEditSize(int blockEditSize) {
        this.blockEditSize = blockEditSize;
    }

    /**
     * Gets fabPath
     */
    public String getfabPath() {
        return fabPath;
    }

    /**
     * Sets fabPath
     */
    public void setfabPath(String fabPath) {
        this.fabPath = fabPath;
    }

    /**
     * Gets blockRotation
     */
    public int getblockRotation() {
        return blockRotation;
    }

    /**
     * Sets blockRotation
     */
    public void setblockRotation(int blockRotation) {
        this.blockRotation = blockRotation;
    }

    /**
     * Parses a message of type RequestMetadata
     */
    public static TerrainMessage parseRequestMetadataMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 0){
            return null;
        }
        TerrainMessage rVal = (TerrainMessage)pool.get(MessageType.TERRAIN_MESSAGE);
        rVal.messageType = TerrainMessageType.REQUESTMETADATA;
        return rVal;
    }

    /**
     * Constructs a message of type RequestMetadata
     */
    public static TerrainMessage constructRequestMetadataMessage(){
        TerrainMessage rVal = new TerrainMessage(TerrainMessageType.REQUESTMETADATA);
        return rVal;
    }

    /**
     * Parses a message of type ResponseMetadata
     */
    public static TerrainMessage parseResponseMetadataMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 28){
            return null;
        }
        TerrainMessage rVal = (TerrainMessage)pool.get(MessageType.TERRAIN_MESSAGE);
        rVal.messageType = TerrainMessageType.RESPONSEMETADATA;
        rVal.setworldSizeDiscrete(byteBuffer.getInt());
        rVal.setworldMinX(byteBuffer.getInt());
        rVal.setworldMinY(byteBuffer.getInt());
        rVal.setworldMinZ(byteBuffer.getInt());
        rVal.setworldMaxX(byteBuffer.getInt());
        rVal.setworldMaxY(byteBuffer.getInt());
        rVal.setworldMaxZ(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type ResponseMetadata
     */
    public static TerrainMessage constructResponseMetadataMessage(int worldSizeDiscrete,int worldMinX,int worldMinY,int worldMinZ,int worldMaxX,int worldMaxY,int worldMaxZ){
        TerrainMessage rVal = new TerrainMessage(TerrainMessageType.RESPONSEMETADATA);
        rVal.setworldSizeDiscrete(worldSizeDiscrete);
        rVal.setworldMinX(worldMinX);
        rVal.setworldMinY(worldMinY);
        rVal.setworldMinZ(worldMinZ);
        rVal.setworldMaxX(worldMaxX);
        rVal.setworldMaxY(worldMaxY);
        rVal.setworldMaxZ(worldMaxZ);
        return rVal;
    }

    /**
     * Parses a message of type RequestEditVoxel
     */
    public static TerrainMessage parseRequestEditVoxelMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 32){
            return null;
        }
        TerrainMessage rVal = (TerrainMessage)pool.get(MessageType.TERRAIN_MESSAGE);
        rVal.messageType = TerrainMessageType.REQUESTEDITVOXEL;
        rVal.setworldX(byteBuffer.getInt());
        rVal.setworldY(byteBuffer.getInt());
        rVal.setworldZ(byteBuffer.getInt());
        rVal.setvoxelX(byteBuffer.getInt());
        rVal.setvoxelY(byteBuffer.getInt());
        rVal.setvoxelZ(byteBuffer.getInt());
        rVal.setterrainWeight(byteBuffer.getFloat());
        rVal.setterrainValue(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type RequestEditVoxel
     */
    public static TerrainMessage constructRequestEditVoxelMessage(int worldX,int worldY,int worldZ,int voxelX,int voxelY,int voxelZ,float terrainWeight,int terrainValue){
        TerrainMessage rVal = new TerrainMessage(TerrainMessageType.REQUESTEDITVOXEL);
        rVal.setworldX(worldX);
        rVal.setworldY(worldY);
        rVal.setworldZ(worldZ);
        rVal.setvoxelX(voxelX);
        rVal.setvoxelY(voxelY);
        rVal.setvoxelZ(voxelZ);
        rVal.setterrainWeight(terrainWeight);
        rVal.setterrainValue(terrainValue);
        return rVal;
    }

    /**
     * Parses a message of type UpdateVoxel
     */
    public static TerrainMessage parseUpdateVoxelMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 32){
            return null;
        }
        TerrainMessage rVal = (TerrainMessage)pool.get(MessageType.TERRAIN_MESSAGE);
        rVal.messageType = TerrainMessageType.UPDATEVOXEL;
        rVal.setworldX(byteBuffer.getInt());
        rVal.setworldY(byteBuffer.getInt());
        rVal.setworldZ(byteBuffer.getInt());
        rVal.setvoxelX(byteBuffer.getInt());
        rVal.setvoxelY(byteBuffer.getInt());
        rVal.setvoxelZ(byteBuffer.getInt());
        rVal.setterrainWeight(byteBuffer.getFloat());
        rVal.setterrainValue(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type UpdateVoxel
     */
    public static TerrainMessage constructUpdateVoxelMessage(int worldX,int worldY,int worldZ,int voxelX,int voxelY,int voxelZ,float terrainWeight,int terrainValue){
        TerrainMessage rVal = new TerrainMessage(TerrainMessageType.UPDATEVOXEL);
        rVal.setworldX(worldX);
        rVal.setworldY(worldY);
        rVal.setworldZ(worldZ);
        rVal.setvoxelX(voxelX);
        rVal.setvoxelY(voxelY);
        rVal.setvoxelZ(voxelZ);
        rVal.setterrainWeight(terrainWeight);
        rVal.setterrainValue(terrainValue);
        return rVal;
    }

    /**
     * Parses a message of type RequestUseTerrainPalette
     */
    public static TerrainMessage parseRequestUseTerrainPaletteMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 36){
            return null;
        }
        TerrainMessage rVal = (TerrainMessage)pool.get(MessageType.TERRAIN_MESSAGE);
        rVal.messageType = TerrainMessageType.REQUESTUSETERRAINPALETTE;
        rVal.setrealLocationX(byteBuffer.getDouble());
        rVal.setrealLocationY(byteBuffer.getDouble());
        rVal.setrealLocationZ(byteBuffer.getDouble());
        rVal.setvalue(byteBuffer.getFloat());
        rVal.setterrainWeight(byteBuffer.getFloat());
        rVal.setterrainValue(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type RequestUseTerrainPalette
     */
    public static TerrainMessage constructRequestUseTerrainPaletteMessage(double realLocationX,double realLocationY,double realLocationZ,float value,float terrainWeight,int terrainValue){
        TerrainMessage rVal = new TerrainMessage(TerrainMessageType.REQUESTUSETERRAINPALETTE);
        rVal.setrealLocationX(realLocationX);
        rVal.setrealLocationY(realLocationY);
        rVal.setrealLocationZ(realLocationZ);
        rVal.setvalue(value);
        rVal.setterrainWeight(terrainWeight);
        rVal.setterrainValue(terrainValue);
        return rVal;
    }

    /**
     * Parses a message of type RequestDestroyTerrain
     */
    public static TerrainMessage parseRequestDestroyTerrainMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 32){
            return null;
        }
        TerrainMessage rVal = (TerrainMessage)pool.get(MessageType.TERRAIN_MESSAGE);
        rVal.messageType = TerrainMessageType.REQUESTDESTROYTERRAIN;
        rVal.setrealLocationX(byteBuffer.getDouble());
        rVal.setrealLocationY(byteBuffer.getDouble());
        rVal.setrealLocationZ(byteBuffer.getDouble());
        rVal.setvalue(byteBuffer.getFloat());
        rVal.setterrainWeight(byteBuffer.getFloat());
        return rVal;
    }

    /**
     * Constructs a message of type RequestDestroyTerrain
     */
    public static TerrainMessage constructRequestDestroyTerrainMessage(double realLocationX,double realLocationY,double realLocationZ,float value,float terrainWeight){
        TerrainMessage rVal = new TerrainMessage(TerrainMessageType.REQUESTDESTROYTERRAIN);
        rVal.setrealLocationX(realLocationX);
        rVal.setrealLocationY(realLocationY);
        rVal.setrealLocationZ(realLocationZ);
        rVal.setvalue(value);
        rVal.setterrainWeight(terrainWeight);
        return rVal;
    }

    /**
     * Parses a message of type SpawnPosition
     */
    public static TerrainMessage parseSpawnPositionMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 24){
            return null;
        }
        TerrainMessage rVal = (TerrainMessage)pool.get(MessageType.TERRAIN_MESSAGE);
        rVal.messageType = TerrainMessageType.SPAWNPOSITION;
        rVal.setrealLocationX(byteBuffer.getDouble());
        rVal.setrealLocationY(byteBuffer.getDouble());
        rVal.setrealLocationZ(byteBuffer.getDouble());
        return rVal;
    }

    /**
     * Constructs a message of type SpawnPosition
     */
    public static TerrainMessage constructSpawnPositionMessage(double realLocationX,double realLocationY,double realLocationZ){
        TerrainMessage rVal = new TerrainMessage(TerrainMessageType.SPAWNPOSITION);
        rVal.setrealLocationX(realLocationX);
        rVal.setrealLocationY(realLocationY);
        rVal.setrealLocationZ(realLocationZ);
        return rVal;
    }

    /**
     * Parses a message of type RequestChunkData
     */
    public static TerrainMessage parseRequestChunkDataMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 12){
            return null;
        }
        TerrainMessage rVal = (TerrainMessage)pool.get(MessageType.TERRAIN_MESSAGE);
        rVal.messageType = TerrainMessageType.REQUESTCHUNKDATA;
        rVal.setworldX(byteBuffer.getInt());
        rVal.setworldY(byteBuffer.getInt());
        rVal.setworldZ(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type RequestChunkData
     */
    public static TerrainMessage constructRequestChunkDataMessage(int worldX,int worldY,int worldZ){
        TerrainMessage rVal = new TerrainMessage(TerrainMessageType.REQUESTCHUNKDATA);
        rVal.setworldX(worldX);
        rVal.setworldY(worldY);
        rVal.setworldZ(worldZ);
        return rVal;
    }

    /**
     * Parses a message of type sendChunkData
     */
    public static TerrainMessage parsesendChunkDataMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 16){
            return null;
        }
        int lenAccumulator = 0;
        int chunkDatalen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + chunkDatalen;
        if(byteBuffer.remaining() < 16 + lenAccumulator){
            return null;
        }
        TerrainMessage rVal = (TerrainMessage)pool.get(MessageType.TERRAIN_MESSAGE);
        rVal.messageType = TerrainMessageType.SENDCHUNKDATA;
        rVal.setworldX(byteBuffer.getInt());
        rVal.setworldY(byteBuffer.getInt());
        rVal.setworldZ(byteBuffer.getInt());
        if(chunkDatalen > 0){
            rVal.setchunkData(ByteStreamUtils.popByteArrayFromByteBuffer(byteBuffer, chunkDatalen));
        }
        return rVal;
    }

    /**
     * Constructs a message of type sendChunkData
     */
    public static TerrainMessage constructsendChunkDataMessage(int worldX,int worldY,int worldZ,byte[] chunkData){
        TerrainMessage rVal = new TerrainMessage(TerrainMessageType.SENDCHUNKDATA);
        rVal.setworldX(worldX);
        rVal.setworldY(worldY);
        rVal.setworldZ(worldZ);
        rVal.setchunkData(chunkData);
        return rVal;
    }

    /**
     * Parses a message of type RequestReducedChunkData
     */
    public static TerrainMessage parseRequestReducedChunkDataMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 16){
            return null;
        }
        TerrainMessage rVal = (TerrainMessage)pool.get(MessageType.TERRAIN_MESSAGE);
        rVal.messageType = TerrainMessageType.REQUESTREDUCEDCHUNKDATA;
        rVal.setworldX(byteBuffer.getInt());
        rVal.setworldY(byteBuffer.getInt());
        rVal.setworldZ(byteBuffer.getInt());
        rVal.setchunkResolution(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type RequestReducedChunkData
     */
    public static TerrainMessage constructRequestReducedChunkDataMessage(int worldX,int worldY,int worldZ,int chunkResolution){
        TerrainMessage rVal = new TerrainMessage(TerrainMessageType.REQUESTREDUCEDCHUNKDATA);
        rVal.setworldX(worldX);
        rVal.setworldY(worldY);
        rVal.setworldZ(worldZ);
        rVal.setchunkResolution(chunkResolution);
        return rVal;
    }

    /**
     * Parses a message of type SendReducedChunkData
     */
    public static TerrainMessage parseSendReducedChunkDataMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 24){
            return null;
        }
        int lenAccumulator = 0;
        int chunkDatalen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + chunkDatalen;
        if(byteBuffer.remaining() < 24 + lenAccumulator){
            return null;
        }
        TerrainMessage rVal = (TerrainMessage)pool.get(MessageType.TERRAIN_MESSAGE);
        rVal.messageType = TerrainMessageType.SENDREDUCEDCHUNKDATA;
        short pair = (short)((TypeBytes.MESSAGE_TYPE_TERRAIN << 4) | TypeBytes.TERRAIN_MESSAGE_TYPE_SENDREDUCEDCHUNKDATA);
        BiConsumer<NetworkMessage,ByteBuffer> customParser = customParserMap.get(pair);
        if(customParser == null){
            throw new Error("Custom parser undefined for message pair!");
        }
        customParser.accept(rVal,byteBuffer);
        return rVal;
    }

    /**
     * Constructs a message of type SendReducedChunkData
     */
    public static TerrainMessage constructSendReducedChunkDataMessage(int worldX,int worldY,int worldZ,int chunkResolution,int homogenousValue,byte[] chunkData){
        TerrainMessage rVal = new TerrainMessage(TerrainMessageType.SENDREDUCEDCHUNKDATA);
        rVal.setworldX(worldX);
        rVal.setworldY(worldY);
        rVal.setworldZ(worldZ);
        rVal.setchunkResolution(chunkResolution);
        rVal.sethomogenousValue(homogenousValue);
        rVal.setchunkData(chunkData);
        return rVal;
    }

    /**
     * Parses a message of type RequestReducedBlockData
     */
    public static TerrainMessage parseRequestReducedBlockDataMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 16){
            return null;
        }
        TerrainMessage rVal = (TerrainMessage)pool.get(MessageType.TERRAIN_MESSAGE);
        rVal.messageType = TerrainMessageType.REQUESTREDUCEDBLOCKDATA;
        rVal.setworldX(byteBuffer.getInt());
        rVal.setworldY(byteBuffer.getInt());
        rVal.setworldZ(byteBuffer.getInt());
        rVal.setchunkResolution(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type RequestReducedBlockData
     */
    public static TerrainMessage constructRequestReducedBlockDataMessage(int worldX,int worldY,int worldZ,int chunkResolution){
        TerrainMessage rVal = new TerrainMessage(TerrainMessageType.REQUESTREDUCEDBLOCKDATA);
        rVal.setworldX(worldX);
        rVal.setworldY(worldY);
        rVal.setworldZ(worldZ);
        rVal.setchunkResolution(chunkResolution);
        return rVal;
    }

    /**
     * Parses a message of type SendReducedBlockData
     */
    public static TerrainMessage parseSendReducedBlockDataMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 24){
            return null;
        }
        int lenAccumulator = 0;
        int chunkDatalen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + chunkDatalen;
        if(byteBuffer.remaining() < 24 + lenAccumulator){
            return null;
        }
        TerrainMessage rVal = (TerrainMessage)pool.get(MessageType.TERRAIN_MESSAGE);
        rVal.messageType = TerrainMessageType.SENDREDUCEDBLOCKDATA;
        short pair = (short)((TypeBytes.MESSAGE_TYPE_TERRAIN << 4) | TypeBytes.TERRAIN_MESSAGE_TYPE_SENDREDUCEDBLOCKDATA);
        BiConsumer<NetworkMessage,ByteBuffer> customParser = customParserMap.get(pair);
        if(customParser == null){
            throw new Error("Custom parser undefined for message pair!");
        }
        customParser.accept(rVal,byteBuffer);
        return rVal;
    }

    /**
     * Constructs a message of type SendReducedBlockData
     */
    public static TerrainMessage constructSendReducedBlockDataMessage(int worldX,int worldY,int worldZ,int chunkResolution,int homogenousValue,byte[] chunkData){
        TerrainMessage rVal = new TerrainMessage(TerrainMessageType.SENDREDUCEDBLOCKDATA);
        rVal.setworldX(worldX);
        rVal.setworldY(worldY);
        rVal.setworldZ(worldZ);
        rVal.setchunkResolution(chunkResolution);
        rVal.sethomogenousValue(homogenousValue);
        rVal.setchunkData(chunkData);
        return rVal;
    }

    /**
     * Parses a message of type UpdateBlock
     */
    public static TerrainMessage parseUpdateBlockMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 32){
            return null;
        }
        TerrainMessage rVal = (TerrainMessage)pool.get(MessageType.TERRAIN_MESSAGE);
        rVal.messageType = TerrainMessageType.UPDATEBLOCK;
        rVal.setworldX(byteBuffer.getInt());
        rVal.setworldY(byteBuffer.getInt());
        rVal.setworldZ(byteBuffer.getInt());
        rVal.setvoxelX(byteBuffer.getInt());
        rVal.setvoxelY(byteBuffer.getInt());
        rVal.setvoxelZ(byteBuffer.getInt());
        rVal.setblockType(byteBuffer.getInt());
        rVal.setblockMetadata(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type UpdateBlock
     */
    public static TerrainMessage constructUpdateBlockMessage(int worldX,int worldY,int worldZ,int voxelX,int voxelY,int voxelZ,int blockType,int blockMetadata){
        TerrainMessage rVal = new TerrainMessage(TerrainMessageType.UPDATEBLOCK);
        rVal.setworldX(worldX);
        rVal.setworldY(worldY);
        rVal.setworldZ(worldZ);
        rVal.setvoxelX(voxelX);
        rVal.setvoxelY(voxelY);
        rVal.setvoxelZ(voxelZ);
        rVal.setblockType(blockType);
        rVal.setblockMetadata(blockMetadata);
        return rVal;
    }

    /**
     * Parses a message of type RequestFluidData
     */
    public static TerrainMessage parseRequestFluidDataMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 12){
            return null;
        }
        TerrainMessage rVal = (TerrainMessage)pool.get(MessageType.TERRAIN_MESSAGE);
        rVal.messageType = TerrainMessageType.REQUESTFLUIDDATA;
        rVal.setworldX(byteBuffer.getInt());
        rVal.setworldY(byteBuffer.getInt());
        rVal.setworldZ(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type RequestFluidData
     */
    public static TerrainMessage constructRequestFluidDataMessage(int worldX,int worldY,int worldZ){
        TerrainMessage rVal = new TerrainMessage(TerrainMessageType.REQUESTFLUIDDATA);
        rVal.setworldX(worldX);
        rVal.setworldY(worldY);
        rVal.setworldZ(worldZ);
        return rVal;
    }

    /**
     * Parses a message of type sendFluidData
     */
    public static TerrainMessage parsesendFluidDataMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 16){
            return null;
        }
        int lenAccumulator = 0;
        int chunkDatalen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + chunkDatalen;
        if(byteBuffer.remaining() < 16 + lenAccumulator){
            return null;
        }
        TerrainMessage rVal = (TerrainMessage)pool.get(MessageType.TERRAIN_MESSAGE);
        rVal.messageType = TerrainMessageType.SENDFLUIDDATA;
        rVal.setworldX(byteBuffer.getInt());
        rVal.setworldY(byteBuffer.getInt());
        rVal.setworldZ(byteBuffer.getInt());
        if(chunkDatalen > 0){
            rVal.setchunkData(ByteStreamUtils.popByteArrayFromByteBuffer(byteBuffer, chunkDatalen));
        }
        return rVal;
    }

    /**
     * Constructs a message of type sendFluidData
     */
    public static TerrainMessage constructsendFluidDataMessage(int worldX,int worldY,int worldZ,byte[] chunkData){
        TerrainMessage rVal = new TerrainMessage(TerrainMessageType.SENDFLUIDDATA);
        rVal.setworldX(worldX);
        rVal.setworldY(worldY);
        rVal.setworldZ(worldZ);
        rVal.setchunkData(chunkData);
        return rVal;
    }

    /**
     * Parses a message of type updateFluidData
     */
    public static TerrainMessage parseupdateFluidDataMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 16){
            return null;
        }
        int lenAccumulator = 0;
        int chunkDatalen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + chunkDatalen;
        if(byteBuffer.remaining() < 16 + lenAccumulator){
            return null;
        }
        TerrainMessage rVal = (TerrainMessage)pool.get(MessageType.TERRAIN_MESSAGE);
        rVal.messageType = TerrainMessageType.UPDATEFLUIDDATA;
        rVal.setworldX(byteBuffer.getInt());
        rVal.setworldY(byteBuffer.getInt());
        rVal.setworldZ(byteBuffer.getInt());
        if(chunkDatalen > 0){
            rVal.setchunkData(ByteStreamUtils.popByteArrayFromByteBuffer(byteBuffer, chunkDatalen));
        }
        return rVal;
    }

    /**
     * Constructs a message of type updateFluidData
     */
    public static TerrainMessage constructupdateFluidDataMessage(int worldX,int worldY,int worldZ,byte[] chunkData){
        TerrainMessage rVal = new TerrainMessage(TerrainMessageType.UPDATEFLUIDDATA);
        rVal.setworldX(worldX);
        rVal.setworldY(worldY);
        rVal.setworldZ(worldZ);
        rVal.setchunkData(chunkData);
        return rVal;
    }

    /**
     * Parses a message of type RequestEditBlock
     */
    public static TerrainMessage parseRequestEditBlockMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 36){
            return null;
        }
        TerrainMessage rVal = (TerrainMessage)pool.get(MessageType.TERRAIN_MESSAGE);
        rVal.messageType = TerrainMessageType.REQUESTEDITBLOCK;
        rVal.setworldX(byteBuffer.getInt());
        rVal.setworldY(byteBuffer.getInt());
        rVal.setworldZ(byteBuffer.getInt());
        rVal.setvoxelX(byteBuffer.getInt());
        rVal.setvoxelY(byteBuffer.getInt());
        rVal.setvoxelZ(byteBuffer.getInt());
        rVal.setblockType(byteBuffer.getInt());
        rVal.setblockMetadata(byteBuffer.getInt());
        rVal.setblockEditSize(byteBuffer.getInt());
        return rVal;
    }

    /**
     * Constructs a message of type RequestEditBlock
     */
    public static TerrainMessage constructRequestEditBlockMessage(int worldX,int worldY,int worldZ,int voxelX,int voxelY,int voxelZ,int blockType,int blockMetadata,int blockEditSize){
        TerrainMessage rVal = new TerrainMessage(TerrainMessageType.REQUESTEDITBLOCK);
        rVal.setworldX(worldX);
        rVal.setworldY(worldY);
        rVal.setworldZ(worldZ);
        rVal.setvoxelX(voxelX);
        rVal.setvoxelY(voxelY);
        rVal.setvoxelZ(voxelZ);
        rVal.setblockType(blockType);
        rVal.setblockMetadata(blockMetadata);
        rVal.setblockEditSize(blockEditSize);
        return rVal;
    }

    /**
     * Parses a message of type RequestPlaceFab
     */
    public static TerrainMessage parseRequestPlaceFabMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 32){
            return null;
        }
        int lenAccumulator = 0;
        int fabPathlen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + fabPathlen;
        if(byteBuffer.remaining() < 32 + lenAccumulator){
            return null;
        }
        TerrainMessage rVal = (TerrainMessage)pool.get(MessageType.TERRAIN_MESSAGE);
        rVal.messageType = TerrainMessageType.REQUESTPLACEFAB;
        rVal.setworldX(byteBuffer.getInt());
        rVal.setworldY(byteBuffer.getInt());
        rVal.setworldZ(byteBuffer.getInt());
        rVal.setvoxelX(byteBuffer.getInt());
        rVal.setvoxelY(byteBuffer.getInt());
        rVal.setvoxelZ(byteBuffer.getInt());
        rVal.setblockRotation(byteBuffer.getInt());
        if(fabPathlen > 0){
            rVal.setfabPath(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, fabPathlen));
        }
        return rVal;
    }

    /**
     * Constructs a message of type RequestPlaceFab
     */
    public static TerrainMessage constructRequestPlaceFabMessage(int worldX,int worldY,int worldZ,int voxelX,int voxelY,int voxelZ,int blockRotation,String fabPath){
        TerrainMessage rVal = new TerrainMessage(TerrainMessageType.REQUESTPLACEFAB);
        rVal.setworldX(worldX);
        rVal.setworldY(worldY);
        rVal.setworldZ(worldZ);
        rVal.setvoxelX(voxelX);
        rVal.setvoxelY(voxelY);
        rVal.setvoxelZ(voxelZ);
        rVal.setblockRotation(blockRotation);
        rVal.setfabPath(fabPath);
        return rVal;
    }

    @Deprecated
    @Override
    void serialize(){
        byte[] intValues = new byte[8];
        byte[] stringBytes;
        switch(this.messageType){
            case REQUESTMETADATA:
                rawBytes = new byte[2];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_TERRAIN;
                //entity messaage header
                rawBytes[1] = TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTMETADATA;
                break;
            case RESPONSEMETADATA:
                rawBytes = new byte[2+4+4+4+4+4+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_TERRAIN;
                //entity messaage header
                rawBytes[1] = TypeBytes.TERRAIN_MESSAGE_TYPE_RESPONSEMETADATA;
                intValues = ByteStreamUtils.serializeIntToBytes(worldSizeDiscrete);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldMinX);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldMinY);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldMinZ);
                for(int i = 0; i < 4; i++){
                    rawBytes[14+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldMaxX);
                for(int i = 0; i < 4; i++){
                    rawBytes[18+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldMaxY);
                for(int i = 0; i < 4; i++){
                    rawBytes[22+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldMaxZ);
                for(int i = 0; i < 4; i++){
                    rawBytes[26+i] = intValues[i];
                }
                break;
            case REQUESTEDITVOXEL:
                rawBytes = new byte[2+4+4+4+4+4+4+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_TERRAIN;
                //entity messaage header
                rawBytes[1] = TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTEDITVOXEL;
                intValues = ByteStreamUtils.serializeIntToBytes(worldX);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldY);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldZ);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(voxelX);
                for(int i = 0; i < 4; i++){
                    rawBytes[14+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(voxelY);
                for(int i = 0; i < 4; i++){
                    rawBytes[18+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(voxelZ);
                for(int i = 0; i < 4; i++){
                    rawBytes[22+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeFloatToBytes(terrainWeight);
                for(int i = 0; i < 4; i++){
                    rawBytes[26+i] = intValues[i];
                }                intValues = ByteStreamUtils.serializeIntToBytes(terrainValue);
                for(int i = 0; i < 4; i++){
                    rawBytes[30+i] = intValues[i];
                }
                break;
            case UPDATEVOXEL:
                rawBytes = new byte[2+4+4+4+4+4+4+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_TERRAIN;
                //entity messaage header
                rawBytes[1] = TypeBytes.TERRAIN_MESSAGE_TYPE_UPDATEVOXEL;
                intValues = ByteStreamUtils.serializeIntToBytes(worldX);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldY);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldZ);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(voxelX);
                for(int i = 0; i < 4; i++){
                    rawBytes[14+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(voxelY);
                for(int i = 0; i < 4; i++){
                    rawBytes[18+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(voxelZ);
                for(int i = 0; i < 4; i++){
                    rawBytes[22+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeFloatToBytes(terrainWeight);
                for(int i = 0; i < 4; i++){
                    rawBytes[26+i] = intValues[i];
                }                intValues = ByteStreamUtils.serializeIntToBytes(terrainValue);
                for(int i = 0; i < 4; i++){
                    rawBytes[30+i] = intValues[i];
                }
                break;
            case REQUESTUSETERRAINPALETTE:
                rawBytes = new byte[2+8+8+8+4+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_TERRAIN;
                //entity messaage header
                rawBytes[1] = TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTUSETERRAINPALETTE;
                intValues = ByteStreamUtils.serializeDoubleToBytes(realLocationX);
                for(int i = 0; i < 8; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(realLocationY);
                for(int i = 0; i < 8; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(realLocationZ);
                for(int i = 0; i < 8; i++){
                    rawBytes[18+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeFloatToBytes(value);
                for(int i = 0; i < 4; i++){
                    rawBytes[26+i] = intValues[i];
                }                intValues = ByteStreamUtils.serializeFloatToBytes(terrainWeight);
                for(int i = 0; i < 4; i++){
                    rawBytes[30+i] = intValues[i];
                }                intValues = ByteStreamUtils.serializeIntToBytes(terrainValue);
                for(int i = 0; i < 4; i++){
                    rawBytes[34+i] = intValues[i];
                }
                break;
            case REQUESTDESTROYTERRAIN:
                rawBytes = new byte[2+8+8+8+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_TERRAIN;
                //entity messaage header
                rawBytes[1] = TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTDESTROYTERRAIN;
                intValues = ByteStreamUtils.serializeDoubleToBytes(realLocationX);
                for(int i = 0; i < 8; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(realLocationY);
                for(int i = 0; i < 8; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(realLocationZ);
                for(int i = 0; i < 8; i++){
                    rawBytes[18+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeFloatToBytes(value);
                for(int i = 0; i < 4; i++){
                    rawBytes[26+i] = intValues[i];
                }                intValues = ByteStreamUtils.serializeFloatToBytes(terrainWeight);
                for(int i = 0; i < 4; i++){
                    rawBytes[30+i] = intValues[i];
                }                break;
            case SPAWNPOSITION:
                rawBytes = new byte[2+8+8+8];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_TERRAIN;
                //entity messaage header
                rawBytes[1] = TypeBytes.TERRAIN_MESSAGE_TYPE_SPAWNPOSITION;
                intValues = ByteStreamUtils.serializeDoubleToBytes(realLocationX);
                for(int i = 0; i < 8; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(realLocationY);
                for(int i = 0; i < 8; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeDoubleToBytes(realLocationZ);
                for(int i = 0; i < 8; i++){
                    rawBytes[18+i] = intValues[i];
                }
                break;
            case REQUESTCHUNKDATA:
                rawBytes = new byte[2+4+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_TERRAIN;
                //entity messaage header
                rawBytes[1] = TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTCHUNKDATA;
                intValues = ByteStreamUtils.serializeIntToBytes(worldX);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldY);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldZ);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                break;
            case SENDCHUNKDATA:
                rawBytes = new byte[2+4+4+4+4+chunkData.length];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_TERRAIN;
                //entity messaage header
                rawBytes[1] = TypeBytes.TERRAIN_MESSAGE_TYPE_SENDCHUNKDATA;
                intValues = ByteStreamUtils.serializeIntToBytes(worldX);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldY);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldZ);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(chunkData.length);
                for(int i = 0; i < 4; i++){
                    rawBytes[14+i] = intValues[i];
                }
                for(int i = 0; i < chunkData.length; i++){
                    rawBytes[18+i] = chunkData[i];
                }
                break;
            case REQUESTREDUCEDCHUNKDATA:
                rawBytes = new byte[2+4+4+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_TERRAIN;
                //entity messaage header
                rawBytes[1] = TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTREDUCEDCHUNKDATA;
                intValues = ByteStreamUtils.serializeIntToBytes(worldX);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldY);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldZ);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(chunkResolution);
                for(int i = 0; i < 4; i++){
                    rawBytes[14+i] = intValues[i];
                }
                break;
            case SENDREDUCEDCHUNKDATA:
                rawBytes = new byte[2+4+4+4+4+4+4+chunkData.length];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_TERRAIN;
                //entity messaage header
                rawBytes[1] = TypeBytes.TERRAIN_MESSAGE_TYPE_SENDREDUCEDCHUNKDATA;
                intValues = ByteStreamUtils.serializeIntToBytes(worldX);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldY);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldZ);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(chunkResolution);
                for(int i = 0; i < 4; i++){
                    rawBytes[14+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(homogenousValue);
                for(int i = 0; i < 4; i++){
                    rawBytes[18+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(chunkData.length);
                for(int i = 0; i < 4; i++){
                    rawBytes[22+i] = intValues[i];
                }
                for(int i = 0; i < chunkData.length; i++){
                    rawBytes[26+i] = chunkData[i];
                }
                break;
            case REQUESTREDUCEDBLOCKDATA:
                rawBytes = new byte[2+4+4+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_TERRAIN;
                //entity messaage header
                rawBytes[1] = TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTREDUCEDBLOCKDATA;
                intValues = ByteStreamUtils.serializeIntToBytes(worldX);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldY);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldZ);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(chunkResolution);
                for(int i = 0; i < 4; i++){
                    rawBytes[14+i] = intValues[i];
                }
                break;
            case SENDREDUCEDBLOCKDATA:
                rawBytes = new byte[2+4+4+4+4+4+4+chunkData.length];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_TERRAIN;
                //entity messaage header
                rawBytes[1] = TypeBytes.TERRAIN_MESSAGE_TYPE_SENDREDUCEDBLOCKDATA;
                intValues = ByteStreamUtils.serializeIntToBytes(worldX);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldY);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldZ);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(chunkResolution);
                for(int i = 0; i < 4; i++){
                    rawBytes[14+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(homogenousValue);
                for(int i = 0; i < 4; i++){
                    rawBytes[18+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(chunkData.length);
                for(int i = 0; i < 4; i++){
                    rawBytes[22+i] = intValues[i];
                }
                for(int i = 0; i < chunkData.length; i++){
                    rawBytes[26+i] = chunkData[i];
                }
                break;
            case UPDATEBLOCK:
                rawBytes = new byte[2+4+4+4+4+4+4+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_TERRAIN;
                //entity messaage header
                rawBytes[1] = TypeBytes.TERRAIN_MESSAGE_TYPE_UPDATEBLOCK;
                intValues = ByteStreamUtils.serializeIntToBytes(worldX);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldY);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldZ);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(voxelX);
                for(int i = 0; i < 4; i++){
                    rawBytes[14+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(voxelY);
                for(int i = 0; i < 4; i++){
                    rawBytes[18+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(voxelZ);
                for(int i = 0; i < 4; i++){
                    rawBytes[22+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(blockType);
                for(int i = 0; i < 4; i++){
                    rawBytes[26+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(blockMetadata);
                for(int i = 0; i < 4; i++){
                    rawBytes[30+i] = intValues[i];
                }
                break;
            case REQUESTFLUIDDATA:
                rawBytes = new byte[2+4+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_TERRAIN;
                //entity messaage header
                rawBytes[1] = TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTFLUIDDATA;
                intValues = ByteStreamUtils.serializeIntToBytes(worldX);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldY);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldZ);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                break;
            case SENDFLUIDDATA:
                rawBytes = new byte[2+4+4+4+4+chunkData.length];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_TERRAIN;
                //entity messaage header
                rawBytes[1] = TypeBytes.TERRAIN_MESSAGE_TYPE_SENDFLUIDDATA;
                intValues = ByteStreamUtils.serializeIntToBytes(worldX);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldY);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldZ);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(chunkData.length);
                for(int i = 0; i < 4; i++){
                    rawBytes[14+i] = intValues[i];
                }
                for(int i = 0; i < chunkData.length; i++){
                    rawBytes[18+i] = chunkData[i];
                }
                break;
            case UPDATEFLUIDDATA:
                rawBytes = new byte[2+4+4+4+4+chunkData.length];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_TERRAIN;
                //entity messaage header
                rawBytes[1] = TypeBytes.TERRAIN_MESSAGE_TYPE_UPDATEFLUIDDATA;
                intValues = ByteStreamUtils.serializeIntToBytes(worldX);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldY);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldZ);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(chunkData.length);
                for(int i = 0; i < 4; i++){
                    rawBytes[14+i] = intValues[i];
                }
                for(int i = 0; i < chunkData.length; i++){
                    rawBytes[18+i] = chunkData[i];
                }
                break;
            case REQUESTEDITBLOCK:
                rawBytes = new byte[2+4+4+4+4+4+4+4+4+4];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_TERRAIN;
                //entity messaage header
                rawBytes[1] = TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTEDITBLOCK;
                intValues = ByteStreamUtils.serializeIntToBytes(worldX);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldY);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldZ);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(voxelX);
                for(int i = 0; i < 4; i++){
                    rawBytes[14+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(voxelY);
                for(int i = 0; i < 4; i++){
                    rawBytes[18+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(voxelZ);
                for(int i = 0; i < 4; i++){
                    rawBytes[22+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(blockType);
                for(int i = 0; i < 4; i++){
                    rawBytes[26+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(blockMetadata);
                for(int i = 0; i < 4; i++){
                    rawBytes[30+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(blockEditSize);
                for(int i = 0; i < 4; i++){
                    rawBytes[34+i] = intValues[i];
                }
                break;
            case REQUESTPLACEFAB:
                rawBytes = new byte[2+4+4+4+4+4+4+4+4+fabPath.length()];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_TERRAIN;
                //entity messaage header
                rawBytes[1] = TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTPLACEFAB;
                intValues = ByteStreamUtils.serializeIntToBytes(worldX);
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldY);
                for(int i = 0; i < 4; i++){
                    rawBytes[6+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(worldZ);
                for(int i = 0; i < 4; i++){
                    rawBytes[10+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(voxelX);
                for(int i = 0; i < 4; i++){
                    rawBytes[14+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(voxelY);
                for(int i = 0; i < 4; i++){
                    rawBytes[18+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(voxelZ);
                for(int i = 0; i < 4; i++){
                    rawBytes[22+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(blockRotation);
                for(int i = 0; i < 4; i++){
                    rawBytes[26+i] = intValues[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(fabPath.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[30+i] = intValues[i];
                }
                stringBytes = fabPath.getBytes();
                for(int i = 0; i < fabPath.length(); i++){
                    rawBytes[34+i] = stringBytes[i];
                }
                break;
        }
        serialized = true;
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        switch(this.messageType){
            case REQUESTMETADATA: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_TERRAIN);
                stream.write(TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTMETADATA);
                
                //
                //Write body of packet
            } break;
            case RESPONSEMETADATA: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_TERRAIN);
                stream.write(TypeBytes.TERRAIN_MESSAGE_TYPE_RESPONSEMETADATA);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, worldSizeDiscrete);
                ByteStreamUtils.writeInt(stream, worldMinX);
                ByteStreamUtils.writeInt(stream, worldMinY);
                ByteStreamUtils.writeInt(stream, worldMinZ);
                ByteStreamUtils.writeInt(stream, worldMaxX);
                ByteStreamUtils.writeInt(stream, worldMaxY);
                ByteStreamUtils.writeInt(stream, worldMaxZ);
            } break;
            case REQUESTEDITVOXEL: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_TERRAIN);
                stream.write(TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTEDITVOXEL);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, worldX);
                ByteStreamUtils.writeInt(stream, worldY);
                ByteStreamUtils.writeInt(stream, worldZ);
                ByteStreamUtils.writeInt(stream, voxelX);
                ByteStreamUtils.writeInt(stream, voxelY);
                ByteStreamUtils.writeInt(stream, voxelZ);
                ByteStreamUtils.writeFloat(stream, terrainWeight);
                ByteStreamUtils.writeInt(stream, terrainValue);
            } break;
            case UPDATEVOXEL: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_TERRAIN);
                stream.write(TypeBytes.TERRAIN_MESSAGE_TYPE_UPDATEVOXEL);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, worldX);
                ByteStreamUtils.writeInt(stream, worldY);
                ByteStreamUtils.writeInt(stream, worldZ);
                ByteStreamUtils.writeInt(stream, voxelX);
                ByteStreamUtils.writeInt(stream, voxelY);
                ByteStreamUtils.writeInt(stream, voxelZ);
                ByteStreamUtils.writeFloat(stream, terrainWeight);
                ByteStreamUtils.writeInt(stream, terrainValue);
            } break;
            case REQUESTUSETERRAINPALETTE: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_TERRAIN);
                stream.write(TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTUSETERRAINPALETTE);
                
                //
                //Write body of packet
                ByteStreamUtils.writeDouble(stream, realLocationX);
                ByteStreamUtils.writeDouble(stream, realLocationY);
                ByteStreamUtils.writeDouble(stream, realLocationZ);
                ByteStreamUtils.writeFloat(stream, value);
                ByteStreamUtils.writeFloat(stream, terrainWeight);
                ByteStreamUtils.writeInt(stream, terrainValue);
            } break;
            case REQUESTDESTROYTERRAIN: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_TERRAIN);
                stream.write(TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTDESTROYTERRAIN);
                
                //
                //Write body of packet
                ByteStreamUtils.writeDouble(stream, realLocationX);
                ByteStreamUtils.writeDouble(stream, realLocationY);
                ByteStreamUtils.writeDouble(stream, realLocationZ);
                ByteStreamUtils.writeFloat(stream, value);
                ByteStreamUtils.writeFloat(stream, terrainWeight);
            } break;
            case SPAWNPOSITION: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_TERRAIN);
                stream.write(TypeBytes.TERRAIN_MESSAGE_TYPE_SPAWNPOSITION);
                
                //
                //Write body of packet
                ByteStreamUtils.writeDouble(stream, realLocationX);
                ByteStreamUtils.writeDouble(stream, realLocationY);
                ByteStreamUtils.writeDouble(stream, realLocationZ);
            } break;
            case REQUESTCHUNKDATA: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_TERRAIN);
                stream.write(TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTCHUNKDATA);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, worldX);
                ByteStreamUtils.writeInt(stream, worldY);
                ByteStreamUtils.writeInt(stream, worldZ);
            } break;
            case SENDCHUNKDATA: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_TERRAIN);
                stream.write(TypeBytes.TERRAIN_MESSAGE_TYPE_SENDCHUNKDATA);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, chunkData.length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, worldX);
                ByteStreamUtils.writeInt(stream, worldY);
                ByteStreamUtils.writeInt(stream, worldZ);
                stream.write(chunkData);
            } break;
            case REQUESTREDUCEDCHUNKDATA: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_TERRAIN);
                stream.write(TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTREDUCEDCHUNKDATA);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, worldX);
                ByteStreamUtils.writeInt(stream, worldY);
                ByteStreamUtils.writeInt(stream, worldZ);
                ByteStreamUtils.writeInt(stream, chunkResolution);
            } break;
            case SENDREDUCEDCHUNKDATA: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_TERRAIN);
                stream.write(TypeBytes.TERRAIN_MESSAGE_TYPE_SENDREDUCEDCHUNKDATA);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, chunkData.length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, worldX);
                ByteStreamUtils.writeInt(stream, worldY);
                ByteStreamUtils.writeInt(stream, worldZ);
                ByteStreamUtils.writeInt(stream, chunkResolution);
                ByteStreamUtils.writeInt(stream, homogenousValue);
                stream.write(chunkData);
            } break;
            case REQUESTREDUCEDBLOCKDATA: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_TERRAIN);
                stream.write(TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTREDUCEDBLOCKDATA);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, worldX);
                ByteStreamUtils.writeInt(stream, worldY);
                ByteStreamUtils.writeInt(stream, worldZ);
                ByteStreamUtils.writeInt(stream, chunkResolution);
            } break;
            case SENDREDUCEDBLOCKDATA: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_TERRAIN);
                stream.write(TypeBytes.TERRAIN_MESSAGE_TYPE_SENDREDUCEDBLOCKDATA);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, chunkData.length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, worldX);
                ByteStreamUtils.writeInt(stream, worldY);
                ByteStreamUtils.writeInt(stream, worldZ);
                ByteStreamUtils.writeInt(stream, chunkResolution);
                ByteStreamUtils.writeInt(stream, homogenousValue);
                stream.write(chunkData);
            } break;
            case UPDATEBLOCK: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_TERRAIN);
                stream.write(TypeBytes.TERRAIN_MESSAGE_TYPE_UPDATEBLOCK);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, worldX);
                ByteStreamUtils.writeInt(stream, worldY);
                ByteStreamUtils.writeInt(stream, worldZ);
                ByteStreamUtils.writeInt(stream, voxelX);
                ByteStreamUtils.writeInt(stream, voxelY);
                ByteStreamUtils.writeInt(stream, voxelZ);
                ByteStreamUtils.writeInt(stream, blockType);
                ByteStreamUtils.writeInt(stream, blockMetadata);
            } break;
            case REQUESTFLUIDDATA: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_TERRAIN);
                stream.write(TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTFLUIDDATA);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, worldX);
                ByteStreamUtils.writeInt(stream, worldY);
                ByteStreamUtils.writeInt(stream, worldZ);
            } break;
            case SENDFLUIDDATA: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_TERRAIN);
                stream.write(TypeBytes.TERRAIN_MESSAGE_TYPE_SENDFLUIDDATA);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, chunkData.length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, worldX);
                ByteStreamUtils.writeInt(stream, worldY);
                ByteStreamUtils.writeInt(stream, worldZ);
                stream.write(chunkData);
            } break;
            case UPDATEFLUIDDATA: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_TERRAIN);
                stream.write(TypeBytes.TERRAIN_MESSAGE_TYPE_UPDATEFLUIDDATA);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, chunkData.length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, worldX);
                ByteStreamUtils.writeInt(stream, worldY);
                ByteStreamUtils.writeInt(stream, worldZ);
                stream.write(chunkData);
            } break;
            case REQUESTEDITBLOCK: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_TERRAIN);
                stream.write(TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTEDITBLOCK);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, worldX);
                ByteStreamUtils.writeInt(stream, worldY);
                ByteStreamUtils.writeInt(stream, worldZ);
                ByteStreamUtils.writeInt(stream, voxelX);
                ByteStreamUtils.writeInt(stream, voxelY);
                ByteStreamUtils.writeInt(stream, voxelZ);
                ByteStreamUtils.writeInt(stream, blockType);
                ByteStreamUtils.writeInt(stream, blockMetadata);
                ByteStreamUtils.writeInt(stream, blockEditSize);
            } break;
            case REQUESTPLACEFAB: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_TERRAIN);
                stream.write(TypeBytes.TERRAIN_MESSAGE_TYPE_REQUESTPLACEFAB);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, fabPath.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeInt(stream, worldX);
                ByteStreamUtils.writeInt(stream, worldY);
                ByteStreamUtils.writeInt(stream, worldZ);
                ByteStreamUtils.writeInt(stream, voxelX);
                ByteStreamUtils.writeInt(stream, voxelY);
                ByteStreamUtils.writeInt(stream, voxelZ);
                ByteStreamUtils.writeInt(stream, blockRotation);
                ByteStreamUtils.writeString(stream, fabPath);
            } break;
        }
    }

}
