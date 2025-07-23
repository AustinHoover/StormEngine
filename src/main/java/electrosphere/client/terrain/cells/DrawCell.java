package electrosphere.client.terrain.cells;

import java.util.List;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.client.terrain.cache.ChunkData;
import electrosphere.engine.Globals;
import electrosphere.entity.ClientEntityUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.types.terrain.TerrainChunk;
import electrosphere.renderer.meshgen.TransvoxelModelGeneration;
import electrosphere.renderer.meshgen.TransvoxelModelGeneration.TransvoxelChunkData;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import electrosphere.util.ds.octree.WorldOctTree.WorldOctTreeNode;
import electrosphere.util.math.GeomUtils;

/**
 * A single drawcell - contains an entity that has a physics mesh and potentially graphics
 */
public class DrawCell {

    /**
     * Number of frames to wait before destroying the chunk entity
     */
    public static final int FRAMES_TO_WAIT_BEFORE_DESTRUCTION = 25;

    /**
     * Number of child cells per parent cell
     */
    static final int CHILD_CELLS_PER_PARENT = 8;

    /**
     * Enum for the different faces of a draw cell -- used when filling in data for higher LOD faces
     */
    public enum DrawCellFace {
        X_POSITIVE,
        X_NEGATIVE,
        Y_POSITIVE,
        Y_NEGATIVE,
        Z_POSITIVE,
        Z_NEGATIVE,
    }
    
    //the position of the draw cell in world coordinates
    Vector3i worldPos;


    /**
     * The LOD of the draw cell
     */
    int lod;
    
    //the main entity for the cell
    Entity modelEntity;

    /**
     * The data for generating the visuals
     */
    TransvoxelChunkData chunkData;

    /**
     * Tracks whether the draw cell has requested its chunk data or not
     */
    boolean hasRequested = false;

    /**
     * Tracks whether the draw cell has generated its entity or not
     */
    boolean hasGenerated = false;


    /**
     * Tracks whether this draw cell is flagged as homogenous from the server or not
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
    DrawCell notifyTarget = null;

    /**
     * The number of cells that have alerted this one
     */
    int generationAlertCount = 0;
    
    
    /**
     * Private constructor
     */
    private DrawCell(){
        
    }
    
    
    /**
     * Constructs a drawcell object
     */
    public static DrawCell generateTerrainCell(
            Vector3i worldPos,
            int lod
    ){
        DrawCell rVal = new DrawCell();
        rVal.lod = lod;
        rVal.worldPos = worldPos;
        return rVal;
    }

    /**
     * Constructs a homogenous drawcell object
     */
    public static DrawCell generateHomogenousTerrainCell(
            Vector3i worldPos,
            int lod
    ){
        DrawCell rVal = new DrawCell();
        rVal.lod = lod;
        rVal.worldPos = worldPos;
        rVal.hasGenerated = true;
        rVal.homogenous = true;
        return rVal;
    }
    
