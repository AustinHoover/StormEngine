package electrosphere.server.physics.terrain.generation.noise.operators;

import electrosphere.server.physics.terrain.generation.noise.NoiseSampler;

/**
 * Gets a constant value
 */
public class NoiseOperatorConst implements NoiseSampler {

    /**
     * The name of this module
     */
    public static final String NAME = "Const";

    /**
     * The value
     */
    double value;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double getValue(double SEED, double x, double y, double z) {
        return value;
    }
    
}
