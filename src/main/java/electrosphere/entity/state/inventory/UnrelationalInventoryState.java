package electrosphere.entity.state.inventory;

import java.util.LinkedList;
import java.util.List;

import electrosphere.entity.Entity;

public class UnrelationalInventoryState {
    
    static int inventoryIncrementer = 0;

    int capacity;
    int id;

    List<Entity> items = new LinkedList<Entity>();

    public static UnrelationalInventoryState createUnrelationalInventory(int capacity){
        return new UnrelationalInventoryState(inventoryIncrementer++,capacity);
    }

    UnrelationalInventoryState(int id, int capacity){
        this.id = id;
        this.capacity = capacity;
    }

    // public UnrelationalInventoryState(int capacity){
    //     this.capacity = capacity;
    // }

    public void addItem(Entity item){
        if(items.size() == capacity){
            throw new Error("Trying to add more items than inventory can hold!");
        }
        items.add(item);
    }

    /**
     * Removes an item from the inventory
     * @param item the item to attempt to remove
     * @return true if an item was removed, false otherwise
     */
    public boolean removeItem(Entity item){
        return items.remove(item);
    }

    public List<Entity> getItems(){
        return items;
    }

    public int getCapacity(){
        return capacity;
    }

    public int getId(){
        return id;
    }


}
