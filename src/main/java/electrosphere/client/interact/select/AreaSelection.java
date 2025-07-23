package electrosphere.client.interact.select;

import org.graalvm.polyglot.HostAccess.Export;
import org.joml.AABBd;
import org.joml.Sphered;
import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.client.block.BlockChunkData;
import electrosphere.engine.Globals;

/**
 * An area of space that is selected by the client
 */
public class AreaSelection {
    
    /**
     * The type of selection
     */
    public static enum AreaSelectionType {
        /**
         * A rectangle
         */
        RECTANGULAR,
    }

    /**
     * Default radius to select
     */
    public static final int DEFAULT_SELECTION_RADIUS = 10;

    /**
     * The type of selection
     */
    private AreaSelectionType type;

    /**
     * The start point of the rectangular selection
     */
    private Vector3d rectStart;

    /**
     * The end point of the rectangular selection
     */
    private Vector3d rectEnd;

    /**
     * The AABB of the area selection
     */
    private AABBd aabb;

    /**
     * Private constructor
     */
    private AreaSelection(){ }

    /**
     * Creates a rectangular selection
     * @param start The start point
     * @param end The end point
     * @return The selection
     */
    public static AreaSelection createRect(Vector3d start, Vector3d end){
        if(start.x > end.x){
            throw new Error("Start x is less than end x! " + start.x + " " + end.x);
        }
        if(start.y > end.y){
            throw new Error("Start y is less than end y! " + start.y + " " + end.y);
        }
        if(start.z > end.z){
            throw new Error("Start y is less than end y! " + start.z + " " + end.z);
        }
        AreaSelection rVal = new AreaSelection();
        rVal.type = AreaSelectionType.RECTANGULAR;
        rVal.rectStart = start;
        rVal.rectEnd = end;
        rVal.aabb = new AABBd(start, end);
        return rVal;
    }

