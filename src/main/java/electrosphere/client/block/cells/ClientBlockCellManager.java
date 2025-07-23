package electrosphere.client.block.cells;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.client.block.BlockChunkData;
import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.engine.Globals;
import electrosphere.entity.EntityUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.util.ds.octree.WorldOctTree;
import electrosphere.util.ds.octree.WorldOctTree.WorldOctTreeNode;
import electrosphere.util.math.GeomUtils;

/**
 * Manages draw cells on the client
 */
public class ClientBlockCellManager {

    /**
     * Number of times to try updating per frame. Lower this to reduce lag but slow down block mesh generation.
     */
    static final int UPDATE_ATTEMPTS_PER_FRAME = 3;

    /**
     * The number of generation attempts before a cell is marked as having not requested its data
     */
    static final int FAILED_GENERATION_ATTEMPT_THRESHOLD = 250;

    /**
     * The distance to draw at full resolution
     */
    public static final double FULL_RES_DIST = 8;

    /**
     * The distance for half resolution
     */
    public static final double HALF_RES_DIST = 16;

    /**
     * The distance for quarter resolution
     */
    public static final double QUARTER_RES_DIST = 20;

    /**
     * The distance for eighth resolution
     */
    public static final double EIGHTH_RES_DIST = 32;

    /**
     * The distance for sixteenth resolution
     */
    public static final double SIXTEENTH_RES_DIST = 64;

    /**
     * The octree holding all the chunks to evaluate
     */
    WorldOctTree<BlockDrawCell> chunkTree;

    /**
     * Tracks what nodes have been evaluated this frame -- used to deduplicate evaluation calls
     */
    Map<WorldOctTreeNode<BlockDrawCell>,Boolean> evaluationMap = new HashMap<WorldOctTreeNode<BlockDrawCell>,Boolean>();

    /**
     * The last recorded player world position
     */
    Vector3i lastPlayerPos = new Vector3i();

    /**
     * Tracks whether the cell manager updated last frame or not
     */
    boolean updatedLastFrame = true;

    /**
     * Controls whether the client draw cell manager should update or not
     */
    boolean shouldUpdate = true;

    /**
     * The voxel texture atlas
     */
    BlockTextureAtlas textureAtlas;

    /**
     * The dimensions of the world
     */
    int worldDim = 0;

    /**
     * Tracks the number of currently valid cells (ie didn't require an update this frame)
     */
    int validCellCount = 0;

    /**
     * The number of maximum resolution chunks
     */
    int maxResCount = 0;

    /**
     * The number of half resolution chunks
     */
    int halfResCount = 0;

    /**
     * The number of generated chunks
     */
    int generated = 0;

    /**
     * Tracks whether the cell manager has initialized or not
     */
    boolean initialized = false;

    /**
     * The number of cells waiting on the network
     */
    private int waitingOnNetworkCount = 0;

    /**
     * The number of cells that triggered a model generation last frame
     */
    private int generationLastFrameCount = 0;

    /**
     * The number of cells that either split or joined last frame
     */
    private int partitionLastFrameCount = 0;

    /**
     * The number of cells that triggered a request last frame
     */
    private int requestLastFrameCount = 0;

    /**
     * Constructor
     * @param voxelTextureAtlas The voxel texture atlas
     * @param worldDim The size of the world in chunks
     */
    public ClientBlockCellManager(BlockTextureAtlas voxelTextureAtlas, int worldDim){
        this.chunkTree = new WorldOctTree<BlockDrawCell>(
            new Vector3i(0,0,0),
            new Vector3i(worldDim, worldDim, worldDim)
        );
        this.chunkTree.getRoot().setData(BlockDrawCell.generateBlockCell(new Vector3i(0,0,0), chunkTree.getMaxLevel()));
        this.worldDim = worldDim;
        this.textureAtlas = voxelTextureAtlas;
    }

