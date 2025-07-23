package electrosphere.server.physics.terrain.generation.noise.operators;

import java.util.Arrays;
import java.util.Collection;

import electrosphere.server.physics.terrain.generation.noise.NoiseContainer;
import electrosphere.server.physics.terrain.generation.noise.NoiseSampler;
import io.github.studiorailgun.MathUtils;

/**
 * Clamps a noise value
 */
public class NoiseOperatorClamp implements NoiseContainer {

    /**
     * The name of this module
     */
    public static final String NAME = "Clamp";

    /**
     * The minimum value to clamp to
     */
    protected NoiseSampler min;

    /**
     * The maximum value to clamp to
     */
    protected NoiseSampler max;

    /**
     * The source to clamp
     */
    protected NoiseSampler source;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double getValue(double SEED, double x, double y, double z) {
        return MathUtils.clamp(source.getValue(SEED, x, y, z), min.getValue(SEED, x, y, z), max.getValue(SEED, x, y, z));
    }

    @Override
    public Collection<NoiseSampler> getChildren(){
        return Arrays.asList(new NoiseSampler[]{
            min,
            max,
            source,
        });
    }
    


}
