package electrosphere.server.datacell;

import electrosphere.collision.CollisionEngine;
import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.collision.hitbox.HitboxManager;
import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.scene.Scene;
import electrosphere.net.parser.net.message.NetworkMessage;
import electrosphere.script.ScriptEngine;
import electrosphere.server.datacell.interfaces.DataCellManager;
import electrosphere.server.datacell.interfaces.PathfindingManager;
import electrosphere.server.entity.ServerContentManager;
import electrosphere.server.macro.MacroData;
import electrosphere.server.macro.MacroDataUpdater;
import electrosphere.server.simulation.MacroSimulation;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.joml.Vector3d;


/**
 * Manages data cells on the server side
 */
public class Realm {

    /**
     * No scene was loaded from script engine alongside this realm
     */
    public static final int NO_SCENE_INSTANCE = -1;
    
    /**
     * The set containing all data cells loaded into this realm
     */
    private Set<ServerDataCell> loadedDataCells = new HashSet<ServerDataCell>();

    /**
     * this is the cell that all players loading into the game (via connection startup, death, etc) reside in
     */
    private ServerDataCell loadingCell = new ServerDataCell(new Scene());

    /**
     * The data cell that will contain in-inventory items
     */
    private ServerDataCell inventoryCell = new ServerDataCell(new Scene());

    /**
     * provides functions for relating data cells to physical locations (eg creating cells, deleting cells, etc)
     */
    private DataCellManager dataCellManager;

    /**
     * The pathfinding manager
     */
    private PathfindingManager pathfindingManager;

    /**
     * Main entity physics collision checking engine
     */
    private CollisionEngine collisionEngine;

    /**
     * The chemistry collision engine
     */
    private CollisionEngine chemistryEngine;

    /**
     * Hitbox manager for the realm
     */
    private HitboxManager hitboxManager;

    /**
     * The world data about the server
     */
    private ServerWorldData serverWorldData;

    /**
     * The content manager
     */
    private ServerContentManager serverContentManager; 

    /**
     * The macro data for the realm
     */
    private MacroData macroData;

    /**
     * The instanceId of the scene that was loaded with this realm
     */
    private int sceneInstanceId = NO_SCENE_INSTANCE;

    /**
     * The list of available spawnpoints
     */
    private List<Vector3d> spawnPoints = new LinkedList<Vector3d>();
    
    /**
     * Realm constructor
     * @param serverWorldData The world data for the realm
     * @param collisionEngine The collision engine for the realm
     * @param chemistryEngine The chemistry system collision engine for the realm
     * @param hitboxManager The hitbox manager for the realm
     * @param serverContentManager The content manager for the realm
     * @param macroData The macro data for the realm (can be null if no macro data is present)
     */
    protected Realm(
        ServerWorldData serverWorldData,
        CollisionEngine collisionEngine,
        CollisionEngine chemistryEngine,
        HitboxManager hitboxManager,
        ServerContentManager serverContentManager,
        MacroData macroData
    ){
        this.serverWorldData = serverWorldData;
        this.collisionEngine = collisionEngine;
        this.chemistryEngine = chemistryEngine;
        this.hitboxManager = hitboxManager;
        this.serverContentManager = serverContentManager;
        this.macroData = macroData;
    }

    /**
     * Creates a new data cell
     * @return The new data cell
     */
    public ServerDataCell createNewCell(){
        ServerDataCell newCell = new ServerDataCell(new Scene());
        loadedDataCells.add(newCell);
        return newCell;
    }

    /**
     * Removes a data cell from tracking in this data cell manager
     * @param cell The data cell to no longer keep track of
     */
    public void deregisterCell(ServerDataCell cell){
        loadedDataCells.remove(cell);
    }


    /**
     * Gets the default loading data cell
     * @return The default loading data cell
     */
    public ServerDataCell getLoadingDataCell(){
        return loadingCell;
    }

    /**
     * Broadcasts a message to all players in a certain serverdatacell
     * @param message The message to send
     * @param cell The serverdatacell
     */
    public void sendNetworkMessageToChunk(NetworkMessage message, Entity e){
        //solve for what data cell the entitiy is in
        ServerDataCell cell = Globals.serverState.entityDataCellMapper.getEntityDataCell(e);
        cell.broadcastNetworkMessage(message);
    }


    /**
     * If we're spawning an entity for the first time, call this method with the cell you want it to start in.
     * It adds the entity to the given cell and initializes it for all players in said cell
     * @param entity The entity we are initializing
     * @param cell The cell we are wanting to initialize the entity in
     */
    public void initializeServerSideEntity(Entity entity, ServerDataCell cell){
        //register entity to this realm
        Globals.serverState.realmManager.mapEntityToRealm(entity, this);
        //add the entity to the cell
        cell.getScene().registerEntity(entity);
        //send the entity to all players
        cell.initializeEntityForNewPlayers(entity, null);
        //register to entity data cell mapper
        Globals.serverState.entityDataCellMapper.registerEntity(entity, cell);
    }