    /**
     * Updates all cells in the chunk
     */
    public void update(){
        Globals.profiler.beginCpuSample("ClientBlockCellManager.update");
        if(shouldUpdate && Globals.clientState.playerEntity != null){
            //reset tracking
            this.waitingOnNetworkCount = 0;
            this.generationLastFrameCount = 0;
            this.partitionLastFrameCount = 0;
            this.requestLastFrameCount = 0;
            Vector3d playerPos = EntityUtils.getPosition(Globals.clientState.playerEntity);
            Vector3i playerWorldPos = Globals.clientState.clientWorldData.convertRealToWorldSpace(playerPos);
            int distCache = this.getDistCache(this.lastPlayerPos, playerWorldPos);
            this.lastPlayerPos.set(playerWorldPos);
            //the sets to iterate through
            updatedLastFrame = true;
            validCellCount = 0;
            evaluationMap.clear();
            //update all full res cells
            WorldOctTreeNode<BlockDrawCell> rootNode = this.chunkTree.getRoot();
            Globals.profiler.beginCpuSample("ClientBlockCellManager.update - full res cells");
            updatedLastFrame = this.recursivelyUpdateCells(rootNode, playerWorldPos, evaluationMap, BlockChunkData.LOD_LOWEST_RES, distCache);
            Globals.profiler.endCpuSample();
            if(!updatedLastFrame && !this.initialized){
                this.initialized = true;
            }
        }
        Globals.profiler.endCpuSample();
    }

    /**
     * Recursively update child nodes
     * @param node The root node
     * @param playerPos The player's position
     * @param minLeafLod The minimum LOD required to evaluate a leaf
     * @param evaluationMap Map of leaf nodes that have been evaluated this frame
     * @return true if there is work remaining to be done, false otherwise
     */
    private boolean recursivelyUpdateCells(WorldOctTreeNode<BlockDrawCell> node, Vector3i playerPos, Map<WorldOctTreeNode<BlockDrawCell>,Boolean> evaluationMap, int minLeafLod, int distCache){
        boolean updated = false;
        if(evaluationMap.containsKey(node)){
            return false;
        }
        if(node.getData().hasGenerated() && 
        (
            node.getData().isHomogenous() ||
            this.getMinDistance(playerPos, node, distCache) > SIXTEENTH_RES_DIST
        )
        ){
            return false;
        }
        if(node.isLeaf()){
            if(this.isMeta(playerPos, node, distCache)){
                this.flagAsMeta(node);
            } else if(this.shouldSplit(playerPos, node, distCache)){
                Globals.profiler.beginCpuSample("ClientDrawCellManager.split");
                //perform op
                WorldOctTreeNode<BlockDrawCell> container = chunkTree.split(node);
                BlockDrawCell containerCell = BlockDrawCell.generateBlockCell(container.getMinBound(), this.chunkTree.getMaxLevel() - container.getLevel());
                container.setData(containerCell);
                container.getData().transferChunkData(node.getData());
    
                //do creations
                container.getChildren().forEach(child -> {
                    Vector3i cellWorldPos = new Vector3i(
                        child.getMinBound().x,
                        child.getMinBound().y,
                        child.getMinBound().z
                    );
                    BlockDrawCell drawCell = BlockDrawCell.generateBlockCell(cellWorldPos,this.chunkTree.getMaxLevel() - child.getLevel());
                    drawCell.registerNotificationTarget(node.getData());
                    child.setLeaf(true);
                    child.setData(drawCell);
                    evaluationMap.put(child,true);
                });

                //do deletions
                this.recursivelyDestroy(node);
    
                //update neighbors
                this.conditionalUpdateAdjacentNodes(container, container.getChildren().get(0).getLevel());

                //update tracking
                this.partitionLastFrameCount++;
                
                Globals.profiler.endCpuSample();
                updated = true;
            } else if(this.shouldRequest(playerPos, node, minLeafLod, distCache)){
                Globals.profiler.beginCpuSample("ClientDrawCellManager.request");
    
                //calculate what to request
                BlockDrawCell cell = node.getData();
                //actually send requests
                if(this.requestChunks(node)){
                    cell.setHasRequested(true);
                }
                evaluationMap.put(node,true);

                //update tracking
                this.requestLastFrameCount++;
    
                Globals.profiler.endCpuSample();
                updated = true;
            } else if(this.shouldGenerate(playerPos, node, minLeafLod, distCache)){
                Globals.profiler.beginCpuSample("ClientDrawCellManager.generate");
                int lodLevel = this.getLODLevel(node);
    
                if(this.containsDataToGenerate(node)){
                    node.getData().generateDrawableEntity(textureAtlas, lodLevel);
                    if(node.getData().getFailedGenerationAttempts() > FAILED_GENERATION_ATTEMPT_THRESHOLD){
                        node.getData().setHasRequested(false);
                    }
                    this.requestLastFrameCount++;
                } else if(node.getData() != null){
                    this.waitingOnNetworkCount++;
                    node.getData().setFailedGenerationAttempts(node.getData().getFailedGenerationAttempts() + 1);
                    if(node.getData().getFailedGenerationAttempts() > FAILED_GENERATION_ATTEMPT_THRESHOLD){
                        node.getData().setHasRequested(false);
                    }
                    this.generationLastFrameCount++;
                }
                evaluationMap.put(node,true);
                Globals.profiler.endCpuSample();
                updated = true;
            }
        } else {
            if(this.shouldJoin(playerPos, node, distCache)) {
                this.join(node);
                this.partitionLastFrameCount++;
                updated = true;
            } else {
                this.validCellCount++;
                List<WorldOctTreeNode<BlockDrawCell>> children = node.getChildren();
                boolean isHomogenous = true;
                boolean fullyGenerated = true;
                for(int i = 0; i < 8; i++){
                    WorldOctTreeNode<BlockDrawCell> child = children.get(i);
                    boolean childUpdate = this.recursivelyUpdateCells(child, playerPos, evaluationMap, minLeafLod, distCache);
                    if(childUpdate == true){
                        updated = true;
                    }
                    if(!child.getData().hasGenerated()){
                        fullyGenerated = false;
                    }
                    if(!child.getData().isHomogenous()){
                        isHomogenous = false;
                    }
                }
                WorldOctTreeNode<BlockDrawCell> newNode = null;
                if(isHomogenous){
                    newNode = this.join(node);
                    newNode.getData().setHomogenous(true);
                }
                if(fullyGenerated && newNode != null){
                    newNode.getData().setHasGenerated(true);
                }
                if((this.chunkTree.getMaxLevel() - node.getLevel()) < minLeafLod){
                    evaluationMap.put(node,true);
                }
            }
        }
        return updated;
    }

