package electrosphere.net.client.protocol;


import java.util.LinkedList;
import java.util.List;

import org.joml.Vector3f;
import org.joml.Vector3i;

import electrosphere.client.block.BlockChunkData;
import electrosphere.client.scene.ClientWorldData;
import electrosphere.client.terrain.cache.ChunkData;
import electrosphere.collision.CollisionWorldData;
import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.TerrainMessage;
import electrosphere.net.template.ClientProtocolTemplate;

/**
 * The client protocol for handling terrain messages
 */
public class TerrainProtocol implements ClientProtocolTemplate<TerrainMessage> {

    @Override
    public TerrainMessage handleAsyncMessage(TerrainMessage message) {
        return message;
    }

    @Override
    public void handleSyncMessage(TerrainMessage message) {
        Globals.profiler.beginAggregateCpuSample("TerrainProtocol.handleTerrainMessage");
        switch(message.getMessageSubtype()){
            case RESPONSEMETADATA:
                Globals.clientState.clientWorldData = new ClientWorldData(
                        //Vector3f worldMinPoint, Vector3f worldMaxPoint, int dynamicInterpolationRatio, float randomDampener, int worldDiscreteSize
                        new Vector3f(
                            message.getworldMinX(),
                            message.getworldMinY(),
                            message.getworldMinZ()
                        ),
                        new Vector3f(
                            message.getworldMaxX(),
                            message.getworldMaxY(),
                            message.getworldMaxZ()
                        ),
                        message.getworldSizeDiscrete()
                );
                Globals.clientState.clientSceneWrapper.getCollisionEngine().setCollisionWorldData(new CollisionWorldData());
                Globals.clientState.clientConnection.getMessageProtocol().setHasReceivedWorld(true);
                break;
            case SPAWNPOSITION:
                LoggerInterface.loggerNetworking.WARNING("Received spawnPosition packet on client. This is deprecated!");
                break;
            case SENDCHUNKDATA: {
                LoggerInterface.loggerNetworking.DEBUG("(Client) Received terrain at " + message.getworldX() + " " + message.getworldY() + " " + message.getworldZ());
                Globals.clientState.clientTerrainManager.attachTerrainMessage(message);
            } break;
            case SENDREDUCEDCHUNKDATA: {
                LoggerInterface.loggerNetworking.DEBUG("(Client) Received terrain at " + message.getworldX() + " " + message.getworldY() + " " + message.getworldZ() + " " + message.getchunkResolution());
                Globals.clientState.clientTerrainManager.attachTerrainMessage(message);
            } break;
            case SENDREDUCEDBLOCKDATA: {
                LoggerInterface.loggerNetworking.DEBUG("(Client) Received blocks at " + message.getworldX() + " " + message.getworldY() + " " + message.getworldZ() + " " + message.getchunkResolution());
                Globals.clientState.clientBlockManager.attachTerrainMessage(message);
            } break;
            case UPDATEVOXEL: {
                //
                //find what all drawcells might be updated by this voxel update
                Vector3i worldPos = new Vector3i(message.getworldX(), message.getworldY(), message.getworldZ());
                List<Vector3i> positionsToUpdate = new LinkedList<Vector3i>();
                positionsToUpdate.add(worldPos);
                if(message.getvoxelX() < 1){
                    positionsToUpdate.add(new Vector3i(worldPos).sub(1,0,0));
                    if(message.getvoxelY() < 1){
                        positionsToUpdate.add(new Vector3i(worldPos).sub(1,1,0));
                        if(message.getvoxelZ() < 1){
                            positionsToUpdate.add(new Vector3i(worldPos).sub(1,1,1));
                        }
                    } else {
                        if(message.getvoxelZ() < 1){
                            positionsToUpdate.add(new Vector3i(worldPos).sub(1,0,1));
                        }
                    }
                } else {
                    if(message.getvoxelY() < 1){
                        positionsToUpdate.add(new Vector3i(worldPos).sub(0,1,0));
                        if(message.getvoxelZ() < 1){
                            positionsToUpdate.add(new Vector3i(worldPos).sub(0,1,1));
                        }
                    } else {
                        if(message.getvoxelZ() < 1){
                            positionsToUpdate.add(new Vector3i(worldPos).sub(0,0,1));
                        }
                    }
                }
                //
                //update the terrain cache
                if(Globals.clientState.clientTerrainManager.containsChunkDataAtWorldPoint(worldPos.x, worldPos.y, worldPos.z, ChunkData.NO_STRIDE)){
                    ChunkData data = Globals.clientState.clientTerrainManager.getChunkDataAtWorldPoint(worldPos.x, worldPos.y, worldPos.z, ChunkData.NO_STRIDE);
                    if(data != null){
                        data.updatePosition(
                            message.getvoxelX(),
                            message.getvoxelY(),
                            message.getvoxelZ(),
                            message.getterrainWeight(),
                            message.getterrainValue()
                        );
                    }
                }
                //
                //mark all relevant drawcells as updateable
                for(Vector3i worldPosToUpdate : positionsToUpdate){
                    if(
                        worldPosToUpdate.x >= 0 && worldPosToUpdate.x < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
                        worldPosToUpdate.y >= 0 && worldPosToUpdate.y < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
                        worldPosToUpdate.z >= 0 && worldPosToUpdate.z < Globals.clientState.clientWorldData.getWorldDiscreteSize()
                    ){
                        //
                        //mark terrain chunk for update
                        Globals.clientState.clientDrawCellManager.markUpdateable(worldPosToUpdate.x, worldPosToUpdate.y, worldPosToUpdate.z);

                        //
                        //update foliage manager
                        Globals.clientState.foliageCellManager.markUpdateable(
                            worldPosToUpdate.x, worldPosToUpdate.y, worldPosToUpdate.z,
                            message.getvoxelX(), message.getvoxelY(), message.getvoxelZ()
                        );
                        if(message.getvoxelX() > 0){
                            Globals.clientState.foliageCellManager.markUpdateable(
                                worldPosToUpdate.x, worldPosToUpdate.y, worldPosToUpdate.z,
                                message.getvoxelX() - 1, message.getvoxelY(), message.getvoxelZ()
                            );
                            if(message.getvoxelY() > 0){
                                Globals.clientState.foliageCellManager.markUpdateable(
                                    worldPosToUpdate.x, worldPosToUpdate.y, worldPosToUpdate.z,
                                    message.getvoxelX() - 1, message.getvoxelY() - 1, message.getvoxelZ()
                                );
                                if(message.getvoxelZ() > 0){
                                    Globals.clientState.foliageCellManager.markUpdateable(
                                        worldPosToUpdate.x, worldPosToUpdate.y, worldPosToUpdate.z,
                                        message.getvoxelX() - 1, message.getvoxelY() - 1, message.getvoxelZ() - 1
                                    );
                                }
                            } else {
                                if(message.getvoxelZ() > 0){
                                    Globals.clientState.foliageCellManager.markUpdateable(
                                        worldPosToUpdate.x, worldPosToUpdate.y, worldPosToUpdate.z,
                                        message.getvoxelX() - 1, message.getvoxelY(), message.getvoxelZ() - 1
                                    );
                                }
                            }
                        } else {
                            if(message.getvoxelY() > 0){
                                Globals.clientState.foliageCellManager.markUpdateable(
                                    worldPosToUpdate.x, worldPosToUpdate.y, worldPosToUpdate.z,
                                    message.getvoxelX(), message.getvoxelY() - 1, message.getvoxelZ()
                                );
                                if(message.getvoxelZ() > 0){
                                    Globals.clientState.foliageCellManager.markUpdateable(
                                        worldPosToUpdate.x, worldPosToUpdate.y, worldPosToUpdate.z,
                                        message.getvoxelX(), message.getvoxelY() - 1, message.getvoxelZ() - 1
                                    );
                                }
                            } else {
                                if(message.getvoxelZ() > 0){
                                    Globals.clientState.foliageCellManager.markUpdateable(
                                        worldPosToUpdate.x, worldPosToUpdate.y, worldPosToUpdate.z,
                                        message.getvoxelX(), message.getvoxelY(), message.getvoxelZ() - 1
                                    );
                                }
                            }
                        }
                    }
                }
            } break;
            case UPDATEBLOCK: {
                //
                //find what all drawcells might be updated by this voxel update
                Vector3i worldPos = new Vector3i(message.getworldX(), message.getworldY(), message.getworldZ());
                List<Vector3i> positionsToUpdate = new LinkedList<Vector3i>();
                positionsToUpdate.add(worldPos);
                if(message.getvoxelX() < 1){
                    positionsToUpdate.add(new Vector3i(worldPos).sub(1,0,0));
                    if(message.getvoxelY() < 1){
                        positionsToUpdate.add(new Vector3i(worldPos).sub(1,1,0));
                        if(message.getvoxelZ() < 1){
                            positionsToUpdate.add(new Vector3i(worldPos).sub(1,1,1));
                        }
                    } else {
                        if(message.getvoxelZ() < 1){
                            positionsToUpdate.add(new Vector3i(worldPos).sub(1,0,1));
                        }
                    }
                } else {
                    if(message.getvoxelY() < 1){
                        positionsToUpdate.add(new Vector3i(worldPos).sub(0,1,0));
                        if(message.getvoxelZ() < 1){
                            positionsToUpdate.add(new Vector3i(worldPos).sub(0,1,1));
                        }
                    } else {
                        if(message.getvoxelZ() < 1){
                            positionsToUpdate.add(new Vector3i(worldPos).sub(0,0,1));
                        }
                    }
                }
                //
                //update the block cache
                if(Globals.clientState.clientBlockManager.containsChunkDataAtWorldPoint(worldPos.x, worldPos.y, worldPos.z, ChunkData.NO_STRIDE)){
                    BlockChunkData data = Globals.clientState.clientBlockManager.getChunkDataAtWorldPoint(worldPos.x, worldPos.y, worldPos.z, ChunkData.NO_STRIDE);
                    if(data != null){
                        data.setType(
                            message.getvoxelX(),
                            message.getvoxelY(),
                            message.getvoxelZ(),
                            message.getblockType()
                        );
                        if(data.getHomogenousValue() != message.getblockType()){
                            data.setHomogenousValue(BlockChunkData.NOT_HOMOGENOUS);
                        }
                    }
                }
                //
                //mark all relevant drawcells as updateable
                for(Vector3i worldPosToUpdate : positionsToUpdate){
                    if(
                        worldPosToUpdate.x >= 0 && worldPosToUpdate.x < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
                        worldPosToUpdate.y >= 0 && worldPosToUpdate.y < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
                        worldPosToUpdate.z >= 0 && worldPosToUpdate.z < Globals.clientState.clientWorldData.getWorldDiscreteSize()
                    ){
                        //
                        //mark terrain chunk for update
                        Globals.clientState.clientBlockCellManager.markUpdateable(worldPosToUpdate.x, worldPosToUpdate.y, worldPosToUpdate.z);
                        Globals.clientState.clientBlockCellManager.markHomogenous(worldPosToUpdate.x, worldPosToUpdate.y, worldPosToUpdate.z, false);
                    }
                }
            } break;
            case SENDFLUIDDATA: {
                Globals.clientState.clientFluidManager.attachFluidMessage(message);
            } break;
            case UPDATEFLUIDDATA: {
                Globals.clientState.clientFluidManager.attachFluidMessage(message);
            } break;
            default: {
                LoggerInterface.loggerNetworking.WARNING("Client networking: Unhandled message of type: " + message.getMessageSubtype());
            } break;
        }
        Globals.profiler.endCpuSample();
    }

}
