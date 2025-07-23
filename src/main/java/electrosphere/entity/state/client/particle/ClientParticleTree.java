package electrosphere.entity.state.client.particle;

import org.joml.AxisAngle4f;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.data.entity.particle.ParticleData;
import electrosphere.data.entity.particle.ParticleEmitter;
import electrosphere.engine.Globals;
import electrosphere.entity.ClientEntityUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.renderer.actor.instance.InstancedActor;
import electrosphere.renderer.texture.TextureAtlas;

/**
 * Particle component for a client-side particle
 */
public class ClientParticleTree implements BehaviorTree {
    
    //The parent entity
    Entity parent;

    //used to toggle life simulation
    boolean hasLife = true;
    //max life
    int maxLife;
    //current life
    int lifeCurrent;

    //the velocity of the particle
    Vector3d velocity;

    //the acceleration of the particle
    float acceleration;

    /**
     * The data for this particle
     */
    ParticleData particleData;

    /**
     * The color of the particle
     */
    Vector3d color;

    /**
     * The data about the emitter
     */
    ParticleEmitter emitterData;
    

    /**
     * Constructor
     * @param parent
     * @param params
     */
    private ClientParticleTree(Entity parent, Object ... params){
        if(params.length < 1){
            throw new IllegalArgumentException("No particle data was provided");
        }
        this.particleData = (ParticleData)params[0];

        //sets data for the tree
        this.parent = parent;
        this.maxLife = particleData.getMaxLife();
        this.velocity = particleData.getVelocity();
        this.acceleration = particleData.getAcceleration();
        this.hasLife = particleData.getMaxLife() != null;
        this.lifeCurrent = maxLife;
        this.emitterData = particleData.getEmitter();
        this.color = new Vector3d(particleData.getColor());
    }

    public int getMaxLife() {
        return maxLife;
    }

    public int getLifeCurrent() {
        return lifeCurrent;
    }

    public Vector3d getVelocity() {
        return velocity;
    }

    public float getAcceleration() {
        return acceleration;
    }
    
    @Override
    public void simulate(float deltaTime){
        InstancedActor instancedActor = InstancedActor.getInstancedActor(parent);
        Vector3d position = EntityUtils.getPosition(parent);
        Vector3d cameraPos = CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera);

        //update position
        position.add(velocity);
        EntityUtils.setPosition(parent, position);
        
        //update velocity
        Vector3d accelerationVec = new Vector3d(velocity).normalize().mul(acceleration);
        velocity = new Vector3d(velocity).add(accelerationVec);
        if(velocity.length() < 0){
            velocity = new Vector3d(0,0,0);
            acceleration = 0;
        }
        //add radial acceleration
        if(this.emitterData.getRadialAcceleration() != null){
            Vector3d towardsParent = new Vector3d(EntityUtils.getPosition(this.particleData.getParentEmitter())).sub(position.x, position.y, position.z).normalize().mul(this.emitterData.getRadialAcceleration());
            velocity = velocity.add(towardsParent);
        }

        //rotate the model to face the camera
        Matrix4f rotationMatrix = new Matrix4f(Globals.renderingEngine.getViewMatrix()).invert();
        Quaternionf rotation = new Quaternionf(rotationMatrix.getRotation(new AxisAngle4f()));
        EntityUtils.getRotation(parent).set(rotation);

        //scale the particle
        Vector3f scale = EntityUtils.getScale(parent);
        scale.set(this.particleData.getSize());

        //calculate alpha
        float alpha = 1.0f;
        if(this.emitterData.getTransparency() != null){
            float t = 1.0f - (lifeCurrent / (float)maxLife);
            alpha = (float)this.emitterData.getTransparency().calculate(t);
        }

        //store color
        Vector4f currentColor = new Vector4f(
            (float)this.color.y,
            (float)this.color.z,
            alpha,
            (float)this.color.x
        );

        //calculate life left
        if(hasLife){
            lifeCurrent--;
            if(lifeCurrent <= 0){
                ClientEntityUtils.destroyEntity(parent);
                Globals.clientState.clientSceneWrapper.getScene().deregisterBehaviorTree(this);
            }
        }

        //push values to buffer that eventually gets uploaded to gpu
        if(instancedActor != null){
            TextureAtlas particleTextureAtlas = Globals.particleService.getTextureAtlas();
            int textureIndex = particleTextureAtlas.getTextureIndex(this.particleData.getTexture());
            instancedActor.setAttribute(Globals.particleService.getModelAttrib(), new Matrix4d().translationRotateScale(
                new Vector3d((float)position.x,(float)position.y,(float)position.z).sub(cameraPos.x, cameraPos.y, cameraPos.z),
                new Quaterniond(rotation),
                new Vector3d(scale)
            ));
            instancedActor.setAttribute(Globals.particleService.getColorAttrib(), currentColor);

            //when written to buffer, will be written in order w, x, y, z
            //but gpu will fetch in order x, y, z, w
            instancedActor.setAttribute(Globals.particleService.getTextureAttrib(), new Vector4f(
                particleTextureAtlas.getNDCDimension(),
                particleTextureAtlas.getNDCCoordX(textureIndex),
                particleTextureAtlas.getNDCCoordY(textureIndex),
                particleTextureAtlas.getNDCDimension()
            ));
        }
    }

    /**
     * Attaches the client particle tree to the given entity
     * @param parent The parent entity
     * @param params The params
     */
    public static ClientParticleTree attachTree(Entity parent, Object ... params){
        ClientParticleTree rVal = new ClientParticleTree(parent,params);
        parent.putData(EntityDataStrings.TREE_CLIENTPARTICLETREE, rVal);
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(rVal);
        return rVal;
    }

}
