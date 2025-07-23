package electrosphere.util.noise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maps objects to a frequency, then allows normalized noise values to index into the frequency range
 */
public class NoiseMapper<T> {
    
    /**
     * The objects in the buckets
     */
    List<T> objects;

    /**
     * The frequencies
     */
    float[] frequencies;

    public NoiseMapper(List<T> objects){
        Map<NoiseMapperElement,T> typeMap = new HashMap<NoiseMapperElement,T>();
        //convert types
        List<NoiseMapperElement> elements = objects.stream().map(object -> {
            if(object instanceof NoiseMapperElement){
                NoiseMapperElement casted = (NoiseMapperElement)object;
                typeMap.put(casted,object);
                return casted;
            } else {
                throw new Error("Supplied a class that does not extend NoiseMapper! " + object.getClass());
            }
        }).collect(Collectors.toList());
        //set frequencies
        float frequencySum = 0;
        for(NoiseMapperElement el : elements){
            frequencySum = frequencySum + el.getFrequency();
        }
        this.frequencies = new float[elements.size()];
        this.objects = new ArrayList<T>();
        int i = 0;
        float accumulator = 0;
        for(NoiseMapperElement el : elements){
            this.frequencies[i] = el.getFrequency() / frequencySum + accumulator;
            this.objects.add(typeMap.get(el));
            accumulator = accumulator + this.frequencies[i];
            i++;
        }
    }

    /**
     * Looks up a value's corresponding object
     * @param value The value
     * @return The object
     */
    public T lookup(float value){
        if(value < 0){
            throw new Error("Supplied value less than 0! " + value);
        } else if(value > 1){
            throw new Error("Supplied value greater than 1! " + value);
        }
        int searchIndex = 0;
        float prevFreq = 0;
        while(searchIndex < frequencies.length){
            if(value >= prevFreq && value <= frequencies[searchIndex]){
                return objects.get(searchIndex);
            }
            prevFreq = frequencies[searchIndex];
            searchIndex++;
        }
        throw new Error("Failed to mape value " + value + " into object! " + frequencies + " " + objects);
    }

}
