package electrosphere.server.ai.nodes.actions.inventory;

import electrosphere.entity.Entity;
import electrosphere.entity.state.equip.ServerToolbarState;
import electrosphere.entity.state.inventory.InventoryUtils;
import electrosphere.entity.state.inventory.RelationalInventoryState;
import electrosphere.entity.state.inventory.UnrelationalInventoryState;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Tries to equip a type of item to the toolbar and select that item in the toolbar
 */
public class EquipToolbarNode implements AITreeNode {

    /**
     * Key to lookup the item type under
     */
    String itemTypeKey;

    /**
     * The block type key
     */
    String blockTypeKey;

    /**
     * Constructor
     */
    private EquipToolbarNode(){
    }

    /**
     * Creates a EquipToolbarNode that tries to equip a type of item
     * @param blockTypeKey Key that stores the item type id
     * @return The EquipToolbarNode
     */
    public static EquipToolbarNode equipItem(String itemTypeKey){
        EquipToolbarNode rVal = new EquipToolbarNode();
        rVal.itemTypeKey = itemTypeKey;
        return rVal;
    }

    /**
     * Creates a EquipToolbarNode that tries to equip a block
     * @param blockTypeKey Key that stores the block type (NOT THE ENTITY TYPE)
     * @return The EquipToolbarNode
     */
    public static EquipToolbarNode equipBlock(String blockTypeKey){
        EquipToolbarNode rVal = new EquipToolbarNode();
        rVal.blockTypeKey = blockTypeKey;
        return rVal;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        String targetItemType = null;

        if(this.itemTypeKey != null){
            targetItemType = (String)blackboard.get(this.itemTypeKey);
        } else if(this.blockTypeKey != null){
            targetItemType = (String)blackboard.get(this.blockTypeKey);
        } else {
            throw new Error("No keys defined!");
        }

        //check if that item type is already equipped
        ServerToolbarState serverToolbarState = ServerToolbarState.getServerToolbarState(entity);
        if(serverToolbarState.getRealWorldItem() != null){
            Entity realWorldItem = serverToolbarState.getRealWorldItem();
            String type = CommonEntityUtils.getEntitySubtype(realWorldItem);
            if(type.equals(targetItemType)){
                return AITreeNodeResult.SUCCESS;
            }
        }

        //check if this item type is already in toolbar and we can just swap to it
        if(InventoryUtils.hasToolbarInventory(entity)){
            RelationalInventoryState toolbarInventory = InventoryUtils.getToolbarInventory(entity);
            for(Entity itemEnt : toolbarInventory.getItems()){
                if(itemEnt == null){
                    continue;
                }
                String type = CommonEntityUtils.getEntitySubtype(itemEnt);
                if(type.equals(targetItemType)){
                    String slotId = toolbarInventory.getItemSlot(itemEnt);
                    serverToolbarState.attemptChangeSelection(Integer.parseInt(slotId));
                    return AITreeNodeResult.SUCCESS;
                }
            }
        }

        //make sure we have a free toolbar slot
        int freeToolbarSlot = 0;
        if(InventoryUtils.hasToolbarInventory(entity)){
            RelationalInventoryState toolbarInventory = InventoryUtils.getToolbarInventory(entity);
            int i = 0;
            for(Entity itemEnt : toolbarInventory.getItems()){
                if(itemEnt == null){
                    freeToolbarSlot = i;
                    break;
                }
                i++;
            }
        }

        //check if this item type is in the natural inventory, if it is, try to equip it
        if(InventoryUtils.hasNaturalInventory(entity)){
            UnrelationalInventoryState naturalInventory = InventoryUtils.getNaturalInventory(entity);
            //find matching natural item
            Entity naturalItem = null;
            for(Entity itemEnt : naturalInventory.getItems()){
                String type = CommonEntityUtils.getEntitySubtype(itemEnt);
                if(type.equals(targetItemType)){
                    naturalItem = itemEnt;
                    break;
                }
            }
            if(naturalItem != null){
                serverToolbarState.attemptEquip(naturalItem, freeToolbarSlot);
                return AITreeNodeResult.SUCCESS;
            }
        }

        return AITreeNodeResult.FAILURE;
    }

}
