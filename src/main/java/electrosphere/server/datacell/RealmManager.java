package electrosphere.server.datacell;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.joml.Vector3d;

import electrosphere.collision.CollisionEngine;
import electrosphere.collision.CollisionWorldData;
import electrosphere.collision.PhysicsCallback;
import electrosphere.collision.hitbox.HitboxManager;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.net.server.player.Player;
import electrosphere.server.datacell.gridded.GriddedDataCellManager;
import electrosphere.server.entity.ServerContentManager;
import electrosphere.server.physics.chemistry.ServerChemistryCollisionCallback;
import electrosphere.server.physics.collision.ServerHitboxResolutionCallback;
import electrosphere.server.physics.fluid.simulator.FluidAcceleratedSimulator;

/**
 * Manages all realms for the engine. Should be a singleton
 */
public class RealmManager {

    /**
     * All realms in this manager
     */
    Set<Realm> realms = new HashSet<Realm>();

    /**
     * Map of entities to the realm the entity is in
     */
    Map<Entity,Realm> entityToRealmMap = new HashMap<Entity,Realm>();

    /**
     * Map of player to the realm the player is in
     */
    Map<Player,Realm> playerToRealmMap = new HashMap<Player,Realm>();

    /**
     * Lock for thread-safing the manager
     */
    ReentrantLock lock = new ReentrantLock();

    /**
     * Constructor
     */
    public RealmManager(){

    }


    /**
     * Creates a realm
     * @return The realm
     */
    public Realm createRealm(){
        //create chemistry engine
        CollisionEngine chemistryEngine = new CollisionEngine("serverChem");
        chemistryEngine.setCollisionResolutionCallback(new ServerChemistryCollisionCallback());
        Realm rVal = new Realm(
            new ServerWorldData(),
            CollisionEngine.create("serverPhysics", new PhysicsCallback()),
            chemistryEngine,
            new HitboxManager(new ServerHitboxResolutionCallback()),
            ServerContentManager.createServerContentManager(false, null),
            null
        );
        return rVal;
    }

    /**
     * Creates a realm that uses a gridded layout (ie an array of cells in 3d space)
     * @return The realm
     */
    public Realm createGriddedRealm(ServerWorldData serverWorldData, ServerContentManager serverContentManager){
        //create collision engine
        CollisionEngine collisionEngine = CollisionEngine.create("serverPhysics", new PhysicsCallback());
        collisionEngine.setCollisionWorldData(new CollisionWorldData(serverWorldData));
        //create chemistry engine
        CollisionEngine chemistryEngine = new CollisionEngine("serverChem");
        chemistryEngine.setCollisionWorldData(new CollisionWorldData(serverWorldData));
        chemistryEngine.setCollisionResolutionCallback(new ServerChemistryCollisionCallback());
        //create realm
        Realm realm = new Realm(
            serverWorldData,
            collisionEngine,
            chemistryEngine,
            new HitboxManager(new ServerHitboxResolutionCallback()),
            serverContentManager,
            serverContentManager.getMacroData()
        );
        //create function classes
        GriddedDataCellManager griddedDataCellManager = new GriddedDataCellManager(realm);
        //add function classes to realm
        realm.setDataCellManager(griddedDataCellManager);
        realm.setPathfindingManager(griddedDataCellManager);
        //register within the manager
        realms.add(realm);
        return realm;
    }

    /**
     * Creates a viewport realm
     * @return The viewport realm
     */
    public Realm createViewportRealm(Vector3d minPoint, Vector3d maxPoint){
        //create the server world data
        ServerWorldData serverWorldData = ServerWorldData.createFixedWorldData(minPoint, maxPoint);

        //create collision engine
        CollisionEngine collisionEngine = CollisionEngine.create("serverPhysics", new PhysicsCallback());
        collisionEngine.setCollisionWorldData(new CollisionWorldData(serverWorldData));

        //create chemistry engine
        CollisionEngine chemistryEngine = new CollisionEngine("serverChem");
        chemistryEngine.setCollisionWorldData(new CollisionWorldData(serverWorldData));
        chemistryEngine.setCollisionResolutionCallback(new ServerChemistryCollisionCallback());

        //create realm
        Realm realm = new Realm(
            serverWorldData,
            collisionEngine,
            chemistryEngine,
            new HitboxManager(new ServerHitboxResolutionCallback()),
            ServerContentManager.createServerContentManager(false, null),
            null
        );

        //add function classes to realm
        realm.setDataCellManager(ViewportDataCellManager.create(realm));

        //register
        realms.add(realm);

        return realm;
    }

