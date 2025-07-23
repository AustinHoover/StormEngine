package electrosphere.test.testutils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;

import org.joml.Vector3d;
import org.junit.jupiter.api.Assertions;

import java.util.function.Supplier;

import electrosphere.engine.Globals;
import electrosphere.engine.Main;
import electrosphere.engine.loadingthreads.LoadingThread;
import electrosphere.entity.Entity;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.net.server.ServerConnectionHandler;
import electrosphere.server.datacell.utils.EntityLookupUtils;
import electrosphere.server.macro.character.PlayerCharacterCreation;
import electrosphere.server.macro.character.Character;

/**
 * Utils for testing the engine
 */
public class TestEngineUtils {

    /**
     * Prints a logging message
     * @param log the message to print
     */
    public static void log(String log){
        System.out.println(log);
    }

    /**
     * Simulates a certain number of frames
     * @param frameCount The number of frames to simulate
     */
    public static void simulateFrames(int frameCount){
        int i = 0;
        while(i < frameCount){
            Main.setFramestep(1);
            Main.mainLoop(1);
            i++;
        }
    }


    /**
     * The maximum number of frames to wait before an entity propagates to the client
     */
    static final int MAX_FRAMES_TO_WAIT_FOR_CLIENT_PROPAGATION = 10;

    /**
     * Gets the client equivalent of an entity
     * @param serverEntity The server entity
     * @return The client entity
     */
    public static Entity getClientEquivalent(Entity serverEntity){
        int frames = 0;
        while(frames < MAX_FRAMES_TO_WAIT_FOR_CLIENT_PROPAGATION && !Globals.clientState.clientSceneWrapper.containsServerId(serverEntity.getId())){
            TestEngineUtils.simulateFrames(1);
        }
        if(Globals.clientState.clientSceneWrapper.containsServerId(serverEntity.getId())){
            Entity rVal = Globals.clientState.clientSceneWrapper.getEntityFromServerId(serverEntity.getId());
            if(rVal == null){
                fail("Failed to find client entity at server id lookup for id: " + serverEntity.getId());
            }
            return rVal;
        }
        fail("Failed to find client entity at server id lookup for id: " + serverEntity.getId());
        return null;
    }

    /**
     * Gets the server equivalent of an entity
     * @param clientEntity The client entity
     * @return The server entity
     */
    public static Entity getServerEquivalent(Entity clientEntity){
        if(clientEntity == null){
            fail("Provided null entity to get server equivalent!");
        }
        if(Globals.clientState.clientSceneWrapper.containsServerId(clientEntity.getId())){
            fail("Provided server entity to getServerEquivalent");
        }
        int serverId = Globals.clientState.clientSceneWrapper.mapClientToServerId(clientEntity.getId());
        Entity rVal = EntityLookupUtils.getEntityById(serverId);
        if(rVal == null){
            fail("Failed to find client entity at server id lookup for id: " + clientEntity.getId());
        }
        return rVal;
    }

    /**
     * Waits for a given test to be true
     * @param test The test to wait for
     */
    public static void waitForCondition(Supplier<Boolean> test, int maxFrames){
        int frameCount = 0;
        boolean testResult = false;
        while(!(testResult = test.get()) && frameCount < maxFrames){
            TestEngineUtils.simulateFrames(1);
            frameCount++;
        }
        org.junit.jupiter.api.Assertions.assertTrue(testResult);
    }

    /**
     * Waits for a given test to be true
     * @param test The test to wait for
     */
    public static void waitForCondition(Supplier<Boolean> test){
        waitForCondition(test,100);
    }

    /**
     * Flushes any signals that haven't been processed yet
     */
    public static void flush(){
        //
        //wait for client to be fully init'd
        int frames = 0;
        long startTime = System.currentTimeMillis();
        while(Globals.engineState.threadManager.isLoading()){
            TestEngineUtils.simulateFrames(1);
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            frames++;
            if(
                frames > EngineInit.MAX_FRAMES_TO_WAIT &&
                System.currentTimeMillis() - startTime > EngineInit.MAX_TIME_TO_WAIT
            ){
                String errorMessage = "Failed to setup connected test scene!\n" +
                "Still running threads are:\n"
                ;
                for(LoadingThread thread : Globals.engineState.threadManager.getLoadingThreads()){
                    errorMessage = errorMessage + thread.getType() + "\n";
                }
                errorMessage = errorMessage + "frames: " + frames + "\n";
                errorMessage = errorMessage + "Time elapsed: " + (System.currentTimeMillis() - startTime) + "\n";
                Assertions.fail(errorMessage);
            }
        }

        if(frames == 0){
            TestEngineUtils.simulateFrames(1);
        }

        while(
            Globals.elementService.getSignalQueueCount() > 0
        ){
            Globals.elementService.handleAllSignals();
        }
    }

    /**
     * Spawns an entity for the player
     * @return The entity
     */
    public static void spawnPlayerEntity(){
        ObjectTemplate creatureTemplate = ObjectTemplate.createDefault(EntityType.CREATURE, "human");
        ServerConnectionHandler serverConnection = Globals.serverState.server.getFirstConnection();
        serverConnection.setCreatureTemplate(creatureTemplate);
        Character chara = Globals.serverState.characterService.createCharacter(creatureTemplate, serverConnection.getPlayerId(), new Vector3d());
        serverConnection.setCharacterId(chara.getId());
        PlayerCharacterCreation.spawnPlayerCharacter(serverConnection);
    }

}