    /**
     * Generates a drawable entity based on this chunk
     */
    public void generateDrawableEntity(VoxelTextureAtlas atlas, int lod, List<DrawCellFace> higherLODFaces){
        boolean success = true;
        if(chunkData == null){
            ChunkData currentChunk = Globals.clientState.clientTerrainManager.getChunkDataAtWorldPoint(
                worldPos.x,
                worldPos.y,
                worldPos.z,
                lod
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
            this.chunkData = new TransvoxelChunkData(currentChunk.getVoxelWeight(), currentChunk.getVoxelType(), lod);
        }
        if(higherLODFaces != null){
            for(DrawCellFace face : higherLODFaces){
                Globals.profiler.beginCpuSample("DrawCell.fillInFaceData");
                success = this.fillInFaceData(this.chunkData,face,lod);
                Globals.profiler.endCpuSample();
                if(!success){
                    this.setFailedGenerationAttempts(this.getFailedGenerationAttempts() + 1);
                    return;
                }
            }
        }
        Entity toDelete = this.modelEntity;
        modelEntity = TerrainChunk.clientCreateTerrainChunkEntity(
            this.chunkData,
            this.notifyTarget,
            toDelete,
            lod,
            atlas,
            this.worldPos,
            this.hasPolygons()
        );
        ClientEntityUtils.initiallyPositionEntity(modelEntity, this.getRealPos(), new Quaterniond());
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
    public void registerNotificationTarget(DrawCell notifyTarget){
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
        if(modelEntity != null){
            Entity target = this.modelEntity;
            Globals.clientState.clientScene.registerBehaviorTree(new BehaviorTree(){
                int framesSimulated = 0;
                public void simulate(float deltaTime) {
                    if(framesSimulated < FRAMES_TO_WAIT_BEFORE_DESTRUCTION){
                        framesSimulated++;
                    } else {
                        ClientEntityUtils.destroyEntity(target);
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
     * Transfers chunk data from the source to this draw cell
     * @param source The source draw cell
     */
    public void transferChunkData(DrawCell source){
        this.chunkData = source.chunkData;
        this.homogenous = source.homogenous;
        this.hasRequested = source.hasRequested;
    }


    /**
     * Fills in the data for the higher resolution face
     * @param chunkData The data for the chunk to generate
     * @param higherLODFace The face that is higher LOD
     * @param lod The Level of Detail for this chunk
     * @return true if successfully filled in data, false otherwise
     */
    private boolean fillInFaceData(TransvoxelChunkData chunkData, DrawCellFace higherLODFace, int lod){
        int mainSpacing = (int)Math.pow(2,lod);
        int higherLOD = lod - 1;
        int higherResSpacing = (int)Math.pow(2,higherLOD);
        float[][] faceWeights = new float[TransvoxelModelGeneration.FACE_DATA_DIMENSIONS][TransvoxelModelGeneration.FACE_DATA_DIMENSIONS];
        int[][] faceTypes = new int[TransvoxelModelGeneration.FACE_DATA_DIMENSIONS][TransvoxelModelGeneration.FACE_DATA_DIMENSIONS];
        //allocate face array
        for(int x = 0; x < TransvoxelModelGeneration.FACE_DATA_DIMENSIONS; x++){
            for(int y = 0; y < TransvoxelModelGeneration.FACE_DATA_DIMENSIONS; y++){
                int worldCoordOffset1 = x / ChunkData.CHUNK_DATA_SIZE * higherResSpacing;
                int worldCoordOffset2 = y / ChunkData.CHUNK_DATA_SIZE * higherResSpacing;
                //solve coordinates relative to the face
                int localCoord1 = x % ChunkData.CHUNK_DATA_SIZE;
                int localCoord2 = y % ChunkData.CHUNK_DATA_SIZE;

                //implicitly performing transforms to adapt from face-space to world & local space
                switch(higherLODFace){
                    case X_POSITIVE: {
                        ChunkData currentChunk = Globals.clientState.clientTerrainManager.getChunkDataAtWorldPoint(
                            new Vector3i(
                                worldPos.x + mainSpacing,
                                worldPos.y + worldCoordOffset1,
                                worldPos.z + worldCoordOffset2
                            ),
                            higherLOD
                        );
                        if(currentChunk == null){
                            return false;
                        }
                        if(currentChunk.getHomogenousValue() == ChunkData.NOT_HOMOGENOUS){
                            this.homogenous = false;
                            faceWeights[x][y] = currentChunk.getWeight(
                                0,
                                localCoord1,
                                localCoord2
                            );
                            faceTypes[x][y] = currentChunk.getType(
                                0,
                                localCoord1,
                                localCoord2
                            );
                        }
                    } break;
                    case X_NEGATIVE: {
                        ChunkData currentChunk = Globals.clientState.clientTerrainManager.getChunkDataAtWorldPoint(
                            new Vector3i(
                                worldPos.x,
                                worldPos.y + worldCoordOffset1,
                                worldPos.z + worldCoordOffset2
                            ),
                            higherLOD
                        );
                        if(currentChunk == null){
                            return false;
                        }
                        if(currentChunk.getHomogenousValue() == ChunkData.NOT_HOMOGENOUS){
                            this.homogenous = false;
                            faceWeights[x][y] = currentChunk.getWeight(
                                0,
                                localCoord1,
                                localCoord2
                            );
                            faceTypes[x][y] = currentChunk.getType(
                                0,
                                localCoord1,
                                localCoord2
                            );
                        }
                    } break;
                    case Y_POSITIVE: {
                        ChunkData currentChunk = Globals.clientState.clientTerrainManager.getChunkDataAtWorldPoint(
                            new Vector3i(
                                worldPos.x + worldCoordOffset1,
                                worldPos.y + mainSpacing,
                                worldPos.z + worldCoordOffset2
                            ),
                            higherLOD
                        );
                        if(currentChunk == null){
                            return false;
                        }
                        if(currentChunk.getHomogenousValue() == ChunkData.NOT_HOMOGENOUS){
                            this.homogenous = false;
                            faceWeights[x][y] = currentChunk.getWeight(
                                localCoord1,
                                0,
                                localCoord2
                            );
                            faceTypes[x][y] = currentChunk.getType(
                                localCoord1,
                                0,
                                localCoord2
                            );
                        }
                    } break;
                    case Y_NEGATIVE: {
                        ChunkData currentChunk = Globals.clientState.clientTerrainManager.getChunkDataAtWorldPoint(
                            new Vector3i(
                                worldPos.x + worldCoordOffset1,
                                worldPos.y,
                                worldPos.z + worldCoordOffset2
                            ),
                            higherLOD
                        );
                        if(currentChunk == null){
                            return false;
                        }
                        if(currentChunk.getHomogenousValue() == ChunkData.NOT_HOMOGENOUS){
                            this.homogenous = false;
                            faceWeights[x][y] = currentChunk.getWeight(
                                localCoord1,
                                0,
                                localCoord2
                            );
                            faceTypes[x][y] = currentChunk.getType(
                                localCoord1,
                                0,
                                localCoord2
                            );
                        }
                    } break;
                    case Z_POSITIVE: {
                        ChunkData currentChunk = Globals.clientState.clientTerrainManager.getChunkDataAtWorldPoint(
                            new Vector3i(
                                worldPos.x + worldCoordOffset1,
                                worldPos.y + worldCoordOffset2,
                                worldPos.z + mainSpacing
                            ),
                            higherLOD
                        );
                        if(currentChunk == null){
                            return false;
                        }
                        if(currentChunk.getHomogenousValue() == ChunkData.NOT_HOMOGENOUS){
                            this.homogenous = false;
                            faceWeights[x][y] = currentChunk.getWeight(
                                localCoord1,
                                localCoord2,
                                0
                            );
                            faceTypes[x][y] = currentChunk.getType(
                                localCoord1,
                                localCoord2,
                                0
                            );
                        }
                    } break;
                    case Z_NEGATIVE: {
                        ChunkData currentChunk = Globals.clientState.clientTerrainManager.getChunkDataAtWorldPoint(
                            new Vector3i(
                                worldPos.x + worldCoordOffset1,
                                worldPos.y + worldCoordOffset2,
                                worldPos.z
                            ),
                            higherLOD
                        );
                        if(currentChunk == null){
                            return false;
                        }
                        if(currentChunk.getHomogenousValue() == ChunkData.NOT_HOMOGENOUS){
                            this.homogenous = false;
                            faceWeights[x][y] = currentChunk.getWeight(
                                localCoord1,
                                localCoord2,
                                0
                            );
                            faceTypes[x][y] = currentChunk.getType(
                                localCoord1,
                                localCoord2,
                                0
                            );
                        }
                    } break;
                }
                // Vector3i sampleChunkWorldPos = new Vector3i(
                //     worldPos.x + (x * higherResSpacing) / ChunkData.CHUNK_SIZE,
                //     worldPos.y + (y * higherResSpacing) / ChunkData.CHUNK_SIZE,
                //     worldPos.z + (z * spacingFactor) / ChunkData.CHUNK_SIZE
                // );
                // ChunkData currentChunk = Globals.clientTerrainManager.getChunkDataAtWorldPoint(sampleChunkWorldPos);
                // if(currentChunk == null){
                //     throw new Error("Chunk is null! " + worldPos);
                // }
                // weights[x][y][z] = currentChunk.getWeight(
                //     (x * higherResSpacing) % ChunkData.CHUNK_SIZE,
                //     (y * higherResSpacing) % ChunkData.CHUNK_SIZE,
                //     (z * spacingFactor) % ChunkData.CHUNK_SIZE
                // );
                // types[x][y][z] = currentChunk.getType(
                //     (x * higherResSpacing) % ChunkData.CHUNK_SIZE,
                //     (y * higherResSpacing) % ChunkData.CHUNK_SIZE,
                //     (z * spacingFactor) % ChunkData.CHUNK_SIZE
                // );
            }
        }
        switch(higherLODFace){
            case X_POSITIVE: {
                chunkData.addXPositiveEdge(faceWeights, faceTypes);
            } break;
            case X_NEGATIVE: {
                chunkData.addXNegativeEdge(faceWeights, faceTypes);
            } break;
            case Y_POSITIVE: {
                chunkData.addYPositiveEdge(faceWeights, faceTypes);
            } break;
            case Y_NEGATIVE: {
                chunkData.addYNegativeEdge(faceWeights, faceTypes);
            } break;
            case Z_POSITIVE: {
                chunkData.addZPositiveEdge(faceWeights, faceTypes);
            } break;
            case Z_NEGATIVE: {
                chunkData.addZNegativeEdge(faceWeights, faceTypes);
            } break;
        }
        return true;
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
    public long getMinDistance(Vector3i worldPos, WorldOctTreeNode<DrawCell> node, int distCache){
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

    @Override
    public String toString(){
        Vector3d entityPos = null;
        if(modelEntity != null){
            entityPos = EntityUtils.getPosition(modelEntity);
        }
        String rVal = "" +
        "worldPos: " + worldPos + "\n" +
        "lod: " + lod + "\n" +
        "modelEntity: " + modelEntity + "\n" +
        "entityPos: " + entityPos + "\n" +
        "hasRequested: " + hasRequested + "\n" +
        "hasGenerated: " + hasGenerated + "\n" +
        "homogenous: " + homogenous + "\n" +
        "cachedMinDistance: " + cachedMinDistance + "\n" +
        ""
        ;
        return rVal;
    }
    
    
}
