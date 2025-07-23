package electrosphere.server.pathfinding.voxel;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.client.block.BlockChunkData;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.datacell.interfaces.VoxelCellManager;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import electrosphere.util.math.HashUtils;

/**
 * finds a path between two points given a voxel manager
 */
public class VoxelPathfinder {

    /**
     * Maximum distance to allow searching
     */
    public static final double MAX_DIST = 1000;

    /**
     * Default maximum cost to tolerate
     */
    public static final int DEFAULT_MAX_COST = 12000;

    /**
     * Maximum number of blocks for a walkable position
     */
    static final int MAX_BLOCKS_FOR_WALKABLE = 0;

    /**
     * Maximum distance to scan for a walkable position
     */
    public static final double DEFAULT_MAX_TARGET_SCAN_DIST = 5;

    /**
     * The heuristic lookup table
     */
    private int[][][] heuristic = new int[3][3][3];
    
    /**
     * Finds a path between two points given a voxel cell manager
     * @param voxelCellManager The voxel cell manager
     * @param startPoint The start point
     * @param endPoint The end point
     * @param maxDist
     * @return The path if it is solvable, null otherwise
     */
    public List<Vector3d> findPath(VoxelCellManager voxelCellManager, Vector3d startPoint, Vector3d endPoint, long maxCost){
        List<Vector3d> rVal = null;

        if(startPoint == null || endPoint == null){
            throw new Error("Points undefined! " + startPoint + " " + endPoint);
        }
        if(startPoint.distance(endPoint) > MAX_DIST){
            throw new Error("Distance is outside range provided! " + startPoint.distance(endPoint) + " vs " + MAX_DIST);
        }

        //create sets
        PriorityQueue<PathfinderNode> openSet = new PriorityQueue<PathfinderNode>();
        Map<Long,PathfinderNode> openSetLookup = new HashMap<Long,PathfinderNode>();
        Map<Long,PathfinderNode> closetSet = new HashMap<Long,PathfinderNode>();

        //add starting node
        PathfinderNode startingNode = new PathfinderNode(
            ServerWorldData.convertRealToChunkSpace(startPoint.x), ServerWorldData.convertRealToChunkSpace(startPoint.y), ServerWorldData.convertRealToChunkSpace(startPoint.z),
            ServerWorldData.convertRealToVoxelSpace(startPoint.x), ServerWorldData.convertRealToVoxelSpace(startPoint.y), ServerWorldData.convertRealToVoxelSpace(startPoint.z),
            0, 0, 0
        );
        openSet.add(startingNode);

        //structures used throughout iteration
        Vector3i worldPos = new Vector3i();
        Vector3i voxelPos = new Vector3i();

        worldPos.set(ServerWorldData.convertRealToChunkSpace(endPoint));
        voxelPos.set(ServerWorldData.convertRealToVoxelSpace(endPoint));
        long goalHash = HashUtils.hashVoxel(
            worldPos.x,worldPos.y,worldPos.z,
            voxelPos.x,voxelPos.y,voxelPos.z
        );

        //set heuristic
        this.setHeuristic(startPoint, endPoint);

        //main A* loop
        this.aStar(voxelCellManager, openSet, openSetLookup, closetSet, endPoint, goalHash, maxCost);

        //string pulling
        List<PathfinderNode> stringPulled = this.stringPull(voxelCellManager, closetSet, goalHash);

        rVal = stringPulled.stream().map(
            (PathfinderNode node) -> {
                Vector3i currWorld = new Vector3i(
                    node.worldX,
                    node.worldY,
                    node.worldZ
                );
                Vector3i currVoxel = new Vector3i(
                    node.voxelX,
                    node.voxelY,
                    node.voxelZ
                );
                float weight = voxelCellManager.getVoxelWeightAtLocalPosition(currWorld,currVoxel);
                // int type = voxelCellManager.getVoxelTypeAtLocalPosition(currWorld,currVoxel);
                if(weight < 0){
                    weight = 0;
                }
                currVoxel.y++;
                float aboveWeight = voxelCellManager.getVoxelWeightAtLocalPosition(currWorld,currVoxel);
                float total = (weight - aboveWeight);
                float percentage = weight / total;
                return new Vector3d(
                    ServerWorldData.convertVoxelToRealSpace(node.voxelX, node.worldX),
                    ServerWorldData.convertVoxelToRealSpace(node.voxelY, node.worldY) + percentage,
                    ServerWorldData.convertVoxelToRealSpace(node.voxelZ, node.worldZ)
                );
            }
        ).collect(Collectors.toList());

        return rVal;
    }


