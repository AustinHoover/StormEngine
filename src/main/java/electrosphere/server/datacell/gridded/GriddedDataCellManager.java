package electrosphere.server.datacell.gridded;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.client.block.BlockChunkData;
import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.engine.threads.ThreadCounts;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.entity.state.server.ServerPlayerViewDirTree;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.EntityMessage;
import electrosphere.net.parser.net.message.TerrainMessage;
import electrosphere.net.server.player.Player;
import electrosphere.net.server.protocol.TerrainProtocol;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerDataCell;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.datacell.interfaces.DataCellManager;
import electrosphere.server.datacell.interfaces.PathfindingManager;
import electrosphere.server.datacell.interfaces.VoxelCellManager;
import electrosphere.server.datacell.physics.PhysicsDataCell;
import electrosphere.server.entity.ServerContentManager;
import electrosphere.server.entity.serialization.ContentSerialization;
import electrosphere.server.macro.MacroDataUpdater;
import electrosphere.server.macro.spatial.MacroObject;
import electrosphere.server.pathfinding.recast.PathingProgressiveData;
import electrosphere.server.pathfinding.voxel.VoxelPathfinder;
import electrosphere.server.physics.block.manager.ServerBlockManager;
import electrosphere.server.physics.fluid.manager.ServerFluidChunk;
import electrosphere.server.physics.fluid.manager.ServerFluidManager;
import electrosphere.server.physics.terrain.manager.ServerChunkCache;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import electrosphere.server.physics.terrain.manager.ServerTerrainManager;
import electrosphere.server.physics.terrain.models.TerrainModel;
import electrosphere.util.math.HashUtils;

/**
 * Implementation of DataCellManager that lays out cells in a logical grid (array). Useful for eg 3d terrain gridded world.
 */
public class GriddedDataCellManager implements DataCellManager, VoxelCellManager, PathfindingManager {

    /**
     * The minimum grid size allowed
     */
    public static final int MIN_GRID_SIZE = 1;

    /**
     * The max grid size allowed
     */
    public static final int MAX_GRID_SIZE = TerrainModel.MAX_MACRO_DATA_SIZE * TerrainModel.DEFAULT_MACRO_DATA_SCALE * ServerTerrainChunk.CHUNK_DIMENSION;

    /**
     * The number of frames without players that must pass before a server data cell is unloaded
     */
    static final int UNLOAD_FRAME_THRESHOLD = 100;

    /**
     * The distance at which simulation is queued
     */
    static final double SIMULATION_DISTANCE_CUTOFF = 5;

    /**
     * Big number used when scanning for a data cell to spawn a macro object within
     */
    static final double MACRO_SCANNING_BIG_NUMBER = 10000;

    /**
     * Used for generating physics chunks
     */
    ExecutorService generationService = null;

    /**
     * The service for loading data cells from disk
     */
    GriddedDataCellLoaderService loaderService;

    /**
     * Tracks whether this manager has been flagged to unload cells or not
     */
    boolean unloadCells = true;

    /**
     * These are going to be the natural ground grid of data cells, but we're going to have more than this
     */
    Map<Long,ServerDataCell> groundDataCells = new HashMap<Long,ServerDataCell>();

    /**
     * Map of server cell to its world position
     */
    Map<ServerDataCell,Vector3i> cellPositionMap = new HashMap<ServerDataCell,Vector3i>();

    /**
     * Map of server data cell to the number of frames said cell has had no players
     */
    Map<ServerDataCell,Integer> cellPlayerlessFrameMap = new HashMap<ServerDataCell,Integer>();

    /**
     * A map of ServerDataCell->GriddedDataCellTrackingData
     */
    Map<ServerDataCell,GriddedDataCellTrackingData> cellTrackingMap = new HashMap<ServerDataCell,GriddedDataCellTrackingData>();

    /**
     * Loaded cells
     */
    ReentrantLock loadedCellsLock = new ReentrantLock();

    /**
     * Parent realm
     */
    Realm parent;

    /**
     * The world data of the parent
     */
    ServerWorldData serverWorldData;

    /**
     * Manager for terrain for this particular cell manager
     */
    ServerTerrainManager serverTerrainManager;

    /**
     * Manager for fluids for this particular cell manager
     */
    ServerFluidManager serverFluidManager;

    /**
     * Lock for terrain editing
     */
    Semaphore terrainEditLock = new Semaphore(1);

    /**
     * Manager for getting entities to fill in a cell
     */
    ServerContentManager serverContentManager;

    /**
     * Used for cleaning server data cells no longer in use from the realm
     */
    Set<ServerDataCell> toCleanQueue = new HashSet<ServerDataCell>();

    /**
     * Map of world position key -> physics cell
     */
    Map<Long,PhysicsDataCell> posPhysicsMap = new HashMap<Long,PhysicsDataCell>();

    /**
     * Number of data cells cleaned up in the most recent frame
     */
    int numCleaned = 0;

    /**
     * Queue of cells that need to have their physics regenerated (ie on block edits)
     */
    Map<Long,Vector3i> physicsQueue = new HashMap<Long,Vector3i>();

    /**
     * The pathfinder for the manager
     */
    VoxelPathfinder pathfinder;

    /**
     * Caches lookups for nearby entities between simulate() calls
     */
    private Map<Long,List<Entity>> nearbyLookupCache = new HashMap<Long,List<Entity>>();
    
    /**
     * Constructor
     * @param parent The gridded data cell manager's parent realm
     */
    public GriddedDataCellManager(
        Realm parent
    ) {
        this.parent = parent;
        this.serverWorldData = this.parent.getServerWorldData();
        this.serverTerrainManager = serverWorldData.getServerTerrainManager();
        this.serverFluidManager = serverWorldData.getServerFluidManager();
        this.serverContentManager = this.parent.getServerContentManager();

        //Assert the gridded data cell manager was given good data
        if(
            this.parent == null ||
            this.serverWorldData == null ||
            this.serverTerrainManager == null ||
            this.serverFluidManager == null ||
            this.serverContentManager == null
        ){
            throw new Error("Tried to create a GriddedDataCellManager with invalid parameters " +
                this.parent + " " +
                this.serverWorldData + " " +
                this.serverTerrainManager + " " +
                this.serverFluidManager + " " +
                this.serverContentManager + " "
            );
        }
        this.pathfinder = new VoxelPathfinder();
        this.loaderService = new GriddedDataCellLoaderService();
        this.generationService = Globals.engineState.threadManager.requestFixedThreadPool(ThreadCounts.GRIDDED_DATACELL_PHYSICS_GEN_THREADS);
    }
    
