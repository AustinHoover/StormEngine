package electrosphere.client.terrain.foliage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.client.terrain.cache.ChunkData;
import electrosphere.engine.Globals;
import electrosphere.entity.ClientEntityUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.renderer.buffer.HomogenousUniformBuffer.HomogenousBufferTypes;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import electrosphere.renderer.buffer.ShaderAttribute;
import electrosphere.util.ds.octree.WorldOctTree.WorldOctTreeNode;
import electrosphere.util.math.GeomUtils;

/**
 * A single foliagecell - contains an entity that has a physics mesh and potentially graphics
 */
public class FoliageCell {

    /**
     * Number of frames to wait before destroying the chunk entity
     */
    public static final int FRAMES_TO_WAIT_BEFORE_DESTRUCTION = 25;

    /**
     * Number of child cells per parent cell
     */
    static final int CHILD_CELLS_PER_PARENT = 8;

    /**
     * Wiggle room in number of entries
     */
    static final int BUFFER_WIGGLE_ROOM = 200;

    /**
     * The interval to space along
     */
    static final int TARGET_FOLIAGE_SPACING = 50;

    /**
     * The target number of foliage to place per cell
     */
    static final int TARGET_FOLIAGE_PER_CELL = TARGET_FOLIAGE_SPACING * TARGET_FOLIAGE_SPACING + BUFFER_WIGGLE_ROOM;

    /**
     * The length of the ray to ground test with
     */
    static final float RAY_LENGTH = 1.0f;

    /**
     * The height above the chunk to start from when sampling downwards
     */
    static final float SAMPLE_START_HEIGHT = 0.5f;

    /**
     * The ID of the air voxel
     */
    static final int AIR_VOXEL_ID = 0;

    /**
     * The map of all attributes for instanced foliage
     */
    static final Map<ShaderAttribute,HomogenousBufferTypes> attributes = new HashMap<ShaderAttribute,HomogenousBufferTypes>();

    /**
     * Model matrix shader attribute
     */
    static ShaderAttribute modelMatrixAttribute;

    /**
     * The list of voxel type ids that should have grass generated on top of them
     */
    static final List<Integer> grassGeneratingVoxelIds = new ArrayList<Integer>();
    
    //set attributes
    static {
        int[] attributeIndices = new int[]{
            5,6,7,8
        };
        modelMatrixAttribute = new ShaderAttribute(attributeIndices);
        attributes.put(modelMatrixAttribute,HomogenousBufferTypes.MAT4F);

        //set grass generating voxel ids
        grassGeneratingVoxelIds.add(2);
    }

    /**
     * Vertex shader path
     */
    protected static final String vertexPath = "Shaders/entities/foliage/foliage.vs";

    /**
     * Fragment shader path
     */
    protected static final String fragmentPath = "Shaders/entities/foliage/foliage.fs";

    /**
     * Random for finding new positions for foliage
     */
    Random placementRandomizer = new Random();

    
    /**
     * The position of the foliage cell in world coordinates
     */
    Vector3i worldPos;

    /**
     * The position of this cell voxel-wise within its chunk
     */
    Vector3i voxelPos;


    /**
     * The LOD of the foliage cell
     */
    int lod;
    
    /**
     * The main entity for the cell
     */
    Entity modelEntity;

    /**
     * The data for generating the visuals
     */
    ChunkData chunkData;

    /**
     * Tracks whether the foliage cell has requested its chunk data or not
     */
    boolean hasRequested = false;

    /**
     * Tracks whether the foliage cell has generated its entity or not
     */
    boolean hasGenerated = false;


    /**
     * Tracks whether this foliage cell is flagged as homogenous from the server or not
     */
    boolean homogenous = false;

    /**
     * Number of failed generation attempts
     */
    int failedGenerationAttempts = 0;

    /**
     * Labels an invalid distance cache
     */
    static final int INVALID_DIST_CACHE = -1;

    /**
     * The cached minimum distance
     */
    long cachedMinDistance = -1;

    /**
     * Target to notify on generation completion
     */
    FoliageCell notifyTarget = null;

    /**
     * The number of cells that have alerted this one
     */
    int generationAlertCount = 0;

    /**
     * If set to true, FoliageCellManager should debug when evaluating this cell
     */
    boolean tripDebugFlag = false;
    
    
    /**
     * Private constructor
     */
    private FoliageCell(){
        
    }
    
    
    /**
     * Constructs a foliagecell object
     */
    public static FoliageCell generateTerrainCell(
            Vector3i voxelAbsPos,
            int lod
    ){
        FoliageCell rVal = new FoliageCell();
        rVal.lod = lod;
        rVal.worldPos = Globals.clientState.clientWorldData.convertAbsoluteVoxelToWorldSpace(voxelAbsPos);
        rVal.voxelPos = Globals.clientState.clientWorldData.convertAbsoluteVoxelToRelativeVoxelSpace(voxelAbsPos);
        return rVal;
    }

