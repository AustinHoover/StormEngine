package electrosphere.util.annotation;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 * Used to exclude single fields from a gson serialization in a black list manner.
 * Refer to https://stackoverflow.com/a/27986860/ for reference for why this works and what it is.
 */
public class AnnotationExclusionStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return f.getAnnotation(Exclude.class) != null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
    
}
