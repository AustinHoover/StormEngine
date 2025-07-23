package electrosphere.data.entity.foliage;

/**
 * Data for creating branches on a procedural tree
 */
public class ProceduralTreeBranchModel {
    
    //how quickly do the limbs shrink
    float limbScalarFalloffFactor;
    
    //How small are the terminal limbs, basically how small can it get before it stops generating
    float minimumLimbScalar;

    //The maximum a single branch can disperse from the current line
    float maximumLimbDispersion;

    //The minimum a single branch must disperse from the current line
    float minimumLimbDispersion;

    //The minimum number of branch forks per iteration
    int minimumNumberForks;

    //The maximum number of branch forks per iteration
    int maximumNumberForks;

    //The maximum number of linear segments for the branch (ie how many times can a function recurse)
    int maximumBranchSegments;

    //The rate at which number of branch segments from the current trunk falls off over time
    float maxBranchSegmentFalloffFactor;

    //The minimum segment number required to start spawning leaves
    int minimumSegmentToSpawnLeaves;

    /**
     * how quickly do the limbs shrink
     * @return
     */
    public float getLimbScalarFalloffFactor(){
        return limbScalarFalloffFactor;
    }

    /**
     * How small are the terminal limbs
     * @return
     */
    public float getMinimumLimbScalar(){
        return minimumLimbScalar;
    }

    /**
     * The maximum a single branch can disperse from the current line
     * @return
     */
    public float getMaximumLimbDispersion(){
        return maximumLimbDispersion;
    }
    
    /**
     * The minimum a single branch must disperse from the current line
     * @return
     */
    public float getMinimumLimbDispersion(){
        return minimumLimbDispersion;
    }

    /**
     * The minimum number of branch forks per iteration
     * @return
     */
    public int getMinimumNumberForks(){
        return minimumNumberForks;
    }

    /**
     * The maximum number of branch forks per iteration
     * @return
     */
    public int getMaximumNumberForks(){
        return maximumNumberForks;
    }

    /**
     * The maximum number of linear segments for the branch (ie how many times can a function recurse)
     * @return
     */
    public int getMaximumBranchSegments(){
        return maximumBranchSegments;
    }

    /**
     * The rate at which number of branch segments from the current trunk falls off over time
     * @return
     */
    public float getMaxBranchSegmentFalloffFactor(){
        return maxBranchSegmentFalloffFactor;
    }

    /**
     * The minimum segment number required to start spawning leaves
     * @return
     */
    public int getMinimumSegmentToSpawnLeaves(){
        return minimumSegmentToSpawnLeaves;
    }

    public void setLimbScalarFalloffFactor(float limbScalarFalloffFactor) {
        this.limbScalarFalloffFactor = limbScalarFalloffFactor;
    }

    public void setMinimumLimbScalar(float minimumLimbScalar) {
        this.minimumLimbScalar = minimumLimbScalar;
    }

    public void setMaximumLimbDispersion(float maximumLimbDispersion) {
        this.maximumLimbDispersion = maximumLimbDispersion;
    }

    public void setMinimumLimbDispersion(float minimumLimbDispersion) {
        this.minimumLimbDispersion = minimumLimbDispersion;
    }

    public void setMinimumNumberForks(int minimumNumberForks) {
        this.minimumNumberForks = minimumNumberForks;
    }

    public void setMaximumNumberForks(int maximumNumberForks) {
        this.maximumNumberForks = maximumNumberForks;
    }

    public void setMaximumBranchSegments(int maximumBranchSegments) {
        this.maximumBranchSegments = maximumBranchSegments;
    }

    public void setMaxBranchSegmentFalloffFactor(float maxBranchSegmentFalloffFactor) {
        this.maxBranchSegmentFalloffFactor = maxBranchSegmentFalloffFactor;
    }

    public void setMinimumSegmentToSpawnLeaves(int minimumSegmentToSpawnLeaves) {
        this.minimumSegmentToSpawnLeaves = minimumSegmentToSpawnLeaves;
    }

}
