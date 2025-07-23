package electrosphere.client.script;

import org.graalvm.polyglot.HostAccess.Export;
import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.client.interact.select.AreaSelection;
import electrosphere.client.scene.ClientWorldData;
import electrosphere.controls.cursor.CursorState;
import electrosphere.engine.Globals;
import electrosphere.entity.EntityUtils;

/**
 * Script utils for clients dealing with areas
 */
public class ScriptClientAreaUtils {
    
    /**
     * Tries to select a rectangular area
     */
    @Export
    public static AreaSelection selectAreaRectangular(){
        // Vector3d blockCursorPos = Globals.cursorState.getBlockCursorPos();
        Vector3d cursorPos = new Vector3d(EntityUtils.getPosition(Globals.cursorState.playerCursor));
        Vector3i chunkPos = Globals.clientState.clientWorldData.convertRealToWorldSpace(cursorPos);
        Vector3i blockPos = ClientWorldData.convertRealToLocalBlockSpace(cursorPos);
        AreaSelection selection = AreaSelection.selectRectangularBlockCavity(chunkPos, blockPos, AreaSelection.DEFAULT_SELECTION_RADIUS);
        return selection;
    }

    /**
     * Makes a selection visible
     * @param selection The selection
     */
    @Export
    public static void makeSelectionVisible(AreaSelection selection){
        Globals.cursorState.selectRectangularArea(selection);
        CursorState.makeAreaVisible();
    }

}
