package electrosphere.data.entity.creature.block;

/**
 * Equip point cases that this variant is used as the default for
 * IE, if you create a variant default for "handRight, weapon",
 * that means this should be used as the default variant for when
 * the handRight equip point has a weapon equipped and the block
 * tree is triggered
 */
public class VariantDefaults {
    
    //the equip point
    String equipPoint;

    //the class of item equipped to that equip point
    String itemClassEquipped;

    /**
     * the equip point
     * @return
     */
    public String getEquipPoint(){
        return equipPoint;
    }

    /**
     * the class of item equipped to that equip point
     * @return
     */
    public String getItemClassEquipped(){
        return itemClassEquipped;
    }

}
