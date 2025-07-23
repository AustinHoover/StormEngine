package electrosphere.data.entity.foliage;

import electrosphere.data.entity.collidable.CollidableTemplate;

/**
 * Describes characteristics about a type of tree (how do the limbs dispere, where to the leaves start growing, how sturdy is it, etc)
 */
public class TreeModel {

    //trunk data
    ProceduralTreeTrunkModel trunkModel;

    //branch data
    ProceduralTreeBranchModel branchModel;

    //the minimum distance along a given segment to start spawning leaves at
    float minBranchHeightToStartSpawningLeaves;

    //the maximum distance along a given segment to start spawning leaves at
    float maxBranchHeightToStartSpawningLeaves;

    //The increment along the branch segment to spawn leaves at
    float leafIncrement;

    //the minimum leaves to spawn per leaf point
    int minLeavesToSpawnPerPoint;

    //the maximum leaves to spawn per leaf point
    int maxLeavesToSpawnPerPoint;

    //The distance from the central line of a branch to spawn a leaf at
    float leafDistanceFromCenter;

    //The rigid body definition for a full scale tree branch
    CollidableTemplate physicsBody;


    //
    //Tree branch sway factors
    //

    //How much can the peel vary hypothetically while it's swinging
    double peelVariance;

    //a minimum amount of peel (For instance forcing weather to cause large motions in the branches)
    double peelMinimum;

    //The value of the sigmoid controlling branch way speed over time (check branch btree for details)
    double swaySigmoidFactor;

    //The minimum number of frames that a branch should sway for
    int minimumSwayTime;

    //The maximum amount of frames that can be added to minimumSwayTime to increase the time of a single sway
    int swayTimeVariance;

    //How much can the yaw vary hypothetically while it's swinging
    double yawVariance;

    //a minimum amount of yaw (For instance forcing weather to cause large motions in the branches)
    double yawMinimum;

    //The minimum scalar of a branch to generate a sway behavior tree
    float minimumScalarToGenerateSwayTree;

    //The maximum scalar of a branch to generate a sway behavior tree*
    float maximumScalarToGenerateSwayTree;

    //The model for the trunk of the tree
    String trunkModelPath;

    //the model for a leaf blob
    String leafModelPath;

    //The height of a single branch, should be the height of the model
    float branchHeight;

    /**
     * Gets the data model for the trunk
     * @return The data model for the trunk
     */
    public ProceduralTreeTrunkModel getTrunkModel(){
        return trunkModel;
    }

    /**
     * Gets the data model for branches
     * @return The data model for branches
     */
    public ProceduralTreeBranchModel getBranchModel(){
        return branchModel;
    }

    /**
     * the minimum distance along a given segment to start spawning leaves at
     * @return
     */
    public float getMinBranchHeightToStartSpawningLeaves(){
        return minBranchHeightToStartSpawningLeaves;
    }

    /**
     * the maximum distance along a given segment to start spawning leaves at
     * @return
     */
    public float getMaxBranchHeightToStartSpawningLeaves(){
        return maxBranchHeightToStartSpawningLeaves;
    }

    /**
     * The increment along the branch segment to spawn leaves at
     * @return
     */
    public float getLeafIncrement(){
        return leafIncrement;
    }

    /**
     * the minimum leaves to spawn per leaf point
     * @return
     */
    public int getMinLeavesToSpawnPerPoint(){
        return minLeavesToSpawnPerPoint;
    }

    /**
     * the maximum leaves to spawn per leaf point
     * @return
     */
    public int getMaxLeavesToSpawnPerPoint(){
        return maxLeavesToSpawnPerPoint;
    }

    /**
     * The distance from the central line of a branch to spawn a leaf at
     * @return
     */
    public float getLeafDistanceFromCenter(){
        return leafDistanceFromCenter;
    }

    /**
     * How much can the peel vary hypothetically while it's swinging
     * @return
     */
    public double getPeelVariance(){
        return this.peelVariance;
    }

    /**
     * a minimum amount of peel (For instance forcing weather to cause large motions in the branches)
     * @return
     */
    public double getPeelMinimum(){
        return this.peelMinimum;
    }

    /**
     * The value of the sigmoid controlling branch way speed over time (check branch btree for details)
     * @return
     */
    public double getSwaySigmoidFactor(){
        return this.swaySigmoidFactor;
    }

    /**
     * The minimum number of frames that a branch should sway for
     * @return
     */
    public int getMinimumSwayTime(){
        return this.minimumSwayTime;
    }

