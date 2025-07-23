package electrosphere.entity.state.inventory;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;

public class InventoryUtils {
    

    /**
     * Checks if the entity has a natural inventory
     * @param target The entity
     * @return The inventory if it exists, null otherwise
     */
    public static boolean hasNaturalInventory(Entity target){
        return target.containsKey(EntityDataStrings.NATURAL_INVENTORY);
    }

    /**
     * Sets the natural inventory of the entity
     * @param target The entity
     * @param inventoryState The inventory
     */
    public static void setNaturalInventory(Entity target, UnrelationalInventoryState inventoryState){
        target.putData(EntityDataStrings.NATURAL_INVENTORY, inventoryState);
    }

    /**
     * Gets the natural inventory of the entity
     * @param target The entity
     * @return The inventory if it exists, null otherwise
     */
    public static UnrelationalInventoryState getNaturalInventory(Entity target){
        if(!target.containsKey(EntityDataStrings.NATURAL_INVENTORY)){
            return null;
        }
        return (UnrelationalInventoryState)target.getData(EntityDataStrings.NATURAL_INVENTORY);
    }

    /**
     * Checks if the entity has an equip inventory
     * @param target The entity
     * @return true if it has an equip inventory, false if it does not
     */
    public static boolean hasEquipInventory(Entity target){
        return target.containsKey(EntityDataStrings.EQUIP_INVENTORY);
    }

    /**
     * Sets the equipment inventory of the entity
     * @param target The target
     * @param inventoryState The inventory
     */
    public static void setEquipInventory(Entity target, RelationalInventoryState inventoryState){
        target.putData(EntityDataStrings.EQUIP_INVENTORY, inventoryState);
    }

    /**
     * Gets the equipment inventory of the entity
     * @param target The entity
     * @return The inventory if it exists, null otherwise
     */
    public static RelationalInventoryState getEquipInventory(Entity target){
        if(!target.containsKey(EntityDataStrings.EQUIP_INVENTORY)){
            return null;
        }
        return (RelationalInventoryState)target.getData(EntityDataStrings.EQUIP_INVENTORY);
    }

    /**
     * Checks if the entity has a toolbar inventory
     * @param target The entity
     * @return true if it has a toolbar inventory, false if it does not
     */
    public static boolean hasToolbarInventory(Entity target){
        return target.containsKey(EntityDataStrings.INVENTORY_TOOLBAR);
    }

    /**
     * Sets the toolbar inventory of the entity
     * @param target The target
     * @param inventoryState The inventory
     */
    public static void setToolbarInventory(Entity target, RelationalInventoryState inventoryState){
        target.putData(EntityDataStrings.INVENTORY_TOOLBAR, inventoryState);
    }

    /**
     * Gets the toolbar inventory of the entity
     * @param target The entity
     * @return The inventory if it exists, null otherwise
     */
    public static RelationalInventoryState getToolbarInventory(Entity target){
        if(!target.containsKey(EntityDataStrings.INVENTORY_TOOLBAR)){
            return null;
        }
        return (RelationalInventoryState)target.getData(EntityDataStrings.INVENTORY_TOOLBAR);
    }

    /**
     * Gets the list of all items in all inventories of a creature
     * @param creature The creature
     * @return The list of all items
     */
    public static List<Entity> getAllInventoryItems(Entity creature){
        List<Entity> rVal = new LinkedList<Entity>();
        if(InventoryUtils.hasEquipInventory(creature)){
            RelationalInventoryState equipInventory = InventoryUtils.getEquipInventory(creature);
            rVal.addAll(equipInventory.getItems());
        }
        if(InventoryUtils.hasNaturalInventory(creature)){
            UnrelationalInventoryState naturalInventory = InventoryUtils.getNaturalInventory(creature);
            rVal.addAll(naturalInventory.getItems());
        }
        if(InventoryUtils.hasToolbarInventory(creature)){
            RelationalInventoryState toolbarInventory = InventoryUtils.getToolbarInventory(creature);
            rVal.addAll(toolbarInventory.getItems());
        }
        //filter null items
        rVal = rVal.stream().filter((Entity el) -> {return el != null;}).collect(Collectors.toList());
        return rVal;
    }

}
