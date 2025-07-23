package electrosphere.test.testutils;

import java.util.concurrent.TimeUnit;

import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.engine.loadingthreads.LoadingThread;
import electrosphere.entity.Entity;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.net.NetUtils;
import electrosphere.net.server.ServerConnectionHandler;
import electrosphere.net.server.player.Player;
import electrosphere.server.macro.character.PlayerCharacterCreation;

/**
 * Utilities for testing in the viewport
 */
public class TestViewportUtils {

    //The maximum number of frames to wait before failing the startup routine
    public static final int MAX_FRAMES_TO_WAIT = 100;

    /**
     * Spawns a creature assigned to the player
     * @param creatureType The type of creature to spawn
     * @return Returns the player's entity
     */
    public static Entity spawnPlayerCharacter(String creatureType){
        //spawn creature
        ObjectTemplate creatureTemplate = ObjectTemplate.createDefault(EntityType.CREATURE, creatureType);
        Entity creature = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(0,0,0), creatureType, creatureTemplate);

        //get required data
        ServerConnectionHandler connectionHandler = Globals.serverState.server.getFirstConnection();
        Player playerObj = Globals.serverState.playerManager.getFirstPlayer();

        //attach
        PlayerCharacterCreation.attachEntityToPlayerObject(creature, playerObj, connectionHandler);
        //must manually send packet because the player is added to the viewport before the entity is created
        //in the real flow, the entity is created, then the player is sent
        playerObj.addMessage(NetUtils.createSetCreatureControllerIdEntityMessage(creature));

        //wait for player to spawn
        int frames = 0;
        while(Globals.clientState.playerEntity == null){
            TestEngineUtils.simulateFrames(1);
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            frames++;
            if(frames > MAX_FRAMES_TO_WAIT){
                String errorMessage = "Failed to spawn player character!\n" +
                "Still running threads are:\n"
                ;
                for(LoadingThread thread : Globals.engineState.threadManager.getLoadingThreads()){
                    errorMessage = errorMessage + thread.getType() + "\n";
                }
                throw new IllegalStateException(errorMessage);
            }
        }

        return creature;
    }
    
}
