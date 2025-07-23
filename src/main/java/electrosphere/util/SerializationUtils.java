package electrosphere.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import electrosphere.data.entity.creature.ai.AITreeData;
import electrosphere.data.entity.creature.ai.AITreeDataSerializer;
import electrosphere.data.entity.creature.movement.MovementSystem;
import electrosphere.data.entity.creature.movement.MovementSystemSerializer;
import electrosphere.server.macro.character.data.CharacterData;
import electrosphere.server.macro.character.data.CharacterDataSerializer;
import electrosphere.server.physics.terrain.generation.noise.NoiseModuleSerializer;
import electrosphere.server.physics.terrain.generation.noise.NoiseSampler;
import electrosphere.util.annotation.AnnotationExclusionStrategy;

/**
 * Utilities for serializing/deserializing entities
 */
public class SerializationUtils {
    
    /**
     * Creates the gson instance
     */
    static {
        //init gson
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(MovementSystem.class, new MovementSystemSerializer());
        gsonBuilder.registerTypeAdapter(AITreeData.class, new AITreeDataSerializer());
        gsonBuilder.registerTypeAdapter(NoiseSampler.class, new NoiseModuleSerializer());
        gsonBuilder.registerTypeAdapter(CharacterData.class, new CharacterDataSerializer());
        gsonBuilder.addDeserializationExclusionStrategy(new AnnotationExclusionStrategy());
        gsonBuilder.addSerializationExclusionStrategy(new AnnotationExclusionStrategy());
        gson = gsonBuilder.create();
    }
    
    /**
     * used for serialization/deserialization in file operations
     */
    static Gson gson;

    /**
     * Serializes an object
     * @param o The object
     * @return The serialization string
     */
    public static String serialize(Object o){
        return gson.toJson(o);
    }

    /**
     * Deserializes an object from a JSON string
     * @param <T> The type of object
     * @param jsonString The JSON string to deserialize the object from
     * @param className The class of the object inside the json string
     * @return The Object
     */
    public static <T>T deserialize(String jsonString, Class<T> className){
        return gson.fromJson(jsonString, className);
    }

}
