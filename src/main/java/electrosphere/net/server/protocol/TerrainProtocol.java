package electrosphere.net.server.protocol;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.function.Consumer;

import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.client.block.BlockChunkData;
import electrosphere.client.terrain.cache.ChunkData;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.TerrainMessage;
import electrosphere.net.server.ServerConnectionHandler;
import electrosphere.net.server.player.Player;
import electrosphere.net.template.ServerProtocolTemplate;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.datacell.utils.EntityLookupUtils;
import electrosphere.server.physics.fluid.manager.ServerFluidChunk;
import electrosphere.server.physics.terrain.editing.TerrainEditing;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import electrosphere.server.player.BlockActions;

/**
 * Server handling for terrain network messages
 */
public class TerrainProtocol implements ServerProtocolTemplate<TerrainMessage> {

    @Override
    public TerrainMessage handleAsyncMessage(ServerConnectionHandler connectionHandler, TerrainMessage message) {
        switch(message.getMessageSubtype()){
            case REQUESTCHUNKDATA: {
                throw new Error("Deprecated call!");
            }
            case REQUESTREDUCEDCHUNKDATA: {
                TerrainProtocol.sendWorldSubChunkAsyncStrided(connectionHandler, 
                    message.getworldX(), message.getworldY(), message.getworldZ(), message.getchunkResolution()
                );
                return null;
            }
            case REQUESTREDUCEDBLOCKDATA: {
                TerrainProtocol.sendBlocksAsyncStrided(connectionHandler, 
                    message.getworldX(), message.getworldY(), message.getworldZ(), message.getchunkResolution()
                );
                return null;
            }
            default: {
            } break;
        }
        return message;
    }

    @Override
    public void handleSyncMessage(ServerConnectionHandler connectionHandler, TerrainMessage message) {
        switch(message.getMessageSubtype()){
            case REQUESTMETADATA: {
                TerrainProtocol.sendWorldMetadata(connectionHandler);
            } break;
            case REQUESTCHUNKDATA: {
                LoggerInterface.loggerNetworking.DEBUG("(Server) Received request for terrain " + message.getworldX() + " " + message.getworldY() + " " + message.getworldZ());
                // System.out.println("Received request for terrain " + message.getworldX() + " " + message.getworldY() + " " + message.getworldZ());
                TerrainProtocol.sendWorldSubChunk(connectionHandler, 
                message.getworldX(), message.getworldY(), message.getworldZ(), message.getchunkResolution()
                );
            } break;
            case REQUESTEDITVOXEL: {
                TerrainProtocol.attemptTerrainEdit(connectionHandler, message);
            } break;
            case REQUESTUSETERRAINPALETTE: {
                TerrainProtocol.attemptUseTerrainEditPalette(connectionHandler, message);
            } break;
            case REQUESTDESTROYTERRAIN: {
                TerrainProtocol.attemptDestroyTerrain(connectionHandler, message);
            } break;
            case REQUESTFLUIDDATA: {
                LoggerInterface.loggerNetworking.DEBUG("(Server) Received request for fluid " + message.getworldX() + " " + message.getworldY() + " " + message.getworldZ());
                // System.out.println("Received request for fluid " + message.getworldX() + " " + message.getworldY() + " " + message.getworldZ());
                TerrainProtocol.sendWorldFluidSubChunk(connectionHandler,
                message.getworldX(), message.getworldY(), message.getworldZ()
                );
            } break;
            case REQUESTEDITBLOCK: {
                LoggerInterface.loggerNetworking.DEBUG("(Server) Received request to edit block at " + message.getworldX() + " " + message.getworldY() + " " + message.getworldZ());
                Entity targetEntity = EntityLookupUtils.getEntityById(connectionHandler.getPlayerEntityId());
                Vector3i worldPos = new Vector3i(message.getworldX(),message.getworldY(),message.getworldZ());
                Vector3i blockPos = new Vector3i(message.getvoxelX(),message.getvoxelY(),message.getvoxelZ());
                BlockActions.editBlockArea(targetEntity, worldPos, blockPos, (short)message.getblockType(), message.getblockEditSize());
            } break;
            case REQUESTPLACEFAB: {
                Vector3i worldPos = new Vector3i(message.getworldX(),message.getworldY(),message.getworldZ());
                Vector3i blockPos = new Vector3i(message.getvoxelX(),message.getvoxelY(),message.getvoxelZ());
                Entity targetEntity = EntityLookupUtils.getEntityById(connectionHandler.getPlayerEntityId());
                BlockActions.placeFab(targetEntity, worldPos, blockPos, message.getblockRotation(), message.getfabPath());
            } break;
            //all ignored message types
            case UPDATEFLUIDDATA:
            case RESPONSEMETADATA:
            case SPAWNPOSITION:
            case UPDATEVOXEL:
            case UPDATEBLOCK:
            case SENDCHUNKDATA:
            case SENDFLUIDDATA:
            case REQUESTREDUCEDCHUNKDATA:
            case SENDREDUCEDCHUNKDATA:
            case REQUESTREDUCEDBLOCKDATA:
            case SENDREDUCEDBLOCKDATA:
            //silently ignore
            break;
        }
    }

