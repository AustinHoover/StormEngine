package electrosphere.entity.state.foliage;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;

/**
 * Behavior tree for ambient foliage. Controls regrowing, wind movement, etc
 */
public class AmbientFoliage implements BehaviorTree {

    //the parent entity
    Entity parent;

    //The current offset by wind (used to snap back to 0)
    float windOffset = 0;
    //The current growth level
    float growthLevel = MAX_GROWTH_LEVEL;
    //The increment to increase growth level to until 1
    float growthRate = MAX_GROWTH_LEVEL;
    //the maximum growth level
    static final float MAX_GROWTH_LEVEL = 1;

    /**
     * Constructor
     * @param parent The parent entity
     * @param regrowFactor The initial growth level
     * @param growthRate The growth rate
     */
    private AmbientFoliage(Entity parent, float growthLevel, float growthRate){
        this.growthLevel = growthLevel;
        this.growthRate = growthRate;
        this.parent = parent;
    }

    @Override
    public void simulate(float deltaTime) {
        //increase growth factor if relevant
        if(growthLevel < MAX_GROWTH_LEVEL){
            growthLevel = growthLevel + growthRate;
            if(growthLevel > MAX_GROWTH_LEVEL){
                growthLevel = MAX_GROWTH_LEVEL;
            }
        }
        EntityUtils.getScale(parent).set(growthLevel);

        //rotate to face cameras
        // Vector3f cameraEyeVector = CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera);
        // EntityUtils.getRotation(parent).rotateTo(MathUtils.ORIGIN_VECTOR, new Vector3d(cameraEyeVector));


        //TODO: simulate wind offset
    }

    /**
     * Attaches an ambient foliage behavior tree to the provided entity
     * @param parent The entity
     * @param growthLevel The initial growth level of the foliage
     * @param growthRate The rate of growth of the foliage
     */
    public static void attachAmbientFoliageTree(Entity parent, float growthLevel, float growthRate){
        AmbientFoliage tree = new AmbientFoliage(parent, growthLevel, growthRate);
        parent.putData(EntityDataStrings.FOLIAGE_AMBIENT_TREE, tree);
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(tree);
    }

    /**
     * Gets the ambient foliage tree on a given entity if it exists
     * @param entity The entity
     * @return The ambient foliage tree if it exists, null otherwise
     */
    public static AmbientFoliage getAmbientFoliageTree(Entity entity){
        return (AmbientFoliage) entity.getData(EntityDataStrings.FOLIAGE_AMBIENT_TREE);
    }
    
}
