package electrosphere.data.entity.creature.movement;

/**
 * Data about the editor movement system
 */
public class EditorMovementSystem implements MovementSystem {
    
    //move system type string
    public static final String EDITOR_MOVEMENT_SYSTEM = "EDITOR";
    
    //type of move system
    String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    
    
}
