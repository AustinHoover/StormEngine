package electrosphere.net.server.player;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.server.datacell.Realm;

/**
 * Server player manager
 */
public class PlayerManager {
    
    Map<Integer,Player> idMap = new HashMap<Integer,Player>();
    
    public PlayerManager(){
        
    }
    
    /**
     * Registers a player
     * @param player The player
     */
    public void registerPlayer(Player player){
        idMap.put(player.getId(),player);
    }
    
    /**
     * Gets a player by their id
     * @param id The id of the player
     * @return The player if it exists, null otherwise
     */
    public Player getPlayerFromId(int id){
        return idMap.get(id);
    }

    /**
     * Gets the first registered player
     * @return The first registered player if it exists, null otherwise
     */
    public Player getFirstPlayer(){
        if(idMap.values().size() > 0){
            return idMap.values().iterator().next();
        }
        return null;
    }

    /**
     * Gets the list of all players
     * @return The list of all players
     */
    public List<Player> getPlayers(){
        List<Player> rVal = new LinkedList<Player>();
        if(idMap != null && idMap.size() > 0){
            rVal.addAll(idMap.values());
        }
        return rVal;
    }

    /**
     * Gets the realm a player is within
     * @param player The player
     * @return The realm if it exists, null otherwise
     */
    public Realm getPlayerRealm(Player player){
        if(Globals.serverState.realmManager.getRealms().size() == 1){
            return Globals.serverState.realmManager.first();
        }
        Entity playerEntity = player.getPlayerEntity();
        if(playerEntity == null){
            throw new IllegalStateException("Trying to get realm of player who does not have an entity assigned!");
        }
        return Globals.serverState.realmManager.getEntityRealm(playerEntity);
    }
    
}
