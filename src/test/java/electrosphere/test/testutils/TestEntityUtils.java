package electrosphere.test.testutils;

import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerDataCell;

/**
 * Utilities for testing
 */
public class TestEntityUtils {
    
    /**
     * Returns the number of entities in the bounding box defined by boxMin and boxMax, noninclusive.
     * @param boxMin The minimum point for the bounding box
     * @param boxMax The maximum point for the bounding box
     * @return The number of entities in the bounding box
     */
    public static int numberOfEntitiesInBox(Vector3d boxMin, Vector3d boxMax){
        double minX = boxMin.x < boxMax.x ? boxMin.x : boxMax.x;
        double minY = boxMin.y < boxMax.y ? boxMin.y : boxMax.y;
        double minZ = boxMin.z < boxMax.z ? boxMin.z : boxMax.z;
        double maxX = boxMin.x > boxMax.x ? boxMin.x : boxMax.x;
        double maxY = boxMin.y > boxMax.y ? boxMin.y : boxMax.y;
        double maxZ = boxMin.z > boxMax.z ? boxMin.z : boxMax.z;

        int accumulator = 0;

        Realm realm = Globals.serverState.realmManager.getRealms().iterator().next();
        ServerDataCell dataCell = realm.getDataCellManager().getDataCellAtPoint(new Vector3d(minX,minY,minZ));
        for(Entity entity : dataCell.getScene().getEntityList()){
            if(EntityUtils.getPosition(entity) != null){
                Vector3d position = EntityUtils.getPosition(entity);
                if(
                    position.x > minX &&
                    position.x < maxX &&
                    position.y > minY &&
                    position.y < maxY &&
                    position.z > minZ &&
                    position.z < maxZ
                ){
                    accumulator++;
                }
            }
        }
        return accumulator;
    }

}
