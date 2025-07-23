package electrosphere.client.entity.character;

import electrosphere.entity.types.creature.ObjectTemplate;

/**
 * Describes a character
 */
public class CharacterDescriptionDTO {
    
    /**
     * The id of the character
     */
    String id;

    /**
     * The character's template data
     */
    ObjectTemplate template;

    /**
     * Gets the id of the character
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the character
     * @param id The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the creature template for the character
     * @return The creature template
     */
    public ObjectTemplate getTemplate() {
        return template;
    }

    /**
     * Sets the creature template for the character
     * @param template The creature template
     */
    public void setTemplate(ObjectTemplate template) {
        this.template = template;
    }
    

}
