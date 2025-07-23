package electrosphere.data.entity.creature;

import electrosphere.data.entity.common.treedata.TreeDataAnimation;

/**
 * Data about how the creature will behave when in idle state
 */
public class IdleData {
    
    /**
     * The animation that plays when the creature is idle
     */
    TreeDataAnimation animation;

    /**
     * The animation that plays when the creature is idle
     * @return The animation
     */
    public TreeDataAnimation getAnimation(){
        return animation;
    }

}
