package electrosphere.data.entity.creature.block;

import java.util.List;

import electrosphere.logger.LoggerInterface;

/**
 * Stores data related to an entity blocking attacks
 */
public class BlockSystem {

    //blocking with a weapon equipped in the right hand
    //NOTE: the names provided here should line up with the actual field names on this object
    public static final String BLOCK_VARIANT_WEAPON_RIGHT = "blockWeaponRight";
    public static final String BLOCK_VARIANT_WEAPON_2H = "block2H";

    //the list of block variants
    List<BlockVariant> variants;

    /**
     * Gets the list of block variants
     * @return the list
     */
    public List<BlockVariant> getAllVariants(){
        return variants;
    }

    /**
     * Gets a block variant from its variant string
     * @param variantString The variant string
     * @return The block variant if it exists, null otherwise
     */
    public BlockVariant getBlockVariant(String variantString){
        for(BlockVariant variant : variants){
            if(variant.variantId.equals(variantString)){
                return variant;
            }
        }
        return null;
    }

    /**
     * Gets the block variant that is default for the provided equip point when the provided item class is equipped to that point
     * @param equipPoint The equip point
     * @param itemClass The item class
     * @return The block variant if it exists, null otherwise
     */
    public BlockVariant getVariantForPointWithItem(String equipPoint, String itemClass){
        for(BlockVariant variant : variants){
            for(VariantDefaults variantDefault : variant.getDefaults()){
                if(variantDefault.itemClassEquipped.equalsIgnoreCase(itemClass) && !variantDefault.itemClassEquipped.equals(itemClass)){
                    String message = "Block variant passed over because the item class for the block variant does not match the item's defined item class\n" +
                    "However, the difference is only in capitalization! Block-variant defined class:" + variantDefault.itemClassEquipped + " Item-defined class:" + itemClass;
                    ;
                    LoggerInterface.loggerEngine.WARNING(message);
                }
                if(variantDefault.equipPoint.equals(equipPoint) && variantDefault.itemClassEquipped.equals(itemClass)){
                    return variant;
                }
            }
        }
        return null;
    }

}
