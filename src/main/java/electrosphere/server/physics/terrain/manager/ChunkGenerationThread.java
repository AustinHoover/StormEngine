package electrosphere.server.physics.terrain.manager;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.joml.AABBd;
import org.joml.Vector3i;

import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.macro.MacroData;
import electrosphere.server.macro.civilization.town.Town;
import electrosphere.server.macro.spatial.MacroAreaObject;
import electrosphere.server.macro.spatial.MacroLODObject;
import electrosphere.server.macro.spatial.MacroObject;
import electrosphere.server.macro.structure.VirtualStructure;
import electrosphere.server.physics.terrain.diskmap.ChunkDiskMap;
import electrosphere.server.physics.terrain.generation.interfaces.ChunkGenerator;

/**
 * A job that fetches a chunk, either by generating it or by reading it from disk
 */
public class ChunkGenerationThread implements Runnable {

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
    ChunkDiskMap chunkDiskMap;

    /**
     * The chunk cache on the server
     */
    ServerChunkCache chunkCache;

    /**
     * The chunk generator
     */
    ChunkGenerator chunkGenerator;

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
    Consumer<ServerTerrainChunk> onLoad;

    /**
     * Creates the chunk generation job
     * @param macroData The macro data
     * @param chunkDiskMap The chunk disk map
     * @param chunkCache The chunk cache on the server
     * @param chunkGenerator The chunk generator
     * @param worldX The world x coordinate
     * @param worldY The world y coordinate
     * @param worldZ The world z coordinate
     * @param stride The stride of the data
     * @param onLoad The work to do once the chunk is available
     */
    public ChunkGenerationThread(
        MacroData macroData,
        ChunkDiskMap chunkDiskMap,
        ServerChunkCache chunkCache,
        ChunkGenerator chunkGenerator,
        int worldX, int worldY, int worldZ,
        int stride,
        Consumer<ServerTerrainChunk> onLoad
    ){
        this.macroData = macroData;
        this.chunkDiskMap = chunkDiskMap;
        this.chunkCache = chunkCache;
        this.chunkGenerator = chunkGenerator;
        this.worldX = worldX;
        this.worldY = worldY;
        this.worldZ = worldZ;
        this.stride = stride;
        this.onLoad = onLoad;
    }

    @Override
    public void run() {
        try {
            int i = 0;
            ServerTerrainChunk chunk = null;
            while(chunk == null && i < MAX_TIME_TO_WAIT && Globals.engineState.threadManager.shouldKeepRunning()){
                chunk = ChunkGenerationThread.getChunk(macroData, worldX, worldY, worldZ, stride, chunkDiskMap, chunkCache, chunkGenerator);
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
     * Gets a chunk
     * @param macroData The macro data
     * @param worldX The world x coordinate
     * @param worldY The world y coordinate
     * @param worldZ The world z coordinate
     * @param stride The stride
     * @param chunkDiskMap The chunk disk map
     * @param chunkCache The chunk cache
     * @param chunkGenerator The chunk generator to use
     * @return The chunk if it was fetched or created, null otherwise
     */
    public static ServerTerrainChunk getChunk(
        MacroData macroData,
        int worldX, int worldY, int worldZ, int stride,
        ChunkDiskMap chunkDiskMap,
        ServerChunkCache chunkCache,
        ChunkGenerator chunkGenerator
    ){
        ServerTerrainChunk rVal = null;

        //get the macro data that affects this chunk
        List<MacroObject> objects = null;
        if(macroData == null){
            objects = new LinkedList<MacroObject>();
        } else {
            if(macroData != null){
                objects = macroData.getNearbyObjects(ServerWorldData.convertChunkToRealSpace(worldX, worldY, worldZ));
            }
            //if any of this macro data isn't ready, return a null chunk
            long notFullResCount = objects.stream().filter((MacroObject macroObj) -> macroObj instanceof MacroLODObject).map((MacroObject oldView) -> (MacroLODObject)oldView).filter((MacroLODObject lodObj) -> !lodObj.isFullRes()).count();
            if(notFullResCount > 0){
                return null;
            }
            List<MacroObject> towns = objects.stream().filter((MacroObject obj) -> obj instanceof Town).collect(Collectors.toList());
            for(MacroObject currObj : towns){
                Town town = (Town)currObj;
                List<VirtualStructure> structs = town.getStructures(macroData);
                objects.addAll(structs);
                objects.addAll(town.getFarmPlots(macroData));
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

        if(chunkCache.containsChunk(worldX, worldY, worldZ, stride)){
            rVal = chunkCache.get(worldX, worldY, worldZ, stride);
        } else {
            //pull from disk if it exists
            if(chunkDiskMap != null && stride == ServerChunkCache.STRIDE_FULL_RES){
                if(chunkDiskMap.containsTerrainAtPosition(worldX, worldY, worldZ, stride)){
                    rVal = chunkDiskMap.getTerrainChunk(worldX, worldY, worldZ, stride);
                }
            }
            //generate if it does not exist
            if(rVal == null){
                rVal = chunkGenerator.generateChunk(objects, worldX, worldY, worldZ, stride);
            }
            if(rVal != null){
                chunkCache.add(worldX, worldY, worldZ, stride, rVal);
            }
        }
        return rVal;
    }
    
}
