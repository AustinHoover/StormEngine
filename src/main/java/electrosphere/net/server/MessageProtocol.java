package electrosphere.net.server;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.AuthMessage;
import electrosphere.net.parser.net.message.CharacterMessage;
import electrosphere.net.parser.net.message.CombatMessage;
import electrosphere.net.parser.net.message.EntityMessage;
import electrosphere.net.parser.net.message.InventoryMessage;
import electrosphere.net.parser.net.message.LoreMessage;
import electrosphere.net.parser.net.message.NetworkMessage;
import electrosphere.net.parser.net.message.PlayerMessage;
import electrosphere.net.parser.net.message.ServerMessage;
import electrosphere.net.parser.net.message.SynchronizationMessage;
import electrosphere.net.parser.net.message.TerrainMessage;
import electrosphere.net.server.protocol.AuthProtocol;
import electrosphere.net.server.protocol.CharacterProtocol;
import electrosphere.net.server.protocol.CombatProtocol;
import electrosphere.net.server.protocol.EntityProtocol;
import electrosphere.net.server.protocol.InventoryProtocol;
import electrosphere.net.server.protocol.LoreProtocol;
import electrosphere.net.server.protocol.PlayerProtocol;
import electrosphere.net.server.protocol.ServerProtocol;
import electrosphere.net.server.protocol.SynchronizationProtocol;
import electrosphere.net.server.protocol.TerrainProtocol;

/**
 * The server message protocol
 */
public class MessageProtocol {
    
    //the lock used for synchronizing the synchronous message queue
    Semaphore synchronousMessageLock = new Semaphore(1);

    //the queue of synchonous network messages
    LinkedList<NetworkMessage> synchronousMessageQueue = new LinkedList<NetworkMessage>();

    //The server connection handler
    ServerConnectionHandler serverConnectionHandler;

    //The individual protocols
    AuthProtocol authProtocol = new AuthProtocol();
    CharacterProtocol characterProtocol = new CharacterProtocol();
    CombatProtocol combatProtocol = new CombatProtocol();
    EntityProtocol entityProtocol = new EntityProtocol();
    InventoryProtocol inventoryProtocol = new InventoryProtocol();
    LoreProtocol loreProtocol = new LoreProtocol();
    PlayerProtocol playerProtocol = new PlayerProtocol();
    ServerProtocol serverProtocol = new ServerProtocol();
    SynchronizationProtocol synchronizationProtocol = new SynchronizationProtocol();
    TerrainProtocol terrainProtocol = new TerrainProtocol();

    /**
     * Constructor
     * @param serverConnectionHandler The linked server connection handler
     */
    public MessageProtocol(ServerConnectionHandler serverConnectionHandler){
        this.serverConnectionHandler = serverConnectionHandler;
    }

    /**
     * Asynchronously handles a message
     * @param message The message
     */
    public void handleAsyncMessage(NetworkMessage message){
        Globals.profiler.beginAggregateCpuSample("MessageProtocol(server).handleAsyncMessage");
        printMessage(message);
        NetworkMessage result = null;
        switch(message.getType()){
            case AUTH_MESSAGE:
                result = this.authProtocol.handleAsyncMessage(serverConnectionHandler, (AuthMessage)message);
                break;
            case CHARACTER_MESSAGE:
                result = this.characterProtocol.handleAsyncMessage(serverConnectionHandler, (CharacterMessage)message);
                break;
            case COMBAT_MESSAGE: {
                result = this.combatProtocol.handleAsyncMessage(serverConnectionHandler, (CombatMessage)message);
            } break;
            case ENTITY_MESSAGE:
                result = this.entityProtocol.handleAsyncMessage(serverConnectionHandler, (EntityMessage)message);
                break;
            case INVENTORY_MESSAGE:
                result = this.inventoryProtocol.handleAsyncMessage(serverConnectionHandler, (InventoryMessage)message);
                break;
            case LORE_MESSAGE:
                result = this.loreProtocol.handleAsyncMessage(serverConnectionHandler, (LoreMessage)message);
                break;
            case PLAYER_MESSAGE:
                result = this.playerProtocol.handleAsyncMessage(serverConnectionHandler, (PlayerMessage)message);
                break;
            case SERVER_MESSAGE:
                result = this.serverProtocol.handleAsyncMessage(serverConnectionHandler, (ServerMessage)message);
                break;
            case SYNCHRONIZATION_MESSAGE:
                result = this.synchronizationProtocol.handleAsyncMessage(serverConnectionHandler, (SynchronizationMessage)message);
                break;
            case TERRAIN_MESSAGE:
                result = this.terrainProtocol.handleAsyncMessage(serverConnectionHandler, (TerrainMessage)message);
                break;
        }
        //queue bounced messages for synchronous resolution
        if(result != null){
            this.synchronousMessageLock.acquireUninterruptibly();
            this.synchronousMessageQueue.add(result);
            LoggerInterface.loggerNetworking.DEBUG_LOOP("ADD SYNC MESSAGE [Sync queue size: " + this.synchronousMessageQueue.size() + "]");
            this.synchronousMessageLock.release();
        }
        Globals.profiler.endCpuSample();
    }

    public void handleSyncMessages(){
        Globals.profiler.beginAggregateCpuSample("MessageProtocol(server).handleSyncMessages");
        this.synchronousMessageLock.acquireUninterruptibly();
        LoggerInterface.loggerNetworking.DEBUG_LOOP("[SERVER] HANDLE SYNC MESSAGE [Sync queue size: " + this.synchronousMessageQueue.size() + "]");
        for(NetworkMessage message : synchronousMessageQueue){
            switch(message.getType()){
                case AUTH_MESSAGE:
                    this.authProtocol.handleSyncMessage(serverConnectionHandler, (AuthMessage)message);
                    break;
                case CHARACTER_MESSAGE:
                    this.characterProtocol.handleSyncMessage(serverConnectionHandler, (CharacterMessage)message);
                    break;
                case COMBAT_MESSAGE: {
                    this.combatProtocol.handleSyncMessage(serverConnectionHandler, (CombatMessage)message);
                } break;
                case ENTITY_MESSAGE:
                    this.entityProtocol.handleSyncMessage(serverConnectionHandler, (EntityMessage)message);
                    break;
                case INVENTORY_MESSAGE:
                    this.inventoryProtocol.handleSyncMessage(serverConnectionHandler, (InventoryMessage)message);
                    break;
                case LORE_MESSAGE:
                    this.loreProtocol.handleSyncMessage(serverConnectionHandler, (LoreMessage)message);
                    break;
                case PLAYER_MESSAGE:
                    this.playerProtocol.handleSyncMessage(serverConnectionHandler, (PlayerMessage)message);
                    break;
                case SERVER_MESSAGE:
                    this.serverProtocol.handleSyncMessage(serverConnectionHandler, (ServerMessage)message);
                    break;
                case SYNCHRONIZATION_MESSAGE:
                    this.synchronizationProtocol.handleSyncMessage(serverConnectionHandler, (SynchronizationMessage)message);
                    break;
                case TERRAIN_MESSAGE:
                    this.terrainProtocol.handleSyncMessage(serverConnectionHandler, (TerrainMessage)message);
                    break;
            }
        }
        synchronousMessageQueue.clear();
        this.synchronousMessageLock.release();
        Globals.profiler.endCpuSample();
    }

    /**
     * Print out the network message type, this only prints ping and pong if echoPings is true
     */
    void printMessage(NetworkMessage message){
        LoggerInterface.loggerNetworking.DEBUG_LOOP("[Server] New message " + message.getType());
    }

}
