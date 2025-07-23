package electrosphere.util.worldviewer;

import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.physics.terrain.generation.OverworldChunkGenerator;
import electrosphere.server.physics.terrain.manager.ServerTerrainManager;
import electrosphere.server.physics.terrain.models.TerrainModel;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;

/**
 * Utility for viewing terrain generated on server
 */
public class TerrainViewer {
    
    
    public static void runViewer(){
        
        TerrainModel terrainModel;
        
        ServerWorldData worldData = ServerWorldData.createGriddedRealmWorldData(2000);
        ServerTerrainManager terrainManager = new ServerTerrainManager(worldData, new Random().nextLong(), new OverworldChunkGenerator());
        terrainManager.generate();
        terrainModel = terrainManager.getModel();

//        Utilities.saveObjectToBakedJsonFile("/Config/testingTerrain.json", terrainModel);
//        terrainModel = FileLoadingUtils.loadObjectFromAssetPath("/Config/testingTerrain.json", TerrainModel.class);
        
        JFrame frame = new JFrame();
        TerrainViewerJComponent jComponent = new TerrainViewerJComponent(terrainModel);
        frame.add(jComponent);
        frame.addKeyListener(new TerrainViewerKeyListener(jComponent));
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(0, 0, 1050, 1050);
        frame.setVisible(true);
        
        while(true){
            frame.repaint();
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