    /**
     * Sets the heuristic
     * @param start The start pos
     * @param end The end pos
     */
    private void setHeuristic(Vector3d start, Vector3d end){
        //clear out array
        for(int x = 0; x < 3; x++){
            for(int y = 0; y < 3; y++){
                for(int z = 0; z < 3; z++){
                    this.heuristic[x][y][z] = 1;
                }
            }
        }


        if(start.x > end.x){
            for(int x = 0; x < 3; x++){
                for(int y = 0; y < 3; y++){
                    this.heuristic[0][x][y] = this.heuristic[0][x][y] + 1;
                    this.heuristic[1][x][y] = this.heuristic[1][x][y] + 3;
                    this.heuristic[2][x][y] = this.heuristic[2][x][y] + 5;
                }
            }
        } else {
            for(int x = 0; x < 3; x++){
                for(int y = 0; y < 3; y++){
                    this.heuristic[0][x][y] = this.heuristic[0][x][y] + 5;
                    this.heuristic[1][x][y] = this.heuristic[1][x][y] + 3;
                    this.heuristic[2][x][y] = this.heuristic[2][x][y] + 1;
                }
            }
        }

        if(start.y > end.y){
            for(int x = 0; x < 3; x++){
                for(int y = 0; y < 3; y++){
                    this.heuristic[x][0][y] = this.heuristic[x][0][y] + 1;
                    this.heuristic[x][1][y] = this.heuristic[x][1][y] + 3;
                    this.heuristic[x][2][y] = this.heuristic[x][2][y] + 5;
                }
            }
        } else {
            for(int x = 0; x < 3; x++){
                for(int y = 0; y < 3; y++){
                    this.heuristic[x][0][y] = this.heuristic[x][0][y] + 5;
                    this.heuristic[x][1][y] = this.heuristic[x][1][y] + 3;
                    this.heuristic[x][2][y] = this.heuristic[x][2][y] + 1;
                }
            }
        }

        if(start.z > end.z){
            for(int x = 0; x < 3; x++){
                for(int y = 0; y < 3; y++){
                    this.heuristic[x][y][0] = this.heuristic[x][y][0] + 1;
                    this.heuristic[x][y][1] = this.heuristic[x][y][1] + 3;
                    this.heuristic[x][y][2] = this.heuristic[x][y][2] + 5;
                }
            }
        } else {
            for(int x = 0; x < 3; x++){
                for(int y = 0; y < 3; y++){
                    this.heuristic[x][y][0] = this.heuristic[x][y][0] + 5;
                    this.heuristic[x][y][1] = this.heuristic[x][y][1] + 3;
                    this.heuristic[x][y][2] = this.heuristic[x][y][2] + 1;
                }
            }
        }


    }

    /**
     * Solves for the A* closed set
     * @param voxelCellManager The voxel manager
     * @param openSet The open set
     * @param openSetLookup The open set lookup table
     * @param closetSet The closet set
     * @param goalPos The goal position
     * @param goalHash The goal hash
     * @param maxCost The max allowable cost
     */
    private void aStar(
        VoxelCellManager voxelCellManager,
        PriorityQueue<PathfinderNode> openSet,
        Map<Long,PathfinderNode> openSetLookup,
        Map<Long,PathfinderNode> closetSet,
        Vector3d goalPos,
        long goalHash,
        long maxCost
    ){
        //tracks whether we've found the goal or not
        boolean foundGoal = false;
        int countConsidered = 0;

        int chunkPosX = 0;
        int chunkPosY = 0;
        int chunkPosZ = 0;

        int voxelPosX = 0;
        int voxelPosY = 0;
        int voxelPosZ = 0;

        while(openSet.size() > 0 && !foundGoal){

            //pull from open set
            PathfinderNode currentNode = openSet.poll();
            long currentCost = currentNode.cost;
            openSetLookup.remove(currentNode.hash);
            closetSet.put(currentNode.hash, currentNode);
            countConsidered++;



            //scan all neighbors
            for(int x = -1; x <= 1; x++){
                if(foundGoal){
                    continue;
                }
                for(int y = -1; y <= 1; y++){
                    if(foundGoal){
                        continue;
                    }
                    for(int z = -1; z <= 1; z++){
                        if(foundGoal){
                            continue;
                        }
                        if(x == 0 && y == 0 && z == 0){
                            continue;
                        }

                        //calculate chunk offsets
                        voxelPosX = (currentNode.voxelX + x);
                        voxelPosY = (currentNode.voxelY + y);
                        voxelPosZ = (currentNode.voxelZ + z);
                        if(voxelPosX < 0){
                            voxelPosX = -1;
                        } else {
                            voxelPosX = voxelPosX / ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
                        }
                        if(voxelPosY < 0){
                            voxelPosY = -1;
                        } else {
                            voxelPosY = voxelPosY / ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
                        }
                        if(voxelPosZ < 0){
                            voxelPosZ = -1;
                        } else {
                            voxelPosZ = voxelPosZ / ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
                        }
                        //update world position
                        chunkPosX = currentNode.worldX + voxelPosX;
                        chunkPosY = currentNode.worldY + voxelPosY;
                        chunkPosZ = currentNode.worldZ + voxelPosZ;
                        voxelPosX = (currentNode.voxelX + x + ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET) % ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
                        voxelPosY = (currentNode.voxelY + y + ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET) % ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
                        voxelPosZ = (currentNode.voxelZ + z + ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET) % ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;

                        //error/bounds check
                        if(chunkPosX < 0 || chunkPosY < 0 || chunkPosZ < 0){
                            continue;
                        }


                        //
                        //checking if this is the goal
                        //

                        //calculte hash for neighbor pos
                        long newHash = HashUtils.hashVoxel(
                            chunkPosX,chunkPosY,chunkPosZ,
                            voxelPosX,voxelPosY,voxelPosZ
                        );

                        //check if found goal
                        if(newHash == goalHash){
                            foundGoal = true;
                            PathfinderNode newNode = new PathfinderNode(
                                chunkPosX, chunkPosY, chunkPosZ,
                                voxelPosX, voxelPosY, voxelPosZ,
                                0, newHash, currentNode.hash
                            );
                            closetSet.put(goalHash, newNode);
                            continue;
                        }

                        //
                        //creating a new node
                        //

                        //it's a solid block
                        if(!this.isWalkable(voxelCellManager, new Vector3i(chunkPosX,chunkPosY,chunkPosZ), new Vector3i(voxelPosX,voxelPosY,voxelPosZ))){
                            continue;
                        }

                        //calculate new cost
                        //TODO: apply heuristic here
                        Vector3d currPosReal = new Vector3d(
                            ServerWorldData.convertVoxelToRealSpace(voxelPosX, chunkPosX),
                            ServerWorldData.convertVoxelToRealSpace(voxelPosY, chunkPosY),
                            ServerWorldData.convertVoxelToRealSpace(voxelPosZ, chunkPosZ)
                        );
                        long newCost = currentCost + (int)currPosReal.distance(goalPos);

                        //check cost boundary
                        if(newCost > maxCost){
                            continue;
                        }

                        //push to open set
                        if(!closetSet.containsKey(newHash) && !openSetLookup.containsKey(newHash)){
                            PathfinderNode newNode = new PathfinderNode(
                                chunkPosX, chunkPosY, chunkPosZ,
                                voxelPosX, voxelPosY, voxelPosZ,
                                newCost, newHash, currentNode.hash
                            );
                            openSet.add(newNode);
                            openSetLookup.put(newHash, newNode);
                        }
                    }
                }
            }

            if(openSet.size() < 1){
                throw new Error("Open set ran out of nodes! " + countConsidered);
            }
        }

        if(!foundGoal){
            throw new Error("Failed to find goal " + countConsidered);
        }
    }

