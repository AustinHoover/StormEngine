package electrosphere.server.physics.terrain.generation.noise.operators;

import java.util.Arrays;
import java.util.Collection;

import electrosphere.server.physics.terrain.generation.noise.NoiseContainer;
import electrosphere.server.physics.terrain.generation.noise.NoiseSampler;

/**
 * Invokes another sample definition file
 */
public class NoiseOperoatorInvoke implements NoiseContainer {

    /**
     * The name of this module
     */
    public static final String NAME = "Invoke";

    /**
     * The sampler to invoke
     */
    NoiseSampler invokeSampler;

    /**
     * The name of the sampler definition file to invoke
     */
    String target;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double getValue(double SEED, double x, double y, double z) {
        if(invokeSampler != null){
            return invokeSampler.getValue(SEED, x, y, z);
        } else {
            throw new Error("Invoke sampler undefined for " + target);
        }
    }

    /**
     * Sets the sampler to invoke
     * @param invokeSampler The sampler
     */
    public void setInvokeSampler(NoiseSampler invokeSampler){
        this.invokeSampler = invokeSampler;
    }

    /**
     * Gets the invoke sampler
     * @return The sampler
     */
    public NoiseSampler getInvokeSampler(){
        return invokeSampler;
    }

    /**
     * Gets the target this operator wants to invoke
     * @return The target
     */
    public String getTarget(){
        return target;
    }
    
    @Override
    public Collection<NoiseSampler> getChildren(){
        return Arrays.asList(new NoiseSampler[]{
            invokeSampler
        });
    }

}