    /**
     * Constructs a homogenous foliagecell object
     */
    public static FoliageCell generateHomogenousTerrainCell(
            Vector3i voxelAbsPos,
            int lod
    ){
        FoliageCell rVal = new FoliageCell();
        rVal.lod = lod;
        rVal.worldPos = Globals.clientState.clientWorldData.convertAbsoluteVoxelToWorldSpace(voxelAbsPos);
        rVal.voxelPos = Globals.clientState.clientWorldData.convertAbsoluteVoxelToRelativeVoxelSpace(voxelAbsPos);
        rVal.hasGenerated = true;
        rVal.homogenous = true;
        return rVal;
    }
    
    /**
     * Generates a drawable entity based on this chunk
     */
    public void generateDrawableEntity(int lod){
        boolean success = true;
        if(chunkData == null){
            ChunkData currentChunk = Globals.clientState.clientTerrainManager.getChunkDataAtWorldPoint(
                worldPos.x,
                worldPos.y,
                worldPos.z,
                ChunkData.NO_STRIDE
            );
            if(currentChunk == null){
                success = false;
            } else {
                this.homogenous = currentChunk.getHomogenousValue() != ChunkData.NOT_HOMOGENOUS;
                success = true;
            }
            if(!success){
                this.setFailedGenerationAttempts(this.getFailedGenerationAttempts() + 1);
                return;
            }
            this.chunkData = currentChunk;
        }
        if(success){
            this.generate();
        }
    }


    /**
     * Generates the foliage cell
     */
    protected void generate(){
        int airID = 0;
        boolean shouldGenerate = false;
        //get foliage types supported
        List<String> foliageTypesSupported = new LinkedList<String>();
        Map<Integer,Boolean> handledTypes = new HashMap<Integer,Boolean>();
        handledTypes.put(airID,true);
        boolean airAbove = true;
        int scale = (int)Math.pow(2,lod);
        for(int x = 0; x < scale; x++){
            for(int y = 0; y < scale; y++){
                for(int z = 0; z < scale; z++){
                    int voxelType = chunkData.getType(new Vector3i(this.voxelPos).add(x,y,z));
                    if(handledTypes.containsKey(voxelType)){
                        continue;
                    }
                    if(voxelPos.y + y >= ServerTerrainChunk.CHUNK_DIMENSION){
                        continue;
                    }
                    List<String> currentList = Globals.gameConfigCurrent.getVoxelData().getTypeFromId(voxelType).getAmbientFoliage();
                    if(currentList == null){
                        handledTypes.put(voxelType,true);
                        continue;
                    }
                    foliageTypesSupported.addAll(currentList);
                    airAbove = chunkData.getType(voxelPos.x + x,voxelPos.y + y + 1,voxelPos.z + z) == airID;
                    if(foliageTypesSupported != null && foliageTypesSupported.size() > 0 && airAbove){
                        shouldGenerate = true;
                        handledTypes.put(voxelType,true);
                    }
                }
            }
        }
        // if(Math.abs(worldPos.x - 32767) < 5 && Math.abs(worldPos.y - 5) < 3 && Math.abs(worldPos.z - 32767) < 3 && this.chunkData.getHomogenousValue() != 0){
        //     System.out.println(worldPos.x + " " + worldPos.y + " " + worldPos.z + " - " + shouldGenerate);
        // }
        if(shouldGenerate){
            Entity oldEntity = this.modelEntity;
            //create entity
            this.modelEntity = FoliageModel.clientCreateFoliageChunkEntity(foliageTypesSupported,scale,this.getRealPos(),worldPos,voxelPos,notifyTarget,oldEntity);
        } else {
            if(this.modelEntity != null){
                ClientEntityUtils.destroyEntity(this.modelEntity);
                this.modelEntity = null;
            }
            this.homogenous = true;
        }
        this.hasGenerated = true;
    }

    /**
     * Gets the real-space position of the foliage cell
     * @return the real-space position
     */
    protected Vector3d getRealPos(){
        return new Vector3d(
            worldPos.x * ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET + voxelPos.x,
            worldPos.y * ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET + voxelPos.y,
            worldPos.z * ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET + voxelPos.z
        );
    }

    /**
     * Gets the world-space position of the foliage cell
     * @return the world-space position
     */
    protected Vector3i getWorldPos(){
        return new Vector3i(worldPos);
    }