    /**
     * The maximum amount of frames that can be added to minimumSwayTime to increase the time of a single sway
     * @return
     */
    public int getSwayTimeVariance(){
        return this.swayTimeVariance;
    }

    /**
     * How much can the yaw vary hypothetically while it's swinging
     * @return
     */
    public double getYawVariance(){
        return this.yawVariance;
    }

    /**
     * a minimum amount of yaw (For instance forcing weather to cause large motions in the branches)
     * @return
     */
    public double getYawMinimum(){
        return this.yawMinimum;
    }

    /**
     * The minimum scalar of a branch to generate a sway behavior tree
     * @return
     */
    public float getMinimumScalarToGenerateSwayTree(){
        return this.minimumScalarToGenerateSwayTree;
    }

    /**
     * The maximum scalar of a branch to generate a sway behavior tree
     * @return
     */
    public float getMaximumScalarToGenerateSwayTree(){
        return this.maximumScalarToGenerateSwayTree;
    }

    /**
     * The rigid body definition for a full scale tree branch
     * @return
     */
    public CollidableTemplate getPhysicsBody(){
        return this.physicsBody;
    }

    /**
     * The model for the trunk of the tree
     * @return
     */
    public String getTrunkModelPath(){
        return trunkModelPath;
    }

    /**
     * the model for a leaf blob
     * @return
     */
    public String getLeafModelPath(){
        return leafModelPath;
    }

    /**
     * The height of a single branch, should be the height of the model
     * @return
     */
    public float getBranchHeight(){
        return branchHeight;
    }

    public void setMinBranchHeightToStartSpawningLeaves(float minBranchHeightToStartSpawningLeaves) {
        this.minBranchHeightToStartSpawningLeaves = minBranchHeightToStartSpawningLeaves;
    }

    public void setMaxBranchHeightToStartSpawningLeaves(float maxBranchHeightToStartSpawningLeaves) {
        this.maxBranchHeightToStartSpawningLeaves = maxBranchHeightToStartSpawningLeaves;
    }

    public void setLeafIncrement(float leafIncrement) {
        this.leafIncrement = leafIncrement;
    }

    public void setMinLeavesToSpawnPerPoint(int minLeavesToSpawnPerPoint) {
        this.minLeavesToSpawnPerPoint = minLeavesToSpawnPerPoint;
    }

    public void setMaxLeavesToSpawnPerPoint(int maxLeavesToSpawnPerPoint) {
        this.maxLeavesToSpawnPerPoint = maxLeavesToSpawnPerPoint;
    }

    public void setLeafDistanceFromCenter(float leafDistanceFromCenter) {
        this.leafDistanceFromCenter = leafDistanceFromCenter;
    }

    public void setPhysicsBody(CollidableTemplate physicsBody) {
        this.physicsBody = physicsBody;
    }

    public void setPeelVariance(double peelVariance) {
        this.peelVariance = peelVariance;
    }

    public void setPeelMinimum(double peelMinimum) {
        this.peelMinimum = peelMinimum;
    }

    public void setSwaySigmoidFactor(double swaySigmoidFactor) {
        this.swaySigmoidFactor = swaySigmoidFactor;
    }

    public void setMinimumSwayTime(int minimumSwayTime) {
        this.minimumSwayTime = minimumSwayTime;
    }

    public void setSwayTimeVariance(int swayTimeVariance) {
        this.swayTimeVariance = swayTimeVariance;
    }

    public void setYawVariance(double yawVariance) {
        this.yawVariance = yawVariance;
    }

    public void setYawMinimum(double yawMinimum) {
        this.yawMinimum = yawMinimum;
    }

    public void setMinimumScalarToGenerateSwayTree(float minimumScalarToGenerateSwayTree) {
        this.minimumScalarToGenerateSwayTree = minimumScalarToGenerateSwayTree;
    }

    public void setMaximumScalarToGenerateSwayTree(float maximumScalarToGenerateSwayTree) {
        this.maximumScalarToGenerateSwayTree = maximumScalarToGenerateSwayTree;
    }

    public void setTrunkModelPath(String trunkModelPath) {
        this.trunkModelPath = trunkModelPath;
    }

    public void setLeafModelPath(String leafModelPath) {
        this.leafModelPath = leafModelPath;
    }

    public void setBranchHeight(float branchHeight) {
        this.branchHeight = branchHeight;
    }

    

}
