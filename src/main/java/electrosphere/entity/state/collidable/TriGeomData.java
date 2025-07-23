package electrosphere.entity.state.collidable;

/**
 * A data object that can be used to generate a collidable with arbitrary geometry
 */
public interface TriGeomData {
    
    /**
     * Gets the vertex data
     * @return the vertex data
     */
    public float[] getVertices();

    /**
     * Gets the face element data
     * @return the face element data
     */
    public int[] getFaceElements();

}