    /**
     * Steps through the A* solver to a given number of closet set items
     * @param voxelCellManager The voxel manager
     * @param startPoint The start point
     * @param endPoint The end point
     * @param maxCost The max allowable cost
     * @param closetSetSize The size of the closed set to stop at
     * @return The closed set from iteration through A*
     */
    public List<PathfinderNode> aStarStep(VoxelCellManager voxelCellManager, Vector3d startPoint, Vector3d endPoint, long maxCost, int closetSetSize){
        if(startPoint.distance(endPoint) > MAX_DIST){
            throw new Error("Distance is outside range provided! " + startPoint.distance(endPoint) + " vs " + MAX_DIST);
        }

        List<PathfinderNode> rVal = new LinkedList<PathfinderNode>();

        //create sets
        PriorityQueue<PathfinderNode> openSet = new PriorityQueue<PathfinderNode>();
        Map<Long,PathfinderNode> openSetLookup = new HashMap<Long,PathfinderNode>();
        Map<Long,PathfinderNode> closetSet = new HashMap<Long,PathfinderNode>();

        //add starting node
        PathfinderNode startingNode = new PathfinderNode(
            ServerWorldData.convertRealToChunkSpace(startPoint.x), ServerWorldData.convertRealToChunkSpace(startPoint.y), ServerWorldData.convertRealToChunkSpace(startPoint.z),
            ServerWorldData.convertRealToVoxelSpace(startPoint.x), ServerWorldData.convertRealToVoxelSpace(startPoint.y), ServerWorldData.convertRealToVoxelSpace(startPoint.z),
            0, 0, 0
        );
        openSet.add(startingNode);

        //structures used throughout iteration
        int chunkPosX = 0;
        int chunkPosY = 0;
        int chunkPosZ = 0;

        int voxelPosX = 0;
        int voxelPosY = 0;
        int voxelPosZ = 0;

        Vector3i endWorldPos = ServerWorldData.convertRealToChunkSpace(endPoint);
        Vector3i endVoxelPos = ServerWorldData.convertRealToVoxelSpace(endPoint);

        long goalHash = HashUtils.hashVoxel(
            endWorldPos.x,endWorldPos.y,endWorldPos.z,
            endVoxelPos.x,endVoxelPos.y,endVoxelPos.z
        );

        //set heuristic
        this.setHeuristic(startPoint, endPoint);

        //tracks whether we've found the goal or not
        boolean foundGoal = false;
        int countConsidered = 0;

        while(openSet.size() > 0 && !foundGoal && rVal.size() < closetSetSize + 1){

            //pull from open set
            PathfinderNode currentNode = openSet.poll();
            long currentCost = currentNode.cost;
            openSetLookup.remove(currentNode.hash);
            closetSet.put(currentNode.hash, currentNode);
            rVal.add(currentNode);
            countConsidered++;



            //scan all neighbors
            for(int x = -1; x <= 1; x++){
                if(foundGoal){
                    continue;
                }
                for(int y = -1; y <= 1; y++){
                    if(foundGoal){
                        continue;
                    }
                    for(int z = -1; z <= 1; z++){
                        if(foundGoal){
                            continue;
                        }
                        if(x == 0 && y == 0 && z == 0){
                            continue;
                        }

                        //calculate chunk offsets
                        voxelPosX = (currentNode.voxelX + x);
                        voxelPosY = (currentNode.voxelY + y);
                        voxelPosZ = (currentNode.voxelZ + z);
                        if(voxelPosX < 0){
                            voxelPosX = -1;
                        } else {
                            voxelPosX = voxelPosX / ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
                        }
                        if(voxelPosY < 0){
                            voxelPosY = -1;
                        } else {
                            voxelPosY = voxelPosY / ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
                        }
                        if(voxelPosZ < 0){
                            voxelPosZ = -1;
                        } else {
                            voxelPosZ = voxelPosZ / ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
                        }
                        //update world position
                        chunkPosX = currentNode.worldX + voxelPosX;
                        chunkPosY = currentNode.worldY + voxelPosY;
                        chunkPosZ = currentNode.worldZ + voxelPosZ;
                        voxelPosX = (currentNode.voxelX + x + ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET) % ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
                        voxelPosY = (currentNode.voxelY + y + ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET) % ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
                        voxelPosZ = (currentNode.voxelZ + z + ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET) % ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;

                        //error/bounds check
                        if(chunkPosX < 0 || chunkPosY < 0 || chunkPosZ < 0){
                            continue;
                        }


                        //
                        //checking if this is the goal
                        //

                        //calculte hash for neighbor pos
                        long newHash = HashUtils.hashVoxel(
                            chunkPosX,chunkPosY,chunkPosZ,
                            voxelPosX,voxelPosY,voxelPosZ
                        );

                        //check if found goal
                        if(newHash == goalHash){
                            foundGoal = true;
                            PathfinderNode newNode = new PathfinderNode(
                                chunkPosX, chunkPosY, chunkPosZ,
                                voxelPosX, voxelPosY, voxelPosZ,
                                0, newHash, currentNode.hash
                            );
                            closetSet.put(goalHash, newNode);
                            rVal.add(currentNode);
                            continue;
                        }

                        //
                        //creating a new node
                        //

                        //it's a solid block
                        if(!this.isWalkable(voxelCellManager, new Vector3i(chunkPosX,chunkPosY,chunkPosZ), new Vector3i(voxelPosX,voxelPosY,voxelPosZ))){
                            continue;
                        }

                        //calculate new cost
                        //TODO: apply heuristic here
                        Vector3d currPosReal = new Vector3d(
                            ServerWorldData.convertVoxelToRealSpace(voxelPosX, chunkPosX),
                            ServerWorldData.convertVoxelToRealSpace(voxelPosY, chunkPosY),
                            ServerWorldData.convertVoxelToRealSpace(voxelPosZ, chunkPosZ)
                        );
                        long newCost = currentCost + (int)currPosReal.distance(endPoint);

                        //check cost boundary
                        if(newCost > maxCost){
                            continue;
                        }

                        //push to open set
                        if(!closetSet.containsKey(newHash) && !openSetLookup.containsKey(newHash)){
                            PathfinderNode newNode = new PathfinderNode(
                                chunkPosX, chunkPosY, chunkPosZ,
                                voxelPosX, voxelPosY, voxelPosZ,
                                newCost, newHash, currentNode.hash
                            );
                            openSet.add(newNode);
                            openSetLookup.put(newHash, newNode);
                        }
                    }
                }
            }

            if(openSet.size() < 1){
                throw new Error("Open set ran out of nodes! " + countConsidered);
            }
        }

        return rVal;
    }