    /**
     * Sends a subchunk to the client
     * @param connectionHandler The connection handler
     * @param worldX the world x
     * @param worldY the world y
     * @param worldZ the world z
     */
    static void sendWorldSubChunk(ServerConnectionHandler connectionHandler, int worldX, int worldY, int worldZ, int stride){
        Globals.profiler.beginAggregateCpuSample("TerrainProtocol(server).sendWorldSubChunk");
                
        // System.out.println("Received request for chunk " + message.getworldX() + " " + message.getworldY());
        Realm realm = Globals.serverState.playerManager.getPlayerRealm(connectionHandler.getPlayer());
        if(realm.getServerWorldData().getServerTerrainManager() == null){
            return;
        }

        //request chunk
        ServerTerrainChunk chunk = realm.getServerWorldData().getServerTerrainManager().getChunk(worldX, worldY, worldZ, stride);

        //The length along each access of the chunk data. Typically, should be at least 17.
        //Because CHUNK_SIZE is 16, 17 adds the necessary extra value. Each chunk needs the value of the immediately following position to generate
        //chunk data that connects seamlessly to the next chunk.
        int xWidth = chunk.getWeights().length;
        int yWidth = chunk.getWeights()[0].length;
        int zWidth = chunk.getWeights()[0][0].length;

        ByteBuffer buffer = ByteBuffer.allocate(xWidth*yWidth*zWidth*(4+4));
        FloatBuffer floatView = buffer.asFloatBuffer();

        for(int x = 0; x < xWidth; x++){
            for(int y = 0; y < yWidth; y++){
                for(int z = 0; z < zWidth; z++){
                    floatView.put(chunk.getWeights()[x][y][z]);
                }
            }
        }

        IntBuffer intView = buffer.asIntBuffer();
        intView.position(floatView.position());

        for(int x = 0; x < xWidth; x++){
            for(int y = 0; y < yWidth; y++){
                for(int z = 0; z < zWidth; z++){
                    intView.put(chunk.getValues()[x][y][z]);
                }
            }
        }

        System.out.println("(Server) Send terrain at " + worldX + " " + worldY + " " + worldZ);
        LoggerInterface.loggerNetworking.DEBUG("(Server) Send terrain at " + worldX + " " + worldY + " " + worldZ);
        connectionHandler.addMessagetoOutgoingQueue(TerrainMessage.constructsendChunkDataMessage(worldX, worldY, worldZ, buffer.array()));

        Globals.profiler.endCpuSample();
    }

