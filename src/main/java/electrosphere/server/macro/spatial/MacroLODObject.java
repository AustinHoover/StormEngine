package electrosphere.server.macro.spatial;

/**
 * A macro object that can be simulated at different resolutions
 */
public interface MacroLODObject {
    
    /**
     * Checks if this macro object is full resolution or not
     * @return true if it is full resolution, false otherwise
     */
    public boolean isFullRes();

}