    /**
     * Gets the minimum distance from a node to a point
     * @param pos the position to check against
     * @param node the node
     * @return the distance
     */
    public long getMinDistance(Vector3i worldPos, WorldOctTreeNode<BlockDrawCell> node, int distCache){
        return node.getData().getMinDistance(worldPos, node, distCache);
    }

    /**
     * Gets the distance cache value
     * @param lastPlayerPos The last player world position
     * @param currentPlayerPos The current player world position
     * @return The distance cache value
     */
    private int getDistCache(Vector3i lastPlayerPos, Vector3i currentPlayerPos){
        if(
            lastPlayerPos.x / 16 != currentPlayerPos.x / 16 || lastPlayerPos.z / 16 != currentPlayerPos.z / 16 || lastPlayerPos.z / 16 != currentPlayerPos.z / 16
        ){
            return this.chunkTree.getMaxLevel();
        }
        if(
            lastPlayerPos.x / 16 != currentPlayerPos.x / 16 || lastPlayerPos.z / 16 != currentPlayerPos.z / 16 || lastPlayerPos.z / 16 != currentPlayerPos.z / 16
        ){
            return BlockChunkData.LOD_SIXTEENTH_RES + 2;
        }
        if(
            lastPlayerPos.x / 8 != currentPlayerPos.x / 8 || lastPlayerPos.z / 8 != currentPlayerPos.z / 8 || lastPlayerPos.z / 8 != currentPlayerPos.z / 8
        ){
            return BlockChunkData.LOD_SIXTEENTH_RES + 1;
        }
        if(
            lastPlayerPos.x / 4 != currentPlayerPos.x / 4 || lastPlayerPos.z / 4 != currentPlayerPos.z / 4 || lastPlayerPos.z / 4 != currentPlayerPos.z / 4
        ){
            return BlockChunkData.LOD_SIXTEENTH_RES;
        }
        if(
            lastPlayerPos.x / 2 != currentPlayerPos.x / 2 || lastPlayerPos.z / 2 != currentPlayerPos.z / 2 || lastPlayerPos.z / 2 != currentPlayerPos.z / 2
        ){
            return BlockChunkData.LOD_EIGHTH_RES;
        }
        if(
            lastPlayerPos.x != currentPlayerPos.x || lastPlayerPos.z != currentPlayerPos.z || lastPlayerPos.z != currentPlayerPos.z
        ){
            return BlockChunkData.LOD_QUARTER_RES;
        }
        return -1;
    }

