package electrosphere.client.scene;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.joml.Vector3d;
import org.ode4j.ode.DContactGeom;
import org.ode4j.ode.DGeom;

import electrosphere.client.collision.ClientLocalHitboxCollision;
import electrosphere.collision.CollisionEngine;
import electrosphere.collision.CollisionEngine.CollisionResolutionCallback;
import electrosphere.collision.collidable.Collidable;
import electrosphere.collision.hitbox.HitboxManager;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.scene.Scene;
import electrosphere.logger.LoggerInterface;

/**
 * Wrapper around the scene object to provide lots of much needed client-specific utility
 * Does all the server<->client id translation and provides utilities to map between the two
 */
public class ClientSceneWrapper {
    
    /**
     * Translates client entity IDs to server IDs
     */
    Map<Integer,Integer> clientToServerIdMap = new HashMap<Integer,Integer>();

    /**
     * Translates server entity IDs to client IDs
     */
    Map<Integer,Integer> serverToClientIdMap = new HashMap<Integer,Integer>();

    /**
     * The list of server IDs that have been deleted
     */
    Map<Integer,Boolean> deletedServerIds = new HashMap<Integer,Boolean>();

    /**
     * The scene backing the wrapper
     */
    Scene scene;

    /**
     * The engine used to back physics collision checks in client
     */
    CollisionEngine collisionEngine;

    /**
     * The chemistry engine
     */
    CollisionEngine chemistryEngine;

    /**
     * The interaction engine
     */
    CollisionEngine interactionEngine;

    /**
     * The hitbox manager
     */
    HitboxManager hitboxManager;

    /**
     * Lock for threadsafing the scene wrapper
     */
    ReentrantLock lock = new ReentrantLock();

    /**
     * Constructor
     * @param scene The scene
     * @param collisionEngine The collision engine
     * @param chemistryEngine The chemsitry engine
     * @param interactionEngine The interaction engine
     */
    public ClientSceneWrapper(Scene scene, CollisionEngine collisionEngine, CollisionEngine chemistryEngine, CollisionEngine interactionEngine){
        this.scene = scene;
        this.collisionEngine = collisionEngine;
        this.chemistryEngine = chemistryEngine;
        this.interactionEngine = interactionEngine;
        this.hitboxManager = new HitboxManager(resolutionCallback);
    }

    /**
     * Registers a server provided ID as a mapping to a given ID on the client
     * @param clientId The client's generated ID
     * @param serverId The server's provided ID
     */
    public void mapIdToId(int clientId, int serverId){
        LoggerInterface.loggerNetworking.DEBUG("[CLIENT] MapID: " + clientId + " <===> " + serverId);
        lock.lock();
        clientToServerIdMap.put(clientId, serverId);
        serverToClientIdMap.put(serverId, clientId);
        Globals.clientState.clientSynchronizationManager.ejectDeletedKey(serverId);
        lock.unlock();
    }

    /**
     * Resolves a client ID to the equivalent ID on the server
     * @param clientId The id provided by the client
     * @return The equivalent id on the server, or -1 if no equivalent is found
     */
    public int mapClientToServerId(int clientId){
        lock.lock();
        if(clientToServerIdMap.get(clientId) == null){
            LoggerInterface.loggerNetworking.ERROR(new Error("Failed to map client entity " + clientId + " to server entity!"));
            lock.unlock();
            return -1;
        }
        int rVal = clientToServerIdMap.get(clientId);
        lock.unlock();
        return rVal;
    }

    /**
     * Translates the id provided by the server into the equivalent id on the client
     * @param serverId The id provided by the server
     * @return The equivalent id on the client
     */
    public int mapServerToClientId(int serverId){
        lock.lock();
        int rVal = serverToClientIdMap.get(serverId);
        lock.unlock();
        return rVal;
    }

    /**
     * Checks if the scene wrapper contains the provided server id
     * @param serverId The server id
     * @return true if the map contains that id, false otherwise
     */
    public boolean containsServerId(int serverId){
        lock.lock();
        boolean rVal = serverToClientIdMap.containsKey(serverId);
        lock.unlock();
        return rVal;
    }


    /**
     * Returns true if the server->client map contains a given id
     * @param id The id to search for
     * @return True if the server->client map contains the provided id
     */
    public boolean serverToClientMapContainsId(int id){
        lock.lock();
        boolean rVal = serverToClientIdMap.containsKey(id);
        lock.unlock();
        return rVal;
    }

    /**
     * Checks if the client->server map contains a given id
     * @param id The client id
     * @return true if there's a corresponding server id, false otherwise
     */
    public boolean clientToServerMapContainsId(int id){
        lock.lock();
        boolean rVal = clientToServerIdMap.containsKey(id);
        lock.unlock();
        return rVal;
    }

