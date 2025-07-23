package electrosphere.engine.loadingthreads;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.auth.AuthenticationManager;
import electrosphere.data.entity.creature.CreatureData;
import electrosphere.data.entity.creature.visualattribute.VisualAttribute;
import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.engine.threads.LabeledThread.ThreadLabel;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.NetUtils;
import electrosphere.net.client.ClientNetworking;
import electrosphere.net.parser.net.message.CharacterMessage;
import electrosphere.net.server.Server;
import electrosphere.net.server.ServerConnectionHandler;
import electrosphere.net.server.player.Player;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.entity.serialization.ContentSerialization;
import electrosphere.server.macro.character.Character;

/**
 * Utilities for all loading thread types
 */
public class LoadingUtils {

    /**
     * The size of the buffer
     */
    static final int STREAM_BUFFER_SIZE = 16 * 1024 * 1024;

    /**
     * The name of the editor race
     */
    public static final String EDITOR_RACE_NAME = "editor";
    
    
    
    static void initServerThread(){
        //start server networking
        if(EngineState.EngineFlags.RUN_SERVER){
            Globals.serverState.server = new Server(NetUtils.getPort());
            Thread serverThread = new Thread(Globals.serverState.server);
            Globals.engineState.threadManager.start(ThreadLabel.NETWORKING_SERVER, serverThread);
        }
    }

    /**
     * Initializes the authentication manager
     * @param mock true if it should make a mock authentication manager, false for a real auth manager
     */
    static void initAuthenticationManager(boolean mock){
        if(EngineState.EngineFlags.RUN_SERVER){
            Globals.authenticationManager = AuthenticationManager.create(mock);
        }
    }
    
    
    
    static void initClientThread(){
        //start client networking
        if(EngineState.EngineFlags.RUN_CLIENT){
            Globals.clientState.clientConnection = new ClientNetworking(NetUtils.getAddress(),NetUtils.getPort());
            Globals.engineState.threadManager.start(ThreadLabel.NETWORKING_CLIENT, new Thread(Globals.clientState.clientConnection));
        }
    }


    /**
     * Initializes a local connection 
     * @param runServerThread true if should run the server in a separate thread, false otherwise
     * @return The server connection
     */
    static ServerConnectionHandler initLocalConnection(boolean runServerThread){
        ServerConnectionHandler rVal = null;
        try {
            if(runServerThread){
                LoadingUtils.initServerThread();
            } else {
                Globals.serverState.server = new Server(NetUtils.getPort());
            }
            //client -> server pipe
            PipedInputStream clientInput = new PipedInputStream(STREAM_BUFFER_SIZE);
            PipedOutputStream serverOutput = new PipedOutputStream(clientInput);
            //server -> client pipe
            PipedInputStream serverInput = new PipedInputStream(STREAM_BUFFER_SIZE);
            PipedOutputStream clientOutput;
            clientOutput = new PipedOutputStream(serverInput);
            //start server communication thread
            rVal = Globals.serverState.server.addLocalPlayer(serverInput, serverOutput);
            //start client communication thread
            Globals.clientState.clientConnection = new ClientNetworking(clientInput,clientOutput);
            Globals.engineState.threadManager.start(ThreadLabel.NETWORKING_CLIENT, new Thread(Globals.clientState.clientConnection));
        } catch (IOException e) {
            LoggerInterface.loggerNetworking.ERROR(e);
        }
        return rVal;
    }


    /**
     * Loads graphics assets necessary for the client of the game engine. This should be stuff that is used essentially universally (ie textures for debugging).
     */
    protected static void initGameGraphicalEntities(){
        Globals.assetManager.addTexturePathtoQueue("Textures/transparent_red.png");
        Globals.assetManager.addTexturePathtoQueue("Textures/transparent_blue.png");
        Globals.assetManager.addTexturePathtoQueue("Textures/transparent_grey.png");
    }


    /**
     * Spawns the character, and sets server side connection player object values to the appropriate chunk
     */
    static void spawnLocalPlayerTestEntity(ServerConnectionHandler serverPlayerConnection, boolean isEditor){
        //
        //Create entity
        //
        //send default template back
        String race = EDITOR_RACE_NAME;
        if(!isEditor){
            List<String> races = Globals.gameConfigCurrent.getCreatureTypeLoader().getPlayableRaces().stream().filter((String name) -> !name.equals(EDITOR_RACE_NAME)).collect(Collectors.toList());
            race = races.get(new Random().nextInt(races.size()));
        }
        CreatureData type = Globals.gameConfigCurrent.getCreatureTypeLoader().getType(race);
        ObjectTemplate template = ObjectTemplate.create(EntityType.CREATURE, race);
        for(VisualAttribute attribute : type.getVisualAttributes()){
            if(attribute.getType().equals(VisualAttribute.TYPE_BONE)){
                float min = attribute.getMinValue();
                float max = attribute.getMaxValue();
                float defaultValue = min + (max - min)/2.0f;
                //add attribute to creature template
                template.putAttributeValue(attribute.getAttributeId(), defaultValue);
            } else if(attribute.getType().equals(VisualAttribute.TYPE_REMESH)){
                template.putAttributeValue(attribute.getAttributeId(), attribute.getVariants().get(0).getId());
            }
        }
        String[] itemIds = new String[]{
            "terrainTool",
            "spawningPalette",
            "entityinspector",
            "waterSpawner",
            "fabTool",
            "roomTool"
        };
        int i = 0;
        for(String itemId : itemIds){
            template.getInventoryData().addToolbarItem(i + "", ContentSerialization.createNewSerialization(EntityType.ITEM, itemId));
            i++;
        }
        //set player character template
        serverPlayerConnection.setCreatureTemplate(template);
        Character chara = Globals.serverState.characterService.createCharacter(template, serverPlayerConnection.getPlayerId(), new Vector3d());
        Globals.clientState.clientConnection.queueOutgoingMessage(CharacterMessage.constructRequestSpawnCharacterMessage(chara.getId() + ""));

        //set player world-space coordinates
        Player playerObject = Globals.serverState.playerManager.getFirstPlayer();
        Realm realm = Globals.serverState.realmManager.getRealms().iterator(). next();
        Vector3d spawnPoint = realm.getSpawnPoint();
        playerObject.setWorldPos(new Vector3i(
            ServerWorldData.convertRealToChunkSpace(spawnPoint.x),
            ServerWorldData.convertRealToChunkSpace(spawnPoint.y),
            ServerWorldData.convertRealToChunkSpace(spawnPoint.z)
        ));
    }

    static void initMacroSimulation(){
        // Globals.macroData = MacroData.generateWorld(0);
//        Globals.macroData.describeWorld();
        // Globals.macroSimulation = new MacroSimulation();
        // Globals.macroSimulation.simulate();
//        Town startTown = Globals.macroData.getTowns().get(0);
//        Vector2i firstPos = startTown.getPositions().get(0);
//        double startX = firstPos.x * Globals.serverTerrainManager.getChunkWidth();
//        double startZ = firstPos.y * Globals.serverTerrainManager.getChunkWidth();
//        Globals.spawnPoint.set((float)startX,(float)Globals.commonWorldData.getElevationAtPoint(new Vector3d(startX,0,startZ)),(float)startZ);
//        Globals.macroSimulation.setReady(true);
    }
    
    /**
     * Sets the server simulations to ready
     */
    static void setSimulationsToReady(){
        Globals.serverState.microSimulation.setReady(true);
    }

}
