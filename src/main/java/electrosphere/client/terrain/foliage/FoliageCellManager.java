package electrosphere.client.terrain.foliage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.client.terrain.cache.ChunkData;
import electrosphere.data.entity.foliage.FoliageType;
import electrosphere.engine.Globals;
import electrosphere.entity.EntityUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import electrosphere.util.ds.octree.WorldOctTree;
import electrosphere.util.ds.octree.WorldOctTree.WorldOctTreeNode;
import electrosphere.util.math.GeomUtils;

/**
 * Manages foliage cells on the client
 */
public class FoliageCellManager {

    /**
     * If moved this many cells in 1 frame, completely bust meta cells
     */
    public static final int TELEPORT_DISTANCE = 5000;

    /**
     * Number of times to try updating per frame. Lower this to reduce lag but slow down terrain mesh generation.
     */
    static final int UPDATE_ATTEMPTS_PER_FRAME = 3;

    /**
     * The number of generation attempts before a cell is marked as having not requested its data
     */
    static final int FAILED_GENERATION_ATTEMPT_THRESHOLD = 250;

    /**
     * The distance to foliage at full resolution
     */
    public static final double FULL_RES_DIST = 16;

    /**
     * The distance for half resolution
     */
    public static final double HALF_RES_DIST = 20;

    /**
     * The distance for quarter resolution
     */
    public static final double QUARTER_RES_DIST = 16;

    /**
     * The distance for eighth resolution
     */
    public static final double EIGHTH_RES_DIST = 24;

    /**
     * The distance for sixteenth resolution
     */
    public static final double SIXTEENTH_RES_DIST = 64;

    /**
     * Lod value for a full res chunk
     */
    public static final int FULL_RES_LOD = 0;

    /**
     * Lod value for a half res chunk
     */
    public static final int HALF_RES_LOD = 1;

    /**
     * Lod value for a quarter res chunk
     */
    public static final int QUARTER_RES_LOD = 2;

    /**
     * Lod value for a eighth res chunk
     */
    public static final int EIGHTH_RES_LOD = 3;

    /**
     * Lod value for a sixteenth res chunk
     */
    public static final int SIXTEENTH_RES_LOD = 4;

    /**
     * Lod value for evaluating all lod levels
     */
    public static final int ALL_RES_LOD = 5;

    /**
     * Lod value for busting up meta cells
     */
    public static final int BUST_META_CELLS = 40;

    /**
     * The octree holding all the chunks to evaluate
     */
    private WorldOctTree<FoliageCell> chunkTree;

    /**
     * Tracks what nodes have been evaluated this frame -- used to deduplicate evaluation calls
     */
    private Map<WorldOctTreeNode<FoliageCell>,Boolean> evaluationMap = new HashMap<WorldOctTreeNode<FoliageCell>,Boolean>();

    /**
     * The last recorded player world position
     */
    private Vector3i lastPlayerPos = new Vector3i();

    /**
     * Tracks whether the cell manager updated last frame or not
     */
    private boolean updatedLastFrame = true;

    /**
     * Controls whether the foliage cell manager should update or not
     */
    private boolean shouldUpdate = true;

    /**
     * The dimensions of the world
     */
    private int worldDim = 0;

    /**
     * Tracks the number of currently valid cells (ie didn't require an update this frame)
     */
    private int validCellCount = 0;

    /**
     * The number of maximum resolution chunks
     */
    private int maxResCount = 0;

    /**
     * The number of half resolution chunks
     */
    private int halfResCount = 0;

    /**
     * The number of generated chunks
     */
    private int generated = 0;

    /**
     * Tracks whether the cell manager has initialized or not
     */
    private boolean initialized = false;

    /**
     * The list of points to break at next evaluation
     */
    private List<Vector3i> breakPoints = new LinkedList<Vector3i>();

    /**
     * Used to bust the distance cache from external calls
     */
    private boolean bustDistCache = false;

