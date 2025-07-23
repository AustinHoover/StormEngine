package electrosphere.entity;

/**
 * Data strings for entities
 */
public class EntityDataStrings {

    /**
     * Serialization
     */
    public static final String SHOULD_SERIALIZE = "shouldSerialize";
    public static final String SHOULD_SYNCHRONIZE = "shouldSynchronize";
    
    
    /*
    Drawable Entity
    */
    public static final String DATA_STRING_POSITION = "position";
    public static final String DATA_STRING_ROTATION = "rotation";
    public static final String DATA_STRING_SCALE = "scale";
    public static final String DATA_STRING_MODEL_PATH = "modelPath";
    public static final String DATA_STRING_ACTOR = "actor";
    public static final String DATA_STRING_DRAW = "drawFlag";
    public static final String DRAW_SOLID_PASS = "drawSolidPass";
    public static final String DRAW_TRANSPARENT_PASS = "drawTransparentPass";
    public static final String DRAW_OUTLINE = "drawOutline";
    public static final String INSTANCED_ACTOR = "instancedActor";
    public static final String DRAW_INSTANCED = "drawInstanced";
    public static final String TEXTURE_INSTANCED_ACTOR = "textureInstancedActor";
    public static final String HAS_UNIQUE_MODEL = "hasUniqueModel";


    /*
    Instanced Entity
    */
    public static final String INSTANCED_MODEL_ATTRIBUTE = "instancedModelAttribute";
    
    
    /**
     * Entity type
     */
    public static final String ENTITY_TYPE = "entityType"; //ie "creature", "foliage", "terrain", etc
    public static final String ENTITY_SUBTYPE = "entitySubtype"; //ie "human", "woodenCrate", etc
    public static final String COMMON_DATA = "commonData";

    /*
    Terrain Entity
    */
    public static final String TERRAIN_IS_TERRAIN = "terrainEntity";
    public static final String BLOCK_ENTITY = "blockEntity";
    public static final String CORRESPONDING_DRAW_CELL = "correspondingDrawCell";

    /*
     * Fluid Entity
     */
    public static final String FLUID_IS_FLUID = "fluidEntity";
    
    
    /*
    Creature Entity
    */
    public static final String DATA_STRING_CREATURE_CONTROLLER_PLAYER_ID = "creaturePlayerId";
    public static final String CLIENT_MOVEMENT_BT = "clientMovementBT";
    public static final String SERVER_MOVEMENT_BT = "serverMovementBT";
    public static final String TREE_CLIENTGROUNDMOVEMENTTREE = "treeClientGroundMovementTree";
    public static final String TREE_SERVERGROUNDMOVEMENTTREE = "treeServerGroundMovementTree";
    public static final String TREE_CLIENTEDITORMOVEMENTTREE = "treeClientEditorMovementTree";
    public static final String TREE_SERVEREDITORMOVEMENTTREE = "treeServerEditorMovementTree";
    public static final String TREE_CLIENTSPRINTTREE = "treeClientSprintTree";
    public static final String TREE_SERVERSPRINTTREE = "treeServerSprintTree";
    public static final String DATA_STRING_FACING_VECTOR = "facingVector";
    public static final String DATA_STRING_VELOCITY = "velocity";
    public static final String DATA_STRING_ACCELERATION = "acceleration";
    public static final String DATA_STRING_MAX_NATURAL_VELOCITY = "velocityMaxNatural";
    public static final String CREATURE_ATTRIBUTE_VARIANT = "creatureAttributeVariant";
    public static final String CLIENT_ROTATOR_TREE = "clientRotatorTree";
    public static final String SERVER_ROTATOR_TREE = "serverRotatorTree";
    public static final String TREE_CLIENTJUMPTREE = "treeClientJumpTree";
    public static final String TREE_SERVERJUMPTREE = "treeServerJumpTree";
    public static final String FALL_TREE = "fallTree";
    public static final String OBJECT_TEMPLATE = "objectTemplate";
    public static final String FIRST_PERSON_TREE = "firstPersonTree";
    
    
    /*
    All Camera Types
    */
    
