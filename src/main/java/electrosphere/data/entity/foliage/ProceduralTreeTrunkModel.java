package electrosphere.data.entity.foliage;

/**
 * The data model for the trunk of the procedural tree
 */
public class ProceduralTreeTrunkModel {
    
    //how quickly does the trunk shrink
    float trunkScalarFalloffFactor;
    
    //How small are the trunk segments, basically how small can it get before it stops generating
    float minimumTrunkScalar;

    //The minimum number of branch forks per iteration
    int minimumBranches;

    //The maximum number of branch forks per iteration
    int maximumBranches;

    //if true, always generates a central trunk
    boolean centralTrunk;

    //The maximum number of linear segments for the trunk (ie how many times can a function recurse)
    int maximumTrunkSegments;

    //The branch count to stop generating physics for each branch
    int physicsCutoff;


    /**
     * Gets the scalar falloff factor (how fast does the trunk shrink)
     * @return The scalar falloff factor
     */
    public float getTrunkScalarFalloffFactor(){
        return trunkScalarFalloffFactor;
    }

    /**
     * Gets the minimum scalar for a trunk segment
     * @return The minimum scalar
     */
    public float getMinimumTrunkScalar(){
        return minimumTrunkScalar;
    }

    /**
     * if true, always generates a central trunk
     * @return
     */
    public boolean getCentralTrunk(){
        return centralTrunk;
    }

    /**
     * The maximum number of linear segments for the trunk (ie how many times can a function recurse)
     * @return
     */
    public int getMaximumTrunkSegments(){
        return maximumTrunkSegments;
    }

    /**
     * The branch count to stop generating physics for each branch
     * @return
     */
    public int getPhysicsCutoff(){
        return this.physicsCutoff;
    }

    public void setCentralTrunk(boolean centralTrunk) {
        this.centralTrunk = centralTrunk;
    }

    public void setMaximumTrunkSegments(int maximumTrunkSegments) {
        this.maximumTrunkSegments = maximumTrunkSegments;
    }

    public void setPhysicsCutoff(int physicsCutoff) {
        this.physicsCutoff = physicsCutoff;
    }

}
