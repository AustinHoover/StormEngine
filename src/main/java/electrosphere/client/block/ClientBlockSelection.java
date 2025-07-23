package electrosphere.client.block;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.client.interact.select.AreaSelection;
import electrosphere.client.interact.select.AreaSelection.AreaSelectionType;
import electrosphere.client.scene.ClientWorldData;
import electrosphere.data.block.fab.BlockFab;
import electrosphere.engine.Globals;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.util.math.HashUtils;

/**
 * Class for selecting blocks on the client
 */
public class ClientBlockSelection {
    
    /**
     * Selects all blocks on the client
     */
    public static void selectAllBlocks(){
        Vector3d minPos = new Vector3d();
        Vector3d maxPos = new Vector3d();
        
        Vector3i minVoxelPos = new Vector3i();
        Vector3i maxVoxelPos = new Vector3i();

        Vector3i chunkPos = new Vector3i();

        boolean encountered = false;

        for(int x = 0; x < Globals.clientState.clientWorldData.getWorldDiscreteSize(); x++){
            for(int y = 0; y < Globals.clientState.clientWorldData.getWorldDiscreteSize(); y++){
                for(int z = 0; z < Globals.clientState.clientWorldData.getWorldDiscreteSize(); z++){
                    chunkPos = new Vector3i(x,y,z);

                    BlockChunkData blockChunkData = Globals.clientState.clientBlockManager.getChunkDataAtWorldPoint(chunkPos, 0);
                    if(blockChunkData.getHomogenousValue() == BlockChunkData.BLOCK_TYPE_EMPTY){
                        continue;
                    }

                    boolean foundVoxel = false;
                    for(int i = 0; i < BlockChunkData.CHUNK_DATA_WIDTH; i++){
                        for(int j = 0; j < BlockChunkData.CHUNK_DATA_WIDTH; j++){
                            for(int k = 0; k < BlockChunkData.CHUNK_DATA_WIDTH; k++){
                                if(blockChunkData.getType(i, j, k) == BlockChunkData.BLOCK_TYPE_EMPTY){
                                    continue;
                                }
                                if(!foundVoxel){
                                    foundVoxel = true;
                                    minVoxelPos.set(i,j,k);
                                    maxVoxelPos.set(i+1,j+1,k+1);
                                } else {
                                    minVoxelPos.min(new Vector3i(i,j,k));
                                    maxVoxelPos.max(new Vector3i(i+1,j+1,k+1));
                                }
                            }
                        }
                    }

                    Vector3d localMin = Globals.clientState.clientWorldData.convertBlockToRealSpace(chunkPos, minVoxelPos);
                    Vector3d localMax = Globals.clientState.clientWorldData.convertBlockToRealSpace(chunkPos, maxVoxelPos);

                    if(!encountered){
                        encountered = true;
                        minPos.set(localMin);
                        maxPos.set(localMax);
                    } else {
                        minPos.min(localMin);
                        maxPos.max(localMax);
                    }
                }
            }
        }
        AreaSelection selection = AreaSelection.createRect(minPos, maxPos);
        Globals.cursorState.selectRectangularArea(selection);
    }

    /**
     * Exports currently selected area of voxels
     */
    public static void exportSelection(){
        BlockFab fab = ClientBlockSelection.convertSelectionToFab();
        File exportLoc = new File("./assets/Data/fab/struct.block");
        fab.write(exportLoc);
    }

    /**
     * Converts the current selection by the player into a fab
     * @return The fab
     */
    public static BlockFab convertSelectionToFab(){
        AreaSelection selection = Globals.cursorState.getAreaSelection();

        //get dims
        int dimX = (int)((selection.getRectEnd().x - selection.getRectStart().x) * BlockChunkData.BLOCKS_PER_UNIT_DISTANCE);
        int dimY = (int)((selection.getRectEnd().y - selection.getRectStart().y) * BlockChunkData.BLOCKS_PER_UNIT_DISTANCE);
        int dimZ = (int)((selection.getRectEnd().z - selection.getRectStart().z) * BlockChunkData.BLOCKS_PER_UNIT_DISTANCE);

        Vector3d posCurr = new Vector3d();
        Vector3i chunkPos = null;
        Vector3i blockPos = null;

        int blockCount = dimX * dimY * dimZ;
        short[] types = new short[blockCount];
        short[] metadata = new short[blockCount];
        int i = 0;
        for(int x = 0; x < dimX; x++){
            for(int y = 0; y < dimY; y++){
                for(int z = 0; z < dimZ; z++){
                    posCurr.set(selection.getRectStart()).add(
                        x * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                        y * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                        z * BlockChunkData.BLOCK_SIZE_MULTIPLIER
                    );
                    chunkPos = ServerWorldData.convertRealToChunkSpace(posCurr);
                    BlockChunkData chunk = Globals.clientState.clientBlockManager.getChunkDataAtWorldPoint(chunkPos, 0);
                    if(chunk == null){
                        throw new Error("Failed to grab chunk at " + chunkPos);
                    }
                    blockPos = ServerWorldData.convertRealToLocalBlockSpace(posCurr);
                    types[i] = chunk.getType(blockPos.x, blockPos.y, blockPos.z);
                    metadata[i] = chunk.getMetadata(blockPos.x, blockPos.y, blockPos.z);
                    i++;
                }
            }
        }

        Vector3i dimensions = new Vector3i(dimX, dimY, dimZ);

        BlockFab fab = BlockFab.create(dimensions, types, metadata);
        return fab;
    }


