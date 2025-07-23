package electrosphere.client.terrain.cells;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.client.terrain.cells.DrawCell.DrawCellFace;
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
public class ClientDrawCellManager {

    /**
     * Number of times to try updating per frame. Lower this to reduce lag but slow down terrain mesh generation.
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
    public static final double QUARTER_RES_DIST = 24;

    /**
     * The distance for eighth resolution
     */
    public static final double EIGHTH_RES_DIST = 64;

    /**
     * The distance for sixteenth resolution
     */
    public static final double SIXTEENTH_RES_DIST = 128;

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
     * The octree holding all the chunks to evaluate
     */
    WorldOctTree<DrawCell> chunkTree;

    /**
     * Tracks what nodes have been evaluated this frame -- used to deduplicate evaluation calls
     */
    Map<WorldOctTreeNode<DrawCell>,Boolean> evaluationMap = new HashMap<WorldOctTreeNode<DrawCell>,Boolean>();

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
    VoxelTextureAtlas textureAtlas;

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
     * Used to bust the distance cache from external calls
     */
    boolean bustDistCache = false;

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
    public ClientDrawCellManager(VoxelTextureAtlas voxelTextureAtlas, int worldDim){
        this.chunkTree = new WorldOctTree<DrawCell>(
            new Vector3i(0,0,0),
            new Vector3i(worldDim, worldDim, worldDim)
        );
        this.chunkTree.getRoot().setData(DrawCell.generateTerrainCell(new Vector3i(0,0,0), chunkTree.getMaxLevel()));
        this.worldDim = worldDim;
        this.textureAtlas = voxelTextureAtlas;
    }