    public static final String DATA_STRING_CAMERA_TYPE = "cameraType";
    public static final String DATA_STRING_CAMERA_TYPE_BASIC = "cameraTypeBasic";
    public static final String DATA_STRING_CAMERA_TYPE_ORBIT = "cameraTypeOrbit";
    public static final String DATA_STRING_CAMERA_EYE = "cameraEye";
    public static final String DATA_STRING_CAMERA_CENTER = "cameraCenter";
    public static final String CAMERA_PITCH = "cameraPitch";
    public static final String CAMERA_YAW = "cameraYaw";
    
    
    /*
    Orbital Camera
    */
    public static final String DATA_STRING_CAMERA_ORBIT_TARGET = "cameraOrbitTarget";
    public static final String DATA_STRING_CAMERA_ORBIT_DISTANCE = "cameraOrbitDistance";
    public static final String CAMERA_ORBIT_RADIAL_OFFSET = "cameraOrbitRadialOffset";
    
    /*
    Light Entity
    */
    public static final String DATA_STRING_LIGHT_TYPE = "lightType";
    public static final String DATA_STRING_LIGHT_TYPE_DIRECTIONAL = "lightTypeDirectional";
    public static final String DATA_STRING_LIGHT_TYPE_POINT = "lightTypePoint";
    public static final String DATA_STRING_LIGHT_TYPE_SPOT = "lightTypeSpot";
    public static final String DATA_STRING_LIGHT_AMBIENT = "lightAmbient";
    public static final String DATA_STRING_LIGHT_DIFFUSE = "lightDiffuse";
    public static final String DATA_STRING_LIGHT_SPECULAR = "lightSpecular";
    public static final String DATA_STRING_LIGHT_CONSTANT = "lightConstant";
    public static final String DATA_STRING_LIGHT_LINEAR = "lightLinear";
    public static final String DATA_STRING_LIGHT_QUADRATIC = "lightQuadratic";
    public static final String DATA_STRING_LIGHT_DIRECTION = "lightDirection";
    public static final String DATA_STRING_LIGHT_CUTOFF = "lightCutoff";
    public static final String DATA_STRING_LIGHT_CUTOFF_OUTER = "lightCutoffOuter";
    
    /*
    Anim related
    */
    public static final String ANIM_IDLE = "animIdle";

    /*
    UI Entity
    */
    public static final String DATA_STRING_UI_ELEMENT = "uiEntity";
    public static final String DATA_STRING_UI_ELEMENT_FONT = "uiFont";
    
    /*
    Physics Entity
    */
    public static final String PHYSICS_COLLISION_BODY = "physicsRigidBody";
    public static final String PHYSICS_GEOM = "physicsGeom";
    public static final String PHYSICS_COLLISION_BODY_TRANSFORM = "physicsRigidBodyTransform"; // the transform matrix from origin of entity to origin of physics body
    public static final String PHYSICS_COLLIDABLE = "physicsCollidable";
    public static final String PHYSICS_MODEL_TEMPLATE = "physicsModelTemplate";
    public static final String PHYSICS_MASS = "physicsMass";
    public static final String PHYSICS_ENGINE_AUTHORITATIVE_TRANSFORM = "physicsEngineAuthoritativeTransform"; // The physics engine is authoritative abound transforms of object (eg position, rotation)

    /*
     * Interaction
     */
    public static final String INTERACTION_OFFSET_TRANSFORM = "interactionOffsetTransform";
    public static final String INTERACTION_TEMPLATE = "interactionTemplate";
    public static final String INTERACTION_COLLIDABLE = "interactionCollidable";
    public static final String INTERACTION_BODY = "interactionBody";
    
    /*
    Gravity Entity
    */
    public static final String GRAVITY_ENTITY = "gravityEntity";
    public static final String TREE_CLIENTGRAVITY = "treeClientGravity";
    public static final String TREE_SERVERGRAVITY = "treeServerGravity";
    
    /*
    Collision Entity
    */
    public static final String COLLISION_ENTITY_ID = "collisionEntityId";
    public static final String DATA_STRING_COLLISION_ENTITY = "collisionEntity";
    public static final String DATA_STRING_COLLISION_ENTITY_TYPE_SPHERE = "collisionSphere";
    
    
    public static final String COLLISION_ENTITY_COLLISION_OBJECT = "collisionEntityBulletObject";
    public static final String COLLISION_ENTITY_COLLIDABLE = "collisionEntityCollidable";
    public static final String COLLISION_ENTITY_PARENT = "collisionEntityParent";
    
