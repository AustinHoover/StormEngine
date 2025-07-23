package electrosphere.data.tutorial;

/**
 * A hint that can be shown as a popup in game
 */
public class TutorialHint {
    
    //the id of the hint
    String id;

    //the title
    String titleString;
    
    //the description paired with the title
    String descriptionString;

    //the image to display
    String image;

    /**
     * Gets the id of the hint
     * @return the hint's id
     */
    public String getId(){
        return id;
    }

    /**
     * Gets the string for the title of the hint
     * @return The title
     */
    public String getTitleString(){
        return titleString;
    }

    /**
     * Gets the string for the description of the hint
     * @return The description
     */
    public String getDescriptionString(){
        return descriptionString;
    }

    /**
     * Gets the string for the path to the image associated with the hint
     * @return The image path
     */
    public String getImage(){
        return image;
    }

}
