package electrosphere.server.ai.blackboard;

/**
 * Keys for the blackboard
 */
public class BlackboardKeys {
    
    /*
     * Melee ai related
     */
    public static final String MELEE_TARGET = "meleeTarget";

    /**
     * The collection of nearby entities
     */
    public static final String NEARBY_ENTITIES = "nearbyEntities";

    /**
     * The target of the current action
     */
    public static final String ENTITY_TARGET = "target";

    /**
     * The target to move towards
     */
    public static final String MOVE_TO_TARGET = "moveToTarget";

    /**
     * The structure that is being targeted
     */
    public static final String STRUCTURE_TARGET = "structureTarget";

    /**
     * The target position to place a voxel at
     */
    public static final String STRUCTURE_BUILD_TARGET = "structureBuildTarget";

    /**
     * The block type to try to build with
     */
    public static final String BUILD_BLOCK = "buildBlock";

    /**
     * The material currently needed for building the targeted structure
     */
    public static final String BUILDING_MATERIAL_CURRENT = "buildingMaterialCurrent";

    /**
     * The macro object that is being targeted
     */
    public static final String MACRO_TARGET = "macroTarget";

    /**
     * The type of item to try to acquire
     */
    public static final String GOAL_ITEM_ACQUISITION_TARGET = "goalItemAcquisitionTarget";

    /**
     * The type of item to scan the inventory for
     */
    public static final String INVENTORY_CHECK_TYPE = "inventoryCheckType";

    /**
     * Tree that stores the item sourcing
     */
    public static final String ITEM_SOURCING_TREE = "itemSourcingTree";

    /**
     * Sourcing data for the item that is currently being sought after
     */
    public static final String ITEM_SOURCING_DATA = "itemSourcingData";

    /**
     * The category of item to target
     */
    public static final String ITEM_TARGET_CATEGORY = "itemTargetCategory";

    /**
     * The type of entity to try to harvest
     */
    public static final String HARVEST_TARGET_TYPE = "harvestTargetType";

    /**
     * The pathfinding data
     */
    public static final String PATHFINDING_DATA = "pathfindingData";

    /**
     * The pathfinding point
     */
    public static final String PATHFINDING_POINT = "pathfindingPoint";

    /**
     * A point that the entity is targeting
     */
    public static final String POINT_TARGET = "pointTarget";

    /**
     * A town that the entity is targeting
     */
    public static final String TOWN_TARGET = "townTarget";

}
