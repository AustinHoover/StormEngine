package electrosphere.client.block.solver;

import java.util.LinkedList;
import java.util.List;

import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.client.block.BlockChunkData;
import electrosphere.client.block.ClientBlockSelection;
import electrosphere.client.block.solver.FurnitureSolver.LayoutType;
import electrosphere.client.interact.select.AreaSelection;
import electrosphere.client.interact.select.AreaSelection.AreaSelectionType;
import electrosphere.client.scene.ClientWorldData;
import electrosphere.data.block.fab.FurnitureSlotMetadata;
import electrosphere.data.block.fab.RoomMetadata;
import electrosphere.data.block.fab.StructureMetadata;
import electrosphere.engine.Globals;

/**
 * Solves for the rooms in a structure
 */
public class RoomSolver {

    /**
     * Maximum radius of room in blocks
     */
    public static final int MAX_ROOM_SIZE = 20;

    /**
     * Minimum height of a doorway
     */
    public static final int DOORWAY_MIN_HEIGHT = 8;

    /**
     * Minimum width of a doorway
     */
    public static final int DOORWAY_MIN_WIDTH = 4;
    
    /**
     * 6-connected neighbors (orthogonal)
     */
    static final int[][] NEIGHBORS = {
        { 1, 0, 0}, {-1, 0, 0},
        { 0, 1, 0}, { 0,-1, 0},
        { 0, 0, 1}, { 0, 0,-1}
    };

    /**
     * Computes rooms from the currently selected voxels
     */
    public static void computeRoomsFromSelection(AreaSelection boundingArea, StructureMetadata structureMetadata){
        if(boundingArea.getType() != AreaSelectionType.RECTANGULAR){
            throw new Error("Unsupported type! " + boundingArea.getType());
        }
        int dimX = (int)Math.ceil((boundingArea.getRectEnd().x - boundingArea.getRectStart().x) * BlockChunkData.BLOCKS_PER_UNIT_DISTANCE);
        int dimY = (int)Math.ceil((boundingArea.getRectEnd().y - boundingArea.getRectStart().y) * BlockChunkData.BLOCKS_PER_UNIT_DISTANCE);
        int dimZ = (int)Math.ceil((boundingArea.getRectEnd().z - boundingArea.getRectStart().z) * BlockChunkData.BLOCKS_PER_UNIT_DISTANCE);

        int[][][] distField = ClientBlockSelection.computeCavitySDF(boundingArea);

        LinkedList<Vector3i> localMaximums = new LinkedList<Vector3i>();
        for(int x = 0; x < dimX; x++){
            for(int y = 0; y < dimY; y++){
                for(int z = 0; z < dimZ; z++){
                    //don't consider edges of SDF
                    if(x == 0 || y == 0 || z == 0 || x == dimX - 1 || y == dimY - 1 || z == dimZ - 1){
                        continue;
                    }
                    if(
                        distField[x][y][z] >= distField[x-1][y][z] &&
                        distField[x][y][z] >= distField[x+1][y][z] &&
                        distField[x][y][z] >= distField[x][y-1][z] &&
                        distField[x][y][z] >= distField[x][y+1][z] &&
                        distField[x][y][z] >= distField[x][y][z-1] &&
                        distField[x][y][z] >= distField[x][y][z+1]
                    ){
                        localMaximums.add(new Vector3i(x,y,z));
                    }
                }
            }
        }

        //vectors used across scanning
        Vector3d selectionStart = new Vector3d();

        while(localMaximums.size() > 0){
            Vector3i toConsider = localMaximums.poll();
            selectionStart.set(boundingArea.getRectStart()).add(
                toConsider.x * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                toConsider.y * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                toConsider.z * BlockChunkData.BLOCK_SIZE_MULTIPLIER
            );

            //make sure it's not already interecting any solved rooms
            boolean contained = false;
            for(RoomMetadata room : structureMetadata.getRooms()){
                if(room.getArea().containsPoint(selectionStart)){
                    contained = true;
                    break;
                }
            }
            if(contained){
                continue;
            }

            //don't consider edge points
            if(
                toConsider.x == 0 || toConsider.y == 0 || toConsider.z == 0 ||
                toConsider.x == dimX || toConsider.y == dimY || toConsider.z == dimZ
            ){
                continue;
            }


            //Calculate the cavity
            Vector3i roomCenterChunkPos = ClientWorldData.convertRealToChunkSpace(selectionStart);
            Vector3i roomCenterBlockPos = ClientWorldData.convertRealToLocalBlockSpace(selectionStart);
            AreaSelection roomArea = AreaSelection.selectRectangularBlockCavity(roomCenterChunkPos, roomCenterBlockPos, MAX_ROOM_SIZE);

            //check if outside bounds of map
            if(roomArea.getRectStart().y <= 0){
                continue;
            }

            //scan along the floor and make sure we have voxels underneath each position
            boolean floorIsValid = true;
            int floorDimX = (int)((roomArea.getRectEnd().x - roomArea.getRectStart().x) * BlockChunkData.BLOCKS_PER_UNIT_DISTANCE);
            int floorDimZ = (int)((roomArea.getRectEnd().z - roomArea.getRectStart().z) * BlockChunkData.BLOCKS_PER_UNIT_DISTANCE);
            for(int x = 0; x < floorDimX; x++){
                for(int z = 0; z < floorDimZ; z++){
                    selectionStart.set(roomArea.getRectStart()).add(
                        floorDimX * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                        -1 * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                        floorDimZ * BlockChunkData.BLOCK_SIZE_MULTIPLIER
                    );
                    Vector3i floorChunkPos = ClientWorldData.convertRealToChunkSpace(selectionStart);
                    Vector3i floorBlockPos = ClientWorldData.convertRealToLocalBlockSpace(selectionStart);
                    BlockChunkData chunkData = Globals.clientState.clientBlockManager.getChunkDataAtWorldPoint(floorChunkPos, BlockChunkData.LOD_FULL_RES);
                    short type = chunkData.getType(floorBlockPos.x, floorBlockPos.y, floorBlockPos.z);
                    if(!RoomSolver.isFloor(type)){
                        floorIsValid = false;
                        break;
                    }
                }
                if(!floorIsValid){
                    break;
                }
            }
            if(!floorIsValid){
                continue;
            }

            //scan for entrances to the room
            List<Vector3d> entryPoints = RoomSolver.scanForEntrances(roomArea.getRectStart(), floorDimX, floorDimZ);
            if(entryPoints.size() < 1){
                continue;
            }

            RoomMetadata data = new RoomMetadata(roomArea);
            data.setEntryPoints(entryPoints);
            structureMetadata.getRooms().add(data);

            //scan for furniture slots
            List<FurnitureSlotMetadata> furnitureSlots = FurnitureSolver.solveFurnitureSpots(data, LayoutType.WALL_ALIGNED);
            data.setFurnitureSlots(furnitureSlots);
        }

    }