    /**
     * Gets whether this should be split or not
     * @param pos the player position
     * @param node The node
     * @return true if should split, false otherwise
     */
    public boolean shouldSplit(Vector3i pos, WorldOctTreeNode<BlockDrawCell> node, int distCache){
        //breaking out into dedicated function so can add case handling ie if we want
        //to combine fullres nodes into larger nodes to conserve on draw calls
        return
        node.canSplit() &&
        (node.getLevel() != this.chunkTree.getMaxLevel()) &&
        !node.getData().isHomogenous() &&
        (node.getParent() != null || node == this.chunkTree.getRoot()) &&
        (
            (
                node.getLevel() < this.chunkTree.getMaxLevel() - BlockChunkData.LOD_SIXTEENTH_RES &&
                this.getMinDistance(pos, node, distCache) <= SIXTEENTH_RES_DIST
            )
             ||
            (
                node.getLevel() < this.chunkTree.getMaxLevel() - BlockChunkData.LOD_EIGHTH_RES &&
                this.getMinDistance(pos, node, distCache) <= EIGHTH_RES_DIST
            )
             ||
            (
                node.getLevel() < this.chunkTree.getMaxLevel() - BlockChunkData.LOD_QUARTER_RES &&
                this.getMinDistance(pos, node, distCache) <= QUARTER_RES_DIST
            )
             ||
            (
                node.getLevel() < this.chunkTree.getMaxLevel() - BlockChunkData.LOD_HALF_RES &&
                this.getMinDistance(pos, node, distCache) <= HALF_RES_DIST
            )
             ||
            (
                node.getLevel() < this.chunkTree.getMaxLevel() &&
                this.getMinDistance(pos, node, distCache) <= FULL_RES_DIST
            )
        )
        ;
    }

    /**
     * Gets the LOD level of the draw cell
     * @param node The node to consider
     * @return -1 if outside of render range, -1 if the node is not a valid draw cell leaf, otherwise returns the LOD level
     */
    private int getLODLevel(WorldOctTreeNode<BlockDrawCell> node){
        return this.chunkTree.getMaxLevel() - node.getLevel();
    }

    /**
     * Conditionally updates all adjacent nodes if their level would require transition cells in the voxel rasterization
     * @param node The node to search from adjacencies from
     * @param level The level to check against
     */
    private void conditionalUpdateAdjacentNodes(WorldOctTreeNode<BlockDrawCell> node, int level){
        //don't bother to check if it's a lowest-res chunk
        if(this.chunkTree.getMaxLevel() - level > BlockChunkData.LOD_FULL_RES){
            return;
        }
        if(node.getMinBound().x - 1 >= 0){
            WorldOctTreeNode<BlockDrawCell> xNegNode = this.chunkTree.search(new Vector3i(node.getMinBound()).add(-1,0,0), false);
            if(xNegNode != null && xNegNode.getLevel() < level){
                xNegNode.getData().setHasGenerated(false);
            }
        }
        if(node.getMinBound().y - 1 >= 0){
            WorldOctTreeNode<BlockDrawCell> yNegNode = this.chunkTree.search(new Vector3i(node.getMinBound()).add(0,-1,0), false);
            if(yNegNode != null && yNegNode.getLevel() < level){
                yNegNode.getData().setHasGenerated(false);
            }
        }
        if(node.getMinBound().z - 1 >= 0){
            WorldOctTreeNode<BlockDrawCell> zNegNode = this.chunkTree.search(new Vector3i(node.getMinBound()).add(0,0,-1), false);
            if(zNegNode != null && zNegNode.getLevel() < level){
                zNegNode.getData().setHasGenerated(false);
            }
        }
        if(node.getMaxBound().x + 1 < this.worldDim){
            WorldOctTreeNode<BlockDrawCell> xPosNode = this.chunkTree.search(new Vector3i(node.getMaxBound()).add(1,-1,-1), false);
            if(xPosNode != null && xPosNode.getLevel() < level){
                xPosNode.getData().setHasGenerated(false);
            }
        }
        if(node.getMaxBound().y + 1 < this.worldDim){
            WorldOctTreeNode<BlockDrawCell> yPosNode = this.chunkTree.search(new Vector3i(node.getMaxBound()).add(-1,1,-1), false);
            if(yPosNode != null && yPosNode.getLevel() < level){
                yPosNode.getData().setHasGenerated(false);
            }
        }
        if(node.getMaxBound().z  + 1 < this.worldDim){
            WorldOctTreeNode<BlockDrawCell> zPosNode = this.chunkTree.search(new Vector3i(node.getMaxBound()).add(-1,-1,1), false);
            if(zPosNode != null && zPosNode.getLevel() < level){
                zPosNode.getData().setHasGenerated(false);
            }
        }
    }