    /**
     * Steps through the A* solver to a given number of closet set items
     * @param voxelCellManager The voxel manager
     * @param startPoint The start point
     * @param endPoint The end point
     * @param maxCost The max allowable cost
     * @param closetSetSize The size of the closed set to stop at
     * @return The closed set from iteration through A*
     */
    public List<PathfinderNode> aStarStepOpen(VoxelCellManager voxelCellManager, Vector3d startPoint, Vector3d endPoint, long maxCost, int closetSetSize){
        if(startPoint.distance(endPoint) > MAX_DIST){
            throw new Error("Distance is outside range provided! " + startPoint.distance(endPoint) + " vs " + MAX_DIST);
        }

        List<PathfinderNode> rVal = new LinkedList<PathfinderNode>();

        //create sets
        PriorityQueue<PathfinderNode> openSet = new PriorityQueue<PathfinderNode>();
        Map<Long,PathfinderNode> openSetLookup = new HashMap<Long,PathfinderNode>();
        Map<Long,PathfinderNode> closetSet = new HashMap<Long,PathfinderNode>();

        //add starting node
        PathfinderNode startingNode = new PathfinderNode(
            ServerWorldData.convertRealToChunkSpace(startPoint.x), ServerWorldData.convertRealToChunkSpace(startPoint.y), ServerWorldData.convertRealToChunkSpace(startPoint.z),
            ServerWorldData.convertRealToVoxelSpace(startPoint.x), ServerWorldData.convertRealToVoxelSpace(startPoint.y), ServerWorldData.convertRealToVoxelSpace(startPoint.z),
            0, 0, 0
        );
        openSet.add(startingNode);

        //structures used throughout iteration
        int chunkPosX = 0;
        int chunkPosY = 0;
        int chunkPosZ = 0;

        int voxelPosX = 0;
        int voxelPosY = 0;
        int voxelPosZ = 0;

        Vector3i endWorldPos = ServerWorldData.convertRealToChunkSpace(endPoint);
        Vector3i endVoxelPos = ServerWorldData.convertRealToVoxelSpace(endPoint);

        long goalHash = HashUtils.hashVoxel(
            endWorldPos.x,endWorldPos.y,endWorldPos.z,
            endVoxelPos.x,endVoxelPos.y,endVoxelPos.z
        );

        //set heuristic
        this.setHeuristic(startPoint, endPoint);

        //tracks whether we've found the goal or not
        boolean foundGoal = false;
        int countConsidered = 0;

        int iteration = 0;
        while(openSet.size() > 0 && !foundGoal && iteration < closetSetSize){

            //pull from open set
            PathfinderNode currentNode = openSet.poll();
            long currentCost = currentNode.cost;
            openSetLookup.remove(currentNode.hash);
            closetSet.put(currentNode.hash, currentNode);
            countConsidered++;



            //scan all neighbors
            for(int x = -1; x <= 1; x++){
                if(foundGoal){
                    continue;
                }
                for(int y = -1; y <= 1; y++){
                    if(foundGoal){
                        continue;
                    }
                    for(int z = -1; z <= 1; z++){
                        if(foundGoal){
                            continue;
                        }
                        if(x == 0 && y == 0 && z == 0){
                            continue;
                        }

                        //calculate chunk offsets
                        voxelPosX = (currentNode.voxelX + x);
                        voxelPosY = (currentNode.voxelY + y);
                        voxelPosZ = (currentNode.voxelZ + z);
                        if(voxelPosX < 0){
                            voxelPosX = -1;
                        } else {
                            voxelPosX = voxelPosX / ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
                        }
                        if(voxelPosY < 0){
                            voxelPosY = -1;
                        } else {
                            voxelPosY = voxelPosY / ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
                        }
                        if(voxelPosZ < 0){
                            voxelPosZ = -1;
                        } else {
                            voxelPosZ = voxelPosZ / ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
                        }
                        //update world position
                        chunkPosX = currentNode.worldX + voxelPosX;
                        chunkPosY = currentNode.worldY + voxelPosY;
                        chunkPosZ = currentNode.worldZ + voxelPosZ;
                        voxelPosX = (currentNode.voxelX + x + ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET) % ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
                        voxelPosY = (currentNode.voxelY + y + ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET) % ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
                        voxelPosZ = (currentNode.voxelZ + z + ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET) % ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;

                        //error/bounds check
                        if(chunkPosX < 0 || chunkPosY < 0 || chunkPosZ < 0){
                            continue;
                        }


                        //
                        //checking if this is the goal
                        //

                        //calculte hash for neighbor pos
                        long newHash = HashUtils.hashVoxel(
                            chunkPosX,chunkPosY,chunkPosZ,
                            voxelPosX,voxelPosY,voxelPosZ
                        );

                        //check if found goal
                        if(newHash == goalHash){
                            foundGoal = true;
                            PathfinderNode newNode = new PathfinderNode(
                                chunkPosX, chunkPosY, chunkPosZ,
                                voxelPosX, voxelPosY, voxelPosZ,
                                0, newHash, currentNode.hash
                            );
                            closetSet.put(goalHash, newNode);
                            continue;
                        }

                        //
                        //creating a new node
                        //

                        //it's a solid block
                        if(!this.isWalkable(voxelCellManager, new Vector3i(chunkPosX,chunkPosY,chunkPosZ), new Vector3i(voxelPosX,voxelPosY,voxelPosZ))){
                            continue;
                        }

                        //calculate new cost
                        //TODO: apply heuristic here
                        Vector3d currPosReal = new Vector3d(
                            ServerWorldData.convertVoxelToRealSpace(voxelPosX, chunkPosX),
                            ServerWorldData.convertVoxelToRealSpace(voxelPosY, chunkPosY),
                            ServerWorldData.convertVoxelToRealSpace(voxelPosZ, chunkPosZ)
                        );
                        long newCost = currentCost + (int)currPosReal.distance(endPoint);

                        //check cost boundary
                        if(newCost > maxCost){
                            continue;
                        }

                        //push to open set
                        if(!closetSet.containsKey(newHash) && !openSetLookup.containsKey(newHash)){
                            PathfinderNode newNode = new PathfinderNode(
                                chunkPosX, chunkPosY, chunkPosZ,
                                voxelPosX, voxelPosY, voxelPosZ,
                                newCost, newHash, currentNode.hash
                            );
                            openSet.add(newNode);
                            openSetLookup.put(newHash, newNode);
                        }
                    }
                }
            }

            if(openSet.size() < 1){
                throw new Error("Open set ran out of nodes! " + countConsidered);
            }

            iteration++;
        }

        rVal.addAll(openSet);

        return rVal;
    }

