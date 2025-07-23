package electrosphere.data.entity.particle;

import java.util.List;

/**
 * File that defines all particles
 */
public class ParticleDefinition {
    
    /**
     * The particle data
     */
    List<ParticleData> data;

    /**
     * Gets the particle data
     * @return The particle data
     */
    public List<ParticleData> getData(){
        return data;
    }

}