    /**
     * 6-connected neighbors (orthogonal)
     */
    static final int[][] NEIGHBORS = {
        { 1, 0, 0}, {-1, 0, 0},
        { 0, 1, 0}, { 0,-1, 0},
        { 0, 0, 1}, { 0, 0,-1}
    };

    /**
     * Computes the SDF of distance from nearest solid block for the selected area
     * @param boundingArea The bounding area
     * @return The sdf
     */
    public static int[][][] computeCavitySDF(AreaSelection boundingArea){
        if(boundingArea.getType() != AreaSelectionType.RECTANGULAR){
            throw new Error("Unsupported type! " + boundingArea.getType());
        }
        int dimX = (int)Math.ceil((boundingArea.getRectEnd().x - boundingArea.getRectStart().x) * BlockChunkData.BLOCKS_PER_UNIT_DISTANCE);
        int dimY = (int)Math.ceil((boundingArea.getRectEnd().y - boundingArea.getRectStart().y) * BlockChunkData.BLOCKS_PER_UNIT_DISTANCE);
        int dimZ = (int)Math.ceil((boundingArea.getRectEnd().z - boundingArea.getRectStart().z) * BlockChunkData.BLOCKS_PER_UNIT_DISTANCE);

        Vector3d currPos = new Vector3d();

        //compute dist field
        int[][][] distField = new int[dimX][dimY][dimZ];


        //sets for breadth-first search
        LinkedList<Long> openSet = new LinkedList<Long>();
        Map<Long,Integer> closedSet = new HashMap<Long,Integer>();

        //enqueue all positions
        for(int x = 0; x < dimX; x++){
            for(int y = 0; y < dimY; y++){
                for(int z = 0; z < dimZ; z++){
                    currPos.set(boundingArea.getRectStart()).add(
                        x * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                        y * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                        z * BlockChunkData.BLOCK_SIZE_MULTIPLIER
                    );
                    Vector3i chunkPos = ClientWorldData.convertRealToChunkSpace(currPos);
                    Vector3i blockPos = ClientWorldData.convertRealToLocalBlockSpace(currPos);
                    BlockChunkData chunkData = Globals.clientState.clientBlockManager.getChunkDataAtWorldPoint(chunkPos, BlockChunkData.LOD_FULL_RES);
                    if(chunkData == null){
                        throw new Error("Missing chunk! " + chunkPos);
                    }
                    short type = chunkData.getType(blockPos.x, blockPos.y, blockPos.z);
                    if(type == BlockChunkData.BLOCK_TYPE_EMPTY){
                    } else {
                        openSet.add(HashUtils.hashIVec(x, y, z));
                        closedSet.put(HashUtils.hashIVec(x, y, z),1);
                    }
                }
            }
        }

        //error check initialization
        if(closedSet.size() < 1){
            throw new Error("Failed to detect empty chunks!");
        }

        //search
        while(openSet.size() > 0){
            Long hash = openSet.poll();
            int x = HashUtils.unhashIVec(hash, HashUtils.UNHASH_COMPONENT_X);
            int y = HashUtils.unhashIVec(hash, HashUtils.UNHASH_COMPONENT_Y);
            int z = HashUtils.unhashIVec(hash, HashUtils.UNHASH_COMPONENT_Z);
            int currVal = closedSet.get(hash);

            for(int[] dir : NEIGHBORS) {
                int nx = x + dir[0];
                int ny = y + dir[1];
                int nz = z + dir[2];
                
                if(
                    nx >= 0 && ny >= 0 && nz >= 0 &&
                    nx < dimX && ny < dimY && nz < dimZ
                ){
                    long nHash = HashUtils.hashIVec(nx, ny, nz);
                    if(!closedSet.containsKey(nHash)){
                        //evaluate all neighbors of this neighbor
                        openSet.add(nHash);
                        //store dist of neighbor
                        int neighborVal = currVal + 1;
                        closedSet.put(nHash, neighborVal);
                        distField[nx][ny][nz] = neighborVal;
                    }
                }
            }
        }

        return distField;
    }

}
