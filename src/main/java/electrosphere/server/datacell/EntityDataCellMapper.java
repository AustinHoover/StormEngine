package electrosphere.server.datacell;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import electrosphere.entity.Entity;

/**
 * The idea with this class is to break out the entity->server data cell mapping out from DataCellManager so that the latter can focus on just managing the existance of data cells
 */
public class EntityDataCellMapper {
    
    /**
     * The map of entity -> server data cell
     */
    Map<Entity,ServerDataCell> entityDataCellMap = new HashMap<Entity,ServerDataCell>();

    /**
     * Lock for thread safe-ing the mapper
     */
    ReentrantLock lock = new ReentrantLock();

    /**
     * Registers an entity into the map. Should be called every time any entity is created on the server.
     * @param entity The entity that was just created
     * @param serverDataCell The server data cell to register this entity to
     */
    public void registerEntity(Entity entity, ServerDataCell serverDataCell){
        lock.lock();
        if(serverDataCell == null){
            throw new Error("Mapping entity to null!");
        }
        entityDataCellMap.put(entity, serverDataCell);
        lock.unlock();
    }

    /**
     * Gets a server data cell that an entity corresponds to
     * @param entity The entity to search by
     * @return The server data cell that the entity is inside of
     */
    public ServerDataCell getEntityDataCell(Entity entity){
        lock.lock();
        ServerDataCell rVal = entityDataCellMap.get(entity);
        lock.unlock();
        return rVal;
    }

    /**
     * Updates the server data cell that an entity is inside of. Should be called every time an entity changes server data cell.
     * @param entity The entity to update
     * @param serverDataCell The new server data cell for the entity
     */
    public void updateEntityCell(Entity entity, ServerDataCell serverDataCell){
        lock.lock();
        if(serverDataCell == null){
            throw new Error("Passing null to cell mapper update! " + entity + " " + serverDataCell);
        }
        entityDataCellMap.put(entity, serverDataCell);
        lock.unlock();
    }

    /**
     * Ejects an entity from the server data cell map. Should only be called when an entity is being destroyed. Furthermore, should be called every time an entity is destroyed.
     * @param entity The entity to eject
     */
    public void ejectEntity(Entity entity){
        lock.lock();
        entityDataCellMap.remove(entity);
        lock.unlock();
    }

}
