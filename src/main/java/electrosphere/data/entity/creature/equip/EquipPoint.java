package electrosphere.data.entity.creature.equip;

import java.util.List;

import electrosphere.data.entity.common.treedata.TreeDataAnimation;

/**
 * A portion of the creature that can have an item attached to it
 */
public class EquipPoint {

    //the id of the equip point
    String equipPointId;

    //the bone that can have the item attached to it (may not be defined depending on the type of equip point (think legs))
    String bone;

    //the bone to attach items to for the first person viewmodel (may not be defined based on the viewmodel)
    String firstPersonBone;

    //the offset to apply to items that are attached to the bone
    List<Float> offsetVectorThirdPerson;

    //the offset to apply to items that are attached to the bone
    List<Float> offsetVectorFirstPerson;

    //the rotation to apply to the items that are attached to the bone
    List<Float> offsetRotationThirdPerson;

    //the rotation to apply to the items that are attached to the view model's bone
    List<Float> offsetRotationFirstPerson;

    //signals that this equip point can block
    boolean canBlock;

    //the equip classes that are whitelisted for this equip point
    List<String> equipClassWhitelist;

    //The animation to play when this equip point has an item equipped (ie if a hand is grasping something)
    TreeDataAnimation equippedAnimation;








    /**
     * 
     * 
     *     COMBINED EQUIP POINTS
     * 
     *      The idea of a combined equip point is that it is a combination of multiple single equip points.
     *      Think of equipping an item in both the left and right hands of a character at once (ie a two handed sword).
     * 
     */


    /**
     * Indicates whether this is a conbined equip point or not
     */
    boolean isCombinedPoint;

    /**
     * The list of points that are contained within a combined equip point
     */
    List<String> subPoints;

    /**
     * Controls whether this is a toolbar slot or an equipment slot
     */
    boolean isToolbarSlot;












    /**
     * Gets the equip point id
     * @return the id of the equip point
     */
    public String getEquipPointId(){
        return equipPointId;
    }

    /**
     * Sets the id of the equip point
     * @param id The id
     */
    public void setEquipPointId(String id){
        this.equipPointId = id;
    }

    /**
     * Gets the bone that can have the item attached to it (may not be defined depending on the type of equip point (think legs))
     * @return the bone
     */
    public String getBone(){
        return bone;
    }

    /**
     * Gets the bone to attach items to for the first person viewmodel (may not be defined based on the viewmodel)
     * @return the bone
     */
    public String getFirstPersonBone(){
        return firstPersonBone;
    }

    /**
     * Gets the offset to apply to items that are attached to the bone
     * @return the offset
     */
    public List<Float> getOffsetVectorThirdPerson(){
        return offsetVectorThirdPerson;
    }

    /**
     * Sets the third person offset vector values
     * @param offsetVector The offset vector
     */
    public void setOffsetVectorThirdPerson(List<Float> offsetVector){
        this.offsetVectorThirdPerson = offsetVector;
    }

    /**
     * Gets the offset to apply to items that are attached to the bone
     * @return the offset
     */
    public List<Float> getOffsetVectorFirstPerson(){
        return offsetVectorFirstPerson;
    }

    /**
     * Sets the first person offset vector values
     * @param offsetVector The offset vector
     */
    public void setOffsetVectorFirstPerson(List<Float> offsetVector){
        this.offsetVectorFirstPerson = offsetVector;
    }

    /**
     * [Third Person]
     * Gets the rotation to apply to the items that are attached to the third person model's bone
     * @return the rotation
     */
    public List<Float> getOffsetRotationThirdPerson(){
        return offsetRotationThirdPerson;
    }

    /**
     * Sets the offset rotation (used primarily for debug and engine testing)
     * @param offsetRotation The new offset rotation
     */
    public void setOffsetRotationThirdPerson(List<Float> offsetRotation){
        this.offsetRotationThirdPerson = offsetRotation;
    }

    /**
     * [First Person]
     * Gets the rotation to apply to the items that are attached to the view model's bone
     * @return the rotation
     */
    public List<Float> getOffsetRotationFirstPerson(){
        return offsetRotationFirstPerson;
    }

    /**
     * Sets the offset rotation (used primarily for debug and engine testing)
     * @param offsetRotation The new offset rotation
     */
    public void setOffsetRotationFirstPerson(List<Float> offsetRotation){
        this.offsetRotationFirstPerson = offsetRotation;
    }

    /**
     * Signals that this equip point can block
     * @return true if can block, false otherwise
     */
    public boolean getCanBlock(){
        return canBlock;
    }

    /**
     * Gets the equip classes that are whitelisted for this equip point
     * @return the classes
     */
    public List<String> getEquipClassWhitelist(){
        return equipClassWhitelist;
    }

    /**
     * Gets the animation to play when this point has an item equipped
     * @return The animation if it exists, null otherwise
     */
    public TreeDataAnimation getEquippedAnimation(){
        return equippedAnimation;
    }

    /**
     * Checks whether this is a combined equip point or not
     * @return true if this is a combined equip point, false otherwise
     */
    public boolean isCombinedPoint(){
        return this.isCombinedPoint;
    }

    /**
     * Gets the list of sub point ids that are contained within a combined point
     * @return The list of sub point ids
     */
    public List<String> getSubPoints(){
        return this.subPoints;
    }

    /**
     * Gets whether this is a toolbar slot or not
     * @return true for toolbar slot, false otherwise
     */
    public boolean isToolbarSlot(){
        return this.isToolbarSlot;
    }
    
}
