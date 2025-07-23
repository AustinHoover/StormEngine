package electrosphere.server;

import electrosphere.engine.Globals;
import electrosphere.net.server.Server;
import electrosphere.net.server.player.PlayerManager;
import electrosphere.net.synchronization.server.EntityValueTrackingService;
import electrosphere.net.synchronization.server.ServerSynchronizationManager;
import electrosphere.server.ai.AIManager;
import electrosphere.server.datacell.EntityDataCellMapper;
import electrosphere.server.datacell.RealmManager;
import electrosphere.server.db.DatabaseController;
import electrosphere.server.saves.Save;
import electrosphere.server.service.CharacterService;
import electrosphere.server.service.LODEmitterService;
import electrosphere.server.service.MacroPathingService;
import electrosphere.server.service.StructureScanningService;
import electrosphere.server.simulation.MicroSimulation;

/**
 * Server state
 */
public class ServerState {
    
    /**
     * Server networking
     */
    public Server server;

    /**
     * Synchronization manager on the server
     */
    public final ServerSynchronizationManager serverSynchronizationManager = new ServerSynchronizationManager();

    /**
     * ai manager
     */
    public final AIManager aiManager = new AIManager(0);

    /**
     * The entity->datacell mapper
     */
    public final EntityDataCellMapper entityDataCellMapper = new EntityDataCellMapper();

    /**
     * Realm manager
     */
    public final RealmManager realmManager = new RealmManager();

    /**
     * The currently loaded save
     */
    public Save currentSave = null;
    
    /**
     * Service for managing characters
     */
    public final CharacterService characterService;

    /**
     * Service for background scanning to detect when players create structures
     */
    public final StructureScanningService structureScanningService;

    /**
     * The lod emitter service
     */
    public final LODEmitterService lodEmitterService;

    /**
     * The macro pathing service
     */
    public final MacroPathingService macroPathingService;

    /**
     * behavior tree tracking service
     */
    public EntityValueTrackingService entityValueTrackingService = new EntityValueTrackingService();

    /**
     * Player manager
     */
    public final PlayerManager playerManager = new PlayerManager();

    /**
     * Database controller
     */
    public final DatabaseController dbController = new DatabaseController();

    /**
     * The micro simulation
     */
    public final MicroSimulation microSimulation = new MicroSimulation();

    /**
     * Constructor
     */
    public ServerState(){
        this.characterService = (CharacterService)Globals.engineState.serviceManager.registerService(new CharacterService());
        this.structureScanningService = (StructureScanningService)Globals.engineState.serviceManager.registerService(new StructureScanningService());
        this.lodEmitterService = (LODEmitterService)Globals.engineState.serviceManager.registerService(new LODEmitterService());
        this.macroPathingService = (MacroPathingService)Globals.engineState.serviceManager.registerService(new MacroPathingService());
    }

}