    /**
     * Adds a player to the realm that this manager controls. Should do this intelligently based on the player's location
     * @param player The player
     */
    public void addPlayerToRealm(Player player){
        Globals.serverState.realmManager.setPlayerRealm(player, parent);
        int playerSimulationRadius = player.getSimulationRadius();
        Vector3i worldPos = player.getWorldPos();
        Vector3i tempVec = new Vector3i();
        for(int x = worldPos.x - playerSimulationRadius; x < worldPos.x + playerSimulationRadius + 1; x++){
            for(int y = worldPos.y - playerSimulationRadius; y < worldPos.y + playerSimulationRadius + 1; y++){
                for(int z = worldPos.z - playerSimulationRadius; z < worldPos.z + playerSimulationRadius + 1; z++){
                    tempVec.set(x,y,z);
                    double distance = this.calcDistance(tempVec, worldPos);
                    if(this.canCreateCell(x, y, z) && this.shouldContainPlayer(distance, playerSimulationRadius)){
                        LoggerInterface.loggerEngine.DEBUG("GriddedDataCellManager: Add player to " + x + " " + y + " " + z);
                        loadedCellsLock.lock();
                        if(groundDataCells.get(this.getServerDataCellKey(tempVec)) != null){
                            groundDataCells.get(this.getServerDataCellKey(tempVec)).addPlayer(player);
                        } else {
                            LoggerInterface.loggerEngine.DEBUG("Creating new cell @ " + x + " " + y + " " + z);
                            //create data cell
                            this.createServerDataCell(tempVec);
                            //add player
                            groundDataCells.get(this.getServerDataCellKey(tempVec)).addPlayer(player);
                        }
                        loadedCellsLock.unlock();
                    }
                }
            }
        }
    }
    
