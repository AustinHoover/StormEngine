package electrosphere.server.datacell.utils;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.server.datacell.ServerDataCell;

/**
 * 
 */
public class ServerEntityTagUtils {
    
    /**
     * Attachs a tag to an entity. !!WARNING!! This depends on the entity already being in a datacell.
     * @param entity The entity
     * @param tag The tag
     */
    public static void attachTagToEntity(Entity entity, String tag){
        ServerDataCell cell = Globals.serverState.entityDataCellMapper.getEntityDataCell(entity);
        cell.getScene().registerEntityToTag(entity, tag);
    }

    /**
     * Removes a tag from an entity. !!WARNING!! This depends on the entity already being in a datacell.
     * @param entity The entity
     * @param tag The tag
     */
    public static void removeTagFromEntity(Entity entity, String tag){
        ServerDataCell cell = Globals.serverState.entityDataCellMapper.getEntityDataCell(entity);
        cell.getScene().removeEntityFromTag(entity, tag);
    }

}
