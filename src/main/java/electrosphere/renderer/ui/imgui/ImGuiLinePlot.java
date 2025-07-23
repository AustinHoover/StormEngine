package electrosphere.renderer.ui.imgui;

import java.util.LinkedList;
import java.util.List;

import imgui.ImVec2;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.flag.ImPlotAxisFlags;

/**
 * A line plot of a given set of data
 */
public class ImGuiLinePlot implements ImGuiElement {

    //the title of the plot
    String plotTitle;

    //the data sets to draw
    List<ImGuiLinePlotDataset> dataSets = new LinkedList<ImGuiLinePlotDataset>();

    /**
     * Size of the plot
     */
    ImVec2 size = new ImVec2(-1,-1);

    /**
     * Creates an im gui line plot
     */
    public ImGuiLinePlot(String plotTitle){
        this.plotTitle = plotTitle;
    }

    /**
     * Creates an im gui line plot
     */
    public ImGuiLinePlot(String plotTitle, int sizeX, int sizeY){
        this.plotTitle = plotTitle;
        this.size.x = sizeX;
        this.size.y = sizeY;
    }

    @Override
    public void draw() {
        if(ImPlot.beginPlot(plotTitle,"","",size,0,ImPlotAxisFlags.AutoFit,ImPlotAxisFlags.AutoFit)){
            for(ImGuiLinePlotDataset dataSet : dataSets){
                double[] xs = dataSet.xData.stream().mapToDouble(Double::doubleValue).toArray();//(Double[])dataSet.xData.toArray(new Double[dataSet.xData.size()]);
                double[] ys = dataSet.yData.stream().mapToDouble(Double::doubleValue).toArray();//(Double[])dataSet.yData.toArray(new Double[dataSet.yData.size()]);
                ImPlot.plotLine(dataSet.label, xs, ys, xs.length, 0);
            }
            ImPlot.endPlot();
        }
    }

    /**
     * Adds a dataset to the line plot
     * @param dataset The dataset
     */
    public void addDataset(ImGuiLinePlotDataset dataset){
        this.dataSets.add(dataset);
    }

    /**
     * A single set of data to be plotted in this graph
     */
    public static class ImGuiLinePlotDataset {

        //x data
        List<Double> xData = new LinkedList<Double>();

        //y data
        List<Double> yData = new LinkedList<Double>();

        //the label of the line
        String label;

        //the max number of points
        int limit;

        /**
         * Creates a dataset object
         * @param x the x data
         * @param y the y data
         * @param label the label for the data
         * @param limit the maximum number of objects to keep in the dataset
         */
        public ImGuiLinePlotDataset(String label, int limit){
            this.label = label;
            this.limit = limit;
        }

        /**
         * Adds data to the data set. If the amount of data is greater than the limit, it will remove the oldest datapoint
         * @param x the x value
         * @param y the y value
         */
        public void addPoint(double x, double y){
            xData.add(x);
            yData.add(y);
            while(xData.size() > limit){
                xData.remove(0);
            }
            while(yData.size() > limit){
                yData.remove(0);
            }
        }

        /**
         * Adds data to the data set. If the amount of data is greater than the limit, it will remove the oldest datapoint.
         * Does not clear the x axis so can constantly receive data and update without axis freaking out.
         * @param y the y value
         */
        public void addPoint(double y){
            yData.add(y);
            while(yData.size() > limit){
                yData.remove(0);
            }
        }

        /**
         * Zeroes out the dataset
         */
        public void zeroOut(){
            for(int i = 0; i < limit; i++){
                this.addPoint(i, 0);
            }
        }

        
    }
    
}
