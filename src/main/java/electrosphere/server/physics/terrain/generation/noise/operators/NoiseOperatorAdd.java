package electrosphere.server.physics.terrain.generation.noise.operators;

import java.util.Arrays;
import java.util.Collection;

import electrosphere.server.physics.terrain.generation.noise.NoiseContainer;
import electrosphere.server.physics.terrain.generation.noise.NoiseSampler;

public class NoiseOperatorAdd implements NoiseContainer {

    /**
     * The name of this module
     */
    public static final String NAME = "Add";

    /**
     * The first value to pull from
     */
    protected NoiseSampler first;

    /**
     * The second value to pull from
     */
    protected NoiseSampler second;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double getValue(double SEED, double x, double y, double z) {
        return first.getValue(SEED, x, y, z) + second.getValue(SEED, x, y, z);
    }

    @Override
    public Collection<NoiseSampler> getChildren(){
        return Arrays.asList(new NoiseSampler[]{
            first,
            second,
        });
    }
    
}