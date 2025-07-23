package electrosphere.server;

import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;

/**
 * Functions that should be fired every server frame
 */
public class MainServerFunctions {
    
    /**
     * Calls the main server routines that should fire each frame
     */
    public static void simulate(){
        //check dependencies
        if(Globals.serverState == null){
            return;
        }

        Globals.profiler.beginCpuSample("MainServerFunctions.simulate");

        //
        //Cleanup disconnected clients
        Globals.profiler.beginCpuSample("MainServerFunctions.simulate - Cleanup connections");
        if(Globals.serverState.server != null){
            Globals.serverState.server.cleanupDeadConnections();
        }
        Globals.profiler.endCpuSample();

        //
        //Synchronous player message parsing\
        Globals.profiler.beginCpuSample("MainServerFunctions.simulate - Server synchronous packet parsing");
        if(Globals.serverState.server != null){
            Globals.serverState.server.synchronousPacketHandling();
        }
        Globals.profiler.endCpuSample();
        Globals.profiler.beginCpuSample("MainServerFunctions.simulate - Server process synchronization messages");
        if(Globals.serverState.serverSynchronizationManager != null){
            Globals.serverState.serverSynchronizationManager.processMessages();
        }
        Globals.profiler.endCpuSample();

        //
        //Update AI
        Globals.serverState.aiManager.simulate();

        //
        //Services
        MainServerFunctions.simulateServices();

        //
        //Simulation
        Globals.profiler.beginCpuSample("MainServerFunctions.simulate - Realm simulation");
        LoggerInterface.loggerEngine.DEBUG_LOOP("Begin server realm simulation");
        Globals.serverState.realmManager.simulate();
        Globals.profiler.endCpuSample();

        Globals.profiler.endCpuSample();
    }

    /**
     * Simulates server services
     */
    private static void simulateServices(){
        Globals.profiler.beginCpuSample("MainServerFunctions.simulateServices");
        Globals.serverState.structureScanningService.simulate();
        Globals.serverState.lodEmitterService.simulate();
        Globals.profiler.endCpuSample();
    }
    
}
