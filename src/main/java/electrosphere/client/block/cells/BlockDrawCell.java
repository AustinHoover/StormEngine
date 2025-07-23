package electrosphere.client.block.cells;

import java.util.LinkedList;
import java.util.List;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.client.block.BlockChunkData;
import electrosphere.engine.Globals;
import electrosphere.entity.ClientEntityUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.types.terrain.BlockChunkEntity;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import electrosphere.util.ds.octree.WorldOctTree.WorldOctTreeNode;
import electrosphere.util.math.GeomUtils;

/**
 * A single drawcell - contains an entity that has a physics mesh and potentially graphics
 */
public class BlockDrawCell {

    /**
     * Number of frames to wait before destroying the chunk entity
     */
    public static final int FRAMES_TO_WAIT_BEFORE_DESTRUCTION = 25;

    /**
     * Number of child cells per parent cell
     */
    private static final int CHILD_CELLS_PER_PARENT = 8;
    
    /**
     * the position of the draw cell in world coordinates
     */
    private Vector3i worldPos;

    /**
     * The LOD of the draw cell
     */
    protected int lod;
    
    /**
     * the main entity for the cell
     */
    private List<Entity> modelEntities = new LinkedList<Entity>();

    /**
     * The data for generating the visuals
     */
    private BlockChunkData chunkData;

    /**
     * Tracks whether the draw cell has requested its chunk data or not
     */
    private boolean hasRequested = false;

    /**
     * Tracks whether the draw cell has generated its entity or not
     */
    private boolean hasGenerated = false;


    /**
     * Tracks whether this draw cell is flagged as homogenous from the server or not
     */
    private boolean homogenous = false;

    /**
     * Number of failed generation attempts
     */
    private int failedGenerationAttempts = 0;

    /**
     * Labels an invalid distance cache
     */
    private static final int INVALID_DIST_CACHE = -1;

    /**
     * The cached minimum distance
     */
    private long cachedMinDistance = -1;

    /**
     * Target to notify on generation completion
     */
    private BlockDrawCell notifyTarget = null;

    /**
     * The number of cells that have alerted this one
     */
    private int generationAlertCount = 0;
    
    
    /**
     * Private constructor
     */
    private BlockDrawCell(){
        
    }
    
    
    /**
     * Constructs a drawcell object
     */
    public static BlockDrawCell generateBlockCell(
            Vector3i worldPos,
            int lod
    ){
        BlockDrawCell rVal = new BlockDrawCell();
        rVal.lod = lod;
        rVal.worldPos = worldPos;
        return rVal;
    }

    /**
     * Generates a drawable entity based on this chunk
     */
    public void generateDrawableEntity(BlockTextureAtlas atlas, int lod){
        boolean success = true;
        if(chunkData == null){
            BlockChunkData currentChunk = Globals.clientState.clientBlockManager.getChunkDataAtWorldPoint(
                worldPos.x,
                worldPos.y,
                worldPos.z,
                lod
            );
            if(currentChunk == null){
                success = false;
            } else {
                this.homogenous = currentChunk.getHomogenousValue() != BlockChunkData.NOT_HOMOGENOUS;
                success = true;
            }
            if(!success){
                this.setFailedGenerationAttempts(this.getFailedGenerationAttempts() + 1);
                return;
            }
            this.chunkData = currentChunk;
        }
        List<Entity> toDelete = this.modelEntities;
        this.modelEntities = BlockChunkEntity.clientCreateBlockChunkEntity(chunkData, notifyTarget, toDelete, lod, atlas, this.hasPolygons());
        for(Entity ent : modelEntities){
            ClientEntityUtils.initiallyPositionEntity(ent, this.getRealPos(), new Quaterniond());
        }
        this.setHasGenerated(true);
    }

    /**
     * Gets the real-space position of the draw cell
     * @return the real-space position
     */
    protected Vector3d getRealPos(){
        return new Vector3d(
            worldPos.x * ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET,
            worldPos.y * ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET,
            worldPos.z * ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET
        );
    }

    /**
     * Gets the world-space position of the draw cell
     * @return the world-space position
     */
    protected Vector3i getWorldPos(){
        return new Vector3i(worldPos);
    }

    /**
     * Registers a target draw cell to notify once this one has completed generating its model
     * @param notifyTarget The target to notify
     */
    public void registerNotificationTarget(BlockDrawCell notifyTarget){
        this.notifyTarget = notifyTarget;
    }

