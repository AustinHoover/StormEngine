package electrosphere.entity.types.creature;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import electrosphere.server.entity.serialization.EntitySerialization;

/**
 * Inventory data for a object template
 */
public class ObjectInventoryData {

    /**
     * Toolbar items
     */
    Map<String,EntitySerialization> toolbarItemMap = new HashMap<String,EntitySerialization>();

    /**
     * Equipped items
     */
    Map<String,EntitySerialization> equipItemMap = new HashMap<String,EntitySerialization>();

    /**
     * Natural inventory items
     */
    List<EntitySerialization> naturalItems = new LinkedList<EntitySerialization>();

    /**
     * Maps toolbar slot -> id of the entity that was serialized
     */
    Map<String,Integer> toolbarIdMap = new HashMap<String,Integer>();

    /**
     * Maps equipped slot -> id of the entity that was serialized
     */
    Map<String,Integer> equippedIdMap = new HashMap<String,Integer>();

    /**
     * Maps natural slot -> id of the entity that was serialized
     */
    Map<Integer,Integer> naturalIdMap = new HashMap<Integer,Integer>();

    /**
     * Adds an item to the toolbar
     * @param slot The slot to add it at
     * @param itemSerialization The item serialization
     */
    public void addToolbarItem(String slot, EntitySerialization itemSerialization){
        if(toolbarItemMap.containsKey(slot)){
            throw new Error("Item slot already occupied " + slot);
        }
        toolbarItemMap.put(slot, itemSerialization);
    }

    /**
     * Adds an item to the equipped item set
     * @param slot The slot to add it at
     * @param itemSerialization The item serialization
     */
    public void addEquippedItem(String slot, EntitySerialization itemSerialization){
        if(equipItemMap.containsKey(slot)){
            throw new Error("Item slot already occupied " + slot);
        }
        equipItemMap.put(slot, itemSerialization);
    }

    /**
     * Adds an item to the natural inventory
     * @param itemSerialization The item serialization
     */
    public void addNaturalItem(EntitySerialization itemSerialization){
        naturalItems.add(itemSerialization);
    }

    /**
     * Gets the set of entries of toolbar items
     * @return The set
     */
    public Set<Entry<String,EntitySerialization>> getToolbarItems(){
        return toolbarItemMap.entrySet();
    }

    /**
     * Gets the set of entries of equipped items
     * @return The set
     */
    public Set<Entry<String,EntitySerialization>> getEquipItems(){
        return equipItemMap.entrySet();
    }

    /**
     * Gets the list of natural items
     * @return The list of natural items
     */
    public List<EntitySerialization> getNaturalItems(){
        return naturalItems;
    }

    /**
     * Sets the id of a toolbar entity
     * @param slot The slot of the entity on the toolbar
     * @param id The id
     */
    public void setToolbarId(String slot, int id){
        toolbarIdMap.put(slot, id);
    }

    /**
     * Gets the id of a toolbar entity
     * @param slot The slot in the toolbar
     * @return The id
     */
    public int getToolbarId(String slot){
        return toolbarIdMap.get(slot);
    }

    /**
     * Sets the id of an equipped entity
     * @param slot The slot of the equipped entity
     * @param id The id
     */
    public void setEquippedId(String slot, int id){
        equippedIdMap.put(slot, id);
    }

    /**
     * Gets the id of an euqipped entity
     * @param slot The slot equipped to
     * @return The id
     */
    public int getEquippedId(String slot){
        return equippedIdMap.get(slot);
    }

    /**
     * Sets the id of a natural inventory entity
     * @param slot The slot of the natural item
     * @param id The id
     */
    public void setNaturalId(int slot, int id){
        naturalIdMap.put(slot, id);
    }

    /**
     * Gets the id of a natural inventory item
     * @param slot The slot of the natural item
     * @return The id
     */
    public int getNaturalId(int slot){
        return naturalIdMap.get(slot);
    }

    /**
     * Gets all items in the inventory data
     * @return The list of all items
     */
    public List<EntitySerialization> getAllItems(){
        List<EntitySerialization> rVal = new LinkedList<EntitySerialization>();
        rVal.addAll(this.naturalItems);
        rVal.addAll(this.equipItemMap.values());
        rVal.addAll(this.toolbarItemMap.values());
        return rVal;
    }

    /**
     * Clears the serialization
     */
    public void clear(){
        this.naturalIdMap.clear();
        this.equippedIdMap.clear();
        this.toolbarIdMap.clear();
        this.naturalItems.clear();
        this.equipItemMap.clear();
        this.toolbarItemMap.clear();
    }

}

