package electrosphere.client.entity.particle;

import java.util.Arrays;
import java.util.List;

import org.joml.Quaterniond;
import org.joml.Vector3d;

import electrosphere.client.entity.instance.InstanceTemplate;
import electrosphere.client.entity.instance.InstancedEntityUtils;
import electrosphere.data.entity.particle.ParticleData;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.engine.signal.Signal;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.engine.signal.SignalServiceImpl;
import electrosphere.entity.DrawableUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.client.particle.ClientParticleTree;
import electrosphere.renderer.actor.instance.StridedInstanceData;
import electrosphere.renderer.buffer.HomogenousUniformBuffer.HomogenousBufferTypes;
import electrosphere.renderer.texture.TextureAtlas;
import electrosphere.renderer.buffer.ShaderAttribute;

/**
 * The particle service
 */
public class ParticleService extends SignalServiceImpl {

    /**
     * The maximum number of particles
     */
    static final int MAX_PARTICLES = 1000;

    /**
     * The bind point for the particle ssbo
     */
    static final int PARTICLE_SSBO_BIND_POINT = 4;

    /**
     * Path to the vertex shader
     */
    static final String VERTEX_SHADER_PATH = "Shaders/entities/particle/particle.vs";

    /**
     * Path to the fragment shader
     */
    static final String FRAGMENT_SHADER_PATH = "Shaders/entities/particle/particle.fs";

    /**
     * The particle texture atlas
     */
    TextureAtlas particleTextureAtlas;


    /**
     * The instance data for the particles
     */
    StridedInstanceData particleInstanceData;

    /**
     * The template to use when creating instanced actors
     */
    InstanceTemplate instanceTemplate;


    ShaderAttribute modelAttrib;

    ShaderAttribute colorAttrib;

    ShaderAttribute textureAttrib;

    /**
     * Constructor
     */
    public ParticleService() {
        super(
            "ParticleService",
            new SignalType[]{
                SignalType.RENDERING_ENGINE_READY,
            }
        );
    }

    @Override
    public void destroy() {
        super.destroy();
        if(particleInstanceData != null){
            particleInstanceData.destroy();
        }
    }

    @Override
    public boolean handle(Signal signal){
        boolean rVal = false;
        switch(signal.getType()){
            case RENDERING_ENGINE_READY: {
                modelAttrib = new ShaderAttribute("model", HomogenousBufferTypes.MAT4D);
                colorAttrib = new ShaderAttribute("color", HomogenousBufferTypes.VEC4F);
                textureAttrib = new ShaderAttribute("texture", HomogenousBufferTypes.VEC4F);
                List<ShaderAttribute> types = Arrays.asList(new ShaderAttribute[]{
                    modelAttrib,
                    colorAttrib,
                    textureAttrib,
                });
                this.particleInstanceData = new StridedInstanceData(MAX_PARTICLES,PARTICLE_SSBO_BIND_POINT,types,VERTEX_SHADER_PATH,FRAGMENT_SHADER_PATH);
                this.instanceTemplate = InstanceTemplate.createInstanceTemplate(MAX_PARTICLES, AssetDataStrings.MODEL_PARTICLE, VERTEX_SHADER_PATH, FRAGMENT_SHADER_PATH, particleInstanceData, PARTICLE_SSBO_BIND_POINT);
                rVal = true;
            } break;
            default: {
            } break;
        }
        return rVal;
    }

    /**
     * Gets the instance data for the particles
     * @return The instance data
     */
    public StridedInstanceData getInstanceData(){
        return this.particleInstanceData;
    }

    /**
     * Sets the particle texture atlas
     * @param particleTextureAtlas The particle texture atlas
     */
    public void setParticleTextureAtlas(TextureAtlas particleTextureAtlas){
        this.particleTextureAtlas = particleTextureAtlas;
    }

    /**
     * Sets the texture atlas for the particle texture
     * @return The atlas
     */
    public TextureAtlas getTextureAtlas(){
        return particleTextureAtlas;
    }

    /**
     * Spawns a particle
     * @param data The particle data for the particle
     * @param position The position of the particle
     * @param rotation The rotation of the particle
     * @return The particle
     */
    public Entity spawn(ParticleData data, Vector3d position, Quaterniond rotation){
        Entity rVal = EntityCreationUtils.createClientSpatialEntity();
        InstancedEntityUtils.makeEntityInstancedWithModelTransform(rVal, instanceTemplate, modelAttrib);
        DrawableUtils.makeEntityTransparent(rVal);
        EntityUtils.setPosition(rVal, position);
        EntityUtils.getRotation(rVal).set(rotation);
        EntityUtils.getScale(rVal).set(data.getSize());
        ClientParticleTree.attachTree(rVal, data);
        return rVal;
    }

    public ShaderAttribute getModelAttrib(){
        return modelAttrib;
    }

    public ShaderAttribute getColorAttrib(){
        return colorAttrib;
    }

    public ShaderAttribute getTextureAttrib(){
        return textureAttrib;
    }
    
}
