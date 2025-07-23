package electrosphere.server.simulation.chara;

import java.util.List;
import java.util.stream.Collectors;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.types.creature.ObjectInventoryData;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.server.entity.serialization.EntitySerialization;
import electrosphere.server.macro.character.Character;

/**
 * Checks if a character has an inventory item
 */
public class CharaInventoryUtils {
    
    /**
     * Gets all items in the character's inventories
     * @param chara The character
     * @return The list of all items in the character's inventories
     */
    public static List<EntitySerialization> getInventoryContents(Character chara){
        ObjectTemplate template = chara.getCreatureTemplate();
        if(CharaMacroUtils.isMicroSim(chara)){
            Entity creature = Globals.serverState.characterService.getEntity(chara);
            template = CommonEntityUtils.getObjectTemplate(creature);
        }
        ObjectInventoryData inventoryData = template.getInventoryData();
        return inventoryData.getAllItems();
    }

    /**
     * Gets the list of all inventory contents by their item ids
     * @param chara The character
     * @return The list of all item ids
     */
    public static List<String> getInventoryContentIds(Character chara){
        return CharaInventoryUtils.getInventoryContents(chara).stream().map((EntitySerialization serialization) -> {
            return serialization.getSubtype();
        }).collect(Collectors.toList());
    }

    /**
     * Checks if the character contains a given type of item
     * @param chara The character
     * @param itemId The item id
     * @return true if the character contains that item type, false otherwise
     */
    public static boolean containsItem(Character chara, String itemId){
        return CharaInventoryUtils.getInventoryContentIds(chara).contains(itemId);
    }

}
