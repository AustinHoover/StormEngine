package electrosphere.data.entity.common.treedata;

import java.util.List;

/**
 * Data about an animation
 */
public class TreeDataAnimation {
    
    /**
     * The name of the animation to play in third person if it exists, null otherwise
     */
    String nameThirdPerson;

    /**
     * The name of the animation to play in first person if it exists, null otherwise
     */
    String nameFirstPerson;

    /**
     * The length of the animation in frames if it exists, null otherwise
     */
    int length;

    /**
     * If boolean, should loop if it exists, null otherwise
     */
    boolean loops;

    /**
     * The priority for this animation in particular
     */
    Integer priority;

    /**
     * The priority category this animation is in
     */
    String priorityCategory;

    /**
     * The list of bone groups this animation applies to
     */
    List<String> boneGroups;

    /**
     * Gets the name of the animation to play in third person
     * @return The name of the animation to play in third person
     */
    public String getNameThirdPerson() {
        return nameThirdPerson;
    }

    /**
     * Gets the name of the animation to play in first person
     * @return The name of the animation to play in first person
     */
    public String getNameFirstPerson() {
        return nameFirstPerson;
    }

    /**
     * Gets the length of the animation in frames
     * @return The length in number of frames
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets whether the animation loops or not
     * @return true if loops, false otherwise
     */
    public boolean isLoops() {
        return loops;
    }

    /**
     * Gets the priority for this animation
     * @return The priority
     */
    public Integer getPriority(){
        return priority;
    }

    /**
     * Gets the priority category of this animation
     * @return The priority category
     */
    public String getPriorityCategory(){
        return priorityCategory;
    }

    /**
     * Gets the bone groups this animation applies to
     * @return The list of bone groups this animation applies to
     */
    public List<String> getBoneGroups(){
        return boneGroups;
    }
    
}
