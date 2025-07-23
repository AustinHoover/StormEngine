package electrosphere.server.physics.terrain.generation.noise.operators;

import java.util.Arrays;
import java.util.Collection;

import electrosphere.server.physics.terrain.generation.noise.NoiseContainer;
import electrosphere.server.physics.terrain.generation.noise.NoiseSampler;

/**
 * Multiplies one noise source by another
 */
public class NoiseOperatorMul implements NoiseContainer {

    /**
     * The name of this module
     */
    public static final String NAME = "Mul";

    /**
     * First sample
     */
    protected NoiseSampler first;

    /**
     * Second sample
     */
    protected NoiseSampler second;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double getValue(double SEED, double x, double y, double z) {
        return first.getValue(SEED, x, y, z) * second.getValue(SEED, x, y, z);
    }

    @Override
    public Collection<NoiseSampler> getChildren(){
        return Arrays.asList(new NoiseSampler[]{
            first,
            second,
        });
    }
    
}
