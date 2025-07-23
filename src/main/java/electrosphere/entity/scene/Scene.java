package electrosphere.entity.scene;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityTags;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.util.annotation.Exclude;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A game scene
 */
public class Scene {
    
    /**
     * The map of id -> entity
     */
    private Map<Integer,Entity> entityIdMap;

    /**
     * The map of tag -> set of entities corresponding to that tag
     */
    private Map<String,Set<Entity>> tagEntityMap;

    /**
     * The map of entity -> tags that entity is registered to
     */
    private Map<Entity,List<String>> entityTagMap;

    /**
     * The list of behavior trees
     */
    private List<BehaviorTree> behaviorTreeList;

    /**
     * Accumulator for new behavior trees
     */
    private List<BehaviorTree> additionList;

    /**
     * The list of trees to remove
     */
    private List<BehaviorTree> removalList;

    @Exclude
    /**
     * Lock for threadsafeing the scene
     */
    private ReentrantLock lock = new ReentrantLock();
    
    /**
     * Constructor
     */
    public Scene(){
        this.entityIdMap = new HashMap<Integer,Entity>();
        this.tagEntityMap = new HashMap<String,Set<Entity>>();
        this.behaviorTreeList = new LinkedList<BehaviorTree>();
        this.entityTagMap = new HashMap<Entity,List<String>>();
        this.additionList = new LinkedList<BehaviorTree>();
        this.removalList = new LinkedList<BehaviorTree>();
        this.tagEntityMap.put(EntityTags.BONE_ATTACHED, new HashSet<Entity>());
        this.tagEntityMap.put(EntityTags.COLLIDABLE, new HashSet<Entity>());
        this.tagEntityMap.put(EntityTags.SPRINTABLE, new HashSet<Entity>());
        this.tagEntityMap.put(EntityTags.MOVEABLE, new HashSet<Entity>());
        this.tagEntityMap.put(EntityTags.ATTACKER, new HashSet<Entity>());
        this.tagEntityMap.put(EntityTags.TARGETABLE, new HashSet<Entity>());
        this.tagEntityMap.put(EntityTags.LIFE_STATE, new HashSet<Entity>());
        this.tagEntityMap.put(EntityTags.CREATURE, new HashSet<Entity>());
        this.tagEntityMap.put(EntityTags.UI, new HashSet<Entity>());
        this.tagEntityMap.put(EntityTags.DRAWABLE, new HashSet<Entity>());
        this.tagEntityMap.put(EntityTags.DRAW_INSTANCED, new HashSet<Entity>());
        this.tagEntityMap.put(EntityTags.DRAW_VOLUMETIC_SOLIDS_PASS, new HashSet<Entity>());
        this.tagEntityMap.put(EntityTags.DRAW_VOLUMETIC_DEPTH_PASS, new HashSet<Entity>());
        this.tagEntityMap.put(EntityTags.DRAW_CAST_SHADOW, new HashSet<Entity>());
        this.tagEntityMap.put(EntityTags.LIGHT, new HashSet<Entity>());
        this.tagEntityMap.put(EntityTags.ITEM, new HashSet<Entity>());
        this.tagEntityMap.put(EntityTags.GRAVITY, new HashSet<Entity>());
        this.tagEntityMap.put(EntityTags.PARTICLE, new HashSet<Entity>());
        this.tagEntityMap.put(EntityTags.TRANSFORM_ATTACHED, new HashSet<Entity>());
    }
    
    /**
     * Registers an entity to the scene
     * @param e The entity to register
     */
    public void registerEntity(Entity e){
        lock.lock();
        entityIdMap.put(e.getId(), e);
        lock.unlock();
    }

    /**
     * Registers an entity to a given tag
     * @param e The entity
     * @param tag The tag
     */
    public void registerEntityToTag(Entity e, String tag){
        lock.lock();
        if(tagEntityMap.containsKey(tag)){
            tagEntityMap.get(tag).add(e);
        } else {
            Set<Entity> newEntityList = new HashSet<Entity>();
            newEntityList.add(e);
            tagEntityMap.put(tag,newEntityList);
        }
        if(this.entityTagMap.containsKey(e)){
            List<String> tagList = this.entityTagMap.get(e);
            if(!tagList.contains(tag)){
                tagList.add(tag);
            }
        } else {
            List<String> tagList = new LinkedList<String>();
            tagList.add(tag);
            this.entityTagMap.put(e, tagList);
        }
        lock.unlock();
    }

    /**
     * Gets all entities registered to a tag
     * @param tag The tag
     * @return A list of all entities with the tag, or null if no entities have been added to the tag yet
     */
    public Set<Entity> getEntitiesWithTag(String tag){
        lock.lock();
        Set<Entity> rVal = null;
        if(tagEntityMap.containsKey(tag)){
            rVal = new HashSet<Entity>(tagEntityMap.get(tag));
        }
        lock.unlock();
        return rVal;
    }

    /**
     * Gets all entities registered to a tag
     * @param tag The tag
     * @param set The pre-existing set to populate
     * @return A list of all entities with the tag, or null if no entities have been added to the tag yet
     */
    public Set<Entity> getEntitiesWithTag(String tag, Set<Entity> set){
        lock.lock();
        set.clear();
        if(tagEntityMap.containsKey(tag)){
            set.addAll(tagEntityMap.get(tag));
        }
        lock.unlock();
        return set;
    }

