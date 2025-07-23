package electrosphere.data.entity.creature.block;

import java.util.List;

import electrosphere.data.entity.common.treedata.TreeDataAnimation;
import electrosphere.data.entity.common.treedata.TreeDataAudio;

/**
 * A variant of data that can be loaded into the block system. Variants are for different types of equip states.
 * IE: holding just a sword in your right hand will have a different block animation vs a shield in your right
 * hand vs two handing a sword with your right hand.
 */
public class BlockVariant {
    
    //The id of the block variant
    String variantId;

    //the animation to play when winding up
    TreeDataAnimation windUpAnimation;

    //the audio to play when winding up
    TreeDataAudio windUpAudio;

    //the main animation to play while blocking
    TreeDataAnimation mainAnimation;

    //the main audio to play while blocking
    TreeDataAudio mainAudio;

    //the animation to play when cooling down
    TreeDataAnimation cooldownAnimation;

    //the audio to play while cooling down
    TreeDataAudio cooldownAudio;

    //the list of default equipment cases that this variant should be used for
    List<VariantDefaults> defaults;

    /**
     * The id of the block variant
     * @return
     */
    public String getVariantId(){
        return variantId;
    }

    /**
     * The animation to play when winding up
     * @return
     */
    public TreeDataAnimation getWindUpAnimation(){
        return windUpAnimation;
    }

    /**
     * Gets the audio to play when winding up
     * @return The audio
     */
    public TreeDataAudio getWindUpAudio(){
        return windUpAudio;
    }

    /**
     * The main animation to play while blocking
     * @return
     */
    public TreeDataAnimation getMainAnimation(){
        return mainAnimation;
    }

    /**
     * Gets the audio to play when blocking
     * @return The audio
     */
    public TreeDataAudio getMainAudio(){
        return mainAudio;
    }

    /**
     * The animation to play when cooling down
     * @return
     */
    public TreeDataAnimation getCooldownAnimation(){
        return cooldownAnimation;
    }

    /**
     * Gets the audio to play when cooling down
     * @return The audio
     */
    public TreeDataAudio getCooldownAudio(){
        return cooldownAudio;
    }

    /**
     * the list of default equipment cases that this variant should be used for
     * @return
     */
    public List<VariantDefaults> getDefaults(){
        return defaults;
    }

}
