package electrosphere.data.entity.particle;

import org.joml.Vector3d;

import electrosphere.entity.Entity;

/**
 * Data on how a particle should behave
 */
public class ParticleData {
    

    /**
     * The name of the particle type
     */
    String name;
    
    /**
     * The maximum life of the particle
     */
    Integer maxLife;
    
    /**
     * The life the particle starts with
     */
    Integer lifeCurrent;


    /**
     * The initial velocity of the particle
     */
    Vector3d velocity;

    /**
     * The acceleration of the particle
     */
    Float acceleration;

    /**
     * The texture of the particle
     */
    String texture;

    /**
     * The size of the particle
     */
    Float size;

    /**
     * The color of the particle
     */
    Vector3d color;

    /**
     * The particle emitter DO NOT SET THIS IN JSON
     */
    ParticleEmitter emitter;

    /**
     * The parent emitter
     */
    Entity parentEmitter;

    /**
     * Gets the max life of the particle
     * @return The max life
     */
    public Integer getMaxLife(){
        return maxLife;
    }

    /**
     * Gets the starting life of the particle
     * @return The starting life
     */
    public Integer getLifeCurrent(){
        return lifeCurrent;
    }

    /**
     * Gets the starting velocity of the particle
     * @return The starting velocity
     */
    public Vector3d getVelocity(){
        return velocity;
    }

    /**
     * Gets the acceleration of the particle
     * @return The acceleration
     */
    public Float getAcceleration(){
        return acceleration;
    }

    /**
     * Gets the texture of the particle
     * @return The texture
     */
    public String getTexture(){
        return texture;
    }

    /**
     * Gets the name of the particle type
     * @return The name of the particle type
     */
    public String getName(){
        return name;
    }

    /**
     * Gets the size of the particle
     * @return The size of the particle
     */
    public Float getSize(){
        return size;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMaxLife(Integer maxLife) {
        this.maxLife = maxLife;
    }

    public void setLifeCurrent(Integer lifeCurrent) {
        this.lifeCurrent = lifeCurrent;
    }

    public void setVelocity(Vector3d velocity) {
        this.velocity = velocity;
    }

    public void setAcceleration(Float acceleration) {
        this.acceleration = acceleration;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    public void setSize(Float size) {
        this.size = size;
    }

    public void setColor(Vector3d color) {
        this.color = color;
    }

    public Vector3d getColor() {
        return color;
    }

    public ParticleEmitter getEmitter() {
        return emitter;
    }

    public void setEmitter(ParticleEmitter emitter) {
        this.emitter = emitter;
    }

    public Entity getParentEmitter() {
        return parentEmitter;
    }

    public void setParentEmitter(Entity parentEmitter) {
        this.parentEmitter = parentEmitter;
    }
    

}
