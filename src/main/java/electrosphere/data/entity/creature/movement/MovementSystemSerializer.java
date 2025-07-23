package electrosphere.data.entity.creature.movement;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class MovementSystemSerializer implements JsonDeserializer<MovementSystem> {

    @Override
    public MovementSystem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        switch(json.getAsJsonObject().get("type").getAsString()){
            case GroundMovementSystem.GROUND_MOVEMENT_SYSTEM: {
                return context.deserialize(json, GroundMovementSystem.class);
            }
            case JumpMovementSystem.JUMP_MOVEMENT_SYSTEM: {
                return context.deserialize(json, JumpMovementSystem.class);
            }
            case FallMovementSystem.FALL_MOVEMENT_SYSTEM: {
                return context.deserialize(json, FallMovementSystem.class);
            }
            case AirplaneMovementSystem.AIRPLANE_MOVEMENT_SYSTEM: {
                return context.deserialize(json, AirplaneMovementSystem.class);
            }
            case WalkMovementSystem.WALK_MOVEMENT_SYSTEM: {
                return context.deserialize(json, WalkMovementSystem.class);
            }
            case EditorMovementSystem.EDITOR_MOVEMENT_SYSTEM: {
                return context.deserialize(json, EditorMovementSystem.class);
            }
        }
        return null;
    }
    
}