    /**
     * Sends a subchunk to the client
     * @param connectionHandler The connection handler
     * @param worldX the world x
     * @param worldY the world y
     * @param worldZ the world z
     * @param stride The stride of the data
     */
    static void sendWorldSubChunkAsyncStrided(ServerConnectionHandler connectionHandler, int worldX, int worldY, int worldZ, int stride){
        Globals.profiler.beginAggregateCpuSample("TerrainProtocol(server).sendWorldSubChunk");
                
        // System.out.println("Received request for chunk " + message.getworldX() + " " + message.getworldY());
        Realm realm = Globals.serverState.playerManager.getPlayerRealm(connectionHandler.getPlayer());
        if(realm.getServerWorldData().getServerTerrainManager() == null){
            return;
        }

        ServerWorldData serverWorldData = realm.getServerWorldData();

        //special handling for a world of size 1
        if(
            serverWorldData.getWorldBoundMax().x == ServerTerrainChunk.CHUNK_DIMENSION &&
            serverWorldData.getWorldBoundMax().y == ServerTerrainChunk.CHUNK_DIMENSION &&
            serverWorldData.getWorldBoundMax().z == ServerTerrainChunk.CHUNK_DIMENSION
        ){
            TerrainProtocol.sendWorldSubChunkAsyncStridedSingleChunk(connectionHandler, worldX, worldY, worldZ, stride);
            Globals.profiler.endCpuSample();
            return;
        }

        //normal multi-chunk world error checking
        if(worldX + Math.pow(2,stride) * ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE >= serverWorldData.getWorldBoundMax().x){
            throw new Error("Requested invalid position! " + worldX);
        }
        if(worldY + Math.pow(2,stride) * ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE >= serverWorldData.getWorldBoundMax().y){
            throw new Error("Requested invalid position! " + worldY);
        }
        if(worldZ + Math.pow(2,stride) * ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE >= serverWorldData.getWorldBoundMax().z){
            throw new Error("Requested invalid position! " + worldZ);
        }

        Consumer<ServerTerrainChunk> onLoad = (ServerTerrainChunk chunk) -> {
            //The length along each access of the chunk data. Typically, should be at least 17.
            //Because CHUNK_SIZE is 16, 17 adds the necessary extra value. Each chunk needs the value of the immediately following position to generate
            //chunk data that connects seamlessly to the next chunk.
            int xWidth = chunk.getWeights().length;
            int yWidth = chunk.getWeights()[0].length;
            int zWidth = chunk.getWeights()[0][0].length;

            byte[] toSend = null;

            if(chunk.getHomogenousValue() == ChunkData.NOT_HOMOGENOUS){
                ByteBuffer buffer = ByteBuffer.allocate(xWidth*yWidth*zWidth*(4+4));
                FloatBuffer floatView = buffer.asFloatBuffer();

                for(int x = 0; x < xWidth; x++){
                    for(int z = 0; z < zWidth; z++){
                        for(int y = 0; y < yWidth; y++){
                            floatView.put(chunk.getWeights()[x][y][z]);
                        }
                    }
                }

                IntBuffer intView = buffer.asIntBuffer();
                intView.position(floatView.position());

                for(int x = 0; x < xWidth; x++){
                    for(int z = 0; z < zWidth; z++){
                        for(int y = 0; y < yWidth; y++){
                            intView.put(chunk.getValues()[x][y][z]);
                        }
                    }
                }
                toSend = buffer.array();
            } else {
                toSend = new byte[]{ 0 };
            }


            // System.out.println("(Server) Send terrain at " + worldX + " " + worldY + " " + worldZ);
            LoggerInterface.loggerNetworking.DEBUG("(Server) Send terrain at " + worldX + " " + worldY + " " + worldZ);
            connectionHandler.addMessagetoOutgoingQueue(TerrainMessage.constructSendReducedChunkDataMessage(chunk.getWorldX(), chunk.getWorldY(), chunk.getWorldZ(), stride, chunk.getHomogenousValue(), toSend));
        };

        //request chunk
        realm.getServerWorldData().getServerTerrainManager().getChunkAsync(worldX, worldY, worldZ, stride, onLoad);

        Globals.profiler.endCpuSample();
    }


