package electrosphere.server.datacell.utils;

import java.util.Set;

import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerDataCell;

/**
 * Provides utilities for searching cells for groups of entities
 */
public class DataCellSearchUtils {

    /**
     * Gets the data cell the entity is inside of
     * @param entity The entity
     * @return The data cell the entitiy is inside of, or null if it isn't inside a data cell for some reason
     */
    public static ServerDataCell getEntityDataCell(Entity entity){
        if(entity == null){
            LoggerInterface.loggerEngine.ERROR(new IllegalArgumentException("Trying to get entity data cell of null!"));
        }
        Realm realm = Globals.serverState.realmManager.getEntityRealm(entity);
        if(realm == null){
            String message = "Trying to get entity data cell of an entity that is not assigned to a realm!\n";
            message = message + entity + "\n";
            EntityType type = CommonEntityUtils.getEntityType(entity);
            message = message + type + "\n";
            switch(type){
                case ITEM: {
                    if(ItemUtils.itemIsInInventory(entity)){
                        message = message + "In inventory: true\n";
                        message = message + "Containing parent: " + ItemUtils.getContainingParent(entity);
                    } else {
                        message = message + "In inventory: false\n";
                    }
                } break;
                default: {
                } break;
            }
            message = message + "Attach parent: " + AttachUtils.getParent(entity) + "\n";
            LoggerInterface.loggerEngine.ERROR(new Error(message));
            return null;
        }
        return Globals.serverState.entityDataCellMapper.getEntityDataCell(entity);
    }

    /**
     * Searches around the cell containing location (radius 1 extra cell including corners) for all entities under the specified tag
     * @param location The real (non-world) location to search
     * @param tag The tag to search for
     * @return All entities with the tag in the search area
     */
    public static Set<Entity> getEntitiesWithTagAroundLocation(Realm realm, Vector3d location, String tag){
        ServerDataCell cell = realm.getDataCellManager().getDataCellAtPoint(location);
        return cell.getScene().getEntitiesWithTag(tag);
    }

    
}