    /**
     * Checks if this is a meta node
     * @param pos The position of the player
     * @param node The node
     * @param distCache The distance cache
     * @return true if it is a meta node, false otherwise
     */
    private boolean isMeta(Vector3i pos, WorldOctTreeNode<BlockDrawCell> node, int distCache){
        return 
        node.getLevel() < this.chunkTree.getMaxLevel() - BlockChunkData.LOD_SIXTEENTH_RES &&
        this.getMinDistance(pos, node, distCache) > SIXTEENTH_RES_DIST
        ;
    }

    /**
     * Sets this node to be a meta node
     * @param node The node
     */
    private void flagAsMeta(WorldOctTreeNode<BlockDrawCell> node){
        node.getData().setHasGenerated(true);
    }

    /**
     * Gets whether this should be joined or not
     * @param pos the player position
     * @param node The node
     * @return true if should be joined, false otherwise
     */
    public boolean shouldJoin(Vector3i pos, WorldOctTreeNode<BlockDrawCell> node, int distCache){
        //breaking out into dedicated function so can add case handling ie if we want
        //to combine fullres nodes into larger nodes to conserve on draw calls
        return
        node.getLevel() > 0 &&
        (node.getLevel() != this.chunkTree.getMaxLevel()) &&
        (
            (
                node.getLevel() == this.chunkTree.getMaxLevel() - BlockChunkData.LOD_HALF_RES &&
                this.getMinDistance(pos, node, distCache) > FULL_RES_DIST
            )
            ||
            (
                node.getLevel() == this.chunkTree.getMaxLevel() - BlockChunkData.LOD_QUARTER_RES &&
                this.getMinDistance(pos, node, distCache) > HALF_RES_DIST
            )
            ||
            (
                node.getLevel() == this.chunkTree.getMaxLevel() - BlockChunkData.LOD_EIGHTH_RES &&
                this.getMinDistance(pos, node, distCache) > QUARTER_RES_DIST
            )
            ||
            (
                node.getLevel() == this.chunkTree.getMaxLevel() - BlockChunkData.LOD_SIXTEENTH_RES &&
                this.getMinDistance(pos, node, distCache) > EIGHTH_RES_DIST
            )
            ||
            (
                this.getMinDistance(pos, node, distCache) > SIXTEENTH_RES_DIST
            )
        )
        ;
    }

    /**
     * Joins a parent node
     * @param node The parent node
     */
    private WorldOctTreeNode<BlockDrawCell> join(WorldOctTreeNode<BlockDrawCell> node){
        Globals.profiler.beginCpuSample("ClientDrawCellManager.join");

        //queue destructions prior to join -- the join operator clears all children on node
        this.recursivelyDestroy(node);

        //perform op
        BlockDrawCell newLeafCell = BlockDrawCell.generateBlockCell(node.getMinBound(),node.getData().lod);
        WorldOctTreeNode<BlockDrawCell> newLeaf = chunkTree.join(node, newLeafCell);
        newLeaf.getData().transferChunkData(node.getData());

        //update neighbors
        this.conditionalUpdateAdjacentNodes(newLeaf, newLeaf.getLevel());
        evaluationMap.put(newLeaf,true);

        Globals.profiler.endCpuSample();
        return newLeaf;
    }