    /**
     * Removes an entity from a tag
     * @param e The entity
     * @param tag The tag
     */
    public void removeEntityFromTag(Entity e, String tag){
        lock.lock();
        tagEntityMap.get(tag).remove(e);
        if(this.entityTagMap.containsKey(e)){
            List<String> tagList = this.entityTagMap.get(e);
            tagList.remove(tag);
        }
        lock.unlock();
    }
    
    /**
     * Deregisters an entity from an entity manager
     * @param e
     */
    public void deregisterEntity(Entity e){
        lock.lock();
        for(String key : tagEntityMap.keySet()){
            tagEntityMap.get(key).remove(e);
        }
        this.entityTagMap.remove(e);
        entityIdMap.remove(e.getId());
        lock.unlock();
    }

    /**
     * Extracts all tags this entity is registered to
     * @param e The entity
     * @return The list of tags this entity is registered to
     */
    public List<String> extractTags(Entity e){
        lock.lock();
        List<String> rVal = this.entityTagMap.get(e);
        lock.unlock();
        return rVal;
    }

    /**
     * Registers an entity to a collection of tags
     * @param e The entity
     * @param tags The list of tags
     */
    public void registerEntityToTags(Entity e, List<String> tags){
        for(String tag : tags){
            this.registerEntityToTag(e, tag);
        }
    }
    
    /**
     * Recursively deregisters an entity and all entities attached via AttachUtils
     * @param target The top level entity to deregister
     */
    public void recursiveDeregister(Entity target){
        lock.lock();
        if(AttachUtils.hasChildren(target)){
            List<Entity> childrenList = AttachUtils.getChildrenList(target);
            for(Entity currentChild : childrenList){
                this.recursiveDeregister(currentChild);
            }
        }
        this.deregisterEntity(target);
        lock.unlock();
    }
    
    /**
     * Gets an entity via its ID
     * @param id The id to search for
     * @return The entity with that ID
     */
    public Entity getEntityFromId(int id){
        lock.lock();
        Entity rVal = (Entity)entityIdMap.get(id);
        lock.unlock();
        return rVal;
    }

    /**
     * Checks if a scene contains a given entity
     * @param e The entity
     * @return true if the scene contains the entity, false otherwise
     */
    public boolean containsEntity(Entity e){
        lock.lock();
        boolean rVal = entityIdMap.containsKey(e.getId());
        lock.unlock();
        return rVal;
    }

    /**
     * Registers a behavior tree to simulate each scene simulation frame
     * @param tree The behavior tree to register
     */
    public void registerBehaviorTree(BehaviorTree tree){
        lock.lock();
        this.additionList.add(tree);
        if(this.removalList.contains(tree)){
            this.removalList.remove(tree);
        }
        lock.unlock();
    }

    /**
     * Registers a new behavior tree that executes a task
     * @param task The task
     */
    public void registerBehaviorTree(Runnable task){
        lock.lock();
        this.registerBehaviorTree(new BehaviorTree(){public void simulate(float deltaTime) {
            task.run();
        }});
        lock.unlock();
    }

    /**
     * Deregisters a behavior tree from the scene
     * @param tree The behavior tree to deregister
     */
    public void deregisterBehaviorTree(BehaviorTree tree){
        lock.lock();
        this.removalList.add(tree);
        if(this.additionList.contains(tree)){
            this.additionList.remove(tree);
        }
        lock.unlock();
    }

    /**
     * Simulates all behavior trees stored in the entity manager
     */
    public void simulateBehaviorTrees(float deltaTime){
        lock.lock();
        Globals.profiler.beginAggregateCpuSample("Scene.simulateBehaviorTrees");

        //additions should happen before removals so that we don't re-introduce a tree that shouldn't be simulated
        //in other words its better to fail by not simulating instead of fail by simulating
        //add all trees that were queued to be added
        this.behaviorTreeList.addAll(this.additionList);

        //remove all trees that were queued to be removed
        this.behaviorTreeList.removeAll(this.removalList);

        //clear both the lists that are accumulating
        this.removalList.clear();
        this.additionList.clear();

        //simulate all trees
        for(BehaviorTree tree : this.behaviorTreeList){
            tree.simulate(deltaTime);
        }
        Globals.profiler.endCpuSample();
        lock.unlock();
    }

    /**
     * Gets the collection of all entities in the scene
     * @return The collection of all entities in the scene
     */
    public Collection<Entity> getEntityList(){
        lock.lock();
        Collection<Entity> rVal = new LinkedList<Entity>(this.entityIdMap.values());
        lock.unlock();
        return rVal;
    }

    /**
     * Gets the list of behavior trees attached to the scene
     * @return The list of behavior trees attached to the scene
     */
    public Collection<BehaviorTree> getBehaviorTrees(){
        lock.lock();
        Collection<BehaviorTree> rVal = Collections.unmodifiableList(this.behaviorTreeList);
        lock.unlock();
        return rVal;
    }

    /**
     * Gets the number of behavior trees that will execute next frame
     * @return The number of trees
     */
    public int getNumBehaviorTrees(){
        int rVal = 0;
        lock.lock();
        rVal = this.behaviorTreeList.size() + this.additionList.size();
        lock.unlock();
        return rVal;
    }

    /**
     * Describes the scene in log messages
     */
    public void describeScene(){
        LoggerInterface.loggerEngine.WARNING("Entities present in scene:");
        for(Entity entity : this.entityIdMap.values()){
            LoggerInterface.loggerEngine.WARNING(entity.getId() + "");
        }
    }
    
}