    /**
     * Updates all cells in the chunk
     */
    public void update(){
        Globals.profiler.beginCpuSample("ClientDrawCellManager.update");
        if(shouldUpdate && Globals.clientState.playerEntity != null){
            //reset tracking
            this.waitingOnNetworkCount = 0;
            this.generationLastFrameCount = 0;
            this.partitionLastFrameCount = 0;
            this.requestLastFrameCount = 0;
            Vector3d playerPos = EntityUtils.getPosition(Globals.clientState.playerEntity);
            Vector3i playerWorldPos = Globals.clientState.clientWorldData.convertRealToWorldSpace(playerPos);
            int distCache = this.getDistCache(this.lastPlayerPos, playerWorldPos);
            if(this.bustDistCache){
                this.bustDistCache = false;
                distCache = this.chunkTree.getMaxLevel();
            }
            this.lastPlayerPos.set(playerWorldPos);
            //the sets to iterate through
            updatedLastFrame = true;
            validCellCount = 0;
            evaluationMap.clear();
            //update all full res cells
            WorldOctTreeNode<DrawCell> rootNode = this.chunkTree.getRoot();
            Globals.profiler.beginCpuSample("ClientDrawCellManager.update - full res cells");
            updatedLastFrame = this.recursivelyUpdateCells(rootNode, playerWorldPos, evaluationMap, SIXTEENTH_RES_LOD, distCache);
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
    private boolean recursivelyUpdateCells(WorldOctTreeNode<DrawCell> node, Vector3i playerPos, Map<WorldOctTreeNode<DrawCell>,Boolean> evaluationMap, int minLeafLod, int distCache){
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
                WorldOctTreeNode<DrawCell> container = chunkTree.split(node);
                DrawCell containerCell = DrawCell.generateTerrainCell(container.getMinBound(), this.chunkTree.getMaxLevel() - container.getLevel());
                container.setData(containerCell);
                container.getData().transferChunkData(node.getData());
    
                //do creations
                container.getChildren().forEach(child -> {
                    Vector3i cellWorldPos = new Vector3i(
                        child.getMinBound().x,
                        child.getMinBound().y,
                        child.getMinBound().z
                    );
                    DrawCell drawCell = DrawCell.generateTerrainCell(cellWorldPos,this.chunkTree.getMaxLevel() - child.getLevel());
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
                DrawCell cell = node.getData();
                List<DrawCellFace> highResFaces = null;
                if(this.shouldSolveFaces(node,playerPos, distCache)){
                    highResFaces = this.solveHighResFace(node);
                }
    
                //actually send requests
                if(this.requestChunks(node, highResFaces)){
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
    
                //high res faces
                List<DrawCellFace> highResFaces = null;
                if(this.shouldSolveFaces(node,playerPos, distCache)){
                    highResFaces = this.solveHighResFace(node);
                }
    
                if(this.containsDataToGenerate(node,highResFaces)){
                    node.getData().generateDrawableEntity(textureAtlas, lodLevel, highResFaces);
                    if(node.getData().getFailedGenerationAttempts() > FAILED_GENERATION_ATTEMPT_THRESHOLD){
                        node.getData().setHasRequested(false);
                    }
                    this.generationLastFrameCount++;
                } else if(node.getData() != null){
                    node.getData().setFailedGenerationAttempts(node.getData().getFailedGenerationAttempts() + 1);
                    if(node.getData().getFailedGenerationAttempts() > FAILED_GENERATION_ATTEMPT_THRESHOLD){
                        node.getData().setHasRequested(false);
                    }
                    this.waitingOnNetworkCount++;
                }
                evaluationMap.put(node,true);
                Globals.profiler.endCpuSample();
                updated = true;
            }
        } else {
            if(this.shouldJoin(playerPos, node, distCache)) {
                if(node.getMinBound().x == 192 && node.getMinBound().y == 0 && node.getMinBound().z == 192){
                    System.out.println("Joining target node");
                }
                this.join(node);
                this.partitionLastFrameCount++;
                updated = true;
            } else {
                this.validCellCount++;
                List<WorldOctTreeNode<DrawCell>> children = node.getChildren();
                boolean isHomogenous = true;
                boolean fullyGenerated = true;
                for(int i = 0; i < 8; i++){
                    WorldOctTreeNode<DrawCell> child = children.get(i);
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
                WorldOctTreeNode<DrawCell> newNode = null;
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
    public long getMinDistance(Vector3i worldPos, WorldOctTreeNode<DrawCell> node, int distCache){
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
    public boolean shouldSplit(Vector3i pos, WorldOctTreeNode<DrawCell> node, int distCache){
        //breaking out into dedicated function so can add case handling ie if we want
        //to combine fullres nodes into larger nodes to conserve on draw calls
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
             ||
            (
                node.getLevel() < this.chunkTree.getMaxLevel() - HALF_RES_LOD &&
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
    private int getLODLevel(WorldOctTreeNode<DrawCell> node){
        return this.chunkTree.getMaxLevel() - node.getLevel();
    }

    /**
     * Tracks whether the high res faces should be solved for or not
     * @param node The node
     * @param playerWorldPos The player's world position
     * @return true if should solve for high res faces, false otherwise
     */
    private boolean shouldSolveFaces(WorldOctTreeNode<DrawCell> node, Vector3i playerWorldPos, int distCache){
        if(node.getLevel() == this.chunkTree.getMaxLevel()){
            return false;
        }
        int lod = this.chunkTree.getMaxLevel() - node.getLevel();
        if(lod > SIXTEENTH_RES_LOD){
            return false;
        }
        switch(lod){
            case HALF_RES_LOD: {
                return this.getMinDistance(playerWorldPos, node, distCache) > HALF_RES_DIST;
            }
            case QUARTER_RES_LOD: {
                return this.getMinDistance(playerWorldPos, node, distCache) > QUARTER_RES_DIST;
            }
            case EIGHTH_RES_LOD: {
                return this.getMinDistance(playerWorldPos, node, distCache) > EIGHTH_RES_DIST;
            }
            case SIXTEENTH_RES_LOD: {
                return this.getMinDistance(playerWorldPos, node, distCache) > SIXTEENTH_RES_DIST;
            }
            default: {
                throw new Error("Unsupported lod: " + lod);
            }
        }
    }

    /**
     * Solves which face (if any) is the high res face for a LOD chunk
     * @param node The node for the chunk
     * @return The face if there is a higher resolution face, null otherwise
     */
    private List<DrawCellFace> solveHighResFace(WorldOctTreeNode<DrawCell> node){
        //don't bother to check if it's a full res chunk
        if(node.getLevel() == this.chunkTree.getMaxLevel()){
            return null;
        }
        if(this.chunkTree.getMaxLevel() - node.getLevel() > SIXTEENTH_RES_LOD){
            return null;
        }
        int lodMultiplitier = this.chunkTree.getMaxLevel() - node.getLevel() + 1;
        int spacing = (int)Math.pow(2,lodMultiplitier);
        List<DrawCellFace> faces = new LinkedList<DrawCellFace>();
        if(node.getMinBound().x - 1 >= 0){
            WorldOctTreeNode<DrawCell> xNegNode = this.chunkTree.search(new Vector3i(node.getMinBound()).add(-1,1,1), false);
            if(xNegNode != null && xNegNode.getLevel() > node.getLevel()){
                faces.add(DrawCellFace.X_NEGATIVE);
            }
        }
        if(node.getMinBound().y - 1 >= 0){
            WorldOctTreeNode<DrawCell> yNegNode = this.chunkTree.search(new Vector3i(node.getMinBound()).add(1,-1,1), false);
            if(yNegNode != null && yNegNode.getLevel() > node.getLevel()){
                faces.add(DrawCellFace.Y_NEGATIVE);
            }
        }
        if(node.getMinBound().z - 1 >= 0){
            WorldOctTreeNode<DrawCell> zNegNode = this.chunkTree.search(new Vector3i(node.getMinBound()).add(1,1,-1), false);
            if(zNegNode != null && zNegNode.getLevel() > node.getLevel()){
                faces.add(DrawCellFace.Z_NEGATIVE);
            }
        }
        if(node.getMaxBound().x + spacing + 1 < this.worldDim){
            WorldOctTreeNode<DrawCell> xPosNode = this.chunkTree.search(new Vector3i(node.getMinBound()).add(spacing + 1,1,1), false);
            if(xPosNode != null && xPosNode.getLevel() > node.getLevel()){
                faces.add(DrawCellFace.X_POSITIVE);
            }
        }
        if(node.getMaxBound().y + spacing + 1 < this.worldDim){
            WorldOctTreeNode<DrawCell> yPosNode = this.chunkTree.search(new Vector3i(node.getMinBound()).add(1,spacing + 1,1), false);
            if(yPosNode != null && yPosNode.getLevel() > node.getLevel()){
                faces.add(DrawCellFace.Y_POSITIVE);
            }
        }
        if(node.getMaxBound().z + spacing + 1 < this.worldDim){
            WorldOctTreeNode<DrawCell> zPosNode = this.chunkTree.search(new Vector3i(node.getMinBound()).add(1,1,spacing + 1), false);
            if(zPosNode != null && zPosNode.getLevel() > node.getLevel()){
                faces.add(DrawCellFace.Z_POSITIVE);
            }
        }
        if(faces.size() > 0){
            return faces;
        }
        return null;
    }

    /**
     * Conditionally updates all adjacent nodes if their level would require transition cells in the voxel rasterization
     * @param node The node to search from adjacencies from
     * @param level The level to check against
     */
    private void conditionalUpdateAdjacentNodes(WorldOctTreeNode<DrawCell> node, int level){
        //don't bother to check if it's a lowest-res chunk
        if(this.chunkTree.getMaxLevel() - level > ClientDrawCellManager.FULL_RES_LOD){
            return;
        }
        if(node.getMinBound().x - 1 >= 0){
            WorldOctTreeNode<DrawCell> xNegNode = this.chunkTree.search(new Vector3i(node.getMinBound()).add(-1,0,0), false);
            if(xNegNode != null && xNegNode.getLevel() < level){
                xNegNode.getData().setHasGenerated(false);
            }
        }
        if(node.getMinBound().y - 1 >= 0){
            WorldOctTreeNode<DrawCell> yNegNode = this.chunkTree.search(new Vector3i(node.getMinBound()).add(0,-1,0), false);
            if(yNegNode != null && yNegNode.getLevel() < level){
                yNegNode.getData().setHasGenerated(false);
            }
        }
        if(node.getMinBound().z - 1 >= 0){
            WorldOctTreeNode<DrawCell> zNegNode = this.chunkTree.search(new Vector3i(node.getMinBound()).add(0,0,-1), false);
            if(zNegNode != null && zNegNode.getLevel() < level){
                zNegNode.getData().setHasGenerated(false);
            }
        }
        if(node.getMaxBound().x + 1 < this.worldDim){
            WorldOctTreeNode<DrawCell> xPosNode = this.chunkTree.search(new Vector3i(node.getMaxBound()).add(1,-1,-1), false);
            if(xPosNode != null && xPosNode.getLevel() < level){
                xPosNode.getData().setHasGenerated(false);
            }
        }
        if(node.getMaxBound().y + 1 < this.worldDim){
            WorldOctTreeNode<DrawCell> yPosNode = this.chunkTree.search(new Vector3i(node.getMaxBound()).add(-1,1,-1), false);
            if(yPosNode != null && yPosNode.getLevel() < level){
                yPosNode.getData().setHasGenerated(false);
            }
        }
        if(node.getMaxBound().z  + 1 < this.worldDim){
            WorldOctTreeNode<DrawCell> zPosNode = this.chunkTree.search(new Vector3i(node.getMaxBound()).add(-1,-1,1), false);
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
    private boolean isMeta(Vector3i pos, WorldOctTreeNode<DrawCell> node, int distCache){
        return 
        node.getLevel() < this.chunkTree.getMaxLevel() - SIXTEENTH_RES_LOD &&
        this.getMinDistance(pos, node, distCache) > SIXTEENTH_RES_DIST
        ;
    }

    /**
     * Sets this node to be a meta node
     * @param node The node
     */
    private void flagAsMeta(WorldOctTreeNode<DrawCell> node){
        node.getData().setHasGenerated(true);
    }

    /**
     * Gets whether this should be joined or not
     * @param pos the player position
     * @param node The node
     * @return true if should be joined, false otherwise
     */
    public boolean shouldJoin(Vector3i pos, WorldOctTreeNode<DrawCell> node, int distCache){
        //breaking out into dedicated function so can add case handling ie if we want
        //to combine fullres nodes into larger nodes to conserve on draw calls
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
    private WorldOctTreeNode<DrawCell> join(WorldOctTreeNode<DrawCell> node){
        Globals.profiler.beginCpuSample("ClientDrawCellManager.join");

        //queue destructions prior to join -- the join operator clears all children on node
        this.recursivelyDestroy(node);

        //perform op
        Globals.profiler.beginCpuSample("ClientDrawCellManager.join - Perform Op");
        DrawCell newLeafCell = DrawCell.generateTerrainCell(node.getMinBound(),node.getData().lod);
        Globals.profiler.beginCpuSample("ClientDrawCellManager.join - Perform Op - Tree join");
        WorldOctTreeNode<DrawCell> newLeaf = chunkTree.join(node, newLeafCell);
        Globals.profiler.endCpuSample();
        newLeaf.getData().transferChunkData(node.getData());
        newLeaf.getData().setHasGenerated(false);
        Globals.profiler.endCpuSample();

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
    public boolean shouldRequest(Vector3i pos, WorldOctTreeNode<DrawCell> node, int minLeafLod, int distCache){
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
    public boolean shouldGenerate(Vector3i pos, WorldOctTreeNode<DrawCell> node, int minLeafLod, int distCache){
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
    public boolean shouldDestroy(WorldOctTreeNode<DrawCell> node){
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
    private void recursivelyDestroy(WorldOctTreeNode<DrawCell> node){
        if(node.getChildren().size() > 0){
            for(WorldOctTreeNode<DrawCell> child : node.getChildren()){
                this.recursivelyDestroy(child);
            }
        }
        node.getData().destroy();
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
        DrawCell drawCell = this.getDrawCell(worldX, worldY, worldZ);
        drawCell.ejectChunkData();
        drawCell.setHasGenerated(false);
        drawCell.setHasRequested(false);
    }

    /**
     * Requests all chunks for a given draw cell
     * @param cell The cell
     * @return true if all cells were successfully requested, false otherwise
     */
    private boolean requestChunks(WorldOctTree.WorldOctTreeNode<DrawCell> node, List<DrawCellFace> highResFaces){
        DrawCell cell = node.getData();
        int lod = this.chunkTree.getMaxLevel() - node.getLevel();
        int spacingFactor = (int)Math.pow(2,lod);
        Vector3i worldPos = node.getMinBound();
        if(
            worldPos.x >= 0 &&
            worldPos.x < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
            worldPos.y >= 0 &&
            worldPos.y < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
            worldPos.z >= 0 &&
            worldPos.z < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
            !Globals.clientState.clientTerrainManager.containsChunkDataAtWorldPoint(worldPos.x, worldPos.y, worldPos.z, lod)
            ){
                //client should request chunk data from server for each chunk necessary to create the model
                LoggerInterface.loggerNetworking.DEBUG("(Client) Send Request for terrain at " + worldPos);
                if(!Globals.clientState.clientTerrainManager.requestChunk(worldPos.x, worldPos.y, worldPos.z, lod)){
                    return false;
                }
        }
        int highResLod = this.chunkTree.getMaxLevel() - (node.getLevel() + 1);
        int highResSpacingFactor = (int)Math.pow(2,highResLod);
        if(highResFaces != null){
            for(DrawCellFace highResFace : highResFaces){
                //x & y are in face-space
                for(int x = 0; x < 3; x++){
                    for(int y = 0; y < 3; y++){
                        Vector3i posToCheck = null;
                        //implicitly performing transforms to adapt from face-space to world space
                        switch(highResFace){
                            case X_POSITIVE: {
                                posToCheck = new Vector3i(cell.getWorldPos()).add(spacingFactor,x*highResSpacingFactor,y*highResSpacingFactor);
                            } break;
                            case X_NEGATIVE: {
                                posToCheck = new Vector3i(cell.getWorldPos()).add(0,x*highResSpacingFactor,y*highResSpacingFactor);
                            } break;
                            case Y_POSITIVE: {
                                posToCheck = new Vector3i(cell.getWorldPos()).add(x*highResSpacingFactor,spacingFactor,y*highResSpacingFactor);
                            } break;
                            case Y_NEGATIVE: {
                                posToCheck = new Vector3i(cell.getWorldPos()).add(x*highResSpacingFactor,0,y*highResSpacingFactor);
                            } break;
                            case Z_POSITIVE: {
                                posToCheck = new Vector3i(cell.getWorldPos()).add(x*highResSpacingFactor,y*highResSpacingFactor,spacingFactor);
                            } break;
                            case Z_NEGATIVE: {
                                posToCheck = new Vector3i(cell.getWorldPos()).add(x*highResSpacingFactor,y*highResSpacingFactor,0);
                            } break;
                        }
                        if(
                            posToCheck.x >= 0 &&
                            posToCheck.x < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
                            posToCheck.y >= 0 &&
                            posToCheck.y < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
                            posToCheck.z >= 0 &&
                            posToCheck.z < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
                            !Globals.clientState.clientTerrainManager.containsChunkDataAtWorldPoint(posToCheck.x, posToCheck.y, posToCheck.z, highResLod)
                            ){
                            LoggerInterface.loggerNetworking.DEBUG("(Client) Send Request for terrain at " + posToCheck);
                            if(!Globals.clientState.clientTerrainManager.requestChunk(posToCheck.x, posToCheck.y, posToCheck.z, highResLod)){
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Checks if all chunk data required to generate this draw cell is present
     * @param node The node
     * @param highResFace The higher resolution face of a not-full-resolution chunk. Null if the chunk is max resolution or there is no higher resolution face for the current chunk
     * @return true if all data is available, false otherwise
     */
    private boolean containsDataToGenerate(WorldOctTree.WorldOctTreeNode<DrawCell> node, List<DrawCellFace> highResFaces){
        DrawCell cell = node.getData();
        int lod = this.chunkTree.getMaxLevel() - node.getLevel();
        int spacingFactor = (int)Math.pow(2,lod);
        Vector3i worldPos = cell.getWorldPos();
        if(!Globals.clientState.clientTerrainManager.containsChunkDataAtWorldPoint(worldPos.x, worldPos.y, worldPos.z, lod)){
            return false;
        }
        int highResLod = this.chunkTree.getMaxLevel() - (node.getLevel() + 1);
        int highResSpacingFactor = (int)Math.pow(2,highResLod);
        if(highResFaces != null){
            for(DrawCellFace highResFace : highResFaces){
                //x & y are in face-space
                for(int x = 0; x < 2; x++){
                    for(int y = 0; y < 2; y++){
                        Vector3i posToCheck = null;
                        //implicitly performing transforms to adapt from face-space to world space
                        switch(highResFace){
                            case X_POSITIVE: {
                                posToCheck = new Vector3i(cell.getWorldPos()).add(spacingFactor,x*highResSpacingFactor,y*highResSpacingFactor);
                            } break;
                            case X_NEGATIVE: {
                                posToCheck = new Vector3i(cell.getWorldPos()).add(0,x*highResSpacingFactor,y*highResSpacingFactor);
                            } break;
                            case Y_POSITIVE: {
                                posToCheck = new Vector3i(cell.getWorldPos()).add(x*highResSpacingFactor,spacingFactor,y*highResSpacingFactor);
                            } break;
                            case Y_NEGATIVE: {
                                posToCheck = new Vector3i(cell.getWorldPos()).add(x*highResSpacingFactor,0,y*highResSpacingFactor);
                            } break;
                            case Z_POSITIVE: {
                                posToCheck = new Vector3i(cell.getWorldPos()).add(x*highResSpacingFactor,y*highResSpacingFactor,spacingFactor);
                            } break;
                            case Z_NEGATIVE: {
                                posToCheck = new Vector3i(cell.getWorldPos()).add(x*highResSpacingFactor,y*highResSpacingFactor,0);
                            } break;
                        }
                        if(
                            posToCheck.x >= 0 &&
                            posToCheck.x < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
                            posToCheck.y >= 0 &&
                            posToCheck.y < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
                            posToCheck.z >= 0 &&
                            posToCheck.z < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
                            !Globals.clientState.clientTerrainManager.containsChunkDataAtWorldPoint(posToCheck.x, posToCheck.y, posToCheck.z, highResLod)
                            ){
                            return false;
                        }
                    }
                }
            }
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
    private void recursivelyCalculateStatus(WorldOctTreeNode<DrawCell> node){
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
            List<WorldOctTreeNode<DrawCell>> children = new LinkedList<WorldOctTreeNode<DrawCell>>(node.getChildren());
            for(WorldOctTreeNode<DrawCell> child : children){
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
     * Gets the number of nodes in the tree
     * @return The number of nodes
     */
    public int getNodeCount(){
        return this.chunkTree.getNodeCount();
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
    public DrawCell getDrawCell(int worldX, int worldY, int worldZ){
        WorldOctTreeNode<DrawCell> node = this.chunkTree.search(new Vector3i(worldX,worldY,worldZ), false);
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
        DrawCell cell = this.getDrawCell(worldX, worldY, worldZ);
        if(cell != null && cell.getEntity() != null){
            return PhysicsEntityUtils.containsDBody(cell.getEntity());
        }
        return false;
    }

    /**
     * Busts the distance cache
     */
    public void bustDistanceCache(){
        this.bustDistCache = true;
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
