package electrosphere.data.entity.graphics;

/**
 * A graphics template for an entity
 */
public class GraphicsTemplate {

    /**
     * The procedural model definition
     */
    ProceduralModel proceduralModel;

    /**
     * The non-procedural model definition
     */
    NonproceduralModel model;

    /**
     * Gets the procedural model
     * @return The procedural model
     */
    public ProceduralModel getProceduralModel() {
        return proceduralModel;
    }

    /**
     * Gets the procedural model
     * @param proceduralModel The procedural model
     */
    public void setProceduralModel(ProceduralModel proceduralModel) {
        this.proceduralModel = proceduralModel;
    }

    /**
     * Gets the non-procedural model
     * @return The non-procedural model
     */
    public NonproceduralModel getModel() {
        return model;
    }

    /**
     * Sets the non-procedural model
     * @param model The non-procedural model
     */
    public void setModel(NonproceduralModel model) {
        this.model = model;
    }

    

}
