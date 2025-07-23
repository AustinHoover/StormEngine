package electrosphere.server.ai;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import electrosphere.data.entity.creature.ai.AITreeData;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.logger.LoggerInterface;
import electrosphere.server.ai.services.NearbyEntityService;
import electrosphere.server.ai.services.PathfindingService;
import electrosphere.server.ai.services.TimerService;

/**
 * Server manager for all entity AIs
 */
public class AIManager {

    /**
     * Lock for thread-safeing the manager
     */
    private ReentrantLock lock = new ReentrantLock();
    
    /**
     * The list of ais
     */
    private List<AI> aiList = new LinkedList<AI>();

    /**
     * The map of ai to associated entity
     */
    private Map<AI,Entity> aiEntityMap = new HashMap<AI,Entity>();

    /**
     * The map of entity to associated ai
     */
    private Map<Entity,AI> entityAIMap = new HashMap<Entity,AI>();

    /**
     * Controls whether the ai manager should simulate each frame or not
     */
    private boolean active = false;

    /**
     * The timer service
     */
    private TimerService timerService = new TimerService();

    /**
     * The nearby entity service
     */
    private NearbyEntityService nearbyEntityService = new NearbyEntityService();

    /**
     * Service for performing pathfinding
     */
    private PathfindingService pathfindingService = new PathfindingService();

    /**
     * The random of the ai
     */
    private Random random = null;
    
    /**
     * Constructor
     */
    public AIManager(long seed){
        this.random = new Random(seed);
    }
    
    /**
     * Simulates all AIs currently available
     */
    public void simulate(){
        Globals.profiler.beginCpuSample("AIManager.simulate");
        lock.lock();
        //exec the services
        Globals.profiler.beginCpuSample("AIManager.simulate - services");
        this.execServices();
        Globals.profiler.endCpuSample();

        //simulate each tree
        Globals.profiler.beginCpuSample("AIManager.simulate - ai logic");
        if(this.isActive()){
            for(AI ai : aiList){
                try {
                    ai.simulate();
                } catch(Error|Exception e){
                    LoggerInterface.loggerAI.ERROR(e);
                }
            }
        }
        Globals.profiler.endCpuSample();
        lock.unlock();
        Globals.profiler.endCpuSample();
    }

    /**
     * Sets the active status of the ai manager
     * @param isActive true to simulate ai each frame, false otherwise
     */
    public void setActive(boolean isActive){
        lock.lock();
        //turn off ai components if deactivating ai
        if(this.active && !isActive){
            for(AI ai : aiList){
                ai.resetComponents();
            }
        }
        //actually set
        this.active = isActive;
        lock.unlock();
    }

    /**
     * Gets whether the ai manager is active or not
     * @return true if simulating each frame, false otherwise
     */
    public boolean isActive(){
        return active;
    }

    /**
     * Gets the list of all registered AIs
     * @return The list of AIs
     */
    public List<AI> getAIList(){
        lock.lock();
        List<AI> rVal = Collections.unmodifiableList(this.aiList);
        lock.unlock();
        return rVal;
    }

    /**
     * Attaches an AI to an entity
     * @param entity The entity
     * @param treeData The list of data on trees to be provided
     */
    public void attachAI(Entity entity, List<AITreeData> treeData){
        if(entity == null){
            LoggerInterface.loggerEngine.ERROR(new IllegalArgumentException("Entity provided is null!"));
        }
        lock.lock();
        AI ai = AI.constructAI(entity, treeData);
        aiList.add(ai);
        entityAIMap.put(entity,ai);
        aiEntityMap.put(ai,entity);
        AI.setAI(entity, ai);
        lock.unlock();
    }

    /**
     * Removes the ai for this entity from the manager
     * @param entity The entity
     */
    public void removeAI(Entity entity){
        lock.lock();
        AI targetAI = entityAIMap.get(entity);
        aiList.remove(targetAI);
        aiEntityMap.remove(targetAI);
        entityAIMap.remove(entity);
        lock.unlock();
    }

    /**
     * Executes all the services
     */
    private void execServices(){
        timerService.exec();
        nearbyEntityService.exec();
    }

    /**
     * Gets the timer service
     * @return The timer service
     */
    public TimerService getTimerService(){
        return timerService;
    }

    /**
     * Gets the nearby entity service
     * @return The nearby enttiy service
     */
    public NearbyEntityService getNearbyEntityService(){
        return nearbyEntityService;
    }

    /**
     * Gets the pathfinding service
     * @return The pathfinding service
     */
    public PathfindingService getPathfindingService(){
        return this.pathfindingService;
    }

    /**
     * Gets the ai manager's random
     * @return The random
     */
    public Random getRandom(){
        return random;
    }

    /**
     * Shuts down the ai manager
     */
    public void shutdown(){
        lock.lock();
        this.pathfindingService.shutdown();
        this.nearbyEntityService.shutdown();
        this.timerService.shutdown();
        lock.unlock();
    }
    
}
