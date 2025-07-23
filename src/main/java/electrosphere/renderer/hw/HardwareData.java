package electrosphere.renderer.hw;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;

/**
 * Data about the renderer hardware
 */
public class HardwareData {

    /**
     * The list of monitors available
     */
    public final List<MonitorData> monitors = new LinkedList<MonitorData>();
    
    /**
     * Constructor
     */
    public HardwareData(){
        PointerBuffer monitorBuff = GLFW.glfwGetMonitors();
        if(monitorBuff != null){
            while(monitorBuff.hasRemaining()){
                long id = monitorBuff.get();
                String name = GLFW.glfwGetMonitorName(id);
                monitors.add(new MonitorData(id, name));
            }
        }
    }

}