    /**
     * Constructor
     * @param worldDim The size of the world in chunks
     */
    public FoliageCellManager(int worldDim){
        this.chunkTree = new WorldOctTree<FoliageCell>(
            new Vector3i(0,0,0),
            new Vector3i(worldDim * ServerTerrainChunk.CHUNK_DIMENSION, worldDim * ServerTerrainChunk.CHUNK_DIMENSION, worldDim * ServerTerrainChunk.CHUNK_DIMENSION)
        );
        this.chunkTree.getRoot().setData(FoliageCell.generateTerrainCell(new Vector3i(0,0,0), chunkTree.getMaxLevel()));
        this.worldDim = worldDim;
    }

    /**
     * Inits the foliage cell data
     */
    public void init(){
        //queue ambient foliage models
        for(FoliageType foliageType : Globals.gameConfigCurrent.getFoliageMap().getTypes()){
            if(foliageType.getTokens().contains(FoliageType.TOKEN_AMBIENT)){
                Globals.assetManager.addModelPathToQueue(foliageType.getGraphicsTemplate().getModel().getPath());
                Globals.assetManager.addShaderToQueue(FoliageCell.vertexPath, FoliageCell.fragmentPath);
            }
        }
    }

    /**
     * Updates all cells in the chunk
     */
    public void update(){
        Globals.profiler.beginCpuSample("FoliageCellManager.update");
        if(shouldUpdate && Globals.clientState.playerEntity != null && Globals.gameConfigCurrent.getSettings().getGraphicsPerformanceEnableFoliageManager()){
            Vector3d playerPos = EntityUtils.getPosition(Globals.clientState.playerEntity);
            Vector3i absVoxelPos = Globals.clientState.clientWorldData.convertRealToAbsoluteVoxelSpace(playerPos);
            int distCache = this.getDistCache(this.lastPlayerPos, absVoxelPos);
            if(bustDistCache || absVoxelPos.distance(this.lastPlayerPos) > TELEPORT_DISTANCE){
                distCache = BUST_META_CELLS;
                bustDistCache = false;
            }
            this.lastPlayerPos.set(absVoxelPos);
            //the sets to iterate through
            updatedLastFrame = true;
            validCellCount = 0;
            evaluationMap.clear();
            //update all full res cells
            WorldOctTreeNode<FoliageCell> rootNode = this.chunkTree.getRoot();
            Globals.profiler.beginCpuSample("FoliageCellManager.update - full res cells");
            updatedLastFrame = this.recursivelyUpdateCells(rootNode, absVoxelPos, evaluationMap, SIXTEENTH_RES_LOD, distCache);
            Globals.profiler.endCpuSample();
            if(!updatedLastFrame && !this.initialized){
                this.initialized = true;
            }
            if(this.breakPoints.size() > 0){
                this.breakPoints.clear();
            }
        }
        Globals.profiler.endCpuSample();
    }

