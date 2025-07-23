package electrosphere.server.physics.terrain.generation.heightmap;

public class EmptySkyGen implements HeightmapGenerator {

    /**
     * The seed of the generator
     */
    long seed = 0;

    @Override
    public float getHeight(long SEED, double x, double y) {
        return 0;
    }

    @Override
    public String getTag() {
        return "empty";
    }

    @Override
    public void setSeed(long seed){
        this.seed = seed;
    }
    
}
