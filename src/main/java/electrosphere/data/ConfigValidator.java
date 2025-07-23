package electrosphere.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import electrosphere.data.crafting.RecipeValidator;
import electrosphere.data.entity.common.CommonEntityType;
import electrosphere.data.entity.common.CommonEntityValidator;
import electrosphere.data.entity.creature.CreatureData;
import electrosphere.data.entity.creature.CreatureDataValidator;
import electrosphere.data.entity.creature.CreatureTypeLoader;
import electrosphere.server.macro.character.race.RaceValidator;

/**
 * Used to validate the config
 */
public class ConfigValidator {
    
    /**
     * Validates a config
     * @param config The config
     */
    public static void valdiate(Config config){
        //validate the creatures
        CreatureTypeLoader creatureTypeLoader = config.getCreatureTypeLoader();
        for(CreatureData creatureData : creatureTypeLoader.getTypes()){
            CreatureDataValidator.validate(creatureData);
        }

        //validate common entity data
        List<CommonEntityType> allData = new LinkedList<CommonEntityType>();
        allData.addAll(config.getCreatureTypeLoader().getTypeIds().stream().map((String id) -> {return config.getCreatureTypeLoader().getType(id);}).collect(Collectors.toList()));
        allData.addAll(config.getFoliageMap().getTypeIds().stream().map((String id) -> {return config.getFoliageMap().getType(id);}).collect(Collectors.toList()));
        allData.addAll(config.getItemMap().getTypeIds().stream().map((String id) -> {return config.getItemMap().getType(id);}).collect(Collectors.toList()));
        allData.addAll(config.getObjectTypeMap().getTypeIds().stream().map((String id) -> {return config.getObjectTypeMap().getType(id);}).collect(Collectors.toList()));
        for(CommonEntityType type : allData){
            CommonEntityValidator.validate(config, type);
        }

        //validate recipes
        RecipeValidator.validate(config);

        //validate races
        RaceValidator.validate(config);

        ConfigValidator.checkIdCollisions(config);
    }

    /**
     * Checks if there are any id collisions
     * @param config The config
     */
    private static void checkIdCollisions(Config config){
        //check for id collisions
        Map<String,Boolean> occupancyMap = new HashMap<String,Boolean>();
        for(CommonEntityType type : config.getObjectTypeMap().getTypes()){
            if(occupancyMap.containsKey(type.getId())){
                throw new Error("Entity id collision: " + type.getId());
            } else {
                occupancyMap.put(type.getId(),true);
            }
        }
        for(CommonEntityType type : config.getCreatureTypeLoader().getTypes()){
            if(occupancyMap.containsKey(type.getId())){
                throw new Error("Entity id collision: " + type.getId());
            } else {
                occupancyMap.put(type.getId(),true);
            }
        }
        for(CommonEntityType type : config.getFoliageMap().getTypes()){
            if(occupancyMap.containsKey(type.getId())){
                throw new Error("Entity id collision: " + type.getId());
            } else {
                occupancyMap.put(type.getId(),true);
            }
        }
        for(CommonEntityType type : config.getItemMap().getTypes()){
            if(occupancyMap.containsKey(type.getId())){
                throw new Error("Entity id collision: " + type.getId());
            } else {
                occupancyMap.put(type.getId(),true);
            }
        }
    }

}