    /**
     * Performs string pulling on the closed set to get the optimized path
     * @param voxelCellManager The voxel cell manager
     * @param closetSet The closed set
     * @param goalHash The goal hash
     * @return The list of minimal points
     */
    private List<PathfinderNode> stringPull(
        VoxelCellManager voxelCellManager,
        Map<Long,PathfinderNode> closetSet,
        long goalHash
    ){
        List<PathfinderNode> rVal = new LinkedList<PathfinderNode>();
        //reverse the raw path
        List<PathfinderNode> pathRaw = new LinkedList<PathfinderNode>();
        PathfinderNode current = closetSet.get(goalHash);
        if(current == null){
            throw new Error("End node not stored in closed set!");
        }
        while(current.prevNode != current.hash){
            pathRaw.add(current);
            current = closetSet.get(current.prevNode);
            if(current == null){
                throw new Error("Node undefined!");
            }
        }
        Collections.reverse(pathRaw);

        int i = 0;
        while(i < pathRaw.size() - 1){
            rVal.add(pathRaw.get(i));
            int j = i + 1;
            for(; j < pathRaw.size() - 1; j++){
                PathfinderNode from = pathRaw.get(i);
                PathfinderNode to = pathRaw.get(j);
                if(this.hasLineOfSight(voxelCellManager, from, to)) {
                    break;
                }
            }
            i = j;
        }

        return rVal;
    }

