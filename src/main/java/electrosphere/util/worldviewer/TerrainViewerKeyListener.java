package electrosphere.util.worldviewer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Key listener for the terrain viewer debug tool
 */
public class TerrainViewerKeyListener implements KeyListener {

    TerrainViewerJComponent jComponent;
    
    public TerrainViewerKeyListener(TerrainViewerJComponent jComponent){
        this.jComponent = jComponent;
    }
    
    
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()){
            case KeyEvent.VK_RIGHT:
                jComponent.addViewerOffsetX();
                break;
            case KeyEvent.VK_LEFT:
                jComponent.removeViewerOffsetX();
                break;
            case KeyEvent.VK_UP:
                jComponent.removeViewerOffsetY();
                break;
            case KeyEvent.VK_DOWN:
                jComponent.addViewerOffsetY();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    
}