    /**
     * Deregisters the translation mapping for this entity
     * @param clientEntity The client entity
     */
    public void deregisterTranslationMapping(Entity clientEntity){
        lock.lock();
        if(this.clientToServerMapContainsId(clientEntity.getId())){
            //remove from client->server map
            int serverId = clientToServerIdMap.remove(clientEntity.getId());
            //remove from server->client map
            serverToClientIdMap.remove(serverId);
            deletedServerIds.put(serverId,true);
            LoggerInterface.loggerNetworking.DEBUG("[CLIENT] Remove scene from client<->server translation layer: " + clientEntity.getId() + "<->" + serverId);
        }
        lock.unlock();
    }

    /**
     * Checks if the client scene wrapper has deleted this id or not
     * @param serverId The server id
     * @return true if it was registered at one point and has since been deleted, false otherwise
     */
    public boolean hasBeenDeleted(int serverId){
        lock.lock();
        boolean rVal = deletedServerIds.containsKey(serverId);
        lock.unlock();
        return rVal;
    }


    /**
     * Gets the entity provided a server-provided id
     * @param id The server-provided ID
     * @return The entity in question
     */
    public Entity getEntityFromServerId(int id){
        Entity rVal = null;
        lock.lock();
        if(serverToClientIdMap.containsKey(id)){
            int clientId = mapServerToClientId(id);
            rVal = scene.getEntityFromId(clientId);
        }
        lock.unlock();
        return rVal;
    }

    /**
     * Dumps the status of the network translation layer
     */
    public void dumpTranslationLayerStatus(){
        lock.lock();
        LoggerInterface.loggerNetworking.WARNING("Client -> Server keys");
        LoggerInterface.loggerNetworking.WARNING(clientToServerIdMap.keySet() + "");
        LoggerInterface.loggerNetworking.WARNING("Server -> Client keys");
        LoggerInterface.loggerNetworking.WARNING(serverToClientIdMap.keySet() + "");
        lock.unlock();
    }

    /**
     * Dumps data about a given id
     * @param id The id
     */
    public void dumpIdData(int id){
        lock.lock();
        LoggerInterface.loggerNetworking.WARNING("Offending ID " + id);
        LoggerInterface.loggerNetworking.WARNING("Client->Server Map contains? " + clientToServerIdMap.containsKey(id));
        LoggerInterface.loggerNetworking.WARNING("Server->Client Map contains? " + serverToClientIdMap.containsKey(id));
        if(clientToServerIdMap.containsKey(id)){
            LoggerInterface.loggerNetworking.WARNING("Client->Server Map entity: " + clientToServerIdMap.get(id));
        }
        if(serverToClientIdMap.containsKey(id)){
            LoggerInterface.loggerNetworking.WARNING("Server->Client Map entity: " + serverToClientIdMap.get(id));
        }
        lock.unlock();
    }

    /**
     * Gets the scene backing this client scene wrapper
     * @return The scene
     */
    public Scene getScene(){
        return this.scene;
    }

    /**
     * Gets the collision engine backing the wrapper
     * @return The collision engine used for the physics system
     */
    public CollisionEngine getCollisionEngine(){
        return collisionEngine;
    }

    /**
     * Gets the chemistry engine backing the wrapper
     * @return The collision engine used for the chemistry system
     */
    public CollisionEngine getChemistryEngine(){
        return collisionEngine;
    }

    /**
     * Gets the interaction-based collision engine backing the wrapper
     * @return The collision engine used for interaction ray casting
     */
    public CollisionEngine getInteractionEngine(){
        return interactionEngine;
    }

    /**
     * Gets the hitbox manager for the client
     * @return The hitbox manager
     */
    public HitboxManager getHitboxManager(){
        return hitboxManager;
    }

    /**
     * Destroys all entities outside simulation range
     */
    public void destroyEntitiesOutsideSimRange(){
        Globals.profiler.beginCpuSample("destroyEntitiesOutsideSimRange");
        // if(Globals.drawCellManager != null && Globals.clientState.playerEntity != null){
        //     double cullRadius = Globals.drawCellManager.getDrawRadius() + ServerTerrainChunk.CHUNK_DIMENSION;
        //     Vector3d playerPosition = EntityUtils.getPosition(Globals.clientState.playerEntity);
        //     List<Entity> entityList = scene.getEntityList();
        //     for(Entity entity : entityList){
        //         Vector3d position = EntityUtils.getPosition(entity);
        //         if(playerPosition.distance(position) > cullRadius){
        //             EntityUtils.cleanUpEntity(entity);
        //         }
        //     }
        // }
        Globals.profiler.endCpuSample();
    }

    
    /**
     * The resolution callback that is invoked once a collision has occurred
     */
    CollisionResolutionCallback resolutionCallback = new CollisionResolutionCallback() {
        @Override
        public void resolve(DContactGeom geom, DGeom impactorGeom, DGeom receiverGeom, Collidable impactor, Collidable receiver, Vector3d normal, Vector3d localPosition, Vector3d worldPos, float magnitude) {
            ClientLocalHitboxCollision.clientDamageHitboxColision(geom, impactorGeom, receiverGeom, impactor, receiver, normal, localPosition, worldPos, magnitude);
        }
    };

}
