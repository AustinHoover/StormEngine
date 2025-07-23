package electrosphere.renderer.hw;

/**
 * Data about a monitor
 */
public class MonitorData {
    
    /**
     * The window id of the monitor
     */
    public final long windowId;

    /**
     * The name of the monitor
     */
    public final String name;

    /**
     * Constructor
     * @param windowId The window id of the monitor
     * @param name The name of the monitor
     */
    public MonitorData(long windowId, String name){
        this.windowId = windowId;
        this.name = name;
    }

}