    /**
     * Checks if this cell should request chunk data
     * @param pos the player's position
     * @param node the node
     * @param minLeafLod The minimum LOD required to evaluate a leaf
     * @return true if should request chunk data, false otherwise
     */
    public boolean shouldRequest(Vector3i pos, WorldOctTreeNode<BlockDrawCell> node, int minLeafLod, int distCache){
        return 
        node.getData() != null &&
        !node.getData().hasRequested() &&
        (this.chunkTree.getMaxLevel() - node.getLevel()) <= minLeafLod &&
        (
            (
                node.getLevel() == this.chunkTree.getMaxLevel()
                // &&
                // this.getMinDistance(pos, node) <= FULL_RES_DIST
            )
             ||
            (
                node.getLevel() == this.chunkTree.getMaxLevel() - BlockChunkData.LOD_HALF_RES
                &&
                this.getMinDistance(pos, node, distCache) <= QUARTER_RES_DIST
            )
            ||
            (
                node.getLevel() == this.chunkTree.getMaxLevel() - BlockChunkData.LOD_QUARTER_RES
                &&
                this.getMinDistance(pos, node, distCache) <= EIGHTH_RES_DIST
            )
            ||
            (
                node.getLevel() == this.chunkTree.getMaxLevel() - BlockChunkData.LOD_EIGHTH_RES
                &&
                this.getMinDistance(pos, node, distCache) <= SIXTEENTH_RES_DIST
            )
            ||
            (
                node.getLevel() == this.chunkTree.getMaxLevel() - BlockChunkData.LOD_SIXTEENTH_RES
                &&
                this.getMinDistance(pos, node, distCache) <= SIXTEENTH_RES_DIST
            )
        )
        ;
    }

    /**
     * Checks if this cell should generate
     * @param pos the player's position
     * @param node the node
     * @param minLeafLod The minimum LOD required to evaluate a leaf
     * @return true if should generate, false otherwise
     */
    public boolean shouldGenerate(Vector3i pos, WorldOctTreeNode<BlockDrawCell> node, int minLeafLod, int distCache){
        return 
        !node.getData().hasGenerated() &&
        (this.chunkTree.getMaxLevel() - node.getLevel()) <= minLeafLod &&
        (
            (
                node.getLevel() == this.chunkTree.getMaxLevel()
                // &&
                // this.getMinDistance(pos, node) <= FULL_RES_DIST
            )
            ||
            (
                node.getLevel() == this.chunkTree.getMaxLevel() - BlockChunkData.LOD_HALF_RES
                &&
                this.getMinDistance(pos, node, distCache) <= QUARTER_RES_DIST
            )
            ||
            (
                node.getLevel() == this.chunkTree.getMaxLevel() - BlockChunkData.LOD_QUARTER_RES
                &&
                this.getMinDistance(pos, node, distCache) <= EIGHTH_RES_DIST
            )
            ||
            (
                node.getLevel() == this.chunkTree.getMaxLevel() - BlockChunkData.LOD_EIGHTH_RES
                &&
                this.getMinDistance(pos, node, distCache) <= SIXTEENTH_RES_DIST
            )
            ||
            (
                node.getLevel() == this.chunkTree.getMaxLevel() - BlockChunkData.LOD_SIXTEENTH_RES
                &&
                this.getMinDistance(pos, node, distCache) <= SIXTEENTH_RES_DIST
            )
        )
        ;
    }

    /**
     * Checks if the node should have destroy called on it
     * @param node The node
     * @return true if should destroy, false otherwise
     */
    public boolean shouldDestroy(WorldOctTreeNode<BlockDrawCell> node){
        return 
        node.getData() != null &&
        node.getData().getEntities() != null &&
        node.getData().getEntities().size() > 0
        ;
    }

    /**
     * Destroys the foliage chunk
     */
    protected void destroy(){
        this.recursivelyDestroy(this.chunkTree.getRoot());
    }

