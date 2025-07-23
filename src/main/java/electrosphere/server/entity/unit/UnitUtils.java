package electrosphere.server.entity.unit;

import org.joml.Vector3d;

import electrosphere.data.macro.units.UnitDefinition;
import electrosphere.data.macro.units.UnitEquippedItem;
import electrosphere.data.macro.units.UnitLoader;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.state.inventory.ServerInventoryState;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.server.protocol.InventoryProtocol;
import electrosphere.server.datacell.Realm;

/**
 * Utilities for dealing with units
 */
public class UnitUtils {
    
    /**
     * Spawns a unit
     * @param realm The realm to spawn in
     * @param position The position to spawn at
     * @param type The type of unit
     * @return The entity encompassing the unit
     */
    public static Entity spawnUnit(Realm realm, Vector3d position, String type){
        UnitLoader unitLoader = Globals.gameConfigCurrent.getUnitLoader();
        UnitDefinition unitDefinition = unitLoader.getUnit(type);
        if(unitDefinition == null){
            LoggerInterface.loggerEngine.ERROR(new IllegalArgumentException("Tried to spawn undefined unit type! " + type));
            return null;
        }
        String creatureId = unitDefinition.getCreatureId();
        if(creatureId == null || creatureId.equals("")){
            LoggerInterface.loggerEngine.ERROR(new IllegalArgumentException("Tried to spawn unit with invalid creatureId! \"" + creatureId + "\""));
            return null;
        }
        Entity rVal = CreatureUtils.serverSpawnBasicCreature(realm, position, creatureId, ObjectTemplate.createDefault(EntityType.CREATURE, creatureId));
        
        //optionally apply ai
        if(unitDefinition.getAI() != null){
            Globals.serverState.aiManager.removeAI(rVal);
            Globals.serverState.aiManager.attachAI(rVal, unitDefinition.getAI());
        }

        //optionally add equipment
        if(unitDefinition.getEquipment() != null){
            for(UnitEquippedItem equippedItem : unitDefinition.getEquipment()){
                if(equippedItem.getItemId() == null || equippedItem.getItemId().equals("")){
                    LoggerInterface.loggerEngine.ERROR(new IllegalStateException("Equipped item id is invalid! \"" + equippedItem.getItemId() + "\""));
                }
                if(equippedItem.getPointId() == null || equippedItem.getPointId().equals("")){
                    LoggerInterface.loggerEngine.ERROR(new IllegalStateException("Equipped point id is invalid! \"" + equippedItem.getPointId() + "\""));
                }
                //spawn the item in the world
                Entity itemInWorld = ItemUtils.serverSpawnBasicItem(realm, position, equippedItem.getItemId());

                //add the item to the creature's inventory
                ServerInventoryState.attemptStoreItemTransform(rVal, itemInWorld, InventoryProtocol.INVENTORY_TYPE_EQUIP, equippedItem.getPointId());
            }
        }

        return rVal;
    }

}