    /**
     * Moves a player to a new position
     * @param player The player
     * @param newPosition The new position
     */
    public void movePlayer(Player player, Vector3i newPosition){
        int playerSimulationRadius = player.getSimulationRadius();
        player.setWorldPos(newPosition);
        for(ServerDataCell cell : this.groundDataCells.values()){
            Vector3i worldPos = this.getCellWorldPosition(cell);
            if(cell.containsPlayer(player) && !this.shouldContainPlayer(this.calcDistance(worldPos, newPosition), playerSimulationRadius)){
                cell.removePlayer(player);
                this.broadcastDestructionToPlayer(player, cell);
                if(cell.getScene().containsEntity(player.getPlayerEntity())){
                    throw new Error(
                        "Unregistering player from cell that contains player's entity!\n " +
                        player + "\n " +
                        worldPos + "\n " +
                        player.getPlayerEntity() + "\n " +
                        EntityUtils.getPosition(player.getPlayerEntity())
                    );
                }
            }
        }
        for(int x = newPosition.x - playerSimulationRadius; x < newPosition.x + playerSimulationRadius + 1; x++){
            for(int y = newPosition.y - playerSimulationRadius; y < newPosition.y + playerSimulationRadius + 1; y++){
                for(int z = newPosition.x - playerSimulationRadius; z < newPosition.z + playerSimulationRadius + 1; z++){
                    if(this.canCreateCell(x, y, z) && this.shouldContainPlayer(this.calcDistance(new Vector3i(x, y, z), newPosition), playerSimulationRadius)){
                        Vector3i targetPos = new Vector3i(x,y,z);
                        if(groundDataCells.get(this.getServerDataCellKey(targetPos)) != null){
                            loadedCellsLock.lock();
                            groundDataCells.get(this.getServerDataCellKey(targetPos)).addPlayer(player);
                            loadedCellsLock.unlock();
                        } else {
                            loadedCellsLock.lock();
                            //create data cell
                            this.createServerDataCell(targetPos);
                            //add player
                            groundDataCells.get(this.getServerDataCellKey(targetPos)).addPlayer(player);
                            loadedCellsLock.unlock();
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if a player should be contained in a cell
     * @param cellWorldPos The world position of the cell
     * @param playerPos The world position of the player
     * @param simRadius The simulation radius of the player
     * @return true if the player should be contained in the cell, false otherwise
     */
    private boolean shouldContainPlayer(double distance, int simRadius){
        return distance < simRadius;
    }

    /**
     * Calculates the distance from the player to the cell
     * @param playerPos The player
     * @param cellWorldPos The cell
     * @return The distance
     */
    public double calcDistance(Vector3i playerPos, Vector3i cellWorldPos){
        return cellWorldPos.distance(playerPos);
    }

    /**
     * Checks if a cell can be created at the position
     * @param cellPos The position to check
     * @return true if a cell can be created at that position, false otherwise
     */
    private boolean canCreateCell(Vector3i cellPos){
        return this.canCreateCell(cellPos.x, cellPos.y, cellPos.z);
    }

    /**
     * Checks if a cell can be created at the position
     * @return true if a cell can be created at that position, false otherwise
     */
    private boolean canCreateCell(int x, int y, int z){
        return
            x >= 0 && x < this.serverWorldData.getWorldSizeDiscrete() &&
            y >= 0 && y < this.serverWorldData.getWorldSizeDiscrete() &&
            z >= 0 && z < this.serverWorldData.getWorldSizeDiscrete()
        ;
    }

    /**
     * Broadcasts messages to player to destroy all entities in a given cell
     * @param player The player
     * @param cell The cell
     */
    private void broadcastDestructionToPlayer(Player player, ServerDataCell cell){
        for(Entity entity : cell.getScene().getEntityList()){
            player.addMessage(EntityMessage.constructDestroyMessage(entity.getId()));
        }
    }

    /**
     * Creates physics entities when new data cell being created
     */
    private void createTerrainPhysicsEntities(Vector3i worldPos){
        Long key = this.getServerDataCellKey(worldPos);
        loadedCellsLock.lock();
        if(posPhysicsMap.containsKey(key)){
            PhysicsDataCell cell = posPhysicsMap.get(key);
            cell.retireCell();
        }
        loadedCellsLock.unlock();
        //get data to generate with
        Vector3d realPos = new Vector3d(
            worldPos.x * ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET,
            worldPos.y * ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET,
            worldPos.z * ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET
        );

        MacroDataUpdater.update(parent, parent.getMacroData(), realPos);
        BlockChunkData blockChunkData = parent.getServerWorldData().getServerBlockManager().getChunk(worldPos.x, worldPos.y, worldPos.z);
        ServerTerrainChunk terrainChunk = parent.getServerWorldData().getServerTerrainManager().getChunk(worldPos.x, worldPos.y, worldPos.z, ServerChunkCache.STRIDE_FULL_RES);

        //create entities
        Entity blockEntity = EntityCreationUtils.createServerEntity(parent, realPos);
        Entity terrainEntity = EntityCreationUtils.createServerEntity(parent, realPos);
        
        //position entity
        //this needs to be called at the end of this function.
        //Burried underneath this is function call to initialize a server side entity.
        //The server initialization logic checks what type of entity this is, if this function is called prior to its type being stored
        //the server will not be able to synchronize it properly.
        ServerEntityUtils.initiallyPositionEntity(parent,blockEntity,realPos);
        ServerEntityUtils.initiallyPositionEntity(parent,terrainEntity,realPos);
        PhysicsDataCell cell = PhysicsDataCell.createPhysicsCell(terrainEntity, blockEntity);
        cell.setTerrainChunk(terrainChunk);
        cell.setBlockChunk(blockChunkData);
        cell.generatePhysics();

        loadedCellsLock.lock();
        posPhysicsMap.put(key, cell);
        loadedCellsLock.unlock();
    }
    
    /**
     * For every player, looks at their entity and determines what data cell they should be considered inside of
     * @return True if the player changed cell, false otherwise
     */
    public boolean updatePlayerPositions(){
        Globals.profiler.beginCpuSample("GriddedDataCellManager.updatePlayerPositions - Reset chunk distances");
        loadedCellsLock.lock();
        for(ServerDataCell cell : this.groundDataCells.values()){
            GriddedDataCellTrackingData trackingData = this.cellTrackingMap.get(cell);
            trackingData.setClosestPlayer(GriddedDataCellTrackingData.REALLY_LARGE_DISTANCE);
        }
        loadedCellsLock.unlock();
        Globals.profiler.endCpuSample();

        Globals.profiler.beginCpuSample("GriddedDataCellManager.updatePlayerPositions - Actually update player positions");
        boolean playerChangedChunk = false;
        for(Player player : Globals.serverState.playerManager.getPlayers()){
            Entity playerEntity = player.getPlayerEntity();
            if(playerEntity != null && !parent.getLoadingDataCell().containsPlayer(player)){
                Vector3d position = EntityUtils.getPosition(playerEntity);
                int currentWorldX = ServerWorldData.convertRealToChunkSpace(position.x);
                int currentWorldY = ServerWorldData.convertRealToChunkSpace(position.y);
                int currentWorldZ = ServerWorldData.convertRealToChunkSpace(position.z);
                Vector3i newPosition = new Vector3i(currentWorldX,currentWorldY,currentWorldZ);
                player.setWorldPos(newPosition);

                int playerSimulationRadius = player.getSimulationRadius();

                //remove from cells that are out of range
                loadedCellsLock.lock();
                Globals.profiler.beginCpuSample("GriddedDataCellManager.updatePlayerPositions - Remove from old cells");
                for(ServerDataCell cell : this.groundDataCells.values()){
                    GriddedDataCellTrackingData trackingData = this.cellTrackingMap.get(cell);
                    Vector3i cellWorldPos = this.getCellWorldPosition(cell);
                    double distance = this.calcDistance(cellWorldPos, newPosition);
                    if(distance < trackingData.getClosestPlayer()){
                        trackingData.setClosestPlayer(distance);
                    }
                    if(cell.containsPlayer(player) && !this.shouldContainPlayer(distance, playerSimulationRadius)){
                        if(cell.getScene().containsEntity(player.getPlayerEntity())){
                            // throw new Error("Trying to remove player from a cell that contains its entity!");
                            continue;
                        }
                        cell.removePlayer(player);
                        this.broadcastDestructionToPlayer(player, cell);
                    }
                }
                Globals.profiler.endCpuSample();
                loadedCellsLock.unlock();

                //Add to cells that are in range
                Globals.profiler.beginCpuSample("GriddedDataCellManager.updatePlayerPositions - Create new cells");
                Vector3i tempVec = new Vector3i();
                for(int x = newPosition.x - playerSimulationRadius + 1; x < newPosition.x + playerSimulationRadius; x++){
                    for(int y = newPosition.y - playerSimulationRadius + 1; y < newPosition.y + playerSimulationRadius; y++){
                        for(int z = newPosition.z - playerSimulationRadius + 1; z < newPosition.z + playerSimulationRadius; z++){
                            tempVec.set(x,y,z);
                            double distance = this.calcDistance(tempVec, newPosition);
                            if(this.canCreateCell(x,y,z) && this.shouldContainPlayer(distance, playerSimulationRadius)){
                                if(groundDataCells.get(this.getServerDataCellKey(tempVec)) != null){
                                    loadedCellsLock.lock();
                                    ServerDataCell cell = groundDataCells.get(this.getServerDataCellKey(tempVec));
                                    if(!cell.containsPlayer(player)){
                                        cell.addPlayer(player);
                                    }
                                    loadedCellsLock.unlock();
                                } else {
                                    loadedCellsLock.lock();
                                    //create data cell
                                    this.createServerDataCell(tempVec);
                                    //add player
                                    groundDataCells.get(this.getServerDataCellKey(tempVec)).addPlayer(player);
                                    loadedCellsLock.unlock();
                                }
                            }
                        }
                    }
                }
                Globals.profiler.endCpuSample();
            }
        }
        Globals.profiler.endCpuSample();
        return playerChangedChunk;
    }

    /**
     * Updates the tracking data for each cell
     */
    private void updateTrackingData(){
        Globals.profiler.beginCpuSample("GriddedDataCellManager.updateTrackingData");
        loadedCellsLock.lock();
        for(ServerDataCell cell : this.groundDataCells.values()){
            GriddedDataCellTrackingData trackingData = this.cellTrackingMap.get(cell);
            trackingData.setCreatureCount(cell.getScene().getEntitiesWithTag(EntityTags.CREATURE).size());
        }
        loadedCellsLock.unlock();
        Globals.profiler.endCpuSample();
    }


    /**
     * Unloads all chunks that haven't had players in them for a set amount of time
     */
    public void unloadPlayerlessChunks(){
        Globals.profiler.beginCpuSample("GriddedDataCellManager.unloadPlayerlessChunks");
        if(this.unloadCells){
            //TODO: improve to make have less performance impact
            loadedCellsLock.lock();
            Globals.profiler.beginCpuSample("GriddedDataCellManager.unloadPlayerlessChunks - Increment cleaning time");
            for(ServerDataCell cell : this.groundDataCells.values()){
                if(cellPlayerlessFrameMap.containsKey(cell)){
                    if(cell.isReady() && cell.getPlayers().size() < 1){
                        int frameCount = cellPlayerlessFrameMap.get(cell) + 1;
                        cellPlayerlessFrameMap.put(cell,frameCount);
                        if(frameCount > UNLOAD_FRAME_THRESHOLD){
                            toCleanQueue.add(cell);
                        }
                    } else {
                        if(cellPlayerlessFrameMap.get(cell) > 0){
                            cellPlayerlessFrameMap.put(cell, 0);
                        }
                    }
                }
            }
            Globals.profiler.endCpuSample();
            Globals.profiler.beginCpuSample("GriddedDataCellManager.unloadPlayerlessChunks - Deconstruct timed out cells");
            this.numCleaned = toCleanQueue.size();
            for(ServerDataCell cell : toCleanQueue){
                //error check before actually queueing for deletion
                boolean containsPlayerEntity = false;
                for(Entity entity : cell.getScene().getEntityList()){
                    if(ServerPlayerViewDirTree.hasTree(entity)){
                        containsPlayerEntity = true;
                        break;
                        // int playerId = CreatureUtils.getControllerPlayerId(entity);
                        // Player player = Globals.serverState.playerManager.getPlayerFromId(playerId);
                        // throw new Error(
                        //     "Trying to unload a player's entity! " +
                        //     "entity: " + entity + "\n" +
                        //     "entity pos  (real): " + EntityUtils.getPosition(entity) + "\n" +
                        //     "entity pos (world): " + serverWorldData.convertRealToWorldSpace(EntityUtils.getPosition(entity)) + "\n" +
                        //     "chunk  pos (world): " + worldPos + "\n" +
                        //     "player pos (world): " + player.getWorldPos() + "\n" +
                        //     "Number of players in cell: " + cell.getPlayers().size()
                        // );
                    }
                    // ServerEntityUtils.destroyEntity(entity);
                }
                if(containsPlayerEntity){
                    continue;
                }


                Vector3i worldPos = this.getCellWorldPosition(cell);
                Long key = this.getServerDataCellKey(worldPos);
                //entities are serialized before tracking is removed. This makes sure that any side effects from calling destroyEntity (ie if it looks up the chunk that we're deleting)
                //don't trigger the chunk to be re-created
                Globals.profiler.beginCpuSample("GriddedDataCellManager.unloadPlayerlessChunks - Serialize entities");
                ContentSerialization serializedEntities = ContentSerialization.constructContentSerialization(cell.getScene().getEntityList());
                Globals.profiler.endCpuSample();


                Globals.profiler.beginCpuSample("GriddedDataCellManager.unloadPlayerlessChunks - Destroy entities");
                for(Entity entity : cell.getScene().getEntityList()){
                    ServerEntityUtils.destroyEntity(entity);
                }
                Globals.profiler.endCpuSample();

                //save terrain to disk
                //terrain is saved before tracking is removed. This makes sure that any side effects from calling savePositionToDisk (ie if it looks up the chunk that we're deleting)
                //don't trigger the chunk to be re-created
                Globals.profiler.beginCpuSample("GriddedDataCellManager.unloadPlayerlessChunks - Store data");
                this.loaderService.queueLocationBasedOperation(key, () -> {
                    try {
                        serverContentManager.saveSerializationToDisk(key, serializedEntities);
                        serverTerrainManager.savePositionToDisk(worldPos);
                    } catch(Throwable e){
                        e.printStackTrace();
                    }
                });
                Globals.profiler.endCpuSample();

                //destroy physics
                PhysicsDataCell physicsCell = this.posPhysicsMap.get(key);
                if(physicsCell != null){
                    physicsCell.destroyPhysics();
                }

                //deregister from all tracking structures
                parent.deregisterCell(cell);
                groundDataCells.remove(key);
                this.posPhysicsMap.remove(key);
                this.cellPositionMap.remove(cell);
                this.cellTrackingMap.remove(cell);
                this.cellPlayerlessFrameMap.remove(cell);
            }
            Globals.profiler.endCpuSample();
            loadedCellsLock.unlock();
            toCleanQueue.clear();
        }
        Globals.profiler.endCpuSample();
    }

    /**
     * Evicts all loaded chunks.
     */
    public void evictAll(){
        //TODO: improve to make have less performance impact
        loadedCellsLock.lock();
        for(ServerDataCell cell : this.groundDataCells.values()){
            int frameCount = cellPlayerlessFrameMap.get(cell) + 1;
            cellPlayerlessFrameMap.put(cell,frameCount);
            toCleanQueue.add(cell);
        }
        for(ServerDataCell cell : toCleanQueue){
            parent.deregisterCell(cell);
            Vector3i worldPos = this.getCellWorldPosition(cell);
            Long key = getServerDataCellKey(worldPos);
            groundDataCells.remove(key);
            this.posPhysicsMap.remove(key);
            this.cellPositionMap.remove(cell);
            this.cellPlayerlessFrameMap.remove(cell);
            this.cellTrackingMap.remove(cell);
            //offload all entities in cell to chunk file
            serverContentManager.saveContentToDisk(key, cell.getScene().getEntityList());
            //clear all entities in cell
            for(Entity entity : cell.getScene().getEntityList()){
                ServerEntityUtils.destroyEntity(entity);
            }
        }
        loadedCellsLock.unlock();
        this.serverTerrainManager.evictAll();
        toCleanQueue.clear();
    }

    /**
     * Get data cell at a given real point in this realm
     * @param point The real point
     * @return Either the data cell if found, or null if not found
     */
    public ServerDataCell getDataCellAtPoint(Vector3d point){
        ServerDataCell rVal = null;
        int worldX = ServerWorldData.convertRealToChunkSpace(point.x);
        int worldY = ServerWorldData.convertRealToChunkSpace(point.y);
        int worldZ = ServerWorldData.convertRealToChunkSpace(point.z);
        Vector3i worldPos = new Vector3i(worldX,worldY,worldZ);
        if(
            //in bounds of array
            worldX >= 0 && worldX < this.serverWorldData.getWorldSizeDiscrete() &&
            worldY >= 0 && worldY < this.serverWorldData.getWorldSizeDiscrete() &&
            worldZ >= 0 && worldZ < this.serverWorldData.getWorldSizeDiscrete() &&
            //isn't null
            groundDataCells.get(getServerDataCellKey(worldPos)) != null
        ){
            LoggerInterface.loggerEngine.DEBUG("Get server data cell key: " + this.getServerDataCellKey(worldPos));
            rVal = groundDataCells.get(getServerDataCellKey(worldPos));
        } else {
            LoggerInterface.loggerEngine.DEBUG("Failed to get server data cell at: " + worldPos);
        }
        return rVal;
    }

    /**
     * Tries to create a data cell at a given real point
     * @param point The real point
     * @return The data cell if created, null otherwise
     */
    public ServerDataCell tryCreateCellAtPoint(Vector3d point){
        int worldX = ServerWorldData.convertRealToChunkSpace(point.x);
        int worldY = ServerWorldData.convertRealToChunkSpace(point.y);
        int worldZ = ServerWorldData.convertRealToChunkSpace(point.z);
        Vector3i worldPos = new Vector3i(worldX,worldY,worldZ);
        return this.tryCreateCellAtPoint(worldPos);
    }

    /**
     * Tries to create a data cell at a given discrete point
     * @param point The discrete point
     * @return The data cell if created, null otherwise
     */
    public ServerDataCell tryCreateCellAtPoint(Vector3i worldPos){
        if(this.canCreateCell(worldPos) && groundDataCells.get(this.getServerDataCellKey(worldPos)) == null){
            loadedCellsLock.lock();
            //create data cell
            this.createServerDataCell(worldPos);
            loadedCellsLock.unlock();
        } else if(groundDataCells.get(this.getServerDataCellKey(worldPos)) == null) {
            LoggerInterface.loggerEngine.ERROR(
                new Error(
                    "Trying to create data cell outside world bounds!\n" +
                    worldPos + "\n" +
                    this.serverWorldData.getWorldSizeDiscrete()
                )
            );
        }
        return groundDataCells.get(this.getServerDataCellKey(worldPos));
    }

    /**
     * Gets a data cell at a given world position
     * @param position The world position
     * @return The data cell if found, null otherwise
     */
    public ServerDataCell getCellAtWorldPosition(Vector3i position){
        if(this.canCreateCell(position) && groundDataCells.get(this.getServerDataCellKey(position)) != null){
            return groundDataCells.get(this.getServerDataCellKey(position));
        }
        return null;
    }


    /**
     * Calls the simulate function on all loaded cells
     */
    public void simulate(){
        Globals.profiler.beginCpuSample("GriddedDataCellManager.simulate");
        loadedCellsLock.lock();


        //
        //clear nearby entity lookup cache
        this.nearbyLookupCache.clear();


        //regenerate physics where relevant
        terrainEditLock.acquireUninterruptibly();
        if(physicsQueue.size() > 0){
            for(Vector3i pos : physicsQueue.values()){
                this.createTerrainPhysicsEntities(pos);
            }
            physicsQueue.clear();
        }
        terrainEditLock.release();


        //micro simulation
        boolean runMicroSim = Globals.serverState.microSimulation != null && Globals.serverState.microSimulation.isReady();
        if(runMicroSim){
            List<ServerDataCell> simulationTargets = this.groundDataCells.values().stream().filter((ServerDataCell cell) -> this.shouldSimulate(cell)).collect(Collectors.toList());
            for(ServerDataCell cell : simulationTargets){
                Globals.serverState.microSimulation.simulate(cell);

                //queue fluid simulation
                if(EngineState.EngineFlags.RUN_FLUIDS){
                    Vector3i cellPos = this.getCellWorldPosition(cell);
                    if(cellPos != null){
                        this.serverFluidManager.queue(cellPos.x, cellPos.y, cellPos.z);
                    }
                }
            }
        }

        //simulate fluids
        if(EngineState.EngineFlags.RUN_FLUIDS){
            this.serverFluidManager.simulate((ServerFluidChunk fluidChunk) -> {
                ServerDataCell cell = getCellAtWorldPosition(fluidChunk.getWorldPosition());
                ServerFluidChunk chunk = getFluidChunkAtPosition(fluidChunk.getWorldPosition());
                cell.broadcastNetworkMessage(
                    TerrainMessage.constructupdateFluidDataMessage(fluidChunk.getWorldX(), fluidChunk.getWorldY(), fluidChunk.getWorldZ(), TerrainProtocol.constructFluidByteBuffer(chunk).array())
                );
            });
        }

        loadedCellsLock.unlock();
        this.unloadPlayerlessChunks();
        this.updatePlayerPositions();
        this.updateTrackingData();
        Globals.profiler.endCpuSample();
    }

    /**
     * Checks if a server data cell should be simulated or not
     * @param cell The cell
     * @return true if it should be simulated, false otherwise
     */
    private boolean shouldSimulate(ServerDataCell cell){
        GriddedDataCellTrackingData trackingData = this.cellTrackingMap.get(cell);
        return 
        //has creature
        (trackingData.getCreatureCount() > 0) &&
        //has player
        (cell.getPlayers().size() > 0 && trackingData.getClosestPlayer() < SIMULATION_DISTANCE_CUTOFF)
        ;
    }


    /**
     * Gets the server terrain manager for this realm if it exists
     * @return The server terrain manager if it exists, null otherwise
     */
    public ServerTerrainManager getServerTerrainManager(){
        return serverTerrainManager;
    }

    /**
     * Gets the server fluid manager for this realm if it exists
     * @return The server fluid manager if it exists, null otherwise
     */
    public ServerFluidManager getServerFluidManager(){
        return serverFluidManager;
    }

    /**
     * Runs code to generate physics entities and register cell in a dedicated thread.
     * Because cell hasn't been registered yet, no simulation is performed until the physics is created.
     * @param worldPos
     */
    private void runPhysicsGenerationThread(
        Vector3i worldPos,
        Long key,
        PhysicsDataCell cell,
        Map<Long, PhysicsDataCell> posPhysicsMap,
        Map<Long, ServerDataCell> groundDataCells,
        Map<ServerDataCell,GriddedDataCellTrackingData> cellTrackingMap,
        Realm realm
    ){
        //get data to generate with
        Vector3d realPos = new Vector3d(
            worldPos.x * ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET,
            worldPos.y * ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET,
            worldPos.z * ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET
        );
        ServerDataCell dataCell = groundDataCells.get(key);

        //create entities
        Entity blockEntity = EntityCreationUtils.createServerEntity(realm, realPos);
        Entity terrainEntity = EntityCreationUtils.createServerEntity(realm, realPos);
        
        //position entity
        //this needs to be called at the end of this function.
        //Burried underneath this is function call to initialize a server side entity.
        //The server initialization logic checks what type of entity this is, if this function is called prior to its type being stored
        //the server will not be able to synchronize it properly.
        ServerEntityUtils.initiallyPositionEntity(realm,blockEntity,realPos);
        ServerEntityUtils.initiallyPositionEntity(realm,terrainEntity,realPos);

        PhysicsDataCell targetCell = PhysicsDataCell.createPhysicsCell(terrainEntity, blockEntity);
        if(cell == null){
            posPhysicsMap.put(key, targetCell);
        } else {
            ServerEntityUtils.destroyEntity(terrainEntity);
            ServerEntityUtils.destroyEntity(blockEntity);
        }

        if(parent.getMacroData() != null){
            MacroDataUpdater.update(parent, parent.getMacroData(), realPos);
        }
        this.generationService.submit(() -> {
            try {
                BlockChunkData blockChunkData = realm.getServerWorldData().getServerBlockManager().getChunk(worldPos.x, worldPos.y, worldPos.z);
                ServerTerrainChunk terrainChunk = realm.getServerWorldData().getServerTerrainManager().getChunk(worldPos.x, worldPos.y, worldPos.z, ServerChunkCache.STRIDE_FULL_RES);
                while(terrainChunk == null){
                    TimeUnit.MILLISECONDS.sleep(1);
                    terrainChunk = realm.getServerWorldData().getServerTerrainManager().getChunk(worldPos.x, worldPos.y, worldPos.z, ServerChunkCache.STRIDE_FULL_RES);
                }
                while(blockChunkData == null){
                    TimeUnit.MILLISECONDS.sleep(1);
                    blockChunkData = realm.getServerWorldData().getServerBlockManager().getChunk(worldPos.x, worldPos.y, worldPos.z);
                }
                targetCell.setTerrainChunk(terrainChunk);
                targetCell.setBlockChunk(blockChunkData);

                //create physics entities
                if(cell != null){
                    cell.retireCell();
                    cell.generatePhysics();
                } else {
                    targetCell.generatePhysics();
                }

                //set ready
                dataCell.setReady(true);
            } catch(Throwable ex){
                LoggerInterface.loggerEngine.ERROR(ex);
            }
        });
    }

    /**
     * Gets the key in the groundDataCells map for the data cell at the provided world pos
     * @param worldPos The position in world coordinates of the server data cell
     * @return The server data cell if it exists, otherwise null
     */
    private Long getServerDataCellKey(Vector3i worldPos){
        return (long)HashUtils.hashIVec(worldPos.x, worldPos.y, worldPos.z);
    }

    /**
     * Registers a server data cell with the internal datastructure for tracking them
     * @param key The key to register the cell at
     * @param cell The cell itself
     */
    private ServerDataCell createServerDataCell(Vector3i worldPos){
        Globals.profiler.beginCpuSample("GriddedDataCellManager.createServerDataCell");
        ServerDataCell rVal = parent.createNewCell();
        Vector3i localWorldPos = new Vector3i(worldPos);
        Long cellKey = this.getServerDataCellKey(localWorldPos);

        loadedCellsLock.lock();
        groundDataCells.put(cellKey,rVal);
        cellPlayerlessFrameMap.put(rVal,0);
        LoggerInterface.loggerEngine.DEBUG("Create server data cell with key " + cellKey);
        cellPositionMap.put(rVal,localWorldPos);
        GriddedDataCellTrackingData trackingData = new GriddedDataCellTrackingData();
        this.cellTrackingMap.put(rVal,trackingData);
        loadedCellsLock.unlock();

        Long key = this.getServerDataCellKey(localWorldPos);
        //generate content
        this.loaderService.queueLocationBasedOperation(key, () -> {
            try {
                serverContentManager.generateContentForDataCell(parent, localWorldPos, rVal, cellKey);
            } catch(Throwable e){
                e.printStackTrace();
            }
        });
        //generates physics for the cell in a dedicated thread then finally registers
        loadedCellsLock.lock();
        PhysicsDataCell cell = posPhysicsMap.get(key);
        this.runPhysicsGenerationThread(localWorldPos,key,cell,this.posPhysicsMap,this.groundDataCells,this.cellTrackingMap,this.parent);
        loadedCellsLock.unlock();


        Globals.profiler.endCpuSample();
        return rVal;
    }

    @Override
    /**
     * Gets the weight of a single voxel at a position
     * @param worldPosition The position in world coordinates of the chunk to grab data from
     * @param voxelPosition The position in voxel coordinates (local/relative to the chunk) to get voxel values from
     * @return The weight of the described voxel
     */
    public float getVoxelWeightAtLocalPosition(Vector3i worldPosition, Vector3i voxelPosition) {
        return serverTerrainManager.getChunk(worldPosition.x, worldPosition.y, worldPosition.z, ServerChunkCache.STRIDE_FULL_RES).getWeights()[voxelPosition.x][voxelPosition.y][voxelPosition.z];
    }

    @Override
    /**
     * Gets the type of a single voxel at a position
     * @param worldPosition The position in world coordinates of the chunk to grab data from
     * @param voxelPosition The position in voxel coordinates (local/relative to the chunk) to get voxel values from
     * @return The type of the described voxel
     */
    public int getVoxelTypeAtLocalPosition(Vector3i worldPosition, Vector3i voxelPosition) {
        return serverTerrainManager.getChunk(worldPosition.x, worldPosition.y, worldPosition.z, ServerChunkCache.STRIDE_FULL_RES).getValues()[voxelPosition.x][voxelPosition.y][voxelPosition.z];
    }

    @Override
    /**
     * Gets the chunk data at a given world position
     * @param worldPosition The position in world coordinates
     * @return The ServerTerrainChunk of data at that position, or null if it is out of bounds or otherwise doesn't exist
     */
    public ServerTerrainChunk getChunkAtPosition(Vector3i worldPosition) {
        return this.getChunkAtPosition(worldPosition.x,worldPosition.y,worldPosition.z);
    }

    @Override
    /**
     * Edits a single voxel
     * @param worldPosition The world position of the chunk to edit
     * @param voxelPosition The voxel position of the voxel to edit
     * @param weight The weight to set the voxel to
     * @param type The type to set the voxel to
     */
    public void editChunk(Vector3i worldPosition, Vector3i voxelPosition, float weight, int type) {
        terrainEditLock.acquireUninterruptibly();
        List<Vector3i> worldPositionsToUpdate = new LinkedList<Vector3i>();
        worldPositionsToUpdate.add(worldPosition);
        if(voxelPosition.x < 1){
            worldPositionsToUpdate.add(new Vector3i(worldPosition).sub(1,0,0));
            if(voxelPosition.y < 1){
                worldPositionsToUpdate.add(new Vector3i(worldPosition).sub(1,1,0));
                if(voxelPosition.z < 1){
                    worldPositionsToUpdate.add(new Vector3i(worldPosition).sub(1,1,1));
                }
            }
            if(voxelPosition.z < 1){
                worldPositionsToUpdate.add(new Vector3i(worldPosition).sub(1,0,1));
            }
        }
        if(voxelPosition.y < 1){
            worldPositionsToUpdate.add(new Vector3i(worldPosition).sub(0,1,0));
            if(voxelPosition.z < 1){
                worldPositionsToUpdate.add(new Vector3i(worldPosition).sub(0,1,1));
            }
        }
        if(voxelPosition.z < 1){
            worldPositionsToUpdate.add(new Vector3i(worldPosition).sub(0,0,1));
        }
        //update all loaded cells
        for(Vector3i toUpdate : worldPositionsToUpdate){
            if(
                toUpdate.x >= 0 && toUpdate.x < this.serverWorldData.getWorldSizeDiscrete() &&
                toUpdate.y >= 0 && toUpdate.y < this.serverWorldData.getWorldSizeDiscrete() &&
                toUpdate.z >= 0 && toUpdate.z < this.serverWorldData.getWorldSizeDiscrete()
            ){
                //update terrain
                int localVoxelX = voxelPosition.x + (ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE - 1) * (worldPosition.x - toUpdate.x);
                int localVoxelY = voxelPosition.y + (ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE - 1) * (worldPosition.y - toUpdate.y);
                int localVoxelZ = voxelPosition.z + (ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE - 1) * (worldPosition.z - toUpdate.z);
                serverTerrainManager.deformTerrainAtLocationToValue(toUpdate, new Vector3i(localVoxelX, localVoxelY, localVoxelZ), weight, type);

                //update anything loaded
                this.loadedCellsLock.lock();
                ServerDataCell cell = groundDataCells.get(this.getServerDataCellKey(toUpdate));
                if(cell != null){
                    //update physics
                    this.createTerrainPhysicsEntities(toUpdate);

                    //broadcast update
                    cell.broadcastNetworkMessage(TerrainMessage.constructUpdateVoxelMessage(
                        toUpdate.x, toUpdate.y, toUpdate.z, 
                        localVoxelX, localVoxelY, localVoxelZ, 
                        weight, type));
                }
                this.loadedCellsLock.unlock();
            }
        }
        terrainEditLock.release();
    }


    /**
     * Gets the world position of a given data cell
     * @param cell The data cell
     * @return The world position
     */
    public Vector3i getCellWorldPosition(ServerDataCell cell){
        return cellPositionMap.get(cell);
    }

    @Override
    /**
     * Gets the fluid chunk at a given position
     */
    public ServerFluidChunk getFluidChunkAtPosition(Vector3i worldPosition) {
        return serverFluidManager.getChunk(worldPosition.x, worldPosition.y, worldPosition.z);
    }

    /**
     * Loads all cells
     */
    public void loadAllCells(){
        this.unloadCells = false;
        for(int x = 0; x < this.serverWorldData.getWorldSizeDiscrete(); x++){
            for(int y = 0; y < this.serverWorldData.getWorldSizeDiscrete(); y++){
                for(int z = 0; z < this.serverWorldData.getWorldSizeDiscrete(); z++){
                    this.tryCreateCellAtPoint(new Vector3i(x,y,z));
                }
            }
        }
    }

    @Override
    public void save(String saveName) {
        for(ServerDataCell cell : this.groundDataCells.values()){
            Long key = this.getServerDataCellKey(this.getCellWorldPosition(cell));
            //offload all entities in cell to chunk file
            serverContentManager.saveContentToDisk(key, cell.getScene().getEntityList());
        }
    }

    @Override
    public Vector3d guaranteePositionIsInBounds(Vector3d positionToTest) {
        Vector3d returnPos = new Vector3d(positionToTest);
        if(positionToTest.x < 0){
            returnPos.x = 0;
        }
        if(positionToTest.x >= ServerWorldData.convertChunkToRealSpace(parent.getServerWorldData().getWorldSizeDiscrete())){
            returnPos.x = ServerWorldData.convertChunkToRealSpace(parent.getServerWorldData().getWorldSizeDiscrete()) - 1;
        }
        if(positionToTest.y < 0){
            returnPos.y = 0;
        }
        if(positionToTest.y >= ServerWorldData.convertChunkToRealSpace(parent.getServerWorldData().getWorldSizeDiscrete())){
            returnPos.y = ServerWorldData.convertChunkToRealSpace(parent.getServerWorldData().getWorldSizeDiscrete()) - 1;
        }
        if(positionToTest.z < 0){
            returnPos.z = 0;
        }
        if(positionToTest.z >= ServerWorldData.convertChunkToRealSpace(parent.getServerWorldData().getWorldSizeDiscrete())){
            returnPos.z = ServerWorldData.convertChunkToRealSpace(parent.getServerWorldData().getWorldSizeDiscrete()) - 1;
        }
        return returnPos;
    }

    /**
     * Stops the executor service
     */
    public void halt(){
        this.generationService.shutdownNow();
        this.loaderService.haltThreads();
    }

    @Override
    /**
     * Gets the block chunk data at a given world position
     */
    public BlockChunkData getBlocksAtPosition(Vector3i worldPosition) {
        return this.serverWorldData.getServerBlockManager().getChunk(worldPosition.x, worldPosition.y, worldPosition.z);
    }

    @Override
    /**
     * Edits a block chunk in the world
     */
    public void editBlock(Vector3i worldPosition, Vector3i voxelPosition, short type, short metadata) {
        terrainEditLock.acquireUninterruptibly();
        if(
            worldPosition.x >= 0 && worldPosition.x < this.serverWorldData.getWorldSizeDiscrete() &&
            worldPosition.y >= 0 && worldPosition.y < this.serverWorldData.getWorldSizeDiscrete() &&
            worldPosition.z >= 0 && worldPosition.z < this.serverWorldData.getWorldSizeDiscrete()
        ){
            ServerBlockManager serverBlockManager = this.serverWorldData.getServerBlockManager();
            //update terrain
            int localVoxelX = voxelPosition.x;
            int localVoxelY = voxelPosition.y;
            int localVoxelZ = voxelPosition.z;
            serverBlockManager.editBlockAtLocationToValue(worldPosition, voxelPosition, type, metadata);

            //update anything loaded
            this.loadedCellsLock.lock();
            ServerDataCell cell = groundDataCells.get(this.getServerDataCellKey(worldPosition));
            if(cell != null){
                //update physics
                long key = this.getServerDataCellKey(worldPosition);
                if(!this.physicsQueue.containsKey(key)){
                    this.physicsQueue.put(key,worldPosition);
                }

                //broadcast update
                cell.broadcastNetworkMessage(TerrainMessage.constructUpdateBlockMessage(
                    worldPosition.x, worldPosition.y, worldPosition.z, 
                    localVoxelX, localVoxelY, localVoxelZ, 
                    type, metadata
                ));
            }
            this.loadedCellsLock.unlock();
        }
        terrainEditLock.release();
    }

    /**
     * Gets the set of loaded cells
     * @return The set of loaded cells
     */
    public Collection<ServerDataCell> getLoadedCells(){
        return Collections.unmodifiableCollection(this.groundDataCells.values());
    }

    /**
     * Gets the number of cells cleaned in the most recent frame
     * @return The number of cells cleaned
     */
    public int getNumCleaned(){
        return this.numCleaned;
    }

    /**
     * Gets the playerless cell->frame count map
     * @return The playerless cell->frame count map
     */
    public Map<ServerDataCell,Integer> getCellPlayerlessFrameMap(){
        return cellPlayerlessFrameMap;
    }

    @Override
    public List<Vector3d> findPath(Vector3d start, Vector3d end){
        Vector3i startChunkPos = ServerWorldData.convertRealToChunkSpace(start);
        ServerDataCell cell = this.getCellAtWorldPosition(startChunkPos);
        GriddedDataCellTrackingData trackingData = this.cellTrackingMap.get(cell);
        if(trackingData == null){
            throw new Error("Failed to find tracking data for " + start);
        }
        Vector3d nearestValidGoal = this.pathfinder.scanNearestWalkable(this, end, VoxelPathfinder.DEFAULT_MAX_TARGET_SCAN_DIST);
        if(nearestValidGoal == null){
            nearestValidGoal = this.pathfinder.scanNearestWalkable(this, end, VoxelPathfinder.DEFAULT_MAX_TARGET_SCAN_DIST);
            throw new Error("Failed to resolve valid point near " + end.x + "," + end.y + "," + end.z);
        }
        List<Vector3d> points = this.pathfinder.findPath(this, start, nearestValidGoal, VoxelPathfinder.DEFAULT_MAX_COST);
        return points;
    }

    /**
     * Prints information about the provided server data cell
     * @param serverDataCell The cell
     */
    public void printCellInfo(ServerDataCell serverDataCell){
        LoggerInterface.loggerEngine.WARNING("Number of players: " + serverDataCell.getPlayers().size());
        GriddedDataCellTrackingData trackingData = this.cellTrackingMap.get(serverDataCell);
        if(trackingData == null){
            LoggerInterface.loggerEngine.WARNING("No tracking data for this cell in the gridded data cell manager");
        } else {
            LoggerInterface.loggerEngine.WARNING("Closest player distance: " + trackingData.getClosestPlayer());
        }
        LoggerInterface.loggerEngine.WARNING("Should simulate: " + this.shouldSimulate(serverDataCell));
        LoggerInterface.loggerEngine.WARNING("Cell is ready: " + serverDataCell.isReady());
    }

    @Override
    public Collection<Entity> entityLookup(Vector3d pos, double radius) {
        Vector3i chunkPos = ServerWorldData.convertRealToChunkSpace(pos);
        long key = this.getServerDataCellKey(chunkPos);
        if(this.nearbyLookupCache.containsKey(key)){
            return Collections.unmodifiableCollection(this.nearbyLookupCache.get(key));
        }
        List<Entity> rVal = new LinkedList<Entity>();
        this.loadedCellsLock.lock();
        for(ServerDataCell cell : this.groundDataCells.values()){
            if(ServerWorldData.convertChunkToRealSpace(this.getCellWorldPosition(cell)).distance(pos) > radius){
                continue;
            }
            rVal.addAll(cell.getScene().getEntityList());
        }
        this.nearbyLookupCache.put(key,rVal);
        this.loadedCellsLock.unlock();
        return Collections.unmodifiableCollection(rVal);
    }

    @Override
    public ServerTerrainChunk getChunkAtPosition(int worldX, int worldY, int worldZ) {
        return serverTerrainManager.getChunk(worldX, worldY, worldZ, ServerChunkCache.STRIDE_FULL_RES);
    }

    @Override
    public PathingProgressiveData findPathAsync(Vector3d start, Vector3d end) {
        return Globals.serverState.aiManager.getPathfindingService().queuePathfinding(start, end, this.pathfinder, this);
    }

    @Override
    public Vector3d getMacroEntryPoint(Vector3d point){
        Vector3d rVal = null;

        //find the closest data cell
        ServerDataCell closestCell = null;
        double closestDist = MACRO_SCANNING_BIG_NUMBER;
        for(ServerDataCell cell : this.groundDataCells.values()){
            Vector3i cellChunkPos = this.cellPositionMap.get(cell);
            Vector3d cellRealPos = ServerWorldData.convertChunkToRealSpace(cellChunkPos);
            double dist = cellRealPos.distance(point);
            if(dist < closestDist){
                closestCell = cell;
                closestDist = dist;
            }
        }
        if(closestDist == MACRO_SCANNING_BIG_NUMBER || closestCell == null){
            throw new Error("Failed to find closer cell");
        }

        //get surface height at the (1,?,1) point of this cell
        Vector3i cellChunkPos = this.cellPositionMap.get(closestCell);
        double elevation = this.serverTerrainManager.getElevation(cellChunkPos.x, cellChunkPos.z, 1, 1);

        //store as return vec
        rVal = new Vector3d(
            ServerWorldData.convertVoxelToRealSpace(1, cellChunkPos.x),
            elevation,
            ServerWorldData.convertVoxelToRealSpace(1, cellChunkPos.z)
        );


        return rVal;
    }

    @Override
    public void evaluateMacroObject(MacroObject object){
        //figure out if a cell should contain this object
        Vector3i macroObjectChunkPos = ServerWorldData.convertRealToChunkSpace(object.getPos());
        ServerDataCell container = null;
        for(ServerDataCell serverDataCell : this.groundDataCells.values()){
            Vector3i pos = this.getCellWorldPosition(serverDataCell);
            if(pos.equals(macroObjectChunkPos)){
                container = serverDataCell;
                break;
            }
        }

        //if it is in real space, spawn the object
        if(container != null){
            this.serverContentManager.spawnMacroObject(parent, object);
        }
    }

    @Override
    public boolean containsCell(ServerDataCell cell) {
        return this.groundDataCells.values().contains(cell);
    }

    @Override
    public boolean hasBlocksAtPosition(Vector3i worldPosition) {
        return this.serverWorldData.getServerBlockManager().hasChunk(worldPosition.x, worldPosition.y, worldPosition.z);
    }

}
