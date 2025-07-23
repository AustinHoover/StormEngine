package electrosphere.entity;

import java.util.List;

import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.state.hitbox.HitboxCollectionState;
import electrosphere.entity.state.inventory.ServerInventoryState;
import electrosphere.entity.state.server.ServerCharacterData;
import electrosphere.entity.state.server.ServerPlayerViewDirTree;
import electrosphere.entity.types.collision.CollisionObjUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.EntityMessage;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerDataCell;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.datacell.utils.DataCellSearchUtils;
import electrosphere.server.datacell.utils.EntityLookupUtils;
import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;

/**
 * Entity utilities specifically for the server side
 */
public class ServerEntityUtils {

    /**
     * Called when the creature is first spawned to serialize to all people in its initial chunk.
     * <p>
     * !!NOTE!!: This function must be called after the entity has fully been created.
     *   The initializeServerSideEntity logic requires knowing the type of entity (creature, foliage, etc)
     *   which is typically set further in the function than the initial "spawnServerEntity" that returns
     *   the actual Entity() object.
     * </p>
     * @param entity
     * @param position
     */
    public static void initiallyPositionEntity(Realm realm, Entity entity, Vector3d position){
        if(position == null){
            throw new Error("Trying to set server entity position to null!");
        }
        double startX = position.x;
        double startY = position.y;
        double startZ = position.z;
        //reposition entity, if the position isn't correct then it will spawn at 0,0,0 when the synchronization part is called
        CollisionObjUtils.serverPositionCharacter(entity, position);
        //get current server data cell
        ServerDataCell cell = realm.getDataCellManager().getDataCellAtPoint(position);
        if(startX != position.x || startX != position.x || startX != position.x){
            throw new Error("Position not preserved while initially positioning entity! " + startX + "," + startY + "," + startZ + "   " + position.x + "," + position.y + "," + position.z);
        }
        Vector3d entPos = EntityUtils.getPosition(entity);
        if(startX != entPos.x || startX != entPos.x || startX != entPos.x){
            throw new Error("Position not preserved while initially positioning entity! " + startX + "," + startY + "," + startZ + "   " + entPos.x + "," + entPos.y + "," + entPos.z);
        }
        if(cell != null){
            //initialize server datacell tracking of this entity
            realm.initializeServerSideEntity(entity, cell);
        } else {
            //if it doesn't already exist, try creating it and if successfull move creature
            cell = realm.getDataCellManager().tryCreateCellAtPoint(position);
            if(cell == null){
                throw new Error("Trying to initially position entity to position that cannot generate a data cell! " + position);
            }
            //initialize server datacell tracking of this entity
            realm.initializeServerSideEntity(entity, cell);
        }
        if(startX != position.x || startX != position.x || startX != position.x){
            throw new Error("Position not preserved while initially positioning entity! " + startX + "," + startY + "," + startZ + "   " + position.x + "," + position.y + "," + position.z);
        }
    }
    
    /**
     * Called to reposition the entity
     * @param entity
     * @param position
     */
    public static void repositionEntity(Entity entity, Vector3d position){
        if(position == null){
            throw new Error("Trying to set server entity position to null!");
        }
        if(AttachUtils.getParent(entity) != null){
            throw new Error("Trying to reposition attached entity!");
        }
        double startX = position.x;
        double startY = position.y;
        double startZ = position.z;

        Realm realm = Globals.serverState.realmManager.getEntityRealm(entity);
        ServerWorldData worldDat = realm.getServerWorldData();
        if(
            position.x < 0 ||
            position.y < 0 ||
            position.z < 0 ||
            ServerWorldData.convertRealToChunkSpace(position.x) >= worldDat.getWorldSizeDiscrete() ||
            ServerWorldData.convertRealToChunkSpace(position.y) >= worldDat.getWorldSizeDiscrete() ||
            ServerWorldData.convertRealToChunkSpace(position.z) >= worldDat.getWorldSizeDiscrete()
        ){
            throw new Error("Providing invalid location to reposition! " + position);
        }
        ServerEntityUtils.repositionEntityRecursive(realm, entity, position);
        //reposition entity
        CollisionObjUtils.serverPositionCharacter(entity, position);

        //error checking
        if(position.x != startX || position.y != startY || position.z != startZ){
            throw new Error("Position mutated while repositioning! " + position + "  " + startX + "," + startY + "," + startZ);
        }
    }

