package electrosphere.server.physics.terrain.generation.noise;

import java.util.Collection;

/**
 * A module that contains other modules
 */
public interface NoiseContainer extends NoiseSampler {
    
    /**
     * Gets all child modules of this one
     * @return All child modules
     */
    public Collection<NoiseSampler> getChildren();

}