    /**
     * Recursively destroy a tree
     * @param node The root of the tree
     */
    private void recursivelyDestroy(WorldOctTreeNode<BlockDrawCell> node){
        if(node.getChildren().size() > 0){
            for(WorldOctTreeNode<BlockDrawCell> child : node.getChildren()){
                this.recursivelyDestroy(child);
            }
        }
        if(node.getData() != null){
            node.getData().destroy();
        }
    }

    /**
     * Checks if the cell manager made an update last frame or not
     * @return true if an update occurred, false otherwise
     */
    public boolean updatedLastFrame(){
        return this.updatedLastFrame;
    }

    /**
     * Checks if the position is within the full LOD range
     * @param worldPos The world position
     * @return true if within full LOD range, false otherwise
     */
    public boolean isFullLOD(Vector3i worldPos){
        Vector3d playerRealPos = EntityUtils.getPosition(Globals.clientState.playerEntity);
        Vector3d chunkMin = Globals.clientState.clientWorldData.convertWorldToRealSpace(worldPos);
        Vector3d chunkMax = Globals.clientState.clientWorldData.convertWorldToRealSpace(new Vector3i(worldPos).add(1,1,1));
        return GeomUtils.getMinDistanceAABB(playerRealPos, chunkMin, chunkMax) <= FULL_RES_DIST;
    }

    /**
     * Evicts all cells
     */
    public void evictAll(){
        this.recursivelyDestroy(this.chunkTree.getRoot());
        this.chunkTree.clear();
    }


    /**
     * Marks a draw cell as updateable
     * @param worldX The world x position
     * @param worldY The world y position
     * @param worldZ The world z position
     */
    public void markUpdateable(int worldX, int worldY, int worldZ){
        BlockDrawCell drawCell = this.getDrawCell(worldX, worldY, worldZ);
        drawCell.ejectChunkData();
        drawCell.setHasGenerated(false);
        drawCell.setHasRequested(false);
    }

    /**
     * Marks a draw cell as homogenous or not
     * @param worldX The world x position
     * @param worldY The world y position
     * @param worldZ The world z position
     */
    public void markHomogenous(int worldX, int worldY, int worldZ, boolean homogenous){
        BlockDrawCell drawCell = this.getDrawCell(worldX, worldY, worldZ);
        drawCell.ejectChunkData();
        drawCell.setHomogenous(homogenous);
    }

    /**
     * Requests all chunks for a given draw cell
     * @param cell The cell
     * @return true if all cells were successfully requested, false otherwise
     */
    private boolean requestChunks(WorldOctTree.WorldOctTreeNode<BlockDrawCell> node){
        int lod = this.chunkTree.getMaxLevel() - node.getLevel();
        Vector3i worldPos = node.getMinBound();
        if(
            worldPos.x >= 0 &&
            worldPos.x < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
            worldPos.y >= 0 &&
            worldPos.y < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
            worldPos.z >= 0 &&
            worldPos.z < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
            !Globals.clientState.clientBlockManager.containsChunkDataAtWorldPoint(worldPos.x, worldPos.y, worldPos.z, lod)
            ){
                //client should request chunk data from server for each chunk necessary to create the model
                LoggerInterface.loggerNetworking.DEBUG("(Client) Send Request for block data at " + worldPos);
                if(!Globals.clientState.clientBlockManager.requestChunk(worldPos.x, worldPos.y, worldPos.z, lod)){
                    return false;
                }
        }
        return true;
    }

    /**
     * Checks if all chunk data required to generate this draw cell is present
     * @param node The node
     * @return true if all data is available, false otherwise
     */
    private boolean containsDataToGenerate(WorldOctTree.WorldOctTreeNode<BlockDrawCell> node){
        BlockDrawCell cell = node.getData();
        int lod = this.chunkTree.getMaxLevel() - node.getLevel();
        Vector3i worldPos = cell.getWorldPos();
        if(!Globals.clientState.clientBlockManager.containsChunkDataAtWorldPoint(worldPos.x, worldPos.y, worldPos.z, lod)){
            return false;
        }
        return true;
    }

    /**
     * Sets whether the draw cell manager should update or not
     * @param shouldUpdate true if should update, false otherwise
     */
    public void setShouldUpdate(boolean shouldUpdate){
        this.shouldUpdate = shouldUpdate;
    }

