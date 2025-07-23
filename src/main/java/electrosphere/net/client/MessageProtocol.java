package electrosphere.net.client;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import electrosphere.engine.Globals;
import electrosphere.net.client.protocol.AuthProtocol;
import electrosphere.net.client.protocol.CharacterProtocol;
import electrosphere.net.client.protocol.CombatProtocol;
import electrosphere.net.client.protocol.EntityProtocol;
import electrosphere.net.client.protocol.InventoryProtocol;
import electrosphere.net.client.protocol.LoreProtocol;
import electrosphere.net.client.protocol.PlayerProtocol;
import electrosphere.net.client.protocol.ServerProtocol;
import electrosphere.net.client.protocol.SynchronizationProtocol;
import electrosphere.net.client.protocol.TerrainProtocol;
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

/**
 * The message protocol on the client
 */
public class MessageProtocol {
    
    /**
     * Tracks whether the message protocol has received world data or not
     */
    boolean hasReceivedWorld = false;

    //the lock used for synchronizing the synchronous message queue
    Semaphore synchronousMessageLock = new Semaphore(1);

    //the queue of synchonous network messages
    LinkedList<NetworkMessage> synchronousMessageQueue = new LinkedList<NetworkMessage>();


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
     * Asynchronously handles a message
     * @param message The message
     */
    public void handleAsyncMessage(NetworkMessage message){
        Globals.profiler.beginAggregateCpuSample("MessageProtocol(client).handleAsyncMessage");
        NetworkMessage result = null;
        switch(message.getType()){
            case AUTH_MESSAGE:
                result = this.authProtocol.handleAsyncMessage((AuthMessage)message);
                break;
            case CHARACTER_MESSAGE:
                result = this.characterProtocol.handleAsyncMessage((CharacterMessage)message);
                break;
            case COMBAT_MESSAGE: {
                result = this.combatProtocol.handleAsyncMessage((CombatMessage)message);
            } break;
            case ENTITY_MESSAGE:
                result = this.entityProtocol.handleAsyncMessage((EntityMessage)message);
                break;
            case INVENTORY_MESSAGE:
                result = this.inventoryProtocol.handleAsyncMessage((InventoryMessage)message);
                break;
            case LORE_MESSAGE:
                result = this.loreProtocol.handleAsyncMessage((LoreMessage)message);
                break;
            case PLAYER_MESSAGE:
                result = this.playerProtocol.handleAsyncMessage((PlayerMessage)message);
                break;
            case SERVER_MESSAGE:
                result = this.serverProtocol.handleAsyncMessage((ServerMessage)message);
                break;
            case SYNCHRONIZATION_MESSAGE:
                result = this.synchronizationProtocol.handleAsyncMessage((SynchronizationMessage)message);
                break;
            case TERRAIN_MESSAGE:
                result = this.terrainProtocol.handleAsyncMessage((TerrainMessage)message);
                break;
        }
        //queue bounced messages for synchronous resolution
        if(result != null){
            Globals.profiler.beginAggregateCpuSample("MessageProtocol(client) Await lock to synchronize message");
            this.synchronousMessageLock.acquireUninterruptibly();
            this.synchronousMessageQueue.add(result);
            this.synchronousMessageLock.release();
            Globals.profiler.endCpuSample();
        }
        Globals.profiler.endCpuSample();
    }

    public void handleSyncMessages(){
        Globals.profiler.beginAggregateCpuSample("MessageProtocol(client).handleSyncMessages");
        this.synchronousMessageLock.acquireUninterruptibly();
        for(NetworkMessage message : synchronousMessageQueue){
            switch(message.getType()){
                case AUTH_MESSAGE:
                    this.authProtocol.handleSyncMessage((AuthMessage)message);
                    break;
                case CHARACTER_MESSAGE:
                    this.characterProtocol.handleSyncMessage((CharacterMessage)message);
                    break;
                case COMBAT_MESSAGE: {
                    this.combatProtocol.handleSyncMessage((CombatMessage)message);
                } break;
                case ENTITY_MESSAGE:
                    this.entityProtocol.handleSyncMessage((EntityMessage)message);
                    break;
                case INVENTORY_MESSAGE:
                    this.inventoryProtocol.handleSyncMessage((InventoryMessage)message);
                    break;
                case LORE_MESSAGE:
                    this.loreProtocol.handleSyncMessage((LoreMessage)message);
                    break;
                case PLAYER_MESSAGE:
                    this.playerProtocol.handleSyncMessage((PlayerMessage)message);
                    break;
                case SERVER_MESSAGE:
                    this.serverProtocol.handleSyncMessage((ServerMessage)message);
                    break;
                case SYNCHRONIZATION_MESSAGE:
                    this.synchronizationProtocol.handleSyncMessage((SynchronizationMessage)message);
                    break;
                case TERRAIN_MESSAGE:
                    this.terrainProtocol.handleSyncMessage((TerrainMessage)message);
                    break;
            }
        }
        synchronousMessageQueue.clear();
        this.synchronousMessageLock.release();
        Globals.profiler.endCpuSample();
    }
    
    
    
    
    public void setHasReceivedWorld(boolean hasReceivedWorld){
        this.hasReceivedWorld = hasReceivedWorld;
    }
    
    
    public boolean hasReceivedWorld(){
        return hasReceivedWorld;
    }
}
