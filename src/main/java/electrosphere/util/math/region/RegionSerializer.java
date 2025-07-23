package electrosphere.util.math.region;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class RegionSerializer implements JsonSerializer<Region>, JsonDeserializer<Region> {

    @Override
    public Region deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        switch(json.getAsJsonObject().get("type").getAsString()){
            case RegionRectangular.TYPE_STRING: {
                return context.deserialize(json, RegionRectangular.class);
            }
            case RegionPrism.TYPE_STRING: {
                return context.deserialize(json, RegionPrism.class);
            }
            default: {
                throw new Error("Failed to serialize datatype: " + json.getAsJsonObject().get("type").getAsString());
            }
        }
    }

    @Override
    public JsonElement serialize(Region src, Type typeOfSrc, JsonSerializationContext context) {
        switch(src.getType()){

            //race
            case RegionPrism.TYPE_STRING: {
                return context.serialize((RegionPrism)src);
            }

            //diety data
            case RegionRectangular.TYPE_STRING: {
                return context.serialize((RegionRectangular)src);
            }

            default: {
                throw new Error("Failed to serialize datatype: " + src.getType());
            }
        }
    }
    
}