    /**
     * Performs LOS checks for string pulling
     * @param voxelCellManager The voxel cell manager
     * @param from The cell to start string pulling from
     * @param to THe cell to start string pulling towards
     * @return true if we can keep string pulling, false otherwise
     */
    private boolean hasLineOfSight(
        VoxelCellManager voxelCellManager,
        PathfinderNode from,
        PathfinderNode to
    ){
        double x0 = ServerWorldData.convertVoxelToRealSpace(from.voxelX,from.worldX);
        double y0 = ServerWorldData.convertVoxelToRealSpace(from.voxelY,from.worldY);
        double z0 = ServerWorldData.convertVoxelToRealSpace(from.voxelZ,from.worldZ);

        double x1 = ServerWorldData.convertVoxelToRealSpace(to.voxelX,to.worldX);
        double y1 = ServerWorldData.convertVoxelToRealSpace(to.voxelY,to.worldY);
        double z1 = ServerWorldData.convertVoxelToRealSpace(to.voxelZ,to.worldZ);

        double dx = Math.abs(x1 - x0);
        double dy = Math.abs(y1 - x0);
        double dz = Math.abs(z1 - z0);
        int sx = Double.compare(x1, x0);
        int sy = Double.compare(y1, y0);
        int sz = Double.compare(z1, z0);
    
        Vector3d realPos = new Vector3d();
        double err1, err2;
        if(dx >= dy && dx >= dz){
            err1 = 2 * dy - dx;
            err2 = 2 * dz - dx;
            for(; x0 != x1; x0 += sx){
                realPos.set(x0,y0,z0);
                if(!this.isWalkable(
                    voxelCellManager,
                    ServerWorldData.convertRealToChunkSpace(realPos),
                    ServerWorldData.convertRealToVoxelSpace(realPos)
                )){
                    return false;
                }
                if(err1 > 0){
                    y0 += sy;
                    err1 -= 2 * dx;
                }
                if(err2 > 0){
                    z0 += sz;
                    err2 -= 2 * dx;
                }
                err1 += 2 * dy;
                err2 += 2 * dz;
            }
        } else if(dy >= dx && dy >= dz){
            err1 = 2 * dx - dy;
            err2 = 2 * dz - dy;
            for(; y0 != y1; y0 += sy){
                realPos.set(x0,y0,z0);
                if(!this.isWalkable(
                    voxelCellManager,
                    ServerWorldData.convertRealToChunkSpace(realPos),
                    ServerWorldData.convertRealToVoxelSpace(realPos)
                )){
                    return false;
                }
                if(err1 > 0){
                    x0 += sx;
                    err1 -= 2 * dy;
                }
                if(err2 > 0){
                    z0 += sz;
                    err2 -= 2 * dy;
                }
                err1 += 2 * dx;
                err2 += 2 * dz;
            }
        } else {
            err1 = 2 * dy - dz;
            err2 = 2 * dx - dz;
            for(; z0 != z1; z0 += sz){
                realPos.set(x0,y0,z0);
                if(!this.isWalkable(
                    voxelCellManager,
                    ServerWorldData.convertRealToChunkSpace(realPos),
                    ServerWorldData.convertRealToVoxelSpace(realPos)
                )){
                    return false;
                }
                if(err1 > 0){
                    y0 += sy;
                    err1 -= 2 * dz;
                }
                if(err2 > 0){
                    x0 += sx;
                    err2 -= 2 * dz;
                }
                err1 += 2 * dy;
                err2 += 2 * dx;
            }
        }
        return true;
    }

    /**
     * Checks if a voxel is passable
     * @param voxelCellManager The voxel cell manager
     * @param chunkPos The position of the chunk
     * @param voxelPos The position of the boxel
     * @return true if it is passable, false otherwise
     */
    private boolean isWalkable(VoxelCellManager voxelCellManager, Vector3i chunkPos, Vector3i voxelPos){
        int voxelType = voxelCellManager.getVoxelTypeAtLocalPosition(chunkPos, voxelPos);
        float voxelWeight = voxelCellManager.getVoxelWeightAtLocalPosition(chunkPos, voxelPos);

        //get the type of voxel above the current voxel
        int aboveChunk = chunkPos.y + ((voxelPos.y + 1) / ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET);
        int aboveVoxel = ((voxelPos.y + 1) % ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET);
        ServerTerrainChunk chunk = voxelCellManager.getChunkAtPosition(chunkPos.x, aboveChunk, chunkPos.z);
        int aboveType = chunk.getType(voxelPos.x, aboveVoxel, voxelPos.z);
        float aboveWeight = chunk.getWeight(voxelPos.x, aboveVoxel, voxelPos.z);

        //checks
        boolean standingOnGround = voxelType != ServerTerrainChunk.VOXEL_TYPE_AIR && voxelWeight > 0;
        boolean aboveIsAir = (aboveType == ServerTerrainChunk.VOXEL_TYPE_AIR || aboveWeight <= 0);

        //check blocks as well
        BlockChunkData blockChunkData = null;

        Vector3i blockPos = new Vector3i();
        Vector3i currChunk = new Vector3i(chunkPos);
        Vector3i offsets = new Vector3i();
        int numBlocks = 0;
        for(int x = -1; x < 4; x++){
            for(int y = -2; y < 4; y++){
                for(int z = -1; z < 3; z++){
                    blockPos.set(voxelPos).mul(BlockChunkData.BLOCKS_PER_UNIT_DISTANCE);
                    currChunk.set(chunkPos);
                    offsets.set(
                        x,
                        y + BlockChunkData.BLOCKS_PER_UNIT_DISTANCE,
                        z
                    );
                    VoxelPathfinder.clampBlockOffsets(blockPos, currChunk, offsets);
                    blockChunkData = voxelCellManager.getBlocksAtPosition(currChunk);
                    if(blockChunkData.getType(
                        blockPos.x,
                        blockPos.y,
                        blockPos.z
                    ) != BlockChunkData.BLOCK_TYPE_EMPTY){
                        numBlocks++;
                    }
                }
            }
        }
        boolean blocksChestHeight = numBlocks > MAX_BLOCKS_FOR_WALKABLE;

        return standingOnGround && aboveIsAir && !blocksChestHeight;
    }


