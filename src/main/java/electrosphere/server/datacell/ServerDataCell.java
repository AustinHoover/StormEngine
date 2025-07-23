package electrosphere.server.datacell;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.scene.Scene;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.foliage.FoliageUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.net.parser.net.message.EntityMessage;
import electrosphere.net.parser.net.message.NetworkMessage;
import electrosphere.net.server.player.Player;
import electrosphere.server.macro.character.Character;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Container for entities loaded into memory. This isn't intended to be in charge
 * of simulation. It just acts as an object to relate players and entities by location.
 * This SHOULD be used for networking purposes. This is the mechanism to scope
 * network messages by location. If you are looking for something closer to a scene from
 * a traditional game engine, Realm is effectively a scene for all intents and
 * purposes.
 * 
 */
public class ServerDataCell {
    
    /**
     * All players attached to this server data cell
     */
    private Set<Player> activePlayers = new HashSet<Player>();

    /**
     * The scene backing the server data cell
     */
    private Scene scene;

    /**
     * Controls whether the server data cell simulates its entities or not
     */
    private boolean ready = false;
    
    /**
     * Constructs a datacell based on a virtual cell. Should be used when a player
     * first comes into range of the cell.
     * @param virtualCell 
     */
    protected ServerDataCell(Scene scene){
        this.scene = scene;
    }
    

    /**
     * Creates a server data cell
     * @param scene The scene to wrap the server data cell around
     * @return The server data cell
     */
    protected static ServerDataCell createServerDataCell(Scene scene){
        return new ServerDataCell(scene);
    }
    
    /**
     * Add a player to the current datacell. This should be done if a player moves
     * into close enough proximity with the cell that they are concerned with
     * the microlevel simulation of the cell. This should _not_ be called if
     * this is the first player coming into range.
     * @param p 
     */
    public void addPlayer(Player p){
        if(!this.containsPlayer(p)){
            activePlayers.add(p);
            this.serializeStateToPlayer(p);
        }
    }
    
    /**
     * Removes a player from the current datacell. Basically the player has moved
     * far enough away that this cell doesn't concern them anymore.
     * If the number of players for this cell is zero, we should prepare it to be
     * unloaded from memory and converted back into a virtual cell.
     * @param p 
     */
    public void removePlayer(Player p){
        activePlayers.remove(p);
    }

    /**
     * Gets the set of all players in the server data cell
     * @return The set of players in the data cell
     */
    public Set<Player> getPlayers(){
        return activePlayers;
    }
    
    /**
     * This should be used to translate a character from macrolevel simulation to
     * microlevel, datacell based simulation.
     * @param character 
     */
    public void addCharacter(Character character){
        // Entity newEntity = new Entity();
        // loadedEntities.add(newEntity);
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    /**
     * Broadcast a message to all players within range of this cell.
     * @param message 
     */
    public void broadcastNetworkMessage(NetworkMessage message){
        for(Player player : activePlayers){
            if(player != Globals.clientState.clientPlayer){
                player.addMessage(message);
            }
        }
    }

    /**
     * Serializes the given creature to all players in this cell that aren't in the provided list of players
     * whom have already received spawn messages for the entity
     * @param creature The creature entity to spawn
     * @param previousCell The datacell the creature was previously in, which we will use to selectively not send the creature. Pass null if there is no previous cell (eg on first initialization).
     */
    public void initializeEntityForNewPlayers(Entity creature, ServerDataCell previousCell){
        for(Player player : this.activePlayers){
            if(previousCell != null){
                if(!previousCell.containsPlayer(player)){
                    this.serializeEntityToPlayer(creature,player);
                }
            } else {
                this.serializeEntityToPlayer(creature,player);
            }
        }
    }
    
    /**
     * Sends the current state of the datacell to the player
     * Commonly, this should be called when a player is added to the cell
     */
    private void serializeStateToPlayer(Player player){
        for(Entity entity : scene.getEntityList()){
            this.serializeEntityToPlayer(entity,player);
        }
    }

    /**
     * Serializes an entity to a given player
     * @param entity The entity to serialize
     * @param player The player to send the entity to
     */
    private void serializeEntityToPlayer(Entity entity, Player player){
        if(!player.hasSentPlayerEntity() || player.getPlayerEntity() == null || player.getPlayerEntity() != entity){
            EntityType type = CommonEntityUtils.getEntityType(entity);
            if(type != null){
                switch(type){
                    case CREATURE: {
                        CreatureUtils.sendEntityToPlayer(player, entity);
                    } break;
                    case ITEM: {
                        ItemUtils.sendEntityToPlayer(player, entity);
                    } break;
                    case FOLIAGE: {
                        FoliageUtils.sendFoliageToPlayer(player, entity);
                    } break;
                    case COMMON: {
                        CommonEntityUtils.sendEntityToPlayer(player, entity);
                    } break;
                    case ENGINE: {
                        //silently ignore
                    } break;
                }
            }
        }
    }
    
    
    /**
     * Check if player is relevant to cell
     */
    public boolean containsPlayer(Player player){
        return activePlayers.contains(player);
    }


    /**
     * Moves an entity from one datacell to another. This implicitly destroys the entity on player connections that would no longer
     * observe said entity (IE it sends destroy packets). It also initializes the entity for players that would not have already been
     * tracking the entity
     * @param entity The entity to move
     * @param oldCell The old datacell it used to be in
     * @param newCell The new datacell it's moving to
     */
    public static void moveEntityFromCellToCell(Entity entity, ServerDataCell oldCell, ServerDataCell newCell){
        if(entity == null){
            throw new Error("Passed null entity! " + entity);
        }
        if(newCell == null){
            throw new Error("Passed null newCell! " + newCell);
        }
        if(oldCell == null){
            throw new Error("Passed null oldCell! " + oldCell);
        }
        //swap which holds the entity
        List<String> tags = oldCell.getScene().extractTags(entity);
        oldCell.getScene().deregisterEntity(entity);
        newCell.getScene().registerEntity(entity);
        if(tags != null){
            newCell.getScene().registerEntityToTags(entity, tags);
        }
        //update entity data cell mapper
        Globals.serverState.entityDataCellMapper.updateEntityCell(entity, newCell);
        //send the entity to new players that should care about it
        for(Player player : newCell.activePlayers){
            if(player.getPlayerEntity() == null || player.getPlayerEntity() != entity){
                if(oldCell != null){
                    //if the player hasn't already seen the entity, serialize it
                    if(!oldCell.containsPlayer(player)){
                        newCell.serializeEntityToPlayer(entity, player);
                    }
                } else {
                    //if the entity wasn't in a previous cell, send it to all players
                    newCell.serializeEntityToPlayer(entity, player);
                }
            }
        }
        //delete the entity for players that dont care about it
        for(Player player : oldCell.activePlayers){
            if(
                !newCell.containsPlayer(player) &&
                (player.getPlayerEntity() == null || player.getPlayerEntity() != entity)
            ){
                //if the player isn't also in the new cell, delete the entity
                player.addMessage(EntityMessage.constructDestroyMessage(entity.getId()));
            }
        }
    }

    /**
     * Gets the scene backing this data cell
     * @return The scene backing this data cell
     */
    public Scene getScene(){
        return scene;
    }

    /**
     * Gets the simulation ready status of the server data cell
     * @return True if ready, false otherwise
     */
    public boolean isReady(){
        return ready;
    }

    /**
     * Sets the simulation ready status of the server data cell
     * @param ready True if ready, false otherwise
     */
    public void setReady(boolean ready){
        this.ready = ready;
    }
    
}
