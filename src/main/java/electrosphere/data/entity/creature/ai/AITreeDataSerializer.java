package electrosphere.data.entity.creature.ai;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import electrosphere.logger.LoggerInterface;
import electrosphere.server.ai.trees.test.BlockerAITree;
import electrosphere.server.ai.trees.character.StandardCharacterTree;
import electrosphere.server.ai.trees.creature.AttackerAITree;
import electrosphere.server.ai.trees.hierarchy.MaslowTree;

/**
 * Deserializes ai tree data types
 */
public class AITreeDataSerializer implements JsonDeserializer<AITreeData> {

    @Override
    public AITreeData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        switch(json.getAsJsonObject().get("name").getAsString()){
            case AttackerAITree.TREE_NAME: {
                return context.deserialize(json, AttackerTreeData.class);
            }
            case BlockerAITree.TREE_NAME: {
                return context.deserialize(json, BlockerTreeData.class);
            }
            case MaslowTree.TREE_NAME: {
                return context.deserialize(json, MaslowTreeData.class);
            }
            case StandardCharacterTree.TREE_NAME: {
                return context.deserialize(json, StandardCharacterTreeData.class);
            }
        }
        
        LoggerInterface.loggerEngine.ERROR(new IllegalArgumentException("JSON Object provided to AITreeDataSerializer that cannot deserialize into a tree data type cleanly " + json.getAsJsonObject().get("name").getAsString()));
        return null;
    }
    
}
