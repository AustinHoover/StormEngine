package electrosphere.client.ui.menu.debug.perf;

import electrosphere.engine.Globals;
import electrosphere.renderer.ui.imgui.ImGuiLinePlot;
import electrosphere.renderer.ui.imgui.ImGuiLinePlot.ImGuiLinePlotDataset;
import electrosphere.renderer.ui.imgui.ImGuiWindow;
import electrosphere.renderer.ui.imgui.ImGuiWindow.ImGuiWindowCallback;

public class ImGuiNetworkMonitor {
    
    /**
     * Num datapoints
     */
    public static final int PRESSURE_GRAPH_POINT_COUNT = 100;

    /**
     * Window for viewing chunk status on server and client
     */
    public static ImGuiWindow netMonitorWindow;

    /**
     * Client scene entity view
     */
    public static void createNetworkMonitorWindow(){
        netMonitorWindow = new ImGuiWindow("Network Monitor");

        //client network pressure graph
        ImGuiLinePlot clientNetworkBandwith = new ImGuiLinePlot("Client Network Pressure",400,400);
        ImGuiLinePlotDataset clientPressureDataset = new ImGuiLinePlotDataset("Client bytes per frame", PRESSURE_GRAPH_POINT_COUNT);
        clientPressureDataset.zeroOut();
        clientNetworkBandwith.addDataset(clientPressureDataset);

        //server network pressure graph
        ImGuiLinePlot serverNetworkPressureGraph = new ImGuiLinePlot("Server Network Pressure",400,400);
        ImGuiLinePlotDataset serverPressureDataset = new ImGuiLinePlotDataset("Server bytes per frame", PRESSURE_GRAPH_POINT_COUNT);
        serverPressureDataset.zeroOut();
        serverNetworkPressureGraph.addDataset(serverPressureDataset);

        netMonitorWindow.setCallback(new ImGuiWindowCallback() {
            long clientPressureLastValue = 0;
            long serverPressureLastValue = 0;

            @Override
            public void exec() {


                //client network pressure
                if(Globals.clientState.clientConnection != null){
                    long clientPressureNewTotal = Globals.clientState.clientConnection.getNumBytesRead();
                    long clientPressureDelta = clientPressureNewTotal - clientPressureLastValue;
                    clientPressureDataset.addPoint(clientPressureDelta);
                    clientPressureLastValue = clientPressureNewTotal;
                }
                clientNetworkBandwith.draw();

                //server network pressure
                if(Globals.serverState.server != null && Globals.serverState.server.getFirstConnection() != null){
                    long serverPressureNewTotal = Globals.serverState.server.getFirstConnection().getNumBytesRead();
                    long serverPressureDelta = serverPressureNewTotal - serverPressureLastValue;
                    serverPressureDataset.addPoint(serverPressureDelta);
                    serverPressureLastValue = serverPressureNewTotal;
                }
                serverNetworkPressureGraph.draw();
                
            }
        });
        netMonitorWindow.setOpen(false);
        Globals.renderingEngine.getImGuiPipeline().addImGuiWindow(netMonitorWindow);
    }

}