    /**
     * Maps an entity to a realm
     * @param entity The entity
     * @param realm The realm
     */
    public void mapEntityToRealm(Entity entity, Realm realm){
        lock.lock();
        entityToRealmMap.put(entity, realm);
        lock.unlock();
    }

    /**
     * Removes the entity from tracking in this realm manager
     * @param entity The entity to remove
     */
    public void removeEntity(Entity entity){
        lock.lock();
        entityToRealmMap.remove(entity);
        lock.unlock();
    }

    /**
     * Gets the realm an entity is inside of
     * @param entity The entity
     * @return The realm, or null if the entity is not inside a realm
     */
    public Realm getEntityRealm(Entity entity){
        lock.lock();
        Realm rVal = entityToRealmMap.get(entity);
        lock.unlock();
        return rVal;
    }

    /**
     * Gets the set containing all realms in the manager
     * @return The set containing all realms in the manager
     */
    public Set<Realm> getRealms(){
        lock.lock();
        Set<Realm> rVal = new HashSet<Realm>(realms);
        lock.unlock();
        return rVal;
    }


    /**
     * Simulates all realms in this manager
     */
    public void simulate(){
        Globals.profiler.beginCpuSample("RealmManager.simulate");
        Set<Realm> realms = this.getRealms();
        for(Realm realm : realms){
            realm.simulate();
        }
        Globals.profiler.endCpuSample();
    }

    //TODO: !!URGENT!! come up with some mechanism to enforce this actually being called every time a player is added to a server data cell
    /**
     * Adds a player to a realm
     * @param player The player
     * @param realm The realm
     */
    public void setPlayerRealm(Player player, Realm realm){
        lock.lock();
        playerToRealmMap.put(player, realm);
        lock.unlock();
    }

    /**
     * Gets the realm of a given player
     * @param player The player
     * @return The realm
     */
    public Realm getPlayerRealm(Player player){
        lock.lock();
        Realm rVal = playerToRealmMap.get(player);
        lock.unlock();
        return rVal;
    }

    /**
     * Saves all cells in all realms in the realm manager
     * @param saveName The name of the save
     */
    public void save(String saveName){
        Set<Realm> realms = this.getRealms();
        for(Realm realm : realms){
            realm.save(saveName);
        }
    }

    /**
     * Returns the first realm in the manager
     * @return The first realm in the manager
     */
    public Realm first(){
        if(realms.size() == 0){
            return null;
        }
        return realms.iterator().next();
    }

    /**
     * Resets the realm manager
     */
    public void reset(){
        for(Realm realm : this.realms){
            if(realm.getServerWorldData() != null && realm.getServerWorldData().getServerTerrainManager() != null){
                realm.getServerWorldData().getServerTerrainManager().closeThreads();
            }
        }
        for(Realm realm : this.realms){
            if(
                realm.getServerWorldData() != null &&
                realm.getServerWorldData().getServerFluidManager() != null &&
                realm.getServerWorldData().getServerFluidManager().getSimulator() != null &&
                realm.getServerWorldData().getServerFluidManager().getSimulator() instanceof FluidAcceleratedSimulator
            ){
                FluidAcceleratedSimulator.cleanup();
            }
            if(realm.getServerWorldData() != null && realm.getServerWorldData().getServerTerrainManager() != null){
                realm.getServerWorldData().getServerTerrainManager().closeThreads();
            }
            if(realm.getServerWorldData() != null && realm.getServerWorldData().getServerBlockManager() != null){
                realm.getServerWorldData().getServerBlockManager().closeThreads();
            }
            if(realm.getDataCellManager() != null){
                realm.getDataCellManager().halt();
            }
        }
        this.realms.clear();
        lock.lock();
        this.entityToRealmMap.clear();
        this.playerToRealmMap.clear();
        lock.unlock();
    }

    
}
