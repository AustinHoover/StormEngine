package electrosphere.server.datacell;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.scene.Scene;
import electrosphere.net.server.player.Player;
import electrosphere.server.datacell.interfaces.DataCellManager;
import electrosphere.server.macro.spatial.MacroObject;

/**
 * A viewport data cell manager
 */
public class ViewportDataCellManager implements DataCellManager {

    /**
     * The players in the realm
     */
    List<Player> players;

    /**
     * The data cell for the realm
     */
    ServerDataCell serverDataCell;

    /**
     * The parent realm
     */
    Realm parent;

    /**
     * Creates a viewport data cell manager
     * @param realm The realm that will be parent to this manager
     * @return The viewport data cell manager
     */
    public static ViewportDataCellManager create(Realm realm){
        ViewportDataCellManager rVal = new ViewportDataCellManager();
        rVal.players = new LinkedList<Player>();
        rVal.serverDataCell = new ServerDataCell(new Scene());
        rVal.serverDataCell.setReady(true);
        rVal.parent = realm;
        return rVal;
    }

    @Override
    public void addPlayerToRealm(Player player) {
        players.add(player);
        this.serverDataCell.addPlayer(player);
    }

    @Override
    public void movePlayer(Player player, Vector3i newPosition) {
        //do nothing, only one data cell in the viewport manager
    }

    @Override
    public boolean updatePlayerPositions() {
        //never moves to another cell
        return false;
    }

    @Override
    public ServerDataCell getDataCellAtPoint(Vector3d point) {
        return serverDataCell;
    }

    @Override
    public Vector3i getCellWorldPosition(ServerDataCell cell) {
        return new Vector3i(0,0,0);
    }

    @Override
    public ServerDataCell tryCreateCellAtPoint(Vector3d point) {
        return serverDataCell;
    }

    @Override
    public ServerDataCell getCellAtWorldPosition(Vector3i position) {
        return serverDataCell;
    }

    @Override
    public void simulate() {
        if(Globals.serverState.microSimulation != null && Globals.serverState.microSimulation.isReady()){
            Globals.serverState.microSimulation.simulate(this.serverDataCell);
        }
        this.updatePlayerPositions();
    }

    @Override
    public void unloadPlayerlessChunks() {
        //does nothing
    }

    @Override
    public void save(String saveName) {
        //does nothing
    }

    @Override
    public Vector3d guaranteePositionIsInBounds(Vector3d positionToTest) {
        Vector3d returnPos = new Vector3d(positionToTest);
        if(positionToTest.x < parent.getServerWorldData().getWorldBoundMin().x){
            returnPos.x = parent.getServerWorldData().getWorldBoundMin().x + 1;
        }
        if(positionToTest.x >= parent.getServerWorldData().getWorldBoundMax().x){
            returnPos.x = parent.getServerWorldData().getWorldBoundMax().x - 1;
        }
        if(positionToTest.y < parent.getServerWorldData().getWorldBoundMin().y){
            returnPos.y = parent.getServerWorldData().getWorldBoundMin().y + 1;
        }
        if(positionToTest.y >= parent.getServerWorldData().getWorldBoundMax().y){
            returnPos.y = parent.getServerWorldData().getWorldBoundMax().y - 1;
        }
        if(positionToTest.z < parent.getServerWorldData().getWorldBoundMin().z){
            returnPos.z = parent.getServerWorldData().getWorldBoundMin().z + 1;
        }
        if(positionToTest.z >= parent.getServerWorldData().getWorldBoundMax().z){
            returnPos.z = parent.getServerWorldData().getWorldBoundMax().z - 1;
        }
        return returnPos;
    }

    @Override
    public void halt(){
        //does nothing
    }

    @Override
    public Collection<Entity> entityLookup(Vector3d pos, double radius) {
        List<Entity> rVal = new LinkedList<Entity>();
        for(Entity entity : this.serverDataCell.getScene().getEntityList()){
            // boundingSphere
            Vector3d entPos = EntityUtils.getPosition(entity);
            if(pos.distance(entPos) > radius){
                continue;
            }
            rVal.add(entity);
        }
        return rVal;
    }

    @Override
    public Vector3d getMacroEntryPoint(Vector3d point) {
        return new Vector3d();
    }

    @Override
    public void evaluateMacroObject(MacroObject object) {
        throw new Error("ViewportDataCellManager does not support macro objects currently");
    }

    @Override
    public boolean containsCell(ServerDataCell cell) {
        return cell == this.serverDataCell;
    }
    
}
