package electrosphere.net.parser.net.message;


import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import electrosphere.net.parser.net.message.NetworkMessage.MessageType;

/**
 * Pools message objects to reduce allocations
 */
public class MessagePool {
    
    /**
     * Pools Entity messages
     */
    List<NetworkMessage> entityMessagePool = new LinkedList<NetworkMessage>();

    /**
     * Pools Lore messages
     */
    List<NetworkMessage> loreMessagePool = new LinkedList<NetworkMessage>();

    /**
     * Pools Player messages
     */
    List<NetworkMessage> playerMessagePool = new LinkedList<NetworkMessage>();

    /**
     * Pools Terrain messages
     */
    List<NetworkMessage> terrainMessagePool = new LinkedList<NetworkMessage>();

    /**
     * Pools Server messages
     */
    List<NetworkMessage> serverMessagePool = new LinkedList<NetworkMessage>();

    /**
     * Pools Auth messages
     */
    List<NetworkMessage> authMessagePool = new LinkedList<NetworkMessage>();

    /**
     * Pools Character messages
     */
    List<NetworkMessage> characterMessagePool = new LinkedList<NetworkMessage>();

    /**
     * Pools Inventory messages
     */
    List<NetworkMessage> inventoryMessagePool = new LinkedList<NetworkMessage>();

    /**
     * Pools Synchronization messages
     */
    List<NetworkMessage> synchronizationMessagePool = new LinkedList<NetworkMessage>();

    /**
     * Pools Combat messages
     */
    List<NetworkMessage> combatMessagePool = new LinkedList<NetworkMessage>();

    /**
     * Controls whether the pool should always allocate or not
     */
    boolean alwaysAllocate = false;

    /**
     * Lock for thread-safeing operations
     */
    ReentrantLock lock = new ReentrantLock();

    /**
     * Gets a network message from the pool. Allocates if no free one is available.
     * @param type The type of the message
     * @return A network message of the requested type
     */
    public NetworkMessage get(MessageType type){
        NetworkMessage rVal = null;
        lock.lock();
        if(type == MessageType.ENTITY_MESSAGE){
            if(!alwaysAllocate && entityMessagePool.size() > 0){
                rVal = entityMessagePool.remove(0);
            } else {
                rVal = new EntityMessage();
            }
        } else if(type == MessageType.LORE_MESSAGE){
            if(!alwaysAllocate && loreMessagePool.size() > 0){
                rVal = loreMessagePool.remove(0);
            } else {
                rVal = new LoreMessage();
            }
        } else if(type == MessageType.PLAYER_MESSAGE){
            if(!alwaysAllocate && playerMessagePool.size() > 0){
                rVal = playerMessagePool.remove(0);
            } else {
                rVal = new PlayerMessage();
            }
        } else if(type == MessageType.TERRAIN_MESSAGE){
            if(!alwaysAllocate && terrainMessagePool.size() > 0){
                rVal = terrainMessagePool.remove(0);
            } else {
                rVal = new TerrainMessage();
            }
        } else if(type == MessageType.SERVER_MESSAGE){
            if(!alwaysAllocate && serverMessagePool.size() > 0){
                rVal = serverMessagePool.remove(0);
            } else {
                rVal = new ServerMessage();
            }
        } else if(type == MessageType.AUTH_MESSAGE){
            if(!alwaysAllocate && authMessagePool.size() > 0){
                rVal = authMessagePool.remove(0);
            } else {
                rVal = new AuthMessage();
            }
        } else if(type == MessageType.CHARACTER_MESSAGE){
            if(!alwaysAllocate && characterMessagePool.size() > 0){
                rVal = characterMessagePool.remove(0);
            } else {
                rVal = new CharacterMessage();
            }
        } else if(type == MessageType.INVENTORY_MESSAGE){
            if(!alwaysAllocate && inventoryMessagePool.size() > 0){
                rVal = inventoryMessagePool.remove(0);
            } else {
                rVal = new InventoryMessage();
            }
        } else if(type == MessageType.SYNCHRONIZATION_MESSAGE){
            if(!alwaysAllocate && synchronizationMessagePool.size() > 0){
                rVal = synchronizationMessagePool.remove(0);
            } else {
                rVal = new SynchronizationMessage();
            }
        } else if(type == MessageType.COMBAT_MESSAGE){
            if(!alwaysAllocate && combatMessagePool.size() > 0){
                rVal = combatMessagePool.remove(0);
            } else {
                rVal = new CombatMessage();
            }
        } else {
            throw new Error("Unsupported message type! " + type);
        }
        lock.unlock();
        return rVal;
    }

    /**
     * Releases a message back into the pool
     * @param message The message
     */
    public void release(NetworkMessage message){
        lock.lock();
        if(message instanceof EntityMessage){
            if(entityMessagePool.size() < 1000){
                entityMessagePool.add(message);
            }
        } else if(message instanceof LoreMessage){
            if(loreMessagePool.size() < 1000){
                loreMessagePool.add(message);
            }
        } else if(message instanceof PlayerMessage){
            if(playerMessagePool.size() < 1000){
                playerMessagePool.add(message);
            }
        } else if(message instanceof TerrainMessage){
            if(terrainMessagePool.size() < 1000){
                terrainMessagePool.add(message);
            }
        } else if(message instanceof ServerMessage){
            if(serverMessagePool.size() < 1000){
                serverMessagePool.add(message);
            }
        } else if(message instanceof AuthMessage){
            if(authMessagePool.size() < 1000){
                authMessagePool.add(message);
            }
        } else if(message instanceof CharacterMessage){
            if(characterMessagePool.size() < 1000){
                characterMessagePool.add(message);
            }
        } else if(message instanceof InventoryMessage){
            if(inventoryMessagePool.size() < 1000){
                inventoryMessagePool.add(message);
            }
        } else if(message instanceof SynchronizationMessage){
            if(synchronizationMessagePool.size() < 1000){
                synchronizationMessagePool.add(message);
            }
        } else if(message instanceof CombatMessage){
            if(combatMessagePool.size() < 1000){
                combatMessagePool.add(message);
            }
        } else {
            throw new Error("Unsupported message type! " + message.getClass());
        }
        lock.unlock();
    }

    /**
     * Sets whether the pool should always allocate or try to pool
     * @param alwaysAllocate true to always allocate a new message, false enable message pooling
     */
    public void setAlwaysAllocate(boolean alwaysAllocate){
        this.alwaysAllocate = alwaysAllocate;
    }

}
