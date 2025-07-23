package electrosphere.entity.state.collidable;

import java.util.Collection;

/**
 * A tri geom data set that contains multiple shapes
 */
public interface MultiShapeTriGeomData {
    
    /**
     * Gets the data for the tri geom shapes
     * @return The data
     */
    public Collection<TriGeomData> getData();

}