    /**
     * Sends a subchunk to the client
     * @param connectionHandler The connection handler
     * @param worldX the world x
     * @param worldY the world y
     * @param worldZ the world z
     * @param stride The stride of the data
     */
    static void sendWorldSubChunkAsyncStridedSingleChunk(ServerConnectionHandler connectionHandler, int worldX, int worldY, int worldZ, int stride){
        Globals.profiler.beginAggregateCpuSample("TerrainProtocol(server).sendWorldSubChunk");
                
        // System.out.println("Received request for chunk " + message.getworldX() + " " + message.getworldY());
        Realm realm = Globals.serverState.playerManager.getPlayerRealm(connectionHandler.getPlayer());
        if(realm.getServerWorldData().getServerTerrainManager() == null){
            return;
        }

        Consumer<ServerTerrainChunk> onLoad = (ServerTerrainChunk chunk) -> {
            //The length along each access of the chunk data. Typically, should be at least 17.
            //Because CHUNK_SIZE is 16, 17 adds the necessary extra value. Each chunk needs the value of the immediately following position to generate
            //chunk data that connects seamlessly to the next chunk.
            int xWidth = chunk.getWeights().length;
            int yWidth = chunk.getWeights()[0].length;
            int zWidth = chunk.getWeights()[0][0].length;

            byte[] toSend = null;

            if(chunk.getHomogenousValue() == ChunkData.NOT_HOMOGENOUS){
                ByteBuffer buffer = ByteBuffer.allocate(xWidth*yWidth*zWidth*(4+4));
                FloatBuffer floatView = buffer.asFloatBuffer();

                for(int x = 0; x < xWidth; x++){
                    for(int z = 0; z < zWidth; z++){
                        for(int y = 0; y < yWidth; y++){
                            floatView.put(chunk.getWeights()[x][y][z]);
                        }
                    }
                }

                IntBuffer intView = buffer.asIntBuffer();
                intView.position(floatView.position());

                for(int x = 0; x < xWidth; x++){
                    for(int z = 0; z < zWidth; z++){
                        for(int y = 0; y < yWidth; y++){
                            intView.put(chunk.getValues()[x][y][z]);
                        }
                    }
                }
                toSend = buffer.array();
            } else {
                toSend = new byte[]{ 0 };
            }


            // System.out.println("(Server) Send terrain at " + worldX + " " + worldY + " " + worldZ);
            LoggerInterface.loggerNetworking.DEBUG("(Server) Send terrain at " + worldX + " " + worldY + " " + worldZ);
            connectionHandler.addMessagetoOutgoingQueue(TerrainMessage.constructSendReducedChunkDataMessage(chunk.getWorldX(), chunk.getWorldY(), chunk.getWorldZ(), stride, chunk.getHomogenousValue(), toSend));
        };

        //request chunk
        realm.getServerWorldData().getServerTerrainManager().getChunkAsync(worldX, worldY, worldZ, stride, onLoad);

        Globals.profiler.endCpuSample();
    }

