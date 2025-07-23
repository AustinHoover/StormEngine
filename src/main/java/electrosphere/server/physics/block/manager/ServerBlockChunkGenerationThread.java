package electrosphere.server.physics.block.manager;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.joml.AABBd;
import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.client.block.BlockChunkCache;
import electrosphere.client.block.BlockChunkData;
import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.macro.MacroData;
import electrosphere.server.macro.spatial.MacroAreaObject;
import electrosphere.server.macro.spatial.MacroLODObject;
import electrosphere.server.macro.spatial.MacroObject;
import electrosphere.server.macro.structure.VirtualStructure;
import electrosphere.server.physics.block.diskmap.ServerBlockChunkDiskMap;
import electrosphere.server.physics.terrain.manager.ServerChunkCache;

/**
 * A job that fetches a chunk, either by generating it or by reading it from disk
 */
public class ServerBlockChunkGenerationThread implements Runnable {

    /**
     * The number of milliseconds to wait per iteration
     */
    static final int WAIT_TIME_MS = 2;

    /**
     * The maximum number of iterations to wait before failing
     */
    static final int MAX_TIME_TO_WAIT = 10;

    /**
     * The chunk disk map
     */
    ServerBlockChunkDiskMap chunkDiskMap;

    /**
     * The chunk cache on the server
     */
    BlockChunkCache chunkCache;

    /**
     * The macro data
     */
    MacroData macroData;

    /**
     * The world x coordinate
     */
    int worldX;

    /**
     * The world y coordinate
     */
    int worldY;

    /**
     * The world z coordinate
     */
    int worldZ;

    /**
     * The stride of the data
     */
    int stride;
    
    /**
     * The work to do once the chunk is available
     */
    Consumer<BlockChunkData> onLoad;

    /**
     * Creates the chunk generation job
     * @param macroData The macro data
     * @param chunkDiskMap The chunk disk map
     * @param chunkCache The chunk cache on the server
     * @param worldX The world x coordinate
     * @param worldY The world y coordinate
     * @param worldZ The world z coordinate
     * @param stride The stride of the data
     * @param onLoad The work to do once the chunk is available
     */
    public ServerBlockChunkGenerationThread(
        MacroData macroData,
        ServerBlockChunkDiskMap chunkDiskMap,
        BlockChunkCache chunkCache,
        int worldX, int worldY, int worldZ,
        int stride,
        Consumer<BlockChunkData> onLoad
    ){
        this.chunkDiskMap = chunkDiskMap;
        this.chunkCache = chunkCache;
        this.worldX = worldX;
        this.worldY = worldY;
        this.worldZ = worldZ;
        this.stride = stride;
        this.onLoad = onLoad;
        this.macroData = macroData;
    }

