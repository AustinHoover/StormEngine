package electrosphere.server.macro.character;

import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.state.server.ServerCharacterData;
import electrosphere.entity.state.server.ServerPlayerViewDirTree;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.server.ServerConnectionHandler;
import electrosphere.net.server.player.Player;
import electrosphere.net.server.protocol.CharacterProtocol;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerWorldData;

/**
 * Deals with spawning player characters
 */
public class PlayerCharacterCreation {
    
    /**
     * Spawns the player's character
     * @param connectionHandler The connection handler of the player
     */
    public static Entity spawnPlayerCharacter(ServerConnectionHandler connectionHandler){
        Player playerObject = Globals.serverState.playerManager.getPlayerFromId(connectionHandler.getPlayerId());
        Realm realm = Globals.serverState.realmManager.getRealms().iterator().next();

        //
        //get template
        Character charaData = Globals.serverState.characterService.getCharacter(connectionHandler.getCharacterId());
        ObjectTemplate template = charaData.getCreatureTemplate();
        if(connectionHandler.getCharacterId() == CharacterProtocol.SPAWN_EXISTING_TEMPLATE){
            template = connectionHandler.getCurrentCreatureTemplate();
        }
        String raceName = template.getObjectType();

        //
        //spawn entity in world
        Vector3d spawnPoint = PlayerCharacterCreation.solveSpawnPoint(realm, connectionHandler);
        Globals.serverState.lodEmitterService.addTempVec(spawnPoint);
        Entity newPlayerEntity = CreatureUtils.serverSpawnBasicCreature(realm,new Vector3d(spawnPoint.x,spawnPoint.y,spawnPoint.z),raceName,template);

        //
        //attach entity to player object
        LoggerInterface.loggerEngine.INFO("Spawned entity for player. Entity id: " + newPlayerEntity.getId() + " Player id: " + playerObject.getId());
        PlayerCharacterCreation.attachEntityToPlayerObject(newPlayerEntity,playerObject,connectionHandler);
        playerObject.setWorldPos(new Vector3i(
            ServerWorldData.convertRealToChunkSpace(spawnPoint.x),
            ServerWorldData.convertRealToChunkSpace(spawnPoint.y),
            ServerWorldData.convertRealToChunkSpace(spawnPoint.z)
        ));
        realm.getDataCellManager().addPlayerToRealm(playerObject);
        //save character's position in case the engine crashes for some reason (ie I hit the x button instead of save)
        Globals.serverState.characterService.saveCharacter(newPlayerEntity);

        //must come after the player is assigned, otherwise the player will not get the item attachment messages
        CreatureUtils.serverApplyTemplate(realm, newPlayerEntity, template);

        //if macro data hasn't been generated in this area, generate it
        //but only if it's a player's entity
        realm.updateMacroData(spawnPoint);

        //
        //error checking
        Realm searchedRealm = Globals.serverState.realmManager.getEntityRealm(newPlayerEntity);
        if(searchedRealm == null){
            LoggerInterface.loggerEngine.ERROR(new IllegalArgumentException("Player entity created but not attached to a realm!"));
        }

        return newPlayerEntity;
    }

    /**
     * Attachs an entity to a player object
     * @param entity The entity
     * @param playerObject The player object
     * @param serverConnectionHandler The connection handler
     */
    public static void attachEntityToPlayerObject(Entity entity, Player playerObject, ServerConnectionHandler serverConnectionHandler){
        int playerEntityId = entity.getId();
        serverConnectionHandler.setPlayerEntityId(playerEntityId);
        CreatureUtils.setControllerPlayerId(entity, serverConnectionHandler.getPlayerId());
        Player player = serverConnectionHandler.getPlayer();
        player.setPlayerEntity(entity);
        //custom player btrees
        PlayerCharacterCreation.addPlayerServerBTrees(entity, serverConnectionHandler);
    }

    /**
     * Adds behavior trees that are unique to players a given entity
     * @param entity The entity to add to
     * @param serverConnectionHandler The server connection handler for the entity
     */
    static void addPlayerServerBTrees(Entity entity, ServerConnectionHandler serverConnectionHandler){
        ServerPlayerViewDirTree.attachServerPlayerViewDirTree(entity);
        ServerCharacterData.attachServerCharacterData(entity, Globals.serverState.characterService.getCharacter(serverConnectionHandler.getCharacterId()));
        Globals.serverState.lodEmitterService.registerLODEmitter(entity);
    }

    /**
     * Solves the spawn point for the player
     * @param realm The realm that the entity is being spawned in
     * @param connectionHandler The connection object
     * @return The spawn point for the player
     */
    public static Vector3d solveSpawnPoint(Realm realm, ServerConnectionHandler connectionHandler){
        Vector3d spawnPoint = Globals.serverState.characterService.getCharacter(connectionHandler.getCharacterId()).getPos();
        if(spawnPoint == null){
            spawnPoint = realm.getSpawnPoint();
        }
        return spawnPoint;
    }

}
