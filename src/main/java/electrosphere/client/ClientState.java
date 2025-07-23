package electrosphere.client;

import electrosphere.client.block.ClientBlockManager;
import electrosphere.client.block.cells.ClientBlockCellManager;
import electrosphere.client.chemistry.ClientChemistryCollisionCallback;
import electrosphere.client.entity.character.ClientCharacterManager;
import electrosphere.client.fluid.cells.FluidCellManager;
import electrosphere.client.fluid.manager.ClientFluidManager;
import electrosphere.client.player.ClientPlayerData;
import electrosphere.client.scene.ClientLevelEditorData;
import electrosphere.client.scene.ClientSceneWrapper;
import electrosphere.client.scene.ClientWorldData;
import electrosphere.client.service.ClientTemporalService;
import electrosphere.client.sim.ClientSimulation;
import electrosphere.client.terrain.cells.ClientDrawCellManager;
import electrosphere.client.terrain.foliage.FoliageCellManager;
import electrosphere.client.terrain.manager.ClientTerrainManager;
import electrosphere.collision.CollisionEngine;
import electrosphere.collision.PhysicsCallback;
import electrosphere.data.entity.common.CommonEntityType;
import electrosphere.data.voxel.VoxelType;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.scene.Scene;
import electrosphere.net.client.ClientNetworking;
import electrosphere.net.server.player.Player;
import electrosphere.net.synchronization.client.ClientSynchronizationManager;

/**
 * State on the client
 */
public class ClientState {
    
    /**
     * Data on the currently loaded world
     */
    public ClientWorldData clientWorldData;

    /**
     * The scene on the client
     */
    public final Scene clientScene = new Scene();

    /**
     * The client scene wrapper
     */
    public ClientSceneWrapper clientSceneWrapper;

    /**
     * The client simulation
     */
    public ClientSimulation clientSimulation;

    /**
     * The synchronization manager on the client
     */
    public final ClientSynchronizationManager clientSynchronizationManager = new ClientSynchronizationManager();

    /**
     * The client network connection
     */
    public ClientNetworking clientConnection;

    /**
     * The foliage cell manager
     */
    public FoliageCellManager foliageCellManager;

    /**
     * Manages characters on the client
     */
    public final ClientCharacterManager clientCharacterManager = new ClientCharacterManager();

    /**
     * Manages terrain data on client
     */
    public final ClientTerrainManager clientTerrainManager = new ClientTerrainManager();
    
    /**
     * Manages fluid data on client
     */
    public final ClientFluidManager clientFluidManager = new ClientFluidManager();

    /**
     * Manages block data on client
     */
    public final ClientBlockManager clientBlockManager = new ClientBlockManager();

    /**
     * Terrain cell manager
     */
    public ClientDrawCellManager clientDrawCellManager;

    /**
     * Block cell manager
     */
    public ClientBlockCellManager clientBlockCellManager;

    /**
     * The fluid cell manager
     */
    public FluidCellManager fluidCellManager;

    /**
     * client level editor data management
     */
    public final ClientLevelEditorData clientLevelEditorData = new ClientLevelEditorData();

    /**
     * client current selected voxel type
     */
    public VoxelType clientSelectedVoxelType = null;

    /**
     * the selected type of entity to spawn
     */
    public CommonEntityType selectedSpawntype = null;

    /**
     * Client player data
     */
    public Player clientPlayer;

    /**
     * Current auth username
     */
    public String clientUsername;

    /**
     * Current auth password
     */
    public String clientPassword;

    /**
     * client player data
     */
    public final ClientPlayerData clientPlayerData = new ClientPlayerData();

    /**
     * The client side equivalent of this client's entity on the server
     */
    public Entity playerEntity;

    /**
     * the entity for the first person model (view model)
     */
    public Entity firstPersonEntity;

    /**
     * the player camera entity
     */
    public Entity playerCamera;

    /**
     * The target of the interaction
     */
    public Entity interactionTarget = null;
    
    /**
     * skybox entity
     */
    public Entity skybox;

    /**
     * The inventory entity currently being dragged
     */
    public Entity draggedItem = null;
    
    /**
     * The source inventory of the currently dragged inventory item
     */
    public Object dragSourceInventory = null;

    /**
     * The target container to drop the dragged inventory entity into
     */
    public Entity targetContainer = null;

    /**
     * The number of open inventories
     */
    public int openInventoriesCount = 0;

    //
    //Services
    //
    public final ClientTemporalService clientTemporalService;

    /**
     * Constructor
     */
    public ClientState(){
        this.clientSceneWrapper = new ClientSceneWrapper(this.clientScene, CollisionEngine.create("clientPhysics", new PhysicsCallback()), CollisionEngine.create("clientChem", new ClientChemistryCollisionCallback()), new CollisionEngine("clientInteraction"));
        this.clientTemporalService = (ClientTemporalService)Globals.engineState.serviceManager.registerService(new ClientTemporalService());
    }

}