    @Override
    public void run() {
        BlockChunkData chunk = null;
        int i = 0;
        try {
            while(chunk == null && i < MAX_TIME_TO_WAIT && Globals.engineState.threadManager.shouldKeepRunning()){
                chunk = fetchOrGenerate(macroData, worldX, worldY, worldZ, stride, chunkDiskMap, chunkCache);
                if(chunk == null){
                    try {
                        TimeUnit.MILLISECONDS.sleep(WAIT_TIME_MS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                i++;
            }
            if(i >= MAX_TIME_TO_WAIT){
                throw new Error("Failed to resolve chunk!");
            }
            this.onLoad.accept(chunk);
        } catch (Error e){
            LoggerInterface.loggerEngine.ERROR(e);
        } catch(Exception e){
            LoggerInterface.loggerEngine.ERROR(e);
        }
    }

    /**
     * Fetches or generates a block chunk's data
     * @param macroData The macro data
     * @param worldX The world x coordinate
     * @param worldY The world y coordinate
     * @param worldZ The world z coordinate
     * @param stride The stride
     * @param chunkDiskMap The chunk disk map
     * @param chunkCache The chunk cache
     * @return The block chunk if it fetched/generated successfully, null otherwise
     */
    protected static BlockChunkData fetchOrGenerate(
        MacroData macroData,
        int worldX, int worldY, int worldZ, int stride,
        ServerBlockChunkDiskMap chunkDiskMap,
        BlockChunkCache chunkCache
    ){
        //get the macro data that affects this chunk
        List<MacroObject> objects = null;
        if(macroData == null){
            objects = new LinkedList<MacroObject>();
        } else {
            objects = macroData.getNearbyObjects(ServerWorldData.convertChunkToRealSpace(worldX, worldY, worldZ));
            //if any of this macro data isn't ready, return a null chunk
            long notFullResCount = objects.stream().filter((MacroObject macroObj) -> macroObj instanceof MacroLODObject).map((MacroObject oldView) -> (MacroLODObject)oldView).filter((MacroLODObject lodObj) -> !lodObj.isFullRes()).count();
            if(notFullResCount > 0){
                return null;
            }
            //filter to just objects that are within bounds of the chunk
            AABBd chunkAABB = new AABBd(ServerWorldData.convertChunkToRealSpace(new Vector3i(worldX,worldY,worldZ)), ServerWorldData.convertChunkToRealSpace(new Vector3i(worldX+1,worldY+1,worldZ+1)));
            objects = objects.stream().filter((MacroObject obj) -> {
                if(obj instanceof MacroAreaObject areaObj){
                    return chunkAABB.testAABB(areaObj.getAABB());
                } else {
                    return chunkAABB.testPoint(obj.getPos());
                }
            }).collect(Collectors.toList());
        }

        BlockChunkData chunk = null;
        if(chunkCache.containsChunk(worldX, worldY, worldZ, stride)){
            chunk = chunkCache.get(worldX, worldY, worldZ, stride);
        } else {
            //pull from disk if it exists
            if(chunkDiskMap != null && stride == ServerChunkCache.STRIDE_FULL_RES){
                if(chunkDiskMap.containsBlocksAtPosition(worldX, worldY, worldZ)){
                    chunk = chunkDiskMap.getBlockChunk(worldX, worldY, worldZ);
                }
            }
            //generate if it does not exist
            if(chunk == null){
                chunk = new BlockChunkData();
                chunk.setWorldX(worldX);
                chunk.setWorldY(worldY);
                chunk.setWorldZ(worldZ);
                ServerBlockChunkGenerationThread.generate(chunk, macroData, worldX, worldY, worldZ, stride);
            }
            if(chunk != null){
                chunkCache.add(worldX, worldY, worldZ, stride, chunk);
            }
        }
        return chunk;
    }

    /**
     * Generates the actual values of the chunk 
     * @param chunk THe chunk
     * @param macroData The macro data
     * @param worldX THe world x coordinate
     * @param worldY The world y coordinate
     * @param worldZ The world z coordinate
     * @param stride The stride of the data to generate
     */
    protected static void generate(BlockChunkData chunk, MacroData macroData, int worldX, int worldY, int worldZ, int stride){
        if(macroData == null){
            chunk.setHomogenousValue(0);
            return;
        }
        //check if this chunk intersects any macro data
        int strideMultiplier = (int)Math.pow(2,stride);
        if(strideMultiplier > 16){
            throw new Error("Invalid stride size!");
        }
        AABBd localAABB = new AABBd(ServerWorldData.convertChunkToRealSpace(worldX,worldY,worldZ),ServerWorldData.convertChunkToRealSpace(worldX+strideMultiplier,worldY+strideMultiplier,worldZ+strideMultiplier));
        List<VirtualStructure> filtered = macroData.getStructures().stream().filter((VirtualStructure struct) -> {return !struct.isRepairable() && struct.getAABB().testAABB(localAABB);}).collect(Collectors.toList());
        if(filtered.size() > 0){
            Vector3i chunkPos = new Vector3i(worldX, worldY, worldZ);
            Vector3d chunkRealPos = ServerWorldData.convertChunkToRealSpace(chunkPos);
            Vector3d localBlockPos = new Vector3d();
            Vector3d currRealPos = new Vector3d(chunkRealPos);
            //contains at least one structure
            for(int x = 0; x < BlockChunkData.CHUNK_DATA_WIDTH; x++){
                for(int y = 0; y < BlockChunkData.CHUNK_DATA_WIDTH; y++){
                    for(int z = 0; z < BlockChunkData.CHUNK_DATA_WIDTH; z++){
                        boolean placedBlock = false;

                        currRealPos.set(chunkRealPos).add(x * strideMultiplier * BlockChunkData.BLOCK_SIZE_MULTIPLIER,y * strideMultiplier * BlockChunkData.BLOCK_SIZE_MULTIPLIER,z * strideMultiplier * BlockChunkData.BLOCK_SIZE_MULTIPLIER);

                        //try placing a structure block
                        for(VirtualStructure struct : filtered){
                            if(struct.getAABB().testPoint(currRealPos.x, currRealPos.y, currRealPos.z)){
                                AABBd aabb = struct.getAABB();
                                localBlockPos.set(
                                    Math.round((currRealPos.x - aabb.minX) / BlockChunkData.BLOCK_SIZE_MULTIPLIER),
                                    Math.round((currRealPos.y - aabb.minY) / BlockChunkData.BLOCK_SIZE_MULTIPLIER),
                                    Math.round((currRealPos.z - aabb.minZ) / BlockChunkData.BLOCK_SIZE_MULTIPLIER)
                                );

                                int finalX = Math.round((float)localBlockPos.x);
                                int finalY = Math.round((float)localBlockPos.y);
                                int finalZ = Math.round((float)localBlockPos.z);

                                int intermediateX = finalX;
                                int intermediateZ = finalZ;

                                int dimX = (int)Math.round((struct.getAABB().maxX - struct.getAABB().minX) / BlockChunkData.BLOCK_SIZE_MULTIPLIER);
                                int dimZ = (int)Math.round((struct.getAABB().maxZ - struct.getAABB().minZ) / BlockChunkData.BLOCK_SIZE_MULTIPLIER);

                                switch(struct.getRotation()){
                                    case 0: {
                                    } break;
                                    case 1: {
                                        finalX = intermediateZ;
                                        finalZ = dimZ - intermediateX;
                                    } break;
                                    case 2: {
                                        finalX = dimX - intermediateX;
                                        finalZ = dimZ - intermediateZ;
                                    } break;
                                    case 3: {
                                        finalX = dimX - intermediateZ;
                                        finalZ = intermediateX;
                                    } break;
                                    default: {
                                        throw new Error("Unsupported rotation value " + struct.getRotation());
                                    }
                                }

                                //structure file might have dimensions larger than fab, so need to make sure we're inbounds on fab file to draw data from fab file
                                if(
                                    finalX >= 0 &&
                                    finalY >= 0 &&
                                    finalZ >= 0 &&
                                    finalX < struct.getFab().getDimensions().x &&
                                    finalY < struct.getFab().getDimensions().y &&
                                    finalZ < struct.getFab().getDimensions().z
                                ){
                                    short blocktype = struct.getFab().getType(finalX,finalY,finalZ);
                                    chunk.setType(x, y, z, blocktype);
                                    placedBlock = true;
                                }
                            }
                        }

                        //if failed to place structure block, place an empty block
                        if(!placedBlock){
                            chunk.setType(x, y, z, BlockChunkData.BLOCK_TYPE_EMPTY);
                        }
                    }
                }
            }
        } else {
            //an empty chunk (no structures inside)
            chunk.setHomogenousValue(0);
        }
    }
    
}