    /**
     * Called to reposition the entity
     * @param realm The realm containing the entity
     * @param entity The entity
     * @param position The new position for the entity
     */
    protected static void repositionEntityRecursive(Realm realm, Entity entity, Vector3d position){
        //if server, get current server data cell
        ServerDataCell oldDataCell = Globals.serverState.entityDataCellMapper.getEntityDataCell(entity);
        ServerDataCell newDataCell = realm.getDataCellManager().getDataCellAtPoint(position);
        if(oldDataCell == null){
            LoggerInterface.loggerEngine.WARNING(
                "Trying to reposition entity on server when it's former cell is null!\n" +
                "Entity original position: " + EntityUtils.getPosition(entity) + "\n"
            );
        }
        if(oldDataCell != newDataCell){
            if(newDataCell == null){
                newDataCell = realm.getDataCellManager().tryCreateCellAtPoint(position);
                if(newDataCell == null){
                    LoggerInterface.loggerEngine.WARNING(
                        "Trying to reposition entity on server when it's new cell is null!\n" +
                        "Entity new position: " + position + "\n"
                    );
                    return;
                }
            }
            ServerDataCell.moveEntityFromCellToCell(entity, oldDataCell, newDataCell);
            ServerBehaviorTreeUtils.updateCell(entity, oldDataCell);
            if(oldDataCell != null && oldDataCell.getScene().containsEntity(entity)){
                throw new Error("Entity not removed from scene!");
            }
            //if macro data hasn't been generated in this area, generate it
            //but only if it's a player's entity
            if(ServerPlayerViewDirTree.hasTree(entity)){
                realm.updateMacroData(position);
            }
        }
        if(AttachUtils.hasChildren(entity)){
            List<Entity> children = AttachUtils.getChildrenList(entity);
            for(Entity child : children){
                if(Globals.serverState.realmManager.getEntityRealm(child) == null){
                    continue;
                }
                ServerEntityUtils.repositionEntityRecursive(realm, child, position);
            }
        }
    }

    /**
     * Destroys an entity on the server
     * @param entity the entity to destroy
     */
    public static void destroyEntity(Entity entity){
        if(entity == null){
            throw new IllegalArgumentException("Trying to destroy null!");
        }

        //
        //get info required to destroy
        Realm realm = Globals.serverState.realmManager.getEntityRealm(entity);
        ServerDataCell cell = null;

        //
        //realm specific logic
        if(realm != null){
            realm.getCollisionEngine().destroyPhysics(entity);
            cell = DataCellSearchUtils.getEntityDataCell(entity);
        }

        //
        //cell specific logic
        if(cell != null){
            cell.broadcastNetworkMessage(EntityMessage.constructDestroyMessage(entity.getId()));
            ServerBehaviorTreeUtils.deregisterEntity(entity);
            cell.getScene().deregisterEntity(entity);
        }

        //
        //detatch from all global tracking
        HitboxCollectionState.destroyHitboxState(entity,true);
        Globals.serverState.realmManager.removeEntity(entity);
        EntityLookupUtils.removeEntity(entity);
        if(Globals.serverState.aiManager != null){
            Globals.serverState.aiManager.removeAI(entity);
        }
        if(ServerCharacterData.hasServerCharacterDataTree(entity)){
            Globals.serverState.characterService.removeEntity(ServerCharacterData.getServerCharacterData(entity).getCharacterData());
        }
        Globals.serverState.lodEmitterService.deregisterLODEmitter(entity);

        //
        //Stop inventory watching
        if(ServerInventoryState.getServerInventoryState(entity) != null){
            ServerInventoryState serverInventoryState = ServerInventoryState.getServerInventoryState(entity);
            serverInventoryState.destroy();
        }

        //
        //deregister all behavior trees
        EntityUtils.cleanUpEntity(entity);

        //
        //destroy the child entities, too
        if(AttachUtils.hasChildren(entity)){
            List<Entity> children = AttachUtils.getChildrenList(entity);
            for(Entity child : children){
                ServerEntityUtils.destroyEntity(child);
            }
        }
    }

    /**
     * Guarantees that the returned position is in bounds of the server realm
     * @param realm The realm to test
     * @param position the position
     * @return Either the position if it is in bounds, or the closest position that is in bounds
     */
    public static Vector3d guaranteePositionIsInBounds(Realm realm, Vector3d position){
        return realm.getDataCellManager().guaranteePositionIsInBounds(position);
    }

    /**
     * Sets the scale of the entity
     * @param entity The entity
     * @param scale The scale to set to
     */
    public static void setScale(Entity entity, Vector3d scale){
        EntityUtils.getScale(entity).set(scale);
    }

}
