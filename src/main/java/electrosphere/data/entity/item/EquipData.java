package electrosphere.data.entity.item;

import java.util.List;

/**
 * Data about how this item is equipped (ie, where is it equipped, what slots does it take, who can equip it, etc)
 */
public class EquipData {
    
    //the equip whitelist for this item (what creatures can equip this item?)
    List<EquipWhitelist> equipWhitelist;

    //the class of item
    String equipClass;

    //The list of slots that this item takes up when it is equipped
    List<String> equipSlots;

    /**
     * the equip whitelist for this item (what creatures can equip this item?)
     * @return
     */
    public List<EquipWhitelist> getEquipWhitelist(){
        return equipWhitelist;
    }

    /**
     * the class of item
     * @return
     */
    public String getEquipClass(){
        return equipClass;
    }

    /**
     * Gets the list of equip slot ids that are taken up when equipping this item
     * @return The list of equip slot ids
     */
    public List<String> getEquipSlots(){
        return equipSlots;
    }

}
