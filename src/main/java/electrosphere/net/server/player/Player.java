package electrosphere.net.server.player;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.net.NetUtils;
import electrosphere.net.parser.net.message.NetworkMessage;
import electrosphere.net.server.ServerConnectionHandler;

import java.util.concurrent.Semaphore;

import org.joml.Vector3i;

/**
 * A client logged into the server
 */
public class Player {

    /**
     * The default server-side simulation radius in chunks
     */
    public static final int DEFAULT_SIMULATION_RADIUS = 7;

    /**
     * DBID the client assigns to the player object
     */
    public static final int CLIENT_DB_ID = 0;

    /**
     * Id incrementer lock
     */
    static Semaphore idIncrementerLock = new Semaphore(1);

    /**
     * The actual incrementing id counter
     */
    static int idIncrementer = 0;
    
    /**
     * The corresponding connection handler
     */
    private ServerConnectionHandler connectionHandler;

    /**
     * The id of the player
     */
    private int id;

    /**
     * The database's id of the player
     */
    private int dbId;

    /**
     * The world position of this player
     */
    private Vector3i worldPos;

    /**
     * The simulation radius of this player
     */
    private int simulationRadius = DEFAULT_SIMULATION_RADIUS;

    /**
     * The player's primary entity
     */
    private Entity playerEntity;

    /**
     * Tracks whether the player's entity has been sent or not
     */
    private boolean hasSentPlayerEntity = false;
    
    /**
     * Constructor
     * @param connectionHandler The corresponding connection
     * @param dbId The database's id of the player
     */
    public Player(ServerConnectionHandler connectionHandler, int dbId){
        this.connectionHandler = connectionHandler;
        id = connectionHandler.getPlayerId();
        this.dbId = dbId;
        this.simulationRadius = Globals.gameConfigCurrent.getSettings().getGameplayPhysicsCellRadius();
    }

    /**
     * Used when initing a local connection
     * @param id The id of the local connection
     */
    public Player(int id, int dbId){
        this.id = id;
        this.dbId = dbId;
    }

    /**
     * Gets the id of the player
     * @return The player's id
     */
    public int getId() {
        if(connectionHandler != null){
            return this.connectionHandler.getPlayerId();
        }
        return id;
    }
    
    /**
     * Adds a message that should be sent to this player
     * @param message The message
     */
    public void addMessage(NetworkMessage message){
        connectionHandler.addMessagetoOutgoingQueue(message);
    }

    /**
     * Gets the world position of the player
     * @return The world position
     */
    public Vector3i getWorldPos() {
        return worldPos;
    }

    /**
     * Sets the world position of the player
     * @param worldPos The world position
     */
    public void setWorldPos(Vector3i worldPos) {
        this.worldPos = worldPos;
    }

    /**
     * Gets the simulation radius of the player
     * @return The simulation radius
     */
    public int getSimulationRadius() {
        return simulationRadius;
    }

    /**
     * Sets the simulation radius of the player
     * @param simulationRadius The simulation radius
     */
    public void setSimulationRadius(int simulationRadius) {
        this.simulationRadius = simulationRadius;
    }

    /**
     * Gets the player's entity
     * @return The player's entity
     */
    public Entity getPlayerEntity() {
        return playerEntity;
    }

    /**
     * Sets the player's entity
     * @param playerEntity The player's entity
     */
    public void setPlayerEntity(Entity playerEntity) {
        boolean isReplacing = false;
        if(this.playerEntity != null){
            isReplacing = true;
        }
        this.playerEntity = playerEntity;
        if(isReplacing){
            this.addMessage(NetUtils.createSetCreatureControllerIdEntityMessage(playerEntity));
        }
    }

    /**
     * Gets the next available id
     * @return The id
     */
    public static int getNewId(){
        int rVal = -1;
        idIncrementerLock.acquireUninterruptibly();
        rVal = idIncrementer;
        idIncrementer++;
        idIncrementerLock.release();
        return rVal;
    }

    /**
     * Gets whether the player has been sent their entity or not
     * @return true if has been sent, false otherwise
     */
    public boolean hasSentPlayerEntity() {
        return hasSentPlayerEntity;
    }

    /**
     * Sets whether the player has been sent their entity or not
     * @param hasSentPlayerEntity true if has been sent, false otherwise
     */
    public void setHasSentPlayerEntity(boolean hasSentPlayerEntity) {
        this.hasSentPlayerEntity = hasSentPlayerEntity;
    }

    /**
     * Gets the database's id for the player
     * @return The database's id for the player
     */
    public int getDBID(){
        return this.dbId;
    }

    
    
    
}