    /**
     * Scans the room for entrances
     * @param roomStart The start point of the room
     * @param floorDimX The x dimension of the floor
     * @param floorDimZ The z dimension of the floor
     */
    private static List<Vector3d> scanForEntrances(Vector3d roomStart, int floorDimX, int floorDimZ){
        List<Vector3d> rVal = new LinkedList<Vector3d>();

        Vector3d tempVec = new Vector3d();
        //scan for entrances to the room
        int lastSolidBlock = 0;

        //scan negative z dir
        for(int x = 0; x < floorDimX; x++){
            tempVec.set(roomStart).add(
                x * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                0,
                -1 * BlockChunkData.BLOCK_SIZE_MULTIPLIER
            );
            Vector3i floorChunkPos = ClientWorldData.convertRealToChunkSpace(tempVec);
            Vector3i floorBlockPos = ClientWorldData.convertRealToLocalBlockSpace(tempVec);
            BlockChunkData chunkData = Globals.clientState.clientBlockManager.getChunkDataAtWorldPoint(floorChunkPos, BlockChunkData.LOD_FULL_RES);
            short type = chunkData.getType(floorBlockPos.x, floorBlockPos.y, floorBlockPos.z);
            if(RoomSolver.isWall(type)){
                int dist = (x - lastSolidBlock);
                if(dist >= DOORWAY_MIN_WIDTH){
                    //scan for doorway from here
                    boolean foundDoor = true;
                    for(int z = lastSolidBlock + 1; z < x; z++){
                        for(int y = 0; y < DOORWAY_MIN_HEIGHT; y++){
                            tempVec.set(roomStart).add(
                                z * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                                y * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                                -1 * BlockChunkData.BLOCK_SIZE_MULTIPLIER
                            );
                            floorChunkPos = ClientWorldData.convertRealToChunkSpace(tempVec);
                            floorBlockPos = ClientWorldData.convertRealToLocalBlockSpace(tempVec);
                            chunkData = Globals.clientState.clientBlockManager.getChunkDataAtWorldPoint(floorChunkPos, BlockChunkData.LOD_FULL_RES);
                            type = chunkData.getType(floorBlockPos.x, floorBlockPos.y, floorBlockPos.z);
                            if(!RoomSolver.isDoorway(type)){
                                foundDoor = false;
                                break;
                            }
                        }
                        if(!foundDoor){
                            break;
                        }
                    }
                    if(foundDoor){
                        rVal.add(new Vector3d(roomStart).add(
                            (lastSolidBlock + dist / 2.0) * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                            0,
                            0
                        ));
                    }
                }
                lastSolidBlock = x;
            }
        }

        //scan positive z dir
        lastSolidBlock = 0;
        for(int x = 0; x < floorDimX; x++){
            tempVec.set(roomStart).add(
                x * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                0,
                floorDimZ * BlockChunkData.BLOCK_SIZE_MULTIPLIER
            );
            Vector3i floorChunkPos = ClientWorldData.convertRealToChunkSpace(tempVec);
            Vector3i floorBlockPos = ClientWorldData.convertRealToLocalBlockSpace(tempVec);
            BlockChunkData chunkData = Globals.clientState.clientBlockManager.getChunkDataAtWorldPoint(floorChunkPos, BlockChunkData.LOD_FULL_RES);
            short type = chunkData.getType(floorBlockPos.x, floorBlockPos.y, floorBlockPos.z);
            if(RoomSolver.isWall(type)){
                int dist = (x - lastSolidBlock);
                if(dist >= DOORWAY_MIN_WIDTH){
                    //scan for doorway from here
                    boolean foundDoor = true;
                    for(int z = lastSolidBlock + 1; z < x; z++){
                        for(int y = 0; y < DOORWAY_MIN_HEIGHT; y++){
                            tempVec.set(roomStart).add(
                                z * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                                y * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                                floorDimZ * BlockChunkData.BLOCK_SIZE_MULTIPLIER
                            );
                            floorChunkPos = ClientWorldData.convertRealToChunkSpace(tempVec);
                            floorBlockPos = ClientWorldData.convertRealToLocalBlockSpace(tempVec);
                            chunkData = Globals.clientState.clientBlockManager.getChunkDataAtWorldPoint(floorChunkPos, BlockChunkData.LOD_FULL_RES);
                            type = chunkData.getType(floorBlockPos.x, floorBlockPos.y, floorBlockPos.z);
                            if(!RoomSolver.isDoorway(type)){
                                foundDoor = false;
                                break;
                            }
                        }
                        if(!foundDoor){
                            break;
                        }
                    }
                    if(foundDoor){
                        rVal.add(new Vector3d(roomStart).add(
                            (lastSolidBlock + dist / 2.0) * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                            0,
                            (floorDimZ - 1) * BlockChunkData.BLOCK_SIZE_MULTIPLIER
                        ));
                    }
                }
                lastSolidBlock = x;
            }
        }

        //scan negative x dir
        lastSolidBlock = 0;
        for(int x = 0; x < floorDimZ; x++){
            tempVec.set(roomStart).add(
                -1 * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                0,
                x * BlockChunkData.BLOCK_SIZE_MULTIPLIER
            );
            Vector3i floorChunkPos = ClientWorldData.convertRealToChunkSpace(tempVec);
            Vector3i floorBlockPos = ClientWorldData.convertRealToLocalBlockSpace(tempVec);
            BlockChunkData chunkData = Globals.clientState.clientBlockManager.getChunkDataAtWorldPoint(floorChunkPos, BlockChunkData.LOD_FULL_RES);
            short type = chunkData.getType(floorBlockPos.x, floorBlockPos.y, floorBlockPos.z);
            if(RoomSolver.isWall(type)){
                int dist = (x - lastSolidBlock);
                if(dist >= DOORWAY_MIN_WIDTH){
                    //scan for doorway from here
                    boolean foundDoor = true;
                    for(int z = lastSolidBlock + 1; z < x; z++){
                        for(int y = 0; y < DOORWAY_MIN_HEIGHT; y++){
                            tempVec.set(roomStart).add(
                                -1 * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                                y * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                                z * BlockChunkData.BLOCK_SIZE_MULTIPLIER
                            );
                            floorChunkPos = ClientWorldData.convertRealToChunkSpace(tempVec);
                            floorBlockPos = ClientWorldData.convertRealToLocalBlockSpace(tempVec);
                            chunkData = Globals.clientState.clientBlockManager.getChunkDataAtWorldPoint(floorChunkPos, BlockChunkData.LOD_FULL_RES);
                            type = chunkData.getType(floorBlockPos.x, floorBlockPos.y, floorBlockPos.z);
                            if(!RoomSolver.isDoorway(type)){
                                foundDoor = false;
                                break;
                            }
                        }
                        if(!foundDoor){
                            break;
                        }
                    }
                    if(foundDoor){
                        rVal.add(new Vector3d(roomStart).add(
                            0,
                            0,
                            (lastSolidBlock + dist / 2.0) * BlockChunkData.BLOCK_SIZE_MULTIPLIER
                        ));
                    }
                }
                lastSolidBlock = x;
            }
        }

        //scan positive x dir
        lastSolidBlock = 0;
        for(int x = 0; x < floorDimZ; x++){
            tempVec.set(roomStart).add(
                floorDimX * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                0,
                x * BlockChunkData.BLOCK_SIZE_MULTIPLIER
            );
            Vector3i floorChunkPos = ClientWorldData.convertRealToChunkSpace(tempVec);
            Vector3i floorBlockPos = ClientWorldData.convertRealToLocalBlockSpace(tempVec);
            BlockChunkData chunkData = Globals.clientState.clientBlockManager.getChunkDataAtWorldPoint(floorChunkPos, BlockChunkData.LOD_FULL_RES);
            short type = chunkData.getType(floorBlockPos.x, floorBlockPos.y, floorBlockPos.z);
            if(RoomSolver.isWall(type)){
                int dist = (x - lastSolidBlock);
                if(dist >= DOORWAY_MIN_WIDTH){
                    //scan for doorway from here
                    boolean foundDoor = true;
                    for(int z = lastSolidBlock + 1; z < x; z++){
                        for(int y = 0; y < DOORWAY_MIN_HEIGHT; y++){
                            tempVec.set(roomStart).add(
                                floorDimX * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                                y * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                                z * BlockChunkData.BLOCK_SIZE_MULTIPLIER
                            );
                            floorChunkPos = ClientWorldData.convertRealToChunkSpace(tempVec);
                            floorBlockPos = ClientWorldData.convertRealToLocalBlockSpace(tempVec);
                            chunkData = Globals.clientState.clientBlockManager.getChunkDataAtWorldPoint(floorChunkPos, BlockChunkData.LOD_FULL_RES);
                            type = chunkData.getType(floorBlockPos.x, floorBlockPos.y, floorBlockPos.z);
                            if(!RoomSolver.isDoorway(type)){
                                foundDoor = false;
                                break;
                            }
                        }
                        if(!foundDoor){
                            break;
                        }
                    }
                    if(foundDoor){
                        rVal.add(new Vector3d(roomStart).add(
                            (floorDimX - 1) * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                            0,
                            (lastSolidBlock + dist / 2.0) * BlockChunkData.BLOCK_SIZE_MULTIPLIER
                        ));
                    }
                }
                lastSolidBlock = x;
            }
        }

        return rVal;
    }

    /**
     * Checks if a block is a valid floor block
     * @param type The type of block
     * @return true if it is a valid floor block, false otherwise
     */
    public static boolean isFloor(short type){
        return type != BlockChunkData.BLOCK_TYPE_EMPTY;
    }

    /**
     * Checks if a block is a valid wall block
     * @param type The type of block
     * @return true if it is a valid wall block, false otherwise
     */
    public static boolean isWall(short type){
        return type != BlockChunkData.BLOCK_TYPE_EMPTY;
    }

    /**
     * Checks if a block is a valid dooway block
     * @param type The type of block
     * @return true if is a valid doorway block, false otherwise
     */
    public static boolean isDoorway(short type){
        return type == BlockChunkData.BLOCK_TYPE_EMPTY;
    }

}
