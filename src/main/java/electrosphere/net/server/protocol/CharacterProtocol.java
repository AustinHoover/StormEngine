package electrosphere.net.server.protocol;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.joml.Vector3d;

import com.google.gson.Gson;

import electrosphere.client.entity.character.CharacterDescriptionDTO;
import electrosphere.client.entity.character.ClientCharacterListDTO;
import electrosphere.data.entity.creature.CreatureData;
import electrosphere.data.entity.creature.visualattribute.VisualAttribute;
import electrosphere.engine.Globals;
import electrosphere.engine.loadingthreads.LoadingUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.net.parser.net.message.CharacterMessage;
import electrosphere.net.parser.net.message.PlayerMessage;
import electrosphere.net.server.ServerConnectionHandler;
import electrosphere.net.server.player.Player;
import electrosphere.net.template.ServerProtocolTemplate;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.entity.serialization.ContentSerialization;
import electrosphere.server.macro.character.Character;
import electrosphere.server.macro.character.PlayerCharacterCreation;
import electrosphere.util.Utilities;

/**
 * The server protocol for handling character messages
 */
public class CharacterProtocol implements ServerProtocolTemplate<CharacterMessage> {

    /**
     * Should spawn the existing template
     */
    public static final int SPAWN_EXISTING_TEMPLATE = -1;

    @Override
    public CharacterMessage handleAsyncMessage(ServerConnectionHandler connectionHandler, CharacterMessage message) {
        switch(message.getMessageSubtype()){
            case REQUESTCHARACTERLIST: {
                Gson gson = new Gson();
                List<CharacterDescriptionDTO>characters = Globals.serverState.characterService.getCharacters(connectionHandler.getPlayer().getDBID()).stream().map((Character chara) -> {
                    CharacterDescriptionDTO dtoObj = new CharacterDescriptionDTO();
                    dtoObj.setId(chara.getId() + "");
                    dtoObj.setTemplate(chara.getCreatureTemplate());
                    return dtoObj;
                }).collect(Collectors.toList());
                ClientCharacterListDTO dto = new ClientCharacterListDTO();
                dto.setCharacters(characters);
                connectionHandler.addMessagetoOutgoingQueue(CharacterMessage.constructResponseCharacterListMessage(gson.toJson(dto)));
                return null;
            }
            default: {
            } break;
        }
        return message;
    }

    @Override
    public void handleSyncMessage(ServerConnectionHandler connectionHandler, CharacterMessage message) {
        switch(message.getMessageSubtype()){
            case REQUESTCREATECHARACTER: {
                ObjectTemplate template = Utilities.deserialize(message.getdata(), ObjectTemplate.class);
                if(template != null){
                    Character charaData = Globals.serverState.characterService.createCharacter(template, connectionHandler.getPlayer().getDBID(),Globals.serverState.realmManager.first().getSpawnPoint());
                    connectionHandler.setCreatureTemplate(Utilities.deserialize(message.getdata(), ObjectTemplate.class));
                    connectionHandler.setCharacterId(charaData.getId());
                    connectionHandler.addMessagetoOutgoingQueue(CharacterMessage.constructResponseCreateCharacterSuccessMessage());
                } else {
                    connectionHandler.addMessagetoOutgoingQueue(CharacterMessage.constructResponseCreateCharacterFailureMessage());
                }
            } break;
            case REQUESTSPAWNCHARACTER: {
                int charaId = Integer.parseInt(message.getdata());
                CharacterProtocol.spawnEntityForClient(connectionHandler, charaId);
            } break;
            case EDITORSWAP: {
                CharacterProtocol.swapPlayerCharacter(connectionHandler);
            } break;
            case REQUESTCHARACTERLIST:
                //handled async
                break;
            case RESPONSECHARACTERLIST:
            case RESPONSECREATECHARACTERSUCCESS:
            case RESPONSECREATECHARACTERFAILURE:
            case RESPONSESPAWNCHARACTER:
                //silently ignore
                break;
        }
    }

    /**
     * Spawns the player's entity
     * @param connectionHandler The connection handler for the player
     * @param id The id in the db to lookup the template from
     * @return THe player's entity
     */
    static Entity spawnEntityForClient(ServerConnectionHandler connectionHandler, int id){
        if(id != SPAWN_EXISTING_TEMPLATE){
            connectionHandler.setCharacterId(id);
        }
        Entity rVal = PlayerCharacterCreation.spawnPlayerCharacter(connectionHandler);
        Realm realm = Globals.serverState.playerManager.getPlayerRealm(connectionHandler.getPlayer());
        Vector3d spawnPoint = PlayerCharacterCreation.solveSpawnPoint(realm, connectionHandler);
        //set client initial discrete position
        connectionHandler.addMessagetoOutgoingQueue(
            PlayerMessage.constructSetInitialDiscretePositionMessage(
                ServerWorldData.convertRealToChunkSpace(spawnPoint.x),
                ServerWorldData.convertRealToChunkSpace(spawnPoint.y),
                ServerWorldData.convertRealToChunkSpace(spawnPoint.z)
            )
        );
        return rVal;
    }

    /**
     * Swaps the type of the player character (to editor or vice-versa)
     * @param connectionHandler The connection handler to swap
     * @return The new player entity
     */
    static Entity swapPlayerCharacter(ServerConnectionHandler connectionHandler){
        //change the connection handler's creature template
        if(connectionHandler.getCurrentCreatureTemplate() != null && connectionHandler.getCurrentCreatureTemplate().getObjectType().matches(LoadingUtils.EDITOR_RACE_NAME)){
            //solve what race to pick
            String race = LoadingUtils.EDITOR_RACE_NAME;
            List<String> races = Globals.gameConfigCurrent.getCreatureTypeLoader().getPlayableRaces();
            while(race.matches(LoadingUtils.EDITOR_RACE_NAME)){
                race = races.get(new Random().nextInt(races.size()));
            }
            //create template
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
            connectionHandler.setCreatureTemplate(template);
        } else {
            String race = LoadingUtils.EDITOR_RACE_NAME;
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
            connectionHandler.setCreatureTemplate(template);
        }

        //destroy the old entity
        Player player = connectionHandler.getPlayer();
        Entity playerEntity = player.getPlayerEntity();
        Vector3d position = EntityUtils.getPosition(playerEntity);
        Globals.serverState.realmManager.getEntityRealm(playerEntity).registerSpawnPoint(position);
        Globals.serverState.realmManager.getEntityRealm(playerEntity).getSpawnPoint().set(position);
        ServerEntityUtils.destroyEntity(playerEntity);

        //spawn the new one
        player.setHasSentPlayerEntity(false);
        Entity newEntity = CharacterProtocol.spawnEntityForClient(connectionHandler, CharacterProtocol.SPAWN_EXISTING_TEMPLATE);
        ServerEntityUtils.repositionEntity(newEntity, position);
        return newEntity;
    }

}