    /**
     * Gets the data cell manager for this realm
     * @return The data cell manager for this realm
     */
    public DataCellManager getDataCellManager(){
        return this.dataCellManager;
    }

    /**
     * Sets the data cell manager for this realm
     * @param dataCellManager The data cell manager for this realm
     */
    protected void setDataCellManager(DataCellManager dataCellManager){
        this.dataCellManager = dataCellManager;
    }

    /**
     * Gets the pathfinding manager
     * @return The pathfinding manager
     */
    public PathfindingManager getPathfindingManager(){
        return pathfindingManager;
    }

    /**
     * Sets the pathfinding manager
     * @param pathfindingManager The pathfinding manager
     */
    protected void setPathfindingManager(PathfindingManager pathfindingManager){
        this.pathfindingManager = pathfindingManager;
    }

    /**
     * Gets the collision engine for physics collision checking in this realm
     * @return The collision engine
     */
    public CollisionEngine getCollisionEngine(){
        return this.collisionEngine;
    }

    /**
     * Gets the hitbox manager backing this realm
     * @return The hitbox manager
     */
    public HitboxManager getHitboxManager(){
        return this.hitboxManager;
    }

    /**
     * Tells the data cell manager to simulate all loaded cells
     */
    protected void simulate(){
        Globals.profiler.beginCpuSample("Realm.simulate");

        //
        //simulate bullet physics engine step
        if(EngineState.EngineFlags.RUN_PHYSICS){
            collisionEngine.simulatePhysics();
            collisionEngine.updateDynamicObjectTransforms();
            PhysicsEntityUtils.serverRepositionEntities(this,collisionEngine);
            chemistryEngine.collide();
        }

        //
        //hitbox sim
        hitboxManager.simulate();

        //
        //main simulation
        dataCellManager.simulate();

        //
        //macro data simulation
        if(this.macroData != null && Globals.serverState.dbController != null && Globals.serverState.dbController.isConnected()){
            MacroSimulation.simulate(this);
        }

        //
        //clear collidable impulse lists
        collisionEngine.clearCollidableImpulseLists();
        chemistryEngine.clearCollidableImpulseLists();
        
        //
        //rebase physics origin
        this.collisionEngine.rebaseWorldOrigin();
        
        Globals.profiler.endCpuSample();
    }

    /**
     * Saves all server data cells in the realm to a given save
     * @param saveName The name of the save
     */
    protected void save(String saveName){
        dataCellManager.save(saveName);
        serverWorldData.getServerTerrainManager().save(saveName);
        serverWorldData.getServerBlockManager().save(saveName);
        if(this.macroData != null){
            this.macroData.save(saveName);
        }
    }

    /**
     * Gets the server world data for this realm
     * @return The server world data
     */
    public ServerWorldData getServerWorldData(){
        return this.serverWorldData;
    }

    /**
     * Gets the content manager for this realm
     * @return The content manager
     */
    public ServerContentManager getServerContentManager(){
        return this.serverContentManager;
    }

    /**
     * Gets the spawn point for the realm
     * @return The spawn point
     */
    public Vector3d getSpawnPoint(){
        if(this.spawnPoints.size() > 0){
            return this.spawnPoints.get(0);
        } else {
            return new Vector3d(0,0,0);
        }
    }

    /**
     * Registers a spawn point
     * @param point The spawn point location
     */
    public void registerSpawnPoint(Vector3d point){
        this.spawnPoints.add(point);
    }

    /**
     * Get the inventory data cell
     * @return The inventory data cell
     */
    public ServerDataCell getInventoryCell(){
        return inventoryCell;
    }

    /**
     * Sets the script-engine side instance id for the scene that was loaded with this realm
     * @param sceneInstanceId The instance id
     */
    public void setSceneInstanceId(int sceneInstanceId){
        this.sceneInstanceId = sceneInstanceId;
    }

    /**
     * Fires a signal in this scene
     * @param signalName The name of the signal
     * @param args The arguments provided alongside the signal
     */
    public void fireSignal(String signalName, Object ... args){
        if(Globals.engineState.scriptEngine != null && Globals.engineState.scriptEngine.isInitialized()){
            Globals.engineState.scriptEngine.getScriptContext().executeSynchronously(() -> {
                if(this.sceneInstanceId != NO_SCENE_INSTANCE){
                    Globals.engineState.scriptEngine.getScriptContext().fireSignal(signalName, sceneInstanceId, args);
                } else {
                    Globals.engineState.scriptEngine.getScriptContext().fireSignal(signalName, ScriptEngine.GLOBAL_SCENE, args);
                }
            });
        }
    }

    /**
     * Gets the macro data in the realm
     * @return
     */
    public MacroData getMacroData(){
        return this.macroData;
    }

    /**
     * Generates macro data that needs to be generated near a given player's position
     * @param playerPosition The player's position
     */
    public void updateMacroData(Vector3d playerPosition){
        if(playerPosition == null){
            throw new Error("Null position!");
        }
        if(macroData != null){
            MacroDataUpdater.update(this, macroData, playerPosition);
        }
    }
    
}
