package electrosphere.server.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.joml.Vector3d;

import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.engine.signal.SignalServiceImpl;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.lod.ServerLODComponent;

/**
 * Manages lod emitters
 */
public class LODEmitterService extends SignalServiceImpl {

    /**
     * The set of LOD emitters
     */
    private List<Entity> emitters = new LinkedList<Entity>();

    /**
     * List of temporary vecs for emitter checking - are cleared every frame
     * <p>
     * Intention with this is that it can be used to guarantee a player character has physics generated
     */
    private List<Vector3d> tempVecs = new LinkedList<Vector3d>();

    /**
     * Lock for thread-safeing the service
     */
    private ReentrantLock lock = new ReentrantLock();

    /**
     * Disables lod checking (always returns that everything is full LOD)
     */
    private boolean disable;

    /**
     * Creates the LOD emitter service
     */
    public LODEmitterService() {
        super("LODEmitterService", new SignalType[]{
        });
    }

    /**
     * Gets the list of LOD emitters
     * @return The list of LOD emitters
     */
    public List<Entity> getEmitters(){
        lock.lock();
        List<Entity> rVal = Collections.unmodifiableList(this.emitters);
        lock.unlock();
        return rVal;
    }

    /**
     * Registers a LOD emitter
     * @param entity The entity
     */
    public void registerLODEmitter(Entity entity){
        lock.lock();
        this.emitters.add(entity);
        lock.unlock();
    }

    /**
     * Deregisters a LOD emitter
     * @param entity The entity
     */
    public void deregisterLODEmitter(Entity entity){
        lock.lock();
        this.emitters.remove(entity);
        lock.unlock();
    }

    /**
     * Clears the temp vecs
     */
    public void simulate(){
        this.tempVecs.clear();
    }

    /**
     * Adds a temporary vector
     * @param tempVec The temporary vector
     */
    public void addTempVec(Vector3d tempVec){
        this.tempVecs.add(tempVec);
    }

    /**
     * Checks if a given position would be full LOD
     * @param position The position
     * @return true if it is full lod, false otherwise
     */
    public boolean isFullLod(Vector3d position){
        if(this.disable){
            return true;
        }
        for(Entity emitter : this.getEmitters()){
            Vector3d emitterLoc = EntityUtils.getPosition(emitter);
            double dist = position.distance(emitterLoc);
            if(dist < ServerLODComponent.LOD_RADIUS){
                return true;
            }
        }
        for(Vector3d tempVec : this.tempVecs){
            double dist = position.distance(tempVec);
            if(dist < ServerLODComponent.LOD_RADIUS){
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the disabled status of the lod emitter service
     * @return true if it is disabled, false otherwise
     */
    public boolean getDisable() {
        return disable;
    }

    /**
     * Sets the disabled status of the lod emitter service
     * @param disable true to disable it, false otherwise
     */
    public void setDisable(boolean disable) {
        this.disable = disable;
    }

    
    
}
