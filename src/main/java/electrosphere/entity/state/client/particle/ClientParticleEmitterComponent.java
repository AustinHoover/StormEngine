package electrosphere.entity.state.client.particle;

import org.joml.Quaterniond;
import org.joml.Random;
import org.joml.Vector3d;

import electrosphere.data.entity.particle.ParticleData;
import electrosphere.data.entity.particle.ParticleEmitter;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.equip.ClientEquipState;
import electrosphere.util.math.SpatialMathUtils;

/**
 * A component that causes the entity to emit particles
 */
public class ClientParticleEmitterComponent implements BehaviorTree {


    /**
     * The parent entity
     */
    Entity parent;

    /**
     * The particle emitter data
     */
    ParticleEmitter particleEmitter;

    /**
     * The last frame this emitter sent out a particle
     */
    long lastEmittedFrame = 0;

    /**
     * The random for particle generation
     */
    static Random particleRand = new Random(0);

    @Override
    public void simulate(float deltaTime) {
        Vector3d entityPos = EntityUtils.getPosition(parent);
        if((float)(Globals.engineState.timekeeper.getNumberOfRenderFramesElapsed() - lastEmittedFrame) > particleEmitter.getFrequency()){
            lastEmittedFrame = Globals.engineState.timekeeper.getNumberOfRenderFramesElapsed();
            //create particle here
            Vector3d spawnPos = new Vector3d(entityPos);
            Quaterniond rotation = new Quaterniond(EntityUtils.getRotation(parent));
            if(this.particleEmitter.getOffset() != null){
                spawnPos = spawnPos.add(new Vector3d(this.particleEmitter.getOffset()).rotate(rotation));
            }
            Globals.particleService.spawn(this.getData(), spawnPos, rotation);
        }
    }

    /**
     * Private constructor
     * @param parent
     * @param params
     */
    private ClientParticleEmitterComponent(Entity parent, Object ... params){
        this.parent = parent;
        particleEmitter = (ParticleEmitter)params[0];
    }

    /**
     * Gets particle data for a new particle
     * @return The data
     */
    private ParticleData getData(){
        ParticleData data = new ParticleData();
        data.setAcceleration(this.particleEmitter.getAcceleration());
        data.setColor(this.particleEmitter.getColor());
        data.setLifeCurrent(this.particleEmitter.getLifeCurrent());
        data.setMaxLife(this.particleEmitter.getMaxLife());
        data.setSize(this.particleEmitter.getSize());
        data.setTexture(this.particleEmitter.getTexture());
        Vector3d initialVelocity = new Vector3d(this.particleEmitter.getParticleVelocity());
        if(this.particleEmitter.getSpread() != null){
            Quaterniond rot = new Quaterniond();
            Vector3d cross = null;
            Vector3d normalizedVelocity = new Vector3d(initialVelocity).normalize();
            if(Math.abs(normalizedVelocity.dot(SpatialMathUtils.getOriginVector())) < 0.8){
                cross = SpatialMathUtils.getOriginVector().cross(normalizedVelocity);
            } else {
                cross = SpatialMathUtils.getUpVector().cross(normalizedVelocity);
            }
            rot.rotateAxis(Math.PI * 2 * particleRand.nextFloat(), normalizedVelocity);
            rot.rotateAxis((particleRand.nextFloat() * this.particleEmitter.getSpread()) / 180.0 * Math.PI, cross);
            initialVelocity = rot.transform(initialVelocity);
        }
        data.setVelocity(initialVelocity);
        data.setEmitter(this.particleEmitter);
        data.setParentEmitter(parent);
        return data;
    }
    

    /**
     * <p>
     * Attaches this tree to the entity.
     * </p>
     * @param entity The entity to attach to
     * @param tree The behavior tree to attach
     * @param params Optional parameters that will be provided to the constructor
     */
    public static ClientParticleEmitterComponent attachTree(Entity parent, Object ... params){
        ClientParticleEmitterComponent rVal = new ClientParticleEmitterComponent(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        parent.putData(EntityDataStrings.TREE_CLIENTPARTICLEEMITTERSTATE, rVal);
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(rVal);
        return rVal;
    }

    /**
     * <p>
     * Detatches this tree from the entity.
     * </p>
     * @param entity The entity to detach to
     * @param tree The behavior tree to detach
     */
    public static void detachTree(Entity entity, BehaviorTree tree){
    }

    /**
     * <p>
     * Gets the ClientEquipState of the entity
     * </p>
     * @param entity the entity
     * @return The ClientEquipState
     */
    public static ClientEquipState getClientEquipState(Entity entity){
        return (ClientEquipState)entity.getData(EntityDataStrings.TREE_CLIENTPARTICLEEMITTERSTATE);
    }
    
}