    /**
     * Calculates the rectangular area of empty voxels in the block chunks starting at a given position
     * @param chunkPos The chunk position to start from
     * @param blockPos The block position to start from
     * @param maxRadius The maximum radius to expand the area to
     * @return The AreaSelection
     */
    public static AreaSelection selectRectangularBlockCavity(Vector3i chunkPos, Vector3i blockPos, int maxRadius){
        AreaSelection rVal = null;
        
        Vector3i startOffset = new Vector3i(0,0,0);
        Vector3i endOffset = new Vector3i(1,1,1);
        int increment = 0;
        boolean expandPositiveX = true;
        boolean expandPositiveY = true;
        boolean expandPositiveZ = true;
        boolean expandNegativeX = true;
        boolean expandNegativeY = true;
        boolean expandNegativeZ = true;
        Vector3i currVoxelPos = new Vector3i();
        Vector3i currChunkPos = new Vector3i();
        while(
            startOffset.x > -maxRadius && startOffset.y > -maxRadius && startOffset.z > -maxRadius &&
            endOffset.x < maxRadius && endOffset.y < maxRadius && endOffset.z < maxRadius && 
            (
            expandPositiveX || expandPositiveY || expandPositiveZ ||
            expandNegativeX || expandNegativeY || expandNegativeZ
            )){
            increment++;
            switch(increment % 6){
                case 0: {
                    if(expandPositiveX){
                        endOffset.x = endOffset.x + 1;
                    } else {
                        continue;
                    }
                } break;
                case 1: {
                    if(expandPositiveY){
                        endOffset.y = endOffset.y + 1;
                    } else {
                        continue;
                    }
                } break;
                case 2: {
                    if(expandPositiveZ){
                        endOffset.z = endOffset.z + 1;
                    } else {
                        continue;
                    }
                } break;
                case 3: {
                    if(expandNegativeX){
                        startOffset.x = startOffset.x - 1;
                    } else {
                        continue;
                    }
                } break;
                case 4: {
                    if(expandNegativeY){
                        startOffset.y = startOffset.y - 1;
                    } else {
                        continue;
                    }
                } break;
                case 5: {
                    if(expandNegativeZ){
                        startOffset.z = startOffset.z - 1;
                    } else {
                        continue;
                    }
                } break;
            }
            for(int x = startOffset.x; x < endOffset.x; x++){
                for(int y = startOffset.y; y < endOffset.y; y++){
                    for(int z = startOffset.z; z < endOffset.z; z++){
                        currVoxelPos.set(blockPos).add(x,y,z);
                        currChunkPos.set(chunkPos).add(
                            currVoxelPos.x / BlockChunkData.CHUNK_DATA_WIDTH,
                            currVoxelPos.y / BlockChunkData.CHUNK_DATA_WIDTH,
                            currVoxelPos.z / BlockChunkData.CHUNK_DATA_WIDTH
                        );
                        currVoxelPos.set(
                            currVoxelPos.x % BlockChunkData.CHUNK_DATA_WIDTH,
                            currVoxelPos.y % BlockChunkData.CHUNK_DATA_WIDTH,
                            currVoxelPos.z % BlockChunkData.CHUNK_DATA_WIDTH
                        );
                        if(!Globals.clientState.clientWorldData.chunkInBounds(currChunkPos)){
                            switch(increment % 6){
                                case 0: {
                                    if(expandPositiveX){
                                        expandPositiveX = false;
                                        if(endOffset.x > 1){
                                            endOffset.x--;
                                        }
                                    }
                                } break;
                                case 1: {
                                    if(expandPositiveY){
                                        expandPositiveY = false;
                                        if(endOffset.y > 1){
                                            endOffset.y--;
                                        }
                                    }
                                } break;
                                case 2: {
                                    if(expandPositiveZ){
                                        expandPositiveZ = false;
                                        if(endOffset.z > 1){
                                            endOffset.z--;
                                        }
                                    }
                                } break;
                                case 3: {
                                    if(expandNegativeX){
                                        expandNegativeX = false;
                                        if(startOffset.x < 0){
                                            startOffset.x++;
                                        }
                                    }
                                } break;
                                case 4: {
                                    if(expandNegativeY){
                                        expandNegativeY = false;
                                        if(startOffset.y < 0){
                                            startOffset.y++;
                                        }
                                    }
                                } break;
                                case 5: {
                                    if(expandNegativeZ){
                                        expandNegativeZ = false;
                                        if(startOffset.z < 0){
                                            startOffset.z++;
                                        }
                                    }
                                } break;
                            }
                            continue;
                        }
                        BlockChunkData chunkData = Globals.clientState.clientBlockManager.getChunkDataAtWorldPoint(currChunkPos, BlockChunkData.LOD_FULL_RES);
                        if(chunkData == null){
                            switch(increment % 6){
                                case 0: {
                                    if(expandPositiveX){
                                        expandPositiveX = false;
                                        if(endOffset.x > 1){
                                            endOffset.x--;
                                        }
                                    }
                                } break;
                                case 1: {
                                    if(expandPositiveY){
                                        expandPositiveY = false;
                                        if(endOffset.y > 1){
                                            endOffset.y--;
                                        }
                                    }
                                } break;
                                case 2: {
                                    if(expandPositiveZ){
                                        expandPositiveZ = false;
                                        if(endOffset.z > 1){
                                            endOffset.z--;
                                        }
                                    }
                                } break;
                                case 3: {
                                    if(expandNegativeX){
                                        expandNegativeX = false;
                                        if(startOffset.x < 0){
                                            startOffset.x++;
                                        }
                                    }
                                } break;
                                case 4: {
                                    if(expandNegativeY){
                                        expandNegativeY = false;
                                        if(startOffset.y < 0){
                                            startOffset.y++;
                                        }
                                    }
                                } break;
                                case 5: {
                                    if(expandNegativeZ){
                                        expandNegativeZ = false;
                                        if(startOffset.z < 0){
                                            startOffset.z++;
                                        }
                                    }
                                } break;
                            }
                            continue;
                        }
                        while(currVoxelPos.x < 0){
                            currVoxelPos.x = currVoxelPos.x + BlockChunkData.CHUNK_DATA_WIDTH;
                        }
                        while(currVoxelPos.y < 0){
                            currVoxelPos.y = currVoxelPos.y + BlockChunkData.CHUNK_DATA_WIDTH;
                        }
                        while(currVoxelPos.z < 0){
                            currVoxelPos.z = currVoxelPos.z + BlockChunkData.CHUNK_DATA_WIDTH;
                        }
                        if(!chunkData.isEmpty(currVoxelPos)){
                            switch(increment % 6){
                                case 0: {
                                    if(expandPositiveX){
                                        expandPositiveX = false;
                                        if(endOffset.x > 1){
                                            endOffset.x--;
                                        }
                                    }
                                } break;
                                case 1: {
                                    if(expandPositiveY){
                                        expandPositiveY = false;
                                        if(endOffset.y > 1){
                                            endOffset.y--;
                                        }
                                    }
                                } break;
                                case 2: {
                                    if(expandPositiveZ){
                                        expandPositiveZ = false;
                                        if(endOffset.z > 1){
                                            endOffset.z--;
                                        }
                                    }
                                } break;
                                case 3: {
                                    if(expandNegativeX){
                                        expandNegativeX = false;
                                        if(startOffset.x < 0){
                                            startOffset.x++;
                                        }
                                    }
                                } break;
                                case 4: {
                                    if(expandNegativeY){
                                        expandNegativeY = false;
                                        if(startOffset.y < 0){
                                            startOffset.y++;
                                        }
                                    }
                                } break;
                                case 5: {
                                    if(expandNegativeZ){
                                        expandNegativeZ = false;
                                        if(startOffset.z < 0){
                                            startOffset.z++;
                                        }
                                    }
                                } break;
                            }
                        }
                    }
                }
            }
        }

        Vector3d startPos = new Vector3d(Globals.clientState.clientWorldData.convertBlockToRealSpace(chunkPos, blockPos))
        .add(
            startOffset.x * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
            startOffset.y * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
            startOffset.z * BlockChunkData.BLOCK_SIZE_MULTIPLIER
        );
        Vector3d endPos = new Vector3d(Globals.clientState.clientWorldData.convertBlockToRealSpace(chunkPos, blockPos))
        .add(
            endOffset.x * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
            endOffset.y * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
            endOffset.z * BlockChunkData.BLOCK_SIZE_MULTIPLIER
        );

        rVal = AreaSelection.createRect(startPos, endPos);

        return rVal;
    }

