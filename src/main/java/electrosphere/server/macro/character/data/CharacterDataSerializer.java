package electrosphere.server.macro.character.data;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import electrosphere.server.macro.character.diety.Diety;
import electrosphere.server.macro.character.goal.CharacterGoal;
import electrosphere.server.macro.character.race.Race;
import electrosphere.server.macro.civilization.town.Town;

/**
 * Deserializes noise modules
 */
public class CharacterDataSerializer implements JsonDeserializer<CharacterData>, JsonSerializer<CharacterData> {
    
    @Override
    public CharacterData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String type = json.getAsJsonObject().get("dataType").getAsString();
        switch(type){

            //race
            case CharacterDataStrings.RACE: {
                return context.deserialize(json, Race.class);
            }

            //diety data
            case CharacterDataStrings.DIETY: {
                return context.deserialize(json, Diety.class);
            }

            //a structure
            case CharacterDataStrings.STRUCTURE_ID:
            case CharacterDataStrings.HOMETOWN:
            case CharacterDataStrings.SHELTER: {
                return context.deserialize(json, CharacterAssociatedId.class);
            }

            //goal
            case CharacterDataStrings.ENTITY_GOAL: {
                return context.deserialize(json, CharacterGoal.class);
            }

            //a town
            case CharacterDataStrings.TOWN: {
                return context.deserialize(json, Town.class);
            }

            default: {
                throw new Error("Failed to deserialize datatype: " + type);
            }
        }
    }

    @Override
    public JsonElement serialize(CharacterData src, Type typeOfSrc, JsonSerializationContext context) {
        switch(src.getDataType()){

            //race
            case CharacterDataStrings.RACE: {
                return context.serialize((Race)src);
            }

            //diety data
            case CharacterDataStrings.DIETY: {
                return context.serialize((Diety)src);
            }

            //a structure
            case CharacterDataStrings.STRUCTURE_ID:
            case CharacterDataStrings.HOMETOWN:
            case CharacterDataStrings.SHELTER: {
                return context.serialize((CharacterAssociatedId)src);
            }

            //goal
            case CharacterDataStrings.ENTITY_GOAL: {
                return context.serialize((CharacterGoal)src);
            }

            default: {
                throw new Error("Failed to serialize datatype: " + src.getDataType());
            }
        }
    }

}
