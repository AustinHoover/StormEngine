package electrosphere.server.physics.terrain.generation.noise.operators;

import java.util.Arrays;
import java.util.Collection;

import electrosphere.server.physics.terrain.generation.noise.NoiseContainer;
import electrosphere.server.physics.terrain.generation.noise.NoiseSampler;

/**
 * The warp operator
 */
public class NoiseOperatorDomainWarp implements NoiseContainer {

    /**
     * The name of this module
     */
    public static final String NAME = "DomainWarp";

    /**
     * The sampler to pull the x value from
     */
    protected NoiseSampler x;

    /**
     * The sampler to pull the y value from
     */
    protected NoiseSampler y;

    /**
     * The sampler to pull the z value from
     */
    protected NoiseSampler z;

    /**
     * The amplitude of the warp to apply
     */
    protected double amplitude = 1.0f;

    /**
     * The sampler to pull from for the final emitted value
     */
    protected NoiseSampler source;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double getValue(double SEED, double x, double y, double z) {
        double sampleX = x;
        if(this.x != null){
            sampleX = sampleX + this.x.getValue(SEED, x, y, z) * amplitude;
        }
        double sampleY = y;
        if(this.y != null){
            sampleY = sampleY + this.y.getValue(SEED, x, y, z) * amplitude;
        }
        double sampleZ = z;
        if(this.z != null){
            sampleZ = sampleZ + this.z.getValue(SEED, x, y, z) * amplitude;
        }
        return this.source.getValue(SEED, sampleX, sampleY, sampleZ);
    }

    @Override
    public Collection<NoiseSampler> getChildren(){
        return Arrays.asList(new NoiseSampler[]{
            x,
            y,
            z,
            source,
        });
    }
    
}
