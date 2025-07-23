package electrosphere.data.entity.creature.bonegroups;

import java.util.List;

/**
 * Groups of bones that can be used for functions (ie priority on animations, hitbox data macros, etc)
 */
public class BoneGroup {
    
    /**
     * The id for the bone group
     */
    String id;

    /**
     * The list of names of bones that are within this group for the third person model
     */
    List<String> boneNamesThirdPerson;

    /**
     * The list of names of bones that are within this group for the first person model
     */
    List<String> boneNamesFirstPerson;

    /**
     * Gets the id of the bone group
     * @return The bone group id
     */
    public String getId(){
        return id;
    }

    /**
     * Gets the list of names of bones in the group on the third person model
     * @return The list of names of bones
     */
    public List<String> getBoneNamesThirdPerson(){
        return boneNamesThirdPerson;
    }

    /**
     * Gets the list of names of bones in the group on the first person model
     * @return The list of names of bones
     */
    public List<String> getBoneNamesFirstPerson(){
        return boneNamesFirstPerson;
    }

}