    /**
     * Sends a subchunk to the client
     * @param connectionHandler The connection handler
     * @param worldX the world x
     * @param worldY the world y
     * @param worldZ the world z
     * @param stride The stride of the data
     */
    private static void sendBlocksAsyncStrided(ServerConnectionHandler connectionHandler, int worldX, int worldY, int worldZ, int stride){
        Globals.profiler.beginAggregateCpuSample("TerrainProtocol(server).sendWorldSubChunk");
                
        // System.out.println("Received request for chunk " + message.getworldX() + " " + message.getworldY());
        Realm realm = Globals.serverState.playerManager.getPlayerRealm(connectionHandler.getPlayer());
        if(realm.getServerWorldData().getServerBlockManager() == null){
            return;
        }

        Consumer<BlockChunkData> onLoad = (BlockChunkData chunk) -> {
            //The length along each access of the chunk data. Typically, should be at least 17.
            //Because CHUNK_SIZE is 16, 17 adds the necessary extra value. Each chunk needs the value of the immediately following position to generate
            //chunk data that connects seamlessly to the next chunk.
            byte[] toSend = null;

            if(chunk.getHomogenousValue() == ChunkData.NOT_HOMOGENOUS){
                ByteBuffer buffer = ByteBuffer.allocate(BlockChunkData.BUFFER_SIZE);
                ShortBuffer shortView = buffer.asShortBuffer();

                short[] type = chunk.getType();
                short[] metadata = chunk.getMetadata();

                for(int i = 0; i < BlockChunkData.TOTAL_DATA_WIDTH; i++){
                    shortView.put(type[i]);
                }
                for(int i = 0; i < BlockChunkData.TOTAL_DATA_WIDTH; i++){
                    shortView.put(metadata[i]);
                }
                toSend = buffer.array();
            } else {
                //error checking byte
                toSend = new byte[]{ -1 };
            }

            LoggerInterface.loggerNetworking.DEBUG("(Server) Send block data at " + worldX + " " + worldY + " " + worldZ);
            connectionHandler.addMessagetoOutgoingQueue(TerrainMessage.constructSendReducedBlockDataMessage(worldX, worldY, worldZ, stride, chunk.getHomogenousValue(), toSend));
        };

        //request chunk
        realm.getServerWorldData().getServerBlockManager().getChunkAsync(worldX, worldY, worldZ, stride, onLoad);

        Globals.profiler.endCpuSample();
    }


    /**
     * Sends a fluid sub chunk to the client
     * @param connectionHandler The connection handler
     * @param worldX the world x
     * @param worldY the world y
     * @param worldZ the world z
     */
    static void sendWorldFluidSubChunk(ServerConnectionHandler connectionHandler, int worldX, int worldY, int worldZ){
        Realm realm = Globals.serverState.playerManager.getPlayerRealm(connectionHandler.getPlayer());

        if(realm.getServerWorldData().getServerTerrainManager() == null){
            return;
        }
                
        // System.out.println("Received request for chunk " + message.getworldX() + " " + message.getworldY());
                        
        ServerFluidChunk chunk = realm.getServerWorldData().getServerFluidManager().getChunk(worldX, worldY, worldZ);

        
        ByteBuffer buffer = constructFluidByteBuffer(chunk);

        connectionHandler.addMessagetoOutgoingQueue(TerrainMessage.constructsendFluidDataMessage(worldX, worldY, worldZ, buffer.array()));

    }

    /**
     * Sends world metadata to the client
     * @param connectionHandler The connection handler
     */
    static void sendWorldMetadata(ServerConnectionHandler connectionHandler){
        Realm realm = Globals.serverState.playerManager.getPlayerRealm(connectionHandler.getPlayer());
        //world metadata
        connectionHandler.addMessagetoOutgoingQueue(
            TerrainMessage.constructResponseMetadataMessage(
                realm.getServerWorldData().getWorldSizeDiscrete(),
                (int)realm.getServerWorldData().getWorldBoundMin().x,
                (int)realm.getServerWorldData().getWorldBoundMin().y,
                (int)realm.getServerWorldData().getWorldBoundMin().z,
                (int)realm.getServerWorldData().getWorldBoundMax().x,
                (int)realm.getServerWorldData().getWorldBoundMax().y,
                (int)realm.getServerWorldData().getWorldBoundMax().z
            )
        );
    }

    /**
     * Attempts to perform an edit requested by a client
     * @param message The message containing the edit request
     */
    static void attemptTerrainEdit(ServerConnectionHandler connectionHandler, TerrainMessage message){
        // Player player = Globals.serverState.playerManager.getPlayerFromId(connectionHandler.getPlayerId());
        throw new UnsupportedOperationException();
    }

