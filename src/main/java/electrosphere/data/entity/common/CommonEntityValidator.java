package electrosphere.data.entity.common;

import electrosphere.data.Config;
import electrosphere.data.entity.common.life.loot.LootPool;
import electrosphere.data.entity.common.life.loot.LootTicket;
import electrosphere.logger.LoggerInterface;

/**
 * Validates common entity definition data
 */
public class CommonEntityValidator {
    
    /**
     * Validates a common entity
     * @param config The config
     * @param data The data
     */
    public static void validate(Config config, CommonEntityType data){
        if(data.getId() == null || data.getId().length() == 0){
            String message = "Id undefined for entity type!";
            LoggerInterface.loggerEngine.WARNING(message);
        }

        if(data.getDisplayName() == null || data.getDisplayName().length() == 0){
            String message = "Display name undefined for entity type " + data.getId();
            LoggerInterface.loggerEngine.WARNING(message);
        }

        //validate loot pool
        if(data.getHealthSystem() != null && data.getHealthSystem().getLootPool() != null){
            CommonEntityValidator.validateLootPool(config, data.getHealthSystem().getLootPool());
        }
    }

    /**
     * Validates a loot pool
     * @param config The config
     * @param data The loot pool
     */
    private static void validateLootPool(Config config, LootPool data){
        for(LootTicket ticket : data.getTickets()){
            if(config.getItemMap().getItem(ticket.getItemId()) == null){
                throw new Error("Loot pool has undefined item: " + ticket.getItemId());
            }
        }
    }

}
