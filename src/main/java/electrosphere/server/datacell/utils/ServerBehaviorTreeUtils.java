package electrosphere.server.datacell.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import electrosphere.entity.Entity;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.server.datacell.ServerDataCell;

/**
 * Abstracting away behavior tree management so that it can be a "register btree to entity" and forget about it type deal
 * 
 * Note, there is no universal, hard coupling of an entity to a behavior tree outside of this class.
 * TODO?: Should maybe consider refactoring that mechanism into a dedicated manager or something.
 */
public class ServerBehaviorTreeUtils {

    /**
     * The map of entity to set of behavior trees attached to it
     */
    static Map<Entity,Set<BehaviorTree>> entityBTreeMap = new HashMap<Entity,Set<BehaviorTree>>();

    /**
     * Lock for thread-safeing the util
     */
    static ReentrantLock lock = new ReentrantLock();
    
    /**
     * Tracks behavior trees attached to this entity
     * @param entity
     */
    public static void registerEntity(Entity entity){
        lock.lock();
        entityBTreeMap.put(entity, new HashSet<BehaviorTree>());
        lock.unlock();
    }

    /**
     * Stops tracking behavior trees attached to this entity
     * @param entity
     */
    public static void deregisterEntity(Entity entity){
        lock.lock();
        Set<BehaviorTree> trees = entityBTreeMap.remove(entity);
        ServerDataCell currentCell = DataCellSearchUtils.getEntityDataCell(entity);
        if(trees != null){
            for(BehaviorTree tree : trees){
                currentCell.getScene().deregisterBehaviorTree(tree);
            }
        }
        lock.unlock();
    }

    /**
     * Registers a behavior tree to an entity
     * @param entity The entity
     * @param behaviorTree The behavior tree
     */
    public static void attachBTreeToEntity(Entity entity, BehaviorTree behaviorTree){
        lock.lock();
        entityBTreeMap.get(entity).add(behaviorTree);
        //register to cell
        ServerDataCell currentCell = DataCellSearchUtils.getEntityDataCell(entity);
        currentCell.getScene().registerBehaviorTree(behaviorTree);
        lock.unlock();
    }

    /**
     * Removes the behavior tree from the entity
     * @param entity The entity
     * @param behaviorTree The behavior tree
     */
    public static void detatchBTreeFromEntity(Entity entity, BehaviorTree behaviorTree){
        lock.lock();
        entityBTreeMap.get(entity).remove(behaviorTree);
        //deregister from cell
        ServerDataCell currentCell = DataCellSearchUtils.getEntityDataCell(entity);
        currentCell.getScene().deregisterBehaviorTree(behaviorTree);
        lock.unlock();
    }

    
    /**
     * Updates the server data cell that all entity trees are registered to
     * @param entity The entity
     * @param oldCell The cell the entity used to inhabit
     */
    public static void updateCell(Entity entity, ServerDataCell oldCell){
        lock.lock();
        Set<BehaviorTree> trees = entityBTreeMap.get(entity);
        ServerDataCell newCell = DataCellSearchUtils.getEntityDataCell(entity);
        if(trees != null){
            for(BehaviorTree tree : trees){
                if(oldCell != null){
                    oldCell.getScene().deregisterBehaviorTree(tree);
                }
                newCell.getScene().registerBehaviorTree(tree);
            }
        }
        lock.unlock();
    }

}