    /**
     * Attempts to use an edit palette on the terrain
     * @param connectionHandler The connection handler
     * @param message The message that contains the request to use an edit palette
     */
    static void attemptUseTerrainEditPalette(ServerConnectionHandler connectionHandler, TerrainMessage message){
        Player player = Globals.serverState.playerManager.getPlayerFromId(connectionHandler.getPlayerId());
        Realm realm = Globals.serverState.realmManager.getPlayerRealm(player);
        Vector3d location = new Vector3d(message.getrealLocationX(), message.getrealLocationY(), message.getrealLocationZ());
        TerrainEditing.editTerrain(realm, location, message.getvalue(), message.getterrainValue(), message.getterrainWeight());
    }

    /**
     * Attempts to destroy terrain
     * @param connectionHandler The connection handler
     * @param message The message that contains the request to use an edit palette
     */
    static void attemptDestroyTerrain(ServerConnectionHandler connectionHandler, TerrainMessage message){
        Player player = Globals.serverState.playerManager.getPlayerFromId(connectionHandler.getPlayerId());
        Realm realm = Globals.serverState.realmManager.getPlayerRealm(player);
        Vector3d location = new Vector3d(message.getrealLocationX(), message.getrealLocationY(), message.getrealLocationZ());
        TerrainEditing.destroyTerrain(realm, player.getPlayerEntity(), location, message.getvalue(), message.getterrainWeight());
    }

    /**
     * Constructs a buffer to send a fluid chunk to the client
     * @param chunk The chunk to send
     * @return the buffer
     */
    public static ByteBuffer constructFluidByteBuffer(ServerFluidChunk chunk){
        ByteBuffer rVal = null;
        if(chunk.isHomogenous()){
            rVal = ByteBuffer.allocate(ServerFluidChunk.HOMOGENOUS_BUFFER_SIZE);
            FloatBuffer floatView = rVal.asFloatBuffer();
            floatView.put(ServerFluidChunk.IS_HOMOGENOUS);
        } else {
            rVal = ByteBuffer.allocate(ServerFluidChunk.BUFFER_SIZE*(4+4+4+4) + ServerFluidChunk.HOMOGENOUS_BUFFER_SIZE);
            FloatBuffer floatView = rVal.asFloatBuffer();

            floatView.put(ServerFluidChunk.IS_NOT_HOMOGENOUS);

            for(int x = 0; x < ServerFluidChunk.BUFFER_DIM; x++){
                for(int y = 0; y < ServerFluidChunk.BUFFER_DIM; y++){
                    for(int z = 0; z < ServerFluidChunk.BUFFER_DIM; z++){
                        floatView.put(chunk.getWeight(x, y, z));
                    }
                }
            }

            for(int x = 0; x < ServerFluidChunk.BUFFER_DIM; x++){
                for(int y = 0; y < ServerFluidChunk.BUFFER_DIM; y++){
                    for(int z = 0; z < ServerFluidChunk.BUFFER_DIM; z++){
                        floatView.put(chunk.getVelocityX(x, y, z));
                    }
                }
            }

            for(int x = 0; x < ServerFluidChunk.BUFFER_DIM; x++){
                for(int y = 0; y < ServerFluidChunk.BUFFER_DIM; y++){
                    for(int z = 0; z < ServerFluidChunk.BUFFER_DIM; z++){
                        floatView.put(chunk.getVelocityY(x, y, z));
                    }
                }
            }

            for(int x = 0; x < ServerFluidChunk.BUFFER_DIM; x++){
                for(int y = 0; y < ServerFluidChunk.BUFFER_DIM; y++){
                    for(int z = 0; z < ServerFluidChunk.BUFFER_DIM; z++){
                        floatView.put(chunk.getVelocityZ(x, y, z));
                    }
                }
            }
        }

        return rVal;
    }
    
}