    public static final String COLLISION_ENTITY_TYPE_PLANE = "collisionTypePlane";
    public static final String COLLISION_ENTITY_TYPE_CUBE = "collisionTypeCube";
    public static final String COLLISION_ENTITY_TYPE_CYLINDER = "collisionTypeCylinder";
    
    
    public static final String COLLISION_ENTITY_DATA_TYPE_HIT = "collisionDataTypeHit";
    public static final String COLLISION_ENTITY_DATA_TYPE_HURT = "collisionDataTypeHurt";
    
    public static final String COLLISION_ENTITY_DATA_PARENT = "collisionDataParent";
    
    public static final String SERVER_COLLIDABLE_TREE = "serverCollidableTree";
    public static final String CLIENT_COLLIDABLE_TREE = "clientCollidableTree";
    
    public static final String HITBOX_DATA = "hitboxData";
    public static final String HITBOX_ASSOCIATED_LIST = "hitboxAssociatedList";
    public static final String HURTBOX_ASSOCIATED_LIST = "hurtboxAssociatedList";
    
    
    /*
    Attach Entity
    */
    public static final String ATTACH_ENTITY_IS_ATTACHED = "attachIsAttached";
    public static final String ATTACH_PARENT = "attachParent";
    public static final String ATTACH_TARGET_BONE = "attachTargetBone"; //Attaches to a specific bone of the entity
    public static final String ATTACH_TARGET_BASE = "attachTargetBase"; //Attaches to the base of the entity (should be the same as getPosition(entity))
    public static final String ATTACH_CHILDREN_LIST = "attachChildrenList";
    public static final String ATTACH_ROTATION_OFFSET = "attachRotationOffset";
    public static final String ATTACH_POSITION_OFFSET = "attachPositionOffset";
    public static final String ATTACH_TRANSFORM = "attachTransform";
    
    /*
    Item Entity
    */
    public static final String ITEM_IS_WEAPON = "itemIsWeapon";
    public static final String ITEM_IS_ARMOR = "itemIsArmor";
    public static final String ITEM_EQUIP_WHITELIST = "itemEquipWhitelist";
    public static final String ITEM_EQUIP_CLASS = "itemEquipClass";
    public static final String ITEM_ICON = "itemIcon";
    public static final String ITEM_IN_WORLD_REPRESENTATION = "itemInWorldRepresentation";
    public static final String ITEM_WEAPON_CLASS = "itemWeaponClass";
    public static final String ITEM_WEAPON_DATA_RAW = "itemWeaponDataRaw";
    public static final String ITEM_IS_IN_INVENTORY = "itemIsInInventory";
    public static final String ITEM_CONTAINING_PARENT = "itemContainingParent";
    public static final String TREE_SERVERCHARGESTATE = "treeServerChargeState";
    public static final String TREE_CLIENTCHARGESTATE = "treeClientChargeState";

    /**
     * Data for placing fabs with an item
     */
    public static final String ITEM_FAB_DATA = "itemFabData";
    
    
    /*
    Attack behavior tree
    */
    public static final String TREE_CLIENTATTACKTREE = "treeClientAttackTree";
    public static final String TREE_SERVERATTACKTREE = "treeServerAttackTree";
    
    public static final String ATTACK_MOVE_TYPE_ACTIVE = "attackMoveTypeActive";
    public static final String ATTACK_MOVE_TYPE_MELEE_SWING_ONE_HAND = "MELEE_WEAPON_SWING_ONE_HAND";
    public static final String ATTACK_MOVE_TYPE_MELEE_SWING_TWO_HAND = "MELEE_WEAPON_SWING_TWO_HAND";
    public static final String ATTACK_MOVE_TYPE_BOW_TWO_HAND = "RANGED_WEAPON_BOW_TWO_HAND";
    public static final String ATTACK_MOVE_UNARMED = "ATTACK_MOVE_UNARMED";

    /**
     * Common flags
     */
    public static final String INTERACTABLE = "interactable";

    /**
     * Ambient audio
     */
    public static final String CLIENT_AMBIENT_AUDIO_TREE = "clientAmbientAudioTree";

    /*
     * Shooter tree
     */
    public static final String SHOOTER_TREE = "shooterTree";

    /*
     * Projectile/Projectile tree
     */
    public static final String PROJECTILE_TREE = "projectileTree";
    
    /*
    Health System
    */
    public static final String LIFE_STATE = "lifeState";
    
    /*
    idle behavior tree
    */
    public static final String TREE_IDLE = "treeIdle";
    public static final String TREE_SERVERIDLE = "treeServerIdle";
    