    /**
     * Gets whether the client draw cell manager should update or not
     * @return true if should update, false otherwise
     */
    public boolean getShouldUpdate(){
        return this.shouldUpdate;
    }

    /**
     * Gets the number of currently valid cells
     * @return The number of currently valid cells
     */
    public int getValidCellCount(){
        return validCellCount;
    }

    /**
     * Calculates the status of the draw cell manager
     */
    public void updateStatus(){
        maxResCount = 0;
        halfResCount = 0;
        generated = 0;
        this.recursivelyCalculateStatus(this.chunkTree.getRoot());
    }

    /**
     * Recursively calculates the status of the manager
     * @param node The root node
     */
    private void recursivelyCalculateStatus(WorldOctTreeNode<BlockDrawCell> node){
        if(node.getLevel() == this.chunkTree.getMaxLevel() - 1){
            halfResCount++;
        }
        if(node.getLevel() == this.chunkTree.getMaxLevel()){
            maxResCount++;
        }
        if(node.getData() != null && node.getData().hasGenerated()){
            generated++;
        }
        if(node.getChildren() != null && node.getChildren().size() > 0){
            List<WorldOctTreeNode<BlockDrawCell>> children = new LinkedList<WorldOctTreeNode<BlockDrawCell>>(node.getChildren());
            for(WorldOctTreeNode<BlockDrawCell> child : children){
                recursivelyCalculateStatus(child);
            }
        }
    }

    /**
     * Gets The number of maximum resolution chunks
     * @return The number of maximum resolution chunks
     */
    public int getMaxResCount() {
        return maxResCount;
    }

    /**
     * Gets The number of half resolution chunks
     * @return The number of half resolution chunks
     */
    public int getHalfResCount() {
        return halfResCount;
    }

    /**
     * Gets The number of generated chunks
     * @return
     */
    public int getGenerated() {
        return generated;
    }

    /**
     * Gets whether the client draw cell manager has initialized or not
     * @return true if it has initialized, false otherwise
     */
    public boolean isInitialized(){
        return this.initialized;
    }

    /**
     * Gets the draw cell for a given world coordinate if it has been generated
     * @param worldX The world x coordinate
     * @param worldY The world y coordinate
     * @param worldZ The world z coordinate
     * @return The draw cell if it exists, null otherwise
     */
    public BlockDrawCell getDrawCell(int worldX, int worldY, int worldZ){
        WorldOctTreeNode<BlockDrawCell> node = this.chunkTree.search(new Vector3i(worldX,worldY,worldZ), false);
        if(node != null){
            return node.getData();
        }
        return null;
    }

    /**
     * Checks if physics has been generated for a given world coordinate
     * @param worldX The world x coordinate
     * @param worldY The world y coordinate
     * @param worldZ The world z coordinate
     * @return true if physics has been generated, false otherwise
     */
    public boolean hasGeneratedPhysics(int worldX, int worldY, int worldZ){
        BlockDrawCell cell = this.getDrawCell(worldX, worldY, worldZ);
        if(cell != null && cell.getEntities() != null && cell.getEntities().size() > 0){
            return PhysicsEntityUtils.containsDBody(cell.getEntities().get(0));
        }
        return false;
    }


    /**
     * Gets the number of nodes in the tree
     * @return The number of nodes
     */
    public int getNodeCount(){
        return this.chunkTree.getNodeCount();
    }

    /**
     * Gets the number of cells that are waiting on the network
     * @return The number of cells that are waiting on the network
     */
    public int getWaitingOnNetworkCount(){
        return this.waitingOnNetworkCount;
    }

    /**
     * Gets the number of cells that triggered a model generation last frame
     * @return The number of cells
     */
    public int getGenerationLastFrameCount(){
        return this.generationLastFrameCount;
    }

    /**
     * Gets the number of cells that triggered an octree split/join last frame
     * @return The number of cells
     */
    public int getPartitionLastFrameCount(){
        return this.partitionLastFrameCount;
    }

    /**
     * Gets the number of cells that triggered a terrain data request last frame
     * @return The number of cells
     */
    public int getRequestLastFrameCount(){
        return this.requestLastFrameCount;
    }

    
    
}
