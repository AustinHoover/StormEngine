package electrosphere.util.worldviewer;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JComponent;

import electrosphere.server.physics.terrain.models.TerrainModel;

/**
 * JComponent for hosting the terrain viewer debug tool
 */
public class TerrainViewerJComponent extends JComponent {
    
    TerrainModel model;
    float elevationMap[][];
    float maxHeight = 0;
    int dimX;
    int dimY;
    
    float oceanThreshold;
    float mountainThreshold;
    
    int viewerOffsetX = 0;
    int viewerOffsetY = 0;
    
    public TerrainViewerJComponent(TerrainModel model){
        this.model = model;
        this.elevationMap = model.getElevation();
        dimX = elevationMap.length;
        dimY = elevationMap[0].length;
        
        float counter = 0;
        float average = 0;
        for(int x = 0; x < dimX; x++){
            for(int y = 0; y < dimY; y++){
                counter++;
                if(elevationMap[x][y] > maxHeight){
                    maxHeight = elevationMap[x][y];
                }
                average = average + elevationMap[x][y] * 1.0f / counter;
            }
        }
        
        this.oceanThreshold = model.getRealOceanThreshold();
        this.mountainThreshold = model.getRealMountainThreshold();
        
        System.out.println("Ocean Threshold: " + oceanThreshold);
        System.out.println("Mountain Threshold: " + mountainThreshold);
        System.out.println("Average: " + average);
    }
    
    @Override
    public void paint(Graphics g){
        g.clearRect(0, 0, 1050, 1050);
        for(int x = 0; x < dimX/2; x++){
            for(int y = 0; y < dimY/2; y++){
                g.setColor(new Color(
                        (int)(50 + 205 * elevationMap[x*2][y*2] / maxHeight),
                        (int)(100 + 155 * elevationMap[x*2][y*2] / maxHeight),
                        (int)(255 * elevationMap[x*2][y*2] / maxHeight)
                ));
                if(elevationMap[x*2][y*2] <= oceanThreshold){
                    g.setColor(new Color(
                        (int)(255 * elevationMap[x*2][y*2] / maxHeight),
                        (int)(100 + 155 * elevationMap[x*2][y*2] / maxHeight),
                        (int)(150 + 105 * elevationMap[x*2][y*2] / maxHeight)
                    ));
                }
                g.drawRect(x - viewerOffsetX, y - viewerOffsetY, 1, 1);
            }
        }
    }
    
    public void addViewerOffsetX(){
        viewerOffsetX = viewerOffsetX + 10;
    }
    
    public void removeViewerOffsetX(){
        viewerOffsetX = viewerOffsetX - 10;
    }
    
    public void addViewerOffsetY(){
        viewerOffsetY = viewerOffsetY + 10;
    }
    
    public void removeViewerOffsetY(){
        viewerOffsetY = viewerOffsetY - 10;
    }
    
}