    /*
    particle behavior tree
    */
    public static final String IS_PARTICLE = "isParticle";
    public static final String TREE_CLIENTPARTICLETREE = "treeClientParticleTree";

    
    /*
    Foliage entity
    */
    public static final String FOLIAGE_TYPE = "foliageType";
    public static final String FOLIAGE_AMBIENT_TREE = "foliageAmbientTree";
    public static final String FOLIAGE_IS_SEEDED = "foliageIsSeeded";
    
    /*
    Equip state
    */
    public static final String TREE_CLIENTEQUIPSTATE = "treeClientEquipState";
    public static final String EQUIP_INVENTORY = "equipInventory";
    public static final String TREE_SERVEREQUIPSTATE = "treeServerEquipState";
    public static final String TREE_CLIENTTOOLBARSTATE = "treeClientToolbarState";
    public static final String TREE_SERVERTOOLBARSTATE = "treeServerToolbarState";

    /*
    Client-only components
     */
    public static final String TREE_CLIENTLIGHTSTATE = "treeClientLightState";
    public static final String TREE_CLIENTPARTICLEEMITTERSTATE = "treeClientParticleEmitterState";

    /*
    Inventory in general
    */
    public static final String NATURAL_INVENTORY = "inventoryNatural";
    public static final String INVENTORY_TOOLBAR = "inventoryToolbar";
    public static final String CLIENT_INVENTORY_STATE = "clientInventoryState";
    public static final String SERVER_INVENTORY_STATE = "serverInventoryState";

    /*
    Iron sight
    */
    public static final String IRON_SIGHT_TREE = "ironSightTree";

    /*
     * Block trees
     */
    public static final String TREE_CLIENTBLOCKTREE = "treeClientBlockTree";
    public static final String TREE_SERVERBLOCKTREE = "treeServerBlockTree";

    /*
    AI stuff
    */
    public static final String VIEW_PITCH = "aiViewPitch";
    public static final String AI = "ai";

    /**
     * Life State
     */
    public static final String TREE_CLIENTLIFETREE = "treeClientLifeTree";
    public static final String TREE_SERVERLIFETREE = "treeServerLifeTree";

    /**
     * Pose actor
     */
    public static final String POSE_ACTOR = "poseActor";

    /**
     * Server-specific btrees
     */
    public static final String TREE_SERVERPLAYERVIEWDIR = "treeServerPlayerViewDir";
    public static final String TREE_SERVERCHARACTERDATA = "treeServerCharacterData";

    /**
     * Physics synchronization
     */
    public static final String TREE_SERVERPHYSICSSYNCTREE = "treeServerPhysicsSyncTree";
    public static final String TREE_CLIENTPHYSICSSYNCTREE = "treeClientPhysicsSyncTree";

    /**
     * Always upright tree
     */
    public static final String TREE_CLIENTALWAYSUPRIGHTTREE = "treeClientAlwaysUprightTree";
    public static final String TREE_SERVERALWAYSUPRIGHTTREE = "treeServerAlwaysUprightTree";

    /**
     * Walk tree
     */
    public static final String TREE_CLIENTWALKTREE = "treeClientWalkTree";
    public static final String TREE_SERVERWALKTREE = "treeServerWalkTree";

    /**
     * Weapon stance
     */
    public static final String TREE_CLIENTSTANCECOMPONENT = "treeClientStanceComponent";
    public static final String TREE_SERVERSTANCECOMPONENT = "treeServerStanceComponent";

    /**
     * Furniture
     */
    public static final String TREE_SERVERDOOR = "treeServerDoor";
    public static final String TREE_CLIENTDOOR = "treeClientDoor";

    /**
     * Growth
     */
    public static final String TREE_SERVERGROWTH = "treeServerGrowth";
    public static final String TREE_CLIENTGROWTH = "treeClientGrowth";

    /**
     * LOD component
     */
    public static final String TREE_CLIENTLODTREE = "treeClientLODTree";
    public static final String TREE_SERVERLODTREE = "treeServerLODTree";

    /**
     * Loot pool
     */
    public static final String LOOT_POOL = "lootPool";
    
    /*
    Entity categories
    */
    public static final int ENTITY_CATEGORY_CREATURE = 0;
    public static final int ENTITY_CATEGORY_ITEM = 1;
    public static final int ENTITY_CATEGORY_STRUCTURE = 2;
    
}
