package electrosphere.entity.state.inventory;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import electrosphere.data.entity.creature.equip.EquipPoint;
import electrosphere.entity.Entity;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.logger.LoggerInterface;

public class RelationalInventoryState {

    /**
     * The size of the toolbar
     */
    static final int TOOLBAR_SIZE = 10;
    
    Map<String,Entity> items = new HashMap<String,Entity>();
    Map<String,List<String>> slotWhitelists = new HashMap<String,List<String>>();
    Map<String,EquipPoint> equipPoints = new HashMap<String,EquipPoint>();

    // public static RelationalInventoryState buildRelationalInventoryStateFromStringList(List<String> slots){
    //     RelationalInventoryState rVal = new RelationalInventoryState();
    //     for(String slot : slots){
    //         rVal.items.put(slot,null);
    //     }
    //     return rVal;
    // }

    /**
     * Builds a relational inventory state based on a list of equip points
     * @param points The equip points
     * @return The relational inventory state
     */
    public static RelationalInventoryState buildRelationalInventoryStateFromEquipList(List<EquipPoint> points){
        RelationalInventoryState rVal = new RelationalInventoryState();
        for(EquipPoint point : points){
            rVal.items.put(point.getEquipPointId(),null);
            rVal.slotWhitelists.put(point.getEquipPointId(),point.getEquipClassWhitelist());
            rVal.equipPoints.put(point.getEquipPointId(),point);
        }
        return rVal;
    }

    /**
     * Builds a relational inventory state based on a list of equip points
     * @param points The equip points
     * @return The relational inventory state
     */
    public static RelationalInventoryState buildToolbarInventory(){
        RelationalInventoryState rVal = new RelationalInventoryState();
        for(int i = 0; i < TOOLBAR_SIZE; i ++){
            rVal.items.put("" + i,null);
        }
        return rVal;
    }

    /**
     * Adds an item to the relational inventory state
     * @param slot The slot to add to
     * @param item The item
     */
    public void addItem(String slot, Entity item){
        items.put(slot,item);
    }

    /**
     * Removes the item from a slot in the inventory
     * @param slot The slot
     * @return The item that was removed, or null if no item was removed
     */
    public Entity removeItemSlot(String slot){
        Entity rVal = items.remove(slot);
        items.put(slot,null);
        return rVal;
    }

    /**
     * Gets the item in a given relational inventory state
     * @param slot The slot
     * @return The item if it exists, null otherwise
     */
    public Entity getItemSlot(String slot){
        return items.get(slot);
    }

    /**
     * Gets the collection of items in the inventory
     * @return The entities in the inventory
     */
    public Collection<Entity> getItems(){
        return items.values();
    }

    /**
     * Gets the item slot for a given item
     * @param item The item
     * @return The item slot if it is contained within this inventory, null otherwise
     */
    public String getItemSlot(Entity item){
        if(item == null){
            LoggerInterface.loggerEngine.ERROR(new IllegalArgumentException("Trying to get the item slot of null!"));
        }
        if(items.containsValue(item)){
            for(String slot : items.keySet()){
                if(items.get(slot) == item){
                    return slot;
                }
            }
        }
        return null;
    }

    /**
     * Checks if the relational inventory has an item in a slot
     * @param slot The slot
     * @return true if there is an item in that slot, false otherwise
     */
    public boolean hasItemInSlot(String slot){
        //if the slot is a key return if the value at the key isn't null, otherwise return false
        return items.containsKey(slot) ? items.get(slot) != null : false;
    }

    /**
     * Gets the slots in the relational inventory state
     * @return The list of slots
     */
    public List<String> getSlots(){
        return new LinkedList<String>(items.keySet());
    }

    /**
     * Tries removing an item from the inventory
     * @param item The item
     * @return The item if it was removed, null if it was not
     */
    public Entity tryRemoveItem(Entity item){
        if(items.containsValue(item)){
            for(String slot : items.keySet()){
                if(items.get(slot) == item){
                    items.remove(slot);
                    items.put(slot,null);
                    return item;
                }
            }
        }
        return null;
    }

    /**
     * Checks if the inventory can equip the item to the given slot
     * @param item The item
     * @param slot The slot
     * @return true if can equip, false otherwise
     */
    public boolean canEquipItemToSlot(Entity item, String slot){
        String itemClass = ItemUtils.getEquipClass(item);
        if(slotWhitelists.get(slot).contains(itemClass)){
            return true;
        }
        return false;
    }

    /**
     * Gets an equip point from a given slot name
     * @param slot The name of the slot
     * @return The equip point
     */
    public EquipPoint getEquipPointFromSlot(String slot){
        return equipPoints.get(slot);
    }

    /**
     * Gets whether the item can equipped to a combined slot that contains the singular slot provided
     * @param item The item
     * @param slot The singular (non-combined) slot
     * @return true if there is a combined slot that includes slot which can equip the item, false otherwise
     */
    public boolean canEquipItemToCombinedSlot(Entity item, String slot){
        String itemClass = ItemUtils.getEquipClass(item);
        EquipPoint singlePoint = getEquipPointFromSlot(slot);
        EquipPoint combinedPoint = null;
        for(String currentPointNames : this.equipPoints.keySet()){
            EquipPoint currentPoint = this.equipPoints.get(currentPointNames);
            if(currentPoint.isCombinedPoint() && currentPoint.getSubPoints().contains(singlePoint.getEquipPointId())){
                combinedPoint = currentPoint;
                break;
            }
        }
        if(combinedPoint != null){
            for(String equipClassWhitelisted : combinedPoint.getEquipClassWhitelist()){
                if(equipClassWhitelisted.equalsIgnoreCase(itemClass) && !equipClassWhitelisted.equals(itemClass)){
                    String message = "Combined equip point passed over because the item class for the equip point does not match the item's defined item class\n" +
                    "However, the difference is only in capitalization! Equip point defined class:" + equipClassWhitelisted + " Item-defined class:" + itemClass;
                    ;
                    LoggerInterface.loggerEngine.WARNING(message);
                }
            }
            if(combinedPoint.getEquipClassWhitelist().contains(itemClass)){
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the combined point that contains this point
     * @param slot The singular (non-combined) slot
     * @return The combined point if it exists, null otherwise
     */
    public EquipPoint getCombinedPoint(String slot){
        EquipPoint singlePoint = getEquipPointFromSlot(slot);
        for(String currentPointNames : this.equipPoints.keySet()){
            EquipPoint currentPoint = this.equipPoints.get(currentPointNames);
            if(currentPoint.isCombinedPoint() && currentPoint.getSubPoints().contains(singlePoint.getEquipPointId())){
                return currentPoint;
            }
        }
        return null;
    }

    /**
     * Checks if this inventory contains this item
     * @param item The item
     * @return true if it is inside this inventory, false otherwise
     */
    public boolean containsItem(Entity item){
        return this.items.values().contains(item);
    }

}