    /**
     * Gets the type of area
     * @return The type of area
     */
    public AreaSelectionType getType() {
        return type;
    }

    /**
     * Gets the start point of the rectangular selection
     * @return The start point
     */
    @Export
    public Vector3d getRectStart() {
        return rectStart;
    }

    /**
     * Gets the end point of the rectangular selection
     * @return The end point
     */
    @Export
    public Vector3d getRectEnd() {
        return rectEnd;
    }

    /**
     * Checks if the area contains a point
     * @param point The point
     * @return true if it contains the point, false otherwise
     */
    public boolean containsPoint(Vector3d point){
        return aabb.testPoint(point);
    }

    /**
     * Checks if this area intersects another area
     * @param other The other area
     * @return true if one intersects another, false otherwise
     */
    public boolean intersects(AreaSelection other){
        if(this.type != AreaSelectionType.RECTANGULAR || other.type != AreaSelectionType.RECTANGULAR){
            throw new Error("One of the areas to test is not rectangular! " + this.type + " " + other.type);
        }
        return aabb.testAABB(other.aabb);
    }

    /**
     * Checks if this area intersects a sphere
     * @param sphere The sphere to check
     * @return true if intersects the sphere, false otherwise
     */
    public boolean intersects(Sphered sphere){
        if(this.type != AreaSelectionType.RECTANGULAR){
            throw new Error("One of the areas to test is not rectangular! " + this.type);
        }
        return aabb.testSphere(sphere.x, sphere.y, sphere.z, sphere.r);
    }

}