    /**
     * Scans for the nearest walkable position to the specified target point
     * @param voxelCellManager The voxel cell manager
     * @param targetPoint The target point to scan from
     * @param maxScanRadius The maximum radius to scan for walkable positions
     * @return The closest point that is walkable, or null if no point is found
     */
    public Vector3d scanNearestWalkable(VoxelCellManager voxelCellManager, Vector3d targetPoint, double maxScanRadius){
        int radius = 1;
        int scanned = 0;
        Vector3i originVoxel = ServerWorldData.convertRealToVoxelSpace(targetPoint);
        Vector3i originChunk = ServerWorldData.convertRealToChunkSpace(targetPoint);

        //check if supplied point is valid
        if(this.isWalkable(voxelCellManager, originChunk, originVoxel)){
            return targetPoint;
        }

        Vector3i currVoxel = new Vector3i();
        Vector3i currChunk = new Vector3i();
        Vector3d realPos;
        Vector3i offsets = new Vector3i();


        while(true){
            scanned = 0;
            for(int x = -radius; x <= radius; x++){
                for(int y = -radius; y <= radius; y++){
                    currVoxel.set(originVoxel);
                    currChunk.set(originChunk);
                    offsets.set(-radius,x,y);
                    VoxelPathfinder.clampVoxelOffsets(currVoxel, currChunk, offsets);
                    realPos = ServerWorldData.convertVoxelToRealSpace(currVoxel, currChunk);
                    if(realPos.distance(targetPoint) < maxScanRadius){
                        scanned++;
                        if(this.isWalkable(voxelCellManager, currChunk, currVoxel)){
                            return realPos;
                        }
                    }

                    currVoxel.set(originVoxel);
                    currChunk.set(originChunk);
                    offsets.set(radius,x,y);
                    VoxelPathfinder.clampVoxelOffsets(currVoxel, currChunk, offsets);
                    realPos = ServerWorldData.convertVoxelToRealSpace(currVoxel, currChunk);
                    if(realPos.distance(targetPoint) < maxScanRadius){
                        scanned++;
                        if(this.isWalkable(voxelCellManager, currChunk, currVoxel)){
                            return realPos;
                        }
                    }

                    currVoxel.set(originVoxel);
                    currChunk.set(originChunk);
                    offsets.set(x,-radius,y);
                    VoxelPathfinder.clampVoxelOffsets(currVoxel, currChunk, offsets);
                    realPos = ServerWorldData.convertVoxelToRealSpace(currVoxel, currChunk);
                    if(realPos.distance(targetPoint) < maxScanRadius){
                        scanned++;
                        if(this.isWalkable(voxelCellManager, currChunk, currVoxel)){
                            return realPos;
                        }
                    }

                    currVoxel.set(originVoxel);
                    currChunk.set(originChunk);
                    offsets.set(x,radius,y);
                    VoxelPathfinder.clampVoxelOffsets(currVoxel, currChunk, offsets);
                    realPos = ServerWorldData.convertVoxelToRealSpace(currVoxel, currChunk);
                    if(realPos.distance(targetPoint) < maxScanRadius){
                        scanned++;
                        if(this.isWalkable(voxelCellManager, currChunk, currVoxel)){
                            return realPos;
                        }
                    }

                    currVoxel.set(originVoxel);
                    currChunk.set(originChunk);
                    offsets.set(x,y,-radius);
                    VoxelPathfinder.clampVoxelOffsets(currVoxel, currChunk, offsets);
                    realPos = ServerWorldData.convertVoxelToRealSpace(currVoxel, currChunk);
                    if(realPos.distance(targetPoint) < maxScanRadius){
                        scanned++;
                        if(this.isWalkable(voxelCellManager, currChunk, currVoxel)){
                            return realPos;
                        }
                    }

                    currVoxel.set(originVoxel);
                    currChunk.set(originChunk);
                    offsets.set(x,y,radius);
                    VoxelPathfinder.clampVoxelOffsets(currVoxel, currChunk, offsets);
                    realPos = ServerWorldData.convertVoxelToRealSpace(currVoxel, currChunk);
                    if(realPos.distance(targetPoint) < maxScanRadius){
                        scanned++;
                        if(this.isWalkable(voxelCellManager, currChunk, currVoxel)){
                            return realPos;
                        }
                    }
                }
            }
            if(scanned == 0){
                break;
            }
            radius++;
        }

        return null;
    }