    /**
     * Recursively update child nodes
     * @param node The root node
     * @param absVoxelPos The player's position
     * @param minLeafLod The minimum LOD required to evaluate a leaf
     * @param evaluationMap Map of leaf nodes that have been evaluated this frame
     * @return true if there is work remaining to be done, false otherwise
     */
    private boolean recursivelyUpdateCells(WorldOctTreeNode<FoliageCell> node, Vector3i absVoxelPos, Map<WorldOctTreeNode<FoliageCell>,Boolean> evaluationMap, int minLeafLod, int distCache){
        boolean updated = false;
        if(node.getData().getTripDebug()){
            node.getData().setTripDebug(false);
        }
        //breakpoint handling
        if(this.breakPoints.size() > 0){
            for(Vector3i breakpoint : breakPoints){
                if(GeomUtils.approxMinDistanceAABB(breakpoint, node.getMinBound(), node.getMaxBound()) == 0){
                    LoggerInterface.loggerEngine.WARNING("Break at " + breakpoint + " " + node.getLevel());
                    LoggerInterface.loggerEngine.WARNING("  " + node.getMinBound() + " " + node.getMaxBound());
                    LoggerInterface.loggerEngine.WARNING("  Generated: " + node.getData().hasGenerated());
                    LoggerInterface.loggerEngine.WARNING("  Homogenous: " + node.getData().isHomogenous());
                    LoggerInterface.loggerEngine.WARNING("  Leaf: " + node.isLeaf());
                    LoggerInterface.loggerEngine.WARNING("  Cached min dist: " + node.getData().cachedMinDistance);
                    LoggerInterface.loggerEngine.WARNING("  Actual min dist: " + GeomUtils.approxMinDistanceAABB(breakpoint, node.getMinBound(), node.getMaxBound()));
                }
            }
        }
        if(evaluationMap.containsKey(node)){
            return false;
        }
        if(
            node.getData().hasGenerated() && 
            (
                node.getData().isHomogenous() ||
                this.getMinDistance(absVoxelPos, node, distCache) > SIXTEENTH_RES_DIST
            ) &&
            distCache != BUST_META_CELLS
        ){
            return false;
        }
        if(node.isLeaf()){
            if(distCache == BUST_META_CELLS){
                node.getData().setHasGenerated(false);
            } if(this.isMeta(absVoxelPos, node, distCache)){
                this.flagAsMeta(node);
            } else if(this.shouldSplit(absVoxelPos, node, distCache)){
                Globals.profiler.beginCpuSample("FoliageCellManager.split");
                //perform op
                WorldOctTreeNode<FoliageCell> container = chunkTree.split(node);
                FoliageCell containerCell = FoliageCell.generateTerrainCell(container.getMinBound(), this.chunkTree.getMaxLevel() - container.getLevel());
                container.setData(containerCell);
                container.getData().transferChunkData(node.getData());
    
                //do creations
                container.getChildren().forEach(child -> {
                    Vector3i cellWorldPos = new Vector3i(
                        child.getMinBound().x,
                        child.getMinBound().y,
                        child.getMinBound().z
                    );
                    FoliageCell foliageCell = FoliageCell.generateTerrainCell(cellWorldPos,this.chunkTree.getMaxLevel() - child.getLevel());
                    foliageCell.registerNotificationTarget(node.getData());
                    child.setLeaf(true);
                    child.setData(foliageCell);
                    evaluationMap.put(child,true);
                });

                //do deletions
                this.recursivelyDestroy(node);
    
                //update neighbors
                this.conditionalUpdateAdjacentNodes(container, container.getChildren().get(0).getLevel());
                
                Globals.profiler.endCpuSample();
                updated = true;
            } else if(this.shouldRequest(absVoxelPos, node, minLeafLod, distCache)){
                Globals.profiler.beginCpuSample("FoliageCellManager.request");
    
                //calculate what to request
                FoliageCell cell = node.getData();
    
                //actually send requests
                if(this.requestChunks(node)){
                    cell.setHasRequested(true);
                }
                evaluationMap.put(node,true);
    
                Globals.profiler.endCpuSample();
                updated = true;
            } else if(this.shouldGenerate(absVoxelPos, node, minLeafLod, distCache)){
                Globals.profiler.beginCpuSample("FoliageCellManager.generate");
                int lodLevel = this.getLODLevel(node);
    
                if(this.containsDataToGenerate(node)){
                    node.getData().generateDrawableEntity(lodLevel);
                    if(node.getData().getFailedGenerationAttempts() > FAILED_GENERATION_ATTEMPT_THRESHOLD){
                        node.getData().setHasRequested(false);
                    }
                } else if(node.getData() != null){
                    node.getData().setFailedGenerationAttempts(node.getData().getFailedGenerationAttempts() + 1);
                    if(node.getData().getFailedGenerationAttempts() > FAILED_GENERATION_ATTEMPT_THRESHOLD){
                        node.getData().setHasRequested(false);
                    }
                }
                evaluationMap.put(node,true);
                Globals.profiler.endCpuSample();
                updated = true;
            }
        } else {
            if(this.shouldJoin(absVoxelPos, node, distCache)) {
                this.join(node);
                updated = true;
            } else {
                this.validCellCount++;
                List<WorldOctTreeNode<FoliageCell>> children = node.getChildren();
                boolean isHomogenous = true;
                boolean fullyGenerated = true;
                for(int i = 0; i < 8; i++){
                    WorldOctTreeNode<FoliageCell> child = children.get(i);
                    if(this.getMinDistance(absVoxelPos, child, distCache) > SIXTEENTH_RES_DIST){
                        continue;
                    }
                    boolean childUpdate = this.recursivelyUpdateCells(child, absVoxelPos, evaluationMap, minLeafLod, distCache);
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
                WorldOctTreeNode<FoliageCell> newNode = null;
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
     * @param absVoxelPos the position to check against
     * @param node the node
     * @return the distance
     */
    public long getMinDistance(Vector3i absVoxelPos, WorldOctTreeNode<FoliageCell> node, int distCache){
        return node.getData().getMinDistance(absVoxelPos, node, distCache);
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
            return SIXTEENTH_RES_LOD + 2;
        }
        if(
            lastPlayerPos.x / 8 != currentPlayerPos.x / 8 || lastPlayerPos.z / 8 != currentPlayerPos.z / 8 || lastPlayerPos.z / 8 != currentPlayerPos.z / 8
        ){
            return SIXTEENTH_RES_LOD + 1;
        }
        if(
            lastPlayerPos.x / 4 != currentPlayerPos.x / 4 || lastPlayerPos.z / 4 != currentPlayerPos.z / 4 || lastPlayerPos.z / 4 != currentPlayerPos.z / 4
        ){
            return SIXTEENTH_RES_LOD;
        }
        if(
            lastPlayerPos.x / 2 != currentPlayerPos.x / 2 || lastPlayerPos.z / 2 != currentPlayerPos.z / 2 || lastPlayerPos.z / 2 != currentPlayerPos.z / 2
        ){
            return EIGHTH_RES_LOD;
        }
        if(
            lastPlayerPos.x != currentPlayerPos.x || lastPlayerPos.z != currentPlayerPos.z || lastPlayerPos.z != currentPlayerPos.z
        ){
            return QUARTER_RES_LOD;
        }
        return -1;
    }

    /**
     * Gets whether this should be split or not
     * @param pos the player position
     * @param node The node
     * @return true if should split, false otherwise
     */
    public boolean shouldSplit(Vector3i pos, WorldOctTreeNode<FoliageCell> node, int distCache){
        //breaking out into dedicated function so can add case handling ie if we want
        //to combine fullres nodes into larger nodes to conserve on foliage calls
        return
        node.canSplit() &&
        (node.getLevel() != this.chunkTree.getMaxLevel()) &&
        !node.getData().isHomogenous() &&
        (node.getParent() != null || node == this.chunkTree.getRoot()) &&
        (
            (
                node.getLevel() < this.chunkTree.getMaxLevel() - SIXTEENTH_RES_LOD &&
                this.getMinDistance(pos, node, distCache) <= SIXTEENTH_RES_DIST
            )
             ||
            (
                node.getLevel() < this.chunkTree.getMaxLevel() - EIGHTH_RES_LOD &&
                this.getMinDistance(pos, node, distCache) <= EIGHTH_RES_DIST
            )
             ||
            (
                node.getLevel() < this.chunkTree.getMaxLevel() - QUARTER_RES_LOD &&
                this.getMinDistance(pos, node, distCache) <= QUARTER_RES_DIST
            )
            //  ||
            // (
            //     node.getLevel() < this.chunkTree.getMaxLevel() - HALF_RES_LOD &&
            //     this.getMinDistance(pos, node, distCache) <= HALF_RES_DIST
            // )
            //  ||
            // (
            //     node.getLevel() < this.chunkTree.getMaxLevel() &&
            //     this.getMinDistance(pos, node, distCache) <= FULL_RES_DIST
            // )
        )
        ;
    }

    /**
     * Gets the LOD level of the foliage cell
     * @param node The node to consider
     * @return -1 if outside of render range, -1 if the node is not a valid foliage cell leaf, otherwise returns the LOD level
     */
    private int getLODLevel(WorldOctTreeNode<FoliageCell> node){
        return this.chunkTree.getMaxLevel() - node.getLevel();
    }

    /**
     * Conditionally updates all adjacent nodes if their level would require transition cells in the voxel rasterization
     * @param node The node to search from adjacencies from
     * @param level The level to check against
     */
    private void conditionalUpdateAdjacentNodes(WorldOctTreeNode<FoliageCell> node, int level){
        //don't bother to check if it's a lowest-res chunk
        if(this.chunkTree.getMaxLevel() - level > FoliageCellManager.FULL_RES_LOD){
            return;
        }
        if(node.getMinBound().x - 1 >= 0){
            WorldOctTreeNode<FoliageCell> xNegNode = this.chunkTree.search(new Vector3i(node.getMinBound()).add(-1,0,0), false);
            if(xNegNode != null && xNegNode.getLevel() < level){
                xNegNode.getData().setHasGenerated(false);
            }
        }
        if(node.getMinBound().y - 1 >= 0){
            WorldOctTreeNode<FoliageCell> yNegNode = this.chunkTree.search(new Vector3i(node.getMinBound()).add(0,-1,0), false);
            if(yNegNode != null && yNegNode.getLevel() < level){
                yNegNode.getData().setHasGenerated(false);
            }
        }
        if(node.getMinBound().z - 1 >= 0){
            WorldOctTreeNode<FoliageCell> zNegNode = this.chunkTree.search(new Vector3i(node.getMinBound()).add(0,0,-1), false);
            if(zNegNode != null && zNegNode.getLevel() < level){
                zNegNode.getData().setHasGenerated(false);
            }
        }
        if(node.getMaxBound().x + 1 < this.worldDim){
            WorldOctTreeNode<FoliageCell> xPosNode = this.chunkTree.search(new Vector3i(node.getMaxBound()).add(1,-1,-1), false);
            if(xPosNode != null && xPosNode.getLevel() < level){
                xPosNode.getData().setHasGenerated(false);
            }
        }
        if(node.getMaxBound().y + 1 < this.worldDim){
            WorldOctTreeNode<FoliageCell> yPosNode = this.chunkTree.search(new Vector3i(node.getMaxBound()).add(-1,1,-1), false);
            if(yPosNode != null && yPosNode.getLevel() < level){
                yPosNode.getData().setHasGenerated(false);
            }
        }
        if(node.getMaxBound().z  + 1 < this.worldDim){
            WorldOctTreeNode<FoliageCell> zPosNode = this.chunkTree.search(new Vector3i(node.getMaxBound()).add(-1,-1,1), false);
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
    private boolean isMeta(Vector3i pos, WorldOctTreeNode<FoliageCell> node, int distCache){
        return 
        node.getLevel() < this.chunkTree.getMaxLevel() - SIXTEENTH_RES_LOD &&
        this.getMinDistance(pos, node, distCache) > SIXTEENTH_RES_DIST
        ;
    }

    /**
     * Sets this node to be a meta node
     * @param node The node
     */
    private void flagAsMeta(WorldOctTreeNode<FoliageCell> node){
        node.getData().setHasGenerated(true);
    }

    /**
     * Gets whether this should be joined or not
     * @param pos the player position
     * @param node The node
     * @return true if should be joined, false otherwise
     */
    public boolean shouldJoin(Vector3i pos, WorldOctTreeNode<FoliageCell> node, int distCache){
        //breaking out into dedicated function so can add case handling ie if we want
        //to combine fullres nodes into larger nodes to conserve on foliage calls
        return
        node.getLevel() > 0 &&
        (node.getLevel() != this.chunkTree.getMaxLevel()) &&
        (
            (
                node.getLevel() == this.chunkTree.getMaxLevel() - HALF_RES_LOD &&
                this.getMinDistance(pos, node, distCache) > FULL_RES_DIST
            )
            ||
            (
                node.getLevel() == this.chunkTree.getMaxLevel() - QUARTER_RES_LOD &&
                this.getMinDistance(pos, node, distCache) > HALF_RES_DIST
            )
            ||
            (
                node.getLevel() == this.chunkTree.getMaxLevel() - EIGHTH_RES_LOD &&
                this.getMinDistance(pos, node, distCache) > QUARTER_RES_DIST
            )
            ||
            (
                node.getLevel() == this.chunkTree.getMaxLevel() - SIXTEENTH_RES_LOD &&
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
    private WorldOctTreeNode<FoliageCell> join(WorldOctTreeNode<FoliageCell> node){
        Globals.profiler.beginCpuSample("FoliageCellManager.join");

        //queue destructions prior to join -- the join operator clears all children on node
        this.recursivelyDestroy(node);

        //perform op
        FoliageCell newLeafCell = FoliageCell.generateTerrainCell(node.getMinBound(),node.getData().lod);
        WorldOctTreeNode<FoliageCell> newLeaf = chunkTree.join(node, newLeafCell);
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
    public boolean shouldRequest(Vector3i pos, WorldOctTreeNode<FoliageCell> node, int minLeafLod, int distCache){
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
                node.getLevel() == this.chunkTree.getMaxLevel() - HALF_RES_LOD
                &&
                this.getMinDistance(pos, node, distCache) <= QUARTER_RES_DIST
            )
            ||
            (
                node.getLevel() == this.chunkTree.getMaxLevel() - QUARTER_RES_LOD
                &&
                this.getMinDistance(pos, node, distCache) <= EIGHTH_RES_DIST
            )
            ||
            (
                node.getLevel() == this.chunkTree.getMaxLevel() - EIGHTH_RES_LOD
                &&
                this.getMinDistance(pos, node, distCache) <= SIXTEENTH_RES_DIST
            )
            ||
            (
                node.getLevel() == this.chunkTree.getMaxLevel() - SIXTEENTH_RES_LOD
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
    public boolean shouldGenerate(Vector3i pos, WorldOctTreeNode<FoliageCell> node, int minLeafLod, int distCache){
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
                node.getLevel() == this.chunkTree.getMaxLevel() - HALF_RES_LOD
                &&
                this.getMinDistance(pos, node, distCache) <= QUARTER_RES_DIST
            )
            ||
            (
                node.getLevel() == this.chunkTree.getMaxLevel() - QUARTER_RES_LOD
                &&
                this.getMinDistance(pos, node, distCache) <= EIGHTH_RES_DIST
            )
            ||
            (
                node.getLevel() == this.chunkTree.getMaxLevel() - EIGHTH_RES_LOD
                &&
                this.getMinDistance(pos, node, distCache) <= SIXTEENTH_RES_DIST
            )
            ||
            (
                node.getLevel() == this.chunkTree.getMaxLevel() - SIXTEENTH_RES_LOD
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
    public boolean shouldDestroy(WorldOctTreeNode<FoliageCell> node){
        return 
        node.getData() != null &&
        node.getData().getEntity() != null
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
    private void recursivelyDestroy(WorldOctTreeNode<FoliageCell> node){
        if(node.getChildren().size() > 0){
            for(WorldOctTreeNode<FoliageCell> child : node.getChildren()){
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
        this.chunkTree.getRoot().setData(FoliageCell.generateTerrainCell(new Vector3i(0,0,0), chunkTree.getMaxLevel()));
    }


    /**
     * Marks a foliage cell as updateable
     * @param worldX The world x position
     * @param worldY The world y position
     * @param worldZ The world z position
     * @param voxelX The voxel x position
     * @param voxelY The voxel y position
     * @param voxelZ The voxel z position
     */
    public void markUpdateable(int worldX, int worldY, int worldZ, int voxelX, int voxelY, int voxelZ){
        int absVoxelX = Globals.clientState.clientWorldData.convertRelativeVoxelToAbsoluteVoxelSpace(voxelX,worldX);
        int absVoxelY = Globals.clientState.clientWorldData.convertRelativeVoxelToAbsoluteVoxelSpace(voxelY,worldY);
        int absVoxelZ = Globals.clientState.clientWorldData.convertRelativeVoxelToAbsoluteVoxelSpace(voxelZ,worldZ);
        FoliageCell foliageCell = this.getFoliageCell(absVoxelX, absVoxelY, absVoxelZ);
        foliageCell.ejectChunkData();
        foliageCell.setHasGenerated(false);
        foliageCell.setHasRequested(false);
    }

    /**
     * Marks a foliage cell as updateable
     * @param worldX The world x position
     * @param worldY The world y position
     * @param worldZ The world z position
     */
    public void markUpdateable(int worldX, int worldY, int worldZ){
        for(int x = 0; x < ServerTerrainChunk.CHUNK_DIMENSION; x++){
            for(int y = 0; y < ServerTerrainChunk.CHUNK_DIMENSION; y++){
                for(int z = 0; z < ServerTerrainChunk.CHUNK_DIMENSION; z++){
                    int absVoxelX = Globals.clientState.clientWorldData.convertRelativeVoxelToAbsoluteVoxelSpace(x,worldX);
                    int absVoxelY = Globals.clientState.clientWorldData.convertRelativeVoxelToAbsoluteVoxelSpace(y,worldY);
                    int absVoxelZ = Globals.clientState.clientWorldData.convertRelativeVoxelToAbsoluteVoxelSpace(z,worldZ);
                    FoliageCell foliageCell = this.getFoliageCell(absVoxelX, absVoxelY, absVoxelZ);
                    foliageCell.ejectChunkData();
                    foliageCell.setHasGenerated(false);
                    foliageCell.setHasRequested(false);
                }
            }
        }
    }

    /**
     * Requests all chunks for a given foliage cell
     * @param cell The cell
     * @return true if all cells were successfully requested, false otherwise
     */
    private boolean requestChunks(WorldOctTree.WorldOctTreeNode<FoliageCell> node){
        //min bound is in absolute voxel coordinates, need to convert to world coordinates
        Vector3i worldPos = Globals.clientState.clientWorldData.convertAbsoluteVoxelToWorldSpace(node.getMinBound());
        if(
            worldPos.x >= 0 &&
            worldPos.x < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
            worldPos.y >= 0 &&
            worldPos.y < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
            worldPos.z >= 0 &&
            worldPos.z < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
            !Globals.clientState.clientTerrainManager.containsChunkDataAtWorldPoint(worldPos.x, worldPos.y, worldPos.z, ChunkData.NO_STRIDE)
            ){
                //client should request chunk data from server for each chunk necessary to create the model
                LoggerInterface.loggerNetworking.DEBUG("(Client) Send Request for terrain at " + worldPos);
                if(!Globals.clientState.clientTerrainManager.requestChunk(worldPos.x, worldPos.y, worldPos.z, ChunkData.NO_STRIDE)){
                    return false;
                }
        }
        return true;
    }

    /**
     * Checks if all chunk data required to generate this foliage cell is present
     * @param node The node
     * @return true if all data is available, false otherwise
     */
    private boolean containsDataToGenerate(WorldOctTree.WorldOctTreeNode<FoliageCell> node){
        FoliageCell cell = node.getData();
        Vector3i worldPos = cell.getWorldPos();
        return Globals.clientState.clientTerrainManager.containsChunkDataAtWorldPoint(worldPos.x, worldPos.y, worldPos.z, ChunkData.NO_STRIDE);
    }

    /**
     * Sets whether the foliage cell manager should update or not
     * @param shouldUpdate true if should update, false otherwise
     */
    public void setShouldUpdate(boolean shouldUpdate){
        this.shouldUpdate = shouldUpdate;
    }

    /**
     * Gets whether the client foliage cell manager should update or not
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
     * Calculates the status of the foliage cell manager
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
    private void recursivelyCalculateStatus(WorldOctTreeNode<FoliageCell> node){
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
            List<WorldOctTreeNode<FoliageCell>> children = new LinkedList<WorldOctTreeNode<FoliageCell>>(node.getChildren());
            for(WorldOctTreeNode<FoliageCell> child : children){
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
     * Gets whether the client foliage cell manager has initialized or not
     * @return true if it has initialized, false otherwise
     */
    public boolean isInitialized(){
        return this.initialized;
    }

    /**
     * Gets the foliage cell for a given world coordinate if it has been generated
     * @param worldX The world x coordinate
     * @param worldY The world y coordinate
     * @param worldZ The world z coordinate
     * @return The foliage cell if it exists, null otherwise
     */
    public FoliageCell getFoliageCell(int worldX, int worldY, int worldZ){
        WorldOctTreeNode<FoliageCell> node = this.chunkTree.search(new Vector3i(worldX,worldY,worldZ), false);
        if(node != null){
            return node.getData();
        }
        return null;
    }

    /**
     * Gets the number of nodes in the tree
     * @return The number of nodes
     */
    public int getNodeCount(){
        return this.chunkTree.getNodeCount();
    }

    /**
     * Logs when the manager next evaluates the supplied absolute voxel position. Should be used to break at that point.
     * @param absVoxelPos The absolute voxel position to break at
     */
    public void addBreakPoint(Vector3i absVoxelPos){
        this.breakPoints.add(absVoxelPos);
    }

    /**
     * Busts the distance cache
     */
    public void bustDistanceCache(){
        this.bustDistCache = true;
    }
    
    
}
