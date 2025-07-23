package electrosphere.renderer.ui.imgui;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import imgui.ImVec2;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.flag.ImPlotAxisFlags;

/**
 * A bar plot
 */
public class ImGuiBarPlot implements ImGuiElement {
    
    //the title of the plot
    String plotTitle;

    //the data sets to draw
    List<ImGuiBarPlotDatapoint> dataPoints = new LinkedList<ImGuiBarPlotDatapoint>();

    float width = 0.5f;

    /**
     * Creates an im gui line plot
     */
    public ImGuiBarPlot(String plotTitle){
        this.plotTitle = plotTitle;
    }

    @Override
    public void draw() {
        if(ImPlot.beginPlot(plotTitle,"","",new ImVec2(-1,-1),0,ImPlotAxisFlags.AutoFit,ImPlotAxisFlags.AutoFit)){
            double[] xs = IntStream.range(0, dataPoints.size()).mapToDouble(v -> (double)v).toArray();
            double[] ys = dataPoints.stream().map(v -> v.value).mapToDouble(Double::doubleValue).toArray();
            ImPlot.plotBars(plotTitle, xs, ys, dataPoints.size(), width, 0);
            int i = 0;
            for(ImGuiBarPlotDatapoint point : dataPoints){
                //coordinates are inside the plot itself, not relative to window
                ImPlot.plotText(point.label, i, 1, true);
                i++;
            }
            ImPlot.endPlot();
        }
    }

    /**
     * Adds a datapoint to the bar plot
     * @param datapoint The datapoint
     */
    public void addDatapoint(ImGuiBarPlotDatapoint datapoint){
        this.dataPoints.add(datapoint);
    }

    /**
     * Adds a value amount to the current amount in a given datapoint. If the datapoint is null, it is created with value as its initial amount.
     * @param datapointName The datapoint's name
     * @param value The value to add to the datapoint
     */
    public void addToDatapoint(String datapointName, double value){
        ImGuiBarPlotDatapoint targetDatapoint = null;
        for(ImGuiBarPlotDatapoint point : dataPoints){
            if(point.label.equals(datapointName)){
                targetDatapoint = point;
                break;
            }
        }
        if(targetDatapoint!=null){
            targetDatapoint.value = targetDatapoint.value + value;
        } else {
            dataPoints.add(new ImGuiBarPlotDatapoint(datapointName, value));
        }
    }

    /**
     * Clears the values in all datapoints
     */
    public void clearDatapoints(){
        for(ImGuiBarPlotDatapoint dataPoint : dataPoints){
            dataPoint.value = 0;
        }
    }

    /**
     * A single set of data to be plotted in this graph
     */
    public static class ImGuiBarPlotDatapoint {

        //the label of the line
        String label;

        double value;

        /**
         * Creates a datapoint object
         * @param label the label for the data
         * @param value the value of this bar item
         */
        public ImGuiBarPlotDatapoint(String label, double value){
            this.label = label;
            this.value = value;
        }

        
    }
}