    /**
     * Clamps offsets to valid voxel positions
     * @param voxelPos The existing voxel position
     * @param chunkPos The existing chunk position
     * @param offsets The offsets to apply
     */
    private static void clampVoxelOffsets(Vector3i voxelPos, Vector3i chunkPos, Vector3i offsets){
        //calculate chunk offsets
        int storageX = (voxelPos.x + offsets.x);
        int storageY = (voxelPos.y + offsets.y);
        int storageZ = (voxelPos.z + offsets.z);
        if(storageX < 0){
            storageX = -1;
        } else {
            storageX = storageX / ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
        }
        if(storageY < 0){
            storageY = -1;
        } else {
            storageY = storageY / ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
        }
        if(storageZ < 0){
            storageZ = -1;
        } else {
            storageZ = storageZ / ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
        }
        //update world position
        chunkPos.x = chunkPos.x + storageX;
        chunkPos.y = chunkPos.y + storageY;
        chunkPos.z = chunkPos.z + storageZ;
        voxelPos.x = (voxelPos.x + offsets.x + ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET) % ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
        voxelPos.y = (voxelPos.y + offsets.y + ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET) % ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
        voxelPos.z = (voxelPos.z + offsets.z + ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET) % ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;

        if(
            voxelPos.x < 0 || voxelPos.y < 0 || voxelPos.z < 0 ||
            chunkPos.x < 0 || chunkPos.y < 0 || chunkPos.z < 0 ||
            chunkPos.x > 65536 || chunkPos.y > 65536 || chunkPos.z > 65536
        ){
            String message = "Failed to clamp \n" +
            "voxelPos: " + voxelPos.x + "," + voxelPos.y + "," + voxelPos.z + "\n" +
            "chunkPos: " + chunkPos.x + "," + chunkPos.y + "," + chunkPos.z + "\n" +
            "offsets: " + offsets.x + "," + offsets.y + "," + offsets.z + "\n" +
            "storage: " + storageX + "," + storageY + "," + storageZ + "\n" +
            "";
            throw new Error(message);
        }
    }


    /**
     * Clamps offsets to valid block positions
     * @param blockPos The existing block position
     * @param chunkPos The existing chunk position
     * @param offsets The offsets to apply
     */
    private static void clampBlockOffsets(Vector3i blockPos, Vector3i chunkPos, Vector3i offsets){
        //calculate chunk offsets
        int storageX = (blockPos.x + offsets.x);
        int storageY = (blockPos.y + offsets.y);
        int storageZ = (blockPos.z + offsets.z);
        if(storageX < 0){
            storageX = -1;
        } else {
            storageX = storageX / BlockChunkData.CHUNK_DATA_WIDTH;
        }
        if(storageY < 0){
            storageY = -1;
        } else {
            storageY = storageY / BlockChunkData.CHUNK_DATA_WIDTH;
        }
        if(storageZ < 0){
            storageZ = -1;
        } else {
            storageZ = storageZ / BlockChunkData.CHUNK_DATA_WIDTH;
        }
        //update world position
        chunkPos.x = chunkPos.x + storageX;
        chunkPos.y = chunkPos.y + storageY;
        chunkPos.z = chunkPos.z + storageZ;
        blockPos.x = (blockPos.x + offsets.x + BlockChunkData.CHUNK_DATA_WIDTH) % BlockChunkData.CHUNK_DATA_WIDTH;
        blockPos.y = (blockPos.y + offsets.y + BlockChunkData.CHUNK_DATA_WIDTH) % BlockChunkData.CHUNK_DATA_WIDTH;
        blockPos.z = (blockPos.z + offsets.z + BlockChunkData.CHUNK_DATA_WIDTH) % BlockChunkData.CHUNK_DATA_WIDTH;
    }

    /**
     * A node to use during searching
     */
    public static class PathfinderNode implements Comparable<PathfinderNode> {

        /**
         * The world x position
         */
        int worldX;

        /**
         * The world y position
         */
        int worldY;

        /**
         * The world z position
         */
        int worldZ;

        /**
         * The voxel x position
         */
        int voxelX;

        /**
         * The voxel y position
         */
        int voxelY;

        /**
         * The voxel z position
         */
        int voxelZ;

        /**
         * Cost to get to this node
         */
        long cost = 0;

        /**
         * The hash of this node
         */
        long hash = 0;

        /**
         * The previous node
         */
        long prevNode = 0;

        public PathfinderNode(
            int worldX, int worldY, int worldZ,
            int voxelX, int voxelY, int voxelZ,
            long cost, long hash, long prevNode
        ){
            this.worldX = worldX;
            this.worldY = worldY;
            this.worldZ = worldZ;
            this.voxelX = voxelX;
            this.voxelY = voxelY;
            this.voxelZ = voxelZ;
            this.cost = cost;
            this.hash = hash;
            this.prevNode = prevNode;
        }

        @Override
        public int compareTo(PathfinderNode o) {
            return (int)(this.cost - o.cost);
        }

        /**
         * Gets the position of the node
         * @return The position of the node
         */
        public Vector3d getPosition(){
            return new Vector3d(
                ServerWorldData.convertVoxelToRealSpace(voxelX, worldX),
                ServerWorldData.convertVoxelToRealSpace(voxelY, worldY),
                ServerWorldData.convertVoxelToRealSpace(voxelZ, worldZ)
            );
        }
    }

}