    /**
     * Registers a target foliage cell to notify once this one has completed generating its model
     * @param notifyTarget The target to notify
     */
    public void registerNotificationTarget(FoliageCell notifyTarget){
        this.notifyTarget = notifyTarget;
    }

    /**
     * Alerts this foliage cell that a child it is waiting on has generated
     */
    public void alertToGeneration(){
        this.generationAlertCount++;
        if(this.generationAlertCount >= CHILD_CELLS_PER_PARENT){
            this.destroy();
        }
    }
    
    /**
     * Destroys a foliage cell including its physics
     */
    public void destroy(){
        if(modelEntity != null){
            Globals.clientState.clientScene.registerBehaviorTree(new BehaviorTree(){
                int framesSimulated = 0;
                public void simulate(float deltaTime) {
                    if(framesSimulated < FRAMES_TO_WAIT_BEFORE_DESTRUCTION){
                        framesSimulated++;
                    } else {
                        ClientEntityUtils.destroyEntity(modelEntity);
                        Globals.clientState.clientScene.deregisterBehaviorTree(this);
                    }
                }
            });
        }
    }

    /**
     * Gets the entity for the cell
     * @return The entity if it exists, null otherwise
     */
    public Entity getEntity(){
        return modelEntity;
    }

    /**
     * Transfers chunk data from the source to this foliage cell
     * @param source The source foliage cell
     */
    public void transferChunkData(FoliageCell source){
        this.chunkData = source.chunkData;
        this.homogenous = source.homogenous;
        this.hasRequested = source.hasRequested;
    }

    /**
     * Gets whether this foliage cell has requested its chunk data or not
     * @return true if has requested, false otherwise
     */
    public boolean hasRequested() {
        return hasRequested;
    }

    /**
     * Sets whether this foliage cell has requested its chunk data or not
     * @param hasRequested true if has requested, false otherwise
     */
    public void setHasRequested(boolean hasRequested) {
        this.hasRequested = hasRequested;
        if(!this.hasRequested){
            this.failedGenerationAttempts = 0;
        }
    }

    /**
     * Gets whether this foliage cell has generated its entity or not
     * @return true if has generated, false otherwise
     */
    public boolean hasGenerated() {
        return hasGenerated;
    }

    /**
     * Sets whether this foliage cell has generated its entity or not
     * @param hasGenerated true if has generated, false otherwise
     */
    public void setHasGenerated(boolean hasGenerated) {
        this.hasGenerated = hasGenerated;
    }

    /**
     * Sets whether this foliage cell is homogenous or not
     * @param hasGenerated true if is homogenous, false otherwise
     */
    public void setHomogenous(boolean homogenous) {
        this.homogenous = homogenous;
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
     * Gets whether this foliage cell is homogenous or not
     * @return true if it is homogenous, false otherwise
     */
    public boolean isHomogenous(){
        return homogenous;
    }

    /**
     * Gets the minimum distance from a node to a point
     * @param absVoxelPos the position to check against
     * @param node the node
     * @param distCache the lod value under which distance caches are invalidated
     * @return the distance
     */
    public long getMinDistance(Vector3i absVoxelPos, WorldOctTreeNode<FoliageCell> node, int distCache){
        if(cachedMinDistance != INVALID_DIST_CACHE && distCache < lod){
            return cachedMinDistance;
        } else {
            double dist = GeomUtils.approxMinDistanceAABB(absVoxelPos, node.getMinBound(), node.getMaxBound());
            if(Double.isFinite(dist)){
                this.cachedMinDistance = (long)dist;
            } else {
                this.cachedMinDistance = GeomUtils.REALLY_BIG_NUMBER;
            }
            return cachedMinDistance;
        }
    }

    @Override
    public String toString(){
        return "" +
        "worldPos: " + worldPos + "\n" +
        "voxelPos: " + voxelPos + "\n" +
        "lod: " + lod + "\n" +
        "modelEntity: " + modelEntity + "\n" +
        "hasRequested: " + hasRequested + "\n" +
        "hasGenerated: " + hasGenerated + "\n" +
        "homogenous: " + homogenous + "\n" +
        "";
    }

    /**
     * Sets whether this cell should trip the debug flag on its next evaluation or not
     * @param trip true to debug on next evaluation, false otherwise
     */
    public void setTripDebug(boolean trip){
        this.tripDebugFlag = trip;
    }

    /**
     * Gets whether this cell should trigger debugging on next evaluation or not
     * @return true to debug on next evaluation, false otherwise
     */
    public boolean getTripDebug(){
        return tripDebugFlag;
    }
    
    
}
