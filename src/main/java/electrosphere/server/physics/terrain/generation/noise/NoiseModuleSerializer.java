package electrosphere.server.physics.terrain.generation.noise;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import electrosphere.server.physics.terrain.generation.noise.operators.NoiseOperatorAdd;
import electrosphere.server.physics.terrain.generation.noise.operators.NoiseOperatorClamp;
import electrosphere.server.physics.terrain.generation.noise.operators.NoiseOperatorConst;
import electrosphere.server.physics.terrain.generation.noise.operators.NoiseOperatorDomainWarp;
import electrosphere.server.physics.terrain.generation.noise.operators.NoiseOperatorMul;
import electrosphere.server.physics.terrain.generation.noise.operators.NoiseOperatorOpenSimplex;
import electrosphere.server.physics.terrain.generation.noise.operators.NoiseOperatorVoronoi;
import electrosphere.server.physics.terrain.generation.noise.operators.NoiseOperoatorInvoke;

/**
 * Deserializes noise modules
 */
public class NoiseModuleSerializer implements JsonDeserializer<NoiseSampler> {
    
    @Override
    public NoiseSampler deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        switch(json.getAsJsonObject().get("name").getAsString()){

            //meta
            case NoiseOperoatorInvoke.NAME: {
                return context.deserialize(json, NoiseOperoatorInvoke.class);
            }

            //warps
            case NoiseOperatorDomainWarp.NAME: {
                return context.deserialize(json, NoiseOperatorDomainWarp.class);
            }

            //actual noise sampling
            case NoiseOperatorOpenSimplex.NAME: {
                return context.deserialize(json, NoiseOperatorOpenSimplex.class);
            }
            case NoiseOperatorVoronoi.NAME: {
                return context.deserialize(json, NoiseOperatorVoronoi.class);
            }

            //basic ops
            case NoiseOperatorConst.NAME: {
                return context.deserialize(json, NoiseOperatorConst.class);
            }
            case NoiseOperatorAdd.NAME: {
                return context.deserialize(json, NoiseOperatorAdd.class);
            }
            case NoiseOperatorMul.NAME: {
                return context.deserialize(json, NoiseOperatorMul.class);
            }
            case NoiseOperatorClamp.NAME: {
                return context.deserialize(json, NoiseOperatorClamp.class);
            }
        }
        return null;
    }

}