    /**
     * Alerts this draw cell that a child it is waiting on has generated
     */
    public void alertToGeneration(){
        this.generationAlertCount++;
        if(this.generationAlertCount >= CHILD_CELLS_PER_PARENT){
            this.destroy();
        }
    }
    
    /**
     * Destroys a drawcell including its physics
     */
    public void destroy(){
        if(this.modelEntities != null){
            for(Entity ent : this.modelEntities){
                Globals.clientState.clientScene.registerBehaviorTree(new BehaviorTree(){
                    int framesSimulated = 0;
                    public void simulate(float deltaTime) {
                        if(framesSimulated < FRAMES_TO_WAIT_BEFORE_DESTRUCTION){
                            framesSimulated++;
                        } else {
                            ClientEntityUtils.destroyEntity(ent);
                            Globals.clientState.clientScene.deregisterBehaviorTree(this);
                        }
                    }
                });
            }
        }
    }

    /**
     * Gets the list of entities for the cell
     * @return The list of entities if it exists, null otherwise
     */
    public List<Entity> getEntities(){
        return this.modelEntities;
    }

    /**
     * Transfers chunk data from the source to this draw cell
     * @param source The source draw cell
     */
    public void transferChunkData(BlockDrawCell source){
        this.chunkData = source.chunkData;
        this.homogenous = source.homogenous;
        this.hasRequested = source.hasRequested;
    }

    /**
     * Gets whether this draw cell has requested its chunk data or not
     * @return true if has requested, false otherwise
     */
    public boolean hasRequested() {
        return hasRequested;
    }

    /**
     * Sets whether this draw cell has requested its chunk data or not
     * @param hasRequested true if has requested, false otherwise
     */
    public void setHasRequested(boolean hasRequested) {
        this.hasRequested = hasRequested;
        if(!this.hasRequested){
            this.failedGenerationAttempts = 0;
        }
    }

    /**
     * Gets whether this draw cell has generated its entity or not
     * @return true if has generated, false otherwise
     */
    public boolean hasGenerated() {
        return hasGenerated;
    }

    /**
     * Sets whether this draw cell has generated its entity or not
     * @param hasGenerated true if has generated, false otherwise
     */
    public void setHasGenerated(boolean hasGenerated) {
        this.hasGenerated = hasGenerated;
    }

    /**
     * Sets whether this draw cell is homogenous or not
     * @param hasGenerated true if is homogenous, false otherwise
     */
    public void setHomogenous(boolean homogenous) {
        this.homogenous = homogenous;
    }

    /**
     * Gets whether this draw cell will generate polygons or not
     * @return true if it has polygons, false otherwise
     */
    private boolean hasPolygons(){
        return !this.homogenous;
    }

    /**
     * Gets the number of failed generation attempts
     * @return The number of failed generation attempts
     */
    public int getFailedGenerationAttempts(){
        return failedGenerationAttempts;
    }

    /**
     * Sets the number of failed generation attempts
     * @param attempts The number of failed generation attempts
     */
    public void setFailedGenerationAttempts(int attempts){
        this.failedGenerationAttempts = this.failedGenerationAttempts + attempts;
    }

    /**
     * Ejects the chunk data
     */
    public void ejectChunkData(){
        this.chunkData = null;
    }

    /**
     * Gets whether this draw cell is homogenous or not
     * @return true if it is homogenous, false otherwise
     */
    public boolean isHomogenous(){
        return homogenous;
    }

    /**
     * Gets the minimum distance from a node to a point
     * @param pos the position to check against
     * @param node the node
     * @param distCache the lod value under which distance caches are invalidated
     * @return the distance
     */
    public long getMinDistance(Vector3i worldPos, WorldOctTreeNode<BlockDrawCell> node, int distCache){
        if(cachedMinDistance != INVALID_DIST_CACHE && distCache < lod){
            return cachedMinDistance;
        } else {
            double dist = GeomUtils.approxMinDistanceAABB(worldPos, node.getMinBound(), node.getMaxBound());
            if(Double.isFinite(dist)){
                this.cachedMinDistance = (long)dist;
            } else {
                this.cachedMinDistance = GeomUtils.REALLY_BIG_NUMBER;
            }
            return cachedMinDistance;
        }
    }
    
    
}
