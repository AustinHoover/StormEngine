package electrosphere.client.ui.menu.debug.engine;

import electrosphere.engine.Globals;
import electrosphere.logger.Logger;
import electrosphere.logger.LoggerInterface;
import electrosphere.logger.Logger.LogLevel;
import electrosphere.renderer.ui.imgui.ImGuiWindow;
import electrosphere.renderer.ui.imgui.ImGuiWindow.ImGuiWindowCallback;
import imgui.ImGui;

/**
 * Windows for dealing with loggers
 */
public class ImGuiLogger {
    
    //window for viewing information about loggers
    public static ImGuiWindow loggersWindow;

    /**
     * loggers view
     */
    public static void createLoggersWindow(){
        loggersWindow = new ImGuiWindow("Loggers");
        loggersWindow.setCallback(new ImGuiWindowCallback() {
            @Override
            public void exec() {
                //ui framework text
                ImGui.text("Loggers");


                ImGui.beginTable("Loggers", 6);
                for(Logger logger : LoggerInterface.getLoggers()){
                    ImGui.tableNextRow();

                    ImGui.tableNextColumn();
                    ImGui.text(logger.getName() + ": ");

                    ImGui.tableNextColumn();
                    if(logger.getLevel() == LogLevel.ERROR){
                        ImGui.text("Error");
                    } else {
                        ImGui.pushID(logger.getName() + "error");
                        if(ImGui.button("Error")){
                            logger.setLevel(LogLevel.ERROR);
                        }
                        ImGui.popID();
                    }
                    
                    ImGui.tableNextColumn();
                    if(logger.getLevel() == LogLevel.WARNING){
                        ImGui.text("Warning");
                    } else {
                        ImGui.pushID(logger.getName() + "warning");
                        if(ImGui.button("Warning")){
                            logger.setLevel(LogLevel.WARNING);
                        }
                        ImGui.popID();
                    }
                    
                    ImGui.tableNextColumn();
                    if(logger.getLevel() == LogLevel.INFO){
                        ImGui.text("Info");
                    } else {
                        ImGui.pushID(logger.getName() + "info");
                        if(ImGui.button("Info")){
                            logger.setLevel(LogLevel.INFO);
                        }
                        ImGui.popID();
                    }
                    
                    ImGui.tableNextColumn();
                    if(logger.getLevel() == LogLevel.DEBUG){
                        ImGui.text("Debug");
                    } else {
                        ImGui.pushID(logger.getName() + "debug");
                        if(ImGui.button("Debug")){
                            logger.setLevel(LogLevel.DEBUG);
                        }
                        ImGui.popID();
                    }
                    
                    ImGui.tableNextColumn();
                    if(logger.getLevel() == LogLevel.LOOP_DEBUG){
                        ImGui.text("Debug_Loop");
                    } else {
                        ImGui.pushID(logger.getName() + "debug_loop");
                        if(ImGui.button("Debug_Loop")){
                            logger.setLevel(LogLevel.LOOP_DEBUG);
                        }
                        ImGui.popID();
                    }

                }

                ImGui.endTable();

            }
        });
        loggersWindow.setOpen(false);
        Globals.renderingEngine.getImGuiPipeline().addImGuiWindow(loggersWindow);
    }

}
