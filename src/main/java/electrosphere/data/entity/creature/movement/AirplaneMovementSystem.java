package electrosphere.data.entity.creature.movement;

public class AirplaneMovementSystem implements MovementSystem {

    public static final String AIRPLANE_MOVEMENT_SYSTEM = "AIRPLANE";
    
    float acceleration;
    float maxVelocity;
    float minVelocity;
    float maxRotationSpeed;

    String type;

    public float getAcceleration() {
        return acceleration;
    }

    public float getMaxVelocity() {
        return maxVelocity;
    }

    public float getMinVelocity(){
        return minVelocity;
    }

    public float getMaxRotationSpeed(){
        return maxRotationSpeed;
    }

    @Override
    public String getType() {
        return type;
    }
    
}
